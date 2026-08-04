package com.casesmod;

import com.casesmod.data.CaseManager;
import com.casesmod.data.KitManager;
import com.casesmod.data.QuestManager;
import com.casesmod.data.WarpManager;
import com.casesmod.item.ModItems;
import com.casesmod.network.NetworkHandler;
import com.casesmod.server.commands.MenuCommands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CasesMod.MOD_ID)
public class CasesMod {
    public static final String MOD_ID = "casesmod";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public CasesMod() {
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::commonSetup);
        ModItems.register(modBus);
        com.casesmod.item.ModSounds.register(modBus);
        com.casesmod.item.ModCreativeTab.register(modBus);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        NetworkHandler.register();
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Загружаем все конфиги при старте сервера
        CaseManager.INSTANCE.load();
        KitManager.INSTANCE.load();
        KitManager.INSTANCE.loadCooldowns();
        WarpManager.INSTANCE.load();
        QuestManager.INSTANCE.load();
        QuestManager.INSTANCE.loadProgress();
        com.casesmod.data.CurrencyManager.INSTANCE.load();
        com.casesmod.data.PityManager.INSTANCE.load();
        // PlayerAccountManager грузит лениво из players/<uuid>.json при первом обращении
        LOGGER.info("[CasesMod] Конфиги загружены: {} кейсов, {} китов, {} варпов, {} квестов",
                CaseManager.INSTANCE.getCases().size(),
                KitManager.INSTANCE.getKits().size(),
                WarpManager.INSTANCE.getWarps().size(),
                QuestManager.INSTANCE.getQuests().size());
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        MenuCommands.register(event.getDispatcher());
    }
}
