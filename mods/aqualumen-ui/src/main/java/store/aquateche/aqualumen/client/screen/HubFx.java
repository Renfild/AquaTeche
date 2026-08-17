package store.aquateche.aqualumen.client.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import store.aquateche.aqualumen.client.render.Anim;
import store.aquateche.aqualumen.client.render.Gfx;
import store.aquateche.aqualumen.client.render.HubFont;
import store.aquateche.aqualumen.client.render.Icons;
import store.aquateche.aqualumen.client.theme.LumenTheme;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Screen level motion state: the open animation, the tab transition and the toast stack.
 * Kept static so tab renderers can read it without threading a parameter through every method.
 */
public final class HubFx {

    private static final float TOAST_LIFETIME = 3.2F;
    private static final int TOAST_HEIGHT = 22;
    private static final List<Toast> TOASTS = new ArrayList<>();

    private static float open;
    private static float enter;
    private static int direction = 1;

    private HubFx() {
    }

    /** Called from HubScreen.init so every open replays the intro. */
    public static void reset() {
        open = 0.0F;
        enter = 0.0F;
        direction = 1;
        TOASTS.clear();
    }

    public static void tick(float delta) {
        open = Anim.approach(open, 1.0F, 14.0F, delta);
        enter = Anim.approach(enter, 1.0F, 9.0F, delta);
        for (Iterator<Toast> iterator = TOASTS.iterator(); iterator.hasNext(); ) {
            Toast toast = iterator.next();
            toast.age += delta;
            if (toast.age > TOAST_LIFETIME) {
                iterator.remove();
            }
        }
    }

    /** Content slides in from the direction the player moved in the sidebar. */
    public static void switchTab(int fromIndex, int toIndex) {
        direction = toIndex >= fromIndex ? 1 : -1;
        enter = 0.0F;
    }

    /** 0..1 panel reveal. */
    public static float open() {
        return Anim.enabled() ? Anim.easeOutCubic(open) : 1.0F;
    }

    /** 0..1 content reveal after a tab switch. */
    public static float enter() {
        return Anim.enabled() ? Anim.easeOutCubic(enter) : 1.0F;
    }

    public static int slide(int distance) {
        return Math.round((1.0F - enter()) * distance * direction);
    }

    public static void toast(String message, Icons.Icon icon, int color) {
        if (TOASTS.size() >= 3) {
            TOASTS.remove(0);
        }
        TOASTS.add(new Toast(message, icon, color));
    }

    /** Toasts stack upward from the bottom right corner of the panel. */
    public static void render(GuiGraphics graphics, Font font, LumenTheme theme, int right, int bottom) {
        int y = bottom - TOAST_HEIGHT;
        for (int i = TOASTS.size() - 1; i >= 0; i--) {
            Toast toast = TOASTS.get(i);
            float appear = Anim.easeOutCubic(Math.min(1.0F, toast.age / 0.18F));
            float leave = Anim.clamp01((TOAST_LIFETIME - toast.age) / 0.45F);
            float alpha = Math.min(appear, leave);
            int width = HubFont.width(font, toast.message) + 40;
            int x = right - width + Math.round((1.0F - appear) * 14.0F);

            Gfx.roundedRect(graphics, x, y, width, TOAST_HEIGHT, 10,
                    Anim.fade(Gfx.withAlpha(theme.raised(), 0.96F), alpha));
            Gfx.outline(graphics, x, y, width, TOAST_HEIGHT, 10,
                    Anim.fade(Gfx.withAlpha(toast.color, 0.55F), alpha));
            Icons.drawCentered(graphics, toast.icon, x + 15, y + TOAST_HEIGHT / 2, 10,
                    Anim.fade(toast.color, alpha));
            HubFont.draw(graphics, font, toast.message, x + 26, y + (TOAST_HEIGHT - 8) / 2,
                    Anim.fade(theme.text(), alpha));

            int lifeWidth = Math.round((width - 20) * Anim.clamp01(1.0F - toast.age / TOAST_LIFETIME));
            graphics.fill(x + 10, y + TOAST_HEIGHT - 3, x + 10 + lifeWidth, y + TOAST_HEIGHT - 2,
                    Anim.fade(toast.color, alpha * 0.7F));

            y -= TOAST_HEIGHT + 5;
        }
    }

    private static final class Toast {

        private final String message;
        private final Icons.Icon icon;
        private final int color;
        private float age;

        private Toast(String message, Icons.Icon icon, int color) {
            this.message = message;
            this.icon = icon;
            this.color = color;
        }
    }
}
