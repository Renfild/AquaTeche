-- Migration 0005: skill_points, learned_skills, quests_done columns
-- AquaTech v2.9.49+

ALTER TABLE profiles ADD COLUMN skill_points INTEGER NOT NULL DEFAULT 0;
ALTER TABLE profiles ADD COLUMN learned_skills_json TEXT NOT NULL DEFAULT '["origin"]';
ALTER TABLE profiles ADD COLUMN quests_done INTEGER NOT NULL DEFAULT 0;
ALTER TABLE profiles ADD COLUMN quests_total INTEGER NOT NULL DEFAULT 0;
ALTER TABLE profiles ADD COLUMN leaderboard_rank INTEGER NOT NULL DEFAULT 0;

CREATE INDEX IF NOT EXISTS idx_profiles_skill_points ON profiles(skill_points DESC);
