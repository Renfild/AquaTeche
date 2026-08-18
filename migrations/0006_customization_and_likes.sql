-- Migration 0006: Extended Profile Customization & Real Likes
-- AquaTech Portal v2.9.50+

ALTER TABLE profiles ADD COLUMN status_message TEXT NOT NULL DEFAULT '';
ALTER TABLE profiles ADD COLUMN fav_rod TEXT NOT NULL DEFAULT '';
ALTER TABLE profiles ADD COLUMN social_tg TEXT NOT NULL DEFAULT '';
ALTER TABLE profiles ADD COLUMN social_vk TEXT NOT NULL DEFAULT '';
ALTER TABLE profiles ADD COLUMN social_discord TEXT NOT NULL DEFAULT '';

CREATE TABLE IF NOT EXISTS profile_likes (
  from_user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  to_user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
  PRIMARY KEY (from_user_id, to_user_id)
);

CREATE INDEX IF NOT EXISTS idx_profile_likes_to ON profile_likes(to_user_id);
