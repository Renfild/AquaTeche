import { bad, json, readJson } from "../_lib/http.js";
import { hashPassword, passwordPolicyError, requireUser, verifyPassword } from "../_lib/auth.js";
import { gateLogin } from "../_lib/rate_limit.js";

/** Change password for the logged-in user (old password required). */
export async function onRequestPost(context) {
  const { request, env } = context;
  if (!env.DB) return bad("База не подключена (D1)", 503);
  const user = await requireUser(env.DB, request);
  if (!user) return bad("Не авторизован", 401);

  const body = await readJson(request);
  if (!body) return bad("Некорректный JSON");

  const next = String(body.next || "");
  const policy = passwordPolicyError(next, user.nick);
  if (policy) return bad(policy);

  const gated = await gateLogin(env.DB, request, user.nick);
  if (!gated.ok) return bad(`Слишком много попыток. Подожди ${gated.retrySec} с`, 429);

  const row = await env.DB
    .prepare("SELECT password_hash, password_salt FROM users WHERE id = ?")
    .bind(user.id)
    .first();
  if (!row) return bad("Пользователь не найден", 404);
  const oldOk = await verifyPassword(String(body.old || ""), row.password_hash, row.password_salt);
  if (!oldOk) return bad("Текущий пароль неверный", 403);

  const { hash, salt } = await hashPassword(next);
  await env.DB.prepare("UPDATE users SET password_hash = ?, password_salt = ? WHERE id = ?")
    .bind(hash, salt, user.id)
    .run();
  return json({ ok: true });
}
