export interface Product {
  source: string;
  marketplace: string;
  productId: string;
  url: string;
  title: string;
  quantity: number | null;
  variant?: string;
  priceText?: string;
  imageUrl?: string;
}
export interface Capture {
  kind: 'cart' | 'confirmation' | 'order-detail';
  orderId?: string;
  sourceUrl: string;
  products: Product[];
  warnings: string[];
}
export interface Settings {
  origin: string;
  realm: string;
  clientId: string;
  database: string;
  autoSync: boolean;
  openReview: boolean;
}
export const DEFAULT_SETTINGS: Settings = {
  origin: 'https://10.1.6.27', realm: 'oqm', clientId: 'browser-plugin',
  database: 'default', autoSync: true, openReview: true,
};
export interface Mapping {
  productKey: string;
  itemId: string;
  itemName: string;
  multiplier: number;
  unit: string;
}
export interface Line {
  product: Product;
  mapping?: Mapping;
  choice?: 'existing' | 'new';
  newName?: string;
  excluded?: boolean;
  phase: 'pending' | 'creating' | 'writing' | 'done';
  itemId?: string;
  error?: string;
  deliveryStatus?: 'incoming' | 'received' | 'cancelled';
}
export interface Purchase {
  key: string;
  orderId: string;
  sourceUrl: string;
  capturedAt: string;
  target: Settings;
  lines: Line[];
  warnings: string[];
  approved: boolean;
  status: 'review' | 'queued' | 'syncing' | 'done' | 'error' | 'ignored';
  error?: string;
  attempts: number;
  nextAttempt?: number;
}
export interface State {
  settings: Settings;
  mappings: Record<string, Mapping>;
  purchases: Record<string, Purchase>;
}
export interface InventoryItem {
  id: string;
  name: string;
  description?: string;
  storageType?: string;
  unit: { string: string };
  attributes: Record<string, string>;
  associatedLinks?: { label: string; link: string; description?: string }[];
  keywords?: string[];
}
export interface PurchaseRecord {
  version: 1;
  orderId: string;
  productKey: string;
  status: 'incoming' | 'received' | 'cancelled';
  quantityPurchased: number;
  inventoryQuantity: number;
  unit: string;
  productUrl: string;
  orderUrl: string;
  title: string;
  variant?: string;
  priceText?: string;
  imageUrl?: string;
  recordedAt: string;
}
export const productKey = (p: Product) => `${p.source}:${p.marketplace}:${p.productId}`;
export const targetKey = (s: Settings) => `${s.origin}|${s.realm}|${s.database}`;
export const mappingKey = (s: Settings, p: Product) => `${targetKey(s)}|${productKey(p)}`;
export const purchaseKey = (c: Capture) => `amazon:amazon.com:${c.orderId}`;
export function sourceAttribute(p: Product): string {
  return `oqmCart_source_${p.source}_${p.marketplace.replaceAll('.', '_')}_${p.productId}`;
}
export async function purchaseAttribute(p: Purchase, product: Product): Promise<string> {
  const bytes = await crypto.subtle.digest('SHA-256', new TextEncoder().encode(`${p.key}|${productKey(product)}`));
  return 'oqmCart_purchase_' + [...new Uint8Array(bytes)].map(n => n.toString(16).padStart(2, '0')).join('');
}
export function positiveQuantity(v: unknown): v is number {
  return typeof v === 'number' && Number.isFinite(v) && v > 0 && v <= 1_000_000;
}
export function normalizeOrigin(raw: string): string {
  const u = new URL(raw);
  if (u.username || u.password || u.search || u.hash || (u.pathname !== '/' && u.pathname !== '')) throw new Error('Enter only the server origin, for example https://10.1.6.27.');
  if (u.protocol !== 'https:' && !(u.protocol === 'http:' && ['localhost', '127.0.0.1'].includes(u.hostname))) throw new Error('Use HTTPS (HTTP is supported only for localhost development).');
  return u.origin;
}
export function validateSettings(s: Settings): Settings {
  if (!s || typeof s.origin !== 'string' || typeof s.realm !== 'string' || typeof s.clientId !== 'string' || typeof s.database !== 'string') throw new Error('Invalid connection settings.');
  if (![s.realm, s.clientId, s.database].every(x => /^[a-zA-Z0-9._-]{1,100}$/.test(x))) throw new Error('Realm, client ID and database must use letters, digits, dots, underscores or hyphens.');
  return { origin: normalizeOrigin(s.origin), realm: s.realm, clientId: s.clientId, database: s.database, autoSync: !!s.autoSync, openReview: !!s.openReview };
}
export function amazonUrl(raw: string): URL | null {
  try { const u = new URL(raw); return u.protocol === 'https:' && ['www.amazon.com', 'amazon.com'].includes(u.hostname) && !u.username && !u.password ? u : null; } catch { return null; }
}
export function validateCapture(raw: Capture): Capture {
  const url = amazonUrl(raw?.sourceUrl);
  if (!url || !['cart', 'confirmation', 'order-detail'].includes(raw.kind) || !Array.isArray(raw.products) || raw.products.length > 100) throw new Error('Invalid Amazon capture.');
  if (raw.orderId && !/^\d{3}-\d{7}-\d{7}$/.test(raw.orderId)) throw new Error('Invalid Amazon order ID.');
  const seen = new Set<string>();
  const products = raw.products.map(p => {
    if (p.source !== 'amazon' || p.marketplace !== 'amazon.com' || !/^[A-Z0-9]{10}$/.test(p.productId) || typeof p.title !== 'string' || !p.title.trim()) throw new Error('Invalid product.');
    if (seen.has(p.productId)) throw new Error('Repeated product rows need manual review.');
    seen.add(p.productId);
    return { source: 'amazon', marketplace: 'amazon.com', productId: p.productId, url: `https://www.amazon.com/dp/${p.productId}`, title: p.title.trim().slice(0, 500), quantity: positiveQuantity(p.quantity) && Number.isInteger(p.quantity) ? p.quantity : null, variant: p.variant?.slice(0, 300), priceText: p.priceText?.slice(0, 80), imageUrl: safeImage(p.imageUrl) };
  });
  return { kind: raw.kind, orderId: raw.orderId, sourceUrl: url.origin + url.pathname, products, warnings: Array.isArray(raw.warnings) ? raw.warnings.filter(w => typeof w === 'string').slice(0, 10).map(w => w.slice(0, 300)) : [] };
}
export function safeImage(raw?: string): string | undefined {
  try { const u = new URL(raw ?? ''); if (u.protocol === 'https:' && /(^|\.)(media-amazon\.com|ssl-images-amazon\.com|images-amazon\.com)$/.test(u.hostname)) return u.href; } catch { /* Optional metadata. */ }
}
