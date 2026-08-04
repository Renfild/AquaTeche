# -*- coding: utf-8 -*-
"""
AquaTech: Ocean Horizon — generate 12x50 = 600 plot-driven FTB Quests.
Writes to config/ and server/config/, removes legacy chapters.
"""
from __future__ import annotations

import os
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT_DIRS = [
    ROOT / "config" / "ftbquests" / "quests",
    ROOT / "server" / "config" / "ftbquests" / "quests",
]

LEGACY = [
    "chapter_1_ocean.snbt",
    "chapter_2_animals.snbt",
    "chapter_3_create.snbt",
    "chapter_4_industrial.snbt",
    "chapter_5_airships.snbt",
    "chapter_01_catastrophe.snbt",
    "chapter_02_roost.snbt",
    "chapter_03_bees.snbt",
    "chapter_04_kinetics.snbt",
    "chapter_05_steam.snbt",
    "chapter_06_enderio.snbt",
    "chapter_07_industrial.snbt",
    "chapter_08_thermal.snbt",
    "chapter_09_ae2.snbt",
    "chapter_10_dreadnought.snbt",
]

GROUPS = [
    ("0AC7A00000000001", "Акт I · Катастрофа"),
    ("0AC7A00000000002", "Акт II · Жизнь на атоме"),
    ("0AC7A00000000003", "Акт III · Индустрия волн"),
    ("0AC7A00000000004", "Акт IV · Горизонт"),
]
ACT_I, ACT_II, ACT_III, ACT_IV = (g[0] for g in GROUPS)

# (filename, group, title, icon, chapter_lore, quests[(item, title, subtitle, reward_count)])
# Exactly 50 quests each.


def q(item: str, title: str, sub: str, reward: int = 2):
    return (item, title, sub, reward)


CHAPTERS = []

# ========== CH 01 Kickstarter / Catastrophe ==========
CHAPTERS.append((
    "01_kickstarter", ACT_I,
    "I. Катастрофа · Лодка Кикстартера",
    "minecraft:oak_chest_boat",
    "Мир ушёл под воду. У тебя атом из нескольких блоков и Лодка Кикстартера.",
    [
        q("minecraft:oak_chest_boat", "Лодка Кикстартера", "Сядь в лодку — это твой первый корабль после потопа.", 1),
        q("minecraft:fishing_rod", "Удочка выжившего", "Единственный способ добыть ресурсы с пустоты океана.", 1),
        q("minecraft:stick", "Плавающие палки", "Вылови обломки — палки держат первые крафты.", 8),
        q("minecraft:oak_planks", "Доски из волн", "Дерево тонет редко — береги каждую доску.", 8),
        q("minecraft:cobblestone", "Камень со дна", "Булыжник — кости затонувших городов.", 8),
        q("minecraft:dirt", "Горсть земли", "Земля дороже золота, когда вокруг только вода.", 4),
        q("minecraft:oak_sapling", "Саженец надежды", "Посади жизнь на атоме — без дерева нет будущего.", 2),
        q("minecraft:bone_meal", "Костная мука", "Ускорь рост — океан не ждёт.", 4),
        q("minecraft:crafting_table", "Верстак на волнах", "Собери верстак — первый очаг цивилизации.", 1),
        q("minecraft:chest", "Сундук запасов", "Храни улов — шторм ничего не прощает.", 1),
        q("minecraft:oak_log", "Бревно", "Настоящее бревно — редкий трофей рыбалки.", 4),
        q("minecraft:wooden_pickaxe", "Деревянная кирка", "Даже на атоме нужна кирка.", 1),
        q("minecraft:wooden_axe", "Деревянный топор", "Руби осторожно — дерево растёт медленно.", 1),
        q("minecraft:furnace", "Печь", "Огонь над бездной — тепло и плавка.", 1),
        q("minecraft:charcoal", "Древесный уголь", "Уголь из бревен — топливо без шахт.", 8),
        q("minecraft:torch", "Факел", "Свет на атоме — защита от ночи.", 8),
        q("minecraft:kelp", "Ламинария", "Морская зелень кормит и вяжет.", 8),
        q("minecraft:dried_kelp", "Сушёная ламинария", "Сухой паёк на долгий день.", 8),
        q("minecraft:string", "Нить", "Нити из сетей утопленников.", 4),
        q("minecraft:bread", "Хлеб", "Не голодай — сила нужна для стройки.", 8),
        q("minecraft:sand", "Песок", "Песок с мели — стекло и стройка.", 8),
        q("minecraft:glass", "Стекло", "Окно в горизонт.", 4),
        q("minecraft:clay_ball", "Глина", "Глина с илистого дна.", 8),
        q("minecraft:brick", "Кирпич", "Обожжённый кирпич крепит платформу.", 8),
        q("minecraft:ladder", "Лестница", "Спуск к воде безопаснее прыжка.", 4),
        q("minecraft:scaffolding", "Подмости", "Расширяй атом вверх и вширь.", 8),
        q("minecraft:barrel", "Бочка", "Морской склад компактнее сундука.", 1),
        q("minecraft:campfire", "Костёр", "Дым над водой — знак жизни.", 1),
        q("minecraft:iron_nugget", "Самородок железа", "Мелкое железо из обломков.", 9),
        q("minecraft:iron_ingot", "Слиток железа", "Первый металл эпохи Кикстартера.", 2),
        q("minecraft:bucket", "Ведро", "Вода — и враг, и ресурс.", 1),
        q("minecraft:water_bucket", "Ведро воды", "Контролируй стихию.", 1),
        q("minecraft:stone_pickaxe", "Каменная кирка", "Камень прочнее дерева.", 1),
        q("minecraft:stone_axe", "Каменный топор", "Быстрее валить ростки.", 1),
        q("minecraft:shears", "Ножницы", "Срезай листву и шерсть.", 1),
        q("minecraft:white_bed", "Кровать", "Точка возрождения на атоме.", 1),
        q("minecraft:compass", "Компас", "Ориентир в бесконечной воде.", 1),
        q("minecraft:clock", "Часы", "Время приливов.", 1),
        q("minecraft:spyglass", "Подзорная труба", "Ищи чужие атомы на горизонте.", 1),
        q("minecraft:copper_ingot", "Медь", "Медь сверкает в солёной воде.", 4),
        q("minecraft:lantern", "Фонарь", "Тёплый свет вместо факела.", 2),
        q("minecraft:smooth_stone", "Гладкий камень", "Основа механизмов будущего.", 8),
        q("minecraft:cauldron", "Котёл", "Сбор дождя и варка.", 1),
        q("minecraft:anvil", "Наковальня", "Чини инструменты Кикстартера.", 1),
        q("minecraft:grindstone", "Точило", "Сними лишние чары.", 1),
        q("minecraft:stonecutter", "Камнерез", "Точная обработка камня.", 1),
        q("minecraft:iron_bars", "Решётка", "Ограждение над бездной.", 8),
        q("minecraft:chain", "Цепь", "Связь платформ.", 4),
        q("minecraft:oak_boat", "Лёгкая лодка", "Запасной корпус на всякий случай.", 1),
        q("minecraft:heart_of_the_sea", "Сердце моря", "Легенда: оно зовёт к глубинным руинам.", 1),
    ],
))

