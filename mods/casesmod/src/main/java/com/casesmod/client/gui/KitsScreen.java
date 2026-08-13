package com.casesmod.client.gui;

import com.casesmod.client.ClientMenuCatalog;
import com.casesmod.client.gui.widget.CustomButton;
import com.casesmod.network.NetworkHandler;
import com.casesmod.network.packets.ClaimKitC2SPacket;
import com.casesmod.network.packets.MenuCatalogSyncS2CPacket;
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

public class KitsScreen extends Screen {
    private static final int CARD_W = 240, CARD_H = 36, GAP = 10, LIST_TOP = 70;

    private final List<MenuCatalogSyncS2CPacket.KitSnap> kits = new ArrayList<>();
    private final List<CustomButton> cards = new ArrayList<>();
    private CustomButton backButton;
    private int scrollY;
    private int maxScroll;
    private int listBottom;

    public KitsScreen() {
        super(Component.literal("Киты"));
    }

    @Override
    protected void init() {
        kits.clear();
        kits.addAll(ClientMenuCatalog.kits);
        cards.clear();
        listBottom = height - 48;
        int totalH = kits.isEmpty() ? 0 : kits.size() * (CARD_H + GAP) - GAP;
        maxScroll = Math.max(0, totalH - Math.max(40, listBottom - LIST_TOP));
        scrollY = Mth.clamp(scrollY, 0, maxScroll);

        int x = (width - CARD_W) / 2;
        for (int i = 0; i < kits.size(); i++) {
            MenuCatalogSyncS2CPacket.KitSnap kit = kits.get(i);
            String label = kit.displayName();
            if (kit.cooldownRemain() > 0) {
                label += " §7(" + kit.cooldownRemain() + "с)";
            }
            ItemStack icon = iconFor(kit.iconItemId());
            CustomButton btn = new CustomButton(x, LIST_TOP, CARD_W, CARD_H, Component.literal(label),
                    0xFF3FD16A, b -> NetworkHandler.CHANNEL.sendToServer(new ClaimKitC2SPacket(kit.id())),
                    icon, i * 40);
            btn.active = kit.cooldownRemain() <= 0;
            cards.add(btn);
            addRenderableWidget(btn);
        }
        applyScroll();

        backButton = new CustomButton(20, height - 40, 90, 20, Component.literal("← Назад"),
                0xFF888888, b -> minecraft.setScreen(new MainMenuScreen()), null, 0);
        addRenderableWidget(backButton);
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
        gfx.fillGradient(0, 0, width, height, 0xF0050A10, 0xF0081410);
        gfx.fill(0, 0, width, height, 0x66000000);
        gfx.drawCenteredString(font, Component.literal("§l§fКиты"), width / 2, 30, 0xFFFFFFFF);
        gfx.drawCenteredString(font, Component.literal("§7Нажмите, чтобы получить набор предметов"),
                width / 2, 48, 0xFF8899AA);
        super.render(gfx, mouseX, mouseY, partialTicks);

        if (kits.isEmpty()) {
            gfx.drawCenteredString(font, Component.literal("§7Киты не настроены. config/casesmod/kits.json"),
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
