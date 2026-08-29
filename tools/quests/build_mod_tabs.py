# -*- coding: utf-8 -*-
"""
Add Botania / Alex's Caves / AE2 / Avaritia tabs, rewrite secret + endgame.
Patch broken IU steam item IDs. Every item is checked against jar models.
"""
from __future__ import annotations

import hashlib
import re
import shutil
import zipfile
from pathlib import Path

from useful_rewards import apply_all, _format_item

ROOT = Path(r"C:/Users/xieto/Desktop/AquaTech")
MODS = ROOT / "server" / "mods"
CFG = ROOT / "config" / "ftbquests" / "quests" / "chapters"
SRV = ROOT / "server" / "config" / "ftbquests" / "quests" / "chapters"

JARS = {
    "industrialupgrade": "IndustrialUpgrade-1.20.1-3.4.0.11.jar",
    "ae2": "appliedenergistics2-forge-15.4.10.jar",
    "botania": "Botania-1.20.1-454-FORGE.jar",
    "botanicalmachinery": "BotanicalMachinery-1.20.1-3.0.10.jar",
    "botanicalextramachinery": "botanicalextramachinery-1.20.1-1.1.2.jar",
    "mythicbotany": "MythicBotany-1.20.1-4.0.4.jar",
    "avaritia": "Re-Avaritia-forge-1.20.1-1.4.1-release.jar",
    "avaritia_armor": "avaritia_armor-0.1.3.jar",
    "alexscaves": "alexscaves-2.0.2.jar",
    "aquatech_ui": "aquatech_ui-1.0.30.jar",
    "starcatcher": "starcatcher-2.3.19-FORGE-1.20.1.jar",
}

BANNED_REWARD = (
    "waystones:",
    "ae2:quantum_",
    "ae2:spatial_",
    "ae2:wireless_terminal",
    "botania:flight_tiara",
    "botania:flugel_eye",
    "botania:world_seed",
    "avaritia:infinity_",
    "avaritia:endest_pearl",
    "industrialupgrade:teleporter",
    "industrialupgrade:jetpack",
    "industrialupgrade:creative_",
    "industrialupgrade:upgrades/overclocker",
    "industrialupgrade:upgrades/transformerupgrade",
    "aquatech_ui:speed_upgrade",
    "aquatech_ui:speed_x4",
    "botanicalmachinery:mana_battery_creative",
)


def reward_ok(item: str) -> bool:
    low = item.lower()
    return not any(b.lower() in low for b in BANNED_REWARD)


def filler_for(item: str) -> tuple[str, int]:
    ns = item.split(":")[0] if ":" in item else ""
    table = {
        "botania": ("botania:white_petal", 8),
        "mythicbotany": ("mythicbotany:alfsteel_nugget", 4),
        "botanicalmachinery": ("botania:manasteel_ingot", 2),
        "botanicalextramachinery": ("botania:manasteel_ingot", 2),
        "alexscaves": ("alexscaves:amber", 4),
        "avaritia": ("minecraft:diamond", 4),
        "avaritia_armor": ("avaritia:crystal_matrix_ingot", 1),
        "industrialupgrade": ("industrialupgrade:itemingots/copper_ingot", 8),
        "ae2": ("ae2:item_storage_cell_1k", 1),
        "starcatcher": ("minecraft:cooked_salmon", 8),
        "aquatech_ui": ("minecraft:cooked_salmon", 8),
    }
    pick = table.get(ns, ("minecraft:bread", 8))
    if item.startswith("avaritia:infinity") or item == "avaritia:endest_pearl":
        return ("avaritia:neutron_nugget", 4)
    return pick

# Stable IDs from generate_iu_ftbquests_full.py (existing chapters).
STEAM_LAST = "4FE087A7196F1E38"  # blast furnace controller
BASIC_LAST = "799AE4C1FE0064F3"  # radioactive waste
IMPROVED_LAST = "B969D019BBF81B8E"  # soil purifier
END_ALPHA = "031AE9F35C0842AFAB46B80ABFF9B669"
END_STARS = "9CF6D75A5FFE4A90845E52F198ADF492"
END_ADMIN = "ABC9A464ECF145D1BFE52FDD4581AE9B"
CH_SECRET = "C56C87EE1AC44667B6185BB27FE787CA"
CH_END = "891345EEFA4A4413835FEB0FD67E06AA"


def hid(seed: str) -> str:
    return hashlib.md5(seed.encode("utf-8")).hexdigest()[:16].upper()


def load_models() -> set[str]:
    out: set[str] = set()
    for ns, name in JARS.items():
        z = zipfile.ZipFile(MODS / name)
        prefix = f"assets/{ns}/models/item/"
        for n in z.namelist():
            if n.startswith(prefix) and n.endswith(".json"):
                rel = n[len(prefix) : -5].replace("\\", "/")
                out.add(f"{ns}:{rel}")
    return out


def esc(s: str) -> str:
    return s.replace("\\", "\\\\").replace('"', '\\"')


def wrap(text: str, width: int = 52) -> list[str]:
    words = text.split()
    lines: list[str] = []
    cur: list[str] = []
    n = 0
    for w in words:
        if cur and n + len(w) + 1 > width:
            lines.append(" ".join(cur))
            cur = [w]
            n = len(w)
        else:
            cur.append(w)
            n += len(w) + 1
    if cur:
        lines.append(" ".join(cur))
    return lines[:6]


class Q:
    def __init__(
        self,
        name: str,
        title: str,
        item: str,
        desc: str,
        x: float,
        y: float,
        prev: str | None = None,
        count: int = 1,
        rewards: list[tuple[str, int]] | None = None,
        hide: bool = False,
        checkmark: bool = False,
        extra_deps: list[str] | None = None,
        xp: int = 75,
    ):
        self.name = name
        self.title = title
        self.item = item
        self.desc = desc
        self.x = x
        self.y = y
        self.prev = prev  # str, list[str], or None
        self.count = count
        self.rewards = list(rewards) if rewards else [filler_for(item)]
        self.hide = hide
        self.checkmark = checkmark
        self.extra_deps = extra_deps or []
        self.xp = xp


