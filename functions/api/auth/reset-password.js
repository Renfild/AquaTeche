import { bad, json, readJson } from "../../_lib/http.js";
import { hashPassword, passwordPolicyError } from "../../_lib/auth.js";
import { gateResetCode } from "../../_lib/rate_limit.js";

export async function onRequestPost(context) {
  const { request, env } = context;
  if (!env.DB) return bad("База данных D1 не подключена", 503);

  const body = await readJson(request);
  if (!body) return bad("Некорректный JSON");

  const code = String(body.code || "").trim();
  const password = String(body.password || "");

  if (!code) {
    return bad("Не указан код подтверждения");
  }

  // Брутфорс-щит: 5 попыток на 15 минут на IP (окно кода тоже 15 минут)
  const gate = await gateResetCode(env.DB, request, code);
  if (!gate.ok) {
    return bad(`Слишком много попыток. Попробуй через ${Math.ceil(gate.retrySec / 60)} мин`, 429);
  }

  // Ищем активный запрос сброса по коду (токен остаётся на сервере)
  const row = await env.DB
    .prepare(
      `SELECT prt.token, prt.user_id, prt.code, prt.expires_at, prt.used, u.nick 
       FROM password_reset_tokens prt
       JOIN users u ON u.id = prt.user_id
       WHERE prt.code = ? AND prt.used = 0
       ORDER BY prt.rowid DESC
       LIMIT 1`
    )
    .bind(code)
    .first();

  if (!row) {
    return bad("Запрос на сброс пароля не найден или устарел", 404);
  }

  if (row.used) {
    return bad("Этот код подтверждения уже был использован", 400);
  }

  if (new Date(row.expires_at).getTime() < Date.now()) {
    return bad("Срок действия кода подтверждения истёк (15 минут)", 400);
  }

  if (String(row.code).trim() !== code) {
    return bad("Неверный код подтверждения", 403);
  }

  const policyErr = passwordPolicyError(password, row.nick);
  if (policyErr) {
    return bad(policyErr);
  }

  const { hash, salt } = await hashPassword(password);

  // Update password
  await env.DB
    .prepare("UPDATE users SET password_hash = ?, password_salt = ? WHERE id = ?")
    .bind(hash, salt, row.user_id)
    .run();

  // Mark token as used
  await env.DB
    .prepare("UPDATE password_reset_tokens SET used = 1 WHERE token = ?")
    .bind(row.token)
    .run();

  // Invalidate old sessions for security
  await env.DB
    .prepare("DELETE FROM sessions WHERE user_id = ?")
    .bind(row.user_id)
    .run();

  return json({ ok: true });
}
