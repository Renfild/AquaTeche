package store.aquateche.aqualumen.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import store.aquateche.aqualumen.AquaLumenUI;

public final class LumenNetwork {

    private static final String PROTOCOL = "2";

    /**
     * Accepting absent and vanilla versions is what keeps a Mohist server joinable for players
     * without the mod. Those players simply get the chest fallback.
     */
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(AquaLumenUI.id("hub"))
            .networkProtocolVersion(() -> PROTOCOL)
            .clientAcceptedVersions(version -> true)
            .serverAcceptedVersions(version -> true)
            .simpleChannel();

    private static int nextId;

    private LumenNetwork() {
    }

    public static void register() {
        CHANNEL.messageBuilder(LumenPackets.ClientHello.class, nextId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(LumenPackets.ClientHello::encode)
                .decoder(LumenPackets.ClientHello::new)
                .consumerMainThread(LumenPackets.ClientHello::handle)
                .add();

        CHANNEL.messageBuilder(LumenPackets.HubAction.class, nextId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(LumenPackets.HubAction::encode)
                .decoder(LumenPackets.HubAction::new)
                .consumerMainThread(LumenPackets.HubAction::handle)
                .add();

        CHANNEL.messageBuilder(LumenPackets.HubSync.class, nextId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(LumenPackets.HubSync::encode)
                .decoder(LumenPackets.HubSync::new)
                .consumerMainThread(LumenPackets.HubSync::handle)
                .add();

        AquaLumenUI.LOGGER.debug("[AquaLumen UI] registered {} packets", nextId);
    }

    public static void toPlayer(ServerPlayer player, Object packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void toServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }
}
