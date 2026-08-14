# -*- coding: utf-8 -*-
"""
AquaTech — generate optional FTB Workshop (Мастерские) side chapters.
IDs use WS… prefix (safe vs QUEST_ID_FREEZE spine).
Writes to config/ and server/config/.

Expanded v3 + polish: serpentine layout, milestone shapes, stage titles,
explicit dependencies (all), richer descriptions. Early WS…0001Q IDs stable.
"""
from __future__ import annotations

from pathlib import Path

from workshop_guides import build_description, chapter_subtitle
from workshop_quest_extras import merge_extras

ROOT = Path(__file__).resolve().parents[2]
OUT_DIRS = [
    ROOT / "config" / "ftbquests" / "quests",
    ROOT / "server" / "config" / "ftbquests" / "quests",
]

WS_GROUP = "0AC7A00000000005"
WS_GROUP_TITLE = "Мастерские"


def esc(s: str) -> str:
    return s.replace("\\", "\\\\").replace('"', '\\"')


def q(item: str, title: str, sub: str, reward_item: str | None = None, reward_count: int = 1, xp: int = 25):
    return {
        "item": item,
        "title": title,
        "sub": sub,
        "reward_item": reward_item or item,
        "reward_count": reward_count,
        "xp": xp,
    }


CHAPTERS = [
    {
        "filename": "20_ws_bees",
        "order": 20,
        "title": "Мастерская · Рой",
        "icon": "productivebees:advanced_oak_beehive",
        "lore": "Углубление Productive Bees — параллельно сюжетному Рою.",
        "quests": [
            q("minecraft:honeycomb", "Соты мастера", "Собери соты для стартовых лакомств.", "minecraft:honeycomb", 8, 20),
            q("minecraft:honey_bottle", "Бутылка мёда", "Жидкое топливо роя.", "minecraft:honey_bottle", 8, 20),
            q("productivebees:honey_treat", "Медовое лакомство", "Приручай и корми пчёл treats.", "productivebees:honey_treat", 4, 30),
            q("productivebees:bee_cage", "Клетка пчелы", "Переноси пчёл между ульями.", "productivebees:bee_cage", 2, 30),
            q("productivebees:sturdy_bee_cage", "Крепкая клетка", "Надёжный транспорт редких пчёл.", "productivebees:sturdy_bee_cage", 1, 40),
            q("minecraft:beehive", "Ванильный улей", "База до advanced.", "minecraft:beehive", 1, 25),
            q("productivebees:oak_wood_nest", "Дубовое гнездо", "Дикий старт линии oak.", "productivebees:oak_wood_nest", 1, 30),
            q("productivebees:advanced_oak_beehive", "Продвинутый улей", "Advanced hive — сердце мастерской.", "productivebees:advanced_oak_beehive", 1, 50),
            q("productivebees:advanced_birch_beehive", "Берёзовый улей", "Разнообразие корпусов.", "productivebees:advanced_birch_beehive", 1, 40),
            q("productivebees:advanced_spruce_beehive", "Еловый улей", "Ещё один advanced hive.", "productivebees:advanced_spruce_beehive", 1, 40),
            q("productivebees:expansion_box_oak", "Expansion Box", "Расширь вместимость улья.", "productivebees:expansion_box_oak", 1, 45),
            q("productivebees:feeder", "Кормушка", "Корми рой централизованно.", "productivebees:feeder", 1, 40),
            q("productivebees:centrifuge", "Центрифуга", "Жми соты в эссенции и продукты.", "productivebees:centrifuge", 1, 50),
            q("productivebees:powered_centrifuge", "Силовая центрифуга", "Ускорь переработку энергией.", "productivebees:powered_centrifuge", 1, 60),
            q("productivebees:heated_centrifuge", "Нагретая центрифуга", "Высокотемпературная переработка.", "productivebees:heated_centrifuge", 1, 70),
            q("productivebees:bottler", "Bottler", "Разлей продукты роя.", "productivebees:bottler", 1, 50),
            q("productivebees:incubator", "Инкубатор", "Выращивай гены и линии пчёл.", "productivebees:incubator", 1, 60),
            q("productivebees:breeding_chamber", "Камера разведения", "Селекция пород.", "productivebees:breeding_chamber", 1, 70),
            q("productivebees:gene", "Ген", "Сырой генетический материал.", "productivebees:gene", 2, 50),
            q("productivebees:gene_bottle", "Бутыль гена", "Храни генетический материал.", "productivebees:gene_bottle", 2, 50),
            q("productivebees:gene_indexer", "Индексатор генов", "Каталогизируй рой как инженер.", "productivebees:gene_indexer", 1, 80),
            q("productivebees:cryo_stasis", "Крио-стазис", "Заморозка образцов.", "productivebees:cryo_stasis", 1, 80),
            q("productivebees:honey_generator", "Медовой генератор", "Энергия из мёда.", "productivebees:honey_generator", 1, 80),
            q("productivebees:upgrade_productivity", "Upgrade · Productivity", "Больше выхода с улья.", "productivebees:upgrade_productivity", 1, 70),
            q("productivebees:upgrade_time", "Upgrade · Time", "Быстрее циклы улья.", "productivebees:upgrade_time", 1, 70),
            q("productivebees:upgrade_comb_block", "Upgrade · Comb Block", "Блочные соты.", "productivebees:upgrade_comb_block", 1, 70),
            q("productivebees:upgrade_simulator", "Upgrade · Simulator", "Симуляция без пчёл в мире.", "productivebees:upgrade_simulator", 1, 90),
            q("productivebees:advanced_bamboo_beehive", "Бамбуковый улей", "Экзотика атолла.", "productivebees:advanced_bamboo_beehive", 1, 60),
            q("minecraft:honey_block", "Медовой блок", "Склад и декор.", "minecraft:honey_block", 8, 40),
            q("productivebees:heated_centrifuge", "Капстоун роя", "Закрепи нагретую линию переработки.", "productivebees:honey_treat", 16, 120),
        ],
    },
    {
        "filename": "21_ws_roost",
        "order": 21,
        "title": "Мастерская · Курятня",
        "icon": "chicken_roost:roost",
        "lore": "Roost Ultimate глубже сюжетной курятни — биосинтез руд.",
        "quests": [
            q("minecraft:egg", "Яйцо линии", "Стартовый материал выводка.", "minecraft:egg", 16, 20),
            q("minecraft:feather", "Перо", "Побочный продукт фермы.", "minecraft:feather", 16, 15),
            q("minecraft:chicken", "Сырая курица", "Первый результат фермы.", "minecraft:chicken", 8, 20),
            q("chicken_roost:roost", "Roost", "Поставь основной roost-блок.", "chicken_roost:roost", 1, 40),
            q("chicken_roost:roost_empty", "Пустой roost", "Запасной корпус.", "chicken_roost:roost_empty", 1, 30),
            q("chicken_roost:collector", "Сборщик", "Автосбор продуктов кур.", "chicken_roost:collector", 1, 50),
            q("chicken_roost:feeder", "Кормушка кур", "Автокормление выводка.", "chicken_roost:feeder", 1, 45),
            q("chicken_roost:breeder", "Разводчик", "Ускорь селекцию линий.", "chicken_roost:breeder", 1, 50),
            q("chicken_roost:trainer", "Тренер", "Прокачай кур под ресурс.", "chicken_roost:trainer", 1, 60),
            q("chicken_roost:chicken_scanner", "Сканер кур", "Читай статы линии.", "chicken_roost:chicken_scanner", 1, 50),
            q("chicken_roost:chicken_stick", "Chicken Stick", "Инструмент работы с roost.", "chicken_roost:chicken_stick", 1, 40),
            q("chicken_roost:chicken_food_tier_1", "Корм T1", "Питание первого тира.", "chicken_roost:chicken_food_tier_1", 8, 30),
            q("chicken_roost:chicken_food_tier_3", "Корм T3", "Средний рацион.", "chicken_roost:chicken_food_tier_3", 8, 40),
            q("chicken_roost:chicken_food_tier_5", "Корм T5", "Продвинутый рацион.", "chicken_roost:chicken_food_tier_5", 8, 50),
            q("chicken_roost:chicken_food_tier_7", "Корм T7", "Почти топ.", "chicken_roost:chicken_food_tier_7", 4, 60),
            q("chicken_roost:chicken_food_tier_9", "Корм T9", "Максимальный корм.", "chicken_roost:chicken_food_tier_9", 4, 80),
            q("chicken_roost:chicken_essence_tier_1", "Эссенция T1", "Биосинтез tier 1.", "chicken_roost:chicken_essence_tier_1", 4, 30),
            q("chicken_roost:chicken_essence_tier_3", "Эссенция T3", "Биосинтез tier 3.", "chicken_roost:chicken_essence_tier_3", 4, 45),
            q("chicken_roost:chicken_essence_tier_5", "Эссенция T5", "Биосинтез tier 5.", "chicken_roost:chicken_essence_tier_5", 4, 60),
            q("chicken_roost:chicken_essence_tier_7", "Эссенция T7", "Биосинтез tier 7.", "chicken_roost:chicken_essence_tier_7", 2, 75),
            q("chicken_roost:chicken_essence_tier_9", "Эссенция T9", "Топ эссенция кур.", "chicken_roost:chicken_essence_tier_9", 2, 90),
            q("chicken_roost:soul_extractor", "Soul Extractor", "Духи для soul-линий.", "chicken_roost:soul_extractor", 1, 70),
            q("chicken_roost:soul_breeder", "Soul Breeder", "Продвинутое скрещивание.", "chicken_roost:soul_breeder", 1, 80),
            q("chicken_roost:alpha_roost_container", "Alpha Roost", "Контейнер элитной линии.", "chicken_roost:alpha_roost_container", 1, 90),
            q("chicken_roost:chickenstorage", "Хранилище кур", "Склад выводка.", "chicken_roost:chickenstorage", 1, 60),
            q("minecraft:iron_ingot", "Железо из кур", "Куры = шахта: железо.", "minecraft:iron_ingot", 32, 60),
            q("minecraft:gold_ingot", "Золото из кур", "Биосинтез драгметалла.", "minecraft:gold_ingot", 16, 70),
            q("minecraft:copper_ingot", "Медь из кур", "Проводка атолла.", "minecraft:copper_ingot", 32, 50),
            q("minecraft:redstone", "Редстоун из кур", "Сигналы фермы.", "minecraft:redstone", 32, 55),
            q("minecraft:diamond", "Алмазный выводок", "Капстоун курятны.", "minecraft:diamond", 4, 120),
        ],
    },
    {
        "filename": "22_ws_mystical",
        "order": 22,
        "title": "Мастерская · Mystical Agriculture",
        "icon": "mysticalagriculture:inferium_essence",
        "lore": "Отдельная ветка эссенций — не смешивать с сюжетным Роем.",
        "quests": [
            q("mysticalagriculture:inferium_essence", "Inferium", "Базовая эссенция.", "mysticalagriculture:inferium_essence", 16, 30),
            q("mysticalagriculture:prosperity_shard", "Prosperity", "Осколки для семян.", "mysticalagriculture:prosperity_shard", 8, 30),
            q("mysticalagriculture:inferium_seeds", "Семена Inferium", "Первая грядка.", "mysticalagriculture:inferium_seeds", 4, 40),
            q("mysticalagriculture:prosperity_seed_base", "Seed Base", "Основа кастомных семян.", "mysticalagriculture:prosperity_seed_base", 4, 40),
            q("mysticalagriculture:soulium_dust", "Soulium Dust", "Пыль душ.", "mysticalagriculture:soulium_dust", 8, 40),
            q("mysticalagriculture:soulstone", "Soulstone", "Камень душ.", "mysticalagriculture:soulstone", 16, 40),
            q("mysticalagriculture:inferium_block", "Блок Inferium", "Сжатая эссенция.", "mysticalagriculture:inferium_block", 4, 35),
            q("mysticalagriculture:prudentium_essence", "Prudentium", "Тир 2.", "mysticalagriculture:prudentium_essence", 8, 50),
            q("mysticalagriculture:tertium_essence", "Tertium", "Тир 3.", "mysticalagriculture:tertium_essence", 8, 60),
            q("mysticalagriculture:imperium_essence", "Imperium", "Тир 4.", "mysticalagriculture:imperium_essence", 8, 70),
            q("mysticalagriculture:supremium_essence", "Supremium", "Тир 5.", "mysticalagriculture:supremium_essence", 4, 80),
            q("mysticalagriculture:fertilized_essence", "Fertilized Essence", "Удобрение грядок.", "mysticalagriculture:fertilized_essence", 8, 45),
            q("mysticalagriculture:watering_can", "Лейка", "Базовый полив.", "mysticalagriculture:watering_can", 1, 50),
            q("mysticalagriculture:inferium_watering_can", "Лейка Inferium", "Быстрее рост.", "mysticalagriculture:inferium_watering_can", 1, 55),
            q("mysticalagriculture:imperium_watering_can", "Лейка Imperium", "Продвинутый полив.", "mysticalagriculture:imperium_watering_can", 1, 70),
            q("mysticalagriculture:inferium_growth_accelerator", "Accelerator Inferium", "Ускоритель у грядок.", "mysticalagriculture:inferium_growth_accelerator", 1, 60),
            q("mysticalagriculture:prudentium_growth_accelerator", "Accelerator Prudentium", "Тир 2 ускоритель.", "mysticalagriculture:prudentium_growth_accelerator", 1, 65),
            q("mysticalagriculture:tertium_growth_accelerator", "Accelerator Tertium", "Тир 3 ускоритель.", "mysticalagriculture:tertium_growth_accelerator", 1, 70),
            q("mysticalagriculture:imperium_growth_accelerator", "Accelerator Imperium", "Тир 4 ускоритель.", "mysticalagriculture:imperium_growth_accelerator", 1, 80),
            q("mysticalagriculture:supremium_growth_accelerator", "Accelerator Supremium", "Топ ускорение.", "mysticalagriculture:supremium_growth_accelerator", 1, 90),
            q("mysticalagriculture:infusion_crystal", "Infusion Crystal", "Кристалл инфузии.", "mysticalagriculture:infusion_crystal", 1, 60),
            q("mysticalagriculture:master_infusion_crystal", "Master Crystal", "Мастер-инфузия.", "mysticalagriculture:master_infusion_crystal", 1, 90),
            q("mysticalagriculture:essence_vessel", "Essence Vessel", "Сосуд эссенций.", "mysticalagriculture:essence_vessel", 1, 70),
            q("mysticalagriculture:inferium_furnace", "Печь Inferium", "Плавка агролинии.", "mysticalagriculture:inferium_furnace", 1, 55),
            q("mysticalagriculture:supremium_furnace", "Печь Supremium", "Быстрая плавка.", "mysticalagriculture:supremium_furnace", 1, 85),
            q("mysticalagriculture:awakened_supremium_essence", "Awakened Supremium", "Пробуждённая эссенция.", "mysticalagriculture:awakened_supremium_essence", 2, 100),
            q("mysticalagriculture:awakened_supremium_ingot", "Awakened Ingot", "Слиток пробуждения.", "mysticalagriculture:awakened_supremium_ingot", 2, 110),
            q("mysticalagriculture:awakened_supremium_watering_can", "Awakened лейка", "Макс полив.", "mysticalagriculture:awakened_supremium_watering_can", 1, 120),
            q("mysticalagriculture:awakened_supremium_upgrade", "Awakened Upgrade", "Апгрейд машин.", "mysticalagriculture:awakened_supremium_upgrade", 1, 120),
            q("mysticalagriculture:awakened_supremium_block", "Капстоун агро", "Блок пробуждённой эссенции.", "mysticalagriculture:awakened_supremium_essence", 4, 140),
        ],
    },
    {
        "filename": "23_ws_create_water",
        "order": 23,
        "title": "Мастерская · Create Water",
        "icon": "create_aquatic_ambitions:mechanical_conduit",
        "lore": "Create + Aquatic Ambitions — трубы и фермы в воде.",
        "quests": [
            q("minecraft:prismarine_shard", "Осколки", "Сырьё под channeling.", "minecraft:prismarine_shard", 16, 20),
            q("minecraft:prismarine_crystals", "Кристаллы", "Свечение и рецепты.", "minecraft:prismarine_crystals", 8, 20),
            q("minecraft:heart_of_the_sea", "Сердце моря", "Основа кондуита.", "minecraft:nautilus_shell", 2, 40),
            q("create:copper_casing", "Copper Casing", "Корпус Create у воды.", "create:copper_casing", 4, 30),
            q("create:fluid_pipe", "Fluid Pipe", "Трубы жидкости.", "create:fluid_pipe", 16, 30),
            q("create:mechanical_pump", "Mechanical Pump", "Качай воду/растворы.", "create:mechanical_pump", 1, 40),
            q("create:spout", "Spout", "Розлив в мире.", "create:spout", 1, 40),
            q("create:hose_pulley", "Hose Pulley", "Забор воды океана.", "create:hose_pulley", 1, 50),
            q("create:portable_fluid_interface", "Portable Fluid IO", "Жидкости на контрабанде судов.", "create:portable_fluid_interface", 1, 50),
            q("create:fluid_tank", "Fluid Tank", "Буфер жидкости.", "create:fluid_tank", 2, 45),
            q("create:smart_fluid_pipe", "Smart Fluid Pipe", "Фильтр жидкостей.", "create:smart_fluid_pipe", 4, 45),
            q("create_aquatic_ambitions:mechanical_conduit", "Mechanical Conduit", "Сердце водной автоматизации.", "create_aquatic_ambitions:mechanical_conduit", 1, 70),
            q("create_aquatic_ambitions:calcium_rich_powder", "Кальциевый порошок", "Побочный продукт океана.", "create_aquatic_ambitions:calcium_rich_powder", 8, 40),
            q("create_aquatic_ambitions:suspicious_rock", "Suspicious Rock", "Странный осадок дна.", "create_aquatic_ambitions:suspicious_rock", 4, 40),
            q("create_aquatic_ambitions:nautilus_shard", "Nautilus Shard", "Осколок наутилуса.", "create_aquatic_ambitions:nautilus_shard", 4, 50),
            q("create_aquatic_ambitions:spiky_shell", "Колючая раковина", "Материал сплавов.", "create_aquatic_ambitions:spiky_shell", 4, 50),
            q("create_aquatic_ambitions:prismarine_alloy", "Prismarine Alloy", "Океанский сплав.", "create_aquatic_ambitions:prismarine_alloy", 4, 70),
            q("create_aquatic_ambitions:prismarine_alloy_rod", "Стержень сплава", "Деталь механизмов.", "create_aquatic_ambitions:prismarine_alloy_rod", 4, 70),
            q("create_aquatic_ambitions:prismarine_alloy_block", "Блок сплава", "Склад и витрина.", "create_aquatic_ambitions:prismarine_alloy_block", 1, 80),
            q("create:propeller", "Propeller", "Поток воздуха/воды у ферм.", "create:propeller", 2, 40),
            q("create:encased_fan", "Encased Fan", "Обдув линий.", "create:encased_fan", 1, 45),
            q("create:water_wheel", "Water Wheel", "Энергия прилива.", "create:water_wheel", 1, 40),
            q("create:large_water_wheel", "Large Water Wheel", "Больше крутящего момента.", "create:large_water_wheel", 1, 55),
            q("create:whisk", "Whisk", "Смешивание растворов.", "create:whisk", 1, 35),
            q("create:mechanical_mixer", "Mixer", "Миксер у канала.", "create:mechanical_mixer", 1, 50),
            q("create:basin", "Basin", "Чаша процессов.", "create:basin", 1, 40),
            q("minecraft:conduit", "Ванильный кондуит", "Сравни с mechanical.", "minecraft:sea_lantern", 4, 80),
            q("minecraft:sea_pickle", "Морской огурец", "Свет и декор фермы.", "minecraft:sea_pickle", 8, 30),
            q("minecraft:tube_coral_block", "Коралл", "Живая ферма кораллов.", "minecraft:tube_coral_block", 8, 35),
            q("create_aquatic_ambitions:prismarine_alloy_block", "Капстоун воды", "Закрепи сплавную линию.", "create_aquatic_ambitions:prismarine_alloy", 8, 120),
        ],
    },
    {
        "filename": "24_ws_atoll_atmosphere",
        "order": 24,
        "title": "Мастерская · Атмосфера атолла",
        "icon": "supplementaries:planter",
        "lore": "Quark + Supplementaries — обживи базу без силы.",
        "quests": [
            q("quark:glass_item_frame", "Стеклянная рамка", "Витрины улова.", "quark:glass_item_frame", 4, 20),
            q("quark:glowing_glass_item_frame", "Светящаяся рамка", "Ночные витрины.", "quark:glowing_glass_item_frame", 2, 25),
            q("quark:pipe", "Труба Quark", "Простые item pipes.", "quark:pipe", 8, 30),
            q("quark:encased_pipe", "Encased Pipe", "Эстетичные трубы.", "quark:encased_pipe", 4, 30),
            q("quark:crate", "Ящик Quark", "Компактное хранение.", "quark:crate", 2, 30),
            q("quark:apple_crate", "Ящик яблок", "Склад еды.", "quark:apple_crate", 1, 25),
            q("quark:potato_crate", "Ящик картошки", "Склад фермы.", "quark:potato_crate", 1, 25),
            q("quark:iron_rod", "Железный стержень", "Деталь Quark.", "quark:iron_rod", 8, 20),
            q("quark:backpack", "Рюкзак", "Удобство исследователя.", "quark:backpack", 1, 50),
            q("quark:abacus", "Счёты", "QoL подсчёт стаков.", "quark:abacus", 1, 30),
            q("quark:trowel", "Мастерок", "Быстрая укладка блоков.", "quark:trowel", 1, 35),
            q("quark:white_stool", "Табурет", "Мебель гавани.", "quark:white_stool", 2, 20),
            q("quark:blue_stool", "Синий табурет", "Цвет флота.", "quark:blue_stool", 2, 20),
            q("quark:oak_chest", "Дубовый сундук Quark", "Вариант склада.", "quark:oak_chest", 2, 25),
            q("quark:oak_ladder", "Лестница Quark", "Вертикаль атолла.", "quark:oak_ladder", 8, 20),
            q("quark:black_framed_glass", "Рамочное стекло", "Окна мастерской.", "quark:black_framed_glass", 8, 30),
            q("supplementaries:planter", "Кашпо", "Зелень на платформе.", "supplementaries:planter", 2, 30),
            q("supplementaries:faucet", "Кран", "Вода и эстетика труб.", "supplementaries:faucet", 2, 30),
            q("supplementaries:jar", "Банка", "Декор полок.", "supplementaries:jar", 4, 20),
            q("supplementaries:cage", "Клетка Suppl.", "Декор/мобы.", "supplementaries:cage", 1, 30),
            q("supplementaries:sconce", "Светильник", "Тёплый свет пирса.", "supplementaries:sconce", 4, 20),
            q("supplementaries:sconce_soul", "Soul Sconce", "Холодный свет бездны.", "supplementaries:sconce_soul", 2, 25),
            q("supplementaries:brass_lantern", "Латунный фонарь", "Create-вайб освещения.", "supplementaries:brass_lantern", 2, 35),
            q("supplementaries:candle_holder", "Подсвечник", "Уют каюты.", "supplementaries:candle_holder", 4, 20),
            q("supplementaries:cog_block", "Шестерня-блок", "Декор под Create.", "supplementaries:cog_block", 4, 40),
            q("supplementaries:bellows", "Меха", "Декор/редстоун.", "supplementaries:bellows", 1, 35),
            q("supplementaries:hourglass", "Песочные часы", "Таймеры на пирсе.", "supplementaries:hourglass", 1, 30),
            q("supplementaries:urn", "Урна", "Декор гавани.", "supplementaries:urn", 2, 25),
            q("supplementaries:rope", "Верёвка", "Такелаж.", "supplementaries:rope", 16, 20),
            q("supplementaries:sign_post", "Указатель", "Навигация по атоллу — капстоун.", "supplementaries:sign_post", 4, 80),
        ],
    },
    {
        "filename": "25_ws_ocean_rituals",
        "order": 25,
        "title": "Мастерская · Ритуалы Океана",
        "icon": "aquatech_ui:ocean_altar",
        "lore": "Ocean Altar / Abyssal Portal + Ars / Botania.",
        "quests": [
            q("aquatech_ui:ocean_altar", "Океанский алтарь", "Поставь алтарь AquaTech.", "aquatech_ui:ocean_altar", 1, 40),
            q("aquatech_ui:abyssal_portal", "Абиссальный портал", "Рамка бездны.", "aquatech_ui:abyssal_portal", 1, 50),
            q("aquatech_ui:ocean_guide_book", "Энциклопедия", "Перечитай главы о горизонте.", "aquatech_ui:ocean_guide_book", 1, 20),
            q("botania:lexicon", "Лексикон", "Книга Botania.", "botania:lexicon", 1, 30),
            q("botania:pure_daisy", "Pure Daisy", "Начало живой флоры.", "botania:pure_daisy", 1, 40),
            q("botania:livingrock", "Livingrock", "Живой камень у алтаря.", "botania:livingrock", 16, 40),
            q("botania:livingwood_log", "Livingwood", "Живое дерево ритуала.", "botania:livingwood_log", 16, 40),
            q("botania:livingwood_twig", "Ветвь livingwood", "Жезлы и руны.", "botania:livingwood_twig", 8, 35),
            q("botania:mana_pool", "Пруд маны", "Хранилище маны.", "botania:mana_pool", 1, 60),
            q("botania:diluted_pool", "Разбавленный пруд", "Малый буфер маны.", "botania:diluted_pool", 1, 40),
            q("botania:mana_spreader", "Spreader", "Передача маны.", "botania:mana_spreader", 1, 55),
            q("botania:mana_tablet", "Планшет маны", "Переносной запас.", "botania:mana_tablet", 1, 50),
            q("botania:manasteel_ingot", "Манасталь", "Металл маны.", "botania:manasteel_ingot", 4, 60),
            q("botania:manasteel_block", "Блок манастали", "Склад.", "botania:manasteel_block", 1, 55),
            q("botania:apothecary_livingrock", "Апотекарий", "Лепестковые рецепты.", "botania:apothecary_livingrock", 1, 60),
            q("botania:white_petal", "Белый лепесток", "Сырьё флоры.", "botania:white_petal", 8, 25),
            q("botania:blue_petal", "Синий лепесток", "Цвет океана.", "botania:blue_petal", 8, 25),
            q("botania:rune_water", "Руна воды", "Ритуал прилива.", "botania:rune_water", 1, 70),
            q("botania:rune_earth", "Руна земли", "Ритуал атолла.", "botania:rune_earth", 1, 70),
            q("ars_nouveau:source_gem", "Source Gem", "Кристалл источника.", "ars_nouveau:source_gem", 8, 50),
            q("ars_nouveau:source_jar", "Source Jar", "Хранилище source.", "ars_nouveau:source_jar", 1, 55),
            q("ars_nouveau:magebloom", "Magebloom", "Магический цветок.", "ars_nouveau:magebloom", 8, 40),
            q("ars_nouveau:magebloom_fiber", "Волокно Magebloom", "Ткань чар.", "ars_nouveau:magebloom_fiber", 8, 45),
            q("ars_nouveau:novice_spell_book", "Книга новичка", "Первые заклинания.", "ars_nouveau:novice_spell_book", 1, 50),
            q("ars_nouveau:apprentice_spell_book", "Книга ученика", "Следующий тир.", "ars_nouveau:apprentice_spell_book", 1, 70),
            q("ars_nouveau:arcane_pedestal", "Пьедестал", "Подношения у алтаря.", "ars_nouveau:arcane_pedestal", 2, 70),
            q("ars_nouveau:imbuement_chamber", "Imbuement Chamber", "Насыщение предметов.", "ars_nouveau:imbuement_chamber", 1, 75),
            q("ars_nouveau:enchanting_apparatus", "Enchanting Apparatus", "Аппарат чар.", "ars_nouveau:enchanting_apparatus", 1, 85),
            q("ars_nouveau:ritual_brazier", "Ритуальная жаровня", "Огонь ритуала.", "ars_nouveau:ritual_brazier", 1, 100),
            q("ars_nouveau:ritual_cloudshaping", "Капстоун ритуала", "Облачный ритуал у алтаря океана.", "ars_nouveau:source_gem", 16, 140),
        ],
    },
    {
        "filename": "26_ws_mek",
        "order": 26,
        "title": "Мастерская · Mekanism",
        "icon": "mekanism:steel_casing",
        "lore": "Газовая индустрия — параллельно сюжетному Power.",
        "quests": [
            q("mekanism:ingot_osmium", "Осмий", "База Mek.", "mekanism:ingot_osmium", 8, 30),
            q("mekanism:ingot_tin", "Олово", "Сплавы.", "mekanism:ingot_tin", 8, 25),
            q("mekanism:ingot_lead", "Свинец", "Защита/сплавы.", "mekanism:ingot_lead", 8, 25),
            q("mekanism:ingot_steel", "Сталь", "Конструкционный слиток.", "mekanism:ingot_steel", 8, 35),
            q("mekanism:dust_osmium", "Пыль осмия", "Переработка.", "mekanism:dust_osmium", 8, 30),
            q("mekanism:steel_casing", "Steel Casing", "Корпус машин.", "mekanism:steel_casing", 2, 45),
            q("mekanism:basic_control_circuit", "Basic Circuit", "Электроника T1.", "mekanism:basic_control_circuit", 4, 40),
            q("mekanism:alloy_infused", "Infused Alloy", "Сплав T2.", "mekanism:alloy_infused", 4, 50),
            q("mekanism:metallurgic_infuser", "Metallurgic Infuser", "Инфузии.", "mekanism:metallurgic_infuser", 1, 55),
            q("mekanism:enrichment_chamber", "Enrichment Chamber", "Обогащение.", "mekanism:enrichment_chamber", 1, 55),
            q("mekanism:crusher", "Crusher", "Дробление.", "mekanism:crusher", 1, 50),
            q("mekanism:energized_smelter", "Energized Smelter", "Электропечь.", "mekanism:energized_smelter", 1, 55),
            q("mekanism:energy_tablet", "Energy Tablet", "Перенос энергии.", "mekanism:energy_tablet", 1, 40),
            q("mekanism:basic_energy_cube", "Energy Cube", "Буфер энергии.", "mekanism:basic_energy_cube", 1, 55),
            q("mekanism:basic_universal_cable", "Universal Cable", "Кабели энергии.", "mekanism:basic_universal_cable", 16, 40),
            q("mekanism:basic_bin", "Basic Bin", "Склад сыпучих.", "mekanism:basic_bin", 1, 40),
            q("mekanism:basic_fluid_tank", "Fluid Tank", "Жидкости.", "mekanism:basic_fluid_tank", 1, 50),
            q("mekanism:ingot_uranium", "Уран", "Топливная линия.", "mekanism:ingot_uranium", 4, 70),
            q("mekanism:elite_control_circuit", "Elite Circuit", "Электроника выше.", "mekanism:elite_control_circuit", 2, 90),
            q("mekanism:steel_casing", "Капстоун Mek", "Закрепи стальной контур завода.", "mekanism:basic_control_circuit", 8, 120),
        ],
    },
    {
        "filename": "27_ws_ae2",
        "order": 27,
        "title": "Мастерская · Applied Energistics 2",
        "icon": "ae2:controller",
        "lore": "ME-сеть — параллельно сюжетной главе ME.",
        "quests": [
            q("ae2:certus_quartz_crystal", "Certus", "Кварц сети.", "ae2:certus_quartz_crystal", 16, 30),
            q("ae2:charged_certus_quartz_crystal", "Charged Certus", "Заряженный кварц.", "ae2:charged_certus_quartz_crystal", 8, 35),
            q("ae2:fluix_dust", "Fluix Dust", "Пыль fluix.", "ae2:fluix_dust", 16, 35),
            q("ae2:fluix_crystal", "Fluix", "Сердце AE2.", "ae2:fluix_crystal", 8, 40),
            q("ae2:silicon", "Кремний", "Чипы.", "ae2:silicon", 16, 30),
            q("ae2:inscriber", "Inscriber", "Печать процессоров.", "ae2:inscriber", 1, 50),
            q("ae2:charger", "Charger", "Зарядка certus.", "ae2:charger", 1, 45),
            q("ae2:calculation_processor", "Calculation CPU", "Логика сети.", "ae2:calculation_processor", 2, 50),
            q("ae2:logic_processor", "Logic CPU", "Логический чип.", "ae2:logic_processor", 2, 50),
            q("ae2:engineering_processor", "Engineering CPU", "Инженерный чип.", "ae2:engineering_processor", 2, 60),
            q("ae2:fluix_glass_cable", "Glass Cable", "Базовые кабели.", "ae2:fluix_glass_cable", 16, 40),
            q("ae2:fluix_covered_cable", "Covered Cable", "Изолированные кабели.", "ae2:fluix_covered_cable", 16, 45),
            q("ae2:fluix_smart_cable", "Smart Cable", "Цветные каналы.", "ae2:fluix_smart_cable", 8, 50),
            q("ae2:energy_acceptor", "Energy Acceptor", "Ввод FE.", "ae2:energy_acceptor", 1, 50),
            q("ae2:energy_cell", "Energy Cell", "Буфер ME.", "ae2:energy_cell", 1, 55),
            q("ae2:drive", "ME Drive", "Хранилище ячеек.", "ae2:drive", 1, 60),
            q("ae2:cell_component_1k", "1k Component", "Первая ячейка.", "ae2:cell_component_1k", 1, 50),
            q("ae2:cell_component_4k", "4k Component", "Больше места.", "ae2:cell_component_4k", 1, 60),
            q("ae2:item_storage_cell_1k", "1k Cell", "Готовая ячейка.", "ae2:item_storage_cell_1k", 1, 55),
            q("ae2:terminal", "Terminal", "Интерфейс.", "ae2:terminal", 1, 60),
            q("ae2:crafting_terminal", "Crafting Terminal", "Крафт из сети.", "ae2:crafting_terminal", 1, 70),
            q("ae2:import_bus", "Import Bus", "Ввод в сеть.", "ae2:import_bus", 1, 55),
            q("ae2:export_bus", "Export Bus", "Вывод из сети.", "ae2:export_bus", 1, 55),
            q("ae2:interface", "ME Interface", "Мост машин.", "ae2:interface", 1, 65),
            q("ae2:pattern_provider", "Pattern Provider", "Автокрафт.", "ae2:pattern_provider", 1, 75),
            q("ae2:molecular_assembler", "Molecular Assembler", "Сборка паттернов.", "ae2:molecular_assembler", 1, 80),
            q("ae2:blank_pattern", "Blank Pattern", "Пустые схемы.", "ae2:blank_pattern", 8, 50),
            q("ae2:crafting_unit", "Crafting Unit", "CPU автокрафта.", "ae2:crafting_unit", 1, 70),
            q("ae2:controller", "ME Controller", "Мозг сети.", "ae2:controller", 1, 100),
            q("ae2:dense_energy_cell", "Капстоун AE2", "Плотный энергобуфер.", "ae2:fluix_crystal", 16, 130),
        ],
    },
    {
        "filename": "28_ws_thermal",
        "order": 28,
        "title": "Мастерская · Thermal",
        "icon": "thermal:machine_furnace",
        "lore": "Thermal Series — динамо и машины волн.",
        "quests": [
            q("thermal:rf_coil", "RF Coil", "Катушка энергии.", "thermal:rf_coil", 4, 30),
            q("thermal:redstone_servo", "Redstone Servo", "Сервопривод.", "thermal:redstone_servo", 4, 30),
            q("thermal:tin_ingot", "Олово Thermal", "Сплавы.", "thermal:tin_ingot", 8, 25),
            q("thermal:lead_ingot", "Свинец Thermal", "Сплавы.", "thermal:lead_ingot", 8, 25),
            q("thermal:silver_ingot", "Серебро", "Сплавы.", "thermal:silver_ingot", 8, 30),
            q("thermal:nickel_ingot", "Никель", "Invar линия.", "thermal:nickel_ingot", 8, 30),
            q("thermal:bronze_ingot", "Бронза", "Классический сплав.", "thermal:bronze_ingot", 8, 35),
            q("thermal:invar_ingot", "Инвар", "Жаропрочный сплав.", "thermal:invar_ingot", 8, 40),
            q("thermal:electrum_ingot", "Электрум", "Проводниковый сплав.", "thermal:electrum_ingot", 8, 45),
            q("thermal:signalum_ingot", "Сигналум", "Красный сплав.", "thermal:signalum_ingot", 4, 55),
            q("thermal:lumium_ingot", "Люмиум", "Светящийся сплав.", "thermal:lumium_ingot", 4, 55),
            q("thermal:enderium_ingot", "Эндериум", "Топ сплав Thermal.", "thermal:enderium_ingot", 4, 70),
            q("thermal:machine_furnace", "Redstone Furnace", "Печь.", "thermal:machine_furnace", 1, 50),
            q("thermal:machine_pulverizer", "Pulverizer", "Дробление.", "thermal:machine_pulverizer", 1, 50),
            q("thermal:machine_smelter", "Induction Smelter", "Сплавы.", "thermal:machine_smelter", 1, 60),
            q("thermal:machine_sawmill", "Sawmill", "Пиломатериалы.", "thermal:machine_sawmill", 1, 50),
            q("thermal:machine_press", "Press", "Штамповка.", "thermal:machine_press", 1, 55),
            q("thermal:machine_centrifuge", "Centrifuge", "Разделение.", "thermal:machine_centrifuge", 1, 60),
            q("thermal:machine_crucible", "Crucible", "Плавка жидкостей.", "thermal:machine_crucible", 1, 60),
            q("thermal:machine_insolator", "Insolator", "Агромашина.", "thermal:machine_insolator", 1, 65),
            q("thermal:machine_refinery", "Refinery", "Нефтехимия/масла.", "thermal:machine_refinery", 1, 70),
            q("thermal:machine_crafter", "Crafter", "Автокрафт Thermal.", "thermal:machine_crafter", 1, 75),
            q("thermal:dynamo_stirling", "Stirling Dynamo", "Первое динамо.", "thermal:dynamo_stirling", 1, 50),
            q("thermal:dynamo_magmatic", "Magmatic Dynamo", "Лава → RF.", "thermal:dynamo_magmatic", 1, 60),
            q("thermal:dynamo_lapidary", "Lapidary Dynamo", "Камни → RF.", "thermal:dynamo_lapidary", 1, 65),
            q("thermal:device_water_gen", "Water Gen", "Вода устройства.", "thermal:device_water_gen", 1, 40),
            q("thermal:device_rock_gen", "Rock Gen", "Камень устройства.", "thermal:device_rock_gen", 1, 45),
            q("thermal:energy_cell", "Energy Cell", "Буфер RF.", "thermal:energy_cell", 1, 70),
            q("thermal:fluid_cell", "Fluid Cell", "Буфер жидкостей.", "thermal:fluid_cell", 1, 70),
            q("thermal:machine_crystallizer", "Капстоун Thermal", "Кристаллизатор.", "thermal:enderium_ingot", 4, 130),
        ],
    },
    {
        "filename": "29_ws_if",
        "order": 29,
        "title": "Мастерская · Industrial Foregoing",
        "icon": "industrialforegoing:machine_frame_pity",
        "lore": "IF — латекс, пластик, фермы.",
        "quests": [
            q("industrialforegoing:machine_frame_pity", "Pity Frame", "Стартовая рама.", "industrialforegoing:machine_frame_pity", 1, 40),
            q("industrialforegoing:fluid_extractor", "Fluid Extractor", "Сок/латекс из дерева.", "industrialforegoing:fluid_extractor", 1, 45),
            q("industrialforegoing:latex_processing_unit", "Latex Unit", "Переработка латекса.", "industrialforegoing:latex_processing_unit", 1, 50),
            q("industrialforegoing:dryrubber", "Сухая резина", "Полуфабрикат.", "industrialforegoing:dryrubber", 16, 40),
            q("industrialforegoing:plastic", "Пластик", "Материал IF.", "industrialforegoing:plastic", 16, 45),
            q("industrialforegoing:machine_frame_simple", "Simple Frame", "Тир 2.", "industrialforegoing:machine_frame_simple", 1, 55),
            q("industrialforegoing:plant_gatherer", "Plant Gatherer", "Сбор урожая.", "industrialforegoing:plant_gatherer", 1, 60),
            q("industrialforegoing:plant_sower", "Plant Sower", "Посев.", "industrialforegoing:plant_sower", 1, 60),
            q("industrialforegoing:block_breaker", "Block Breaker", "Ломатель блоков.", "industrialforegoing:block_breaker", 1, 55),
            q("industrialforegoing:block_placer", "Block Placer", "Укладчик.", "industrialforegoing:block_placer", 1, 55),
            q("industrialforegoing:mob_slaughter_factory", "Slaughter Factory", "Переработка мобов.", "industrialforegoing:mob_slaughter_factory", 1, 70),
            q("industrialforegoing:mob_crusher", "Mob Crusher", "Дробление дропа.", "industrialforegoing:mob_crusher", 1, 70),
            q("industrialforegoing:animal_rancher", "Animal Rancher", "Автодоилка/стрижка.", "industrialforegoing:animal_rancher", 1, 60),
            q("industrialforegoing:animal_feeder", "Animal Feeder", "Кормление животных.", "industrialforegoing:animal_feeder", 1, 55),
            q("industrialforegoing:sewer", "Sewer", "Стоки фермы.", "industrialforegoing:sewer", 1, 50),
            q("industrialforegoing:sewage_composter", "Sewage Composter", "Компост.", "industrialforegoing:sewage_composter", 1, 55),
            q("industrialforegoing:bioreactor", "Bioreactor", "Биотопливо.", "industrialforegoing:bioreactor", 1, 65),
            q("industrialforegoing:biofuel_generator", "Biofuel Gen", "Генератор.", "industrialforegoing:biofuel_generator", 1, 70),
            q("industrialforegoing:machine_frame_advanced", "Advanced Frame", "Тир 3.", "industrialforegoing:machine_frame_advanced", 1, 85),
            q("industrialforegoing:laser_drill", "Laser Drill", "Ресурсный бур.", "industrialforegoing:laser_drill", 1, 95),
            q("industrialforegoing:ore_laser_base", "Ore Laser Base", "База лазера.", "industrialforegoing:ore_laser_base", 1, 90),
            q("industrialforegoing:machine_frame_supreme", "Supreme Frame", "Топ рама.", "industrialforegoing:machine_frame_supreme", 1, 110),
            q("industrialforegoing:stasis_chamber", "Stasis Chamber", "Стазис мобов.", "industrialforegoing:stasis_chamber", 1, 100),
            q("industrialforegoing:infinity_drill", "Капстоун IF", "Infinity Drill — финал ветки.", "industrialforegoing:plastic", 32, 140),
        ],
    },
    {
        "filename": "2A_ws_apotheosis",
        "order": 30,
        "title": "Мастерская · Apotheosis",
        "icon": "minecraft:enchanting_table",
        "lore": "Зачарования и библиотека.",
        "quests": [
            q("minecraft:enchanting_table", "Стол зачарований", "База Apotheosis.", "minecraft:book", 8, 30),
            q("minecraft:bookshelf", "Полки", "Набери библиотеку.", "minecraft:bookshelf", 16, 30),
            q("minecraft:lapis_lazuli", "Лазурит", "Топливо чар.", "minecraft:lapis_lazuli", 32, 25),
            q("minecraft:experience_bottle", "Бутылки опыта", "Запас XP.", "minecraft:experience_bottle", 8, 35),
            q("apotheosis:hellshelf", "Hellshelf", "Адская полка.", "apotheosis:hellshelf", 2, 50),
            q("apotheosis:seashelf", "Seashelf", "Океанская полка.", "apotheosis:seashelf", 2, 50),
            q("apotheosis:endshelf", "Endshelf", "Полка Энда.", "apotheosis:endshelf", 2, 60),
            q("apotheosis:library", "Library", "Хранилище чар.", "apotheosis:library", 1, 80),
            q("apotheosis:scrap_tome", "Scrap Tome", "Разбор чар.", "apotheosis:scrap_tome", 1, 70),
            q("minecraft:anvil", "Наковальня", "Слияние чар.", "minecraft:anvil", 1, 40),
            q("minecraft:golden_apple", "Золотое яблоко", "Ритуал силы (еда).", "minecraft:golden_apple", 4, 40),
            q("minecraft:diamond_chestplate", "Алмазный нагрудник", "Кандидат под чары.", "minecraft:diamond", 2, 50),
            q("minecraft:bow", "Лук", "Кандидат под Apotheosis.", "minecraft:arrow", 32, 35),
            q("minecraft:netherite_ingot", "Незерит", "Топ материал.", "minecraft:netherite_ingot", 1, 80),
            q("minecraft:netherite_sword", "Незеритовый клинок", "Зачаруй клинок флота.", "minecraft:diamond", 2, 90),
            q("minecraft:netherite_chestplate", "Незеритовая броня", "Капстоун Apotheosis-ветки.", "minecraft:netherite_ingot", 1, 120),
        ],
    },
    {
        "filename": "2B_ws_enderio",
        "order": 31,
        "title": "Мастерская · EnderIO",
        "icon": "enderio:void_chassis",
        "lore": "Кондуиты и сплавы EnderIO.",
        "quests": [
            q("enderio:grains_of_infinity", "Grains of Infinity", "Пыль бесконечности.", "enderio:grains_of_infinity", 8, 30),
            q("enderio:powdered_coal", "Угольная пыль", "Сырьё сплавов.", "enderio:powdered_coal", 16, 20),
            q("enderio:powdered_iron", "Железная пыль", "Сырьё.", "enderio:powdered_iron", 16, 25),
            q("enderio:powdered_gold", "Золотая пыль", "Сырьё.", "enderio:powdered_gold", 8, 30),
            q("enderio:powdered_copper", "Медная пыль", "Сырьё.", "enderio:powdered_copper", 16, 25),
            q("enderio:copper_alloy_ingot", "Copper Alloy", "Сплав EIO.", "enderio:copper_alloy_ingot", 8, 40),
            q("enderio:energetic_alloy_ingot", "Energetic Alloy", "Энергосплав.", "enderio:energetic_alloy_ingot", 4, 50),
            q("enderio:vibrant_alloy_ingot", "Vibrant Alloy", "Яркий сплав.", "enderio:vibrant_alloy_ingot", 4, 60),
            q("enderio:redstone_alloy_ingot", "Redstone Alloy", "Сигнальный сплав.", "enderio:redstone_alloy_ingot", 8, 40),
            q("enderio:conductive_alloy_ingot", "Conductive Alloy", "Проводник.", "enderio:conductive_alloy_ingot", 8, 45),
            q("enderio:pulsating_alloy_ingot", "Pulsating Alloy", "Пульсирующий сплав.", "enderio:pulsating_alloy_ingot", 4, 55),
            q("enderio:dark_steel_ingot", "Dark Steel", "Тёмная сталь.", "enderio:dark_steel_ingot", 8, 50),
            q("enderio:soularium_ingot", "Soularium", "Сплав душ.", "enderio:soularium_ingot", 4, 55),
            q("enderio:void_chassis", "Void Chassis", "Корпус машин.", "enderio:void_chassis", 1, 55),
            q("enderio:ensouled_chassis", "Ensouled Chassis", "Одушевлённый корпус.", "enderio:ensouled_chassis", 1, 70),
            q("enderio:basic_capacitor", "Basic Capacitor", "Ёмкость T1.", "enderio:basic_capacitor", 2, 40),
            q("enderio:double_layer_capacitor", "Double Capacitor", "Ёмкость T2.", "enderio:double_layer_capacitor", 1, 55),
            q("enderio:octadic_capacitor", "Octadic Capacitor", "Ёмкость T3.", "enderio:octadic_capacitor", 1, 75),
            q("enderio:sag_mill", "SAG Mill", "Дробление.", "enderio:sag_mill", 1, 60),
            q("enderio:alloy_smelter", "Alloy Smelter", "Сплавление.", "enderio:alloy_smelter", 1, 65),
            q("enderio:stirling_generator", "Stirling Gen", "Генератор.", "enderio:stirling_generator", 1, 55),
            q("enderio:conduit_binder", "Conduit Binder", "Связка кондуитов.", "enderio:conduit_binder", 16, 50),
            q("enderio:item_conduit", "Item Conduit", "Предметные трубы.", "enderio:item_conduit", 8, 60),
            q("enderio:fluid_conduit", "Fluid Conduit", "Жидкостные трубы.", "enderio:fluid_conduit", 8, 60),
            q("enderio:energy_conduit", "Energy Conduit", "Энерготрубы.", "enderio:energy_conduit", 8, 65),
            q("enderio:redstone_conduit", "Redstone Conduit", "Сигналы.", "enderio:redstone_conduit", 8, 55),
            q("enderio:yeta_wrench", "Yeta Wrench", "Настройка кондуитов.", "enderio:yeta_wrench", 1, 50),
            q("enderio:conduit_probe", "Conduit Probe", "Диагностика сети.", "enderio:conduit_probe", 1, 55),
            q("enderio:vibrant_alloy_ingot", "Капстоун EIO", "Закрепи vibrant-линию.", "enderio:conduit_binder", 32, 130),
        ],
    },
    {
        "filename": "2C_ws_avaritia",
        "order": 32,
        "title": "Мастерская · Avaritia",
        "icon": "avaritia:infinity_catalyst",
        "lore": "Re:Avaritia — сингулярности, нейтроний, Infinity. Эндгейм флота.",
        "quests": [
            q("avaritia:diamond_lattice", "Алмазная решётка", "Каркас кристальной матрицы.", "avaritia:diamond_lattice", 4, 40),
            q("avaritia:crystal_matrix_ingot", "Crystal Matrix Ingot", "Слиток матрицы.", "avaritia:crystal_matrix_ingot", 2, 55),
            q("avaritia:crystal_matrix", "Crystal Matrix Block", "Блок матрицы.", "avaritia:crystal_matrix", 1, 60),
            q("avaritia:compressed_crafting_table", "Compressed Table", "Сжатый верстак.", "avaritia:compressed_crafting_table", 1, 50),
            q("avaritia:double_compressed_crafting_table", "Double Compressed", "Двойное сжатие.", "avaritia:double_compressed_crafting_table", 1, 60),
            q("avaritia:extreme_crafting_table", "Extreme Crafting Table", "9×9 крафт Avaritia.", "avaritia:extreme_crafting_table", 1, 80),
            q("avaritia:neutron_collector", "Neutron Collector", "Собирает нейтроний из пустоты.", "avaritia:neutron_collector", 1, 70),
            q("avaritia:neutron_pile", "Neutron Pile", "Пыль нейтрония.", "avaritia:neutron_pile", 16, 55),
            q("avaritia:neutron_nugget", "Neutron Nugget", "Самородки.", "avaritia:neutron_nugget", 8, 60),
            q("avaritia:neutron_ingot", "Neutronium Ingot", "Слиток нейтрония.", "avaritia:neutron_ingot", 4, 75),
            q("avaritia:neutron", "Neutronium Block", "Блок нейтрония.", "avaritia:neutron", 1, 85),
            q("avaritia:neutron_compressor", "Neutron Compressor", "Сжимает в сингулярности.", "avaritia:neutron_compressor", 1, 90),
            q("avaritia:dense_neutron_collector", "Dense Collector", "Быстрее сбор.", "avaritia:dense_neutron_collector", 1, 85),
            q("avaritia:record_fragment", "Осколок пластинки", "Для катализатора.", "avaritia:record_fragment", 4, 50),
            q("avaritia:ultimate_stew", "Ultimate Stew", "Еда эндгейма.", "avaritia:ultimate_stew", 2, 60),
            q("avaritia:cosmic_meatballs", "Cosmic Meatballs", "Космические фрикадельки.", "avaritia:cosmic_meatballs", 2, 60),
            q("avaritia:star_fuel", "Star Fuel", "Топливо звёзд.", "avaritia:star_fuel", 4, 70),
            q("avaritia:singularity", "Сингулярность", "Сжатый ресурс (любая).", "avaritia:singularity", 1, 80),
            q("avaritia:eternal_singularity", "Eternal Singularity", "Вечная сингулярность.", "avaritia:eternal_singularity", 1, 110),
            q("avaritia:infinity_catalyst", "Infinity Catalyst", "Катализатор бесконечности.", "avaritia:infinity_catalyst", 1, 120),
            q("avaritia:infinity_ingot", "Infinity Ingot", "Слиток бесконечности.", "avaritia:infinity_ingot", 1, 130),
            q("avaritia:infinity_nugget", "Infinity Nugget", "Самородки Infinity.", "avaritia:infinity_nugget", 4, 100),
            q("avaritia:extreme_smithing_table", "Extreme Smithing", "Ковка Infinity.", "avaritia:extreme_smithing_table", 1, 110),
            q("avaritia:enhancement_core", "Enhancement Core", "Ядро усиления.", "avaritia:enhancement_core", 1, 100),
            q("avaritia:infinity_pickaxe", "Infinity Pickaxe", "Кирка без дна.", "avaritia:infinity_pickaxe", 1, 140),
            q("avaritia:infinity_sword", "Infinity Sword", "Клинок конца споров.", "avaritia:infinity_sword", 1, 150),
            q("avaritia:infinity_helmet", "Infinity Helmet", "Шлем бесконечности.", "avaritia:infinity_helmet", 1, 140),
            q("avaritia:infinity_chestplate", "Infinity Chestplate", "Нагрудник.", "avaritia:infinity_chestplate", 1, 150),
            q("avaritia:infinity_pants", "Infinity Pants", "Поножи.", "avaritia:infinity_pants", 1, 140),
            q("avaritia:infinity_boots", "Infinity Boots", "Сапоги.", "avaritia:infinity_boots", 1, 140),
            q("avaritia:infinity_bow", "Infinity Bow", "Лук без дна.", "avaritia:infinity_bow", 1, 145),
            q("avaritia:infinity_totem", "Infinity Totem", "Тотем бесконечности.", "avaritia:infinity_totem", 1, 130),
            q("avaritia:compressed_chest", "Compressed Chest", "Сжатый сундук.", "avaritia:compressed_chest", 1, 80),
            q("avaritia_armor:crystal_core", "Crystal Core (Armor)", "Ядро Crystal-брони.", "avaritia_armor:crystal_core", 1, 90),
            q("avaritia_armor:crystal_helmet", "Crystal Helmet", "Аддон-броня Avaritia.", "avaritia_armor:crystal_helmet", 1, 100),
            q("avaritia_armor:crystal_chestplate", "Crystal Chestplate", "Нагрудник Crystal.", "avaritia_armor:crystal_chestplate", 1, 110),
            q("avaritia:infinity_catalyst", "Капстоун Avaritia", "Infinity-линия собрана.", "avaritia:infinity_ingot", 1, 200),
        ],
    },
    {
        "filename": "2D_ws_draconic",
        "order": 33,
        "title": "Мастерская · Draconic Evolution",
        "icon": "draconicevolution:draconium_core",
        "lore": "Draconium → Wyvern → Awakened → Chaotic. Энергия и оружие дракона.",
        "quests": [
            q("draconicevolution:info_tablet", "Info Tablet", "Гайд DE в игре.", "draconicevolution:info_tablet", 1, 30),
            q("draconicevolution:draconium_dust", "Draconium Dust", "Пыль руды.", "draconicevolution:draconium_dust", 16, 40),
            q("draconicevolution:draconium_ingot", "Draconium Ingot", "Слиток.", "draconicevolution:draconium_ingot", 8, 50),
            q("draconicevolution:draconium_block", "Draconium Block", "Блок склада.", "draconicevolution:draconium_block", 2, 55),
            q("draconicevolution:draconium_core", "Draconium Core", "Ядро T1.", "draconicevolution:draconium_core", 2, 60),
            q("draconicevolution:wyvern_core", "Wyvern Core", "Ядро T2.", "draconicevolution:wyvern_core", 1, 80),
            q("draconicevolution:module_core", "Module Core", "База модулей.", "draconicevolution:module_core", 2, 55),
            q("draconicevolution:basic_crafting_injector", "Basic Injector", "Инжектор крафта.", "draconicevolution:basic_crafting_injector", 1, 65),
            q("draconicevolution:crafting_core", "Crafting Core", "Сердце fusion-крафта.", "draconicevolution:crafting_core", 1, 75),
            q("draconicevolution:crystal_binder", "Crystal Binder", "Связка кристаллов сети.", "draconicevolution:crystal_binder", 1, 50),
            q("draconicevolution:basic_relay_crystal", "Relay Crystal", "Передача энергии.", "draconicevolution:basic_relay_crystal", 4, 55),
            q("draconicevolution:basic_io_crystal", "IO Crystal", "Ввод/вывод энергии.", "draconicevolution:basic_io_crystal", 2, 55),
            q("draconicevolution:energy_core", "Energy Core", "Буфер энергии DE.", "draconicevolution:energy_core", 1, 80),
            q("draconicevolution:energy_core_stabilizer", "Stabilizer", "Стабилизация ядра.", "draconicevolution:energy_core_stabilizer", 4, 70),
            q("draconicevolution:energy_pylon", "Energy Pylon", "Пилон ввода/вывода.", "draconicevolution:energy_pylon", 2, 70),
            q("draconicevolution:generator", "DE Generator", "Стартовый генератор.", "draconicevolution:generator", 1, 50),
            q("draconicevolution:grinder", "Grinder", "Ферма мобов DE.", "draconicevolution:grinder", 1, 65),
            q("draconicevolution:draconium_chest", "Draconium Chest", "Умный сундук.", "draconicevolution:draconium_chest", 1, 70),
            q("draconicevolution:wyvern_energy_core", "Wyvern Energy Core", "Энергоядро T2.", "draconicevolution:wyvern_energy_core", 1, 85),
            q("draconicevolution:wyvern_capacitor", "Wyvern Capacitor", "Ёмкость T2.", "draconicevolution:wyvern_capacitor", 1, 80),
            q("draconicevolution:wyvern_crafting_injector", "Wyvern Injector", "Инжектор T2.", "draconicevolution:wyvern_crafting_injector", 1, 90),
            q("draconicevolution:wyvern_pickaxe", "Wyvern Pickaxe", "Кирка виверны.", "draconicevolution:wyvern_pickaxe", 1, 95),
            q("draconicevolution:wyvern_sword", "Wyvern Sword", "Клинок виверны.", "draconicevolution:wyvern_sword", 1, 95),
            q("draconicevolution:wyvern_chestpiece", "Wyvern Chestpiece", "Броня виверны.", "draconicevolution:wyvern_chestpiece", 1, 100),
            q("draconicevolution:item_wyvern_aoe", "Модуль AoE", "Зона копания.", "draconicevolution:item_wyvern_aoe", 1, 70),
            q("draconicevolution:item_wyvern_flight", "Модуль полёта", "Полёт виверны.", "draconicevolution:item_wyvern_flight", 1, 85),
            q("draconicevolution:awakened_draconium_ingot", "Awakened Ingot", "Пробуждённый слиток.", "draconicevolution:awakened_draconium_ingot", 4, 110),
            q("draconicevolution:awakened_core", "Awakened Core", "Ядро T3.", "draconicevolution:awakened_core", 1, 120),
            q("draconicevolution:awakened_crafting_injector", "Awakened Injector", "Инжектор T3.", "draconicevolution:awakened_crafting_injector", 1, 120),
            q("draconicevolution:draconic_energy_core", "Draconic Energy Core", "Энергоядро T3.", "draconicevolution:draconic_energy_core", 1, 115),
            q("draconicevolution:draconic_pickaxe", "Draconic Pickaxe", "Кирка дракона.", "draconicevolution:draconic_pickaxe", 1, 125),
            q("draconicevolution:draconic_sword", "Draconic Sword", "Клинок дракона.", "draconicevolution:draconic_sword", 1, 125),
            q("draconicevolution:draconic_chestpiece", "Draconic Chestpiece", "Броня дракона.", "draconicevolution:draconic_chestpiece", 1, 130),
            q("draconicevolution:dislocator", "Dislocator", "Телепорт DE.", "draconicevolution:dislocator", 1, 80),
            q("draconicevolution:advanced_dislocator", "Advanced Dislocator", "Умный телепорт.", "draconicevolution:advanced_dislocator", 1, 100),
            q("draconicevolution:chaos_shard", "Chaos Shard", "Осколок хаоса.", "draconicevolution:chaos_shard", 1, 140),
            q("draconicevolution:chaotic_core", "Chaotic Core", "Ядро T4.", "draconicevolution:chaotic_core", 1, 160),
            q("draconicevolution:reactor_core", "Reactor Core", "Ядро реактора.", "draconicevolution:reactor_core", 1, 150),
            q("draconicevolution:reactor_stabilizer", "Reactor Stabilizer", "Стабилизатор реактора.", "draconicevolution:reactor_stabilizer", 1, 155),
            q("draconicevolution:chaotic_pickaxe", "Капстоун DE", "Хаотическая кирка — финал ветки.", "draconicevolution:awakened_draconium_ingot", 4, 200),
        ],
    },
    {
        "filename": "2E_ws_botania_plus",
        "order": 34,
        "title": "Мастерская · Botania+",
        "icon": "mythicbotany:alfsteel_ingot",
        "lore": "MythicBotany + Botanical Machinery + ExtraBotany — аддоны флоры.",
        "quests": [
            q("botania:lexicon", "Лексикон снова", "Книга Botania под рукой.", "botania:lexicon", 1, 25),
            q("mythicbotany:mana_infuser", "Mana Infuser", "Инфузия → Alfsteel.", "mythicbotany:mana_infuser", 1, 70),
            q("mythicbotany:alfsteel_ingot", "Alfsteel Ingot", "Слиток альвов.", "mythicbotany:alfsteel_ingot", 2, 80),
            q("mythicbotany:alfsteel_nugget", "Alfsteel Nugget", "Мелочь альвов.", "mythicbotany:alfsteel_nugget", 8, 60),
            q("mythicbotany:alfsteel_block", "Alfsteel Block", "Блок склада.", "mythicbotany:alfsteel_block", 1, 75),
            q("mythicbotany:alfsteel_pylon", "Alfsteel Pylon", "Пилон альвов.", "mythicbotany:alfsteel_pylon", 1, 85),
            q("mythicbotany:alfsteel_pick", "Alfsteel Pick", "Кирка альвов.", "mythicbotany:alfsteel_pick", 1, 90),
            q("mythicbotany:alfsteel_sword", "Alfsteel Sword", "Клинок альвов.", "mythicbotany:alfsteel_sword", 1, 90),
            q("mythicbotany:alfsteel_helmet", "Alfsteel Helmet", "Шлем.", "mythicbotany:alfsteel_helmet", 1, 95),
            q("mythicbotany:alfsteel_chestplate", "Alfsteel Chestplate", "Нагрудник.", "mythicbotany:alfsteel_chestplate", 1, 100),
            q("mythicbotany:alfsteel_leggings", "Alfsteel Leggings", "Поножи.", "mythicbotany:alfsteel_leggings", 1, 95),
            q("mythicbotany:alfsteel_boots", "Alfsteel Boots", "Сапоги.", "mythicbotany:alfsteel_boots", 1, 95),
            q("mythicbotany:mana_collector", "Mana Collector", "Сбор маны Mythic.", "mythicbotany:mana_collector", 1, 70),
            q("mythicbotany:midgard_rune", "Руна Midgard", "Скандинавская руна.", "mythicbotany:midgard_rune", 1, 75),
            q("mythicbotany:alfheim_rune", "Руна Alfheim", "Руна альвов.", "mythicbotany:alfheim_rune", 1, 80),
            q("mythicbotany:helheim_rune", "Руна Helheim", "Тёмная руна.", "mythicbotany:helheim_rune", 1, 80),
            q("mythicbotany:asgard_rune", "Руна Asgard", "Высшая руна.", "mythicbotany:asgard_rune", 1, 90),
            q("mythicbotany:fimbultyr_tablet", "Fimbultyr Tablet", "Скрижаль мифа.", "mythicbotany:fimbultyr_tablet", 1, 100),
            q("botanicalmachinery:mechanical_daisy", "Mechanical Daisy", "Авто-daisy.", "botanicalmachinery:mechanical_daisy", 1, 70),
            q("botanicalmachinery:mechanical_apothecary", "Mechanical Apothecary", "Авто-апотекарий.", "botanicalmachinery:mechanical_apothecary", 1, 75),
            q("botanicalmachinery:mechanical_runic_altar", "Mechanical Runic Altar", "Авто-руны.", "botanicalmachinery:mechanical_runic_altar", 1, 80),
            q("botanicalmachinery:mechanical_mana_pool", "Mechanical Mana Pool", "Авто-пул.", "botanicalmachinery:mechanical_mana_pool", 1, 80),
            q("botanicalmachinery:mechanical_brewery", "Mechanical Brewery", "Авто-варево.", "botanicalmachinery:mechanical_brewery", 1, 75),
            q("botanicalmachinery:mechanical_mana_infuser", "Mechanical Infuser", "Авто-инфузия.", "botanicalmachinery:mechanical_mana_infuser", 1, 85),
            q("botanicalmachinery:alfheim_market", "Alfheim Market", "Обмен с альвами.", "botanicalmachinery:alfheim_market", 1, 80),
            q("botanicalmachinery:industrial_agglomeration_factory", "Agglomeration Factory", "Авто-террасталь.", "botanicalmachinery:industrial_agglomeration_factory", 1, 90),
            q("botanicalmachinery:mana_battery", "Mana Battery", "Буфер маны.", "botanicalmachinery:mana_battery", 1, 75),
            q("botanicalmachinery:mana_emerald", "Mana Emerald", "Изумруд маны.", "botanicalmachinery:mana_emerald", 4, 60),
            q("botanicalextramachinery:base_mana_pool", "Base Mana Pool+", "Тир пула Extra.", "botanicalextramachinery:base_mana_pool", 1, 70),
            q("botanicalextramachinery:advanced_mana_pool", "Advanced Mana Pool+", "Продвинутый пул.", "botanicalextramachinery:advanced_mana_pool", 1, 90),
            q("botanicalextramachinery:ultimate_mana_pool", "Ultimate Mana Pool+", "Топ пул.", "botanicalextramachinery:ultimate_mana_pool", 1, 110),
            q("botanicalextramachinery:catalyst_speed", "Catalyst Speed", "Ускорение машин.", "botanicalextramachinery:catalyst_speed", 1, 70),
            q("botanicalextramachinery:upgrade_flower_4x", "Upgrade Flower 4×", "Больше цветов.", "botanicalextramachinery:upgrade_flower_4x", 1, 75),
            q("extrabotany:spirit_fuel", "Spirit Fuel", "Топливо ExtraBotany.", "extrabotany:spirit_fuel", 8, 50),
            q("extrabotany:spirit_fragment", "Spirit Fragment", "Осколок духа.", "extrabotany:spirit_fragment", 8, 55),
            q("extrabotany:annoyingflower", "Annoying Flower", "Цветок ExtraBotany.", "extrabotany:annoyingflower", 1, 60),
            q("extrabotany:bellflower", "Bellflower", "Колокольчик.", "extrabotany:bellflower", 1, 60),
            q("extrabotany:aerialite_ingot", "Aerialite", "Слиток неба.", "extrabotany:aerialite_ingot", 4, 80),
            q("extrabotany:shadowium_ingot", "Shadowium", "Теневой слиток.", "extrabotany:shadowium_ingot", 4, 90),
            q("extrabotany:photonium_ingot", "Photonium", "Световой слиток.", "extrabotany:photonium_ingot", 4, 95),
            q("extrabotany:orichalcos_ingot", "Orichalcos", "Топ-слиток Extra.", "extrabotany:orichalcos_ingot", 2, 120),
            q("extrabotany:hero_medal", "Hero Medal", "Медаль героя.", "extrabotany:hero_medal", 1, 110),
            q("extrabotany:excalibur", "Excalibur", "Клинок ExtraBotany.", "extrabotany:excalibur", 1, 140),
            q("extrabotany:orichalcos_ingot", "Капстоун Botania+", "Закрепи orichalcos-линию.", "extrabotany:orichalcos_ingot", 2, 200),
        ],
    },
    {
        "filename": "2F_ws_industrial_upgrade",
        "order": 35,
        "title": "Мастерская · Industrial Upgrade",
        "icon": "industrialupgrade:forge_hammer",
        "lore": "Гайд Industrial Upgrade для новичка: примитив → пар → электрика → завод → энергия. Карьеры отключены.",
        "quests": [
            # --- I · Примитив ---
            q("industrialupgrade:book/guide_book", "Книга-гайд IU", "Открой guide book — это карта всего мода.", "industrialupgrade:book/guide_book", 1, 25),
            q("industrialupgrade:forge_hammer", "Кузнечный молот", "Этап I · Примитив. Молот штампует пластины.", "industrialupgrade:forge_hammer", 1, 30),
            q("industrialupgrade:block_anvil/block_anvil", "Наковальня IU", "Ставь наковальню: молот + слиток → пластина.", "industrialupgrade:block_anvil/block_anvil", 1, 35),
            q("industrialupgrade:cutter", "Кусачки", "Режут пластины в кабели (с резиной/изоляцией).", "industrialupgrade:cutter", 1, 30),
            q("industrialupgrade:tools/treetap", "Добыватель смолы", "ПКМ по резиновому дереву → latex/смола.", "industrialupgrade:tools/treetap", 1, 30),
            q("industrialupgrade:energy/wrench", "Гаечный ключ IU", "Крутит/снимает машины без ломания.", "industrialupgrade:energy/wrench", 1, 35),
            q("industrialupgrade:itemingots/copper_ingot", "Медь IU", "Слиток меди (своя руда/переработка IU).", "industrialupgrade:itemingots/copper_ingot", 16, 35),
            q("industrialupgrade:itemingots/tin_ingot", "Олово IU", "Нужно для бронзы и кабелей.", "industrialupgrade:itemingots/tin_ingot", 16, 35),
            q("industrialupgrade:itemingots/bronze_ingot", "Бронза", "Медь+олово. Металл примитивного века.", "industrialupgrade:itemingots/bronze_ingot", 16, 40),
            q("industrialupgrade:itemplates/bronze_plate", "Бронзовая пластина", "Молот + наковальня (или rolling позже).", "industrialupgrade:itemplates/bronze_plate", 8, 40),
            q("industrialupgrade:itemplates/iron_plate", "Железная пластина", "База корпусов и механизмов.", "industrialupgrade:itemplates/iron_plate", 8, 40),
            q("industrialupgrade:itemplates/copper_plate", "Медная пластина", "Под кабели и детали.", "industrialupgrade:itemplates/copper_plate", 8, 40),
            q("industrialupgrade:itemplates/tin_plate", "Оловянная пластина", "Детали и сплавы.", "industrialupgrade:itemplates/tin_plate", 8, 40),
            q("industrialupgrade:raw_latex", "Сырой латекс", "С treetap. Путь к резине/изоляции.", "industrialupgrade:raw_latex", 8, 35),
            q("industrialupgrade:synthetic_rubber", "Синтетическая резина", "Изоляция кабелей и сапоги.", "industrialupgrade:synthetic_rubber", 8, 45),
            # --- II · Пар ---
            q("industrialupgrade:blockresource/steam_machine", "Паровой корпус", "Этап II · Пар. Корпус паровых машин.", "industrialupgrade:blockresource/steam_machine", 1, 50),
            q("industrialupgrade:basemachine3/steam_generator", "Паровой генератор", "Пар → энергия. Нужна вода + топливо/котёл.", "industrialupgrade:basemachine3/steam_generator", 1, 55),
            q("industrialupgrade:basemachine3/steam_macerator", "Паровая дробилка", "Дробит руду без электричества.", "industrialupgrade:basemachine3/steam_macerator", 1, 55),
            q("industrialupgrade:basemachine3/steam_compressor", "Паровой компрессор", "Сжимает пыль/пластины паром.", "industrialupgrade:basemachine3/steam_compressor", 1, 55),
            q("industrialupgrade:basemachine3/steam_extractor", "Паровой экстрактор", "Вытяжка материалов на пару.", "industrialupgrade:basemachine3/steam_extractor", 1, 55),
            q("industrialupgrade:basemachine3/steam_rolling", "Паровой прокат", "Пластины паром — быстрее молота.", "industrialupgrade:basemachine3/steam_rolling", 1, 60),
            q("industrialupgrade:basemachine3/steam_cutting", "Паровая резка", "Резка деталей на пару.", "industrialupgrade:basemachine3/steam_cutting", 1, 55),
            q("industrialupgrade:basemachine3/steam_extruder", "Паровой экструдер", "Профили/детали давлением пара.", "industrialupgrade:basemachine3/steam_extruder", 1, 55),
            q("industrialupgrade:block_strong_anvil/block_strong_anvil", "Усиленная наковальня", "Тяжёлая обработка металла.", "industrialupgrade:block_strong_anvil/block_strong_anvil", 1, 60),
            q("industrialupgrade:blastfurnace/blast_furnace_main", "Доменный блок", "Ядро доменной печи (многоблочная).", "industrialupgrade:blastfurnace/blast_furnace_main", 1, 70),
            q("industrialupgrade:blastfurnace/blast_furnace_part", "Часть домны", "Стены/части структуры домны.", "industrialupgrade:blastfurnace/blast_furnace_part", 8, 65),
            q("industrialupgrade:itemingots/steel_ingot", "Сталь IU", "Из домны/сплавов — тир выше железа.", "industrialupgrade:itemingots/steel_ingot", 8, 70),
            q("industrialupgrade:itemplates/steel_plate", "Стальная пластина", "Прочные корпуса.", "industrialupgrade:itemplates/steel_plate", 8, 70),
            # --- III · Электрика ---
            q("industrialupgrade:blockresource/machine", "Machine Block", "Этап III · Электрика. Базовый корпус машин.", "industrialupgrade:blockresource/machine", 2, 60),
            q("industrialupgrade:cable/copper_cable", "Медный кабель", "Первая проводка EU. Режь пластины cutter’ом.", "industrialupgrade:cable/copper_cable", 16, 50),
            q("industrialupgrade:cable/tin_cable", "Оловянный кабель", "Дешёвая линия малой мощности.", "industrialupgrade:cable/tin_cable", 16, 50),
            q("industrialupgrade:cable/gold_cable", "Золотой кабель", "Средняя пропускная способность.", "industrialupgrade:cable/gold_cable", 8, 55),
            q("industrialupgrade:cable/iron_cable", "Железный кабель", "Выше ток — смотри потери/изоляцию.", "industrialupgrade:cable/iron_cable", 8, 55),
            q("industrialupgrade:cable/glass_cable", "Стекловолоконный кабель", "Дальняя передача с малыми потерями.", "industrialupgrade:cable/glass_cable", 8, 65),
            q("industrialupgrade:crafting_elements/crafting_272_element", "Electronic Circuit", "Базовая схема. Крафт в JEI/книге.", "industrialupgrade:crafting_elements/crafting_272_element", 4, 70),
            q("industrialupgrade:crafting_elements/crafting_276_element", "Electric Motor", "Мотор для машин и генераторов.", "industrialupgrade:crafting_elements/crafting_276_element", 2, 65),
            q("industrialupgrade:crafting_elements/crafting_294_element", "Катушка (Coil)", "Обмотка для моторов/трансформаторов.", "industrialupgrade:crafting_elements/crafting_294_element", 4, 60),
            q("industrialupgrade:crafting_elements/crafting_137_element", "Machine Casing", "Обшивка корпуса машины.", "industrialupgrade:crafting_elements/crafting_137_element", 2, 65),
            q("industrialupgrade:battery/re_battery", "Re-Battery", "Переносной аккумулятор EU.", "industrialupgrade:battery/re_battery", 1, 55),
            q("industrialupgrade:wiring_storage/batbox_iu", "BatBox", "Малый стационарный буфер энергии.", "industrialupgrade:wiring_storage/batbox_iu", 1, 60),
            q("industrialupgrade:basemachine3/generator_iu", "Генератор IU", "Уголь/топливо → EU. Первый ток.", "industrialupgrade:basemachine3/generator_iu", 1, 70),
            q("industrialupgrade:simplemachine/furnace_iu", "Электропечь IU", "Плавка на EU быстрее ванили.", "industrialupgrade:simplemachine/furnace_iu", 1, 65),
            q("industrialupgrade:simplemachine/macerator_iu", "Macerator", "Руда → пыль (больше выхода).", "industrialupgrade:simplemachine/macerator_iu", 1, 70),
            q("industrialupgrade:compressor/compressor", "Compressor", "Пыль/слитки → пластины/блоки.", "industrialupgrade:compressor/compressor", 1, 70),
            q("industrialupgrade:simplemachine/extractor_iu", "Extractor", "Вытяжка (резина и др.).", "industrialupgrade:simplemachine/extractor_iu", 1, 70),
            # --- IV · Завод ---
            q("industrialupgrade:moremachine2/rolling", "Прокатный стан", "Этап IV · Завод. Пластины автоматом.", "industrialupgrade:moremachine2/rolling", 1, 75),
            q("industrialupgrade:moremachine2/cutting", "Резательный станок", "Точная резка деталей.", "industrialupgrade:moremachine2/cutting", 1, 75),
            q("industrialupgrade:moremachine2/extruder", "Экструдер", "Профили и заготовки.", "industrialupgrade:moremachine2/extruder", 1, 75),
            q("industrialupgrade:basemachine/alloy_smelter", "Alloy Smelter", "Сплавы (бронза и выше) в машине.", "industrialupgrade:basemachine/alloy_smelter", 1, 80),
            q("industrialupgrade:moremachine1/comb_macerator", "Combined Macerator", "Усиленное дробление руд.", "industrialupgrade:moremachine1/comb_macerator", 1, 85),
            q("industrialupgrade:blockresource/advanced_machine", "Advanced Machine Block", "Корпус тира Advanced.", "industrialupgrade:blockresource/advanced_machine", 1, 85),
            q("industrialupgrade:crafting_elements/crafting_273_element", "Advanced Circuit", "Схема тира 2. Нужна для апгрейдов.", "industrialupgrade:crafting_elements/crafting_273_element", 2, 90),
            q("industrialupgrade:battery/advanced_re_battery", "Advanced Battery", "Ёмче переносной буфер.", "industrialupgrade:battery/advanced_re_battery", 1, 80),
            q("industrialupgrade:wiring_storage/cesu_iu", "CESU", "Средний энергобуфер.", "industrialupgrade:wiring_storage/cesu_iu", 1, 85),
            q("industrialupgrade:wiring_storage/mfe_iu", "MFE", "Большой энергобуфер.", "industrialupgrade:wiring_storage/mfe_iu", 1, 95),
            q("industrialupgrade:wiring_storage/mfsu_iu", "MFSU", "Очень большой буфер завода.", "industrialupgrade:wiring_storage/mfsu_iu", 1, 110),
            q("industrialupgrade:transformer_iu/lv", "Трансформатор", "Меняет напряжение линии EU.", "industrialupgrade:transformer_iu/lv", 1, 90),
            q("industrialupgrade:basemachine3/scanner_iu", "Ore Scanner", "Сканер рудных жил IU (стартовый гаджет).", "industrialupgrade:basemachine3/scanner_iu", 1, 80),
            q("industrialupgrade:basemachine3/adv_scanner", "Advanced Scanner", "Лучше сканирует жилы.", "industrialupgrade:basemachine3/adv_scanner", 1, 90),
            q("industrialupgrade:synthetic_plate", "Пластиковая пластина", "Химия/переработка → пластик.", "industrialupgrade:synthetic_plate", 8, 85),
            q("industrialupgrade:electronics_assembler/electronics_assembler", "Electronics Assembler", "Сборка схем и электроники.", "industrialupgrade:electronics_assembler/electronics_assembler", 1, 95),
            # --- V · Энергия / аддоны (без карьеров) ---
            q("industrialupgrade:basemachine3/minipanel", "Mini Solar Panel", "Этап V · Энергия. Маленькая панель.", "industrialupgrade:basemachine3/minipanel", 1, 70),
            q("industrialupgrade:basemachine3/solar_iu", "Low Solar Panel", "Слабая дневная генерация.", "industrialupgrade:basemachine3/solar_iu", 1, 80),
            q("industrialupgrade:machines/advanced_solar_paneliu", "Advanced Solar Panel", "Серьёзная солнечная станция.", "industrialupgrade:machines/advanced_solar_paneliu", 1, 100),
            q("industrialupgrade:machines/hybrid_solar_paneliu", "Hybrid Solar Panel", "Сильнее Advanced.", "industrialupgrade:machines/hybrid_solar_paneliu", 1, 110),
            q("industrialupgrade:itemsunnarium/sunnarium_plate", "Solarite Plate", "Материал топ-панелей.", "industrialupgrade:itemsunnarium/sunnarium_plate", 4, 100),
            q("industrialupgrade:battery/energy_crystal", "Energy Crystal", "Кристалл энергии mid-game.", "industrialupgrade:battery/energy_crystal", 1, 90),
            q("industrialupgrade:battery/lapotron_crystal", "Lapotron Crystal", "Топ-кристалл энергии.", "industrialupgrade:battery/lapotron_crystal", 1, 110),
            q("powerutils:power_utilities", "Power Converter", "Аддон: EU ↔ RF/FE/TE. Мост к Create/Mek.", "powerutils:power_utilities", 1, 100),
            q("powerutils:module_fe", "Модуль FE", "Вставь в конвертер под Forge Energy.", "powerutils:module_fe", 1, 85),
            q("powerutils:module_rf", "Модуль RF", "Конвертация в RF-сети.", "powerutils:module_rf", 1, 85),
            q("quantumgenerators:phsp_gen", "Photonic Q-Generator", "Аддон Quantum Generators: фотонный ген.", "quantumgenerators:phsp_gen", 1, 120),
            q("quantumgenerators:nsp_gen", "Neutron Q-Generator", "Квантовый ген выше фотонного.", "quantumgenerators:nsp_gen", 1, 130),
            q("quantumgenerators:kvsp_gen", "Quark Q-Generator", "Топ линейки квант-генераторов.", "quantumgenerators:kvsp_gen", 1, 150),
            q("industrialupgrade:circuit/nanocircuit", "Nano Circuit", "Наносхема — электроника выше Advanced.", "industrialupgrade:circuit/nanocircuit", 1, 120),
            q("industrialupgrade:circuit/quantumcircuit", "Quantum Circuit", "Квантовая схема эндгейма IU.", "industrialupgrade:circuit/quantumcircuit", 1, 140),
            q("industrialupgrade:wiring_storage/mfsu_iu", "Капстоун IU", "Завод с MFSU + солнечной/квант-генерацией готов.", "industrialupgrade:machines/advanced_solar_paneliu", 1, 200),
        ],
    },
]


