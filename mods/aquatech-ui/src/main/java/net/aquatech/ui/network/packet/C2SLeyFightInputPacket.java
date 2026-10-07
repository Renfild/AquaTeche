package net.aquatech.ui.network.packet;

import net.aquatech.ui.fishing.LeyFightService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** C2S: что игрок делает с удочкой Лея прямо сейчас. Клиент шлёт раз в тик, пока идёт схватка. */
public class C2SLeyFightInputPacket {

    private final boolean reeling;
    private final byte steer;

    public C2SLeyFightInputPacket(boolean reeling, int steer) {
        this.reeling = reeling;
        this.steer = (byte) Math.max(-1, Math.min(1, steer));
    }

    public C2SLeyFightInputPacket(FriendlyByteBuf buf) {
        this.reeling = buf.readBoolean();
        this.steer = buf.readByte();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(reeling);
        buf.writeByte(steer);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                LeyFightService.input(player, reeling, Math.max(-1, Math.min(1, (int) steer)));
            }
        });
        ctx.setPacketHandled(true);
        return true;
    }
}
