package net.aquatech.ui.client.web;

import com.cinemamod.mcef.MCEFBrowser;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.aquatech.ui.AquaTechUI;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public final class AquaWebBridge {

    private static final ResourceLocation FALLBACK_TEX = new ResourceLocation("aquatech_ui", "textures/gui/web_fallback.png");
    private static final String BRIDGE_JS = """
            if(!window.AquaTechBridge){window.AquaTechBridge={send:function(msg){
            var s=typeof msg==='string'?msg:JSON.stringify(msg);
            location.hash='aqipc='+encodeURIComponent(s);}};}
            """;

    private static AquaWebBridge instance;

    private final String currentUrl;
    private int pixelW;
    private int pixelH;
    private boolean loaded;
    private boolean cef;
    private MCEFBrowser browser;
    private String lastIpcHash = "";
    private boolean jsInjected;

    private AquaWebBridge(String initialUrl, int pixelW, int pixelH) {
        this.currentUrl = initialUrl;
        this.pixelW = Math.max(64, pixelW);
        this.pixelH = Math.max(64, pixelH);
        initBrowserEngine();
    }

    public static synchronized AquaWebBridge getOrCreate(String url, int pixelW, int pixelH) {
        if (instance == null || !instance.currentUrl.equals(url)) {
            if (instance != null) {
                instance.close();
            }
            instance = new AquaWebBridge(url, pixelW, pixelH);
        } else {
            instance.resize(pixelW, pixelH);
        }
        return instance;
    }

    private void initBrowserEngine() {
        try {
            Class.forName("com.cinemamod.mcef.MCEF");
        } catch (ClassNotFoundException e) {
            AquaTechUI.LOGGER.warn("[cef] mcef not on classpath");
            this.cef = false;
            this.loaded = true;
            return;
        }
        this.browser = CefHost.create(currentUrl, pixelW, pixelH);
        this.cef = this.browser != null;
        this.loaded = true;
        if (!cef) {
            AquaTechUI.LOGGER.warn("[cef] browser not created (CEF still downloading?)");
        }
    }

    public void resize(int pixelW, int pixelH) {
        this.pixelW = Math.max(64, pixelW);
        this.pixelH = Math.max(64, pixelH);
        if (browser != null) {
            browser.resize(this.pixelW, this.pixelH);
        }
    }

    public String pollIpcMessage() {
        if (browser == null) {
            return null;
        }
        if (!jsInjected) {
            try {
                browser.executeJavaScript(BRIDGE_JS, currentUrl, 0);
                jsInjected = true;
            } catch (Exception e) {
                AquaTechUI.LOGGER.debug("[cef] js inject: {}", e.toString());
            }
        }
        String url;
        try {
            url = browser.getURL();
        } catch (Exception e) {
            return null;
        }
        if (url == null) {
            return null;
        }
        int hashAt = url.indexOf("#aqipc=");
        if (hashAt < 0) {
            return null;
        }
        String encoded = url.substring(hashAt + 7);
        int amp = encoded.indexOf('&');
        if (amp >= 0) {
            encoded = encoded.substring(0, amp);
        }
        if (encoded.equals(lastIpcHash)) {
            return null;
        }
        lastIpcHash = encoded;
        try {
            browser.executeJavaScript("history.replaceState(null,'',location.pathname+location.search)", currentUrl, 0);
        } catch (Exception e) {
            AquaTechUI.LOGGER.debug("[cef] hash clear: {}", e.toString());
        }
        try {
            return URLDecoder.decode(encoded, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return encoded;
        }
    }

    public void blit(int guiX, int guiY, int guiW, int guiH) {
        if (browser == null) {
            return;
        }
        int tex = browser.getRenderer().getTextureID();
        if (tex == 0) {
            return;
        }
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, tex);
        Tesselator t = Tesselator.getInstance();
        BufferBuilder buffer = t.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.vertex(guiX, guiY + guiH, 0).uv(0f, 1f).color(255, 255, 255, 255).endVertex();
        buffer.vertex(guiX + guiW, guiY + guiH, 0).uv(1f, 1f).color(255, 255, 255, 255).endVertex();
        buffer.vertex(guiX + guiW, guiY, 0).uv(1f, 0f).color(255, 255, 255, 255).endVertex();
        buffer.vertex(guiX, guiY, 0).uv(0f, 0f).color(255, 255, 255, 255).endVertex();
        t.end();
        RenderSystem.setShaderTexture(0, 0);
        RenderSystem.enableDepthTest();
    }

    public void sendMouseMove(int px, int py) {
        if (browser != null) {
            browser.sendMouseMove(px, py);
        }
    }

    public void sendMousePress(int px, int py, int button) {
        if (browser != null) {
            browser.sendMousePress(px, py, button);
            browser.setFocus(true);
        }
    }

    public void sendMouseRelease(int px, int py, int button) {
        if (browser != null) {
            browser.sendMouseRelease(px, py, button);
            browser.setFocus(true);
        }
    }

    public void sendMouseWheel(int px, int py, double delta) {
        if (browser != null) {
            browser.sendMouseWheel(px, py, delta, 0);
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
            browser.setFocus(true);
        }
    }

    public void sendKeyTyped(char codePoint, int modifiers) {
        if (browser != null && codePoint != 0) {
            browser.sendKeyTyped(codePoint, modifiers);
            browser.setFocus(true);
        }
    }

    public ResourceLocation getTextureLocation() {
        return FALLBACK_TEX;
    }

    public boolean isAvailable() {
        return cef;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void close() {
        if (browser != null) {
            try {
                browser.close();
            } catch (Exception e) {
                AquaTechUI.LOGGER.debug("[cef] close: {}", e.toString());
            }
            browser = null;
        }
        loaded = false;
        instance = null;
    }
}
