package net.aquatech.ui.client.tooltip;

import net.aquatech.ui.AquaTechUI;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Shows freshness / smoked state carried in fish NBT (stamped at catch / fish smoker).
 */
@Mod.EventBusSubscriber(modid = AquaTechUI.MOD_ID, value = Dist.CLIENT)
public final class FishItemTooltip {

    public static final long FRESH_WINDOW_MS = 30L * 60L * 1000L;

    private FishItemTooltip() {
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        CompoundTag tag = stack.getTag();
        if (tag == null) return;

        if (tag.getBoolean("AquaSmoked")) {
            event.getToolTip().add(Component.literal(
                    "\u00a76Копчёная рыба: \u00a7e×1.5\u00a76 цены в магазине рыбака"));
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
