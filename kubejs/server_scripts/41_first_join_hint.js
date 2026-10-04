// AquaTech: one-time F4 hub hint + cinematic welcome on first join.
PlayerEvents.loggedIn((event) => {
  const player = event.player
  if (!player || player.persistentData.aquatech_f4_hint) return
  player.persistentData.aquatech_f4_hint = 1
  // самый первый вход: включаем обучение первых 10 минут (42_aquatech_onboarding.js)
  player.persistentData.putInt('aquatech_onboard', 1)
  player.persistentData.putInt('aquatech_onboard_v', 2)
  player.tell(Text.of('§b[AquaTech] §fНапиши §e/is§f, чтобы создать свой личный остров.'))
  player.tell(Text.of('§b[AquaTech] §fМеню сервера — клавиша §eF4§f. Магазин, кейсы, аукцион.'))

  // Плот ставит PersonalRaftSpawner синхронно на этом же логине (server.execute в onLogin),
  // 20 тиков — тот же запас, что у 70_island_auto_claim.js, хватает, чтобы игрок уже стоял
  // на палубе, а не долетал телепортом.
  event.server.scheduleInTicks(20, () => {
    if (!player.level) return
    const server = event.server
    const name = player.username

    server.runCommandSilent(`title ${name} times 10 60 20`)
    server.runCommandSilent(`title ${name} subtitle {"text":"Твой путь начинается здесь","color":"aqua"}`)
    server.runCommandSilent(`title ${name} title {"text":"Добро пожаловать в AquaTech","color":"gold","bold":true}`)
    server.runCommandSilent(`playsound minecraft:ui.toast.challenge_complete master ${name} ${player.x} ${player.y} ${player.z} 1.0 1.0`)
    server.runCommandSilent(`particle minecraft:totem_of_undying ${player.x} ${player.y + 1} ${player.z} 0.6 0.8 0.6 0.02 40 force`)

    welcomeSchool(server, player)
  })
})

// Стайка на первом спавне: та же идея, что у 96_fish_stocking.js (океан живой вокруг игрока),
// но разово и погуще — новичок видит жизнь в воде с первой секунды, а не через 20 секунд
// амбиентного цикла. Пул рыбы отдельный и специально безобидный (без фугу/дельфинов).
const WELCOME_SCHOOL = [
  { type: 'minecraft:cod', count: 4 },
  { type: 'minecraft:tropical_fish', count: 3 },
  { type: 'minecraft:salmon', count: 2 }
]

function blockId(block) {
  if (!block) return ''
  try {
    let id = block.id
    if (typeof id === 'function') id = block.id()
    return String(id)
  } catch (err) {
    return ''
  }
}

function isWaterBlock(block) {
  const id = blockId(block)
  return id === 'minecraft:water' || id === 'minecraft:bubble_column' || id.endsWith(':water')
}

function findNearbyWater(level, px, py, pz, random) {
  for (let attempt = 0; attempt < 16; attempt++) {
    const dx = random.nextInt(20) - 10
    const dz = random.nextInt(20) - 10
    const x = Math.floor(px + dx)
    const z = Math.floor(pz + dz)
    for (let y = Math.floor(py) + 2; y >= Math.floor(py) - 12; y--) {
      if (!isWaterBlock(level.getBlock(x, y, z))) continue
      return { x: x + 0.5, y: y + 0.4, z: z + 0.5 }
    }
  }
  return null
}

function welcomeSchool(server, player) {
  const random = player.level.random
  const spot = findNearbyWater(player.level, player.x, player.y, player.z, random)
  if (!spot) return
  for (let g = 0; g < 2; g++) {
    const school = WELCOME_SCHOOL[random.nextInt(WELCOME_SCHOOL.length)]
    for (let i = 0; i < school.count; i++) {
      const ox = spot.x + (random.nextDouble() - 0.5) * 3.0
      const oz = spot.z + (random.nextDouble() - 0.5) * 3.0
      server.runCommandSilent(`summon ${school.type} ${ox.toFixed(1)} ${spot.y.toFixed(1)} ${oz.toFixed(1)}`)
    }
  }
}
