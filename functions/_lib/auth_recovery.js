/**
 * Таблицы и колонки восстановления доступа. Обычно их создаёт миграция 0009, но на боевой базе нельзя быть
 * уверенным, что она применена, поэтому код сам досоздаёт недостающее (один раз на подключение к базе).
 */
const ready = new WeakSet();

export async function ensureRecoverySchema(db) {
  if (ready.has(db)) return;
  await db
    .prepare(
      `CREATE TABLE IF NOT EXISTS password_reset_tokens (
         token TEXT PRIMARY KEY,
         user_id INTEGER NOT NULL,
         code TEXT NOT NULL,
         expires_at TEXT NOT NULL,
         used INTEGER NOT NULL DEFAULT 0,
         created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
       )`
    )
    .run();
  await db
    .prepare(
      `CREATE TABLE IF NOT EXISTS email_verifications (
         user_id INTEGER PRIMARY KEY,
         email TEXT NOT NULL,
         code TEXT NOT NULL,
         expires_at TEXT NOT NULL,
         attempts INTEGER NOT NULL DEFAULT 0
       )`
    )
    .run();
  try {
    await db.prepare("ALTER TABLE users ADD COLUMN email TEXT").run();
  } catch {
    // колонка уже есть
  }
  ready.add(db);
}
