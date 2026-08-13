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
        Minecraft.getInstance().setScreen(new MainMenuScreen());
    }

    public static void openFishMarket() {
        Minecraft.getInstance().setScreen(new com.casesmod.client.gui.FishMarketScreen());
    }
}
