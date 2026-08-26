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
  return timingSafeEqualStr(again.hash, hash);
}

function timingSafeEqualStr(a, b) {
  const left = String(a || "");
  const right = String(b || "");
  const len = Math.max(left.length, right.length);
  let diff = left.length ^ right.length;
  for (let i = 0; i < len; i++) {
    diff |= (left.charCodeAt(i) || 0) ^ (right.charCodeAt(i) || 0);
  }
  return diff === 0;
}

export function isUnclaimedHash(hash) {
  const h = String(hash || "");
  return h === "" || h === "IN_GAME_UNREGISTERED";
}

const MIN_PASSWORD = 8;
const MAX_PASSWORD = 128;

/** @returns {string | null} error message */
export function passwordPolicyError(password, nick) {
  const p = String(password || "");
  if (p.length < MIN_PASSWORD) return "Пароль от 8 символов";
  if (p.length > MAX_PASSWORD) return "Пароль слишком длинный";
  const n = String(nick || "").trim().toLowerCase();
  if (n && p.toLowerCase() === n) return "Пароль не должен совпадать с ником";
  return null;
}

/** Launcher HttpClient has no Origin; browsers always send one on POST. */
export function wantsLauncherSession(request) {
  if (request.headers.get("x-aquatech-launcher") !== "1") return false;
  return !request.headers.get("origin");
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
export function adminNickSet(env) {
  return new Set(
    String(env?.ADMIN_NICKS || "")
      .split(",")
      .map((s) => s.trim().toLowerCase())
      .filter(Boolean)
  );
}

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

/** Cookie session + ADMIN_NICKS env and/or users.is_admin. */
export async function requireAdmin(db, request, env) {
  const user = await requireUser(db, request);
  if (!user) return null;
  if (adminNickSet(env).has(String(user.nick).toLowerCase())) {
    return { ...user, is_admin: true };
  }
  try {
    const row = await db
      .prepare("SELECT is_admin FROM users WHERE id = ?")
      .bind(user.id)
      .first();
    if (Number(row?.is_admin) === 1) return { ...user, is_admin: true };
  } catch {
    /* is_admin column missing until migration */
  }
  return null;
}

export async function userIsAdmin(db, nick, env) {
  if (adminNickSet(env).has(String(nick || "").toLowerCase())) return true;
  try {
    const row = await db
      .prepare("SELECT is_admin FROM users WHERE nick = ? COLLATE NOCASE")
      .bind(nick)
      .first();
    return Number(row?.is_admin) === 1;
  } catch {
    return false;
  }
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
