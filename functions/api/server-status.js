import { json } from "../_lib/http.js";

const HOST = "katherine-hydro.tun.ply.gg";
const PORT = 31279;

/**
 * Live Minecraft server status (player count from public query APIs).
 * Same-origin so the site/launcher do not depend on random demo numbers.
 */
export async function onRequestGet() {
  const address = `${HOST}:${PORT}`;
  const mirrors = [
    `https://api.mcstatus.io/v2/status/java/${encodeURIComponent(address)}`,
    `https://api.mcsrvstat.us/3/${encodeURIComponent(address)}`,
  ];

  for (const url of mirrors) {
    try {
      const ctrl = new AbortController();
      const timer = setTimeout(() => ctrl.abort(), 6000);
      const res = await fetch(url, {
        headers: { Accept: "application/json", "User-Agent": "AquaTechPortal/1.0" },
        signal: ctrl.signal,
      });
      clearTimeout(timer);
      if (!res.ok) continue;
      const data = await res.json();
      const parsed = parseStatus(data, address);
      if (parsed) return json({ ok: true, ...parsed });
    } catch {
      /* try next mirror */
    }
  }

  return json({
    ok: true,
    online: false,
    players_online: 0,
    players_max: 0,
    host: HOST,
    port: PORT,
    source: "unreachable",
  });
}

function parseStatus(data, address) {
  if (!data || typeof data !== "object") return null;

  // mcstatus.io v2
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

  // mcsrvstat.us v3
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
