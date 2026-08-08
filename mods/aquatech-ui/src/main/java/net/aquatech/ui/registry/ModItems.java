package net.aquatech.ui.registry;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.item.AbyssalMagnetItem;
import net.aquatech.ui.item.RateModItem;
import net.aquatech.ui.item.SonarGogglesItem;
import net.aquatech.ui.item.UpgradeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumMap;
import java.util.Map;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AquaTechUI.MOD_ID);

    // Custom AquaTech fishing rods disabled — use StarCatcher rods.
    public static final Map<UpgradeItem.UpgradeType, RegistryObject<Item>> UPGRADES = new EnumMap<>(UpgradeItem.UpgradeType.class);
    public static final Map<RateModItem.RateTier, RegistryObject<Item>> RATE_MODS = new EnumMap<>(RateModItem.RateTier.class);

    static {
        for (UpgradeItem.UpgradeType type : UpgradeItem.UpgradeType.values()) {
            UPGRADES.put(type, ITEMS.register(type.getId() + "_upgrade",
                    () -> new UpgradeItem(type, new Item.Properties())));
        }

        for (RateModItem.RateTier tier : RateModItem.RateTier.values()) {
            RATE_MODS.put(tier, ITEMS.register(tier.getId(),
                    () -> new RateModItem(tier, new Item.Properties())));
        }
    }

    public static final RegistryObject<Item> MESH_FILTER = ITEMS.register("mesh_filter",
            () -> new Item(new Item.Properties().durability(200)));

    public static final RegistryObject<Item> DREDGER_DRILL_BIT = ITEMS.register("dredger_drill_bit",
            () -> new Item(new Item.Properties().durability(300)));

    public static final RegistryObject<Item> SONAR_GOGGLES = ITEMS.register("sonar_goggles",
            () -> new SonarGogglesItem(new Item.Properties()));

    public static final RegistryObject<Item> ABYSSAL_MAGNET = ITEMS.register("abyssal_magnet",
            () -> new AbyssalMagnetItem(new Item.Properties()));

    public static final RegistryObject<Item> AUTO_FISHER_ITEM = ITEMS.register("auto_fisher",
            () -> new BlockItem(ModBlocks.AUTO_FISHER.get(), new Item.Properties()));

    public static final RegistryObject<Item> OCEAN_FILTER_ITEM = ITEMS.register("ocean_filter",
            () -> new BlockItem(ModBlocks.OCEAN_FILTER.get(), new Item.Properties()));

    public static final RegistryObject<Item> SEABED_DREDGER_ITEM = ITEMS.register("seabed_dredger",
            () -> new BlockItem(ModBlocks.SEABED_DREDGER.get(), new Item.Properties()));

    public static final RegistryObject<Item> OCEAN_ALTAR_ITEM = ITEMS.register("ocean_altar",
            () -> new BlockItem(ModBlocks.OCEAN_ALTAR.get(), new Item.Properties()));

    public static final RegistryObject<Item> ABYSSAL_PORTAL_ITEM = ITEMS.register("abyssal_portal",
            () -> new BlockItem(ModBlocks.ABYSSAL_PORTAL.get(), new Item.Properties()));

    public static final RegistryObject<Item> KELP_BIO_PELLET = ITEMS.register("kelp_bio_pellet",
            () -> new Item(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
