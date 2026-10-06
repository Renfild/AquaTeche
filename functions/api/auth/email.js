import { bad, json, readJson } from "../../_lib/http.js";
import { requireUser } from "../../_lib/auth.js";
import { ensureRecoverySchema } from "../../_lib/auth_recovery.js";
import { confirmEmailVerification, normalizeEmail, startEmailVerification } from "../../_lib/email_verify.js";
import { mailConfigured, maskEmail } from "../../_lib/mail.js";

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
  const body = await readJson(request);
  if (!body) return bad("Некорректный JSON");

  if (body.action === "start") {
    const email = normalizeEmail(body.email);
    if (!email) return bad("Проверь адрес почты");
    const res = await startEmailVerification(env, user, email);
    return res.ok ? json({ ok: true, target: res.target }) : bad(res.error, res.status);
  }

  if (body.action === "confirm") {
    const res = await confirmEmailVerification(env, user.id, body.code);
    return res.ok ? json({ ok: true, email_masked: maskEmail(res.email) }) : bad(res.error, res.status);
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
