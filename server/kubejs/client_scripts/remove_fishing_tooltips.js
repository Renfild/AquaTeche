// KubeJS Client Script: Strip obsolete "drops from fishing" tooltips from Avaritia / Botania / Re-Avaritia items
ClientEvents.tooltip((event) => {
  const targetMods = ['avaritia', 're_avaritia', 'botania'];
  
  targetMods.forEach((modId) => {
    event.addAdvanced(RegExp('^' + modId + ':'), (item, advanced, text) => {
      for (let i = text.length - 1; i >= 0; i--) {
        let lineStr = text.get(i).getString().toLowerCase();
        if (lineStr.includes('рыбалк') || lineStr.includes('fishing') || lineStr.includes('вылавлива')) {
          text.remove(i);
        }
      }
    });
  });
});
