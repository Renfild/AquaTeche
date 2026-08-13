package net.aquatech.ui.client.gui;

import net.aquatech.ui.client.gui.widget.AquaButton;
import net.aquatech.ui.client.gui.widget.AquaGlassPanel;
import net.aquatech.ui.client.render.AquaFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Confirm / cancel overlay on world blur. Confirm runs the callback then returns to {@code parent}.
 */
public class AquaDialogScreen extends AquaBlurredScreen {

    private static final int PANEL_W = 320;
    private static final int BTN_W = 120;
    private static final int BTN_H = 22;

    private final Screen parent;
    private final String heading;
    private final String body;
    private final Runnable onConfirm;
    private final String confirmLabel;
    private final String cancelLabel;

    public AquaDialogScreen(Screen parent, String heading, String body, Runnable onConfirm) {
        this(parent, heading, body, "Подтвердить", "Отмена", onConfirm);
    }

    public AquaDialogScreen(Screen parent, String heading, String body, String confirmLabel, String cancelLabel, Runnable onConfirm) {
        super(Component.literal(heading == null ? "" : heading));
        this.parent = parent;
        this.heading = heading == null ? "" : heading;
        this.body = body == null ? "" : body;
        this.confirmLabel = confirmLabel == null || confirmLabel.isEmpty() ? "Подтвердить" : confirmLabel;
        this.cancelLabel = cancelLabel == null || cancelLabel.isEmpty() ? "Отмена" : cancelLabel;
        this.onConfirm = onConfirm;
        setEnableAtmosphericParticles(false);
    }

    public static void confirm(Minecraft mc, Screen parent, String heading, String body, Runnable onConfirm) {
        if (mc == null) {
            return;
        }
        mc.setScreen(new AquaDialogScreen(parent, heading, body, onConfirm));
    }

    private int panelX() {
        return (width - PANEL_W) / 2;
    }

    private int panelH() {
        int wrapped = AquaFontRenderer.wrappedHeight(font, body, PANEL_W - 32);
        return Math.max(128, 56 + wrapped + 44);
    }

    private int panelY() {
        return (height - panelH()) / 2;
    }

    @Override
    protected void init() {
        super.init();
        int px = panelX();
        int py = panelY();
        int ph = panelH();
        int btnY = py + ph - 34;
        addRenderableWidget(new AquaButton(
                px + 16, btnY, BTN_W, BTN_H,
                Component.literal(confirmLabel),
                this::doConfirm
        ));
        addRenderableWidget(new AquaButton(
                px + PANEL_W - 16 - BTN_W, btnY, BTN_W, BTN_H,
                Component.literal(cancelLabel),
                this::returnToParent
        ));
    }

    private void doConfirm() {
        if (onConfirm != null) {
            onConfirm.run();
        }
        returnToParent();
    }

    private void returnToParent() {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(parent);
    }

    @Override
    public void onClose() {
        returnToParent();
    }

    @Override
    protected void renderScreenContent(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int px = panelX();
        int py = panelY();
        int ph = panelH();
        AquaGlassPanel.draw(g, px, py, PANEL_W, ph, AquaGlassPanel.FILL, AquaGlassPanel.BORDER_HOT, 5, true);
        AquaFontRenderer.drawHeader(g, font, heading, px + 16, py + 12, COLOR_CYAN_ACCENT);
        AquaFontRenderer.drawWrapped(g, font, body, px + 16, py + 34, PANEL_W - 32, COLOR_TEXT_MUTED);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
