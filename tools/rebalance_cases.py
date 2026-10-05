"""Перебалансировка кейсов I-X: цены по гладкой шкале и награды под целевой возврат (EV).

    python tools/rebalance_cases.py            # посчитать с tools/cases_base.json, записать cases.json (+ server/), напечатать отчёт
    python tools/rebalance_cases.py --report   # только отчёт по текущему файлу, ничего не пишет

Правила:
  1. Цена кейса берётся из PRICES (шаг цен ~x2.2-2.9, без скачков).
  2. Средний возврат (EV) растёт с ценой: TARGET_EV, от 50% у дешёвого кейса до 80% у дорогого.
  3. Любая предметная награда в среднем стоит не меньше MIN_SHARE (15%) цены кейса.
  4. Гарант (pity) в среднем стоит не меньше цены кейса.
  5. Веса и состав предметов не трогаем. Меняем только количество предметов, монеты и гемы.

Ценность предмета берётся из VALUE (монет за штуку). Часть значений взята из config/aqualumen/server_shop.json,
остальное оценки по рецептам и редкости: их надо править по ощущениям рынка и запускать скрипт заново.
Классы награды по шансу (доля веса): <1% заглавная (1 шт.), 1-3% крупная, 3-6.5% редкая, 6.5-9.5% средняя, остальное расходник.
Гемы в кейсах не трогаем: это премиум-валюта (Боевой Пропуск стоит 100 кристаллов), поэтому правило 15% на них не действует.
Монеты закрывают остаток EV, в среднем около COIN_AVG цены кейса.
"""
import json
import math
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
TARGETS = [ROOT / "config" / "aqualumen" / "cases.json", ROOT / "server" / "config" / "aqualumen" / "cases.json"]
BASE = ROOT / "tools" / "cases_base.json"  # исходные кейсы без добавок: награды считаются всегда с нуля

MIN_SHARE = 0.15
PITY_SHARE = 1.1
GEM_COINS = 5000  # курс продажи кристалла (gemSellCoins), консервативно
MAX_QTY = 64  # один стак
COIN_AVG = 3.0  # средняя денежная награда в долях цены кейса

PRICES = {
    "starter": 2_500,
    "smeltery": 7_000,
    "steam": 20_000,
    "flora": 55_000,
    "applied": 150_000,
    "abyss": 400_000,
    "superconductor": 1_000_000,
    "singularity": 2_500_000,
    "draconic": 5_500_000,
    "infinity": 12_000_000,
}
TARGET_EV = dict(zip(PRICES, (0.50, 0.53, 0.57, 0.60, 0.63, 0.67, 0.70, 0.73, 0.77, 0.80)))

