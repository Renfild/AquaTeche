import os

ftb_dirs = [
    "config/ftbquests/quests",
    "server/config/ftbquests/quests"
]

data_snbt = """{
	default_reward_team: false
	default_quest_disable_jei: false
	default_quest_shape: ""
	default_consume_items: false
	lock_quests_on_shutdown: false
	title: "AquaTech: Ocean Horizon"
	icon: "minecraft:fishing_rod"
	version: 13
}
"""

ch1_snbt = """{
	id: "chapter_1_ocean"
	group: ""
	order_index: 0
	filename: "chapter_1_ocean"
	title: "1. 🌊 Океанский Старт"
	icon: "minecraft:fishing_rod"
	default_quest_shape: ""
	default_hide_dependency_lines: false
	quests: [
		{
			x: -3.0d
			y: 0.0d
			id: "q1_fishing_rod"
			title: "Первая Удочка"
			icon: "minecraft:fishing_rod"
			subtitle: "Получите удочку для добычи ресурсов из океана."
			tasks: [{
				id: "t1_rod"
				type: "item"
				item: "minecraft:fishing_rod"
			}]
			rewards: [{
				id: "r1_bread"
				type: "item"
				item: "minecraft:bread"
				count: 16
			}]
		}
		{
			x: -1.0d
			y: 0.0d
			id: "q2_logs"
			title: "Морская Древесина"
			icon: "minecraft:oak_log"
			subtitle: "Выловите любое бревно из океана (дуб, береза, ель и др.)."
			dependencies: ["q1_fishing_rod"]
			tasks: [{
				id: "t2_logs"
				type: "item"
				item: {
					id: "itemfilters:tag"
					Count: 1b
					tag: {
						value: "minecraft:logs"
					}
				}
				count: 4L
			}]
			rewards: [{
				id: "r2_iron"
				type: "item"
				item: "minecraft:iron_nugget"
				count: 16
			}]
		}
		{
			x: 1.0d
			y: 0.0d
			id: "q3_planks"
			title: "Обработка Древесины"
			icon: "minecraft:oak_planks"
			subtitle: "Скрафтите любые доски из выловленного дерева."
			dependencies: ["q2_logs"]
			tasks: [{
				id: "t3_planks"
				type: "item"
				item: {
					id: "itemfilters:tag"
					Count: 1b
					tag: {
						value: "minecraft:planks"
					}
				}
				count: 16L
			}]
			rewards: [{
				id: "r3_furnace"
				type: "item"
				item: "minecraft:furnace"
			}]
		}
		{
			x: 3.0d
			y: 0.0d
			id: "q4_cobblestone"
			title: "Каменный Век"
			icon: "minecraft:cobblestone"
			subtitle: "Выловите булыжник из океана."
			dependencies: ["q3_planks"]
			tasks: [{
				id: "t4_cobble"
				type: "item"
				item: {
					id: "itemfilters:tag"
					Count: 1b
					tag: {
						value: "forge:cobblestone"
					}
				}
				count: 16L
			}]
			rewards: [{
				id: "r4_pickaxe"
				type: "item"
				item: "minecraft:stone_pickaxe"
			}]
		}
		{
			x: 5.0d
			y: 0.0d
			id: "q5_iron"
			title: "Первое Железо"
			icon: "minecraft:iron_ingot"
			subtitle: "Получите железные слитки из самородков или руды."
			dependencies: ["q4_cobblestone"]
			tasks: [{
				id: "t5_iron"
				type: "item"
				item: {
					id: "itemfilters:tag"
					Count: 1b
					tag: {
						value: "forge:ingots/iron"
					}
				}
				count: 4L
			}]
			rewards: [{
				id: "r5_iron_bonus"
				type: "item"
				item: "minecraft:iron_ingot"
				count: 8
			}]
		}
	]
}
"""

ch2_snbt = """{
	id: "chapter_2_animals"
	group: ""
	order_index: 1
	filename: "chapter_2_animals"
	title: "2. 🐓 Курицы и Пчелы"
	icon: "productivebees:advanced_oak_beehive"
	default_quest_shape: ""
	default_hide_dependency_lines: false
	quests: [
		{
			x: -2.0d
			y: 0.0d
			id: "q21_chickens"
			title: "Пернатые Ресурсы (Roost)"
			icon: "minecraft:egg"
			subtitle: "Разведите ресурсных куриц в курятниках."
			tasks: [{
				id: "t21_egg"
				type: "item"
				item: "minecraft:egg"
				count: 16L
			}]
			rewards: [{
				id: "r21_raw_iron"
				type: "item"
				item: "minecraft:raw_iron"
				count: 8
			}]
		}
		{
			x: 0.0d
			y: 0.0d
			id: "q22_bees"
			title: "Продуктивные Пчелы"
			icon: "productivebees:advanced_oak_beehive"
			subtitle: "Сберите улей Productive Bees для получения сот."
			dependencies: ["q21_chickens"]
			tasks: [{
				id: "t22_hive"
				type: "item"
				item: "productivebees:advanced_oak_beehive"
			}]
			rewards: [{
				id: "r22_honeycomb"
				type: "item"
				item: "minecraft:honeycomb"
				count: 8
			}]
		}
		{
			x: 2.0d
			y: 0.0d
			id: "q23_mystical"
			title: "Мистическая Агрокультура"
			icon: "mysticalagriculture:prosperous_seed_base"
			subtitle: "Вырастите ресурсные эссенции на грядках."
			dependencies: ["q22_bees"]
			tasks: [{
				id: "t23_seed"
				type: "item"
				item: "mysticalagriculture:prosperous_seed_base"
			}]
			rewards: [{
				id: "r23_essence"
				type: "item"
				item: "mysticalagriculture:inferium_essence"
				count: 16
			}]
		}
	]
}
"""

