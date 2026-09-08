import { bad, json } from "../_lib/http.js";
import { requireUser } from "../_lib/auth.js";
import { enqueueCommand } from "./internal/pending-commands.js";
import { gateSkinUpload } from "../_lib/rate_limit.js";

const KINDS = new Set(["skin", "cape", "avatar"]);
const MAX = { skin: 131072, cape: 65536, avatar: 262144 };

async function ensureTable(db) {
  await db
    .prepare(
      `CREATE TABLE IF NOT EXISTS player_look (
        user_id INTEGER NOT NULL,
        kind TEXT NOT NULL,
        bytes BLOB NOT NULL,
        mime TEXT NOT NULL,
        width INTEGER NOT NULL,
        height INTEGER NOT NULL,
        updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')),
        PRIMARY KEY (user_id, kind)
      )`
    )
    .run();
}

function pngSize(bytes) {
  if (bytes.length < 24) return null;
  if (bytes[0] !== 0x89 || bytes[1] !== 0x50 || bytes[2] !== 0x4e || bytes[3] !== 0x47) return null;
  const w = (bytes[16] << 24) | (bytes[17] << 16) | (bytes[18] << 8) | bytes[19];
  const h = (bytes[20] << 24) | (bytes[21] << 16) | (bytes[22] << 8) | bytes[23];
  if (w < 1 || h < 1 || w > 1024 || h > 1024) return null;
  return { w, h, mime: "image/png" };
}

function jpegOk(bytes) {
  return bytes.length > 24 && bytes[0] === 0xff && bytes[1] === 0xd8;
}

function validSkinSize(w, h) {
  if (w % 32 || h % 32) return false;
  if (w < 64 || w > 128 || h < 32 || h > 128) return false;
  return h === w || h === w / 2;
}

function validCapeSize(w, h) {
  return (w === 64 && h === 32) || (w === 22 && h === 17) || (w === 128 && h === 64);
}

function publicOrigin(request) {
  const url = new URL(request.url);
  return `${url.protocol}//${url.host}`;
}

function toBytes(value) {
  if (!value) return null;
  if (value instanceof Uint8Array) return value.byteLength ? value : null;
  if (value instanceof ArrayBuffer) return value.byteLength ? new Uint8Array(value) : null;
  if (ArrayBuffer.isView(value)) {
    const view = new Uint8Array(value.buffer, value.byteOffset, value.byteLength);
    return view.byteLength ? view : null;
  }
  if (Array.isArray(value)) return value.length ? new Uint8Array(value) : null;
  if (typeof value === "object" && Array.isArray(value.data)) {
    return value.data.length ? new Uint8Array(value.data) : null;
  }
  return null;
}

function imageHeaders(mime, extra = {}) {
  return {
    "content-type": mime || "image/png",
    "cache-control": "private, no-store, no-cache, must-revalidate",
    "cdn-cache-control": "no-store",
    "x-content-type-options": "nosniff",
    "access-control-allow-origin": "*",
    ...extra,
  };
}

function lookUrl(origin, nick, kind, updated) {
  const q = updated ? `?v=${encodeURIComponent(updated)}` : "";
  return `${origin}/api/skins/${encodeURIComponent(nick)}/${kind}.png${q}`;
}

function safeMcNick(nick) {
  const n = String(nick || "").trim();
  if (!/^[A-Za-z0-9_]{1,16}$/.test(n)) return null;
  return n;
}

async function fetchRemotePng(urls) {
  for (const url of urls) {
    try {
      const res = await fetch(url, {
        headers: { "user-agent": "AquaTechPortal/1.0", accept: "image/png" },
        cf: { cacheTtl: 0, cacheEverything: false },
      });
      if (!res.ok) continue;
      const ctype = (res.headers.get("content-type") || "").toLowerCase();
      if (!ctype.includes("image")) continue;
      const buf = await res.arrayBuffer();
      if (buf.byteLength < 80 || buf.byteLength > 262144) continue;
      const bytes = new Uint8Array(buf);
      if (bytes[0] !== 0x89 || bytes[1] !== 0x50 || bytes[2] !== 0x4e || bytes[3] !== 0x47) continue;
      return new Response(buf, { headers: imageHeaders("image/png") });
    } catch {
      /* next mirror */
    }
  }
  return null;
}

async function remoteLook(nick, kind) {
  const n = safeMcNick(nick);
  if (!n) return null;
  const enc = encodeURIComponent(n);
  if (kind === "avatar") {
    return fetchRemotePng([
      `https://mc-heads.net/avatar/${enc}/64`,
      `https://minotar.net/helm/${enc}/64`,
    ]);
  }
  if (kind === "skin") {
    return fetchRemotePng([
      `https://mc-heads.net/skin/${enc}`,
      `https://minotar.net/skin/${enc}`,
    ]);
  }
  return null;
}

