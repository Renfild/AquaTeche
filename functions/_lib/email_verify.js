import { ensureRecoverySchema } from "./auth_recovery.js";
import { codeMail, mailConfigured, maskEmail, sendMail } from "./mail.js";
import { gateEmailStart } from "./rate_limit.js";

const CODE_TTL_MS = 15 * 60 * 1000;
const MAX_ATTEMPTS = 5;
const EMAIL_RE = /^[^\s@]{1,64}@[^\s@.]+(\.[^\s@.]+)+$/;

function newCode() {
  const buf = new Uint32Array(1);
  crypto.getRandomValues(buf);
  return String(100000 + (buf[0] % 900000));
}

/** Приводит адрес к нижнему регистру и проверяет вид. Пустая строка, если адрес не годится. */
export function normalizeEmail(raw) {
  const email = String(raw || "").trim().toLowerCase();
  return email.length <= 120 && EMAIL_RE.test(email) ? email : "";
}

/**
 * Шлёт код на почту, пока не подтверждённую. Почта попадёт в users.email только после confirmEmailVerification.
 * @returns {Promise<{ok: true, target: string} | {ok: false, status: number, error: string}>}
 */
export async function startEmailVerification(env, user, email) {
  await ensureRecoverySchema(env.DB);
  if (!mailConfigured(env)) {
    return { ok: false, status: 503, error: "Отправка почты на сайте пока не включена. Привяжи Telegram или напиши в Discord" };
  }
  const gate = await gateEmailStart(env.DB, user.id);
  if (!gate.ok) {
    return { ok: false, status: 429, error: `Слишком много запросов кода. Попробуй через ${Math.ceil(gate.retrySec / 60)} мин` };
  }
  const code = newCode();
  await env.DB
    .prepare("INSERT OR REPLACE INTO email_verifications (user_id, email, code, expires_at, attempts) VALUES (?, ?, ?, ?, 0)")
    .bind(user.id, email, code, new Date(Date.now() + CODE_TTL_MS).toISOString())
    .run();
  if (!(await sendMail(env, { to: email, ...codeMail({ nick: user.nick, code, purpose: "bind" }) }))) {
    await env.DB.prepare("DELETE FROM email_verifications WHERE user_id = ?").bind(user.id).run();
    return { ok: false, status: 503, error: "Не удалось отправить письмо. Проверь адрес и попробуй позже" };
  }
  return { ok: true, target: maskEmail(email) };
}

/** @returns {Promise<{ok: true, email: string} | {ok: false, status: number, error: string}>} */
export async function confirmEmailVerification(env, userId, rawCode) {
  await ensureRecoverySchema(env.DB);
  const code = String(rawCode || "").trim();
  if (!/^[0-9]{6}$/.test(code)) return { ok: false, status: 400, error: "Код состоит из 6 цифр" };
  const row = await env.DB
    .prepare("SELECT email, code, expires_at, attempts FROM email_verifications WHERE user_id = ?")
    .bind(userId)
    .first();
  if (!row) return { ok: false, status: 404, error: "Сначала запроси код" };
  if (new Date(row.expires_at).getTime() < Date.now()) {
    await env.DB.prepare("DELETE FROM email_verifications WHERE user_id = ?").bind(userId).run();
    return { ok: false, status: 400, error: "Код устарел. Запроси новый" };
  }
  if (Number(row.attempts) >= MAX_ATTEMPTS) {
    await env.DB.prepare("DELETE FROM email_verifications WHERE user_id = ?").bind(userId).run();
    return { ok: false, status: 429, error: "Слишком много неверных кодов. Запроси новый" };
  }
  await env.DB.prepare("UPDATE email_verifications SET attempts = attempts + 1 WHERE user_id = ?").bind(userId).run();
  if (String(row.code) !== code) return { ok: false, status: 403, error: "Неверный код" };
  await env.DB.prepare("UPDATE users SET email = ? WHERE id = ?").bind(row.email, userId).run();
  await env.DB.prepare("DELETE FROM email_verifications WHERE user_id = ?").bind(userId).run();
  return { ok: true, email: row.email };
}
