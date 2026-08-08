export function mapProfile(row) {
  if (!row) return null;
  let badges = [];
  try {
    badges = JSON.parse(row.badges_json || "[]");
  } catch {
    badges = [];
  }
  return {
    nick: row.nick,
    bio: row.bio,
    theme: row.theme,
    privilege: row.privilege,
    coins: row.coins,
    likes: row.likes,
    fish: row.fish,
    playtime: `${row.playtime_hours} ч`,
    playtime_hours: row.playtime_hours,
    views: row.views,
    badges,
    updated_at: row.updated_at,
  };
}

export async function fetchProfileByNick(db, nick) {
  return db
    .prepare(
      `SELECT u.nick, p.bio, p.theme, p.privilege, p.coins, p.likes, p.fish,
              p.playtime_hours, p.views, p.badges_json, p.updated_at
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