ch3_snbt = """{
	id: "chapter_3_create"
	group: ""
	order_index: 2
	filename: "chapter_3_create"
	title: "3. ⚙️ Эра Механики"
	icon: "create:cogwheel"
	default_quest_shape: ""
	default_hide_dependency_lines: false
	quests: [
		{
			x: -1.0d
			y: 0.0d
			id: "q31_waterwheel"
			title: "Водяное Колесо"
			icon: "create:water_wheel"
			subtitle: "Используйте течение океана для генерации кинетической энергии."
			tasks: [{
				id: "t31_wheel"
				type: "item"
				item: "create:water_wheel"
			}]
			rewards: [{
				id: "r31_shafts"
				type: "item"
				item: "create:shaft"
				count: 16
			}]
		}
		{
			x: 1.0d
			y: 0.0d
			id: "q32_crusher"
			title: "Дробильные Колеса"
			icon: "create:crushing_wheel"
			subtitle: "Сберите дробилки для дублирования руды."
			dependencies: ["q31_waterwheel"]
			tasks: [{
				id: "t32_crusher"
				type: "item"
				item: "create:crushing_wheel"
				count: 2L
			}]
			rewards: [{
				id: "r32_alloy"
				type: "item"
				item: "create:andesite_alloy"
				count: 32
			}]
		}
	]
}
"""

ch4_snbt = """{
	id: "chapter_4_industrial"
	group: ""
	order_index: 3
	filename: "chapter_4_industrial"
	title: "4. ⚡ Промышленность"
	icon: "enderio:energy_conduit"
	default_quest_shape: ""
	default_hide_dependency_lines: false
	quests: [
		{
			x: -1.0d
			y: 0.0d
			id: "q41_conduit"
			title: "Кондуиты Ender IO"
			icon: "enderio:energy_conduit"
			subtitle: "Проведите компактную сеть передачи энергии."
			tasks: [{
				id: "t41_conduit"
				type: "item"
				item: "enderio:energy_conduit"
				count: 8L
			}]
			rewards: [{
				id: "r41_binder"
				type: "item"
				item: "enderio:conduit_binder"
				count: 32
			}]
		}
		{
			x: 1.0d
			y: 0.0d
			id: "q42_industrial"
			title: "Авто-Ферма Industrial Foregoing"
			icon: "industrialforegoing:fluid_extractor"
			subtitle: "Автоматизируйте фарм ресурсов и сбор латекса."
			dependencies: ["q41_conduit"]
			tasks: [{
				id: "t42_extractor"
				type: "item"
				item: "industrialforegoing:fluid_extractor"
			}]
			rewards: [{
				id: "r42_plastic"
				type: "item"
				item: "industrialforegoing:plastic"
				count: 16
			}]
		}
	]
}
"""

ch5_snbt = """{
	id: "chapter_5_airships"
	group: ""
	order_index: 4
	filename: "chapter_5_airships"
	title: "5. 🛸 Корабли и МЭ-Сеть"
	icon: "eureka:ship_helm"
	default_quest_shape: ""
	default_hide_dependency_lines: false
	quests: [
		{
			x: -1.0d
			y: 0.0d
			id: "q51_ae2"
			title: "МЭ-Сеть Applied Energistics 2"
			icon: "appliedenergistics2:controller"
			subtitle: "Создайте цифровой центр хранения предметов."
			tasks: [{
				id: "t51_controller"
				type: "item"
				item: "appliedenergistics2:controller"
			}]
			rewards: [{
				id: "r51_cell"
				type: "item"
				item: "appliedenergistics2:cell_component_64k"
			}]
		}
		{
			x: 1.0d
			y: 0.0d
			id: "q52_ship"
			title: "Владыки Океана (Valkyrien Skies)"
			icon: "eureka:ship_helm"
			subtitle: "Сберите штурвал и отправьтесь в плавание на плавучем или летающем корабле!"
			dependencies: ["q51_ae2"]
			tasks: [{
				id: "t52_helm"
				type: "item"
				item: "eureka:ship_helm"
			}]
			rewards: [{
				id: "r52_engine"
				type: "item"
				item: "eureka:engine"
			}]
		}
	]
}
"""

for root in ftb_dirs:
    ch_dir = os.path.join(root, "chapters")
    os.makedirs(ch_dir, exist_ok=True)
    with open(os.path.join(root, "data.snbt"), "w", encoding="utf-8") as f:
        f.write(data_snbt)
    with open(os.path.join(ch_dir, "chapter_1_ocean.snbt"), "w", encoding="utf-8") as f:
        f.write(ch1_snbt)
    with open(os.path.join(ch_dir, "chapter_2_animals.snbt"), "w", encoding="utf-8") as f:
        f.write(ch2_snbt)
    with open(os.path.join(ch_dir, "chapter_3_create.snbt"), "w", encoding="utf-8") as f:
        f.write(ch3_snbt)
    with open(os.path.join(ch_dir, "chapter_4_industrial.snbt"), "w", encoding="utf-8") as f:
        f.write(ch4_snbt)
    with open(os.path.join(ch_dir, "chapter_5_airships.snbt"), "w", encoding="utf-8") as f:
        f.write(ch5_snbt)

print("[SUCCESS] Complete FTB Quests Tree with itemfilters tags generated successfully!")
