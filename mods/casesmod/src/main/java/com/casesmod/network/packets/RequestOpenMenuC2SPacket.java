package com.casesmod.network.packets;

import com.casesmod.network.NetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Клиент просит открыть меню (F4) — сервер синкает данные и шлёт OpenMenu. */
public class RequestOpenMenuC2SPacket {
    public RequestOpenMenuC2SPacket() {
    }

    public static void encode(RequestOpenMenuC2SPacket msg, FriendlyByteBuf buf) {
    }

    public static RequestOpenMenuC2SPacket decode(FriendlyByteBuf buf) {
        return new RequestOpenMenuC2SPacket();
    }

    public static void handle(RequestOpenMenuC2SPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                NetworkHandler.openMenuFor(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