def write_chapter(
    filename: str,
    chapter_id: str,
    title: str,
    icon: str,
    order: int,
    quests: list[Q],
    models: set[str],
    subtitle: str = "",
    id_prefix: str = "",
) -> dict[str, str]:
    prefix = id_prefix or filename
    name_to_id = {q.name: hid(f"{prefix}_{q.name}") for q in quests}
    bad = []
    for q in quests:
        if q.item and not q.item.startswith("minecraft:") and q.item not in models:
            bad.append((q.name, q.item))
        for it, _c in q.rewards:
            if not it.startswith("minecraft:") and it not in models:
                bad.append((q.name + "/reward", it))
            if not reward_ok(it):
                bad.append((q.name + "/banned-reward", it))
    if bad:
        raise SystemExit(f"{filename} BAD ITEMS: {bad}")

    lines = [
        "{",
        "	default_hide_dependency_lines: false",
        '	default_quest_shape: ""',
        f'	filename: "{filename}"',
        '	group: ""',
        f'	icon: "{icon}"',
        f'	id: "{chapter_id}"',
        f"	order_index: {order}",
        "	quest_links: [ ]",
    ]
    if subtitle:
        lines.append(f'	subtitle: "{esc(subtitle)}"')
    lines.append("	quests: [")

    for q in quests:
        qid = name_to_id[q.name]
        deps = []
        prevs = q.prev if isinstance(q.prev, list) else ([q.prev] if q.prev else [])
        for p in prevs:
            if p in name_to_id:
                deps.append(name_to_id[p])
            else:
                deps.append(p)
        deps.extend(q.extra_deps)

        lines.append("		{")
        if deps:
            joined = ", ".join(f'"{d}"' for d in deps)
            lines.append(f"			dependencies: [{joined}]")
        desc_lines = wrap(q.desc)
        if desc_lines:
            lines.append("			description: [")
            for dl in desc_lines:
                lines.append(f'				"{esc(dl)}"')
            lines.append("			]")
        if q.hide:
            lines.append("			hide: true")
            lines.append("			hide_until_deps_complete: true")
        lines.append(f'			id: "{qid}"')
        lines.append(f'			icon: "{q.item}"')

        reward_bits = []
        for i, (it, cnt) in enumerate(q.rewards):
            reward_bits.append(_format_item(qid, it, cnt, i))
        rid_xp = hid(f"{qid}_xp")
        reward_bits.append(
            "{\n"
            f'				id: "{rid_xp}"\n'
            '				type: "xp"\n'
            f"				xp: {q.xp}\n"
            "			}"
        )
        lines.append("			rewards: [" + ",".join(reward_bits) + "]")

        tid = hid(f"{qid}_task")
        if q.checkmark:
            lines.append("			tasks: [{")
            lines.append(f'				id: "{tid}"')
            lines.append('				type: "checkmark"')
            lines.append("			}]")
        else:
            cnt = f"\n				count: {q.count}L" if q.count > 1 else ""
            lines.append("			tasks: [{")
            lines.append(f'				id: "{tid}"{cnt}')
            lines.append(f'				item: "{q.item}"')
            lines.append('				type: "item"')
            lines.append("			}]")
        lines.append(f'			title: "{esc(q.title)}"')
        lines.append(f"			x: {q.x:.1f}d")
        lines.append(f"			y: {q.y:.1f}d")
        lines.append("		}")

    lines.append("	]")
    lines.append(f'	title: "{esc(title)}"')
    lines.append("}")
    text = "\n".join(lines) + "\n"
    for dest in (CFG, SRV):
        dest.mkdir(parents=True, exist_ok=True)
        (dest / f"{filename}.snbt").write_text(text, encoding="utf-8")
    return name_to_id


