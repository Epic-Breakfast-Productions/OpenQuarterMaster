const CACHE = "hello-pwa-v1";

const ASSETS = ["./", "./some-page", "./logo.svg"];

console.log("Setting up PWA listeners");

self.addEventListener("install", (e) => {
	e.waitUntil(caches.open(CACHE).then((c) => c.addAll(ASSETS)));
	self.skipWaiting();
});

self.addEventListener("activate", (e) => {
	e.waitUntil(
		caches.keys().then((keys) => {
			console.log("Setting up PWA listeners activated");
			Promise.all(keys.filter((k) => k !== CACHE).map((k) => caches.delete(k)))
		})
	);
	self.clients.claim();
});

// Cache-first with network fallback
self.addEventListener("fetch", (e) => {
	if (e.request.method !== "GET") return;
	e.respondWith(
		caches.match(e.request).then((cached) =>
			cached ||
			fetch(e.request).then((res) => {
				console.log("Fetching");
				const copy = res.clone();
				caches.open(CACHE).then((c) => c.put(e.request, copy));
				return res;
			})
		)
	);
});
