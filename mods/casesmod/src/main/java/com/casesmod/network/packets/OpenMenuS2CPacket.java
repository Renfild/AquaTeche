package com.casesmod.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Сервер просит клиента открыть главное меню и передаёт актуальный баланс валюты игрока. */
public class OpenMenuS2CPacket {
    public final long balance;

    public OpenMenuS2CPacket(long balance) { this.balance = balance; }

    public static void encode(OpenMenuS2CPacket msg, FriendlyByteBuf buf) { buf.writeLong(msg.balance); }
    public static OpenMenuS2CPacket decode(FriendlyByteBuf buf) { return new OpenMenuS2CPacket(buf.readLong()); }

    public static void handle(OpenMenuS2CPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        com.casesmod.client.ClientPacketHandler.openMainMenu(msg.balance)));
        ctx.get().setPacketHandled(true);
    }
}
