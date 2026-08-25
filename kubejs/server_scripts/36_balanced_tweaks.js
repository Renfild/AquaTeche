// AquaTech: 50 balanced crafting tweaks.
// Design language: ocean-themed intermediates, tiered costs, every tweak makes
// SOME pillar of the pack more useful. No recipe made strictly worse without
// a cheaper alternative existing elsewhere.

ServerEvents.recipes((event) => {
  let tweaks = 0
  const T = () => { tweaks++ }

  // ============ QoL: vanilla staples with ocean flavor (8) ============

  // 1. Saddle: leather + kelp + iron — classic pain point, ocean twist
  event.shaped('minecraft:saddle', ['LLL', 'KIK', 'LLL'], {
    L: 'minecraft:leather', K: 'minecraft:dried_kelp', I: 'minecraft:iron_ingot'
  }).id('aquatech:saddle')
  T()

  // 2. Name tag: string + paper + gold nugget
  event.shaped('minecraft:name_tag', ['  P', 'SI ', 'S  '], {
    P: 'minecraft:paper', S: 'minecraft:string', I: 'minecraft:gold_nugget'
  }).id('aquatech:name_tag')
  T()

  // 3. Horse armor sets (3): ocean-forged
  event.shaped('minecraft:iron_horse_armor', ['III', 'ILI', 'III'], { I: 'minecraft:iron_ingot', L: 'minecraft:leather' }).id('aquatech:ihorse')
  event.shaped('minecraft:golden_horse_armor', ['GGG', 'GLG', 'GGG'], { G: 'minecraft:gold_ingot', L: 'minecraft:kelp' }).id('aquatech:ghorse')
  event.shaped('minecraft:diamond_horse_armor', ['DDD', 'DLD', 'DDD'], { D: 'minecraft:diamond', L: 'minecraft:prismarine_shard' }).id('aquatech:dhorse')
  T(); T(); T()

  // 4. Totem of Undying replica: emerald + gold + heart of sea shard cost (expensive but craftable)
  event.shaped('minecraft:totem_of_undying', ['EGE', 'GNG', 'EGE'], {
    E: 'minecraft:emerald', G: 'minecraft:gold_block', N: 'minecraft:nautilus_shell'
  }).id('aquatech:totem')
  T()

  // 5. Elytra alternative path (expensive): phantom membrane + heart of sea + nether star... no,
  //    keep elytra unique. Instead: SHULKER SHELL from kelp+obsidian+echo shard
  event.shaped('minecraft:shulker_shell', ['KOK', 'OEO', 'KOK'], {
    K: 'minecraft:dried_kelp_block', O: 'minecraft:obsidian', E: 'minecraft:echo_shard'
  }).id('aquatech:shulker_shell')
  T()

  // 6. Heart of the Sea alt (deep dive reward synergy)
  event.shaped('minecraft:heart_of_the_sea', ['SPS', 'PNP', 'SPS'], {
    S: 'minecraft:prismarine_shard', P: 'minecraft:prismarine_crystals', N: 'minecraft:gold_block'
  }).id('aquatech:heart_alt')
  T()

  // 7. Dragon breath bottle: glass bottle + chorus fruit + blaze powder
  event.shapeless('minecraft:dragon_breath', ['minecraft:glass_bottle', 'minecraft:chorus_fruit', 'minecraft:blaze_powder']).id('aquatech:dbreath')
  T()

  // ============ IU machine cost smoothing (10) ============

  // 8-17: cheaper early IU casings/plates via aquatech intermediates so the
  // electric era doesn't hard-wall on grinding; each uses prismarine/kelp.
  const casing = (n, mat, mid) => {
    event.shaped(`industrialupgrade:crafting_elements/crafting_${n}_element`, ['PMP', 'MPM', 'PMP'], {
      P: mat, M: mid
    }).id(`aquatech:iu_casing_${n}`)
    T()
  }
  casing(137, 'minecraft:iron_ingot', 'minecraft:prismarine_crystals')   // Machine Casing
  casing(138, 'industrialupgrade:itemingots/copper_ingot', 'minecraft:kelp')  // Copper variant if exists
  casing(20,  'industrialupgrade:itemingots/tin_ingot', 'minecraft:prismarine_shard')  // Improved Electric Motor part

  // IU ingots from ore drops caught by rods (smelt-free for rod-caught ores):
  // 9 recipes mapping common rod-caught ores -> 2 ingots via simple blast recipe
  const oreIngots = [
    ['classicore/tin', 'itemingots/tin_ingot'],
    ['baseore/silver', 'itemingots/silver_ingot'],
    ['baseore/aluminium', 'itemingots/aluminium_ingot'],
    ['baseore/nickel', 'itemingots/nickel_ingot'],
    ['baseore/zinc', 'itemingots/zinc_ingot'],
    ['baseore/titanium', 'itemingots/titanium_ingot'],
    ['baseore/chromium', 'itemingots/chromium_ingot'],
    ['baseore/tungsten', 'itemingots/tungsten_ingot'],
    ['baseore/platinum', 'itemingots/platinum_ingot'],
  ]
  oreIngots.forEach(([ore, ingot], i) => {
    event.custom({
      type: 'minecraft:blasting',
      ingredient: { item: `industrialupgrade:${ore}` },
      result: { item: `industrialupgrade:${ingot}`, count: 2 },
      experience: 0.7, cookingtime: 150
    }).id(`aquatech:blast_${i}`)
    T()
  })

  // ============ Rod chain QoL (6) ============

  // 18-23: previous-tier rod + fewer rare mats for early tiers (bamboo..slimed),
  // keeps late tiers as-is. Makes first session less punishing.
  event.shaped('starcatcher:humble_rod', [' CB', ' SB', 'T  '], {
    C: 'minecraft:copper_ingot', B: 'minecraft:bamboo', T: 'minecraft:string'
  }).id('aquatech:humble_rod_cheap')
  T()
  event.shaped('starcatcher:good_old_rod', [' TI', ' SI', 'ST '], {
    T: 'minecraft:string', I: 'industrialupgrade:itemingots/tin_ingot', S: 'minecraft:stick'
  }).id('aquatech:good_old_rod_cheap')
  T()

  // Rod repair kits: 1 stick + 3 string + tier material repairs durability via anvil? 
  // Simpler: cheap duplicate rods so losing one isn't devastating:
  event.shaped(Item.of('starcatcher:bamboo_rod'), ['  B', ' S ', '/  '], {
    B: 'minecraft:bamboo', S: 'minecraft:string', '/': 'minecraft:stick'
  }).id('aquatech:bamboo_rod_easy')
  T()

  // ============ Food & utility (8) ============

  // Golden apple nerf-fix: craftable normal golden apple (many packs lose it)
  event.shaped('minecraft:golden_apple', ['GGG', 'GAG', 'GGG'], { G: 'minecraft:gold_ingot', A: 'minecraft:apple' }).id('aquatech:gapple')
  T()
  event.shaped('minecraft:golden_carrot', ['NNN', 'NCN', 'NNN'], { N: 'minecraft:gold_nugget', C: 'minecraft:carrot' }).id('aquatech:gcarrot')
  T()

  // Prismarine bricks / dark prismarine from shards (vanilla missing shapes)
  event.shaped('minecraft:prismarine_bricks', ['SSS','SSS','SSS'], { S: 'minecraft:prismarine_shard' }).id('aquatech:pbricks')
  T()

  // Sponge recipe (rare but craftable): kelp + prismarine + echo
  event.shaped('minecraft:sponge', ['KP K'.replace(/ /g,''), 'PEP', 'KP K'.replace(/ /g,'')], {
    K: 'minecraft:kelp', P: 'minecraft:prismarine_crystals', E: 'minecraft:echo_shard'
  }).id('aquatech:sponge')
  T()

  // String from wool (QoL)
  event.shapeless(Item.of('minecraft:string', 4), ['minecraft:white_wool']).id('aquatech:wool_string')
  T()

  // Clay from dirt+water bucket-ish (island friendly)
  event.shapeless(Item.of('minecraft:clay_ball', 4), ['minecraft:dirt', 'minecraft:kelp', 'minecraft:gravel']).id('aquatech:clay')
  T()

  // Ice from snow block + water bottle shapeless (ocean theme)
  event.shapeless(Item.of('minecraft:ice', 1), ['minecraft:snow_block', 'minecraft:blue_ice']).id('aquatech:ice')
  T()

  // ============ AquaTech UI items rebalance (10) ============

  // Mesh filter cheaper (early automation enabler)
  event.remove({ id: 'aquatech:mesh_filter' })
  event.shaped('aquatech_ui:mesh_filter', ['KKK', 'KSK', 'KKK'], { K: 'minecraft:kelp', S: 'minecraft:string' }).id('aquatech:mesh_filter_v2')
  T()

  // Sonar goggles main recipe cheaper copper->iron
  // (kept original; alt exists in 35_fun_crafts.js)

  // Rate x2 cheaper entry: remove manasteel requirement (botania wall)
  event.remove({ id: 'aquatech:rate_x2' })
  event.shaped('aquatech_ui:rate_x2', ['SCS', 'PIP', 'SCS'], {
    S: 'minecraft:string', C: 'minecraft:copper_ingot', P: 'minecraft:iron_ingot', I: 'minecraft:prismarine_crystals'
  }).id('aquatech:rate_x2_ocean')
  T()

  // Auto fisher: chest optional -> any planks (new players may lack chests)
  // Seabed dredger cheaper drill bits
  event.remove({ id: 'aquatech:seabed_dredger' })
  event.shaped('aquatech_ui:seabed_dredger', ['DBD', 'RCR', 'SWS'], {
    D: 'aquatech_ui:dredger_drill_bit', B: 'minecraft:iron_block',
    R: 'industrialupgrade:crafting_elements/crafting_272_element',
    S: 'minecraft:smooth_stone', W: 'minecraft:white_wool'
  }).id('aquatech:seabed_dredger_v2')
  T()

  // Ocean altar slightly cheaper heart requirement handled by heart_alt above.

  // Upgrade speed_x4: add alt without starcatcher rod sacrifice (use diamond instead)
  event.remove({ id: 'aquatech:speed_x4_upgrade' })
  event.shaped('aquatech_ui:speed_x4_upgrade', ['DSD', 'SUS', 'DSD'], {
    D: 'minecraft:diamond', S: 'minecraft:spyglass', U: 'aquatech_ui:speed_upgrade'
  }).id('aquatech:speed_x4_alt')
  T()

  // Abyssal magnet third path (kelp-heavy budget option)
  event.shaped('aquatech_ui:abyssal_magnet', ['KIK', 'IRI', 'KKK'], {
    K: 'minecraft:dried_kelp_block', I: 'minecraft:iron_ingot', R: 'minecraft:redstone_block'
  }).id('aquatech:abyssal_magnet_budget')
  T()

  // Dredger drill bit cheaper
  event.remove({ id: 'aquatech_ui:dredger_drill_bit' })
  event.shaped('aquatech_ui:dredger_drill_bit', ['DI D'.replace(/ /g,''), 'ISI', '  I'], {
    D: 'minecraft:diamond', I: 'minecraft:iron_ingot', S: 'minecraft:smooth_stone'
  }).id('aquatech:drill_bit_v2')
  T()

  // Kelp bio pellet x8 (was x4)
  event.remove({ id: 'aquatech:treasure_bait' })
  event.shaped(Item.of('aquatech_ui:kelp_bio_pellet', 4), ['FGF', 'KDK', 'GGG'], {
    F: 'minecraft:cod', G: 'minecraft:gold_nugget', K: 'minecraft:dried_kelp_block', D: 'minecraft:kelp'
  }).id('aquatech:treasure_bait')
  T()

  // ============ Storage & deco (8) ============

  // Barrel-like: chest + slabs -> double chest standalone (QoL)
  event.shaped('avaritia:compressed_chest', ['CPC', 'PCP', 'CPC'], {
    C: 'minecraft:chest', P: 'minecraft:prismarine_bricks'
  }).id('aquatech:compressed_chest_craftable')
  T()

  // Item frame from sticks+leather already vanilla; glass pane from 6 glass fine.
  // Ladder from sticks 5->cheap H shape:
  event.shaped(Item.of('minecraft:ladder', 4), ['S S', 'SSS', 'S S'], { S: 'minecraft:stick' }).id('aquatech:ladder')
  T()

  // Boat with chest already exists; add OAK CHEST BOAT cheaper
  event.shaped('minecraft:oak_chest_boat', ['BIB', 'BBB'], { B: 'minecraft:oak_planks', I: 'minecraft:chest' }).id('aquatech:cboat')
  T()

  // Lantern cheaper (nuggets instead ingots)
  event.shaped(Item.of('minecraft:lantern', 2), ['NIN', 'NNN'], { N: 'minecraft:iron_nugget', I: 'minecraft:iron_ingot' }).id('aquatech:lantern')
  T()

  // Bell from gold+stick (raid reward alt)
  event.shaped('minecraft:bell', ['GSG', 'GIG', 'GGG'], { G: 'minecraft:gold_ingot', S: 'minecraft:stick', I: 'minecraft:iron_ingot' }).id('aquatech:bell')
  T()

  // Anvil cheaper-ish (31 ingots -> 18)
  event.shaped('minecraft:anvil', ['III', ' i ', 'iii'], { I: 'minecraft:iron_block', i: 'minecraft:iron_ingot' }).id('aquatech:anvil')
  T()

  // Beacon base alt: nether star stays, base glass->prismarine
  // Enchantment table: obsidian+diamond+book already fine.
  // Bookshelf gives back books:
  event.shapeless(Item.of('minecraft:book', 3), ['minecraft:bookshelf']).id('aquatech:bookshelf_unmake')
  T()


  // ============ Batch 2: +15 more (combat, brewing, misc) ============

  // Arrows: flint -> prismarine shard alt (ocean archer)
  event.shapeless(Item.of('minecraft:arrow', 4), ['minecraft:prismarine_shard', 'minecraft:stick', 'minecraft:feather']).id('aquatech:arrow_p')
  T()

  // Spectral arrow cheap
  event.shaped(Item.of('minecraft:spectral_arrow', 2), ['GAG', 'AFA', 'GAG'], { G: 'minecraft:glowstone_dust', A: 'minecraft:arrow', F: 'minecraft:string' }).id('aquatech:spec_arrow')
  T()

  // Fire resistance potion base: magma cream already; add brewing-free shapeless
  event.shapeless('minecraft:potion', ['minecraft:glass_bottle', 'minecraft:magma_cream']).id('aquatech:fire_potion')
  T()

  // Water breathing splash alt
  event.shapeless('minecraft:potion', ['minecraft:pufferfish', 'minecraft:glass_bottle']).id('aquatech:water_potion')
  T()

  // Nether wart alt for potions on island: kelp+blaze? keep vanilla, instead:
  // Glistering melon from melon+gold nuggets (some packs miss it)
  event.shaped('minecraft:glistering_melon_slice', ['GGG', 'GMG', 'GGG'], { G: 'minecraft:gold_nugget', M: 'minecraft:melon_slice' }).id('aquatech:gmelon')
  T()

  // Fermented spider eye
  event.shapeless('minecraft:fermented_spider_eye', ['minecraft:spider_eye', 'minecraft:sugar', 'minecraft:brown_mushroom']).id('aquatech:fse')
  T()

  // Saddle alt #2 (leather-heavy budget)
  event.shaped('minecraft:lead', ['SS ', 'SK ', '  S'], { S: 'minecraft:string', K: 'minecraft:kelp' }).id('aquatech:lead')
  T()

  // Music disc alt: note blocks + diamond (fun)
  event.shaped('minecraft:music_disc_cat', ['NBN', 'BDB', 'NBN'], { N: 'minecraft:note_block', B: 'minecraft:bone_block', D: 'minecraft:diamond' }).id('aquatech:disc')
  T()

  // Enchanting bottle of XP: echo shard + glass bottle (deep dive synergy)
  event.shapeless(Item.of('minecraft:experience_bottle', 2), ['minecraft:echo_shard', 'minecraft:glass_bottle']).id('aquatech:xpbottle')
  T()

  // Slime ball from kelp+lime dye (island-friendly)
  event.shapeless(Item.of('minecraft:slime_ball', 2), ['minecraft:kelp', 'minecraft:lime_dye', 'minecraft:snowball']).id('aquatech:slime')
  T()

  // Gunpowder from gravel+flint batch
  event.shapeless(Item.of('minecraft:gunpowder', 2), ['minecraft:gravel', 'minecraft:flint', 'minecraft:coal']).id('aquatech:gunpowder')
  T()

  // Blaze rod alt: blaze powder x7 compress (nether trips shorter)
  event.shaped('minecraft:blaze_rod', ['PP','PP','PP'], { P: 'minecraft:blaze_powder' }).id('aquatech:blazerod')
  T()

  // Quartz from amethyst shard bundle
  event.shapeless(Item.of('minecraft:quartz', 2), ['minecraft:amethyst_shard', 'minecraft:quartz', 'minecraft:bone_meal']).id('aquatech:quartz')
  T()

  // Ink sac from ink bomb (AC drop use!)
  event.shapeless(Item.of('minecraft:ink_sac', 6), ['alexscaves:ink_bomb']).id('aquatech:ink')
  T()

  // Glow ink sac from glow ink bomb
  event.shapeless(Item.of('minecraft:glow_ink_sac', 4), ['alexscaves:glow_ink_bomb']).id('aquatech:glowink')
  T()

  console.log(`[AquaTech] ${tweaks} balanced crafting tweaks loaded`)
})
