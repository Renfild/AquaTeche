package net.aquatech.ui.client.render;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.entity.OldLeyEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class OldLeyRenderer extends GeoEntityRenderer<OldLeyEntity> {

    public OldLeyRenderer(EntityRendererProvider.Context context) {
        super(context, new Model());
        this.shadowRadius = 0.0F;
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
