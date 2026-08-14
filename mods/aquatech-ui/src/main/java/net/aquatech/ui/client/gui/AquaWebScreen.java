package net.aquatech.ui.client.gui;

import net.aquatech.ui.client.ClientUiState;
import net.aquatech.ui.client.render.AquaFontRenderer;
import net.aquatech.ui.client.render.UiDraw;
import net.aquatech.ui.client.web.AquaWebBridge;
import net.aquatech.ui.client.web.AquaWebIpcDispatcher;
import net.aquatech.ui.common.ModClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Modern borderless embedded Web Screen.
 * Renders transparent HTML modal UI cleanly over Minecraft with no clunky window chrome.
 */
public class AquaWebScreen extends AquaBlurredScreen {

    private final String targetUrl;
    private final Component pageTitle;
    private AquaWebBridge bridge;

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

    @Override
    protected void init() {
        super.init();
        double scale = minecraft.getWindow().getGuiScale();
        int pw = Math.max(64, (int) (width * scale));
        int ph = Math.max(64, (int) (height * scale));
        this.bridge = AquaWebBridge.getOrCreate(targetUrl, pw, ph);
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        if (bridge != null) {
            double scale = minecraft.getWindow().getGuiScale();
            bridge.resize((int) (width * scale), (int) (height * scale));
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

    private int cefX(double mouseX) {
        return (int) (mouseX * minecraft.getWindow().getGuiScale());
    }

    private int cefY(double mouseY) {
        return (int) (mouseY * minecraft.getWindow().getGuiScale());
    }

    @Override
    protected void renderAtmosphere(GuiGraphics g) {
        // Transparent backdrop: game world remains clear behind the centered web modal
    }

    @Override
    protected void renderScreenContent(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        if (bridge != null && bridge.isAvailable()) {
            bridge.blit(0, 0, width, height);
        } else {
            AquaFontRenderer.drawCentered(g, font, "Загрузка интерфейса…", width / 2, height / 2 - 10, UiDraw.COLOR_PRIMARY);
            AquaFontRenderer.drawCentered(g, font, "Пожалуйста, подождите", width / 2, height / 2 + 6, UiDraw.COLOR_MUTED);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (bridge != null && bridge.isAvailable()) {
            bridge.sendMousePress(cefX(mouseX), cefY(mouseY), button);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (bridge != null && bridge.isAvailable()) {
            bridge.sendMouseRelease(cefX(mouseX), cefY(mouseY), button);
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (bridge != null && bridge.isAvailable()) {
            bridge.sendMouseMove(cefX(mouseX), cefY(mouseY));
        }
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (bridge != null && bridge.isAvailable()) {
            bridge.sendMouseWheel(cefX(mouseX), cefY(mouseY), delta);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            return true;
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