VALUE = {
    # I
    "minecraft:diamond": 400, "aquatech_machines:sea_salt": 150, "minecraft:iron_ingot": 40,
    "minecraft:copper_ingot": 20, "industrialupgrade:classicore/tin": 30, "industrialupgrade:classicore/lead": 30,
    "industrialupgrade:crafting_elements/crafting_773_element": 20, "industrialupgrade:crafting_elements/crafting_772_element": 20,
    "minecraft:leather": 15, "minecraft:coal": 20, "minecraft:redstone": 25,
    # II
    "industrialupgrade:smeltery/smeltery_controller": 8_000, "industrialupgrade:smeltery/smeltery_casing": 400,
    "industrialupgrade:baseore/titanium": 150, "industrialupgrade:baseore1/beryllium": 150,
    "industrialupgrade:baseore2/strontium": 150, "industrialupgrade:baseore2/yttrium": 150,
    "industrialupgrade:baseore2/thallium": 150, "industrialupgrade:itemingots/bronze_ingot": 60,
    "minecraft:blaze_rod": 100, "minecraft:lava_bucket": 300, "minecraft:gold_ingot": 100,
    # III
    "industrialupgrade:blockresource/machine": 3_000, "industrialupgrade:crafting_elements/crafting_273_element": 7_500,
    "industrialupgrade:itemingots/steel_ingot": 120, "industrialupgrade:crafting_elements/crafting_271_element": 100,
    "industrialupgrade:baseore/silver": 120, "industrialupgrade:baseore/nickel": 120,
    "industrialupgrade:upgrades/overclocker": 800, "industrialupgrade:battery/re_battery": 500,
    "industrialupgrade:cable/copper_cable": 100, "aquatech_machines:speed_upgrade": 2_500,
    # IV
    "botania:manasteel_ingot": 400, "botania:mana_pearl": 1_000, "botania:mana_diamond": 1_000,
    "botania:rune_water": 1_800, "botania:rune_fire": 1_800, "botania:rune_mana": 1_800,
    "industrialupgrade:blockresource/advanced_machine": 9_000, "industrialupgrade:crafting_elements/crafting_274_element": 4_000,
    "industrialupgrade:battery/energy_crystal": 3_000,
    # V
    "ae2:item_storage_cell_256k": 360_000, "botania:terrasteel_ingot": 3_000, "ae2:drive": 4_000,
    "ae2:item_storage_cell_64k": 115_000, "ae2:item_storage_cell_16k": 35_000, "ae2:engineering_processor": 2_500,
    "ae2:logic_processor": 2_500, "ae2:calculation_processor": 2_500, "ae2:item_storage_cell_4k": 10_000,
    "botania:elementium_ingot": 2_000,
    # VI
    "botania:life_essence": 4_000, "alexscaves:uranium_rod": 3_500, "industrialupgrade:baseore/tungsten": 3_000,
    "industrialupgrade:baseore/chromium": 3_000, "alexscaves:pearl": 6_000, "alexscaves:moth_dust": 3_000,
    "alexscaves:dark_tatters": 3_500, "minecraft:netherite_ingot": 12_000, "aquatech_machines:speed_upgrade_4": 15_000,
    "industrialupgrade:alloyingot/inconel": 5_000, "industrialupgrade:machines/spectral_solar_panel": 500_000,
    # VII
    "industrialupgrade:battery/lapotron_crystal": 20_000, "industrialupgrade:alloyingot/osmiridium": 9_000,
    "industrialupgrade:itemingots/adamantium": 9_000, "industrialupgrade:baseore/platinum": 7_000,
    "industrialupgrade:baseore/cobalt": 7_000, "aquatech_ui:rate_x32": 200_000,
    "industrialupgrade:nuclearresource/uranium_235": 6_000, "industrialupgrade:machines/photonic_solar_panel": 1_200_000,
    "industrialupgrade:machines/neutronium_solar_panel": 1_200_000,
    # VIII
    "avaritia:crystal_matrix_ingot": 60_000, "extrabotany:orichalcos_ingot": 40_000, "extrabotany:aerialite_ingot": 25_000,
    "extrabotany:spirit_fuel": 20_000, "mythicbotany:alfsteel_ingot": 50_000, "extendedcrafting:black_iron_ingot": 15_000,
    "extendedcrafting:luminessence": 15_000, "minecraft:nether_star": 40_000, "aquatech_ui:rate_x64": 600_000,
    "ae2:singularity": 60_000, "industrialupgrade:machines/barion_solar_panel": 2_800_000,
    # IX
    "draconicevolution:awakened_core": 600_000, "draconicevolution:dragon_heart": 400_000,
    "draconicevolution:awakened_draconium_ingot": 150_000, "draconicevolution:draconium_core": 60_000,
    "draconicevolution:wyvern_core": 30_000, "draconicevolution:draconium_ingot": 20_000,
    "draconicevolution:wyvern_energy_core": 60_000, "draconicevolution:basic_crafting_injector": 150_000,
    "avaritia:neutron_ingot": 150_000, "minecraft:dragon_egg": 500_000,
    "industrialupgrade:machines/hadron_solar_panel": 6_000_000,
    # X
    "avaritia:infinity_ingot": 15_000_000, "avaritia:extreme_crafting_table": 2_500_000,
    "avaritia:infinity_catalyst": 900_000, "draconicevolution:awakened_draconium_block": 600_000,
    "avaritia:neutron_nugget": 16_000, "avaritia:neutron_compressor": 2_500_000,
    "industrialupgrade:machines/graviton_solar_panel": 13_000_000,
}

