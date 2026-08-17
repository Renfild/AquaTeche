package store.aquateche.aqualumen;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import store.aquateche.aqualumen.client.LumenClient;
import store.aquateche.aqualumen.config.LumenConfig;
import store.aquateche.aqualumen.network.LumenNetwork;
import store.aquateche.aqualumen.registry.ModRegistries;

/**
 * AquaLumen UI - "Luminous"-style hub interface for Forge 1.20.1.
 *
 * <p>Runs on a dedicated Forge server, on Mohist (Forge + Bukkit hybrid) and in single player.
 * All gameplay state stays server authoritative: the client only receives a read-only snapshot
 * and sends back validated action identifiers.</p>
 */
@Mod(AquaLumenUI.MODID)
public final class AquaLumenUI {

    public static final String MODID = "aqualumen";
    public static final String VERSION = "0.3.4-alpha";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AquaLumenUI() {
        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModRegistries.register(modBus);
        modBus.addListener(this::commonSetup);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, LumenConfig.COMMON_SPEC, "aqualumen-common.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, LumenConfig.CLIENT_SPEC, "aqualumen-client.toml");

        // Client-only bootstrap; never touched by a dedicated / Mohist server.
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> LumenClient::bootstrap);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(LumenNetwork::register);
        LOGGER.info("[AquaLumen UI] hub interface ready (Forge 1.20.1, Mohist compatible)");
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }
}
