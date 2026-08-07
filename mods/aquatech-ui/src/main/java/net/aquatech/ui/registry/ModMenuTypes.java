package net.aquatech.ui.registry;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.inventory.AutoFisherMenu;
import net.aquatech.ui.inventory.OceanAltarMenu;
import net.aquatech.ui.inventory.OceanFilterMenu;
import net.aquatech.ui.inventory.SeabedDredgerMenu;
import net.aquatech.ui.inventory.TackleBoxMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, AquaTechUI.MOD_ID);

    public static final RegistryObject<MenuType<AutoFisherMenu>> AUTO_FISHER_MENU = MENUS.register("auto_fisher_menu",
            () -> IForgeMenuType.create(AutoFisherMenu::new));

    public static final RegistryObject<MenuType<OceanFilterMenu>> OCEAN_FILTER_MENU = MENUS.register("ocean_filter_menu",
            () -> IForgeMenuType.create(OceanFilterMenu::new));

    public static final RegistryObject<MenuType<SeabedDredgerMenu>> SEABED_DREDGER_MENU = MENUS.register("seabed_dredger_menu",
            () -> IForgeMenuType.create(SeabedDredgerMenu::new));

    public static final RegistryObject<MenuType<TackleBoxMenu>> TACKLE_BOX_MENU = MENUS.register("tackle_box_menu",
            () -> IForgeMenuType.create(TackleBoxMenu::new));

    public static final RegistryObject<MenuType<OceanAltarMenu>> OCEAN_ALTAR_MENU = MENUS.register("ocean_altar_menu",
            () -> IForgeMenuType.create(OceanAltarMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