def chapter_code(ch: dict) -> str:
    for prefix in ("2A", "2B", "2C", "2D", "2E", "2F"):
        if ch["filename"].startswith(prefix):
            return prefix
    return f"{ch['order']:02d}"


# Visual stages for titles / milestone nodes (by progress through chapter).
STAGE_LABELS = (
    (0.00, "I", "Старт"),
    (0.20, "II", "Основа"),
    (0.40, "III", "Цех"),
    (0.60, "IV", "Мастерство"),
    (0.80, "V", "Финал"),
)

# Industrial Upgrade — named eras for beginners
STAGE_LABELS_IU = (
    (0.00, "I", "Примитив"),
    (0.18, "II", "Пар"),
    (0.38, "III", "Электрика"),
    (0.58, "IV", "Завод"),
    (0.78, "V", "Энергия"),
)


def stage_for(idx: int, total: int, filename: str = "") -> tuple[str, str]:
    labels = STAGE_LABELS_IU if filename == "2F_ws_industrial_upgrade" else STAGE_LABELS
    t = (idx - 1) / max(total - 1, 1)
    label, name = labels[0][1], labels[0][2]
    for thr, lab, nm in labels:
        if t >= thr:
            label, name = lab, nm
    return label, name


def layout_xy(idx: int, total: int) -> tuple[float, float]:
    """Serpentine path so dependency lines read as a river, not a brick wall."""
    cols = 6
    i = idx - 1
    row = i // cols
    col = i % cols
    if row % 2 == 1:
        col = cols - 1 - col
    # Slight vertical offset on even rows = wave feel
    wave = 0.18 if (row % 2 == 1) else 0.0
    x = round((col - (cols - 1) / 2) * 1.85, 2)
    y = round(row * 1.75 + wave, 2)
    # Pull the very last quest a bit down/center as a capstone landing
    if idx == total and total > 1:
        y = round(y + 0.35, 2)
    return x, y


