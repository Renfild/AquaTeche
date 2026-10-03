CREATE TABLE IF NOT EXISTS pass_premium_owners (
  nick TEXT PRIMARY KEY COLLATE NOCASE,
  source TEXT NOT NULL DEFAULT '',
  created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now'))
);
