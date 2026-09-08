import { bad, json, readJson, purchasesDisabled } from "../_lib/http.js";
import { requireUser } from "../_lib/auth.js";
import { purchasesEnabled } from "../_lib/settings.js";
import { enqueueCommand } from "./internal/pending-commands.js";

const SLUG_TO_DELIVERY = {
  sailor: { kind: "lp_group", payload: "sailor" },
  sailor_forever: { kind: "lp_group", payload: "sailor" },
  skipper: { kind: "lp_group", payload: "skipper" },
  skipper_forever: { kind: "lp_group", payload: "skipper" },
  captain: { kind: "lp_group", payload: "captain" },
  captain_forever: { kind: "lp_group", payload: "captain" },
  admiral: { kind: "lp_group", payload: "admiral" },
  admiral_forever: { kind: "lp_group", payload: "admiral" },
  legend: { kind: "lp_group", payload: "legend" },
  legend_forever: { kind: "lp_group", payload: "legend" },
  vip: { kind: "lp_group", payload: "vip" },
  vip_forever: { kind: "lp_group", payload: "vip" },
  coins_10k: { kind: "coins", payload: "10000" },
  coins_30k: { kind: "coins", payload: "30000" },
  coins_75k: { kind: "coins", payload: "75000" },
  coins_200k: { kind: "coins", payload: "200000" },
};

const RANK_PRICES = {
  sailor: { title: "Моряк (1 месяц)", price_rub: 99 },
  sailor_forever: { title: "Моряк (Навсегда)", price_rub: 299 },
  skipper: { title: "Шкипер (1 месяц)", price_rub: 249 },
  skipper_forever: { title: "Шкипер (Навсегда)", price_rub: 699 },
  captain: { title: "Капитан (1 месяц)", price_rub: 499 },
  captain_forever: { title: "Капитан (Навсегда)", price_rub: 1299 },
  admiral: { title: "Адмирал (1 месяц)", price_rub: 899 },
  admiral_forever: { title: "Адмирал (Навсегда)", price_rub: 2199 },
  legend: { title: "Легенда (1 месяц)", price_rub: 1499 },
  legend_forever: { title: "Легенда (Навсегда)", price_rub: 3499 },
  vip: { title: "VIP (1 месяц)", price_rub: 199 },
  vip_forever: { title: "VIP (Навсегда)", price_rub: 499 },
};

const COIN_PACKS = {
  coins_10k: { title: "10 000 АкваМонет", coins: 10000, price_rub: 99 },
  coins_30k: { title: "30 000 АкваМонет", coins: 30000, price_rub: 249 },
  coins_75k: { title: "75 000 АкваМонет", coins: 75000, price_rub: 499 },
  coins_200k: { title: "200 000 АкваМонет", coins: 200000, price_rub: 999 },
};

function timingSafe(a, b) {
  const left = String(a || "");
  const right = String(b || "");
  const len = Math.max(left.length, right.length);
  let diff = left.length ^ right.length;
  for (let i = 0; i < len; i++) {
    diff |= (left.charCodeAt(i) || 0) ^ (right.charCodeAt(i) || 0);
  }
  return diff === 0;
}

function lavaApiKey(env) {
  return String(env.LAVA_API_KEY || env.LAVA_SECRET_KEY || "").trim();
}

function lavaOfferId(env) {
  return String(env.LAVA_OFFER_ID || "").trim();
}

function lavaConfigured(env) {
  return Boolean(lavaApiKey(env) && lavaOfferId(env));
}

function buyerEmail(user) {
  const nick = String(user?.nick || "player").toLowerCase().replace(/[^a-z0-9._-]/g, "");
  return (nick || "player") + "@aquateche.store";
}

async function ensureRubTable(env) {
  await env.DB.prepare(
    "CREATE TABLE IF NOT EXISTS rub_balances (nick TEXT PRIMARY KEY, balance INTEGER NOT NULL DEFAULT 0, updated_at TEXT DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')))"
  ).run();
}

