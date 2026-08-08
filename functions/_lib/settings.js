let ensured = false;

export async function ensureSettings(db) {
  if (ensured) return;
  await db
    .prepare(
      `CREATE TABLE IF NOT EXISTS site_settings (
        key TEXT PRIMARY KEY,
        value TEXT NOT NULL
      )`
    )
    .run();
  ensured = true;
}

export async function getSetting(db, key, fallback = "") {
  try {
    await ensureSettings(db);
    const row = await db
      .prepare("SELECT value FROM site_settings WHERE key = ?")
      .bind(key)
      .first();
    if (row && row.value != null) return String(row.value);
  } catch {
    /* ignore */
  }
  return fallback;
}

export async function setSetting(db, key, value) {
  await ensureSettings(db);
  await db
    .prepare(
      `INSERT INTO site_settings (key, value) VALUES (?, ?)
       ON CONFLICT(key) DO UPDATE SET value = excluded.value`
    )
    .bind(key, String(value))
    .run();
}

export async function purchasesEnabled(env) {
  const fromDb = await getSetting(env.DB, "purchases_enabled", "");
  if (fromDb !== "") return fromDb.toLowerCase() === "true";
  return String(env.PURCHASES_ENABLED || "false").toLowerCase() === "true";
}
