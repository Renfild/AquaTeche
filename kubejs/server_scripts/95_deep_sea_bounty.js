// AquaTech: Deep Sea Bounty — награда за спуск на самое дно океана.
// Ниже Y=-30 (глубоководный слой) игрока ждёт:
//  - шанс поймать/найти крутые руды прямо из блоков глубины (event.block break)
//  - усиленный лут удочки (FishingLootHandler уже даёт бонус по качеству)
// Плюс периодические "жилы сокровищ": рандомные блоки глубинной руды вокруг игрока.

const DEEP_Y = -30            // ниже этого уровня — глубоководная зона
const ORE_CHANCE = 0.06       // шанс бонусной руды при добыче блока на глубине
const TREASURE_INTERVAL = 6000 // тиков (5 мин) между "жилами сокровищ"

const DEEP_ORES = [
  'minecraft:diamond_ore',
  'minecraft:ancient_debris',
  'industrialupgrade:baseore/titanium',
  'industrialupgrade:baseore/tungsten',
  'industrialupgrade:baseore/platinum',
  'industrialupgrade:baseore/iridium',
  'industrialupgrade:preciousgem/sapphire_gem',
  'industrialupgrade:preciousgem/topaz_gem',
]

const DEEP_ORE_FALLBACK = {
  'minecraft:diamond_ore': 'minecraft:diamond',
  'minecraft:ancient_debris': 'minecraft:netherite_scrap',
  'industrialupgrade:baseore/titanium': 'minecraft:iron_ingot',
  'industrialupgrade:baseore/tungsten': 'minecraft:iron_ingot',
  'industrialupgrade:baseore/platinum': 'minecraft:gold_ingot',
  'industrialupgrade:baseore/iridium': 'minecraft:diamond',
  'industrialupgrade:preciousgem/sapphire_gem': 'minecraft:lapis_lazuli',
  'industrialupgrade:preciousgem/topaz_gem': 'minecraft:amethyst_shard',
}

function isDeepSea(level, pos) {
  // Только в океанах и ниже порога глубины
  const biome = level.getBiome(pos).id()
  return biome.contains('ocean') || biome.contains('deep') && pos.y < DEEP_Y
}

// Бонусная руда при добыче любого блока на глубине
BlockEvents.broken((event) => {
  const { level, block, player } = event
  if (level.isClientSide()) return
  if (!player) return
  if (block.y > DEEP_Y) return
  if (!isDeepSea(level, block.pos)) return

  if (Math.random() < ORE_CHANCE) {
    const oreId = DEEP_ORES[Math.floor(Math.random() * DEEP_ORES.length)]
    let oreItem = Item.of(oreId)
    // Если блока-руды нет в игре — выдаём предмет-фоллбэк
    const drop = oreItem.isEmpty() ? Item.of(DEEP_ORE_FALLBACK[oreId]) : oreItem
    if (!drop.isEmpty()) {
      block.popItem(drop)
      player.tell('§b[AquaTech] §fГлубина хранит сокровища... §e+ ' + drop.displayName.string)
    }
  }
})

// Жила сокровищ: раз в 5 минут игроку на глубине спавнится кластер глубинной руды рядом
ServerEvents.tick((event) => {
  const { server } = event
  if (server.tickCount % TREASURE_INTERVAL !== 0) return
  server.players.forEach((player) => {
    if (player.level.isClientSide()) return
    if (player.y > DEEP_Y) return
    if (!isDeepSea(player.level, player.block.pos)) return

    const level = player.level
    let spawned = 0
    for (let tries = 0; tries < 24 && spawned < 3; tries++) {
      const x = player.x + (Math.random() * 32 - 16) | 0
      const z = player.z + (Math.random() * 32 - 16) | 0
      const y = Math.max(-60, Math.min(DEEP_Y - 5, player.y - 5 + (Math.random() * 20 | 0)))
      const target = level.getBlock(x, y, z)
      if (target.id() !== 'minecraft:water' && target.id() !== 'minecraft:cave_air') continue
      const below = level.getBlock(x, y - 1, z)
      if (below.id() === 'minecraft:air' || below.id() === 'minecraft:water') continue
      // Заменяем блок под водой на руду
      const oreId = DEEP_ORES[Math.floor(Math.random() * DEEP_ORES.length)]
      const ore = Block.getBlock(oreId)
      if (ore && !ore.defaultState.isEmpty()) {
        target.set(oreId, {})
        spawned++
      } else {
        // fallback: спавним предмет-сущность
        level.spawnEntity('minecraft:item', x + 0.5, y + 0.5, z + 0.5, (it) => {
          it.item = Item.of(DEEP_ORE_FALLBACK[oreId])
        })
        spawned++
      }
    }
    if (spawned > 0) {
      player.setStatusMessage('§b[AquaTech] §fТы чувствуешь... на дне что-то блеснуло. §7(жила сокровищ рядом)')
    }
  })
})

console.log('[AquaTech] Deep Sea Bounty loaded: deep ocean rewards below Y=' + DEEP_Y)
