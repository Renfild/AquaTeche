import { bad, json, readJson } from "../../_lib/http.js";
import { requireUser } from "../../_lib/auth.js";
import { ensureRecoverySchema } from "../../_lib/auth_recovery.js";
import { codeMail, mailConfigured, maskEmail, sendMail } from "../../_lib/mail.js";
import { gateEmailStart } from "../../_lib/rate_limit.js";

const CODE_TTL_MS = 15 * 60 * 1000;
const MAX_ATTEMPTS = 5;
const EMAIL_RE = /^[^\s@]{1,64}@[^\s@.]+(\.[^\s@.]+)+$/;

function newCode() {
  const buf = new Uint32Array(1);
  crypto.getRandomValues(buf);
  return String(100000 + (buf[0] % 900000));
}

/** GET: статус привязки почты. */
export async function onRequestGet(context) {
  const { request, env } = context;
  if (!env.DB) return bad("База не подключена", 503);
  const user = await requireUser(env.DB, request);
  if (!user) return bad("Не авторизован", 401);
  await ensureRecoverySchema(env.DB);
  const row = await env.DB.prepare("SELECT email FROM users WHERE id = ?").bind(user.id).first();
  const email = row?.email ? String(row.email) : "";
  return json({ ok: true, linked: Boolean(email), email_masked: email ? maskEmail(email) : "", configured: mailConfigured(env) });
}

/**
 * POST { action: "start", email }  : отправляет код на новую почту.
 * POST { action: "confirm", code } : проверяет код и только после этого сохраняет почту в аккаунте.
 * Почта попадает в users.email лишь подтверждённой: опечатка или чужой адрес не получат коды сброса.
 */
export async function onRequestPost(context) {
  const { request, env } = context;
  if (!env.DB) return bad("База не подключена", 503);
  const user = await requireUser(env.DB, request);
  if (!user) return bad("Не авторизован", 401);
  await ensureRecoverySchema(env.DB);
  const body = await readJson(request);
  if (!body) return bad("Некорректный JSON");

  if (body.action === "start") {
    if (!mailConfigured(env)) return bad("Отправка почты на сайте пока не включена. Привяжи Telegram или напиши в Discord", 503);
    const email = String(body.email || "").trim().toLowerCase();
    if (email.length > 120 || !EMAIL_RE.test(email)) return bad("Проверь адрес почты");
    const gate = await gateEmailStart(env.DB, user.id);
    if (!gate.ok) return bad(`Слишком много запросов кода. Попробуй через ${Math.ceil(gate.retrySec / 60)} мин`, 429);

    const code = newCode();
    await env.DB
      .prepare("INSERT OR REPLACE INTO email_verifications (user_id, email, code, expires_at, attempts) VALUES (?, ?, ?, ?, 0)")
      .bind(user.id, email, code, new Date(Date.now() + CODE_TTL_MS).toISOString())
      .run();
    const mail = codeMail({ nick: user.nick, code, purpose: "bind" });
    if (!(await sendMail(env, { to: email, ...mail }))) {
      await env.DB.prepare("DELETE FROM email_verifications WHERE user_id = ?").bind(user.id).run();
      return bad("Не удалось отправить письмо. Проверь адрес и попробуй позже", 503);
    }
    return json({ ok: true, target: maskEmail(email) });
  }

  if (body.action === "confirm") {
    const code = String(body.code || "").trim();
    if (!/^[0-9]{6}$/.test(code)) return bad("Код состоит из 6 цифр");
    const row = await env.DB.prepare("SELECT email, code, expires_at, attempts FROM email_verifications WHERE user_id = ?").bind(user.id).first();
    if (!row) return bad("Сначала запроси код", 404);
    if (new Date(row.expires_at).getTime() < Date.now()) {
      await env.DB.prepare("DELETE FROM email_verifications WHERE user_id = ?").bind(user.id).run();
      return bad("Код устарел. Запроси новый", 400);
    }
    if (Number(row.attempts) >= MAX_ATTEMPTS) {
      await env.DB.prepare("DELETE FROM email_verifications WHERE user_id = ?").bind(user.id).run();
      return bad("Слишком много неверных кодов. Запроси новый", 429);
    }
    await env.DB.prepare("UPDATE email_verifications SET attempts = attempts + 1 WHERE user_id = ?").bind(user.id).run();
    if (String(row.code) !== code) return bad("Неверный код", 403);
    await env.DB.prepare("UPDATE users SET email = ? WHERE id = ?").bind(row.email, user.id).run();
    await env.DB.prepare("DELETE FROM email_verifications WHERE user_id = ?").bind(user.id).run();
    return json({ ok: true, email_masked: maskEmail(row.email) });
  }

  return bad("Неизвестное действие");
}

/** DELETE: отвязать почту. */
export async function onRequestDelete(context) {
  const { request, env } = context;
  if (!env.DB) return bad("База не подключена", 503);
  const user = await requireUser(env.DB, request);
  if (!user) return bad("Не авторизован", 401);
  await ensureRecoverySchema(env.DB);
  await env.DB.prepare("UPDATE users SET email = NULL WHERE id = ?").bind(user.id).run();
  await env.DB.prepare("DELETE FROM email_verifications WHERE user_id = ?").bind(user.id).run();
  return json({ ok: true });
}
