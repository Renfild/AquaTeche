// AquaTech: Progression rod crafting chain for StarCatcher & AquaTech rods.
// Each rod is crafted using the PREVIOUS tier rod + resources caught by that previous tier!

ServerEvents.recipes((event) => {
  console.log('[AquaTech] Loading progressive rod upgrade recipes...')

  // =========================================================================
  // 1. STARCATCHER RODS PROGRESSION
  // =========================================================================
  if (Platform.isLoaded('starcatcher')) {
    // Tier 1: Bamboo Rod (Starter Resource Rod)
    event.remove({ output: 'starcatcher:bamboo_rod' })
    event.shaped('starcatcher:bamboo_rod', [' CB', ' SB', 'S  '], {
      B: 'minecraft:bamboo',
      S: 'minecraft:string',
      C: 'minecraft:copper_ingot',
    }).id('aquatech:bamboo_rod_craft')

    // Simple Humble Rod (copper ingots, not plates)
    event.remove({ output: 'starcatcher:humble_rod' })
    event.shaped('starcatcher:humble_rod', ['  S', ' PB', 'P  '], {
      P: 'minecraft:copper_ingot',
      B: 'starcatcher:bamboo_rod',
      S: 'minecraft:string',
    }).id('aquatech:humble_rod_craft')

    // Fish-only: Bone Rod
    event.remove({ output: 'starcatcher:boner_rod' })
    event.shaped('starcatcher:boner_rod', [' BB', ' SB', 'S  '], {
      B: 'minecraft:bone',
      S: 'minecraft:string',
    }).id('aquatech:boner_rod_craft')

    // Fish-only: Sky Rod
    event.remove({ output: 'starcatcher:sky_rod' })
    event.shaped('starcatcher:sky_rod', [' PF', ' SG', 'S  '], {
      P: 'minecraft:phantom_membrane',
      F: 'minecraft:feather',
      G: 'minecraft:gold_ingot',
      S: 'minecraft:string',
    }).id('aquatech:sky_rod_craft')

    // Good Old Rod (ingots only — smelt what the previous tiers catch)
    event.remove({ output: 'starcatcher:good_old_rod' })
    event.shaped('starcatcher:good_old_rod', [' P2', ' R1', 'R P'], {
      P: 'minecraft:iron_ingot',
      1: 'industrialupgrade:itemingots/tin_ingot',
      2: 'minecraft:copper_ingot',
      R: 'starcatcher:humble_rod',
    }).id('aquatech:good_old_rod_craft')

    // Tier 2: Naturalist Rod (Iron Tier)
    event.remove({ output: 'starcatcher:naturalist_rod' })
    event.shaped('starcatcher:naturalist_rod', [' C2', ' SR', 'S 1'], {
      C: 'minecraft:redstone',
      S: 'minecraft:iron_ingot',
      1: 'industrialupgrade:baseore/spinel',
      2: 'industrialupgrade:itemingots/tin_ingot',
      R: 'starcatcher:good_old_rod',
    }).id('aquatech:naturalist_rod_craft')

    // Tier 3: Slimed Rod / StarCatcher Rod (Gold Tier)
    event.remove({ output: 'starcatcher:slimed_rod' })
    event.shaped('starcatcher:slimed_rod', [' B2', ' SR', 'S 1'], {
      B: 'minecraft:slime_block',
      S: 'industrialupgrade:baseore2/barium',
      1: 'industrialupgrade:baseore2/strontium',
      2: 'minecraft:iron_ingot',
      R: 'starcatcher:naturalist_rod',
    }).id('aquatech:slimed_rod_craft')

    // Tier 4: Iceborn Rod (Diamond Tier)
    event.remove({ output: 'starcatcher:iceborn_rod' })
    event.shaped('starcatcher:iceborn_rod', [' A2', ' IR', 'I 1'], {
      A: 'industrialupgrade:baseore/silver',
      I: 'industrialupgrade:baseore/aluminium',
      1: 'minecraft:lapis_lazuli',
      2: 'minecraft:iron_ingot',
      R: 'starcatcher:slimed_rod',
    }).id('aquatech:iceborn_rod_craft')

    // Tier 5: StarCatcher Rod
    event.remove({ output: 'starcatcher:starcatcher_rod' })
    event.shaped('starcatcher:starcatcher_rod', [' S2', ' GR', 'G 1'], {
      S: 'industrialupgrade:preciousgem/sapphire_gem',
      G: 'industrialupgrade:baseore/tungsten',
      1: 'industrialupgrade:baseore/chromium',
      2: 'minecraft:gold_ingot',
      R: 'starcatcher:iceborn_rod',
    }).id('aquatech:starcatcher_rod_craft')

    // Tier 6: Azure Crystal Rod
    event.remove({ output: 'starcatcher:azure_crystal_rod' })
    event.shaped('starcatcher:azure_crystal_rod', [' C2', ' AR', 'A 1'], {
      C: 'industrialupgrade:preciousgem/topaz_gem',
      A: 'minecraft:gold_ingot',
      1: 'industrialupgrade:mineral/crystal',
      2: 'minecraft:lapis_lazuli',
      R: 'starcatcher:starcatcher_rod',
    }).id('aquatech:azure_crystal_rod_craft')

    // Tier 7: Sharktooth Rod
    event.remove({ output: 'starcatcher:sharktooth_rod' })
    event.shaped('starcatcher:sharktooth_rod', [' T2', ' CR', 'C 1'], {
      T: 'industrialupgrade:baseore/titanium',
      C: 'industrialupgrade:baseore/cobalt',
      1: 'minecraft:diamond',
      2: 'minecraft:iron_ingot',
      R: 'starcatcher:azure_crystal_rod',
    }).id('aquatech:sharktooth_rod_craft')

    // Tier 8: Obsidian Rod
    event.remove({ output: 'starcatcher:obsidian_rod' })
    event.shaped('starcatcher:obsidian_rod', [' D2', ' SR', 'S 1'], {
      D: 'minecraft:diamond_block',
      S: 'industrialupgrade:alloyingot/stainless_steel',
      1: 'minecraft:crying_obsidian',
      2: 'minecraft:gold_ingot',
      R: 'starcatcher:sharktooth_rod',
    }).id('aquatech:obsidian_rod_craft')

    // Tier 9: Lush Glowberry Rod
    event.remove({ output: 'starcatcher:lush_glowberry_rod' })
    event.shaped('starcatcher:lush_glowberry_rod', [' H2', ' PR', 'P 1'], {
      H: 'minecraft:heart_of_the_sea',
      P: 'industrialupgrade:baseore/platinum',
      1: 'minecraft:sea_lantern',
      2: 'minecraft:gold_ingot',
      R: 'starcatcher:obsidian_rod',
    }).id('aquatech:lush_glowberry_rod_craft')

    // Tier 10: Magmaforged Rod
    event.remove({ output: 'starcatcher:magmaforged_rod' })
    event.shaped('starcatcher:magmaforged_rod', [' U2', ' IR', 'I 1'], {
      U: 'industrialupgrade:crushed/uranium',
      I: 'industrialupgrade:alloyingot/inconel',
      1: 'minecraft:netherite_ingot',
      2: 'minecraft:nether_star',
      R: 'starcatcher:lush_glowberry_rod',
    }).id('aquatech:magmaforged_rod_craft')

    // Tier 11: Alpha Rod
    event.remove({ output: 'starcatcher:alpha_rod' })
    event.shaped('starcatcher:alpha_rod', [' N2', ' QR', 'Q 1'], {
      N: 'minecraft:nether_star',
      Q: 'industrialupgrade:alloyingot/osmiridium',
      1: 'industrialupgrade:asteroidore/asteroid_adamantium_ore',
      2: 'minecraft:diamond',
      R: 'starcatcher:magmaforged_rod',
    }).id('aquatech:alpha_rod_craft')
  }

  console.log('[AquaTech] Progressive rod upgrade recipes successfully loaded.')
})
