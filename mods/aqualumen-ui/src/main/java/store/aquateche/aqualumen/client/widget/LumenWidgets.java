package store.aquateche.aqualumen.client.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import store.aquateche.aqualumen.client.render.Anim;
import store.aquateche.aqualumen.client.render.Gfx;
import store.aquateche.aqualumen.client.render.HubFont;
import store.aquateche.aqualumen.client.render.Icons;
import store.aquateche.aqualumen.client.theme.LumenTheme;

import java.util.function.Consumer;

/** Reusable widgets of the hub: sidebar item, pill button and card headers. */
public final class LumenWidgets {

    private LumenWidgets() {
    }

    /** Sidebar navigation entry with an accent marker and hover glow. */
    public static final class NavButton extends AbstractWidget {

        private final LumenTheme theme;
        private final Consumer<NavButton> onPress;
        private String badge;
        private final Icons.Icon icon;
        private boolean selected;
        private float animation;
        private float press;
        private float life;

        public NavButton(int x, int y, int width, int height, Component title, String badge,
                         Icons.Icon icon, LumenTheme theme, Consumer<NavButton> onPress) {
            super(x, y, width, height, title);
            this.theme = theme;
            this.badge = badge != null ? badge : "";
            this.icon = icon;
            this.onPress = onPress;
        }

