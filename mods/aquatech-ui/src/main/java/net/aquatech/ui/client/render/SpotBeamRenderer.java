package net.aquatech.ui.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.client.ClientSpotState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Световой луч над личной точкой лова, виден издалека. */
@Mod.EventBusSubscriber(modid = AquaTechUI.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class SpotBeamRenderer {

    private static final float[] BEAM_COLOR = {0.25F, 0.85F, 0.9F};
    private static final int BEAM_HEIGHT = 120;

    private SpotBeamRenderer() {
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (!ClientSpotState.active()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        BlockPos pos = ClientSpotState.pos();
        Vec3 camera = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        BeaconRenderer.renderBeaconBeam(pose, buffers, BeaconRenderer.BEAM_LOCATION, event.getPartialTick(),
                1.0F, mc.level.getGameTime(), 0, BEAM_HEIGHT, BEAM_COLOR, 0.2F, 0.25F);
        buffers.endBatch();
        pose.popPose();
    }
}
