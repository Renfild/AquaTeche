package com.casesmod.data;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Цены StarCatcher-рыбы в Дублонах.
 * NBT: top-level compound {@code starcatcher:caught_fish_info}
 * (size cm, weight grams, rarity lowercase, golden bool).
 */
public final class FishPriceCalculator {
    private static final String CATCH_INFO = "starcatcher:caught_fish_info";
    private static final double DEFAULT_AVG_KG = 0.5;
    private static final double GOLDEN_MULT = 2.5;

    private FishPriceCalculator() {}

    public static PriceResult calculatePrice(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return PriceResult.none();
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null || !"starcatcher".equals(id.getNamespace())) {
            return PriceResult.none();
        }

        CompoundTag root = stack.getTag();
        if (root == null || !root.contains(CATCH_INFO)) {
            // SC fish without catch stats — trash floor price
            long price = Math.max(1L, 2L * stack.getCount());
            return new PriceResult(true, price, "trash", false, 0f, 0);
        }

        CompoundTag info = root.getCompound(CATCH_INFO);
        String rarity = info.contains("rarity") ? info.getString("rarity").toLowerCase() : "common";
        if ("none".equals(rarity) || "golden".equals(rarity)) {
            rarity = info.getBoolean("golden") ? "legendary" : "common";
        }
        boolean golden = info.getBoolean("golden");
        int weightGrams = info.contains("weight") ? info.getInt("weight") : 0;
        int sizeCm = info.contains("size") ? info.getInt("size") : 0;
        double weightKg = weightGrams > 0 ? weightGrams / 1000.0 : 0.0;

        long base = basePrice(rarity);
        double weightMult = weightMultiplier(weightKg);
        double goldenMult = golden ? GOLDEN_MULT : 1.0;
        long unit = Math.max(1L, Math.round(base * weightMult * goldenMult));
        long total = unit * Math.max(1, stack.getCount());

        return new PriceResult(true, total, rarity, golden, (float) weightKg, sizeCm);
    }

    private static long basePrice(String rarity) {
        return switch (rarity) {
            case "trash" -> 2L;
            case "common" -> 12L;
            case "uncommon" -> 35L;
            case "rare" -> 100L;
            case "epic" -> 290L;
            case "legendary", "mythic" -> 900L;
            default -> 12L;
        };
    }

    /** 1.0 + (kg / avgKg) * 0.5, clamped to [1.0, 2.5]. */
    private static double weightMultiplier(double weightKg) {
        if (weightKg <= 0) return 1.0;
        double mult = 1.0 + (weightKg / DEFAULT_AVG_KG) * 0.5;
        return Math.max(1.0, Math.min(2.5, mult));
    }

    public record PriceResult(
            boolean isFish,
            long finalPrice,
            String rarityName,
            boolean isGolden,
            float weightKg,
            int sizeCm
    ) {
        public static PriceResult none() {
            return new PriceResult(false, 0L, "", false, 0f, 0);
        }
    }
}
