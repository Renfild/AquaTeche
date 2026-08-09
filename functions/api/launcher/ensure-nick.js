import { bad, json, readJson } from "../_lib/http.js";
import { hashPassword, nickOk, normalizeNick } from "../_lib/auth.js";

/**
 * Launcher calls this on Play: create portal profile for the nick if missing,
 * so tops/search stay in sync with in-game names (no password UI needed).
 */
export async function onRequestPost(context) {
  const { request, env } = context;
  if (!env.DB) return bad("База не подключена (D1)", 503);

  const body = await readJson(request);
  if (!body) return bad("Некорректный JSON");

  const nick = normalizeNick(body.nick);
  if (!nickOk(nick)) return bad("Ник: 3–16 символов (латиница, цифры, _)");

  const existing = await env.DB.prepare("SELECT id FROM users WHERE nick = ? COLLATE NOCASE")
    .bind(nick)
    .first();
  if (existing) {
    return json({ ok: true, nick, created: false });
  }

  // Guest password is random; player can set a real one later via site register/claim.
  const guestPass = crypto.randomUUID().replace(/-/g, "") + "Aa1!";
  const { hash, salt } = await hashPassword(guestPass);
  const created = await env.DB.prepare(
    "INSERT INTO users (nick, password_hash, password_salt) VALUES (?, ?, ?) RETURNING id"
  )
    .bind(nick, hash, salt)
    .first();

  await env.DB.prepare(
    `INSERT INTO profiles (user_id, bio, badges_json)
     VALUES (?, 'Игрок с лаунчера AquaTech.', ?)`
  )
    .bind(created.id, JSON.stringify(["С лаунчера", "Новичок"]))
    .run();

  return json({ ok: true, nick, created: true }, 201);
}
