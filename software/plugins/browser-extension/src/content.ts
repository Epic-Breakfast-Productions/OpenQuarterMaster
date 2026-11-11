import { adapterFor } from './adapters';
import type { Capture } from './domain';

let last = '';
let timer: ReturnType<typeof setTimeout> | undefined;
let scanning = false;
let stopped = false;
function notice(message: string) {
  if (document.getElementById('oqm-cart-notice')) return;
  const host = document.createElement('div'); host.id = 'oqm-cart-notice';
  const shadow = host.attachShadow({ mode: 'closed' });
  const style = document.createElement('style'); style.textContent = ':host{position:fixed;bottom:24px;right:24px;z-index:2147483647}section{max-width:330px;padding:18px;border-radius:14px;background:#153e39;color:white;box-shadow:0 8px 32px #0004;font:14px/1.5 system-ui}button{padding:7px 12px;margin:10px 8px 0 0;border:0;border-radius:7px;cursor:pointer}';
  const section = document.createElement('section'); const label = document.createElement('div'); label.textContent = message;
  const open = document.createElement('button'); open.textContent = 'Review in OQM Companion'; open.onclick = () => { void chrome.runtime.sendMessage({ type: 'OPEN_REVIEW' }); host.remove(); };
  const close = document.createElement('button'); close.textContent = 'Dismiss'; close.onclick = () => host.remove();
  section.append(label, open, close); shadow.append(style, section); document.documentElement.append(host);
}
async function scan(manual = false): Promise<Capture | null> {
  const url = new URL(location.href); const adapter = adapterFor(url);
  const capture = adapter?.capture(document, url) ?? null;
  if (!capture || (capture.kind === 'order-detail' && !manual)) return capture;
  const fingerprint = JSON.stringify(capture);
  if (!manual && fingerprint === last) return capture;
  const response = await chrome.runtime.sendMessage({ type: 'CAPTURE', capture, manual });
  if (!response?.ok) throw new Error(response?.error ?? 'Extension could not save this order.');
  last = fingerprint;
  if (capture.kind !== 'cart') notice(response.data?.message ?? 'Purchase saved for review.');
  return capture;
}
function schedule() {
  if (stopped || timer) return;
  timer = setTimeout(async () => {
    timer = undefined;
    if (scanning) return;
    scanning = true;
    try { await scan(); } catch { /* Page navigation or extension reload: manual scan remains available. */ }
    finally { scanning = false; }
  }, 1400);
}
chrome.runtime.onMessage.addListener((message, _sender, reply) => {
  if (message?.type !== 'SCAN_NOW') return;
  scan(true).then(c => reply({ ok: true, data: c ? { kind: c.kind, count: c.products.length } : null })).catch(e => reply({ ok: false, error: String(e.message) }));
  return true;
});
const observer = new MutationObserver(schedule);
observer.observe(document.documentElement, { childList: true, subtree: true, characterData: true, attributes: true, attributeFilter: ['data-quantity', 'value'] });
document.addEventListener('change', schedule);
window.addEventListener('popstate', schedule);
window.addEventListener('pagehide', () => { stopped = true; observer.disconnect(); if (timer) clearTimeout(timer); });
schedule();
