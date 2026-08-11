import { json } from "../_lib/http.js";

const DEFAULT_HOST = "g-pl-3.apexnodes.xyz";
const DEFAULT_PORT = 21561;
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

  const payload = await Promise.any(
    mirrors.map((url) => fetchStatus(url, host, port, address))
  ).catch(() => ({
    online: false,
    players_online: 0,
    players_max: 0,
    host,
    port,
    address,
    source: "unreachable",
  }));

  memCache = { at: now, payload };
  return json({ ok: true, ...payload, cached: false });
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
