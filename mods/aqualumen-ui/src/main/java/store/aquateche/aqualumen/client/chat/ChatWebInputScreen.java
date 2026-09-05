package store.aquateche.aqualumen.client.chat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import store.aquateche.aqualumen.client.web.LumenWebBridge;

/**
 * Invisible input screen: while open, all keys go into the CEF chat panel
 * and the game ignores movement keys. Enter is handled by the page itself
 * (it sends {type:'chat.send'}), Esc collapses back to the compact feed.
 */
public final class ChatWebInputScreen extends Screen {

    public ChatWebInputScreen() {
        super(Component.translatable("gui.aqualumen.hub"));
    }

    private LumenWebBridge bridge() {
        return ChatWebOverlay.bridgeForInput();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // intentionally empty: the HUD overlay draws the chat panel behind this screen
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        LumenWebBridge bridge = bridge();
        if (bridge != null) {
            bridge.sendKeyPress(keyCode, scanCode, modifiers);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        LumenWebBridge bridge = bridge();
        if (bridge != null) {
            bridge.sendKeyRelease(keyCode, scanCode, modifiers);
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        LumenWebBridge bridge = bridge();
        if (bridge != null) {
            bridge.sendKeyTyped(codePoint, modifiers);
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void tick() {
        if (bridge() == null) {
            return;
        }
        // panel messages (chat.send / chat.close) route through the shared dispatcher,
        // which collapses the screen itself
        ChatWebOverlay.pollMessages();
    }

    @Override
    public void onClose() {
        ChatWebOverlay.onInputClosed();
        super.onClose();
    }

    private void close() {
        Minecraft.getInstance().setScreen(null);
    }
}
