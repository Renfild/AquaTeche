// AquaTech: IUCore.loginPlayer gives free industrialupgrade:sensor/sensor ("Сканер руд")
// on first join. Ocean pack progresses via fishing — strip that freebie once per player.

const IU_ORE_SCANNER = 'industrialupgrade:sensor/sensor'
const FLAG = 'aquatech_stripped_iu_ore_scanner'

PlayerEvents.loggedIn((event) => {
  const player = event.player
  if (!player) return
  const data = player.persistentData
  if (data.getBoolean(FLAG)) return

  // IU gives the scanner synchronously on login; wait a few ticks then remove.
  event.server.scheduleInTicks(60, () => {
    try {
      if (player.level == null) return
      const scannerCount = player.inventory.count(IU_ORE_SCANNER)
      if (scannerCount > 0) {
        player.inventory.clear(IU_ORE_SCANNER)
        console.log(`[AquaTech] Removed IU free ore scanner x${scannerCount} from ${player.username}`)
      }
      data.putBoolean(FLAG, true)
    } catch (e) {
      console.error('[AquaTech] strip IU ore scanner failed: ' + e)
    }
  })
})
