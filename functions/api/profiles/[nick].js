import { bad, json, readJson } from "../../_lib/http.js";
import { requireUser } from "../../_lib/auth.js";
import { bumpViews, fetchProfileByNick, mapProfile } from "../../_lib/profile.js";

export async function onRequestGet(context) {
  const { request, env, params } = context;
  if (!env.DB) return bad("База не подключена (D1)", 503);
  const nick = String(params.nick || "").trim();
  if (!nick) return bad("Ник не указан");

  const row = await fetchProfileByNick(env.DB, nick);
  if (!row) return bad("Игрок не найден", 404);

  await bumpViews(env.DB, nick);
  const fresh = await fetchProfileByNick(env.DB, nick);
  return json({ ok: true, profile: mapProfile(fresh) });
}

export async function onRequestPatch(context) {
  const { request, env, params } = context;
  if (!env.DB) return bad("База не подключена (D1)", 503);
  const user = await requireUser(env.DB, request);
  if (!user) return bad("Не авторизован", 401);

  const nick = String(params.nick || "").trim();
  if (user.nick.toLowerCase() !== nick.toLowerCase()) {
    return bad("Можно редактировать только свой профиль", 403);
  }

  const body = await readJson(request);
  if (!body) return bad("Некорректный JSON");

  const bio = String(body.bio ?? "").slice(0, 280);
  const theme = ["ocean", "deep", "storm", "abyss"].includes(body.theme)
    ? body.theme
    : "ocean";

  await env.DB.prepare(
    `UPDATE profiles
     SET bio = ?, theme = ?, updated_at = strftime('%Y-%m-%dT%H:%M:%fZ', 'now')
     WHERE user_id = ?`
  )
    .bind(bio || "Исследователь глубин AquaTech.", theme, user.id)
    .run();

  const row = await fetchProfileByNick(env.DB, user.nick);
  return json({ ok: true, profile: mapProfile(row) });
}
