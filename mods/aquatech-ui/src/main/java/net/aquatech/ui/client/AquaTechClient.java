package net.aquatech.ui.client;

import net.aquatech.ui.client.gui.TackleBoxScreen;
import net.aquatech.ui.registry.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
public final class AquaTechClient {
    private AquaTechClient() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(AquaTechClient::clientSetup);
        modBus.addListener(ClientEvents::registerOverlays);
    }

    private static void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            try {
                MinecraftForge.EVENT_BUS.register(net.aquatech.ui.client.nameplate.NameplateHandler.class);
                            MenuScreens.register(ModMenuTypes.TACKLE_BOX_MENU.get(), TackleBoxScreen::new);
                        net.aquatech.ui.AquaTechUI.LOGGER.info("[AquaTechClient] All machine MenuScreens registered successfully!");
            } catch (Throwable t) {
                net.aquatech.ui.AquaTechUI.LOGGER.error("[AquaTechClient] Error during MenuScreens registration", t);
            }
        });
    }
}
