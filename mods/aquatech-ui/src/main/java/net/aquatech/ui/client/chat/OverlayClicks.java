package net.aquatech.ui.client.chat;

import net.aquatech.ui.compat.jei.AquaTechJeiPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * ЛКМ по чипу предмета в свёрнутом чате → рецепты в JEI.
 * Прямоугольники чипов заполняет AquaChatOverlay на каждом кадре рендера.
 */
@Mod.EventBusSubscriber(modid = "aquatech_ui", value = Dist.CLIENT)
public final class OverlayClicks {

    @SubscribeEvent
    public static void onMouseDown(InputEvent.MouseButton.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null || event.getButton() != 0) return;

        double scale = Math.max(1.0, mc.getWindow().getGuiScale());
        double mx = mc.mouseHandler.xpos() / scale;
        double my = mc.mouseHandler.ypos() / scale;

        ItemStack hit = AquaChatOverlay.chipHit(mx, my);
        if (hit != null && !hit.isEmpty() && AquaTechJeiPlugin.showRecipes(hit)) {
            event.setCanceled(true);
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.2F));
        }
    }
}
