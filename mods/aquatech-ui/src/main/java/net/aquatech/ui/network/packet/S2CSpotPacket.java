package net.aquatech.ui.network.packet;

import net.aquatech.ui.client.ClientSpotState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** S2C: личная точка лова игрока (позиция, тип, срок жизни, остаток уловов) или её отмена. */
public class S2CSpotPacket {

    private final boolean active;
    private final BlockPos pos;
    private final long remainingMs;
    private final int catchesLeft;
    private final int radius;
    private final String typeLabel;
    private final float priceMult;
    private final int color;

    public S2CSpotPacket(boolean active, BlockPos pos, long remainingMs, int catchesLeft, int radius,
                          String typeLabel, float priceMult, int color) {
        this.active = active;
        this.pos = pos;
        this.remainingMs = remainingMs;
        this.catchesLeft = catchesLeft;
        this.radius = radius;
        this.typeLabel = typeLabel;
        this.priceMult = priceMult;
        this.color = color;
    }

    public S2CSpotPacket(FriendlyByteBuf buf) {
        this.active = buf.readBoolean();
        this.pos = buf.readBlockPos();
        this.remainingMs = buf.readVarLong();
        this.catchesLeft = buf.readVarInt();
        this.radius = buf.readVarInt();
        this.typeLabel = buf.readUtf(32);
        this.priceMult = buf.readFloat();
        this.color = buf.readVarInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(active);
        buf.writeBlockPos(pos);
        buf.writeVarLong(remainingMs);
        buf.writeVarInt(catchesLeft);
        buf.writeVarInt(radius);
        buf.writeUtf(typeLabel, 32);
        buf.writeFloat(priceMult);
        buf.writeVarInt(color);
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            if (active) {
                ClientSpotState.set(pos, remainingMs, catchesLeft, radius, typeLabel, priceMult, color);
            } else {
                ClientSpotState.clear();
            }
        }));
        ctx.setPacketHandled(true);
    }
}
