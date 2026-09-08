import { bad, json, readJson } from "../../_lib/http.js";
import { requireUser } from "../../_lib/auth.js";
import { CASES_CATALOG } from "../../_lib/cases_data.js";

export async function onRequestPost(context) {
  const { request, env } = context;
  if (!env.DB) return bad("База данных D1 не подключена", 503);

  const user = await requireUser(env.DB, request);
  if (!user) return bad("Не авторизован", 401);

  const body = await readJson(request);
  if (!body) return bad("Некорректный JSON");

  const slug = String(body.slug || "").trim().toLowerCase();
  const caseDef = CASES_CATALOG.find((c) => c.slug.toLowerCase() === slug);
  if (!caseDef) return bad("Кейс не найден", 404);

  const cost = Number(caseDef.cost) || 0;

  // Check current balance
  const profile = await env.DB
    .prepare("SELECT coins FROM profiles WHERE user_id = ?")
    .bind(user.id)
    .first();

  if (!profile) return bad("Профиль игрока не найден", 404);
  if ((profile.coins || 0) < cost) {
    return bad(`Недостаточно монет. Стоимость: ${cost} ¤, у тебя: ${profile.coins || 0} ¤`, 400);
  }

  // Atomically deduct coins
  const deduct = await env.DB
    .prepare("UPDATE profiles SET coins = coins - ? WHERE user_id = ? AND coins >= ?")
    .bind(cost, user.id, cost)
    .run();

  if (!deduct.meta?.changes && !deduct.changes) {
    return bad("Не удалось списать монеты (недостаточный баланс)", 400);
  }

  // Roll item based on weights
  const totalWeight = caseDef.loot.reduce((sum, l) => sum + Math.max(1, Number(l.weight) || 1), 0);
  let pick = Math.floor(Math.random() * totalWeight);
  let selected = caseDef.loot[caseDef.loot.length - 1];

  for (const l of caseDef.loot) {
    pick -= Math.max(1, Number(l.weight) || 1);
    if (pick < 0) {
      selected = l;
      break;
    }
  }

  const min = Math.max(1, Math.min(selected.min || 1, selected.max || 1));
  const max = Math.max(min, Math.max(selected.min || 1, selected.max || 1));
  const amount = min === max ? min : min + Math.floor(Math.random() * (max - min + 1));

  let finalCoins = (profile.coins || 0) - cost;

  if (selected.type === "coins") {
    // Direct coins reward
    try {
      await env.DB
        .prepare("UPDATE profiles SET coins = coins + ? WHERE user_id = ?")
        .bind(amount, user.id)
        .run();
      finalCoins += amount;
    } catch (err) {
      console.warn("Could not add coins reward to profile:", err);
    }
  } else {
    // Deliver in-game item through pending_commands
    const itemSpec = `${selected.item || "minecraft:iron_ingot"}:${amount}`;
    try {
      await env.DB
        .prepare(
          `INSERT INTO pending_commands (nick, kind, payload, provider, status)
           VALUES (?, 'item', ?, 'web_case', 'queued')`
        )
        .bind(user.nick, itemSpec)
        .run();
    } catch (err) {
      try {
        await env.DB.prepare(`
          CREATE TABLE IF NOT EXISTS pending_commands (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            nick TEXT NOT NULL,
            kind TEXT NOT NULL,
            payload TEXT NOT NULL,
            provider TEXT NOT NULL DEFAULT 'web',
            status TEXT NOT NULL DEFAULT 'queued',
            created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
            executed_at TEXT
          );
        `).run();
        await env.DB
          .prepare(
            `INSERT INTO pending_commands (nick, kind, payload, provider, status)
             VALUES (?, 'item', ?, 'web_case', 'queued')`
          )
          .bind(user.nick, itemSpec)
          .run();
      } catch (inner) {
        console.warn("Could not insert pending_commands:", inner);
      }
    }
  }

  // Save into player vault history
  try {
    await env.DB
      .prepare(
        `INSERT INTO player_vault (user_id, source, case_slug, item_spec, item_name, amount, rarity, status)
         VALUES (?, 'case', ?, ?, ?, ?, ?, 'delivered')`
      )
      .bind(
        user.id,
        caseDef.slug,
        selected.item || "coins",
        selected.name || "Предмет",
        amount,
        caseDef.rarity || "common"
      )
      .run();
  } catch (err) {
    try {
      await env.DB.prepare(`
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
      `).run();
      await env.DB.prepare(`
        CREATE INDEX IF NOT EXISTS idx_vault_user ON player_vault(user_id, created_at DESC);
      `).run();
      await env.DB
        .prepare(
          `INSERT INTO player_vault (user_id, source, case_slug, item_spec, item_name, amount, rarity, status)
           VALUES (?, 'case', ?, ?, ?, ?, ?, 'delivered')`
        )
        .bind(
          user.id,
          caseDef.slug,
          selected.item || "coins",
          selected.name || "Предмет",
          amount,
          caseDef.rarity || "common"
        )
        .run();
    } catch (inner) {
      console.warn("Could not save to player_vault:", inner);
    }
  }

  return json({
    ok: true,
    case: {
      slug: caseDef.slug,
      title: caseDef.title,
    },
    loot: {
      name: selected.name,
      item: selected.item || "",
      amount,
      type: selected.type || "item",
      rarity: caseDef.rarity,
    },
    remainingCoins: finalCoins,
  });
}
