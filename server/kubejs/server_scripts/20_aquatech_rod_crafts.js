// AquaTech: Progression rod crafting chain for StarCatcher rods (Hardened Progression).
// Requires previous tier rod + Industrial Upgrade resources, plates, circuits & components.

ServerEvents.recipes((event) => {
  console.log('[AquaTech] Loading hardened StarCatcher rod progression recipes...')

  if (!Platform.isLoaded('starcatcher')) return

  // ---------- 1. Bamboo Rod (Tier 1: Starter Resource Rod) ----------
  event.remove({ output: 'starcatcher:bamboo_rod' })
  event.shaped('starcatcher:bamboo_rod', [' C B', ' SB', 'S  '], {
    B: 'minecraft:bamboo',
    S: 'minecraft:string',
    C: 'minecraft:copper_ingot',
  }).id('aquatech:bamboo_rod_craft')

  // ---------- 2. Humble Rod (Simple Rod) ----------
  event.remove({ output: 'starcatcher:humble_rod' })
  event.shaped('starcatcher:humble_rod', ['  S', ' PB', 'P  '], {
    P: 'industrialupgrade:itemplates/copper_plate',
    B: 'starcatcher:bamboo_rod',
    S: 'minecraft:string',
  }).id('aquatech:humble_rod_craft')

  // ---------- 3. Good Old Rod (Tier 3: Iron & Tin Plates + Synthetic Rubber) ----------
  event.remove({ output: 'starcatcher:good_old_rod' })
  event.shaped('starcatcher:good_old_rod', [' P2', ' R1', 'R P'], {
    P: 'industrialupgrade:itemplates/iron_plate',
    1: 'industrialupgrade:itemplates/tin_plate',
    2: 'industrialupgrade:synthetic_rubber',
    R: 'starcatcher:humble_rod',
  }).id('aquatech:good_old_rod_craft')

  // ---------- 4. Naturalist Rod (Tier 4: LV Circuits & Rubber & Spinel) ----------
  event.remove({ output: 'starcatcher:naturalist_rod' })
  event.shaped('starcatcher:naturalist_rod', [' C2', ' SR', 'S 1'], {
    C: 'industrialupgrade:crafting_elements/crafting_272_element', // Electronic circuit
    S: 'industrialupgrade:synthetic_rubber',
    1: 'industrialupgrade:baseore/spinel',
    2: 'industrialupgrade:itemplates/bronze_plate',
    R: 'starcatcher:good_old_rod',
  }).id('aquatech:naturalist_rod_craft')

  // ---------- 5. Slimed Rod (Tier 5: Slime Blocks & Barium & Strontium) ----------
  event.remove({ output: 'starcatcher:slimed_rod' })
  event.shaped('starcatcher:slimed_rod', [' B2', ' SR', 'S 1'], {
    B: 'minecraft:slime_block',
    S: 'industrialupgrade:baseore2/barium',
    1: 'industrialupgrade:baseore2/strontium',
    2: 'industrialupgrade:crafting_elements/crafting_272_element',
    R: 'starcatcher:naturalist_rod',
  }).id('aquatech:slimed_rod_craft')

  // ---------- 6. Iceborn Rod (Tier 6: Silver & Aluminium & Azote) ----------
  event.remove({ output: 'starcatcher:iceborn_rod' })
  event.shaped('starcatcher:iceborn_rod', [' A2', ' IR', 'I 1'], {
    A: 'industrialupgrade:baseore/silver',
    I: 'industrialupgrade:baseore/aluminium',
    1: 'industrialupgrade:itemcoolupgrade/azote',
    2: 'industrialupgrade:itemplates/iron_plate',
    R: 'starcatcher:slimed_rod',
  }).id('aquatech:iceborn_rod_craft')

  // ---------- 7. StarCatcher Rod (Tier 7: Gold & Tungsten & Sapphire) ----------
  event.remove({ output: 'starcatcher:starcatcher_rod' })
  event.shaped('starcatcher:starcatcher_rod', [' S2', ' GR', 'G 1'], {
    S: 'industrialupgrade:preciousgem/sapphire_gem',
    G: 'industrialupgrade:baseore/tungsten',
    1: 'industrialupgrade:baseore/chromium',
    2: 'industrialupgrade:crafting_elements/crafting_273_element', // Advanced circuit
    R: 'starcatcher:iceborn_rod',
  }).id('aquatech:starcatcher_rod_craft')

  // ---------- 8. Azure Crystal Rod (Tier 8: Advanced Circuit & Crystals) ----------
  event.remove({ output: 'starcatcher:azure_crystal_rod' })
  event.shaped('starcatcher:azure_crystal_rod', [' C2', ' AR', 'A 1'], {
    C: 'industrialupgrade:preciousgem/topaz_gem',
    A: 'industrialupgrade:crafting_elements/crafting_273_element', // Advanced circuit
    1: 'industrialupgrade:mineral/crystal',
    2: 'industrialupgrade:itemplates/gold_plate',
    R: 'starcatcher:starcatcher_rod',
  }).id('aquatech:azure_crystal_rod_craft')

  // ---------- 9. Sharktooth Rod (Tier 9: Titanium & Cobalt & Diamond) ----------
  event.remove({ output: 'starcatcher:sharktooth_rod' })
  event.shaped('starcatcher:sharktooth_rod', [' T2', ' CR', 'C 1'], {
    T: 'industrialupgrade:baseore/titanium',
    C: 'industrialupgrade:baseore/cobalt',
    1: 'minecraft:diamond',
    2: 'industrialupgrade:itemplates/titanium_plate',
    R: 'starcatcher:azure_crystal_rod',
  }).id('aquatech:sharktooth_rod_craft')

  // ---------- 10. Obsidian Rod (Tier 10: Stainless Steel & Diamond Blocks) ----------
  event.remove({ output: 'starcatcher:obsidian_rod' })
  event.shaped('starcatcher:obsidian_rod', [' D2', ' SR', 'S 1'], {
    D: 'minecraft:diamond_block',
    S: 'industrialupgrade:alloyingot/stainless_steel',
    1: 'minecraft:crying_obsidian',
    2: 'industrialupgrade:crafting_elements/crafting_274_element', // Quantum processor
    R: 'starcatcher:sharktooth_rod',
  }).id('aquatech:obsidian_rod_craft')

  // ---------- 11. Lush Glowberry Rod (Tier 11: Platinum & Heart of Sea) ----------
  event.remove({ output: 'starcatcher:lush_glowberry_rod' })
  event.shaped('starcatcher:lush_glowberry_rod', [' H2', ' PR', 'P 1'], {
    H: 'minecraft:heart_of_the_sea',
    P: 'industrialupgrade:baseore/platinum',
    1: 'minecraft:sea_lantern',
    2: 'industrialupgrade:crafting_elements/crafting_274_element',
    R: 'starcatcher:obsidian_rod',
  }).id('aquatech:lush_glowberry_rod_craft')

  // ---------- 12. Magmaforged Rod (Tier 12: Uranium & Inconel & Netherite Block) ----------
  event.remove({ output: 'starcatcher:magmaforged_rod' })
  event.shaped('starcatcher:magmaforged_rod', [' U2', ' IR', 'I 1'], {
    U: 'industrialupgrade:crushed/uranium',
    I: 'industrialupgrade:alloyingot/inconel',
    1: 'minecraft:netherite_block',
    2: 'minecraft:nether_star',
    R: 'starcatcher:lush_glowberry_rod',
  }).id('aquatech:magmaforged_rod_craft')

  // ---------- 13. Alpha Rod (Tier 13: Osmiridium & Nether Stars & Quantum Processor / Endgame) ----------
  event.remove({ output: 'starcatcher:alpha_rod' })
  event.shaped('starcatcher:alpha_rod', [' N2', ' QR', 'Q 1'], {
    N: 'minecraft:nether_star',
    Q: 'industrialupgrade:alloyingot/osmiridium',
    1: 'industrialupgrade:asteroidore/asteroid_adamantium_ore',
    2: 'industrialupgrade:crafting_elements/crafting_274_element',
    R: 'starcatcher:magmaforged_rod',
  }).id('aquatech:alpha_rod_craft')

  console.log('[AquaTech] Hardened StarCatcher rod progression recipes loaded.')
})
