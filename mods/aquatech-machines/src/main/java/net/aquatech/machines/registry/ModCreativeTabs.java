package net.aquatech.machines.registry;

import net.aquatech.machines.AquaTechMachinesMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AquaTechMachinesMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MACHINES_TAB =
            TABS.register("machines_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.aquatech_machines"))
                    .icon(() -> new ItemStack(ModItems.FISHER.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.FISHER.get());
                        output.accept(ModItems.EXCAVATOR.get());
                        output.accept(ModItems.EXTRACTOR.get());
                    })
                    .build());

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }
}
