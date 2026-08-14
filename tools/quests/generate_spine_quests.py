# -*- coding: utf-8 -*-
"""AquaTech spine FTB quests from PLAYER_ROADMAP + StarCatcher craft chain.

IDs use AQ… prefix (safe vs frozen legacy spine and WS/HF workshops).
Writes to config/ and server/config/.
"""
from __future__ import annotations

from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUT_DIRS = [
    ROOT / "config" / "ftbquests" / "quests",
    ROOT / "server" / "config" / "ftbquests" / "quests",
]

GROUP_SPINE = "0AC7A00000000001"
GROUP_WS = "0AC7A00000000005"


def esc(s: str) -> str:
    return s.replace("\\", "\\\\").replace('"', '\\"')


def q(item: str, title: str, sub: str, reward: str | None = None, count: int = 1, xp: int = 30):
    return {
        "item": item,
        "title": title,
        "sub": sub,
        "reward": reward or item,
        "count": count,
        "xp": xp,
    }


# Craft order from kubejs/server_scripts/20_aquatech_rod_crafts.js
CHAPTERS = [
    {
        "filename": "01_primal",
        "order": 1,
        "group": GROUP_SPINE,
        "title": "Акт I · Плот и первый улов",
        "icon": "starcatcher:bamboo_rod",
        "subtitle": "Расширь плот, собери медь и латекс, собери первую цепочку удочек.",
        "quests": [
            q("minecraft:oak_planks", "Доски палубы", "Расширь плот 4×4 дубовыми досками.", "minecraft:oak_planks", 16, 20),
            q("minecraft:stick", "Палки", "Базовый крафт и удочки.", "minecraft:stick", 16, 15),
            q("minecraft:string", "Нитки", "Леска для удочек.", "minecraft:string", 8, 20),
            q("minecraft:bamboo", "Бамбук", "Каркас первой удочки StarCatcher.", "minecraft:bamboo", 8, 25),
            q("minecraft:copper_ingot", "Медь", "Вылови и переплавь медь.", "minecraft:copper_ingot", 8, 30),
            q("starcatcher:bamboo_rod", "Бамбуковая удочка", "Стартовая удочка цепочки StarCatcher.", xp=50),
            q("industrialupgrade:itemplates/copper_plate", "Медная пластина", "Пластины для скромной удочки.", xp=35),
            q("starcatcher:humble_rod", "Скромная удочка", "Второй тир: медь + бамбуковая.", xp=60),
            q("industrialupgrade:synthetic_rubber", "Синтетическая резина", "Высуши латекс и получи резину.", xp=40),
            q("industrialupgrade:itemplates/iron_plate", "Железная пластина", "Нужна для старой доброй удочки.", xp=40),
            q("industrialupgrade:itemplates/tin_plate", "Оловянная пластина", "Второй металл ранней эпохи.", xp=40),
            q(
                "starcatcher:good_old_rod",
                "Старая добрая удочка",
                "Капстоун акта I: железо, олово и резина.",
                xp=90,
            ),
        ],
    },
    {
        "filename": "02_early_lv",
        "order": 2,
        "group": GROUP_SPINE,
        "title": "Акт II · Плавильня и схемы",
        "icon": "starcatcher:naturalist_rod",
        "subtitle": "Железо из улова, бронза, первые электронные схемы.",
        "quests": [
            q("minecraft:iron_ingot", "Железо", "Вылови руду и переплавь.", "minecraft:iron_ingot", 16, 30),
            q("minecraft:redstone", "Редстоун", "Нужен для схем и плавильни.", "minecraft:redstone", 32, 30),
            q("industrialupgrade:itemplates/bronze_plate", "Бронзовая пластина", "Сплав меди и олова.", xp=40),
            q("industrialupgrade:baseore/spinel", "Шпинель", "Редкий улов для натуралиста.", xp=45),
            q(
                "industrialupgrade:crafting_elements/crafting_272_element",
                "Электронная схема",
                "LV-схема: основа машин.",
                xp=55,
            ),
            q(
                "starcatcher:naturalist_rod",
                "Удочка натуралиста",
                "Капстоун акта II: схема + бронза + шпинель.",
                xp=100,
            ),
        ],
    },
    {
        "filename": "03_steam_lv",
        "order": 3,
        "group": GROUP_SPINE,
        "title": "Акт III · Пар, EU и авторыбалка",
        "icon": "aquatech_ui:auto_fisher",
        "subtitle": "Первое электричество, BatBox и автоматический улов.",
        "quests": [
            q("minecraft:slime_ball", "Слизь", "Вылови или добудь для слизневой удочки.", "minecraft:slime_ball", 16, 35),
            q("minecraft:slime_block", "Блок слизи", "Компонент слизневой удочки.", xp=40),
            q("industrialupgrade:baseore2/barium", "Барий", "Улов среднего тира.", xp=45),
            q("industrialupgrade:baseore2/strontium", "Стронций", "Улов среднего тира.", xp=45),
            q("starcatcher:slimed_rod", "Слизневая удочка", "Тир после натуралиста.", xp=80),
            q("industrialupgrade:baseore/silver", "Серебро", "Для ледяной удочки.", xp=50),
            q("industrialupgrade:baseore/aluminium", "Алюминий", "Лёгкий металл LV/MV.", xp=50),
            q("starcatcher:iceborn_rod", "Ледяная удочка", "Серебро, алюминий, хладагент.", xp=90),
            q("aquatech_ui:auto_fisher", "Авторыболов", "Ставь на плот — ловит без тебя.", xp=120),
        ],
    },
    {
        "filename": "04_mv",
        "order": 4,
        "group": GROUP_SPINE,
        "title": "Акт IV · Домна и звёзды",
        "icon": "starcatcher:starcatcher_rod",
        "subtitle": "Вольфрам, хром, продвинутые схемы и удочка Ловца Звёзд.",
        "quests": [
            q("industrialupgrade:baseore/tungsten", "Вольфрам", "Тяжёлый металл MV.", xp=55),
            q("industrialupgrade:baseore/chromium", "Хром", "Для нержавейки и удочек.", xp=55),
            q(
                "industrialupgrade:crafting_elements/crafting_273_element",
                "Продвинутая схема",
                "Advanced Circuit для MV.",
                xp=70,
            ),
            q("industrialupgrade:preciousgem/sapphire_gem", "Сапфир", "Кристалл для Ловца Звёзд.", xp=60),
            q("starcatcher:starcatcher_rod", "Удочка Ловца Звёзд", "Именной тир StarCatcher.", xp=110),
            q("industrialupgrade:preciousgem/topaz_gem", "Топаз", "Для лазуритовой удочки.", xp=60),
            q("industrialupgrade:itemplates/gold_plate", "Золотая пластина", "Обвязка лазуритовой.", xp=50),
            q("starcatcher:azure_crystal_rod", "Лазуритовая удочка", "Капстоун акта IV.", xp=120),
            q(
                "industrialupgrade:alloyingot/stainless_steel",
                "Нержавеющая сталь",
                "Сплав домны — дальше HV.",
                xp=90,
            ),
        ],
    },
    {
        "filename": "05_hv_ev",
        "order": 5,
        "group": GROUP_SPINE,
        "title": "Акт V · Нефть, ядро и AE2",
        "icon": "starcatcher:obsidian_rod",
        "subtitle": "Титан, уран, обсидиан и сеть Applied Energistics.",
        "quests": [
            q("industrialupgrade:baseore/titanium", "Титан", "Каркас акульей удочки.", xp=60),
            q("industrialupgrade:baseore/cobalt", "Кобальт", "Второй металл акулы.", xp=60),
            q("starcatcher:sharktooth_rod", "Удочка акульего зуба", "Титан + кобальт + алмаз.", xp=110),
            q("minecraft:crying_obsidian", "Плачущий обсидиан", "Для обсидиановой удочки.", xp=70),
            q(
                "industrialupgrade:crafting_elements/crafting_274_element",
                "Схема HV",
                "Высокоуровневая электроника.",
                xp=80,
            ),
            q("starcatcher:obsidian_rod", "Обсидиановая удочка", "Нержавейка и обсидиан.", xp=120),
            q("minecraft:heart_of_the_sea", "Сердце моря", "Редкий компонент светоягодной.", xp=90),
            q("industrialupgrade:baseore/platinum", "Платина", "Драгметалл HV.", xp=80),
            q("starcatcher:lush_glowberry_rod", "Светоягодная удочка", "Море + платина.", xp=130),
            q("industrialupgrade:crushed/uranium", "Дроблёный уран", "Топливо и магмовая удочка.", xp=90),
            q("minecraft:nether_star", "Звезда Незера", "Капстоун перед альфой.", xp=100),
            q("starcatcher:magmaforged_rod", "Магмовая удочка", "Уран, инконель, незерит.", xp=150),
            q("ae2:controller", "ME-контроллер", "Сердце склада AE2.", xp=140),
            q("ae2:drive", "ME-накопитель", "Диски для сети.", xp=100),
        ],
    },
    {
        "filename": "06_quantum",
        "order": 6,
        "group": GROUP_SPINE,
        "title": "Акт VI · Квантовый эндгейм",
        "icon": "starcatcher:alpha_rod",
        "subtitle": "Альфа-удочка, иридий, осмиридий и космос.",
        "quests": [
            q("industrialupgrade:alloyingot/osmiridium", "Осмиридий", "Сплав эндгейма.", xp=120),
            q(
                "industrialupgrade:asteroidore/asteroid_adamantium_ore",
                "Астероидный адамантий",
                "Космический улов альфы.",
                xp=130,
            ),
            q("starcatcher:alpha_rod", "Альфа-удочка", "Финал цепочки StarCatcher.", xp=200),
            q("industrialupgrade:baseore/iridium", "Иридий", "Редчайший улов альфы.", xp=140),
            q("industrialupgrade:baseore1/osmium", "Осмий", "Пара к иридию.", xp=140),
            q(
                "minecraft:nether_star",
                "Звёзды для синтеза",
                "Запас звёзд под генератор материи.",
                "minecraft:nether_star",
                4,
                100,
            ),
        ],
    },
]


