import { bad, json } from "../_lib/http.js";
import { listNews } from "../_lib/news.js";

export async function onRequestGet(context) {
  const { env } = context;
  if (!env.DB) return bad("База не подключена", 503);
  try {
    const news = await listNews(env.DB, { publishedOnly: true, limit: 40 });
    return json({ ok: true, news });
  } catch (err) {
    return bad(err?.message || "Не удалось загрузить новости", 500);
  }
}
