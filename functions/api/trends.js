import { bad, json } from "../_lib/http.js";
import { FISH_POOL } from "../trends_data.js";

/**
 * Daily cat-fisher trends: 3 trending fish published by the game server
 * (FishShopConfig / KubeJS share the same deterministic pick).
 * GET  — public, for the site index "Тренд дня" block.
 * POST — server-key only, upserts today's trends.
 */
export async function onRequestGet(context) {
  try {
    return await trendsGet(context);
  } catch (e) {
    return new Response(JSON.stringify({ ok: false, error: String(e && e.stack || e).slice(0, 300) }), {
      status: 500,
      headers: { "content-type": "application/json" },
    });
  }
}

async function trendsGet(context) {
  const { env } = context;
  if (!env.DB) return bad("База не подключена", 503);
  const row = await env.DB
    .prepare("SELECT day, data FROM daily_trends ORDER BY day DESC LIMIT 1")
    .first();
  const day = row ? row.day : daysSinceEpoch();
  let trends = [];
  if (row) {
    try {
      trends = JSON.parse(row.data);
    } catch {
      trends = [];
    }
  }
  if (!trends.length) {
    // Same deterministic math as the game server (FishShopConfig / KubeJS)
    const n = FISH_POOL.length;
    if (n > 0) {
      const i1 = ((day * 7 + 3) % n + n) % n;
      let i2 = ((day * 13 + 5) % n + n) % n;
      if (i2 === i1) i2 = (i2 + 1) % n;
      let i3 = ((day * 29 + 11) % n + n) % n;
      if (i3 === i1 || i3 === i2) i3 = (i3 + 1) % n;
      if (i3 === i1) i3 = (i3 + 1) % n;
      trends = [
        { ...FISH_POOL[i1], mult: 2.0 },
        { ...FISH_POOL[i2], mult: 1.75 },
        { ...FISH_POOL[i3], mult: 1.5 },
      ];
    }
  }
  return json({ ok: true, day, trends });

  function daysSinceEpoch() {
    // Match the game host day (Java LocalDate). Offset is hours east of UTC.
    const off = Number(env.TREND_TZ_OFFSET ?? 3);
    return Math.floor((Date.now() + off * 3600000) / 86400000);
  }
}

export async function onRequestPost(context) {
  const { request, env } = context;
  const serverKey = request.headers.get("X-AquaTech-Server-Key") || "";
  const expectedKey = env.SERVER_SYNC_KEY || "";
  if (!expectedKey || serverKey !== expectedKey) return bad("Неверный ключ сервера", 403);
  if (!env.DB) return bad("База не подключена", 503);

  let body;
  try {
    body = await request.json();
  } catch {
    return bad("Нужен JSON");
  }
  const day = Math.floor(Number(body.day || 0));
  const trends = body.trends;
  if (!day || !Array.isArray(trends) || !trends.length) return bad("Нужны day и trends");

  const data = JSON.stringify(
    trends.slice(0, 5).map((t) => ({
      id: String(t.id || "").slice(0, 80),
      name: String(t.name || "").slice(0, 64),
      mult: Number(t.mult) || 1,
    }))
  );

  await env.DB
    .prepare(
      `INSERT INTO daily_trends (day, data) VALUES (?, ?)
       ON CONFLICT(day) DO UPDATE SET data = excluded.data,
       updated_at = strftime('%Y-%m-%dT%H:%M:%fZ','now')`
    )
    .bind(day, data)
    .run();
  return json({ ok: true, day });
}
