import { bad, json, readJson } from "../../_lib/http.js";

/**
 * POST /api/launcher/verify-token
 * Called by Mohist server (not the browser) to validate a player's session token.
 * Header: X-AquaTech-Server-Key (same secret as /api/sync/player)
 * Body: { "session": "<session_id>", "nick": "<minecraft_nick>" }
 * Returns: { ok: true, nick, balance, rank_id } or 401/403.
 */
export async function onRequestPost(context) {
  const { request, env } = context;

  const serverKey = request.headers.get("X-AquaTech-Server-Key") || "";
  const expectedKey = env.SERVER_SYNC_KEY || "";
  if (!expectedKey || serverKey !== expectedKey) {
    return bad("Forbidden", 403);
  }

  if (!env.DB) return bad("Database not connected", 503);

  const body = await readJson(request);
  const sid = String(body?.session || "").trim();
  const nick = String(body?.nick || "").trim();

  if (!sid || !nick) return bad("Missing session or nick", 400);

  const row = await env.DB.prepare(
    `SELECT u.nick, coalesce(p.coins, 0) AS balance, coalesce(p.privilege, 'player') AS rank_id
     FROM sessions s
     JOIN users u ON u.id = s.user_id
     LEFT JOIN profiles p ON p.user_id = u.id
     WHERE s.id = ?
       AND lower(u.nick) = lower(?)
       AND datetime(s.expires_at) > datetime('now')`
  )
    .bind(sid, nick)
    .first();

  if (!row) return bad("Session invalid or expired", 401);

  return json({ ok: true, nick: row.nick, balance: Number(row.balance) || 0, rank_id: String(row.rank_id || "player") });
}
