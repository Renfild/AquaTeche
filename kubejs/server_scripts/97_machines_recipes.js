// AquaTech: Механизмы — рецепты (aquatech_machines)
// Рыболов MK-2: железо + удочка StarCatcher + электронная схема + reinforced stone
ServerEvents.recipes((event) => {
  event.shaped('aquatech_machines:fisher', ['IFI', 'RCF', 'ISI'], {
    I: 'industrialupgrade:itemingots/aluminium_ingot',
    F: 'starcatcher:good_old_rod',
    R: 'industrialupgrade:crafting_elements/crafting_272_element',
    C: 'minecraft:chest',
    S: 'industrialupgrade:blockresource/reinforced_stone',
  }).id('aquatech_machines:fisher')

  // Экскаватор: буры + электросхемы + reinforced stone
  event.shaped('aquatech_machines:excavator', ['DBD', 'RCR', 'SSS'], {
    D: 'minecraft:diamond',
    B: 'minecraft:iron_block',
    R: 'industrialupgrade:crafting_elements/crafting_273_element',
    C: 'minecraft:chest',
    S: 'industrialupgrade:blockresource/reinforced_stone',
  }).id('aquatech_machines:excavator')

  // Экстрактор: призмарин + электронная схема + chest + iron
  event.shaped('aquatech_machines:extractor', ['PXP', 'ICI', 'PIP'], {
    P: 'minecraft:prismarine_crystals',
    X: 'industrialupgrade:crafting_elements/crafting_273_element',
    I: 'industrialupgrade:itemingots/aluminium_ingot',
    C: 'minecraft:chest',
  }).id('aquatech_machines:extractor')
})
