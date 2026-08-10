-- News posts + editable portal copy keys
CREATE TABLE IF NOT EXISTS news (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  title TEXT NOT NULL,
  body TEXT NOT NULL,
  published_at TEXT NOT NULL,
  published INTEGER NOT NULL DEFAULT 1,
  created_at TEXT NOT NULL DEFAULT (datetime('now')),
  updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_news_published ON news (published, published_at DESC);

INSERT OR IGNORE INTO site_settings (key, value) VALUES ('hero_eyebrow', 'Minecraft 1.20.1 · океанский skyblock');
INSERT OR IGNORE INTO site_settings (key, value) VALUES ('hero_title', 'AquaTech');
INSERT OR IGNORE INTO site_settings (key, value) VALUES ('hero_lead', 'Спавн на плоту. Двенадцать удочек StarCatcher, авторыбалка, кейсы и индустриальные моды. Скачай лаунчер и заходи.');
INSERT OR IGNORE INTO site_settings (key, value) VALUES ('features_title', 'На сервере');
INSERT OR IGNORE INTO site_settings (key, value) VALUES ('features_lead', 'Один мир-океан. Рыбалка, кейсы, прогрессия.');
INSERT OR IGNORE INTO site_settings (key, value) VALUES ('join_title', 'AquaTech Ocean');
INSERT OR IGNORE INTO site_settings (key, value) VALUES ('join_body', 'Океанский skyblock, плот 4×4. Заходи по IP ниже.');
INSERT OR IGNORE INTO site_settings (key, value) VALUES ('footer_blurb', 'Океанский сервер. Скачай лаунчер и заходи.');
INSERT OR IGNORE INTO site_settings (key, value) VALUES ('news_page_lead', 'Что нового на сервере и в лаунчере.');
