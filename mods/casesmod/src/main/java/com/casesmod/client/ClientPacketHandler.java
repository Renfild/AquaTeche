package com.casesmod.client;

import com.casesmod.client.gui.CaseOpeningScreen;
import com.casesmod.client.gui.MainMenuScreen;
import com.casesmod.network.packets.CaseResultS2CPacket;
import net.minecraft.client.Minecraft;

public class ClientPacketHandler {
    public static void onCaseResult(CaseResultS2CPacket msg) {
        ClientBalanceState.balance = msg.newBalance;
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new CaseOpeningScreen(msg.caseId, msg.itemId, msg.count, msg.rarity, msg.displayName));
    }

    public static void openMainMenu(long balance) {
        ClientBalanceState.balance = balance;
        try {
            Class<?> webScreen = Class.forName("net.aquatech.ui.client.gui.AquaWebScreen");
            webScreen.getMethod("openEmbed", String.class, String.class).invoke(null, "Меню", "menu");
        } catch (Throwable t) {
            Minecraft.getInstance().setScreen(new MainMenuScreen());
        }
    }

    public static void openFishMarket() {
        Minecraft.getInstance().setScreen(new com.casesmod.client.gui.FishMarketScreen());
    }
}
