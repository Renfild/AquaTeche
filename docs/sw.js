const CACHE_NAME = "aquatech-v20260915-perf2";
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

const STALE_WHILE_REVALIDATE =
  /\.(?:css|js|woff2?|png|jpe?g|webp|svg|ico|gif)$/;

self.addEventListener("fetch", (event) => {
  const url = new URL(event.request.url);

  // Skip APIs, downloads, cross-origin and non-GET requests
  if (
    event.request.method !== "GET" ||
    url.origin !== self.location.origin ||
    url.pathname.startsWith("/api/") ||
    url.pathname.startsWith("/dl/")
  ) {
    return;
  }

  // Versioned static assets: serve from cache instantly, refresh in background.
  if (STALE_WHILE_REVALIDATE.test(url.pathname)) {
    event.respondWith(
      caches.match(event.request).then((cached) => {
        const network = fetch(event.request)
          .then((response) => {
            if (response && response.status === 200 && response.type === "basic") {
              const copy = response.clone();
              caches.open(CACHE_NAME).then((cache) => cache.put(event.request, copy));
            }
            return response;
          })
          .catch(() => cached);
        return cached || network;
      })
    );
    return;
  }

  event.respondWith(
    caches.match(event.request).then((cached) => {
      // Network-first for HTML and JSON (site copy, versions, news)
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
