package net.aquatech.ui.client.render;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.entity.FishNeighborEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FishNeighborRenderer extends GeoEntityRenderer<FishNeighborEntity> {

    public FishNeighborRenderer(EntityRendererProvider.Context context) {
        super(context, new Model());
        this.shadowRadius = 0.4F;
    }

    private static final class Model extends GeoModel<FishNeighborEntity> {
        private static final ResourceLocation GEO = new ResourceLocation(AquaTechUI.MOD_ID, "geo/fish_neighbor.geo.json");
        private static final ResourceLocation TEXTURE = new ResourceLocation(AquaTechUI.MOD_ID, "textures/entity/fish_neighbor.png");
        private static final ResourceLocation ANIMATIONS = new ResourceLocation(AquaTechUI.MOD_ID, "animations/fish_neighbor.animation.json");

        @Override
        public ResourceLocation getModelResource(FishNeighborEntity animatable) {
            return GEO;
        }

        @Override
        public ResourceLocation getTextureResource(FishNeighborEntity animatable) {
            return TEXTURE;
        }

        @Override
        public ResourceLocation getAnimationResource(FishNeighborEntity animatable) {
            return ANIMATIONS;
        }

        /** Голова добавляет к анимации поворот, который сервер задал через yHeadRot (взгляд на собеседника). */
        @Override
        public void setCustomAnimations(FishNeighborEntity animatable, long instanceId, AnimationState<FishNeighborEntity> state) {
            super.setCustomAnimations(animatable, instanceId, state);
            CoreGeoBone head = getAnimationProcessor().getBone("head");
            EntityModelData data = state.getData(DataTickets.ENTITY_MODEL_DATA);
            if (head != null && data != null) {
                head.setRotX(head.getRotX() + data.headPitch() * Mth.DEG_TO_RAD);
                head.setRotY(head.getRotY() + data.netHeadYaw() * Mth.DEG_TO_RAD);
            }
        }
    }
}
