package net.aquatech.ui.client.hud;

import com.mojang.math.Axis;
import net.aquatech.ui.client.ClientSpotState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

/** Стрелка к личной точке лова: сверху по центру, с расстоянием и временем. */
public final class SpotArrowHud {

    private static final int ARROW_Y = 46;
    private static final int COLOR_FAR = 0xFF3FD8E8;
    private static final int COLOR_HERE = 0xFF7CFC7C;

    private SpotArrowHud() {
    }

    public static void render(GuiGraphics graphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui || mc.screen != null || !ClientSpotState.active()) {
            return;
        }
        double dx = ClientSpotState.pos().getX() + 0.5 - player.getX();
        double dz = ClientSpotState.pos().getZ() + 0.5 - player.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        boolean here = distance <= ClientSpotState.radius();
        int centerX = graphics.guiWidth() / 2;

        if (!here) {
            // yaw Minecraft: 0 = юг (+Z), 90 = запад (-X)
            float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            float relative = Mth.wrapDegrees(targetYaw - player.getViewYRot(partialTick));
            var pose = graphics.pose();
            pose.pushPose();
            pose.translate(centerX, ARROW_Y, 0);
            pose.mulPose(Axis.ZP.rotationDegrees(relative));
            pose.pushPose();
            pose.translate(1, 1, 0);
            drawArrow(graphics, 0x99000000);
            pose.popPose();
            drawArrow(graphics, COLOR_FAR);
            pose.popPose();
        }

        long left = Math.max(0L, ClientSpotState.expiresAtMs() - System.currentTimeMillis());
        String time = (left / 60_000L) + ":" + String.format("%02d", (left % 60_000L) / 1000L);
        String label = here ? "Ты на точке" : ((int) Math.round(distance) + " м");
        graphics.drawCenteredString(mc.font, "§bТочка лова §6×2 §7цена", centerX, ARROW_Y - 26, 0xFFFFFFFF);
        graphics.drawCenteredString(mc.font, label + " §7· " + time, centerX, ARROW_Y + 14,
                here ? COLOR_HERE : 0xFFFFFFFF);
    }

    /** Указывает вверх: наконечник из рядов расширяющейся ширины и короткое древко. */
    private static void drawArrow(GuiGraphics graphics, int color) {
        for (int row = 0; row < 8; row++) {
            int half = (row + 1) / 2;
            graphics.fill(-half, -10 + row, half + 1, -9 + row, color);
        }
        graphics.fill(-1, -2, 2, 6, color);
    }
}
