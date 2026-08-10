import { bad, json, readJson } from "../../../_lib/http.js";
import { requireAdmin } from "../../../_lib/auth.js";
import { ensureNews, getNewsById, mapNewsRow } from "../../../_lib/news.js";

export async function onRequestPatch(context) {
  const { request, env, params } = context;
  if (!env.DB) return bad("База не подключена", 503);
  const admin = await requireAdmin(env.DB, request, env);
  if (!admin) return bad("Нет доступа", 403);

  const id = Number(params?.id);
  if (!Number.isFinite(id) || id < 1) return bad("Неверный id");

  const body = await readJson(request);
  if (!body || typeof body !== "object") return bad("Нужен JSON");

  await ensureNews(env.DB);
  const current = await getNewsById(env.DB, id);
  if (!current) return bad("Новость не найдена", 404);

  const title =
    "title" in body ? String(body.title || "").trim().slice(0, 160) : current.title;
  const text =
    "body" in body ? String(body.body || "").trim().slice(0, 4000) : current.body;
  const published_at =
    "published_at" in body
      ? String(body.published_at || "").trim().slice(0, 32)
      : current.published_at;
  const published =
    "published" in body
      ? body.published === false || body.published === 0
        ? 0
        : 1
      : current.published
        ? 1
        : 0;

  if (title.length < 2) return bad("Нужен заголовок");
  if (text.length < 2) return bad("Нужен текст");
  if (!published_at) return bad("Нужна дата");

  await env.DB
    .prepare(
      `UPDATE news
       SET title = ?, body = ?, published_at = ?, published = ?, updated_at = datetime('now')
       WHERE id = ?`
    )
    .bind(title, text, published_at, published, id)
    .run();

  const row = await env.DB.prepare("SELECT * FROM news WHERE id = ?").bind(id).first();
  return json({ ok: true, item: row ? mapNewsRow(row) : null });
}

export async function onRequestDelete(context) {
  const { request, env, params } = context;
  if (!env.DB) return bad("База не подключена", 503);
  const admin = await requireAdmin(env.DB, request, env);
  if (!admin) return bad("Нет доступа", 403);

  const id = Number(params?.id);
  if (!Number.isFinite(id) || id < 1) return bad("Неверный id");

  await ensureNews(env.DB);
  const current = await getNewsById(env.DB, id);
  if (!current) return bad("Новость не найдена", 404);

  await env.DB.prepare("DELETE FROM news WHERE id = ?").bind(id).run();
  return json({ ok: true, deleted: id });
}
