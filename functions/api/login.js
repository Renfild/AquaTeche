import { bad, json, readJson } from "../_lib/http.js";
import {
  newSessionId,
  nickOk,
  normalizeNick,
  sessionCookie,
  sessionExpiryIso,
  verifyPassword,
} from "../_lib/auth.js";

export async function onRequestPost(context) {
  const { request, env } = context;
  if (!env.DB) return bad("База не подключена (D1)", 503);

  const body = await readJson(request);
  if (!body) return bad("Некорректный JSON");

  const nick = normalizeNick(body.nick);
  const password = String(body.password || "");
  if (!nickOk(nick) || password.length < 1) return bad("Неверный логин или пароль", 401);

  const user = await env.DB.prepare(
    "SELECT id, nick, password_hash, password_salt FROM users WHERE nick = ? COLLATE NOCASE"
  )
    .bind(nick)
    .first();
  if (!user) return bad("Неверный логин или пароль", 401);

  const ok = await verifyPassword(password, user.password_hash, user.password_salt);
  if (!ok) return bad("Неверный логин или пароль", 401);

  const sid = newSessionId();
  await env.DB.prepare("INSERT INTO sessions (id, user_id, expires_at) VALUES (?, ?, ?)")
    .bind(sid, user.id, sessionExpiryIso())
    .run();

  return json(
    { ok: true, user: { nick: user.nick } },
    200,
    { "set-cookie": sessionCookie(sid) }
  );
}
