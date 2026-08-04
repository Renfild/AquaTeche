package com.casesmod.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Лёгкий пакет для обновления баланса на клиенте в реальном времени (после покупки, начисления и т.д.). */
public class BalanceSyncS2CPacket {
    public final long balance;
    public BalanceSyncS2CPacket(long balance) { this.balance = balance; }

    public static void encode(BalanceSyncS2CPacket msg, FriendlyByteBuf buf) { buf.writeLong(msg.balance); }
    public static BalanceSyncS2CPacket decode(FriendlyByteBuf buf) { return new BalanceSyncS2CPacket(buf.readLong()); }

    public static void handle(BalanceSyncS2CPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        com.casesmod.client.ClientBalanceState.balance = msg.balance));
        ctx.get().setPacketHandled(true);
    }
}
