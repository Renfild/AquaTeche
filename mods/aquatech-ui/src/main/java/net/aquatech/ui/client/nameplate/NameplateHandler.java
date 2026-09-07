package net.aquatech.ui.client.nameplate;

import net.aquatech.ui.client.ClientUiState;
import net.aquatech.ui.client.bubble.ChatBubbleManager;
import net.aquatech.ui.client.render.AquaFontRenderer;
import net.aquatech.ui.client.theme.LumenTheme;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class NameplateHandler {
    private static final int BG_DARK = 0x5F09111C;
    private static final int TEXT_MAIN = 0xFFF1F5F9;

    private NameplateHandler() {
    }

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (event.isCancelable()) {
            event.setCanceled(true);
        } else {
            event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
        }
        if (player == minecraft.player && minecraft.options.getCameraType().isFirstPerson()) {
            return;
        }

        var profile = ClientUiState.profile(player.getUUID());
        String rankId = profile != null ? profile.rankId() : "player";
        int rankColor = LumenTheme.getRankColor(rankId);
        String rankText = profile != null ? profile.rankDisplay() : "";
        rankText = rankText == null ? "" : rankText.replaceAll("[\\uE000-\\uF8FF\\uD800-\\uDFFF]", "").trim();
        if (rankText.isBlank()) {
            rankText = LumenTheme.getRankTitle(rankId);
        }

        String pureGameName = player.getGameProfile() != null ? player.getGameProfile().getName() : "";
        String baseName = (pureGameName != null && !pureGameName.isBlank())
                ? pureGameName
                : (profile != null && profile.name() != null && !profile.name().isBlank() ? profile.name() : player.getName().getString());
        baseName = baseName == null ? "" : baseName.trim();

        Font font = minecraft.font;
        Component nameComp = AquaFontRenderer.text(baseName);
        Component rankComp = AquaFontRenderer.text(" " + rankText + " ");
        var view = ChatBubbleManager.viewFor(player.getUUID());

        var pose = event.getPoseStack();
        var buffer = event.getMultiBufferSource();
        int light = event.getPackedLight();
        float height = player.getBbHeight() + 0.5F;

        pose.pushPose();
        try {
            pose.translate(0.0D, height, 0.0D);
            pose.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
            pose.scale(-0.025F, -0.025F, 0.025F);

            // Name under the rank pill
            drawCentered(font, nameComp, 0, TEXT_MAIN, 0, pose, buffer, light);

            // Rank pill above the name: tinted translucent bar + rank-colored text
            float pillW = font.width(rankComp);
            font.drawInBatch(rankComp, -font.width(rankComp) / 2F, -16, rankColor, false,
                    pose.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, light);

            // Chat bubble above the pill: fade-in/out + slide-up
            if (view != null) {
                float fadeIn = Math.min(1F, view.ageTicks() / 5F);
                float fadeOut = Math.min(1F, (view.totalTicks() - view.ageTicks()) / 8F);
                float fade = Math.max(0F, Math.min(fadeIn, fadeOut));
                float slide = (1F - Math.min(1F, view.ageTicks() / 6F)) * 4F;

                var rawLines = font.split(AquaFontRenderer.text(view.message()), 150);
                int lineCount = Math.min(3, rawLines.size());
                float lineH = 10.5F;
                int spaceW = Math.max(1, font.width(AquaFontRenderer.text(" ")));

                float maxW = 0;
                String[] padded = new String[lineCount];
                for (int i = 0; i < lineCount; i++) {
                    StringBuilder sb = new StringBuilder();
                    rawLines.get(i).accept((part, style, ch) -> {
                        sb.appendCodePoint(ch);
                        return true;
                    });
                    padded[i] = sb.toString();
                    maxW = Math.max(maxW, font.width(AquaFontRenderer.text(padded[i])));
                }
                for (int i = 0; i < lineCount; i++) {
                    int lineW = font.width(AquaFontRenderer.text(padded[i]));
                    int extra = Math.max(0, Math.round((maxW - lineW) / (float) spaceW));
                    padded[i] = padded[i] + " ".repeat(extra);
                }

                int bubbleBg = applyFade(0x4F09111C, fade);
                int bubbleText = applyFade(TEXT_MAIN, fade);
                float bubbleH = lineCount * lineH + 8;
                float bubbleY = -16 - 6 - bubbleH + slide;

                for (int i = 0; i < lineCount; i++) {
                    Component lineComp = AquaFontRenderer.text(" " + padded[i] + " ");
                    font.drawInBatch(lineComp, -font.width(lineComp) / 2F, bubbleY + 4 + i * lineH,
                            bubbleText, false, pose.last().pose(), buffer,
                            Font.DisplayMode.NORMAL, bubbleBg, light);
                }
            }
        } finally {
            pose.popPose();
        }
    }

    private static void drawCentered(
            Font font,
            Component text,
            float y,
            int color,
            int background,
            PoseStack pose,
            net.minecraft.client.renderer.MultiBufferSource buffer,
            int light
    ) {
        float x = -font.width(text) / 2.0F;
        font.drawInBatch(text, x, y, color, false, pose.last().pose(), buffer,
                Font.DisplayMode.SEE_THROUGH, background, light);
        font.drawInBatch(text, x, y, color, false, pose.last().pose(), buffer,
                Font.DisplayMode.NORMAL, background, light);
    }

    private static int applyFade(int argb, float fade) {
        int a = (int) (((argb >>> 24) & 0xFF) * Math.max(0F, Math.min(1F, fade)));
        return (a << 24) | (argb & 0x00FFFFFF);
    }
}