async function ensureInvoiceTable(env) {
  await env.DB.prepare(
    `CREATE TABLE IF NOT EXISTS lava_invoices (
      id TEXT PRIMARY KEY,
      nick TEXT NOT NULL,
      amount INTEGER NOT NULL,
      kind TEXT NOT NULL,
      payload TEXT NOT NULL DEFAULT '',
      status TEXT NOT NULL DEFAULT 'pending',
      created_at TEXT DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now'))
    )`
  ).run();
}

async function getRubBalance(env, nick) {
  await ensureRubTable(env);
  const row = await env.DB.prepare("SELECT balance FROM rub_balances WHERE nick = ?").bind(nick).first();
  return Number(row?.balance || 0);
}

async function addRub(env, nick, amount) {
  await ensureRubTable(env);
  await env.DB.prepare(
    "INSERT INTO rub_balances (nick, balance, updated_at) VALUES (?, ?, strftime('%Y-%m-%dT%H:%M:%fZ','now')) ON CONFLICT(nick) DO UPDATE SET balance = balance + ?, updated_at = strftime('%Y-%m-%dT%H:%M:%fZ','now')"
  )
    .bind(nick, amount, amount)
    .run();
}

async function deductRub(env, nick, amount) {
  await ensureRubTable(env);
  const res = await env.DB.prepare(
    "UPDATE rub_balances SET balance = balance - ?, updated_at = strftime('%Y-%m-%dT%H:%M:%fZ','now') WHERE nick = ? AND balance >= ?"
  )
    .bind(amount, nick, amount)
    .run();
  return Number(res?.meta?.changes || 0) > 0;
}

function rubToUsd(rub) {
  const n = Math.max(1, Number(rub) || 1);
  return Math.max(5, Math.round((n / 85) * 100) / 100);
}

const VISA_MIN_RUB = 450;

function lavaPayRoute(method) {
  const m = String(method || "sbp").toLowerCase();
  if (m === "visa" || m === "card_intl" || m === "unlimint" || m === "mastercard") {
    return { currency: "USD", paymentProvider: "UNLIMINT", paymentMethod: "CARD" };
  }
  if (m === "card" || m === "mir" || m === "smart_glocal") {
    return { currency: "RUB", paymentProvider: "SMART_GLOCAL", paymentMethod: "CARD" };
  }
  return { currency: "RUB", paymentProvider: "PAY2ME", paymentMethod: "SBP" };
}

function decodeLavaParams(raw) {
  const clean = decodeURIComponent(String(raw || ""))
    .replace(/-/g, "+")
    .replace(/_/g, "/")
    .replace(/\s+/g, "");
  const pad = "=".repeat((4 - (clean.length % 4)) % 4);
  return JSON.parse(atob(clean + pad));
}

