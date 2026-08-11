import { bad, json, readJson } from "../../_lib/http.js";
import { getSessionId, requireUser } from "../../_lib/auth.js";

function launcherOnly(request) {
  return request.headers.get("x-aquatech-launcher") === "1";
}

/** Cookie session → launcher config session id (same DB row as site cookie). */
export async function onRequestGet(context) {
  const { request, env } = context;
  if (!launcherOnly(request)) return bad("Forbidden", 403);
  if (!env.DB) return bad("База не подключена", 503);

  const user = await requireUser(env.DB, request);
  if (!user) return bad("Не авторизован", 401);

  const sid = getSessionId(request);
  if (!sid) return bad("Сессия не найдена", 401);

  return json({ ok: true, session: sid, user: { nick: user.nick } });
}

/** Validate a launcher-stored session id (no browser cookie). */
export async function onRequestPost(context) {
  const { request, env } = context;
  if (!launcherOnly(request)) return bad("Forbidden", 403);
  if (!env.DB) return bad("База не подключена", 503);

  const body = await readJson(request);
  const sid = String(body?.session || "").trim();
  if (!sid) return bad("Нет сессии", 401);

  const row = await env.DB.prepare(
    `SELECT u.nick FROM sessions s
     JOIN users u ON u.id = s.user_id
     WHERE s.id = ? AND datetime(s.expires_at) > datetime('now')`
  )
    .bind(sid)
    .first();
  if (!row) return bad("Сессия истекла", 401);

  return json({ ok: true, session: sid, user: { nick: row.nick } });
}
