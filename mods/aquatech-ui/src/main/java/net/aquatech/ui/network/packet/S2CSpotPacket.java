package net.aquatech.ui.network.packet;

import net.aquatech.ui.client.ClientSpotState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** S2C: личная точка лова игрока (позиция, срок жизни, остаток уловов) или её отмена. */
public class S2CSpotPacket {

    private final boolean active;
    private final BlockPos pos;
    private final long remainingMs;
    private final int catchesLeft;
    private final int radius;

    public S2CSpotPacket(boolean active, BlockPos pos, long remainingMs, int catchesLeft, int radius) {
        this.active = active;
        this.pos = pos;
        this.remainingMs = remainingMs;
        this.catchesLeft = catchesLeft;
        this.radius = radius;
    }

    public S2CSpotPacket(FriendlyByteBuf buf) {
        this.active = buf.readBoolean();
        this.pos = buf.readBlockPos();
        this.remainingMs = buf.readVarLong();
        this.catchesLeft = buf.readVarInt();
        this.radius = buf.readVarInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(active);
        buf.writeBlockPos(pos);
        buf.writeVarLong(remainingMs);
        buf.writeVarInt(catchesLeft);
        buf.writeVarInt(radius);
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            if (active) {
                ClientSpotState.set(pos, remainingMs, catchesLeft, radius);
            } else {
                ClientSpotState.clear();
            }
        }));
        ctx.setPacketHandled(true);
    }
}
