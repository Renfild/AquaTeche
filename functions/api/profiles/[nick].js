import { bad, json, readJson } from "../../_lib/http.js";
import { requireUser } from "../../_lib/auth.js";
import { bumpViews, fetchProfileByNick, mapProfile } from "../../_lib/profile.js";

const VALID_THEMES = [
  "ocean",
  "deep",
  "storm",
  "abyss",
  "magma",
  "celestial",
  "cyber",
  "aurora",
];

// Telegram / VK / Discord handles: letters, digits, _ . - # and / only.
function sanitizeHandle(value, maxLen) {
  return String(value ?? "")
    .replace(/[^A-Za-z0-9_./#@-]/g, "")
    .slice(0, maxLen);
}

export async function onRequestGet(context) {
  const { request, env, params } = context;
  if (!env.DB) return bad("База не подключена (D1)", 503);
  const nick = String(params.nick || "").trim();
  if (!nick) return bad("Ник не указан");

  const currentUser = await requireUser(env.DB, request);
  const row = await fetchProfileByNick(env.DB, nick, currentUser?.id);
  if (!row) return bad("Игрок не найден", 404);

  await bumpViews(env.DB, nick);
  const fresh = await fetchProfileByNick(env.DB, nick, currentUser?.id);
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

  const bio = String(body.bio ?? "").slice(0, 300);
  const theme = VALID_THEMES.includes(body.theme) ? body.theme : "ocean";
  const status_message = String(body.status_message ?? "").slice(0, 80);
  const fav_rod = String(body.fav_rod ?? "").slice(0, 50);
  const social_tg = sanitizeHandle(body.social_tg, 60);
  const social_vk = sanitizeHandle(body.social_vk, 60);
  const social_discord = sanitizeHandle(body.social_discord, 60);

  await env.DB.prepare(
    `UPDATE profiles
     SET bio = ?, theme = ?, status_message = ?, fav_rod = ?,
         social_tg = ?, social_vk = ?, social_discord = ?,
         updated_at = strftime('%Y-%m-%dT%H:%M:%fZ', 'now')
     WHERE user_id = ?`
  )
    .bind(
      bio || "Исследователь глубин AquaTech.",
      theme,
      status_message,
      fav_rod,
      social_tg,
      social_vk,
      social_discord,
      user.id
    )
    .run();

  const row = await fetchProfileByNick(env.DB, user.nick, user.id);
  return json({ ok: true, profile: mapProfile(row) });
}
