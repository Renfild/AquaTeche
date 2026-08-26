# -*- coding: utf-8 -*-
"""
Add Botania / Alex's Caves / Avaritia tabs, rewrite secret + endgame.
Patch broken IU steam item IDs. Every item is checked against jar models.
"""
from __future__ import annotations

import hashlib
import re
import shutil
import zipfile
from pathlib import Path

ROOT = Path(r"C:/Users/xieto/Desktop/AquaTech")
MODS = ROOT / "server" / "mods"
CFG = ROOT / "config" / "ftbquests" / "quests" / "chapters"
SRV = ROOT / "server" / "config" / "ftbquests" / "quests" / "chapters"

JARS = {
    "industrialupgrade": "IndustrialUpgrade-1.20.1-3.4.0.11.jar",
    "ae2": "appliedenergistics2-forge-15.4.10.jar",
    "botania": "Botania-1.20.1-454-FORGE.jar",
    "avaritia": "Re-Avaritia-forge-1.20.1-1.4.1-release.jar",
    "alexscaves": "alexscaves-2.0.2.jar",
    "aquatech_ui": "aquatech_ui-1.0.30.jar",
    "starcatcher": "starcatcher-2.3.19-FORGE-1.20.1.jar",
}

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
        self.rewards = rewards or []
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
        for it, cnt in q.rewards:
            rid = hid(f"{qid}_{it}_r")
            extra = f"\n				count: {cnt}" if cnt > 1 else ""
            reward_bits.append(
                "{\n"
                f'				id: "{rid}"{extra}\n'
                f'				item: "{it}"\n'
                '				type: "item"\n'
                "			}"
            )
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
    return [
        Q("lexicon", "Лексика Ботании", "botania:lexicon",
          "Открой книгу. Без неё цветы и мана остаются загадкой.",
          0, 0),
        Q("petal", "Белый лепесток", "botania:white_petal",
          "Сломай мистический цветок. Лепестки — сырьё почти всех ранних крафтов Botania.",
          1.5, 0, "lexicon", count=4),
        Q("apothecary", "Лепестковый алтарь", "botania:apothecary_default",
          "Поставь алтарь, налей воду, брось лепестки. Так собираются функциональные цветы.",
          3.0, 0, "petal"),
        Q("pure_daisy", "Чистоцвет", "botania:pure_daisy",
          "Первый рабочий цветок. Превращает дерево в живую древесину, камень — в живой камень.",
          4.5, 0, "apothecary"),
        Q("livingwood", "Живая древесина", "botania:livingwood",
          "Поставь дуб рядом с чистоцветом и подожди. Это каркас мана-сетей.",
          6.0, -1.0, "pure_daisy", count=8),
        Q("livingrock", "Живой камень", "botania:livingrock",
          "Тот же чистоцвет, но на обычном камне. Из него делают бассейн и алтарь рун.",
          6.0, 1.0, "pure_daisy", count=8),
        Q("pool", "Бассейн маны", "botania:mana_pool",
          "Склад маны. Без него спредеры и пластина Терры не работают.",
          7.5, 0, "livingrock"),
        Q("spreader", "Распылитель маны", "botania:mana_spreader",
          "Стреляет маной в бассейн. Нацель его на бассейн и поставь генерирующий цветок сзади.",
          9.0, 0, ["pool", "livingwood"]),
        Q("tablet", "Таблица маны", "botania:mana_tablet",
          "Переносной заряд. Нужна, чтобы не бегать к бассейну каждый раз.",
          10.5, -1.0, "spreader"),
        Q("manasteel", "Манасталь", "botania:manasteel_ingot",
          "Брось железный слиток в полный бассейн. Базовый металл Botania.",
          10.5, 1.0, "spreader", count=4),
        Q("wand", "Жезл из прута", "botania:twig_wand",
          "Связывает спредеры с приёмниками. ПКМ по спредеру, затем по бассейну.",
          12.0, 0, "manasteel"),
        Q("altar", "Рунический алтарь", "botania:runic_altar",
          "Крафт рун. Налей ману в алтарь, разложи ингредиенты, забери руну жезлом.",
          13.5, 0, "wand"),
        Q("rune_water", "Руна воды", "botania:rune_water",
          "Одна из четырёх стихийных рун. Дальше из них собирают сезонные и греховные.",
          15.0, -1.5, "altar"),
        Q("rune_fire", "Руна огня", "botania:rune_fire",
          "Стихийная руна. Нужна для Терры и части функциональных цветов.",
          15.0, -0.5, "rune_water"),
        Q("rune_earth", "Руна земли", "botania:rune_earth",
          "Стихийная руна. Часто идёт в крафт землеройных и защитных цветов.",
          15.0, 0.5, "rune_fire"),
        Q("rune_air", "Руна воздуха", "botania:rune_air",
          "Стихийная руна. Закрывает четвёрку стихий для пластины Терры.",
          15.0, 1.5, "rune_earth"),
        Q("pylon", "Мана-пилон", "botania:mana_pylon",
          "Усиливает передачу маны и нужен у портала в Альфхейм.",
          16.5, 0, "rune_air"),
        Q("terra_plate", "Пластина Терры", "botania:terra_plate",
          "Ставится на живой камень. Глотает ману, манасталь, мана-жемчуг и мана-алмаз.",
          18.0, 0, "pylon"),
        Q("terrasteel", "Террасталь", "botania:terrasteel_ingot",
          "Слиток середины мода. Оружие, броня и ключ к Гайе.",
          19.5, 0, "terra_plate"),
        Q("terra_pick", "Кирка Терры", "botania:terra_pick",
          "Кирка заряжается маной. На этом этапе уже копает быстрее алмазной.",
          21.0, -1.0, "terrasteel"),
        Q("portal", "Портал в Альфхейм", "botania:alfheim_portal",
          "Рамка из живой древесины и натура-пилонов. Открывается зарядом маны.",
          21.0, 1.0, "terrasteel"),
        Q("elementium", "Элементий", "botania:elementium_ingot",
          "Металл Альфхейма. Обмен через портал: бросай ресурсы в воронку живой древесины.",
          22.5, 1.0, "portal", count=4),
        Q("gaia_pylon", "Пилон Гайи", "botania:gaia_pylon",
          "Четыре пилона вокруг маяка. Призыв стража Гайи — проверка на готовность.",
          24.0, 0, "elementium"),
        Q("gaia", "Слиток Гайи", "botania:gaia_ingot",
          "Дроп с стража. Финал обычной ветки Botania до реликвий.",
          25.5, 0, "gaia_pylon"),
    ]


