// Disable Phantom Spawning completely in AquaTech
ServerEvents.loaded(event => {
  event.server.runCommandSilent('gamerule doInsomnia false')
})

EntityEvents.spawned(event => {
  if (event.entity.type === 'minecraft:phantom') {
    event.cancel()
  }
})
