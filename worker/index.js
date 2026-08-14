/**
 * AquaTech Worker: same-origin /api/* + static docs assets.
 * Reachable as https://aquateche.store (also workers.dev fallback).
 */
import { onRequestPost as registerPost } from "../functions/api/register.js";
import { onRequestPost as loginPost } from "../functions/api/login.js";
import { onRequestPost as logoutPost } from "../functions/api/logout.js";
import { onRequestGet as meGet } from "../functions/api/me.js";
import { onRequestGet as playersGet } from "../functions/api/players.js";
import { onRequestGet as catalogGet } from "../functions/api/catalog.js";
import { onRequestGet as serverStatusGet } from "../functions/api/server-status.js";
import { onRequestPost as launcherEnsureNickPost } from "../functions/api/launcher/ensure-nick.js";
import { onRequestPost as launcherVerifyTokenPost } from "../functions/api/launcher/verify-token.js";
import {
  onRequestGet as launcherSessionGet,
  onRequestPost as launcherSessionPost,
} from "../functions/api/launcher/session.js";
import {
  onRequestGet as purchaseGet,
  onRequestPost as purchasePost,
} from "../functions/api/purchase.js";
import {
  onRequestGet as profileGet,
  onRequestPatch as profilePatch,
} from "../functions/api/profiles/[nick].js";
import { onRequestGet as adminMeGet } from "../functions/api/admin/me.js";
import {
  onRequestGet as adminSettingsGet,
  onRequestPatch as adminSettingsPatch,
} from "../functions/api/admin/settings.js";
import {
  onRequestGet as adminCatalogGet,
  onRequestPost as adminCatalogPost,
} from "../functions/api/admin/catalog.js";
import { onRequestPatch as adminCatalogPatch } from "../functions/api/admin/catalog/[id].js";
import { onRequestGet as adminUsersGet } from "../functions/api/admin/users.js";
import { onRequestPatch as adminUserPatch } from "../functions/api/admin/users/[nick].js";
import {
  onRequestGet as adminNewsGet,
  onRequestPost as adminNewsPost,
} from "../functions/api/admin/news.js";
import {
  onRequestPatch as adminNewsPatch,
  onRequestDelete as adminNewsDelete,
} from "../functions/api/admin/news/[id].js";
import { onRequestGet as newsGet } from "../functions/api/news.js";
import { onRequestGet as siteGet } from "../functions/api/site.js";
import { sessionCookie } from "../functions/_lib/auth.js";

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
  if (path === "/api/server-status" && method === "GET") return serverStatusGet(ctx(request, env));
  if (path === "/api/news" && method === "GET") return newsGet(ctx(request, env));
  if (path === "/api/site" && method === "GET") return siteGet(ctx(request, env));
  // ensure-nick intentionally returns 410 (open signup footgun)
  if (path === "/api/launcher/ensure-nick" && method === "POST") return launcherEnsureNickPost(ctx(request, env));
  if (path === "/api/launcher/verify-token" && method === "POST") return launcherVerifyTokenPost(ctx(request, env));
  if (path === "/api/launcher/session") {
    if (method === "GET") return launcherSessionGet(ctx(request, env));
    if (method === "POST") return launcherSessionPost(ctx(request, env));
  }
  if (path === "/api/purchase") {
    if (method === "POST") return purchasePost(ctx(request, env));
    if (method === "GET") return purchaseGet(ctx(request, env));
  }

  if (path === "/api/admin/me" && method === "GET") return adminMeGet(ctx(request, env));
  if (path === "/api/admin/settings") {
    if (method === "GET") return adminSettingsGet(ctx(request, env));
    if (method === "PATCH") return adminSettingsPatch(ctx(request, env));
  }
  if (path === "/api/admin/catalog") {
    if (method === "GET") return adminCatalogGet(ctx(request, env));
    if (method === "POST") return adminCatalogPost(ctx(request, env));
  }
  const catalogId = path.match(/^\/api\/admin\/catalog\/(\d+)$/);
  if (catalogId && method === "PATCH") {
    return adminCatalogPatch(ctx(request, env, { id: catalogId[1] }));
  }
  if (path === "/api/admin/users" && method === "GET") return adminUsersGet(ctx(request, env));
  const adminUser = path.match(/^\/api\/admin\/users\/([^/]+)$/);
  if (adminUser && method === "PATCH") {
    return adminUserPatch(ctx(request, env, { nick: decodeURIComponent(adminUser[1]) }));
  }
  if (path === "/api/admin/news") {
    if (method === "GET") return adminNewsGet(ctx(request, env));
    if (method === "POST") return adminNewsPost(ctx(request, env));
  }
  const adminNews = path.match(/^\/api\/admin\/news\/(\d+)$/);
  if (adminNews) {
    const params = { id: adminNews[1] };
    if (method === "PATCH") return adminNewsPatch(ctx(request, env, params));
    if (method === "DELETE") return adminNewsDelete(ctx(request, env, params));
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
    if (url.pathname.startsWith("/embed/") && env.ASSETS) {
      const sid = String(url.searchParams.get("session") || "").trim();
      const response = await env.ASSETS.fetch(request);
      if (sid.length >= 8) {
        const newHeaders = new Headers(response.headers);
        newHeaders.append("set-cookie", sessionCookie(sid));
        return new Response(response.body, {
          status: response.status,
          statusText: response.statusText,
          headers: newHeaders,
        });
      }
      return response;
    }
    if (env.ASSETS) return env.ASSETS.fetch(request);
    return new Response("AquaTech worker: missing ASSETS binding", { status: 500 });
  },
};