export async function onRequestGet(context) {
  const { request, env, params } = context;
  if (!env.DB) return bad("База не подключена (D1)", 503);
  await ensureTable(env.DB);
  const nick = String(params.nick || "").trim();
  let kind = String(params.kind || "").trim();
  if (kind.endsWith(".png")) kind = kind.replace(/\.png$/, "");
  if (!nick) return bad("Ник не указан");

  if (!kind) {
    const rows = await env.DB.prepare(
      `SELECT l.kind, l.updated_at
       FROM player_look l
       JOIN users u ON u.id = l.user_id
       WHERE u.nick = ? COLLATE NOCASE`
    )
      .bind(nick)
      .all();
    const origin = publicOrigin(request);
    const out = { ok: true, nick, skin: false, cape: false, avatar: false, urls: {} };
    for (const row of rows.results || []) {
      out[row.kind] = true;
      out.urls[row.kind] = lookUrl(origin, nick, row.kind, row.updated_at);
    }
    return json(out, 200, { "access-control-allow-origin": "*" });
  }

  if (!KINDS.has(kind)) return bad("Неизвестный тип", 404);
  const row = await env.DB.prepare(
    `SELECT l.bytes, l.mime, l.updated_at
     FROM player_look l
     JOIN users u ON u.id = l.user_id
     WHERE u.nick = ? COLLATE NOCASE AND l.kind = ?`
  )
    .bind(nick, kind)
    .first();
  if (!row) {
    const remote = await remoteLook(nick, kind);
    if (remote) return remote;
    if ((kind === "avatar" || kind === "skin") && env.ASSETS) {
      const fallback = kind === "avatar" ? "/assets/images/avatar_default.png" : "/assets/images/steve.png";
      const res = await env.ASSETS.fetch(new Request(new URL(fallback, request.url)));
      const headers = new Headers(res.headers);
      headers.set("access-control-allow-origin", "*");
      headers.set("cache-control", "private, no-store");
      headers.set("cdn-cache-control", "no-store");
      return new Response(res.body, { status: res.status, headers });
    }
    return new Response("Not found", { status: 404, headers: { "access-control-allow-origin": "*" } });
  }
  const body = toBytes(row.bytes);
  if (!body) return new Response("Not found", { status: 404, headers: { "access-control-allow-origin": "*" } });
  return new Response(body, {
    headers: imageHeaders(row.mime, { etag: `"${row.updated_at || "look"}"` }),
  });
}

export async function onRequestPost(context) {
  const { request, env } = context;
  if (!env.DB) return bad("База не подключена (D1)", 503);
  const user = await requireUser(env.DB, request);
  if (!user) return bad("Не авторизован", 401);
  const gated = await gateSkinUpload(env.DB, request, user.nick);
  if (!gated.ok) return bad(`Слишком часто. Подожди ${gated.retrySec} с`, 429);

  let form;
  try {
    form = await request.formData();
  } catch {
    return bad("Ожидался файл");
  }
  const kind = String(form.get("kind") || "skin").trim();
  const file = form.get("file");
  if (!KINDS.has(kind)) return bad("Тип: skin, cape или avatar");
  if (!file || typeof file.arrayBuffer !== "function") return bad("Выбери PNG");

  const buf = new Uint8Array(await file.arrayBuffer());
  if (buf.length > MAX[kind]) return bad("Файл слишком большой");

  let meta;
  if (kind === "avatar" && jpegOk(buf)) {
    meta = { w: 0, h: 0, mime: "image/jpeg" };
  } else {
    meta = pngSize(buf);
  }
  if (!meta) return bad("Нужен PNG (аватар ещё JPEG)");
  if (kind === "skin" && !validSkinSize(meta.w, meta.h)) {
    return bad("Скин: PNG 64×64, 64×32 или 128×128");
  }
  if (kind === "cape" && !validCapeSize(meta.w, meta.h)) {
    return bad("Плащ: PNG 64×32 или 128×64");
  }

  await ensureTable(env.DB);
  await env.DB.prepare(
    `INSERT INTO player_look (user_id, kind, bytes, mime, width, height, updated_at)
     VALUES (?, ?, ?, ?, ?, ?, strftime('%Y-%m-%dT%H:%M:%fZ','now'))
     ON CONFLICT(user_id, kind) DO UPDATE SET
       bytes = excluded.bytes,
       mime = excluded.mime,
       width = excluded.width,
       height = excluded.height,
       updated_at = excluded.updated_at`
  )
    .bind(user.id, kind, buf, meta.mime, meta.w, meta.h)
    .run();

  const saved = await env.DB.prepare(
    "SELECT updated_at FROM player_look WHERE user_id = ? AND kind = ?"
  )
    .bind(user.id, kind)
    .first();
  const origin = publicOrigin(request);
  const url = lookUrl(origin, user.nick, kind, saved?.updated_at || String(Date.now()));
  if (kind === "skin") {
    await enqueueCommand(env.DB, {
      nick: user.nick,
      kind: "skin",
      payload: url,
      provider: "site-skin",
      providerPaymentId: `skin:${user.id}:${crypto.randomUUID()}`,
    });
  }

  return json({ ok: true, kind, url, applied: kind === "skin" });
}

export async function onRequestDelete(context) {
  const { request, env } = context;
  if (!env.DB) return bad("База не подключена (D1)", 503);
  const user = await requireUser(env.DB, request);
  if (!user) return bad("Не авторизован", 401);
  let body = {};
  try {
    body = await request.json();
  } catch {
    body = {};
  }
  const kind = String(body.kind || "skin").trim();
  if (!KINDS.has(kind)) return bad("Тип: skin, cape или avatar");
  await ensureTable(env.DB);
  await env.DB.prepare("DELETE FROM player_look WHERE user_id = ? AND kind = ?")
    .bind(user.id, kind)
    .run();
  if (kind === "skin") {
    await enqueueCommand(env.DB, {
      nick: user.nick,
      kind: "skin_clear",
      payload: "",
      provider: "site-skin",
      providerPaymentId: `skin-clear:${user.id}:${crypto.randomUUID()}`,
    });
  }
  return json({ ok: true, kind });
}
