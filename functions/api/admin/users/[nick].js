import { bad, json, readJson } from "../../../_lib/http.js";
import { normalizeNick, nickOk, requireAdmin } from "../../../_lib/auth.js";

export async function onRequestPatch(context) {
  const { request, env, params } = context;
  if (!env.DB) return bad("База не подключена", 503);
  const admin = await requireAdmin(env.DB, request, env);
  if (!admin) return bad("Нет доступа", 403);

  const nick = normalizeNick(params?.nick || "");
  if (!nickOk(nick)) return bad("Неверный ник");

  const body = await readJson(request);
  if (!body || typeof body !== "object") return bad("Нужен JSON");

  const user = await env.DB.prepare("SELECT id, nick FROM users WHERE nick = ? COLLATE NOCASE")
    .bind(nick)
    .first();
  if (!user) return bad("Игрок не найден", 404);

  if ("is_admin" in body) {
    try {
      await env.DB.prepare("UPDATE users SET is_admin = ? WHERE id = ?")
        .bind(body.is_admin ? 1 : 0, user.id)
        .run();
    } catch {
      return bad("Колонка is_admin ещё не создана (нужна миграция 0003)", 503);
    }
  }

  const profileBits = [];
  const binds = [];
  if ("privilege" in body) {
    profileBits.push("privilege = ?");
    binds.push(String(body.privilege || "Игрок").slice(0, 40));
  }
  if ("bio" in body) {
    profileBits.push("bio = ?");
    binds.push(String(body.bio || "").slice(0, 280));
  }
  for (const key of ["coins", "likes", "fish", "playtime_hours"]) {
    if (key in body) {
      const n = Number(body[key]);
      if (!Number.isFinite(n) || n < 0) return bad(`${key}: число ≥ 0`);
      profileBits.push(`${key} = ?`);
      binds.push(Math.floor(n));
    }
  }
  if (profileBits.length) {
    binds.push(user.id);
    await env.DB.prepare(`UPDATE profiles SET ${profileBits.join(", ")} WHERE user_id = ?`)
      .bind(...binds)
      .run();
  }

  let row;
  try {
    row = await env.DB.prepare(
      `SELECT u.nick, COALESCE(u.is_admin, 0) AS is_admin,
              p.privilege, p.coins, p.likes, p.fish, p.playtime_hours, p.bio
       FROM users u LEFT JOIN profiles p ON p.user_id = u.id
       WHERE u.id = ?`
    )
      .bind(user.id)
      .first();
  } catch {
    row = await env.DB.prepare(
      `SELECT u.nick, 0 AS is_admin,
              p.privilege, p.coins, p.likes, p.fish, p.playtime_hours, p.bio
       FROM users u LEFT JOIN profiles p ON p.user_id = u.id
       WHERE u.id = ?`
    )
      .bind(user.id)
      .first();
  }

  return json({
    ok: true,
    user: {
      nick: row.nick,
      is_admin: Number(row.is_admin) === 1,
      privilege: row.privilege || "Игрок",
      coins: row.coins || 0,
      likes: row.likes || 0,
      fish: row.fish || 0,
      playtime_hours: row.playtime_hours || 0,
      bio: row.bio || "",
    },
  });
}
