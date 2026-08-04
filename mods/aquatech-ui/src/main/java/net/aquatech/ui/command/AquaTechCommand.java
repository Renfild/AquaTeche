package net.aquatech.ui.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.aquatech.ui.capability.AquaSkillCapability;
import net.aquatech.ui.horizon.HorizonRoute;
import net.aquatech.ui.horizon.StormEvent;
import net.aquatech.ui.network.NetworkHandler;
import net.aquatech.ui.network.S2CSyncSkillsPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

/**
 * /aquatech — quest bridge + Horizon Route player commands.
 */
@Mod.EventBusSubscriber(modid = net.aquatech.ui.AquaTechUI.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AquaTechCommand {

    private AquaTechCommand() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("aquatech")
                .then(Commands.literal("grantxp")
                        .requires(s -> s.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(AquaTechCommand::grantXp))))
                .then(Commands.literal("settier")
                        .requires(s -> s.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("targetRank", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            builder.suggest("0"); builder.suggest("1"); builder.suggest("2");
                                            builder.suggest("3"); builder.suggest("4"); builder.suggest("5");
                                            for (String name : HorizonRoute.TIER_NAMES) {
                                                builder.suggest(name.toLowerCase(java.util.Locale.ROOT));
                                            }
                                            builder.suggest("admin"); builder.suggest("owner");
                                            builder.suggest("mod"); builder.suggest("dev");
                                            return builder.buildFuture();
                                        })
                                        .executes(AquaTechCommand::setTierUnified))))
                .then(Commands.literal("promote")
                        .requires(s -> s.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("targetRank", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            builder.suggest("1"); builder.suggest("2"); builder.suggest("3");
                                            builder.suggest("4"); builder.suggest("5");
                                            for (String name : HorizonRoute.TIER_NAMES) {
                                                if (!name.equals("Пролог")) builder.suggest(name.toLowerCase(java.util.Locale.ROOT));
                                            }
                                            builder.suggest("admin"); builder.suggest("owner");
                                            builder.suggest("mod"); builder.suggest("dev");
                                            return builder.buildFuture();
                                        })
                                        .executes(AquaTechCommand::promoteUnified))))
                .then(Commands.literal("storm")
                        .requires(s -> s.hasPermission(2))
                        .then(Commands.literal("on").executes(ctx -> setStorm(ctx, true)))
                        .then(Commands.literal("off").executes(ctx -> setStorm(ctx, false)))
                        .then(Commands.literal("auto").executes(AquaTechCommand::stormAuto))
                        .then(Commands.literal("status").executes(AquaTechCommand::stormStatus)))
                // player-facing
                .then(Commands.literal("daily")
                        .executes(AquaTechCommand::dailySelf))
                .then(Commands.literal("season")
                        .executes(AquaTechCommand::seasonSelf))
                .then(Commands.literal("horizon")
                        .executes(AquaTechCommand::horizonSelf))
        );
    }

    private static void sync(ServerPlayer player, AquaSkillCapability cap) {
        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new S2CSyncSkillsPacket(cap));
    }

    private static long dayKey(ServerPlayer player) {
        return player.level().getDayTime() / 24000L;
    }

    private static int grantXp(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
            int amount = IntegerArgumentType.getInteger(ctx, "amount");
            target.getCapability(AquaSkillCapability.INSTANCE).ifPresent(cap -> {
                boolean leveledUp = cap.addXp(amount);
                sync(target, cap);
                target.displayClientMessage(Component.literal(
                        leveledUp ? "§b+" + amount + " Aqua XP §7(новый уровень!)" : "§b+" + amount + " Aqua XP"), true);
            });
            ctx.getSource().sendSuccess(() -> Component.literal("Выдано " + amount + " Aqua XP → " + target.getGameProfile().getName()), true);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Ошибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int setTier(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
            int tier = IntegerArgumentType.getInteger(ctx, "tier");
            target.getCapability(AquaSkillCapability.INSTANCE).ifPresent(cap -> {
                int old = cap.getHorizonTier();
                cap.forceHorizonTier(tier);
                sync(target, cap);
                applyLuckPerms(target, tier);
                target.displayClientMessage(Component.literal(
                        "§bГоризонт: §f" + HorizonRoute.tierName(old) + " §8→ §a" + HorizonRoute.tierName(tier)), false);
            });
            ctx.getSource().sendSuccess(() -> Component.literal("Tier " + tier + " → " + target.getGameProfile().getName()), true);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Ошибка: " + e.getMessage()));
            return 0;
        }
    }

    /** Promote if higher; used by FTB command rewards. */
    public static void promoteTier(ServerPlayer player, int tier) {
        player.getCapability(AquaSkillCapability.INSTANCE).ifPresent(cap -> {
            if (cap.setHorizonTier(tier)) {
                sync(player, cap);
                applyLuckPerms(player, tier);
                player.displayClientMessage(Component.literal(
                        "§6✦ Горизонт " + tier + ": §e" + HorizonRoute.tierName(tier) + "§6!"), false);
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        net.minecraft.sounds.SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.8F, 1.0F);
            }
        });
    }

    /**
     * Clears sticky fleet ranks then applies the target Horizon group.
     * H0 → remove fleet groups only (stay on default).
     */
    private static void applyLuckPerms(ServerPlayer player, int tier) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        String name = player.getGameProfile().getName();
        var src = server.createCommandSourceStack();
        for (String fleet : HorizonRoute.FLEET_LP_GROUPS) {
            server.getCommands().performPrefixedCommand(src,
                    "lp user " + name + " parent remove " + fleet);
        }
        String group = HorizonRoute.lpGroup(tier);
        if ("default".equals(group)) {
            server.getCommands().performPrefixedCommand(src,
                    "lp user " + name + " parent switchprimarygroup default");
            return;
        }
        server.getCommands().performPrefixedCommand(src,
                "lp user " + name + " parent add " + group);
        server.getCommands().performPrefixedCommand(src,
                "lp user " + name + " parent switchprimarygroup " + group);
    }

    private static int promoteUnified(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
            String rawRank = StringArgumentType.getString(ctx, "targetRank");
            int tier = parseTier(rawRank);
            if (tier < 0) {
                ctx.getSource().sendFailure(Component.literal(
                        "Неизвестный ранг: '" + rawRank + "'. Используй: матрос, шкипер, капитан, адмирал, легенда (или 1-5)"));
                return 0;
            }
            promoteTier(target, tier);
            ctx.getSource().sendSuccess(() -> Component.literal(
                    "Promote H" + tier + " (" + HorizonRoute.tierName(tier) + ") → " + target.getGameProfile().getName()), true);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Ошибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int setTierUnified(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
            String rawRank = StringArgumentType.getString(ctx, "targetRank");
            int tier = parseTier(rawRank);
            if (tier < 0) {
                ctx.getSource().sendFailure(Component.literal(
                        "Неизвестный ранг: '" + rawRank + "'. Используй: пролог, матрос, шкипер, капитан, адмирал, легенда (или 0-5)"));
                return 0;
            }
            target.getCapability(AquaSkillCapability.INSTANCE).ifPresent(cap -> {
                int old = cap.getHorizonTier();
                cap.forceHorizonTier(tier);
                sync(target, cap);
                applyLuckPerms(target, tier);
                target.displayClientMessage(Component.literal(
                        "§bГоризонт: §f" + HorizonRoute.tierName(old) + " §8→ §a" + HorizonRoute.tierName(tier)), false);
            });
            ctx.getSource().sendSuccess(() -> Component.literal(
                    "SetTier H" + tier + " (" + HorizonRoute.tierName(tier) + ") → " + target.getGameProfile().getName()), true);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Ошибка: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * Converts a rank name string (in any case, Russian or English) to a tier int (0-5).
     * Returns -1 if unrecognized.
     */
    private static int parseTier(String input) {
        if (input == null || input.isBlank()) return -1;
        String s = input.trim().toLowerCase(java.util.Locale.ROOT);
        return switch (s) {
            case "0", "пролог", "prolog", "none" -> 0;
            case "1", "матрос", "sailor" -> 1;
            case "2", "шкипер", "skipper" -> 2;
            case "3", "капитан", "captain", "mod", "moderator", "модератор" -> 3;
            case "4", "адмирал", "admiral", "admin", "админ", "администратор", "dev", "developer", "разработчик" -> 4;
            case "5", "легенда", "legend", "owner", "владелец" -> 5;
            default -> {
                try { yield Integer.parseInt(s); } catch (NumberFormatException ignored) {}
                yield -1;
            }
        };
    }

    private static int setStorm(CommandContext<CommandSourceStack> ctx, boolean on) {
        StormEvent.setForce(on);
        ctx.getSource().sendSuccess(() -> Component.literal(StormEvent.statusLine()), true);
        return 1;
    }

    private static int stormAuto(CommandContext<CommandSourceStack> ctx) {
        StormEvent.setAuto();
        ctx.getSource().sendSuccess(() -> Component.literal(StormEvent.statusLine()), true);
        return 1;
    }

    private static int stormStatus(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() -> Component.literal(StormEvent.statusLine()), false);
        return 1;
    }

    private static int dailySelf(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            ctx.getSource().sendFailure(Component.literal("Только для игроков"));
            return 0;
        }
        player.getCapability(AquaSkillCapability.INSTANCE).ifPresent(cap -> {
            cap.ensureDaily(dayKey(player));
            HorizonRoute.DailyContract c = cap.currentContract();
            if (cap.isDailyClaimed()) {
                player.displayClientMessage(Component.literal("§8Контракт дня уже сдан. Завтра — новый."), false);
                sync(player, cap);
                return;
            }
            if (cap.claimDaily()) {
                player.displayClientMessage(Component.literal(
                        "§a✓ Контракт сдан! §b+" + HorizonRoute.DAILY_AQUA_XP
                                + " Aqua XP §7и §b+" + HorizonRoute.DAILY_SEASON_XP + " XP сезона"), false);
                sync(player, cap);
                return;
            }
            player.displayClientMessage(Component.literal(
                    "§bКонтракт дня: §f" + c.title), false);
            player.displayClientMessage(Component.literal(
                    "§7" + c.description), false);
            player.displayClientMessage(Component.literal(
                    "§fПрогресс: §a" + cap.getDailyProgress() + "§7/§a" + c.target
                            + (cap.isDailyComplete() ? " §e— напиши /aquatech daily снова, чтобы сдать" : "")), false);
            sync(player, cap);
        });
        return 1;
    }

    private static int seasonSelf(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            ctx.getSource().sendFailure(Component.literal("Только для игроков"));
            return 0;
        }
        player.getCapability(AquaSkillCapability.INSTANCE).ifPresent(cap -> {
            int lvl = cap.getSeasonLevel();
            int into = cap.getSeasonXp() % HorizonRoute.SEASON_XP_PER_LEVEL;
            player.displayClientMessage(Component.literal(
                    "§9Сезон Горизонта §7· ур. §f" + lvl + "§7/§f" + HorizonRoute.SEASON_MAX_LEVEL), false);
            player.displayClientMessage(Component.literal(
                    "§7XP сезона: §f" + into + "§7/§f" + HorizonRoute.SEASON_XP_PER_LEVEL), false);
            if (StormEvent.isActive()) {
                player.displayClientMessage(Component.literal("§9⚡ Шторм выходного дня активен"), false);
            }
        });
        return 1;
    }

    private static int horizonSelf(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            ctx.getSource().sendFailure(Component.literal("Только для игроков"));
            return 0;
        }
        player.getCapability(AquaSkillCapability.INSTANCE).ifPresent(cap -> {
            int t = cap.getHorizonTier();
            player.displayClientMessage(Component.literal(
                    "§bГоризонт §f" + t + "§7/§f" + HorizonRoute.MAX_TIER
                            + " §8— §e" + HorizonRoute.tierName(t)), false);
            player.displayClientMessage(Component.literal(
                    "§7Открой FTB Quests → «Маршрут Горизонта» для вех."), false);
            player.displayClientMessage(Component.literal(
                    "§8Команды: /aquatech daily · season · horizon"), false);
        });
        return 1;
    }
}
