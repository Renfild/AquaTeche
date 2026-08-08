import { bad, json } from "../_lib/http.js";

export async function onRequestGet(context) {
  const { request, env } = context;
  if (!env.DB) return bad("База не подключена (D1)", 503);

  const url = new URL(request.url);
  const kind = url.searchParams.get("kind"); // store | case | null

  let sql = `SELECT id, kind, slug, title, price_rub, description, perks_json, enabled, sort_order
             FROM catalog_items WHERE enabled = 1`;
  const binds = [];
  if (kind === "store" || kind === "case") {
    sql += " AND kind = ?";
    binds.push(kind);
  }
  sql += " ORDER BY sort_order ASC, id ASC";

  const stmt = env.DB.prepare(sql);
  const res = await (binds.length ? stmt.bind(...binds) : stmt).all();
  const items = (res.results || []).map((row) => {
    let perks = [];
    try {
      perks = JSON.parse(row.perks_json || "[]");
    } catch {
      perks = [];
    }
    return {
      id: row.id,
      kind: row.kind,
      slug: row.slug,
      title: row.title,
      price_rub: row.price_rub,
      description: row.description,
      perks,
    };
  });

  const purchasesEnabled = String(env.PURCHASES_ENABLED || "false").toLowerCase() === "true";
  return json({ ok: true, purchases_enabled: purchasesEnabled, items });
}
