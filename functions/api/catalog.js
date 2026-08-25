import { bad, json } from "../_lib/http.js";
import { getSetting, purchasesEnabled } from "../_lib/settings.js";

/** Short player-facing copy until admin saves DB texts (catalog_from_db=1). */
const COPY = {
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

const DEFAULT_ITEMS = [
  {
    id: 1,
    kind: "store",
    slug: "sailor",
    title: "Моряк",
    price_rub: 99,
    description: "Стартовая морская привилегия. Префикс [МОРЯК], 2 точки дома (/sethome), доступ к базовым удобствам.",
    perks: ["Префикс [МОРЯК] в чате", "2 точки дома /sethome", "Цветной ник", "Базовый морской набор"],
  },
  {
    id: 2,
    kind: "store",
    slug: "skipper",
    title: "Шкипер",
    price_rub: 249,
    description: "Продвинутый мореплаватель. Префикс [ШКИПЕР], 3 точки дома, приоритетный вход на сервер.",
    perks: ["Префикс [ШКИПЕР] в чате", "3 точки дома /sethome", "Приоритетный вход на сервер", "Кит Шкипера в меню F4"],
  },
  {
    id: 3,
    kind: "store",
    slug: "captain",
    title: "Капитан",
    price_rub: 499,
    description: "Командир корабля. Префикс [КАПИТАН], 5 точек дома, режим полёта /fly на приватах.",
    perks: ["Префикс [КАПИТАН] в чате", "Режим полёта /fly", "5 точек дома /sethome", "Множитель удачи x2", "Кит Капитана"],
  },
  {
    id: 4,
    kind: "store",
    slug: "admiral",
    title: "Адмирал",
    price_rub: 899,
    description: "Верховный главнокомандующий флота. Префикс [АДМИРАЛ], 10 точек дома, /fly, /nick.",
    perks: ["Префикс [АДМИРАЛ] в чате", "Режим полёта /fly", "Смена ника /nick", "10 точек дома /sethome", "Множитель удачи x4", "Кит Адмирала"],
  },
  {
    id: 5,
    kind: "store",
    slug: "legend",
    title: "Легенда",
    price_rub: 1499,
    description: "Высший статус на сервере AquaTech. Префикс [ЛЕГЕНДА], неограниченные дома, /fly, /hat, /nick.",
    perks: ["Префикс [ЛЕГЕНДА] в чате", "Режим полёта /fly везде", "Блок на голове /hat", "Смена ника /nick", "15 точек дома /sethome", "Максимальный множитель x8", "Эксклюзивный кейс Легенды"],
  },
  {
    id: 6,
    kind: "store",
    slug: "vip",
    title: "VIP",
    price_rub: 199,
    description: "Классическая VIP-привилегия. Префикс [VIP], /fly, /wb, /ec, косметические эффекты.",
    perks: ["Префикс [VIP] в чате", "Виртуальный верстак /wb", "Эндер-сундук /ec", "Режим полёта /fly", "Косметика AquaLumen"],
  },
  {
    id: 7,
    kind: "case",
    slug: "ocean",
    title: "Океанский кейс",
    price_rub: 0,
    description: "Монеты и расходники. Открывается в игре (F4).",
    perks: ["AquaCoins", "Расходники", "Мелкий буст"],
  },
  {
    id: 8,
    kind: "case",
    slug: "fisher",
    title: "Кейс рыбака",
    price_rub: 0,
    description: "Лут под StarCatcher. Рулетки на сайте нет.",
    perks: ["Ресурсы улова", "Буст удочки", "Монеты"],
  },
  {
    id: 9,
    kind: "case",
    slug: "depth",
    title: "Глубинный кейс",
    price_rub: 0,
    description: "Редкая косметика и пробные привилегии. Только сервер.",
    perks: ["Рамка профиля", "Пробная привилегия", "Крупный запас монет"],
  },
];

export async function onRequestGet(context) {
  const { request, env } = context;
  if (!env.DB) return bad("База не подключена", 503);

  const url = new URL(request.url);
  const kind = url.searchParams.get("kind"); // store | case | null
  const fromDb = (await getSetting(env.DB, "catalog_from_db", "0")) === "1";

  // Auto-sync D1 catalog_items if sailor is missing
  try {
    const hasSailor = await env.DB.prepare("SELECT 1 FROM catalog_items WHERE slug = 'sailor'").first();
    if (!hasSailor) {
      await env.DB.prepare("DELETE FROM catalog_items WHERE kind = 'store'").run();
      for (const it of DEFAULT_ITEMS.filter((x) => x.kind === "store")) {
        await env.DB.prepare(
          "INSERT INTO catalog_items (kind, slug, title, price_rub, description, perks_json, enabled, sort_order) VALUES (?, ?, ?, ?, ?, ?, 1, ?)"
        )
          .bind(it.kind, it.slug, it.title, it.price_rub, it.description, JSON.stringify(it.perks), it.id * 10)
          .run();
      }
    }
  } catch {
    // Non-fatal if table not initialized
  }

  let sql = `SELECT id, kind, slug, title, price_rub, description, perks_json, enabled, sort_order
             FROM catalog_items WHERE enabled = 1`;
  const binds = [];
  if (kind === "store" || kind === "case") {
    sql += " AND kind = ?";
    binds.push(kind);
  }
  sql += " ORDER BY sort_order ASC, id ASC";

  let items = [];
  try {
    const stmt = env.DB.prepare(sql);
    const res = await (binds.length ? stmt.bind(...binds) : stmt).all();
    items = (res.results || []).map((row) => {
      let perks = [];
      try {
        perks = JSON.parse(row.perks_json || "[]");
      } catch {
        perks = [];
      }
      const override = fromDb ? null : COPY[row.slug];
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
  } catch {
    items = [];
  }

  if (!items.length) {
    items = DEFAULT_ITEMS.filter((it) => !kind || it.kind === kind);
  }

  return json({
    ok: true,
    purchases_enabled: await purchasesEnabled(env),
    items,
  });
}