# ========== CH 02 Catch / Aquaculture ==========
CHAPTERS.append((
    "02_catch", ACT_I,
    "II. Улов · Море кормит",
    "aquaculture:iron_fishing_rod",
    "Океан отдаёт рыбу, крючки и нептуний. Стань рыбаком Горизонта.",
    [
        q("aquaculture:iron_fishing_rod", "Железная удочка", "Aquaculture: прочнее ванильной.", 1),
        q("aquaculture:iron_hook", "Железный крючок", "Крючок держит крупный улов.", 1),
        q("aquaculture:worm", "Червь", "Наживка с атома.", 8),
        q("minecraft:cod", "Треска", "Простая рыба — начало рациона.", 8),
        q("minecraft:salmon", "Лосось", "Жирнее трески.", 8),
        q("minecraft:tropical_fish", "Тропическая рыба", "Краски тёплых течений.", 4),
        q("minecraft:pufferfish", "Иглобрюх", "Опасна, но ценна для зелий.", 2),
        q("minecraft:cooked_cod", "Жареная треска", "Сытость после шторма.", 8),
        q("minecraft:cooked_salmon", "Жареный лосось", "Сила на стройку.", 8),
        q("aquaculture:fish_fillet_raw", "Сырое филе", "Разделка улова.", 8),
        q("aquaculture:fish_fillet_cooked", "Жареное филе", "Чистый белок океана.", 8),
        q("oceansdelight:tentacles", "Щупальца", "Ocean's Delight с глубин.", 4),
        q("oceansdelight:cut_tentacles", "Нарезанные щупальца", "Готовь морскую кухню.", 4),
        q("crabbersdelight:crab", "Краб", "Crabber's Delight — клешни удачи.", 4),
        q("crabbersdelight:cooked_crab", "Варёный краб", "Пир на атоме.", 4),
        q("fishofthieves:earthworms", "Земляные черви", "Fish of Thieves наживка.", 8),
        q("minecraft:fishing_rod", "Запасная удочка", "Всегда держи вторую.", 1),
        q("minecraft:lily_pad", "Кувшинка", "Плавающая площадка.", 4),
        q("minecraft:sea_pickle", "Морской огурец", "Свет под водой.", 4),
        q("minecraft:seagrass", "Морская трава", "Корм и декор дна.", 8),
        q("minecraft:ink_sac", "Чернильный мешок", "Чернила кальмаров.", 8),
        q("minecraft:glow_ink_sac", "Светящиеся чернила", "Светящиеся таблички.", 4),
        q("minecraft:prismarine_shard", "Осколок призмарина", "Обломки храмов.", 8),
        q("minecraft:prismarine_crystals", "Кристаллы призмарина", "Свет храмов.", 4),
        q("minecraft:nautilus_shell", "Раковина наутилуса", "Ключ к проводнику.", 1),
        q("minecraft:scute", "Щиток", "Панцирь черепахи.", 1),
        q("minecraft:turtle_helmet", "Черепаший шлем", "Дыхание у поверхности.", 1),
        q("minecraft:trident", "Трезубец", "Оружие утопленников.", 1),
        q("aquaculture:gold_fishing_rod", "Золотая удочка", "Удача клева.", 1),
        q("aquaculture:diamond_fishing_rod", "Алмазная удочка", "Вершина рыбалки.", 1),
        q("aquaculture:neptunium_nugget", "Нептуниевый самородок", "Металл глубин.", 4),
        q("aquaculture:neptunium_ingot", "Нептуниевый слиток", "Ковка океана.", 1),
        q("aquaculture:neptunium_fishing_rod", "Нептуниевая удочка", "Легендарный инструмент улова.", 1),
        q("minecraft:bowl", "Миска", "Основа супов.", 4),
        q("crabbersdelight:crab_cakes", "Крабовые котлеты", "Пиршество Crabber's Delight.", 2),
        q("minecraft:suspicious_stew", "Подозрительное рагу", "Эффект в миске.", 2),
        q("minecraft:honey_bottle", "Мёд", "Сладость среди соли.", 2),
        q("minecraft:sugar", "Сахар", "Из тростника и свёклы.", 8),
        q("minecraft:paper", "Бумага", "Карты и книги квестов.", 8),
        q("minecraft:map", "Карта", "Пустая карта горизонтов.", 1),
        q("minecraft:writable_book", "Книга и перо", "Веди журнал Катастрофы.", 1),
        q("minecraft:book", "Книга", "Знания выживших.", 4),
        q("minecraft:glass_bottle", "Бутылочка", "Для зелий и медуз.", 8),
        q("minecraft:water_bucket", "Ведро воды", "Аквариум на атоме.", 1),
        q("minecraft:axolotl_bucket", "Ведро с аксолотлем", "Маленький друг глубин.", 1),
        q("minecraft:cod_bucket", "Ведро с треской", "Живой запас.", 1),
        q("minecraft:salmon_bucket", "Ведро с лососем", "Живой запас.", 1),
        q("minecraft:pufferfish_bucket", "Ведро с иглобрюхом", "Осторожно — раздувается.", 1),
        q("minecraft:tropical_fish_bucket", "Ведро с тропической", "Краски океана дома.", 1),
        q("minecraft:conduit", "Проводник", "Сердце подводной базы.", 1),
    ],
))

# ========== CH 03 Atoll build ==========
CHAPTERS.append((
    "03_atoll", ACT_II,
    "III. Атолл · Расширение атома",
    "minecraft:grass_block",
    "Из точки в атолл: земля, ферма, дом над бездной.",
    [
        q("minecraft:grass_block", "Дёрн", "Зелень на призме.", 4),
        q("minecraft:coarse_dirt", "Каменистая земля", "Стойкая почва.", 8),
        q("minecraft:rooted_dirt", "Корневая земля", "Корни держат атолл.", 4),
        q("minecraft:mud", "Грязь", "Ил после прилива.", 8),
        q("minecraft:wheat_seeds", "Семена пшеницы", "Первая грядка.", 8),
        q("minecraft:wheat", "Пшеница", "Урожай над водой.", 16),
        q("minecraft:hay_block", "Сноп сена", "Корм и компактное хранение.", 4),
        q("minecraft:carrot", "Морковь", "Оранжевый урожай.", 8),
        q("minecraft:potato", "Картофель", "Сытный клубень.", 8),
        q("minecraft:baked_potato", "Печёный картофель", "Простой ужин.", 8),
        q("minecraft:beetroot_seeds", "Семена свёклы", "Ещё одна культура.", 8),
        q("minecraft:beetroot", "Свёкла", "Для супа.", 8),
        q("minecraft:pumpkin_seeds", "Семена тыквы", "Большие плоды.", 4),
        q("minecraft:pumpkin", "Тыква", "Свет и еда.", 2),
        q("minecraft:melon_seeds", "Семена арбуза", "Сладкая вода в мякоти.", 4),
        q("minecraft:melon_slice", "Долька арбуза", "Освежение.", 8),
        q("minecraft:sugar_cane", "Тростник", "Сахар и бумага с края атома.", 8),
        q("minecraft:bamboo", "Бамбук", "Быстрый каркас.", 8),
        q("minecraft:cactus", "Кактус", "Зелёная защита.", 2),
        q("minecraft:oak_sapling", "Дуб", "Тень и брёвна.", 4),
        q("minecraft:spruce_sapling", "Ель", "Хвоя над волнами.", 4),
        q("minecraft:birch_sapling", "Берёза", "Белые стволы.", 4),
        q("minecraft:jungle_sapling", "Тропики", "Густая крона.", 2),
        q("minecraft:dark_oak_sapling", "Тёмный дуб", "Массивная древесина.", 2),
        q("minecraft:composter", "Компостер", "Отходы в костную муку.", 1),
        q("minecraft:flower_pot", "Горшок", "Декор атома.", 2),
        q("minecraft:oak_stairs", "Ступени", "Архитектура края.", 8),
        q("minecraft:oak_slab", "Плиты", "Экономия дерева.", 16),
        q("minecraft:oak_fence", "Забор", "Не упади в пустоту.", 8),
        q("minecraft:oak_door", "Дверь", "Дом начинается с двери.", 1),
        q("minecraft:oak_trapdoor", "Люк", "Спуск к лодке.", 2),
        q("minecraft:glass_pane", "Оконное стекло", "Вид на горизонт.", 8),
        q("minecraft:white_wool", "Шерсть", "Кровать и декор.", 8),
        q("minecraft:painting", "Картина", "Искусство выживших.", 1),
        q("minecraft:item_frame", "Рамка", "Витрина трофеев.", 2),
        q("minecraft:armor_stand", "Стойка для брони", "Храни доспехи.", 1),
        q("minecraft:loom", "Ткацкий станок", "Знамена атолла.", 1),
        q("minecraft:cartography_table", "Картографский стол", "Карты маршрутов.", 1),
        q("minecraft:smithing_table", "Кузнечный стол", "Улучшения позже.", 1),
        q("minecraft:fletching_table", "Стол лучника", "Стрелы на защиту.", 1),
        q("minecraft:blast_furnace", "Плавильня", "Быстрая плавка руд.", 1),
        q("minecraft:smoker", "Коптильня", "Быстрая еда.", 1),
        q("minecraft:hopper", "Воронка", "Логистика улова.", 2),
        q("minecraft:dropper", "Выбрасыватель", "Простая автоматика.", 2),
        q("minecraft:dispenser", "Раздатчик", "Защита периметра.", 1),
        q("minecraft:observer", "Наблюдатель", "Датчик роста фермы.", 2),
        q("minecraft:redstone", "Редстоун", "Кровь механизмов.", 8),
        q("minecraft:piston", "Поршень", "Движение блоков.", 2),
        q("minecraft:slime_ball", "Слизь", "Липкие поршни.", 4),
        q("minecraft:iron_block", "Железный блок", "Сердце укрепления.", 1),
    ],
))

