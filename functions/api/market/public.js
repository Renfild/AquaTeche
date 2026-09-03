import { json } from "../../_lib/http.js";

/**
 * Публичные лоты рынка для витрин сайта (без секретного ключа сервера).
 * Ограничение выборки: 1..40 (единый лимит с воркером).
 */
export async function onRequestGet(context) {
  const { env } = context;
  if (!env.DB) return json({ ok: false, lots: [] }, 503);
  const url = new URL(context.request.url);
  const limit = Math.min(40, Math.max(1, Number(url.searchParams.get("limit") || 6)));
  try {
    const lots = await env.DB
      .prepare(
        `SELECT id, seller, label, count, price FROM market_listings
         WHERE status = 'open' ORDER BY created_at DESC LIMIT ?`
      )
      .bind(limit)
      .all();
    return json({ ok: true, lots: lots.results || [] });
  } catch {
    return json({ ok: false, lots: [] }, 500);
  }
}
