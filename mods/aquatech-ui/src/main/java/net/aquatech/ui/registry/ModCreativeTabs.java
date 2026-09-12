package net.aquatech.ui.registry;

import net.aquatech.ui.AquaTechUI;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {

    public static final net.minecraft.resources.ResourceKey<net.minecraft.core.Registry<CreativeModeTab>> TAB_REGISTRY_KEY =
            net.minecraft.resources.ResourceKey.createRegistryKey(new net.minecraft.resources.ResourceLocation("minecraft", "creative_mode_tab"));

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(TAB_REGISTRY_KEY, AquaTechUI.MOD_ID);


    public static final RegistryObject<CreativeModeTab> AQUATECH_TAB = CREATIVE_MODE_TABS.register("aquatech_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.aquatech_tab"))
                    .icon(() -> new ItemStack(ModItems.RATE_MODS.get(net.aquatech.ui.item.RateModItem.RateTier.X64).get()))
                    .displayItems((parameters, output) -> {
                        // Рыболовство: ресурсные удочки (включая небесную)
                        String[] scRods = {
                                "bamboo_rod", "humble_rod", "boner_rod", "good_old_rod",
                                "naturalist_rod", "sky_rod", "slimed_rod", "iceborn_rod",
                                "starcatcher_rod", "azure_crystal_rod", "sharktooth_rod",
                                "obsidian_rod", "lush_glowberry_rod", "magmaforged_rod", "alpha_rod"
                        };
                        for (String rod : scRods) {
                            var item = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(
                                    new net.minecraft.resources.ResourceLocation("starcatcher", rod));
                            if (item != null && item != net.minecraft.world.item.Items.AIR) {
                                output.accept(item);
                            }
                        }

                        // 4. Рейт-модули
                        for (var entry : ModItems.RATE_MODS.values()) {
                            output.accept(entry.get());
                        }

                        // 5. Оснастка и артефакты
                        output.accept(ModItems.ABYSSAL_PEARL.get());
                        output.accept(ModItems.INFERNAL_PEARL.get());
                        output.accept(ModItems.SONAR_GOGGLES.get());
                        output.accept(ModItems.ABYSSAL_MAGNET.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
