package net.aquatech.ui.client.render;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.client.gui.AquaBlurredScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Turns {@link OceanBlurEngine} on for AquaTech / AquaLumen screens.
 * Blur is processed here (world already in the main target), not via GameRenderer.loadEffect.
 */
@Mod.EventBusSubscriber(modid = AquaTechUI.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ScreenBlurHandler {

    private ScreenBlurHandler() {
    }

    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening event) {
        OceanBlurEngine.setWanted(shouldBlurScreen(event.getNewScreen()));
    }

    @SubscribeEvent
    public static void onScreenClose(ScreenEvent.Closing event) {
        OceanBlurEngine.setWanted(false);
    }

    @SubscribeEvent
    public static void onScreenRenderPre(ScreenEvent.Render.Pre event) {
        if (OceanBlurEngine.isWanted()) {
            OceanBlurEngine.process(event.getPartialTick());
        }
    }

    public static boolean shouldBlurScreen(Screen screen) {
        if (screen == null) {
            return false;
        }
        return BlurScreenPolicy.shouldBlur(
                screen instanceof AquaBlurredScreen,
                screen.getClass().getName()
        );
    }
}
