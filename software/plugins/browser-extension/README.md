# Simple Instructions

1. open chrome://extensions
2. Make sure developer mode is enabled, also make sure to delete any previous installations of this extension
3. Select 'load unpacked'
4. Select the /dist folder NOT the browser extension folder

The extension won't open in a new tab or on the extensions page

## AI GENERATED INFO AND INSTRUCTIONS BELOW:

# OQM Cart Companion

Chrome/Chromium Manifest V3 extension written in TypeScript. It turns completed purchases into **incoming purchase records attached to OpenQuarterMaster items**. It does not place orders, charge payment methods, or count an unpurchased cart as inventory.

No Python process, hosted middleware, or Chrome Web Store registration is needed for the local prototype. The bundled extension has no runtime package dependencies.

## Install the built extension

1. Keep this project folder somewhere permanent. The ready-to-load extension is in `dist/`.
2. In Chrome, enter `chrome://extensions` in the address bar.
3. Enable **Developer mode**, choose **Load unpacked**, and select `dist` — not the project root and not a ZIP file.
4. Pin **OQM Cart Companion** using Chrome's extensions menu. Open it, choose **Open purchase inbox**, then **Connection settings** in the sidebar.
5. The supplied defaults are:

   | Setting | Value |
   | --- | --- |
   | Server origin | `https://10.1.6.27` |
   | Keycloak realm | `oqm` |
   | Client ID | `browser-plugin` |
   | Database | `default` |

6. Paste the team's **browser-plugin client secret** into the password field and click **Save & connect**. The secret is deliberately absent from the source, ZIP and built extension. It is not your Linux password or your Amazon password.
7. Refresh any Amazon tabs that were already open before installation.

You must be on a network/VPN that can reach the campus server. Chrome must trust its HTTPS certificate. The extension cannot bypass certificate validation. Open the server in Chrome to inspect any warning; use a certificate trusted by Chrome or the server/campus CA supplied through an authorized channel. Do not disable browser security globally. The command-line integration test's explicit certificate exception does **not** mean Chrome will already trust this server.

An institution-managed browser may prohibit unpacked extensions. That restriction requires institutional permission; this project does not bypass it.

## How to use it

- Shop normally on **Amazon.com in English**. Reading a cart only creates a temporary snapshot in the current browser session.
- After the order is placed, the confirmation page triggers receipt capture. The extension reads visible product rows, ASINs, quantities, titles, variants and available price/image metadata. It does not collect your payment details, address, cookies or Amazon password.
- An unfamiliar ASIN opens a review window. Choose **Match this product**, then search OQM for an existing item or choose **Create as a new product**. Search results are suggestions, not automatic identity decisions. Check the variant and package size yourself.
- Set the quantity conversion when linking. For example, **2 purchased packs × 12 batteries per pack = 24 incoming inventory units**. Newly created items initially track packages as `units`; link an existing item if you want another unit or package conversion.
- Resolve or skip each line, confirm that the order and quantities are correct, then choose **Import as incoming**. Merely choosing a new product does not write it yet.
- Future orders containing only remembered ASINs and explicit quantities are automatically imported when the reader finds no ambiguity. Turn this off in Settings if you want to review every order.
- For an unrecognized receipt layout, open Amazon's order-details page and click **Scan this Amazon page** in the extension popup, or use **Add a receipt**. Manual/order-details imports always require review.
- **Shipment history** shows imported orders. You can mark purchase lines incoming, received or cancelled, and read their current OQM attributes. Delivery status is manual; this version does not monitor Amazon deliveries or cancellations.

A missing order ID, uncertain quantity, repeated ASIN rows, multiple order IDs, or a mismatch with the recent cart prevents automatic import. Orders spanning multiple Amazon order IDs need separate manual receipts. If Amazon reveals additional rows after an initial import, the new lines reopen for review without reimporting the completed lines. If an already imported quantity was wrong, correct its purchase metadata in OQM; the extension intentionally does not silently rewrite it from a later page scan.

## What “incoming” means in OQM

An **OQM item** describes a kind of thing, such as an AA battery. An **Amazon ASIN** describes a particular marketplace product, such as a 12-pack of those batteries. A **product link** connects the ASIN to the OQM item and records its package conversion. A **purchase record** describes one order's quantity of that product.

The current integration uses the existing OQM item database, not a new shipping database or a custom server plugin:

- Product name and optional variant description on newly created OQM items.
- The canonical Amazon product URL in OQM `associatedLinks`.
- Keywords `oqm-cart`, `amazon`, and `incoming` while at least one recorded line is incoming.
- An `oqmCart_source_...` attribute containing the ASIN-to-item mapping and conversion.
- One `oqmCart_purchase_<sha256>` attribute per order/ASIN, containing JSON with order ID, URLs, purchased package quantity, converted inventory quantity, unit, capture time and status. Available title, variant, displayed price and image URL are included. Prices remain text because taxes, currencies and discounts cannot safely be inferred from an arbitrary receipt row.

