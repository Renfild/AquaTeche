// AquaTech — TEMPORARY: disable Industrial Upgrade space crafts + rescue stuck players.
// Dimension world generation is stripped separately:
//   python tools/toggle_iu_space_dims.py disable|enable
// Pair with startup_scripts/disable_iu_space.js (dimension travel gate).
// Flip both flags (or delete both files) to re-enable.

const IU_SPACE_DISABLED = true

if (IU_SPACE_DISABLED) {
  ServerEvents.recipes((event) => {
    console.log('[AquaTech] Disabling IU Space crafts (temporary)...')

    ;[
      'industrialupgrade:basemachine3/hologram_space',
      'industrialupgrade:basemachine3/probe_assembler',
      'industrialupgrade:basemachine3/research_table_space',
      'industrialupgrade:basemachine3/rocket_assembler',
      'industrialupgrade:basemachine3/rocket_launch_pad',
      'industrialupgrade:basemachine3/rover_assembler',
      'industrialupgrade:basemachine3/satellite_assembler',
      'industrialupgrade:basemachine3/upgrade_rover',
      'industrialupgrade:space/planetary_translocator',
      'industrialupgrade:tome_research',
      'industrialupgrade:creative_tome_research',
      'industrialupgrade:spaceupgrademodule_schedule',
    ].forEach((id) => event.remove({ output: id }))

    event.remove({ output: /industrialupgrade:rover\/.*/ })
    event.remove({ output: /industrialupgrade:spaceupgrademodules\/.*/ })
    event.remove({ output: /industrialupgrade:colonial_building\/.*/ })
    event.remove({ output: /industrialupgrade:research_lens\/.*/ })

    event.remove({
      output: /industrialupgrade:.*(rocket|rover|probe|satellite|space|colonial|planet|launch_pad|hologram_space)/i,
    })
    event.remove({
      id: /industrialupgrade:.*(rocket|rover|probe|satellite|space|colonial|planet|launch_pad|hologram_space)/i,
    })

    console.log('[AquaTech] IU Space crafts disabled.')
  })

  PlayerEvents.loggedIn((event) => {
    try {
      let playerEntity = event.player
      if (!playerEntity || !playerEntity.level) return
      let dim = String(playerEntity.level.dimension)
      if (!dim.startsWith('industrialupgrade:')) return

      let overworld = event.server.getLevel('minecraft:overworld')
      if (!overworld) return
      let spawn = overworld.sharedSpawnPos
      playerEntity.teleportTo(overworld, spawn.x + 0.5, spawn.y, spawn.z + 0.5, playerEntity.yaw, playerEntity.pitch)
      playerEntity.tell(Text.gold('[AquaTech] Космос IU временно выключен — возврат в Overworld.'))
    } catch (e) {
      console.error('[AquaTech] IU space rescue failed: ' + e)
    }
  })
}
