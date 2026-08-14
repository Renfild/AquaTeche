// AquaTech: no phantoms (insomnia off + cancel spawn). Depends: vanilla.
ServerEvents.loaded((event) => {
  event.server.runCommandSilent('gamerule doInsomnia false')
  console.log('[AquaTech] doInsomnia false — phantoms disabled')
})

EntityEvents.spawned((event) => {
  if (event.entity.type === 'minecraft:phantom') {
    event.cancel()
  }
})
