package com.casesmod.client.gui;

import com.casesmod.client.ClientMenuCatalog;
import com.casesmod.client.gui.widget.CustomButton;
import com.casesmod.network.NetworkHandler;
import com.casesmod.network.packets.MenuCatalogSyncS2CPacket;
import com.casesmod.network.packets.TeleportWarpC2SPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class WarpsScreen extends Screen {
    private static final int CARD_W = 240, CARD_H = 32, GAP = 8, LIST_TOP = 70;

    private final List<MenuCatalogSyncS2CPacket.WarpSnap> warps = new ArrayList<>();
    private final List<CustomButton> cards = new ArrayList<>();
    private int scrollY;
    private int maxScroll;
    private int listBottom;

    public WarpsScreen() {
        super(Component.literal("Варпы"));
    }

    @Override
    protected void init() {
        warps.clear();
        warps.addAll(ClientMenuCatalog.warps);
        cards.clear();
        listBottom = height - 48;
        int totalH = warps.isEmpty() ? 0 : warps.size() * (CARD_H + GAP) - GAP;
        maxScroll = Math.max(0, totalH - Math.max(40, listBottom - LIST_TOP));
        scrollY = Mth.clamp(scrollY, 0, maxScroll);

        int x = (width - CARD_W) / 2;
        for (int i = 0; i < warps.size(); i++) {
            MenuCatalogSyncS2CPacket.WarpSnap warp = warps.get(i);
            ItemStack icon = iconFor(warp.iconItemId());
            CustomButton btn = new CustomButton(x, LIST_TOP, CARD_W, CARD_H, Component.literal(warp.displayName()),
                    0xFF3F9EE0, b -> {
                        NetworkHandler.CHANNEL.sendToServer(new TeleportWarpC2SPacket(warp.id()));
                        minecraft.setScreen(null);
                    }, icon, i * 35);
            cards.add(btn);
            addRenderableWidget(btn);
        }
        applyScroll();

        addRenderableWidget(new CustomButton(20, height - 40, 90, 20, Component.literal("← Назад"),
                0xFF888888, b -> WebOverlay.openMainMenu(minecraft), null, 0));
    }

    private void applyScroll() {
        int x = (width - CARD_W) / 2;
        for (int i = 0; i < cards.size(); i++) {
            CustomButton btn = cards.get(i);
            int y = LIST_TOP + i * (CARD_H + GAP) - scrollY;
            btn.setX(x);
            btn.setY(y);
            btn.visible = y + CARD_H > LIST_TOP - 2 && y < listBottom + 2;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (maxScroll <= 0) return super.mouseScrolled(mouseX, mouseY, delta);
        int before = scrollY;
        scrollY = Mth.clamp(scrollY - (int) (delta * 18), 0, maxScroll);
        if (scrollY != before) {
            applyScroll();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private static ItemStack iconFor(String id) {
        try {
            Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(id));
            return new ItemStack(item);
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        gfx.fillGradient(0, 0, width, height, 0xF0050A12, 0xF008101C);
        gfx.fill(0, 0, width, height, 0x66000000);
        gfx.drawCenteredString(font, Component.literal("§l§fВарпы"), width / 2, 30, 0xFFFFFFFF);
        gfx.drawCenteredString(font, Component.literal("§7Быстрое перемещение по точкам сервера"),
                width / 2, 48, 0xFF8899AA);
        super.render(gfx, mouseX, mouseY, partialTicks);
        if (warps.isEmpty()) {
            gfx.drawCenteredString(font, Component.literal("§7Варпы не настроены. config/casesmod/warps.json"),
                    width / 2, height / 2, 0xFFAAAAAA);
        } else if (maxScroll > 0) {
            gfx.drawCenteredString(font, Component.literal("§8колесо — прокрутка"), width / 2, height - 14, 0xFF667788);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