VALUE.update({
    "industrialupgrade:basemachine3/steamboiler": 700,
    "industrialupgrade:basemachine3/steam_peat_generator": 700,
    "industrialupgrade:dryer/dryer": 600,
    "industrialupgrade:basemachine3/rolling_machine": 600,
    "industrialupgrade:basemachine3/steam_converter": 1_500,
    "industrialupgrade:basemachine3/steam_storage": 1_800,
    "industrialupgrade:basemachine3/steam_pump": 1_500,
    "industrialupgrade:basemachine3/steam_handler_ore": 2_000,
    "industrialupgrade:mini_smeltery/mini_smeltery": 2_000,
    "industrialupgrade:basemachine3/steam_ampere_generator": 6_000,
    "industrialupgrade:basemachine3/generator_iu": 5_000,
    "industrialupgrade:simplemachine/macerator_iu": 4_500,
    "industrialupgrade:simplemachine/furnace_iu": 4_500,
    "industrialupgrade:wiring_storage/batbox_iu": 6_000,
    "industrialupgrade:machines/advanced_solar_paneliu": 8_000,
    "industrialupgrade:simplemachine/compressor_iu": 9_000,
    "industrialupgrade:simplemachine/extractor_iu": 9_000,
    "industrialupgrade:basemachine3/electronic_assembler": 10_000,
    "industrialupgrade:moremachine3/orewashing": 9_000,
    "industrialupgrade:basemachine/alloy_smelter": 15_000,
    "industrialupgrade:machines/hybrid_solar_paneliu": 18_000,
    "botania:mana_spreader": 5_000,
    "botania:mana_pool": 8_000,
    "botania:terra_plate": 30_000,
    "industrialupgrade:moremachine3/farmer": 20_000,
    "industrialupgrade:wiring_storage/cesu_iu": 18_000,
    "industrialupgrade:wiring_storage/mfe_iu": 40_000,
    "industrialupgrade:machines/ultimate_solar_paneliu": 45_000,
    "industrialupgrade:machines/quantum_solar_paneliu": 60_000,
    "industrialupgrade:wiring_storage/mfsu_iu": 50_000,
    "ae2:molecular_assembler": 8_000,
    "ae2:controller": 25_000,
    "ae2:inscriber": 15_000,
    "industrialupgrade:basemachine2/plastic_creator": 40_000,
    "industrialupgrade:machines/proton_solar_panel": 250_000,
    "industrialupgrade:wiring_storage/ult_mfsu": 150_000,
    "industrialupgrade:moremachine/quad_furnace": 60_000,
    "industrialupgrade:basemachine1/enrichment": 80_000,
    "industrialupgrade:machines/singular_solar_panel": 700_000,
    "industrialupgrade:wiring_storage/adv_mfsu": 400_000,
    "industrialupgrade:basemachine/neutron_generator": 300_000,
    "industrialupgrade:basemachine1/synthesis": 400_000,
    "industrialupgrade:wiring_storage/per_mfsu": 500_000,
    "industrialupgrade:wiring_storage/bar_mfsu": 1_200_000,
    "industrialupgrade:sintezator/sintezator": 600_000,
    "extendedcrafting:ultimate_table": 900_000,
    "avaritia:neutron_collector": 800_000,
    "industrialupgrade:wiring_storage/had_mfsu": 2_500_000,
    "draconicevolution:crafting_core": 1_000_000,
    "draconicevolution:energy_core": 900_000,
    "industrialupgrade:wiring_storage/gra_mfsu": 5_000_000,
    "industrialupgrade:wiring_storage/qua_mfsu": 6_000_000,
    "industrialupgrade:machines/quark_solar_panel": 15_000_000,
})

