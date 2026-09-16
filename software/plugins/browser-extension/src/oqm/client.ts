import { sourceAttribute, type InventoryItem, type Product, type Settings } from '../domain';

export class ApiError extends Error {
  constructor(message: string, public status = 0, public uncertain = false) { super(message); }
}
export type TokenProvider = () => Promise<string>;
export class OqmClient {
  constructor(readonly settings: Settings, private token: TokenProvider, private fetcher: typeof fetch = fetch) {}
  async request<T>(path: string, method = 'GET', body?: unknown): Promise<T> {
    let response: Response;
    let token: string;
    try { token = await this.token(); }
    catch (e) { throw new ApiError(e instanceof Error ? e.message : 'Authentication failed.', 401, false); }
    try {
      response = await this.fetcher(`${this.settings.origin}/core/api/api/v1${path}`, {
        method, headers: { Authorization: `Bearer ${token}`, Accept: 'application/json', ...(body ? { 'Content-Type': 'application/json' } : {}) },
        body: body ? JSON.stringify(body) : undefined, redirect: 'error', credentials: 'omit', cache: 'no-store', signal: AbortSignal.timeout(12000),
      });
    } catch {
      throw new ApiError('Cannot reach OQM. Check campus/VPN access, host permission and the server HTTPS certificate.', 0, method !== 'GET');
    }
    if (!response.ok) throw new ApiError(response.status === 401 ? 'Session rejected. Reconnect in Settings.' : response.status === 403 ? 'This client does not have permission for this OQM operation.' : `OQM returned HTTP ${response.status}.`, response.status, method !== 'GET' && response.status >= 500);
    try { return await response.json() as T; } catch { throw new ApiError('OQM returned an unexpected response.', response.status, method !== 'GET'); }
  }
  private get items(): string { return `/db/${encodeURIComponent(this.settings.database)}/inventory/item`; }
  databases(): Promise<{ name: string; displayName: string }[]> { return this.request('/inventory/manage/db'); }
  self(): Promise<{ name: string }> { return this.request('/interacting-entity/self'); }
  async search(name: string): Promise<InventoryItem[]> {
    const result = await this.request<{ results: InventoryItem[] }>(`${this.items}?name=${encodeURIComponent(name)}&pageSize=20&pageNum=1&sortBy=name&sortType=ASCENDING`);
    return result.results ?? [];
  }
  async byAttribute(key: string): Promise<InventoryItem[]> {
    // OQM 6.3 requires parallel key/value lists. A space means "exists"; an empty
    // query value is discarded by its parameter binding and triggers HTTP 500.
    const result = await this.request<{ results: InventoryItem[]; numResultsForEntireQuery?: number }>(`${this.items}?attributeKey=${encodeURIComponent(key)}&attributeValue=%20&pageSize=100&pageNum=1&sortBy=name&sortType=ASCENDING`);
    if ((result.numResultsForEntireQuery ?? 0) > 100) throw new ApiError('Too many matching source records. Resolve the duplicate bindings in OQM.');
    return (result.results ?? []).filter(item => item.attributes && key in item.attributes);
  }
  findSource(product: Product): Promise<InventoryItem[]> { return this.byAttribute(sourceAttribute(product)); }
  getItem(id: string): Promise<InventoryItem> {
    if (!/^[a-f\d]{24}$/i.test(id)) throw new ApiError('Invalid OQM item ID.');
    return this.request(`${this.items}/${id}`);
  }
  createItem(body: unknown): Promise<InventoryItem> { return this.request(this.items, 'POST', body); }
  patchItem(id: string, body: unknown): Promise<InventoryItem> { return this.request(`${this.items}/${encodeURIComponent(id)}`, 'PUT', body); }
}

export async function authenticate(settings: Settings, secret: string, fetcher: typeof fetch = fetch): Promise<{ token: string; expiresAt: number }> {
  if (!secret) throw new Error('Enter the client secret in Settings to connect.');
  let response: Response;
  try {
    response = await fetcher(`${settings.origin}/infra/keycloak/realms/${encodeURIComponent(settings.realm)}/protocol/openid-connect/token`, {
      method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({ grant_type: 'client_credentials', client_id: settings.clientId, client_secret: secret }),
      redirect: 'error', credentials: 'omit', cache: 'no-store', signal: AbortSignal.timeout(12000),
    });
  } catch { throw new Error('Cannot reach Keycloak. Check campus/VPN access, host permission and HTTPS certificate trust.'); }
  if (!response.ok) throw new Error(`Keycloak rejected the connection (HTTP ${response.status}). Check the realm, client ID, secret and service-account configuration.`);
  const data = await response.json() as { access_token?: string; expires_in?: number };
  if (!data.access_token || !data.expires_in) throw new Error('Keycloak did not return a usable access token.');
  return { token: data.access_token, expiresAt: Date.now() + Math.max(1, data.expires_in - 30) * 1000 };
}
