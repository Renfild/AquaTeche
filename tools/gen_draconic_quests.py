#!/usr/bin/env python3
"""Generate the FTB Quests chapter "Draconic Evolution" (config/ftbquests/quests/chapters/draconic_aquatech.snbt).

The quest texts quote the rebuilt recipes from tools/gen_draconic_recipes.py. Every recipe fact used in a text is asserted
against the generated recipe, so the quests cannot drift from the real recipes. Do NOT re-run this after the chapter was
edited in game: pull the live files first.

Run:  python tools/gen_draconic_quests.py
"""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

import gen_draconic_recipes as G  # noqa: E402
from ftb_chapter import Chapter, GROUP_FINAL, coins, existing_quest_ids, item, item_names, sid, xp  # noqa: E402

DE = "draconicevolution:"

# quests of other chapters we depend on
BOT_MANASTEEL = "2D24321A0DD539D3"
BOT_LIVINGROCK = "281BD310F4AE52F5"
BOT_TERRASTEEL = "6DEB564BC5BBF7A8"
BOT_ALFSTEEL = "16B8F73ED297BC43"
AE2_FLUIX = "1D392AB2A35961BB"
AE2_ENGINEERING = "61F9290569BDC2AF"
AE2_DENSE_CELL = "5C2C83D9E7FD7C81"
IU_MFE = "59F5CC3C9A10473A"
AVA_LATTICE = "036A0FBF836BB6C2"
AVA_MATRIX = "5E5277DFF3957C35"
AVA_NEUTRON = "34797EC98AE1BB6E"
ALEX_NEODYMIUM = "4099054A63F0A45C"
ALEX_URANIUM_ROD = "60ADD93185D58FC2"
AVA_EXTREME = "4B2004A1971F11D9"
AVA_GEAR = "3B7A081B742E6FED"
AVA_INFINITY_CATALYST = "74E6680AE224D41C"
IU_PHOTONIY = "45EDE4D6196298DC"

IU = "industrialupgrade:"
C2 = IU + "crafting_elements/crafting_273_element"  # улучшенная электросхема
C3 = IU + "crafting_elements/crafting_274_element"  # композит
PHOTON = IU + "photoniy_ingot"
OSMIRIDIUM = IU + "alloyingot/osmiridium"


def pb(key):
    return sid("productivebees_aquatech", "quest", key)


# ---------------------------------------------------------------------------------------------------------------------
# recipe facts are checked against the generated recipes
# ---------------------------------------------------------------------------------------------------------------------
_ALL = G.load_recipes()
G.add_module_rules(sorted(k.split("/", 1)[1] for k in _ALL if k.startswith("modules/")))


def recipe(rid):
    return G.apply_ops(_ALL[rid], G.SPEC.get(rid, []), [])


def expect(rid, **counts):
    """expect('components/wyvern_core', **{'botania:terrasteel_ingot': 4}) asserts the ingredient counts of the new recipe."""
    have = G.multiset(recipe(rid))
    for name, n in counts.items():
        name = name.replace("TAG_forge:", "#")
        got = have.get(name, 0)
        assert got == n, f"{rid}: expected {n}x {name}, recipe has {got} ({dict(have)})"


def table9(rid):
    """The part is crafted on the 9x9 Extreme Crafting Table."""
    r = recipe(rid)
    assert r.get("type") == "avaritia:shaped_table" and r.get("tier") == 4, f"{rid}: not an extreme table recipe"
    assert all(len(row) == 9 for row in r["pattern"]) and len(r["pattern"]) == 9, f"{rid}: pattern is not 9x9"


def tier(rid, expected_tier, energy=None):
    r = recipe(rid)
    assert r.get("tier") == expected_tier, f"{rid}: tier {r.get('tier')} != {expected_tier}"
    if energy is not None:
        assert r.get("total_energy") == energy, f"{rid}: energy {r.get('total_energy')} != {energy}"


