// AquaTech: моды сами выдают книги-гайды при входе (StarCatcher, Industrial Upgrade, Alex's Mobs).
// Гайды живут в F4, поэтому книги отбираем: у IU/SC выдача одноразовая, хватает одной чистки.
// Alex's Mobs дополнительно выключен в config/alexsmobs.toml (giveBookOnStartup = false).

const JOIN_GUIDES = [
  'starcatcher:starcatcher_guide',
  'industrialupgrade:guide_book',
  'industrialupgrade:guide_tablet',
  'industrialupgrade:book',
  'alexsmobs:animal_dictionary'
]
const GUIDES_STRIP_FLAG = 'aquatech_stripped_join_guides'

function stripJoinGuides(player) {
  if (!player || player.level == null) return
  let removed = 0
  for (const id of JOIN_GUIDES) {
    try {
      const cnt = player.inventory.count(id)
      if (cnt > 0) {
        player.inventory.clear(id)
        removed += cnt
      }
    } catch (e) {
      // предмета нет в реестре — пропускаем
    }
  }
  if (removed > 0) {
    console.log('[AquaTech] Removed join guide books x' + removed + ' from ' + player.username)
  }
}

PlayerEvents.loggedIn(function (event) {
  var player = event.player
  if (!player) return
  var data = player.persistentData
  if (data.getBoolean(GUIDES_STRIP_FLAG)) return

  // Моды выдают книги на логине, иногда с задержкой: чистим дважды, флаг ставим после второй чистки.
  event.server.scheduleInTicks(60, function () {
    stripJoinGuides(player)
  })
  event.server.scheduleInTicks(200, function () {
    stripJoinGuides(player)
    data.putBoolean(GUIDES_STRIP_FLAG, true)
  })
})