# Один и тот же предмет в разных тирах стоит по-разному (оверклокер и схемы в VII ценнее, чем в III).
VALUE_BY_CASE = {
    ("superconductor", "industrialupgrade:upgrades/overclocker"): 6_000,
    ("superconductor", "industrialupgrade:crafting_elements/crafting_274_element"): 6_000,
    ("infinity", "minecraft:nether_star"): 130_000,
    ("infinity", "avaritia:crystal_matrix_ingot"): 110_000,
    ("infinity", "avaritia:neutron_nugget"): 45_000,
    ("infinity", "avaritia:neutron_ingot"): 200_000,
    ("infinity", "avaritia:infinity_catalyst"): 1_200_000,
    ("infinity", "draconicevolution:awakened_draconium_block"): 800_000,
}

# Предметы, которые дают только штучно (контроллеры, панели, редкие блоки): количество не больше этого числа.
MAX_QTY_BY_ITEM = {
    "industrialupgrade:smeltery/smeltery_controller": 1,
    "avaritia:infinity_ingot": 1, "avaritia:extreme_crafting_table": 1, "avaritia:neutron_compressor": 1,
    "minecraft:dragon_egg": 2, "draconicevolution:awakened_core": 2, "draconicevolution:dragon_heart": 4,
    "draconicevolution:awakened_draconium_block": 8, "aquatech_ui:rate_x32": 2, "aquatech_ui:rate_x64": 2,
    "industrialupgrade:machines/spectral_solar_panel": 1, "industrialupgrade:machines/photonic_solar_panel": 1,
    "industrialupgrade:machines/neutronium_solar_panel": 1, "industrialupgrade:machines/barion_solar_panel": 1,
    "industrialupgrade:machines/hadron_solar_panel": 1, "industrialupgrade:machines/graviton_solar_panel": 1,
    "ae2:item_storage_cell_256k": 2, "ae2:item_storage_cell_64k": 2, "ae2:item_storage_cell_16k": 4,
    "ae2:item_storage_cell_4k": 10, "minecraft:lava_bucket": 4, "minecraft:diamond": 16, "avaritia:neutron_nugget": 128, "avaritia:infinity_catalyst": 8,
}