# ========== CH 04 Roost ==========
CHAPTERS.append((
    "04_roost", ACT_II,
    "IV. Курятня · Биосинтез",
    "chicken_roost:roost",
    "Руд нет под ногами — куры Roost Ultimate станут шахтой.",
    [
        q("minecraft:egg", "Яйцо", "Начало птичьей линии.", 8),
        q("minecraft:feather", "Перо", "Лёгкость и стрелы.", 8),
        q("minecraft:chicken", "Сырая курица", "Мясо первого выводка.", 4),
        q("minecraft:cooked_chicken", "Жареная курица", "Силы для фермы.", 4),
        q("chicken_roost:roost", "Курятник", "Сердце биосинтеза ресурсов.", 1),
        q("chicken_roost:breeder", "Разведчик", "Разводи линии кур.", 1),
        q("chicken_roost:collector", "Сборщик", "Собирай продукцию автоматически.", 1),
        q("chicken_roost:trainer", "Тренер", "Прокачай кур.", 1),
        q("chicken_roost:chicken_scanner", "Сканер кур", "Читай гены.", 1),
        q("chicken_roost:chicken_stick", "Палка для кур", "Инструмент заводчика.", 1),
        q("chicken_roost:book", "Книга Roost", "Знания биосинтеза.", 1),
        q("chicken_roost:chicken_food_tier_1", "Корм I", "Базовое питание.", 8),
        q("chicken_roost:chicken_food_tier_2", "Корм II", "Лучший рацион.", 8),
        q("chicken_roost:chicken_food_tier_3", "Корм III", "Сильный рост.", 4),
        q("chicken_roost:chicken_essence_tier_1", "Эссенция кур I", "Дистиллят генов.", 4),
        q("chicken_roost:chicken_essence_tier_2", "Эссенция кур II", "Сильнее концентрат.", 4),
        q("chicken_roost:c_cobble", "Курица булыжника", "Камень без шахты.", 1),
        q("chicken_roost:c_soulsoil", "Курица почв душ", "Почва для атолла.", 1),
        q("chicken_roost:c_sand", "Курица песка", "Стекло и бетон.", 1),
        q("chicken_roost:c_gravel", "Курица гравия", "Кремень и тропы.", 1),
        q("chicken_roost:c_clay", "Курица глины", "Кирпичи и фарфор.", 1),
        q("chicken_roost:c_coal", "Курица угля", "Топливо биосинтеза.", 1),
        q("chicken_roost:c_iron", "Курица железа", "Металл из яйца.", 1),
        q("chicken_roost:c_copper", "Курица меди", "Проводники будущего.", 1),
        q("chicken_roost:c_gold", "Курица золота", "Блеск без жил.", 1),
        q("chicken_roost:c_lapis", "Курица лазурита", "Чары и синий свет.", 1),
        q("chicken_roost:c_redstone", "Курица редстоуна", "Сигналы из перьев.", 1),
        q("chicken_roost:c_diamond", "Курица алмазов", "Редчайший ген.", 1),
        q("chicken_roost:c_emerald", "Курица изумрудов", "Торговля и сила.", 1),
        q("chicken_roost:c_quartz", "Курица кварца", "Нижний мир в яйце.", 1),
        q("chicken_roost:c_obsidian", "Курица обсидиана", "Чёрный камень.", 1),
        q("chicken_roost:c_glowstone", "Курица светопыли", "Свет Нижнего.", 1),
        q("chicken_roost:c_netherrack", "Курица незерака", "Адский камень.", 1),
        q("chicken_roost:c_endstone", "Курица эндерняка", "Камень края.", 1),
        q("chicken_roost:c_andesite", "Курица андезита", "Create ждёт.", 1),
        q("chicken_roost:c_zinc", "Курица цинка", "Латунь завтра.", 1),
        q("chicken_roost:c_tin", "Курица олова", "Thermal сплавы.", 1),
        q("chicken_roost:c_silver", "Курица серебра", "Благородный металл.", 1),
        q("chicken_roost:c_lead", "Курица свинца", "Тяжёлый металл.", 1),
        q("chicken_roost:c_nickel", "Курица никеля", "Инвар впереди.", 1),
        q("chicken_roost:c_osmium", "Курица осмия", "Mekanism зовёт.", 1),
        q("chicken_roost:c_uranium", "Курица урана", "Осторожно — энергия.", 1),
        q("chicken_roost:c_certusquartz", "Курица истинного кварца", "AE2 начинается здесь.", 1),
        q("chicken_roost:c_fluixcrystal", "Курица флюикса", "Цифровая магия.", 1),
        q("chicken_roost:c_brass", "Курица латуни", "Create сплав.", 1),
        q("chicken_roost:c_steel", "Курица стали", "Прочность индустрии.", 1),
        q("chicken_roost:c_invar", "Курица инвара", "Thermal сердце.", 1),
        q("chicken_roost:c_electrum", "Курица электрума", "Проводящий сплав.", 1),
        q("chicken_roost:c_enderium", "Курица эндериума", "Поздний сплав.", 1),
        q("chicken_roost:soul_breeder", "Душевный разводчик", "Высшая генетика Roost.", 1),
    ],
))

# ========== CH 05 Bees + MA ==========
CHAPTERS.append((
    "05_swarm", ACT_II,
    "V. Рой · Соты и эссенции",
    "productivebees:advanced_oak_beehive",
    "Пчёлы и Mystical Agriculture закрывают дыры в ресурсах.",
    [
        q("minecraft:bee_nest", "Пчелиное гнездо", "Дикий старт роя.", 1),
        q("minecraft:beehive", "Улей", "Домашние пчёлы.", 1),
        q("minecraft:honeycomb", "Соты", "Стройматериалы роя.", 8),
        q("minecraft:honey_bottle", "Бутылка мёда", "Сладость и крафт.", 4),
        q("minecraft:honey_block", "Блок мёда", "Замедление и декор.", 2),
        q("minecraft:honeycomb_block", "Блок сот", "Плотное хранение.", 2),
        q("productivebees:advanced_oak_beehive", "Продвинутый улей", "Productive Bees ядро.", 1),
        q("productivebees:bee_cage", "Клетка для пчёл", "Перенос генов.", 2),
        q("productivebees:honey_treat", "Медовое лакомство", "Корми рой.", 8),
        q("productivebees:sugarbag_nest", "Сахарное гнездо", "Особый улей.", 1),
        q("mysticalagriculture:inferium_essence", "Inferium", "Базовая эссенция MA.", 16),
        q("mysticalagriculture:prosperity_shard", "Осколок процветания", "Основа семян.", 8),
        q("mysticalagriculture:inferium_seeds", "Семена Inferium", "Ферма эссенций.", 4),
        q("mysticalagriculture:prudentium_essence", "Prudentium", "Второй ярус.", 8),
        q("mysticalagriculture:tertium_essence", "Tertium", "Третий ярус.", 8),
        q("mysticalagriculture:imperium_essence", "Imperium", "Четвёртый ярус.", 4),
        q("mysticalagriculture:supremium_essence", "Supremium", "Пятый ярус.", 2),
        q("mysticalagriculture:infusion_pedestal", "Пьедестал", "Ритуал наполнения.", 4),
        q("mysticalagriculture:infusion_altar", "Алтарь наполнения", "Сердце MA.", 1),
        q("mysticalagriculture:inferium_farmland", "Пашня Inferium", "Ускоренный рост.", 4),
        q("mysticalagriculture:watering_can", "Лейка", "Полив эссенций.", 1),
        q("mysticalagriculture:mystical_fertilizer", "Мистическое удобрение", "Взрывной рост.", 8),
        q("mysticalagriculture:wood_essence", "Эссенция дерева", "Брёвна из магии.", 8),
        q("mysticalagriculture:stone_essence", "Эссенция камня", "Камень без дна.", 8),
        q("mysticalagriculture:dirt_essence", "Эссенция земли", "Почва из воздуха.", 8),
        q("mysticalagriculture:water_essence", "Эссенция воды", "Контроль стихии.", 8),
        q("mysticalagriculture:fire_essence", "Эссенция огня", "Жар без Нижнего.", 8),
        q("mysticalagriculture:ice_essence", "Эссенция льда", "Холод океана.", 8),
        q("mysticalagriculture:nature_essence", "Эссенция природы", "Зелень атолла.", 8),
        q("mysticalagriculture:coal_essence", "Эссенция угля", "Топливо фермы.", 8),
        q("mysticalagriculture:iron_essence", "Эссенция железа", "Металл из семян.", 8),
        q("mysticalagriculture:copper_essence", "Эссенция меди", "Провода из грядок.", 8),
        q("mysticalagriculture:gold_essence", "Эссенция золота", "Золото без жил.", 8),
        q("mysticalagriculture:lapis_essence", "Эссенция лазурита", "Чары с грядки.", 8),
        q("mysticalagriculture:redstone_essence", "Эссенция редстоуна", "Сигналы из почвы.", 8),
        q("mysticalagriculture:diamond_essence", "Эссенция алмазов", "Редкость в ростках.", 4),
        q("mysticalagriculture:emerald_essence", "Эссенция изумрудов", "Торговля магией.", 4),
        q("mysticalagriculture:obsidian_essence", "Эссенция обсидиана", "Чёрный камень.", 4),
        q("mysticalagriculture:nether_essence", "Эссенция Незера", "Ад в семенах.", 4),
        q("mysticalagriculture:end_essence", "Эссенция Энда", "Край на атолле.", 4),
        q("mysticalagriculture:experience_essence", "Эссенция опыта", "Уровни без мобов.", 4),
        q("mysticalagriculture:soulium_dust", "Соулиум пыль", "Душевный материал.", 8),
        q("mysticalagriculture:soulium_ingot", "Соулиум слиток", "Ковка душ.", 2),
        q("mysticalagriculture:infusion_crystal", "Кристалл наполнения", "Многократные ритуалы.", 1),
        q("mysticalagriculture:master_infusion_crystal", "Мастер-кристалл", "Бесконечное наполнение.", 1),
        q("minecraft:shears", "Ножницы", "Срезай соты безопасно.", 1),
        q("minecraft:campfire", "Костёр под ульем", "Успокой пчёл.", 1),
        q("minecraft:flowering_azalea", "Цветущая азалия", "Нектар для роя.", 2),
        q("minecraft:lilac", "Сирень", "Аромат фермы.", 2),
        q("minecraft:rose_bush", "Розовый куст", "Красный нектар.", 2),
    ],
))

