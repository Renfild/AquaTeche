package com.casesmod.client.gui;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;

import java.lang.reflect.Method;
import java.net.URI;

/** Opens aquatech_ui CEF overlay when the mod is present; otherwise system browser. */
public final class WebOverlay {

    private WebOverlay() {
    }

    public static void openMainMenu(Minecraft mc) {
        if (!openEmbed("Меню", "menu")) {
            if (mc != null) {
                mc.setScreen(null);
            }
        }
    }

    public static void openSkills(Minecraft mc) {
        if (!openEmbed("Созвездия Океана", "skills")) {
            if (mc != null) {
                mc.setScreen(null);
            }
        }
    }

    public static void openDonate(Minecraft mc) {
        if (!openEmbed("Донат", "donate")) {
            openExternal("https://aquateche.store/store.html");
            if (mc != null) {
                mc.setScreen(null);
            }
        }
    }

    public static void openCabinet(Minecraft mc) {
        if (!openEmbed("Кабинет", "cabinet")) {
            openExternal("https://aquateche.store/profile.html");
            if (mc != null) {
                mc.setScreen(null);
            }
        }
    }

    private static boolean openEmbed(String title, String page) {
        try {
            Class<?> cls = Class.forName("net.aquatech.ui.client.gui.AquaWebScreen");
            Method m = cls.getMethod("openEmbed", String.class, String.class);
            m.invoke(null, title, page);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static void openExternal(String url) {
        try {
            Util.getPlatform().openUri(new URI(url));
        } catch (Exception ignored) {
        }
    }
}
