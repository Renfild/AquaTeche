// AquaTech Island Management & Corporate Commands (/is, /island, /corp)
// Manages WorldGuard regions, island trust, and team island teleports

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

// Fail-safe respawn handling: ensure player respawns on their island
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

// Island & Corporate Commands (/is, /island, /corp, /clan)
ServerEvents.commandRegistry(event => {
    const { commands: Commands, arguments: Arguments } = event;

    function buildIslandCommand(rootLiteral) {
        return Commands.literal(rootLiteral)
            .then(Commands.literal('trust')
                .then(Commands.argument('target', Arguments.STRING.create(event))
                    .executes(ctx => {
                        let sender = ctx.source.player;
                        if (!sender) return 0;
                        let targetName = Arguments.STRING.getResult(ctx, 'target');
                        let pName = sender.username;

                        ctx.source.server.runCommandSilent(`rg addmember island_${pName} ${targetName} -w world`);
                        sender.tell(Text.gold(`[Остров] Игрок §a${targetName} §fдобавлен в регион вашего острова!`));
                        
                        let targetPlayer = ctx.source.server.getPlayer(targetName);
                        if (targetPlayer) {
                            targetPlayer.persistentData.corpLeader = pName;
                            targetPlayer.tell(Text.gold(`[Остров] Вы получили доступ к острову §a${pName}§f! Используйте §e/is home§f для перемещения.`));
                        }
                        return 1;
                    })
                )
            )
            .then(Commands.literal('add')
                .then(Commands.argument('target', Arguments.STRING.create(event))
                    .executes(ctx => {
                        let sender = ctx.source.player;
                        if (!sender) return 0;
                        let targetName = Arguments.STRING.getResult(ctx, 'target');
                        let pName = sender.username;

                        ctx.source.server.runCommandSilent(`rg addmember island_${pName} ${targetName} -w world`);
                        sender.tell(Text.gold(`[Остров] Игрок §a${targetName} §fдобавлен в регион вашего острова!`));
                        
                        let targetPlayer = ctx.source.server.getPlayer(targetName);
                        if (targetPlayer) {
                            targetPlayer.persistentData.corpLeader = pName;
                            targetPlayer.tell(Text.gold(`[Остров] Вы получили доступ к острову §a${pName}§f! Используйте §e/is home§f для перемещения.`));
                        }
                        return 1;
                    })
                )
            )
            .then(Commands.literal('untrust')
                .then(Commands.argument('target', Arguments.STRING.create(event))
                    .executes(ctx => {
                        let sender = ctx.source.player;
                        if (!sender) return 0;
                        let targetName = Arguments.STRING.getResult(ctx, 'target');
                        let pName = sender.username;

                        ctx.source.server.runCommandSilent(`rg removemember island_${pName} ${targetName} -w world`);
                        sender.tell(Text.gold(`[Остров] Игрок §c${targetName} §fудалён из региона вашего острова.`));
                        
                        let targetPlayer = ctx.source.server.getPlayer(targetName);
                        if (targetPlayer && targetPlayer.persistentData.corpLeader === pName) {
                            delete targetPlayer.persistentData.corpLeader;
                        }
                        return 1;
                    })
                )
            )
            .then(Commands.literal('remove')
                .then(Commands.argument('target', Arguments.STRING.create(event))
                    .executes(ctx => {
                        let sender = ctx.source.player;
                        if (!sender) return 0;
                        let targetName = Arguments.STRING.getResult(ctx, 'target');
                        let pName = sender.username;

                        ctx.source.server.runCommandSilent(`rg removemember island_${pName} ${targetName} -w world`);
                        sender.tell(Text.gold(`[Остров] Игрок §c${targetName} §fудалён из региона вашего острова.`));
                        
                        let targetPlayer = ctx.source.server.getPlayer(targetName);
                        if (targetPlayer && targetPlayer.persistentData.corpLeader === pName) {
                            delete targetPlayer.persistentData.corpLeader;
                        }
                        return 1;
                    })
                )
            )
            .then(Commands.literal('sethome')
                .executes(ctx => {
                    let player = ctx.source.player;
                    if (!player) return 0;
                    let x = Math.floor(player.x);
                    let y = Math.floor(player.y);
                    let z = Math.floor(player.z);
                    player.persistentData.islandX = x;
                    player.persistentData.islandY = y;
                    player.persistentData.islandZ = z;
                    ctx.source.server.runCommandSilent(`spawnpoint ${player.username} ${x} ${y} ${z}`);
                    ctx.source.server.runCommandSilent(`execute as ${player.username} run sethome home`);
                    player.tell(Text.gold(`[Остров] Новая точка дома острова сохранена: §e[${x}, ${y}, ${z}]`));
                    return 1;
                })
            )
            .then(Commands.literal('home')
                .executes(ctx => {
                    let player = ctx.source.player;
                    if (!player) return 0;
                    let leaderName = player.persistentData.corpLeader || player.username;
                    let leader = ctx.source.server.getPlayer(leaderName);

                    let ix, iy, iz;
                    if (leader && leader.persistentData.islandX != null) {
                        ix = leader.persistentData.islandX;
                        iy = leader.persistentData.islandY;
                        iz = leader.persistentData.islandZ;
                    } else if (player.persistentData.islandX != null) {
                        ix = player.persistentData.islandX;
                        iy = player.persistentData.islandY;
                        iz = player.persistentData.islandZ;
                    }

                    if (ix != null && iy != null && iz != null) {
                        let overworld = ctx.source.server.getLevel('minecraft:overworld');
                        player.teleportTo(overworld, ix + 0.5, iy, iz + 0.5, player.yaw, player.pitch);
                        player.tell(Text.gold(`[Остров] Вы телепортированы на остров (${leaderName}).`));
                    } else {
                        player.tell(Text.red('[Остров] Координаты острова не найдены.'));
                    }
                    return 1;
                })
            )
            .then(Commands.literal('info')
                .executes(ctx => {
                    let player = ctx.source.player;
                    if (!player) return 0;
                    let pName = player.username;
                    let leader = player.persistentData.corpLeader || pName;

                    player.tell(Text.gold('=== §bОкеанический Остров §6==='));
                    player.tell(Text.white(`Владелец острова: §a${leader}`));
                    player.tell(Text.white(`Регион WorldGuard: §eisland_${leader}`));
                    player.tell(Text.white(`Координаты: §e[${player.persistentData.islandX || '?'}, ${player.persistentData.islandY || '?'}, ${player.persistentData.islandZ || '?'}]`));
                    player.tell(Text.gray('Команды: §e/is trust <Ник>§7, §e/is untrust <Ник>§7, §e/is home§7, §e/is sethome'));
                    player.tell(Text.gray('Для общего прогресса квестов: §e/ftbteams party create <Имя>'));
                    return 1;
                })
            )
            .executes(ctx => {
                let player = ctx.source.player;
                if (!player) return 0;
                let leaderName = player.persistentData.corpLeader || player.username;
                let leader = ctx.source.server.getPlayer(leaderName);

                let ix, iy, iz;
                if (leader && leader.persistentData.islandX != null) {
                    ix = leader.persistentData.islandX;
                    iy = leader.persistentData.islandY;
                    iz = leader.persistentData.islandZ;
                } else if (player.persistentData.islandX != null) {
                    ix = player.persistentData.islandX;
                    iy = player.persistentData.islandY;
                    iz = player.persistentData.islandZ;
                }

                if (ix != null && iy != null && iz != null) {
                    let overworld = ctx.source.server.getLevel('minecraft:overworld');
                    player.teleportTo(overworld, ix + 0.5, iy, iz + 0.5, player.yaw, player.pitch);
                    player.tell(Text.gold(`[Остров] Вы телепортированы на остров (${leaderName}).`));
                } else {
                    player.tell(Text.gold('§b[AquaTech Остров] §fИспользуйте:'));
                    player.tell(Text.yellow('  /is trust <Игрок> §7— добавить игрока в приват острова'));
                    player.tell(Text.yellow('  /is untrust <Игрок> §7— удалить игрока из привата'));
                    player.tell(Text.yellow('  /is home §7— телепорт на свой остров'));
                    player.tell(Text.yellow('  /is sethome §7— установить точку дома острова'));
                    player.tell(Text.yellow('  /is info §7— информация об острове'));
                }
                return 1;
            });
    }

    // Register all aliases: /is, /island, /corp, /clan
    event.register(buildIslandCommand('is'));
    event.register(buildIslandCommand('island'));
    event.register(buildIslandCommand('corp'));
    event.register(buildIslandCommand('clan'));
});
