package net.aquatech.ui.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.aquatech.ui.client.ClientUiState;
import net.aquatech.ui.client.render.AquaFontRenderer;
import net.aquatech.ui.client.render.LumenGfx;
import net.aquatech.ui.client.render.LumenIcons;
import net.aquatech.ui.client.render.UiDraw;
import net.aquatech.ui.client.theme.LumenTheme;
import net.aquatech.ui.common.ModClientConfig;
import net.aquatech.ui.common.PlayerProfile;
import net.aquatech.ui.server.PressureBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.network.chat.Component;

/**
 * HUD "data first": one flat panel per context, hero numbers instead of
 * label:value tables.
 * 1. Profile panel — avatar + name + rank pill, balance hero, playtime.
 * 2. Dive panel — collapsed to a slim "на поверхности" line on the surface,
 *    expands underwater: depth hero, pressure status pill, oxygen bar.
 */
public final class OceanHudOverlay {

    private static final ResourceLocation COIN_TEXTURE =
            new ResourceLocation("aquatech_ui", "textures/gui/coin.png");

    private OceanHudOverlay() {
    }

    public static void render(GuiGraphics graphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui) {
            return;
        }
        if (!ModClientConfig.HUD_VISIBLE.get()) {
            return;
        }
        if (mc.screen != null || ClientUiState.tabOpen()) {
            return;
        }

        LumenTheme theme = LumenTheme.get();
        Font font = mc.font;

        int w = Math.max(160, ModClientConfig.HUD_WIDTH.get());
        float scale = (float) ModClientConfig.HUD_SCALE.get().doubleValue();
        int marginRight = ModClientConfig.HUD_MARGIN_RIGHT.get();
        int marginTop = ModClientConfig.HUD_MARGIN_TOP.get();
        boolean showPressure = ModClientConfig.SHOW_PRESSURE.get();

        int screenX = graphics.guiWidth() - Math.round(w * scale) - marginRight;
        int screenY = marginTop;

        PlayerProfile profile = ClientUiState.profile(player.getUUID());
        String rankTitle = profile != null ? profile.rankDisplay() : "";
        rankTitle = rankTitle == null ? "" : rankTitle.replaceAll("[\\uE000-\\uF8FF\\uD800-\\uDFFF]", "").trim();
        if (rankTitle.isBlank()) rankTitle = LumenTheme.getRankTitle(profile != null ? profile.rankId() : "player");
        int rankColor = LumenTheme.getRankColor(profile != null ? profile.rankId() : "player");
        String rankGlyph = LumenTheme.getRankGlyph(profile != null ? profile.rankId() : "player");

        PressureBridge.PressureInfo live = PressureBridge.fromPlayer(player);
        boolean inWater = live.inWater()
                || player.isInWater()
                || player.isEyeInFluid(FluidTags.WATER);
        int depth = inWater ? Math.max(0, PressureBridge.SEA_LEVEL_Y - player.blockPosition().getY()) : 0;
        int pressure = inWater ? live.effective() : 0;
        int tolerance = live.tolerance();
        if (inWater && live.depth() == 0 && depth > 0) {
            pressure = Math.max(0, depth - 10);
            tolerance = 10;
        }
        int maxAir = Math.max(1, player.getMaxAirSupply());
        int airPercent = Math.max(0, Math.min(100, player.getAirSupply() * 100 / maxAir));

        graphics.pose().pushPose();
        graphics.pose().translate(screenX, screenY, 0);
        graphics.pose().scale(scale, scale, 1f);

        int pad = 8;
        int cardBg = theme.panelAlpha(0.88f);

        // ── Profile panel: identity row + balance hero ─────────────────────────
        int headerH = 58;
        LumenGfx.roundedRect(graphics, 0, 0, w, headerH, 8, cardBg);
        LumenGfx.outline(graphics, 0, 0, w, headerH, 8, theme.border());
        // rank hairline on the top edge — card identity
        LumenGfx.gradientRoundedH(graphics, 9, 1, w - 18, 1, 0, rankColor & 0x66FFFFFF, 0x00000000);

        UiDraw.drawPlayerHead(graphics, player.getUUID(), player.getGameProfile().getName(), pad, 7, 20);
        String name = AquaFontRenderer.fit(font, player.getGameProfile().getName(), w - pad - 20 - 8);
        AquaFontRenderer.draw(graphics, font, name, pad + 26, 9, theme.text());

        boolean hasGlyph = !rankGlyph.isEmpty();
        int pillW = hasGlyph ? 20 : AquaFontRenderer.width(font, rankTitle) + 8;
        int pillX = w - pad - pillW;
        LumenGfx.roundedRect(graphics, pillX, 8, pillW, 12, 3, rankColor & 0x22FFFFFF);
        LumenGfx.outline(graphics, pillX, 8, pillW, 12, 3, rankColor & 0x55FFFFFF);
        if (hasGlyph) {
            Component g = Component.literal(rankGlyph).withStyle(net.minecraft.network.chat.Style.EMPTY
                    .withFont(new net.minecraft.resources.ResourceLocation("aquatech_ui", "ranks")));
            graphics.drawString(font, g, pillX + 4, 9, 0xFFFFFFFF, false);
        } else {
            AquaFontRenderer.draw(graphics, font, rankTitle, pillX + 4, 10, rankColor);
        }

