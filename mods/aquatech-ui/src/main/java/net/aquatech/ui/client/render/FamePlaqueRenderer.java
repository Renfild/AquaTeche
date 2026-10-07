package net.aquatech.ui.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.aquatech.ui.block.FamePlaqueBlock;
import net.aquatech.ui.block.entity.FamePlaqueBlockEntity;
import net.aquatech.ui.fishing.FishWeight;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Рисует на табличке рыбу вида, имя рыбы и, если рекорд занят, ник рекордсмена с весом.
 * Координаты заданы в системе, где перёд таблички смотрит в +Z; поворот под направление блока делает {@link #render}.
 */
public class FamePlaqueRenderer implements BlockEntityRenderer<FamePlaqueBlockEntity> {

    private static final int TEXT_COLOR = 0xFF1E1206;
    /** Передняя плоскость латунной таблички в модели на 12 единицах от стены плюс 0.1 запаса, чтобы буквы не мерцали. */
    private static final float NAMEPLATE_FRONT_Z = -0.244F;
    /** Внутри ободка таблички остаётся 11 единиц ширины. */
    private static final float TEXT_MAX_WIDTH = 0.66F;
    private static final float TEXT_SCALE = 0.0095F;
    /** Окно модели 11 на 8.5 единиц; спрайт 16 текселей при 0.5 блока даёт 8 единиц, то есть влезает целиком. */
    private static final float FISH_SCALE = 0.5F;

    private final ItemRenderer items;
    private final Font font;

    public FamePlaqueRenderer(BlockEntityRendererProvider.Context context) {
        this.items = context.getItemRenderer();
        this.font = context.getFont();
    }

    @Override
    public void render(FamePlaqueBlockEntity plaque, float partialTick, PoseStack pose, MultiBufferSource buffer,
                       int light, int overlay) {
        String species = plaque.species();
        if (species.isEmpty()) {
            if (plaque.rank() > 0 && plaque.getLevel() != null) {
                renderEmptySlot(plaque, pose, buffer, light);
            }
            return;
        }
        ResourceLocation id = ResourceLocation.tryParse(species);
        Item item = id == null ? Items.AIR : BuiltInRegistries.ITEM.get(id);
        if (item == Items.AIR || plaque.getLevel() == null) {
            return;
        }
        Direction facing = plaque.getBlockState().getValue(FamePlaqueBlock.FACING);
        ItemStack stack = new ItemStack(item);

        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));

        pose.pushPose();
        pose.translate(0.0, 0.08, -0.32);
        pose.scale(FISH_SCALE, FISH_SCALE, FISH_SCALE);
        pose.mulPose(Axis.YP.rotationDegrees(180.0F));
        items.renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, pose, buffer,
                plaque.getLevel(), (int) plaque.getBlockPos().asLong());
        pose.popPose();

        Component fish = stack.getHoverName();
        Component record = plaque.grams() > 0
                ? Component.literal(plaque.holder() + " · " + FishWeight.format(plaque.grams()))
                : Component.literal("место свободно");
        line(pose, buffer, light, fish, -0.265F);
        line(pose, buffer, light, record, -0.35F);

        pose.popPose();
    }

    /** Автоматическая табличка без рекорда: подпись вместо рыбы. */
    private void renderEmptySlot(FamePlaqueBlockEntity plaque, PoseStack pose, MultiBufferSource buffer, int light) {
        Direction facing = plaque.getBlockState().getValue(FamePlaqueBlock.FACING);
        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        line(pose, buffer, light, Component.literal("Стена славы"), -0.265F);
        line(pose, buffer, light, Component.literal("место №" + plaque.rank() + " свободно"), -0.35F);
        pose.popPose();
    }

    /** Строка по центру таблички; длинный текст сжимается, чтобы не вылезти за бумагу. */
    private void line(PoseStack pose, MultiBufferSource buffer, int light, Component text, float topY) {
        int width = Math.max(1, font.width(text));
        float scale = Math.min(TEXT_SCALE, TEXT_MAX_WIDTH / width);
        pose.pushPose();
        pose.translate(0.0, topY, NAMEPLATE_FRONT_Z);
        pose.scale(scale, -scale, scale);
        font.drawInBatch(text, -width / 2.0F, 0.0F, TEXT_COLOR, false, pose.last().pose(), buffer,
                Font.DisplayMode.NORMAL, 0, light);
        pose.popPose();
    }
}
