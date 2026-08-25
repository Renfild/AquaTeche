import json
import os

REPLACEMENTS = {
    # Fix Tier 1: Clay -> Smeltery Mix, Brick -> Smeltery Bricks, Lead Ore
    "minecraft:clay_ball": {
        "item": "industrialupgrade:crafting_elements/crafting_773_element",
        "label": "Плавильная смесь",
        "min": 16,
        "max": 32,
    },
    "minecraft:brick": {
        "item": "industrialupgrade:crafting_elements/crafting_772_element",
        "label": "Плавильные кирпичи",
        "min": 16,
        "max": 32,
    },
    "industrialupgrade:baseore/lead": {
        "item": "industrialupgrade:classicore/lead",
        "label": "Свинцовая руда",
    },
    # Smeltery
    "industrialupgrade:smeltery_furnace/smeltery_furnace": {
        "item": "industrialupgrade:smeltery/smeltery_controller",
        "label": "Контроллер плавильни",
    },
    "industrialupgrade:smeltery_casing/smeltery_casing": {
        "item": "industrialupgrade:smeltery/smeltery_casing",
        "label": "Блоки корпуса плавильни",
    },
    "industrialupgrade:baseore2/beryllium": {
        "item": "industrialupgrade:baseore1/beryllium",
        "label": "Бериллиевая руда",
    },
    # Steam
    "industrialupgrade:rubber_drop/rubber_drop": {
        "item": "industrialupgrade:crafting_elements/crafting_271_element",
        "label": "Резина IU",
    },
    "industrialupgrade:upgrades/overcloker_upgrade": {
        "item": "industrialupgrade:upgrades/overclocker",
        "label": "Ускорители (Оверклокеры)",
    },
    "industrialupgrade:storage_batteries/re_battery": {
        "item": "industrialupgrade:battery/re_battery",
        "label": "Аккумуляторы RE-Battery",
    },
    "industrialupgrade:wiring/copper_cable": {
        "item": "industrialupgrade:cable/copper_cable",
        "label": "Изолированные медные провода",
    },
    # Flora
    "industrialupgrade:blockresource/adv_machine": {
        "item": "industrialupgrade:blockresource/advanced_machine",
        "label": "Улучшенный корпус механизма",
    },
    "industrialupgrade:storage_batteries/energy_crystal": {
        "item": "industrialupgrade:battery/energy_crystal",
        "label": "Энергетические кристаллы",
    },
    # Abyss
    "alexscaves:abyssal_pearl": {
        "item": "alexscaves:pearl",
        "label": "Жемчужины Бездны",
    },
    "aquatech_ui:upgrade_speed_x4": {
        "item": "aquatech_ui:speed_x4_upgrade",
        "label": "Модуль ускорения ×4",
    },
    # Superconductor
    "industrialupgrade:storage_batteries/lapotron_crystal": {
        "item": "industrialupgrade:battery/lapotron_crystal",
        "label": "Лапотронные кристаллы",
    },
    "industrialupgrade:alloyingot/adamantium": {
        "item": "industrialupgrade:itemingots/adamantium",
        "label": "Сплав Адамантий",
    },
    "industrialupgrade:materials_nuclear/uranium_235": {
        "item": "industrialupgrade:nuclearresource/uranium_235",
        "label": "Изотопы Уран-235",
    },
    # Singularity
    "botania:alfsteel_ingot": {
        "item": "mythicbotany:alfsteel_ingot",
        "label": "Слитки Альфстали",
    },
    # Draconic
    "draconicevolution:crafting_injector": {
        "item": "draconicevolution:basic_crafting_injector",
        "label": "Инжекторы слияния",
    },
    # Infinity
    "avaritia:neutronium_compressor": {
        "item": "avaritia:neutron_compressor",
        "label": "Нейтрониевый компрессор",
    },
}

target_files = [
    "config/aqualumen/cases.json",
    "server/config/aqualumen/cases.json"
]

for tf in target_files:
    if not os.path.exists(tf):
        continue
    with open(tf, "r", encoding="utf-8") as f:
        data = json.load(f)

    for case in data["cases"]:
        for loot in case.get("loot", []):
            it = loot.get("item", "")
            if it in REPLACEMENTS:
                rep = REPLACEMENTS[it]
                loot["item"] = rep["item"]
                loot["label"] = rep["label"]
                if "min" in rep:
                    loot["min"] = rep["min"]
                if "max" in rep:
                    loot["max"] = rep["max"]

    with open(tf, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)

    print(f"Updated {tf}")
