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
    // Use unique name (Rhino: "const presses" redeclares and aborts the whole recipes event)
    let ae2InscriberPresses = [
      'ae2:logic_processor_press',
      'ae2:calculation_processor_press',
      'ae2:engineering_processor_press',
      'ae2:silicon_press',
      'ae2:name_press',
    ]

    // 1. Duplication: 1x Press + 1x Iron Block -> 2x Same Press
    ae2InscriberPresses.forEach((press) => {
      let idName = press.split(':')[1]
      event.shapeless(Item.of(press, 2), [press, 'minecraft:iron_block']).id(`aquatech:duplicate_${idName}`)
    })

    // 2. Conversion: 1x Any Press -> 1x Any Other Press
    ae2InscriberPresses.forEach((targetPress) => {
      let targetId = targetPress.split(':')[1]
      ae2InscriberPresses.forEach((sourcePress) => {
        if (sourcePress !== targetPress) {
          let sourceId = sourcePress.split(':')[1]
          event.shapeless(targetPress, [sourcePress]).id(`aquatech:convert_${sourceId}_to_${targetId}`)
        }
      })
    })
  }

  // ---------- Kickstarter boat ----------
  // Plain item — NBT display names break Item.of on some KubeJS/Rhino builds (empty result).
  event.shaped('minecraft:oak_chest_boat', ['PCP', 'PFP', 'KSK'], {
    P: 'minecraft:oak_planks',
    C: 'minecraft:chest',
    F: 'starcatcher:humble_rod',
    K: 'minecraft:kelp',
    S: 'minecraft:string',
  }).id('aquatech:kickstarter_boat')

  // ---------- StarCatcher rods ----------
  // Full progression lives in 20_aquatech_rod_crafts.js (remove+chain).
  // Do NOT add duplicate 3x3 crafts here — they bypass the IU progression.

  // ---------- Upgrades & Catch Multipliers (Rate Mods) ----------
  event.remove({ output: 'aquatech_ui:rate_x2' })
  event.remove({ output: 'aquatech_ui:rate_x4' })
  event.remove({ output: 'aquatech_ui:rate_x8' })
  event.remove({ output: 'aquatech_ui:rate_x16' })
  event.remove({ output: 'aquatech_ui:rate_x32' })
  event.remove({ output: 'aquatech_ui:rate_x64' })
  event.remove({ output: 'industrialupgrade:rate_x32' })
  event.remove({ output: 'industrialupgrade:rate_x64' })

  // x2–x16: normal 3×3 workbench (ingots — IU plates have no recipe in this pack)
  event.shaped('aquatech_ui:rate_x2', ['SCS', 'PMP', 'SCS'], {
    S: 'botania:mana_string',
    C: 'minecraft:copper_ingot',
    P: 'minecraft:iron_ingot',
    M: 'botania:manasteel_ingot',
  }).id('aquatech:rate_x2')

  event.shaped('aquatech_ui:rate_x4', ['PSP', 'RMR', 'PSP'], {
    P: 'botania:mana_pearl',
    S: 'industrialupgrade:baseore/silver',
    R: 'aquatech_ui:rate_x2',
    M: 'botania:mana_diamond',
  }).id('aquatech:rate_x4')

  event.shaped('aquatech_ui:rate_x8', ['EDE', 'RCR', 'EDE'], {
    E: 'botania:elementium_ingot',
    D: 'botania:pixie_dust',
    R: 'aquatech_ui:rate_x4',
    C: 'industrialupgrade:crafting_elements/crafting_272_element',
  }).id('aquatech:rate_x8')

  event.shaped('aquatech_ui:rate_x16', ['TDT', 'RCR', 'TDT'], {
    T: 'botania:terrasteel_ingot',
    D: 'botania:dragonstone',
    R: 'aquatech_ui:rate_x8',
    C: 'industrialupgrade:crafting_elements/crafting_273_element',
  }).id('aquatech:rate_x16')

  // x32 / x64 — ONLY Avaritia Extreme Crafting Table (9×9). No vanilla 3×3.
  event.remove({ id: 'aquatech:rate_x32' })
  event.remove({ id: 'aquatech:rate_x64' })
  event.remove({ id: 'aquatech:rate_x32_extendedcrafting' })
  event.remove({ id: 'aquatech:rate_x64_extendedcrafting' })
  event.remove({ id: 'aquatech:rate_x32_extreme' })
  event.remove({ id: 'aquatech:rate_x64_extreme' })

  let rateX32Pattern = [
    ' CCCCCCC ',
    'CGGGGGGGC',
    'CGTTRTTGC',
    'CGTTQTTGC',
    'CGRQQQRGC',
    'CGTTQTTGC',
    'CGTTRTTGC',
    'CGGGGGGGC',
    ' CCCCCCC ',
  ]
  let rateX32Key = {
    C: { item: 'avaritia:crystal_matrix_ingot' },
    G: { item: 'botania:gaia_ingot' },
    T: { item: 'botania:terrasteel_ingot' },
    R: { item: 'aquatech_ui:rate_x16' },
    Q: { item: 'industrialupgrade:crafting_elements/crafting_274_element' },
  }

  let rateX64Pattern = [
    ' NNNNNNN ',
    'NSSSSSSSN',
    'NSXXRXXSN',
    'NSXXQXXSN',
    'NSRQQQRSN',
    'NSXXQXXSN',
    'NSXXRXXSN',
    'NSSSSSSSN',
    ' NNNNNNN ',
  ]
  let rateX64Key = {
    N: { item: 'avaritia:neutron_ingot' },
    S: { item: 'avaritia:eternal_singularity' },
    X: { item: 'industrialupgrade:alloyingot/osmiridium' },
    R: { item: 'aquatech_ui:rate_x32' },
    Q: { item: 'industrialupgrade:asteroidore/asteroid_adamantium_ore' },
  }

  if (Platform.isLoaded('avaritia')) {
    event.custom({
      type: 'avaritia:shaped_table',
      tier: 4,
      category: 'misc',
      pattern: rateX32Pattern,
      key: rateX32Key,
      result: { item: 'aquatech_ui:rate_x32', count: 1 },
    }).id('aquatech:rate_x32_extreme')

    event.custom({
      type: 'avaritia:shaped_table',
      tier: 4,
      category: 'misc',
      pattern: rateX64Pattern,
      key: rateX64Key,
      result: { item: 'aquatech_ui:rate_x64', count: 1 },
    }).id('aquatech:rate_x64_extreme')
  } else {
    console.log('[AquaTech] avaritia missing — rate_x32/x64 uncraftable')
  }

  // Keep Re-Avaritia's own Extreme Table recipe (7×7 tier-3) — do not replace with a cheap 3×3.
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

  // One auto-fisher craft (also clears jar datapack duplicate)
  // FIX: was craftable on vanilla table from cheap iron — now requires MV-tier IU parts.
  event.remove({ output: 'aquatech_ui:auto_fisher' })
  event.remove({ id: 'aquatech_ui:auto_fisher' })
  event.remove({ id: 'aquatech:auto_fisher' })
  event.remove({ id: 'aquatech_ui:auto_fisher_jar' })
  event.shaped('aquatech_ui:auto_fisher', ['IFI', 'RCR', 'SES'], {
    I: 'industrialupgrade:itemingots/aluminium_ingot',
    F: 'starcatcher:good_old_rod',
    R: 'industrialupgrade:crafting_elements/crafting_272_element', // Electronic Circuit
    C: 'minecraft:chest',
    S: 'industrialupgrade:blockresource/reinforced_stone',
    E: 'industrialupgrade:crafting_elements/crafting_20_element', // Improved Electric Motor
  }).id('aquatech:auto_fisher')

  event.shaped('aquatech_ui:seabed_dredger', ['DBD', 'RCR', 'SSS'], {
    D: 'aquatech_ui:dredger_drill_bit',
    B: 'minecraft:iron_block',
    R: 'industrialupgrade:crafting_elements/crafting_273_element',
    S: 'minecraft:smooth_stone',
    C: 'minecraft:chest',
  }).id('aquatech:seabed_dredger')

  event.remove({ id: 'aquatech_ui:kelp_bio_pellet' })
  if (Item.exists('aquatech_ui:kelp_bio_pellet')) {
    event.shaped(Item.of('aquatech_ui:kelp_bio_pellet', 4), ['KKK', 'KRK', 'KKK'], {
      K: 'minecraft:dried_kelp_block',
      R: 'minecraft:redstone_block',
    }).id('aquatech:kelp_bio_pellet')
  }

  // hydro_reactor removed from ModBlocks — do not craft dead IDs
  event.remove({ id: 'aquatech:hydro_reactor' })
  event.remove({ id: 'aquatech_ui:hydro_reactor' })
  event.remove({ output: 'aquatech_ui:hydro_reactor' })
  event.remove({ id: 'aquatech_ui:double_hook_upgrade' })
  event.remove({ output: 'aquatech_ui:double_hook_upgrade' })

  if (Item.exists('aquatech_ui:ocean_altar')) {
    event.shaped('aquatech_ui:ocean_altar', ['EHE', 'PCP', 'ONO'], {
      E: 'minecraft:echo_shard',
      H: 'minecraft:heart_of_the_sea',
      P: 'minecraft:prismarine_shard',
      C: 'minecraft:crying_obsidian',
      O: 'minecraft:obsidian',
      N: 'minecraft:nether_star',
    }).id('aquatech:ocean_altar')
  }

  event.remove({ id: 'aquatech:ocean_guide_book' })
  event.remove({ output: 'aquatech_ui:ocean_guide_book' })

  // =========================================================================
  // ENDGAME: Administrative Solar Panel (IU admpanel) — 9×9 only
  // Note: machines/admin_solar_panel is a different IU item ("Diffractive").
  // =========================================================================
  if (Platform.isLoaded('industrialupgrade') && Platform.isLoaded('avaritia')) {
    event.remove({ output: 'industrialupgrade:admpanel/admpanel' })
    event.remove({ id: 'aquatech:admin_solar_panel_endgame' })

    let adminPattern = [
      ' ICCCCCC ',
      'ISSSSSSSI',
      'CSXRXRXSC',
      'CSXQXQXSC',
      'CRQPAPQRC',
      'CSXQXQXSC',
      'CSXRXRXSC',
      'ISSSSSSSI',
      ' CCCCCCC ',
    ]
    let adminKey = {
      I: { item: 'avaritia:infinity_ingot' },
      C: { item: 'avaritia:crystal_matrix_ingot' },
      // plain singularity needs NBT — use eternal
      S: { item: 'avaritia:eternal_singularity' },
      X: { item: 'industrialupgrade:alloyingot/osmiridium' },
      R: { item: 'aquatech_ui:rate_x64' },
      Q: { item: 'industrialupgrade:crafting_elements/crafting_274_element' },
      P: { item: 'starcatcher:alpha_rod' },
      A: { item: 'industrialupgrade:machines/photonic_solar_panel' },
    }
    let adminResult = { item: 'industrialupgrade:admpanel/admpanel', count: 1 }

    event.custom({
      type: 'avaritia:shaped_table',
      tier: 4,
      category: 'misc',
      pattern: adminPattern,
      key: adminKey,
      result: adminResult,
      show_notification: true,
    }).id('aquatech:admin_solar_panel_extreme')

    if (Platform.isLoaded('extendedcrafting')) {
      event.custom({
        type: 'extendedcrafting:shaped_table',
        pattern: adminPattern,
        key: adminKey,
        result: adminResult,
      }).id('aquatech:admin_solar_panel_extendedcrafting')
    }
  }

  // =========================================================================
  // OCEAN BOUNTY UPGRADE (aquatech_ui:upgrade_ocean_bounty / ocean_bounty_upgrade)
  // Supports Vanilla 3x3, Avaritia 7x7/9x9, and ExtendedCrafting 7x7/9x9
  // =========================================================================
  event.remove({ id: 'aquatech:upgrade_ocean_bounty_7x7' })
  event.remove({ id: 'aquatech:upgrade_ocean_bounty_avaritia' })
  event.remove({ id: 'aquatech:upgrade_ocean_bounty_extendedcrafting' })
  event.remove({ id: 'aquatech:upgrade_ocean_bounty_fallback' })
  event.remove({ id: 'aquatech:upgrade_ocean_bounty_3x3' })
  event.remove({ id: 'aquatech:upgrade_ocean_bounty_avaritia_extreme' })
  event.remove({ id: 'aquatech:upgrade_ocean_bounty_avaritia_sculk' })

  let oceanBountyResult = { item: 'aquatech_ui:upgrade_ocean_bounty', count: 1 }

  // 1. Universal Vanilla 3x3 Crafting Table Recipe (Always available)
  let cItem = 'minecraft:prismarine_crystals'
  if (Platform.isLoaded('avaritia') && Item.exists('avaritia:crystal_matrix_ingot')) {
    cItem = 'avaritia:crystal_matrix_ingot'
  } else if (Platform.isLoaded('botania') && Item.exists('botania:mana_diamond')) {
    cItem = 'botania:mana_diamond'
  }

  let tItem = Platform.isLoaded('botania') && Item.exists('botania:terrasteel_ingot')
    ? 'botania:terrasteel_ingot'
    : 'minecraft:heart_of_the_sea'

  let iItem = Platform.isLoaded('industrialupgrade') && Item.exists('industrialupgrade:alloyingot/inconel')
    ? 'industrialupgrade:alloyingot/inconel'
    : 'minecraft:nautilus_shell'

  let lItem = Platform.isLoaded('botania') && Item.exists('botania:life_essence')
    ? 'botania:life_essence'
    : 'minecraft:nether_star'

  let aItem = Platform.isLoaded('industrialupgrade') && Item.exists('industrialupgrade:alloyingot/osmiridium')
    ? 'industrialupgrade:alloyingot/osmiridium'
    : 'minecraft:prismarine_shard'

  let key3x3 = {
    C: cItem,
    T: tItem,
    F: 'aquatech_ui:auto_fisher',
    I: iItem,
    L: lItem,
    A: aItem,
  }
  event.shaped('aquatech_ui:upgrade_ocean_bounty', [
    'CTC',
    'IFI',
    'ALA'
  ], key3x3).id('aquatech:upgrade_ocean_bounty_3x3')


  // 2. 7x7 Pattern (Sculk / Elite Table)
  let pattern7x7 = [
    ' CTTTC ',
    'CLSLSLC',
    'TSIAIST',
    'TLIFAIT',
    'TSIAIST',
    'CLSLSLC',
    ' CTTTC ',
  ]
  let keyFull = {
    C: { item: 'avaritia:crystal_matrix_ingot' },
    T: { item: 'botania:terrasteel_ingot' },
    L: { item: 'botania:life_essence' },
    S: Platform.isLoaded('extrabotany')
      ? { item: 'extrabotany:orichalcos_ingot' }
      : { item: 'industrialupgrade:alloyingot/osmiridium' },
    I: { item: 'industrialupgrade:alloyingot/inconel' },
    A: { item: 'industrialupgrade:alloyingot/osmiridium' },
    F: { item: 'aquatech_ui:auto_fisher' },
  }

  // 3. 9x9 Pattern (Extreme / Ultimate Table)
  let pattern9x9 = [
    '         ',
    '  CTTTC  ',
    ' CLSLSLC ',
    ' TSIAIST ',
    ' TLIFAIT ',
    ' TSIAIST ',
    ' CLSLSLC ',
    '  CTTTC  ',
    '         '
  ]

  if (Platform.isLoaded('avaritia')) {
    // 9x9 Extreme Crafting Table (Tier 4)
    event.custom({
      type: 'avaritia:shaped_table',
      tier: 4,
      category: 'misc',
      pattern: pattern9x9,
      key: keyFull,
      result: oceanBountyResult,
      show_notification: true,
    }).id('aquatech:upgrade_ocean_bounty_avaritia_extreme')

    // 7x7 Sculk Crafting Table (Tier 3)
    event.custom({
      type: 'avaritia:shaped_table',
      tier: 3,
      category: 'misc',
      pattern: pattern7x7,
      key: keyFull,
      result: oceanBountyResult,
      show_notification: true,
    }).id('aquatech:upgrade_ocean_bounty_avaritia_sculk')
  }

  if (Platform.isLoaded('extendedcrafting')) {
    event.custom({
      type: 'extendedcrafting:shaped_table',
      tier: 3,
      pattern: pattern7x7,
      key: keyFull,
      result: oceanBountyResult,
    }).id('aquatech:upgrade_ocean_bounty_ec_7x7')

    event.custom({
      type: 'extendedcrafting:shaped_table',
      tier: 4,
      pattern: pattern9x9,
      key: keyFull,
      result: oceanBountyResult,
    }).id('aquatech:upgrade_ocean_bounty_ec_9x9')
  }

  // Shapeless alias converters
  event.shapeless('aquatech_ui:upgrade_ocean_bounty', ['aquatech_ui:ocean_bounty_upgrade']).id('aquatech:ocean_bounty_alias_1')
  event.shapeless('aquatech_ui:ocean_bounty_upgrade', ['aquatech_ui:upgrade_ocean_bounty']).id('aquatech:ocean_bounty_alias_2')

  console.log('[AquaTech] Crafting recipes loaded.')
})

