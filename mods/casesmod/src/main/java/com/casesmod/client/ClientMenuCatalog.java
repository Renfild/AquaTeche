package com.casesmod.client;

import com.casesmod.network.packets.MenuCatalogSyncS2CPacket;

import java.util.Collections;
import java.util.List;

/** Клиентский снимок китов/варпов/квестов с сервера (на выделенном сервере локальные менеджеры пусты). */
public final class ClientMenuCatalog {
    public static List<MenuCatalogSyncS2CPacket.KitSnap> kits = Collections.emptyList();
    public static List<MenuCatalogSyncS2CPacket.WarpSnap> warps = Collections.emptyList();
    public static List<MenuCatalogSyncS2CPacket.QuestSnap> quests = Collections.emptyList();

    private ClientMenuCatalog() {
    }

    public static void apply(MenuCatalogSyncS2CPacket msg) {
        kits = List.copyOf(msg.kits);
        warps = List.copyOf(msg.warps);
        quests = List.copyOf(msg.quests);
    }
}
