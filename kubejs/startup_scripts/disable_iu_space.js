// AquaTech — TEMPORARY: block travel into Industrial Upgrade space dimensions.
// Worlds themselves are stripped via: python tools/toggle_iu_space_dims.py disable|enable
// Must live in startup_scripts (ForgeEvents.onEvent is not reloadable).
// Flip to false (or delete) + full restart to re-enable.

const IU_SPACE_DISABLED = true

if (IU_SPACE_DISABLED) {
  ForgeEvents.onEvent('net.minecraftforge.event.entity.EntityTravelToDimensionEvent', (event) => {
    try {
      const loc = event.dimension.location()
      if (loc && String(loc.namespace) === 'industrialupgrade') {
        event.setCanceled(true)
        const entity = event.entity
        if (entity && entity.isPlayer && entity.isPlayer()) {
          entity.tell(Text.red('[AquaTech] Космос Industrial Upgrade временно отключён.'))
        }
      }
    } catch (e) {
      // ignore
    }
  })
  console.info('[AquaTech] IU Space dimension travel blocked (temporary).')
}
