// AquaTech: StarCatcher rod durability. Keep in sync with RodDurability.java.
// Use setMaxDamage() — assigning item.maxDamage often no-ops on raw Item in KubeJS 2001.6.x.
ItemEvents.modification((event) => {
  const rods = {
    'starcatcher:bamboo_rod': 128,
    'starcatcher:humble_rod': 128,
    'starcatcher:good_old_rod': 128,
    'starcatcher:sky_rod': 128,
    'starcatcher:boner_rod': 128,
    'starcatcher:naturalist_rod': 192,
    'starcatcher:starcatcher_rod': 192,
    'starcatcher:slimed_rod': 192,
    'starcatcher:iceborn_rod': 256,
    'starcatcher:obsidian_rod': 256,
    'starcatcher:sharktooth_rod': 256,
    'starcatcher:azure_crystal_rod': 256,
    'starcatcher:lush_glowberry_rod': 320,
    'starcatcher:magmaforged_rod': 320,
    'starcatcher:alpha_rod': 320,
  }

  for (const [id, max] of Object.entries(rods)) {
    event.modify(id, (item) => {
      item.setMaxDamage(max)
      // Rods are unique tools — never stack (64 rods in one slot breaks durability display)
      item.maxStackSize = 1
    })
  }

  console.log('[AquaTech] Rod durability applied + maxStackSize=1 (no stacking)')
})
