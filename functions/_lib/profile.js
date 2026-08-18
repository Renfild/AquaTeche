export function mapProfile(row) {
  if (!row) return null;
  let badges = [];
  try {
    badges = JSON.parse(row.badges_json || "[]");
  } catch {
    badges = [];
  }
  let learnedSkills = ["origin"];
  try {
    if (row.learned_skills_json) {
      learnedSkills = JSON.parse(row.learned_skills_json);
    }
  } catch {
    learnedSkills = ["origin"];
  }
  return {
    nick: row.nick,
    bio: row.bio || "Исследователь глубин AquaTech.",
    theme: row.theme || "ocean",
    status_message: row.status_message || "",
    fav_rod: row.fav_rod || "",
    social_tg: row.social_tg || "",
    social_vk: row.social_vk || "",
    social_discord: row.social_discord || "",
    privilege: row.privilege || "Игрок",
    coins: row.coins ?? 0,
    likes: row.likes ?? 0,
    fish: row.fish ?? 0,
    has_liked: Boolean(row.has_liked),
    skill_points: row.skill_points ?? 0,
    learned_skills: learnedSkills,
    quests_done: row.quests_done ?? 0,
    quests_total: row.quests_total || 25,
    leaderboard_rank: row.leaderboard_rank || 1,
    playtime: `${row.playtime_hours ?? 0} ч`,
    playtime_hours: row.playtime_hours ?? 0,
    views: row.views ?? 0,
    badges,
    updated_at: row.updated_at,
  };
}

export async function fetchProfileByNick(db, nick, currentUserId = null) {
  if (currentUserId) {
    return db
      .prepare(
        `SELECT u.id AS user_id, u.nick, p.bio, p.theme, p.status_message, p.fav_rod,
                p.social_tg, p.social_vk, p.social_discord,
                p.privilege, p.coins, p.likes, p.fish,
                p.skill_points, p.learned_skills_json, p.quests_done, p.quests_total, p.leaderboard_rank,
                p.playtime_hours, p.views, p.badges_json, p.updated_at,
                (SELECT 1 FROM profile_likes WHERE from_user_id = ? AND to_user_id = u.id LIMIT 1) AS has_liked
         FROM users u
         JOIN profiles p ON p.user_id = u.id
         WHERE u.nick = ? COLLATE NOCASE`
      )
      .bind(currentUserId, nick)
      .first();
  }
  return db
    .prepare(
      `SELECT u.id AS user_id, u.nick, p.bio, p.theme, p.status_message, p.fav_rod,
              p.social_tg, p.social_vk, p.social_discord,
              p.privilege, p.coins, p.likes, p.fish,
              p.skill_points, p.learned_skills_json, p.quests_done, p.quests_total, p.leaderboard_rank,
              p.playtime_hours, p.views, p.badges_json, p.updated_at,
              0 AS has_liked
       FROM users u
       JOIN profiles p ON p.user_id = u.id
       WHERE u.nick = ? COLLATE NOCASE`
    )
    .bind(nick)
    .first();
}

export async function bumpViews(db, nick) {
  await db
    .prepare(
      `UPDATE profiles SET views = views + 1, updated_at = strftime('%Y-%m-%dT%H:%M:%fZ', 'now')
       WHERE user_id = (SELECT id FROM users WHERE nick = ? COLLATE NOCASE)`
    )
    .bind(nick)
    .run();
}