def quest_block(ch_code: str, idx: int, total: int, data: dict, prev_id: str | None) -> str:
    qid = f"AQ{ch_code}{idx:04d}Q"
    tid = f"AQ{ch_code}{idx:04d}T"
    rid = f"AQ{ch_code}{idx:04d}R"
    xid = f"AQ{ch_code}{idx:04d}X"
    x = ((idx - 1) % 5) * 1.6 - 3.2
    y = -((idx - 1) // 5) * 1.5
    dep = ""
    if prev_id:
        dep = f'\n\t\t\tdependencies: ["{prev_id}"]'
    shape = "hexagon" if idx == total else "rsquare"
    size = "1.35" if idx == total else "1.0"
    lines = [f'\t\t\t\t"{esc(data["sub"])}"']
    desc = "\n".join(lines)
    return f"""\t\t{{
\t\t\tx: {x:.1f}d
\t\t\ty: {y:.1f}d
\t\t\tid: "{qid}"
\t\t\ttitle: "{esc(data["title"])}"
\t\t\ticon: "{data["item"]}"
\t\t\tsubtitle: "{esc(data["sub"])}"
\t\t\tdescription: [
{desc}
\t\t\t]
\t\t\tshape: "{shape}"
\t\t\tsize: {size}d
\t\t\tmin_width: 260{dep}
\t\t\ttasks: [{{
\t\t\t\tid: "{tid}"
\t\t\t\ttype: "item"
\t\t\t\titem: "{data["item"]}"
\t\t\t\tcount: 1L
\t\t\t}}]
\t\t\trewards: [
\t\t\t\t{{
\t\t\t\t\tid: "{rid}"
\t\t\t\t\ttype: "item"
\t\t\t\t\titem: "{data["reward"]}"
\t\t\t\t\tcount: {data["count"]}
\t\t\t\t}}
\t\t\t\t{{
\t\t\t\t\tcommand: "/aquatech grantxp @p {data["xp"]}"
\t\t\t\t\televate_perms: true
\t\t\t\t\tid: "{xid}"
\t\t\t\t\tsilent: true
\t\t\t\t\ttype: "command"
\t\t\t\t}}
\t\t\t]
\t\t}}"""


def chapter_snbt(ch: dict) -> str:
    code = f"{ch['order']:02d}"
    parts = []
    prev = None
    total = len(ch["quests"])
    for i, qd in enumerate(ch["quests"], start=1):
        parts.append(quest_block(code, i, total, qd, prev))
        prev = f"AQ{code}{i:04d}Q"
    body = ",\n".join(parts)
    cid = f"AQ{code}0000CH"
    return f"""{{
\tdefault_hide_dependency_lines: false
\tdefault_quest_shape: "rsquare"
\tfilename: "{ch["filename"]}"
\tgroup: "{ch["group"]}"
\ticon: "{ch["icon"]}"
\tid: "{cid}"
\torder_index: {ch["order"]}
\tquest_links: [ ]
\tquests: [
{body}
\t]
\tsubtitle: "{esc(ch["subtitle"])}"
\ttitle: "{esc(ch["title"])}"
}}
"""


GROUPS_SNBT = f"""{{
\tchapter_groups: [
\t\t{{ id: "{GROUP_SPINE}", title: "Сюжет · Океан" }}
\t\t{{ id: "{GROUP_WS}", title: "Мастерские" }}
\t]
}}
"""

DATA_SNBT = """{
\tdefault_reward_team: false
\tdefault_quest_disable_jei: false
\tdefault_quest_shape: "rsquare"
\tdefault_consume_items: false
\tlock_quests_on_shutdown: false
\ttitle: "AquaTech"
\ticon: "starcatcher:starcatcher_rod"
\tversion: 14
}
"""


def main() -> None:
    ids: list[str] = []
    for ch in CHAPTERS:
        code = f"{ch['order']:02d}"
        for i in range(1, len(ch["quests"]) + 1):
            ids.append(f"AQ{code}{i:04d}Q")
    assert len(ids) == len(set(ids)), "duplicate quest ids"

    for out in OUT_DIRS:
        out.mkdir(parents=True, exist_ok=True)
        chapters_dir = out / "chapters"
        chapters_dir.mkdir(parents=True, exist_ok=True)
        for old in chapters_dir.glob("*.snbt"):
            stem = old.stem.upper()
            if len(stem) >= 16 and all(c in "0123456789ABCDEF" for c in stem):
                old.unlink()
                print("removed orphan", old.name)
        (out / "chapter_groups.snbt").write_text(GROUPS_SNBT, encoding="utf-8")
        (out / "data.snbt").write_text(DATA_SNBT, encoding="utf-8")
        for ch in CHAPTERS:
            path = chapters_dir / f"{ch['filename']}.snbt"
            path.write_text(chapter_snbt(ch), encoding="utf-8")
            print("wrote", out.name, ch["filename"], len(ch["quests"]))
    print(f"OK spine {len(CHAPTERS)} chapters, {len(ids)} quests")


if __name__ == "__main__":
    main()