def alex_quests() -> list[Q]:
    return [
        Q("tablet", "Пещерная табличка", "alexscaves:cave_tablet",
          "Находка в сундуках и от мобов пещер. Без неё карта биома не собирается.",
          0, 0),
        Q("codex", "Кодекс пещер", "alexscaves:cave_codex",
          "Расшифрованная табличка. Открывает рецепты и описание биома.",
          1.5, 0, "tablet"),
        Q("book", "Книга пещер", "alexscaves:cave_book",
          "Сводка по всем биомам Alex's Caves. Держи под рукой, пока ищешь входы.",
          3.0, 0, "codex"),
        Q("neo", "Алый неодим", "alexscaves:scarlet_neodymium_ingot",
          "Магнитные пещеры. Неодим — основа перчаток, рельс и Теслы.",
          4.5, -3.0, "book", count=4),
        Q("tesla", "Лампа Теслы", "alexscaves:tesla_bulb",
          "Сердце магнитного биома. Бьёт молнией и кормит галена-перчатку.",
          6.0, -3.5, "neo"),
        Q("gauntlet", "Галена-перчатка", "alexscaves:galena_gauntlet",
          "Тянет и швыряет металлические блоки. Главный инструмент магнитной ветки.",
          7.5, -3.0, "tesla"),
        Q("sulfur", "Серный порошок", "alexscaves:sulfur_dust",
          "Токсичные пещеры. Сера сыпется с наростов на потолке.",
          4.5, -1.0, "book", count=8),
        Q("radon", "Бутылка радона", "alexscaves:radon_bottle",
          "Собери газ в бутылку. Нужен для ламп и части токсичных крафтов.",
          6.0, -1.0, "sulfur"),
        Q("hazmat", "Химзащита", "alexscaves:hazmat_chestplate",
          "Без костюма радиация токсичных пещер съедает здоровье.",
          7.5, -1.0, "radon"),
        Q("amber", "Янтарь", "alexscaves:amber",
          "Первобытные пещеры. Янтарь и амберсол — свет и крафт копья.",
          4.5, 1.0, "book", count=8),
        Q("spear", "Известняковое копьё", "alexscaves:limestone_spear",
          "Метательное оружие биома. Легко крафтится на месте из известняка.",
          6.0, 0.5, "amber"),
        Q("soup", "Первобытный суп", "alexscaves:primordial_soup",
          "Еда биома. Варится из местной живности и растений.",
          6.0, 1.5, "amber"),
        Q("pearl", "Жемчуг бездны", "alexscaves:pearl",
          "Затонувший биом. Жемчуг падает с моллюсков и идёт в посох моря.",
          4.5, 3.0, "book", count=4),
        Q("staff", "Морской посох", "alexscaves:sea_staff",
          "Оружие бездны. Держит дистанцию против стражей глубин.",
          6.0, 2.5, "pearl"),
        Q("gaze", "Созерцающий жемчуг", "alexscaves:gazing_pearl",
          "Редкий жемчуг. Нужен для поздних крафтов бездны.",
          7.5, 3.0, "staff"),
        Q("caramel", "Карамель", "alexscaves:caramel",
          "Кондитерские пещеры. Карамель — базовый ресурс сладкого биома.",
          4.5, 4.5, "book", count=8),
        Q("cane", "Карамельная трость", "alexscaves:candy_cane",
          "Оружие и блок биома. Из тростей собирают крюки и столбы.",
          6.0, 4.5, "caramel"),
        Q("soda", "Фиолетовая сода", "alexscaves:purple_soda_bottle",
          "Жидкость биома в бутылке. Из неё делают ракеты и ведро соды.",
          7.5, 4.5, "cane"),
        Q("darkness", "Чистая тьма", "alexscaves:pure_darkness",
          "Забытые пещеры. Дроп с босса и редких мобов. Сырьё плаща тьмы.",
          4.5, 6.0, "book"),
        Q("hood", "Капюшон тьмы", "alexscaves:hood_of_darkness",
          "Шлем ветки тьмы. Вместе с плащом закрывает сет тьмы.",
          6.0, 6.0, "darkness"),
        Q("dagger", "Клинок запустения", "alexscaves:desolate_dagger",
          "Финал пещерной книги. Оружие забытого биома.",
          7.5, 6.0, "hood"),
    ]


