package com.casesmod.event;

import com.casesmod.data.RecentWinsManager;
import com.casesmod.network.NetworkHandler;
import com.casesmod.network.packets.RecentWinsSyncS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

/** При входе игрока на сервер отправляет снимок ленты последних выигрышей после завершения handshake. */
@Mod.EventBusSubscriber(modid = "casesmod")
public class PlayerSyncHandler {
    private static final int LOGIN_SYNC_DELAY_TICKS = 80;

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        MinecraftServer server = sp.getServer();
        if (server == null) return;

        server.tell(new TickTask(server.getTickCount() + LOGIN_SYNC_DELAY_TICKS, () -> {
            if (sp.hasDisconnected()) return;
            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp),
                    new RecentWinsSyncS2CPacket(RecentWinsManager.INSTANCE.getAll()));
        }));
    }
}
