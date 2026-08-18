// Auto-claim WorldGuard region and setup spawn/home for player islands
PlayerEvents.loggedIn(event => {
    let player = event.player;
    let pName = player.username;
    
    // First-time island setup
    if (!player.persistentData.hasClaimedIsland) {
        player.persistentData.hasClaimedIsland = true;
        let x = Math.floor(player.x);
        let y = Math.floor(player.y);
        let z = Math.floor(player.z);
        
        player.persistentData.islandX = x;
        player.persistentData.islandY = y;
        player.persistentData.islandZ = z;

        event.server.scheduleInTicks(20, callback => {
            // Set vanilla Minecraft spawnpoint for the player at their island
            event.server.runCommandSilent(`spawnpoint ${pName} ${x} ${y} ${z}`);
            // Set Essentials home for the player at their island
            event.server.runCommandSilent(`execute as ${pName} run sethome home`);
            
            // WorldGuard island region
            let r = 32;
            event.server.runCommandSilent(`execute as ${pName} run //pos1 ${x - r},0,${z - r}`);
            event.server.runCommandSilent(`execute as ${pName} run //pos2 ${x + r},320,${z + r}`);
            event.server.runCommandSilent(`rg define island_${pName} ${pName}`);
        });
    } else if (player.persistentData.islandX == null) {
        // Backfill for existing players: if they are away from central spawn, save their coords
        let x = Math.floor(player.x);
        let y = Math.floor(player.y);
        let z = Math.floor(player.z);
        if (Math.max(Math.abs(x), Math.abs(z)) > 10) {
            player.persistentData.islandX = x;
            player.persistentData.islandY = y;
            player.persistentData.islandZ = z;
            event.server.runCommandSilent(`spawnpoint ${pName} ${x} ${y} ${z}`);
        }
    }
});

// Fail-safe respawn handling: ensure player respawns on their island if bed is missing/obstructed
PlayerEvents.respawned(event => {
    let player = event.player;
    let pName = player.username;

    let ix = player.persistentData.islandX;
    let iy = player.persistentData.islandY;
    let iz = player.persistentData.islandZ;

    if (ix != null && iy != null && iz != null) {
        event.server.scheduleInTicks(1, callback => {
            let px = Math.floor(player.x);
            let pz = Math.floor(player.z);
            
            let distFromSpawn = Math.max(Math.abs(px), Math.abs(pz));
            let islandDistFromSpawn = Math.max(Math.abs(ix), Math.abs(iz));

            // If player ended up at central spawn (0,0) instead of their island
            if (distFromSpawn <= 10 && islandDistFromSpawn > 10) {
                let overworld = event.server.getLevel('minecraft:overworld');
                if (overworld) {
                    player.teleportTo(overworld, ix + 0.5, iy, iz + 0.5, player.yaw, player.pitch);
                    player.tell(Text.aqua('[AquaTech] Вы возродились на своём острове.'));
                }
            }
        });
    }
});
