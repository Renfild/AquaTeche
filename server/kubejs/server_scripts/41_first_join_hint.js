// AquaTech: one-time F4 hub hint on first join.
PlayerEvents.loggedIn((event) => {
  const player = event.player
  if (!player || player.persistentData.aquatech_f4_hint) return
  player.persistentData.aquatech_f4_hint = 1
  player.tell(Text.of('§b[AquaTech] §fМеню сервера — клавиша §eF4§f. Магазин, кейсы, аукцион.'))
})