# --- the facts the texts below rely on ---------------------------------------------------------------------------------
expect("components/draconium_core", **{"TAG_forge:ingots/draconium": 4, "botania:manasteel_ingot": 4, "botania:mana_diamond": 1})
expect("machines/crafting_core", **{"ae2:engineering_processor": 4, "botania:mana_diamond": 4, "draconicevolution:draconium_core": 1})
expect("machines/basic_crafting_injector", **{"botania:mana_diamond": 2, "botania:livingrock": 5, "botania:manasteel_block": 1, "draconicevolution:draconium_core": 1})
expect("machines/basic_relay_crystal", **{"ae2:fluix_crystal": 4, "draconicevolution:wyvern_energy_core": 1})
expect("components/wyvern_core", **{"botania:terrasteel_ingot": 4, "draconicevolution:draconium_core": 4, "TAG_forge:nether_stars": 1})
expect("components/wyvern_energy_core", **{"industrialupgrade:wiring_storage/mfe_iu": 4, "TAG_forge:ingots/draconium": 4, "draconicevolution:draconium_core": 1})
expect("machines/wyvern_crafting_injector", **{"botania:dragonstone": 4, "draconicevolution:draconium_core": 2, "draconicevolution:wyvern_core": 1, "TAG_forge:storage_blocks/draconium": 1, C2: 2})
tier("machines/wyvern_crafting_injector", "DRACONIUM", 32000)
expect("tools/wyvern_pickaxe", **{"botania:terrasteel_ingot": 2, "draconicevolution:draconium_core": 1, "draconicevolution:basic_relay_crystal": 2, "draconicevolution:wyvern_energy_core": 1, C2: 1, "avaritia:diamond_lattice": 1})
tier("tools/wyvern_pickaxe", "WYVERN", 8000000)
expect("tools/wyvern_capacitor", **{"botania:terrasteel_ingot": 2, "TAG_forge:ingots/draconium": 2, "draconicevolution:wyvern_energy_core": 4, C2: 2})
expect("awakened_draconium_block", **{"draconicevolution:draconium_core": 4, "mythicbotany:alfsteel_ingot": 2, "draconicevolution:dragon_heart": 1})
tier("awakened_draconium_block", "WYVERN", 50000000)
expect("components/awakened_core", **{"draconicevolution:wyvern_core": 4, "TAG_forge:ingots/draconium_awakened": 2, "mythicbotany:alfsteel_ingot": 2, C3: 2})
tier("components/awakened_core", "WYVERN", 1000000)
expect("machines/awakened_crafting_injector", **{"avaritia:diamond_lattice": 4, "draconicevolution:wyvern_core": 2, OSMIRIDIUM: 2})
tier("machines/awakened_crafting_injector", "WYVERN", 256000)
expect("components/draconic_energy_core", **{"TAG_forge:ingots/draconium_awakened": 4, "draconicevolution:wyvern_energy_core": 4, "megacells:mega_energy_cell": 1, "avaritia:neutron_ingot": 4, PHOTON: 4})
table9("components/draconic_energy_core")
expect("machines/energy_core", **{"TAG_forge:ingots/draconium": 6, "draconicevolution:wyvern_energy_core": 2, "megacells:accumulation_processor": 1, "avaritia:crystal_matrix_ingot": 4, C3: 4})
table9("machines/energy_core")
expect("machines/draconic_relay_crystal", **{"draconicevolution:wyvern_energy_core": 4, "avaritia:diamond_lattice": 4, "draconicevolution:wyvern_core": 1, C3: 2})
expect("tools/draconic_staff", **{"avaritia:neutron_ingot": 2})
expect("machines/basic_wireless_crystal", **{"ae2:wireless_receiver": 2, "minecraft:ender_pearl": 4})
expect("machines/wyvern_wireless_crystal", **{"ae2:wireless_booster": 2})
expect("machines/draconic_wireless_crystal", **{"ae2:wireless_booster": 2, "botania:pixie_dust": 4})
expect("tools/draconic_pickaxe", **{"TAG_forge:ingots/netherite": 2, "mythicbotany:alfsteel_ingot": 2, "draconicevolution:wyvern_core": 1, "TAG_forge:ingots/draconium_awakened": 2, "draconicevolution:draconic_energy_core": 1, "avaritia:neutron_ingot": 1, C3: 1})
tier("tools/draconic_pickaxe", "DRACONIC", 32000000)
expect("machines/reactor_prt_stab_frame", **{"alexscaves:scarlet_neodymium_ingot": 6, "draconicevolution:wyvern_core": 1, "TAG_forge:ingots/draconium_awakened": 1, "avaritia:neutron_ingot": 4, OSMIRIDIUM: 4})
expect("machines/reactor_prt_in_rotor", **{"TAG_forge:ingots/draconium_awakened": 3, "alexscaves:scarlet_neodymium_ingot": 2, "draconicevolution:draconium_core": 1, "avaritia:neutron_ingot": 4, OSMIRIDIUM: 4})
expect("machines/reactor_prt_out_rotor", **{"avaritia:diamond_lattice": 3, "alexscaves:azure_neodymium_ingot": 2, "draconicevolution:draconium_core": 1, "avaritia:neutron_ingot": 4, OSMIRIDIUM: 4})
expect("machines/reactor_prt_focus_ring", **{"botania:elementium_ingot": 4, "avaritia:diamond_lattice": 3, "draconicevolution:wyvern_core": 2, "avaritia:neutron_ingot": 4, OSMIRIDIUM: 4})
for _part in ("reactor_prt_stab_frame", "reactor_prt_in_rotor", "reactor_prt_out_rotor", "reactor_prt_focus_ring"):
    table9("machines/" + _part)
