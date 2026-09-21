import { json } from "../_lib/http.js";

const RANGES = {
  "24h": { hours: 24, bucketMinutes: 15 },
  "7d": { hours: 24 * 7, bucketMinutes: 60 },
  "30d": { hours: 24 * 30, bucketMinutes: 240 },
};
const MAX_POINTS = 200;

async function ensureTable(db) {
  await db
    .prepare(
      `CREATE TABLE IF NOT EXISTS server_status_samples (
         id INTEGER PRIMARY KEY AUTOINCREMENT,
         at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
         online INTEGER NOT NULL DEFAULT 0,
         players INTEGER NOT NULL DEFAULT 0,
         max_players INTEGER NOT NULL DEFAULT 0,
         source TEXT
       )`
    )
    .run();
}

function bucketKey(iso, bucketMs) {
  const t = Date.parse(iso);
  if (!Number.isFinite(t)) return null;
  return Math.floor(t / bucketMs) * bucketMs;
}

/** История онлайна и аптайма из сэмплов, которые пишет /api/server-status. */
export async function onRequestGet(context) {
  const { request, env } = context;
  const url = new URL(request.url);
  const rangeKey = (url.searchParams.get("range") || "24h").toLowerCase();
  const range = RANGES[rangeKey] || RANGES["24h"];

  if (!env.DB) return json({ ok: true, range: rangeKey, points: [], uptime: null, samples: 0 });

  try {
    await ensureTable(env.DB);

    const sinceIso = new Date(Date.now() - range.hours * 3600_000).toISOString();
    const { results } = await env.DB
      .prepare(
        `SELECT at, online, players
         FROM server_status_samples
         WHERE at >= ?
         ORDER BY at ASC
         LIMIT 12000`
      )
      .bind(sinceIso)
      .all();

    const rows = results || [];
    const bucketMs = range.bucketMinutes * 60_000;
    const buckets = new Map();

    for (const row of rows) {
      const key = bucketKey(row.at, bucketMs);
      if (key == null) continue;
      const bucket = buckets.get(key) || { at: key, samples: 0, onlineSamples: 0, players: 0, peak: 0 };
      bucket.samples += 1;
      if (Number(row.online)) {
        bucket.onlineSamples += 1;
        const players = Math.max(0, Number(row.players) || 0);
        bucket.players += players;
        bucket.peak = Math.max(bucket.peak, players);
      }
      buckets.set(key, bucket);
    }

    const ordered = [...buckets.values()].sort((a, b) => a.at - b.at);
    const step = Math.max(1, Math.ceil(ordered.length / MAX_POINTS));
    const points = ordered
      .filter((_, idx) => idx % step === 0)
      .map((b) => ({
        at: new Date(b.at).toISOString(),
        online: b.onlineSamples > 0 ? 1 : 0,
        playing: b.onlineSamples > 0 ? Math.round(b.players / b.onlineSamples) : 0,
        peak: b.peak,
        samples: b.samples,
        uptime: b.samples ? Math.round((b.onlineSamples / b.samples) * 1000) / 10 : null,
      }));

    const onlineSamples = ordered.reduce((sum, b) => sum + b.onlineSamples, 0);
    const totalSamples = ordered.reduce((sum, b) => sum + b.samples, 0);
    const peakPlayers = ordered.reduce((max, b) => Math.max(max, b.peak), 0);
    const playerAvgDen = ordered.reduce((sum, b) => sum + (b.onlineSamples || 0), 0);
    const playerAvgNum = ordered.reduce((sum, b) => sum + b.players, 0);

    return json({
      ok: true,
      range: rangeKey,
      points,
      firstSampleAt: rows.length ? rows[0].at : null,
      lastSampleAt: rows.length ? rows[rows.length - 1].at : null,
      samples: totalSamples,
      uptime: totalSamples ? Math.round((onlineSamples / totalSamples) * 1000) / 10 : null,
      peakPlayers,
      avgPlayers: playerAvgDen ? Math.round((playerAvgNum / playerAvgDen) * 10) / 10 : 0,
    });
  } catch (err) {
    console.warn("status history unavailable:", err);
    return json({ ok: false, error: "История статуса недоступна", range: rangeKey, points: [] }, 200);
  }
}
