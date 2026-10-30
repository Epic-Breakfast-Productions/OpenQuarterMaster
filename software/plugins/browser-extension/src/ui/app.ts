import { DEFAULT_SETTINGS, normalizeOrigin, type Capture, type InventoryItem, type Purchase, type State } from '../domain';
import { esc, rpc } from './rpc';
type ViewState = State & { connected: boolean; remembered: boolean; draft?: Capture };
let state: ViewState;
const content = document.querySelector<HTMLElement>('#content')!;
const feedback = document.querySelector<HTMLElement>('#feedback')!;
let busy = false;
let manualRows = 1;
function say(message: string, error = false) { feedback.textContent = message; feedback.className = error ? 'error' : ''; }
function field(id: string): HTMLInputElement { return document.getElementById(id) as HTMLInputElement; }
function keyData(p: Purchase) { return `data-key="${esc(p.key)}"`; }
function orderCard(p: Purchase): string {
  const history = ['done', 'ignored'].includes(p.status);
  return `<article class="card"><div class="card-head"><div><h2>Order ${esc(p.orderId)}</h2><small>Amazon.com · ${esc(new Date(p.capturedAt).toLocaleString())} · ${esc(p.target.database)}</small></div><span class="pill ${esc(p.status)}">${esc(p.status)}</span></div>
  ${p.error ? `<p class="callout warning">${esc(p.error)}</p>` : ''}
  ${p.warnings.length && !history ? `<div class="callout warning">${p.warnings.map(esc).join('<br>')}</div>` : ''}
  ${p.lines.map((l, i) => `<section class="line" id="line-${esc(p.orderId)}-${i}"><div class="card-head"><div><h3>${esc(l.product.title)}</h3><p class="metadata"><a href="${esc(l.product.url)}" target="_blank" rel="noreferrer">${esc(l.product.productId)}</a> · ${l.product.quantity ?? '?'} purchased package(s)${l.product.priceText ? ` · ${esc(l.product.priceText)}` : ''}${l.product.variant ? ` · ${esc(l.product.variant)}` : ''}</p></div><span class="pill">${l.excluded ? 'Skipped' : l.phase === 'done' ? 'Recorded' : l.mapping ? 'Linked' : l.choice === 'new' ? 'New item' : 'Match needed'}</span></div>
  ${l.mapping ? `<p>Linked to <strong>${esc(l.mapping.itemName)}</strong> · ${l.product.quantity === null ? '?' : l.product.quantity * l.mapping.multiplier} ${esc(l.mapping.unit)} in this purchase (${l.mapping.multiplier} per package)</p>` : l.choice === 'new' ? `<p>Will create <strong>${esc(l.newName)}</strong></p>` : ''}
  ${l.error ? `<p class="inline-error">${esc(l.error)}</p>` : ''}
  ${l.phase === 'done' ? `<div class="row"><small>OQM item: ${esc(l.itemId)}</small><select aria-label="Delivery status" id="delivery-${esc(p.orderId)}-${i}" style="width:auto">${(['incoming','received','cancelled'] as const).map(status => `<option value="${status}" ${(l.deliveryStatus ?? 'incoming') === status ? 'selected' : ''}>${status === 'incoming' ? 'Incoming' : status === 'received' ? 'Received (metadata only)' : 'Cancelled (metadata only)'}</option>`).join('')}</select><button class="small secondary" data-action="delivery" ${keyData(p)} data-index="${i}">Save status</button></div>` : !history ? `<button class="small secondary" data-action="edit" ${keyData(p)} data-index="${i}">${l.mapping || l.choice ? 'Edit match / quantity' : 'Match this product'}</button><div id="resolve-${esc(p.orderId)}-${i}"></div>` : ''}
  </section>`).join('')}
  ${!history ? `<div class="actions"><label class="check"><input type="checkbox" id="confirm-${esc(p.orderId)}">I placed this order and checked all purchased products, quantities and unit conversions.</label></div><div class="actions"><button data-action="approve" ${keyData(p)}>Import as incoming</button>${p.approved ? `<button class="secondary" data-action="retry" ${keyData(p)}>Retry / reconcile</button>` : ''}<button class="secondary" data-action="ignore" ${keyData(p)}>Dismiss order</button></div>` : `<div class="actions"><button class="small secondary" data-action="ledger" ${keyData(p)}>Read shipment records from OQM</button></div><div id="ledger-${esc(p.orderId)}"></div>`}
  </article>`;
}
function review(history = false) {
  const all = Object.values(state.purchases).sort((a,b) => b.capturedAt.localeCompare(a.capturedAt));
  const visible = all.filter(p => history === ['done', 'ignored'].includes(p.status));
  return `${!state.connected ? '<div class="callout warning">Connection needed. <a href="#settings">Enter your client secret in Settings</a> before importing. Captures stay on this browser until connected.</div>' : ''}
  ${state.draft ? '<div class="callout warning">A receipt needs an order ID or product details. <a href="#manual">Complete the captured receipt</a>.</div>' : ''}
  <div class="stats"><div class="stat"><strong>${all.filter(p => p.status === 'review' || p.status === 'error').length}</strong><span>Need your attention</span></div><div class="stat"><strong>${all.filter(p => p.status === 'queued' || p.status === 'syncing').length}</strong><span>Waiting to sync</span></div><div class="stat"><strong>${all.filter(p => p.status === 'done').length}</strong><span>Orders recorded</span></div></div>
  ${visible.length ? visible.map(orderCard).join('') : `<div class="empty"><div class="glyph">↘</div><h2>${history ? 'Your shipment history starts here' : 'Ready for your next purchase'}</h2><p>${history ? 'Imported orders will appear here with their OQM item links and shipment records.' : 'Finish an Amazon.com purchase. We’ll recognize repeat products and ask you about anything new.'}</p><a href="#manual">Already have an order? Add its receipt</a><div class="steps"><div class="step"><b>01 · Connect</b><p>Choose your OQM server and database in Settings.</p></div><div class="step"><b>02 · Purchase</b><p>The confirmation page triggers capture. A cart alone never adds inventory.</p></div><div class="step"><b>03 · Match once</b><p>Link an existing product or create one. Future purchases remember that choice.</p></div></div></div>`}
  <footer>Incoming records include purchased quantities but do not increase on-hand stock. Receive physical stock through OQM’s normal inventory workflow.</footer>`;
}
function settings() {
  const s = state.settings ?? DEFAULT_SETTINGS;
  return `<section class="card setting-card"><h2>Your OQM connection</h2><p class="muted">Use the server and service account supplied by your team.</p><form id="settings-form"><div class="form-grid">
  <label class="full">Server origin<input id="origin" type="url" required value="${esc(s.origin)}"><small>Just the server address. The Core API path is added automatically.</small></label>
  <label>Keycloak realm<input id="realm" required value="${esc(s.realm)}"></label><label>Client ID<input id="clientId" required value="${esc(s.clientId)}"></label>
  <label class="full">Client secret<input id="secret" type="password" autocomplete="off" placeholder="${state.connected ? 'Already available — leave blank to keep' : 'Paste your browser-plugin secret'}"><small>Your secret is never included in exports or sent to Amazon.</small></label>
  <label class="full">OQM database<input id="database" required value="${esc(s.database)}"><small>Existing orders keep their original database, even if you change this setting.</small></label></div>
  <label class="check"><input id="remember" type="checkbox" ${state.remembered ? 'checked' : ''}>Remember the secret on this Chrome profile. Otherwise it is cleared when Chrome restarts.</label>
  <label class="check"><input id="autoSync" type="checkbox" ${s.autoSync ? 'checked' : ''}>Automatically import confirmed orders when every product is already linked and every quantity is known.</label>
  <label class="check"><input id="openReview" type="checkbox" ${s.openReview ? 'checked' : ''}>Open a review window for unfamiliar products.</label>
  <div class="actions"><button type="submit">Save & connect</button><button type="button" class="secondary" data-action="disconnect">Forget credentials</button></div></form></section>
  <section class="card setting-card"><h2>First connection on this campus server</h2><p>Chrome must trust the server’s HTTPS certificate. If connection fails, open <a href="${esc(s.origin)}/core/api" target="_blank" rel="noreferrer">the Core API</a> and check the certificate warning. The durable fix is to install the campus/server CA supplied by its administrator, or use a trusted certificate. The extension cannot bypass certificate errors.</p><p class="muted">This prototype supports your existing service-account login. A shared secret in a browser is suitable only for a controlled team deployment; a public release should use individual user login with PKCE or a server-side credential bridge.</p></section>
  <section class="card setting-card"><h2>Local records</h2><p>Export a backup of mappings and orders. It contains purchase information but no passwords or tokens.</p><div class="actions"><button class="secondary" data-action="export">Export backup</button><button class="secondary" data-action="clear">Clear completed local history</button></div><small>Clearing local history does not delete OQM items or their purchase records.</small></section>`;
}
function mappings() {
  const entries = Object.entries(state.mappings);
  return `<div class="callout">A product link identifies the OQM item and the number of inventory units per Amazon package. Matching uses the ASIN, never just the title.</div><section class="card"><h2>${entries.length} remembered product links</h2>${entries.map(([key,m]) => `<div class="mapping"><div><strong>${esc(m.itemName)}</strong><small>${esc(m.productKey)} · ${m.multiplier} ${esc(m.unit)} per package</small><small>${esc(key.split('|').slice(0,3).join(' · '))}</small></div><button class="small secondary" data-action="unlink" data-mapping="${esc(key)}">Forget locally</button></div>`).join('') || '<p class="muted">Match a purchase to start building your product links.</p>'}</section>`;
}
function manualRow(i: number): string {
  const p = state.draft?.products[i];
  return `<div class="receipt-row"><div class="form-grid"><label>Product name<input name="name-${i}" required value="${esc(p?.title)}"></label><label>Amazon ASIN<input name="asin-${i}" required pattern="[A-Za-z0-9]{10}" maxlength="10" value="${esc(p?.productId)}"></label><label>Packages purchased<input name="qty-${i}" type="number" min="1" max="1000000" step="1" required value="${p?.quantity ?? ''}"></label></div></div>`;
}
function manual() {
  manualRows = Math.max(1, state.draft?.products.length ?? 1);
  return `<section class="card"><h2>Complete an Amazon receipt</h2><p class="muted">For receipts the page reader could not fully extract. Enter one order at a time. An ASIN is the 10-character product ID in an Amazon /dp/ link.</p><form id="manual-form"><label>Amazon order ID<input id="manual-order" required pattern="[0-9]{3}-[0-9]{7}-[0-9]{7}" placeholder="123-1234567-1234567" value="${esc(state.draft?.orderId)}"></label><div id="manual-rows">${Array.from({length: manualRows},(_,i) => manualRow(i)).join('')}</div><button type="button" class="secondary" data-action="add-row">+ Add another product</button><label class="check" style="margin-top:20px"><input id="manual-confirm" type="checkbox" required>This is a completed purchase. I checked the order ID, products and quantities.</label><button type="submit">Save receipt for matching</button></form></section>`;
}
async function render() {
  state = await rpc<ViewState>('STATE');
  const page = location.hash.slice(1) || 'review';
  const titles: Record<string,string> = { review:'Purchase inbox', history:'Shipment history', mappings:'Product links', settings:'Connection settings', manual:'Add a receipt' };
  document.querySelector('#title')!.textContent = titles[page] ?? titles.review;
  document.querySelectorAll('nav a').forEach(a => a.classList.toggle('active', a.getAttribute('href') === '#'+page));
  content.innerHTML = page === 'settings' ? settings() : page === 'mappings' ? mappings() : page === 'manual' ? manual() : review(page === 'history');
}
async function perform(work: () => Promise<void>, refresh = true) {
  if (busy) return; busy = true;
  try { await work(); if (refresh) await render(); } catch(e) { say(e instanceof Error ? e.message : 'Operation failed.', true); } finally { busy = false; }
}
async function editor(p: Purchase, index: number) {
  const l = p.lines[index]; const id = `${p.orderId}-${index}`; const box = document.getElementById(`resolve-${id}`)!;
  box.innerHTML = `<div class="resolve"><h3>Is this the same as an item you already track?</h3><p>Choose an existing item, or create a new product record. Check package size and variants before linking.</p><label>Packages purchased<input id="quantity-${id}" type="number" min="1" max="1000000" step="1" value="${l.product.quantity ?? ''}"></label><div class="search-row"><input id="query-${id}" aria-label="Search OQM items" placeholder="Search your OQM inventory" value="${esc(l.product.title.split(/\s+/).slice(0,4).join(' '))}"><button class="secondary" data-action="search" ${keyData(p)} data-index="${index}">Search</button></div><div class="results" id="results-${id}">Looking for existing items…</div><label>Or paste an existing OQM item ID<input id="itemId-${id}" placeholder="24-character item ID" value="${esc(l.mapping?.itemId)}"></label><label>Inventory units per purchased package<input id="multiplier-${id}" type="number" min="0.000001" step="any" value="${l.mapping?.multiplier ?? 1}"><small>Example: 2 packages × 12 batteries = 24 inventory units. Use 1 to track packages.</small></label><button class="secondary" data-action="link" ${keyData(p)} data-index="${index}">Link selected item</button><hr><label>New product name<input id="newName-${id}" value="${esc(l.newName || l.product.title)}"></label><div class="actions"><button data-action="new" ${keyData(p)} data-index="${index}">Create as a new product</button><button class="secondary" data-action="skip" ${keyData(p)} data-index="${index}">Skip this line</button></div><small>New products track packages as “units”. Creating happens after you approve the order.</small></div>`;
  try { const result = await rpc<{exact: InventoryItem[]; suggestions: InventoryItem[]}>('SUGGEST', { key:p.key,index }); showResults(p,index,[...result.exact,...result.suggestions]); }
  catch(e) { document.getElementById(`results-${id}`)!.textContent = e instanceof Error ? e.message : 'Search unavailable. You can still choose a new product.'; }
}
function showResults(p: Purchase,index: number,items: InventoryItem[]) {
  const unique = [...new Map(items.map(i => [i.id,i])).values()];
  document.getElementById(`results-${p.orderId}-${index}`)!.innerHTML = unique.map(item => `<div class="result"><div><strong>${esc(item.name)}</strong><small>${esc(item.id)} · unit: ${esc(item.unit.string)}</small></div><button class="small secondary" data-action="select" ${keyData(p)} data-index="${index}" data-id="${esc(item.id)}">Select</button></div>`).join('') || '<p class="muted">No similar items found. Try another search or create a new product.</p>';
}
content.addEventListener('click', event => {
  const b = (event.target as Element).closest<HTMLButtonElement>('button[data-action]'); if (!b) return;
  const action=b.dataset.action, key=b.dataset.key, index=Number(b.dataset.index), p=key ? state.purchases[key] : undefined;
  const id=p ? `${p.orderId}-${index}` : '';
  if (action==='select') { field(`itemId-${id}`).value=b.dataset.id!; return; }
  if (action==='add-row') { document.getElementById('manual-rows')!.insertAdjacentHTML('beforeend',manualRow(manualRows++)); return; }
  void perform(async () => {
    if(action==='edit') { await editor(p!,index); return; }
    if(action==='search') { showResults(p!,index,await rpc('SEARCH',{key,query:field(`query-${id}`).value})); return; }
    if(['link','new','skip'].includes(action!)) {
      await rpc('RESOLVE',{key,index,choice:action==='link'?'existing':action==='new'?'new':'skip',quantity:Number(field(`quantity-${id}`).value),itemId:field(`itemId-${id}`).value.trim(),multiplier:Number(field(`multiplier-${id}`).value),name:field(`newName-${id}`).value}); say('Product choice saved. Approve the order when every line is ready.');
    } else if(action==='approve') { await rpc('APPROVE',{key,confirmed:field(`confirm-${p!.orderId}`).checked}); say('Purchase queued. Writes are verified against OQM.'); }
    else if(action==='retry') { await rpc('RETRY',{key}); say('Queued for reconciliation. Existing remote order markers are checked first.'); }
    else if(action==='ignore') { if(!confirm('Dismiss this order? Any lines already imported will remain in OQM.')) return; await rpc('IGNORE',{key}); say('Order dismissed.'); }
    else if(action==='disconnect') { await rpc('DISCONNECT'); say('Credentials removed from this extension.'); }
    else if(action==='unlink') { await rpc('UNLINK',{mappingKey:b.dataset.mapping}); say('Local link forgotten. Source information in OQM remains available for matching.'); }
    else if(action==='delivery') { await rpc('DELIVERY',{key,index,status:field(`delivery-${id}`).value}); say('Shipment metadata updated. Physical stock quantities were not changed.'); }
    else if(action==='ledger') { const data=await rpc('LEDGER',{key}); document.getElementById(`ledger-${p!.orderId}`)!.innerHTML=`<details open><summary>Remote item attributes</summary><pre>${esc(JSON.stringify(data,null,2))}</pre></details>`; return; }
    else if(action==='export') { const data=await rpc('EXPORT'); const url=URL.createObjectURL(new Blob([JSON.stringify(data,null,2)],{type:'application/json'})); const a=document.createElement('a');a.href=url;a.download=`oqm-companion-backup-${new Date().toISOString().slice(0,10)}.json`;a.click();setTimeout(()=>URL.revokeObjectURL(url),1000);say('Backup exported without credentials.'); }
    else if(action==='clear') { if(!confirm('Export a backup first. Clear completed and dismissed orders from this browser? OQM records and product links remain.')) return; await rpc('CLEAR_HISTORY');say('Completed local history cleared.'); }
  },!['edit','search','ledger'].includes(action!));
});
content.addEventListener('submit', event => {
  event.preventDefault(); const form=event.target as HTMLFormElement;
  if(form.id==='settings-form') {
    // Request host permission directly within the user gesture.
    let origin:string; try { origin=normalizeOrigin(field('origin').value.trim()); } catch(e) { say((e as Error).message,true);return; }
    const permission=chrome.permissions.request({origins:[`${origin}/*`]});
    void perform(async()=>{
      if(!await permission) throw new Error('Server access was not granted.');
      const settings={origin,realm:field('realm').value.trim(),clientId:field('clientId').value.trim(),database:field('database').value.trim(),autoSync:field('autoSync').checked,openReview:field('openReview').checked};
      const result=await rpc('SAVE_SETTINGS',{settings,secret:field('secret').value.trim(),remember:field('remember').checked});
      field('secret').value='';say(`Connected as ${result.who}. Databases: ${result.databases.map((d:{name:string})=>d.name).join(', ')}.`);
    });
  } else if(form.id==='manual-form') {
    void perform(async()=>{
      const values=new FormData(form); const products=Array.from({length:manualRows},(_,i)=>{const asin=String(values.get(`asin-${i}`)).trim().toUpperCase();return {source:'amazon',marketplace:'amazon.com',productId:asin,url:`https://www.amazon.com/dp/${asin}`,title:String(values.get(`name-${i}`)).trim(),quantity:Number(values.get(`qty-${i}`))};});
      await rpc('MANUAL',{confirmed:field('manual-confirm').checked,capture:{kind:'order-detail',orderId:field('manual-order').value.trim(),sourceUrl:'https://www.amazon.com/gp/your-account/order-details',products,warnings:[]}});
      location.hash='review';say('Receipt saved. Match each product, then approve its import.');
    });
  }
});
window.addEventListener('hashchange',()=>{say('');void render().catch(e=>say(e.message,true));});
document.querySelector('#refresh')!.addEventListener('click',()=>{void perform(async()=>{say('');});});
chrome.storage.onChanged.addListener(changes=>{
  const editing=!!document.activeElement?.matches('input,select,textarea') || !!document.querySelector('.resolve');
  if(changes.state && !busy && !editing && !['#manual','#settings'].includes(location.hash)) void render().catch(()=>{});
});
void render().catch(e=>say(e.message,true));
