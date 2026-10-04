"""Перебалансировка наград кейсов IV и V и отчёт по возврату (EV) всех кейсов.

    python tools/rebalance_cases.py            # применить правки и напечатать отчёт
    python tools/rebalance_cases.py --report   # только отчёт

Правила:
  1. Любая предметная награда в среднем стоит не меньше MIN_SHARE (15%) цены кейса.
  2. Гарант (pity) в среднем стоит не меньше цены кейса.
  3. Заглавная награда кейса V больше не «комплект прессов за 12 500», а крупные AE2-ячейки.

Цены предметов (монет за штуку). SHOP: взяты из config/aqualumen/server_shop.json (твёрдые).
EST: оценки по рецептам AE2 и Botania, их надо править по ощущениям рынка. Предметов без цены в отчёте помечено «?»,
их вклад в EV не учитывается.
"""
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
TARGETS = [ROOT / "config" / "aqualumen" / "cases.json", ROOT / "server" / "config" / "aqualumen" / "cases.json"]
SHOP = ROOT / "config" / "aqualumen" / "server_shop.json"

MIN_SHARE = 0.15
GEM_COINS = 5000  # курс продажи кристалла (gemSellCoins), консервативно

EST = {
    # AE2: стоимость цепочки по рецептам, процессор ≈ 2500 (ближе всего к схемам IU из магазина 4000-7500)
    "ae2:engineering_processor": 2500,
    "ae2:logic_processor": 2500,
    "ae2:calculation_processor": 2500,
    "ae2:fluix_crystal": 300,
    "ae2:drive": 4000,
    "ae2:molecular_assembler": 8000,
    "ae2:item_storage_cell_4k": 10000,
    "ae2:item_storage_cell_16k": 35000,
    "ae2:item_storage_cell_64k": 115000,
    "ae2:item_storage_cell_256k": 360000,
    # Botania
    "botania:terrasteel_ingot": 3000,
    "botania:elementium_ingot": 2000,
    "botania:rune_water": 1800,
    "botania:rune_fire": 1800,
    "botania:rune_mana": 1800,
}


def shop_prices():
    out = {}
    for entry in json.loads(SHOP.read_text(encoding="utf-8"))["items"]:
        ident, _, count = entry["item"].rpartition(":")
        if not count.isdigit():
            ident, count = entry["item"], "1"
        out[ident] = entry["price"] / int(count)
    return out


def item(item_id, label, low, high, weight):
    return {"type": "item", "item": item_id, "label": label, "min": low, "max": high, "weight": weight}


def set_loot(case, item_id, **fields):
    for entry in case["loot"]:
        if entry["item"] == item_id:
            entry.update(fields)
            return
    raise KeyError(item_id)


def rebalance(cases):
    by_id = {c["id"]: c for c in cases}

    flora = by_id["flora"]
    set_loot(flora, "botania:manasteel_ingot", min=56, max=64)
    set_loot(flora, "botania:mana_pearl", min=24, max=36)
    set_loot(flora, "botania:mana_diamond", min=24, max=36)
    for rune in ("botania:rune_water", "botania:rune_fire", "botania:rune_mana"):
        set_loot(flora, rune, min=12, max=16)
    flora["pity"] = item("industrialupgrade:crafting_elements/crafting_274_element", "Улучшенные микросхемы", 40, 44, 10)

    applied = by_id["applied"]
    pity = item("ae2:item_storage_cell_256k", "МЭ ячейка хранения 256k", 1, 2, 10)
    loot = applied["loot"]
    loot[0] = dict(pity)
    set_loot(applied, "botania:terrasteel_ingot", min=24, max=32)
    set_loot(applied, "ae2:drive", min=16, max=24)
    set_loot(applied, "ae2:engineering_processor", min=28, max=40)
    set_loot(applied, "ae2:logic_processor", min=28, max=40)
    set_loot(applied, "ae2:calculation_processor", min=28, max=40)
    set_loot(applied, "botania:elementium_ingot", min=32, max=48)
    for index, entry in enumerate(loot):
        if entry["item"] == "ae2:fluix_crystal":
            loot[index] = item("ae2:item_storage_cell_4k", "МЭ ячейка хранения 4k", 6, 10, entry["weight"])
    applied["pity"] = item("ae2:item_storage_cell_256k", "МЭ ячейка хранения 256k", 2, 2, 10)


def report(cases):
    price = {**shop_prices(), **EST}
    print(f"{'кейс':15}{'цена':>10}{'EV известный':>14}{'%':>6}  без цены")
    for case in cases:
        loot = case["loot"]
        total = sum(e["weight"] for e in loot)
        ev = 0.0
        unknown = []
        low_items = []
        for e in loot:
            share = e["weight"] / total
            avg = (e["min"] + e["max"]) / 2
            if e["type"] == "coins":
                ev += share * avg
            elif e["type"] == "gems":
                ev += share * avg * GEM_COINS
            else:
                unit = price.get(e["item"])
                if unit is None:
                    unknown.append(f"{e['label']} {share * 100:.0f}%")
                    continue
                value = avg * unit
                ev += share * value
                if value < MIN_SHARE * case["costCoins"]:
                    low_items.append(f"{e['label']}={value:,.0f}")
        pity = case.get("pity") or {}
        pity_unit = price.get(pity.get("item"))
        pity_value = (pity["min"] + pity["max"]) / 2 * pity_unit if pity_unit else None
        pity_note = "?" if pity_value is None else f"{pity_value:,.0f} ({pity_value / case['costCoins'] * 100:.0f}% цены)"
        print(f"{case['id']:15}{case['costCoins']:>10,}{ev:>14,.0f}{ev / case['costCoins'] * 100:>5.0f}%  "
              f"гарант {pity_note}; без цены: {', '.join(unknown) or '-'}")
        if low_items:
            print(f"{'':15}  ниже {MIN_SHARE:.0%} цены: {', '.join(low_items)}")


def main():
    only_report = "--report" in sys.argv
    with open(TARGETS[0], encoding="utf-8", newline="") as source:
        data = json.loads(source.read())
    if not only_report:
        rebalance(data["cases"])
        text = json.dumps(data, ensure_ascii=False, indent=2).replace("\n", "\r\n") + "\r\n"
        for target in TARGETS:
            with open(target, "w", encoding="utf-8", newline="") as out:
                out.write(text)
        print("записано:", ", ".join(str(t.relative_to(ROOT)) for t in TARGETS))
    report(data["cases"])


if __name__ == "__main__":
    main()