def botania_quests() -> list[Q]:
    p = "botania:white_petal"
    ms = "botania:manasteel_ingot"
    return [
        Q("lexicon", "Лексика Ботании", "botania:lexicon",
          "Открой книгу. Без неё цветы и мана остаются загадкой.",
          0, 0, rewards=[(p, 16)]),
        Q("petal", "Белый лепесток", "botania:white_petal",
          "Сломай мистический цветок. Лепестки — сырьё почти всех ранних крафтов Botania.",
          1.5, 0, "lexicon", count=4, rewards=[(p, 16)]),
        Q("apothecary", "Лепестковый алтарь", "botania:apothecary_default",
          "Поставь алтарь, налей воду, брось лепестки. Так собираются функциональные цветы.",
          3.0, 0, "petal", rewards=[(p, 16)]),
        Q("pure_daisy", "Чистоцвет", "botania:pure_daisy",
          "Первый рабочий цветок. Превращает дерево в живую древесину, камень — в живой камень.",
          4.5, 0, "apothecary", rewards=[("botania:livingwood", 8)]),
        Q("livingwood", "Живая древесина", "botania:livingwood",
          "Поставь дуб рядом с чистоцветом и подожди. Это каркас мана-сетей.",
          6.0, -1.0, "pure_daisy", count=8, rewards=[("botania:livingwood", 16)]),
        Q("livingrock", "Живой камень", "botania:livingrock",
          "Тот же чистоцвет, но на обычном камне. Из него делают бассейн и алтарь рун.",
          6.0, 1.0, "pure_daisy", count=8, rewards=[("botania:livingrock", 16)]),
        Q("pool", "Бассейн маны", "botania:mana_pool",
          "Склад маны. Без него спредеры и пластина Терры не работают.",
          7.5, 0, "livingrock", rewards=[(ms, 4)]),
        Q("spreader", "Распылитель маны", "botania:mana_spreader",
          "Стреляет маной в бассейн. Нацель его на бассейн и поставь генерирующий цветок сзади.",
          9.0, 0, ["pool", "livingwood"], rewards=[(ms, 4)]),
        Q("tablet", "Таблица маны", "botania:mana_tablet",
          "Переносной заряд. Нужна, чтобы не бегать к бассейну каждый раз.",
          10.5, -1.0, "spreader", rewards=[("botania:mana_tablet", 1)]),
        Q("manasteel", "Манасталь", "botania:manasteel_ingot",
          "Брось железный слиток в полный бассейн. Базовый металл Botania.",
          10.5, 1.0, "spreader", count=4, rewards=[(ms, 8)]),
        Q("wand", "Жезл из прута", "botania:twig_wand",
          "Связывает спредеры с приёмниками. ПКМ по спредеру, затем по бассейну.",
          12.0, 0, "manasteel", rewards=[(ms, 4)]),
        Q("altar", "Рунический алтарь", "botania:runic_altar",
          "Крафт рун. Налей ману в алтарь, разложи ингредиенты, забери руну жезлом.",
          13.5, 0, "wand", rewards=[("botania:rune_mana", 2)]),
        Q("rune_water", "Руна воды", "botania:rune_water",
          "Одна из четырёх стихийных рун. Дальше из них собирают сезонные и греховные.",
          15.0, -1.5, "altar", rewards=[("botania:rune_water", 2)]),
        Q("rune_fire", "Руна огня", "botania:rune_fire",
          "Стихийная руна. Нужна для Терры и части функциональных цветов.",
          15.0, -0.5, "rune_water", rewards=[("botania:rune_fire", 2)]),
        Q("rune_earth", "Руна земли", "botania:rune_earth",
          "Стихийная руна. Часто идёт в крафт землеройных и защитных цветов.",
          15.0, 0.5, "rune_fire", rewards=[("botania:rune_earth", 2)]),
        Q("rune_air", "Руна воздуха", "botania:rune_air",
          "Стихийная руна. Закрывает четвёрку стихий для пластины Терры.",
          15.0, 1.5, "rune_earth", rewards=[("botania:rune_air", 2)]),
        Q("pylon", "Мана-пилон", "botania:mana_pylon",
          "Усиливает передачу маны и нужен у портала в Альфхейм.",
          16.5, 0, "rune_air", rewards=[(ms, 8)]),
        Q("terra_plate", "Пластина Терры", "botania:terra_plate",
          "Ставится на живой камень. Глотает ману, манасталь, мана-жемчуг и мана-алмаз.",
          18.0, 0, "pylon", rewards=[("botania:mana_pearl", 4)]),
        Q("terrasteel", "Террасталь", "botania:terrasteel_ingot",
          "Слиток середины мода. Оружие, броня и ключ к Гайе.",
          19.5, 0, "terra_plate", rewards=[("botania:terrasteel_ingot", 2)]),
        Q("terra_pick", "Кирка Терры", "botania:terra_pick",
          "Кирка заряжается маной. На этом этапе уже копает быстрее алмазной.",
          21.0, -1.0, "terrasteel", rewards=[("botania:mana_diamond", 2)]),
        Q("portal", "Портал в Альфхейм", "botania:alfheim_portal",
          "Рамка из живой древесины и натура-пилонов. Открывается зарядом маны.",
          21.0, 1.0, "terrasteel", rewards=[("botania:elementium_ingot", 4)]),
        Q("elementium", "Элементий", "botania:elementium_ingot",
          "Металл Альфхейма. Обмен через портал: бросай ресурсы в воронку живой древесины.",
          22.5, 1.0, "portal", count=4, rewards=[("botania:elementium_ingot", 8)]),
        Q("gaia_pylon", "Пилон Гайи", "botania:gaia_pylon",
          "Четыре пилона вокруг маяка. Призыв стража Гайи — проверка на готовность.",
          24.0, 0, "elementium", rewards=[("botania:pixie_dust", 8)]),
        Q("gaia", "Слиток Гайи", "botania:gaia_ingot",
          "Дроп с стража. Финал обычной ветки Botania до реликвий.",
          25.5, 0, "gaia_pylon", rewards=[("botania:gaia_ingot", 1)]),
        Q("mech_daisy", "Механический чистоцвет", "botanicalmachinery:mechanical_daisy",
          "Botanical Machinery: чистоцвет без ожидания. Ставь после живой древесины, когда надоело стоять у цветка.",
          9.0, 2.5, "livingwood", rewards=[("botania:livingwood", 16)]),
        Q("mech_apothecary", "Механический алтарь лепестков", "botanicalmachinery:mechanical_apothecary",
          "Авто-алтарь. Кидает лепестки сам, если подать воду и предметы трубами.",
          10.5, 2.5, "mech_daisy", rewards=[(p, 32)]),
        Q("mech_altar", "Механический рунический алтарь", "botanicalmachinery:mechanical_runic_altar",
          "Руны без ручной раскладки. Нужен, когда рунный крафт идёт пачками.",
          12.0, 2.5, "mech_apothecary", rewards=[("botania:rune_mana", 4)]),
        Q("mech_pool", "Механический бассейн", "botanicalmachinery:mechanical_mana_pool",
          "Бассейн с авто-вводом. Связка с инфузером и батареей маны.",
          13.5, 2.5, "mech_altar", rewards=[(ms, 8)]),
        Q("mech_infuser", "Механический инфузер", "botanicalmachinery:mechanical_mana_infuser",
          "Льёт ману в предметы конвейером. Замена ручному бросанию слитков в бассейн.",
          15.0, 2.5, "mech_pool", rewards=[(ms, 8)]),
        Q("mech_brewery", "Механическая пивоварня", "botanicalmachinery:mechanical_brewery",
          "Варит зелья Botania без стояния у котла.",
          16.5, 2.5, "mech_infuser", rewards=[("minecraft:glass_bottle", 16)]),
        Q("agglo", "Фабрика агломерации", "botanicalmachinery:industrial_agglomeration_factory",
          "Террасталь пачками. Жрёт ману — поставь батарею рядом.",
          18.0, 2.5, "mech_brewery", rewards=[("botania:terrasteel_ingot", 2)]),
        Q("market", "Рынок Альфхейма", "botanicalmachinery:alfheim_market",
          "Авто-обмен портала. Кидай ресурсы, забирай элементий без беготни к воронке.",
          19.5, 2.5, "agglo", rewards=[("botania:elementium_ingot", 8)]),
        Q("mana_bat", "Батарея маны", "botanicalmachinery:mana_battery",
          "Буфер маны для механических станков. Без неё фабрика агломерации голодает.",
          21.0, 2.5, "market", rewards=[(ms, 8)]),
        Q("base_daisy", "Базовый механический чистоцвет", "botanicalextramachinery:base_daisy",
          "Extra Machinery, тир 1. Быстрее ванильного чистоцвета, слабее ultimate.",
          10.5, 4.0, "mech_daisy", rewards=[("botania:livingrock", 16)]),
        Q("adv_daisy", "Продвинутый чистоцвет", "botanicalextramachinery:advanced_daisy",
          "Второй тир Extra Machinery. Имеет смысл, когда живой камень идёт стаками.",
          12.0, 4.0, "base_daisy", rewards=[("botania:livingrock", 32)]),
        Q("base_pool", "Базовый механический бассейн", "botanicalextramachinery:base_mana_pool",
          "Тир 1 бассейна Extra Machinery. Ставь до advanced, не прыгай сразу в ultimate.",
          13.5, 4.0, "mech_pool", rewards=[(ms, 8)]),
        Q("greenhouse", "Теплица", "botanicalextramachinery:greenhouse",
          "Растит цветы Botania без ручной фермы. Апгрейды слотов и тепла ставятся внутрь.",
          15.0, 4.0, "base_pool", rewards=[(p, 32)]),
        Q("mb_infuser", "Инфузер MythicBotany", "mythicbotany:mana_infuser",
          "MythicBotany: инфузия для альфстали. Открывается после портала в Альфхейм.",
          22.5, -1.5, "portal", rewards=[("botania:elementium_ingot", 4)]),
        Q("mb_collector", "Коллектор маны", "mythicbotany:mana_collector",
          "Собирает ману с цветов на большей площади, чем обычный спредер.",
          24.0, -1.5, "mb_infuser", rewards=[(ms, 8)]),
        Q("alf_pylon", "Пилон альфстали", "mythicbotany:alfsteel_pylon",
          "Пилон MythicBotany. Нужен для крафта альфстали и части рун миров.",
          25.5, -1.5, "mb_collector", rewards=[("mythicbotany:alfsteel_nugget", 8)]),
        Q("alf_ingot", "Альфсталь", "mythicbotany:alfsteel_ingot",
          "Металл поверх террастали. Кирка и броня MythicBotany собираются из него.",
          27.0, -1.5, "alf_pylon", rewards=[("mythicbotany:alfsteel_nugget", 16)]),
        Q("alf_pick", "Кирка альфстали", "mythicbotany:alfsteel_pick",
          "Кирка MythicBotany. Заряжается маной, копает быстрее террастальной.",
          28.5, -1.5, "alf_ingot", rewards=[("mythicbotany:alfsteel_nugget", 8)]),
        Q("asgard", "Руна Асгарда", "mythicbotany:asgard_rune",
          "Одна из рун девяти миров. Дальше из них собирают Мьёльнир и реликвии.",
          27.0, 0, "alf_ingot", rewards=[("mythicbotany:alfsteel_nugget", 4)]),
    ]


