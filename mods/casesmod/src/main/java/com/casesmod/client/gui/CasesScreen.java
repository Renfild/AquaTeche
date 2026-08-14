package com.casesmod.client.gui;

import com.casesmod.client.ClientBalanceState;
import com.casesmod.client.ClientCaseState;
import com.casesmod.client.gui.widget.CustomButton;
import com.casesmod.client.gui.widget.GlassUI;
import com.casesmod.client.gui.widget.OceanParallax;
import com.casesmod.network.NetworkHandler;
import com.casesmod.network.packets.CaseListSyncS2CPacket;
import com.casesmod.network.packets.OpenCaseC2SPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

/**
 * Список кейсов сервера. Данные приходят по сети (CaseListSyncS2CPacket).
 */
public class CasesScreen extends Screen {
    private static final int CARD_W = 170, CARD_H = 108, GAP = 14, START_Y = 76;
    private static final int FOOTER_H = 52;

    private final List<CaseListSyncS2CPacket.CaseSnapshot> cases = new ArrayList<>();
    private final List<CustomButton> cards = new ArrayList<>();
    private final long openedAtMs = System.currentTimeMillis();
    private long screenOpenTime;
    private int cols = 1;
    private int scrollY;
    private int maxScroll;
    private int listBottom;
    private CustomButton backBtn;

    public CasesScreen() {
        super(Component.literal("Кейсы"));
    }

    private void recomputeScroll() {
        int rows = cols <= 0 ? 0 : (cases.size() + cols - 1) / cols;
        int contentH = Math.max(0, rows * (CARD_H + GAP) - GAP);
        int viewH = Math.max(40, listBottom - START_Y);
        maxScroll = Math.max(0, contentH - viewH);
        scrollY = Mth.clamp(scrollY, 0, maxScroll);
    }

    @Override
    protected void init() {
        cases.clear();
        cases.addAll(ClientCaseState.cases);
        screenOpenTime = System.currentTimeMillis();
        cards.clear();

        listBottom = height - FOOTER_H;
        cols = Math.max(1, (width - 40) / (CARD_W + GAP));
        recomputeScroll();

        int totalW = cols * CARD_W + (cols - 1) * GAP;
        int startX = (width - totalW) / 2;

        for (int i = 0; i < cases.size(); i++) {
            CaseListSyncS2CPacket.CaseSnapshot def = cases.get(i);
            int col = i % cols, row = i / cols;
            int x = startX + col * (CARD_W + GAP);
            int y = START_Y + row * (CARD_H + GAP) - scrollY;
            CustomButton btn = new CustomButton(x, y, CARD_W, CARD_H, Component.literal(""),
                    GlassUI.AQUA, b -> openCase(def), null, i * 45);
            btn.active = def.availableNow();
            cards.add(btn);
            addRenderableWidget(btn);
        }
        applyScroll();

        backBtn = new CustomButton(20, height - 36, 90, 22, Component.literal("← Назад"),
                0xFF888888, b -> WebOverlay.openMainMenu(minecraft), null, 0);
        addRenderableWidget(backBtn);
    }

