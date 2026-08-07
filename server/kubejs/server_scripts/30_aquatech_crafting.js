// AquaTech: custom recipes for installed mods only (vanilla + aquatech_ui + AE2).
// Create / Thermal / Mekanism / EnderIO / IF / etc. are not in this pack — no recipes for them.

ServerEvents.recipes((event) => {
  console.log('[AquaTech] Loading AquaTech crafting recipes...')



  // ---------- Botania Floral Fertilizer & Seeds ----------
  if (Platform.isLoaded('botania')) {
    event.shapeless(Item.of('botania:fertilizer', 2), ['minecraft:bone_meal', 'minecraft:dirt']).id('aquatech:floral_fertilizer_dirt')
    event.shapeless(Item.of('botania:fertilizer', 2), ['minecraft:bone_meal', 'minecraft:kelp']).id('aquatech:floral_fertilizer_kelp')
  }

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

    // ---------- AE2 Inscriber Presses Duplication & Conversion ----------
    const presses = [
      'ae2:logic_processor_press',
      'ae2:calculation_processor_press',
      'ae2:engineering_processor_press',
      'ae2:silicon_press',
      'ae2:name_press',
    ]

    // 1. Duplication: 1x Press + 1x Iron Block -> 2x Same Press
    presses.forEach((press) => {
      let idName = press.split(':')[1]
      event.shapeless(Item.of(press, 2), [press, 'minecraft:iron_block']).id(`aquatech:duplicate_${idName}`)
    })

    // 2. Conversion: 1x Any Press -> 1x Any Other Press
    presses.forEach((targetPress) => {
      let targetId = targetPress.split(':')[1]
      presses.forEach((sourcePress) => {
        if (sourcePress !== targetPress) {
          let sourceId = sourcePress.split(':')[1]
          event.shapeless(targetPress, [sourcePress]).id(`aquatech:convert_${sourceId}_to_${targetId}`)
        }
      })
    })
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

  // ---------- Upgrades & Catch Multipliers (Rate Mods - Single Recipe per Tier) ----------
  event.remove({ output: 'aquatech_ui:rate_x2' })
  event.remove({ output: 'aquatech_ui:rate_x4' })
  event.remove({ output: 'aquatech_ui:rate_x8' })
  event.remove({ output: 'aquatech_ui:rate_x16' })
  event.remove({ output: 'aquatech_ui:rate_x32' })
  event.remove({ output: 'aquatech_ui:rate_x64' })

  // x2 Rate Mod (Botania Mana String + Mana Steel + IU Copper/Iron Plates)
  event.shaped('aquatech_ui:rate_x2', ['SCS', 'PMP', 'SCS'], {
    S: 'botania:mana_string',
    C: 'industrialupgrade:itemplates/copper_plate',
    P: 'industrialupgrade:itemplates/iron_plate',
    M: 'botania:manasteel_ingot',
  }).id('aquatech:rate_x2')

  // x4 Rate Mod (Botania Mana Pearl/Diamond + IU Silver + Synthetic Rubber)
  event.shaped('aquatech_ui:rate_x4', ['PSP', 'RMR', 'PSP'], {
    P: 'botania:mana_pearl',
    S: 'industrialupgrade:baseore/silver',
    R: 'aquatech_ui:rate_x2',
    M: 'botania:mana_diamond',
  }).id('aquatech:rate_x4')

  // x8 Rate Mod (Botania Elementium/Pixie Dust + IU Titanium + Electronic Circuit)
  event.shaped('aquatech_ui:rate_x8', ['EDE', 'RCR', 'EDE'], {
    E: 'botania:elementium_ingot',
    D: 'botania:pixie_dust',
    R: 'aquatech_ui:rate_x4',
    C: 'industrialupgrade:crafting_elements/crafting_272_element', // Electronic circuit
  }).id('aquatech:rate_x8')

  // x16 Rate Mod (Botania Terrasteel/Dragonstone + IU Stainless Steel + Advanced Circuit)
  event.shaped('aquatech_ui:rate_x16', ['TDT', 'RCR', 'TDT'], {
    T: 'botania:terrasteel_ingot',
    D: 'botania:dragonstone',
    R: 'aquatech_ui:rate_x8',
    C: 'industrialupgrade:crafting_elements/crafting_273_element', // Advanced circuit
  }).id('aquatech:rate_x16')

  // Endgame Rate x32 (Avaritia 9x9 Extreme Crafting Table)
  if (Platform.isLoaded('avaritia')) {
    event.custom({
      type: 'avaritia:shaped_table',
      tier: 4,
      pattern: [
        ' CCCCCCC ',
        'CGGGGGGGC',
        'CGTTRTTGC',
        'CGTTQTTGC',
        'CGRQQQRGC',
        'CGTTQTTGC',
        'CGTTRTTGC',
        'CGGGGGGGC',
        ' CCCCCCC ',
      ],
      key: {
        'C': { item: 'avaritia:crystal_matrix_ingot' },
        'G': { item: 'botania:gaia_ingot' },
        'T': { item: 'botania:terrasteel_ingot' },
        'R': { item: 'aquatech_ui:rate_x16' },
        'Q': { item: 'industrialupgrade:crafting_elements/crafting_274_element' },
      },
      result: { item: 'aquatech_ui:rate_x32', count: 1 },
    }).id('aquatech:rate_x32_extreme')

    // True Endgame Rate x64 (Avaritia 9x9 Extreme Crafting Table)
    event.custom({
      type: 'avaritia:shaped_table',
      tier: 4,
      pattern: [
        ' NNNNNNN ',
        'NSSSSSSSN',
        'NSXXRXXSN',
        'NSXXQXXSN',
        'NSRQQQRSN',
        'NSXXQXXSN',
        'NSXXRXXSN',
        'NSSSSSSSN',
        ' NNNNNNN ',
      ],
      key: {
        'N': { item: 'avaritia:neutronium_ingot' },
        'S': { item: 'avaritia:singularity' },
        'X': { item: 'industrialupgrade:alloyingot/osmiridium' },
        'R': { item: 'aquatech_ui:rate_x32' },
        'Q': { item: 'industrialupgrade:asteroidore/asteroid_adamantium_ore' },
      },
      result: { item: 'aquatech_ui:rate_x64', count: 1 },
    }).id('aquatech:rate_x64_extreme')
  }

  // ---------- Extended Crafting (9x9 Ultimate Table / 7x7 Elite Table Support) ----------
  if (Platform.isLoaded('extendedcrafting')) {
    event.custom({
      type: 'extendedcrafting:shaped_table',
      pattern: [
        ' CCCCCCC ',
        'CGGGGGGGC',
        'CGTTRTTGC',
        'CGTTQTTGC',
        'CGRQQQRGC',
        'CGTTQTTGC',
        'CGTTRTTGC',
        'CGGGGGGGC',
        ' CCCCCCC ',
      ],
      key: {
        'C': { item: 'avaritia:crystal_matrix_ingot' },
        'G': { item: 'botania:gaia_ingot' },
        'T': { item: 'botania:terrasteel_ingot' },
        'R': { item: 'aquatech_ui:rate_x16' },
        'Q': { item: 'industrialupgrade:crafting_elements/crafting_274_element' },
      },
      result: { item: 'aquatech_ui:rate_x32' },
    }).id('aquatech:rate_x32_extendedcrafting')

    event.custom({
      type: 'extendedcrafting:shaped_table',
      pattern: [
        ' NNNNNNN ',
        'NSSSSSSSN',
        'NSXXRXXSN',
        'NSXXQXXSN',
        'NSRQQQRSN',
        'NSXXQXXSN',
        'NSXXRXXSN',
        'NSSSSSSSN',
        ' NNNNNNN ',
      ],
      key: {
        'N': { item: 'avaritia:neutronium_ingot' },
        'S': { item: 'avaritia:singularity' },
        'X': { item: 'industrialupgrade:alloyingot/osmiridium' },
        'R': { item: 'aquatech_ui:rate_x32' },
        'Q': { item: 'industrialupgrade:asteroidore/asteroid_adamantium_ore' },
      },
      result: { item: 'aquatech_ui:rate_x64' },
    }).id('aquatech:rate_x64_extendedcrafting')
  }

  // Recipe for Extreme Crafting Table 9x9 (Heavy Workbench)
  if (Platform.isLoaded('avaritia')) {
    event.remove({ output: 'avaritia:extreme_crafting_table' })
    event.shaped('avaritia:extreme_crafting_table', ['CCC', 'CWC', 'CCC'], {
      C: 'avaritia:crystal_matrix_ingot',
      W: 'avaritia:double_compressed_crafting_table',
    }).id('aquatech:extreme_crafting_table')
  }

  event.shaped('aquatech_ui:speed_upgrade', ['IRI', 'GRG', 'RGR'], {
    I: 'minecraft:iron_ingot',
    R: 'minecraft:redstone',
    G: 'minecraft:gold_ingot',
  }).id('aquatech:speed_upgrade')

  event.shaped('aquatech_ui:speed_x4_upgrade', ['DRD', 'BCB', 'DBD'], {
    D: 'minecraft:diamond',
    R: 'starcatcher:starcatcher_rod',
    B: 'minecraft:iron_block',
    C: 'minecraft:chest',
  }).id('aquatech:speed_x4_upgrade')

  event.shaped('aquatech_ui:mesh_filter', ['SNS', 'NSN', 'SNS'], {
    S: 'minecraft:string',
    N: 'minecraft:iron_nugget',
  }).id('aquatech:mesh_filter')

  event.shaped('aquatech_ui:sonar_goggles', ['PLP', 'HGH', 'XRX'], {
    P: 'minecraft:prismarine_crystals',
    L: 'minecraft:leather_helmet',
    H: 'minecraft:heart_of_the_sea',
    G: 'minecraft:glass_pane',
    X: 'industrialupgrade:crafting_elements/crafting_273_element',
    R: 'minecraft:redstone_block',
  }).id('aquatech:sonar_goggles')

  event.shaped('aquatech_ui:abyssal_magnet', ['NDN', 'MEM', ' S '], {
    N: 'minecraft:netherite_ingot',
    D: 'minecraft:diamond_block',
    M: 'minecraft:echo_shard',
    E: 'minecraft:ender_eye',
    S: 'minecraft:nether_star',
  }).id('aquatech:abyssal_magnet')

  event.shaped('aquatech_ui:auto_fisher', ['IFI', 'RCR', 'SGS'], {
    I: 'minecraft:iron_block',
    F: 'starcatcher:humble_rod',
    R: 'minecraft:redstone_block',
    C: 'minecraft:chest',
    S: 'minecraft:smooth_stone',
    G: 'industrialupgrade:crafting_elements/crafting_272_element',
  }).id('aquatech:auto_fisher')

  event.shaped('aquatech_ui:seabed_dredger', ['DBD', 'RCR', 'SSS'], {
    D: 'aquatech_ui:dredger_drill_bit',
    B: 'minecraft:iron_block',
    R: 'industrialupgrade:crafting_elements/crafting_273_element',
    S: 'minecraft:smooth_stone',
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

  // =========================================================================
  // ENDGAME TROPHY: Admin Solar Panel (Avaritia 9x9 Extreme Crafting Table)
  // =========================================================================
  if (Platform.isLoaded('industrialupgrade') && Platform.isLoaded('avaritia')) {
    event.remove({ output: 'industrialupgrade:machines/admin_solar_panel' })
    event.custom({
      type: 'avaritia:shaped_table',
      tier: 4,
      pattern: [
        ' ICCCCCC ',
        'ISSSSSSSI',
        'CSXRXRXSC',
        'CSXQXQXSC',
        'CRQPAPQRC',
        'CSXQXQXSC',
        'CSXRXRXSC',
        'ISSSSSSSI',
        ' CCCCCCC ',
      ],
      key: {
        'I': { item: 'avaritia:infinity_ingot' },
        'C': { item: 'avaritia:crystal_matrix_ingot' },
        'S': { item: 'avaritia:singularity' },
        'X': { item: 'industrialupgrade:alloyingot/osmiridium' },
        'R': { item: 'aquatech_ui:rate_x64' },
        'Q': { item: 'industrialupgrade:crafting_elements/crafting_274_element' },
        'P': { item: 'starcatcher:alpha_rod' },
        'A': { item: 'industrialupgrade:machines/photonic_solar_panel' },
      },
      result: { item: 'industrialupgrade:machines/admin_solar_panel', count: 1 },
    }).id('aquatech:admin_solar_panel_endgame')
  }

  console.log('[AquaTech] Crafting recipes loaded.')
})
