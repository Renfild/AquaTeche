
const json = (data, status = 200) => new Response(JSON.stringify(data), {
  status, headers: { "content-type": "application/json" }
});
const bad = (message, status = 400) => json({ error: message }, status);

/**
 * Telegram Bot Webhook — @aquatechebot.
 * Аккаунт привязывается через сайт: Профиль → Telegram → код → /start КОД.
 * После привязки команды работают по аккаунту игрока из tg_links.
 * Секрет: X-Telegram-Bot-Api-Secret-Token == env.TG_WEBHOOK_SECRET
 */
export async function onRequestPost(context) {
  const { request, env } = context;
  const secret = request.headers.get("X-Telegram-Bot-Api-Secret-Token") || "";
  if (!env.TG_WEBHOOK_SECRET || secret !== env.TG_WEBHOOK_SECRET) {
    return bad("forbidden", 403);
  }
  if (!env.DB) return json({ ok: true });
  const update = await request.json().catch(() => null);
  const msg = update?.message;
  if (!msg || !msg.text) return json({ ok: true });

  const chatId = String(msg.chat.id);
  const tgName = [msg.from?.first_name, msg.from?.last_name].filter(Boolean).join(" ") || msg.from?.username || "";
  const text = msg.text.trim();
  let reply = "";

  try {
    const boundNick = await linkedNick(env.DB, chatId);

    if (text.startsWith("/start")) {
      const code = text.slice(6).trim().toUpperCase();
      if (!code) {
        reply = boundNick
          ? `Привет, ${tgName}! Твой аккаунт уже привязан: ${boundNick}.\nБаланс — /balance, тренды — /trends, улов — /fish.`
          : `Привет, ${tgName}! Это бот сервера AquaTech.\n\nЧтобы привязать аккаунт:\n1. Зайди на aquateche.store → Профиль → Telegram\n2. Нажми «Получить код»\n3. Пришли сюда: /start КОД`;
      } else if (boundNick) {
        reply = `Аккаунт уже привязан (${boundNick}). Сначала отвяжи: /unlink`;
      } else {
        reply = await linkByCode(env.DB, chatId, tgName, code);
      }
    } else if (text.startsWith("/unlink") || text.startsWith("/отвязать")) {
      const res = await env.DB.prepare("DELETE FROM tg_links WHERE chat_id = ?").bind(chatId).run();
      reply = res.meta.changes ? "Аккаунт отвязан. Вернуть: Профиль → Telegram на сайте." : "Аккаунт и не был привязан.";
    } else if (text.startsWith("/balance") || text.startsWith("/баланс")) {
      if (!boundNick) {
        reply = needLink();
      } else {
        const p = await env.DB.prepare(
          `SELECT coins FROM profiles WHERE user_id = (SELECT id FROM users WHERE nick = ? COLLATE NOCASE)`
        ).bind(boundNick).first().catch(() => null);
        const rub = await env.DB.prepare(`SELECT balance FROM rub_balances WHERE nick = ? COLLATE NOCASE`)
          .bind(boundNick).first().catch(() => null);
        reply = `💰 ${boundNick}\nАкваМонеты: ${Number(p?.coins ?? 0).toLocaleString("ru-RU")} ¤\nВалютный баланс: ${Number(rub?.balance ?? 0).toLocaleString("ru-RU")} ₽`;
      }
    } else if (text.startsWith("/fish") || text.startsWith("/рыба")) {
      if (!boundNick) {
        reply = needLink();
      } else {
        const p = await env.DB.prepare(
          `SELECT fish FROM profiles WHERE user_id = (SELECT id FROM users WHERE nick = ? COLLATE NOCASE)`
        ).bind(boundNick).first().catch(() => null);
        reply = `🎣 ${boundNick}: ${Number(p?.fish ?? 0).toLocaleString("ru-RU")} рыб поймано`;
      }
    } else if (text.startsWith("/trends") || text.startsWith("/тренд")) {
      let trends = [];
      try {
        const row = await env.DB.prepare(`SELECT day, data FROM daily_trends ORDER BY day DESC LIMIT 1`).first();
        trends = row?.data ? JSON.parse(row.data) : [];
      } catch { /* fall through to empty */ }
      reply = trends.length
        ? "📈 Тренд дня:\n" + trends.map(t => `• ${t.name || t.id} ×${t.mult}`).join("\n")
        : "Тренды ещё не сгенерированы — зайди в игру.";
    } else if (text.startsWith("/pass") || text.startsWith("/пропуск")) {
      reply = "🎫 Уровень сезонного пропуска смотри в игре: F4 → Пропуск.\nИз бота пока: /balance, /fish, /trends.";
    } else if (text.startsWith("/help") || text.startsWith("/помощь")) {
      reply = "🤖 AquaTech Bot\n/balance — баланс (монеты + рубли)\n/fish — улов\n/trends — тренды дня\n/unlink — отвязать аккаунт\n\nПривязка: aquateche.store → Профиль → Telegram → /start КОД";
    } else {
      reply = boundNick
        ? "Не понял команду. /help — список команд."
        : "Не понял команду. Сначала привяжи аккаунт: /start КОД (код — на сайте, Профиль → Telegram).";
    }
  } catch (e) {
    reply = "Ошибка: " + (e.message || "unknown");
  }

  if (env.TG_BOT_TOKEN) {
    try {
      await fetch(`https://api.telegram.org/bot${env.TG_BOT_TOKEN}/sendMessage`, {
        method: "POST",
        headers: { "content-type": "application/json" },
        body: JSON.stringify({ chat_id: chatId, text: reply })
      });
    } catch (e) { /* fire-and-forget */ }
  }

  return json({ ok: true });
}

function needLink() {
  return `Аккаунт не привязан.\nОткрой aquateche.store → Профиль → Telegram → «Получить код», потом пришли /start КОД.`;
}

async function linkedNick(db, chatId) {
  try {
    const row = await db.prepare(
      `SELECT u.nick FROM tg_links l JOIN users u ON u.id = l.user_id WHERE l.chat_id = ?`
    ).bind(chatId).first();
    return row?.nick || null;
  } catch {
    return null;
  }
}

async function linkByCode(db, chatId, tgName, code) {
  if (!/^[A-Z0-9]{6,10}$/.test(code)) return "Код не похож на код привязки. Возьми новый на сайте: Профиль → Telegram.";
  const row = await db.prepare(
    `SELECT c.user_id, u.nick FROM tg_link_codes c JOIN users u ON u.id = c.user_id
     WHERE c.code = ? AND c.expires_at > datetime('now')`
  ).bind(code).first();
  if (!row) return "Код не найден или истёк (живёт 15 минут). Возьми новый: Профиль → Telegram → «Получить код».";
  await db.batch([
    db.prepare("DELETE FROM tg_link_codes WHERE user_id = ?").bind(row.user_id),
    db.prepare("DELETE FROM tg_links WHERE user_id = ?").bind(row.user_id),
    db.prepare(
      `INSERT INTO tg_links (user_id, chat_id, tg_name) VALUES (?, ?, ?)
       ON CONFLICT(user_id) DO UPDATE SET chat_id = excluded.chat_id, tg_name = excluded.tg_name,
         linked_at = strftime('%Y-%m-%dT%H:%M:%fZ','now')`
    ).bind(row.user_id, chatId, tgName),
  ]);
  return `✅ Готово! Аккаунт ${row.nick} привязан.\n/balance — баланс, /fish — улов, /trends — тренды.\nЕщё пришлю уведомление, когда купят твой лот на аукционе.`;
}
