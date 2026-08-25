export function cleanPrivilege(priv) {
  if (!priv) return "Игрок";
  let s = String(priv)
    .replace(/[\uE000-\uF8FF\uD800-\uDFFF]/g, "")
    .replace(/§[0-9a-fk-or]/gi, "")
    .trim();
  s = s.replace(/^[\[\(<]+/, "").replace(/[\]\)>]+$/, "").trim();
  const low = s.toLowerCase();
  if (!s || low === "default" || low === "player" || low === "игрок" || low === "матрос" || low === "пролог") return "Игрок";
  if (low.includes("owner") || low.includes("создател") || low.includes("владел")) return "Владелец";
  if (low.includes("admin") || low.includes("админ")) return "Админ";
  if (low.includes("dev") || low.includes("разраб")) return "Разработчик";
  if (low.includes("mod") || low.includes("модер")) return "Модератор";
  if (low.includes("helper") || low.includes("хелпер")) return "Хелпер";
  if (low.includes("manager") || low.includes("куратор") || low.includes("менеджер")) return "Куратор";
  if (low.includes("staff") || low.includes("персонал")) return "Персонал";
  if (low.includes("vipplus") || low.includes("vip+")) return "VIP+";
  if (low.includes("vip")) return "VIP";
  if (low.includes("deluxe") || low.includes("делюкс")) return "Deluxe";
  if (low.includes("ultimate") || low.includes("ультимейт")) return "Ultimate";
  if (low.includes("legend") || low.includes("легенд")) return "Легенда";
  if (low.includes("admiral") || low.includes("адмирал")) return "Адмирал";
  if (low.includes("captain") || low.includes("капитан")) return "Капитан";
  if (low.includes("skipper") || low.includes("шкипер")) return "Шкипер";
  if (low.includes("sailor") || low.includes("моряк")) return "Моряк";
  if (low.includes("streamer") || low.includes("стример") || low.includes("twitch")) return "Стример";
  if (low.includes("youtuber") || low.includes("ютубер") || low.includes("youtube")) return "YouTuber";
  if (low.includes("artist") || low.includes("артист")) return "Артист";
  if (low.includes("builder") || low.includes("билдер") || low.includes("строитель")) return "Билдер";
  if (low.includes("friend") || low.includes("друг")) return "Друг";
  if (low.includes("trainee") || low.includes("стажер") || low.includes("стажёр")) return "Стажер";
  return s;
}


export function computePlayerBadges(row) {
  const list = [];
  let manual = [];
  try {
    manual = JSON.parse(row.badges_json || "[]");
  } catch {
    manual = [];
  }
  for (const b of manual) {
    if (b && typeof b === "string") {
      list.push({ title: b, rarity: "special", desc: "Особая награда" });
    } else if (b && b.title) {
      list.push({
        title: b.title,
        rarity: b.rarity || "special",
        desc: b.desc || "Особый титул",
      });
    }
  }

  // 1. Fishing tier badges
  const fish = Number(row.fish || 0);
  if (fish >= 1000) {
    list.push({ title: "Легенда океана", rarity: "legendary", desc: "Поймано более 1 000 рыб" });
  } else if (fish >= 500) {
    list.push({ title: "Охотник Бездны", rarity: "epic", desc: "Поймано более 500 рыб" });
  } else if (fish >= 250) {
    list.push({ title: "Мастер катушки", rarity: "rare", desc: "Поймано более 250 рыб" });
  } else if (fish >= 100) {
    list.push({ title: "Опытный удильщик", rarity: "rare", desc: "Поймано более 100 рыб" });
  } else if (fish >= 25) {
    list.push({ title: "Рыболов-любитель", rarity: "common", desc: "Поймано более 25 рыб" });
  } else {
    list.push({ title: "Новичок глубин", rarity: "common", desc: "Первые шаги в рыбалке" });
  }

  // 2. Economy badges
  const coins = Number(row.coins || 0);
  if (coins >= 500000) {
    list.push({ title: "Олигарх глубин", rarity: "legendary", desc: "Баланс более 500 000 ¤" });
  } else if (coins >= 100000) {
    list.push({ title: "Океанский магнат", rarity: "epic", desc: "Баланс более 100 000 ¤" });
  } else if (coins >= 50000) {
    list.push({ title: "Состоятельный", rarity: "rare", desc: "Баланс более 50 000 ¤" });
  } else if (coins >= 10000) {
    list.push({ title: "Первый капитал", rarity: "common", desc: "Баланс более 10 000 ¤" });
  }

  // 3. Playtime badges
  const hours = Number(row.playtime_hours || 0);
  if (hours >= 100) {
    list.push({ title: "Хранитель океана", rarity: "legendary", desc: "Более 100 часов на сервере" });
  } else if (hours >= 50) {
    list.push({ title: "Ветеран AquaTech", rarity: "epic", desc: "Более 50 часов на сервере" });
  } else if (hours >= 20) {
    list.push({ title: "Бывалый мореплаватель", rarity: "rare", desc: "Более 20 часов на сервере" });
  } else if (hours >= 5) {
    list.push({ title: "Житель плота", rarity: "common", desc: "Более 5 часов на сервере" });
  }

  // 4. Social / Likes badges
  const likes = Number(row.likes || 0);
  if (likes >= 50) {
    list.push({ title: "Звезда сообщества", rarity: "legendary", desc: "Более 50 похвал от игроков" });
  } else if (likes >= 15) {
    list.push({ title: "Любимец океана", rarity: "epic", desc: "Более 15 похвал от игроков" });
  } else if (likes >= 5) {
    list.push({ title: "Заметный игрок", rarity: "rare", desc: "Более 5 похвал от игроков" });
  }

  // 5. Privilege / Staff badges
  const priv = cleanPrivilege(row.privilege);
  if (priv && priv !== "Игрок") {
    const isStaff = ["Создатель", "Владелец", "Owner", "Админ", "Admin", "Разработчик", "Developer", "Управляющий", "Manager"].includes(priv);
    list.push({
      title: priv,
      rarity: isStaff ? "legendary" : "epic",
      desc: `Привилегия ${priv}`,
    });
  }

  const seen = new Set();
  const deduped = [];
  for (const item of list) {
    if (!seen.has(item.title.toLowerCase())) {
      seen.add(item.title.toLowerCase());
      deduped.push(item);
    }
  }
  return deduped;
}

export function mapProfile(row) {
  if (!row) return null;
  const badges = computePlayerBadges(row);
  let learnedSkills = ["origin"];
  try {
    if (row.learned_skills_json) {
      learnedSkills = JSON.parse(row.learned_skills_json);
    }
  } catch {
    learnedSkills = ["origin"];
  }
  const cleanPriv = cleanPrivilege(row.privilege);
  return {
    nick: row.nick,
    bio: row.bio || "Исследователь глубин AquaTech.",
    theme: row.theme || "ocean",
    status_message: row.status_message || "",
    fav_rod: row.fav_rod || "",
    social_tg: row.social_tg || "",
    social_vk: row.social_vk || "",
    social_discord: row.social_discord || "",
    privilege: cleanPriv,
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
