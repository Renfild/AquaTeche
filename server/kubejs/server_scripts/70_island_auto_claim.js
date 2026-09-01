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

// Island & Corporate Commands (/is, /island, /corp, /clan)
// Coords source of truth = aquatech_ui PersonalRaftSpawner tags (aquatech_ui:raft_*).

// Raft region id, mirrors Java raftRegionId(): lowercase + sanitized + _raft.
function raftRegion(name) {
    return (name + '_raft').toLowerCase().replace(/[^a-z0-9_-]/g, '_');
}

// Raft deck coords from the Java mod tags; +1 to stand on top of the deck.
function raftCoords(p) {
    if (p == null) return null;
    let d = p.persistentData;
    if (!d.contains('aquatech_ui:raft_ready')) return null;
    return { x: d.getInt('aquatech_ui:raft_x'), y: d.getInt('aquatech_ui:raft_y') + 1, z: d.getInt('aquatech_ui:raft_z') };
}

function legacyIslandCoords(p) {
    if (p == null || p.persistentData.islandX == null) return null;
    return { x: p.persistentData.islandX, y: p.persistentData.islandY, z: p.persistentData.islandZ };
}

function islandHomeCoords(player, leader) {
    return raftCoords(leader) || raftCoords(player) || legacyIslandCoords(player);
}

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

                        ctx.source.server.runCommandSilent(`rg addmember ${raftRegion(pName)} ${targetName} -w world`);
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

                        ctx.source.server.runCommandSilent(`rg addmember ${raftRegion(pName)} ${targetName} -w world`);
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

                        ctx.source.server.runCommandSilent(`rg removemember ${raftRegion(pName)} ${targetName} -w world`);
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

                        ctx.source.server.runCommandSilent(`rg removemember ${raftRegion(pName)} ${targetName} -w world`);
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

                    let pos = islandHomeCoords(player, leader);

                    if (pos) {
                        let overworld = ctx.source.server.getLevel('minecraft:overworld');
                        player.teleportTo(overworld, pos.x + 0.5, pos.y, pos.z + 0.5, player.yaw, player.pitch);
                        player.tell(Text.gold(`[Остров] Вы телепортированы на остров (${leaderName}).`));
                    } else {
                        player.tell(Text.red('[Остров] Плот не найден. Введите /raft, чтобы создать плот.'));
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
                    player.tell(Text.white(`Регион WorldGuard: §e${raftRegion(leader)}`));
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

                let pos = islandHomeCoords(player, leader);

                if (pos) {
                    let overworld = ctx.source.server.getLevel('minecraft:overworld');
                    player.teleportTo(overworld, pos.x + 0.5, pos.y, pos.z + 0.5, player.yaw, player.pitch);
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
