package net.aquatech.ui.client.nameplate;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import net.aquatech.ui.client.ClientUiState;
import net.aquatech.ui.client.bubble.ChatBubbleManager;
import net.aquatech.ui.client.render.AquaFontRenderer;
import net.aquatech.ui.client.theme.LumenTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class NameplateHandler {
    private static final int BG_BUBBLE = 0xE809111C;
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
        Component rankComp = AquaFontRenderer.text(rankText);
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

            // Smooth rank pill above the name
            float pillW = font.width(rankComp) + 9;
            float pillH = 12;
            float pillY = -16;
            drawRoundedRect(pose.last().pose(), buffer.getBuffer(RenderType.gui()), -pillW / 2F, pillY, pillW, pillH, 3.5F,
                    applyFade(rankColor & 0x2AFFFFFF, 1F));
            drawRoundedRectOutline(pose.last().pose(), buffer.getBuffer(RenderType.gui()), -pillW / 2F, pillY, pillW, pillH, 3.5F,
                    applyFade(rankColor & 0x66FFFFFF, 1F));
            font.drawInBatch(rankComp, -font.width(rankComp) / 2F + 1, pillY + 2, applyFade(rankColor, 1F), false, pose.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, light);

            // Chat bubble: smooth rounded card, fade-in/out, slide-up on appear
            if (view != null) {
                float fadeIn = Math.min(1F, view.ageTicks() / 5F);
                float fadeOut = Math.min(1F, (view.totalTicks() - view.ageTicks()) / 8F);
                float fade = Math.min(fadeIn, fadeOut);
                float slide = (1F - Math.min(1F, view.ageTicks() / 6F)) * 6F;

                var lines = font.split(AquaFontRenderer.text(view.message()), 150);
                int lineCount = Math.min(3, lines.size());
                float lineH = 10.5F;
                float padX = 7;
                float padY = 5;
                float bw = 0;
                for (int i = 0; i < lineCount; i++) {
                    bw = Math.max(bw, font.width(lines.get(i)));
                }
                bw += padX * 2;
                float bh = lineCount * lineH + padY * 2;
                float by = pillY - 6 - bh + slide;
                float bx = -bw / 2F;

                drawRoundedRect(pose.last().pose(), buffer.getBuffer(RenderType.gui()), bx, by, bw, bh, 4F, applyFade(BG_BUBBLE, fade));
                int textCol = applyFade(TEXT_MAIN, fade);
                for (int i = 0; i < lineCount; i++) {
                    var seq = lines.get(i);
                    font.drawInBatch(seq, -font.width(seq) / 2F, by + padY + i * lineH, textCol,
                            false, pose.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, light);
                }
            }
        } finally {
            pose.popPose();
        }
    }

    private static int applyFade(int argb, float fade) {
        int a = (int) (((argb >>> 24) & 0xFF) * Math.max(0F, Math.min(1F, fade)));
        return (a << 24) | (argb & 0x00FFFFFF);
    }

    /** Filled rounded rect emitted as position-color quads (no pixel font involved). */
    private static void drawRoundedRect(org.joml.Matrix4f pose, VertexConsumer consumer,
                                        float x, float y, float w, float h, float r, int argb) {
        int a = (argb >>> 24) & 0xFF;
        int cr = (argb >>> 16) & 0xFF;
        int cg = (argb >>> 8) & 0xFF;
        int cb = argb & 0xFF;
        float x2 = x + w;
        float y2 = y + h;
        rect(pose, consumer, x + r, y, x2 - r, y2 - r, cr, cg, cb, a);
        rect(pose, consumer, x + r, y + r, x2 - r, y2, cr, cg, cb, a);
        rect(pose, consumer, x, y + r, x + r, y2 - r, cr, cg, cb, a);
        rect(pose, consumer, x2 - r, y + r, x2, y2 - r, cr, cg, cb, a);
        arc(pose, consumer, x + r, y + r, r, 180F, 270F, cr, cg, cb, a);
        arc(pose, consumer, x2 - r, y + r, r, 270F, 360F, cr, cg, cb, a);
        arc(pose, consumer, x2 - r, y2 - r, r, 0F, 90F, cr, cg, cb, a);
        arc(pose, consumer, x + r, y2 - r, r, 90F, 180F, cr, cg, cb, a);
    }

    private static void drawRoundedRectOutline(org.joml.Matrix4f pose, VertexConsumer consumer,
                                               float x, float y, float w, float h, float r, int argb) {
        int a = (argb >>> 24) & 0xFF;
        int cr = (argb >>> 16) & 0xFF;
        int cg = (argb >>> 8) & 0xFF;
        int cb = argb & 0xFF;
        float x2 = x + w;
        float y2 = y + h;
        float t = 0.75F;
        rect(pose, consumer, x + r, y, x2 - r, y + t, cr, cg, cb, a);
        rect(pose, consumer, x + r, y2 - t, x2 - r, y2, cr, cg, cb, a);
        rect(pose, consumer, x, y + r, x + t, y2 - r, cr, cg, cb, a);
        rect(pose, consumer, x2 - t, y + r, x2, y2 - r, cr, cg, cb, a);
    }

    private static void rect(org.joml.Matrix4f pose, VertexConsumer consumer,
                             float x1, float y1, float x2, float y2, int r, int g, int b, int a) {
        consumer.vertex(pose, x1, y1, 0).color(r, g, b, a).endVertex();
        consumer.vertex(pose, x1, y2, 0).color(r, g, b, a).endVertex();
        consumer.vertex(pose, x2, y2, 0).color(r, g, b, a).endVertex();
        consumer.vertex(pose, x2, y1, 0).color(r, g, b, a).endVertex();
    }

    private static void arc(org.joml.Matrix4f pose, VertexConsumer consumer,
                            float cx, float cy, float r, float fromDeg, float toDeg, int rC, int gC, int bC, int a) {
        int segments = 4;
        for (int i = 0; i < segments; i++) {
            float a1 = (float) Math.toRadians(fromDeg + (toDeg - fromDeg) * i / segments);
            float a2 = (float) Math.toRadians(fromDeg + (toDeg - fromDeg) * (i + 1) / segments);
            consumer.vertex(pose, cx, cy, 0).color(rC, gC, bC, a).endVertex();
            consumer.vertex(pose, cx + r * (float) Math.cos(a1), cy + r * (float) Math.sin(a1), 0).color(rC, gC, bC, a).endVertex();
            consumer.vertex(pose, cx + r * (float) Math.cos(a2), cy + r * (float) Math.sin(a2), 0).color(rC, gC, bC, a).endVertex();
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
}
