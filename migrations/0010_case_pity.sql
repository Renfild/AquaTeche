-- AquaTech D1 Migration 0010: Case pity counters (web openings mirror the in-game guarantee)

CREATE TABLE IF NOT EXISTS case_pity (
  user_id INTEGER NOT NULL,
  case_slug TEXT NOT NULL,
  counter INTEGER NOT NULL DEFAULT 0,
  updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
  PRIMARY KEY (user_id, case_slug)
);
