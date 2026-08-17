package store.aquateche.aqualumen.client.web;

import com.cinemamod.mcef.MCEFBrowser;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import store.aquateche.aqualumen.AquaLumenUI;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public final class LumenWebBridge {

    public static final String HUB_URL = "mod://aqualumen/html/hub.html";
    private static final String IPC_MARKER = "#lumenipc=";
    private static final String BRIDGE_SCRIPT = """
            if (!window.AquaLumenBridge) {
              window.AquaLumenBridge = {
                send: function(message) {
                  var encoded = typeof message === 'string' ? message : JSON.stringify(message);
                  location.hash = 'lumenipc=' + encodeURIComponent(encoded);
                }
              };
            }
            if (window.AquaLumen) {
              window.AquaLumenBridge.send({type:'ready'});
            }
            """;

    private final String url;
    private MCEFBrowser browser;
    private String lastMessage = "";
    private boolean bridgeInjected;

    public LumenWebBridge(int pixelWidth, int pixelHeight) {
        this.url = HUB_URL;
        this.browser = LumenCefHost.create(url, pixelWidth, pixelHeight);
    }

    public boolean isAvailable() {
        return browser != null;
    }

    public void resize(int pixelWidth, int pixelHeight) {
        if (browser != null) {
            browser.resize(Math.max(64, pixelWidth), Math.max(64, pixelHeight));
        }
    }

    public String pollMessage() {
        if (browser == null) {
            return null;
        }
        String currentUrl;
        try {
            currentUrl = browser.getURL();
        } catch (RuntimeException error) {
            return null;
        }
        if (currentUrl == null) {
            return null;
        }
        if (!bridgeInjected && currentUrl.startsWith(url)) {
            execute(BRIDGE_SCRIPT);
            bridgeInjected = true;
        }
        int markerAt = currentUrl.indexOf(IPC_MARKER);
        if (markerAt < 0) {
            return null;
        }
        String encoded = currentUrl.substring(markerAt + IPC_MARKER.length());
        int ampersand = encoded.indexOf('&');
        if (ampersand >= 0) {
            encoded = encoded.substring(0, ampersand);
        }
        if (encoded.equals(lastMessage)) {
            return null;
        }
        lastMessage = encoded;
        execute("history.replaceState(null,'',location.pathname+location.search)");
        try {
            return URLDecoder.decode(encoded, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException error) {
            AquaLumenUI.LOGGER.debug("[AquaLumen CEF] malformed IPC: {}", error.toString());
            return null;
        }
    }

    public void execute(String script) {
        if (browser == null) {
            return;
        }
        try {
            browser.executeJavaScript(script, url, 0);
        } catch (RuntimeException error) {
            AquaLumenUI.LOGGER.debug("[AquaLumen CEF] JavaScript call failed: {}", error.toString());
        }
    }

    public void blit(int x, int y, int width, int height) {
        if (browser == null) {
            return;
        }
        int texture = browser.getRenderer().getTextureID();
        if (texture == 0) {
            return;
        }

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.vertex(x, y + height, 0).uv(0.0F, 1.0F).color(255, 255, 255, 255).endVertex();
        buffer.vertex(x + width, y + height, 0).uv(1.0F, 1.0F).color(255, 255, 255, 255).endVertex();
        buffer.vertex(x + width, y, 0).uv(1.0F, 0.0F).color(255, 255, 255, 255).endVertex();
        buffer.vertex(x, y, 0).uv(0.0F, 0.0F).color(255, 255, 255, 255).endVertex();
        tessellator.end();

        RenderSystem.setShaderTexture(0, 0);
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    public void sendMouseMove(int x, int y) {
        if (browser != null) {
            browser.sendMouseMove(x, y);
        }
    }

    public void sendMousePress(int x, int y, int button) {
        if (browser != null) {
            browser.sendMousePress(x, y, button);
            browser.setFocus(true);
        }
    }

    public void sendMouseRelease(int x, int y, int button) {
        if (browser != null) {
            browser.sendMouseRelease(x, y, button);
            browser.setFocus(true);
        }
    }

    public void sendMouseWheel(int x, int y, double delta) {
        if (browser != null) {
            browser.sendMouseWheel(x, y, delta, 0);
        }
    }

    public void sendKeyPress(int keyCode, int scanCode, int modifiers) {
        if (browser != null) {
            browser.sendKeyPress(keyCode, scanCode, modifiers);
            browser.setFocus(true);
        }
    }

    public void sendKeyRelease(int keyCode, int scanCode, int modifiers) {
        if (browser != null) {
            browser.sendKeyRelease(keyCode, scanCode, modifiers);
        }
    }

    public void sendKeyTyped(char codePoint, int modifiers) {
        if (browser != null && codePoint != 0) {
            browser.sendKeyTyped(codePoint, modifiers);
        }
    }

    public void close() {
        if (browser == null) {
            return;
        }
        try {
            browser.close();
        } catch (RuntimeException error) {
            AquaLumenUI.LOGGER.debug("[AquaLumen CEF] close failed: {}", error.toString());
        } finally {
            browser = null;
        }
    }
}
