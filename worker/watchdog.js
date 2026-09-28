// Uptime watchdog: runs on the worker cron, keeps state in D1 and pings the
// #логи webhook when the Minecraft server flips online/offline.
// The portal status API only samples when someone visits the site, so this is
// the piece that actually notices the server going down at 3am.

const DEFAULT_HOST = "g-pl-2.apexnodes.xyz";
const DEFAULT_PORT = 21924;
const STATE_KEY = "watchdog_online";
const SAMPLE_MIN_GAP_MS = 4 * 60_000;
const SAMPLE_RETENTION_MS = 45 * 24 * 3600_000;
const TIMEOUT_MS = 8000;

function resolveAddress(env) {
  const raw = String(env?.SERVER_ADDRESS || `${DEFAULT_HOST}:${DEFAULT_PORT}`).trim();
  const cleaned = raw.replace(/^https?:\/\//, "");
  const idx = cleaned.lastIndexOf(":");
  if (idx > 0) {
    const host = cleaned.slice(0, idx).trim();
    const port = Number(cleaned.slice(idx + 1)) || DEFAULT_PORT;
    return { host, port, address: `${host}:${port}` };
  }
  const host = cleaned || DEFAULT_HOST;
  return { host, port: DEFAULT_PORT, address: `${host}:${DEFAULT_PORT}` };
}

function parseStatus(data, host, port, address) {
  if (!data || typeof data !== "object" || typeof data.online !== "boolean") return null;
  return {
    online: data.online,
    players_online: Number(data.players?.online ?? 0) || 0,
    players_max: Number(data.players?.max ?? 0) || 0,
    host,
    port,
    address,
    source: typeof data.players === "object" ? "mcstatus.io" : "mcsrvstat.us",
  };
}

async function fetchFrom(url, host, port, address) {
  const ctrl = new AbortController();
  const timer = setTimeout(() => ctrl.abort(), TIMEOUT_MS);
  try {
    const res = await fetch(url, {
      headers: { Accept: "application/json", "User-Agent": "AquaTechWatchdog/1.0" },
      signal: ctrl.signal,
    });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const parsed = parseStatus(await res.json(), host, port, address);
    if (!parsed) throw new Error("unparsed");
    return parsed;
  } finally {
    clearTimeout(timer);
  }
}

async function fetchStatus(host, port, address) {
  const mirrors = [
    `https://api.mcstatus.io/v2/status/java/${encodeURIComponent(address)}`,
    `https://api.mcsrvstat.us/3/${encodeURIComponent(address)}`,
  ];
  const answers = await Promise.allSettled(mirrors.map((url) => fetchFrom(url, host, port, address)));
  return answers.filter((a) => a.status === "fulfilled").map((a) => a.value);
}

async function postAlert(env, content) {
  const url = String(env?.ALERT_WEBHOOK_URL || "").trim();
  if (!url) return;
  try {
    await fetch(url, {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify({ content }),
    });
  } catch (err) {
    console.warn("alert webhook failed:", err);
  }
}

async function ensureMeta(db) {
  await db.prepare("CREATE TABLE IF NOT EXISTS worker_meta (key TEXT PRIMARY KEY, value TEXT)").run();
}

async function recordSample(db, status) {
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

  const last = await db.prepare("SELECT at FROM server_status_samples ORDER BY id DESC LIMIT 1").first();
  const lastAt = last?.at ? Date.parse(last.at) : 0;
  if (Number.isFinite(lastAt) && lastAt > 0 && Date.now() - lastAt < SAMPLE_MIN_GAP_MS) return;

  await db
    .prepare("INSERT INTO server_status_samples (online, players, max_players, source) VALUES (?, ?, ?, ?)")
    .bind(status.online ? 1 : 0, status.players_online, status.players_max, status.source)
    .run();

  await db
    .prepare("DELETE FROM server_status_samples WHERE at < ?")
    .bind(new Date(Date.now() - SAMPLE_RETENTION_MS).toISOString())
    .run();
}

export async function runStatusWatchdog(env) {
  const db = env?.DB;
  if (!db) return;

  const { host, port, address } = resolveAddress(env);
  const results = await fetchStatus(host, port, address);
  if (!results.length) {
    console.warn("watchdog: no mirror answered, skipping run");
    return;
  }

  // Prefer the optimistic answer: a single flaky mirror must not page anyone.
  const status = results.find((r) => r.online) || results[0];

  await ensureMeta(db);
  const prevRow = await db.prepare("SELECT value FROM worker_meta WHERE key = ?").bind(STATE_KEY).first();
  const prev = prevRow?.value ?? null;
  const next = status.online ? "online" : "offline";

  if (prev !== null && prev !== next) {
    if (next === "online") {
      await postAlert(
        env,
        `🟢 **Сервер снова онлайн** — \`${address}\`, игроков: ${status.players_online}/${status.players_max}`
      );
    } else {
      await postAlert(env, `🔴 **Сервер недоступен** — \`${address}\`. Панель: https://panel.apexnodes.xyz`);
    }
  }

  await db
    .prepare(
      "INSERT INTO worker_meta (key, value) VALUES (?, ?) ON CONFLICT(key) DO UPDATE SET value = excluded.value"
    )
    .bind(STATE_KEY, next)
    .run();

  await recordSample(db, status);
}
