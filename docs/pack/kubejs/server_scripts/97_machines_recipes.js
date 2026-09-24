// AquaTech: Механизмы — рецепты (aquatech_machines)
// Рыболов MK-2: алюминий + ОДНА удочка StarCatcher + схема + reinforced stone
// (F ровно один раз: удочка ещё нужна в слоте машины)
ServerEvents.recipes((event) => {
  event.shaped('aquatech_machines:fisher', ['IFI', 'RCR', 'ISI'], {
    I: 'industrialupgrade:itemingots/aluminium_ingot',
    F: 'starcatcher:obsidian_rod',
    R: 'industrialupgrade:crafting_elements/crafting_272_element',
    C: 'minecraft:chest',
    S: 'industrialupgrade:blockresource/reinforced_stone',
  }).id('aquatech_machines:fisher')

  // Экскаватор: буры + электросхемы + reinforced stone
  event.shaped('aquatech_machines:excavator', ['DBD', 'RCR', 'SSS'], {
    D: 'minecraft:diamond',
    B: 'minecraft:iron_block',
    R: 'industrialupgrade:crafting_elements/crafting_273_element',
    C: 'minecraft:chest',
    S: 'industrialupgrade:blockresource/reinforced_stone',
  }).id('aquatech_machines:excavator')

  // Экстрактор: призмарин + электронная схема + chest + iron
  // Цветолов: призмариновые блочки + мана-сталь + мана-жемчуг + лепесток + схема + укреплённый камень
  event.shaped('aquatech_machines:flower_collector', ['PLP', 'RCR', 'SBS'], {
    P: 'minecraft:prismarine_shard',
    L: 'botania:manasteel_ingot',
    R: 'botania:mana_pearl',
    C: 'industrialupgrade:crafting_elements/crafting_272_element',
    S: 'industrialupgrade:blockresource/reinforced_stone',
    B: 'botania:pink_petal',
  }).id('aquatech_machines:flower_collector')

  // Мана-Фабрикатор: FE → мана. Без него цикл Botania не запускается (ману негде брать).
  event.shaped('aquatech_machines:mana_fabricator', ['PCP', 'ASA', 'PCP'], {
    P: 'industrialupgrade:itemingots/aluminium_ingot',
    C: 'industrialupgrade:crafting_elements/crafting_273_element',
    A: 'botania:apothecary_default',
    S: 'industrialupgrade:blockresource/reinforced_stone',
  }).id('aquatech_machines:mana_fabricator')


  // Гайдбук сервера
  event.shaped(Item.of('patchouli:guide_book', '{patchouli:book:"aquatech_ui:guide"}'), [' P ', 'PBP', ' P '], {
    P: 'minecraft:prismarine_shard',
    B: 'minecraft:book',
  }).id('aquatech_machines:guide_book')

  event.shaped('aquatech_machines:extractor', ['PXP', 'ICI', 'PIP'], {
    P: 'minecraft:prismarine_crystals',
    X: 'industrialupgrade:crafting_elements/crafting_273_element',
    I: 'industrialupgrade:itemingots/aluminium_ingot',
    C: 'minecraft:chest',
  }).id('aquatech_machines:extractor')

  // Гидротермальный Синтезатор: обсидиан + плавильня + лава + продвинутая схема + reinforced stone
  event.shaped('aquatech_machines:synthesizer', ['OBO', 'XCX', 'SSS'], {
    O: 'minecraft:obsidian',
    B: 'minecraft:blast_furnace',
    X: 'industrialupgrade:crafting_elements/crafting_274_element',
    C: 'minecraft:lava_bucket',
    S: 'industrialupgrade:blockresource/reinforced_stone',
  }).id('aquatech_machines:synthesizer')

  // Батиметрическая Центрифуга: призмарин + воронка + ведро + схема + алюминий
  event.shaped('aquatech_machines:centrifuge', ['PHP', 'XCX', 'SWS'], {
    P: 'minecraft:prismarine',
    H: 'minecraft:hopper',
    X: 'industrialupgrade:crafting_elements/crafting_273_element',
    C: 'minecraft:bucket',
    S: 'industrialupgrade:itemingots/aluminium_ingot',
    W: 'industrialupgrade:blockresource/reinforced_stone',
  }).id('aquatech_machines:centrifuge')

  // Улучшение скорости (x1)
  event.shaped('aquatech_machines:speed_upgrade_1', ['RGR', 'GUG', 'RGR'], {
    R: 'minecraft:redstone',
    G: 'minecraft:gold_ingot',
    U: 'industrialupgrade:crafting_elements/crafting_272_element',
  }).id('aquatech_machines:speed_upgrade_1')

  // Улучшение скорости (x4) - требует Абиссальный Сплав и Вулканический Кристалл
  event.shaped('aquatech_machines:speed_upgrade_4', ['ASA', 'SVS', 'ASA'], {
    A: 'aquatech_machines:abyssal_alloy',
    S: 'aquatech_machines:speed_upgrade_1',
    V: 'aquatech_machines:volcanic_crystal',
  }).id('aquatech_machines:speed_upgrade_4')

  // Альтернативный высокотехнологичный крафт Синтезатора с Абиссальным Сплавом
  event.shaped('aquatech_machines:synthesizer', ['ABA', 'XCX', 'SSS'], {
    A: 'aquatech_machines:abyssal_alloy',
    B: 'minecraft:blast_furnace',
    X: 'industrialupgrade:crafting_elements/crafting_274_element',
    C: 'minecraft:lava_bucket',
    S: 'industrialupgrade:blockresource/reinforced_stone',
  }).id('aquatech_machines:synthesizer_alloy')

  // Крафты с Морской Солью (sea_salt):
  // 1. Прессованный минеральный кальцит из соли
  event.shaped('minecraft:calcite', ['SS', 'SS'], {
    S: 'aquatech_machines:sea_salt',
  }).id('aquatech_machines:salt_to_calcite')

  // 2. Химический синтез пороха из морской соли и угля
  event.shapeless('4x minecraft:gunpowder', [
    'aquatech_machines:sea_salt',
    'aquatech_machines:sea_salt',
    'minecraft:charcoal',
    'minecraft:redstone',
  ]).id('aquatech_machines:salt_gunpowder')

  // 3. Засолка и консервация рыбы (морская соль + сырая рыба -> питательная соленая рыба)
  event.shapeless('2x minecraft:cooked_cod', [
    'aquatech_machines:sea_salt',
    'minecraft:cod',
  ]).id('aquatech_machines:salt_cured_cod')

  event.shapeless('2x minecraft:cooked_salmon', [
    'aquatech_machines:sea_salt',
    'minecraft:salmon',
  ]).id('aquatech_machines:salt_cured_salmon')

  // Крафты с Вулканическим Кристаллом (volcanic_crystal):
  // Мощный термальный бустер энергоэффективности
  event.shaped('aquatech_machines:energy_efficiency', ['LGL', 'GVE', 'LGL'], {
    L: 'minecraft:lapis_lazuli',
    G: 'minecraft:gold_ingot',
    V: 'aquatech_machines:volcanic_crystal',
    E: 'industrialupgrade:crafting_elements/crafting_272_element',
  }).id('aquatech_machines:energy_efficiency_crystal')
})
