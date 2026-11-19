import type { Capture } from '../domain';
/** Retailers produce data only. Authentication, matching and persistence live elsewhere. */
export interface SiteAdapter {
  readonly id: string;
  matches(url: URL): boolean;
  capture(document: Document, url: URL): Capture | null;
}
