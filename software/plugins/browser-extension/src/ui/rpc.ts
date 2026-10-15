export async function rpc<T = any>(type: string, data: Record<string, unknown> = {}): Promise<T> {
  const r = await chrome.runtime.sendMessage({ type, ...data });
  if (!r?.ok) throw new Error(r?.error ?? 'The extension did not respond. Reload it from chrome://extensions.');
  return r.data;
}
export const esc = (v: unknown) => String(v ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]!));
