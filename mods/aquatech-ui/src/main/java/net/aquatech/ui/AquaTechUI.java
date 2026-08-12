package net.aquatech.ui;

import net.aquatech.ui.common.ModClientConfig;
import net.aquatech.ui.common.ModConfig;
import net.aquatech.ui.network.NetworkHandler;
import net.aquatech.ui.registry.ModBlockEntities;
import net.aquatech.ui.registry.ModBlocks;
import net.aquatech.ui.registry.ModCreativeTabs;
import net.aquatech.ui.registry.ModItems;
import net.aquatech.ui.registry.ModMenuTypes;
import net.aquatech.ui.registry.ModSounds;
import net.aquatech.ui.server.ProfileSyncService;
import net.aquatech.ui.server.bukkit.LuckPermsHook;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(AquaTechUI.MOD_ID)
public class AquaTechUI {
    public static final String MOD_ID = "aquatech_ui";
    public static final Logger LOGGER = LogManager.getLogger();

    public AquaTechUI() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.register(modBus);
        ModItems.register(modBus);
        ModBlockEntities.register(modBus);
        ModMenuTypes.register(modBus);
        ModCreativeTabs.register(modBus);
        ModSounds.register(modBus);

        modBus.addListener(net.aquatech.ui.capability.AquaSkillCapability::register);

        ModLoadingContext.get().registerConfig(Type.COMMON, ModConfig.SPEC);
        ModLoadingContext.get().registerConfig(Type.CLIENT, ModClientConfig.SPEC);

        modBus.addListener(this::commonSetup);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            net.aquatech.ui.client.AquaTechClient.register(modBus);
        }
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            NetworkHandler.register();
            net.aquatech.ui.fishing.RodDurabilityApplier.apply();
        });
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        ProfileSyncService.start(event.getServer());
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        event.getServer().execute(LuckPermsHook::register);
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        ProfileSyncService.stop();
    }
}
