package com.casesmod.client;

import com.casesmod.CasesMod;
import com.casesmod.client.gui.CaseOpeningScreen;
import com.casesmod.client.gui.CasesScreen;
import com.casesmod.client.gui.DonateScreen;
import com.casesmod.client.gui.KitsScreen;
import com.casesmod.client.gui.MainMenuScreen;
import com.casesmod.client.gui.QuestsScreen;
import com.casesmod.client.gui.WarpsScreen;
import com.casesmod.network.NetworkHandler;
import com.casesmod.network.packets.RequestOpenMenuC2SPacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = CasesMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientEvents {

    public static final KeyMapping KEY_OPEN_MENU = new KeyMapping(
            "key.casesmod.open_menu",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F4,
            "key.categories.casesmod"
    );

    private ClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        while (KEY_OPEN_MENU.consumeClick()) {
            Screen screen = mc.screen;
            if (isCasesMenu(screen)) {
                mc.setScreen(null);
                continue;
            }
            if (screen != null) {
                continue; // не перебиваем чат/инвентарь/другие GUI
            }
            NetworkHandler.CHANNEL.sendToServer(new RequestOpenMenuC2SPacket());
        }
    }

    private static boolean isCasesMenu(Screen screen) {
        return screen instanceof MainMenuScreen
                || screen instanceof CasesScreen
                || screen instanceof KitsScreen
                || screen instanceof WarpsScreen
                || screen instanceof QuestsScreen
                || screen instanceof DonateScreen
                || screen instanceof CaseOpeningScreen;
    }
}
