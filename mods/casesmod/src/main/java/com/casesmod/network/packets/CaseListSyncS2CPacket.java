package com.casesmod.network.packets;

import com.casesmod.data.CaseDefinition;
import com.casesmod.data.CaseItem;
import com.casesmod.data.CaseManager;
import com.casesmod.data.PlayerAccountManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Полный список кейсов сервера с ценами, пулом призов и шансами — отправляется клиенту,
 * который иначе (на настоящем выделенном сервере) вообще не знает, какие кейсы существуют.
 * Отправляется при открытии меню и при /casesmod reload (всем игрокам онлайн).
 */
public class CaseListSyncS2CPacket {

    public record ItemSnapshot(String itemId, int count, double weight, String rarity, String displayName) {}

    public record CaseSnapshot(String id, String displayName, String iconItemId, long price,
                                int pityThreshold, String pityRarity, int pityProgress, boolean availableNow,
                                int ownedCount, List<ItemSnapshot> items) {}

    public final List<CaseSnapshot> cases;

    public CaseListSyncS2CPacket(List<CaseSnapshot> cases) { this.cases = cases; }

    /** Снимок без персонального pity/owned — для широковещательной рассылки при /reload. */
    public static CaseListSyncS2CPacket buildCurrent() {
        return build(null);
    }

    /** Снимок с pity и количеством кейсов у игрока — при открытии меню. */
    public static CaseListSyncS2CPacket buildForPlayer(net.minecraft.server.level.ServerPlayer player) {
        return build(player);
    }

    private static CaseListSyncS2CPacket build(net.minecraft.server.level.ServerPlayer playerOrNull) {
        List<CaseSnapshot> snaps = new ArrayList<>();
        for (CaseDefinition def : CaseManager.INSTANCE.getCases()) {
            List<ItemSnapshot> items = new ArrayList<>();
            for (CaseItem it : def.items) {
                items.add(new ItemSnapshot(it.itemId, it.count, it.weight, it.rarity, it.displayName));
            }
            int pityProgress = (playerOrNull != null && def.pityThreshold > 0)
                    ? com.casesmod.data.PityManager.INSTANCE.getCount(playerOrNull.getUUID(), def.id) : 0;
            int owned = playerOrNull != null
                    ? PlayerAccountManager.INSTANCE.get(playerOrNull.getUUID()).getCaseCount(def.id) : 0;
            snaps.add(new CaseSnapshot(def.id, def.displayName, def.iconItemId, def.price,
                    def.pityThreshold, def.pityRarity, pityProgress,
                    CaseManager.INSTANCE.isAvailable(def), owned, items));
        }
        return new CaseListSyncS2CPacket(snaps);
    }

    public static void encode(CaseListSyncS2CPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.cases.size());
        for (CaseSnapshot c : msg.cases) {
            buf.writeUtf(c.id());
            buf.writeUtf(c.displayName());
            buf.writeUtf(c.iconItemId());
            buf.writeLong(c.price());
            buf.writeVarInt(c.pityThreshold());
            buf.writeUtf(c.pityRarity());
            buf.writeVarInt(c.pityProgress());
            buf.writeBoolean(c.availableNow());
            buf.writeVarInt(c.ownedCount());
            buf.writeVarInt(c.items().size());
            for (ItemSnapshot it : c.items()) {
                buf.writeUtf(it.itemId());
                buf.writeVarInt(it.count());
                buf.writeDouble(it.weight());
                buf.writeUtf(it.rarity());
                buf.writeUtf(it.displayName());
            }
        }
    }

    public static CaseListSyncS2CPacket decode(FriendlyByteBuf buf) {
        int caseCount = buf.readVarInt();
        List<CaseSnapshot> cases = new ArrayList<>(caseCount);
        for (int i = 0; i < caseCount; i++) {
            String id = buf.readUtf();
            String displayName = buf.readUtf();
            String iconItemId = buf.readUtf();
            long price = buf.readLong();
            int pityThreshold = buf.readVarInt();
            String pityRarity = buf.readUtf();
            int pityProgress = buf.readVarInt();
            boolean availableNow = buf.readBoolean();
            int ownedCount = buf.readVarInt();
            int itemCount = buf.readVarInt();
            List<ItemSnapshot> items = new ArrayList<>(itemCount);
            for (int j = 0; j < itemCount; j++) {
                items.add(new ItemSnapshot(buf.readUtf(), buf.readVarInt(), buf.readDouble(), buf.readUtf(), buf.readUtf()));
            }
            cases.add(new CaseSnapshot(id, displayName, iconItemId, price, pityThreshold, pityRarity,
                    pityProgress, availableNow, ownedCount, items));
        }
        return new CaseListSyncS2CPacket(cases);
    }

    public static void handle(CaseListSyncS2CPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        com.casesmod.client.ClientCaseState.cases = msg.cases));
        ctx.get().setPacketHandled(true);
    }
}
