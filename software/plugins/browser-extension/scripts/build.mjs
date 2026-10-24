import { build } from 'esbuild';
import { mkdir, cp, writeFile } from 'node:fs/promises';
await mkdir('dist', { recursive: true });
await build({ entryPoints: { background: 'src/background.ts', content: 'src/content.ts', app: 'src/ui/app.ts', popup: 'src/ui/popup.ts' }, bundle: true, outdir: 'dist', format: 'iife', target: 'chrome120', minify: true, legalComments: 'none' });
await cp('public', 'dist', { recursive: true });
await writeFile('dist/manifest.json', JSON.stringify({
  manifest_version: 3, name: 'OQM Cart Companion', version: '0.1.0', minimum_chrome_version: '120',
  description: 'Turn Amazon purchases into reviewed, linked incoming inventory in OpenQuarterMaster.',
  permissions: ['storage', 'alarms', 'activeTab'],
  host_permissions: ['https://10.1.6.27/*'],
  optional_host_permissions: ['https://*/*', 'http://localhost/*', 'http://127.0.0.1/*'],
  background: { service_worker: 'background.js' },
  action: { default_title: 'OQM Cart Companion', default_popup: 'popup.html' },
  options_page: 'app.html',
  content_scripts: [{ matches: ['https://www.amazon.com/*', 'https://amazon.com/*'], js: ['content.js'], run_at: 'document_idle' }],
  content_security_policy: { extension_pages: "script-src 'self'; object-src 'none'; base-uri 'none'" }
}, null, 2));
console.log('Load the dist directory as an unpacked Chrome/Chromium extension.');
