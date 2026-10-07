package net.aquatech.ui.command;

import com.mojang.brigadier.CommandDispatcher;
import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.fishing.LeyService;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** /aqualey start|stop|status: управление событием Старого Лея для операторов (проверка и ручной запуск). */
@Mod.EventBusSubscriber(modid = AquaTechUI.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class LeyCommand {

    private LeyCommand() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("aqualey")
                .requires(s -> s.hasPermission(2))
                .then(Commands.literal("start").executes(ctx -> {
                    LeyService.forceAppear(ctx.getSource().getPlayerOrException());
                    return 1;
                }))
                .then(Commands.literal("stop").executes(ctx -> {
                    LeyService.forceClose(ctx.getSource().getServer());
                    return 1;
                }))
                .then(Commands.literal("status").executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal("[Старый Лей] " + LeyService.status()), false);
                    return 1;
                })));
    }
}
