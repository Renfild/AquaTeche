package com.casesmod.network;

import com.casesmod.CasesMod;
import com.casesmod.network.packets.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "6";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CasesMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int id = 0;
    private static int next() { return id++; }

    public static void register() {
        CHANNEL.registerMessage(next(), OpenCaseC2SPacket.class,
                OpenCaseC2SPacket::encode, OpenCaseC2SPacket::decode, OpenCaseC2SPacket::handle);

        CHANNEL.registerMessage(next(), CaseResultS2CPacket.class,
                CaseResultS2CPacket::encode, CaseResultS2CPacket::decode, CaseResultS2CPacket::handle);

        CHANNEL.registerMessage(next(), ClaimKitC2SPacket.class,
                ClaimKitC2SPacket::encode, ClaimKitC2SPacket::decode, ClaimKitC2SPacket::handle);

        CHANNEL.registerMessage(next(), TeleportWarpC2SPacket.class,
                TeleportWarpC2SPacket::encode, TeleportWarpC2SPacket::decode, TeleportWarpC2SPacket::handle);

        CHANNEL.registerMessage(next(), ClaimQuestC2SPacket.class,
                ClaimQuestC2SPacket::encode, ClaimQuestC2SPacket::decode, ClaimQuestC2SPacket::handle);

        CHANNEL.registerMessage(next(), OpenMenuS2CPacket.class,
                OpenMenuS2CPacket::encode, OpenMenuS2CPacket::decode, OpenMenuS2CPacket::handle);

        CHANNEL.registerMessage(next(), BalanceSyncS2CPacket.class,
                BalanceSyncS2CPacket::encode, BalanceSyncS2CPacket::decode, BalanceSyncS2CPacket::handle);

        CHANNEL.registerMessage(next(), RecentWinsSyncS2CPacket.class,
                RecentWinsSyncS2CPacket::encode, RecentWinsSyncS2CPacket::decode, RecentWinsSyncS2CPacket::handle);

        CHANNEL.registerMessage(next(), CaseListSyncS2CPacket.class,
                CaseListSyncS2CPacket::encode, CaseListSyncS2CPacket::decode, CaseListSyncS2CPacket::handle);

        CHANNEL.registerMessage(next(), MenuCatalogSyncS2CPacket.class,
                MenuCatalogSyncS2CPacket::encode, MenuCatalogSyncS2CPacket::decode, MenuCatalogSyncS2CPacket::handle);

        CHANNEL.registerMessage(next(), RequestOpenMenuC2SPacket.class,
                RequestOpenMenuC2SPacket::encode, RequestOpenMenuC2SPacket::decode, RequestOpenMenuC2SPacket::handle);

        CHANNEL.registerMessage(next(), C2SSellFishPacket.class,
                C2SSellFishPacket::encode, C2SSellFishPacket::decode, C2SSellFishPacket::handle);

        CHANNEL.registerMessage(next(), OpenFishMarketS2CPacket.class,
                OpenFishMarketS2CPacket::encode, OpenFishMarketS2CPacket::decode, OpenFishMarketS2CPacket::handle);
    }

    /** Открыть меню: баланс + кейсы + киты/варпы/квесты. */
    public static void openMenuFor(net.minecraft.server.level.ServerPlayer player) {
        long balance = com.casesmod.data.CurrencyManager.INSTANCE.getBalance(player.getUUID());
        CHANNEL.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                new OpenMenuS2CPacket(balance));
        syncPlayerCatalog(player);
    }

    /** Персональный снимок кейсов/китов/варпов/квестов одному игроку. */
    public static void syncPlayerCatalog(net.minecraft.server.level.ServerPlayer player) {
        CHANNEL.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                CaseListSyncS2CPacket.buildForPlayer(player));
        CHANNEL.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
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
        CHANNEL.send(net.minecraftforge.network.PacketDistributor.ALL.noArg(), packet);
    }
}
