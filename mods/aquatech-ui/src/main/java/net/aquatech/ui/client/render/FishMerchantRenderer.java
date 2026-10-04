package net.aquatech.ui.client.render;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.entity.FishMerchantEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FishMerchantRenderer extends GeoEntityRenderer<FishMerchantEntity> {

    public FishMerchantRenderer(EntityRendererProvider.Context context) {
        super(context, new Model());
        this.shadowRadius = 0.4F;
    }

    private static final class Model extends GeoModel<FishMerchantEntity> {
        private static final ResourceLocation GEO = new ResourceLocation(AquaTechUI.MOD_ID, "geo/fish_merchant.geo.json");
        private static final ResourceLocation TEXTURE = new ResourceLocation(AquaTechUI.MOD_ID, "textures/entity/fish_merchant.png");
        private static final ResourceLocation ANIMATIONS = new ResourceLocation(AquaTechUI.MOD_ID, "animations/fish_merchant.animation.json");

        @Override
        public ResourceLocation getModelResource(FishMerchantEntity animatable) {
            return GEO;
        }

        @Override
        public ResourceLocation getTextureResource(FishMerchantEntity animatable) {
            return TEXTURE;
        }

        @Override
        public ResourceLocation getAnimationResource(FishMerchantEntity animatable) {
            return ANIMATIONS;
        }
    }
}