function findPayRedirect(node, depth) {
  if (depth > 10 || node == null) return "";
  if (typeof node === "string") {
    if (/^https:\/\//i.test(node) && /pay2me\.com|cardpay\.com|unlimint|payment\.html/i.test(node)) {
      return node;
    }
    return "";
  }
  if (Array.isArray(node)) {
    for (const item of node) {
      const hit = findPayRedirect(item, depth + 1);
      if (hit) return hit;
    }
    return "";
  }
  if (typeof node === "object") {
    const direct = node.redirect_url || node.redirectUrl || node.payment_url || node.paymentUrl;
    if (typeof direct === "string" && /^https:\/\//i.test(direct)) return direct;
    for (const value of Object.values(node)) {
      const hit = findPayRedirect(value, depth + 1);
      if (hit) return hit;
    }
  }
  return "";
}

function lavaDirectPayUrl(lavaUrl) {
  const fallback = String(lavaUrl || "");
  try {
    const u = new URL(fallback);
    const raw = u.searchParams.get("paymentParams");
    if (!raw) return fallback;
    return findPayRedirect(decodeLavaParams(raw), 0) || fallback;
  } catch {
    return fallback;
  }
}

function payHost(url) {
  try {
    return new URL(url).host;
  } catch {
    return "";
  }
}

async function lavaCreateInvoice(env, { email, amount, nick, kind, payload, successPath, failPath, payMethod }) {
  const siteUrl = env.SITE_CANONICAL || "https://aquateche.store";
  const route = lavaPayRoute(payMethod);
  if (route.currency === "USD" && Number(amount) < VISA_MIN_RUB) {
    throw new Error("Visa / Mastercard от " + VISA_MIN_RUB + " ₽. Для меньшей суммы — СБП или МИР.");
  }
  const chargeAmount = route.currency === "USD" ? rubToUsd(amount) : amount;
  const res = await fetch("https://gate.lava.top/api/v3/invoice", {
    method: "POST",
    headers: {
      "X-Api-Key": lavaApiKey(env),
      "Content-Type": "application/json",
      Accept: "application/json",
    },
    body: JSON.stringify({
      email,
      offerId: lavaOfferId(env),
      currency: route.currency,
      amount: chargeAmount,
      buyerLanguage: "RU",
      paymentProvider: route.paymentProvider,
      paymentMethod: route.paymentMethod,
      clientUtm: {
        utm_source: "aquateche.store",
        utm_medium: kind,
        utm_campaign: nick,
        utm_content: String(payload || amount),
      },
      successful_return_url: siteUrl + successPath,
      failure_return_url: siteUrl + failPath,
      cancel_return_url: siteUrl + failPath,
    }),
  });
  const data = await res.json().catch(() => ({}));
  const rawUrl = String(data?.paymentUrl || data?.payment_url || data?.data?.paymentUrl || "");
  const payUrl = lavaDirectPayUrl(rawUrl);
  if (!res.ok || !payUrl) {
    throw new Error(data?.error || data?.message || "Lava.top HTTP " + res.status);
  }
  const host = payHost(payUrl);
  const needsDirect = route.paymentMethod === "SBP" || route.currency === "USD";
  if (needsDirect && /lava\.top$/i.test(host)) {
    throw new Error("Шлюз вернул карточную страницу Lava вместо " + (route.paymentMethod === "SBP" ? "СБП" : "Visa") + ". Попробуй ещё раз.");
  }
  const invoiceId = String(data.id || data.invoiceId || "");
  if (invoiceId) {
    await ensureInvoiceTable(env);
    await env.DB.prepare(
      "INSERT INTO lava_invoices (id, nick, amount, kind, payload, status) VALUES (?, ?, ?, ?, ?, 'pending')"
    )
      .bind(invoiceId, nick, amount, kind, String(payload || ""))
      .run();
  }
  return { url: payUrl, id: invoiceId, rail: route.paymentMethod, host };
}

export async function onRequestGet(context) {
  const enabled = await purchasesEnabled(context.env);
  const configured = lavaConfigured(context.env);
  return json({
    ok: true,
    purchases_enabled: enabled,
    configured,
    currency: "RUB",
    methods: ["balance", "lava", "sbp", "card", "visa"],
    visa_min_rub: VISA_MIN_RUB,
    gateway: configured ? "lava.top" : "none",
    message: !enabled
      ? "Покупки временно отключены."
      : configured
        ? "Пополни баланс и покупай привилегии с него."
        : "Платёжный шлюз ещё настраивается.",
  });
}

export async function onRequestPost(context) {
  const ctx = context;
  if (!ctx.env.DB) return bad("База данных не подключена", 503);
  if (!(await purchasesEnabled(ctx.env))) return purchasesDisabled();

  const body = await readJson(ctx.request);
  const action = String(body?.action || "buy").toLowerCase();

  if (action === "topup") {
    const user = await requireUser(ctx.env.DB, ctx.request);
    if (!user) return bad("Войди в аккаунт, чтобы пополнить баланс", 401);
    const targetNick = user.nick;
    const amountRub = Math.max(10, Math.min(50000, Math.floor(Number(body?.amount || 100))));
    if (!lavaConfigured(ctx.env)) {
      return bad("Платёжный шлюз не настроен. Напиши в Discord — зачислим вручную.", 503);
    }
    try {
      const inv = await lavaCreateInvoice(ctx.env, {
        email: buyerEmail(user),
        amount: amountRub,
        nick: targetNick,
        kind: "rub_topup",
        payload: String(amountRub),
        payMethod: body?.method || "sbp",
        successPath: "/profile.html?status=success",
        failPath: "/store.html?status=fail",
      });
      return json({ ok: true, type: "redirect", url: inv.url, orderId: inv.id, amount: amountRub, rail: inv.rail, host: inv.host });
    } catch (err) {
      return bad("Не удалось создать счёт: " + err.message, 502);
    }
  }

  const user = await requireUser(ctx.env.DB, ctx.request);
  if (!user) return bad("Войди в аккаунт, чтобы купить привилегию", 401);
  const nick = user.nick;

  let slug = String(body?.slug || "").trim().toLowerCase();
  const period = String(body?.period || "").trim().toLowerCase();
  if ((period === "forever" || period === "навсегда") && !slug.endsWith("_forever") && RANK_PRICES[`${slug}_forever`]) {
    slug = `${slug}_forever`;
  }
  const delivery = SLUG_TO_DELIVERY[slug];
  if (!delivery) return bad("Товар не найден в каталоге");

  const catalog = await ctx.env.DB.prepare("SELECT title, price_rub FROM catalog_items WHERE slug = ? AND enabled = 1")
    .bind(slug)
    .first();
  const coinPack = COIN_PACKS[slug];
  const rankPack = RANK_PRICES[slug];
  const title = rankPack?.title || coinPack?.title || catalog?.title || slug;
  const priceRub = rankPack ? rankPack.price_rub : (coinPack ? coinPack.price_rub : Math.max(1, Number(catalog?.price_rub || 99)));
  const method = String(body?.method || "balance").toLowerCase();

  if (method === "balance") {
    const paid = await deductRub(ctx.env, nick, priceRub);
    if (!paid) {
      const rub = await getRubBalance(ctx.env, nick);
      return bad("Недостаточно средств. Нужно " + priceRub + " ₽, на балансе " + rub + " ₽. Пополните баланс.", 400);
    }
    const paymentId = "bal_" + Date.now() + "_" + nick + "_" + slug;
    await enqueueCommand(ctx.env.DB, {
      nick,
      kind: delivery.kind,
      payload: delivery.payload,
      provider: "balance",
      providerPaymentId: paymentId,
    });
    if (delivery.kind === "coins") {
      const addCoins = parseInt(delivery.payload, 10) || 0;
      await ctx.env.DB.prepare(
        "UPDATE profiles SET coins = coins + ? WHERE user_id = (SELECT id FROM users WHERE lower(nick) = lower(?))"
      ).bind(addCoins, nick).run();
    }
    const left = await getRubBalance(ctx.env, nick);
    const prof = await ctx.env.DB.prepare(
      "SELECT coins FROM profiles WHERE user_id = (SELECT id FROM users WHERE lower(nick) = lower(?))"
    ).bind(nick).first();
    return json({
      ok: true,
      method: "balance",
      title,
      slug,
      spent_rub: priceRub,
      balance_rub: left,
      coins: prof?.coins ?? 0,
      message: delivery.kind === "coins"
        ? `Куплено: «${title}». Монеты моментально зачислены на ваш баланс!`
        : `Куплено: «${title}». Привилегия выдастся на сервере в течение минуты!`,
    });
  }

  if (method === "lava" || method === "sbp" || method === "card" || method === "visa" || method === "card_intl") {
    if (!lavaConfigured(ctx.env)) return bad("Оплата картой / СБП не настроена. Пополни баланс через «+».", 503);
    try {
      const intl = method === "visa" || method === "card_intl";
      const inv2 = await lavaCreateInvoice(ctx.env, {
        email: buyerEmail(user),
        amount: priceRub,
        nick,
        kind: delivery.kind,
        payload: delivery.payload,
        payMethod: intl ? "visa" : method === "card" ? "card" : "sbp",
        successPath: "/store.html?status=success&bought=" + encodeURIComponent(slug),
        failPath: "/store.html?status=fail",
      });
      return json({ ok: true, type: "redirect", confirmation_url: inv2.url, url: inv2.url, order_id: inv2.id, amount: priceRub, rail: inv2.rail, host: inv2.host });
    } catch (err) {
      return bad("Не удалось создать счёт: " + err.message, 502);
    }
  }

  return bad("Неизвестный способ оплаты: " + method);
}

export async function onRequestCallback(context) {
  const ctx = context;
  if (!ctx.env.DB) return bad("База не подключена", 503);
  const raw = await ctx.request.text();
  let body;
  try {
    body = JSON.parse(raw || "{}");
  } catch {
    return bad("JSON");
  }

  const webhookSecret = String(ctx.env.PAYMENT_WEBHOOK_SECRET || "").trim();
  const given = ctx.request.headers.get("X-Api-Key") || ctx.request.headers.get("x-api-key") || "";
  if (webhookSecret) {
    if (!given || !timingSafe(webhookSecret, given)) {
      return bad("Подпись отклонена", 403);
    }
  }

  const eventType = String(body.eventType || body.event_type || "").toLowerCase();
  const status = String(body.status || "").toLowerCase();
  const success =
    eventType === "payment.success" ||
    status === "completed" ||
    status === "success" ||
    status === "paid" ||
    status === "succeeded";
  if (!success) {
    return json({ ok: true, ignored: eventType || status || "unknown" });
  }

  const contractId = String(body.contractId || body.contract_id || body.id || "").trim();
  const paidCurrency = String(body.currency || body.amountTotal?.currency || body.receipt?.currency || "RUB").toUpperCase();
  const paidAmount = Math.floor(Number(body.amount || body.amountTotal?.amount || body.receipt?.amount || 0));
  await ensureInvoiceTable(ctx.env);
  let row = null;
  if (contractId) {
    row = await ctx.env.DB.prepare("SELECT id, nick, amount, kind, payload, status FROM lava_invoices WHERE id = ?")
      .bind(contractId)
      .first();
  }
  if (!row) {
    return json({ ok: true, ignored: "no_invoice", contractId });
  }
  if (row.status === "paid") {
    return json({ ok: true, duplicate: true, id: row.id });
  }

  const nick = String(row.nick || "").trim();
  const kind = String(row.kind || "").trim();
  const payload = String(row.payload || "").trim();
  if (!nick || !kind) return bad("Нет nick/kind");

  const amount = Math.max(0, Math.floor(Number(row.amount || paidAmount || 0)));
  if (paidCurrency === "RUB" && paidAmount > 0 && amount > 0 && paidAmount + 1 < amount) {
    return bad("Сумма вебхука меньше счёта", 400);
  }

  await ctx.env.DB.prepare("UPDATE lava_invoices SET status = 'paid' WHERE id = ? AND status = 'pending'")
    .bind(row.id)
    .run();

  if (kind === "rub_topup") {
    if (amount > 0) await addRub(ctx.env, nick, amount);
    return json({ ok: true, action: "topup", nick, amount });
  }

  const queued = await enqueueCommand(ctx.env.DB, {
    nick,
    kind,
    payload,
    provider: "lava_webhook",
    providerPaymentId: contractId,
  });
  if (!queued.ok) return bad(queued.error || "enqueue", 500);
  return json({ ok: true, queued: queued.id, duplicate: Boolean(queued.duplicate) });
}
