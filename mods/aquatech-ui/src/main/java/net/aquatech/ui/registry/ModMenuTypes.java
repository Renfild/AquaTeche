package net.aquatech.ui.registry;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.inventory.TackleBoxMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, AquaTechUI.MOD_ID);




    public static final RegistryObject<MenuType<TackleBoxMenu>> TACKLE_BOX_MENU = MENUS.register("tackle_box_menu",
            () -> IForgeMenuType.create(TackleBoxMenu::new));



    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
