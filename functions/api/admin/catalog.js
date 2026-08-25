import { bad, json, readJson } from "../../_lib/http.js";
import { requireAdmin } from "../../_lib/auth.js";
import { setSetting } from "../../_lib/settings.js";

const SHORT = {
  sailor: {
    description: "Стартовая морская привилегия. Префикс [МОРЯК], 2 точки дома (/sethome), доступ к базовым удобствам.",
    perks: ["Префикс [МОРЯК] в чате", "2 точки дома /sethome", "Цветной ник", "Базовый морской набор"],
  },
  skipper: {
    description: "Продвинутый мореплаватель. Префикс [ШКИПЕР], 3 точки дома, приоритетный вход на сервер.",
    perks: ["Префикс [ШКИПЕР] в чате", "3 точки дома /sethome", "Приоритетный вход на сервер", "Кит Шкипера в меню F4"],
  },
  captain: {
    description: "Командир корабля. Префикс [КАПИТАН], 5 точек дома, режим полёта /fly на приватах.",
    perks: ["Префикс [КАПИТАН] в чате", "Режим полёта /fly", "5 точек дома /sethome", "Множитель удачи x2", "Кит Капитана"],
  },
  admiral: {
    description: "Верховный главнокомандующий флота. Префикс [АДМИРАЛ], 10 точек дома, /fly, /nick.",
    perks: ["Префикс [АДМИРАЛ] в чате", "Режим полёта /fly", "Смена ника /nick", "10 точек дома /sethome", "Множитель удачи x4", "Кит Адмирала"],
  },
  legend: {
    description: "Высший статус на сервере AquaTech. Префикс [ЛЕГЕНДА], неограниченные дома, /fly, /hat, /nick.",
    perks: ["Префикс [ЛЕГЕНДА] в чате", "Режим полёта /fly везде", "Блок на голове /hat", "Смена ника /nick", "15 точек дома /sethome", "Максимальный множитель x8", "Эксклюзивный кейс Легенды"],
  },
  vip: {
    description: "Классическая VIP-привилегия. Префикс [VIP], /fly, /wb, /ec, косметические эффекты.",
    perks: ["Префикс [VIP] в чате", "Виртуальный верстак /wb", "Эндер-сундук /ec", "Режим полёта /fly", "Косметика AquaLumen"],
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