expect("machines/reactor_stabilizer", **{OSMIRIDIUM: 2})
expect("machines/reactor_injector", **{PHOTON: 2})
expect("machines/reactor_core", **{"alexscaves:uranium_rod": 3, "TAG_forge:ingots/draconium_awakened": 4, "draconicevolution:large_chaos_frag": 2, "avaritia:neutron_ingot": 2})
tier("machines/reactor_core", "CHAOTIC", 64000000)
expect("machines/chaotic_crafting_injector", **{"avaritia:neutron_ingot": 4, "avaritia:crystal_matrix_ingot": 4, "minecraft:dragon_egg": 1, PHOTON: 2})
tier("machines/chaotic_crafting_injector", "DRACONIC", 8000000)
expect("components/chaotic_core", **{"avaritia:crystal_matrix_ingot": 2, "avaritia:neutron_gear": 2, "draconicevolution:awakened_core": 4, "draconicevolution:large_chaos_frag": 4})
tier("components/chaotic_core", "DRACONIC", 100000000)
expect("components/chaotic_energy_core", **{"draconicevolution:medium_chaos_frag": 4, "draconicevolution:draconic_energy_core": 4, "industrialupgrade:wiring_storage/qua_mfsu": 1, "avaritia:neutron_gear": 4, PHOTON: 4})
table9("components/chaotic_energy_core")
expect("tools/chaotic_pickaxe", **{"TAG_forge:ingots/draconium_awakened": 3, "avaritia:crystal_matrix_ingot": 2, "avaritia:neutron_ingot": 1, "draconicevolution:chaotic_core": 1, "draconicevolution:chaotic_energy_core": 1, "avaritia:neutron_gear": 1, PHOTON: 2})
tier("tools/chaotic_pickaxe", "CHAOTIC", 128000000)
tier("tools/chaotic_staff", "CHAOTIC", 1024000000)
expect("tools/chaotic_staff", **{"avaritia:eternal_singularity": 1, "avaritia:infinity_catalyst": 1})
expect("tools/chaotic_staff_alt", **{"avaritia:eternal_singularity": 1, "avaritia:infinity_catalyst": 1})
expect("modules/item_chaotic_damage", **{"avaritia:neutron_nugget": 4, C3: 4})
table9("modules/item_chaotic_damage")
expect("modules/module_core", **{"botania:manasteel_ingot": 4, "ae2:logic_processor": 2, "TAG_forge:ingots/gold": 2, "TAG_forge:ingots/draconium": 1})
expect("tools/magnet", **{"ae2:fluix_pearl": 1, "botania:manasteel_ingot": 2})

# ---------------------------------------------------------------------------------------------------------------------
ch = Chapter("draconic_aquatech", "Draconic Evolution", "draconicevolution:draconic_chestpiece", GROUP_FINAL, 3)
add = ch.add


def s(x, y):
    return x * 2, y * 2


# ---------------------------------------------------------------------------------------------------------------------
# Драконит и основы
# ---------------------------------------------------------------------------------------------------------------------
add("d01", "Draconic Evolution", DE + "draconic_chestpiece", *s(0, 0), [
    "Draconic Evolution строится на слиянии (Fusion Crafting): в центре стоит ядро слияния, вокруг него инжекторы. В инжекторы кладут ингредиенты, ядру подводят энергию, и через время получается результат.",
    "На AquaTech рецепты мода пересобраны под моды сервера. Вместо алмазов и золота нужны манасталь, террасталь и альфсталь из Botania и Mythic Botany, процессоры AE2 и MEGA, накопители IndustrialUpgrade, а в финале нейтроний и кристаллическая матрица Avaritia.",
    "Это эндгейм: верхние тиры просят тяжёлые детали. Улучшенные электросхемы и композит, осмиридий и фотонные слитки IndustrialUpgrade, решётки алмаза, нейтроний и шестерни нейтрония Avaritia, а для посоха хаоса ещё вечная сингулярность и катализатор бесконечности. Самые дорогие части (энергоядра, детали реактора, модули хаоса) собираются на Экстремальном верстаке Avaritia 9×9.",
    "Тиры идут по порядку: драконит, виверна (Wyvern), дракон (Draconic, пробуждённый), хаос (Chaotic). Инжектор следующего тира собирают на инжекторах предыдущего.",
    "Энергия и время слияния остались как в моде, сложность идёт от материалов. Все рецепты смотри в JEI, а здесь они расписаны по шагам.",
    "Дислокаторы (телепорты) на сервере отключены. Магнит собирается без них.",
], ("checkmark", "Прочитать про слияние"), rewards=[xp(100), coins(1000)])

