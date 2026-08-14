package com.casesmod.client.gui;

import com.casesmod.client.ClientMenuCatalog;
import com.casesmod.client.gui.widget.CustomButton;
import com.casesmod.client.gui.widget.OceanParallax;
import com.casesmod.network.NetworkHandler;
import com.casesmod.network.packets.ClaimQuestC2SPacket;
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
import java.util.Comparator;
import java.util.List;

public class QuestsScreen extends Screen {
    private static final int CARD_W = 300, CARD_H = 56, GAP = 10, LIST_TOP = 78;
    private static final int FOOTER_H = 52;

    private final List<MenuCatalogSyncS2CPacket.QuestSnap> quests = new ArrayList<>();
    private final List<CustomButton> cards = new ArrayList<>();
    private final long openedAtMs = System.currentTimeMillis();
    private int scrollY;
    private int maxScroll;
    private int listBottom;
    private CustomButton backBtn;

    public QuestsScreen() {
        super(Component.literal("Квесты"));
    }

    @Override
    protected void init() {
        quests.clear();
        quests.addAll(ClientMenuCatalog.quests);
        quests.sort(Comparator
                .comparingInt(MenuCatalogSyncS2CPacket.QuestSnap::stageOrder)
                .thenComparing(MenuCatalogSyncS2CPacket.QuestSnap::id));

        cards.clear();
        listBottom = height - FOOTER_H;
        int totalH = quests.isEmpty() ? 0 : quests.size() * (CARD_H + GAP) - GAP;
        maxScroll = Math.max(0, totalH - Math.max(40, listBottom - LIST_TOP));
        scrollY = Mth.clamp(scrollY, 0, maxScroll);

        int x = (width - CARD_W) / 2;
        for (int i = 0; i < quests.size(); i++) {
            MenuCatalogSyncS2CPacket.QuestSnap q = quests.get(i);
            ItemStack icon = iconFor(q.iconItemId());
            CustomButton btn = new CustomButton(x, LIST_TOP, CARD_W, CARD_H, Component.empty(),
                    0xFFE0A93F, b -> NetworkHandler.CHANNEL.sendToServer(new ClaimQuestC2SPacket(q.id())),
                    icon, i * 35);
            btn.active = q.complete() && !q.claimed();
            cards.add(btn);
            addRenderableWidget(btn);
        }
        applyScroll();

        backBtn = new CustomButton(20, height - 36, 90, 22, Component.literal("← Назад"),
                0xFF888888, b -> WebOverlay.openMainMenu(minecraft), null, 0);
        addRenderableWidget(backBtn);
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
        if (backBtn != null) {
            backBtn.setY(height - 36);
            backBtn.visible = true;
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

    private static String ellipsize(net.minecraft.client.gui.Font font, String text, int maxPx) {
        if (text == null || text.isEmpty()) return "";
        if (font.width(text) <= maxPx) return text;
        String ell = "…";
        int ellW = font.width(ell);
        StringBuilder sb = new StringBuilder(text);
        while (sb.length() > 0 && font.width(sb.toString()) + ellW > maxPx) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb + ell;
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        float timeSec = (System.currentTimeMillis() - openedAtMs) / 1000f;
        OceanParallax.render(gfx, width, height, timeSec, partialTicks);
        gfx.fill(0, 0, width, height, 0x66000000);

        gfx.drawCenteredString(font, Component.literal("§l§fКонтракты океана"), width / 2, 26, 0xFFFFFFFF);
        gfx.drawCenteredString(font, Component.literal("§7Иди по этапам · сюжет — в книге FTB Quests"),
                width / 2, 42, 0xFF8899AA);
        gfx.drawCenteredString(font, Component.literal("§81 Берег → 2 Улов → 3 Атолл → 4 Индустрия → 5 Глубины"),
                width / 2, 56, 0xFF667788);

        gfx.enableScissor(0, LIST_TOP - 4, width, listBottom);
        super.render(gfx, mouseX, mouseY, partialTicks);

        int x = (width - CARD_W) / 2;
        int textLeft = x + 30;
        int textRight = x + CARD_W - 10;

        for (int i = 0; i < quests.size(); i++) {
            MenuCatalogSyncS2CPacket.QuestSnap q = quests.get(i);
            CustomButton btn = cards.get(i);
            if (!btn.visible) continue;
            int y = btn.getY();

            String stage = q.stage() == null || q.stage().isEmpty() ? "Этап ?" : q.stage();
            gfx.drawString(font, "§8" + stage, textLeft, y + 4, 0xFF8899AA, false);

            String status = q.claimed() ? " §a✓" : q.complete() ? " §eготово" : "";
            String prog = q.progress() + "/" + q.requiredAmount();
            int progW = font.width(prog);
            gfx.drawString(font, prog, textRight - progW, y + 4, 0xFF8899AA, false);

            String title = (q.displayName() == null ? q.id() : q.displayName()) + status;
            title = ellipsize(font, title, textRight - progW - 10 - textLeft);
            gfx.drawString(font, title, textLeft, y + 16, 0xFFFFFFFF, false);

            String desc = q.description() == null ? "" : q.description();
            desc = ellipsize(font, "§7" + desc, textRight - textLeft);
            gfx.drawString(font, desc, textLeft, y + 28, 0xFFAABBCC, false);

            int barX1 = textLeft;
            int barX2 = textRight;
            float pct = q.requiredAmount() > 0 ? Math.min(1f, q.progress() / (float) q.requiredAmount()) : 0f;
            gfx.fill(barX1, y + 42, barX2, y + 47, 0xFF222833);
            gfx.fill(barX1, y + 42, barX1 + (int) ((barX2 - barX1) * pct), y + 47, 0xFF55CC55);
        }
        gfx.disableScissor();

        if (backBtn != null) {
            backBtn.render(gfx, mouseX, mouseY, partialTicks);
        }

        if (maxScroll > 0) {
            int trackX = width - 12;
            int trackTop = LIST_TOP;
            int trackH = listBottom - LIST_TOP;
            gfx.fill(trackX, trackTop, trackX + 3, trackTop + trackH, 0x44223344);
            int thumbH = Math.max(16, trackH * trackH / (trackH + maxScroll));
            int thumbY = trackTop + (int) ((trackH - thumbH) * (scrollY / (float) maxScroll));
            gfx.fill(trackX, thumbY, trackX + 3, thumbY + thumbH, 0xAA5CE1FF);
        }

        if (quests.isEmpty()) {
            gfx.drawCenteredString(font, Component.literal("§7Квесты не настроены. config/casesmod/quests.json"),
                    width / 2, height / 2, 0xFFAAAAAA);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
