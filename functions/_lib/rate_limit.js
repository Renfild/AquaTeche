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
