package store.aquateche.aqualumen.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import store.aquateche.aqualumen.AquaLumenUI;

@Mod.EventBusSubscriber(modid = AquaLumenUI.MODID, value = Dist.CLIENT)
public final class LumenAutoJoin {

    private static final int MENU_READY_TICKS = 10;
    private static boolean attempted;
    private static int titleTicks;

    private LumenAutoJoin() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (attempted || event.phase != TickEvent.Phase.END) {
            return;
        }

        String target = System.getProperty("aquatech.autoJoin", "").trim();
        if (target.isEmpty()) {
            attempted = true;
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            attempted = true;
            return;
        }
        if (!(minecraft.screen instanceof TitleScreen titleScreen) || minecraft.getOverlay() != null) {
            titleTicks = 0;
            return;
        }
        if (++titleTicks < MENU_READY_TICKS) {
            return;
        }

        attempted = true;
        AquaLumenUI.LOGGER.info("[AquaLumen] joining {} after client initialization", target);
        ServerData server = new ServerData("AquaTech", target, false);
        ConnectScreen.startConnecting(
                titleScreen, minecraft, ServerAddress.parseString(target), server, false);
    }
}
