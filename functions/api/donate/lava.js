import { bad, json, readJson, purchasesDisabled } from "../../_lib/http.js";
import { requireUser } from "../../_lib/auth.js";
import { purchasesEnabled } from "../../_lib/settings.js";
import { enqueueCommand } from "../internal/pending-commands.js";

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
  ocean: { kind: "coins", payload: "250" },
  fisher: { kind: "coins", payload: "800" },
  depth: { kind: "coins", payload: "2000" },
};

function hexHmac(buf) {
  return [...new Uint8Array(buf)].map((b) => b.toString(16).padStart(2, "0")).join("");
}

async function hmacSha256(secret, raw) {
  const key = await crypto.subtle.importKey(
    "raw",
    new TextEncoder().encode(secret),
    { name: "HMAC", hash: "SHA-256" },
    false,
    ["sign"]
  );
  const sig = await crypto.subtle.sign("HMAC", key, new TextEncoder().encode(raw));
  return hexHmac(sig);
}

function timingSafe(a, b) {
  const left = String(a || "").toLowerCase();
  const right = String(b || "").toLowerCase();
  const len = Math.max(left.length, right.length);
  let diff = left.length ^ right.length;
  for (let i = 0; i < len; i++) {
    diff |= (left.charCodeAt(i) || 0) ^ (right.charCodeAt(i) || 0);
  }
  return diff === 0;
}

/**
 * GET /api/donate/lava: Check status and gateway availability
 */
export async function onRequestGet(context) {
  const { env } = context;
  const enabled = await purchasesEnabled(env);
  const hasSecret = Boolean(env.LAVA_API_KEY || env.LAVA_SECRET_KEY);
  const hasOffer = Boolean(env.LAVA_OFFER_ID);
  const configured = hasSecret && hasOffer;
  return json({
    ok: true,
    gateway: "lava.top",
    purchases_enabled: enabled,
    configured,
    has_secret: hasSecret,
    has_offer: hasOffer,
    has_project_id: hasOffer,
    currencies: ["RUB", "USD", "EUR"],
    methods: ["SBP", "Card MIR/Visa/Mastercard", "Crypto"]
  });
}

/**
 * POST /api/donate/lava:
 * - If action === "invoice": Creates payment link via Lava.top API
 * - If action === "webhook" or called by Lava: Processes signed payment notification
 */
export async function onRequestPost(context) {
  const { request, env } = context;
  const url = new URL(request.url);

  // Check if this is a webhook call from Lava.top
  const isWebhook = url.searchParams.get("action") === "webhook" || 
                    request.headers.get("Signature") || 
                    request.headers.get("X-Api-Signature");

  if (isWebhook) {
    return handleLavaWebhook(context);
  }

  // Otherwise, player creating an invoice
  if (!(await purchasesEnabled(env))) {
    return purchasesDisabled();
  }

  const user = await requireUser(env.DB, request);
  if (!user) return bad("Войди в аккаунт перед оплатой", 401);

  if (!env.LAVA_PROJECT_ID || !env.LAVA_SECRET_KEY) {
    return bad("Lava.top мерчант не настроен (LAVA_PROJECT_ID/LAVA_SECRET_KEY)", 503);
  }

  const body = await readJson(request);
  const slug = String(body?.slug || "").trim().toLowerCase();
  const delivery = SLUG_TO_DELIVERY[slug];
  if (!delivery) return bad("Товар не найден в каталоге");

  const catalog = await env.DB.prepare(
    "SELECT title, price_rub FROM catalog_items WHERE slug = ? AND enabled = 1"
  )
    .bind(slug)
    .first();

  const title = catalog?.title || slug;
  const amount = Math.max(1, Number(catalog?.price_rub || 0));
  if (!amount) return bad("Цена товара не задана");

  const orderId = `at_${Date.now()}_${user.nick.slice(0, 10)}`;
  const siteUrl = env.SITE_CANONICAL || "https://aquateche.store";

  const invoiceReq = {
    email: user.email || `${user.nick.toLowerCase()}@aquateche.store`,
    sum: amount,
    currency: "RUB",
    shopId: env.LAVA_PROJECT_ID,
    orderId: orderId,
    hookUrl: `${siteUrl}/api/donate/lava?action=webhook`,
    failUrl: `${siteUrl}/store.html?status=fail`,
    successUrl: `${siteUrl}/profile.html?status=success`,
    custom_fields: JSON.stringify({
      nick: user.nick,
      slug,
      kind: delivery.kind,
      payload: delivery.payload
    }),
    comment: `AquaTech: ${title} для ${user.nick}`
  };

  const reqJson = JSON.stringify(invoiceReq);
  const signature = await hmacSha256(env.LAVA_SECRET_KEY, reqJson);

  try {
    const apiRes = await fetch("https://api.lava.ru/business/invoice/create", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Accept": "application/json",
        "Signature": signature
      },
      body: reqJson
    });

    const resData = await apiRes.json().catch(() => ({}));
    if (!apiRes.ok || (!resData?.data?.url && !resData?.url)) {
      return bad(resData?.message || resData?.error || "Ошибка создания счёта в Lava.top", 502);
    }

    const payUrl = resData?.data?.url || resData?.url;
    return json({
      ok: true,
      url: payUrl,
      order_id: orderId,
      amount
    });
  } catch (err) {
    return bad("Не удалось соединиться с сервисом Lava.top: " + (err.message || ""), 502);
  }
}