add("d02", "Откуда драконит", DE + "draconium_dust", *s(1, 0), [
    "В нашем мире нет руды драконита и острова хаоса. Драконит берётся тремя способами.",
    "Первый: дракон Энда. С него падает драконья пыль (по настройке сервера 64 штуки за убийство), а яйцо дракона появляется после каждого убийства. Дракона можно вызвать заново четырьмя кристаллами Энда.",
    "Второй: кейс «Драконий» (F4, вкладка «Кейсы»). В нём слитки, ядра драконита и виверны, детали слияния и сердце дракона.",
    "Третий: пчела драконита из главы Productive Bees. Её соты в центрифуге дают самородки драконита.",
    "Драконья пыль плавится в слиток драконита. Из девяти самородков складывается слиток, из девяти слитков блок.",
], ("item", DE + "draconium_dust", 1), deps=["d01"], rewards=[xp(100), coins(1000)])

add("d03", "Слиток драконита", DE + "draconium_ingot", *s(2, 0), [
    "Переплавь драконью пыль в печи. Самородки, слитки и блоки складываются друг в друга и обратно рецептами 3×3 и бесформенными рецептами.",
    "Слиток драконита нужен везде. Он идёт в ядра, инжекторы, инструменты и заготовки машин. Береги его: добыча ограничена драконом, кейсами и пчёлами.",
], ("item", DE + "draconium_ingot", 1), deps=["d02"], rewards=[xp(100), coins(1000)])

add("d04", "Ядро драконита", DE + "draconium_core", *s(3, 0), [
    "Базовая деталь всего мода. Рецепт 3×3: 4 слитка драконита по углам, 4 слитка манасталь по сторонам и алмаз маны в центре.",
    "Манасталь и алмаз маны делают в Botania: мана-бассейн, слиток манасталь, алмаз маны. Манасталь можно получать и от пчелы манасталь (глава Productive Bees).",
    "Ядро драконита идёт в ядро виверны, ядро слияния и почти все инжекторы.",
], ("item", DE + "draconium_core", 1), deps=["d03", BOT_MANASTEEL], rewards=[xp(150), coins(1500)])

add("d05", "Ядро слияния", DE + "crafting_core", *s(4, 0), [
    "Ядро слияния (Fusion Crafting Core) стоит в центре. Рецепт 3×3: 4 инженерных процессора AE2 по углам, 4 алмаза маны по сторонам и ядро драконита в центре.",
    "Инженерный процессор печатают на инскрайбере AE2 (см. главу Applied Energistics).",
    "Правый клик по ядру открывает его окно: там выбирается рецепт и виден заряд.",
], ("item", DE + "crafting_core", 1), deps=["d04", AE2_ENGINEERING], rewards=[xp(150), coins(1500)])

add("d06", "Инжектор слияния", DE + "basic_crafting_injector", *s(5, 0), [
    "Инжекторы ставят вокруг ядра. В каждый кладут по ингредиенту, а ядро забирает их и собирает результат.",
    "Рецепт базового инжектора: сверху 2 алмаза маны и ядро драконита между ними, в центре блок манасталь, по бокам и снизу 5 живых камней (Livingrock).",
    "Инжекторы старших тиров делаются слиянием и требуют инжекторов предыдущего тира.",
], ("item", DE + "basic_crafting_injector", 1), deps=["d05", BOT_LIVINGROCK], rewards=[xp(150), coins(1500)])

add("d07", "Как проходит слияние", DE + "crafting_core", *s(6, 0), [
    "Поставь ядро слияния, а инжекторы вокруг него на расстоянии от 2 до 16 блоков (так настроен сервер). Положи катализатор в ядро, а ингредиенты рецепта в инжекторы.",
    "Тир инжекторов должен быть не ниже тира рецепта: у рецепта в JEI указан тир (DRACONIUM, WYVERN, DRACONIC, CHAOTIC) и сколько энергии он требует.",
    "Энергию в ядро приводят кристаллами-реле от энергоядра. Кристаллы связываются кристальным связывателем (Crystal Binder), который собирается так же из драконита, алмаза маны и палки блейза.",
], ("checkmark", "Провести первое слияние"), deps=["d06"], rewards=[xp(200), coins(2000)])

# ---------------------------------------------------------------------------------------------------------------------
# Виверна
# ---------------------------------------------------------------------------------------------------------------------
add("d10", "Ядро виверны", DE + "wyvern_core", *s(0, 1), [
    "Ядро виверны: 4 слитка террастали по углам, 4 ядра драконита по сторонам и звезда Незера в центре.",
    "Террасталь делают на терра-плите Botania или берут из сот пчелы террастали (глава Productive Bees). Звезду Незера дают Иссушитель и редкий улов удочкой (секретный квест «Звезда в Сети»).",
    "Это ключевая деталь тира виверны: ядро идёт в инжектор виверны, инструменты, броню и ядро пробуждения.",
], ("item", DE + "wyvern_core", 1), deps=["d04", BOT_TERRASTEEL], rewards=[xp(250), coins(3000)])

