package net.aquatech.ui.network.packet;

import net.aquatech.ui.entity.FishNeighborEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * C2S: игрок нажал ПКМ по Рыбаку-соседу. Прямой клик по сущности на сервере отменяет WorldGuard в охраняемом регионе
 * спавна (так же, как клик по жителю), поэтому клиент сам сообщает серверу, с кем он разговаривает.
 */
public class C2SNeighborTalkPacket {

    /** Дальше этого от соседа разговор не принимается (ПКМ достаёт на 3-5 блоков, остальное запас на лаг). */
    private static final double MAX_DISTANCE = 8.0D;

    private final int entityId;

    public C2SNeighborTalkPacket(int entityId) {
        this.entityId = entityId;
    }

    public C2SNeighborTalkPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readVarInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            Entity target = player.level().getEntity(entityId);
            if (target instanceof FishNeighborEntity neighbor && neighbor.isAlive()
                    && player.distanceToSqr(neighbor) <= MAX_DISTANCE * MAX_DISTANCE) {
                neighbor.talkTo(player);
            }
        });
        ctx.setPacketHandled(true);
        return true;
    }
}
