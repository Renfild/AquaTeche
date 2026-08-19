package store.aquateche.aqualumen.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
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
        ensureServerInList(minecraft, target);
        AquaLumenUI.LOGGER.info("[AquaLumen] joining {} after client initialization", target);
        ServerData server = new ServerData("AquaTech", target, false);
        ConnectScreen.startConnecting(
                titleScreen, minecraft, ServerAddress.parseString(target), server, false);
    }

    private static void ensureServerInList(Minecraft minecraft, String target) {
        try {
            ServerList serverList = new ServerList(minecraft);
            serverList.load();
            boolean found = false;
            for (int i = 0; i < serverList.size(); i++) {
                ServerData s = serverList.get(i);
                if (s != null && s.ip != null && s.ip.equalsIgnoreCase(target)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                serverList.add(new ServerData("AquaTech", target, false), false);
                serverList.save();
                AquaLumenUI.LOGGER.info("[AquaLumen] auto-added AquaTech ({}) to server list", target);
            }
        } catch (Exception ex) {
            AquaLumenUI.LOGGER.warn("[AquaLumen] failed to save server list: {}", ex.getMessage());
        }
    }
}
