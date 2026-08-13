package com.casesmod.client.gui;

import com.casesmod.CasesMod;
import com.casesmod.client.ClientBalanceState;
import com.casesmod.client.gui.widget.GuiHotspot;
import com.casesmod.data.FishPriceCalculator;
import com.casesmod.network.NetworkHandler;
import com.casesmod.network.packets.C2SSellFishPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Specimen Buyer GUI with Super Cute Kawaii Cat mascot and clean symmetrical back arrow.
 * Extra icons removed for a sleek, minimal aesthetic.
 */
public class FishMarketScreen extends Screen {
    public static final int TEX_W = 256;
    public static final int TEX_H = 192;
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(CasesMod.MOD_ID, "textures/gui/fish_market.png");

    private int left;
    private int top;

    public FishMarketScreen() {
        super(Component.literal("Скупщик Улова"));
    }

    @Override
    protected void init() {
        left = (width - TEX_W) / 2;
        top = (height - TEX_H) / 2;

        // Sell Hand Button (No hover rectangle outline)
        addRenderableWidget(new GuiHotspot(
                left + 28, top + 94, 200, 28,
                Component.literal("Продать рыбу в руке"),
                b -> NetworkHandler.CHANNEL.sendToServer(new C2SSellFishPacket(true)), false));

        // Sell All Button (No hover rectangle outline)
        addRenderableWidget(new GuiHotspot(
                left + 28, top + 126, 200, 28,
                Component.literal("Продать всю рыбу"),
                b -> NetworkHandler.CHANNEL.sendToServer(new C2SSellFishPacket(false)), false));

        // Back Button (No hover rectangle outline)
        addRenderableWidget(new GuiHotspot(
                left + 16, top + 158, 80, 22,
                Component.literal("Назад"),
                b -> minecraft.setScreen(null), false));
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        renderBackground(gfx);

        // Render clean custom PNG GUI panel
        gfx.blit(TEXTURE, left, top, 0, 0, TEX_W, TEX_H, TEX_W, TEX_H);

        // Balance Box (Clean bright gold, no caps, no bold, no shadow, centered at X=125)
        String balanceStr = String.format("%,d", ClientBalanceState.balance).replace(',', ' ');
        drawCenteredNoShadow(gfx, Component.literal("§7Баланс: §e" + balanceStr + " дублонов"),
                left + 125, top + 44, 0xFFFFFFFF);

        // Hand Preview Box (Centered at X=125)
        if (minecraft != null && minecraft.player != null) {
            ItemStack hand = minecraft.player.getMainHandItem();
            FishPriceCalculator.PriceResult preview = FishPriceCalculator.calculatePrice(hand);
            if (preview.isFish()) {
                String gold = preview.isGolden() ? " §6★" : "";
                drawCenteredNoShadow(gfx, Component.literal(
                        "§7Улов: §f" + preview.rarityName() + gold + " §a+" + preview.finalPrice() + " 🪙"
                ), left + 125, top + 71, 0xFFFFFFFF);
            } else {
                drawCenteredNoShadow(gfx, Component.literal("§8[ Нет рыбы в руке ]"),
                        left + 125, top + 71, 0xFF888888);
            }
        }

        // Button Text Overlays (Bright white, normal case, NO shadows, centered at X=125)
        drawCenteredNoShadow(gfx, Component.literal("§fПродать рыбу в руке"),
                left + 125, top + 103, 0xFFFFFFFF);

        drawCenteredNoShadow(gfx, Component.literal("§fПродать всю рыбу"),
                left + 125, top + 135, 0xFFFFFFFF);

        drawCenteredNoShadow(gfx, Component.literal("§fНазад"),
                left + 56, top + 165, 0xFFFFFFFF);

        super.render(gfx, mouseX, mouseY, partialTicks);
    }

    private void drawCenteredNoShadow(GuiGraphics gfx, Component text, int cx, int y, int color) {
        int w = font.width(text);
        gfx.drawString(font, text, cx - w / 2, y, color, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
