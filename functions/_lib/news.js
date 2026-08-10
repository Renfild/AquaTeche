const DEFAULT_NEWS = [
  {
    title: "Лаунчер 2.9.20",
    body: "Полноэкранный вход, палитра v2, анимации кнопок и мягкие звуки клика.",
    published_at: "2026-08-08",
  },
  {
    title: "Подключение к серверу",
    body: "Заходи по IP с сайта. Отдельный туннель для модов больше не нужен.",
    published_at: "2026-08-01",
  },
  {
    title: "Авторыбалка + StarCatcher",
    body: "Удочки с кастомным лутом и авторыбалкой на сервере.",
    published_at: "2026-07-15",
  },
];

let ensured = false;

export async function ensureNews(db) {
  if (ensured) return;
  await db
    .prepare(
      `CREATE TABLE IF NOT EXISTS news (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        title TEXT NOT NULL,
        body TEXT NOT NULL,
        published_at TEXT NOT NULL,
        published INTEGER NOT NULL DEFAULT 1,
        created_at TEXT NOT NULL DEFAULT (datetime('now')),
        updated_at TEXT NOT NULL DEFAULT (datetime('now'))
      )`
    )
    .run();
  await db
    .prepare(
      `CREATE INDEX IF NOT EXISTS idx_news_published ON news (published, published_at DESC)`
    )
    .run();
  const row = await db.prepare("SELECT COUNT(*) AS n FROM news").first();
  if (!row || Number(row.n) === 0) {
    for (const item of DEFAULT_NEWS) {
      await db
        .prepare(
          `INSERT INTO news (title, body, published_at, published) VALUES (?, ?, ?, 1)`
        )
        .bind(item.title, item.body, item.published_at)
        .run();
    }
  }
  ensured = true;
}

export function mapNewsRow(row) {
  return {
    id: Number(row.id),
    title: String(row.title || ""),
    body: String(row.body || ""),
    published_at: String(row.published_at || ""),
    published: Number(row.published) === 1,
    created_at: row.created_at ? String(row.created_at) : null,
    updated_at: row.updated_at ? String(row.updated_at) : null,
  };
}

export async function listNews(db, { publishedOnly = true, limit = 50 } = {}) {
  await ensureNews(db);
  const cap = Math.min(Math.max(Number(limit) || 50, 1), 100);
  const sql = publishedOnly
    ? `SELECT * FROM news WHERE published = 1 ORDER BY published_at DESC, id DESC LIMIT ?`
    : `SELECT * FROM news ORDER BY published_at DESC, id DESC LIMIT ?`;
  const { results } = await db.prepare(sql).bind(cap).all();
  return (results || []).map(mapNewsRow);
}

export async function getNewsById(db, id) {
  await ensureNews(db);
  const row = await db.prepare("SELECT * FROM news WHERE id = ?").bind(id).first();
  return row ? mapNewsRow(row) : null;
}