**Incoming quantity is metadata, not on-hand stock.** New item types start with zero stock and no storage block. Even marking a shipment “received” changes only the purchase metadata. Receive actual stock using OQM's normal inventory/storage workflow. This separation prevents ordered-but-undelivered goods from inflating usable inventory.

Mappings are cached locally for automatic repeat purchases and also saved in OQM for recovery and suggestions. Purchase history UI is local to this Chrome profile; it is not a global all-users dashboard. “Read shipment records from OQM” reads current remote data for that order, while the status selector remembers the last status saved from this profile. “Forget locally” removes only the local shortcut, not the binding stored in OQM.

## Architecture and adding websites

```text
AmazonAdapter → normalized Capture → background review / durable queue
                                           ↓
                                    OqmClient + syncLine
                                           ↓
                              Keycloak token → OQM Core API
```

| File | Responsibility |
| --- | --- |
| `src/adapters/site-adapter.ts` | Small `SiteAdapter` interface: URL matching and DOM-to-Capture extraction |
| `src/adapters/amazon.ts` | Amazon-specific page recognition and product extraction |
| `src/adapters/index.ts` | Adapter registry |
| `src/domain.ts` | Shared products, captures, mappings, orders, validation and stable keys |
| `src/content.ts` | Debounced DOM observation and isolated on-page notice; no credentials |
| `src/background.ts` | Message authorization, review rules, serialized durable queue and retry alarms |
| `src/store.ts` | Trusted-context local/session storage and credential handling |
| `src/oqm/client.ts` | Keycloak authentication and typed OQM HTTP operations |
| `src/oqm/sync.ts` | Product creation/linking, conversion, remote order markers and reconciliation |
| `src/ui/` | Popup, settings, matching, manual receipts and shipment history |

The intermediary is TypeScript running in Chrome's extension service worker. Site adapters know nothing about OQM credentials or database writes. The UI and queue consume normalized products rather than Amazon DOM elements.

To add another store, implement `SiteAdapter`, register it, add exact manifest content-script host matches, extend the **trusted capture validation and sender allowlist** in `domain.ts`/`background.ts`, and generalize the currently Amazon-specific order IDs/URLs and manual-entry UI. Add fixture tests for confirmation vs. cart vs. unrelated pages, quantities, variants and split orders. Adding only a selector class is deliberately insufficient to grant a new website write authority.

## OQM API contract

Verified with the supplied campus OQM 6.3.0 deployment:

```text
POST /infra/keycloak/realms/oqm/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded
grant_type=client_credentials&client_id=browser-plugin&client_secret=<secret>

GET /core/api/api/v1/inventory/manage/db
GET /core/api/api/v1/interacting-entity/self
GET /core/api/api/v1/db/default/inventory/item?name=Battery&pageNum=1&pageSize=20&sortBy=name&sortType=ASCENDING
GET /core/api/api/v1/db/default/inventory/item?attributeKey=<key>&attributeValue=%20&pageNum=1&pageSize=100&sortBy=name&sortType=ASCENDING
POST /core/api/api/v1/db/default/inventory/item
GET /core/api/api/v1/db/default/inventory/item/<itemId>
PUT /core/api/api/v1/db/default/inventory/item/<itemId>
Authorization: Bearer <short-lived access token>
Content-Type: application/json   (for POST/PUT)
```

The repeated `/api/api/` is correct for this deployment. The client secret is exchanged with Keycloak, not sent as the inventory API bearer token. Keycloak identifies the application/service account; it is part of this OQM authentication setup, not a Linux shell login. Client IDs are names, not network addresses.

Search details matter: page numbers begin at **1**, the client sends explicit sorting, and OQM's attribute key/value query lists must have matching lengths. `%20` supplies the blank value meaning “attribute exists”; an entirely empty query value caused HTTP 500 on this deployment.

References: [OQM project](https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster), [attribute search implementation](https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/blob/main/software/core/oqm-core-api/src/main/java/tech/ebp/oqm/core/api/service/mongo/search/SearchUtils.java), and the deployed server's `/core/api/q/openapi/` schema. Paths for another deployment may require changing `OqmClient`; host, realm, client ID and database are configurable in Settings.

## Reliability and security boundaries