        public void setBadge(String badge) {
            this.badge = badge != null ? badge : "";
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public boolean isSelected() {
            return selected;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            float delta = Anim.delta();
            life += delta;
            float target = selected ? 1.0F : isHovered() ? 0.55F : 0.0F;
            animation = Anim.approach(animation, target, 16.0F, delta);
            press = Anim.approach(press, 0.0F, 9.0F, delta);

            int shift = Math.round(animation * 2.0F - press * 1.5F);
            if (animation > 0.01F) {
                Gfx.roundedRect(graphics, getX(), getY(), getWidth(), getHeight(), 10,
                        Gfx.withAlpha(theme.accent(), 0.10F * animation + 0.06F * press));
                int barHeight = Math.max(2, Math.round((getHeight() - 12) * Anim.easeOutCubic(animation)));
                Gfx.roundedRect(graphics, getX(), getY() + (getHeight() - barHeight) / 2, 3, barHeight, 2,
                        Gfx.lerpColor(theme.accent(), theme.accentAlt(), 0.5F));
            }

            int textColor = selected ? theme.text() : Gfx.lerpColor(theme.textDim(), theme.text(), animation);
            Font font = Minecraft.getInstance().font;
            Icons.drawCentered(graphics, icon, getX() + 14 + shift, getY() + getHeight() / 2, 8,
                    Gfx.lerpColor(theme.textDim(), theme.accent(), animation));
            HubFont.draw(graphics, font, getMessage(), getX() + 24 + shift,
                    getY() + (getHeight() - 8) / 2, textColor);

            if (!badge.isEmpty()) {
                float beat = Anim.pulse(life * 20.0F, 0.16F);
                int badgeWidth = HubFont.width(font, badge) + 10;
                int badgeX = getX() + getWidth() - badgeWidth - 8;
                int badgeY = getY() + (getHeight() - 14) / 2;
                Gfx.roundedRect(graphics, badgeX, badgeY, badgeWidth, 14, 7,
                        Gfx.withAlpha(theme.accentAlt(), 0.16F + 0.14F * beat));
                HubFont.draw(graphics, font, badge, badgeX + 5, badgeY + 3, theme.accentAlt());
            }
        }

        public void onPress() {
            press = 1.0F;
            onPress.accept(this);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            onPress();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }

    /** Primary / secondary pill button. */
    public static final class PillButton extends AbstractWidget {

        private final LumenTheme theme;
        private final boolean primary;
        private final Runnable onPress;
        private float hover;
        private float press;
        // Busy state: draws a spinning arc instead of the label while a request runs.
        private int busyTicks;

        /** Marks the button busy for ~1s so the player sees the action fired. */
        public void showBusy() {
            this.busyTicks = 20;
        }

        public PillButton(int x, int y, int width, int height, Component title, boolean primary,
                          LumenTheme theme, Runnable onPress) {
            super(x, y, width, height, title);
            this.theme = theme;
            this.primary = primary;
            this.onPress = onPress;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            float delta = Anim.delta();
            hover = Anim.approach(hover, isHovered() ? 1.0F : 0.0F, 16.0F, delta);
            press = Anim.approach(press, 0.0F, 10.0F, delta);
            if (busyTicks > 0) busyTicks--;

            int radius = getHeight() / 2;
            int y = getY() + Math.round(press);
            Font font = Minecraft.getInstance().font;

            if (busyTicks > 0) {
                // Busy spinner: rotating arc in accent colour
                Gfx.roundedRect(graphics, getX(), y, getWidth(), getHeight(), radius,
                        Gfx.withAlpha(theme.raised(), 0.75F));
                Gfx.outline(graphics, getX(), y, getWidth(), getHeight(), radius, theme.border());
                float angle = (System.nanoTime() % 1_000_000_000L) / 1_000_000_000.0F * (float) Math.PI * 2.0F;
                graphics.pose().pushPose();
                graphics.pose().translate(getX() + getWidth() / 2.0F, y + getHeight() / 2.0F, 0);
                graphics.pose().mulPose(com.mojang.math.Axis.YP.rotationDegrees((float) Math.toDegrees(angle)));
                Gfx.outline(graphics, -5, -5, 10, 10, 5,
                        Gfx.withAlpha(theme.accent(), 0.9F));
                graphics.pose().popPose();
                return;
            }

            if (primary) {
                if (hover > 0.02F) {
                    Gfx.glow(graphics, getX(), y, getWidth(), getHeight(), radius, theme.accent(),
                            Math.max(1, Math.round(hover * 4.0F)));
                }
                Gfx.gradientRoundedH(graphics, getX(), y, getWidth(), getHeight(), radius,
                        Gfx.lerpColor(theme.accent(), theme.accentAlt(), 0.2F * hover),
                        Gfx.lerpColor(theme.accentAlt(), theme.accent(), 0.2F * hover));
                HubFont.centered(graphics, font, getMessage(),
                        getX() + getWidth() / 2, y + (getHeight() - 8) / 2, 0xFF08131A);
            } else {
                Gfx.roundedRect(graphics, getX(), y, getWidth(), getHeight(), radius,
                        Gfx.withAlpha(theme.raised(), 0.75F + 0.25F * hover));
                Gfx.outline(graphics, getX(), y, getWidth(), getHeight(), radius,
                        Gfx.lerpColor(theme.border(), Gfx.withAlpha(theme.accent(), 0.5F), hover));
                HubFont.centered(graphics, font, getMessage(),
                        getX() + getWidth() / 2, y + (getHeight() - 8) / 2,
                        Gfx.lerpColor(theme.text(), theme.accent(), hover * 0.6F));
            }
        }

        public void onPress() {
            press = 1.0F;
            onPress.run();
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            onPress();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }

    /** Card surface with an optional caption in the top left corner. */
    public static void card(GuiGraphics graphics, LumenTheme theme, int x, int y, int width, int height, String caption) {
        Gfx.roundedRect(graphics, x, y, width, height, 12, theme.shade(theme.raised(), 0.92F));
        Gfx.outline(graphics, x, y, width, height, 12, theme.border());
        if (caption != null && !caption.isEmpty()) {
            HubFont.draw(graphics, Minecraft.getInstance().font, caption, x + 14, y + 12, theme.textDim());
        }
    }

    /** Compact statistic tile: value on top, label below. */
    public static void statTile(GuiGraphics graphics, LumenTheme theme, int x, int y, int width, int height,
                                String value, String label, int accent, Icons.Icon icon) {
        Gfx.roundedRect(graphics, x, y, width, height, 10, theme.shade(theme.surface(), 0.85F));
        Gfx.outline(graphics, x, y, width, height, 10, theme.border());
        Font font = Minecraft.getInstance().font;
        Icons.drawCentered(graphics, icon, x + width - 13, y + 14, 8, Gfx.withAlpha(accent, 0.7F));
        HubFont.draw(graphics, font, value, x + 12, y + 12, accent);
        HubFont.draw(graphics, font, label, x + 12, y + 26, theme.textDim());
    }
}
