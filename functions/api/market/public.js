import { json } from "../../_lib/http.js";

/**
 * Открытые лоты аукциона для сайта (без авторизации).
 * Пагинация: page (1..), limit (1..48, по умолчанию 12).
 * Для каждого лота отдаём среднюю цену продаж за 30 дней (avg30) и их количество (sold30),
 * чтобы карточка могла показать «выгодно» или «дороже рынка».
 */
export async function onRequestGet(context) {
  const { env } = context;
  if (!env.DB) return json({ ok: false, lots: [], items: [], total: 0, page: 1, pages: 0 }, 503);

  const url = new URL(context.request.url);
  const page = Math.max(1, Math.floor(Number(url.searchParams.get("page") || 1)) || 1);
  const limit = Math.min(48, Math.max(1, Math.floor(Number(url.searchParams.get("limit") || 12)) || 12));
  const offset = (page - 1) * limit;

  try {
    const totalRow = await env.DB
      .prepare("SELECT COUNT(*) AS n FROM market_listings WHERE status = 'open'")
      .first();
    const total = Number(totalRow?.n || 0);

    const rows = await env.DB
      .prepare(
        `SELECT l.id, l.seller, l.item_id, l.label, l.count, l.price, l.created_at,
                (SELECT AVG(m2.price) FROM market_listings m2
                  WHERE m2.item_id = l.item_id AND m2.status = 'sold'
                    AND COALESCE(m2.updated_at, m2.created_at) > datetime('now', '-30 days')) AS avg30,
                (SELECT COUNT(*) FROM market_listings m2
                  WHERE m2.item_id = l.item_id AND m2.status = 'sold'
                    AND COALESCE(m2.updated_at, m2.created_at) > datetime('now', '-30 days')) AS sold30
         FROM market_listings l
         WHERE l.status = 'open'
         ORDER BY l.created_at DESC
         LIMIT ? OFFSET ?`
      )
      .bind(limit, offset)
      .all();

    const lots = (rows.results || []).map((r) => ({
      id: r.id,
      seller: r.seller,
      item_id: r.item_id || "",
      label: r.label || r.item_id || "предмет",
      count: r.count || 1,
      price: r.price || 0,
      created_at: r.created_at || "",
      avg30: r.avg30 ? Math.round(Number(r.avg30)) : 0,
      sold30: Number(r.sold30 || 0),
    }));

    return json({
      ok: true,
      lots,
      items: lots,
      total,
      page,
      limit,
      pages: Math.max(1, Math.ceil(total / limit)),
    });
  } catch {
    return json({ ok: false, lots: [], items: [], total: 0, page, pages: 0 }, 500);
  }
}
