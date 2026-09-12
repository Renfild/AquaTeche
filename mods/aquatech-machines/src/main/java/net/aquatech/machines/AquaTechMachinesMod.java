package net.aquatech.machines;

import net.aquatech.machines.registry.ModBlockEntities;
import net.aquatech.machines.registry.ModBlocks;
import net.aquatech.machines.registry.ModCreativeTabs;
import net.aquatech.machines.registry.ModItems;
import net.aquatech.machines.registry.ModMenuTypes;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * AquaTech: Механизмы — Рыболов MK-2, Экскаватор, Экстрактор.
 * FE-энергия, авто-выход в сундук. Всё билдится кодом, модели — простые кубы с текстурами.
 */
@Mod(AquaTechMachinesMod.MOD_ID)
public class AquaTechMachinesMod {

    public static final String MOD_ID = "aquatech_machines";

    public AquaTechMachinesMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.register(bus);
        ModBlockEntities.register(bus);
        ModItems.register(bus);
        ModMenuTypes.register(bus);
        ModCreativeTabs.register(bus);
    }
}
