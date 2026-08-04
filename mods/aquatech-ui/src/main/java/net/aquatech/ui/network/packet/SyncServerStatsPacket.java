package net.aquatech.ui.network.packet;

import net.aquatech.ui.client.ClientUiState;
import net.aquatech.ui.common.ServerStats;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SyncServerStatsPacket(ServerStats stats) {
    public static void encode(SyncServerStatsPacket packet, FriendlyByteBuf buf) {
        packet.stats.write(buf);
    }

    public static SyncServerStatsPacket decode(FriendlyByteBuf buf) {
        return new SyncServerStatsPacket(ServerStats.read(buf));
    }

    public static void handle(SyncServerStatsPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientUiState.updateStats(packet.stats));
        ctx.get().setPacketHandled(true);
    }
}
