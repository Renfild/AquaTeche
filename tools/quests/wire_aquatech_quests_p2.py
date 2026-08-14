# -*- coding: utf-8 -*-
"""Wire aquatech_ui side-quests into chapters 04-09, 11-13.

Follows the exact pattern of wire_aquatech_quests.py: appends new quest blocks
right before the chapter's closing `]` + `title:` line, using fresh AQT-suffixed
ids namespaced by the chapter's own hex-ish prefix (see QUEST_ID_FREEZE.md) so we
never collide with or renumber existing frozen spine ids.
"""


def inject_before_quests_close(path, snippet, guard_ids):
    text = open(path, encoding="utf-8").read()
    marker = "\t]\n\ttitle:"
    if marker not in text:
        raise SystemExit(f"marker not found in {path}")
    if any(f'id: "{gid}"' in text for gid in guard_ids):
        print("already wired", path)
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


# --- Chapter 04 Roost: capstone -> 1400000000000009 ---
ch04 = ""
ch04 += quest_block(
    "14AQT00000000001", "1400000000000009",
    "aquatech_ui:thermal_lure",
    "Тёплое перо",
    "Курятник Roost греет воду вокруг — тёплая наживка AquaTech привлекает рыбу теплолюбивых видов.",
    "minecraft:feather", 32,
    "aquatech_ui:thermal_lure", 2,
    -2.7, 2.7,
    "24AQT00000000001", "34AQT00000000001",
)
inject_before_quests_close(r"server\config\ftbquests\quests\chapters\04_roost.snbt", ch04, ["14AQT00000000001"])

# --- Chapter 05 Swarm: capstone -> 1500000000000009 ---
ch05 = ""
ch05 += quest_block(
    "15AQT00000000001", "1500000000000009",
    "aquatech_ui:kinetic_lure",
    "Улей и удочка",
    "Пасека Swarm даёт избыток воска и меда — переработай их в наживку с кинетическим зарядом.",
    "minecraft:honeycomb", 8,
    "aquatech_ui:kinetic_lure", 2,
    -2.7, 2.7,
    "25AQT00000000001", "35AQT00000000001",
)
inject_before_quests_close(r"server\config\ftbquests\quests\chapters\05_swarm.snbt", ch05, ["15AQT00000000001"])

# --- Chapter 06 Kinetics: gate speed/efficiency upgrades behind Create kinetics ---
ch06 = ""
ch06 += quest_block(
    "16AQT00000000001", "1600000000000009",
    "aquatech_ui:speed_upgrade",
    "Кинетический привод",
    "Валы и шестерни Create крутят не только станки — научись переносить их момент в модуль скорости AquaTech.",
    "create:cogwheel_shaft", 4,
    "aquatech_ui:speed_upgrade", 1,
    -4.5, 2.7,
    "26AQT00000000001", "36AQT00000000001",
)
ch06 += quest_block(
    "16AQT00000000002", "16AQT00000000001",
    "aquatech_ui:efficiency_upgrade",
    "Большая передача",
    "Большая шестерня держит нагрузку стабильнее — основа для модуля энергоэффективности.",
    "create:andesite_encased_large_cogwheel", 2,
    "aquatech_ui:efficiency_upgrade", 1,
    -0.9, 2.7,
    "26AQT00000000002", "36AQT00000000002",
)
inject_before_quests_close(r"server\config\ftbquests\quests\chapters\06_kinetics.snbt", ch06, ["16AQT00000000001"])

# --- Chapter 07 Steam ---
ch07 = ""
ch07 += quest_block(
    "17AQT00000000001", "1700000000000009",
    "aquatech_ui:mesh_filter",
    "Паровой конденсат",
    "Пар из машинного зала конденсируется в чистую воду — идеальный фильтр для морской сетки.",
    "create:steam_engine", 1,
    "aquatech_ui:mesh_filter", 2,
    -2.7, 2.7,
    "27AQT00000000001", "37AQT00000000001",
)
inject_before_quests_close(r"server\config\ftbquests\quests\chapters\07_steam.snbt", ch07, ["17AQT00000000001"])

