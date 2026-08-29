package net.aquatech.ui.fishing;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Maps StarCatcher rods to AquaTech loot / AutoFisher.
 * Fish-only: sky_rod. Bone rod farms overworld hostile drops.
 */
public final class FishingRodCompat {
    /** Only these rods keep StarCatcher fish catches. */
    private static final Set<String> FISH_ONLY = Set.of(
            "sky_rod"
    );

    private FishingRodCompat() {
    }

    public static boolean isSupportedRod(ItemStack stack) {
        return isStarCatcherRod(stack);
    }

    /** Resource rods — AutoFisher + AquaTech IU loot override. */
    public static boolean isResourceRod(ItemStack stack) {
        String path = rodPath(stack);
        return path != null && !FISH_ONLY.contains(path);
    }

    public static boolean isFishOnlyRod(ItemStack stack) {
        String path = rodPath(stack);
        return path != null && FISH_ONLY.contains(path);
    }

    /**
     * Loot tier for resource rods. Temporary defaults until loot tables are assigned per rod.
     */
    @Nullable
    public static AquaTechFishingRodItem.RodType resolveRodType(ItemStack stack) {
        String path = rodPath(stack);
        if (path == null) return AquaTechFishingRodItem.RodType.NOVICE;

        return switch (path) {
            case "bamboo_rod", "good_old_rod" ->
                    AquaTechFishingRodItem.RodType.NOVICE; // Tier 1 (bamboo = starter blocks)
            // humble is early ores (maps closer to iron-tier pool in generateLoot fallback)
            case "humble_rod", "naturalist_rod", "sky_rod" ->
                    AquaTechFishingRodItem.RodType.IRON; // Tier 2
            case "boner_rod" ->
                    AquaTechFishingRodItem.RodType.DIAMOND;
            case "starcatcher_rod", "slimed_rod" ->
                    AquaTechFishingRodItem.RodType.GOLD; // Tier 3
            case "iceborn_rod", "obsidian_rod" ->
                    AquaTechFishingRodItem.RodType.DIAMOND; // Tier 4
            case "sharktooth_rod", "azure_crystal_rod" ->
                    AquaTechFishingRodItem.RodType.EMERALD; // Tier 5
            case "magmaforged_rod" ->
                    AquaTechFishingRodItem.RodType.NETHERITE; // Tier 6
            case "lush_glowberry_rod" ->
                    AquaTechFishingRodItem.RodType.PRISMARINE; // Tier 7
            case "thermal_rod" ->
                    AquaTechFishingRodItem.RodType.THERMAL; // Tier 8
            case "kinetic_rod" ->
                    AquaTechFishingRodItem.RodType.KINETIC; // Tier 9
            case "ender_rod" ->
                    AquaTechFishingRodItem.RodType.ENDER; // Tier 10
            case "alpha_rod", "abyssal_rod" ->
                    AquaTechFishingRodItem.RodType.ABYSSAL; // Tier 11
            default -> AquaTechFishingRodItem.RodType.NOVICE;
        };
    }

    private static boolean isStarCatcherRod(ItemStack stack) {
        return rodPath(stack) != null;
    }

    @Nullable
    public static String getRodId(ItemStack stack) {
        return rodPath(stack);
    }

    @Nullable
    private static String rodPath(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null) return null;
        if ("minecraft".equals(id.getNamespace()) && "fishing_rod".equals(id.getPath())) {
            return "bamboo_rod";
        }
        if (!"starcatcher".equals(id.getNamespace())) return null;
        String path = id.getPath();
        if (!path.endsWith("_rod") && !"starcatcher_rod".equals(path)) return null;
        return path;
    }
}
