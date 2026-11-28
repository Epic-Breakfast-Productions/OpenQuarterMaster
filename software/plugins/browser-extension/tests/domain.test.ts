import { describe,it,expect } from 'vitest';
import { DEFAULT_SETTINGS, mappingKey, normalizeOrigin, purchaseAttribute, safeImage, validateCapture, validateSettings, type Purchase, type Product } from '../src/domain';
const product: Product={source:'amazon',marketplace:'amazon.com',productId:'B012345678',title:'Cable',url:'https://www.amazon.com/dp/B012345678',quantity:2};
describe('domain boundaries',()=>{
  it('limits server input to an origin',()=>{expect(normalizeOrigin('https://10.1.6.27/')).toBe('https://10.1.6.27');expect(()=>normalizeOrigin('https://u:p@example.com')).toThrow();expect(()=>normalizeOrigin('https://example.com/path')).toThrow();expect(()=>normalizeOrigin('http://example.com')).toThrow();expect(normalizeOrigin('http://localhost:8080')).toBe('http://localhost:8080');});
  it('rejects unsafe config path segments',()=>expect(()=>validateSettings({...DEFAULT_SETTINGS,database:'../default'})).toThrow());
  it('scopes product mappings to the server and database',()=>expect(mappingKey(DEFAULT_SETTINGS,product)).not.toBe(mappingKey({...DEFAULT_SETTINGS,database:'other'},product)));
  it('only retains Amazon image URLs',()=>{expect(safeImage('https://m.media-amazon.com/images/abc.jpg')).toBeTruthy();expect(safeImage('https://evil.example/image')).toBeUndefined();});
  it('sanitizes external product links and invalid quantity',()=>{const c=validateCapture({kind:'confirmation',orderId:'123-1234567-1234567',sourceUrl:'https://www.amazon.com/gp/buy/thankyou/',products:[{...product,url:'https://evil.example',quantity:-2}],warnings:[]});expect(c.products[0].url).toBe(product.url);expect(c.products[0].quantity).toBeNull();});
  it('rejects unbounded payloads and duplicate ASINs',()=>{const c={kind:'confirmation' as const,sourceUrl:'https://www.amazon.com/gp/buy/thankyou/',products:[product,product],warnings:[]};expect(()=>validateCapture(c)).toThrow();});
  it('uses stable per-order/per-product remote markers',async()=>{const p={key:'amazon:amazon.com:123-1234567-1234567'} as Purchase;const a=await purchaseAttribute(p,product);expect(a).toBe(await purchaseAttribute(p,product));expect(a).not.toBe(await purchaseAttribute({...p,key:'different'},product));expect(a).toMatch(/^oqmCart_purchase_[a-f0-9]{64}$/);});
});
