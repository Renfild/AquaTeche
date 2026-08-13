package net.aquatech.ui.fishing;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Catch uses for StarCatcher rods. Numbers match kubejs/startup_scripts/40_rod_durability.js
 * (KubeJS sets maxDamage so the vanilla bar + tooltip work).
 */
public final class RodDurability {
    private static final String LEGACY_WEAR = "AquaFisherWear";

    private RodDurability() {
    }

    public static int maxUses(ItemStack rod) {
        if (rod != null && !rod.isEmpty() && rod.isDamageableItem()) {
            return rod.getMaxDamage();
        }
        return maxUsesForPath(FishingRodCompat.getRodId(rod));
    }

    public static int maxUsesForPath(String path) {
        if (path == null) return 128;
        return switch (path) {
            case "bamboo_rod", "good_old_rod", "humble_rod", "sky_rod", "boner_rod" -> 128;
            case "naturalist_rod", "starcatcher_rod", "slimed_rod" -> 192;
            case "iceborn_rod", "obsidian_rod", "sharktooth_rod", "azure_crystal_rod" -> 256;
            default -> 320;
        };
    }

    public static int remaining(ItemStack rod) {
        if (rod == null || rod.isEmpty()) return 0;
        if (rod.isDamageableItem()) {
            return Math.max(0, rod.getMaxDamage() - rod.getDamageValue());
        }
        CompoundTag tag = rod.getTag();
        int wear = tag != null ? tag.getInt(LEGACY_WEAR) : 0;
        return Math.max(0, maxUses(rod) - wear);
    }

    /**
     * Spend one catch. Returns false if the rod broke (caller should clear inventory slot).
     */
    public static boolean wearOne(ItemStack rod, @Nullable LivingEntity breaker) {
        if (rod == null || rod.isEmpty()) return false;

        if (rod.isDamageableItem()) {
            int nextDamage = rod.getDamageValue() + 1;
            rod.setDamageValue(nextDamage);
            if (nextDamage >= rod.getMaxDamage()) {
                if (breaker != null) {
                    breaker.broadcastBreakEvent(breaker.getUsedItemHand());
                }
                rod.setCount(0);
                return false;
            }
            return true;
        }

        CompoundTag tag = rod.getOrCreateTag();
        int wear = tag.getInt(LEGACY_WEAR) + 1;
        int max = maxUses(rod);
        if (wear >= max) {
            rod.setCount(0);
            return false;
        }
        tag.putInt(LEGACY_WEAR, wear);
        tag.putInt("Damage", wear);
        tag.putInt("aquatech_ui:af_max", max);
        return true;
    }
}
