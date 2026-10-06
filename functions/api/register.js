import { bad, json, readJson } from "../_lib/http.js";
import {
  hashPassword,
  isUnclaimedHash,
  newSessionId,
  nickOk,
  normalizeNick,
  passwordPolicyError,
  sessionCookie,
  sessionExpiryIso,
  wantsLauncherSession,
} from "../_lib/auth.js";
import { gateRegister } from "../_lib/rate_limit.js";
import { normalizeEmail, startEmailVerification } from "../_lib/email_verify.js";

/** Отправляет код на почту сразу после регистрации. Сбой письма регистрацию не отменяет: почту можно привязать в кабинете. */
async function emailExtras(env, user, email) {
  if (!email) return {};
  const res = await startEmailVerification(env, user, email);
  return res.ok ? { email_pending: res.target } : { email_error: res.error };
}

export async function onRequestPost(context) {
  const { request, env } = context;
  if (!env.DB) return bad("База не подключена (D1)", 503);

  const body = await readJson(request);
  if (!body) return bad("Некорректный JSON");

  const nick = normalizeNick(body.nick);
  const password = String(body.password || "");
  if (!nickOk(nick)) return bad("Ник: 3–16 символов (латиница, цифры, _)");
  const policy = passwordPolicyError(password, nick);
  if (policy) return bad(policy);
  // Почта необязательна для сервера (лаунчер регистрирует без неё), но если пришла, она должна быть настоящей
  const rawEmail = String(body.email || "").trim();
  const email = rawEmail ? normalizeEmail(rawEmail) : "";
  if (rawEmail && !email) return bad("Проверь адрес почты");

  const gated = await gateRegister(env.DB, request);
  if (!gated.ok) {
    return bad(`Слишком много регистраций с этого IP. Подождите ${gated.retrySec} с.`, 429);
  }

  const existing = await env.DB.prepare(
    "SELECT id, password_hash FROM users WHERE nick = ? COLLATE NOCASE"
  )
    .bind(nick)
    .first();

  const { hash, salt } = await hashPassword(password);

  if (existing) {
    if (!isUnclaimedHash(existing.password_hash)) {
      return bad("Ник уже занят", 409);
    }
    await env.DB.prepare("UPDATE users SET password_hash = ?, password_salt = ? WHERE id = ?")
      .bind(hash, salt, existing.id)
      .run();
    const sid = newSessionId();
    await env.DB.prepare("INSERT INTO sessions (id, user_id, expires_at) VALUES (?, ?, ?)")
      .bind(sid, existing.id, sessionExpiryIso())
      .run();
    return json(
      {
        ok: true,
        claimed: true,
        user: { nick },
        ...(wantsLauncherSession(request) ? { session: sid } : {}),
        ...(await emailExtras(env, { id: existing.id, nick }, email)),
      },
      200,
      { "set-cookie": sessionCookie(sid) }
    );
  }

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
    {
      ok: true,
      user: { nick },
      ...(wantsLauncherSession(request) ? { session: sid } : {}),
      ...(await emailExtras(env, { id: userId, nick }, email)),
    },
    201,
    { "set-cookie": sessionCookie(sid) }
  );
}
