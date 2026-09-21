package net.aquatech.ui.registry;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.item.AbyssalMagnetItem;
import net.aquatech.ui.item.RateModItem;
import net.aquatech.ui.item.SonarGogglesItem;
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
    public static final Map<RateModItem.RateTier, RegistryObject<Item>> RATE_MODS = new EnumMap<>(RateModItem.RateTier.class);

    static {
        for (RateModItem.RateTier tier : RateModItem.RateTier.values()) {
            RATE_MODS.put(tier, ITEMS.register(tier.getId(),
                    () -> new RateModItem(tier, new Item.Properties())));
        }
    }



    public static final RegistryObject<Item> SONAR_GOGGLES = ITEMS.register("sonar_goggles",
            () -> new SonarGogglesItem(new Item.Properties()));

    public static final RegistryObject<Item> ABYSSAL_MAGNET = ITEMS.register("abyssal_magnet",
            () -> new AbyssalMagnetItem(new Item.Properties()));









    public static final RegistryObject<Item> ABYSSAL_PEARL = ITEMS.register("abyssal_pearl",
            () -> new net.aquatech.ui.item.AbyssalPearlItem(new Item.Properties().stacksTo(1).fireResistant()));

    public static final RegistryObject<Item> INFERNAL_PEARL = ITEMS.register("infernal_pearl",
            () -> new net.aquatech.ui.item.InfernalPearlItem(new Item.Properties().stacksTo(1).fireResistant()));

    // Приманки из рыбного сырья (заряжаются в удочку, тратятся на ручной улов)
    public static final RegistryObject<Item> BAIT_SHOAL = ITEMS.register("bait_shoal",
            () -> new Item(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> BAIT_ORE = ITEMS.register("bait_ore",
            () -> new Item(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> BAIT_ABYSS = ITEMS.register("bait_abyss",
            () -> new Item(new Item.Properties().stacksTo(16)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
