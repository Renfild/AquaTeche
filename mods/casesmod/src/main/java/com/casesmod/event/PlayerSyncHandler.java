package com.casesmod.event;

import com.casesmod.data.RecentWinsManager;
import com.casesmod.network.NetworkHandler;
import com.casesmod.network.packets.RecentWinsSyncS2CPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

/** При входе игрока на сервер сразу отправляет ему текущий снимок ленты последних выигрышей. */
@Mod.EventBusSubscriber(modid = "casesmod")
public class PlayerSyncHandler {
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp),
                new RecentWinsSyncS2CPacket(RecentWinsManager.INSTANCE.getAll()));
    }
}
