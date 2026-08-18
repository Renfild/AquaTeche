import { bad, json } from "../../../_lib/http.js";
import { requireUser } from "../../../_lib/auth.js";

export async function onRequestPost(context) {
  const { request, env, params } = context;
  if (!env.DB) return bad("База не подключена (D1)", 503);

  const currentUser = await requireUser(env.DB, request);
  if (!currentUser) return bad("Войдите в аккаунт, чтобы поставить лайк", 401);

  const targetNick = String(params.nick || "").trim();
  if (!targetNick) return bad("Ник не указан");

  const targetUser = await env.DB.prepare(
    "SELECT id, nick FROM users WHERE nick = ? COLLATE NOCASE"
  )
    .bind(targetNick)
    .first();

  if (!targetUser) return bad("Игрок не найден", 404);

  if (currentUser.id === targetUser.id) {
    return bad("Нельзя ставить лайк своему профилю", 400);
  }

  // Check if like already exists
  const existing = await env.DB.prepare(
    "SELECT 1 FROM profile_likes WHERE from_user_id = ? AND to_user_id = ?"
  )
    .bind(currentUser.id, targetUser.id)
    .first();

  let liked = false;
  if (existing) {
    // Remove like
    await env.DB.batch([
      env.DB.prepare(
        "DELETE FROM profile_likes WHERE from_user_id = ? AND to_user_id = ?"
      ).bind(currentUser.id, targetUser.id),
      env.DB.prepare(
        "UPDATE profiles SET likes = MAX(0, likes - 1), updated_at = strftime('%Y-%m-%dT%H:%M:%fZ', 'now') WHERE user_id = ?"
      ).bind(targetUser.id),
    ]);
    liked = false;
  } else {
    // Add like
    await env.DB.batch([
      env.DB.prepare(
        "INSERT INTO profile_likes (from_user_id, to_user_id) VALUES (?, ?)"
      ).bind(currentUser.id, targetUser.id),
      env.DB.prepare(
        "UPDATE profiles SET likes = likes + 1, updated_at = strftime('%Y-%m-%dT%H:%M:%fZ', 'now') WHERE user_id = ?"
      ).bind(targetUser.id),
    ]);
    liked = true;
  }

  const updatedProfile = await env.DB.prepare(
    "SELECT likes FROM profiles WHERE user_id = ?"
  )
    .bind(targetUser.id)
    .first();

  return json({
    ok: true,
    liked,
    likes: Number(updatedProfile?.likes ?? 0),
  });
}
