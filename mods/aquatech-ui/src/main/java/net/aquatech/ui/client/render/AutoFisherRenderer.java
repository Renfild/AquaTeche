package net.aquatech.ui.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.aquatech.ui.block.entity.AutoFisherBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class AutoFisherRenderer implements BlockEntityRenderer<AutoFisherBlockEntity> {

    public AutoFisherRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(AutoFisherBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // Render and splash particles removed per user request.
    }
}

