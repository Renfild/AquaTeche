package net.aquatech.ui.registry;

import net.aquatech.ui.AquaTechUI;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AquaTechUI.MOD_ID);

    public static final RegistryObject<CreativeModeTab> AQUATECH_TAB = CREATIVE_MODE_TABS.register("aquatech_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.aquatech_tab"))
                    .icon(() -> new ItemStack(ModItems.OCEAN_GUIDE_BOOK.get()))
                    .displayItems((parameters, output) -> {
                        for (var entry : ModItems.RATE_MODS.values()) {
                            output.accept(entry.get());
                        }
                        for (var entry : ModItems.UPGRADES.values()) {
                            output.accept(entry.get());
                        }
                        output.accept(ModItems.MESH_FILTER.get());
                        output.accept(ModItems.DREDGER_DRILL_BIT.get());
                        output.accept(ModItems.SONAR_GOGGLES.get());
                        output.accept(ModItems.ABYSSAL_MAGNET.get());
                        output.accept(ModItems.AUTO_FISHER_ITEM.get());
                        output.accept(ModItems.OCEAN_FILTER_ITEM.get());
                        output.accept(ModItems.SEABED_DREDGER_ITEM.get());
                        output.accept(ModItems.OCEAN_ALTAR_ITEM.get());
                        output.accept(ModItems.ABYSSAL_PORTAL_ITEM.get());
                        output.accept(ModItems.KELP_BIO_PELLET.get());
                        output.accept(ModItems.OCEAN_GUIDE_BOOK.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
