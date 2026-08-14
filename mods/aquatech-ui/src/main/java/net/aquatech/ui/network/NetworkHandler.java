package net.aquatech.ui.network;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.network.packet.ChatBubblePacket;
import net.aquatech.ui.network.packet.SyncAllProfilesPacket;
import net.aquatech.ui.network.packet.SyncServerStatsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkHandler {

    private static final String PROTOCOL_VERSION = "6";
    /** Mohist reports isAcceptingMessages=true too early — gate custom S2C by join tick. */
    public static final int LOGIN_READY_DELAY_TICKS = 10;
    private static final Map<UUID, Integer> JOIN_TICK = new ConcurrentHashMap<>();

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(AquaTechUI.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            version -> true,
            version -> true
    );

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        // HUD / TAB profile sync (S2C)
        CHANNEL.messageBuilder(SyncAllProfilesPacket.class, id())
                .encoder(SyncAllProfilesPacket::encode)
                .decoder(SyncAllProfilesPacket::decode)
                .consumerMainThread(SyncAllProfilesPacket::handle)
                .add();

        CHANNEL.messageBuilder(SyncServerStatsPacket.class, id())
                .encoder(SyncServerStatsPacket::encode)
                .decoder(SyncServerStatsPacket::decode)
                .consumerMainThread(SyncServerStatsPacket::handle)
                .add();

        CHANNEL.messageBuilder(ChatBubblePacket.class, id())
                .encoder(ChatBubblePacket::encode)
                .decoder(ChatBubblePacket::decode)
                .consumerMainThread(ChatBubblePacket::handle)
                .add();

        // Skill tree packets
        CHANNEL.messageBuilder(S2CSyncSkillsPacket.class, id())
                .encoder(S2CSyncSkillsPacket::toBytes)
                .decoder(S2CSyncSkillsPacket::new)
                .consumerMainThread(S2CSyncSkillsPacket::handle)
                .add();

        CHANNEL.messageBuilder(C2SUnlockSkillPacket.class, id())
                .encoder(C2SUnlockSkillPacket::toBytes)
                .decoder(C2SUnlockSkillPacket::new)
                .consumerMainThread(C2SUnlockSkillPacket::handle)
                .add();

        CHANNEL.messageBuilder(C2SOpenSkillTreePacket.class, id())
                .encoder(C2SOpenSkillTreePacket::toBytes)
                .decoder(C2SOpenSkillTreePacket::new)
                .consumerMainThread(C2SOpenSkillTreePacket::handle)
                .add();

        CHANNEL.messageBuilder(S2CStartRhythmHookPacket.class, id())
                .encoder(S2CStartRhythmHookPacket::toBytes)
                .decoder(S2CStartRhythmHookPacket::new)
                .consumerMainThread(S2CStartRhythmHookPacket::handle)
                .add();

        CHANNEL.messageBuilder(C2SRhythmHookResultPacket.class, id())
                .encoder(C2SRhythmHookResultPacket::toBytes)
                .decoder(C2SRhythmHookResultPacket::new)
                .consumerMainThread(C2SRhythmHookResultPacket::handle)
                .add();

        CHANNEL.messageBuilder(net.aquatech.ui.network.packet.C2SAuthPacket.class, id())
                .encoder(net.aquatech.ui.network.packet.C2SAuthPacket::encode)
                .decoder(net.aquatech.ui.network.packet.C2SAuthPacket::new)
                .consumerMainThread(net.aquatech.ui.network.packet.C2SAuthPacket::handle)
                .add();

        CHANNEL.messageBuilder(net.aquatech.ui.network.packet.C2SOpenContainerPacket.class, id())
                .encoder(net.aquatech.ui.network.packet.C2SOpenContainerPacket::encode)
                .decoder(net.aquatech.ui.network.packet.C2SOpenContainerPacket::new)
                .consumerMainThread(net.aquatech.ui.network.packet.C2SOpenContainerPacket::handle)
                .add();

        CHANNEL.messageBuilder(net.aquatech.ui.network.packet.S2CSyncLimitersPacket.class, id())
                .encoder(net.aquatech.ui.network.packet.S2CSyncLimitersPacket::encode)
                .decoder(net.aquatech.ui.network.packet.S2CSyncLimitersPacket::new)
                .consumerMainThread(net.aquatech.ui.network.packet.S2CSyncLimitersPacket::handle)
                .add();

        CHANNEL.messageBuilder(net.aquatech.ui.network.packet.S2COpenContainerPacket.class, id())
                .encoder(net.aquatech.ui.network.packet.S2COpenContainerPacket::encode)
                .decoder(net.aquatech.ui.network.packet.S2COpenContainerPacket::new)
                .consumerMainThread(net.aquatech.ui.network.packet.S2COpenContainerPacket::handle)
                .add();

        CHANNEL.messageBuilder(net.aquatech.ui.network.packet.S2CSessionSyncPacket.class, id())
                .encoder(net.aquatech.ui.network.packet.S2CSessionSyncPacket::encode)
                .decoder(net.aquatech.ui.network.packet.S2CSessionSyncPacket::new)
                .consumerMainThread(net.aquatech.ui.network.packet.S2CSessionSyncPacket::handle)
                .add();
    }

    public static void markJoined(ServerPlayer player) {
        if (player == null) return;
        MinecraftServer server = player.getServer();
        int tick = server != null ? server.getTickCount() : 0;
        JOIN_TICK.put(player.getUUID(), tick);
    }

    public static void markLeft(UUID uuid) {
        if (uuid != null) JOIN_TICK.remove(uuid);
    }

    public static void sendToAll(Object packet) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
    }

    /** Skip players still finishing the login/config phase — early S2C here crashes clients (Index 192 / length 3). */
    public static boolean canReceivePlayPackets(ServerPlayer player) {
        if (player == null || player.hasDisconnected() || player.connection == null) return false;
        if (!player.connection.isAcceptingMessages()) return false;
        Integer joinedAt = JOIN_TICK.get(player.getUUID());
        if (joinedAt == null) return false;
        MinecraftServer server = player.getServer();
        if (server == null) return false;
        return server.getTickCount() - joinedAt >= LOGIN_READY_DELAY_TICKS;
    }

    public static void sendToPlayReady(Object packet, Iterable<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            if (canReceivePlayPackets(player)) {
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
            }
        }
    }

    public static void sendToPlayerWhenReady(Object packet, ServerPlayer player) {
        if (canReceivePlayPackets(player)) {
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
        }
    }
}