add("d11", "Инжектор виверны", DE + "wyvern_crafting_injector", *s(1, 1), [
    "Слияние тира DRACONIUM (32 000 энергии). Катализатор: базовый инжектор.",
    "Ингредиенты: ядро виверны, 2 ядра драконита, 4 драконьих камня Botania (Elven Dragonstone), блок драконита и 2 улучшенные электросхемы IndustrialUpgrade.",
    "Драконьи камни получают у эльфов в Альфхейме (глава Botania).",
], ("item", DE + "wyvern_crafting_injector", 1), deps=["d10", "d06"], rewards=[xp(250), coins(3000)])

add("d08", "Кристаллы-реле", DE + "basic_relay_crystal", *s(2, 1), [
    "Кристаллы-реле передают энергию от энергоядра к машинам и ядру слияния. Базовый: 4 кристалла флюкса AE2 вокруг энергоядра виверны. Из одного рецепта выходит 4 кристалла.",
    "Старшие кристаллы: виверна (4 энергоядра виверны по углам, 4 базовых кристалла по сторонам и инженерный процессор AE2 в центре) и дракон (слияние тира DRACONIC на 128 000 энергии: 4 энергоядра виверны, ядро виверны, 4 решётки алмаза Avaritia и 2 композита IndustrialUpgrade).",
    "Из реле делают входные и выходные кристаллы (IO), два одинаковых IO сливаются обратно в реле.",
], ("item", DE + "basic_relay_crystal", 1), deps=["d07", AE2_FLUIX], rewards=[xp(250), coins(3000)])

add("d09", "Энергоядро виверны", DE + "wyvern_energy_core", *s(3, 1), [
    "Хранит энергию для инструментов и машин. Рецепт 3×3: 4 слитка драконита по углам, 4 накопителя MFE IndustrialUpgrade по сторонам и ядро драконита в центре.",
    "Одного энергоядра хватает на один инструмент. Для кристаллов-реле и кабелей понадобится несколько штук: строй линию MFE заранее.",
    "Сами MFE описаны в главе улучшенной электрической эры.",
], ("item", DE + "wyvern_energy_core", 1), deps=["d08", "d10", IU_MFE], rewards=[xp(250), coins(4000)])

add("d12", "Энергоядро (мультиблок)", DE + "energy_core", *s(4, 1), [
    "Большое хранилище энергии из блоков. Ядро собирается на Экстремальном верстаке Avaritia 9×9. В центре поле 3×3: 6 слитков драконита, 2 энергоядра виверны и процессор накопления MEGA. Вокруг него по диагоналям 4 слитка кристаллической матрицы, по осям 4 композита IndustrialUpgrade.",
    "Вокруг ядра ставят стабилизаторы (4 драконьих камня и генератор частиц), к ним подключаются энергопилоны и кристаллы-реле. Энергопилон: 4 слитка драконита, пыль пикси, 2 заряженных кристалла истинного кварца AE2, ядро драконита и алмаз маны (выходит 2 штуки).",
    "Ядро строится ступенями: чем выше тир блоков, тем больше ёмкость.",
], ("item", DE + "energy_core", 1), deps=["d09", AVA_EXTREME], rewards=[xp(300), coins(4000)])

add("d13", "Инструменты виверны", DE + "wyvern_pickaxe", *s(5, 1), [
    "Слияние тира WYVERN (8 000 000 энергии). Катализатор: алмазный инструмент того же вида.",
    "Ингредиенты: ядро драконита, 2 слитка террастали, 2 базовых кристалла-реле, энергоядро виверны, улучшенная электросхема IndustrialUpgrade и решётка алмаза Avaritia. Катализатор: алмазный инструмент того же вида (для лука обычный лук, для нагрудника алмазный нагрудник). Так собираются кирка, топор, лопата, мотыга, меч, лук и нагрудник виверны.",
    "Инструменты работают на энергии: заряжай их в энергоядре или зарядном блоке. Модули (отдельная ветка) дают им новые умения.",
], ("item", DE + "wyvern_pickaxe", 1), deps=["d11", "d09"], rewards=[xp(400), coins(5000)])

add("d14", "Конденсатор виверны", DE + "wyvern_capacitor", *s(6, 1), [
    "Слияние тира WYVERN (8 000 000 энергии). Катализатор: ядро виверны. Ингредиенты: 4 энергоядра виверны, 2 слитка террастали, 2 слитка драконита и 2 улучшенные электросхемы IndustrialUpgrade.",
    "Конденсатор (Capacitor) носят с собой, он подзаряжает вещи из инвентаря. Нагрудник виверны собирается слиянием на алмазном нагруднике так же, как инструменты.",
], ("item", DE + "wyvern_capacitor", 1), deps=["d13"], rewards=[xp(400), coins(6000)])

add("d15", "Модули", DE + "module_core", *s(7, 1), [
    "Модули ставятся в инструменты и броню через окно модулей. Основа всех модулей: ядро модуля. Рецепт: 4 слитка манасталь по углам, 2 процессора логики AE2, 2 золотых слитка и слиток драконита.",
    "Лестница материалов в модулях: драконит использует манасталь, виверна использует террасталь вместо драконита, дракон использует альфсталь вместо незерита, хаос использует кристаллическую матрицу Avaritia.",
    "Каждый следующий модуль собирается из предыдущего: так, модуль урона виверны требует модуль урона драконита.",
], ("item", DE + "module_core", 1), deps=["d10"], rewards=[xp(300), coins(4000)])

