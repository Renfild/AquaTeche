package net.aquatech.ui.client;

import net.aquatech.ui.AquaTechUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Ensures the official AquaTech server is always present and active in the Minecraft Multiplayer server list.
 */
@Mod.EventBusSubscriber(modid = AquaTechUI.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class AquaServerListHandler {

    public static final String DEFAULT_SERVER_NAME = "AquaTech";
    public static final String DEFAULT_SERVER_IP = "g-pl-3.apexnodes.xyz:21561";

    private static boolean initialized = false;

    private AquaServerListHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || initialized) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            return;
        }
        initialized = true;
        ensureServerInList(mc);
    }

    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening event) {
        if (event.getNewScreen() instanceof JoinMultiplayerScreen || event.getNewScreen() instanceof TitleScreen) {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) {
                ensureServerInList(mc);
            }
        }
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof JoinMultiplayerScreen joinScreen) {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) {
                ensureServerInList(mc);
                try {
                    ServerList serverList = joinScreen.getServers();
                    if (serverList != null) {
                        serverList.load();
                        ServerSelectionList selectionList = null;
                        for (var child : joinScreen.children()) {
                            if (child instanceof ServerSelectionList ssl) {
                                selectionList = ssl;
                                break;
                            }
                        }
                        if (selectionList != null) {
                            selectionList.updateOnlineServers(serverList);
                        }
                    }
                } catch (Throwable ignored) {
                }
            }
        }
    }

    public static void ensureServerInList(Minecraft mc) {
        try {
            ServerList serverList = new ServerList(mc);
            serverList.load();

            boolean found = false;
            for (int i = 0; i < serverList.size(); i++) {
                ServerData entry = serverList.get(i);
                if (entry == null) continue;
                String ip = entry.ip != null ? entry.ip.trim() : "";
                String name = entry.name != null ? entry.name.trim() : "";

                if (ip.equalsIgnoreCase(DEFAULT_SERVER_IP)
                        || ip.equalsIgnoreCase("aquateche.store")
                        || ip.equalsIgnoreCase("aquatech.santcrail.workers.dev")
                        || ip.contains("apexnodes.xyz")
                        || ip.equalsIgnoreCase("localhost:21561")
                        || name.equalsIgnoreCase(DEFAULT_SERVER_NAME)) {
                    entry.name = DEFAULT_SERVER_NAME;
                    entry.ip = DEFAULT_SERVER_IP;
                    found = true;
                    break;
                }
            }

            if (!found) {
                ServerData aquaServer = new ServerData(DEFAULT_SERVER_NAME, DEFAULT_SERVER_IP, false);
                serverList.add(aquaServer, false);
            }

            serverList.save();
        } catch (Throwable t) {
            AquaTechUI.LOGGER.warn("[AquaTechUI] Could not ensure default server in server list: {}", t.getMessage());
        }
    }
}
