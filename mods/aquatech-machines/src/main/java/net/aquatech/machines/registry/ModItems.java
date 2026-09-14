package net.aquatech.machines.registry;

import net.aquatech.machines.item.UpgradeItem;
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

    public static final RegistryObject<Item> SYNTHESIZER = ITEMS.register("synthesizer",
            () -> new BlockItem(ModBlocks.SYNTHESIZER.get(), new Item.Properties()));

    public static final RegistryObject<Item> CENTRIFUGE = ITEMS.register("centrifuge",
            () -> new BlockItem(ModBlocks.CENTRIFUGE.get(), new Item.Properties()));

    public static final RegistryObject<Item> FLOWER_COLLECTOR = ITEMS.register("flower_collector",
            () -> new BlockItem(ModBlocks.FLOWER_COLLECTOR.get(), new Item.Properties()));

    public static final RegistryObject<Item> MANA_FABRICATOR = ITEMS.register("mana_fabricator",
            () -> new BlockItem(ModBlocks.MANA_FABRICATOR.get(), new Item.Properties()));

    public static final RegistryObject<Item> VOLCANIC_CRYSTAL = ITEMS.register("volcanic_crystal",
            () -> new Item(new Item.Properties().fireResistant()));

    public static final RegistryObject<Item> ABYSSAL_ALLOY = ITEMS.register("abyssal_alloy",
            () -> new Item(new Item.Properties().fireResistant()));

    public static final RegistryObject<Item> SEA_SALT = ITEMS.register("sea_salt",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> FISHING_CORE = ITEMS.register("fishing_core",
            () -> new UpgradeItem(UpgradeItem.Type.FISHING_CORE, new Item.Properties().stacksTo(1).fireResistant()));

    public static final RegistryObject<Item> SPEED_UPGRADE_1 = ITEMS.register("speed_upgrade_1",
            () -> new UpgradeItem(UpgradeItem.Type.SPEED_1, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SPEED_UPGRADE_4 = ITEMS.register("speed_upgrade_4",
            () -> new UpgradeItem(UpgradeItem.Type.SPEED_4, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SPEED_UPGRADE = ITEMS.register("speed_upgrade",
            () -> new UpgradeItem(UpgradeItem.Type.SPEED_1, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> ENERGY_EFFICIENCY = ITEMS.register("energy_efficiency",
            () -> new UpgradeItem(UpgradeItem.Type.ENERGY_EFFICIENCY, new Item.Properties().stacksTo(1)));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
