package net.aquatech.ui.client;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.client.render.BeeKeeperRenderer;
import net.aquatech.ui.client.render.FamePlaqueRenderer;
import net.aquatech.ui.client.render.FishMerchantRenderer;
import net.aquatech.ui.client.render.FishNeighborRenderer;
import net.aquatech.ui.client.render.OldLeyRenderer;
import net.aquatech.ui.registry.ModBlockEntities;
import net.aquatech.ui.registry.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AquaTechUI.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModBusClientEvents {

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ClientEvents.KEY_MARKET);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.FISH_MERCHANT.get(), FishMerchantRenderer::new);
        event.registerEntityRenderer(ModEntities.BEE_KEEPER.get(), BeeKeeperRenderer::new);
        event.registerEntityRenderer(ModEntities.FISH_NEIGHBOR.get(), FishNeighborRenderer::new);
        event.registerEntityRenderer(ModEntities.OLD_LEY.get(), OldLeyRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FAME_PLAQUE.get(), FamePlaqueRenderer::new);
    }
}