def visual_for(idx: int, total: int, filename: str = "") -> tuple[str, float]:
    """shape, size — milestones stand out on the map."""
    if idx == 1:
        return "hexagon", 1.35
    if idx == total:
        return "gear", 1.55
    labels = STAGE_LABELS_IU if filename == "2F_ws_industrial_upgrade" else STAGE_LABELS
    stage_edges = {1}
    for thr, _, _ in labels[1:]:
        stage_edges.add(1 + int(round(thr * (total - 1))))
    if idx in stage_edges or idx % 10 == 0:
        return "diamond", 1.25
    if idx % 5 == 0:
        return "circle", 1.1
    return "rsquare", 1.0


def quest_block(
    ch_code: str,
    filename: str,
    idx: int,
    total: int,
    data: dict,
    deps: list[str],
    optional_root: bool,
) -> str:
    qid = f"WS{ch_code}{idx:04d}Q"
    tid = f"WS{ch_code}{idx:04d}T"
    rid = f"WS{ch_code}{idx:04d}R"
    xid = f"WS{ch_code}{idx:04d}X"
    x, y = layout_xy(idx, total)
    shape, size = visual_for(idx, total, filename)
    stage_lab, stage_name = stage_for(idx, total, filename)
    item = data["item"]

    sub = data["sub"]
    if idx == 1:
        sub = f"✦ ГАЙД МОДА · {sub}"
    elif idx == total:
        sub = f"★ КАПСТОУН · {sub}"
    elif shape == "diamond":
        sub = f"◆ Вехи · {stage_name} · {sub}"

    title = f"{stage_lab}·{idx}  {data['title']}"
    desc_lines = build_description(
        filename,
        idx,
        total,
        item,
        data["sub"],
        stage_lab=stage_lab,
        stage_name=stage_name,
        xp=data["xp"],
        reward_item=data["reward_item"],
        reward_count=data["reward_count"],
        has_deps=bool(deps),
    )
    desc_snbt = ",\n".join(f'\t\t\t\t"{esc(line)}"' for line in desc_lines)

    if deps:
        dep_inner = ", ".join(f'"{d}"' for d in deps)
        dep_block = (
            f"\n\t\t\tdependencies: [{dep_inner}]"
            f"\n\t\t\tdependency_requirement: \"all\""
            f"\n\t\t\thide_dependency_lines: false"
        )
    else:
        dep_block = ""
    opt_line = "\n\t\t\toptional: true" if optional_root else ""

    return f"""\t\t{{
\t\t\tx: {x}d
\t\t\ty: {y}d
\t\t\tid: "{qid}"
\t\t\ttitle: "{esc(title)}"
\t\t\ticon: "{item}"
\t\t\tsubtitle: "{esc(sub)}"
\t\t\tdescription: [
{desc_snbt}
\t\t\t]
\t\t\tshape: "{shape}"
\t\t\tsize: {size}d
\t\t\tmin_width: 280{opt_line}{dep_block}
\t\t\ttasks: [{{
\t\t\t\tid: "{tid}"
\t\t\t\ttype: "item"
\t\t\t\titem: "{item}"
\t\t\t\tcount: 1L
\t\t\t}}]
\t\t\trewards: [
\t\t\t\t{{
\t\t\t\t\tid: "{rid}"
\t\t\t\t\ttype: "item"
\t\t\t\t\titem: "{data["reward_item"]}"
\t\t\t\t\tcount: {data["reward_count"]}
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
    code = chapter_code(ch)
    total = len(ch["quests"])
    parts = []
    prev = None
    for i, qd in enumerate(ch["quests"], start=1):
        qid = f"WS{code}{i:04d}Q"
        deps = [prev] if prev else []
        parts.append(
            quest_block(
                code,
                ch["filename"],
                i,
                total,
                qd,
                deps,
                optional_root=(i == 1),
            )
        )
        prev = qid
    body = ",\n".join(parts)
    cid = f"WS{code}0000CH"
    ch_sub = chapter_subtitle(ch["filename"], ch.get("lore", ""))
    return f"""{{
\tdefault_hide_dependency_lines: false
\tdefault_quest_shape: "rsquare"
\tfilename: "{ch["filename"]}"
\tgroup: "{WS_GROUP}"
\ticon: "{ch["icon"]}"
\tid: "{cid}"
\torder_index: {ch["order"]}
\tquest_links: [ ]
\tquests: [
{body}
\t]
\tsubtitle: "{esc(ch_sub)}"
\ttitle: "{esc(ch["title"])}"
}}
"""


def ensure_workshop_group(path: Path) -> None:
    if path.exists() and WS_GROUP in path.read_text(encoding="utf-8"):
        return
    # Spine generator owns the full groups file; only bootstrap if missing.
    if not path.exists() or "chapter_groups: [ ]" in path.read_text(encoding="utf-8"):
        path.write_text(
            "{\n\tchapter_groups: [\n"
            f'\t\t{{ id: "0AC7A00000000001", title: "Сюжет · Океан" }}\n'
            f'\t\t{{ id: "{WS_GROUP}", title: "{WS_GROUP_TITLE}" }}\n'
            "\t]\n}\n",
            encoding="utf-8",
        )


def main() -> None:
    merge_extras(CHAPTERS)
    for out in OUT_DIRS:
        out.mkdir(parents=True, exist_ok=True)
        chapters_dir = out / "chapters"
        chapters_dir.mkdir(parents=True, exist_ok=True)
        groups = out / "chapter_groups.snbt"
        ensure_workshop_group(groups)
        for ch in CHAPTERS:
            (chapters_dir / f"{ch['filename']}.snbt").write_text(chapter_snbt(ch), encoding="utf-8")
            print("wrote", ch["filename"], len(ch["quests"]), "quests")
    ids = []
    for ch in CHAPTERS:
        code = chapter_code(ch)
        for i in range(1, len(ch["quests"]) + 1):
            ids.append(f"WS{code}{i:04d}Q")
    assert len(ids) == len(set(ids)), "duplicate quest ids"
    print(f"OK {len(CHAPTERS)} chapters, {len(ids)} quests, group {WS_GROUP}")


if __name__ == "__main__":
    main()
