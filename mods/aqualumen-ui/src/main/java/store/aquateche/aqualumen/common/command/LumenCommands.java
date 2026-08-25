package store.aquateche.aqualumen.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import store.aquateche.aqualumen.common.service.FishShopConfig;
import store.aquateche.aqualumen.common.service.HubDataService;
import store.aquateche.aqualumen.common.service.MarketService;
import store.aquateche.aqualumen.common.service.KitConfig;
import store.aquateche.aqualumen.common.service.WarpConfig;
import store.aquateche.aqualumen.config.LumenConfig;

import java.util.List;

public final class LumenCommands {

    private LumenCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("aqualumen")
                .executes(ctx -> open(ctx.getSource().getPlayerOrException()))
                .then(Commands.literal("open")
                        .executes(ctx -> open(ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("target", EntityArgument.player())
                                .requires(source -> source.hasPermission(2))
                                .executes(ctx -> open(EntityArgument.getPlayer(ctx, "target")))))
                .then(Commands.literal("refresh")
                        .executes(ctx -> {
                            HubDataService.push(ctx.getSource().getPlayerOrException());
                            return 1;
                        }))
                .then(Commands.literal("status")
                        .requires(source -> source.hasPermission(2))
                        .executes(ctx -> {
                            ctx.getSource().sendSuccess(() -> Component.literal(HubDataService.status()), false);
                            return 1;
                        }))
                .then(Commands.literal("reload")
                        .requires(source -> source.hasPermission(2))
                        .executes(ctx -> reloadAll(ctx.getSource())))
                .then(buildKitCommands())
                .then(buildWarpCommands())
                .then(buildFishCommands());

        dispatcher.register(root);

        LiteralArgumentBuilder<CommandSourceStack> ah = Commands.literal("ah")
                .then(Commands.literal("sell")
                        .then(Commands.argument("price", LongArgumentType.longArg(1))
                                .executes(ctx -> {
                                    MarketService.sell(ctx.getSource().getPlayerOrException(),
                                            LongArgumentType.getLong(ctx, "price"));
                                    return 1;
                                })))
                .then(Commands.literal("cancel")
                        .then(Commands.argument("id", IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    MarketService.cancel(ctx.getSource().getPlayerOrException(),
                                            IntegerArgumentType.getInteger(ctx, "id"));
                                    return 1;
                                })))
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal(
                            "§b[Рынок] Держите предмет в руке: /ah sell <цена> · отмена: /ah cancel <id>"), false);
                    return 1;
                });
        dispatcher.register(ah);

        // Register /hub with the same capabilities
        if (LumenConfig.COMMON.hubEnabled.get()) {
            if (dispatcher.getRoot().getChild("hub") == null) {
                LiteralArgumentBuilder<CommandSourceStack> hubRoot = Commands.literal("hub")
                        .executes(ctx -> open(ctx.getSource().getPlayerOrException()))
                        .then(Commands.literal("reload")
                                .requires(source -> source.hasPermission(2))
                                .executes(ctx -> reloadAll(ctx.getSource())))
                        .then(buildKitCommands())
                        .then(buildWarpCommands())
                        .then(buildFishCommands());
                dispatcher.register(hubRoot);
            }
            if (dispatcher.getRoot().getChild("menu") == null) {
                dispatcher.register(Commands.literal("menu")
                        .executes(ctx -> open(ctx.getSource().getPlayerOrException())));
            }
            if (dispatcher.getRoot().getChild("kit") == null) {
                dispatcher.register(Commands.literal("kit")
                        .executes(ctx -> {
                            KitConfig.grantKit(ctx.getSource().getPlayerOrException(), "start");
                            return 1;
                        })
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(ctx -> {
                                    KitConfig.grantKit(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "id"));
                                    return 1;
                                })));
            }
            if (dispatcher.getRoot().getChild("kits") == null) {
                dispatcher.register(Commands.literal("kits")
                        .executes(ctx -> open(ctx.getSource().getPlayerOrException())));
            }
        }
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildKitCommands() {
        return Commands.literal("kit")
                .executes(ctx -> {
                    KitConfig.grantKit(ctx.getSource().getPlayerOrException(), "start");
                    return 1;
                })
                .then(Commands.literal("list")
                        .executes(ctx -> {
                            CommandSourceStack src = ctx.getSource();
                            src.sendSuccess(() -> Component.literal("\u00a7b[AquaTech Kits] \u00a77\u0421\u043f\u0438\u0441\u043e\u043a \u043d\u0430\u0431\u043e\u0440\u043e\u0432:"), false);
                            for (KitConfig.KitDef k : KitConfig.get().kits) {
                                src.sendSuccess(() -> Component.literal("\u00a7e• \u00a76" + k.title + " \u00a77(id: \u00a7f" + k.id + "\u00a77, \u043f\u043b\u0430\u0448\u043a\u0430: \u00a7a" + k.badge + "\u00a77) -> \u00a7f/kit " + k.id), false);
                            }
                            return 1;
                        }))
                .then(Commands.literal("claim")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(ctx -> {
                                    KitConfig.grantKit(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "id"));
                                    return 1;
                                })))
                .then(Commands.literal("set")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .then(Commands.argument("title", StringArgumentType.string())
                                        .then(Commands.argument("badge", StringArgumentType.string())
                                                .then(Commands.argument("command", StringArgumentType.greedyString())
                                                        .executes(ctx -> {
                                                            String id = StringArgumentType.getString(ctx, "id");
                                                            String title = StringArgumentType.getString(ctx, "title");
                                                            String badge = StringArgumentType.getString(ctx, "badge");
                                                            String command = StringArgumentType.getString(ctx, "command");

                                                            KitConfig.addOrUpdate(new KitConfig.KitDef(id, title, "\u041d\u0430\u0431\u043e\u0440 " + title, badge, 0L, List.of(), List.of(command)));
                                                            refreshAll(ctx.getSource().getServer());
                                                            ctx.getSource().sendSuccess(() -> Component.literal("\u00a7a[AquaTech] \u041d\u0430\u0431\u043e\u0440 \u00a7e" + id + " \u00a7a\u0443\u0441\u043f\u0435\u0448\u043d\u043e \u0441\u043e\u0445\u0440\u0430\u043d\u0451\u043d \u0438 \u0434\u043e\u0431\u0430\u0432\u043b\u0435\u043d \u0432 \u043c\u0435\u043d\u044e!"), true);
                                                            return 1;
                                                        }))))))
                .then(Commands.literal("remove")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(ctx -> {
                                    String id = StringArgumentType.getString(ctx, "id");
                                    boolean ok = KitConfig.remove(id);
                                    if (ok) {
                                        refreshAll(ctx.getSource().getServer());
                                        ctx.getSource().sendSuccess(() -> Component.literal("\u00a7a[AquaTech] \u041d\u0430\u0431\u043e\u0440 \u00a7e" + id + " \u00a7a\u0443\u0434\u0430\u043b\u0451\u043d!"), true);
                                    } else {
                                        ctx.getSource().sendFailure(Component.literal("\u00a7c[AquaTech] \u041d\u0430\u0431\u043e\u0440 \u00a7e" + id + " \u00a7c\u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d."));
                                    }
                                    return 1;
                                })));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildWarpCommands() {
        return Commands.literal("warp")
                .then(Commands.literal("list")
                        .executes(ctx -> {
                            CommandSourceStack src = ctx.getSource();
                            src.sendSuccess(() -> Component.literal("\u00a7b[AquaTech Warps] \u00a77\u0421\u043f\u0438\u0441\u043e\u043a \u0432\u0430\u0440\u043f\u043e\u0432:"), false);
                            for (WarpConfig.WarpDef w : WarpConfig.get().warps) {
                                src.sendSuccess(() -> Component.literal("\u00a7e• \u00a76" + w.title + " \u00a77(id: \u00a7f" + w.id + "\u00a77, \u0442\u0435\u0433: \u00a7b[" + w.tag + "]\u00a77) -> \u00a7f/" + w.command), false);
                            }
                            return 1;
                        }))
                .then(Commands.literal("set")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .then(Commands.argument("title", StringArgumentType.string())
                                        .then(Commands.argument("tag", StringArgumentType.string())
                                                .then(Commands.argument("command", StringArgumentType.greedyString())
                                                        .executes(ctx -> {
                                                            String id = StringArgumentType.getString(ctx, "id");
                                                            String title = StringArgumentType.getString(ctx, "title");
                                                            String tag = StringArgumentType.getString(ctx, "tag");
                                                            String command = StringArgumentType.getString(ctx, "command");

                                                            WarpConfig.addOrUpdate(new WarpConfig.WarpDef(id, title, "\u0422\u043e\u0447\u043a\u0430 " + title, tag, command));
                                                            refreshAll(ctx.getSource().getServer());
                                                            ctx.getSource().sendSuccess(() -> Component.literal("\u00a7a[AquaTech] \u0412\u0430\u0440\u043f \u00a7e" + id + " \u00a7a\u0443\u0441\u043f\u0435\u0448\u043d\u043e \u0441\u043e\u0445\u0440\u0430\u043d\u0451\u043d \u0438 \u0434\u043e\u0431\u0430\u0432\u043b\u0435\u043d \u0432 \u043c\u0435\u043d\u044e!"), true);
                                                            return 1;
                                                        }))))))
                .then(Commands.literal("remove")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(ctx -> {
                                    String id = StringArgumentType.getString(ctx, "id");
                                    boolean ok = WarpConfig.remove(id);
                                    if (ok) {
                                        refreshAll(ctx.getSource().getServer());
                                        ctx.getSource().sendSuccess(() -> Component.literal("\u00a7a[AquaTech] \u0412\u0430\u0440\u043f \u00a7e" + id + " \u00a7a\u0443\u0434\u0430\u043b\u0451\u043d!"), true);
                                    } else {
                                        ctx.getSource().sendFailure(Component.literal("\u00a7c[AquaTech] \u0412\u0430\u0440\u043f \u00a7e" + id + " \u00a7c\u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d."));
                                    }
                                    return 1;
                                })));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildFishCommands() {
        return Commands.literal("fish")
                .then(Commands.literal("list")
                        .executes(ctx -> {
                            CommandSourceStack src = ctx.getSource();
                            src.sendSuccess(() -> Component.literal("\u00a7b[AquaTech \u0420\u044b\u0431\u0430\u043b\u043a\u0430] \u00a77\u0421\u043f\u0438\u0441\u043e\u043a \u0441\u043a\u0443\u043f\u043a\u0438 \u0440\u044b\u0431\u044b:"), false);
                            for (FishShopConfig.FishDef f : FishShopConfig.get().fishes) {
                                src.sendSuccess(() -> Component.literal("\u00a7e• \u00a7f" + f.name + " \u00a77(" + f.id + ") \u00a77\u2014 \u0411\u0430\u0437\u0430: \u00a76" + f.priceCoins + " \u043c\u043e\u043d. \u00a77| \u0420\u0435\u0434\u043a\u043e\u0441\u0442\u044c: \u00a7a" + f.rarity), false);
                            }
                            return 1;
                        }))
                .then(Commands.literal("sellall")
                        .executes(ctx -> {
                            FishShopConfig.sellAll(ctx.getSource().getPlayerOrException());
                            return 1;
                        }))
                .then(Commands.literal("set")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .then(Commands.argument("price", IntegerArgumentType.integer(1))
                                        .then(Commands.argument("rarity", StringArgumentType.word())
                                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                                        .executes(ctx -> {
                                                            String id = StringArgumentType.getString(ctx, "id");
                                                            int price = IntegerArgumentType.getInteger(ctx, "price");
                                                            String rarity = StringArgumentType.getString(ctx, "rarity");
                                                            String name = StringArgumentType.getString(ctx, "name");

                                                            String tag = id.contains(":") ? id.split(":")[0] : "custom";
                                                            FishShopConfig.addOrUpdate(new FishShopConfig.FishDef(id, name, price, rarity, tag));
                                                            refreshAll(ctx.getSource().getServer());
                                                            ctx.getSource().sendSuccess(() -> Component.literal("\u00a7a[AquaTech] \u0420\u044b\u0431\u0430 \u00a7e" + id + " \u00a7a\u0441\u043e\u0445\u0440\u0430\u043d\u0435\u043d\u0430 \u0441 \u0431\u0430\u0437\u043e\u0432\u043e\u0439 \u0446\u0435\u043d\u043e\u0439 \u00a76" + price + " \u043c\u043e\u043d\u0435\u0442\u00a7a!"), true);
                                                            return 1;
                                                        }))))))
                .then(Commands.literal("remove")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(ctx -> {
                                    String id = StringArgumentType.getString(ctx, "id");
                                    boolean ok = FishShopConfig.remove(id);
                                    if (ok) {
                                        refreshAll(ctx.getSource().getServer());
                                        ctx.getSource().sendSuccess(() -> Component.literal("\u00a7a[AquaTech] \u0420\u044b\u0431\u0430 \u00a7e" + id + " \u00a7a\u0443\u0434\u0430\u043b\u0435\u043d\u0430 \u0438\u0437 \u0441\u043a\u0443\u043f\u043a\u0438!"), true);
                                    } else {
                                        ctx.getSource().sendFailure(Component.literal("\u00a7c[AquaTech] \u0420\u044b\u0431\u0430 \u00a7e" + id + " \u00a7c\u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u0430."));
                                    }
                                    return 1;
                                })));
    }

    private static int reloadAll(CommandSourceStack source) {
        KitConfig.reload();
        WarpConfig.reload();
        FishShopConfig.reload();
        HubDataService.invalidate();
        refreshAll(source.getServer());
        source.sendSuccess(() -> Component.literal("\u00a7a[AquaTech] \u041a\u043e\u043d\u0444\u0438\u0433\u0443\u0440\u0430\u0446\u0438\u0438 \u043a\u0438\u0442\u043e\u0432, \u0432\u0430\u0440\u043f\u043e\u0432 \u0438 \u0441\u043a\u0443\u043f\u043a\u0438 \u0440\u044b\u0431\u044b \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u043f\u0435\u0440\u0435\u0437\u0430\u0433\u0440\u0443\u0436\u0435\u043d\u044b!"), true);
        return 1;
    }

    private static void refreshAll(MinecraftServer server) {
        if (server != null) {
            HubDataService.refreshOpenHubs(server);
        }
    }

    private static int open(ServerPlayer player) {
        HubDataService.open(player);
        return 1;
    }
}
