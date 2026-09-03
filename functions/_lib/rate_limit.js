const LOGIN_MAX_FAILS = 8;
const LOGIN_WINDOW_MS = 15 * 60 * 1000;
const REGISTER_MAX = 6;
const REGISTER_WINDOW_MS = 60 * 60 * 1000;

function clientIp(request) {
  return (
    request.headers.get("cf-connecting-ip") ||
    request.headers.get("x-forwarded-for")?.split(",")[0]?.trim() ||
    "unknown"
  );
}

async function ensureTable(db) {
  await db
    .prepare(
      `CREATE TABLE IF NOT EXISTS auth_rate (
         k TEXT PRIMARY KEY,
         n INTEGER NOT NULL,
         window_start INTEGER NOT NULL
       )`
    )
    .run();
}

/**
 * @param {import('@cloudflare/workers-types').D1Database} db
 * @param {string} key
 * @param {number} max
 * @param {number} windowMs
 * @returns {Promise<{ ok: true } | { ok: false, retrySec: number }>}
 */
async function checkAndBump(db, key, max, windowMs) {
  await ensureTable(db);
  const now = Date.now();
  const row = await db.prepare("SELECT n, window_start FROM auth_rate WHERE k = ?").bind(key).first();
  if (!row || now - Number(row.window_start) > windowMs) {
    await db
      .prepare("INSERT OR REPLACE INTO auth_rate (k, n, window_start) VALUES (?, 1, ?)")
      .bind(key, now)
      .run();
    return { ok: true };
  }
  const n = Number(row.n) || 0;
  if (n >= max) {
    const retrySec = Math.max(1, Math.ceil((Number(row.window_start) + windowMs - now) / 1000));
    return { ok: false, retrySec };
  }
  await db.prepare("UPDATE auth_rate SET n = n + 1 WHERE k = ?").bind(key).run();
  return { ok: true };
}

export async function gateLogin(db, request, nick) {
  const ip = clientIp(request);
  return checkAndBump(db, `login:${ip}:${String(nick || "").toLowerCase()}`, LOGIN_MAX_FAILS, LOGIN_WINDOW_MS);
}

export async function clearLoginFails(db, request, nick) {
  await ensureTable(db);
  const ip = clientIp(request);
  await db
    .prepare("DELETE FROM auth_rate WHERE k = ?")
    .bind(`login:${ip}:${String(nick || "").toLowerCase()}`)
    .run();
}

export async function gateRegister(db, request) {
  const ip = clientIp(request);
  return checkAndBump(db, `reg:${ip}`, REGISTER_MAX, REGISTER_WINDOW_MS);
}

const NICK_LOOKUP_MAX = 20;
const NICK_LOOKUP_WINDOW_MS = 15 * 60 * 1000;

export async function gateNickLookup(db, request) {
  const ip = clientIp(request);
  return checkAndBump(db, `nick:${ip}`, NICK_LOOKUP_MAX, NICK_LOOKUP_WINDOW_MS);
}

const SKIN_MAX = 12;
const SKIN_WINDOW_MS = 15 * 60 * 1000;

export async function gateSkinUpload(db, request, nick) {
  const ip = clientIp(request);
  return checkAndBump(db, `skin:${ip}:${String(nick || "").toLowerCase()}`, SKIN_MAX, SKIN_WINDOW_MS);
}

const PWRESET_MAX = 3;
const PWRESET_WINDOW_MS = 15 * 60 * 1000;

/** Лимит запросов сброса пароля: на IP, без привязки к нику (анти-энумерация). */
export async function gatePasswordReset(db, request) {
  const ip = clientIp(request);
  return checkAndBump(db, `pwreset:${ip}`, PWRESET_MAX, PWRESET_WINDOW_MS);
}

const PWCODE_MAX = 5;
const PWCODE_WINDOW_MS = 15 * 60 * 1000;

/** Анти-брутфорс 6-значного кода: на IP + префикс запроса. */
export async function gateResetCode(db, request, tokenOrCode) {
  const ip = clientIp(request);
  return checkAndBump(db, `pwcode:${ip}:${String(tokenOrCode || "").slice(0, 8)}`, PWCODE_MAX, PWCODE_WINDOW_MS);
}
