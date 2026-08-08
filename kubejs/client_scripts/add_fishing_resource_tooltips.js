// KubeJS Client Script: Tooltips indicating which fishing rod catches resources.

ItemEvents.tooltip((event) => {
  // Starter items caught by Bamboo Rod (Бамбуковая удочка T-1)
  const t1Items = [
    'minecraft:dirt',
    'minecraft:grass_block',
    'minecraft:cobblestone',
    'minecraft:gravel',
    'minecraft:sand',
    'minecraft:clay_ball',
    'minecraft:clay',
    'minecraft:oak_log',
    'minecraft:oak_wood',
    'minecraft:oak_planks',
    'minecraft:oak_sapling',
    'minecraft:birch_sapling',
    'minecraft:spruce_sapling',
    'minecraft:jungle_sapling',
    'minecraft:acacia_sapling',
    'minecraft:dark_oak_sapling',
    'industrialupgrade:sapling/rubber_sapling',
    'industrialupgrade:raw_latex',
    'industrialupgrade:blockresource/untreated_peat',
    'minecraft:copper_ore',
    'industrialupgrade:classicore/tin'
  ];

  t1Items.forEach((id) => {
    event.add(id, [
      Text.of('⚓ Вылавливается Бамбуковой удочкой (Т-1)').cyan(),
      Text.of('💡 Ловится в воде на вашем плоту').darkGray()
    ]);
  });

  // Items caught by Humble Rod (Скромная удочка T-2)
  event.add('industrialupgrade:baseore/titanium', [
    Text.of('⚓ Вылавливается Скромной удочкой (Т-2)').gold(),
    Text.of('💡 Скромная удочка крафтится из Бамбуковой').darkGray()
  ]);

  event.add('minecraft:iron_ore', [
    Text.of('⚓ Вылавливается Скромной удочкой (Т-2)').cyan()
  ]);

  event.add('minecraft:redstone', [
    Text.of('⚓ Вылавливается Скромной удочкой (Т-2)').cyan()
  ]);
});