# Новые награды: (предмет, название, шанс в процентах, максимум штук); ценность за штуку лежит в VALUE.
# Эпохи: I-II только паровая техника (энергии ещё нет), III базовая электрика, IV-V улучшенная электрика, дальше поздняя техника.
# Старые веса при добавлении сжимаются на OLD_WEIGHT_SCALE.
OLD_WEIGHT_SCALE = 0.8
ADDITIONS = {
    "starter": [
        ("industrialupgrade:basemachine3/steamboiler", "Паровой котёл", 5, 2),
        ("industrialupgrade:basemachine3/steam_peat_generator", "Паровой генератор на торфе", 5, 2),
        ("industrialupgrade:dryer/dryer", "Примитивная сушилка латекса", 4, 2),
        ("industrialupgrade:basemachine3/rolling_machine", "Примитивный прокатный механизм", 4, 2),
    ],
    "smeltery": [
        ("industrialupgrade:basemachine3/steam_converter", "Паровой преобразователь пара", 4, 2),
        ("industrialupgrade:basemachine3/steam_storage", "Паровое хранилище", 4, 2),
        ("industrialupgrade:basemachine3/steam_pump", "Паровая помпа", 3, 2),
        ("industrialupgrade:basemachine3/steam_handler_ore", "Паровой сепаратор", 4, 2),
        ("industrialupgrade:mini_smeltery/mini_smeltery", "Мини-плавильня", 4, 2),
    ],
    "steam": [
        ("industrialupgrade:basemachine3/steam_ampere_generator", "Паровой преобразователь тока", 3, 2),
        ("industrialupgrade:basemachine3/generator_iu", "Генератор", 4, 2),
        ("industrialupgrade:simplemachine/macerator_iu", "Дробитель", 4, 2),
        ("industrialupgrade:simplemachine/furnace_iu", "Электрическая печь", 4, 2),
        ("industrialupgrade:wiring_storage/batbox_iu", "Энергохранилище", 3, 2),
        ("industrialupgrade:machines/advanced_solar_paneliu", "Улучшенная солнечная панель", 3, 2),
    ],
    "flora": [
        ("industrialupgrade:simplemachine/compressor_iu", "Сжиматель", 4, 2),
        ("industrialupgrade:simplemachine/extractor_iu", "Экстрактор", 4, 2),
        ("industrialupgrade:basemachine3/electronic_assembler", "Электрический сборщик электроники", 3, 2),
        ("industrialupgrade:moremachine3/orewashing", "Рудопромывочный механизм", 3, 2),
        ("industrialupgrade:basemachine/alloy_smelter", "Завод сплавов", 3, 2),
        ("industrialupgrade:machines/hybrid_solar_paneliu", "Гибридная солнечная панель", 3, 2),
        ("botania:mana_spreader", "Распространитель маны", 4, 4),
        ("botania:mana_pool", "Бассейн маны", 3, 2),
        ("botania:terra_plate", "Теллурическая агломерационная пластина", 2, 1),
    ],
    "applied": [
        ("industrialupgrade:moremachine3/farmer", "Автономная ферма", 4, 2),
        ("industrialupgrade:wiring_storage/cesu_iu", "МЭСН", 3, 2),
        ("industrialupgrade:wiring_storage/mfe_iu", "МФЭ", 4, 2),
        ("industrialupgrade:machines/ultimate_solar_paneliu", "Совершенная гибридная солнечная панель", 3, 2),
        ("industrialupgrade:machines/quantum_solar_paneliu", "Квантовая солнечная панель", 3, 2),
        ("industrialupgrade:wiring_storage/mfsu_iu", "МФСУ", 3, 2),
        ("ae2:molecular_assembler", "Молекулярный сборщик", 4, 6),
        ("ae2:controller", "МЭ-регулятор", 3, 2),
        ("ae2:inscriber", "Вырезатель", 3, 4),
        ("industrialupgrade:basemachine2/plastic_creator", "Химический завод", 2, 1),
    ],
    "abyss": [
        ("industrialupgrade:machines/proton_solar_panel", "Протонная солнечная панель", 3, 1),
        ("industrialupgrade:wiring_storage/ult_mfsu", "Продвинутое МФСУ", 4, 2),
        ("industrialupgrade:moremachine/quad_furnace", "Совершенная электрическая печь", 3, 4),
        ("industrialupgrade:basemachine1/enrichment", "Обогатитель", 3, 2),
    ],
    "superconductor": [
        ("industrialupgrade:machines/singular_solar_panel", "Сингулярная солнечная панель", 2, 1),
        ("industrialupgrade:wiring_storage/adv_mfsu", "Улучшенное МФСУ", 4, 2),
        ("industrialupgrade:basemachine/neutron_generator", "Генератор нейтронных частиц", 3, 2),
        ("industrialupgrade:basemachine1/synthesis", "Реактор ядерного синтеза", 3, 1),
        ("industrialupgrade:wiring_storage/per_mfsu", "Совершенное МФСУ", 3, 1),
    ],
    "singularity": [
        ("industrialupgrade:wiring_storage/bar_mfsu", "Барионное МФСУ", 3, 2),
        ("industrialupgrade:sintezator/sintezator", "Объединитель панелей", 4, 2),
        ("extendedcrafting:ultimate_table", "Максимальный верстак", 3, 1),
        ("avaritia:neutron_collector", "Нейтрониевый коллектор", 3, 2),
    ],
    "draconic": [
        ("industrialupgrade:wiring_storage/had_mfsu", "Адронное МФСУ", 4, 2),
        ("draconicevolution:crafting_core", "Ядро слияния", 4, 2),
        ("draconicevolution:energy_core", "Энергетическое ядро", 3, 2),
    ],
    "infinity": [
        ("industrialupgrade:wiring_storage/gra_mfsu", "Гравитонное МФСУ", 4, 2),
        ("industrialupgrade:wiring_storage/qua_mfsu", "Кварковое МФСУ", 4, 2),
        ("industrialupgrade:machines/quark_solar_panel", "Кварковая солнечная панель", 0.2, 1),
    ],
}

