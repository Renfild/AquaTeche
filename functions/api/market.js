import { bad, json, readJson } from "../_lib/http.js";

/**
 * Lumen Market — player-to-player item market, server-authoritative.
 * All requests come from the game server with the X-AquaTech-Server-Key header.
 * Seller proceeds are credited to the portal wallet at buy time; the existing
 * coin sync pulls them into the game automatically.
 */

function auth(context) {
  const { request, env } = context;
  const serverKey = request.headers.get("X-AquaTech-Server-Key") || "";
  const expectedKey = env.SERVER_SYNC_KEY || "";
  if (!expectedKey || serverKey !== expectedKey) {
    return false;
  }
  return Boolean(env.DB);
}

export async function onRequestGet(context) {
  const { request, env } = context;
  if (!auth(context)) return bad("Неверный ключ сервера", 403);

  const url = new URL(request.url);
  const limit = Math.min(60, Math.max(1, Number(url.searchParams.get("limit") || 40)));
  const lots = await env.DB.prepare(
    `SELECT id, seller, item_id, label, count, price, created_at
     FROM market_listings WHERE status = 'open'
     ORDER BY created_at DESC LIMIT ?`
  )
    .bind(limit)
    .all();
  return json({ ok: true, lots: lots.results || [] });
}

export async function onRequestPost(context) {
  const { request, env } = context;
  if (!auth(context)) return bad("Неверный ключ сервера", 403);

  const body = await readJson(request);
  if (!body || !body.op) return bad("Укажите op");
  const op = String(body.op);

  if (op === "sell") {
    const nick = String(body.nick || "").trim();
    const itemId = String(body.item_id || "").trim();
    const label = String(body.label || itemId).slice(0, 64);
    const nbt = String(body.nbt || "").slice(0, 8000);
    const count = Math.max(1, Math.min(2304, Math.floor(Number(body.count || 1))));
    const price = Math.max(1, Math.min(50000000, Math.floor(Number(body.price || 0))));
    if (!nick || !itemId || !nbt || !price) return bad("Некорректный лот");

    const open = await env.DB.prepare(
      "SELECT COUNT(*) AS n FROM market_listings WHERE seller = ? AND status = 'open'"
    )
      .bind(nick)
      .first();
    if ((open?.n || 0) >= 12) return bad("Слишком много открытых лотов (макс. 12)", 429);

    const res = await env.DB.prepare(
      `INSERT INTO market_listings (seller, item_id, label, nbt, count, price)
       VALUES (?, ?, ?, ?, ?, ?)`
    )
      .bind(nick, itemId, label, nbt, count, price)
      .run();
    return json({ ok: true, id: res.meta.last_row_id });
  }

  if (op === "buy") {
    const id = Math.floor(Number(body.id || 0));
    const buyer = String(body.buyer || "").trim();
    if (!id || !buyer) return bad("Некорректная покупка");

    // Атомарно одной транзакцией: помечаем лот проданным и кредитуем продавца,
    // иначе сбой между запросами оставляет продавца без монет.
    const results = await env.DB.batch([
      env.DB.prepare(
        `UPDATE market_listings
         SET status = 'sold', buyer = ?, updated_at = strftime('%Y-%m-%dT%H:%M:%fZ','now')
         WHERE id = ? AND status = 'open' AND seller != ? COLLATE NOCASE`
      ).bind(buyer, id, buyer),
      env.DB.prepare(
        `UPDATE profiles
         SET coins = coins + (SELECT price FROM market_listings WHERE id = ?),
             updated_at = strftime('%Y-%m-%dT%H:%M:%fZ','now')
         WHERE user_id = (SELECT id FROM users WHERE nick = (SELECT seller FROM market_listings WHERE id = ?) COLLATE NOCASE)`
      ).bind(id, id),
    ]);

    if (!results[0].meta.changes) {
      return bad("Лот уже продан, отменён или это ваш собственный лот", 409);
    }

    const lot = await env.DB.prepare(
      "SELECT seller, item_id, label, nbt, count, price FROM market_listings WHERE id = ?"
    )
      .bind(id)
      .first();
    return json({ ok: true, lot });
  }

  if (op === "cancel") {
    const id = Math.floor(Number(body.id || 0));
    const nick = String(body.nick || "").trim();
    if (!id || !nick) return bad("Некорректная отмена");

    const cancelled = await env.DB.prepare(
      `UPDATE market_listings
       SET status = 'cancelled', updated_at = strftime('%Y-%m-%dT%H:%M:%fZ','now')
       WHERE id = ? AND seller = ? COLLATE NOCASE AND status = 'open'`
    )
      .bind(id, nick)
      .run();
    if (!cancelled.meta.changes) return bad("Лот не найден или уже закрыт", 409);

    const lot = await env.DB.prepare(
      "SELECT item_id, label, nbt, count FROM market_listings WHERE id = ?"
    )
      .bind(id)
      .first();
    return json({ ok: true, lot });
  }

  return bad("Неизвестная операция");
}
