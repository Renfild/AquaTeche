package com.casesmod.network.packets;

import com.casesmod.data.FishSellService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client asks server to sell StarCatcher fish (hand or whole inventory). */
public class C2SSellFishPacket {
    private final boolean sellHandOnly;

    public C2SSellFishPacket(boolean sellHandOnly) {
        this.sellHandOnly = sellHandOnly;
    }

    public static void encode(C2SSellFishPacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.sellHandOnly);
    }

    public static C2SSellFishPacket decode(FriendlyByteBuf buf) {
        return new C2SSellFishPacket(buf.readBoolean());
    }

    public static void handle(C2SSellFishPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;
            FishSellService.sell(player, packet.sellHandOnly);
        });
        ctx.setPacketHandled(true);
    }
}
