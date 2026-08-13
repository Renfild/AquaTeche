package net.aquatech.ui.network.packet;

import net.aquatech.ui.client.gui.ClientContainerScreens;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2COpenContainerPacket {

    private final C2SOpenContainerPacket.ContainerType type;

    public S2COpenContainerPacket(C2SOpenContainerPacket.ContainerType type) {
        this.type = type;
    }

    public S2COpenContainerPacket(FriendlyByteBuf buf) {
        this.type = buf.readEnum(C2SOpenContainerPacket.ContainerType.class);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(this.type);
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ClientContainerScreens.open(type)
        ));
        ctx.setPacketHandled(true);
    }
}
