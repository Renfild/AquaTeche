package net.aquatech.ui.client.gui;

import net.aquatech.ui.client.ClientUiState;
import net.aquatech.ui.client.gui.widget.AquaButton;
import net.aquatech.ui.client.gui.widget.AquaGlassPanel;
import net.aquatech.ui.client.render.AquaFontRenderer;
import net.aquatech.ui.client.render.UiDraw;
import net.aquatech.ui.skyblock.IslandLimiterRules;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.Map;

public class IslandLimiterScreen extends AquaBlurredScreen {

    public IslandLimiterScreen() {
        super(Component.literal("Лимиты острова"));
        setEnableAtmosphericParticles(false);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new AquaButton(width / 2 - 50, height / 2 + 88, 100, 22,
                Component.literal("Закрыть"), this::onClose));
    }

    @Override
    protected void renderScreenContent(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int panelW = 340;
        Map<String, Integer> caps = IslandLimiterRules.allMax();
        int rows = caps.size();
        int panelH = 56 + rows * 28 + 40;
        int x = (width - panelW) / 2;
        int y = (height - panelH) / 2;
        AquaGlassPanel.draw(g, x, y, panelW, panelH, AquaGlassPanel.FILL, AquaGlassPanel.BORDER_HOT, 5, true);
        AquaFontRenderer.drawCenteredHeader(g, font, "Лимиты машин", x + panelW / 2, y + 12, COLOR_CYAN_ACCENT);
        AquaFontRenderer.drawCentered(g, font, "Считается на вашем плоту", x + panelW / 2, y + 28, COLOR_TEXT_MUTED);

        int rowY = y + 48;
        for (Map.Entry<String, Integer> e : caps.entrySet()) {
            String id = e.getKey();
            int placed = ClientUiState.limiterPlaced(id);
            int max = ClientUiState.limiterMax(id);
            if (max <= 0) {
                max = e.getValue();
            }
            AquaGlassPanel.draw(g, x + 16, rowY, panelW - 32, 24, AquaGlassPanel.FILL_LIGHT, AquaGlassPanel.BORDER, 3, false);
            AquaFontRenderer.draw(g, font, IslandLimiterRules.title(id), x + 24, rowY + 8, UiDraw.COLOR_TEXT);
            String frac = placed + " / " + max;
            int color = placed >= max ? 0xFFFF6B6B : 0xFF63E6A5;
            AquaFontRenderer.draw(g, font, frac, x + panelW - 24 - AquaFontRenderer.width(font, frac), rowY + 8, color);
            rowY += 28;
        }
    }
}
