package net.aquatech.ui.server;

import net.aquatech.ui.network.NetworkHandler;
import net.aquatech.ui.network.packet.C2SOpenContainerPacket;
import net.aquatech.ui.network.packet.S2COpenContainerPacket;
import net.aquatech.ui.skyblock.IslandLimiterTracker;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;

public final class ContainerOpenService {

    private ContainerOpenService() {
    }

    public static void open(ServerPlayer player, C2SOpenContainerPacket.ContainerType type) {
        if (player == null || type == null) {
            return;
        }
        switch (type) {
            case STORAGE_VAULT -> openVault(player);
            case BLOCK_LIMITERS -> openLimiters(player);
            case PERSONALIZATION -> NetworkHandler.sendToPlayerWhenReady(
                    new S2COpenContainerPacket(C2SOpenContainerPacket.ContainerType.PERSONALIZATION), player);
        }
    }

    private static void openVault(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> ChestMenu.threeRows(id, inv, p.getEnderChestInventory()),
                Component.literal("Хранилище острова")
        ));
    }

    private static void openLimiters(ServerPlayer player) {
        if (player.level() instanceof ServerLevel level) {
            java.util.UUID owner = net.aquatech.ui.skyblock.IslandLimiterHandler.ownerAt(level, player.blockPosition());
            IslandLimiterTracker.get(level).syncTo(player, owner != null ? owner : player.getUUID());
        }
        NetworkHandler.sendToPlayerWhenReady(
                new S2COpenContainerPacket(C2SOpenContainerPacket.ContainerType.BLOCK_LIMITERS), player);
    }
}
