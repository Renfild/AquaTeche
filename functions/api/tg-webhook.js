
const json = (data, status = 200) => new Response(JSON.stringify(data), {
  status, headers: { "content-type": "application/json" }
});
const bad = (message, status = 400) => json({ error: message }, status);


/**
 * Telegram Bot Webhook — обрабатывает команды @aquatechebot.
 * /balance — баланс по нику (только для привязанного владельца)
 * /trends  — тренды дня
 * /help    — помощь
 * Секрет: X-Telegram-Bot-Api-Secret-Token == env.TG_WEBHOOK_SECRET
 */
export async function onRequestPost(context) {
  const { request, env } = context;
  const secret = request.headers.get("X-Telegram-Bot-Api-Secret-Token") || "";
  if (!env.TG_WEBHOOK_SECRET || secret !== env.TG_WEBHOOK_SECRET) {
    return bad("forbidden", 403);
  }
  const update = await request.json().catch(() => null);
  const msg = update?.message;
  if (!msg || !msg.text) return json({ ok: true });

  const chatId = msg.chat.id;
  const text = msg.text.trim();
  let reply = "";

  try {
    if (text.startsWith("/balance") || text.startsWith("/баланс")) {
      const nick = "Renfild"; // MVP: жёстко для владельца, позже — привязка аккаунта
      const row = await env.DB.prepare(
        `SELECT balance FROM users WHERE nick = ? COLLATE NOCASE`
      ).bind(nick).first();
      reply = row
        ? `💰 Баланс ${nick}: ${row.balance.toLocaleString("ru-RU")} ¤`
        : `Игрок ${nick} не найден в базе.`;
    } else if (text.startsWith("/trends") || text.startsWith("/тренд")) {
      const rows = await env.DB.prepare(
        `SELECT fish_id, mult FROM daily_trends WHERE day = date('now') ORDER BY mult DESC LIMIT 3`
      ).all();
      if (rows.results?.length) {
        reply = "📈 Тренд дня:\n" + rows.results.map(r => `• ${r.fish_id} ×${r.mult}`).join("\n");
      } else {
        reply = "Тренды ещё не сгенерированы — зайди в игру.";
      }
    } else if (text.startsWith("/pass") || text.startsWith("/пропуск")) {
      const row = await env.DB.prepare(
        `SELECT json_extract(data, '$.season.tier') AS tier FROM hub_snapshots WHERE nick = ? COLLATE NOCASE ORDER BY updated_at DESC LIMIT 1`
      ).bind("Renfild").first();
      reply = row?.tier
        ? `🎫 Сезонный пропуск: уровень ${row.tier}`
        : "Зайди в игру — уровень появится после первого входа.";
    } else if (text.startsWith("/help") || text.startsWith("/help")) {
      reply = "🤖 AquaTech Bot\n/balance — баланс\n/trends — тренды дня\n/pass — сезонный пропуск";
    } else {
      reply = "Не понял команду. /help — список команд.";
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
