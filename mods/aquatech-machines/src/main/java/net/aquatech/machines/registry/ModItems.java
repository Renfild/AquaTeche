package net.aquatech.machines.registry;

import net.aquatech.machines.block.ExcavatorBlock;
import net.aquatech.machines.block.ExtractorBlock;
import net.aquatech.machines.block.FisherBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, "aquatech_machines");

    public static final RegistryObject<Item> FISHER = ITEMS.register("fisher",
            () -> new BlockItem(ModBlocks.FISHER.get(), new Item.Properties()));

    public static final RegistryObject<Item> EXCAVATOR = ITEMS.register("excavator",
            () -> new BlockItem(ModBlocks.EXCAVATOR.get(), new Item.Properties()));

    public static final RegistryObject<Item> EXTRACTOR = ITEMS.register("extractor",
            () -> new BlockItem(ModBlocks.EXTRACTOR.get(), new Item.Properties()));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
