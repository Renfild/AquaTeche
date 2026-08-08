import { bad, json, readJson } from "../../../_lib/http.js";
import { requireAdmin } from "../../../_lib/auth.js";
import { setSetting } from "../../../_lib/settings.js";

export async function onRequestPatch(context) {
  const { request, env, params } = context;
  if (!env.DB) return bad("База не подключена", 503);
  const admin = await requireAdmin(env.DB, request, env);
  if (!admin) return bad("Нет доступа", 403);

  const id = Number(params?.id);
  if (!Number.isFinite(id) || id < 1) return bad("Неверный id");

  const body = await readJson(request);
  if (!body || typeof body !== "object") return bad("Нужен JSON");

  const row = await env.DB.prepare("SELECT id FROM catalog_items WHERE id = ?").bind(id).first();
  if (!row) return bad("Не найдено", 404);

  const fields = [];
  const binds = [];
  if ("title" in body) {
    fields.push("title = ?");
    binds.push(String(body.title || "").slice(0, 80));
  }
  if ("description" in body) {
    fields.push("description = ?");
    binds.push(String(body.description || "").slice(0, 500));
  }
  if ("price_rub" in body) {
    const price = Number(body.price_rub);
    if (!Number.isFinite(price) || price < 0) return bad("Цена должна быть числом ≥ 0");
    fields.push("price_rub = ?");
    binds.push(Math.floor(price));
  }
  if ("enabled" in body) {
    fields.push("enabled = ?");
    binds.push(body.enabled ? 1 : 0);
  }
  if ("sort_order" in body) {
    const sort = Number(body.sort_order);
    if (!Number.isFinite(sort)) return bad("sort_order должен быть числом");
    fields.push("sort_order = ?");
    binds.push(Math.floor(sort));
  }
  if ("perks" in body) {
    if (!Array.isArray(body.perks)) return bad("perks: массив строк");
    fields.push("perks_json = ?");
    binds.push(JSON.stringify(body.perks.map((p) => String(p).slice(0, 120)).slice(0, 20)));
  }
  if (!fields.length) return bad("Нечего обновлять");

  binds.push(id);
  await env.DB.prepare(`UPDATE catalog_items SET ${fields.join(", ")} WHERE id = ?`)
    .bind(...binds)
    .run();
  await setSetting(env.DB, "catalog_from_db", "1");

  const updated = await env.DB.prepare(
    `SELECT id, kind, slug, title, price_rub, description, perks_json, enabled, sort_order
     FROM catalog_items WHERE id = ?`
  )
    .bind(id)
    .first();

  let perks = [];
  try {
    perks = JSON.parse(updated.perks_json || "[]");
  } catch {
    perks = [];
  }

  return json({
    ok: true,
    item: {
      id: updated.id,
      kind: updated.kind,
      slug: updated.slug,
      title: updated.title,
      price_rub: updated.price_rub,
      description: updated.description,
      perks,
      enabled: Number(updated.enabled) === 1,
      sort_order: updated.sort_order,
    },
  });
}
