import { bad, json } from "../_lib/http.js";
import { requireUser, userIsAdmin } from "../_lib/auth.js";
import { fetchProfileByNick, mapProfile } from "../_lib/profile.js";

async function getRubBalance(db, nick) {
  try {
    await db.prepare("CREATE TABLE IF NOT EXISTS rub_balances (nick TEXT PRIMARY KEY, balance INTEGER NOT NULL DEFAULT 0)").run();
    const row = await db.prepare("SELECT balance FROM rub_balances WHERE nick = ?").bind(nick).first();
    return Number(row?.balance || 0);
  } catch {
    return 0;
  }
}

export async function onRequestGet(context) {
  const { request, env } = context;
  if (!env.DB) return bad("База не подключена", 503);
  const user = await requireUser(env.DB, request);
  if (!user) return bad("Не авторизован", 401);
  const row = await fetchProfileByNick(env.DB, user.nick);
  const is_admin = await userIsAdmin(env.DB, user.nick, env);
  const rub_balance = await getRubBalance(env.DB, user.nick);
  return json({
    ok: true,
    user: { nick: user.nick, is_admin },
    profile: row ? mapProfile(row) : { nick: user.nick },
    rub_balance,
  });
}
