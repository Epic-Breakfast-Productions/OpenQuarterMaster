// Opt-in: creates ONE clearly labeled zero-stock test item in the configured OQM database.
import { build } from 'esbuild';
import { mkdir } from 'node:fs/promises';
import { resolve } from 'node:path';
import { pathToFileURL } from 'node:url';
import { request } from 'node:https';
import { randomBytes, randomInt } from 'node:crypto';
import assert from 'node:assert/strict';

if (!process.argv.includes('--allow-test-write')) throw new Error('Pass --allow-test-write to create one zero-stock test item.');
if (!process.env.OQM_CLIENT_SECRET) throw new Error('Set OQM_CLIENT_SECRET for this process only.');
await mkdir('artifacts', { recursive: true });
await build({stdin:{contents:"export * from './src/oqm/client'; export * from './src/oqm/sync'; export * from './src/domain';",resolveDir:process.cwd()},bundle:true,platform:'node',format:'esm',outfile:'artifacts/live-core.mjs'});
const core = await import(pathToFileURL(resolve('artifacts/live-core.mjs')).href);
const settings = core.validateSettings({...core.DEFAULT_SETTINGS, origin:process.env.OQM_ORIGIN || core.DEFAULT_SETTINGS.origin, database:process.env.OQM_DATABASE || 'default'});
const writes = [];
// Certificate bypass is opt-in and scoped to this one test client's origin, never global.
const fetcher = (input, init = {}) => new Promise((ok, fail) => {
  const url = new URL(String(input));
  if (url.origin !== settings.origin) { fail(new Error('Unexpected host')); return; }
  const req = request(url, {method:init.method,headers:init.headers,signal:init.signal,rejectUnauthorized:!process.argv.includes('--insecure-test-certificate')}, res => {
    const chunks=[];
    res.on('data',chunk=>chunks.push(chunk)); res.on('error',fail);
    res.on('end',()=>ok(new Response(Buffer.concat(chunks),{status:res.statusCode,headers:{'Content-Type':String(res.headers['content-type'] || '')}})));
  });
  req.on('error',fail);
  if (init.method && init.method !== 'GET' && !url.pathname.endsWith('/token')) writes.push(init.method);
  if (init.body) req.write(String(init.body)); req.end();
});
const {token} = await core.authenticate(settings,process.env.OQM_CLIENT_SECRET,fetcher);
const api = new core.OqmClient(settings,async()=>token,fetcher);
const who = await api.self();
const asin = 'T'+randomBytes(5).toString('hex').slice(0,9).toUpperCase();
const orderId = () => `000-${String(Date.now()).slice(-7)}-${String(randomInt(1000000,10000000))}`;
function makeOrder(quantity) {
  const id=orderId();
  return {key:`amazon:amazon.com:${id}`,orderId:id,sourceUrl:'https://www.amazon.com/gp/buy/thankyou/',capturedAt:new Date().toISOString(),target:settings,lines:[{product:{source:'amazon',marketplace:'amazon.com',productId:asin,title:`TEST - OQM Cart Companion - ${new Date().toISOString()}`,quantity,url:`https://www.amazon.com/dp/${asin}`},phase:'pending',choice:'new'}],warnings:['Synthetic integration test; not an actual Amazon purchase.'],approved:true,status:'queued',attempts:0};
}
const first=makeOrder(2), replay=structuredClone(first);
await core.syncLine(api,first,first.lines[0],async()=>{});
const itemId=first.lines[0].itemId;
console.log(`Created test item: ${itemId}`);
await core.syncLine(api,replay,replay.lines[0],async()=>{});
assert.equal(replay.lines[0].itemId,itemId);
assert.equal(writes.filter(w=>w==='POST').length,1,'Replay must not create again');
const before=await api.getItem(itemId);
await api.patchItem(itemId,{attributes:{...before.attributes,oqmCart_testOnly:'true'}});
const second=makeOrder(3);
await core.syncLine(api,second,second.lines[0],async()=>{});
assert.equal(second.lines[0].itemId,itemId,'ASIN binding must recover the same product');
const saved=await api.getItem(itemId);
assert.equal(saved.attributes.oqmCart_testOnly,'true','Unrelated metadata must survive');
for(const p of [first,second]) {
  const marker=await core.purchaseAttribute(p,p.lines[0].product);
  assert.equal(JSON.parse(saved.attributes[marker]).inventoryQuantity,p.lines[0].product.quantity);
  assert.equal((await api.byAttribute(marker))[0].id,itemId);
  await core.setDeliveryStatus(api,p,p.lines[0],'cancelled');
}
const final=await api.getItem(itemId);
assert.ok(!final.keywords.includes('incoming'));
assert.equal(final.storageBlocks.length,0,'No stock locations or physical quantity may be created');
assert.equal(writes.filter(w=>w==='POST').length,1);
console.log(JSON.stringify({result:'PASS',identity:who.name,itemId,name:final.name,database:settings.database,testPurchases:2,status:'cancelled',physicalStockAdded:0,checks:['authentication','create','readback','replay deduplication','ASIN lookup','repeat-order update','metadata preservation','attribute query','delivery status']},null,2));
