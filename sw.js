// Caches the app so it opens offline once installed. Bump VERSION when you update index.html.
const VERSION = 'interval-timer-v9';
const FILES = ['./', './index.html', './manifest.webmanifest', './icon-192.png', './icon-512.png', './icon-maskable-512.png'];
const OPTIONAL = ['./frogue-messages.js', './sync-config.js'];   // missing is fine
self.addEventListener('install', (e) => { e.waitUntil(caches.open(VERSION).then((c) => c.addAll(FILES).then(() => Promise.all(OPTIONAL.map((u) => c.add(u).catch(() => {}))))).then(() => self.skipWaiting())); });
self.addEventListener('activate', (e) => {
  e.waitUntil(caches.keys().then((ks) => Promise.all(ks.filter((k) => k !== VERSION).map((k) => caches.delete(k)))).then(() => self.clients.claim()));
});
self.addEventListener('fetch', (e) => {
  const req = e.request;
  if (req.method !== 'GET') return;
  // only ever touch this app's own files. Anything else (e.g. the live TV/phone link to Firebase) goes straight to the network.
  if (new URL(req.url).origin !== self.location.origin) return;
  // network first (so updates show up), fall back to the cache when offline
  e.respondWith(fetch(req).then((r) => { if (r.ok) { const copy = r.clone(); caches.open(VERSION).then((c) => c.put(req, copy)); } return r; })
    .catch(() => caches.match(req).then((m) => m || (req.mode === 'navigate' ? caches.match('./index.html') : Response.error()))));
});