# ========== CH 06 Create kinetics ==========
CHAPTERS.append((
    "06_kinetics", ACT_III,
    "VI. Кинетика · Течения",
    "create:water_wheel",
    "Океан крутит колёса Create — первая настоящая индустрия.",
    [
        q("create:wrench", "Ключ", "Язык инженера Create.", 1),
        q("create:goggles", "Очки", "Видь стресс сети.", 1),
        q("create:andesite_alloy", "Андезитовый сплав", "Кровь кинетики.", 16),
        q("create:shaft", "Вал", "Передача вращения.", 8),
        q("create:cogwheel", "Шестерня", "Зубчатая связь.", 8),
        q("create:large_cogwheel", "Большая шестерня", "Крупный узел.", 4),
        q("create:andesite_casing", "Андезитовый корпус", "Основа машин.", 8),
        q("create:hand_crank", "Ручной привод", "Первый оборот вручную.", 1),
        q("create:water_wheel", "Водяное колесо", "Сила течений океана.", 2),
        q("create:large_water_wheel", "Большое колесо", "Мощь прилива.", 1),
        q("create:gearbox", "Редуктор", "Поворот оси.", 2),
        q("create:clutch", "Сцепление", "Отключай вал.", 2),
        q("create:gearshift", "Реверс", "Меняй направление.", 2),
        q("create:millstone", "Жернов", "Дроби ресурсы.", 1),
        q("create:mechanical_press", "Пресс", "Листы и компакты.", 1),
        q("create:basin", "Чаша", "Смешивание.", 1),
        q("create:mechanical_mixer", "Миксер", "Авто-смеси.", 1),
        q("create:depot", "Депо", "Точка предметов.", 2),
        q("create:belt_connector", "Лента", "Конвейер атолла.", 4),
        q("create:chute", "Желоб", "Вертикальный сброс.", 4),
        q("create:andesite_funnel", "Воронка", "Ввод на ленту.", 4),
        q("create:andesite_tunnel", "Туннель", "Разделение потоков.", 2),
        q("create:encased_fan", "Вентилятор", "Обдув и фермы.", 2),
        q("create:mechanical_saw", "Пила", "Авто-лес.", 1),
        q("create:mechanical_drill", "Бур", "Авто-камень.", 1),
        q("create:mechanical_harvester", "Жнец", "Авто-ферма.", 1),
        q("create:mechanical_plough", "Плуг", "Готовь почву.", 1),
        q("create:deployer", "Установщик", "Рука механизма.", 1),
        q("create:portable_storage_interface", "Интерфейс хранения", "Связь с контрапшеном.", 2),
        q("create:copper_casing", "Медный корпус", "Жидкостные машины.", 4),
        q("create:fluid_pipe", "Труба", "Потоки жидкостей.", 8),
        q("create:mechanical_pump", "Помпа", "Качай воду океана.", 2),
        q("create:fluid_tank", "Резервуар", "Запас жидкости.", 2),
        q("create:spout", "Диспенсер жидкости", "Розлив.", 1),
        q("create:item_drain", "Слив предметов", "Осуши вёдра.", 1),
        q("create:fluid_valve", "Вентиль", "Перекрывай трубы.", 2),
        q("create:hose_pulley", "Шланговый подъёмник", "Забор из моря.", 1),
        q("create:copper_sheet", "Медный лист", "Штамповка.", 8),
        q("create:iron_sheet", "Железный лист", "Корпуса.", 8),
        q("create:golden_sheet", "Золотой лист", "Декор и схемы.", 4),
        q("create:zinc_ingot", "Цинк", "Путь к латуни.", 8),
        q("create:brass_ingot", "Латунь", "Точная механика.", 8),
        q("create:brass_sheet", "Латунный лист", "Продвинутые детали.", 8),
        q("create:brass_casing", "Латунный корпус", "Умные машины.", 4),
        q("create:rose_quartz", "Розовый кварц", "Электроника Create.", 8),
        q("create:polished_rose_quartz", "Полированный кварц", "Чистый сигнал.", 4),
        q("create:electron_tube", "Электронная лампа", "Логика машин.", 4),
        q("create:precision_mechanism", "Точный механизм", "Ключ к автокрафту.", 2),
        q("create:brass_funnel", "Латунная воронка", "Фильтры потока.", 4),
        q("create:brass_tunnel", "Латунный туннель", "Умное разделение.", 2),
        q("create:smart_chute", "Умный желоб", "Фильтр вертикали.", 2),
    ],
))

# ========== CH 07 Steam ==========
CHAPTERS.append((
    "07_steam", ACT_III,
    "VII. Пар · Давление и пути",
    "create:steam_engine",
    "Пар над океаном: котлы, логистика и рельсы Steam Rails.",
    [
        q("create:blaze_burner", "Горелка ифрита", "Жар для котла.", 1),
        q("create:empty_blaze_burner", "Пустая горелка", "Каркас жара.", 1),
        q("create:steam_engine", "Паровой двигатель", "Сила давления.", 1),
        q("create:steam_whistle", "Свисток", "Голос завода.", 1),
        q("create:fluid_tank", "Котловой резервуар", "Вода под давлением.", 4),
        q("create:copper_valve_handle", "Ручка вентиля", "Контроль пара.", 2),
        q("create:mechanical_bearing", "Подшипник", "Вращающиеся контрапшены.", 1),
        q("create:windmill_bearing", "Ветряной подшипник", "Сила ветра над морем.", 1),
        q("create:clockwork_bearing", "Часовой подшипник", "Тайминг конструкций.", 1),
        q("create:gantry_carriage", "Каретка портала", "Линейное движение.", 1),
        q("create:gantry_shaft", "Вал портала", "Путь каретки.", 4),
        q("create:cart_assembler", "Сборщик вагонеток", "Мобильные машины.", 1),
        q("create:minecart_coupling", "Сцепка", "Поезда вагонеток.", 2),
        q("create:track", "Рельсы Create", "Путь по атоллам.", 16),
        q("create:track_station", "Станция", "Остановка состава.", 1),
        q("create:track_signal", "Сигнал", "Безопасность путей.", 2),
        q("create:track_observer", "Наблюдатель путей", "Детектор состава.", 2),
        q("create:controls", "Управление поездом", "Штурвал состава.", 1),
        q("create:schedule", "Расписание", "Автомаршруты.", 1),
        q("railways:track_switch_andesite", "Стрелка", "Steam Rails развилка.", 2),
        q("create:item_vault", "Хранилище", "Массовый склад.", 2),
        q("create:stockpile_switch", "Переключатель запасов", "Авто-логика склада.", 2),
        q("create:content_observer", "Наблюдатель содержимого", "Сенсор предметов.", 2),
        q("create:stockpile_switch", "Переключатель запасов II", "Складская логика.", 2),
        q("create:pulse_repeater", "Импульсный повторитель", "Тайминг редстоуна.", 2),
        q("create:pulse_extender", "Импульсный удлинитель", "Длинный сигнал.", 2),
        q("create:redstone_link", "Редстоун-линк", "Беспроводной сигнал.", 4),
        q("create:display_board", "Табло", "Информация завода.", 2),
        q("create:display_link", "Связь табло", "Данные на экран.", 2),
        q("create:nixie_tube", "Никси-трубки", "Цифры давления.", 4),
        q("create:rope_pulley", "Канатный подъёмник", "Вертикаль атома.", 1),
        q("create:elevator_pulley", "Лифт", "Этажи атолла.", 1),
        q("create:mechanical_arm", "Механическая рука", "Точная укладка.", 1),
        q("create:rotation_speed_controller", "Регулятор скорости", "Контроль RPM.", 1),
        q("create:sequenced_gearshift", "Секвенсор", "Программируемый вал.", 1),
        q("create:speedometer", "Спидометр", "Замер скорости.", 1),
        q("create:stressometer", "Стрессометр", "Замер нагрузки.", 1),
        q("create:cuckoo_clock", "Часы с кукушкой", "Таймер завода.", 1),
        q("create:peculiar_bell", "Колокол", "Сигнал смены.", 1),
        q("create:super_glue", "Супер-клей", "Склей контрапшен.", 4),
        q("create:linear_chassis", "Линейное шасси", "Каркас машин.", 4),
        q("create:radial_chassis", "Радиальное шасси", "Круглые конструкции.", 4),
        q("create:sticker", "Липучка", "Удержание блоков.", 2),
        q("create:contraption_controls", "Пульт контрапшена", "Управление сборкой.", 1),
        q("create:schematic_table", "Стол схематики", "Чертежи построек.", 1),
        q("create:schematicannon", "Схематическая пушка", "Печать зданий.", 1),
        q("create:empty_schematic", "Пустая схематика", "Запиши атолл.", 1),
        q("create:crafting_blueprint", "Чертёж крафта", "Призрачный рецепт.", 1),
        q("create:filter", "Фильтр", "Сортировка предметов.", 2),
        q("create:attribute_filter", "Атрибут-фильтр", "Умная сортировка.", 2),
        q("create:package_filter", "Фильтр посылок", "Логистика коробок.", 2),
    ],
))