# --- Chapter 08 Power: reward hydro_reactor once player has RF generation ---
ch08 = ""
ch08 += quest_block(
    "18AQT00000000001", "1800000000000009",
    "aquatech_ui:hydro_reactor",
    "Гидро-Термальный Реактор",
    "Раз у тебя есть стабильная генерация FE — собери Hydro Reactor и жги био-пеллеты из водорослей на энергию.",
    "thermal:dynamo_stirling", 1,
    "aquatech_ui:hydro_reactor", 1,
    -2.7, 2.7,
    "28AQT00000000001", "38AQT00000000001",
    shape="hexagon",
)
inject_before_quests_close(r"server\config\ftbquests\quests\chapters\08_power.snbt", ch08, ["18AQT00000000001"])

# --- Chapter 09 Industry: reward double_hook_upgrade ---
ch09 = ""
ch09 += quest_block(
    "19AQT00000000001", "1900000000000009",
    "aquatech_ui:double_hook_upgrade",
    "Промышленный крюк",
    "Индустриальная автоматизация ловит вдвое быстрее — модуль двойного крюка для твоего Авто-рыбака.",
    "industrialforegoing:biofuel_generator", 1,
    "aquatech_ui:double_hook_upgrade", 1,
    -2.7, 2.7,
    "29AQT00000000001", "39AQT00000000001",
)
inject_before_quests_close(r"server\config\ftbquests\quests\chapters\09_industry.snbt", ch09, ["19AQT00000000001"])

# --- Chapter 11 ME: neptune_trident materials tie-in with Ocean Altar ---
ch11 = ""
ch11 += quest_block(
    "1BAQT00000000001", "1B00000000000025",
    "aquatech_ui:abyssal_magnet",
    "Реагенты для Алтаря",
    "ME-система может авто-крафтить редкие реагенты. Собери их для Алтаря Морских Реликвий — 4 предмета в него дают Трезубец Нептуна.",
    "ae2:certus_quartz_crystal", 4,
    "aquatech_ui:abyssal_magnet", 1,
    -2.7, 2.7,
    "2BAQT00000000001", "3BAQT00000000001",
    shape="hexagon",
)
inject_before_quests_close(r"server\config\ftbquests\quests\chapters\11_me.snbt", ch11, ["1BAQT00000000001"])

# --- Chapter 12 Dreadnought ---
ch12 = ""
ch12 += quest_block(
    "1CAQT00000000001", "1C00000000000010",
    "aquatech_ui:abyssal_lure",
    "Трофей Дредноута",
    "Обломки Дредноута хранят абиссальный янтарь — из него получается лучшая наживка бездны.",
    "aquamirae:abyssal_amethyst", 2,
    "aquatech_ui:abyssal_lure", 2,
    -2.7, 2.7,
    "2CAQT00000000001", "3CAQT00000000001",
)
inject_before_quests_close(r"server\config\ftbquests\quests\chapters\12_dreadnought.snbt", ch12, ["1CAQT00000000001"])

# --- Chapter 13 Horizon Raids (optional endgame): full-circle Ocean Altar reward ---
ch13 = ""
ch13 += quest_block(
    "1EAQT00000000001", "1E00000000000006",
    "aquatech_ui:ocean_altar",
    "Алтарь Морских Реликвий",
    "Финал горизонта: собери Алтарь и выкуй Трезубец Нептуна из четырёх редчайших реликвий похода.",
    "minecraft:nether_star", 1,
    "aquatech_ui:ocean_altar", 1,
    -2.7, 2.7,
    "2EAQT00000000001", "3EAQT00000000001",
    shape="hexagon",
)
inject_before_quests_close(r"server\config\ftbquests\quests\chapters\13_horizon_raids.snbt", ch13, ["1EAQT00000000001"])

print("done")
