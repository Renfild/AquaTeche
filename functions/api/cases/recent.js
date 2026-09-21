import { json } from "../../_lib/http.js";

/**
 * Последние дропы из кейсов (игра + сайт) для витрины кейсов.
 * Читается из player_vault, поэтому отдаёт и предмет, и редкость, и ник.
 */
export async function onRequestGet(context) {
  const { env } = context;
  if (!env.DB) return json({ ok: true, drops: [] });

  try {
    const { results } = await env.DB
      .prepare(
        `SELECT v.item_name, v.item_spec, v.amount, v.rarity, v.case_slug, v.created_at, u.nick
         FROM player_vault v
         JOIN users u ON u.id = v.user_id
         WHERE v.source = 'case'
         ORDER BY v.id DESC
         LIMIT 14`
      )
      .all();
    return json({ ok: true, drops: results || [] });
  } catch (err) {
    console.warn("recent drops unavailable:", err);
    return json({ ok: true, drops: [] });
  }
}
