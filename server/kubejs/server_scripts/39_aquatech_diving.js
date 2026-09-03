// AquaTech: Deep Diving — block water-breathing potion use
// (Vanilla PotionBrewing is not a recipe table; CraftTweaker hooked it directly.)

const BLOCKED_POTIONS = [
  'minecraft:water_breathing',
  'minecraft:long_water_breathing',
]

function isBlockedPotion(stack) {
  if (!stack || stack.isEmpty()) return false
  const id = String(stack.id)
  if (!id.includes('potion')) return false
  const potion = stack.nbt && stack.nbt.Potion
  return potion && BLOCKED_POTIONS.includes(String(potion))
}

ItemEvents.rightClicked((event) => {
  if (isBlockedPotion(event.item)) {
    event.cancel()
    if (event.player) {
      event.player.statusMessage = Text.of('§3AquaTech§r: дыхание под водой только через дайвинг-снаряжение')
    }
  }
})

PlayerEvents.tick((event) => {
  const player = event.player
  if (!player || player.level.isClientSide()) return
  if (player.age % 20 !== 0) return
  if (player.hasEffect('minecraft:water_breathing')) {
    player.removeEffect('minecraft:water_breathing')
  }
})

console.info('[AquaTech] Deep diving tweaks loaded (water breathing blocked).')
