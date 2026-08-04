package com.casesmod.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Сервер сообщает клиенту итог открытия кейса — клиент проигрывает анимацию рулетки и обновляет баланс. */
public class CaseResultS2CPacket {
    public final String caseId;
    public final String itemId;
    public final int count;
    public final String rarity;
    public final String displayName;
    public final long newBalance;

    public CaseResultS2CPacket(String caseId, String itemId, int count, String rarity, String displayName, long newBalance) {
        this.caseId = caseId; this.itemId = itemId; this.count = count;
        this.rarity = rarity; this.displayName = displayName; this.newBalance = newBalance;
    }

    public static void encode(CaseResultS2CPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.caseId);
        buf.writeUtf(msg.itemId);
        buf.writeInt(msg.count);
        buf.writeUtf(msg.rarity);
        buf.writeUtf(msg.displayName);
        buf.writeLong(msg.newBalance);
    }

    public static CaseResultS2CPacket decode(FriendlyByteBuf buf) {
        return new CaseResultS2CPacket(buf.readUtf(), buf.readUtf(), buf.readInt(), buf.readUtf(), buf.readUtf(), buf.readLong());
    }

    public static void handle(CaseResultS2CPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        com.casesmod.client.ClientPacketHandler.onCaseResult(msg)));
        ctx.get().setPacketHandled(true);
    }
}
