# -*- coding: utf-8 -*-
"""Wire aquatech_ui items into mid/late quest chapters + economy starters."""
import re

def inject_before_quests_close(path, snippet):
    text = open(path, encoding="utf-8").read()
    # Insert before final `\t]\n\ttitle:` of chapter
    marker = "\t]\n\ttitle:"
    if marker not in text:
        raise SystemExit(f"marker not found in {path}")
    if "aquatech_ui:" in text and "WIRED_AQUATECH" in text:
        print("already wired", path)
        return
    # avoid double-inject: check for a stable quest id we add
    if 'id: "12AQT00000000001"' in text or 'id: "13AQT00000000001"' in text or 'id: "1AAQT00000000001"' in text or 'id: "10AQT00000000001"' in text:
        print("already wired ids", path)
        return
    text = text.replace(marker, snippet + marker, 1)
    open(path, "w", encoding="utf-8", newline="\n").write(text)
    print("wired", path)

def quest_block(qid, dep, icon, title, subtitle, task_item, task_count, reward_item, reward_count, x, y, tid, rid, shape="diamond"):
    task_cnt = f"\n\t\t\t\tcount: {task_count}" if task_count and task_count > 1 else ""
    rew_cnt = f"\n\t\t\t\t\tcount: {reward_count}" if reward_count and reward_count > 1 else ""
    return f"""\t\t{{
\t\t\tdependencies: ["{dep}"]
\t\t\tentity_vis_size: 1.0f
\t\t\ticon: "{icon}"
\t\t\tid: "{qid}"
\t\t\trewards: [{{
{rew_cnt}
\t\t\t\t\tid: "{rid}"
\t\t\t\t\titem: "{reward_item}"
\t\t\t\t\ttype: "item"
\t\t\t}}]
\t\t\tshape: "{shape}"
\t\t\tsize: 0.95d
\t\t\tsubtitle: "{subtitle}"
\t\t\ttasks: [{{{task_cnt}
\t\t\t\tid: "{tid}"
\t\t\t\titem: "{task_item}"
\t\t\t\ttype: "item"
\t\t\t}}]
\t\t\ttitle: "{title}"
\t\t\tx: {x}d
\t\t\ty: {y}d
\t\t}}
"""

# --- Chapter 01: starter rod reward ---
p1 = r"server\config\ftbquests\quests\chapters\01_kickstarter.snbt"
t1 = open(p1, encoding="utf-8").read()
if 'aquatech_ui:novice_fishing_rod' not in t1:
    t1 = t1.replace(
        'id: "minecraft:fishing_rod"',
        'id: "aquatech_ui:novice_fishing_rod"',
        1,
    )
    # also replace simple string form if any in first rewards
    t1 = t1.replace(
        'item: "minecraft:fishing_rod"',
        'item: "aquatech_ui:novice_fishing_rod"',
        1,
    )
    open(p1, "w", encoding="utf-8", newline="\n").write(t1)
    print("updated ch01 starter rod")
else:
    print("ch01 already has novice rod")

# --- Chapter 02: aquatech rods + tackle ---
ch2 = ""
ch2 += quest_block(
    "12AQT00000000001", "1200000000000002",
    "aquatech_ui:novice_fishing_rod",
    "Удочка AquaTech",
    "Журнал Кикстартера выдаёт не простую палку — удочку новичка AquaTech. С ней открываются навыки и снасти.",
    "aquatech_ui:novice_fishing_rod", None,
    "aquatech_ui:luck_tackle", 1,
    -8.1, -1.8,
    "22AQT00000000001", "32AQT00000000001",
)
ch2 += quest_block(
    "12AQT00000000002", "1200000000000004",
    "aquatech_ui:iron_fishing_rod",
    "Железная леска",
    "Железная удочка AquaTech держит крупный улов и открывает слоты снастей.",
    "aquatech_ui:iron_fishing_rod", None,
    "aquatech_ui:speed_tackle", 1,
    -4.5, -1.8,
    "22AQT00000000002", "32AQT00000000002",
)
ch2 += quest_block(
    "12AQT00000000003", "1200000000000009",
    "aquatech_ui:prismarine_fishing_rod",
    "Призмариновый каст",
    "Призмариновая удочка — мост к глубинам. Держи её до главы Depths.",
    "aquatech_ui:prismarine_fishing_rod", None,
    "aquatech_ui:depth_tackle", 1,
    4.5, -1.8,
    "22AQT00000000003", "32AQT00000000003",
)
inject_before_quests_close(r"server\config\ftbquests\quests\chapters\02_catch.snbt", ch2)

