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

  let stats = { total: 0, rare: 0, lastAt: "" };
  try {
    const row = await env.DB
      .prepare(
        `SELECT COUNT(*) AS total,
                SUM(CASE WHEN rarity IN ('rare','epic','legendary','mythic','exotic') THEN 1 ELSE 0 END) AS rare,
                MAX(created_at) AS last_at
         FROM player_vault WHERE user_id = ?`
      )
      .bind(user.id)
      .first();
    stats = {
      total: Number(row?.total || 0),
      rare: Number(row?.rare || 0),
      lastAt: row?.last_at || "",
    };
  } catch {
    // таблицы ещё нет — считаем пустой коллекцией
  }

  return json({
    ok: true,
    vault: results || [],
    stats,
  });
}
