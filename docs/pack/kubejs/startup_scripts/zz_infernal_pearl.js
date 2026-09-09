// Жемчужина Пламени: телепорт в Ад и обратно к точке входа (память в persistentData).
StartupEvents.registry('item', (event) => {
  event.create('infernal_pearl')
    .displayName('Жемчужина Пламени')
    .maxStackSize(1)
    .fireResistant(true)
    .texture('kubejs:item/infernal_pearl')
    .tooltip('§7ПКМ: Ад ↔ точка входа. §8Кулдаун 10 мин')
})