# ---------------------------------------------------------------------------------------------------------------------
# Дракон (пробуждённый)
# ---------------------------------------------------------------------------------------------------------------------
add("d16", "Пробуждённый драконит", DE + "awakened_draconium_block", *s(0, 2), [
    "Слияние тира WYVERN (50 000 000 энергии). Катализатор: 4 блока драконита. Ингредиенты: 4 ядра драконита, 2 слитка альфсталь и сердце дракона. Получается 4 блока пробуждённого драконита.",
    "Из блока выходит девять слитков, из слитка девять самородков. Альфсталь делают в Mythic Botany или берут у пчелы альфсталь. Сердце дракона даёт дракон Энда и кейс «Драконий».",
    "Дальше пробуждённый драконит можно добывать у пчелы пробуждения (глава Productive Bees).",
], ("item", DE + "awakened_draconium_block", 1), deps=["d10", BOT_ALFSTEEL], rewards=[xp(500), coins(8000)])

add("d17", "Ядро пробуждения", DE + "awakened_core", *s(1, 2), [
    "Слияние тира WYVERN (1 000 000 энергии). Катализатор: звезда Незера. Ингредиенты: 4 ядра виверны, 2 слитка пробуждённого драконита, 2 слитка альфсталь и 2 композита IndustrialUpgrade.",
    "Ядро пробуждения нужно для инжектора пробуждения, энергоядра дракона и всех инструментов дракона.",
], ("item", DE + "awakened_core", 1), deps=["d16", "d11"], rewards=[xp(500), coins(8000)])

add("d18", "Инжектор пробуждения", DE + "awakened_crafting_injector", *s(2, 2), [
    "Слияние тира WYVERN (256 000 энергии). Катализатор: инжектор виверны. Ингредиенты: 4 решётки алмаза Avaritia, 2 ядра виверны, блок пробуждённого драконита и 2 осмиридиевых слитка IndustrialUpgrade.",
    "Решётка алмаза открывает цепочку Avaritia: следуй главе про Avaritia, пока не получишь решётку.",
], ("item", DE + "awakened_crafting_injector", 1), deps=["d17", AVA_LATTICE], rewards=[xp(500), coins(8000)])

add("d19", "Энергоядро дракона", DE + "draconic_energy_core", *s(3, 2), [
    "Экстремальный верстак Avaritia 9×9. В центре поле 3×3: 4 слитка пробуждённого драконита по углам, 4 энергоядра виверны по сторонам и энергоячейка MEGA в центре. Вокруг по диагоналям 4 нейтрониевых слитка, по осям 4 фотонных слитка IndustrialUpgrade.",
    "Энергоячейка MEGA собирается на основе плотной энергоячейки AE2 (см. главу AE2).",
    "Нужно для инструментов, брони и конденсатора дракона, а также для энергоядра хаоса.",
], ("item", DE + "draconic_energy_core", 1), deps=["d17", "d09", AE2_DENSE_CELL, AVA_EXTREME, IU_PHOTONIY], rewards=[xp(500), coins(9000)])

add("d20", "Беспроводные кристаллы", DE + "wyvern_wireless_crystal", *s(4, 2), [
    "Передают энергию без кабелей. Рецепт 3×3: 4 жемчужины Края по углам, генератор частиц сверху и снизу, беспроводной приёмник или усилитель AE2 слева и справа и кристалл-реле в центре.",
    "Базовый кристалл использует приёмник AE2, виверна и дракон используют усилитель. Кристалл дракона дополнительно требует пыль пикси вместо жемчужин.",
], ("item", DE + "wyvern_wireless_crystal", 1), deps=["d12"], rewards=[xp(400), coins(6000)])

add("d21", "Инструменты дракона", DE + "draconic_pickaxe", *s(5, 2), [
    "Слияние тира DRACONIC (32 000 000 энергии). Катализатор: инструмент виверны того же вида.",
    "Ингредиенты: ядро виверны, 2 слитка незерита, 2 слитка альфсталь, 2 слитка пробуждённого драконита, энергоядро дракона, нейтрониевый слиток и композит IndustrialUpgrade.",
    "Так собираются кирка, топор, лопата, мотыга, меч, лук и нагрудник дракона. Конденсатор и посох собираются отдельно, посох дракона дополнительно просит 2 нейтрониевых слитка.",
], ("item", DE + "draconic_pickaxe", 1), deps=["d18", "d19"], rewards=[xp(800), coins(15000)])

