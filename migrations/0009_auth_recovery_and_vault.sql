-- AquaTech D1 Migration 0009: Password Recovery, User Email, and Web Drop Vault

-- 1. Add email column to users table
ALTER TABLE users ADD COLUMN email TEXT;

-- 2. Password reset tokens table
CREATE TABLE IF NOT EXISTS password_reset_tokens (
  token TEXT PRIMARY KEY,
  user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  code TEXT NOT NULL,
  expires_at TEXT NOT NULL,
  used INTEGER NOT NULL DEFAULT 0,
  created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
);

CREATE INDEX IF NOT EXISTS idx_reset_user ON password_reset_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_reset_token_lookup ON password_reset_tokens(token, used, expires_at);

-- 3. Web Drop Vault (Items won from web cases before or after server delivery)
CREATE TABLE IF NOT EXISTS player_vault (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  source TEXT NOT NULL DEFAULT 'case',
  case_slug TEXT NOT NULL DEFAULT '',
  item_spec TEXT NOT NULL,
  item_name TEXT NOT NULL,
  amount INTEGER NOT NULL DEFAULT 1,
  rarity TEXT NOT NULL DEFAULT 'common',
  status TEXT NOT NULL DEFAULT 'delivered',
  created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
);

CREATE INDEX IF NOT EXISTS idx_vault_user ON player_vault(user_id, created_at DESC);
