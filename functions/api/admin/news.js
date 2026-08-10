import { bad, json, readJson } from "../../_lib/http.js";
import { requireAdmin } from "../../_lib/auth.js";
import { ensureNews, listNews, mapNewsRow } from "../../_lib/news.js";

export async function onRequestGet(context) {
  const { request, env } = context;
  if (!env.DB) return bad("База не подключена", 503);
  const admin = await requireAdmin(env.DB, request, env);
  if (!admin) return bad("Нет доступа", 403);
  const news = await listNews(env.DB, { publishedOnly: false, limit: 100 });
  return json({ ok: true, news });
}

export async function onRequestPost(context) {
  const { request, env } = context;
  if (!env.DB) return bad("База не подключена", 503);
  const admin = await requireAdmin(env.DB, request, env);
  if (!admin) return bad("Нет доступа", 403);
  const body = await readJson(request);
  if (!body || typeof body !== "object") return bad("Нужен JSON");

  const title = String(body.title || "").trim().slice(0, 160);
  const text = String(body.body || "").trim().slice(0, 4000);
  const published_at = String(body.published_at || new Date().toISOString().slice(0, 10)).slice(0, 32);
  const published = body.published === false || body.published === 0 ? 0 : 1;
  if (title.length < 2) return bad("Нужен заголовок");
  if (text.length < 2) return bad("Нужен текст");

  await ensureNews(env.DB);
  const result = await env.DB
    .prepare(
      `INSERT INTO news (title, body, published_at, published) VALUES (?, ?, ?, ?)
       RETURNING *`
    )
    .bind(title, text, published_at, published)
    .first();

  if (!result) {
    const info = await env.DB
      .prepare(`INSERT INTO news (title, body, published_at, published) VALUES (?, ?, ?, ?)`)
      .bind(title, text, published_at, published)
      .run();
    const id = info?.meta?.last_row_id;
    const row = id
      ? await env.DB.prepare("SELECT * FROM news WHERE id = ?").bind(id).first()
      : null;
    return json({ ok: true, item: row ? mapNewsRow(row) : null }, 201);
  }
  return json({ ok: true, item: mapNewsRow(result) }, 201);
}
