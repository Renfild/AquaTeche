import { bad, json, readJson } from "../_lib/http.js";
import {
  hashPassword,
  newSessionId,
  nickOk,
  normalizeNick,
  sessionCookie,
  sessionExpiryIso,
} from "../_lib/auth.js";

export async function onRequestPost(context) {
  const { request, env } = context;
  if (!env.DB) return bad("База не подключена (D1)", 503);

  const body = await readJson(request);
  if (!body) return bad("Некорректный JSON");

  const nick = normalizeNick(body.nick);
  const password = String(body.password || "");
  if (!nickOk(nick)) return bad("Ник: 3–16 символов (латиница, цифры, _)");
  if (password.length < 4) return bad("Пароль от 4 символов");

  const exists = await env.DB.prepare("SELECT id FROM users WHERE nick = ? COLLATE NOCASE")
    .bind(nick)
    .first();
  if (exists) return bad("Ник уже занят", 409);

  const { hash, salt } = await hashPassword(password);
  const created = await env.DB.prepare(
    "INSERT INTO users (nick, password_hash, password_salt) VALUES (?, ?, ?) RETURNING id"
  )
    .bind(nick, hash, salt)
    .first();

  const userId = created.id;
  await env.DB.prepare(
    `INSERT INTO profiles (user_id, bio, badges_json)
     VALUES (?, 'Новый игрок AquaTech.', ?)`
  )
    .bind(userId, JSON.stringify(["Новичок", "С сайта"]))
    .run();

  const sid = newSessionId();
  await env.DB.prepare("INSERT INTO sessions (id, user_id, expires_at) VALUES (?, ?, ?)")
    .bind(sid, userId, sessionExpiryIso())
    .run();

  return json(
    { ok: true, user: { nick } },
    201,
    { "set-cookie": sessionCookie(sid) }
  );
}
