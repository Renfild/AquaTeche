package com.casesmod.server;

import com.casesmod.CasesMod;
import com.casesmod.network.NetworkHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** При входе игрока синкаем каталог меню после завершения handshake. */
@Mod.EventBusSubscriber(modid = CasesMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PlayerJoinSync {
    private static final int LOGIN_SYNC_DELAY_TICKS = 80;

    private PlayerJoinSync() {}

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        MinecraftServer server = player.getServer();
        if (server == null) return;
        server.tell(new TickTask(server.getTickCount() + LOGIN_SYNC_DELAY_TICKS, () -> {
            if (player.hasDisconnected()) return;
            NetworkHandler.syncPlayerCatalog(player);
        }));
    }
}
