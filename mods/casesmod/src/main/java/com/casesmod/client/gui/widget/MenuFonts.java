package com.casesmod.client.gui.widget;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

/** Smooth TTF labels for liquid-glass menus (Cyrillic via Noto Sans). */
public final class MenuFonts {
    public static final ResourceLocation MENU =
            new ResourceLocation("casesmod", "menu");

    private MenuFonts() {
    }

    public static MutableComponent text(String plain) {
        return Component.literal(plain).withStyle(Style.EMPTY.withFont(MENU));
    }
}
