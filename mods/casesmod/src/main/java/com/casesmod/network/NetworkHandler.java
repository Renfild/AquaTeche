package com.casesmod.network;

import com.casesmod.CasesMod;
import com.casesmod.network.packets.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "7";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CasesMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;
    private static int id() { return packetId++; }

    public static void register() {
        CHANNEL.messageBuilder(OpenCaseC2SPacket.class, id())
                .encoder(OpenCaseC2SPacket::encode)
                .decoder(OpenCaseC2SPacket::decode)
                .consumerMainThread(OpenCaseC2SPacket::handle)
                .add();

        CHANNEL.messageBuilder(CaseResultS2CPacket.class, id())
                .encoder(CaseResultS2CPacket::encode)
                .decoder(CaseResultS2CPacket::decode)
                .consumerMainThread(CaseResultS2CPacket::handle)
                .add();

        CHANNEL.messageBuilder(ClaimKitC2SPacket.class, id())
                .encoder(ClaimKitC2SPacket::encode)
                .decoder(ClaimKitC2SPacket::decode)
                .consumerMainThread(ClaimKitC2SPacket::handle)
                .add();

        CHANNEL.messageBuilder(TeleportWarpC2SPacket.class, id())
                .encoder(TeleportWarpC2SPacket::encode)
                .decoder(TeleportWarpC2SPacket::decode)
                .consumerMainThread(TeleportWarpC2SPacket::handle)
                .add();

        CHANNEL.messageBuilder(ClaimQuestC2SPacket.class, id())
                .encoder(ClaimQuestC2SPacket::encode)
                .decoder(ClaimQuestC2SPacket::decode)
                .consumerMainThread(ClaimQuestC2SPacket::handle)
                .add();

        CHANNEL.messageBuilder(OpenMenuS2CPacket.class, id())
                .encoder(OpenMenuS2CPacket::encode)
                .decoder(OpenMenuS2CPacket::decode)
                .consumerMainThread(OpenMenuS2CPacket::handle)
                .add();

        CHANNEL.messageBuilder(BalanceSyncS2CPacket.class, id())
                .encoder(BalanceSyncS2CPacket::encode)
                .decoder(BalanceSyncS2CPacket::decode)
                .consumerMainThread(BalanceSyncS2CPacket::handle)
                .add();

        CHANNEL.messageBuilder(RecentWinsSyncS2CPacket.class, id())
                .encoder(RecentWinsSyncS2CPacket::encode)
                .decoder(RecentWinsSyncS2CPacket::decode)
                .consumerMainThread(RecentWinsSyncS2CPacket::handle)
                .add();

        CHANNEL.messageBuilder(CaseListSyncS2CPacket.class, id())
                .encoder(CaseListSyncS2CPacket::encode)
                .decoder(CaseListSyncS2CPacket::decode)
                .consumerMainThread(CaseListSyncS2CPacket::handle)
                .add();

        CHANNEL.messageBuilder(MenuCatalogSyncS2CPacket.class, id())
                .encoder(MenuCatalogSyncS2CPacket::encode)
                .decoder(MenuCatalogSyncS2CPacket::decode)
                .consumerMainThread(MenuCatalogSyncS2CPacket::handle)
                .add();

        CHANNEL.messageBuilder(RequestOpenMenuC2SPacket.class, id())
                .encoder(RequestOpenMenuC2SPacket::encode)
                .decoder(RequestOpenMenuC2SPacket::decode)
                .consumerMainThread(RequestOpenMenuC2SPacket::handle)
                .add();

        CHANNEL.messageBuilder(C2SSellFishPacket.class, id())
                .encoder(C2SSellFishPacket::encode)
                .decoder(C2SSellFishPacket::decode)
                .consumerMainThread(C2SSellFishPacket::handle)
                .add();

        CHANNEL.messageBuilder(OpenFishMarketS2CPacket.class, id())
                .encoder(OpenFishMarketS2CPacket::encode)
                .decoder(OpenFishMarketS2CPacket::decode)
                .consumerMainThread(OpenFishMarketS2CPacket::handle)
                .add();
    }

    /** Открыть меню: баланс + кейсы + киты/варпы/квесты. */
    public static void openMenuFor(net.minecraft.server.level.ServerPlayer player) {
        long balance = com.casesmod.data.CurrencyManager.INSTANCE.getBalance(player.getUUID());
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new OpenMenuS2CPacket(balance));
        syncPlayerCatalog(player);
    }

    /** Персональный снимок кейсов/китов/варпов/квестов одному игроку. */
    public static void syncPlayerCatalog(net.minecraft.server.level.ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                CaseListSyncS2CPacket.buildForPlayer(player));
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                MenuCatalogSyncS2CPacket.forPlayer(player));
    }

    /**
     * Рассылает актуальный каталог всем онлайн — после /casesmod reload
     * и после любой настройки варпов/китов/кейсов в игре.
     */
    public static void broadcastCaseList(net.minecraft.server.MinecraftServer server) {
        if (server == null) return;
        for (net.minecraft.server.level.ServerPlayer p : server.getPlayerList().getPlayers()) {
            syncPlayerCatalog(p);
        }
    }

    /** Рассылает актуальную ленту последних выигрышей всем игрокам онлайн — вызывать после каждого открытия кейса. */
    public static void broadcastRecentWins(net.minecraft.server.MinecraftServer server) {
        if (server == null) return;
        var packet = new RecentWinsSyncS2CPacket(com.casesmod.data.RecentWinsManager.INSTANCE.getAll());
        CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
    }
}
