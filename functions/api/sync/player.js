import { bad, json, readJson } from "../../_lib/http.js";

const DEFAULT_SYNC_KEY = "aquatech_internal_sync_key_2026";

export async function onRequestPost(context) {
  const { request, env } = context;
  if (!env.DB) return bad("База не подключена (D1)", 503);

  const serverKey = request.headers.get("X-AquaTech-Server-Key") || "";
  const expectedKey = env.SERVER_SYNC_KEY || DEFAULT_SYNC_KEY;

  if (serverKey !== expectedKey) {
    return bad("Неверный ключ сервера", 403);
  }

  const body = await readJson(request);
  if (!body || !body.nick) return bad("Укажите ник игрока");

  const nick = String(body.nick).trim();
  const coins = Math.max(0, Math.floor(Number(body.coins || 0)));
  const fish = Math.max(0, Math.floor(Number(body.fish || 0)));
  const playtimeHours = Math.max(0, Math.floor(Number(body.playtime_hours || 0)));
  const privilege = String(body.privilege || "").slice(0, 32);
  const questsDone = Math.max(0, Math.floor(Number(body.quests_done || 0)));

  // Check if user exists
  let user = await env.DB.prepare(
    "SELECT id FROM users WHERE nick = ? COLLATE NOCASE"
  )
    .bind(nick)
    .first();

  if (!user) {
    // Create new player record from server sync
    const res = await env.DB.prepare(
      "INSERT INTO users (nick, password_hash, password_salt) VALUES (?, 'IN_GAME_UNREGISTERED', '')"
    )
      .bind(nick)
      .run();

    const userId = res.meta.last_row_id;
    await env.DB.prepare(
      `INSERT INTO profiles (user_id, bio, theme, privilege, coins, fish, playtime_hours, quests_done)
       VALUES (?, 'Игрок сервера AquaTech.', 'ocean', ?, ?, ?, ?, ?)`
    )
      .bind(
        userId,
        privilege || "Игрок",
        coins,
        fish,
        playtimeHours,
        questsDone
      )
      .run();
  } else {
    // Update existing profile stats
    let updates = [];
    let binds = [];

    if (body.coins !== undefined) {
      updates.push("coins = ?");
      binds.push(coins);
    }
    if (body.fish !== undefined) {
      updates.push("fish = ?");
      binds.push(fish);
    }
    if (body.playtime_hours !== undefined) {
      updates.push("playtime_hours = ?");
      binds.push(playtimeHours);
    }
    if (body.privilege !== undefined && privilege) {
      updates.push("privilege = ?");
      binds.push(privilege);
    }
    if (body.quests_done !== undefined) {
      updates.push("quests_done = ?");
      binds.push(questsDone);
    }

    if (updates.length > 0) {
      updates.push("updated_at = strftime('%Y-%m-%dT%H:%M:%fZ', 'now')");
      binds.push(user.id);

      await env.DB.prepare(
        `UPDATE profiles SET ${updates.join(", ")} WHERE user_id = ?`
      )
        .bind(...binds)
        .run();
    }
  }

  return json({ ok: true, synced: true, nick });
}
