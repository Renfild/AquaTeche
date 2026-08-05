package com.casesmod.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * S2C Packet to open the FishMarketScreen GUI on the client.
 */
public class OpenFishMarketS2CPacket {

    public OpenFishMarketS2CPacket() {
    }

    public static void encode(OpenFishMarketS2CPacket msg, FriendlyByteBuf buf) {
    }

    public static OpenFishMarketS2CPacket decode(FriendlyByteBuf buf) {
        return new OpenFishMarketS2CPacket();
    }

    public static void handle(OpenFishMarketS2CPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                com.casesmod.client.ClientPacketHandler.openFishMarket()));
        ctx.setPacketHandled(true);
    }
}
