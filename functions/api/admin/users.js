import { bad, json } from "../../_lib/http.js";
import { requireAdmin } from "../../_lib/auth.js";

export async function onRequestGet(context) {
  const { request, env } = context;
  if (!env.DB) return bad("База не подключена", 503);
  const admin = await requireAdmin(env.DB, request, env);
  if (!admin) return bad("Нет доступа", 403);

  const url = new URL(request.url);
  const q = String(url.searchParams.get("q") || "")
    .trim()
    .slice(0, 32);
  const limit = Math.min(50, Math.max(1, Number(url.searchParams.get("limit") || 40) || 40));

  let sql = `SELECT u.id, u.nick, u.created_at,
                    COALESCE(u.is_admin, 0) AS is_admin,
                    p.privilege, p.coins, p.likes, p.fish, p.playtime_hours, p.bio
             FROM users u
             LEFT JOIN profiles p ON p.user_id = u.id`;
  const binds = [];
  if (q) {
    sql += " WHERE u.nick LIKE ? COLLATE NOCASE";
    binds.push(`%${q}%`);
  }
  sql += " ORDER BY u.created_at DESC LIMIT ?";
  binds.push(limit);

  let res;
  try {
    res = await env.DB.prepare(sql)
      .bind(...binds)
      .all();
  } catch {
    // is_admin column may be missing
    sql = `SELECT u.id, u.nick, u.created_at, 0 AS is_admin,
                  p.privilege, p.coins, p.likes, p.fish, p.playtime_hours, p.bio
           FROM users u
           LEFT JOIN profiles p ON p.user_id = u.id`;
    const binds2 = [];
    if (q) {
      sql += " WHERE u.nick LIKE ? COLLATE NOCASE";
      binds2.push(`%${q}%`);
    }
    sql += " ORDER BY u.created_at DESC LIMIT ?";
    binds2.push(limit);
    res = await env.DB.prepare(sql)
      .bind(...binds2)
      .all();
  }

  const users = (res.results || []).map((row) => ({
    id: row.id,
    nick: row.nick,
    created_at: row.created_at,
    is_admin: Number(row.is_admin) === 1,
    privilege: row.privilege || "Игрок",
    coins: row.coins || 0,
    likes: row.likes || 0,
    fish: row.fish || 0,
    playtime_hours: row.playtime_hours || 0,
    bio: row.bio || "",
  }));

  return json({ ok: true, users });
}
