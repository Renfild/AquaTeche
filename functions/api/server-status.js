import { json } from "../_lib/http.js";

const DEFAULT_HOST = "g-pl-2.apexnodes.xyz";
const DEFAULT_PORT = 21924;
const CACHE_TTL_MS = 30_000;

/** @type {{ at: number, payload: object } | null} */
let memCache = null;

function resolveAddress(env) {
  const raw = String(env?.SERVER_ADDRESS || `${DEFAULT_HOST}:${DEFAULT_PORT}`).trim();
  const cleaned = raw.replace(/^https?:\/\//, "");
  const idx = cleaned.lastIndexOf(":");
  if (idx > 0) {
    const host = cleaned.slice(0, idx).trim();
    const port = Number(cleaned.slice(idx + 1)) || DEFAULT_PORT;
    return { host, port, address: `${host}:${port}` };
  }
  return { host: cleaned || DEFAULT_HOST, port: DEFAULT_PORT, address: `${cleaned || DEFAULT_HOST}:${DEFAULT_PORT}` };
}

/**
 * Live Minecraft server status (player count from public query APIs).
 * Cached briefly so the header pill does not stall every page load.
 */
export async function onRequestGet(context) {
  const { host, port, address } = resolveAddress(context?.env);
  const now = Date.now();
  if (memCache && now - memCache.at < CACHE_TTL_MS && memCache.payload?.address === address) {
    return json({ ok: true, ...memCache.payload, cached: true });
  }

  const mirrors = [
    `https://api.mcstatus.io/v2/status/java/${encodeURIComponent(address)}`,
    `https://api.mcsrvstat.us/3/${encodeURIComponent(address)}`,
  ];

  const answers = await Promise.allSettled(mirrors.map((url) => fetchStatus(url, host, port, address)));
  const results = answers.filter((a) => a.status === "fulfilled").map((a) => a.value);
  const payload =
    results.find((r) => r && r.online) ||
    results[0] || {
      online: false,
      players_online: 0,
      players_max: 0,
      host,
      port,
      address,
      source: "unreachable",
    };

  memCache = { at: now, payload };
  if (context?.env?.DB) {
    recordSample(context.env.DB, payload, context.waitUntil);
  }
  return json({ ok: true, ...payload, cached: false });
}

const SAMPLE_MIN_GAP_MS = 4 * 60_000;
const SAMPLE_RETENTION_MS = 45 * 24 * 3600_000;

/** Пишем сэмпл в D1 не чаще раза в 4 минуты, чтобы у /api/status-history была история. */
function recordSample(db, payload, waitUntil) {
  const write = async () => {
    try {
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

      const last = await db
        .prepare("SELECT at FROM server_status_samples ORDER BY id DESC LIMIT 1")
        .first();
      const lastAt = last?.at ? Date.parse(last.at) : 0;
      if (Number.isFinite(lastAt) && lastAt > 0 && Date.now() - lastAt < SAMPLE_MIN_GAP_MS) return;

      await db
        .prepare(
          "INSERT INTO server_status_samples (online, players, max_players, source) VALUES (?, ?, ?, ?)"
        )
        .bind(
          payload.online ? 1 : 0,
          Math.max(0, Number(payload.players_online) || 0),
          Math.max(0, Number(payload.players_max) || 0),
          String(payload.source || "")
        )
        .run();

      await db
        .prepare("DELETE FROM server_status_samples WHERE at < ?")
        .bind(new Date(Date.now() - SAMPLE_RETENTION_MS).toISOString())
        .run();
    } catch (err) {
      console.warn("status sample failed:", err);
    }
  };

  if (typeof waitUntil === "function") waitUntil(write());
  else write();
}

async function fetchStatus(url, host, port, address) {
  const ctrl = new AbortController();
  const timer = setTimeout(() => ctrl.abort(), 2500);
  try {
    const res = await fetch(url, {
      headers: { Accept: "application/json", "User-Agent": "AquaTechPortal/1.0" },
      signal: ctrl.signal,
    });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const data = await res.json();
    const parsed = parseStatus(data, host, port, address);
    if (!parsed) throw new Error("unparsed");
    return parsed;
  } finally {
    clearTimeout(timer);
  }
}

function parseStatus(data, host, port, address) {
  if (!data || typeof data !== "object") return null;

  if (typeof data.online === "boolean" && data.players) {
    return {
      online: data.online,
      players_online: Number(data.players.online ?? 0) || 0,
      players_max: Number(data.players.max ?? 0) || 0,
      version: data.version?.name_clean || data.version?.name || null,
      host,
      port,
      address,
      source: "mcstatus.io",
    };
  }

  if (typeof data.online === "boolean") {
    return {
      online: data.online,
      players_online: Number(data.players?.online ?? 0) || 0,
      players_max: Number(data.players?.max ?? 0) || 0,
      version: data.version || null,
      host,
      port,
      address,
      source: "mcsrvstat.us",
    };
  }

  return null;
}
