package net.aquatech.ui.network.packet;

import net.aquatech.ui.server.ContainerOpenService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SOpenContainerPacket {

    public enum ContainerType {
        STORAGE_VAULT,
        BLOCK_LIMITERS,
        PERSONALIZATION
    }

    private final ContainerType type;

    public C2SOpenContainerPacket(ContainerType type) {
        this.type = type;
    }

    public C2SOpenContainerPacket(FriendlyByteBuf buf) {
        this.type = buf.readEnum(ContainerType.class);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(this.type);
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            ContainerOpenService.open(player, this.type);
        });
        ctx.setPacketHandled(true);
    }
}
