// AquaTech: IUCore.loginPlayer gives free industrialupgrade:sensor/sensor ("Сканер руд")
// on first join. Ocean pack progresses via fishing — strip that freebie once per player.

const IU_ORE_SCANNER = 'industrialupgrade:sensor/sensor'
const STRIP_FLAG = 'aquatech_stripped_iu_ore_scanner'

PlayerEvents.loggedIn(function (event) {
  var player = event.player
  if (!player) return
  var data = player.persistentData
  if (data.getBoolean(STRIP_FLAG)) return

  // IU gives the scanner synchronously on login; wait a few ticks then remove.
  event.server.scheduleInTicks(60, function () {
    try {
      if (player.level == null) return
      var cnt = player.inventory.count(IU_ORE_SCANNER)
      if (cnt > 0) {
        player.inventory.clear(IU_ORE_SCANNER)
        console.log('[AquaTech] Removed IU free ore scanner x' + cnt + ' from ' + player.username)
      }
      data.putBoolean(STRIP_FLAG, true)
    } catch (e) {
      console.error('[AquaTech] strip IU ore scanner failed: ' + e)
    }
  })
})
