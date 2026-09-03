import { bad, json, readJson } from "../../_lib/http.js";
import { newSessionId } from "../../_lib/auth.js";
import { gatePasswordReset } from "../../_lib/rate_limit.js";

function maskEmail(email) {
  if (!email || !email.includes("@")) return "***";
  const [user, domain] = email.split("@");
  const visible = user.length > 2 ? user.slice(0, 2) + "***" : "***";
  return `${visible}@${domain}`;
}

export async function onRequestPost(context) {
  const { request, env } = context;
  if (!env.DB) return bad("База данных D1 не подключена", 503);

  // Без настроенного почтового сервиса сброс невозможен — fail closed,
  // иначе код попадёт в HTTP-ответ и любой сбросит чужой пароль.
  if (!env.RESEND_API_KEY) {
    return bad("Сброс пароля по почте сейчас недоступен — напиши в Discord, админ поможет", 503);
  }

  const gate = await gatePasswordReset(env.DB, request);
  if (!gate.ok) {
    return bad(`Слишком много запросов на сброс. Попробуй через ${Math.ceil(gate.retrySec / 60)} мин`, 429);
  }

  const body = await readJson(request);
  if (!body) return bad("Некорректный JSON");

  const nick = String(body.nick || "").trim();
  if (!nick || nick.length < 3) {
    return bad("Укажи корректный никнейм");
  }

  const user = await env.DB
    .prepare("SELECT id, nick, email FROM users WHERE nick = ? COLLATE NOCASE")
    .bind(nick)
    .first();

  if (!user) {
    return bad("Игрок с таким ником не найден", 404);
  }

  const targetEmail = user.email ? String(user.email).trim().toLowerCase() : "";

  // Почта может быть привязана только из кабинета (авторизованно):
  // привязка по нику из анонимного эндпоинта = угон аккаунта.
  if (!targetEmail) {
    return bad("К аккаунту не привязана почта — напиши в Discord, привяжем вручную", 403);
  }

  // Генерируем 6-значный код; сам токен сброса никогда не покидает сервер —
  // клиент подтверждает сброс кодом из письма.
  const code = String(Math.floor(100000 + Math.random() * 900000));
  const token = newSessionId();
  const expiresAt = new Date(Date.now() + 15 * 60 * 1000).toISOString();

  // Invalidate previous unused tokens for this user
  await env.DB
    .prepare("UPDATE password_reset_tokens SET used = 1 WHERE user_id = ? AND used = 0")
    .bind(user.id)
    .run();

  // Insert new token
  await env.DB
    .prepare(
      "INSERT INTO password_reset_tokens (token, user_id, code, expires_at, used) VALUES (?, ?, ?, ?, 0)"
    )
    .bind(token, user.id, code, expiresAt)
    .run();

  try {
    const mailRes = await fetch("https://api.resend.com/emails", {
      method: "POST",
      headers: {
        Authorization: `Bearer ${env.RESEND_API_KEY}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        from: "AquaTech <auth@aquateche.store>",
        to: [targetEmail],
        subject: "Сброс пароля на AquaTech",
        html: `<p>Привет, <b>${user.nick}</b>!</p><p>Код для подтверждения сброса пароля: <b style="font-size:18px;letter-spacing:2px;">${code}</b></p><p>Код действует 15 минут. Если ты не запрашивал сброс, просто проигнорируй это письмо.</p>`,
      }),
    });
    if (!mailRes.ok) {
      return bad("Не удалось отправить письмо — попробуй позже", 503);
    }
  } catch (e) {
    return bad("Не удалось отправить письмо — попробуй позже", 503);
  }

  // Токен намеренно не возвращаем: подтверждение сброса идёт по коду из письма
  return json({
    ok: true,
    emailMasked: maskEmail(targetEmail),
  });
}
