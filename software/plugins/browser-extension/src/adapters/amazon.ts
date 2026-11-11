import { amazonUrl, safeImage, type Capture, type Product } from '../domain';
import type { SiteAdapter } from './site-adapter';

const ORDER_ID = /(?<!\d)\d{3}-\d{7}-\d{7}(?!\d)/g;
function text(el: Element | null): string {
  if (!el) return '';
  const rendered = (el as HTMLElement).innerText;
  if (typeof rendered === 'string') return rendered.replace(/\s+/g, ' ').trim();
  const walker = el.ownerDocument.createTreeWalker(el, 4); // Text nodes, separated at element boundaries.
  const parts: string[] = []; let node: Node | null;
  while ((node = walker.nextNode())) parts.push(node.nodeValue ?? '');
  return parts.join(' ').replace(/\s+/g, ' ').trim();
}
function asinFromLink(link: string): string | null {
  const u = amazonUrl(link);
  return u?.pathname.match(/\/(?:dp|gp\/product)\/([A-Z0-9]{10})(?:\/|$)/i)?.[1].toUpperCase() ?? null;
}
function quantity(row: Element): number | null {
  const field = row.querySelector<HTMLInputElement | HTMLSelectElement>('select[name="quantity"], input[name="quantity"], [data-a-selector="quantity"] input');
  const raw = field?.value ?? row.getAttribute('data-quantity') ?? text(row.querySelector('[data-testid="item-quantity"], .item-view-qty, .a-dropdown-prompt'));
  const parsed = raw.match(/^(?:Qty(?:uantity)?\s*:?\s*)?(\d+)$/i) ?? text(row).match(/\b(?:Qty|Quantity)\s*:?\s*(\d+)\b/i);
  const n = parsed ? Number(parsed[1]) : NaN;
  return Number.isSafeInteger(n) && n > 0 && n <= 1_000_000 ? n : null;
}
export class AmazonAdapter implements SiteAdapter {
  readonly id = 'amazon';
  matches(url: URL): boolean { return !!amazonUrl(url.href); }
  capture(doc: Document, url: URL): Capture | null {
    if (!this.matches(url)) return null;
    const isCart = /^\/gp\/cart(?:\/|$)/.test(url.pathname) || /^\/cart(?:\/|$)/.test(url.pathname);
    const isThankYou = /\/(?:gp\/buy\/thankyou|checkout\/.*thank|checkout\/thankyou)/i.test(url.pathname);
    const isDetail = /\/(?:gp\/your-account\/order-details|your-orders\/order-details)/i.test(url.pathname);
    if (!isCart && !isThankYou && !isDetail) return null;
    // No page-world JavaScript or request interception. Only rendered order/cart content.
    const scope = doc.querySelector(isCart ? '#sc-active-cart' : '#thank-you-page, #orderDetails, #order-details, #order-details-content, [data-testid="order-confirmation"], #yourOrders') ?? doc.querySelector('main') ?? doc.body;
    const confirmation = /(?:order (?:has been |was )?placed|thank you[!,. ]+your order|thanks for your order|order confirmation)/i.test(text(scope));
    if (isThankYou && !confirmation) return null;
    const ids = [...new Set([...(text(scope).match(ORDER_ID) ?? []), ...((url.searchParams.get('orderID') ?? url.searchParams.get('orderId') ?? '').match(ORDER_ID) ?? [])])];
    const warnings: string[] = [];
    if (!isCart && scope === doc.body) warnings.push('No isolated order container was found. Verify that all extracted lines belong to this purchase.');
    if (!isCart && ids.length !== 1) warnings.push(ids.length > 1 ? 'Multiple order IDs found. Review and import each order separately.' : 'Order ID is missing. Enter it from the purchase confirmation.');
    let rows: Element[];
    if (isCart) rows = [...scope.querySelectorAll('.sc-list-item[data-asin]')].filter(r => r.getAttribute('data-removed') !== 'true' && r.getAttribute('data-itemtype') !== 'saved');
    else {
      rows = [...scope.querySelectorAll('[data-testid="order-item"], .yohtmlc-item, .order-item, .shipment-item, .item-box')];
      if (!rows.length) {
        rows = [...scope.querySelectorAll<HTMLAnchorElement>('a[href*="/dp/"], a[href*="/gp/product/"]')]
          .filter(a => !a.closest('[id*="recommend"], [class*="recommend"], .a-carousel, [id*="sponsored"], #rhf'))
          .map(a => a.closest('.a-fixed-left-grid') ?? a.closest('[data-asin]'))
          .filter((r): r is Element => !!r);
        rows = [...new Set(rows)];
      }
    }
    const products: Product[] = [];
    for (const row of rows) {
      if (row.closest('[id*="recommend"], [class*="recommend"], .a-carousel, [id*="sponsored"], #rhf')) continue;
      const links = [...row.querySelectorAll<HTMLAnchorElement>('a[href*="/dp/"], a[href*="/gp/product/"]')];
      const link = links.find(a => text(a).length > 5) ?? links[0];
      const asin = row.getAttribute('data-asin')?.toUpperCase() || (link && asinFromLink(new URL(link.getAttribute('href')!, url).href));
      if (!asin || !/^[A-Z0-9]{10}$/.test(asin)) continue;
      const image = row.querySelector<HTMLImageElement>('img');
      const title = text(row.querySelector('.sc-product-title, [data-testid="product-title"], .yohtmlc-product-title')) || text(link ?? null) || image?.alt || '';
      if (!title.trim()) continue;
      const product: Product = {
        source: 'amazon', marketplace: 'amazon.com', productId: asin,
        url: `https://www.amazon.com/dp/${asin}`, title: title.slice(0, 500), quantity: quantity(row),
        priceText: text(row.querySelector('.sc-product-price, .a-price, [data-testid="item-price"], .item-view-price')) || undefined,
        variant: text(row.querySelector('.sc-product-variation, [data-testid="item-variant"], .item-view-variation')) || undefined,
        imageUrl: safeImage(image?.getAttribute('src') ?? undefined),
      };
      if (products.some(p => p.productId === asin)) { warnings.push(`Repeated rows for ${asin}; verify its total quantity.`); continue; }
      products.push(product);
    }
    if (products.some(p => p.quantity === null)) warnings.push('Some quantities were not explicit on the page. Please confirm them.');
    if (!products.length && !isCart) warnings.push('No order lines could be read. Open Order details or add the purchased items manually.');
    return { kind: isCart ? 'cart' : isThankYou ? 'confirmation' : 'order-detail', orderId: ids.length === 1 ? ids[0] : undefined, sourceUrl: url.href, products, warnings: [...new Set(warnings)] };
  }
}