# Названия по реальным именам предметов (lang-файлы модов), без выдуманных «тиров».
RENAME = {
    "industrialupgrade:machines/spectral_solar_panel": "Спектральная солнечная панель",
    "industrialupgrade:machines/photonic_solar_panel": "Фотонная солнечная панель",
    "industrialupgrade:machines/neutronium_solar_panel": "Нейтронная солнечная панель",
    "industrialupgrade:machines/barion_solar_panel": "Барионная солнечная панель",
    "industrialupgrade:machines/hadron_solar_panel": "Адронная солнечная панель",
    "industrialupgrade:machines/graviton_solar_panel": "Гравитонная солнечная панель",
}


def apply_additions(case):
    """Добавляет новые предметы в лут один раз и сжимает старые веса, чтобы общая сумма осталась около 100."""
    rows = [r for r in ADDITIONS.get(case["id"], []) if all(e.get("item") != r[0] for e in case["loot"])]
    if rows:
        percent = sum(e["weight"] for e in case["loot"]) / 100  # вес одного процента: у кейсов сумма весов 500, а не 100
        for entry in case["loot"]:
            entry["weight"] = max(1, round(entry["weight"] * OLD_WEIGHT_SCALE))
        position = next(i for i, e in enumerate(case["loot"]) if e["type"] == "coins")
        for offset, (item, label, weight, cap) in enumerate(rows):
            case["loot"].insert(position + offset, {"type": "item", "item": item, "label": label,
                                                    "min": 1, "max": cap, "weight": max(1, round(weight * percent))})
    for entry in case["loot"] + ([case["pity"]] if case.get("pity") else []):
        if entry.get("item") in RENAME:
            entry["label"] = RENAME[entry["item"]]


def _register_caps():
    for rows in ADDITIONS.values():
        for item, _label, _weight, cap in rows:
            MAX_QTY_BY_ITEM.setdefault(item, cap)


_register_caps()


def nice_qty(value):
    """Округляет количество до «человеческого» числа: 1-12 как есть, дальше шагами 2, 4, 8, 16."""
    value = max(1, round(value))
    if value <= 12:
        return value
    step = 2 if value < 24 else 4 if value < 64 else 8 if value < 128 else 16
    return int(round(value / step) * step)


def nice_money(value):
    """Две значащие цифры."""
    if value < 100:
        return max(1, int(round(value)))
    digits = int(math.floor(math.log10(value)))
    base = 10 ** (digits - 1)
    return int(round(value / base) * base)


def weight_class(share):
    if share < 0.01:
        return None  # заглавная: ровно одна штука
    if share < 0.03:
        return 2.5
    if share < 0.065:
        return 1.0
    if share < 0.095:
        return 0.55
    return 0.35


def unit_value(case_id, item_id):
    return VALUE_BY_CASE.get((case_id, item_id), VALUE[item_id])


def qty_range(case_id, item_id, target_value):
    unit = unit_value(case_id, item_id)
    cap = MAX_QTY_BY_ITEM.get(item_id, MAX_QTY)
    middle = min(cap, nice_qty(target_value / unit))
    low = max(1, min(middle, nice_qty(middle * 0.8)))
    high = min(cap, max(middle, nice_qty(middle * 1.25)))
    return low, high


def avg_value(entry, case_id=None):
    avg = (entry["min"] + entry["max"]) / 2
    if entry["type"] == "coins":
        return avg
    if entry["type"] == "gems":
        return avg * GEM_COINS
    return avg * unit_value(case_id, entry["item"])


