// @vitest-environment jsdom
import { describe, expect, it } from 'vitest';
import { AmazonAdapter } from '../src/adapters/amazon';
import { validateCapture } from '../src/domain';
const adapter = new AmazonAdapter();
const url = 'https://www.amazon.com/gp/buy/thankyou/handlers/display.html';
const row = (asin='B012345678',qty='2',title='USB C Cable') => `<div data-testid="order-item"><a href="/dp/${asin}">${title}</a><span data-testid="item-quantity">Qty: ${qty}</span><span data-testid="item-price">$12.99</span></div>`;
function capture(html: string, at=url) { document.body.innerHTML=html;return adapter.capture(document,new URL(at)); }
function receipt(rows=row(), extra='') { return `<main id="thank-you-page"><h1>Order placed, thank you!</h1><p>Order # 123-1234567-1234567</p>${rows}${extra}</main>`; }
describe('Amazon receipts',()=>{
  it('extracts a completed order, ASIN, explicit quantity and price',()=>{
    const c=capture(receipt())!;expect(c.kind).toBe('confirmation');expect(c.orderId).toBe('123-1234567-1234567');expect(c.products[0]).toMatchObject({title:'USB C Cable',quantity:2,productId:'B012345678',priceText:'$12.99'});expect(c.warnings).toEqual([]);
  });
  it('does not infer a purchase on the checkout form',()=>expect(capture(receipt(),'https://www.amazon.com/gp/buy/spc/handlers/display.html')).toBeNull());
  it('does not infer a purchase just from a thankyou URL',()=>expect(capture('<main>Something went wrong</main>')).toBeNull());
  it('does not mistake a product page for an order',()=>expect(capture(receipt(),'https://www.amazon.com/dp/B012345678')).toBeNull());
  it('rejects lookalike domains',()=>expect(capture(receipt(),'https://www.amazon.com.evil.example/gp/buy/thankyou/')).toBeNull());
  it('captures cart quantities but labels them only as cart',()=>{
    const c=capture('<div id="sc-active-cart"><div class="sc-list-item" data-asin="B012345678"><span class="sc-product-title">Cable</span><select name="quantity"><option selected>3</option></select></div></div>','https://www.amazon.com/gp/cart/view.html')!;
    expect(c.kind).toBe('cart');expect(c.products[0].quantity).toBe(3);expect(c.orderId).toBeUndefined();
  });
  it('marks missing quantities for human review instead of assuming one',()=>{
    const c=capture(receipt(row('B012345678','')))!;expect(c.products[0].quantity).toBeNull();expect(c.warnings.join(' ')).toContain('quantities');
  });
  it('excludes recommended product rows',()=>{
    const c=capture(receipt(row(),`<div id="recommendations">${row('B099999999')}</div>`))!;expect(c.products).toHaveLength(1);
  });
  it('handles duplicate responsive rows conservatively',()=>{
    const c=capture(receipt(row()+row()))!;expect(c.products).toHaveLength(1);expect(c.warnings.join(' ')).toContain('Repeated');
  });
  it('requires review for multiple order IDs',()=>{
    const c=capture(receipt(row(),'<p>111-1111111-1111111</p>'))!;expect(c.orderId).toBeUndefined();expect(c.warnings.join(' ')).toContain('Multiple');
  });
  it('extracts an order ID from order details URL',()=>{
    const c=capture(`<main id="orderDetails">${row()}</main>`,'https://www.amazon.com/gp/your-account/order-details?orderID=123-1234567-1234567')!;expect(c.kind).toBe('order-detail');expect(c.orderId).toBe('123-1234567-1234567');
  });
  it('retains product variants',()=>{const html=row().replace('</div>','<span data-testid="item-variant">Color: Blue</span></div>');expect(capture(receipt(html))!.products[0].variant).toBe('Color: Blue');});
  it('strips source URL query data before persistence',()=>{
    const c=capture(receipt(),url+'?sessionId=private')!;expect(validateCapture(c).sourceUrl).toBe(url);
  });
  it('does not capture saved-for-later rows',()=>{
    const c=capture('<div id="sc-active-cart"><div class="sc-list-item" data-asin="B012345678" data-itemtype="saved"><span class="sc-product-title">Cable</span></div></div>','https://www.amazon.com/gp/cart/view.html')!;expect(c.products).toEqual([]);
  });
});