add("d22", "Детали реактора", DE + "reactor_prt_stab_frame", *s(6, 2), [
    "Реактор вырабатывает много энергии, но способен взорваться. Строй его вдали от базы, держи под рукой аварийное отключение и проверь стабилизацию по компаратору.",
    "Четыре детали собираются на Экстремальном верстаке Avaritia 9×9. В центре поле 3×3, вокруг по диагоналям 4 нейтрониевых слитка, по осям 4 осмиридиевых слитка IndustrialUpgrade.",
    "Каркас стабилизатора, центр 3×3: 6 слитков алого неодима AlexsCaves, ядро виверны и слиток пробуждённого драконита. Входной ротор: 3 слитка пробуждённого драконита, 2 слитка алого неодима и ядро драконита.",
    "Выходной ротор: 3 решётки алмаза Avaritia, 2 слитка лазурного неодима и ядро драконита. Фокусирующее кольцо: 4 слитка элементиума, 3 решётки алмаза и 2 ядра виверны.",
    "Стабилизатор реактора дополнительно просит 2 осмиридиевых слитка, инжектор реактора 2 фотонных слитка.",
], ("item", DE + "reactor_prt_stab_frame", 1), deps=["d21", ALEX_NEODYMIUM, AVA_EXTREME], rewards=[xp(800), coins(15000)])

# ---------------------------------------------------------------------------------------------------------------------
# Хаос
# ---------------------------------------------------------------------------------------------------------------------
add("d24", "Путь к хаосу", DE + "chaos_shard", *s(0, 3), [
    "Хранителя хаоса и острова хаоса в нашем мире нет, поэтому осколки хаоса получают только от пчелы хаоса (глава Productive Bees).",
    "Чтобы не получился замкнутый круг, инжектор хаоса и сама пчела хаоса собираются без осколков: на нейтронии и кристаллической матрице Avaritia.",
    "План: инжектор хаоса, затем слияние пчелы хаоса, затем улей с пчёлами хаоса, затем центрифуга. Осколки пойдут потоком, и только после этого открываются ядро хаоса, реактор, оружие и посох.",
], ("checkmark", "Составить план по хаосу"), deps=["d18", AVA_NEUTRON, AVA_MATRIX], rewards=[xp(800), coins(15000)])

add("d25", "Инжектор хаоса", DE + "chaotic_crafting_injector", *s(1, 3), [
    "Слияние тира DRACONIC (8 000 000 энергии). Катализатор: инжектор пробуждения. Ингредиенты: 4 нейтрониевых слитка, 4 слитка кристаллической матрицы, яйцо дракона и 2 фотонных слитка IndustrialUpgrade.",
    "Осколки хаоса в этом рецепте не нужны, это сознательное отличие от оригинального мода.",
], ("item", DE + "chaotic_crafting_injector", 1), deps=["d24"], rewards=[xp(1000), coins(20000)])

add("d26", "Пчела хаоса", "productivebees:spawn_egg_configurable_bee", *s(2, 3), [
    "Рецепт и подробности смотри в главе Productive Bees, квест «Пчела хаоса». Это слияние тира CHAOTIC (64 000 000 энергии) на яйце пчелы пробуждения.",
    "Яйцо выпускают в улей. Её соты в центрифуге дают малые и средние осколки хаоса.",
], ("checkmark", "Получить пчелу хаоса"), deps=["d25", pb("i3")], rewards=[xp(1000), coins(20000)])

add("d27", "Осколки хаоса", DE + "small_chaos_frag", *s(3, 3), [
    "Соты пчелы хаоса дают 1–2 малых осколка и с шансом 15% средний. Девять малых складываются в средний, девять средних в большой, девять больших в осколок хаоса.",
    "Для ядра хаоса нужно 4 больших осколка, то есть 324 малых. Строй ферму с продуктивностью, расширителями и электрической центрифугой (рецепт фермы в главе Productive Bees).",
], ("item", DE + "small_chaos_frag", 1), deps=["d26", pb("i5")], rewards=[xp(1000), coins(20000)])

add("d28", "Ядро хаоса", DE + "chaotic_core", *s(4, 3), [
    "Слияние тира DRACONIC (100 000 000 энергии). Катализатор: большой осколок хаоса. Ингредиенты: 2 слитка кристаллической матрицы, 2 шестерни нейтрония, 4 ядра пробуждения и ещё 4 больших осколка.",
    "Всего на ядро уходит 5 больших осколков: 4 в ингредиентах и 1 катализатором, итого около 405 малых.",
], ("item", DE + "chaotic_core", 1), deps=["d27", "d17"], rewards=[xp(1500), coins(30000)])

add("d29", "Энергоядро хаоса", DE + "chaotic_energy_core", *s(5, 3), [
    "Экстремальный верстак Avaritia 9×9. В центре поле 3×3: 4 средних осколка хаоса по углам, 4 энергоядра дракона по сторонам и накопитель Quantum MFSU IndustrialUpgrade в центре. Вокруг по диагоналям 4 шестерни нейтрония, по осям 4 фотонных слитка.",
    "Хранит больше всего энергии и питает инструменты и броню хаоса.",
], ("item", DE + "chaotic_energy_core", 1), deps=["d28", "d19", AVA_GEAR], rewards=[xp(1500), coins(30000)])

