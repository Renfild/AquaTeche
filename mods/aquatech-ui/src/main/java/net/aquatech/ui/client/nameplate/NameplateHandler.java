package net.aquatech.ui.client.nameplate;

import net.aquatech.ui.client.ClientUiState;
import net.aquatech.ui.client.bubble.ChatBubbleManager;
import net.aquatech.ui.client.render.UiDraw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class NameplateHandler {
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
        if (player == minecraft.player) {
            return;
        }

        var profile = ClientUiState.profile(player.getUUID());
        String rank = profile != null ? profile.rankDisplay() : "";
        String label = rank.isBlank()
                ? player.getName().getString()
                : (rank.trim() + " " + player.getName().getString());
        Component name = Component.literal(label);
        Font font = minecraft.font;
        var pose = event.getPoseStack();
        var buffer = event.getMultiBufferSource();
        int light = event.getPackedLight();
        float height = player.getBbHeight() + 0.5F;

        pose.pushPose();
        try {
            pose.translate(0.0D, height, 0.0D);
            pose.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
            pose.scale(-0.025F, -0.025F, 0.025F);
            drawCentered(font, name, 0, UiDraw.COLOR_TEXT, 0, pose, buffer, light);

            String bubbleMessage = ChatBubbleManager.messageFor(player.getUUID());
            if (bubbleMessage != null) {
                String fitted = font.plainSubstrByWidth(bubbleMessage, 180);
                if (fitted.length() < bubbleMessage.length()) {
                    fitted = font.plainSubstrByWidth(bubbleMessage, 168) + "...";
                }
                Component bubble = Component.literal(fitted);
                drawCentered(font, bubble, -14, UiDraw.COLOR_TEXT, 0x990B1F2A, pose, buffer, light);
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
            com.mojang.blaze3d.vertex.PoseStack pose,
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