# ========== CH 08 Power EnderIO + Mekanism ==========
CHAPTERS.append((
    "08_power", ACT_III,
    "VIII. Энергия · Сплавы и ток",
    "enderio:energy_conduit",
    "Электрификация атолла: Ender IO и первые шаги Mekanism.",
    [
        q("enderio:conduit_binder", "Связующее", "Клей кондуитов.", 16),
        q("enderio:energy_conduit", "Энергокондуит", "Ток по атоллам.", 8),
        q("enderio:item_conduit", "Предметный кондуит", "Предметы по трубам.", 8),
        q("enderio:fluid_conduit", "Жидкостный кондуит", "Жидкости без Create.", 8),
        q("enderio:redstone_conduit", "Редстоун-кондуит", "Сигналы в кабеле.", 8),
        q("enderio:basic_capacitor", "Базовый конденсатор", "Сердце машин EIO.", 4),
        q("enderio:double_layer_capacitor", "Двойной конденсатор", "Больше ёмкости.", 2),
        q("enderio:octadic_capacitor", "Восьмеричный конденсатор", "Топовая ёмкость.", 1),
        q("enderio:primitive_alloy_smelter", "Примитивная плавильня", "Первые сплавы.", 1),
        q("enderio:alloy_smelter", "Плавильня сплавов", "Авто-сплавы.", 1),
        q("enderio:sag_mill", "SAG Mill", "Дробление руд.", 1),
        q("enderio:stirling_generator", "Генератор Стирлинга", "Жги топливо — получи RF.", 1),
        q("enderio:electrical_steel_ingot", "Электрическая сталь", "Базовый сплав EIO.", 8),
        q("enderio:energetic_alloy_ingot", "Энергетический сплав", "Светится силой.", 8),
        q("enderio:vibrant_alloy_ingot", "Вибрантовый сплав", "Яркая проводимость.", 4),
        q("enderio:redstone_alloy_ingot", "Редстоун-сплав", "Сигнальный металл.", 8),
        q("enderio:conductive_alloy_ingot", "Проводящий сплав", "Ток в металле.", 8),
        q("enderio:pulsating_alloy_ingot", "Пульсирующий сплав", "Ритм энергии.", 4),
        q("enderio:dark_steel_ingot", "Тёмная сталь", "Прочность тьмы.", 8),
        q("enderio:soularium_ingot", "Соулариум", "Металл душ.", 4),
        q("enderio:end_steel_ingot", "Эндер-сталь", "Сталь Края.", 4),
        q("enderio:vacuum_chest", "Вакуумный сундук", "Всасывай дроп.", 1),
        q("enderio:crafter", "Автокрафтер", "Крафт по шаблону.", 1),
        q("enderio:impulse_hopper", "Импульсная воронка", "Умный перенос.", 1),
        q("enderio:yeta_wrench", "Ключ Yeta", "Настройка EIO.", 1),
        q("mekanism:ingot_osmium", "Осмий", "Сердце Mekanism.", 8),
        q("mekanism:ingot_tin", "Олово Mekanism", "Сплавы механизма.", 8),
        q("mekanism:ingot_lead", "Свинец", "Защита и сплавы.", 8),
        q("mekanism:ingot_uranium", "Уран", "Ядерный путь.", 4),
        q("mekanism:steel_casing", "Стальной корпус", "Рама машин Mek.", 4),
        q("mekanism:basic_control_circuit", "Базовая схема", "Мозг машин.", 4),
        q("mekanism:alloy_infused", "Насыщенный сплав", "Улучшение tier I.", 8),
        q("mekanism:metallurgic_infuser", "Металлургический инфузер", "Легирование.", 1),
        q("mekanism:enrichment_chamber", "Камера обогащения", "Улучшение руд.", 1),
        q("mekanism:crusher", "Дробитель", "Пыль из блоков.", 1),
        q("mekanism:energized_smelter", "Энергоплавильня", "Электро-печь.", 1),
        q("mekanism:basic_energy_cube", "Энергокуб", "Хранилище FE.", 1),
        q("mekanism:basic_universal_cable", "Универсальный кабель", "Провод Mekanism.", 8),
        q("mekanismgenerators:heat_generator", "Теплогенератор", "Первый ток Mekanism.", 1),
        q("mekanismgenerators:solar_generator", "Солнечный генератор", "Свет над океаном.", 1),
        q("mekanismgenerators:advanced_solar_generator", "Продвинутая панель", "Больше солнца.", 1),
        q("mekanismgenerators:wind_generator", "Ветрогенератор", "Ветер горизонта.", 1),
        q("mekanismgenerators:bio_generator", "Биогенератор", "Жги органику.", 1),
        q("mekanismgenerators:gas_burning_generator", "Газовый генератор", "Гори топливом газа.", 1),
        q("enderio:solar_panel_basic", "Солнечная панель EIO", "Дневной ток.", 1),
        q("enderio:travel_anchor", "Якорь телепорта", "Сеть якорей.", 2),
        q("enderio:staff_of_travelling", "Посох путешествий", "Прыжки по якорям.", 1),
        q("waystones:waystone", "Вейстоун", "Камень быстрых путей.", 1),
        q("waystones:warp_stone", "Камень варпа", "Личный телепорт.", 1),
        q("mekanism:dust_osmium", "Осмиевая пыль", "Пыль для инфузии.", 8),
    ],
))

