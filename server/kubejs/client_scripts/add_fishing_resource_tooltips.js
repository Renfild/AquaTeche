// AquaTech: only rod badges and rod notes live here. «Где ловится» у ресурсов рисует Java (FishingOreTooltips + RodDropTable).
ItemEvents.tooltip((event) => {
  // Tier signature on every rod, mirroring the docs/rods.html badges (T1–T13 + Костяная + Небесная).
  const rodTiers = [
    ['minecraft:fishing_rod', 'T1 · Бамбуковая / ванильная'],
    ['starcatcher:bamboo_rod', 'T1 · Бамбуковая'],
    ['starcatcher:humble_rod', 'T2 · Скромная'],
    ['starcatcher:good_old_rod', 'T3 · Старая добрая'],
    ['starcatcher:naturalist_rod', 'T4 · Натуралиста'],
    ['starcatcher:slimed_rod', 'T5 · Слизневая'],
    ['starcatcher:iceborn_rod', 'T6 · Ледяная'],
    ['starcatcher:starcatcher_rod', 'T7 · Ловец Звёзд'],
    ['starcatcher:azure_crystal_rod', 'T8 · Лазурный кристалл'],
    ['starcatcher:sharktooth_rod', 'T9 · Акулий клык'],
    ['starcatcher:obsidian_rod', 'T10 · Обсидиановая'],
    ['starcatcher:lush_glowberry_rod', 'T11 · Светящаяся ягода'],
    ['starcatcher:magmaforged_rod', 'T12 · Магматическая'],
    ['starcatcher:alpha_rod', 'T13 · Альфа'],
    ['starcatcher:boner_rod', 'Костяная · мобы'],
    ['starcatcher:sky_rod', 'Небесная · только рыба'],
  ]
  for (const [rodId, tierLabel] of rodTiers) {
    event.add(rodId, [Text.of('⚓ ' + tierLabel).aqua()])
  }

  event.add('starcatcher:boner_rod', [
    Text.of('Ловит дроп враждебных мобов обычного мира: кости, паутина, снег, гниль, порох, стрелы, жемчуг…').gray(),
    Text.of('Крафт: 3 алмаза + нить. Незера и Энда в пуле нет.').darkGray(),
  ])
  event.add('starcatcher:slimed_rod', [
    Text.of('С этой удочки уже ловится обсидиан — хватит на портал в Ад на плоту.').gray(),
  ])
})
