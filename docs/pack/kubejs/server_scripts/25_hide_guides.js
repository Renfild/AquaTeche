// Скрытые гайдбуки модов — у нас свой Гайдбук в F4 (30_aquatech_crafting.js не трогаем)
ItemEvents.modification((event) => {
  ['industrialupgrade:guide_book', 'industrialupgrade:guide_tablet', 'industrialupgrade:book',
   'starcatcher:starcatcher_guide'].forEach((id) => {
    try {
      const stack = Item.of(id);
      if (stack && !stack.isEmpty()) {
        event.hide(stack.getItem());
        console.log('[AquaTech] hidden guide item: ' + id);
      }
    } catch (err) {
      // предмета нет в этой версии мода — пропускаем
    }
  });
})