/**
 * Validates and fulfills payment from Lava.top webhook
 */
async function handleLavaWebhook(context) {
  const { request, env } = context;
  if (!env.DB) return bad("База данных D1 не подключена", 503);

  const rawBody = await request.text();
  let data;
  try {
    data = JSON.parse(rawBody || "{}");
  } catch {
    return bad("Некорректный JSON в вебхуке", 400);
  }

  // Validate signature
  const secret = env.LAVA_SECRET_KEY || env.LAVA_API_KEY || "";
  if (!secret) {
    return bad("Сервис оплаты не настроен на сервере", 503);
  }

  const givenSig = request.headers.get("Signature") || 
                   request.headers.get("X-Api-Signature") || 
                   data.signature || "";

  if (!givenSig) {
    return bad("Отсутствует цифровая подпись вебхука", 401);
  }

  const expectedSig = await hmacSha256(secret, rawBody);
  if (!timingSafe(expectedSig, givenSig)) {
    return bad("Неверная подпись вебхука Lava.top", 403);
  }

  // Status check: Lava uses "success" or "PAID"
  const status = String(data.status || "").toLowerCase();
  if (status !== "success" && status !== "paid") {
    return json({ ok: true, ignored: status });
  }

  let custom = {};
  try {
    if (typeof data.custom_fields === "string") custom = JSON.parse(data.custom_fields);
    else if (data.custom_fields) custom = data.custom_fields;
  } catch {
    custom = {};
  }

  const nick = String(custom.nick || data.nick || "").trim();
  const slug = String(custom.slug || "").trim().toLowerCase();
  const delivery = SLUG_TO_DELIVERY[slug] || {
    kind: custom.kind || "coins",
    payload: custom.payload || String(Math.floor(Number(data.amount || data.sum || 0) * 10))
  };

  if (!nick || !delivery.kind) {
    return bad("Отсутствуют обязательные метаданные игрока (nick/kind)");
  }

  const paymentId = String(data.order_id || data.orderId || data.id || "");

  // Idempotently enqueue in-game delivery for AquaLumen PendingDeliveryService
  const queued = await enqueueCommand(env.DB, {
    nick,
    kind: delivery.kind,
    payload: delivery.payload,
    provider: "lava",
    providerPaymentId: paymentId
  });

  if (!queued.ok) {
    return bad(queued.error || "Ошибка очереди доставки", 500);
  }

  return json({
    ok: true,
    queued: queued.id,
    duplicate: Boolean(queued.duplicate)
  });
}
