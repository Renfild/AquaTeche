const CACHE_NAME = "aquatech-v20260906-dlfix";
const PRECACHE_ASSETS = [
  "/",
  "/index.html",
  "/start.html",
  "/rods.html",
  "/cases.html",
  "/market.html",
  "/assets/css/site.css",
  "/assets/js/site.js",
  "/assets/logo.png",
  "/favicon.ico"
];

self.addEventListener("install", (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => cache.addAll(PRECACHE_ASSETS)).catch(() => {})
  );
  self.skipWaiting();
});

self.addEventListener("activate", (event) => {
  event.waitUntil(
    caches.keys().then((keys) =>
      Promise.all(
        keys.filter((key) => key !== CACHE_NAME).map((key) => caches.delete(key))
      )
    )
  );
  self.clients.claim();
});

self.addEventListener("fetch", (event) => {
  const url = new URL(event.request.url);

  // Skip APIs, downloads, and non-GET requests
  if (
    event.request.method !== "GET" ||
    url.pathname.startsWith("/api/") ||
    url.pathname.startsWith("/dl/")
  ) {
    return;
  }

  event.respondWith(
    caches.match(event.request).then((cached) => {
      // Network-first for dynamic content, fallback to cache
      return fetch(event.request)
        .then((response) => {
          if (response && response.status === 200 && response.type === "basic") {
            const copy = response.clone();
            caches.open(CACHE_NAME).then((cache) => cache.put(event.request, copy));
          }
          return response;
        })
        .catch(() => cached || (event.request.mode === "navigate" ? caches.match("/index.html") : null));
    })
  );
});
