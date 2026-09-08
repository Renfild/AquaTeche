import { bad, json } from "../../_lib/http.js";
import { requireUser } from "../../_lib/auth.js";

export async function onRequestGet(context) {
  const { request, env } = context;
  if (!env.DB) return bad("База данных D1 не подключена", 503);

  const user = await requireUser(env.DB, request);
  if (!user) return bad("Не авторизован", 401);

  const { results } = await env.DB
    .prepare(
      `SELECT id, source, case_slug, item_spec, item_name, amount, rarity, status, created_at
       FROM player_vault
       WHERE user_id = ?
       ORDER BY id DESC
       LIMIT 50`
    )
    .bind(user.id)
    .all();

  return json({
    ok: true,
    vault: results || [],
  });
}
