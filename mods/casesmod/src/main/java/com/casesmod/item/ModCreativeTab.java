package com.casesmod.item;

import com.casesmod.CasesMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** Отдельная вкладка в творческом режиме — для тестирования предмета-открывателя меню. */
public class ModCreativeTab {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CasesMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN_TAB = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.literal("Cases / Kits / Warps"))
                    .icon(() -> new ItemStack(Items.ENDER_CHEST))
                    .displayItems((params, output) -> output.accept(ModItems.MENU_OPENER.get()))
                    .build());

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }
}
