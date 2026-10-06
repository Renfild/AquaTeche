package net.aquatech.ui.client.render;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.entity.BeeKeeperEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BeeKeeperRenderer extends GeoEntityRenderer<BeeKeeperEntity> {

    public BeeKeeperRenderer(EntityRendererProvider.Context context) {
        super(context, new Model());
        this.shadowRadius = 0.4F;
    }

    private static final class Model extends GeoModel<BeeKeeperEntity> {
        private static final ResourceLocation GEO = new ResourceLocation(AquaTechUI.MOD_ID, "geo/bee_keeper.geo.json");
        private static final ResourceLocation TEXTURE = new ResourceLocation(AquaTechUI.MOD_ID, "textures/entity/bee_keeper.png");
        private static final ResourceLocation ANIMATIONS = new ResourceLocation(AquaTechUI.MOD_ID, "animations/bee_keeper.animation.json");

        @Override
        public ResourceLocation getModelResource(BeeKeeperEntity animatable) {
            return GEO;
        }

        @Override
        public ResourceLocation getTextureResource(BeeKeeperEntity animatable) {
            return TEXTURE;
        }

        @Override
        public ResourceLocation getAnimationResource(BeeKeeperEntity animatable) {
            return ANIMATIONS;
        }
    }
}
