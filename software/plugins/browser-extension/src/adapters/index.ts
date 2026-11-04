import { AmazonAdapter } from './amazon';
import type { SiteAdapter } from './site-adapter';
export const adapters: SiteAdapter[] = [new AmazonAdapter()];
export const adapterFor = (url: URL) => adapters.find(a => a.matches(url));
