package store.aquateche.aqualumen.common.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import store.aquateche.aqualumen.common.service.HubDataService;
import store.aquateche.aqualumen.config.LumenConfig;

public final class LumenCommands {

    private LumenCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("aqualumen")
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
                        .requires(source -> source.hasPermission(3))
                        .executes(ctx -> {
                            HubDataService.invalidate();
                            ctx.getSource().sendSuccess(() -> Component.translatable("msg.aqualumen.reloaded"), true);
                            return 1;
                        })));

        // Aliases for players to easily open the menu
        if (LumenConfig.COMMON.hubEnabled.get()) {
            if (dispatcher.getRoot().getChild("hub") == null) {
                dispatcher.register(Commands.literal("hub")
                        .executes(ctx -> open(ctx.getSource().getPlayerOrException())));
            }
            if (dispatcher.getRoot().getChild("menu") == null) {
                dispatcher.register(Commands.literal("menu")
                        .executes(ctx -> open(ctx.getSource().getPlayerOrException())));
            }
            if (dispatcher.getRoot().getChild("m") == null) {
                dispatcher.register(Commands.literal("m")
                        .executes(ctx -> open(ctx.getSource().getPlayerOrException())));
            }
        }
    }

    private static int open(ServerPlayer player) {
        HubDataService.open(player);
        return 1;
    }
}
