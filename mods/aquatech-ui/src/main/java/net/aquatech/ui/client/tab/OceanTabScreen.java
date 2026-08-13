package net.aquatech.ui.client.tab;

import net.aquatech.ui.client.ClientUiState;
import net.aquatech.ui.client.gui.AquaBlurredScreen;
import net.aquatech.ui.client.gui.widget.AquaBadge;
import net.aquatech.ui.client.gui.widget.AquaGlassPanel;
import net.aquatech.ui.client.render.AquaFontRenderer;
import net.aquatech.ui.client.render.UiDraw;
import net.aquatech.ui.common.PlayerProfile;
import net.aquatech.ui.common.ServerStats;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public class OceanTabScreen extends AquaBlurredScreen {
    private static final int COLUMNS = 3;
    private static final int ROW_HEIGHT = 42;
    private double scroll;

    public OceanTabScreen() {
        super(Component.literal("AquaTech Tab"));
        setEnableAtmosphericParticles(false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void renderScreenContent(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ServerStats stats = ClientUiState.stats();
        List<PlayerProfile> profiles = ClientUiState.profiles();

        int panelW = Math.min(760, this.width - 40);
        int panelH = Math.min(420, this.height - 60);
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;
        AquaGlassPanel.draw(graphics, panelX, panelY, panelW, panelH, AquaGlassPanel.FILL, AquaGlassPanel.BORDER, 5, true);

        String header = "На сервере " + stats.online() + " игроков";
        AquaFontRenderer.drawCenteredHeader(graphics, this.font, header, this.width / 2, panelY + 12, UiDraw.COLOR_ACCENT);
        String sub = "в том числе " + stats.staffOnline() + " участника команды";
        AquaFontRenderer.drawCentered(graphics, this.font, sub, this.width / 2, panelY + 26, UiDraw.COLOR_MUTED);

        int listTop = panelY + 44;
        int listBottom = panelY + panelH - 12;
        int listHeight = listBottom - listTop;
        int colWidth = (panelW - 24) / COLUMNS;
        int contentHeight = ((profiles.size() + COLUMNS - 1) / COLUMNS) * ROW_HEIGHT;
        int maxScroll = Math.max(0, contentHeight - listHeight);
        scroll = Math.max(0, Math.min(scroll, maxScroll));

        graphics.enableScissor(panelX + 8, listTop, panelX + panelW - 8, listBottom);
        for (int i = 0; i < profiles.size(); i++) {
            PlayerProfile profile = profiles.get(i);
            int col = i % COLUMNS;
            int row = i / COLUMNS;
            int x = panelX + 12 + col * colWidth;
            int y = listTop + row * ROW_HEIGHT - (int) scroll;
            if (y + ROW_HEIGHT < listTop || y > listBottom) {
                continue;
            }
            renderEntry(graphics, profile, x, y, colWidth - 8);
        }
        graphics.disableScissor();

        if (maxScroll > 0) {
            int barX = panelX + panelW - 10;
            graphics.fill(barX, listTop, barX + 4, listBottom, 0x66000000);
            int thumbH = Math.max(20, listHeight * listHeight / Math.max(1, contentHeight));
            int thumbY = listTop + (int) ((scroll / (double) maxScroll) * (listHeight - thumbH));
            graphics.fill(barX, thumbY, barX + 4, thumbY + thumbH, UiDraw.COLOR_ACCENT_DARK);
        }
    }

    private void renderEntry(GuiGraphics graphics, PlayerProfile profile, int x, int y, int width) {
        UiDraw.drawPlayerHead(graphics, profile.uuid(), profile.name(), x, y + 4, 28);
        int textX = x + 34;
        AquaFontRenderer.draw(graphics, this.font, profile.name(), textX, y + 6, UiDraw.COLOR_TEXT);
        int badgeX = textX;
        badgeX += AquaBadge.draw(graphics, this.font, badgeX, y + 20, profile.rankDisplay(), UiDraw.rankColor(profile.rankId())) + 4;
        if (profile.staff()) {
            AquaBadge.draw(graphics, this.font, badgeX, y + 20, "STAFF", UiDraw.COLOR_ACCENT);
        }
        String ping = profile.ping() + "ms";
        AquaFontRenderer.draw(graphics, this.font, ping, x + width - AquaFontRenderer.width(this.font, ping), y + 8, 0xFF55FF55);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scroll -= delta * 18;
        return true;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (this.minecraft.options.keyPlayerList.matches(keyCode, scanCode)) {
            this.minecraft.setScreen(null);
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }
}
