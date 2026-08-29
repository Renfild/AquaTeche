// AquaTech: resource tooltips = earliest rod that can catch the item (FishingLootHandler).
// Keep in sync with mods/aquatech-ui/.../FishingLootHandler.java rollStarCatcherRodLoot.
ItemEvents.tooltip((event) => {
  const addTier = (ids, title, styleFn) => {
    for (const id of ids) {
      event.add(id, [
        styleFn(Text.of('⚓ ' + title)),
        Text.of('Ловится удочкой AquaTech / StarCatcher').darkGray(),
      ])
    }
  }

  addTier([
    'minecraft:dirt',
    'minecraft:cobblestone',
    'minecraft:gravel',
    'minecraft:sand',
    'minecraft:clay_ball',
    'minecraft:bamboo',
    'minecraft:oak_sapling',
    'minecraft:birch_sapling',
    'industrialupgrade:sapling/rubber_sapling',
    'industrialupgrade:raw_latex',
    'industrialupgrade:blockresource/untreated_peat',
    'minecraft:copper_ore',
    'industrialupgrade:classicore/tin',
    'minecraft:string',
  ], 'Бамбуковая удочка (Т-1)', (t) => t.aqua())

  addTier([
    'minecraft:iron_ore',
    'minecraft:coal_ore',
    'industrialupgrade:baseore/titanium',
  ], 'Скромная удочка (Т-2)', (t) => t.gold())

  addTier([
    'minecraft:redstone_ore',
    'minecraft:lapis_ore',
    'industrialupgrade:baseore/spinel',
    'industrialupgrade:baseore2/strontium',
    'industrialupgrade:baseore2/yttrium',
    'industrialupgrade:baseore2/thallium',
  ], 'Старая добрая удочка (Т-3)', (t) => t.green())

  addTier([
    'industrialupgrade:baseore2/barium',
    'industrialupgrade:baseore2/polonium',
  ], 'Натуралист / слизневая (Т-4+)', (t) => t.green())

  addTier([
    'industrialupgrade:baseore/aluminium',
    'industrialupgrade:baseore/silver',
    'minecraft:obsidian',
  ], 'Слизневая удочка (Т-5)', (t) => t.aqua())

  addTier([
    'industrialupgrade:baseore/zinc',
  ], 'Ледяная удочка (Т-6)', (t) => t.white())

  addTier([
    'minecraft:bone',
    'minecraft:cobweb',
    'minecraft:snow_block',
    'minecraft:snowball',
    'minecraft:rotten_flesh',
    'minecraft:spider_eye',
    'minecraft:gunpowder',
    'minecraft:arrow',
    'minecraft:ender_pearl',
    'minecraft:phantom_membrane',
    'minecraft:totem_of_undying',
  ], 'Костяная удочка', (t) => t.gold())

  addTier([
    'minecraft:gold_ore',
    'minecraft:lapis_lazuli',
    'industrialupgrade:baseore/tungsten',
    'industrialupgrade:baseore/chromium',
    'industrialupgrade:preciousgem/sapphire_gem',
    'industrialupgrade:preciousgem/topaz_gem',
  ], 'Удочка Ловца Звёзд (Т-7)', (t) => t.lightPurple())

  addTier([
    'minecraft:amethyst_shard',
    'industrialupgrade:blockpreciousore/sapphire_ore',
    'industrialupgrade:mineral/crystal',
  ], 'Лазуритовая удочка (Т-8)', (t) => t.blue())

  addTier([
    'industrialupgrade:baseore/cobalt',
    'industrialupgrade:baseore/manganese',
    'industrialupgrade:baseore/nickel',
  ], 'Акулья удочка (Т-9)', (t) => t.red())

  addTier([
    'minecraft:diamond',
    'industrialupgrade:alloyingot/stainless_steel',
  ], 'Обсидиановая удочка (Т-10)', (t) => t.gray())

  addTier([
    'minecraft:prismarine_shard',
    'minecraft:prismarine_crystals',
    'industrialupgrade:baseore/platinum',
    'minecraft:heart_of_the_sea',
  ], 'Светящаяся удочка (Т-11)', (t) => t.green())

  addTier([
    'minecraft:quartz',
    'minecraft:netherite_scrap',
    'industrialupgrade:crushed/uranium',
    'industrialupgrade:alloyingot/inconel',
  ], 'Магмовая удочка (Т-12)', (t) => t.gold())

  addTier([
    'industrialupgrade:baseore/iridium',
    'industrialupgrade:baseore1/osmium',
    'industrialupgrade:alloyingot/osmiridium',
    'industrialupgrade:asteroidore/asteroid_adamantium_ore',
    'minecraft:nether_star',
  ], 'Альфа-удочка (Т-13)', (t) => t.lightPurple())

  event.add('starcatcher:boner_rod', [
    Text.of('⚓ Костяная удочка').gold(),
    Text.of('Ловит дроп враждебных мобов обычного мира: кости, паутина, снег, гниль, порох, стрелы, жемчуг…').gray(),
    Text.of('Крафт: 3 алмаза + нить. Незера и Энда в пуле нет.').darkGray(),
  ])
  event.add('starcatcher:slimed_rod', [
    Text.of('⚓ Слизневая (Т-5)').aqua(),
    Text.of('С этой удочки уже ловится обсидиан — хватит на портал в Ад на плоту.').gray(),
  ])
})
