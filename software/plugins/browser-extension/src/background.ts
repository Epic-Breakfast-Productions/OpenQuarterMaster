import { amazonUrl, mappingKey, positiveQuantity, productKey, purchaseKey, targetKey, validateCapture, validateSettings, type Capture, type Mapping, type Purchase, type Settings } from './domain';
import { ApiError, authenticate, OqmClient } from './oqm/client';
import { setDeliveryStatus, syncLine } from './oqm/sync';
import { credentialKey, getSecret, initStorage, readState, saveState } from './store';

let chain: Promise<unknown> = Promise.resolve();
function serialized<T>(work: () => Promise<T>): Promise<T> {
  const result = chain.then(work, work); chain = result.catch(() => {}); return result;
}
const ready = initStorage();
async function client(settings: Settings): Promise<OqmClient> {
  const key = credentialKey(settings);
  return new OqmClient(settings, async () => {
    const { tokenCache } = await chrome.storage.session.get<{tokenCache?: {key:string;expiresAt:number;token:string}}>('tokenCache');
    if (tokenCache?.key === key && tokenCache.expiresAt > Date.now()) return tokenCache.token;
    const token = await authenticate(settings, await getSecret(key));
    await chrome.storage.session.set({ tokenCache: { key, ...token } });
    return token.token;
  });
}
async function badge() {
  const s = await readState();
  const n = Object.values(s.purchases).filter(p => !['done', 'ignored'].includes(p.status)).length;
  await chrome.action.setBadgeText({ text: n ? String(n) : '' });
  await chrome.action.setBadgeBackgroundColor({ color: '#b75727' });
}
async function openReview(focus = true) {
  const { reviewWindow } = await chrome.storage.session.get<{reviewWindow?:number}>('reviewWindow');
  if (reviewWindow) {
    try { await chrome.windows.get(reviewWindow); if (focus) await chrome.windows.update(reviewWindow, { focused: true }); return; } catch { /* Window closed. */ }
  }
  const win = await chrome.windows.create({ url: chrome.runtime.getURL('app.html'), type: 'popup', width: 1100, height: 830, focused: focus });
  if (win?.id) await chrome.storage.session.set({ reviewWindow: win.id });
}
async function ingest(raw: Capture, tabId: number, manual: boolean) {
  const c = validateCapture(raw);
  const s = await readState();
  const { carts = {} } = await chrome.storage.session.get<{carts?:Record<string,{capture:Capture;at:number}>}>('carts');
  if (c.kind === 'cart') {
    await chrome.storage.session.set({ carts: { ...carts, [tabId]: { capture: c, at: Date.now() } } });
    return { message: 'Cart remembered. Nothing is imported until a purchase is confirmed.' };
  }
  if (c.kind === 'order-detail' && !manual) return { message: 'Order detail imports require Scan this page.' };
  const recentCart = carts[tabId] && Date.now() - carts[tabId].at < 60 * 60 * 1000 ? carts[tabId].capture : undefined;
  if (recentCart && c.products.length) {
    const signature = (capture: Capture) => capture.products.map(p => `${productKey(p)}:${p.quantity}`).sort().join('|');
    if (signature(recentCart) !== signature(c)) c.warnings.push('The receipt differs from your recent cart. Review the purchased products and quantities; the cart may include unpurchased items.');
  }
  if (!c.products.length && carts[tabId] && Date.now() - carts[tabId].at < 60 * 60 * 1000) {
    c.products = carts[tabId].capture.products;
    c.warnings.push('These products came from your earlier cart, not the receipt. Confirm every purchased line and quantity.');
  }
  if (!c.orderId || !c.products.length) {
    await chrome.storage.session.set({ draft: c });
    await chrome.action.setBadgeText({ text: '?' });
    if (s.settings.openReview) await openReview(false);
    return { message: 'Receipt needs more detail. Open Review to enter the order ID or products.' };
  }
  const key = purchaseKey(c);
  if (carts[tabId]) { delete carts[tabId]; await chrome.storage.session.set({ carts }); }
  const existing = s.purchases[key];
  if (existing?.status === 'ignored') return { message: 'This order was dismissed. Nothing was imported.' };
  if (existing?.status === 'done') {
    const additional = c.products.some(product => !existing.lines.some(line => productKey(line.product) === productKey(product)));
    if (!additional) return { message: 'This order was already handled. No duplicate was created.' };
    existing.approved = false;
    c.warnings.push('Amazon revealed additional products for an imported order. Review the new lines; previously imported lines will not be imported again.');
  }
  if (!existing && Object.keys(s.purchases).length >= 1000) throw new Error('Local history is full (1,000 orders). Export a backup before clearing completed local history.');
  const lines = c.products.map(product => ({ product, mapping: s.mappings[mappingKey(s.settings, product)], phase: 'pending' as const }));
  const p: Purchase = existing ?? {
    key, orderId: c.orderId, sourceUrl: c.sourceUrl, capturedAt: new Date().toISOString(), target: { ...s.settings },
    lines, warnings: c.warnings, approved: false, status: 'review', attempts: 0,
  };
  if (existing) {
    for (const line of lines) {
      const previous = p.lines.find(l => productKey(l.product) === productKey(line.product));
      if (!previous) { p.lines.push(line); p.approved = false; }
      else if (previous.phase === 'pending' && !p.approved) previous.product = line.product;
    }
    p.warnings = [...new Set([...p.warnings, ...c.warnings])];
  }
  const sameTarget = targetKey(p.target) === targetKey(s.settings);
  const canAuto = sameTarget && c.kind === 'confirmation' && !p.warnings.length && p.lines.every(l => l.mapping && positiveQuantity(l.product.quantity));
  if (!existing && s.settings.autoSync && canAuto) p.approved = true;
  p.status = p.approved ? 'queued' : 'review'; s.purchases[key] = p;
  await saveState(s); await badge();
  if (p.status === 'review' && !existing && s.settings.openReview) await openReview(false);
  if (p.status === 'queued') void serialized(runQueue);
  return { message: p.approved ? 'Known products queued as incoming purchases.' : 'New or uncertain products need your review.' };
}
async function runQueue() {
  const s = await readState();
  const deadline = Date.now() + 20_000;
  for (const p of Object.values(s.purchases)) {
    if (!p.approved || !['queued', 'syncing'].includes(p.status) || (p.nextAttempt && p.nextAttempt > Date.now())) continue;
    if (Date.now() > deadline) break;
    p.status = 'syncing'; p.error = undefined; await saveState(s);
    try {
      const api = await client(p.target);
      for (const line of p.lines) {
        if (line.excluded || line.phase === 'done') continue;
        if (Date.now() > deadline) break;
        try {
          await syncLine(api, p, line, () => saveState(s));
          if (line.mapping) s.mappings[mappingKey(p.target, line.product)] = line.mapping;
          await saveState(s);
        } catch (e) { line.error = errorText(e); throw e; }
      }
      p.status = p.lines.every(l => l.excluded || l.phase === 'done') ? 'done' : 'queued';
      p.attempts = 0; p.nextAttempt = undefined;
    } catch (e) {
      p.error = errorText(e); p.attempts++;
      const transient = e instanceof ApiError && (e.status === 0 || e.status === 429 || e.status >= 500);
      p.status = transient && p.attempts < 5 ? 'queued' : 'error';
      p.nextAttempt = transient ? Date.now() + Math.min(60, 2 ** p.attempts) * 60_000 : undefined;
    }
    await saveState(s);
  }
  await badge();
}
function errorText(e: unknown) { return e instanceof Error ? e.message : 'Operation failed.'; }
function order(s: Awaited<ReturnType<typeof readState>>, key: unknown): Purchase {
  if (typeof key !== 'string' || !s.purchases[key]) throw new Error('Order not found.');
  return s.purchases[key];
}
async function command(message: Record<string, any>, sender: chrome.runtime.MessageSender): Promise<unknown> {
  await ready;
  // Extension pages opened in tabs also have sender.tab; URL distinguishes them.
  const extensionPage = sender.url?.startsWith(chrome.runtime.getURL('')) === true;
  const page = !!sender.tab && !extensionPage;
  if (sender.id !== chrome.runtime.id) throw new Error('Unknown message sender.');
  if (page) {
    const u = amazonUrl(sender.url ?? '');
    if (!u || sender.frameId !== 0) throw new Error('Only the main Amazon page can submit a capture.');
    if (message.type === 'OPEN_REVIEW') { await openReview(); return; }
    if (message.type !== 'CAPTURE') throw new Error('This operation is available only in the extension.');
    const claimed = amazonUrl(message.capture?.sourceUrl);
    if (!claimed || claimed.origin !== u.origin || claimed.pathname !== u.pathname) throw new Error('Capture does not match the sending page.');
    return ingest(message.capture, sender.tab!.id!, message.manual === true);
  }
  if (!extensionPage) throw new Error('Untrusted extension page.');
  const s = await readState();
  switch (message.type) {
    case 'STATE': {
      const { draft } = await chrome.storage.session.get('draft');
      const { savedCredential } = await chrome.storage.local.get<{savedCredential?:{key:string}}>('savedCredential');
      return { ...s, draft, connected: !!await getSecret(credentialKey(s.settings)), remembered: savedCredential?.key === credentialKey(s.settings) };
    }
    case 'SAVE_SETTINGS': {
      const settings = validateSettings(message.settings);
      if (!await chrome.permissions.contains({ origins: [`${settings.origin}/*`] })) throw new Error('Grant access to this server using Save & connect.');
      const key = credentialKey(settings);
      const secret = typeof message.secret === 'string' && message.secret ? message.secret : await getSecret(key);
      s.settings = settings; await saveState(s);
      await chrome.storage.session.remove('tokenCache');
      if (secret) await chrome.storage.session.set({ credentials: { key, secret } });
      if (message.remember && secret) await chrome.storage.local.set({ savedCredential: { key, secret } });
      else await chrome.storage.local.remove('savedCredential');
      const api = await client(settings); const who = await api.self(); const databases = await api.databases();
      if (!databases.some(db => db.name === settings.database)) throw new Error(`Connected, but database '${settings.database}' does not exist. Choose from: ${databases.map(d => d.name).join(', ')}.`);
      return { who: who.name, databases };
    }
    case 'DISCONNECT': await chrome.storage.session.remove(['credentials', 'tokenCache']); await chrome.storage.local.remove('savedCredential'); return;
    case 'SEARCH': {
      const p = order(s, message.key); const api = await client(p.target);
      if (typeof message.query !== 'string' || message.query.trim().length < 2) throw new Error('Enter at least two characters.');
      return api.search(message.query.trim().slice(0, 150));
    }
    case 'SUGGEST': {
      const p = order(s, message.key); const line = p.lines[message.index]; if (!line) throw new Error('Line not found.');
      const api = await client(p.target); const exact = await api.findSource(line.product);
      return { exact, suggestions: await api.search(line.product.title.split(/\s+/).slice(0, 4).join(' ')) };
    }
    case 'RESOLVE': {
      const p = order(s, message.key); if (['done', 'ignored'].includes(p.status)) throw new Error('This order has already been handled.');
      const line = p.lines[message.index]; if (!line || line.phase === 'done') throw new Error('This line cannot be edited.');
      if (message.choice === 'skip') { line.excluded = true; await saveState(s); return; }
      if (!positiveQuantity(message.quantity) || !Number.isInteger(message.quantity)) throw new Error('Enter a positive whole number of purchased packages.');
      line.product.quantity = message.quantity; line.excluded = false;
      if (message.choice === 'existing') {
        if (!positiveQuantity(message.multiplier)) throw new Error('Enter the number of inventory units per purchased package.');
        const api = await client(p.target); const item = await api.getItem(message.itemId);
        const mapping: Mapping = { productKey: productKey(line.product), itemId: item.id, itemName: item.name, multiplier: message.multiplier, unit: item.unit.string };
        line.mapping = mapping; line.choice = 'existing'; line.itemId = item.id;
      } else if (message.choice === 'new') {
        if (line.phase === 'creating' || line.itemId) throw new Error('A previous write may have created this item. Link the existing item instead.');
        if (typeof message.name !== 'string' || !message.name.trim() || message.name.length > 500) throw new Error('Enter a product name (up to 500 characters).');
        line.choice = 'new'; line.newName = message.name.trim(); line.mapping = undefined;
      } else throw new Error('Invalid product choice.');
      line.error = undefined; p.status = 'review'; p.error = undefined; p.approved = false;
      await saveState(s); return;
    }
    case 'APPROVE': {
      const p = order(s, message.key); if (p.status === 'done' || p.status === 'ignored') return;
      if (message.confirmed !== true) throw new Error('Confirm that the order was placed and its quantities are correct.');
      if (!p.lines.every(l => l.excluded || l.phase === 'done' || (positiveQuantity(l.product.quantity) && (l.mapping || l.choice === 'new')))) throw new Error('Resolve every product first, or skip lines you do not want to import.');
      p.approved = true; p.status = 'queued'; p.attempts = 0; p.nextAttempt = undefined;
      await saveState(s); void serialized(runQueue); return;
    }
    case 'RETRY': {
      const p = order(s, message.key); if (!p.approved) throw new Error('Review and approve this order first.');
      if (p.status === 'done' || p.status === 'ignored') return;
      p.status = 'queued'; p.nextAttempt = undefined; p.attempts = 0; await saveState(s); void serialized(runQueue); return;
    }
    case 'IGNORE': { const p = order(s, message.key); p.status = 'ignored'; await saveState(s); await badge(); return; }
    case 'MANUAL': {
      if (message.confirmed !== true) throw new Error('Confirm this is a completed purchase.');
      const c = validateCapture({ ...message.capture, kind: 'order-detail', warnings: ['Manually entered receipt. Review product identities and quantities.'] });
      if (!c.orderId || !c.products.length) throw new Error('Enter an order ID and at least one product.');
      const result = await ingest(c, -1, true); await chrome.storage.session.remove('draft'); return result;
    }
    case 'DELIVERY': {
      const p = order(s, message.key); const line = p.lines[message.index];
      if (!line || !['incoming', 'received', 'cancelled'].includes(message.status)) throw new Error('Invalid shipment update.');
      await setDeliveryStatus(await client(p.target), p, line, message.status);
      line.deliveryStatus = message.status;
      await saveState(s);
      return;
    }
    case 'LEDGER': {
      const p = order(s, message.key); const api = await client(p.target);
      return Promise.all(p.lines.filter(l => l.itemId).map(async l => { const item = await api.getItem(l.itemId!); return { itemId: item.id, attributes: item.attributes }; }));
    }
    case 'UNLINK': if (typeof message.mappingKey === 'string') { delete s.mappings[message.mappingKey]; await saveState(s); } return;
    case 'EXPORT': return { version: 1, exportedAt: new Date().toISOString(), ...s };
    case 'CLEAR_HISTORY': {
      for (const [k, p] of Object.entries(s.purchases)) if (['done', 'ignored'].includes(p.status)) delete s.purchases[k];
      await saveState(s); return;
    }
    case 'OPEN_REVIEW': return openReview();
    default: throw new Error('Unknown operation.');
  }
}
chrome.runtime.onMessage.addListener((message, sender, reply) => {
  if (!message || typeof message.type !== 'string') return;
  serialized(() => command(message, sender)).then(data => reply({ ok: true, data })).catch(e => reply({ ok: false, error: errorText(e) }));
  return true;
});
chrome.runtime.onInstalled.addListener(() => { void ready.then(async () => { await chrome.alarms.create('oqm-queue', { periodInMinutes: 1 }); await badge(); }); });
chrome.runtime.onStartup.addListener(() => { void ready.then(async () => { await chrome.alarms.create('oqm-queue', { periodInMinutes: 1 }); await serialized(runQueue); }); });
chrome.alarms.onAlarm.addListener(a => { if (a.name === 'oqm-queue') void serialized(async () => { await ready; await runQueue(); }); });
