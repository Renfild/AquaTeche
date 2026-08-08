import { bad, json } from "../_lib/http.js";
import { requireUser, userIsAdmin } from "../_lib/auth.js";
import { fetchProfileByNick, mapProfile } from "../_lib/profile.js";

export async function onRequestGet(context) {
  const { request, env } = context;
  if (!env.DB) return bad("База не подключена", 503);
  const user = await requireUser(env.DB, request);
  if (!user) return bad("Не авторизован", 401);
  const row = await fetchProfileByNick(env.DB, user.nick);
  const is_admin = await userIsAdmin(env.DB, user.nick, env);
  return json({
    ok: true,
    user: { nick: user.nick, is_admin },
    profile: mapProfile(row),
  });
}
