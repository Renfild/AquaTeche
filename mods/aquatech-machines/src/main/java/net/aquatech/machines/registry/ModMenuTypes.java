package net.aquatech.machines.registry;

import net.aquatech.machines.AquaTechMachinesMod;
import net.aquatech.machines.inventory.ExcavatorMenu;
import net.aquatech.machines.inventory.ExtractorMenu;
import net.aquatech.machines.inventory.FisherMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, AquaTechMachinesMod.MOD_ID);

    public static final RegistryObject<MenuType<FisherMenu>> FISHER =
            MENUS.register("fisher", () -> IForgeMenuType.create(FisherMenu::new));

    public static final RegistryObject<MenuType<ExcavatorMenu>> EXCAVATOR =
            MENUS.register("excavator", () -> IForgeMenuType.create(ExcavatorMenu::new));

    public static final RegistryObject<MenuType<ExtractorMenu>> EXTRACTOR =
            MENUS.register("extractor", () -> IForgeMenuType.create(ExtractorMenu::new));

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }
}
