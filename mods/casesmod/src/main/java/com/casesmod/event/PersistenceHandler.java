package com.casesmod.event;

import com.casesmod.data.QuestManager;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Сохраняет прогресс квестов на диск в безопасные моменты — при выходе игрока и при
 * остановке сервера — вместо записи на каждое событие прогресса (добыча блока и т.п.),
 * что было бы слишком частым для диска. Баланс валюты и кулдауны китов сохраняются сразу
 * при изменении (эти события редкие), поэтому отдельного хука для них не требуется.
 */
@Mod.EventBusSubscriber(modid = "casesmod")
public class PersistenceHandler {
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        QuestManager.INSTANCE.saveProgress();
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        QuestManager.INSTANCE.saveProgress();
    }
}
