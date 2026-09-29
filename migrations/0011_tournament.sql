-- AquaTech D1 Migration 0011: Weekly fishing tournament (live board + archive)

CREATE TABLE IF NOT EXISTS tournament_weeks (
  week INTEGER PRIMARY KEY,
  ends_at TEXT NOT NULL,
  next_starts_at TEXT,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active', 'finalized')),
  updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
);

CREATE TABLE IF NOT EXISTS tournament_entries (
  week INTEGER NOT NULL,
  uuid TEXT NOT NULL,
  nick TEXT NOT NULL,
  weight REAL NOT NULL,
  fish TEXT NOT NULL DEFAULT '',
  PRIMARY KEY (week, uuid)
);

CREATE INDEX IF NOT EXISTS idx_tournament_entries_weight ON tournament_entries (week, weight DESC);
CREATE INDEX IF NOT EXISTS idx_tournament_entries_uuid ON tournament_entries (uuid);
