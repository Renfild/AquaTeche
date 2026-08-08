const COOKIE = "at_session";
const SESSION_DAYS = 30;

function b64(buf) {
  const bytes = new Uint8Array(buf);
  let s = "";
  for (let i = 0; i < bytes.length; i++) s += String.fromCharCode(bytes[i]);
  return btoa(s).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/g, "");
}

function fromB64(str) {
  const pad = str.length % 4 === 0 ? "" : "=".repeat(4 - (str.length % 4));
  const b64s = (str + pad).replace(/-/g, "+").replace(/_/g, "/");
  const bin = atob(b64s);
  const out = new Uint8Array(bin.length);
  for (let i = 0; i < bin.length; i++) out[i] = bin.charCodeAt(i);
  return out;
}

export async function hashPassword(password, saltB64) {
  const salt = saltB64 ? fromB64(saltB64) : crypto.getRandomValues(new Uint8Array(16));
  const key = await crypto.subtle.importKey(
    "raw",
    new TextEncoder().encode(password),
    "PBKDF2",
    false,
    ["deriveBits"]
  );
  const bits = await crypto.subtle.deriveBits(
    // Keep iterations modest: Workers CPU budget on free plan is tight.
    { name: "PBKDF2", hash: "SHA-256", salt, iterations: 31000 },
    key,
    256
  );
  return { hash: b64(bits), salt: b64(salt) };
}

export async function verifyPassword(password, hash, salt) {
  const again = await hashPassword(password, salt);
  return again.hash === hash;
}

export function newSessionId() {
  return b64(crypto.getRandomValues(new Uint8Array(32)));
}

export function sessionCookie(id, maxAgeSec = SESSION_DAYS * 86400) {
  const secure = "Secure; ";
  return `${COOKIE}=${id}; Path=/; HttpOnly; ${secure}SameSite=Lax; Max-Age=${maxAgeSec}`;
}

export function clearSessionCookie() {
  return `${COOKIE}=; Path=/; HttpOnly; Secure; SameSite=Lax; Max-Age=0`;
}

/** @param {Request} request */
export function getSessionId(request) {
  const raw = request.headers.get("cookie") || "";
  const m = raw.match(/(?:^|;\s*)at_session=([^;]+)/);
  return m ? decodeURIComponent(m[1]) : null;
}

/**
 * @param {import('@cloudflare/workers-types').D1Database} db
 * @param {Request} request
 */
export async function requireUser(db, request) {
  const sid = getSessionId(request);
  if (!sid) return null;
  const row = await db
    .prepare(
      `SELECT u.id, u.nick, s.expires_at
       FROM sessions s
       JOIN users u ON u.id = s.user_id
       WHERE s.id = ?`
    )
    .bind(sid)
    .first();
  if (!row) return null;
  if (new Date(row.expires_at).getTime() < Date.now()) {
    await db.prepare("DELETE FROM sessions WHERE id = ?").bind(sid).run();
    return null;
  }
  return { id: row.id, nick: row.nick, sessionId: sid };
}

export function normalizeNick(nick) {
  return String(nick || "")
    .trim()
    .replace(/\s+/g, "_")
    .slice(0, 16);
}

export function nickOk(nick) {
  return /^[A-Za-z0-9_]{3,16}$/.test(nick);
}

export function sessionExpiryIso() {
  const d = new Date(Date.now() + SESSION_DAYS * 86400 * 1000);
  return d.toISOString();
}

export { COOKIE, SESSION_DAYS };
