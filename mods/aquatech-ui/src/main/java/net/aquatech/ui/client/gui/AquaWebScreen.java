package net.aquatech.ui.client.gui;

import net.aquatech.ui.client.ClientUiState;
import net.aquatech.ui.client.gui.widget.AquaButton;
import net.aquatech.ui.client.gui.widget.AquaGlassPanel;
import net.aquatech.ui.client.render.AquaFontRenderer;
import net.aquatech.ui.client.render.UiDraw;
import net.aquatech.ui.client.web.AquaWebBridge;
import net.aquatech.ui.client.web.AquaWebIpcDispatcher;
import net.aquatech.ui.common.ModClientConfig;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class AquaWebScreen extends AquaBlurredScreen {

    private final String targetUrl;
    private final Component pageTitle;
    private AquaWebBridge bridge;
    private int frameX;
    private int frameY;
    private int frameW;
    private int frameH;
    private int contentX;
    private int contentY;
    private int contentW;
    private int contentH;

    public AquaWebScreen(Component title, String targetUrl) {
        super(title);
        this.pageTitle = title;
        this.targetUrl = targetUrl;
    }

    public static void openEmbed(String title, String page) {
        Minecraft mc = Minecraft.getInstance();
        String base = ModClientConfig.PORTAL_BASE.get();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        String url = base + "/embed/" + page + ".html";
        String nick = mc.player != null ? mc.player.getGameProfile().getName() : "Renfild";
        url += "?nick=" + URLEncoder.encode(nick, StandardCharsets.UTF_8);
        String token = ClientUiState.sessionToken();
        if (token != null && token.length() >= 8 && !token.startsWith("local_")) {
            url += "&session=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
        }
        mc.setScreen(new AquaWebScreen(Component.literal(title), url));
    }

    private void layoutFrame() {
        frameW = Math.min(960, width - 24);
        frameH = Math.min(600, height - 32);
        frameX = (width - frameW) / 2;
        frameY = (height - frameH) / 2;
        contentX = frameX + 2;
        contentY = frameY + 32;
        contentW = frameW - 4;
        contentH = frameH - 34;
    }

    @Override
    protected void init() {
        super.init();
        layoutFrame();
        double scale = minecraft.getWindow().getGuiScale();
        this.bridge = AquaWebBridge.getOrCreate(targetUrl, (int) (contentW * scale), (int) (contentH * scale));

        addRenderableWidget(new AquaButton(
                frameX + frameW - 32, frameY + 5, 24, 22,
                Component.literal("X"),
                this::onClose
        ));
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        layoutFrame();
        if (bridge != null) {
            double scale = minecraft.getWindow().getGuiScale();
            bridge.resize((int) (contentW * scale), (int) (contentH * scale));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (bridge != null) {
            String msg = bridge.pollIpcMessage();
            if (msg != null) {
                AquaWebIpcDispatcher.dispatch(msg);
            }
        }
    }

    private boolean inContent(double mouseX, double mouseY) {
        return mouseX >= contentX && mouseX < contentX + contentW
                && mouseY >= contentY && mouseY < contentY + contentH;
    }

    private int cefX(double mouseX) {
        return (int) ((mouseX - contentX) * minecraft.getWindow().getGuiScale());
    }

    private int cefY(double mouseY) {
        return (int) ((mouseY - contentY) * minecraft.getWindow().getGuiScale());
    }

    @Override
    protected void renderScreenContent(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        layoutFrame();
        AquaGlassPanel.draw(g, frameX, frameY, frameW, frameH, COLOR_GLASS_PANEL, COLOR_CYAN_ACCENT, 4, true);
        g.fill(frameX + 1, frameY + 1, frameX + frameW - 1, frameY + 31, 0xFF0D2136);
        UiDraw.border(g, frameX, frameY, frameW, 31, COLOR_BORDER_MUTED);

        g.drawString(font, AquaFontRenderer.withMain(pageTitle), frameX + 14, frameY + 11, COLOR_CYAN_ACCENT, false);

        if (bridge != null && bridge.isAvailable()) {
            bridge.blit(contentX, contentY, contentW, contentH);
        } else {
            AquaFontRenderer.drawCentered(g, font, "Chromium ещё не готов", frameX + frameW / 2, frameY + frameH / 2 - 10, COLOR_CYAN_ACCENT);
            AquaFontRenderer.drawCentered(g, font, "Нажми «В браузере» или подожди загрузку MCEF", frameX + frameW / 2, frameY + frameH / 2 + 6, COLOR_TEXT_MUTED);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (bridge != null && bridge.isAvailable() && inContent(mouseX, mouseY)) {
            bridge.sendMousePress(cefX(mouseX), cefY(mouseY), button);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (bridge != null && bridge.isAvailable() && inContent(mouseX, mouseY)) {
            bridge.sendMouseRelease(cefX(mouseX), cefY(mouseY), button);
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (bridge != null && bridge.isAvailable() && inContent(mouseX, mouseY)) {
            bridge.sendMouseMove(cefX(mouseX), cefY(mouseY));
        }
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (bridge != null && bridge.isAvailable() && inContent(mouseX, mouseY)) {
            bridge.sendMouseWheel(cefX(mouseX), cefY(mouseY), delta);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (bridge != null && bridge.isAvailable()) {
            bridge.sendKeyPress(keyCode, scanCode, modifiers);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            return super.keyReleased(keyCode, scanCode, modifiers);
        }
        if (bridge != null && bridge.isAvailable()) {
            bridge.sendKeyRelease(keyCode, scanCode, modifiers);
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (bridge != null && bridge.isAvailable() && codePoint != 0) {
            bridge.sendKeyTyped(codePoint, modifiers);
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void onClose() {
        if (bridge != null) {
            bridge.close();
            bridge = null;
        }
        super.onClose();
    }
}
