import { rpc } from './rpc';
import type { State } from '../domain';
const status = document.querySelector<HTMLElement>('#status')!;
rpc<State & { connected: boolean }>('STATE').then(s => {
  const orders = Object.values(s.purchases); const count = orders.filter(p => !['done', 'ignored'].includes(p.status)).length;
  document.querySelector('#summary')!.textContent = s.connected ? `${count} purchase${count === 1 ? '' : 's'} waiting · ${orders.filter(p => p.status === 'done').length} imported` : 'Connect to your OQM server to start importing purchases.';
}).catch(e => { status.textContent = e.message; });
document.querySelector('#open')!.addEventListener('click', () => { void chrome.runtime.openOptionsPage(); });
document.querySelector('#scan')!.addEventListener('click', async () => {
  try {
    const [tab] = await chrome.tabs.query({ active: true, currentWindow: true });
    if (!tab?.id || !/^https:\/\/(www\.)?amazon\.com\//.test(tab.url ?? '')) throw new Error('Open an Amazon.com cart, confirmation, or order-details page first.');
    const response = await chrome.tabs.sendMessage(tab.id, { type: 'SCAN_NOW' });
    if (!response?.ok) throw new Error(response?.error ?? 'Could not scan the page.');
    status.textContent = !response.data ? 'This page is not a supported cart or receipt. Open Order details, or use Add a receipt in the inbox.' : response.data.kind === 'cart' ? 'Cart remembered. No inventory was added.' : 'Receipt captured. Open the inbox to review.';
  } catch (e) { status.textContent = e instanceof Error ? e.message.includes('Receiving end') ? 'Refresh the Amazon tab after loading the extension, then scan again.' : e.message : 'Scan failed.'; }
});