def rebalance_case(case):
    apply_additions(case)
    price = PRICES[case["id"]]
    target = TARGET_EV[case["id"]] * price
    loot = case["loot"]
    total = sum(e["weight"] for e in loot)
    items = [e for e in loot if e["type"] == "item"]
    coins = next((e for e in loot if e["type"] == "coins"), None)
    gems = next((e for e in loot if e["type"] == "gems"), None)

    def items_ev(scale):
        ev = avg_value(gems) * gems["weight"] / total if gems else 0.0
        for entry in items:
            share = entry["weight"] / total
            factor = weight_class(share)
            if factor is None:
                entry["min"] = entry["max"] = 1
            else:
                entry["min"], entry["max"] = qty_range(case["id"], entry["item"], factor * price * scale)
            ev += share * avg_value(entry, case["id"])
        return ev

    # Монеты в среднем около COIN_AVG цены кейса: чем выше множитель scale, тем больше достаётся предметам.
    coin_share = coins["weight"] / total if coins else 1.0
    low, high = 0.5, 2.0
    for _ in range(40):
        scale = (low + high) / 2
        if target - items_ev(scale) > COIN_AVG * price * coin_share:
            low = scale
        else:
            high = scale
    remainder = target - items_ev(high)
    remainder = max(remainder, 0.1 * target)
    if coins:
        avg = remainder / (coins["weight"] / total)
        coins["min"], coins["max"] = nice_money(avg * 0.7), nice_money(avg * 1.3)

    pity = case.get("pity")
    if pity:
        unit = unit_value(case["id"], pity["item"])
        cap = MAX_QTY_BY_ITEM.get(pity["item"], MAX_QTY)
        need = min(cap, max(1, math.ceil(PITY_SHARE * price / unit)))
        pity["min"] = pity["max"] = need
    case["costCoins"] = price


def check_schema(cases):
    """CaseConfig в моде читает weight, min и max как int: дробное число роняет весь файл, и хаб показывает запасные кейсы."""
    for case in cases:
        for entry in case["loot"] + ([case["pity"]] if case.get("pity") else []):
            for field in ("weight", "min", "max"):
                if field in entry and not isinstance(entry[field], int):
                    raise ValueError(f"{case['id']} / {entry.get('label')}: {field}={entry[field]!r} не целое")
        if not isinstance(case["costCoins"], int):
            raise ValueError(f"{case['id']}: costCoins не целое")


def rebalance(cases):
    for case in cases:
        rebalance_case(case)
    check_schema(cases)


def report(cases, before=None):
    print(f"{'кейс':15}{'цена':>12}{'было':>12}{'EV':>12}{'%':>5}  гарант / слабые предметы")
    for case in cases:
        price = case["costCoins"]
        loot = case["loot"]
        total = sum(e["weight"] for e in loot)
        ev = sum(e["weight"] / total * avg_value(e, case["id"]) for e in loot)
        low = [f"{e['label']}={avg_value(e, case['id']) / price:.0%}" for e in loot
               if e["type"] == "item" and avg_value(e, case['id']) < MIN_SHARE * price]
        for e in loot:
            if e["type"] == "coins" and e["min"] < MIN_SHARE * price:
                low.append(f"{e['label']} от {e['min']}")
        pity = case.get("pity")
        pity_note = f"{avg_value(pity, case['id']) / price:.0%}" if pity else "-"
        was = f"{before[case['id']]:,}" if before else ""
        print(f"{case['id']:15}{price:>12,}{was:>12}{ev:>12,.0f}{ev / price * 100:>4.0f}%  "
              f"гарант {pity_note}; {', '.join(low) or 'слабых нет'}")


def main():
    only_report = "--report" in sys.argv
    with open(TARGETS[0] if only_report else BASE, encoding="utf-8", newline="") as source:
        data = json.loads(source.read())
    before = {c["id"]: c["costCoins"] for c in data["cases"]}
    if not only_report:
        rebalance(data["cases"])
        text = json.dumps(data, ensure_ascii=False, indent=2).replace("\n", "\r\n") + "\r\n"
        for target in TARGETS:
            with open(target, "w", encoding="utf-8", newline="") as out:
                out.write(text)
        print("записано:", ", ".join(str(t.relative_to(ROOT)) for t in TARGETS))
    report(data["cases"], before)


if __name__ == "__main__":
    main()
