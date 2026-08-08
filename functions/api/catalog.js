import { bad, json } from "../_lib/http.js";

/** Short player-facing copy; wins over stale D1 seed rows until migrations run. */
const COPY = {
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

export async function onRequestGet(context) {
  const { request, env } = context;
  if (!env.DB) return bad("База не подключена", 503);

  const url = new URL(request.url);
  const kind = url.searchParams.get("kind"); // store | case | null

  let sql = `SELECT id, kind, slug, title, price_rub, description, perks_json, enabled, sort_order
             FROM catalog_items WHERE enabled = 1`;
  const binds = [];
  if (kind === "store" || kind === "case") {
    sql += " AND kind = ?";
    binds.push(kind);
  }
  sql += " ORDER BY sort_order ASC, id ASC";

  const stmt = env.DB.prepare(sql);
  const res = await (binds.length ? stmt.bind(...binds) : stmt).all();
  const items = (res.results || []).map((row) => {
    let perks = [];
    try {
      perks = JSON.parse(row.perks_json || "[]");
    } catch {
      perks = [];
    }
    const override = COPY[row.slug];
    return {
      id: row.id,
      kind: row.kind,
      slug: row.slug,
      title: row.title,
      price_rub: row.price_rub,
      description: override?.description || row.description,
      perks: override?.perks || perks,
    };
  });

  const purchasesEnabled = String(env.PURCHASES_ENABLED || "false").toLowerCase() === "true";
  return json({ ok: true, purchases_enabled: purchasesEnabled, items });
}
