package com.casesmod.network.packets;

import com.casesmod.data.KitDefinition;
import com.casesmod.data.KitManager;
import com.casesmod.data.QuestDefinition;
import com.casesmod.data.QuestManager;
import com.casesmod.data.WarpDefinition;
import com.casesmod.data.WarpManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Синхронизация китов/варпов/квестов на клиент при открытии меню. */
public class MenuCatalogSyncS2CPacket {

    public record KitSnap(String id, String displayName, String iconItemId, long cooldownRemain) {
    }

    public record WarpSnap(String id, String displayName, String iconItemId) {
    }

    public record QuestSnap(String id, String displayName, String description, String iconItemId,
                            String stage, int stageOrder,
                            int requiredAmount, int progress, boolean claimed, boolean complete) {
    }

    public final List<KitSnap> kits;
    public final List<WarpSnap> warps;
    public final List<QuestSnap> quests;

    public MenuCatalogSyncS2CPacket(List<KitSnap> kits, List<WarpSnap> warps, List<QuestSnap> quests) {
        this.kits = kits;
        this.warps = warps;
        this.quests = quests;
    }

    public static MenuCatalogSyncS2CPacket forPlayer(ServerPlayer player) {
        List<KitSnap> kits = new ArrayList<>();
        for (KitDefinition k : KitManager.INSTANCE.getKits()) {
            kits.add(new KitSnap(k.id, k.displayName, k.iconItemId,
                    KitManager.INSTANCE.secondsUntilAvailable(player.getUUID(), k)));
        }
        List<WarpSnap> warps = new ArrayList<>();
        for (WarpDefinition w : WarpManager.INSTANCE.getWarps()) {
            warps.add(new WarpSnap(w.id, w.displayName, w.iconItemId));
        }
        List<QuestSnap> quests = new ArrayList<>();
        for (QuestDefinition q : QuestManager.INSTANCE.getQuests()) {
            int progress = QuestManager.INSTANCE.getProgress(player.getUUID(), q.id);
            boolean claimed = QuestManager.INSTANCE.isClaimed(player.getUUID(), q.id);
            boolean complete = QuestManager.INSTANCE.isComplete(player.getUUID(), q);
            quests.add(new QuestSnap(q.id, q.displayName, q.description, q.iconItemId,
                    q.stage == null ? "" : q.stage, q.stageOrder,
                    q.requiredAmount, progress, claimed, complete));
        }
        return new MenuCatalogSyncS2CPacket(kits, warps, quests);
    }

    public static void encode(MenuCatalogSyncS2CPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.kits.size());
        for (KitSnap k : msg.kits) {
            buf.writeUtf(k.id());
            buf.writeUtf(k.displayName());
            buf.writeUtf(k.iconItemId());
            buf.writeVarLong(k.cooldownRemain());
        }
        buf.writeVarInt(msg.warps.size());
        for (WarpSnap w : msg.warps) {
            buf.writeUtf(w.id());
            buf.writeUtf(w.displayName());
            buf.writeUtf(w.iconItemId());
        }
        buf.writeVarInt(msg.quests.size());
        for (QuestSnap q : msg.quests) {
            buf.writeUtf(q.id());
            buf.writeUtf(q.displayName());
            buf.writeUtf(q.description() == null ? "" : q.description());
            buf.writeUtf(q.iconItemId());
            buf.writeUtf(q.stage() == null ? "" : q.stage());
            buf.writeVarInt(q.stageOrder());
            buf.writeVarInt(q.requiredAmount());
            buf.writeVarInt(q.progress());
            buf.writeBoolean(q.claimed());
            buf.writeBoolean(q.complete());
        }
    }

    public static MenuCatalogSyncS2CPacket decode(FriendlyByteBuf buf) {
        int kc = buf.readVarInt();
        List<KitSnap> kits = new ArrayList<>(kc);
        for (int i = 0; i < kc; i++) {
            kits.add(new KitSnap(buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readVarLong()));
        }
        int wc = buf.readVarInt();
        List<WarpSnap> warps = new ArrayList<>(wc);
        for (int i = 0; i < wc; i++) {
            warps.add(new WarpSnap(buf.readUtf(), buf.readUtf(), buf.readUtf()));
        }
        int qc = buf.readVarInt();
        List<QuestSnap> quests = new ArrayList<>(qc);
        for (int i = 0; i < qc; i++) {
            quests.add(new QuestSnap(buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readUtf(),
                    buf.readUtf(), buf.readVarInt(),
                    buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readBoolean()));
        }
        return new MenuCatalogSyncS2CPacket(kits, warps, quests);
    }

    public static void handle(MenuCatalogSyncS2CPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        com.casesmod.client.ClientMenuCatalog.apply(msg)));
        ctx.get().setPacketHandled(true);
    }
}