def alex_quests() -> list[Q]:
    return [
        Q("tablet", "Пещерная табличка", "alexscaves:cave_tablet",
          "Находка в сундуках и от мобов пещер. Без неё карта биома не собирается.",
          0, 0, rewards=[("alexscaves:cave_tablet", 1)]),
        Q("codex", "Кодекс пещер", "alexscaves:cave_codex",
          "Расшифрованная табличка. Открывает рецепты и описание биома.",
          1.5, 0, "tablet", rewards=[("alexscaves:amber", 8)]),
        Q("book", "Книга пещер", "alexscaves:cave_book",
          "Сводка по всем биомам Alex's Caves. Держи под рукой, пока ищешь входы.",
          3.0, 0, "codex", rewards=[("minecraft:map", 4)]),
        Q("neo", "Алый неодим", "alexscaves:scarlet_neodymium_ingot",
          "Магнитные пещеры. Неодим — основа перчаток, рельс и Теслы.",
          4.5, -3.0, "book", count=4, rewards=[("alexscaves:scarlet_neodymium_ingot", 8)]),
        Q("tesla", "Лампа Теслы", "alexscaves:tesla_bulb",
          "Сердце магнитного биома. Бьёт молнией и кормит галена-перчатку.",
          6.0, -3.5, "neo", rewards=[("alexscaves:azure_neodymium_ingot", 4)]),
        Q("gauntlet", "Галена-перчатка", "alexscaves:galena_gauntlet",
          "Тянет и швыряет металлические блоки. Главный инструмент магнитной ветки.",
          7.5, -3.0, "tesla", rewards=[("alexscaves:tesla_bulb", 1)]),
        Q("gizmo", "Нотор-гизмо", "alexscaves:notor_gizmo",
          "Гаджет магнитных пещер. Нужен для части крафтов с галеной и активатором.",
          9.0, -3.5, "gauntlet", rewards=[("alexscaves:scarlet_neodymium_ingot", 4)]),
        Q("activator", "Магнитный активатор", "alexscaves:magnetic_activator",
          "Включает магнитные блоки и рельсы левитации.",
          9.0, -2.5, "gauntlet", rewards=[("alexscaves:magnetic_levitation_rail", 8)]),
        Q("shield", "Резисторный щит", "alexscaves:resistor_shield",
          "Щит магнитной ветки. Держит удар и часть электрических атак биома.",
          10.5, -3.0, "gizmo", rewards=[("alexscaves:heavyweight", 1)]),
        Q("sulfur", "Серный порошок", "alexscaves:sulfur_dust",
          "Токсичные пещеры. Сера сыпется с наростов на потолке.",
          4.5, -1.0, "book", count=8, rewards=[("alexscaves:sulfur_dust", 16)]),
        Q("radon", "Бутылка радона", "alexscaves:radon_bottle",
          "Собери газ в бутылку. Нужен для ламп и части токсичных крафтов.",
          6.0, -1.0, "sulfur", rewards=[("alexscaves:radon_bottle", 4)]),
        Q("hazmat", "Химзащита", "alexscaves:hazmat_chestplate",
          "Без костюма радиация токсичных пещер съедает здоровье.",
          7.5, -1.0, "radon", rewards=[("alexscaves:uranium", 8)]),
        Q("uranium", "Уран", "alexscaves:uranium",
          "Руда токсичных пещер. Идёт в стержни и печь.",
          9.0, -1.5, "hazmat", count=8, rewards=[("alexscaves:uranium", 16)]),
        Q("u_rod", "Урановый стержень", "alexscaves:uranium_rod",
          "Топливо ядерной ветки биома. Не носи в кармане без химзащиты.",
          9.0, -0.5, "uranium", rewards=[("alexscaves:nuclear_siren", 1)]),
        Q("raygun", "Радиационный луч", "alexscaves:raygun",
          "Оружие токсичных пещер. Садит мобов и оставляет радиацию.",
          10.5, -1.0, "u_rod", rewards=[("alexscaves:uranium_rod", 2)]),
        Q("amber", "Янтарь", "alexscaves:amber",
          "Первобытные пещеры. Янтарь и амберсол — свет и крафт копья.",
          4.5, 1.0, "book", count=8, rewards=[("alexscaves:amber", 16)]),
        Q("ambersol", "Амберсол", "alexscaves:ambersol",
          "Светящийся янтарь. Ставь как лампу в тёмных залах биома.",
          6.0, 0.2, "amber", rewards=[("alexscaves:ambersol", 8)]),
        Q("spear", "Известняковое копьё", "alexscaves:limestone_spear",
          "Метательное оружие биома. Легко крафтится на месте из известняка.",
          6.0, 1.0, "amber", rewards=[("alexscaves:limestone_spear", 2)]),
        Q("soup", "Первобытный суп", "alexscaves:primordial_soup",
          "Еда биома. Варится из местной живности и растений.",
          6.0, 1.8, "amber", rewards=[("alexscaves:dinosaur_nugget", 8)]),
        Q("tunic", "Первобытная туника", "alexscaves:primordial_tunic",
          "Броня динозавров. Дешёвая защита, пока нет алмазов.",
          7.5, 1.0, "spear", rewards=[("alexscaves:primordial_soup", 4)]),
        Q("pearl", "Жемчуг бездны", "alexscaves:pearl",
          "Затонувший биом. Жемчуг падает с моллюсков и идёт в посох моря.",
          4.5, 3.0, "book", count=4, rewards=[("alexscaves:pearl", 8)]),
        Q("staff", "Морской посох", "alexscaves:sea_staff",
          "Оружие бездны. Держит дистанцию против стражей глубин.",
          6.0, 2.5, "pearl", rewards=[("alexscaves:depth_charge", 4)]),
        Q("gaze", "Созерцающий жемчуг", "alexscaves:gazing_pearl",
          "Редкий жемчуг. Нужен для поздних крафтов бездны.",
          7.5, 3.0, "staff", rewards=[("alexscaves:pearl", 8)]),
        Q("charge", "Глубинная бомба", "alexscaves:depth_charge",
          "Взрывчатка бездны. Кидай в воду — тонет и бахает.",
          9.0, 3.0, "gaze", rewards=[("alexscaves:depth_charge", 8)]),
        Q("caramel", "Карамель", "alexscaves:caramel",
          "Кондитерские пещеры. Карамель — базовый ресурс сладкого биома.",
          4.5, 4.5, "book", count=8, rewards=[("alexscaves:caramel", 16)]),
        Q("cane", "Карамельная трость", "alexscaves:candy_cane",
          "Оружие и блок биома. Из тростей собирают крюки и столбы.",
          6.0, 4.5, "caramel", rewards=[("alexscaves:candy_cane", 8)]),
        Q("soda", "Фиолетовая сода", "alexscaves:purple_soda_bottle",
          "Жидкость биома в бутылке. Из неё делают ракеты и ведро соды.",
          7.5, 4.5, "cane", rewards=[("alexscaves:purple_soda_bottle", 4)]),
        Q("mint", "Мятное копьё", "alexscaves:frostmint_spear",
          "Холодное оружие сладкого биома. Кидается дальше трости.",
          9.0, 4.5, "soda", rewards=[("alexscaves:frostmint", 8)]),
        Q("darkness", "Чистая тьма", "alexscaves:pure_darkness",
          "Забытые пещеры. Дроп с босса и редких мобов. Сырьё плаща тьмы.",
          4.5, 6.0, "book", rewards=[("alexscaves:moth_ball", 8)]),
        Q("hood", "Капюшон тьмы", "alexscaves:hood_of_darkness",
          "Шлем ветки тьмы. Вместе с плащом закрывает сет тьмы.",
          6.0, 6.0, "darkness", rewards=[("alexscaves:vesper_wing", 4)]),
        Q("cloak", "Плащ тьмы", "alexscaves:cloak_of_darkness",
          "Накидка забытого биома. Носится с капюшоном.",
          7.5, 5.5, "hood", rewards=[("alexscaves:occult_gem", 1)]),
        Q("occult", "Оккультный камень", "alexscaves:occult_gem",
          "Редкий дроп тьмы. Идёт в клинок и тотем.",
          7.5, 6.5, "hood", rewards=[("alexscaves:moth_dust", 8)]),
        Q("dagger", "Клинок запустения", "alexscaves:desolate_dagger",
          "Финал пещерной книги. Оружие забытого биома.",
          9.0, 6.0, ["cloak", "occult"], rewards=[("alexscaves:totem_of_possession", 1)]),
    ]


