package net.aquatech.ui.client.tooltip;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.fishing.FishGrade;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Shows freshness / smoked state / grade / tournament weight carried in fish NBT.
 */
@Mod.EventBusSubscriber(modid = AquaTechUI.MOD_ID, value = Dist.CLIENT)
public final class FishItemTooltip {

    public static final long FRESH_WINDOW_MS = 30L * 60L * 1000L;

    private FishItemTooltip() {
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        net.aquatech.ui.fishing.FishingBait.Kind baitKind =
                net.aquatech.ui.fishing.FishingBait.kindOf(stack);
        if (baitKind != null) {
            event.getToolTip().add(net.aquatech.ui.fishing.FishingBait.effectLine(baitKind));
            event.getToolTip().add(Component.literal(
                    "\u00a77Заряд: \u00a7f" + net.aquatech.ui.fishing.FishingBait.CHARGES_PER_ITEM
                            + "\u00a77 уловов — ПКМ, держа удочку во второй руке"));
        }

        CompoundTag tag = stack.getTag();
        if (tag == null) return;

        if (baitKind == null) {
            net.aquatech.ui.fishing.FishingBait.Kind active =
                    net.aquatech.ui.fishing.FishingBait.active(stack);
            if (active != null) {
                event.getToolTip().add(Component.literal("\u00a7b" + active.label + " \u00a77("
                        + net.aquatech.ui.fishing.FishingBait.uses(stack) + " уловов)"));
            }
        }

        if (tag.getBoolean("AquaSmoked")) {
            event.getToolTip().add(Component.literal(
                    "\u00a76Копчёная рыба: \u00a7e×1.5\u00a76 цены в магазине рыбака"));
        }
        int grade = FishGrade.of(stack);
        if (grade > FishGrade.NONE) {
            event.getToolTip().add(Component.literal(
                    FishGrade.color(grade) + "Грейд: " + FishGrade.name(grade)
                            + " \u00a77(×" + FishGrade.priceMultiplier(grade) + " цены)"));
        }
        double weight = tag.getDouble("aquatech_tournament_weight");
        if (weight > 0.0) {
            event.getToolTip().add(Component.literal(
                    "\u00a77Вес: \u00a7f" + String.format("%.2f", weight) + " кг"));
        }
        long caughtAt = tag.getLong("AquaCaughtAt");
        if (caughtAt > 0L) {
            long leftMs = FRESH_WINDOW_MS - (System.currentTimeMillis() - caughtAt);
            if (leftMs > 0L) {
                long mm = leftMs / 60000L;
                long ss = (leftMs % 60000L) / 1000L;
                event.getToolTip().add(Component.literal(
                        "\u00a7aСвежая: \u00a7e+20%\u00a7a к цене (\u00a7f" + mm + ":" + String.format("%02d", ss) + "\u00a7a)"));
            }
        }
    }
}
