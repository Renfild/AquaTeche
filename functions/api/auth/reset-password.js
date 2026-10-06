import { bad, json, readJson } from "../../_lib/http.js";
import { hashPassword, nickOk, normalizeNick, passwordPolicyError } from "../../_lib/auth.js";
import { gateResetCode, gateResetToken } from "../../_lib/rate_limit.js";

/** Сравнение без раннего выхода: время ответа не выдаёт, сколько цифр угадано. */
function sameCode(a, b) {
  const x = String(a);
  const y = String(b);
  let diff = x.length ^ y.length;
  const len = Math.max(x.length, y.length);
  for (let i = 0; i < len; i++) diff |= (x.charCodeAt(i) || 0) ^ (y.charCodeAt(i) || 0);
  return diff === 0;
}

/**
 * POST /api/auth/reset-password { nick, code, password }
 * Код привязан к нику и запросу: пять попыток на запрос, потом он сгорает и нужен новый.
 */
export async function onRequestPost(context) {
  const { request, env } = context;
  if (!env.DB) return bad("База данных D1 не подключена", 503);

  const body = await readJson(request);
  if (!body) return bad("Некорректный JSON");

  const nick = normalizeNick(body.nick);
  const code = String(body.code || "").trim();
  const password = String(body.password || "");
  if (!nickOk(nick)) return bad("Укажи ник");
  if (!/^[0-9]{6}$/.test(code)) return bad("Код состоит из 6 цифр");

  const ipGate = await gateResetCode(env.DB, request);
  if (!ipGate.ok) {
    return bad(`Слишком много попыток. Попробуй через ${Math.ceil(ipGate.retrySec / 60)} мин`, 429);
  }

  const row = await env.DB
    .prepare(
      `SELECT prt.token, prt.user_id, prt.code, prt.expires_at, u.nick
       FROM password_reset_tokens prt
       JOIN users u ON u.id = prt.user_id
       WHERE u.nick = ? COLLATE NOCASE AND prt.used = 0
       ORDER BY prt.rowid DESC
       LIMIT 1`
    )
    .bind(nick)
    .first();
  if (!row) return bad("Запрос на сброс пароля не найден или устарел. Запроси код заново", 404);

  if (new Date(row.expires_at).getTime() < Date.now()) {
    await env.DB.prepare("UPDATE password_reset_tokens SET used = 1 WHERE token = ?").bind(row.token).run();
    return bad("Срок действия кода истёк (15 минут). Запроси новый", 400);
  }

  const tokenGate = await gateResetToken(env.DB, row.token);
  if (!tokenGate.ok) {
    await env.DB.prepare("UPDATE password_reset_tokens SET used = 1 WHERE token = ?").bind(row.token).run();
    return bad("Слишком много неверных кодов. Запроси новый код", 429);
  }

  if (!sameCode(row.code, code)) return bad("Неверный код подтверждения", 403);

  const policyErr = passwordPolicyError(password, row.nick);
  if (policyErr) return bad(policyErr);

  const { hash, salt } = await hashPassword(password);
  await env.DB
    .prepare("UPDATE users SET password_hash = ?, password_salt = ? WHERE id = ?")
    .bind(hash, salt, row.user_id)
    .run();
  await env.DB.prepare("UPDATE password_reset_tokens SET used = 1 WHERE token = ?").bind(row.token).run();
  await env.DB.prepare("DELETE FROM sessions WHERE user_id = ?").bind(row.user_id).run();

  return json({ ok: true });
}
