package com.casesmod.server.commands;

import com.casesmod.data.*;
import com.casesmod.item.ModItems;
import com.casesmod.network.NetworkHandler;
import com.casesmod.network.packets.BalanceSyncS2CPacket;
import com.casesmod.network.packets.CaseListSyncS2CPacket;
import com.casesmod.network.packets.CaseResultS2CPacket;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Команды мода + настройка варпов/китов/кейсов прямо в игре.
 * После любой правки каталог сразу синкается всем онлайн (без ожидания перезахода).
 */
public class MenuCommands {

    private static final String[] RARITIES = {"COMMON", "UNCOMMON", "RARE", "EPIC", "LEGENDARY"};

    private static final SuggestionProvider<CommandSourceStack> CASE_SUGGESTIONS = (ctx, builder) -> {
        CaseManager.INSTANCE.getCases().forEach(c -> builder.suggest(c.id));
        return builder.buildFuture();
    };
    private static final SuggestionProvider<CommandSourceStack> KIT_SUGGESTIONS = (ctx, builder) -> {
        KitManager.INSTANCE.getKits().forEach(k -> builder.suggest(k.id));
        return builder.buildFuture();
    };
    private static final SuggestionProvider<CommandSourceStack> WARP_SUGGESTIONS = (ctx, builder) -> {
        WarpManager.INSTANCE.getWarps().forEach(w -> builder.suggest(w.id));
        return builder.buildFuture();
    };
    private static final SuggestionProvider<CommandSourceStack> RARITY_SUGGESTIONS = (ctx, builder) -> {
        for (String r : RARITIES) builder.suggest(r);
        return builder.buildFuture();
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("menu")
                .executes(ctx -> {
                    openMenuFor(ctx.getSource().getPlayerOrException());
                    return 1;
                }));

        dispatcher.register(Commands.literal("deposit")
                .executes(ctx -> depositLightmansCoins(ctx.getSource().getPlayerOrException())));

        dispatcher.register(Commands.literal("withdraw")
                .then(Commands.argument("amount", LongArgumentType.longArg(1))
                        .executes(ctx -> withdrawLightmansCoins(
                                ctx.getSource().getPlayerOrException(),
                                LongArgumentType.getLong(ctx, "amount")))));

