package com.casesmod.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * Calculates selling prices (in Dubloons) for fish based on StarCatcher NBT tags:
 * Rarity, Weight, Size, and Golden trophy status.
 */
public class FishPriceCalculator {

    public static class PriceResult {
        public final boolean isFish;
        public final long finalPrice;
        public final String rarityName;
        public final boolean isGolden;
        public final float weightKg;

        public PriceResult(boolean isFish, long finalPrice, String rarityName, boolean isGolden, float weightKg) {
            this.isFish = isFish;
            this.finalPrice = finalPrice;
            this.rarityName = rarityName;
            this.isGolden = isGolden;
            this.weightKg = weightKg;
        }

        public static PriceResult NOT_FISH = new PriceResult(false, 0, "", false, 0f);
    }

    public static PriceResult calculatePrice(ItemStack stack) {
        if (stack.isEmpty()) return PriceResult.NOT_FISH;

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null) return PriceResult.NOT_FISH;

        String namespace = id.getNamespace();
        String path = id.getPath().toLowerCase();

        boolean isStarCatcher = "starcatcher".equals(namespace);
        boolean isVanillaFish = stack.is(Items.COD) || stack.is(Items.SALMON) || stack.is(Items.PUFFERFISH) || stack.is(Items.TROPICAL_FISH) || stack.is(Items.COOKED_COD) || stack.is(Items.COOKED_SALMON);
        boolean isNameFish = path.contains("fish") || path.contains("salmon") || path.contains("cod") 
                || path.contains("bream") || path.contains("bass") || path.contains("trout") || path.contains("tuna") 
                || path.contains("eel") || path.contains("shark") || path.contains("crab") || path.contains("shrimp") 
                || path.contains("squid") || path.contains("octopus") || path.contains("lobster") || path.contains("clam") 
                || path.contains("ray") || path.contains("carp") || path.contains("perch") || path.contains("anchovy") 
                || path.contains("sardine") || path.contains("herring") || path.contains("halibut") || path.contains("mackerel") 
                || path.contains("catfish") || path.contains("sturgeon") || path.contains("flounder") || path.contains("pike") 
                || path.contains("angelfish") || path.contains("betta") || path.contains("guppy") || path.contains("tetra") 
                || path.contains("koi") || path.contains("goldfish") || path.contains("piranha") || path.contains("barracuda") 
                || path.contains("swordfish") || path.contains("marlin") || path.contains("sailfish") || path.contains("sunfish") 
                || path.contains("angler") || path.contains("blobfish") || path.contains("coelacanth") || path.contains("hammerhead") 
                || path.contains("megalodon") || path.contains("kraken");

        if (!isStarCatcher && !isVanillaFish && !isNameFish) {
            return PriceResult.NOT_FISH;
        }

        // Base price calculation by rarity
        long basePrice = 12L;
        String rarityName = "Обычная";
        float weightKg = 1.0f;
        boolean isGolden = false;

        CompoundTag nbt = stack.getTag();
        if (nbt != null) {
            CompoundTag scTag = nbt.contains("starcatcher") ? nbt.getCompound("starcatcher") : nbt;

            // Rarity check
            if (scTag.contains("rarity")) {
                String rarity = scTag.getString("rarity").toUpperCase();
                switch (rarity) {
                    case "TRASH":
                        basePrice = 2L;
                        rarityName = "Мусор";
                        break;
                    case "COMMON":
                        basePrice = 12L;
                        rarityName = "Обычная";
                        break;
                    case "UNCOMMON":
                        basePrice = 32L;
                        rarityName = "Необычная";
                        break;
                    case "RARE":
                        basePrice = 95L;
                        rarityName = "Редкая";
                        break;
                    case "EPIC":
                        basePrice = 260L;
                        rarityName = "Эпическая";
                        break;
                    case "LEGENDARY":
                    case "MYTHIC":
                        basePrice = 850L;
                        rarityName = "Легендарная";
                        break;
                    default:
                        basePrice = 15L;
                        rarityName = "Обычная";
                        break;
                }
            }

            // Weight check
            if (scTag.contains("weight")) {
                weightKg = scTag.getFloat("weight");
            } else if (scTag.contains("Weight")) {
                weightKg = scTag.getFloat("Weight");
            } else if (scTag.contains("size")) {
                weightKg = scTag.getFloat("size") * 0.5f;
            }

            // Golden check
            if (scTag.contains("golden") && scTag.getBoolean("golden")) {
                isGolden = true;
            } else if (scTag.contains("Golden") && scTag.getBoolean("Golden")) {
                isGolden = true;
            }
        } else {
            // Vanilla fish defaults
            if (stack.is(Items.PUFFERFISH)) {
                basePrice = 25L;
                rarityName = "Необычная";
            } else if (stack.is(Items.TROPICAL_FISH)) {
                basePrice = 35L;
                rarityName = "Редкая";
            }
        }

        // Weight multiplier
        float weightMult = Math.max(0.8f, Math.min(3.5f, 1.0f + ((weightKg - 1.0f) * 0.25f)));

        // Golden multiplier
        float goldenMult = isGolden ? 2.5f : 1.0f;

        long unitPrice = Math.max(1L, Math.round(basePrice * weightMult * goldenMult));
        long finalPrice = unitPrice * stack.getCount();

        return new PriceResult(true, finalPrice, rarityName, isGolden, weightKg);
    }
}
