import { json } from "../_lib/http.js";

const HOST = "katherine-hydro.tun.ply.gg";
const PORT = 31279;
const CACHE_TTL_MS = 30_000;

/** @type {{ at: number, payload: object } | null} */
let memCache = null;

/**
 * Live Minecraft server status (player count from public query APIs).
 * Cached briefly so the header pill does not stall every page load.
 */
export async function onRequestGet() {
  const now = Date.now();
  if (memCache && now - memCache.at < CACHE_TTL_MS) {
    return json({ ok: true, ...memCache.payload, cached: true });
  }

  const address = `${HOST}:${PORT}`;
  const mirrors = [
    `https://api.mcstatus.io/v2/status/java/${encodeURIComponent(address)}`,
    `https://api.mcsrvstat.us/3/${encodeURIComponent(address)}`,
  ];

  const payload = await Promise.any(
    mirrors.map((url) => fetchStatus(url, address))
  ).catch(() => ({
    online: false,
    players_online: 0,
    players_max: 0,
    host: HOST,
    port: PORT,
    address,
    source: "unreachable",
  }));

  memCache = { at: now, payload };
  return json({ ok: true, ...payload, cached: false });
}

async function fetchStatus(url, address) {
  const ctrl = new AbortController();
  const timer = setTimeout(() => ctrl.abort(), 2500);
  try {
    const res = await fetch(url, {
      headers: { Accept: "application/json", "User-Agent": "AquaTechPortal/1.0" },
      signal: ctrl.signal,
    });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const data = await res.json();
    const parsed = parseStatus(data, address);
    if (!parsed) throw new Error("unparsed");
    return parsed;
  } finally {
    clearTimeout(timer);
  }
}

function parseStatus(data, address) {
  if (!data || typeof data !== "object") return null;

  if (typeof data.online === "boolean" && data.players) {
    return {
      online: data.online,
      players_online: Number(data.players.online ?? 0) || 0,
      players_max: Number(data.players.max ?? 0) || 0,
      version: data.version?.name_clean || data.version?.name || null,
      host: HOST,
      port: PORT,
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
      host: HOST,
      port: PORT,
      address,
      source: "mcsrvstat.us",
    };
  }

  return null;
}