        dispatcher.register(Commands.literal("sellfish")
                .requires(src -> src.hasPermission(2))
                .executes(ctx -> {
                    FishSellService.sell(ctx.getSource().getPlayerOrException(), false);
                    return 1;
                })
                .then(Commands.literal("hand")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> {
                            FishSellService.sell(ctx.getSource().getPlayerOrException(), true);
                            return 1;
                        }))
                .then(Commands.literal("all")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> {
                            FishSellService.sell(ctx.getSource().getPlayerOrException(), false);
                            return 1;
                        })));

        dispatcher.register(Commands.literal("fishmarket")
                .requires(src -> src.hasPermission(2))
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    openFishMarket(player);
                    return 1;
                })
                .then(Commands.argument("player", EntityArgument.player())
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> {
                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                            openFishMarket(target);
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                    "§aРынок открыт для " + target.getGameProfile().getName()), true);
                            return 1;
                        })));

        dispatcher.register(Commands.literal("casesmod")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("reload")
                        .executes(ctx -> {
                            CaseManager.INSTANCE.load();
                            KitManager.INSTANCE.load();
                            WarpManager.INSTANCE.load();
                            QuestManager.INSTANCE.load();
                            NetworkHandler.broadcastCaseList(ctx.getSource().getServer());
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                    "§aКонфиги casesmod перезагружены и разосланы всем онлайн."), true);
                            return 1;
                        }))

                .then(Commands.literal("openfishmarket")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> {
                                    ServerPlayer p = EntityArgument.getPlayer(ctx, "player");
                                    openFishMarket(p);
                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                            "§aРынок открыт для " + p.getGameProfile().getName()), true);
                                    return 1;
                                })))

                .then(Commands.literal("addbalance")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", LongArgumentType.longArg(1))
                                        .executes(ctx -> {
                                            ServerPlayer p = EntityArgument.getPlayer(ctx, "player");
                                            long amount = LongArgumentType.getLong(ctx, "amount");
                                            CurrencyManager.INSTANCE.add(p.getUUID(), amount);
                                            long bal = CurrencyManager.INSTANCE.getBalance(p.getUUID());
                                            syncBalance(p, bal);
                                            ctx.getSource().sendSuccess(() -> Component.literal(
                                                    "§aНачислено §f" + amount + " §aигроку " + p.getGameProfile().getName()
                                                            + " §7(баланс: " + bal + ")"), true);
                                            p.sendSystemMessage(Component.literal("§aВам начислено §f" + amount + " §aвалюты!"));
                                            return 1;
                                        }))))

                .then(Commands.literal("setbalance")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", LongArgumentType.longArg(0))
                                        .executes(ctx -> {
                                            ServerPlayer p = EntityArgument.getPlayer(ctx, "player");
                                            long amount = LongArgumentType.getLong(ctx, "amount");
                                            CurrencyManager.INSTANCE.setBalance(p.getUUID(), amount);
                                            syncBalance(p, amount);
                                            ctx.getSource().sendSuccess(() -> Component.literal(
                                                    "§aБаланс " + p.getGameProfile().getName() + " установлен: §f" + amount), true);
                                            return 1;
                                        }))))

                .then(Commands.literal("balance")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> {
                                    ServerPlayer p = EntityArgument.getPlayer(ctx, "player");
                                    long bal = CurrencyManager.INSTANCE.getBalance(p.getUUID());
                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                            "§7Баланс " + p.getGameProfile().getName() + ": §f" + bal), false);
                                    return (int) Math.min(Integer.MAX_VALUE, bal);
                                })))

                .then(Commands.literal("opencase")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("caseId", StringArgumentType.word())
                                        .suggests(CASE_SUGGESTIONS)
                                        .executes(ctx -> openCaseInstant(ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player"),
                                                StringArgumentType.getString(ctx, "caseId"))))))

                .then(Commands.literal("givecase")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("caseId", StringArgumentType.word())
                                        .suggests(CASE_SUGGESTIONS)
                                        .executes(ctx -> giveCase(ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player"),
                                                StringArgumentType.getString(ctx, "caseId"), 1))
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 999))
                                                .executes(ctx -> giveCase(ctx.getSource(),
                                                        EntityArgument.getPlayer(ctx, "player"),
                                                        StringArgumentType.getString(ctx, "caseId"),
                                                        IntegerArgumentType.getInteger(ctx, "amount")))))))

                .then(Commands.literal("givecaseall")
                        .then(Commands.argument("caseId", StringArgumentType.word())
                                .suggests(CASE_SUGGESTIONS)
                                .executes(ctx -> giveCaseAll(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "caseId"), 1))
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1, 999))
                                        .executes(ctx -> giveCaseAll(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "caseId"),
                                                IntegerArgumentType.getInteger(ctx, "amount"))))))

                .then(Commands.literal("givekit")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("kitId", StringArgumentType.word())
                                        .suggests(KIT_SUGGESTIONS)
                                        .executes(ctx -> giveKit(ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player"),
                                                StringArgumentType.getString(ctx, "kitId"))))))

                .then(Commands.literal("givemenu")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> {
                                    ServerPlayer p = EntityArgument.getPlayer(ctx, "player");
                                    ItemStack stack = new ItemStack(ModItems.MENU_OPENER.get());
                                    stack.setHoverName(Component.literal("§dМеню сервера"));
                                    if (!p.getInventory().add(stack)) p.drop(stack, false);
                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                            "§aВыдан предмет меню игроку " + p.getGameProfile().getName()), true);
                                    return 1;
                                })))

                // ===== Варпы =====
                .then(Commands.literal("setwarp")
                        .then(Commands.argument("id", StringArgumentType.string())
                                .suggests(WARP_SUGGESTIONS)
                                .executes(ctx -> createWarp(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "id"), null))
                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                        .executes(ctx -> createWarp(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "id"),
                                                StringArgumentType.getString(ctx, "name"))))))

                .then(Commands.literal("delwarp")
                        .then(Commands.argument("id", StringArgumentType.string())
                                .suggests(WARP_SUGGESTIONS)
                                .executes(ctx -> {
                                    String id = StringArgumentType.getString(ctx, "id");
                                    WarpManager.INSTANCE.remove(id);
                                    syncLive(ctx.getSource());
                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                            "§aВарп \"" + id + "\" удалён."), true);
                                    return 1;
                                })))

                .then(Commands.literal("listwarps")
                        .executes(ctx -> listWarps(ctx.getSource())))

                // ===== Киты =====
                .then(Commands.literal("setkit")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .suggests(KIT_SUGGESTIONS)
                                .executes(ctx -> setKit(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "id"), null))
                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                        .executes(ctx -> setKit(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "id"),
                                                StringArgumentType.getString(ctx, "name"))))))

                .then(Commands.literal("setkitcooldown")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .suggests(KIT_SUGGESTIONS)
                                .then(Commands.argument("seconds", IntegerArgumentType.integer(0))
                                        .executes(ctx -> {
                                            KitDefinition kit = KitManager.INSTANCE.get(StringArgumentType.getString(ctx, "id"));
                                            if (kit == null) {
                                                ctx.getSource().sendFailure(Component.literal("§cКит не найден."));
                                                return 0;
                                            }
                                            kit.cooldownSeconds = IntegerArgumentType.getInteger(ctx, "seconds");
                                            KitManager.INSTANCE.addOrUpdate(kit);
                                            syncLive(ctx.getSource());
                                            ctx.getSource().sendSuccess(() -> Component.literal(
                                                    "§aКулдаун кита \"" + kit.id + "§a\": " + kit.cooldownSeconds + " сек."), true);
                                            return 1;
                                        }))))

                .then(Commands.literal("setkitpermission")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .suggests(KIT_SUGGESTIONS)
                                .then(Commands.argument("permission", StringArgumentType.word())
                                        .executes(ctx -> {
                                            KitDefinition kit = KitManager.INSTANCE.get(StringArgumentType.getString(ctx, "id"));
                                            if (kit == null) {
                                                ctx.getSource().sendFailure(Component.literal("§cКит не найден."));
                                                return 0;
                                            }
                                            String perm = StringArgumentType.getString(ctx, "permission");
                                            kit.permission = perm.equals("-") ? "" : perm;
                                            KitManager.INSTANCE.addOrUpdate(kit);
                                            syncLive(ctx.getSource());
                                            ctx.getSource().sendSuccess(() -> Component.literal(
                                                    "§aПраво кита \"" + kit.id + "§a\": "
                                                            + (kit.permission.isEmpty() ? "§7всем" : kit.permission)), true);
                                            return 1;
                                        }))))

                .then(Commands.literal("delkit")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .suggests(KIT_SUGGESTIONS)
                                .executes(ctx -> {
                                    String id = StringArgumentType.getString(ctx, "id");
                                    KitManager.INSTANCE.remove(id);
                                    syncLive(ctx.getSource());
                                    ctx.getSource().sendSuccess(() -> Component.literal("§aКит \"" + id + "\" удалён."), true);
                                    return 1;
                                })))

                // ===== Кейсы =====
                .then(Commands.literal("createcase")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                                            String id = StringArgumentType.getString(ctx, "id");
                                            if (CaseManager.INSTANCE.get(id) != null) {
                                                ctx.getSource().sendFailure(Component.literal("§cКейс \"" + id + "\" уже существует."));
                                                return 0;
                                            }
                                            CaseDefinition def = new CaseDefinition();
                                            def.id = id;
                                            def.displayName = StringArgumentType.getString(ctx, "name");
                                            def.price = 100;
                                            def.pityThreshold = 15;
                                            def.pityRarity = "EPIC";
                                            ItemStack held = player.getMainHandItem();
                                            def.iconItemId = held.isEmpty() ? "minecraft:chest"
                                                    : BuiltInRegistries.ITEM.getKey(held.getItem()).toString();
                                            def.items = new ArrayList<>();
                                            CaseManager.INSTANCE.addOrUpdate(def);
                                            syncLive(ctx.getSource());
                                            ctx.getSource().sendSuccess(() -> Component.literal(
                                                    "§aКейс создан (id: " + id + "). Призы: /casesmod addcaseitem "
                                                            + id + " <вес> <редкость>"), true);
                                            return 1;
                                        }))))

                .then(Commands.literal("addcaseitem")
                        .then(Commands.argument("caseId", StringArgumentType.word())
                                .suggests(CASE_SUGGESTIONS)
                                .then(Commands.argument("weight", DoubleArgumentType.doubleArg(0.0001))
                                        .then(Commands.argument("rarity", StringArgumentType.word())
                                                .suggests(RARITY_SUGGESTIONS)
                                                .executes(ctx -> addCaseItem(ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "caseId"),
                                                        DoubleArgumentType.getDouble(ctx, "weight"),
                                                        StringArgumentType.getString(ctx, "rarity"), -1, null))
                                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 6400))
                                                        .executes(ctx -> addCaseItem(ctx.getSource(),
                                                                StringArgumentType.getString(ctx, "caseId"),
                                                                DoubleArgumentType.getDouble(ctx, "weight"),
                                                                StringArgumentType.getString(ctx, "rarity"),
                                                                IntegerArgumentType.getInteger(ctx, "count"), null))
                                                        .then(Commands.argument("command", StringArgumentType.greedyString())
                                                                .executes(ctx -> addCaseItem(ctx.getSource(),
                                                                        StringArgumentType.getString(ctx, "caseId"),
                                                                        DoubleArgumentType.getDouble(ctx, "weight"),
                                                                        StringArgumentType.getString(ctx, "rarity"),
                                                                        IntegerArgumentType.getInteger(ctx, "count"),
                                                                        StringArgumentType.getString(ctx, "command")))))))))

                .then(Commands.literal("addcasemoney")
                        .then(Commands.argument("caseId", StringArgumentType.word())
                                .suggests(CASE_SUGGESTIONS)
                                .then(Commands.argument("weight", DoubleArgumentType.doubleArg(0.0001))
                                        .then(Commands.argument("rarity", StringArgumentType.word())
                                                .suggests(RARITY_SUGGESTIONS)
                                                .then(Commands.argument("amount", LongArgumentType.longArg(1))
                                                        .executes(ctx -> addCaseMoney(ctx.getSource(),
                                                                StringArgumentType.getString(ctx, "caseId"),
                                                                DoubleArgumentType.getDouble(ctx, "weight"),
                                                                StringArgumentType.getString(ctx, "rarity"),
                                                                LongArgumentType.getLong(ctx, "amount"))))))))

                .then(Commands.literal("addcasecmd")
                        .then(Commands.argument("caseId", StringArgumentType.word())
                                .suggests(CASE_SUGGESTIONS)
                                .then(Commands.argument("weight", DoubleArgumentType.doubleArg(0.0001))
                                        .then(Commands.argument("rarity", StringArgumentType.word())
                                                .suggests(RARITY_SUGGESTIONS)
                                                .then(Commands.argument("iconItem", StringArgumentType.word())
                                                        .then(Commands.argument("details", StringArgumentType.greedyString())
                                                                .executes(ctx -> addCaseCmd(ctx.getSource(),
                                                                        StringArgumentType.getString(ctx, "caseId"),
                                                                        DoubleArgumentType.getDouble(ctx, "weight"),
                                                                        StringArgumentType.getString(ctx, "rarity"),
                                                                        StringArgumentType.getString(ctx, "iconItem"),
                                                                        StringArgumentType.getString(ctx, "details")))))))))

                .then(Commands.literal("listcaseitems")
                        .then(Commands.argument("caseId", StringArgumentType.word())
                                .suggests(CASE_SUGGESTIONS)
                                .executes(ctx -> {
                                    CaseDefinition def = CaseManager.INSTANCE.get(StringArgumentType.getString(ctx, "caseId"));
                                    if (def == null) {
                                        ctx.getSource().sendFailure(Component.literal("§cКейс не найден."));
                                        return 0;
                                    }
                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                            "§7Кейс §f" + def.id + " §7| цена §b" + def.price
                                                    + " §7| pity §d" + (def.pityThreshold <= 0
                                                    ? "выкл"
                                                    : def.pityThreshold + " → " + def.pityRarity)), false);
                                    if (def.items.isEmpty()) {
                                        ctx.getSource().sendSuccess(() -> Component.literal("§7Призов нет."), false);
                                        return 1;
                                    }
                                    for (int i = 0; i < def.items.size(); i++) {
                                        CaseItem it = def.items.get(i);
                                        int idx = i;
                                        ctx.getSource().sendSuccess(() -> Component.literal(
                                                "§7[" + idx + "] §f" + it.displayName + " §7x" + it.count
                                                        + " §7(вес " + it.weight + ", " + it.rarity + ")"), false);
                                    }
                                    return def.items.size();
                                })))

                .then(Commands.literal("removecaseitem")
                        .then(Commands.argument("caseId", StringArgumentType.word())
                                .suggests(CASE_SUGGESTIONS)
                                .then(Commands.argument("index", IntegerArgumentType.integer(0))
                                        .executes(ctx -> {
                                            CaseDefinition def = CaseManager.INSTANCE.get(StringArgumentType.getString(ctx, "caseId"));
                                            int index = IntegerArgumentType.getInteger(ctx, "index");
                                            if (def == null || index < 0 || index >= def.items.size()) {
                                                ctx.getSource().sendFailure(Component.literal(
                                                        "§cКейс или номер приза не найден. /casesmod listcaseitems"));
                                                return 0;
                                            }
                                            List<CaseItem> items = new ArrayList<>(def.items);
                                            CaseItem removed = items.remove(index);
                                            def.items = items;
                                            CaseManager.INSTANCE.addOrUpdate(def);
                                            syncLive(ctx.getSource());
                                            ctx.getSource().sendSuccess(() -> Component.literal(
                                                    "§aУдалён приз \"" + removed.displayName + "§a\"."), true);
                                            return 1;
                                        }))))

                .then(Commands.literal("setcaseprice")
                        .then(Commands.argument("caseId", StringArgumentType.word())
                                .suggests(CASE_SUGGESTIONS)
                                .then(Commands.argument("price", LongArgumentType.longArg(0))
                                        .executes(ctx -> {
                                            CaseDefinition def = CaseManager.INSTANCE.get(StringArgumentType.getString(ctx, "caseId"));
                                            if (def == null) {
                                                ctx.getSource().sendFailure(Component.literal("§cКейс не найден."));
                                                return 0;
                                            }
                                            def.price = LongArgumentType.getLong(ctx, "price");
                                            CaseManager.INSTANCE.addOrUpdate(def);
                                            syncLive(ctx.getSource());
                                            ctx.getSource().sendSuccess(() -> Component.literal(
                                                    "§aЦена кейса \"" + def.id + "§a\": §f" + def.price), true);
                                            return 1;
                                        }))))

                .then(Commands.literal("setcasepity")
                        .then(Commands.argument("caseId", StringArgumentType.word())
                                .suggests(CASE_SUGGESTIONS)
                                .then(Commands.argument("threshold", IntegerArgumentType.integer(0, 999))
                                        .executes(ctx -> setCasePity(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "caseId"),
                                                IntegerArgumentType.getInteger(ctx, "threshold"),
                                                "EPIC"))
                                        .then(Commands.argument("rarity", StringArgumentType.word())
                                                .suggests(RARITY_SUGGESTIONS)
                                                .executes(ctx -> setCasePity(ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "caseId"),
                                                        IntegerArgumentType.getInteger(ctx, "threshold"),
                                                        StringArgumentType.getString(ctx, "rarity")))))))

                .then(Commands.literal("delcase")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .suggests(CASE_SUGGESTIONS)
                                .executes(ctx -> {
                                    String id = StringArgumentType.getString(ctx, "id");
                                    CaseManager.INSTANCE.remove(id);
                                    syncLive(ctx.getSource());
                                    ctx.getSource().sendSuccess(() -> Component.literal("§aКейс \"" + id + "\" удалён."), true);
                                    return 1;
                                })))
        );
    }

    /** Сразу рассылает обновлённый каталог всем онлайн. */
    private static void syncLive(CommandSourceStack source) {
        NetworkHandler.broadcastCaseList(source.getServer());
    }

    private static void openMenuFor(ServerPlayer player) {
        NetworkHandler.openMenuFor(player);
    }

    private static void openFishMarket(ServerPlayer player) {
        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new com.casesmod.network.packets.OpenFishMarketS2CPacket());
    }

    private static void syncBalance(ServerPlayer player, long balance) {
        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new BalanceSyncS2CPacket(balance));
    }

    private static int setCasePity(CommandSourceStack source, String caseId, int threshold, String rarityInput) {
        CaseDefinition def = CaseManager.INSTANCE.get(caseId);
        if (def == null) {
            source.sendFailure(Component.literal("§cКейс не найден."));
            return 0;
        }
        String rarity = rarityInput.toUpperCase();
        boolean ok = false;
        for (String r : RARITIES) if (r.equals(rarity)) ok = true;
        if (!ok) {
            source.sendFailure(Component.literal("§cРедкость: COMMON, UNCOMMON, RARE, EPIC, LEGENDARY"));
            return 0;
        }
        def.pityThreshold = threshold;
        def.pityRarity = rarity;
        CaseManager.INSTANCE.addOrUpdate(def);
        syncLive(source);
        if (threshold <= 0) {
            source.sendSuccess(() -> Component.literal("§aPity для \"" + def.id + "\" выключен."), true);
        } else {
            source.sendSuccess(() -> Component.literal(
                    "§aPity \"" + def.id + "\": §d" + threshold + " §aоткрытий → §d" + rarity), true);
        }
        return 1;
    }

    private static int setKit(CommandSourceStack source, String id, String nameOrNull) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        KitDefinition kit = KitManager.INSTANCE.get(id);
        boolean isNew = kit == null;
        if (isNew) {
            kit = new KitDefinition();
            kit.id = id;
            kit.displayName = id;
            kit.cooldownSeconds = 0;
            kit.permission = "";
        }
        if (nameOrNull != null) kit.displayName = nameOrNull;

        List<KitDefinition.KitItem> items = new ArrayList<>();
        Inventory inv = player.getInventory();
        for (ItemStack stack : inv.items) {
            if (stack.isEmpty()) continue;
            KitDefinition.KitItem ki = new KitDefinition.KitItem();
            ki.itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            ki.count = stack.getCount();
            items.add(ki);
        }
        kit.items = items;
        if (isNew) {
            ItemStack held = player.getMainHandItem();
            kit.iconItemId = held.isEmpty() ? "minecraft:chest_minecart"
                    : BuiltInRegistries.ITEM.getKey(held.getItem()).toString();
        }

        KitManager.INSTANCE.addOrUpdate(kit);
        syncLive(source);
        int count = items.size();
        final String savedName = kit.displayName;
        source.sendSuccess(() -> Component.literal(
                "§aКит \"" + savedName + "§a\" сохранён: §f" + count + " §aпредметов. Уже в меню."), true);
        return 1;
    }

    private static int createWarp(CommandSourceStack source, String id, String displayNameOrNull)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        WarpDefinition w = new WarpDefinition();
        w.id = id;
        w.displayName = (displayNameOrNull != null && !displayNameOrNull.trim().isEmpty()) ? displayNameOrNull.trim() : id;
        ItemStack held = player.getMainHandItem();
        w.iconItemId = held.isEmpty() ? "minecraft:ender_pearl"
                : BuiltInRegistries.ITEM.getKey(held.getItem()).toString();
        w.dimension = player.level().dimension().location().toString();
        w.x = player.getX(); w.y = player.getY(); w.z = player.getZ();
        w.yaw = player.getYRot(); w.pitch = player.getXRot();
        WarpManager.INSTANCE.addOrUpdate(w);
        syncLive(source);
        source.sendSuccess(() -> Component.literal(
                "§aВарп \"" + w.id + "\" (" + w.displayName + "§a) сохранён и доступен в меню."), true);
        return 1;
    }

    private static int listWarps(CommandSourceStack source) {
        java.util.Collection<WarpDefinition> warps = WarpManager.INSTANCE.getWarps();
        if (warps.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§7Варпы отсутствуют."), false);
            return 0;
        }
        source.sendSuccess(() -> Component.literal("§b✦ Список варпов (" + warps.size() + "):"), false);
        for (WarpDefinition w : warps) {
            String coords = String.format("x: %.0f, y: %.0f, z: %.0f", w.x, w.y, w.z);
            source.sendSuccess(() -> Component.literal(
                    "§7- ID: §f" + w.id + " §7| Название: §f" + w.displayName + " §7(" + coords + ")"), false);
        }
        return warps.size();
    }

    private static int addCaseItem(CommandSourceStack source, String caseId, double weight, String rarityInput, int countOverride, String commandOrNull)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        CaseDefinition def = CaseManager.INSTANCE.get(caseId);
        if (def == null) {
            source.sendFailure(Component.literal("§cКейс не найден. Сначала /casesmod createcase " + caseId + " <название>"));
            return 0;
        }
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            source.sendFailure(Component.literal("§cВозьмите в руку предмет-приз."));
            return 0;
        }
        String rarity = rarityInput.toUpperCase();
        boolean valid = false;
        for (String r : RARITIES) if (r.equals(rarity)) valid = true;
        if (!valid) {
            source.sendFailure(Component.literal("§cРедкость: COMMON, UNCOMMON, RARE, EPIC, LEGENDARY"));
            return 0;
        }

        CaseItem it = new CaseItem();
        it.itemId = BuiltInRegistries.ITEM.getKey(held.getItem()).toString();
        it.count = countOverride > 0 ? countOverride : held.getCount();
        it.weight = weight;
        it.rarity = rarity;
        it.displayName = held.getHoverName().getString();
        if (commandOrNull != null && !commandOrNull.trim().isEmpty()) {
            it.command = commandOrNull.trim();
        }

        List<CaseItem> items = new ArrayList<>(def.items);
        items.add(it);
        def.items = items;
        CaseManager.INSTANCE.addOrUpdate(def);
        syncLive(source);

        source.sendSuccess(() -> Component.literal("§aВ кейс добавлен: §f" + it.displayName
                + " §7x" + it.count + " §7(вес " + it.weight + ", " + it.rarity + ")"), true);
        return 1;
    }

    private static int addCaseMoney(CommandSourceStack source, String caseId, double weight, String rarityInput, long amount) {
        CaseDefinition def = CaseManager.INSTANCE.get(caseId);
        if (def == null) {
            source.sendFailure(Component.literal("§cКейс не найден."));
            return 0;
        }
        String rarity = rarityInput.toUpperCase();
        CaseItem it = new CaseItem();
        it.itemId = "minecraft:gold_nugget";
        it.count = 1;
        it.weight = weight;
        it.rarity = rarity;
        it.displayName = "§e" + amount + " Дублонов";
        it.command = "casesmod addbalance %player% " + amount;

        List<CaseItem> items = new ArrayList<>(def.items);
        items.add(it);
        def.items = items;
        CaseManager.INSTANCE.addOrUpdate(def);
        syncLive(source);

        source.sendSuccess(() -> Component.literal("§aВ кейс \"" + caseId + "\" добавлено начисление денег: §e"
                + amount + " Дублонов §7(вес " + weight + ", " + rarity + ")"), true);
        return 1;
    }

    private static int addCaseCmd(CommandSourceStack source, String caseId, double weight, String rarityInput, String iconItem, String details) {
        CaseDefinition def = CaseManager.INSTANCE.get(caseId);
        if (def == null) {
            source.sendFailure(Component.literal("§cКейс не найден."));
            return 0;
        }
        String rarity = rarityInput.toUpperCase();

        String displayName;
        String command;
        if (details.contains("|")) {
            String[] parts = details.split("\\|", 2);
            displayName = parts[0].trim();
            command = parts[1].trim();
        } else {
            displayName = details.trim();
            command = details.trim();
        }

        CaseItem it = new CaseItem();
        it.itemId = iconItem.contains(":") ? iconItem : "minecraft:" + iconItem;
        it.count = 1;
        it.weight = weight;
        it.rarity = rarity;
        it.displayName = displayName;
        it.command = command;

        List<CaseItem> items = new ArrayList<>(def.items);
        items.add(it);
        def.items = items;
        CaseManager.INSTANCE.addOrUpdate(def);
        syncLive(source);

        source.sendSuccess(() -> Component.literal("§aВ кейс \"" + caseId + "\" добавлена команда: §f"
                + displayName + " §7(команда: /" + command + ")"), true);
        return 1;
    }

    private static int openCaseInstant(CommandSourceStack source, ServerPlayer target, String caseId) {
        CaseDefinition def = CaseManager.INSTANCE.get(caseId);
        if (def == null || def.items.isEmpty()) {
            source.sendFailure(Component.literal("§cКейс \"" + caseId + "\" не найден или пуст."));
            return 0;
        }
        CaseItem won = CaseManager.INSTANCE.roll(def, new Random());
        if (won.itemId != null && !won.itemId.isEmpty() && !won.itemId.equals("minecraft:air") && !won.itemId.equals("air")) {
        Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(won.itemId));
            if (item != null && item != net.minecraft.world.item.Items.AIR) {
        ItemStack reward = new ItemStack(item, won.count);
                if (!target.getInventory().add(reward)) target.drop(reward, false);
            }
        }
        if (won.command != null && !won.command.isEmpty()) {
            String cmd = won.command.replace("%player%", target.getGameProfile().getName());
            target.getServer().getCommands().performPrefixedCommand(target.getServer().createCommandSourceStack(), cmd);
        }
        long balance = CurrencyManager.INSTANCE.getBalance(target.getUUID());
        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> target),
                new CaseResultS2CPacket(def.id, won.itemId, won.count, won.rarity, won.displayName, balance));
        RecentWinsManager.INSTANCE.addWin(target.getGameProfile().getName(), won.displayName, won.rarity);
        NetworkHandler.broadcastRecentWins(target.getServer());
        source.sendSuccess(() -> Component.literal("§aКейс открыт игроку " + target.getGameProfile().getName()
                + ", приз: " + won.displayName), true);
        return 1;
    }

    private static int giveCase(CommandSourceStack source, ServerPlayer target, String caseId, int amount) {
        CaseDefinition def = CaseManager.INSTANCE.get(caseId);
        if (def == null) {
            source.sendFailure(Component.literal("§cКейс \"" + caseId + "\" не найден."));
            return 0;
        }
        PlayerAccount acc = PlayerAccountManager.INSTANCE.get(target.getUUID());
        acc.addCases(caseId, amount);
        PlayerAccountManager.INSTANCE.save(target.getUUID());
        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> target),
                CaseListSyncS2CPacket.buildForPlayer(target));
        int total = acc.getCaseCount(caseId);
        source.sendSuccess(() -> Component.literal("§aВыдано §f×" + amount + " §a\"" + def.displayName
                + "§a\" игроку " + target.getGameProfile().getName() + " §7(всего: ×" + total + ")"), true);
        target.sendSystemMessage(Component.literal("§aВам выдан кейс: " + def.displayName + " §7(×" + amount
                + ", всего ×" + total + ")"));
        return 1;
    }

    private static int giveCaseAll(CommandSourceStack source, String caseId, int amount) {
        CaseDefinition def = CaseManager.INSTANCE.get(caseId);
        if (def == null) {
            source.sendFailure(Component.literal("§cКейс \"" + caseId + "\" не найден."));
            return 0;
        }
        int n = 0;
        for (ServerPlayer p : source.getServer().getPlayerList().getPlayers()) {
            PlayerAccount acc = PlayerAccountManager.INSTANCE.get(p.getUUID());
            acc.addCases(caseId, amount);
            PlayerAccountManager.INSTANCE.save(p.getUUID());
            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> p),
                    CaseListSyncS2CPacket.buildForPlayer(p));
            p.sendSystemMessage(Component.literal("§aВам выдан кейс: " + def.displayName + " §7(×" + amount + ")"));
            n++;
        }
        int given = n;
        source.sendSuccess(() -> Component.literal("§aВыдано §f×" + amount + " §a\"" + def.displayName
                + "§a\" всем онлайн (" + given + ")"), true);
        return given;
    }

    private static int giveKit(CommandSourceStack source, ServerPlayer target, String kitId) {
        KitDefinition kit = KitManager.INSTANCE.get(kitId);
        if (kit == null) {
            source.sendFailure(Component.literal("§cКит \"" + kitId + "\" не найден."));
            return 0;
        }
        for (KitDefinition.KitItem ki : kit.items) {
            Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(ki.itemId));
            ItemStack stack = new ItemStack(item, ki.count);
            if (!target.getInventory().add(stack)) target.drop(stack, false);
        }
        source.sendSuccess(() -> Component.literal("§aВыдан кит \"" + kit.displayName + "§a\" игроку "
                + target.getGameProfile().getName()), true);
        return 1;
    }

    public static int depositLightmansCoins(ServerPlayer player) {
        Inventory inv = player.getInventory();
        long totalDeposited = 0;

        for (int slot = 0; slot < inv.getContainerSize(); slot++) {
            ItemStack stack = inv.getItem(slot);
            if (stack.isEmpty()) continue;

            ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (key == null || !"lightmanscurrency".equals(key.getNamespace())) continue;

            long coinValue = switch (key.getPath()) {
                case "coin_copper" -> 1L;
                case "coin_iron" -> 10L;
                case "coin_gold" -> 100L;
                case "coin_emerald" -> 1000L;
                case "coin_diamond" -> 10000L;
                case "coin_netherite" -> 100000L;
                default -> 0L;
            };

            if (coinValue > 0) {
                int count = stack.getCount();
                totalDeposited += coinValue * count;
                inv.setItem(slot, ItemStack.EMPTY);
            }
        }

        if (totalDeposited > 0) {
            CurrencyManager.INSTANCE.add(player.getUUID(), totalDeposited);
            long newBal = CurrencyManager.INSTANCE.getBalance(player.getUUID());
            syncBalance(player, newBal);
            final long finalDeposited = totalDeposited;
            player.sendSystemMessage(Component.literal("§aДепозит внесен! Пополнено на §f" + finalDeposited + " 🪙 §7(Баланс: " + newBal + ")"));
        } else {
            player.sendSystemMessage(Component.literal("§cУ вас в инвентаре нет монет Lightman's Currency!"));
        }
        return (int) Math.min(Integer.MAX_VALUE, totalDeposited);
    }

    public static int withdrawLightmansCoins(ServerPlayer player, long amount) {
        long currentBal = CurrencyManager.INSTANCE.getBalance(player.getUUID());
        if (amount <= 0 || currentBal < amount) {
            player.sendSystemMessage(Component.literal("§cНедостаточно средств на балансе! Доступно: §f" + currentBal + " 🪙"));
            return 0;
        }

        CurrencyManager.INSTANCE.add(player.getUUID(), -amount);
        long newBal = CurrencyManager.INSTANCE.getBalance(player.getUUID());
        syncBalance(player, newBal);

        long rem = amount;
        long[] values = {100000L, 10000L, 1000L, 100L, 10L, 1L};
        String[] paths = {"coin_netherite", "coin_diamond", "coin_emerald", "coin_gold", "coin_iron", "coin_copper"};

        for (int i = 0; i < values.length && rem > 0; i++) {
            long val = values[i];
            long count = rem / val;
            if (count > 0) {
                rem %= val;
                Item coinItem = BuiltInRegistries.ITEM.get(new ResourceLocation("lightmanscurrency", paths[i]));
                if (coinItem != null && coinItem != net.minecraft.world.item.Items.AIR) {
                    while (count > 0) {
                        int stackSize = (int) Math.min(64, count);
                        ItemStack coinStack = new ItemStack(coinItem, stackSize);
                        if (!player.getInventory().add(coinStack)) {
                            player.drop(coinStack, false);
                        }
                        count -= stackSize;
                    }
                }
            }
        }

        player.sendSystemMessage(Component.literal("§aВыведено §f" + amount + " 🪙 §aмонетами! §7(Остаток: " + newBal + ")"));
        return 1;
    }

}