def ae2_quests() -> list[Q]:
    c1 = "ae2:item_storage_cell_1k"
    fluix = "ae2:fluix_crystal"
    return [
        Q("guide", "Гайд AE2", "ae2:guide",
          "Книга сети. Держи под рукой, пока собираешь инскрайбер и кабели.",
          0, 0, rewards=[("ae2:certus_quartz_crystal", 16)]),
        Q("certus", "Кварц Цертус", "ae2:certus_quartz_crystal",
          "Метеориты AE2 или рост на бутоне. Без цертуса нет флюикса и процессоров.",
          1.5, 0, "guide", count=8, rewards=[("ae2:certus_quartz_crystal", 16)]),
        Q("charged", "Заряженный цертус", "ae2:charged_certus_quartz_crystal",
          "Зарядка в заряднике. Нужен для флюикса и ускорителя роста.",
          3.0, -1.0, "certus", count=4, rewards=[("ae2:charged_certus_quartz_crystal", 8)]),
        Q("sky", "Небесный камень", "ae2:sky_stone_block",
          "Дроп с метеорита. Идёт в сундук, танк и гладкие блоки сети.",
          3.0, 1.0, "certus", count=8, rewards=[("ae2:sky_stone_block", 16)]),
        Q("fluix", "Кристалл флюикса", "ae2:fluix_crystal",
          "Цертус + редстоун + незер-кварц в мире или росте. Основа кабелей и ядер.",
          4.5, 0, ["charged", "sky"], count=8, rewards=[(fluix, 16)]),
        Q("silicon", "Кремний AE2", "ae2:silicon",
          "Плавь пыль цертуса в печи. Печатный кремний без него не сделать.",
          6.0, 0, "fluix", count=8, rewards=[("ae2:silicon", 16)]),
        Q("press", "Пресс кремния", "ae2:silicon_press",
          "Пресс из метеорита. Без прессов инскрайбер пустой.",
          6.0, -1.5, "silicon", rewards=[("ae2:logic_processor_press", 1)]),
        Q("inscriber", "Инскрайбер", "ae2:inscriber",
          "Печатает кремний и три процессора. Ставь рядом с зарядником.",
          7.5, 0, "press", rewards=[("ae2:printed_silicon", 8)]),
        Q("charger", "Зарядник", "ae2:charger",
          "Заряжает цертус. Питается от любой FE-сети, не только ME.",
          7.5, -1.5, "inscriber", rewards=[("ae2:charged_certus_quartz_crystal", 8)]),
        Q("printed", "Печатный кремний", "ae2:printed_silicon",
          "Заготовка процессора. Дальше логика, расчёт и инженерия.",
          9.0, 0, "inscriber", count=4, rewards=[("ae2:silicon", 16)]),
        Q("logic", "Логический процессор", "ae2:logic_processor",
          "Золото + кремний в инскрайбере. Кабели, шины, терминал.",
          10.5, -1.0, "printed", count=4, rewards=[("ae2:logic_processor", 8)]),
        Q("calc", "Расчётный процессор", "ae2:calculation_processor",
          "Цертус + кремний. Ячейки и компоненты памяти.",
          10.5, 0, "printed", count=4, rewards=[("ae2:calculation_processor", 8)]),
        Q("eng", "Инженерный процессор", "ae2:engineering_processor",
          "Алмаз + кремний. Контроллер, сборщик, плотные кабели.",
          10.5, 1.0, "printed", count=4, rewards=[("ae2:engineering_processor", 8)]),
        Q("fiber", "Кварцевое волокно", "ae2:quartz_fiber",
          "Мост между сетями и канал в стеклянном кабеле.",
          12.0, -1.0, "fluix", count=4, rewards=[("ae2:quartz_fiber", 8)]),
        Q("cable", "Стеклянный кабель флюикса", "ae2:fluix_glass_cable",
          "Каналы ME. Восемь штук — уже хватает на маленький остров.",
          12.0, 0, ["logic", "fiber"], count=8, rewards=[("ae2:fluix_glass_cable", 16)]),
        Q("ntool", "Сетевой инструмент", "ae2:network_tool",
          "ПКМ по кабелю: каналы, энергия, привязка. Без него сеть слепая.",
          12.0, 1.0, "cable", rewards=[("ae2:fluix_glass_cable", 8)]),
        Q("acceptor", "Приёмник энергии", "ae2:energy_acceptor",
          "Вход FE в ME. Первая живая сеть — акцептор + кабель + привод.",
          13.5, 0, "cable", rewards=[("ae2:energy_cell", 1)]),
        Q("energy", "Энергоячейка", "ae2:energy_cell",
          "Буфер сети. Ставь до контроллера, иначе шины моргают.",
          15.0, 0, "acceptor", rewards=[("ae2:fluix_glass_cable", 8)]),
        Q("drive", "ME-привод", "ae2:drive",
          "Десять слотов под ячейки. Сердце склада.",
          16.5, 0, "energy", rewards=[("ae2:item_cell_housing", 2)]),
        Q("housing", "Корпус ячейки", "ae2:item_cell_housing",
          "Пустой корпус. Компонент 1k + корпус = ячейка.",
          18.0, 0, "drive", count=2, rewards=[("ae2:cell_component_1k", 2)]),
        Q("comp1k", "Компонент 1k", "ae2:cell_component_1k",
          "Память ячейки. Расчётный процессор и редстоун.",
          19.5, 0, ["housing", "calc"], count=2, rewards=[(c1, 2)]),
        Q("cell1k", "Ячейка 1k", "ae2:item_storage_cell_1k",
          "Первый склад ME. Форматируется в приводе под типы предметов.",
          21.0, 0, "comp1k", rewards=[("ae2:terminal", 1)]),
        Q("terminal", "Терминал", "ae2:terminal",
          "Окно склада. Ставь на кабель рядом с приводом.",
          22.5, 0, "cell1k", rewards=[("ae2:import_bus", 2)]),
        Q("ibus", "Шина импорта", "ae2:import_bus",
          "Засасывает сундук в ME. Фильтры — позже картами.",
          22.5, 1.5, "terminal", rewards=[("ae2:export_bus", 2)]),
        Q("ebus", "Шина экспорта", "ae2:export_bus",
          "Выплёвывает предметы в сундук или машину IU.",
          24.0, 1.5, "ibus", rewards=[("ae2:storage_bus", 2)]),
        Q("sbus", "Шина хранения", "ae2:storage_bus",
          "Подключает внешний сундук как часть ME без перекладывания в ячейки.",
          25.5, 1.5, "ebus", rewards=[("ae2:interface", 1)]),
        Q("iface", "Интерфейс", "ae2:interface",
          "Мост автокрафта и машин. Сюда сажают провайдер паттернов.",
          24.0, 2.5, "sbus", rewards=[("ae2:io_port", 1)]),
        Q("ioport", "IO-порт", "ae2:io_port",
          "Копирует ячейку в сеть и обратно. Удобно возить островной лут.",
          25.5, 2.5, "iface", rewards=[("ae2:item_storage_cell_4k", 1)]),
        Q("controller", "ME-контроллер", "ae2:controller",
          "32 канала на грань. Ставь, когда стеклянных кабелей уже мало.",
          24.0, 0, ["terminal", "eng"], rewards=[("ae2:fluix_smart_cable", 8)]),
        Q("smart", "Умный кабель", "ae2:fluix_smart_cable",
          "Показывает каналы цветом. Дальше плотный кабель на контроллер.",
          25.5, 0, "controller", count=8, rewards=[("ae2:fluix_covered_dense_cable", 4)]),
        Q("dense", "Плотная энергоячейка", "ae2:dense_energy_cell",
          "Большой буфер. Нужна, когда автокрафт и шины жрут энергию пачками.",
          16.5, 1.5, "energy", rewards=[("ae2:energy_cell", 2)]),
        Q("growth", "Ускоритель роста", "ae2:growth_accelerator",
          "Растит бутоны цертуса. Свой кварц без беготни по метеоритам.",
          4.5, -1.5, "charger", rewards=[("ae2:charged_certus_quartz_crystal", 8)]),
        Q("cell4k", "Ячейка 4k", "ae2:item_storage_cell_4k",
          "Следующий тир склада. Крафт из четырёх 1k-компонентов.",
          22.5, -1.5, "cell1k", rewards=[("ae2:cell_component_16k", 1)]),
        Q("cell16k", "Ячейка 16k", "ae2:item_storage_cell_16k",
          "Средний склад. Хватает на остров до 64k.",
          24.0, -1.5, "cell4k", rewards=[("ae2:item_storage_cell_16k", 1)]),
        Q("pprov", "Провайдер паттернов", "ae2:pattern_provider",
          "Автокрафт: паттерн внутрь, интерфейс к машине.",
          25.5, -1.5, "controller", rewards=[("ae2:blank_pattern", 8)]),
        Q("pattern", "Пустой паттерн", "ae2:blank_pattern",
          "Кодируется в терминале крафта. Без стопки автокрафт мёртв.",
          27.0, -1.5, "pprov", count=8, rewards=[("ae2:blank_pattern", 16)]),
        Q("assembler", "Молекулярный сборщик", "ae2:molecular_assembler",
          "Крафтит по паттерну. Ставь рядом с провайдером.",
          25.5, -2.5, "pprov", rewards=[("ae2:crafting_terminal", 1)]),
        Q("cterm", "Терминал крафта", "ae2:crafting_terminal",
          "Верстак + склад в одном окне. Кодирует паттерны.",
          27.0, -2.5, "assembler", rewards=[("ae2:crafting_unit", 2)]),
        Q("cunit", "Блок крафта", "ae2:crafting_unit",
          "Каркас CPU автокрафта. Дальше вешается хранилище 1k.",
          28.5, -2.5, "cterm", count=4, rewards=[("ae2:1k_crafting_storage", 1)]),
        Q("cstor", "Хранилище крафта 1k", "ae2:1k_crafting_storage",
          "Память CPU. Без него молекулярный сборщик не берёт заказы.",
          30.0, -2.5, "cunit", rewards=[("ae2:crafting_accelerator", 1)]),
        Q("fluid1k", "Жидкостная ячейка 1k", "ae2:fluid_storage_cell_1k",
          "Склад жидкостей ME. Пар, лава, латекс IU — сюда, не в вёдра.",
          21.0, 1.5, "cell1k", rewards=[("ae2:fluid_storage_cell_1k", 1)]),
        Q("cell64k", "Ячейка 64k", "ae2:item_storage_cell_64k",
          "Финал вкладки. Склад под остров и автокрафт без постоянной чистки.",
          27.0, 0, ["smart", "cell16k"], rewards=[("ae2:item_storage_cell_64k", 1), ("ae2:fluid_storage_cell_64k", 1)]),
    ]


