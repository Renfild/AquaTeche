# Полная таблица лута удочек AquaTech

Источник истины: `mods/aquatech-ui/.../FishingLootHandler.java` → `rollStarCatcherRodLoot`  
Дата: **2026-08-11** (сверка с кодом)

## Как работает бросок

1. Ресурсные удочки StarCatcher и ванильная `minecraft:fishing_rod` (= `bamboo_rod`) **отменяют** ванильный/SC дроп и крутят пул ниже.
2. Каждый предмет в пуле сначала проходит свой **шанс** (`maybeAdd`).
3. Из прошедших случайно оставляют **1–3** стака (`pickFromPool`; у T1 bamboo — **1–2** к гарантированному).
4. Кол-во в стаке — диапазон в колонке «Кол-во».
5. Рейт-мод в удочке (×2…×64) умножает количество.
6. Бонусный treasure (навык / шторм Horizon / полная луна) — отдельно в конце.
7. **Fish-only** (`sky_rod`, `boner_rod`) — только рыба StarCatcher, пул AquaTech **не** крутится.

---

## T1 — `starcatcher:bamboo_rod` / `minecraft:fishing_rod`

**Всегда 1 гарантированный стартовый дроп** (взаимоисключающие сегменты):

| Шанс сегмента | Предмет | Кол-во |
|---------------|---------|--------|
| 16% | `minecraft:cobblestone` | 2–4 |
| 14% | `minecraft:dirt` | 2–4 |
| 12% | `minecraft:clay_ball` | 2–4 |
| 12% | `minecraft:bamboo` | 2–4 |
| 8% | `minecraft:oak_sapling` | 1–2 |
| 8% | `industrialupgrade:sapling/rubber_sapling` | 1 |
| 10% | `minecraft:gravel` | 2–3 |
| 10% | `minecraft:sand` | 2–3 |
| 10% | `minecraft:birch_sapling` | 1 |

**Плюс пул (шанс каждого → потом 1–2 стака):**

| Шанс | Предмет | Кол-во |
|------|---------|--------|
| 50% | `minecraft:cobblestone` | 1–3 |
| 45% | `minecraft:dirt` | 1–3 |
| 45% | `minecraft:clay_ball` | 1–3 |
| 45% | `minecraft:bamboo` | 1–3 |
| 35% | `minecraft:oak_sapling` | 1 |
| 25% | `minecraft:birch_sapling` | 1 |
| 35% | `industrialupgrade:sapling/rubber_sapling` | 1 |
| 30% | `industrialupgrade:raw_latex` | 1–2 |
| 25% | `industrialupgrade:blockresource/untreated_peat` | 1 |
| 40% | `minecraft:gravel` | 1–2 |
| 35% | `minecraft:sand` | 1–2 |
| 40% | `minecraft:copper_ore` | 1 |
| 30% | `industrialupgrade:classicore/tin` | 1 |

---

## T2 — `starcatcher:humble_rod`

| Шанс | Предмет | Кол-во |
|------|---------|--------|
| 45% | `minecraft:cobblestone` | 1–2 |
| 35% | `minecraft:clay_ball` | 1–2 |
| 65% | `minecraft:copper_ore` | 1–2 |
| 50% | `industrialupgrade:classicore/tin` | 1–2 |
| 45% | `minecraft:iron_ore` | 1–2 |
| 40% | `minecraft:coal_ore` | 1–2 |
| 60% | `industrialupgrade:baseore/titanium` | 1–2 |

Итог: **1–3** стака.

---

## T3 — `starcatcher:good_old_rod`

| Шанс | Предмет | Кол-во |
|------|---------|--------|
| 55% | `minecraft:iron_ore` | 1–2 |
| 50% | `minecraft:redstone_ore` | 1–2 |
| 50% | `industrialupgrade:baseore/spinel` | 1–2 |
| 45% | `minecraft:lapis_ore` | 1–2 |
| 45% | `industrialupgrade:classicore/tin` | 1–2 |
| 40% | `industrialupgrade:baseore2/strontium` | 1 |
| 40% | `industrialupgrade:baseore2/yttrium` | 1 |
| 35% | `industrialupgrade:baseore2/thallium` | 1 |

Итог: **1–3** стака.

---

## T4 — `starcatcher:naturalist_rod`

| Шанс | Предмет | Кол-во |
|------|---------|--------|
| 50% | `industrialupgrade:baseore2/barium` | 1–2 |
| 45% | `industrialupgrade:classicore/tin` | 1–2 |
| 40% | `minecraft:iron_ore` | 1–2 |

Итог: **1–3** стака.

---

## T5 — `starcatcher:slimed_rod`

| Шанс | Предмет | Кол-во |
|------|---------|--------|
| 55% | `industrialupgrade:baseore/spinel` | 1–2 |
| 50% | `industrialupgrade:baseore2/barium` | 1–2 |
| 45% | `industrialupgrade:baseore2/polonium` | 1–2 |
| 40% | `minecraft:iron_ore` | 1–2 |

Итог: **1–3** стака.

---

## T6 — `starcatcher:iceborn_rod`

| Шанс | Предмет | Кол-во |
|------|---------|--------|
| 50% | `industrialupgrade:baseore/aluminium` | 1–2 |
| 45% | `industrialupgrade:baseore/silver` | 1 |
| 45% | `industrialupgrade:baseore/zinc` | 1 |
| 40% | `minecraft:iron_ore` | 1–2 |

Итог: **1–3** стака.

---

## T7 — `starcatcher:starcatcher_rod`