    private void applyScroll() {
        int totalW = cols * CARD_W + (cols - 1) * GAP;
        int startX = (width - totalW) / 2;
        for (int i = 0; i < cards.size(); i++) {
            CustomButton btn = cards.get(i);
            int col = i % cols, row = i / cols;
            int x = startX + col * (CARD_W + GAP);
            int y = START_Y + row * (CARD_H + GAP) - scrollY;
            btn.setX(x);
            btn.setY(y);
            btn.visible = y + CARD_H > START_Y - 4 && y < listBottom + 4;
        }
        if (backBtn != null) {
            backBtn.setY(height - 36);
            backBtn.visible = true;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (maxScroll <= 0) return super.mouseScrolled(mouseX, mouseY, delta);
        int before = scrollY;
        scrollY = Mth.clamp(scrollY - (int) (delta * 22), 0, maxScroll);
        if (scrollY != before) {
            applyScroll();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private void openCase(CaseListSyncS2CPacket.CaseSnapshot def) {
        if (!def.availableNow()) {
            minecraft.player.sendSystemMessage(Component.literal("§cЭтот кейс сейчас недоступен."));
            return;
        }
        if (def.ownedCount() <= 0 && def.price() > 0 && def.price() > ClientBalanceState.balance) {
            minecraft.player.sendSystemMessage(Component.literal(
                    "§cНет кейса и недостаточно средств! Нужно §f" + def.price()
                            + "§c, у вас §f" + ClientBalanceState.balance));
            return;
        }
        NetworkHandler.CHANNEL.sendToServer(new OpenCaseC2SPacket(def.id()));
    }

    private static float easeOutCubic(float t) {
        float p = t - 1;
        return p * p * p + 1;
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        float timeSec = (System.currentTimeMillis() - openedAtMs) / 1000f;
        OceanParallax.render(gfx, width, height, timeSec, partialTicks);
        gfx.fill(0, 0, width, height, 0x66000000);

        gfx.drawCenteredString(font, Component.literal("§l§fКейсы сервера"), width / 2, 28, 0xFFFFFFFF);
        String balanceText = "§7Ваш баланс: §b✦ §f" + ClientBalanceState.balance;
        gfx.drawCenteredString(font, Component.literal(balanceText), width / 2, 46, 0xFFAAAAAA);

        // Cards clipped above the footer so they never cover «Назад»
        gfx.enableScissor(0, START_Y - 4, width, listBottom);
        super.render(gfx, mouseX, mouseY, partialTicks);

        int totalW = cols * CARD_W + (cols - 1) * GAP;
        int startX = (width - totalW) / 2;
        long now = System.currentTimeMillis();

        for (int i = 0; i < cases.size(); i++) {
            CaseListSyncS2CPacket.CaseSnapshot def = cases.get(i);
            CustomButton btn = cards.get(i);
            if (!btn.visible) continue;

            int x = btn.getX();
            int y = btn.getY();

            long elapsed = now - screenOpenTime - (i * 45L);
            float appear = elapsed <= 0 ? 0f : Math.min(1f, elapsed / 260f);
            if (appear <= 0f) continue;
            float eased = easeOutCubic(appear);
            int yOffset = (int) ((1f - eased) * 16f);
            int alphaMul = def.availableNow() ? 255 : 140;
            int alpha = (int) (alphaMul * eased);
            int textColor = (alpha << 24) | 0xFFFFFF;

            int cy = y + yOffset;
            if (cy + CARD_H < START_Y - 8 || cy > listBottom) continue;

            try {
                var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
                        new net.minecraft.resources.ResourceLocation(def.iconItemId()));
                var stack = new net.minecraft.world.item.ItemStack(item);
                float iconAlpha = alpha / 255f;
                com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1f, 1f, 1f, iconAlpha);
                gfx.renderItem(stack, x + CARD_W / 2 - 8, cy + 8);
                com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            } catch (Exception ignored) {
            }

            String priceLabel = def.price() > 0 ? ("§b✦ " + def.price()) : "§aБЕСПЛАТНО";
            gfx.drawCenteredString(font, Component.literal(def.displayName()), x + CARD_W / 2, cy + 30, textColor);
            gfx.drawCenteredString(font, Component.literal(priceLabel), x + CARD_W / 2, cy + 44, textColor);

            String ownedLabel = def.ownedCount() > 0
                    ? ("§aУ вас: ×" + def.ownedCount())
                    : "§8У вас: ×0";
            gfx.drawCenteredString(font, Component.literal(ownedLabel), x + CARD_W / 2, cy + 58, textColor);

            if (!def.availableNow()) {
                gfx.drawCenteredString(font, Component.literal("§c🔒 Недоступен"),
                        x + CARD_W / 2, cy + CARD_H - 16, (alpha << 24) | 0xFF5555);
            } else if (def.pityThreshold() > 0) {
                String pityText = "§dPity " + def.pityProgress() + "/" + def.pityThreshold()
                        + " → " + def.pityRarity();
                gfx.drawCenteredString(font, Component.literal(pityText),
                        x + CARD_W / 2, cy + CARD_H - 16, (alpha << 24) | 0xDDAAFF);
            } else {
                gfx.drawCenteredString(font, Component.literal("§8Pity выкл"),
                        x + CARD_W / 2, cy + CARD_H - 16, (alpha << 24) | 0x888888);
            }
        }
        gfx.disableScissor();

        // Back button outside scissor
        if (backBtn != null) {
            backBtn.render(gfx, mouseX, mouseY, partialTicks);
        }

        if (maxScroll > 0) {
            int trackX = width - 12;
            int trackTop = START_Y;
            int trackH = listBottom - START_Y;
            gfx.fill(trackX, trackTop, trackX + 3, trackTop + trackH, 0x44223344);
            int thumbH = Math.max(18, trackH * trackH / (trackH + maxScroll));
            int thumbY = trackTop + (int) ((trackH - thumbH) * (scrollY / (float) maxScroll));
            gfx.fill(trackX, thumbY, trackX + 3, thumbY + thumbH, 0xAA5CE1FF);
        }

        if (cases.isEmpty()) {
            gfx.drawCenteredString(font, Component.literal("§7Список кейсов ещё грузится или пуст на сервере."),
                    width / 2, height / 2, 0xFFAAAAAA);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
