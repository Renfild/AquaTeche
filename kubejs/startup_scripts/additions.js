StartupEvents.registry('item', event => {
  event.create('invar_boiler')
  event.create('heat_resistant_clay')
  event.create('blast_brick')
  event.create('coin')
  event.create('shop')
})
StartupEvents.registry('block', event => {
  event.create('quartz_glass')
    .material('glass')
    .transparent(true)
    .hardness(0.3)
    .resistance(0.3)
    .requiresTool(false)
    .soundType('glass')
    .renderType('cutout')
})
Platform.mods.kubejs.name = 'Industrial Upgrade Horizon'