| Шанс | Предмет | Кол-во |
|------|---------|--------|
| 50% | `minecraft:gold_ore` | 1–2 |
| 45% | `minecraft:lapis_lazuli` | 2–5 |
| 35% | `minecraft:lapis_ore` | 1–2 |
| 45% | `industrialupgrade:baseore/tungsten` | 1 |
| 45% | `industrialupgrade:baseore/chromium` | 1 |
| 40% | `industrialupgrade:preciousgem/sapphire_gem` | 1 |
| 40% | `industrialupgrade:preciousgem/topaz_gem` | 1 |

Итог: **1–3** стака.

---

## T8 — `starcatcher:azure_crystal_rod`

| Шанс | Предмет | Кол-во |
|------|---------|--------|
| 55% | `minecraft:lapis_lazuli` | 3–7 |
| 40% | `minecraft:lapis_ore` | 1–2 |
| 55% | `minecraft:amethyst_shard` | 2–4 |
| 50% | `industrialupgrade:preciousgem/sapphire_gem` | 1–2 |
| 50% | `industrialupgrade:preciousgem/topaz_gem` | 1–2 |
| 40% | `industrialupgrade:blockpreciousore/sapphire_ore` | 1 |
| 40% | `industrialupgrade:mineral/crystal` | 1 |

Итог: **1–3** стака.

---

## T9 — `starcatcher:sharktooth_rod`

| Шанс | Предмет | Кол-во |
|------|---------|--------|
| 50% | `industrialupgrade:baseore/titanium` | 1 |
| 45% | `industrialupgrade:baseore/cobalt` | 1 |
| 45% | `industrialupgrade:baseore/manganese` | 1 |
| 45% | `industrialupgrade:baseore/nickel` | 1 |

Итог: **1–3** стака.

---

## T10 — `starcatcher:obsidian_rod`

| Шанс | Предмет | Кол-во |
|------|---------|--------|
| 55% | `minecraft:diamond` | 1 |
| 55% | `minecraft:obsidian` | 1–2 |
| 45% | `industrialupgrade:baseore/titanium` | 1 |
| 40% | `industrialupgrade:alloyingot/stainless_steel` | 1 |

Итог: **1–3** стака.

---

## T11 — `starcatcher:lush_glowberry_rod`

| Шанс | Предмет | Кол-во |
|------|---------|--------|
| 55% | `minecraft:prismarine_shard` | 2–4 |
| 50% | `minecraft:prismarine_crystals` | 1–2 |
| 40% | `industrialupgrade:baseore/platinum` | 1 |
| 20% | `minecraft:heart_of_the_sea` | 1 |

Итог: **1–3** стака.

---

## T12 — `starcatcher:magmaforged_rod`

| Шанс | Предмет | Кол-во |
|------|---------|--------|
| 55% | `minecraft:quartz` | 2–4 |
| 50% | `industrialupgrade:crushed/uranium` | 1–2 |
| 45% | `minecraft:netherite_scrap` | 1 |
| 40% | `industrialupgrade:alloyingot/inconel` | 1 |

Итог: **1–3** стака.

---

## T13 — `starcatcher:alpha_rod`

| Шанс | Предмет | Кол-во |
|------|---------|--------|
| 50% | `industrialupgrade:baseore/iridium` | 1 |
| 50% | `industrialupgrade:baseore1/osmium` | 1 |
| 40% | `industrialupgrade:baseore2/polonium` | 1 |
| 40% | `industrialupgrade:alloyingot/osmiridium` | 1 |
| 30% | `industrialupgrade:asteroidore/asteroid_adamantium_ore` | 1 |
| 20% | `minecraft:nether_star` | 1 |

Итог: **1–3** стака.

---

## Fish-only (без ресурсного пула AquaTech)

| Удочка | Поведение |
|--------|-----------|
| `starcatcher:sky_rod` | Рыба StarCatcher (дефолт мода + мини-игра) |
| `starcatcher:boner_rod` | Рыба StarCatcher (дефолт мода + мини-игра) |

---

## Бонусный treasure (поверх основного улова)

`rareTreasure` — взвешенный бросок:

| Вес (кумулятивно) | Предмет | Кол-во |
|-------------------|---------|--------|
| 35% | `minecraft:prismarine_shard` | 1–2 |
| 20% | `minecraft:prismarine_crystals` | 1–2 |
| 15% | `minecraft:gold_ore` | 1 |
| 12% | `minecraft:emerald` | 1 |
| 10% | `minecraft:diamond` | 1 |
| 5% | `minecraft:nautilus_shell` | 1 |
| 3% | `minecraft:heart_of_the_sea` | 1 |

Когда может выпасть:
- навык rare loot / шторм Horizon;
- фаза луны 0 (полная) — 18%.

---

## Сводная матрица (удочка → фокус)

| Тир | Удочка | Фокус |
|-----|--------|--------|
| 1 | bamboo / fishing_rod | блоки, саженцы, старт IU |
| 2 | humble | медь, олово, титан |
| 3 | good_old | железо, spinel, редстоун, лазурит, Sr/Y/Tl |
| 4 | naturalist | barium |
| 5 | slimed | spinel, barium, polonium |
| 6 | iceborn | Al, Ag, Zn |
| 7 | starcatcher | золото, W, Cr, сапфир/топаз |
| 8 | azure_crystal | кристаллы / precious gems |
| 9 | sharktooth | Ti, Co, Mn, Ni |
| 10 | obsidian | алмаз, обсидиан, нерж. |
| 11 | lush_glowberry | призмарин, платина |
| 12 | magmaforged | кварц, уран, незерит-лом |
| 13 | alpha | Ir, Os, polonium, nether star |
