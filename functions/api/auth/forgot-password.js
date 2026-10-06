import { bad, json, readJson } from "../../_lib/http.js";
import { newSessionId, nickOk, normalizeNick } from "../../_lib/auth.js";
import { ensureRecoverySchema } from "../../_lib/auth_recovery.js";
import { codeMail, maskEmail, sendMail } from "../../_lib/mail.js";
import { gatePasswordReset, gateResetNick } from "../../_lib/rate_limit.js";
import { sendTelegramMessage, telegramLinkOf } from "../telegram.js";

const CODE_TTL_MS = 15 * 60 * 1000;

function maskTelegram(name) {
  const clean = String(name || "").replace(/^@/, "");
  if (!clean) return "привязанный Telegram";
  return "@" + (clean.length > 2 ? clean.slice(0, 2) + "***" : "***");
}

/** Шесть цифр из криптостойкого генератора: код защищает аккаунт, Math.random тут не годится. */
function newCode() {
  const buf = new Uint32Array(1);
  crypto.getRandomValues(buf);
  return String(100000 + (buf[0] % 900000));
}

/**
 * POST /api/auth/forgot-password { nick }
 * Код уходит во все каналы, которые владелец аккаунта привязал заранее: Telegram (бот @aquatechebot) и подтверждённая
 * почта. Хватает одного доставленного. На сервере вход требует сессию сайта, так что присылать код в игру нельзя: забывший пароль
 * в игру не попадёт. Сам код в ответ никогда не попадает.
 */
export async function onRequestPost(context) {
  const { request, env } = context;
  if (!env.DB) return bad("База данных D1 не подключена", 503);
  await ensureRecoverySchema(env.DB);

  const gate = await gatePasswordReset(env.DB, request);
  if (!gate.ok) {
    return bad(`Слишком много запросов на сброс. Попробуй через ${Math.ceil(gate.retrySec / 60)} мин`, 429);
  }

  const body = await readJson(request);
  if (!body) return bad("Некорректный JSON");

  const nick = normalizeNick(body.nick);
  if (!nickOk(nick)) return bad("Укажи корректный никнейм");

  const user = await env.DB
    .prepare("SELECT id, nick, email FROM users WHERE nick = ? COLLATE NOCASE")
    .bind(nick)
    .first();
  if (!user) return bad("Игрок с таким ником не найден", 404);

  const link = await telegramLinkOf(env.DB, user.id);
  const email = user.email ? String(user.email).trim().toLowerCase() : "";
  if (!link && !email) {
    return bad("К аккаунту не привязан Telegram или почта. Напиши администратору в Discord: он проверит ник и поможет", 403);
  }

  const nickGate = await gateResetNick(env.DB, user.nick);
  if (!nickGate.ok) {
    return bad(`Код для этого ника уже запрашивали несколько раз. Попробуй через ${Math.ceil(nickGate.retrySec / 60)} мин`, 429);
  }

  const code = newCode();
  const token = newSessionId();
  const expiresAt = new Date(Date.now() + CODE_TTL_MS).toISOString();

  await env.DB.prepare("UPDATE password_reset_tokens SET used = 1 WHERE user_id = ? AND used = 0").bind(user.id).run();
  await env.DB
    .prepare("INSERT INTO password_reset_tokens (token, user_id, code, expires_at, used) VALUES (?, ?, ?, ?, 0)")
    .bind(token, user.id, code, expiresAt)
    .run();

  const channels = [];
  if (link) {
    const text =
      `Сброс пароля AquaTech для ника ${user.nick}.\n` +
      `Код: ${code}\n` +
      `Он действует 15 минут. Если ты ничего не запрашивал, просто проигнорируй это сообщение: пароль не изменится.`;
    if (await sendTelegramMessage(env, link.chat_id, text)) channels.push({ type: "telegram", target: maskTelegram(link.tg_name) });
  }
  if (email && (await sendMail(env, { to: email, ...codeMail({ nick: user.nick, code, purpose: "reset" }) }))) {
    channels.push({ type: "email", target: maskEmail(email) });
  }

  if (!channels.length) {
    await env.DB.prepare("UPDATE password_reset_tokens SET used = 1 WHERE token = ?").bind(token).run();
    return bad("Не удалось отправить код. Для Telegram открой чат с @aquatechebot и напиши ему /start, для почты проверь папку «Спам» и повтори. Не выходит, пиши в Discord", 503);
  }

  return json({ ok: true, channels, channel: channels[0].type, target: channels[0].target });
}
