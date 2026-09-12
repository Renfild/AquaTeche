package net.aquatech.machines.client;

import net.aquatech.machines.client.gui.ExcavatorScreen;
import net.aquatech.machines.client.gui.ExtractorScreen;
import net.aquatech.machines.client.gui.FisherScreen;
import net.aquatech.machines.registry.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = "aquatech_machines", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class MachineClient {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.FISHER.get(), FisherScreen::new);
            MenuScreens.register(ModMenuTypes.EXCAVATOR.get(), ExcavatorScreen::new);
            MenuScreens.register(ModMenuTypes.EXTRACTOR.get(), ExtractorScreen::new);
        });
    }
}
