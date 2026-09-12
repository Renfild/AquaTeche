// Ядро Рыболова — апгрейд рыболова для ловли РЫБЫ (без него — ресурсы).
// 8x8: Avaritia Extreme Table (tier 4)
ServerEvents.recipes((event) => {
  if (Platform.isLoaded('avaritia')) {
    event.custom({
      type: 'avaritia:shaped_table',
      tier: 4,
      category: 'misc',
      pattern: [
        '  SRRS  ',
        ' SRCCRS ',
        'SRCDDCRS',
        'RCDHHDRC',
        'RCDHHDRC',
        'SRCDDCRS',
        ' SRCCRS ',
        '  SRRS  '
      ],
      key: {
        S: { item: 'minecraft:heart_of_the_sea' },
        R: { item: 'minecraft:prismarine_shard' },
        C: { item: 'minecraft:lapis_block' },
        D: { item: 'industrialupgrade:preciousgem/diamond_gem' },
        H: { item: 'starcatcher:humble_rod' }
      },
      result: { item: 'aquatech_machines:fishing_core', count: 1 },
      show_notification: true
    }).id('aquatech_machines:fishing_core_extreme')
  } else {
    event.shaped('aquatech_machines:fishing_core', ['RHR', 'CDC', 'RCR'], {
      R: 'minecraft:heart_of_the_sea',
      H: 'starcatcher:humble_rod',
      C: 'minecraft:lapis_block',
      D: 'minecraft:diamond'
    }).id('aquatech_machines:fishing_core_fallback')
  }
})
