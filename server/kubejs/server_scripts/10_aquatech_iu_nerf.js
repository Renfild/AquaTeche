// AquaTech — block teleport + creative only. Flight crafts stay enabled.

ServerEvents.recipes((event) => {
  console.log('[AquaTech] Loading TP / creative nerfs (flight kept)...')

  const removeOut = (id) => event.remove({ output: id })
  const removeId = (re) => event.remove({ id: re })

  // Teleport / long-range mobility
  ;[
    'industrialupgrade:basemachine3/teleporter_iu',
    'industrialupgrade:tools/frequency_transmitter',
    'draconicevolution:dislocator',
    'draconicevolution:advanced_dislocator',
    'draconicevolution:player_dislocator',
    'draconicevolution:player_dislocator_unbound',
    'draconicevolution:p2p_dislocator',
    'draconicevolution:p2p_dislocator_unbound',
    'draconicevolution:dislocator_pedestal',
    'draconicevolution:dislocator_receptacle',
    'botania:flugel_eye',
    'botania:lens_warp',
    'botania:ender_hand',
    'avaritia:endest_pearl',
    'ae2:quantum_ring',
    'ae2:quantum_link',
    'ae2:quantum_entangled_singularity',
    'ae2:spatial_io_port',
    'ae2:spatial_anchor',
    'ae2:spatial_pylon',
  ].forEach(removeOut)

  removeId(/ae2:.*spatial.*/)

  // Creative / infinity
  removeId(/industrialupgrade:creative_.*/)
  removeId(/ae2:creative_.*/)
  removeId(/avaritia:infinity_.*/)

  ;[
    'draconicevolution:creative_capacitor',
    'draconicevolution:creative_op_capacitor',
    'avaritia_armor:crystal_helmet',
    'avaritia_armor:crystal_chestplate',
    'avaritia_armor:crystal_leggings',
    'avaritia_armor:crystal_boots',
    'avaritia_armor:crystal_core',
    'botanicalextramachinery:catalyst_mana_infinity',
    'botanicalextramachinery:catalyst_living_rock_infinity',
    'botanicalextramachinery:catalyst_seed_infinity',
    'botanicalextramachinery:catalyst_stone_infinity',
    'botanicalextramachinery:catalyst_water_infinity',
    'botanicalextramachinery:catalyst_wood_infinity',
  ].forEach(removeOut)

  console.log('[AquaTech] TP / creative nerfs loaded.')
})
