import { positiveQuantity, productKey, purchaseAttribute, sourceAttribute, type InventoryItem, type Line, type Mapping, type Purchase, type PurchaseRecord } from '../domain';
import { ApiError, OqmClient } from './client';

export function bindingFrom(item: InventoryItem, line: Line): Mapping | undefined {
  try {
    const data = JSON.parse(item.attributes[sourceAttribute(line.product)]);
    if (data.productKey !== productKey(line.product) || !positiveQuantity(data.multiplier) || data.unit !== item.unit.string) return;
    return { productKey: data.productKey, itemId: item.id, itemName: item.name, multiplier: data.multiplier, unit: data.unit };
  } catch { return; }
}
export function ledgerRecord(purchase: Purchase, line: Line, unit: string): PurchaseRecord {
  if (!positiveQuantity(line.product.quantity)) throw new Error('Confirm the purchased quantity first.');
  const multiplier = line.mapping?.multiplier ?? 1;
  const inventoryQuantity = line.product.quantity * multiplier;
  if (!positiveQuantity(inventoryQuantity)) throw new Error('Inventory quantity is outside the supported range.');
  return {
    version: 1, orderId: purchase.orderId, productKey: productKey(line.product), status: 'incoming',
    quantityPurchased: line.product.quantity, inventoryQuantity, unit,
    productUrl: line.product.url, orderUrl: `https://www.amazon.com/gp/your-account/order-details?orderID=${purchase.orderId}`,
    title: line.product.title, variant: line.product.variant, priceText: line.product.priceText, imageUrl: line.product.imageUrl,
    recordedAt: purchase.capturedAt,
  };
}
function attributes(p: Purchase, line: Line, key: string, unit: string): Record<string, string> {
  return {
    [sourceAttribute(line.product)]: JSON.stringify({ productKey: productKey(line.product), url: line.product.url, multiplier: line.mapping?.multiplier ?? 1, unit }),
    [key]: JSON.stringify(ledgerRecord(p, line, unit)),
  };
}
function matchesRecord(raw: string | undefined, p: Purchase, line: Line): boolean {
  try {
    const r = JSON.parse(raw ?? '');
    return r.orderId === p.orderId && r.productKey === productKey(line.product) && r.quantityPurchased === line.product.quantity && r.inventoryQuantity === line.product.quantity! * (line.mapping?.multiplier ?? 1);
  } catch { return false; }
}
export function createPayload(p: Purchase, line: Line, key: string) {
  return {
    name: (line.newName || line.product.title).trim(), description: line.product.variant || `Imported from Amazon (${line.product.productId}). Purchase status is recorded in oqmCart attributes.`,
    storageType: 'BULK', unit: { string: 'units' }, identifiers: [], idGenerators: [], defaultPrices: [], categories: [], storageBlocks: [], imageIds: [], attachedFiles: [],
    keywords: ['oqm-cart', 'incoming', 'amazon'], attributes: attributes(p, line, key, 'units'),
    associatedLinks: [{ label: `Amazon ${line.product.productId}`, link: line.product.url }],
  };
}
/** Persist phase BEFORE each write. An uncertain POST is reconciled, never blindly repeated. */
export async function syncLine(api: OqmClient, purchase: Purchase, line: Line, persist: () => Promise<void>): Promise<void> {
  if (line.excluded || line.phase === 'done') return;
  if (!purchase.approved || !positiveQuantity(line.product.quantity)) throw new Error('This purchase needs review.');
  const marker = await purchaseAttribute(purchase, line.product);
  let item: InventoryItem | undefined;
  if (line.itemId || line.mapping) item = await api.getItem(line.itemId ?? line.mapping!.itemId);
  else {
    const already = await api.byAttribute(marker);
    if (already.length > 1) throw new Error('Multiple OQM items contain this order line. Resolve duplicates before continuing.');
    if (already.length === 1) item = already[0];
    else {
      const source = await api.findSource(line.product);
      if (source.length > 1) throw new Error('This Amazon product is linked to multiple OQM items. Choose the correct item in Review.');
      if (source.length === 1) {
        item = source[0];
        const existing = bindingFrom(item, line);
        if (!existing) throw new Error('Existing source mapping has different units. Link the item explicitly in Review.');
        line.mapping = existing;
      }
    }
  }
  if (item && marker in item.attributes) {
    if (!matchesRecord(item.attributes[marker], purchase, line)) throw new Error('This order line already exists with different quantities. Review it in OQM; no second purchase was written.');
    line.itemId = item.id;
    line.mapping ??= bindingFrom(item, line);
    line.phase = 'done'; line.error = undefined; await persist(); return;
  }
  if (!item) {
    if (line.phase === 'creating') throw new Error('A previous creation may have reached OQM, but it cannot yet be found. Check OQM and link that item in Review; automatic creation is paused to avoid a duplicate.');
    if (line.choice !== 'new') throw new Error('Choose an existing OQM item or create a new one.');
    line.phase = 'creating'; await persist();
    try { item = await api.createItem(createPayload(purchase, line, marker)); }
    catch (e) { if (e instanceof ApiError && !e.uncertain) { line.phase = 'pending'; await persist(); } throw e; }
    line.itemId = item.id; await persist();
  } else {
    const unit = item.unit.string;
    if (!line.mapping || line.mapping.unit !== unit) throw new Error('The item unit changed. Review the quantity conversion before importing.');
    const attr = { ...item.attributes, ...attributes(purchase, line, marker, unit) };
    const links = [...(item.associatedLinks ?? [])];
    if (!links.some(l => l.link === line.product.url)) links.push({ label: `Amazon ${line.product.productId}`, link: line.product.url });
    line.phase = 'writing'; line.itemId = item.id; await persist();
    await api.patchItem(item.id, { attributes: attr, associatedLinks: links, keywords: [...new Set([...(item.keywords ?? []), 'oqm-cart', 'incoming', 'amazon'])] });
  }
  const verified = await api.getItem(line.itemId!);
  if (!matchesRecord(verified.attributes[marker], purchase, line)) throw new Error('Write could not be verified. Retry will check the remote purchase marker before writing.');
  line.mapping = { productKey: productKey(line.product), itemId: verified.id, itemName: verified.name, multiplier: line.mapping?.multiplier ?? 1, unit: verified.unit.string };
  line.phase = 'done'; line.error = undefined; await persist();
}

export async function setDeliveryStatus(api: OqmClient, p: Purchase, line: Line, status: PurchaseRecord['status']): Promise<void> {
  if (!line.itemId || line.phase !== 'done') throw new Error('Import this line first.');
  const item = await api.getItem(line.itemId);
  const key = await purchaseAttribute(p, line.product);
  const record = JSON.parse(item.attributes[key] ?? 'null') as PurchaseRecord | null;
  if (!record || record.orderId !== p.orderId) throw new Error('Purchase metadata was not found in OQM.');
  record.status = status;
  const attrs = { ...item.attributes, [key]: JSON.stringify(record) };
  const hasIncoming = Object.entries(attrs).some(([k, v]) => { if (!k.startsWith('oqmCart_purchase_')) return false; try { return JSON.parse(v).status === 'incoming'; } catch { return false; } });
  const keywords = (item.keywords ?? []).filter(k => k !== 'incoming');
  if (hasIncoming) keywords.push('incoming');
  await api.patchItem(item.id, { attributes: attrs, keywords });
  const verified = await api.getItem(item.id);
  if (JSON.parse(verified.attributes[key] ?? 'null')?.status !== status) throw new Error('Delivery status could not be verified.');
}