# ========== CH 09 Industrial Thermal IF ==========
CHAPTERS.append((
    "09_industry", ACT_III,
    "IX. Промысел · Заводы на воде",
    "industrialforegoing:machine_frame_pity",
    "Латекс, пластик, Thermal и бесконечный промысел ресурсов.",
    [
        q("industrialforegoing:machine_frame_pity", "Примитивный корпус", "Старт IF.", 1),
        q("industrialforegoing:fluid_extractor", "Экстрактор", "Качай латекс из брёвен.", 1),
        q("industrialforegoing:latex_processing_unit", "Процессор латекса", "В каучук.", 1),
        q("industrialforegoing:dryrubber", "Сухой каучук", "Полуфабрикат.", 8),
        q("industrialforegoing:plastic", "Пластик", "Кровь IF.", 8),
        q("industrialforegoing:machine_frame_simple", "Простой корпус", "Tier 2.", 1),
        q("industrialforegoing:machine_frame_advanced", "Продвинутый корпус", "Tier 3.", 1),
        q("industrialforegoing:machine_frame_supreme", "Высший корпус", "Tier 4.", 1),
        q("industrialforegoing:plant_gatherer", "Сборщик растений", "Авто-ферма.", 1),
        q("industrialforegoing:plant_sower", "Сеятель", "Авто-посадка.", 1),
        q("industrialforegoing:plant_fertilizer", "Удобритель", "Авто-рост.", 1),
        q("industrialforegoing:water_condensator", "Конденсатор воды", "Вода из воздуха.", 1),
        q("industrialforegoing:block_breaker", "Ломатель блоков", "Авто-добыча.", 1),
        q("industrialforegoing:block_placer", "Установщик блоков", "Авто-стройка.", 1),
        q("industrialforegoing:mob_slaughter_factory", "Бойня", "Ферма мобов.", 1),
        q("industrialforegoing:mob_crusher", "Дробитель мобов", "Опыт и дроп.", 1),
        q("industrialforegoing:laser_drill", "Лазерный бур", "Руда из луча.", 1),
        q("industrialforegoing:ore_laser_base", "Рудная лазерная база", "Цель лазера.", 1),
        q("industrialforegoing:bioreactor", "Биореактор", "Биотопливо.", 1),
        q("industrialforegoing:pitiful_generator", "Простой генератор", "Жги что угодно.", 1),
        q("industrialforegoing:biofuel_generator", "Биотопливный генератор", "Чистый ток.", 1),
        q("thermal:rf_coil", "RF-катушка", "Сердце Thermal.", 4),
        q("thermal:redstone_servo", "Сервопривод", "Механика Thermal.", 4),
        q("thermal:machine_crafter", "Автокрафтер Thermal", "Крафт на RF.", 1),
        q("thermal:machine_furnace", "Красная печь", "Электроплавка.", 1),
        q("thermal:machine_pulverizer", "Измельчитель", "Пыль руд.", 1),
        q("thermal:machine_smelter", "Индукционная плавильня", "Сплавы 2-в-1.", 1),
        q("thermal:machine_sawmill", "Лесопилка", "Доски оптом.", 1),
        q("thermal:machine_insolator", "Фитоинкубатор", "Супер-ферма.", 1),
        q("thermal:machine_centrifuge", "Центрифуга", "Разделение.", 1),
        q("thermal:machine_press", "Пресс Thermal", "Штамповка.", 1),
        q("thermal:machine_crucible", "Тигель", "Плавка жидкостей.", 1),
        q("thermal:machine_refinery", "Перегонка", "Дистилляция.", 1),
        q("thermal:dynamo_stirling", "Динамо Стирлинга", "Топливный ток.", 1),
        q("thermal:dynamo_magmatic", "Магма-динамо", "Жар лавы.", 1),
        q("thermal:dynamo_lapidary", "Ювелирное динамо", "Жги гемы.", 1),
        q("thermal:energy_cell", "Ячейка энергии", "Буфер RF.", 1),
        q("thermal:fluid_cell", "Ячейка жидкости", "Буфер жидкостей.", 1),
        q("thermal:tin_ingot", "Олово", "Базовый металл T.", 8),
        q("thermal:lead_ingot", "Свинец Thermal", "Сплавы.", 8),
        q("thermal:silver_ingot", "Серебро", "Блеск и схемы.", 8),
        q("thermal:nickel_ingot", "Никель", "Инвар.", 8),
        q("thermal:bronze_ingot", "Бронза", "Классический сплав.", 8),
        q("thermal:invar_ingot", "Инвар", "Жаропрочность.", 8),
        q("thermal:electrum_ingot", "Электрум", "Проводник.", 8),
        q("thermal:signalum_ingot", "Сигналиум", "Сигнальный сплав.", 4),
        q("thermal:lumium_ingot", "Люмиум", "Светящийся сплав.", 4),
        q("thermal:enderium_ingot", "Эндериум", "Топовый сплав Thermal.", 2),
        q("thermal:device_rock_gen", "Генератор породы", "Камень из воды и лавы.", 1),
        q("thermal:device_water_gen", "Водный накопитель", "Вода для котлов.", 1),
        q("thermal:device_collector", "Сборщик", "Подбирай дроп.", 1),
        q("thermal:upgrade_augment_1", "Аугмент I", "Ускорь машины.", 1),
        q("thermal:upgrade_augment_2", "Аугмент II", "Ещё быстрее.", 1),
        q("thermal:upgrade_augment_3", "Аугмент III", "Максимум Thermal.", 1),
    ],
))

# ========== CH 10 Aquamirae ==========
CHAPTERS.append((
    "10_depths", ACT_IV,
    "X. Глубины · Aquamirae",
    "aquamirae:terrible_blade",
    "Под атоллами спит Maelstrom. Спуск в сюжетные глубины.",
    [
        q("aquamirae:sharp_bones", "Острые кости", "Останки глубин.", 8),
        q("aquamirae:fin", "Плавник", "Трофей морских тварей.", 4),
        q("aquamirae:esca", "Эска", "Приманка ужаса.", 4),
        q("aquamirae:angler_fang", "Клык удильщика", "Оружие тьмы.", 2),
        q("aquamirae:pirate_pouch", "Пиратский мешочек", "Добыча корсаров.", 2),
        q("aquamirae:dead_sea_scroll", "Свиток мёртвого моря", "Знание катастрофы.", 1),
        q("aquamirae:logbook", "Судовой журнал", "Хроника потопа.", 1),
        q("aquamirae:frozen_key", "Мёрзлый ключ", "К сундукам льда.", 1),
        q("aquamirae:frozen_chest", "Мёрзлый сундук", "Награда глубин.", 1),
        q("aquamirae:sea_stew", "Морское рагу", "Еда исследователя.", 4),
        q("aquamirae:poseidon_breakfast", "Завтрак Посейдона", "Сила на спуск.", 2),
        q("aquamirae:sea_casserole", "Морская запеканка", "Пир перед боем.", 2),
        q("aquamirae:cooked_spinefish", "Жареная хребет-рыба", "Сытность Maelstrom.", 4),
        q("aquamirae:jellyfish_jelly", "Медузий желе", "Скользкий ресурс.", 4),
        q("aquamirae:esca_skewer", "Шашлык из эски", "Опасный деликатес.", 2),
        q("aquamirae:salvager_helmet", "Шлем спасателя", "Комплект глубин.", 1),
        q("aquamirae:salvager_suit", "Костюм спасателя", "Защита от давления.", 1),
        q("aquamirae:salvager_waders", "Штаны спасателя", "Шаги по дну.", 1),
        q("aquamirae:salvager_boots", "Ботинки спасателя", "Сцепление со льдом.", 1),
        q("aquamirae:terrible_blade", "Ужасный клинок", "Клинок бездны.", 1),
        q("aquamirae:remnant_saber", "Сабля останков", "Пиратская сталь.", 1),
        q("aquamirae:dagger_of_greed", "Кинжал жадности", "Цена алчности.", 1),
        q("aquamirae:shatterblade", "Осколочный клинок", "Оружие капитана.", 1),
        q("aquamirae:terrible_fang", "Ужасный клык", "Яд глубин.", 1),
        q("aquamirae:terrible_cleaver", "Ужасный тесак", "Инструмент охоты.", 1),
        q("aquamirae:abyssal_amethyst", "Абиссальный аметист", "Кристалл бездны.", 4),
        q("aquamirae:abyssal_heaume", "Абиссальный шлем", "Корона глубин.", 1),
        q("aquamirae:abyssal_brigandine", "Абиссальная бригантина", "Броня тьмы.", 1),
        q("aquamirae:abyssal_leggings", "Абиссальные поножи", "Шаги в Maelstrom.", 1),
        q("aquamirae:abyssal_boots", "Абиссальные ботинки", "Дно под ногами.", 1),
        q("aquamirae:abyssal_tiara", "Абиссальная тиара", "Знак избранных.", 1),
        q("aquamirae:rune_of_the_storm", "Руна шторма", "Сила бури.", 1),
        q("aquamirae:ship_graveyard_echo", "Эхо кладбища кораблей", "Голос горизонта.", 1),
        q("aquamirae:niveis_tear", "Слеза Нивеис", "Холодная реликвия.", 1),
        q("aquamirae:maze_rose", "Роза лабиринта", "Цветок ужаса.", 1),
        q("aquamirae:golden_moth_jar", "Банка золотой моли", "Свет в банке.", 1),
        q("aquamirae:cracked_record", "Треснувшая пластинка", "Музыка потопа.", 1),
        q("aquamirae:music_disc_horizon", "Пластинка Horizon", "Гимн сервера.", 1),
        q("aquamirae:music_disc_forsaken_drownage", "Пластинка Drownage", "Плач утопленных.", 1),
        q("aquamirae:shell_horn", "Раковинный рог", "Зов союзников.", 1),
        q("aquamirae:echo_compass", "Эхо-компас", "Путь сквозь бездну.", 1),
        q("minecraft:dark_prismarine", "Тёмный призмарин", "Камень храмов.", 16),
        q("minecraft:sea_lantern", "Морской фонарь", "Свет руин.", 4),
        q("minecraft:sponge", "Губка", "Осуши камеры руин.", 4),
        q("minecraft:wet_sponge", "Мокрая губка", "После осушения.", 4),
        q("minecraft:conduit", "Проводник", "База под водой.", 1),
        q("minecraft:trident", "Трезубец глубин", "Оружие стражей.", 1),
        q("minecraft:heart_of_the_sea", "Сердце моря", "Ключ к проводнику.", 1),
        q("alexscaves:depth_charge", "Глубинная бомба", "Штурм пещер глубин.", 1),
        q("minecraft:netherite_ingot", "Незерит", "Подготовка к финалу.", 1),
        q("aquamirae:terrible_helmet", "Ужасный шлем", "Реликвия капитана.", 1),
    ],
))

