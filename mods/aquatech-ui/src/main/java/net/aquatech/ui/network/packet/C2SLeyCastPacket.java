package net.aquatech.ui.network.packet;

import net.aquatech.ui.fishing.LeyFightService;
import net.aquatech.ui.registry.ModItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * C2S: игрок нажал ПКМ удочкой Лея. Клик по воздуху и блоку на сервере может отменить WorldGuard в охраняемом
 * регионе, поэтому клиент сообщает о заброске отдельным пакетом, так же как и в разговоре с Рыбаком-соседом.
 */
public class C2SLeyCastPacket {

    public C2SLeyCastPacket() {
    }

    public C2SLeyCastPacket(FriendlyByteBuf buf) {
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null && player.getItemInHand(InteractionHand.MAIN_HAND).is(ModItems.LEY_ROD.get())) {
                LeyFightService.tryStart(player);
            }
        });
        ctx.setPacketHandled(true);
        return true;
    }
}
