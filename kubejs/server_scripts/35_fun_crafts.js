// AquaTech: Fun-first crafting rebalance.
// x32 stays on Avaritia 9×9 only (30_aquatech_crafting.js). No extra workbench copy.

ServerEvents.recipes((event) => {
event.remove({ id: 'aquatech:rate_x32_workbench' })

console.log('[AquaTech] Fun crafting: workbench x32 duplicate removed')
})
