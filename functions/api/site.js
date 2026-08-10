import { bad, json } from "../_lib/http.js";
import { getSiteCopy } from "../_lib/siteCopy.js";
import { listNews } from "../_lib/news.js";

export async function onRequestGet(context) {
  const { env, request } = context;
  if (!env.DB) return bad("База не подключена", 503);
  const url = new URL(request.url);
  const withNews = url.searchParams.get("news") !== "0";
  try {
    const copy = await getSiteCopy(env.DB);
    const news = withNews
      ? await listNews(env.DB, { publishedOnly: true, limit: 6 })
      : undefined;
    return json({ ok: true, copy, ...(news ? { news } : {}) });
  } catch (err) {
    return bad(err?.message || "Не удалось загрузить сайт", 500);
  }
}
