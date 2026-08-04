package com.casesmod.network.packets;

import com.casesmod.data.RecentWinsManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Полный снимок последних выигрышей на сервере, рассылается всем игроками при новом
 * открытии кейса (для эффекта "живой" ленты) и при входе игрока на сервер.
 */
public class RecentWinsSyncS2CPacket {
    public final List<RecentWinsManager.WinEntry> entries;

    public RecentWinsSyncS2CPacket(List<RecentWinsManager.WinEntry> entries) { this.entries = entries; }

    public static void encode(RecentWinsSyncS2CPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entries.size());
        for (RecentWinsManager.WinEntry e : msg.entries) {
            buf.writeUtf(e.playerName());
            buf.writeUtf(e.itemDisplayName());
            buf.writeUtf(e.rarity());
            buf.writeLong(e.timestamp());
        }
    }

    public static RecentWinsSyncS2CPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<RecentWinsManager.WinEntry> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(new RecentWinsManager.WinEntry(buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readLong()));
        }
        return new RecentWinsSyncS2CPacket(list);
    }

    public static void handle(RecentWinsSyncS2CPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        com.casesmod.client.ClientRecentWinsState.entries = msg.entries));
        ctx.get().setPacketHandled(true);
    }
}
