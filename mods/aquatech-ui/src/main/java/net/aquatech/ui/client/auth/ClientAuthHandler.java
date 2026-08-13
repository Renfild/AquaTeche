package net.aquatech.ui.client.auth;

import net.aquatech.ui.network.NetworkHandler;
import net.aquatech.ui.network.packet.C2SAuthPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "aquatech_ui", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientAuthHandler {

    private ClientAuthHandler() {
    }

    @SubscribeEvent
    public static void onClientLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        Minecraft mc = Minecraft.getInstance();
        String nick = mc.getUser() != null ? mc.getUser().getName() : "Player";
        String token = System.getProperty("aquatech.session_token", "").trim();
        NetworkHandler.CHANNEL.sendToServer(new C2SAuthPacket(nick, token));
    }
}
