import { bad, json, readJson, purchasesDisabled } from "../_lib/http.js";
import { requireUser } from "../_lib/auth.js";
import { purchasesEnabled } from "../_lib/settings.js";
import { enqueueCommand } from "./internal/pending-commands.js";

const SLUG_TO_DELIVERY = {
  sailor: { kind: "lp_group", payload: "sailor" },
  skipper: { kind: "lp_group", payload: "skipper" },
  captain: { kind: "lp_group", payload: "captain" },
  admiral: { kind: "lp_group", payload: "admiral" },
  legend: { kind: "lp_group", payload: "legend" },
  vip: { kind: "lp_group", payload: "vip" },
  ocean: { kind: "coins", payload: "250" },
  fisher: { kind: "coins", payload: "800" },
  depth: { kind: "coins", payload: "2000" },
};

function hexHmac(buf) {
  return [...new Uint8Array(buf)].map((b) => b.toString(16).padStart(2, "0")).join("");
}

async function hmacHex(secret, raw) {
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
  const left = String(a || "");
  const right = String(b || "");
  const len = Math.max(left.length, right.length);
  let diff = left.length ^ right.length;
  for (let i = 0; i < len; i++) {
    diff |= (left.charCodeAt(i) || 0) ^ (right.charCodeAt(i) || 0);
  }
  return diff === 0;
}

export async function onRequestGet(context) {
  const enabled = await purchasesEnabled(context.env);
  return json({
    ok: true,
    purchases_enabled: enabled,
    gateway: context.env.YOOKASSA_SHOP_ID ? "yookassa" : "hmac",
    message: enabled
      ? "Оплата через ЮKassa, выдача на сервер когда зайдёте в игру."
      : "Покупки на сайте выключены. В F4 магазин — за монеты и кристаллы.",
  });
}

export async function onRequestPost(context) {
  const { request, env } = context;
  if (!(await purchasesEnabled(env))) {
    return purchasesDisabled();
  }
  const user = await requireUser(env.DB, request);
  if (!user) return bad("Войдите в аккаунт", 401);
  if (!env.YOOKASSA_SHOP_ID || !env.YOOKASSA_SECRET) {
    return bad("Платёжный шлюз не задан", 503);
  }
  const body = await readJson(request);
  const slug = String(body?.slug || "").trim().toLowerCase();
  const delivery = SLUG_TO_DELIVERY[slug];
  if (!delivery) return bad("Нет такого товара");

  const catalog = await env.DB.prepare(
    "SELECT title, price_rub FROM catalog_items WHERE slug = ? AND enabled = 1"
  )
    .bind(slug)
    .first();
  const title = catalog?.title || slug;
  const amount = Math.max(1, Number(catalog?.price_rub || 0));
  if (!amount) return bad("Цена не задана");

  const idempotence = crypto.randomUUID();
  const auth = btoa(`${env.YOOKASSA_SHOP_ID}:${env.YOOKASSA_SECRET}`);
  const created = await fetch("https://api.yookassa.ru/v3/payments", {
    method: "POST",
    headers: {
      Authorization: `Basic ${auth}`,
      "Idempotence-Key": idempotence,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      amount: { value: amount.toFixed(2), currency: "RUB" },
      capture: true,
      confirmation: {
        type: "redirect",
        return_url: "https://aquateche.store/store.html",
      },
      description: `AquaTech ${title} (${user.nick})`,
      metadata: { nick: user.nick, slug, kind: delivery.kind, payload: delivery.payload },
    }),
  });
  const payment = await created.json();
  if (!created.ok || !payment?.confirmation?.confirmation_url) {
    return bad(payment?.description || "ЮKassa не создала платёж", 502);
  }
  return json({
    ok: true,
    confirmation_url: payment.confirmation.confirmation_url,
    payment_id: payment.id,
  });
}

export async function onRequestCallback(context) {
  const { request, env } = context;
  if (!env.DB) return bad("База не подключена", 503);
  const raw = await request.text();
  let body;
  try {
    body = JSON.parse(raw || "{}");
  } catch {
    return bad("JSON");
  }

  const hmacSecret = env.PAYMENT_WEBHOOK_SECRET || "";
  const given = request.headers.get("x-aquatech-sign") || request.headers.get("X-AquaTech-Sign") || "";
  let trusted = false;
  if (hmacSecret && given) {
    const expect = await hmacHex(hmacSecret, raw);
    trusted = timingSafe(expect, given.toLowerCase());
  }

  if (!trusted && env.YOOKASSA_SHOP_ID && env.YOOKASSA_SECRET && body?.object?.id) {
    const auth = btoa(`${env.YOOKASSA_SHOP_ID}:${env.YOOKASSA_SECRET}`);
    const check = await fetch(`https://api.yookassa.ru/v3/payments/${body.object.id}`, {
      headers: { Authorization: `Basic ${auth}` },
    });
    const payment = await check.json();
    if (check.ok && payment.status === "succeeded") {
      body.object = payment;
      trusted = true;
    }
  }

  if (!trusted) return bad("Подпись отклонена", 403);

  const obj = body.object || body;
  const status = String(obj.status || body.event || "");
  if (status && status !== "succeeded" && !String(body.event || "").includes("succeeded")) {
    return json({ ok: true, ignored: status });
  }

  const meta = obj.metadata || {};
  const nick = String(meta.nick || body.nick || "").trim();
  const slug = String(meta.slug || body.slug || "").trim().toLowerCase();
  const mapped = SLUG_TO_DELIVERY[slug] || {
    kind: String(meta.kind || body.kind || "").trim(),
    payload: String(meta.payload || body.payload || "").trim(),
  };
  if (!nick || !mapped.kind) return bad("Нет nick/kind");

  const queued = await enqueueCommand(env.DB, {
    nick,
    kind: mapped.kind,
    payload: mapped.payload,
    provider: obj.id ? "yookassa" : "hmac",
    providerPaymentId: String(obj.id || body.provider_payment_id || ""),
  });
  if (!queued.ok) return bad(queued.error || "enqueue", 500);
  return json({ ok: true, queued: queued.id, duplicate: Boolean(queued.duplicate) });
}
