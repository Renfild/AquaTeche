// Жемчужина Пламени: ПКМ — Ад ↔ точка входа. Кулдаун 10 мин. Порталы в Ада отключены.
const CD_KEY = 'infernal_pearl_cd'
const X_KEY = 'infernal_pearl_entry_x'
const Y_KEY = 'infernal_pearl_entry_y'
const Z_KEY = 'infernal_pearl_entry_z'
const CD_MS = 10 * 60 * 1000

// запрет формирования порталов в Ад (рамка не активируется огнивом)
ForgeEvents.onEvent('net.minecraftforge.event.level.BlockEvent.PortalSpawnEvent', (event) => {
  event.setCanceled(true)
})

ItemEvents.rightClicked('kubejs:infernal_pearl', (event) => {
  const { player, item, level } = event
  if (level.isClientSide()) return

  const data = player.persistentData
  const now = Date.now()
  const last = data.getLong(CD_KEY)
  if (now - last < CD_MS) {
    const leftMin = Math.ceil((CD_MS - (now - last)) / 60000)
    player.tell('§7[Жемчужина Пламени] Перезарядка: ещё ~' + leftMin + ' мин.')
    return
  }

  const nick = player.gameProfile.name
  const inNether = level.dimension.id() == 'minecraft:the_nether'

  if (!inNether) {
    data.putDouble(X_KEY, player.x)
    data.putDouble(Y_KEY, player.y)
    data.putDouble(Z_KEY, player.z)
    player.server.runCommandSilent('execute in minecraft:the_nether positioned 0 70 0 run fill -2 68 -2 2 68 2 minecraft:obsidian')
    player.server.runCommandSilent('execute in minecraft:the_nether positioned 0 70 0 run fill -2 69 -2 2 71 2 minecraft:air')
    player.server.runCommandSilent(`execute as ${nick} in minecraft:the_nether run tp @s 0.5 69.1 0.5`)
    player.tell('§6[Жемчужина Пламени] §fТы в Аду. Обратно — та же жемчужина.')
  } else {
    if (data.contains(X_KEY)) {
      const x = data.getDouble(X_KEY), y = data.getDouble(Y_KEY), z = data.getDouble(Z_KEY)
      player.server.runCommandSilent(`execute as ${nick} in minecraft:overworld run tp @s ${x} ${y} ${z}`)
      player.tell('§6[Жемчужина Пламени] §fВозвращение завершено.')
    } else {
      player.tell('§c[Жемчужина Пламени] Точка входа не найдена.')
      return
    }
  }

  data.putLong(CD_KEY, Date.now())
  player.addItemCooldown(item, 20 * 60 * 10)
})
