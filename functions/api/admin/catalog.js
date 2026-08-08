import { bad, json, readJson } from "../../_lib/http.js";
import { requireAdmin } from "../../_lib/auth.js";
import { setSetting } from "../../_lib/settings.js";

const SHORT = {
  vip: {
    description: "Префикс, цветной ник, +1 дом. Купить на сайте пока нельзя.",
    perks: ["Префикс VIP в чате", "+1 дом /sethome", "Цветной ник", "Приоритет в очереди"],
  },
  premium: {
    description: "Всё из VIP, кейс в день на сервере, приоритет входа.",
    perks: ["Всё из VIP", "Кейс в день (в игре)", "Приоритет входа", "Доп. слот варпа"],
  },
  deluxe: {
    description: "Бонус к улову и рамка профиля. Оплата на сайте выключена.",
    perks: ["Всё из Premium", "Рамка профиля", "Бонус к улову", "Бейдж Deluxe"],
  },
  ultimate: {
    description: "Максимум привилегий на сервере. Оплата на сайте позже.",
    perks: ["Всё из Deluxe", "Бейдж Ultimate", "Максимум домов", "Приоритет в поддержке"],
  },
  ocean: {
    description: "Монеты и расходники. Открывается в игре (F4).",
    perks: ["AquaCoins", "Расходники", "Мелкий буст"],
  },
  fisher: {
    description: "Лут под StarCatcher. Рулетки на сайте нет.",
    perks: ["Ресурсы улова", "Буст удочки", "Монеты"],
  },
  depth: {
    description: "Редкая косметика и пробные привилегии. Только сервер.",
    perks: ["Рамка профиля", "Пробная привилегия", "Крупный запас монет"],
  },
};

function parsePerks(raw) {
  try {
    const v = JSON.parse(raw || "[]");
    return Array.isArray(v) ? v.map(String) : [];
  } catch {
    return [];
  }
}

function mapRow(row) {
  return {
    id: row.id,
    kind: row.kind,
    slug: row.slug,
    title: row.title,
    price_rub: row.price_rub,
    description: row.description,
    perks: parsePerks(row.perks_json),
    enabled: Number(row.enabled) === 1,
    sort_order: row.sort_order,
  };
}

export async function onRequestGet(context) {
  const { request, env } = context;
  if (!env.DB) return bad("База не подключена", 503);
  const admin = await requireAdmin(env.DB, request, env);
  if (!admin) return bad("Нет доступа", 403);

  const res = await env.DB.prepare(
    `SELECT id, kind, slug, title, price_rub, description, perks_json, enabled, sort_order
     FROM catalog_items ORDER BY kind ASC, sort_order ASC, id ASC`
  ).all();

  return json({ ok: true, items: (res.results || []).map(mapRow) });
}

/** Apply short default copy to all known slugs. */
export async function onRequestPost(context) {
  const { request, env } = context;
  if (!env.DB) return bad("База не подключена", 503);
  const admin = await requireAdmin(env.DB, request, env);
  if (!admin) return bad("Нет доступа", 403);
  const body = await readJson(request);
  if (body?.action !== "short_copy") return bad("Неизвестное действие");

  for (const [slug, copy] of Object.entries(SHORT)) {
    await env.DB.prepare(
      `UPDATE catalog_items SET description = ?, perks_json = ? WHERE slug = ?`
    )
      .bind(copy.description, JSON.stringify(copy.perks), slug)
      .run();
  }
  await setSetting(env.DB, "catalog_from_db", "1");
  return onRequestGet(context);
}