def avaritia_quests() -> list[Q]:
    nn = "avaritia:neutron_nugget"
    dia = "minecraft:diamond"
    return [
        Q("lattice", "Алмазная решётка", "avaritia:diamond_lattice",
          "Старт Avaritia. Крафтится из алмазов. Идёт в кристаллическую матрицу.",
          0, 0, count=4, rewards=[(dia, 8)]),
        Q("matrix", "Кристаллическая матрица", "avaritia:crystal_matrix_ingot",
          "Слиток каркаса. Без него не собрать сжатый верстак и коллектор нейтрония.",
          1.5, 0, "lattice", rewards=[("avaritia:crystal_matrix_ingot", 2)]),
        Q("table1", "Сжатый верстак", "avaritia:compressed_crafting_table",
          "3×3 верстаков в одном блоке. Промежуточный стол до экстремального.",
          3.0, -1.0, "matrix", rewards=[(dia, 8)]),
        Q("table2", "Двойной сжатый верстак", "avaritia:double_compressed_crafting_table",
          "Следующее сжатие. Нужен, чтобы скрафтить стол 9×9.",
          4.5, -1.0, "table1", rewards=[(dia, 8)]),
        Q("nether_t", "Адский верстак", "avaritia:nether_crafting_table",
          "Верстак измерения Нижнего мира. Параллельная ветка сжатия.",
          4.5, -2.0, "table1", rewards=[("minecraft:netherite_ingot", 1)]),
        Q("sculk_t", "Скалк-верстак", "avaritia:sculk_crafting_table",
          "Верстак дип-дарка. Крафт из скалка и сжатого стола.",
          6.0, -2.0, "nether_t", rewards=[("minecraft:echo_shard", 4)]),
        Q("end_t", "Эндер-верстак", "avaritia:end_crafting_table",
          "Верстак Края. Последний из размерных столов до экстремального.",
          7.5, -2.0, "sculk_t", rewards=[("minecraft:ender_pearl", 16)]),
        Q("extreme", "Экстремальный верстак", "avaritia:extreme_crafting_table",
          "Сетка 9×9. Здесь собирают нейтроний, сингулярности и Infinity.",
          6.0, -1.0, "table2", rewards=[("avaritia:crystal_matrix_ingot", 2)]),
        Q("anvil", "Экстремальная наковальня", "avaritia:extreme_anvil",
          "Наковальня 9×9-ветки. Чинит то, что ванильная уже не берёт.",
          7.5, -1.0, "extreme", rewards=[("minecraft:anvil", 1)]),
        Q("smith", "Экстремальный стол кузнеца", "avaritia:extreme_smithing_table",
          "Кузнечный стол Avaritia. Нужен для части брони аддона.",
          9.0, -1.0, "anvil", rewards=[("avaritia:upgrade_smithing_template", 1)]),
        Q("collector", "Коллектор нейтрония", "avaritia:neutron_collector",
          "Пассивно сыпет кучи. Чем плотнее коллектор, тем быстрее.",
          3.0, 1.0, "matrix", rewards=[("avaritia:neutron_pile", 16)]),
        Q("pile", "Куча нейтрония", "avaritia:neutron_pile",
          "Пыль из коллектора. Копится долго — поставь коллектор и жди.",
          4.5, 1.0, "collector", count=8, rewards=[("avaritia:neutron_pile", 32)]),
        Q("nugget", "Самородки нейтрония", "avaritia:neutron_nugget",
          "Сжатие куч. Девять куч = самородок.",
          6.0, 1.0, "pile", count=9, rewards=[(nn, 9)]),
        Q("ingot", "Слиток нейтрония", "avaritia:neutron_ingot",
          "Основной металл конца. Идёт в компрессор и катализатор Infinity.",
          7.5, 1.0, "nugget", rewards=[(nn, 16)]),
        Q("gear", "Шестерня нейтрония", "avaritia:neutron_gear",
          "Механическая деталь компрессора и части станков.",
          9.0, 1.5, "ingot", rewards=[(nn, 8)]),
        Q("fuel", "Звёздное топливо", "avaritia:star_fuel",
          "Топливо конца. Горит дольше угля, нужно в части 9×9 рецептов.",
          9.0, 0.5, "ingot", rewards=[("avaritia:refined_coal", 16)]),
        Q("compressor", "Компрессор нейтрония", "avaritia:neutron_compressor",
          "Жмёт стаки предметов в сингулярности. Без него катализатор не собрать.",
          10.5, 0, ["ingot", "extreme"], rewards=[(nn, 16)]),
        Q("singularity", "Сингулярность", "avaritia:singularity",
          "Сжатый стак на компрессоре. Набирай разные типы под катализатор.",
          12.0, 1.0, "compressor", rewards=[(nn, 8)]),
        Q("catalyst", "Катализатор Infinity", "avaritia:infinity_catalyst",
          "Смесь сингулярностей и нейтрония на столе 9×9. Ключ к слитку Infinity.",
          12.0, 0, "singularity", rewards=[(nn, 16)]),
        Q("infinity", "Слиток Infinity", "avaritia:infinity_ingot",
          "Финал крафта. Оружие, броня и админ-панель IU требуют именно его.",
          13.5, 0, "catalyst", rewards=[(nn, 32)]),
        Q("stew", "Абсолютная похлёбка", "avaritia:ultimate_stew",
          "Еда конца. Одна миска закрывает голод надолго.",
          12.0, -1.5, "catalyst", rewards=[("avaritia:cosmic_meatballs", 4)]),
        Q("blaze_cube", "Куб ифрита", "avaritia:blaze_cube",
          "Материал огненной брони аддона. Крафт из стержней искрита.",
          3.0, 2.5, "matrix", count=4, rewards=[("minecraft:blaze_rod", 8)]),
        Q("blaze_helm", "Шлем ифрита", "avaritia_armor:blaze_helmet",
          "Первый кусок огненного сета. Дальше нагрудник и поножи тем же путём.",
          4.5, 2.5, "blaze_cube", rewards=[("avaritia:blaze_cube", 4)]),
        Q("blaze_chest", "Нагрудник ифрита", "avaritia_armor:blaze_chestplate",
          "Торс огненного сета. Не Infinity — можно носить до нейтрония.",
          6.0, 2.5, "blaze_helm", rewards=[("avaritia:blaze_cube", 4)]),
        Q("crystal_core", "Кристаллическое ядро", "avaritia_armor:crystal_core",
          "Ядро кристальной брони. Крафт из матрицы.",
          3.0, 3.5, "matrix", rewards=[("avaritia:crystal_matrix_ingot", 2)]),
        Q("crystal_helm", "Кристальный шлем", "avaritia_armor:crystal_helmet",
          "Шлем кристального сета аддона. Средний тир между алмазом и Infinity.",
          4.5, 3.5, "crystal_core", rewards=[("avaritia:crystal_matrix_ingot", 1)]),
        Q("sword", "Меч Infinity", "avaritia:infinity_sword",
          "Одно попадание. Не носите на хабе без причины.",
          15.0, -1.5, "infinity", rewards=[(nn, 8)]),
        Q("pick", "Кирка Infinity", "avaritia:infinity_pickaxe",
          "Копает целые пласты. Переключается в молот.",
          15.0, -0.5, "infinity", rewards=[(nn, 8)]),
        Q("chest", "Нагрудник Infinity", "avaritia:infinity_chestplate",
          "Кусок сета. Собирается на экстремальном столе.",
          15.0, 0.5, "infinity", rewards=[(nn, 8)]),
        Q("helm", "Шлем Infinity", "avaritia:infinity_helmet",
          "Шлем сета. Крафт 9×9, как остальная броня.",
          15.0, 1.5, "infinity", rewards=[(nn, 8)]),
        Q("pearl", "Эндест-жемчуг", "avaritia:endest_pearl",
          "Гравитационная жемчужина. Редкий крафт ветки конца.",
          16.5, 0.5, "infinity", rewards=[(nn, 8)]),
        Q("box", "Сжатый сундук", "avaritia:compressed_chest",
          "Хранилище под стаки Infinity-крафта. На этом этапе обычных сундуков мало.",
          16.5, -0.5, "infinity", rewards=[("avaritia:compressed_chest", 1)]),
    ]


