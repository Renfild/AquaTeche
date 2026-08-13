package net.aquatech.ui.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.aquatech.ui.AquaTechUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Own PostChain gaussian blur (LoliLand IBlurredGui analogue).
 * Does not steal {@link net.minecraft.client.renderer.GameRenderer#loadEffect} —
 * that path kills entity outlines / fabulous graphics on GUI close.
 */
public final class OceanBlurEngine {

    public static final ResourceLocation CHAIN_ID =
            new ResourceLocation(AquaTechUI.MOD_ID, "shaders/post/ocean_blur.json");

    private static final float TARGET_RADIUS = 12f;
    private static final long FADE_MS = 180L;

    private static PostChain chain;
    private static boolean wanted;
    private static long fadeStartMs;
    private static int lastW = -1;
    private static int lastH = -1;
    private static Field passesField;

    private OceanBlurEngine() {
    }

    public static void setWanted(boolean blur) {
        if (blur == wanted) {
            return;
        }
        wanted = blur;
        fadeStartMs = System.currentTimeMillis();
        if (!blur) {
            closeChain();
        }
    }

    public static boolean isWanted() {
        return wanted;
    }

    /**
     * Call from {@code ScreenEvent.Render.Pre} after the world is in the main target.
     */
    public static void process(float partialTick) {
        if (!wanted) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.gameRenderer == null) {
            return;
        }

        int w = mc.getWindow().getWidth();
        int h = mc.getWindow().getHeight();
        if (chain == null) {
            loadChain(mc);
        }
        if (chain == null) {
            return;
        }
        if (w != lastW || h != lastH) {
            chain.resize(w, h);
            lastW = w;
            lastH = h;
        }

        float t = Math.min(1f, (System.currentTimeMillis() - fadeStartMs) / (float) FADE_MS);
        float radius = TARGET_RADIUS * easeOut(t);
        applyRadius(radius);

        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        chain.process(partialTick);
        mc.getMainRenderTarget().bindWrite(false);
    }

    private static void loadChain(Minecraft mc) {
        closeChain();
        try {
            chain = new PostChain(
                    mc.getTextureManager(),
                    mc.getResourceManager(),
                    mc.getMainRenderTarget(),
                    CHAIN_ID
            );
            lastW = mc.getWindow().getWidth();
            lastH = mc.getWindow().getHeight();
            chain.resize(lastW, lastH);
        } catch (Exception e) {
            AquaTechUI.LOGGER.warn("[OceanBlur] PostChain load failed: {}", e.toString());
            chain = null;
        }
    }

    private static void applyRadius(float radius) {
        if (chain == null) {
            return;
        }
        try {
            if (passesField == null) {
                passesField = PostChain.class.getDeclaredField("passes");
                passesField.setAccessible(true);
            }
            @SuppressWarnings("unchecked")
            List<PostPass> passes = (List<PostPass>) passesField.get(chain);
            for (PostPass pass : passes) {
                var u = pass.getEffect().getUniform("Radius");
                if (u != null) {
                    u.set(radius);
                }
            }
        } catch (ReflectiveOperationException e) {
            AquaTechUI.LOGGER.debug("[OceanBlur] radius uniform: {}", e.toString());
        }
    }

    private static void closeChain() {
        if (chain != null) {
            chain.close();
            chain = null;
        }
        lastW = -1;
        lastH = -1;
    }

    private static float easeOut(float t) {
        float p = t - 1f;
        return p * p * p + 1f;
    }
}
