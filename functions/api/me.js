import { bad, json } from "../_lib/http.js";
import { requireUser } from "../_lib/auth.js";
import { fetchProfileByNick, mapProfile } from "../_lib/profile.js";

export async function onRequestGet(context) {
  const { request, env } = context;
  if (!env.DB) return bad("База не подключена (D1)", 503);
  const user = await requireUser(env.DB, request);
  if (!user) return bad("Не авторизован", 401);
  const row = await fetchProfileByNick(env.DB, user.nick);
  return json({ ok: true, user: { nick: user.nick }, profile: mapProfile(row) });
}
