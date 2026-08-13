package net.aquatech.ui.client;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.fishing.FishingRodCompat;
import net.aquatech.ui.fishing.RodDurability;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.IItemDecorator;
import net.minecraftforge.client.event.RegisterItemDecorationsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Always draw a uses bar on StarCatcher rods (vanilla bar only appears after first damage).
 */
@Mod.EventBusSubscriber(modid = AquaTechUI.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class RodDurabilityBar {
    private static final IItemDecorator DECORATOR = RodDurabilityBar::render;

    private RodDurabilityBar() {
    }

    @SubscribeEvent
    public static void onRegister(RegisterItemDecorationsEvent event) {
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack probe = new ItemStack(item);
            if (FishingRodCompat.isSupportedRod(probe)) {
                event.register(item, DECORATOR);
            }
        }
    }

    private static boolean render(GuiGraphics g, Font font, ItemStack stack, int x, int y) {
        int max = RodDurability.maxUses(stack);
        if (max <= 0) return false;
        int left = RodDurability.remaining(stack);
        int barX = x + 2;
        int barY = y + 13;
        int width = Mth.clamp(Math.round(13f * left / (float) max), 0, 13);
        int color = Mth.hsvToRgb(Math.max(0f, left / (float) max / 3f), 1f, 1f);
        g.fill(barX, barY, barX + 13, barY + 2, 0xFF000000);
        g.fill(barX, barY, barX + width, barY + 1, 0xFF000000 | color);
        return true;
    }
}
