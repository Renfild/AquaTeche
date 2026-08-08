/**
 * AquaTech Worker: same-origin /api/* + static docs assets.
 * Reachable as https://aquatech.santcrail.workers.dev (pages.dev is blocked in some networks).
 */
import { onRequestPost as registerPost } from "../functions/api/register.js";
import { onRequestPost as loginPost } from "../functions/api/login.js";
import { onRequestPost as logoutPost } from "../functions/api/logout.js";
import { onRequestGet as meGet } from "../functions/api/me.js";
import { onRequestGet as playersGet } from "../functions/api/players.js";
import { onRequestGet as catalogGet } from "../functions/api/catalog.js";
import {
  onRequestGet as purchaseGet,
  onRequestPost as purchasePost,
} from "../functions/api/purchase.js";
import {
  onRequestGet as profileGet,
  onRequestPatch as profilePatch,
} from "../functions/api/profiles/[nick].js";

function ctx(request, env, params = {}) {
  return { request, env, params };
}

function normalizePath(pathname) {
  if (pathname.length > 1 && pathname.endsWith("/")) return pathname.slice(0, -1);
  return pathname || "/";
}

async function handleApi(request, env) {
  const url = new URL(request.url);
  const path = normalizePath(url.pathname);
  const method = request.method.toUpperCase();

  if (path === "/api/register" && method === "POST") return registerPost(ctx(request, env));
  if (path === "/api/login" && method === "POST") return loginPost(ctx(request, env));
  if (path === "/api/logout" && method === "POST") return logoutPost(ctx(request, env));
  if (path === "/api/me" && method === "GET") return meGet(ctx(request, env));
  if (path === "/api/players" && method === "GET") return playersGet(ctx(request, env));
  if (path === "/api/catalog" && method === "GET") return catalogGet(ctx(request, env));
  if (path === "/api/purchase") {
    if (method === "POST") return purchasePost(ctx(request, env));
    if (method === "GET") return purchaseGet(ctx(request, env));
  }

  const profileMatch = path.match(/^\/api\/profiles\/([^/]+)$/);
  if (profileMatch) {
    const params = { nick: decodeURIComponent(profileMatch[1]) };
    if (method === "GET") return profileGet(ctx(request, env, params));
    if (method === "PATCH") return profilePatch(ctx(request, env, params));
  }

  return new Response(JSON.stringify({ ok: false, error: "Not found" }), {
    status: 404,
    headers: { "content-type": "application/json; charset=utf-8" },
  });
}

export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    if (url.pathname === "/api" || url.pathname.startsWith("/api/")) {
      return handleApi(request, env);
    }
    if (env.ASSETS) return env.ASSETS.fetch(request);
    return new Response("AquaTech worker: missing ASSETS binding", { status: 500 });
  },
};
