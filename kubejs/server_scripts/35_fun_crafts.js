// AquaTech: Fun-first crafting rebalance.
//
// Goal: give players MORE reasons to engage with each pillar of the pack:
//   Alex's Caves mobs, deep-ocean diving, StarCatcher rods, IU machines.

ServerEvents.recipes((event) => {
// ---------- 1. Rate x32: accessible mid-game version ----------
event.remove({ id: 'aquatech:rate_x32_workbench' })
event.shaped(Item.of('aquatech_ui:rate_x32', 1), ['GDG', 'DCD', 'GDG'], {
  G: 'industrialupgrade:baseore/platinum',           // caught by obsidian/lush rods
  D: 'alexscaves:gazing_pearl',                      // Abyssal Chasm mob drop
  C: 'aquatech_ui:rate_x16',
}).id('aquatech:rate_x32_workbench')

// ---------- 2. Treasure Bait: kelp+fish+gold -> bio pellet bundle ----------
event.remove({ id: 'aquatech:kelp_dummy_remove' })
event.remove({ id: 'aquatech:treasure_bait' })
event.shaped(Item.of('aquatech_ui:kelp_bio_pellet', 4), ['FGF', 'KDK', 'GGG'], {
  F: 'minecraft:cod',
  G: 'minecraft:gold_nugget',
  K: 'minecraft:dried_kelp_block',
  D: 'minecraft:kelp',
}).id('aquatech:treasure_bait')

// ---------- 3. Sonar Goggles alt-craft (AC gazing pearl path) ----------
event.remove({ id: 'aquatech:sonar_goggles_alt' })
event.shaped('aquatech_ui:sonar_goggles', ['PSP', 'SGS', ' R '], {
  P: 'alexscaves:pearl',
  S: 'minecraft:spyglass',
  G: 'alexscaves:occult_gem',
  R: 'minecraft:redstone_block',
}).id('aquatech:sonar_goggles_alt')

// ---------- 4. Abyssal Magnet alt-craft (AC heart_of_iron path) ----------
event.remove({ id: 'aquatech:abyssal_magnet_alt' })
event.shaped('aquatech_ui:abyssal_magnet', ['IHI', 'RDR', 'SSS'], {
  I: 'industrialupgrade:itemingots/aluminium_ingot',
  H: 'alexscaves:heart_of_iron',
  R: 'minecraft:redstone_block',
  D: 'minecraft:diamond',
  S: 'minecraft:smooth_stone',
}).id('aquatech:abyssal_magnet_alt')

console.log('[AquaTech] Fun crafting rebalance loaded: x32 workbench path, AC alt-crafts')
})
