// Auto-claim WorldGuard region for new player islands
PlayerEvents.loggedIn(event => {
    let player = event.player;
    if (!player.persistentData.hasClaimedIsland) {
        player.persistentData.hasClaimedIsland = true;
        let pName = player.username;
        event.server.scheduleInTicks(40, callback => {
            event.server.runCommandSilent(`rg claim island_${pName} ${pName}`);
        });
    }
});
