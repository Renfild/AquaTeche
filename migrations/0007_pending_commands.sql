CREATE TABLE IF NOT EXISTS pending_commands (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  nick TEXT NOT NULL,
  kind TEXT NOT NULL,
  payload TEXT NOT NULL DEFAULT '',
  provider TEXT NOT NULL DEFAULT '',
  provider_payment_id TEXT NOT NULL DEFAULT '',
  status TEXT NOT NULL DEFAULT 'queued',
  created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')),
  claimed_at TEXT
);

CREATE UNIQUE INDEX IF NOT EXISTS pending_commands_provider_id
  ON pending_commands(provider, provider_payment_id)
  WHERE provider_payment_id != '';

CREATE INDEX IF NOT EXISTS pending_commands_nick_status
  ON pending_commands(nick, status);
