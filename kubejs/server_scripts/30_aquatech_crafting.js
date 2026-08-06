// AquaTech: custom recipes for installed mods only (vanilla + aquatech_ui + AE2).
// Create / Thermal / Mekanism / EnderIO / IF / etc. are not in this pack — no recipes for them.

ServerEvents.recipes((event) => {
  console.log('[AquaTech] Loading AquaTech crafting recipes...')

  // ---------- AE2 (installed) ----------
  if (Platform.isLoaded('ae2')) {
    event.remove({ output: 'ae2:controller' })
    event.shaped('ae2:controller', ['SFS', 'FEF', 'SFS'], {
      S: 'ae2:smooth_sky_stone_block',
      F: 'ae2:fluix_crystal',
      E: 'ae2:engineering_processor',
    }).id('aquatech:me_controller')

    event.remove({ output: 'ae2:drive' })
    event.shaped('ae2:drive', ['IEI', 'CEC', 'IEI'], {
      I: 'minecraft:iron_ingot',
      E: 'ae2:engineering_processor',
      C: 'ae2:fluix_glass_cable',
    }).id('aquatech:me_drive')
  }

  // ---------- Kickstarter boat ----------
  event.shaped(
    Item.of('minecraft:oak_chest_boat', {
      display: {
        Name: '{"text":"Лодка Кикстартера","color":"aqua","italic":false,"bold":true}',
        Lore: [
          '{"text":"Твой первый корабль после Катастрофы","color":"gray","italic":false}',
          '{"text":"Лови дерево, камень и обломки удочкой","color":"dark_aqua","italic":false}',
        ],
      },
    }),
    ['PCP', 'PFP', 'KSK'],
    {
      P: 'minecraft:oak_planks',
      C: 'minecraft:chest',
      F: 'starcatcher:humble_rod',
      K: 'minecraft:kelp',
      S: 'minecraft:string',
    }
  ).id('aquatech:kickstarter_boat')

  // ---------- StarCatcher resource rods (AquaTech IU loot) ----------
  // Fish-only rods stay vanilla SC loot; these five are craftable progression.
  if (Platform.isLoaded('starcatcher')) {
    event.shaped('starcatcher:naturalist_rod', [' IS', ' SR', 'S C'], {
      I: 'minecraft:iron_ingot',
      S: 'minecraft:stick',
      R: 'minecraft:string',
      C: 'minecraft:copper_ingot',
    }).id('aquatech:naturalist_rod')

    event.shaped('starcatcher:starcatcher_rod', [' GS', ' SR', 'S N'], {
      G: 'minecraft:gold_ingot',
      S: 'minecraft:stick',
      R: 'starcatcher:naturalist_rod',
      N: 'minecraft:gold_nugget',
    }).id('aquatech:starcatcher_rod')

    event.shaped('starcatcher:obsidian_rod', [' DO', ' SR', 'S P'], {
      D: 'minecraft:diamond',
      O: 'minecraft:obsidian',
      S: 'minecraft:stick',
      R: 'starcatcher:starcatcher_rod',
      P: 'minecraft:prismarine_shard',
    }).id('aquatech:obsidian_rod')

    event.shaped('starcatcher:lush_glowberry_rod', [' BG', ' SR', 'S C'], {
      B: 'minecraft:glow_berries',
      G: 'minecraft:sea_lantern',
      S: 'minecraft:stick',
      R: 'starcatcher:obsidian_rod',
      C: 'minecraft:prismarine_crystals',
    }).id('aquatech:lush_glowberry_rod')

    event.shaped('starcatcher:magmaforged_rod', [' MN', ' SR', 'B M'], {
      M: 'minecraft:magma_block',
      N: 'minecraft:netherite_scrap',
      S: 'minecraft:blaze_rod',
      R: 'starcatcher:lush_glowberry_rod',
      B: 'minecraft:nether_brick',
    }).id('aquatech:magmaforged_rod')
  }

  // ---------- Upgrades & Catch Multipliers (Rate Mods) ----------
  event.shaped('aquatech_ui:rate_x2', [' C ', 'RMR', ' C '], {
    C: 'minecraft:copper_ingot',
    R: 'minecraft:redstone',
    M: 'industrialupgrade:itemplates/iron_plate',
  }).id('aquatech:rate_x2')

  event.shaped('aquatech_ui:rate_x4', [' S ', 'RMR', ' S '], {
    S: 'industrialupgrade:baseore/silver',
    R: 'aquatech_ui:rate_x2',
    M: 'industrialupgrade:synthetic_rubber',
  }).id('aquatech:rate_x4')

  event.shaped('aquatech_ui:rate_x8', [' T ', 'RMR', ' T '], {
    T: 'industrialupgrade:baseore/titanium',
    R: 'aquatech_ui:rate_x4',
    M: 'industrialupgrade:crafting_elements/crafting_272_element', // Electronic circuit
  }).id('aquatech:rate_x8')

  event.shaped('aquatech_ui:rate_x16', [' S ', 'RMR', ' S '], {
    S: 'industrialupgrade:alloyingot/stainless_steel',
    R: 'aquatech_ui:rate_x8',
    M: 'industrialupgrade:crafting_elements/crafting_273_element', // Advanced circuit
  }).id('aquatech:rate_x16')

  // Endgame Rate x32
  event.shaped('aquatech_ui:rate_x32', ['INI', 'RQR', 'PNP'], {
    I: 'industrialupgrade:alloyingot/inconel',
    N: 'minecraft:nether_star',
    R: 'aquatech_ui:rate_x16',
    Q: 'industrialupgrade:crafting_elements/crafting_274_element', // Quantum processor
    P: 'industrialupgrade:baseore/platinum',
  }).id('aquatech:rate_x32')

  // True Endgame Rate x64
  event.shaped('aquatech_ui:rate_x64', ['NON', 'RQR', 'BOB'], {
    N: 'minecraft:nether_star',
    O: 'industrialupgrade:alloyingot/osmiridium',
    R: 'aquatech_ui:rate_x32',
    Q: 'industrialupgrade:asteroidore/asteroid_adamantium_ore',
    B: 'minecraft:netherite_block',
  }).id('aquatech:rate_x64')

  event.shaped('aquatech_ui:speed_upgrade', ['IRI', 'GRG', 'RGR'], {
    I: 'minecraft:iron_ingot',
    R: 'minecraft:redstone',
    G: 'minecraft:gold_ingot',
  }).id('aquatech:speed_upgrade')

  event.shaped('aquatech_ui:efficiency_upgrade', ['CRC', 'RIR', 'CRC'], {
    C: 'minecraft:copper_ingot',
    R: 'minecraft:redstone',
    I: 'minecraft:iron_ingot',
  }).id('aquatech:efficiency_upgrade')

  event.shaped('aquatech_ui:double_hook_upgrade', ['DRD', 'BCB', 'DBD'], {
    D: 'minecraft:diamond',
    R: 'starcatcher:starcatcher_rod',
    B: 'minecraft:iron_block',
    C: 'minecraft:chest',
  }).id('aquatech:double_hook_upgrade')

  event.shaped('aquatech_ui:mesh_filter', ['SNS', 'NSN', 'SNS'], {
    S: 'minecraft:string',
    N: 'minecraft:iron_nugget',
  }).id('aquatech:mesh_filter')

  event.shaped('aquatech_ui:ocean_filter', ['BMB', 'IRI', 'SCS'], {
    B: 'minecraft:iron_block',
    M: 'aquatech_ui:mesh_filter',
    I: 'minecraft:iron_ingot',
    R: 'minecraft:redstone_block',
    S: 'minecraft:smooth_stone',
    C: 'minecraft:chest',
  }).id('aquatech:ocean_filter')

  event.shaped('aquatech_ui:dredger_drill_bit', ['DID', 'OIO', 'NDN'], {
    D: 'minecraft:diamond',
    I: 'minecraft:iron_block',
    O: 'minecraft:obsidian',
    N: 'minecraft:netherite_scrap',
  }).id('aquatech:dredger_drill_bit')

  event.shaped('aquatech_ui:sonar_goggles', ['PLP', 'HGH', 'XRX'], {
    P: 'minecraft:prismarine_crystals',
    L: 'minecraft:leather_helmet',
    H: 'minecraft:heart_of_the_sea',
    G: 'minecraft:glass',
    X: 'minecraft:gold_ingot',
    R: 'minecraft:redstone',
  }).id('aquatech:sonar_goggles')

  event.shaped('aquatech_ui:abyssal_magnet', ['IEC', 'RHR', 'ICI'], {
    I: 'minecraft:iron_block',
    E: 'minecraft:echo_shard',
    C: 'minecraft:copper_block',
    R: 'minecraft:redstone_block',
    H: 'minecraft:heart_of_the_sea',
  }).id('aquatech:abyssal_magnet')

  event.shaped('aquatech_ui:seabed_dredger', ['ODO', 'IDI', 'NCN'], {
    O: 'minecraft:obsidian',
    D: 'aquatech_ui:dredger_drill_bit',
    I: 'minecraft:iron_block',
    N: 'minecraft:netherite_ingot',
    C: 'minecraft:chest',
  }).id('aquatech:seabed_dredger')

  event.remove({ id: 'aquatech_ui:kelp_bio_pellet' })
  event.shaped(Item.of('aquatech_ui:kelp_bio_pellet', 4), ['KKK', 'KRK', 'KKK'], {
    K: 'minecraft:dried_kelp_block',
    R: 'minecraft:redstone_block',
  }).id('aquatech:kelp_bio_pellet')

  event.shaped('aquatech_ui:hydro_reactor', ['PHP', 'FAF', 'NRN'], {
    P: 'minecraft:prismarine_bricks',
    H: 'minecraft:heart_of_the_sea',
    F: 'minecraft:furnace',
    A: 'aquatech_ui:abyssal_fishing_rod',
    N: 'minecraft:netherite_block',
    R: 'minecraft:redstone_block',
  }).id('aquatech:hydro_reactor')

  event.shaped('aquatech_ui:ocean_altar', ['EHE', 'PCP', 'ONO'], {
    E: 'minecraft:echo_shard',
    H: 'minecraft:heart_of_the_sea',
    P: 'minecraft:prismarine_shard',
    C: 'minecraft:crying_obsidian',
    O: 'minecraft:obsidian',
    N: 'minecraft:nether_star',
  }).id('aquatech:ocean_altar')

  event.shapeless('aquatech_ui:ocean_guide_book', ['minecraft:book', 'minecraft:kelp']).id('aquatech:ocean_guide_book')

  console.log('[AquaTech] AquaTech crafting recipes loaded.')
})