# --- Chapter 03: ocean machines + first coins ---
ch3 = ""
ch3 += quest_block(
    "13AQT00000000001", "1300000000000003",
    "aquatech_ui:auto_fisher",
    "Авто-рыбак",
    "Пока ты строишь атолл, AutoFisher тянет сеть без рук. Нужна энергия позже — пока хватит ручного запуска.",
    "aquatech_ui:auto_fisher", None,
    "aquatech_ui:mesh_filter", 1,
    -4.5, 1.8,
    "23AQT00000000001", "33AQT00000000001",
)
ch3 += quest_block(
    "13AQT00000000002", "1300000000000005",
    "aquatech_ui:ocean_filter",
    "Океанский фильтр",
    "Ocean Filter чистит ил и мусор со дна — сырьё для компоста и наживки.",
    "aquatech_ui:ocean_filter", None,
    "lightmanscurrency:coin_copper", 16,
    -0.9, 1.8,
    "23AQT00000000002", "33AQT00000000002",
)
ch3 += quest_block(
    "13AQT00000000003", "1300000000000007",
    "aquatech_ui:seabed_dredger",
    "Дноуглубитель",
    "Seabed Dredger поднимает песок, гравий и обломки с мелководья вокруг атома.",
    "aquatech_ui:seabed_dredger", None,
    "aquatech_ui:dredger_drill_bit", 1,
    2.7, 1.8,
    "23AQT00000000003", "33AQT00000000003",
)
ch3 += quest_block(
    "13AQT00000000004", "1300000000000009",
    "lightmanscurrency:coin_iron",
    "Первые монеты атома",
    "После Atoll открывается торговля Lightman's Currency. Собери железные монеты — вход в лавки архипелага.",
    "lightmanscurrency:coin_iron", 8,
    "lightmanscurrency:coin_gold", 4,
    4.5, 1.8,
    "23AQT00000000004", "33AQT00000000004",
    shape="hexagon",
)
inject_before_quests_close(r"server\config\ftbquests\quests\chapters\03_atoll.snbt", ch3)

# --- Chapter 10: pressure / sonar / abyssal ---
ch10 = ""
ch10 += quest_block(
    "1AAQT00000000001", "1A00000000000002",
    "aquatech_ui:sonar_goggles",
    "Сонар-очки",
    "Давление в HUD растёт с глубиной. Сонар-очки подсказывают, куда нырять без критической нагрузки.",
    "aquatech_ui:sonar_goggles", None,
    "aquatech_ui:abyssal_magnet", 1,
    -8.1, 1.8,
    "2AAQT00000000001", "3AAQT00000000001",
)
ch10 += quest_block(
    "1AAQT00000000002", "1A00000000000005",
    "aquatech_ui:abyssal_fishing_rod",
    "Абиссальная удочка",
    "На глубине Maelstrom обычная леска рвётся. Абиссальная удочка AquaTech создана для чудовищ Aquamirae.",
    "aquatech_ui:abyssal_fishing_rod", None,
    "aquatech_ui:abyssal_lure", 1,
    -2.7, 1.8,
    "2AAQT00000000002", "3AAQT00000000002",
)
ch10 += quest_block(
    "1AAQT00000000003", "1A00000000000009",
    "aquatech_ui:depth_tackle",
    "Давление под контролем",
    "Снасть глубины + экип Aquamirae — и HUD давления остаётся в зелёной зоне у дна каньона.",
    "aquatech_ui:depth_tackle", None,
    "aquamirae:abyssal_amethyst", 2,
    4.5, 1.8,
    "2AAQT00000000003", "3AAQT00000000003",
)
inject_before_quests_close(r"server\config\ftbquests\quests\chapters\10_depths.snbt", ch10)

print("done")
