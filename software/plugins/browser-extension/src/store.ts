import { DEFAULT_SETTINGS, type State } from './domain';
export const initStorage = async () => {
  await chrome.storage.local.setAccessLevel({ accessLevel: 'TRUSTED_CONTEXTS' });
  await chrome.storage.session.setAccessLevel({ accessLevel: 'TRUSTED_CONTEXTS' });
};
export async function readState(): Promise<State> {
  const { state } = await chrome.storage.local.get<{ state?: State }>('state');
  return state ?? { settings: { ...DEFAULT_SETTINGS }, purchases: {}, mappings: {} };
}
export async function saveState(state: State): Promise<void> { await chrome.storage.local.set({ state }); }
export const credentialKey = (s: { origin: string; realm: string; clientId: string }) => `${s.origin}|${s.realm}|${s.clientId}`;
export async function getSecret(key: string): Promise<string> {
  const { credentials } = await chrome.storage.session.get<{credentials?: {key:string;secret:string}}>('credentials');
  if (credentials?.key === key) return credentials.secret;
  const { savedCredential } = await chrome.storage.local.get<{savedCredential?: {key:string;secret:string}}>('savedCredential');
  return savedCredential?.key === key ? savedCredential.secret : '';
}