def secret_quests(ids: dict[str, dict[str, str]]) -> list[Q]:
    bot = ids["botania"]
    caves = ids["alexscaves"]
    ava = ids["avaritia"]
    return [
        Q("steam_stash", "Секрет пара", "industrialupgrade:solar_energy",
          "Открывается после контроллера доменной печи. Ранняя солнечная панель IU и ячейки ME на потом.",
          0, 0, extra_deps=[STEAM_LAST], hide=True, checkmark=True, xp=200,
          rewards=[
              ("industrialupgrade:solar_energy", 1),
              ("ae2:item_storage_cell_1k", 1),
              ("ae2:fluid_storage_cell_1k", 1),
          ]),
        Q("basic_stash", "Секрет электрики", "industrialupgrade:adv_solar_energy",
          "Открывается после радиоактивных отходов. Улучшенная панель и ячейки 4k — запас на химическую ветку.",
          2.0, 0, extra_deps=[BASIC_LAST], hide=True, checkmark=True, xp=350,
          rewards=[
              ("industrialupgrade:adv_solar_energy", 1),
              ("ae2:item_storage_cell_4k", 1),
              ("ae2:fluid_storage_cell_4k", 1),
          ]),
        Q("imp_stash", "Секрет улучшенной эры", "industrialupgrade:machines/advanced_solar_paneliu",
          "Открывается после очистителя почвы. Панель следующего яруса и ячейки 16k.",
          4.0, 0, extra_deps=[IMPROVED_LAST], hide=True, checkmark=True, xp=500,
          rewards=[
              ("industrialupgrade:machines/advanced_solar_paneliu", 1),
              ("ae2:item_storage_cell_16k", 1),
              ("ae2:fluid_storage_cell_16k", 1),
          ]),
        Q("botania_stash", "Секрет цветника", "botania:mana_tablet",
          "Открывается после террастали. Запасная таблица маны и ячейки 16k на эльфийскую ветку.",
          6.0, -1.5, extra_deps=[bot["terrasteel"]], hide=True, checkmark=True, xp=300,
          rewards=[
              ("botania:mana_tablet", 1),
              ("ae2:item_storage_cell_16k", 1),
          ]),
        Q("caves_stash", "Секрет пещер", "alexscaves:tesla_bulb",
          "Открывается после галена-перчатки. Запасная Тесла и ячейки 4k на токсичную ветку.",
          6.0, 1.5, extra_deps=[caves["gauntlet"]], hide=True, checkmark=True, xp=300,
          rewards=[
              ("alexscaves:tesla_bulb", 1),
              ("ae2:item_storage_cell_4k", 1),
              ("ae2:fluid_storage_cell_4k", 1),
          ]),
        Q("ava_stash", "Секрет бесконечности", "ae2:item_storage_cell_256k",
          "Открывается после слитка Infinity. Предметная и жидкостная ячейки 256k — склад под сингулярности.",
          8.0, 0, extra_deps=[ava["infinity"]], hide=True, checkmark=True, xp=800,
          rewards=[
              ("ae2:item_storage_cell_256k", 1),
              ("ae2:fluid_storage_cell_256k", 1),
          ]),
        Q("abyss_stash", "Секрет бездны", "minecraft:echo_shard",
          "Поймай 3 осколка эха. Награда — плотная энергоячейка AE2 и ячейки 64k.",
          2.0, 2.5, extra_deps=[STEAM_LAST], hide=True, xp=400, count=3,
          rewards=[
              ("ae2:item_storage_cell_64k", 1),
              ("ae2:fluid_storage_cell_64k", 1),
              ("ae2:dense_energy_cell", 1),
          ]),
    ]


def patch_steam() -> None:
    repls = [
        ('item: "industrialupgrade:steam_machine"',
         'item: "industrialupgrade:blockresource/steam_machine"'),
        ('item: "industrialupgrade:fluidcoppersulfate"',
         'item: "industrialupgrade:bucket/coppersulfate"'),
    ]
    for folder in (CFG, SRV):
        path = folder / "steam_era.snbt"
        text = path.read_text(encoding="utf-8")
        for a, b in repls:
            if a in text:
                text = text.replace(a, b)
            elif b not in text:
                raise SystemExit(f"steam patch miss {a} in {path}")
        path.write_text(text, encoding="utf-8")


