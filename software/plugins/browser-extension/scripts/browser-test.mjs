import { chromium, expect } from '@playwright/test';
import { mkdtemp, mkdir } from 'node:fs/promises';
import { resolve, join } from 'node:path';
import { tmpdir } from 'node:os';
import assert from 'node:assert/strict';

const profile = await mkdtemp(join(tmpdir(), 'oqm-extension-test-'));
await mkdir('artifacts', { recursive: true });
const extension = resolve('dist');
const attached = process.env.OQM_TEST_CDP_URL ? await chromium.connectOverCDP(process.env.OQM_TEST_CDP_URL) : null;
const context = attached ? attached.contexts()[0] : await chromium.launchPersistentContext(profile, {
  ...(process.env.OQM_TEST_BROWSER ? { executablePath: process.env.OQM_TEST_BROWSER } : { channel: 'chromium' }), headless: true,
  args: [`--disable-extensions-except=${extension}`, `--load-extension=${extension}`],
  viewport: { width: 1280, height: 920 },
});
try {
  const worker = context.serviceWorkers()[0] ?? await context.waitForEvent('serviceworker');
  const extensionId = new URL(worker.url()).hostname;
  // In-memory OQM simulation inside the isolated test worker. No campus writes or purchases.
  await worker.evaluate(() => {
    globalThis.fixtureItems = new Map([['111111111111111111111111', { id:'111111111111111111111111', name:'AA batteries', unit:{string:'units'}, attributes:{department:'Physics'}, associatedLinks:[], keywords:['lab'] }]]);
    globalThis.fixtureWrites = [];
    globalThis.fetch = async (input, init = {}) => {
      const url = new URL(String(input));
      if(url.origin !== 'https://10.1.6.27') throw new Error('Unexpected remote host');
      if(url.pathname.endsWith('/token')) return Response.json({access_token:'fixture-token',expires_in:300});
      if(init.headers.Authorization !== 'Bearer fixture-token') return new Response('',{status:401});
      if(url.pathname.endsWith('/interacting-entity/self')) return Response.json({name:'service-account-browser-plugin'});
      if(url.pathname.endsWith('/inventory/manage/db')) return Response.json([{name:'default',displayName:'Default'}]);
      const base='/core/api/api/v1/db/default/inventory/item';
      if(url.pathname===base) {
        if(init.method==='POST') {
          const item={...JSON.parse(init.body),id:(fixtureItems.size+1).toString(16).padStart(24,'0')};
          fixtureItems.set(item.id,item);fixtureWrites.push({method:'POST',id:item.id});return Response.json(item);
        }
        const attr=url.searchParams.get('attributeKey'), name=url.searchParams.get('name')?.toLowerCase();
        const results=[...fixtureItems.values()].filter(i=>(!attr || attr in i.attributes) && (!name || i.name.toLowerCase().includes(name)));
        return Response.json({results,numResultsForEntireQuery:results.length});
      }
      if(url.pathname.startsWith(base+'/')) {
        const id=url.pathname.slice(base.length+1), item=fixtureItems.get(id);
        if(!item)return new Response('',{status:404});
        if(init.method==='PUT') {Object.assign(item,JSON.parse(init.body));fixtureWrites.push({method:'PUT',id});}
        return Response.json(item);
      }
      throw new Error('Unmocked API route: '+url.pathname);
    };
  });
  const products = [
    {asin:'B012345678',title:'USB-C Charging Cable',quantity:1},
    {asin:'B087654321',title:'AA Battery Pack of 12',quantity:2},
  ];
  let currentOrder='123-1234567-1234567';
  let currentProducts=products;
  let currentKind='cart';
  const html=()=>`<!doctype html><html><head><title>Amazon fixture</title></head><body><main id="${currentKind==='cart'?'sc-active-cart':'thank-you-page'}"><h1>${currentKind==='cart'?'Your cart':'Order placed, thank you!'}</h1>${currentKind==='cart'?'':`<p>Order # ${currentOrder}</p>`}${currentProducts.map(p=>`<div ${currentKind==='cart'?'class="sc-list-item"':'data-testid="order-item"'} data-asin="${p.asin}" data-quantity="${p.quantity}"><a href="/dp/${p.asin}" class="sc-product-title">${p.title}</a><span data-testid="item-quantity">Qty: ${p.quantity}</span><span data-testid="item-price">$14.99</span></div>`).join('')}</main></body></html>`;
  await context.route('https://www.amazon.com/**',route=>route.fulfill({contentType:'text/html',body:html()}));
  const page=await context.newPage();
  const errors=[];page.on('pageerror',e=>errors.push(e.message));
  await page.goto(`chrome-extension://${extensionId}/app.html#settings`);
  await expect(page.getByRole('heading',{name:'Your OQM connection'})).toBeVisible();
  await page.locator('#secret').fill('fixture-secret');
  await page.locator('#openReview').uncheck();
  await page.getByRole('button',{name:'Save & connect'}).click();
  await expect(page.locator('#feedback')).toContainText('Connected as service-account-browser-plugin');
  const state=()=>worker.evaluate(async()=> (await chrome.storage.local.get('state')).state);
  const amazon=await context.newPage();
  await amazon.goto('https://www.amazon.com/gp/cart/view.html');
  await expect.poll(()=>worker.evaluate(async()=>Object.keys((await chrome.storage.session.get('carts')).carts??{}).length)).toBe(1);
  assert.equal(Object.keys((await state()).purchases).length,0,'cart must not become a purchase');
  currentKind='confirmation';
  await amazon.goto('https://www.amazon.com/gp/buy/thankyou/handlers/display.html');
  await expect.poll(async()=>Object.keys((await state()).purchases).length).toBe(1);
  await page.goto(`chrome-extension://${extensionId}/app.html#review`);
  await expect(page.getByRole('heading',{name:'Order '+currentOrder})).toBeVisible();
  await page.screenshot({path:'artifacts/review-before-matching.png',fullPage:true});
  await page.getByRole('button',{name:'Match this product'}).first().click();
  await page.getByRole('button',{name:'Create as a new product'}).click();
  await expect(page.getByText('Will create',{exact:false})).toBeVisible();
  await page.getByRole('button',{name:'Match this product'}).click();
  await page.locator(`[id="query-${currentOrder}-1"]`).fill('AA');
  await page.getByRole('button',{name:'Search',exact:true}).click();
  await page.getByRole('button',{name:'Select',exact:true}).click();
  await page.locator(`[id="multiplier-${currentOrder}-1"]`).fill('12');
  await page.getByRole('button',{name:'Link selected item'}).click();
  await page.locator(`[id="confirm-${currentOrder}"]`).check();
  await page.getByRole('button',{name:'Import as incoming'}).click();
  await expect.poll(async()=>Object.values((await state()).purchases)[0].status).toBe('done');
  const contents=await worker.evaluate(()=>({items:[...fixtureItems.values()],writes:fixtureWrites}));
  assert.equal(contents.items.length,2);
  assert.equal(contents.writes.filter(w=>w.method==='POST').length,1);
  const battery=contents.items.find(i=>i.name==='AA batteries');
  const record=JSON.parse(Object.entries(battery.attributes).find(([k])=>k.startsWith('oqmCart_purchase_'))[1]);
  assert.equal(record.inventoryQuantity,24);assert.equal(record.status,'incoming');assert.equal(battery.attributes.department,'Physics');
  await amazon.reload();
  await amazon.waitForFunction(()=>!!document.querySelector('#oqm-cart-notice'));
  assert.equal((await worker.evaluate(()=>fixtureWrites.length)),2,'revisit must not write again');
  currentOrder='222-2222222-2222222';currentProducts=[products[0]];
  await amazon.goto('https://www.amazon.com/gp/buy/thankyou/handlers/display.html?fixture=2');
  await expect.poll(async()=>Object.values((await state()).purchases).find(p=>p.orderId===currentOrder)?.status).toBe('done');
  assert.equal((await worker.evaluate(()=>fixtureWrites.filter(w=>w.method==='POST').length)),1,'repeat product should reuse item');
  await page.goto(`chrome-extension://${extensionId}/app.html#history`);
  await expect(page.getByRole('heading',{name:'Order '+currentOrder})).toBeVisible();
  await page.locator(`[id="delivery-${currentOrder}-0"]`).selectOption('received');
  await page.getByRole('button',{name:'Save status',exact:true}).first().click();
  await expect(page.locator('#feedback')).toContainText('Shipment metadata updated');
  await expect(page.locator(`[id="delivery-${currentOrder}-0"]`)).toHaveValue('received');
  await page.screenshot({path:'artifacts/shipment-history.png',fullPage:true});
  // A receipt can reveal more products after its first render. Reopen only new lines.
  currentProducts=products;
  await amazon.goto('https://www.amazon.com/gp/buy/thankyou/handlers/display.html?fixture=late-lines');
  await expect.poll(async()=>Object.values((await state()).purchases).find(p=>p.orderId===currentOrder)?.status).toBe('review');
  await page.goto(`chrome-extension://${extensionId}/app.html#review`);
  await page.locator(`[id="confirm-${currentOrder}"]`).check();
  await page.getByRole('button',{name:'Import as incoming'}).click();
  await expect.poll(async()=>Object.values((await state()).purchases).find(p=>p.orderId===currentOrder)?.status).toBe('done');
  assert.equal(await worker.evaluate(()=>fixtureWrites.filter(w=>w.method==='POST').length),1);
  // A receipt/cart mismatch must pause even when all products have known mappings.
  currentKind='cart';
  await amazon.goto('https://www.amazon.com/gp/cart/view.html');
  await expect.poll(()=>worker.evaluate(async()=>Object.keys((await chrome.storage.session.get('carts')).carts??{}).length)).toBe(1);
  currentKind='confirmation';currentProducts=[products[0]];currentOrder='333-3333333-3333333';
  await amazon.goto('https://www.amazon.com/gp/buy/thankyou/handlers/display.html?fixture=mismatch');
  await expect.poll(async()=>Object.values((await state()).purchases).find(p=>p.orderId===currentOrder)?.status).toBe('review');
  // Manual receipts remain review-only until approved.
  await page.goto(`chrome-extension://${extensionId}/app.html#manual`);
  await page.locator('#manual-order').fill('999-9999999-9999999');
  await page.locator('[name="name-0"]').fill('Manually entered cable');
  await page.locator('[name="asin-0"]').fill(products[0].asin);
  await page.locator('[name="qty-0"]').fill('3');
  await page.locator('#manual-confirm').check();
  await page.getByRole('button',{name:'Save receipt for matching'}).click();
  await expect.poll(async()=>Object.values((await state()).purchases).find(p=>p.orderId==='999-9999999-9999999')?.status).toBe('review');
  await page.goto(`chrome-extension://${extensionId}/app.html#settings`);
  await page.screenshot({path:'artifacts/settings.png',fullPage:true});
  assert.equal(await page.locator('#secret').inputValue(),'','secret must not be rendered back into settings');
  const exported=await page.evaluate(async()=> (await chrome.runtime.sendMessage({type:'EXPORT'})).data);
  assert.ok(!JSON.stringify(exported).includes('fixture-secret'));assert.ok(!JSON.stringify(exported).includes('fixture-token'));
  await page.setViewportSize({width:620,height:900});
  await page.screenshot({path:'artifacts/settings-narrow.png',fullPage:true});
  assert.equal(errors.length,0,errors.join('\n'));
  console.log('PASS: real Chromium extension loading, settings/auth, cart-only capture, receipt review, new-item creation, existing-item mapping, pack conversion, independent readback, duplicate suppression, automatic repeat import, delivery status, late receipt lines, cart mismatch review, manual receipt, export secret exclusion, desktop/narrow UI.');
} finally { if (attached) await attached.close(); else await context.close(); }
