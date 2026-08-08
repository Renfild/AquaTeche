-- Admin flags + runtime site settings
ALTER TABLE users ADD COLUMN is_admin INTEGER NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS site_settings (
  key TEXT PRIMARY KEY,
  value TEXT NOT NULL
);

INSERT OR IGNORE INTO site_settings (key, value) VALUES ('purchases_enabled', 'false');
INSERT OR IGNORE INTO site_settings (key, value) VALUES ('catalog_from_db', '0');

UPDATE users SET is_admin = 1 WHERE nick = 'Renfild' COLLATE NOCASE;
