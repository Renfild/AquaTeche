package net.aquatech.ui.client;

import net.aquatech.ui.client.gui.AutoFisherScreen;
import net.aquatech.ui.client.gui.OceanAltarScreen;
import net.aquatech.ui.client.gui.OceanFilterScreen;
import net.aquatech.ui.client.gui.SeabedDredgerScreen;
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
            MinecraftForge.EVENT_BUS.register(net.aquatech.ui.client.nameplate.NameplateHandler.class);
            MenuScreens.register(ModMenuTypes.AUTO_FISHER_MENU.get(), AutoFisherScreen::new);
            MenuScreens.register(ModMenuTypes.OCEAN_FILTER_MENU.get(), OceanFilterScreen::new);
            MenuScreens.register(ModMenuTypes.SEABED_DREDGER_MENU.get(), SeabedDredgerScreen::new);
            MenuScreens.register(ModMenuTypes.TACKLE_BOX_MENU.get(), TackleBoxScreen::new);
            MenuScreens.register(ModMenuTypes.OCEAN_ALTAR_MENU.get(), OceanAltarScreen::new);
        });
    }
}
