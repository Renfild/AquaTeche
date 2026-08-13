import { bad, json, readJson } from "../../_lib/http.js";

/**
 * POST /api/launcher/verify-token
 * Called by Mohist server (not the browser) to validate a player's session token.
 * Header: x-aquatech-launcher: 1
 * Body: { "session": "<session_id>", "nick": "<minecraft_nick>" }
 * Returns: { ok: true, nick, balance, rank_id } or 401/403.
 */
export async function onRequestPost(context) {
  const { request, env } = context;

  if (request.headers.get("x-aquatech-launcher") !== "1") {
    return bad("Forbidden", 403);
  }

  if (!env.DB) return bad("Database not connected", 503);

  const body = await readJson(request);
  const sid = String(body?.session || "").trim();
  const nick = String(body?.nick || "").trim();

  if (!sid || !nick) return bad("Missing session or nick", 400);

  const row = await env.DB.prepare(
    `SELECT u.nick, u.balance, u.rank_id
     FROM sessions s
     JOIN users u ON u.id = s.user_id
     WHERE s.id = ?
       AND lower(u.nick) = lower(?)
       AND datetime(s.expires_at) > datetime('now')`
  )
    .bind(sid, nick)
    .first();

  if (!row) return bad("Session invalid or expired", 401);

  return json({ ok: true, nick: row.nick, balance: row.balance, rank_id: row.rank_id || "player" });
}