        LumenGfx.gradientRoundedH(graphics, pad, 32, w - pad * 2, 1, 0, theme.accentAlpha(0.22f), 0x00000000);

        // Balance hero: coin + big gold number + dim caption
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(COIN_TEXTURE, pad, 41, 12, 12, 0, 0, 32, 32, 32, 32);
        int bal = ClientUiState.sessionBalance();
        String balStr = AquaFontRenderer.fit(font, String.valueOf(bal), 74);
        graphics.pose().pushPose();
        graphics.pose().scale(1.18F, 1.18F, 1F);
        AquaFontRenderer.draw(graphics, font, balStr, Math.round((pad + 16) / 1.18F), Math.round(40 / 1.18F), theme.gold());
        graphics.pose().popPose();

        // Playtime at the right edge
        String playtime = ClientUiState.getPlaytimeFormatted();
        LumenIcons.draw(graphics, LumenIcons.Icon.CLOCK, w - pad - AquaFontRenderer.width(font, playtime) - 13, 45, 9, theme.textDim());
        AquaFontRenderer.draw(graphics, font, playtime, w - pad - AquaFontRenderer.width(font, playtime), 45, theme.textDim());

        // ── Dive panel: collapsed on the surface, expands underwater ───────────
        int diveY = headerH + 5;
        if (!inWater) {
            int diveH = 24;
            LumenGfx.roundedRect(graphics, 0, diveY, w, diveH, 8, cardBg);
            LumenGfx.outline(graphics, 0, diveY, w, diveH, 8, theme.border());
            LumenIcons.draw(graphics, LumenIcons.Icon.WAVE, pad, diveY + 7, 10, theme.textDim());
            AquaFontRenderer.draw(graphics, font, "на поверхности", pad + 15, diveY + 8, theme.textDim());
            graphics.pose().popPose();
            return;
        }

        int diveH = 78;
        LumenGfx.roundedRect(graphics, 0, diveY, w, diveH, 8, cardBg);
        LumenGfx.outline(graphics, 0, diveY, w, diveH, 8, theme.border());
        LumenIcons.draw(graphics, LumenIcons.Icon.WAVE, pad, diveY + 7, 10, theme.accent());
        AquaFontRenderer.draw(graphics, font, "ПОГРУЖЕНИЕ", pad + 15, diveY + 8, theme.accent());
        LumenGfx.gradientRoundedH(graphics, pad, diveY + 20, w - pad * 2, 1, 0, theme.accentAlpha(0.22f), 0x00000000);

        // Depth hero
        graphics.pose().pushPose();
        graphics.pose().scale(1.3F, 1.3F, 1F);
        AquaFontRenderer.draw(graphics, font, depth + " м", Math.round(pad / 1.3F), Math.round((diveY + 27) / 1.3F), theme.text());
        graphics.pose().popPose();

        // Pressure status pill (right)
        String pressureValue = pressure <= 0 ? "норма" : pressureLabel(pressure);
        int pColor = pressureColor(pressure, theme);
        int pw = AquaFontRenderer.width(font, pressureValue) + 10;
        LumenGfx.roundedRect(graphics, w - pad - pw, diveY + 30, pw, 13, 3, pColor & 0x22FFFFFF);
        LumenGfx.outline(graphics, w - pad - pw, diveY + 30, pw, 13, 3, pColor & 0x55FFFFFF);
        AquaFontRenderer.draw(graphics, font, pressureValue, w - pad - pw + 5, diveY + 32, pColor);

        if (showPressure) {
            AquaFontRenderer.draw(graphics, font, "запас защиты: " + tolerance + " м", pad, diveY + 50, theme.textDim());
        }

        // Oxygen bar with inline percent
        AquaFontRenderer.draw(graphics, font, "O₂", pad, diveY + 62, theme.textDim());
        String airText = airPercent + "%";
        AquaFontRenderer.draw(graphics, font, airText, w - pad - AquaFontRenderer.width(font, airText), diveY + 62, theme.accent());
        LumenGfx.progressBar(graphics, pad, diveY + 73, w - pad * 2, 3, airPercent / 100.0F,
                0x3316202C, theme.accent(), theme.accentAlt());

        graphics.pose().popPose();
    }

    private static String pressureLabel(int pressure) {
        if (pressure <= 0) return "норма";
        if (pressure <= 5) return "лёгкое";
        if (pressure <= 10) return "среднее";
        if (pressure <= 15) return "высокое";
        if (pressure <= 25) return "критич.";
        return "экстрим";
    }

    private static int pressureColor(int pressure, LumenTheme theme) {
        if (pressure <= 0) return theme.accent();
        if (pressure <= 5) return theme.success();
        if (pressure <= 10) return theme.gold();
        if (pressure <= 15) return 0xFFFFAA55;
        return theme.danger();
    }
}
