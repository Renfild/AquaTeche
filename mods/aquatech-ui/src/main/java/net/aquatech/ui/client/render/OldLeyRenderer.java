package net.aquatech.ui.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.entity.OldLeyEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class OldLeyRenderer extends GeoEntityRenderer<OldLeyEntity> {

    public OldLeyRenderer(EntityRendererProvider.Context context) {
        super(context, new Model());
        this.shadowRadius = 0.0F;
    }

    /** Пока игрок держит Лея на удочке, от его руки к рыбе тянется леска; провисает тем меньше, чем дальше рыба. */
    @Override
    public void render(OldLeyEntity ley, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        super.render(ley, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        int hooker = ley.hookedBy();
        if (hooker == 0 || ley.level() == null) {
            return;
        }
        Entity player = ley.level().getEntity(hooker);
        if (player == null) {
            return;
        }
        float yaw = player.getViewYRot(partialTick) * Mth.DEG_TO_RAD;
        double fx = -Mth.sin(yaw);
        double fz = Mth.cos(yaw);
        double hx = player.getEyePosition(partialTick).x + fx * 0.75D - fz * 0.28D;
        double hy = player.getEyePosition(partialTick).y - 0.45D;
        double hz = player.getEyePosition(partialTick).z + fz * 0.75D + fx * 0.28D;
        double lx = Mth.lerp(partialTick, ley.xOld, ley.getX());
        double ly = Mth.lerp(partialTick, ley.yOld, ley.getY());
        double lz = Mth.lerp(partialTick, ley.zOld, ley.getZ());
        float dx = (float) (hx - lx);
        float dy = (float) (hy - ly) - 0.5F;
        float dz = (float) (hz - lz);
        float length = Mth.sqrt(dx * dx + dy * dy + dz * dz);
        float sag = Math.max(0.15F, 1.2F - length * 0.04F);
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.lineStrip());
        PoseStack.Pose pose = poseStack.last();
        int segments = 20;
        for (int i = 0; i <= segments; i++) {
            float t = i / (float) segments;
            float x = dx * t;
            float y = 0.5F + dy * t - sag * 4.0F * t * (1.0F - t);
            float z = dz * t;
            float nx = dx / Math.max(length, 0.001F);
            float ny = dy / Math.max(length, 0.001F);
            float nz = dz / Math.max(length, 0.001F);
            buffer.vertex(pose.pose(), x, y, z).color(235, 232, 220, 255).normal(pose.normal(), nx, ny, nz).endVertex();
        }
    }

    private static final class Model extends GeoModel<OldLeyEntity> {
        private static final ResourceLocation GEO = new ResourceLocation(AquaTechUI.MOD_ID, "geo/old_ley.geo.json");
        private static final ResourceLocation TEXTURE = new ResourceLocation(AquaTechUI.MOD_ID, "textures/entity/old_ley.png");
        private static final ResourceLocation ANIMATIONS = new ResourceLocation(AquaTechUI.MOD_ID, "animations/old_ley.animation.json");

        @Override
        public ResourceLocation getModelResource(OldLeyEntity animatable) {
            return GEO;
        }

        @Override
        public ResourceLocation getTextureResource(OldLeyEntity animatable) {
            return TEXTURE;
        }

        @Override
        public ResourceLocation getAnimationResource(OldLeyEntity animatable) {
            return ANIMATIONS;
        }
    }
}