def avaritia_quests() -> list[Q]:
    return [
        Q("lattice", "Алмазная решётка", "avaritia:diamond_lattice",
          "Старт Avaritia. Крафтится из алмазов. Идёт в кристаллическую матрицу.",
          0, 0, count=4),
        Q("matrix", "Кристаллическая матрица", "avaritia:crystal_matrix_ingot",
          "Слиток каркаса. Без него не собрать сжатый верстак и коллектор нейтрония.",
          1.5, 0, "lattice"),
        Q("table1", "Сжатый верстак", "avaritia:compressed_crafting_table",
          "3×3 верстаков в одном блоке. Промежуточный стол до экстремального.",
          3.0, -1.0, "matrix"),
        Q("table2", "Двойной сжатый верстак", "avaritia:double_compressed_crafting_table",
          "Следующее сжатие. Нужен, чтобы скрафтить стол 9×9.",
          4.5, -1.0, "table1"),
        Q("extreme", "Экстремальный верстак", "avaritia:extreme_crafting_table",
          "Сетка 9×9. Здесь собирают нейтроний, сингулярности и Infinity.",
          6.0, -1.0, "table2"),
        Q("collector", "Коллектор нейтрония", "avaritia:neutron_collector",
          "Пассивно сыпет кучи. Чем плотнее коллектор, тем быстрее.",
          3.0, 1.0, "matrix"),
        Q("pile", "Куча нейтрония", "avaritia:neutron_pile",
          "Пыль из коллектора. Копится долго — поставь коллектор и жди.",
          4.5, 1.0, "collector", count=8),
        Q("nugget", "Самородки нейтрония", "avaritia:neutron_nugget",
          "Сжатие куч. Девять куч = самородок.",
          6.0, 1.0, "pile", count=9),
        Q("ingot", "Слиток нейтрония", "avaritia:neutron_ingot",
          "Основной металл конца. Идёт в компрессор и катализатор Infinity.",
          7.5, 1.0, "nugget"),
        Q("compressor", "Компрессор нейтрония", "avaritia:neutron_compressor",
          "Жмёт стаки предметов в сингулярности. Без него катализатор не собрать.",
          9.0, 0, ["ingot", "extreme"]),
        Q("catalyst", "Катализатор Infinity", "avaritia:infinity_catalyst",
          "Смесь сингулярностей и нейтрония на столе 9×9. Ключ к слитку Infinity.",
          10.5, 0, "compressor"),
        Q("infinity", "Слиток Infinity", "avaritia:infinity_ingot",
          "Финал крафта. Оружие, броня и админ-панель IU требуют именно его.",
          12.0, 0, "catalyst"),
        Q("sword", "Меч Infinity", "avaritia:infinity_sword",
          "Одно попадание. Не носите на хабе без причины.",
          13.5, -1.5, "infinity"),
        Q("pick", "Кирка Infinity", "avaritia:infinity_pickaxe",
          "Копает целые пласты. Переключается в молот.",
          13.5, -0.5, "infinity"),
        Q("chest", "Нагрудник Infinity", "avaritia:infinity_chestplate",
          "Кусок сета. Собирается на экстремальном столе.",
          13.5, 0.5, "infinity"),
        Q("pearl", "Эндест-жемчуг", "avaritia:endest_pearl",
          "Гравитационная жемчужина. Редкий крафт ветки конца.",
          13.5, 1.5, "infinity"),
        Q("box", "Сжатый сундук", "avaritia:compressed_chest",
          "Хранилище под стаки Infinity-крафта. На этом этапе обычных сундуков мало.",
          15.0, 0, "infinity"),
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
          "Поймай 3 осколка эха. Награда — квантовая связь ME и ячейки 64k.",
          2.0, 2.5, extra_deps=[STEAM_LAST], hide=True, xp=400, count=3,
          rewards=[
              ("ae2:quantum_link", 1),
              ("ae2:item_storage_cell_64k", 1),
              ("ae2:fluid_storage_cell_64k", 1),
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
        for it, cnt in q.rewards:
            rid = hid(f"{qid}_{it}_r")
            extra = f"\n				count: {cnt}" if cnt > 1 else ""
            reward_bits.append(
                "{\n"
                f'				id: "{rid}"{extra}\n'
                f'				item: "{it}"\n'
                '				type: "item"\n'
                "			}"
            )
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

    ids = {"botania": bot_ids, "alexscaves": cave_ids, "avaritia": ava_ids}

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
    print("botania", len(bq), "caves", 21, "avaritia", 17, "secrets", len(secrets))
    print("wrote chapters to config/ and server/config/")


if __name__ == "__main__":
    main()
