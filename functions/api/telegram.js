import { bad, json } from "../_lib/http.js";
import { requireUser } from "../_lib/auth.js";

/**
 * Telegram ↔ сайт привязка аккаунта.
 * 1. Игрок на сайте (Профиль → Telegram) получает одноразовый код (15 мин).
 * 2. Шлёт боту @aquatechebot команду /start КОД.
 * 3. Webhook (tg-webhook.js) связывает chat_id ↔ user_id в таблице tg_links.
 * После этого команды бота (/balance, /trends, /pass) работают по его аккаунту,
 * а при продаже лота на аукционе приходит личное уведомление.
 */

export const TG_BOT_USERNAME = "aquatechebot";
const CODE_TTL_SEC = 900;
// Без похожих символов (0/O, 1/I/L): игрок читает код с экрана и печатает в чат.
const CODE_ALPHABET = "ACDEFGHJKLMNPQRSTUVWXYZ2345679";

function ensureTables(db) {
  return db.batch([
    db.prepare(
      `CREATE TABLE IF NOT EXISTS tg_links (
        user_id INTEGER PRIMARY KEY,
        chat_id TEXT NOT NULL,
        tg_name TEXT NOT NULL DEFAULT '',
        linked_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now'))
      )`
    ),
    db.prepare(
      `CREATE TABLE IF NOT EXISTS tg_link_codes (
        code TEXT PRIMARY KEY,
        user_id INTEGER NOT NULL,
        expires_at TEXT NOT NULL
      )`
    ),
  ]);
}

/** chat_id продавца для личного уведомления, либо null. */
export async function linkedChatId(db, nick) {
  if (!db || !nick) return null;
  try {
    const row = await db
      .prepare(
        `SELECT l.chat_id FROM tg_links l
         JOIN users u ON u.id = l.user_id
         WHERE u.nick = ? COLLATE NOCASE`
      )
      .bind(String(nick))
      .first();
    return row?.chat_id || null;
  } catch {
    return null;
  }
}

function randomCode() {
  const buf = crypto.getRandomValues(new Uint8Array(8));
  let out = "";
  for (const b of buf) out += CODE_ALPHABET[b % CODE_ALPHABET.length];
  return out;
}

export async function onRequestGet(context) {
  const { request, env } = context;
  if (!env.DB) return bad("База не подключена", 503);
  const user = await requireUser(env.DB, request);
  if (!user) return bad("Не авторизован", 401);
  await ensureTables(env.DB);
  const row = await env.DB.prepare(
    "SELECT tg_name, linked_at FROM tg_links WHERE user_id = ?"
  )
    .bind(user.id)
    .first();
  return json({
    ok: true,
    linked: Boolean(row),
    tg_name: row?.tg_name || "",
    linked_at: row?.linked_at || "",
    bot: TG_BOT_USERNAME,
  });
}

export async function onRequestPost(context) {
  const { request, env } = context;
  if (!env.DB) return bad("База не подключена", 503);
  const user = await requireUser(env.DB, request);
  if (!user) return bad("Не авторизован", 401);
  await ensureTables(env.DB);
  // Один активный код на аккаунт: старые сгорают при выпуске нового.
  await env.DB.prepare("DELETE FROM tg_link_codes WHERE user_id = ?").bind(user.id).run();
  const code = randomCode();
  await env.DB.prepare(
    `INSERT INTO tg_link_codes (code, user_id, expires_at)
     VALUES (?, ?, datetime('now', '+${CODE_TTL_SEC} seconds'))`
  )
    .bind(code, user.id)
    .run();
  return json({ ok: true, code, bot: TG_BOT_USERNAME, expires_sec: CODE_TTL_SEC });
}

export async function onRequestDelete(context) {
  const { request, env } = context;
  if (!env.DB) return bad("База не подключена", 503);
  const user = await requireUser(env.DB, request);
  if (!user) return bad("Не авторизован", 401);
  await ensureTables(env.DB);
  await env.DB.prepare("DELETE FROM tg_links WHERE user_id = ?").bind(user.id).run();
  await env.DB.prepare("DELETE FROM tg_link_codes WHERE user_id = ?").bind(user.id).run();
  return json({ ok: true });
}
