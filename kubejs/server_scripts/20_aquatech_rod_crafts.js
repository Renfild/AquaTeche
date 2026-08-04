// AquaTech: Progression rod crafting chain for StarCatcher rods.
// Requires previous tier rod + Industrial Upgrade resources & components.

ServerEvents.recipes((event) => {
  console.log('[AquaTech] Loading StarCatcher rod progression crafting recipes...')

  if (!Platform.isLoaded('starcatcher')) return

  // ---------- 1. Bamboo Rod (Tier 1: Starter Resource Rod) ----------
  event.remove({ output: 'starcatcher:bamboo_rod' })
  event.shaped('starcatcher:bamboo_rod', ['  B', ' SB', 'S  '], {
    B: 'minecraft:bamboo',
    S: 'minecraft:string',
  }).id('aquatech:bamboo_rod_craft')

  // Fallback if no bamboo: craft bamboo rod using sticks + string
  event.shaped('starcatcher:bamboo_rod', ['  W', ' SW', 'S  '], {
    W: 'minecraft:stick',
    S: 'minecraft:string',
  }).id('aquatech:bamboo_rod_from_sticks_craft')

  // ---------- 2. Humble Rod (Simple Rod) ----------
  event.remove({ output: 'starcatcher:humble_rod' })
  event.shaped('starcatcher:humble_rod', ['  S', ' W ', 'W  '], {
    W: 'minecraft:stick',
    S: 'minecraft:string',
  }).id('aquatech:humble_rod_craft')

  // ---------- 3. Good Old Rod (Tier 2: Iron & Tin Plates) ----------
  event.remove({ output: 'starcatcher:good_old_rod' })
  event.shaped('starcatcher:good_old_rod', ['  P', ' BP', 'C B'], {
    P: 'industrialupgrade:itemplates/iron_plate',
    B: 'industrialupgrade:classicore/tin',
    C: 'starcatcher:bamboo_rod',
  }).id('aquatech:good_old_rod_craft')

  // ---------- 4. Naturalist Rod (Tier 4: LV Circuit & Latex) ----------
  event.remove({ output: 'starcatcher:naturalist_rod' })
  event.shaped('starcatcher:naturalist_rod', ['  C', ' SC', 'S R'], {
    C: 'industrialupgrade:crafting_elements/crafting_272_element', // Electronic circuit
    S: 'industrialupgrade:synthetic_rubber',
    R: 'starcatcher:good_old_rod',
  }).id('aquatech:naturalist_rod_craft')

  // ---------- 5. Slimed Rod (Tier 5: Slime & Spinel/Barium) ----------
  event.remove({ output: 'starcatcher:slimed_rod' })
  event.shaped('starcatcher:slimed_rod', ['  B', ' SB', 'S R'], {
    B: 'minecraft:slime_block',
    S: 'industrialupgrade:baseore/spinel',
    R: 'starcatcher:naturalist_rod',
  }).id('aquatech:slimed_rod_craft')

  // ---------- 6. Iceborn Rod (Tier 6: MV Silver & Aluminium & Azote) ----------
  event.remove({ output: 'starcatcher:iceborn_rod' })
  event.shaped('starcatcher:iceborn_rod', ['  A', ' IA', 'I R'], {
    A: 'industrialupgrade:baseore/silver',
    I: 'industrialupgrade:baseore/aluminium',
    R: 'starcatcher:slimed_rod',
  }).id('aquatech:iceborn_rod_craft')

  // ---------- 7. StarCatcher Rod (Tier 7: Gold & Tungsten & Sapphire) ----------
  event.remove({ output: 'starcatcher:starcatcher_rod' })
  event.shaped('starcatcher:starcatcher_rod', ['  S', ' GS', 'G R'], {
    S: 'industrialupgrade:preciousgem/sapphire_gem',
    G: 'industrialupgrade:baseore/tungsten',
    R: 'starcatcher:iceborn_rod',
  }).id('aquatech:starcatcher_rod_craft')

  // ---------- 8. Azure Crystal Rod (Tier 8: Advanced Circuit & Crystals) ----------
  event.remove({ output: 'starcatcher:azure_crystal_rod' })
  event.shaped('starcatcher:azure_crystal_rod', ['  C', ' AC', 'A R'], {
    C: 'industrialupgrade:preciousgem/topaz_gem',
    A: 'industrialupgrade:crafting_elements/crafting_273_element', // Advanced circuit
    R: 'starcatcher:starcatcher_rod',
  }).id('aquatech:azure_crystal_rod_craft')

  // ---------- 9. Sharktooth Rod (Tier 9: Cobalt & Manganese & Titanium) ----------
  event.remove({ output: 'starcatcher:sharktooth_rod' })
  event.shaped('starcatcher:sharktooth_rod', ['  T', ' CT', 'C R'], {
    T: 'industrialupgrade:baseore/titanium',
    C: 'industrialupgrade:baseore/cobalt',
    R: 'starcatcher:azure_crystal_rod',
  }).id('aquatech:sharktooth_rod_craft')

  // ---------- 10. Obsidian Rod (Tier 10: Stainless Steel & Diamond) ----------
  event.remove({ output: 'starcatcher:obsidian_rod' })
  event.shaped('starcatcher:obsidian_rod', ['  D', ' SD', 'S R'], {
    D: 'minecraft:diamond_block',
    S: 'industrialupgrade:alloyingot/stainless_steel',
    R: 'starcatcher:sharktooth_rod',
  }).id('aquatech:obsidian_rod_craft')

  // ---------- 11. Lush Glowberry Rod (Tier 11: Platinum & Heart of Sea) ----------
  event.remove({ output: 'starcatcher:lush_glowberry_rod' })
  event.shaped('starcatcher:lush_glowberry_rod', ['  H', ' PH', 'P R'], {
    H: 'minecraft:heart_of_the_sea',
    P: 'industrialupgrade:baseore/platinum',
    R: 'starcatcher:obsidian_rod',
  }).id('aquatech:lush_glowberry_rod_craft')

  // ---------- 12. Magmaforged Rod (Tier 12: Uranium & Inconel) ----------
  event.remove({ output: 'starcatcher:magmaforged_rod' })
  event.shaped('starcatcher:magmaforged_rod', ['  U', ' IU', 'I R'], {
    U: 'industrialupgrade:crushed/uranium',
    I: 'industrialupgrade:alloyingot/inconel',
    R: 'starcatcher:lush_glowberry_rod',
  }).id('aquatech:magmaforged_rod_craft')

  // ---------- 13. Alpha Rod (Tier 13: Quantum Circuit & Osmiridium / Endgame) ----------
  event.remove({ output: 'starcatcher:alpha_rod' })
  event.shaped('starcatcher:alpha_rod', ['  N', ' QN', 'Q R'], {
    N: 'minecraft:nether_star',
    Q: 'industrialupgrade:alloyingot/osmiridium',
    R: 'starcatcher:magmaforged_rod',
  }).id('aquatech:alpha_rod_craft')

  console.log('[AquaTech] StarCatcher rod progression crafting recipes loaded.')
})