# ========== CH 11 AE2 ==========
CHAPTERS.append((
    "11_me", ACT_IV,
    "XI. МЭ · Цифра океана",
    "ae2:controller",
    "Вещество в код. Applied Energistics хранит флот ресурсов.",
    [
        q("ae2:certus_quartz_crystal", "Истинный кварц", "Кристалл сети.", 16),
        q("ae2:charged_certus_quartz_crystal", "Заряженный кварц", "Искра флюикса.", 8),
        q("ae2:fluix_crystal", "Флюикс", "Кровь МЭ.", 16),
        q("ae2:fluix_dust", "Флюикс-пыль", "Порошок сети.", 16),
        q("ae2:silicon", "Кремний", "Основа процессоров.", 16),
        q("ae2:logic_processor", "Логический процессор", "Мозг I.", 4),
        q("ae2:calculation_processor", "Вычислительный процессор", "Мозг II.", 4),
        q("ae2:engineering_processor", "Инженерный процессор", "Мозг III.", 4),
        q("ae2:inscriber", "Высекатель", "Печать схем.", 1),
        q("ae2:charger", "Зарядник", "Заряд кварца.", 1),
        q("ae2:energy_acceptor", "Приёмник энергии", "Ввод FE в МЭ.", 1),
        q("ae2:vibration_chamber", "Вибрационная камера", "Жги — заряжай сеть.", 1),
        q("ae2:fluix_glass_cable", "Стеклянный кабель", "Нервы сети.", 16),
        q("ae2:fluix_covered_cable", "Закрытый кабель", "Эстетика линий.", 8),
        q("ae2:fluix_smart_cable", "Умный кабель", "Видимый канал.", 8),
        q("ae2:fluix_covered_dense_cable", "Плотный кабель", "Толстый канал.", 4),
        q("ae2:controller", "МЭ Контроллер", "Сердце цифры.", 1),
        q("ae2:drive", "Накопитель", "Слоты ячеек.", 1),
        q("ae2:chest", "МЭ Сундук", "Простой доступ.", 1),
        q("ae2:cell_component_1k", "Компонент 1k", "Малый объём.", 2),
        q("ae2:cell_component_4k", "Компонент 4k", "Средний объём.", 2),
        q("ae2:cell_component_16k", "Компонент 16k", "Большой объём.", 1),
        q("ae2:cell_component_64k", "Компонент 64k", "Огромный объём.", 1),
        q("ae2:item_storage_cell_1k", "Ячейка 1k", "Первое цифровое хранилище.", 1),
        q("ae2:item_storage_cell_4k", "Ячейка 4k", "Больше слотов.", 1),
        q("ae2:item_storage_cell_16k", "Ячейка 16k", "Склад флота.", 1),
        q("ae2:item_storage_cell_64k", "Ячейка 64k", "Океан предметов.", 1),
        q("ae2:terminal", "Терминал", "Окно в сеть.", 1),
        q("ae2:crafting_terminal", "Крафт-терминал", "Крафт из сети.", 1),
        q("ae2:pattern_encoding_terminal", "Терминал шаблонов", "Запись рецептов.", 1),
        q("ae2:pattern_access_terminal", "Терминал доступа", "Контроль автокрафта.", 1),
        q("ae2:blank_pattern", "Пустой шаблон", "Запиши рецепт.", 8),
        q("ae2:interface", "Интерфейс", "Мост мир↔сеть.", 2),
        q("ae2:pattern_provider", "Поставщик шаблонов", "Автокрафт узел.", 2),
        q("ae2:molecular_assembler", "Молекулярный сборщик", "Крафт из шаблона.", 2),
        q("ae2:crafting_unit", "Блок автокрафта", "CPU стек.", 4),
        q("ae2:crafting_accelerator", "Ускоритель крафта", "Быстрее сборка.", 2),
        q("ae2:1k_crafting_storage", "Крафт-хранилище 1k", "Буфер рецептов.", 1),
        q("ae2:import_bus", "Шина импорта", "В сеть.", 2),
        q("ae2:export_bus", "Шина экспорта", "Из сети.", 2),
        q("ae2:storage_bus", "Шина хранилища", "Внешний сундук как МЭ.", 2),
        q("ae2:annihilation_plane", "Плоскость уничтожения", "Ломай в сеть.", 1),
        q("ae2:formation_plane", "Плоскость формирования", "Ставь из сети.", 1),
        q("ae2:level_emitter", "Эмиттер уровня", "Сигнал запасов.", 2),
        q("ae2:energy_cell", "Энергоячейка МЭ", "Буфер сети.", 1),
        q("ae2:dense_energy_cell", "Плотная энергоячейка", "Большой буфер.", 1),
        q("ae2:wireless_receiver", "Беспроводной приёмник", "Эфирный доступ.", 1),
        q("ae2:wireless_terminal", "Беспроводной терминал", "МЭ с палубы.", 1),
        q("ae2:quantum_ring", "Квантовое кольцо", "Дальняя связь.", 2),
        q("ae2:quantum_link", "Квантовая связь", "Мост островов.", 1),
        q("ae2:spatial_cell_component_2", "Простр. компонент", "Карманные измерения.", 1),
    ],
))

