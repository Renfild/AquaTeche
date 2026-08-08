import { bad, json } from "../_lib/http.js";
import { mapProfile } from "../_lib/profile.js";

export async function onRequestGet(context) {
  const { request, env } = context;
  if (!env.DB) return bad("База не подключена (D1)", 503);

  const url = new URL(request.url);
  const q = (url.searchParams.get("q") || "").trim();
  const sort = url.searchParams.get("sort") || "likes";
  const limit = Math.min(50, Math.max(1, Number(url.searchParams.get("limit") || 30)));

  const order =
    sort === "fish"
      ? "p.fish DESC"
      : sort === "coins"
        ? "p.coins DESC"
        : sort === "playtime"
          ? "p.playtime_hours DESC"
          : "p.likes DESC";

  let sql = `
    SELECT u.nick, p.bio, p.theme, p.privilege, p.coins, p.likes, p.fish,
           p.playtime_hours, p.views, p.badges_json, p.updated_at
    FROM users u
    JOIN profiles p ON p.user_id = u.id
  `;
  const binds = [];
  if (q) {
    sql += " WHERE u.nick LIKE ? COLLATE NOCASE";
    binds.push(`%${q.replace(/[%_]/g, "")}%`);
  }
  sql += ` ORDER BY ${order} LIMIT ?`;
  binds.push(limit);

  const stmt = env.DB.prepare(sql);
  const res = await (binds.length ? stmt.bind(...binds) : stmt).all();
  const players = (res.results || []).map(mapProfile);
  return json({ ok: true, players });
}
