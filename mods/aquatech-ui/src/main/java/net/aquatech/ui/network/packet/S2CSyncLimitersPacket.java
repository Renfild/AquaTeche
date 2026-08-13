package net.aquatech.ui.network.packet;

import net.aquatech.ui.client.ClientUiState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * S2C Packet to sync island block limiters (Placed vs Maximum allowed) to client.
 */
public class S2CSyncLimitersPacket {

    private final Map<String, Integer> placedCounts;
    private final Map<String, Integer> maxLimits;

    public S2CSyncLimitersPacket(Map<String, Integer> placedCounts, Map<String, Integer> maxLimits) {
        this.placedCounts = placedCounts;
        this.maxLimits = maxLimits;
    }

    public S2CSyncLimitersPacket(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        this.placedCounts = new HashMap<>(size);
        this.maxLimits = new HashMap<>(size);

        for (int i = 0; i < size; i++) {
            String id = buf.readUtf();
            int placed = buf.readVarInt();
            int max = buf.readVarInt();
            this.placedCounts.put(id, placed);
            this.maxLimits.put(id, max);
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(placedCounts.size());
        for (Map.Entry<String, Integer> entry : placedCounts.entrySet()) {
            buf.writeUtf(entry.getKey());
            buf.writeVarInt(entry.getValue());
            buf.writeVarInt(maxLimits.getOrDefault(entry.getKey(), 0));
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ClientUiState.updateLimiters(placedCounts, maxLimits)
        ));
        ctx.setPacketHandled(true);
    }
}