add("d30", "Оружие хаоса", DE + "chaotic_pickaxe", *s(6, 3), [
    "Слияние тира CHAOTIC (128 000 000 энергии). Катализатор: инструмент дракона того же вида.",
    "Ингредиенты: ядро хаоса, энергоядро хаоса, 3 слитка пробуждённого драконита, 2 слитка кристаллической матрицы, нейтрониевый слиток, шестерня нейтрония и 2 фотонных слитка IndustrialUpgrade.",
    "Так собираются кирка, топор, лопата, мотыга, меч, лук, нагрудник и конденсатор хаоса.",
], ("item", DE + "chaotic_pickaxe", 1), deps=["d29", "d21"], rewards=[xp(2000), coins(40000)])

add("d31", "Посох хаоса", DE + "chaotic_staff", *s(7, 3), [
    "Универсальный инструмент: слияние тира CHAOTIC на 1 024 000 000 энергии.",
    "Есть два рецепта: из кирки, меча и лопаты хаоса, либо на основе посоха дракона. Оба требуют осколки хаоса, энергоядро хаоса и ядро хаоса.",
    "В обоих рецептах есть вечная сингулярность и катализатор бесконечности Avaritia. Это самые тяжёлые детали на сервере: рецепты вечной сингулярности и катализатора смотри в JEI и в главе Avaritia.",
], ("item", DE + "chaotic_staff", 1), deps=["d30", AVA_INFINITY_CATALYST], rewards=[xp(3000), coins(60000)])

add("d32", "Модули хаоса", DE + "item_chaotic_damage", *s(8, 3), [
    "Модули хаоса собираются на Экстремальном верстаке Avaritia 9×9 из модулей дракона, ядра пробуждения и средних осколков хаоса. Вместо незерита в них идёт кристаллическая матрица Avaritia. Вокруг основного рецепта по диагоналям 4 нейтрониевых самородка, по осям 4 композита IndustrialUpgrade.",
    "Начни с урона, энергии и скорости: они самые простые. Модуль полёта требует большие осколки хаоса и зелья, модуль бессмертия чарованное золотое яблоко и модуль ёмкости щита.",
], ("item", DE + "item_chaotic_damage", 1), deps=["d28", "d15", AVA_EXTREME], rewards=[xp(2000), coins(40000)])

add("d23", "Реактор", DE + "reactor_core", *s(7, 2), [
    "Слияние тира CHAOTIC (64 000 000 энергии). Катализатор: осколок хаоса. Ингредиенты: 4 слитка пробуждённого драконита, 3 урановых стержня AlexsCaves, 2 больших осколка хаоса и 2 нейтрониевых слитка.",
    "Реактор жжёт драконит и хаос и даёт очень много энергии. Следи за температурой и насыщением: при перегреве реактор взрывается. Включи полуавтоматическое отключение (SAS), если нужно, чтобы он остановился сам.",
    "Построй реактор вдали от основной базы и от соседей: взрыв разрушает всё вокруг.",
], ("item", DE + "reactor_core", 1), deps=["d22", ALEX_URANIUM_ROD, "d27"], rewards=[xp(2000), coins(40000)])

# ---------------------------------------------------------------------------------------------------------------------
# Прочее
# ---------------------------------------------------------------------------------------------------------------------
add("d34", "Магнит", DE + "magnet", *s(8, 1), [
    "Магнит подбирает предметы вокруг игрока. Оригинальный рецепт требовал дислокатор, а дислокаторы на сервере отключены, поэтому магнит собирается без него.",
    "Рецепт: 2 редстоуна сверху по краям, 2 слитка драконита в середине по краям, 2 слитка манасталь снизу по краям и жемчужина флюкса AE2 между ними.",
], ("item", DE + "magnet", 1), deps=["d07"], rewards=[xp(200), coins(3000)])

add("d33", "Хозяин дракона", DE + "chaotic_chestpiece", *s(5, 4), [
    "Ты прошёл весь Draconic Evolution: от драконьей пыли до посоха хаоса, реактора и модулей хаоса.",
    "Теперь у тебя самое сильное снаряжение сервера и вся инфраструктура под него.",
], ("checkmark", "Закончить главу Draconic Evolution"), deps=["d31", "d32", "d23"], rewards=[xp(5000), coins(100000)])


def main():
    names = item_names()
    problems = ch.validate(names, existing_quest_ids())
    if problems:
        print("\n".join(problems))
        sys.exit(1)
    ch.write()
    print(f"draconic_aquatech.snbt: {len(ch.quests)} quests")


if __name__ == "__main__":
    main()