# ========== CH 12 Dreadnought ==========
CHAPTERS.append((
    "12_dreadnought", ACT_IV,
    "XII. Дредноут · Флагман океана",
    "vs_eureka:oak_ship_helm",
    "Собери левитирующий флагман Eureka — символ AquaTech.",
    [
        q("vs_eureka:oak_ship_helm", "Штурвал", "Сердце дредноута.", 1),
        q("vs_eureka:engine", "Судовой двигатель", "Тяга в небеса.", 2),
        q("vs_eureka:ballast", "Балласт", "Осадка и баланс.", 4),
        q("vs_eureka:floater", "Поплавок", "Плавучесть корпуса.", 8),
        q("vs_eureka:balloon", "Баллон", "Подъёмная сила.", 8),
        q("vs_eureka:anchor", "Якорь", "Удержи флагман.", 1),
        q("valkyrienskies:ship_assembler", "Сборщик кораблей", "Собери физику корпуса.", 1),
        q("minecraft:netherite_ingot", "Незерит корпуса", "Броня флагмана.", 4),
        q("minecraft:netherite_block", "Незеритовый блок", "Ядро палубы.", 1),
        q("minecraft:sea_lantern", "Фонари палубы", "Свет дредноута.", 8),
        q("minecraft:dark_prismarine", "Обшивка призмарина", "Морская броня.", 32),
        q("minecraft:prismarine_bricks", "Призмариновый кирпич", "Палубный настил.", 32),
        q("minecraft:iron_block", "Железный каркас", "Силовой набор.", 16),
        q("minecraft:copper_block", "Медная обшивка", "Патина времени.", 16),
        q("create:steam_engine", "Паровые машины", "Силовая установка.", 4),
        q("create:mechanical_bearing", "Подшипники башен", "Вращающиеся орудия.", 2),
        q("ae2:controller", "МЭ на борту", "Цифровой трюм.", 1),
        q("ae2:wireless_terminal", "Терминал капитана", "Сеть с мостика.", 1),
        q("mekanismgenerators:wind_generator", "Ветряки мачт", "Чистая энергия в рейсе.", 2),
        q("thermal:energy_cell", "Энергоячейки", "Буфер похода.", 2),
        q("sophisticatedbackpacks:backpack", "Рюкзак капитана", "Личный трюм.", 1),
        q("sophisticatedstorage:gold_chest", "Золотой сундук", "Казна флагмана.", 1),
        q("lightmanscurrency:coinpile_iron", "Монеты", "Экономика порта.", 4),
        q("waystones:waystone", "Вейстоун на палубе", "Возврат домой.", 1),
        q("minecraft:beacon", "Маяк", "Сигнал флота.", 1),
        q("minecraft:conduit", "Проводник носа", "Власть над водой.", 1),
        q("minecraft:elytra", "Элитры", "Личный полёт с борта.", 1),
        q("minecraft:firework_rocket", "Ракеты", "Ускорение элитр.", 16),
        q("minecraft:nether_star", "Звезда Незера", "Реактор символа.", 1),
        q("minecraft:dragon_egg", "Яйцо дракона", "Трофей края света.", 1),
        q("minecraft:totem_of_undying", "Тотем", "Бессмертие капитана.", 1),
        q("minecraft:enchanted_golden_apple", "Зачарованное яблоко", "Аварийный паёк.", 1),
        q("apotheosis:mythic_material", "Мифический материал", "Apotheosis сила.", 2),
        q("minecraft:trident", "Трезубец флагмана", "Оружие абордажа.", 1),
        q("aquamirae:shatterblade", "Клинок капитана", "Легенда глубин на борту.", 1),
        q("minecraft:spyglass", "Капитанская труба", "Обзор горизонта.", 1),
        q("minecraft:compass", "Компас мостика", "Курс домой.", 1),
        q("minecraft:clock", "Хронометр", "Время рейса.", 1),
        q("minecraft:map", "Карта мира", "Отметь атомы друзей.", 1),
        q("minecraft:writable_book", "Вахтенный журнал", "Пиши историю флота.", 1),
        q("minecraft:name_tag", "Бирка", "Имя дредноута.", 1),
        q("minecraft:bell", "Колокол", "Смена вахты.", 1),
        q("minecraft:respawn_anchor", "Якорь возрождения", "Точка в Незере.", 1),
        q("minecraft:lodestone", "Магнетит", "Компас на флагман.", 1),
        q("minecraft:recovery_compass", "Компас возврата", "Путь к смерти и назад.", 1),
        q("minecraft:end_crystal", "Кристалл Энда", "Опасная мощь.", 2),
        q("minecraft:shulker_box", "Шалкеровый ящик", "Трюм-карман.", 2),
        q("minecraft:netherite_chestplate", "Незеритовая кираса", "Доспех адмирала.", 1),
        q("minecraft:netherite_sword", "Незеритовый меч", "Клинок адмирала.", 1),
        q("minecraft:netherite_pickaxe", "Незеритовая кирка", "Строй и ломай везде.", 1),
        q("vs_eureka:oak_ship_helm", "Финальный штурвал", "Подними флагман. Горизонт твой.", 1),
    ],
))


def esc(s: str) -> str:
    return s.replace("\\", "\\\\").replace('"', '\\"')


def tag_task(item: str) -> str | None:
    tags = {
        "minecraft:oak_log": "minecraft:logs",
        "minecraft:oak_planks": "minecraft:planks",
        "minecraft:cobblestone": "forge:cobblestone",
        "minecraft:iron_ingot": "forge:ingots/iron",
        "minecraft:copper_ingot": "forge:ingots/copper",
        "minecraft:stick": "forge:rods/wooden",
    }
    return tags.get(item)


def quest_snbt(ch: int, qi: int, item: str, title: str, sub: str, reward: int, dep: str | None) -> str:
    qid = f"q_{ch}_{qi}"
    tid = f"t_{ch}_{qi}"
    rid = f"r_{ch}_{qi}"
    # nice path layout: snake / river of quests
    col = (qi - 1) % 10
    row = (qi - 1) // 10
    x = round((col - 4.5) * 1.6, 2)
    y = round((row - 2) * 1.8, 2)
    tag = tag_task(item)
    if tag:
        task_item = f'item: {{ id: "itemfilters:tag", Count: 1b, tag: {{ value: "{tag}" }} }}'
    else:
        task_item = f'item: "{item}"'
    dep_block = f'\n\t\t\tdependencies: ["{dep}"]' if dep else ""
    # chapter gate: first quest of ch>1 depends on last of previous
    return f"""\t\t{{
\t\t\tx: {x}d
\t\t\ty: {y}d
\t\t\tid: "{qid}"
\t\t\ttitle: "{esc(title)}"
\t\t\ticon: "{item}"
\t\t\tsubtitle: "{esc(sub)}"
\t\t\tshape: "rsquare"
\t\t\tsize: 1.0d{dep_block}
\t\t\ttasks: [{{
\t\t\t\tid: "{tid}"
\t\t\t\ttype: "item"
\t\t\t\t{task_item}
\t\t\t\tcount: 1L
\t\t\t}}]
\t\t\trewards: [{{
\t\t\t\tid: "{rid}"
\t\t\t\ttype: "item"
\t\t\t\titem: "{item}"
\t\t\t\tcount: {reward}
\t\t\t}}]
\t\t}}"""


def chapter_file(order: int, filename: str, group: str, title: str, icon: str, lore: str, quests: list) -> str:
    ch = order
    parts = []
    for i, (item, qtitle, sub, reward) in enumerate(quests, start=1):
        dep = None
        if i > 1:
            dep = f"q_{ch}_{i-1}"
        elif ch > 1:
            dep = f"q_{ch-1}_50"  # gate from previous chapter finale
        full_sub = f"{lore} {sub}"
        parts.append(quest_snbt(ch, i, item, f"{i}. {qtitle}", full_sub, reward, dep))
    body = ",\n".join(parts)
    return f"""{{
\tid: "{filename}"
\tgroup: "{group}"
\torder_index: {order - 1}
\tfilename: "{filename}"
\ttitle: "{esc(title)}"
\ticon: "{icon}"
\tdefault_quest_shape: "rsquare"
\tdefault_hide_dependency_lines: false
\tquest_links: []
\tquests: [
{body}
\t]
}}
"""


def data_snbt() -> str:
    return """{
\tdefault_autoclaim_rewards: "disabled"
\tdefault_consume_items: false
\tdefault_quest_disable_jei: false
\tdefault_quest_shape: "rsquare"
\tdefault_reward_team: false
\tdetection_delay: 20
\tdisable_gui: false
\tdrop_loot_crates: false
\temergency_items_cooldown: 0
\tgrid_scale: 0.55d
\ticon: "minecraft:oak_chest_boat"
\tlock_message: ""
\tpause_game: false
\tprogression_mode: "linear"
\ttitle: "AquaTech: Ocean Horizon"
\tversion: 13
}
"""


def groups_snbt() -> str:
    lines = []
    for gid, title in GROUPS:
        lines.append(f'\t\t{{\n\t\t\tid: "{gid}"\n\t\t\ttitle: "{esc(title)}"\n\t\t}}')
    return "{\n\tchapter_groups: [\n" + ",\n".join(lines) + "\n\t]\n}\n"


def main():
    assert len(CHAPTERS) == 12, len(CHAPTERS)
    fixed = []
    for c in CHAPTERS:
        fn, group, title, icon, lore, quests = c
        if len(quests) > 50:
            print(f"trim {fn}: {len(quests)} -> 50")
            quests = quests[:50]
        elif len(quests) < 50:
            raise SystemExit(f"{fn} has only {len(quests)} quests, need 50")
        fixed.append((fn, group, title, icon, lore, quests))
    chapters = fixed

    total = 0
    for out in OUT_DIRS:
        ch_dir = out / "chapters"
        ch_dir.mkdir(parents=True, exist_ok=True)
        for name in LEGACY:
            p = ch_dir / name
            if p.exists():
                p.unlink()
                print("removed", p)
        # wipe any leftover chapter_*.snbt not in new set
        keep = {c[0] + ".snbt" for c in CHAPTERS}
        for p in ch_dir.glob("*.snbt"):
            if p.name not in keep:
                p.unlink()
                print("removed extra", p)

        (out / "data.snbt").write_text(data_snbt(), encoding="utf-8")
        (out / "chapter_groups.snbt").write_text(groups_snbt(), encoding="utf-8")

        for order, (fn, group, title, icon, lore, quests) in enumerate(chapters, start=1):
            text = chapter_file(order, fn, group, title, icon, lore, quests)
            path = ch_dir / f"{fn}.snbt"
            path.write_text(text, encoding="utf-8")
            total += len(quests)
            print(f"wrote {path} ({len(quests)} quests)")

    print(f"TOTAL quest slots written across dirs: {total} (expect {12*50*len(OUT_DIRS)})")
    print("OK: 600 unique quests x", len(OUT_DIRS), "trees")


if __name__ == "__main__":
    main()