- Only completed-confirmation captures or explicitly reviewed receipts can enter the write queue. Content scripts cannot invoke connection settings, searches or arbitrary writes. Main-frame sender URLs are checked against the supported store.
- Writes run one at a time per extension profile. Intent is persisted **before** network writes. A stable remote order-line marker prevents ordinary revisits, retries and a replay after local history loss from adding the same purchase again.
- A creation whose response was lost is looked up by its remote marker. If it cannot be found, creation pauses for reconciliation instead of blindly repeating POST. Partial orders retain the completed lines.
- Timeouts are bounded; selected network/429/5xx failures retry with backoff up to five attempts. Other errors require attention. Chrome must be running and credentials available to sync; it is not a server-side daemon.
- This is **not distributed exactly-once delivery**. OQM does not provide a server-enforced unique order key or compare-and-swap here. Two profiles importing the same product concurrently could duplicate an item or overwrite concurrent metadata. Use **one active importer profile per shared database** for this prototype. Production multi-user use needs a server-side uniqueness/transaction boundary. Avoid concurrent edits to extension-owned attributes.
- Product links are namespaced by server, realm and database. Already captured orders retain their original target after settings changes. Credentials are bound to origin/realm/client ID; switching clients may require reconnecting before an old queue can run.
- Secrets/tokens are session-only by default. “Remember” stores the secret in this browser profile's local extension storage; that is **not an encrypted secrets vault**. Local and session storage are restricted to trusted extension contexts. Tokens and secrets are not placed in the page DOM, logged or exported.
- The existing service-account secret makes this a **controlled-team prototype**, not a safe public-store credential model. Do not distribute it preconfigured. A public release should use individual user authorization with PKCE, or a small server-side bridge that holds confidential credentials. Rotate credentials previously exposed in screenshots/chat through your authorized administrator.
- No analytics, remote scripts, background Amazon crawling, purchases or server/network scans. Captured purchase data is stored in the extension and sent to the configured OQM host. The reader stores image URLs as metadata; it does not download images.
- Only Amazon.com English layouts are supported initially. Amazon can change its HTML, A/B-test checkout, hide line items or show different business/digital/subscription flows. Tested fixtures are not a guarantee of coverage for your account. Always compare the first real receipt with the review screen. Manual entry is the fallback.
- Local history is capped at 1,000 orders and remains subject to Chrome's storage quota. Export before clearing history. Exports contain purchase information but no credentials; they are archival JSON, not an implemented one-click restore feature. Removing the extension removes its local state but does not delete OQM data.

## Development and validation

Use Node.js 22 or newer and npm. Commands from the project root:

```sh
npm ci
npm run check
```

This type-checks, runs the tests and builds `dist`. After source changes, rebuild, click Reload on the extension in Chrome, and refresh Amazon tabs. Development dependencies are not shipped in `dist`.

Synthetic browser integration test:

```sh
npx playwright install chromium
npm run test:browser
```

It creates an isolated profile, intercepts Amazon fixture pages, and simulates OQM in memory. It makes no actual purchase or campus inventory write. Screenshots go to `artifacts/`. On this Windows machine the downloaded Chrome-for-Testing binary had a side-by-side startup error; the test runs successfully in installed Chromium-based Edge with a fresh isolated profile:

```powershell
$env:OQM_TEST_BROWSER = 'C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe'
npm run test:browser
Remove-Item Env:OQM_TEST_BROWSER
```

Opt-in live API test (this **writes one clearly labeled zero-stock item**): set `OQM_CLIENT_SECRET` securely in the current shell, optionally `OQM_ORIGIN` and `OQM_DATABASE`, then run:

```sh
npm run test:live -- --allow-test-write
```

Only for the known private test server's untrusted certificate, the explicit `--insecure-test-certificate` flag scopes a TLS exception to this test client. It is not part of the extension. The test creates two synthetic purchase records on one item, checks replay/reuse/readback, then marks both cancelled. It leaves that labeled test item for inspection and never adds physical stock. Clear the secret environment variable afterward. Do not run repeatedly merely to test the UI.

### Validation performed on 2026-09-14

- TypeScript checks and 43 unit tests passed.
- Real extension loaded and exercised in isolated headless Chromium-based Edge with synthetic Amazon/OQM fixtures. Covers matching, creation, linking, package conversion, repeat import, duplicate prevention, delivery status, late receipt lines, cart mismatch, manual receipt, exports and responsive UI.
- Actual campus API test passed: token authentication, item creation, readback, repeated-order deduplication, ASIN lookup, repeat purchase, metadata preservation, attribute queries and delivery status.
- The live test left item **`6aa84b10dfc308df2ee2953c`**, named **TEST - OQM Cart Companion - 2026-09-14T19:29:20.759Z**, in `default`. Both synthetic purchase records are cancelled; physical stock added is zero. It is safe for your team to delete this test item after inspection.
- No genuine Amazon checkout was performed. Installation into your personal Chrome profile and its certificate trust still need your participation.
