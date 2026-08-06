// AquaTech — disable all quarry crafts (Simply Quarries + Industrial Upgrade)
// KubeJS 1.20.1 / Forge

ServerEvents.recipes((event) => {
  console.log('[AquaTech] Disabling quarry crafts...')

  event.remove({ mod: 'simplyquarries' })
  event.remove({ id: /quarry/i })
  event.remove({ output: /quarry/i })

  const iuQuarries = [
    'industrialupgrade:basemachine/quantum_quarry',
    'industrialupgrade:basemachine/adv_quantum_quarry',
    'industrialupgrade:basemachine/imp_quantum_quarry',
    'industrialupgrade:basemachine/per_quantum_quarry',
    'industrialupgrade:basemachine3/steam_quarry',
    'industrialupgrade:basemachine3/adv_steam_quarry',
    'industrialupgrade:basemachine3/alkalineearthquarry',
    'industrialupgrade:basemachine3/wireless_mineral_quarry',
    'industrialupgrade:basemachine3/quarry_pipe',
    'industrialupgrade:petrol_quarry/petrol_quarry',
    'industrialupgrade:petrol_quarry_item',
    'industrialupgrade:quarry_vein/quarry_vein',
    'industrialupgrade:quarry_vein_item',
    'industrialupgrade:quarrymodule',
    'industrialupgrade:earth_quarry/earth_analyzer',
    'industrialupgrade:earth_quarry/earth_casing',
    'industrialupgrade:earth_quarry/earth_chest',
    'industrialupgrade:earth_quarry/earth_controller',
    'industrialupgrade:earth_quarry/earth_rig',
    'industrialupgrade:earth_quarry/earth_transport',
    'industrialupgrade:quarrymodules/blackmodule',
    'industrialupgrade:quarrymodules/whitemodule',
    'industrialupgrade:quarrymodules/comb_macerator',
    'industrialupgrade:quarrymodules/macerator',
    'industrialupgrade:quarrymodules/polisher',
    'industrialupgrade:quarrymodules/per',
    'industrialupgrade:quarrymodules/ef',
    'industrialupgrade:quarrymodules/ef1',
    'industrialupgrade:quarrymodules/ef2',
    'industrialupgrade:quarrymodules/ef3',
    'industrialupgrade:quarrymodules/ef4',
    'industrialupgrade:quarrymodules/for1',
    'industrialupgrade:quarrymodules/for2',
    'industrialupgrade:quarrymodules/for3',
    'industrialupgrade:quarrymodules/kar1',
    'industrialupgrade:quarrymodules/kar2',
    'industrialupgrade:quarrymodules/kar3',
  ]

  for (const id of iuQuarries) {
    event.remove({ output: id })
  }

  console.log('[AquaTech] Quarry crafts disabled.')
})