def fix_primitive_dupes() -> None:
    for folder in (CFG, SRV):
        path = folder / "57FF374744F4AC76.snbt"
        raw = path.read_text(encoding="utf-8")
        # Drop consecutive duplicate description blocks.
        cleaned = re.sub(
            r"(			description: \[[^\]]*\]\n)\1",
            r"\1",
            raw,
        )
        path.write_text(cleaned, encoding="utf-8")


def write_endgame(ava_infinity_id: str, models: set[str]) -> None:
    quests = [
        Q("alpha", "Альфа-рыбак", "starcatcher:alpha_rod",
          "Добудь Alpha Rod — верх рыбацкой линейки StarCatcher. Без неё финальная панель IU не собирается.",
          -2.0, 1.0, xp=800,
          rewards=[
              ("aquatech_ui:rate_x64", 2),
              ("ae2:item_storage_cell_256k", 2),
          ]),
        Q("stars", "Звёзды из сети", "minecraft:nether_star",
          "Пять звёзд Нижнего мира. На AquaTech их ловят удочкой, а не фармят с визер-фермы.",
          2.0, 1.0, count=5, xp=800,
          rewards=[
              ("avaritia:crystal_matrix_ingot", 4),
              ("ae2:fluid_storage_cell_256k", 2),
          ]),
        Q("infinity_gate", "Infinity на столе", "avaritia:infinity_ingot",
          "Слиток Infinity с экстремального стола. Этот квест закрывается вместе с вкладкой Avaritia.",
          0.0, 2.0, extra_deps=[ava_infinity_id], xp=1000,
          rewards=[("ae2:cell_component_256k", 2)]),
        Q("admin", "Админ-панель", "industrialupgrade:admpanel/admpanel",
          "Финал AquaTech. Admin Solar Panel на столе 9×9: Infinity, сингулярность, Rate x64, Alpha Rod и фотонная панель.",
          0.0, 3.5, extra_deps=[END_ALPHA, END_STARS], xp=2500,
          rewards=[
              ("industrialupgrade:machines/photonic_solar_panel", 1),
              ("avaritia:compressed_chest", 1),
              ("ae2:item_storage_cell_256k", 2),
              ("ae2:fluid_storage_cell_256k", 2),
          ]),
    ]
    # Keep existing IDs for the first two + admin so progress stays.
    name_to_id = {
        "alpha": END_ALPHA,
        "stars": END_STARS,
        "infinity_gate": hid("endgame_infinity_gate"),
        "admin": END_ADMIN,
    }
    quests[2].extra_deps = [ava_infinity_id]
    quests[3].extra_deps = [END_ALPHA, END_STARS, name_to_id["infinity_gate"]]

    for q in quests:
        if q.item and not q.item.startswith("minecraft:") and q.item not in models:
            raise SystemExit(f"endgame bad {q.name} {q.item}")
        for it, _c in q.rewards:
            if it not in models:
                raise SystemExit(f"endgame reward bad {it}")

    lines = [
        "{",
        "	default_hide_dependency_lines: false",
        '	default_quest_shape: ""',
        '	filename: "endgame_aquatech"',
        '	group: ""',
        '	icon: "industrialupgrade:admpanel/admpanel"',
        f'	id: "{CH_END}"',
        "	order_index: 10",
        "	quest_links: [ ]",
        '	subtitle: "Финал. Alpha Rod, Infinity и Admin Solar Panel."',
        "	quests: [",
    ]
    for q in quests:
        qid = name_to_id[q.name]
        deps = list(q.extra_deps)
        lines.append("		{")
        if deps:
            joined = ", ".join(f'"{d}"' for d in deps)
            lines.append(f"			dependencies: [{joined}]")
        lines.append("			description: [")
        for dl in wrap(q.desc):
            lines.append(f'				"{esc(dl)}"')
        lines.append("			]")
        lines.append(f'			id: "{qid}"')
        lines.append(f'			icon: "{q.item}"')
        reward_bits = []
        for i, (it, cnt) in enumerate(q.rewards):
            reward_bits.append(_format_item(qid, it, cnt, i))
        rid_xp = hid(f"{qid}_xp")
        reward_bits.append(
            "{\n"
            f'				id: "{rid_xp}"\n'
            '				type: "xp"\n'
            f"				xp: {q.xp}\n"
            "			}"
        )
        lines.append("			rewards: [" + ",".join(reward_bits) + "]")
        tid = hid(f"{qid}_task")
        cnt = f"\n				count: {q.count}L" if q.count > 1 else ""
        lines.append("			tasks: [{")
        lines.append(f'				id: "{tid}"{cnt}')
        lines.append(f'				item: "{q.item}"')
        lines.append('				type: "item"')
        lines.append("			}]")
        lines.append(f'			title: "{esc(q.title)}"')
        lines.append(f"			x: {q.x:.1f}d")
        lines.append(f"			y: {q.y:.1f}d")
        lines.append("		}")
    lines.append("	]")
    lines.append('	title: "Эндгейм"')
    lines.append("}")
    text = "\n".join(lines) + "\n"
    for dest in (CFG, SRV):
        (dest / "endgame_aquatech.snbt").write_text(text, encoding="utf-8")


def main() -> None:
    models = load_models()
    patch_steam()
    fix_primitive_dupes()

    bq = botania_quests()
    bot_ids = write_chapter(
        "botania_aquatech",
        hid("chapter_botania"),
        "Botania",
        "botania:lexicon",
        6,
        bq,
        models,
        subtitle="Цветы, мана, Терра, Альфхейм, Гайя.",
    )
    cave_ids = write_chapter(
        "alexscaves_aquatech",
        hid("chapter_alexscaves"),
        "Alex's Caves",
        "alexscaves:cave_tablet",
        7,
        alex_quests(),
        models,
        subtitle="Шесть пещер: магнит, токсин, динозавры, бездна, сладости, тьма.",
    )
    ava_ids = write_chapter(
        "avaritia_aquatech",
        hid("chapter_avaritia"),
        "Avaritia",
        "avaritia:infinity_ingot",
        8,
        avaritia_quests(),
        models,
        subtitle="Решётка, нейтроний, стол 9×9, Infinity.",
    )

    ids = {
        "botania": bot_ids,
        "alexscaves": cave_ids,
        "avaritia": ava_ids,
    }

    # abyss secret uses echo shard as task item
    secrets = secret_quests(ids)
    write_chapter(
        "secret_aquatech",
        CH_SECRET,
        "???: Тайны",
        "minecraft:heart_of_the_sea",
        9,
        secrets,
        models,
        subtitle="Квест из обычной вкладки открывает скрытый. Награды: панели, машины, ячейки ME.",
        id_prefix="secret_aquatech",
    )

    write_endgame(ava_ids["infinity"], models)
    aq = ae2_quests()
    ae_ids = write_chapter(
        "ae2_aquatech",
        hid("chapter_ae2"),
        "Applied Energistics",
        "ae2:controller",
        11,
        aq,
        models,
        subtitle="Кварц, инскрайбер, ME-сеть, ячейки, автокрафт.",
    )
    apply_all(CFG, SRV, models)
    print(
        "botania",
        len(bq),
        "caves",
        len(cave_ids),
        "ae2",
        len(ae_ids),
        "avaritia",
        len(ava_ids),
        "secrets",
        len(secrets),
    )
    print("wrote chapters to config/ and server/config/")


if __name__ == "__main__":
    main()
