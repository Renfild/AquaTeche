package net.aquatech.machines.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

/** Мост к ростеру рыб: тир удочки StarCatcher → пул рыбы. */
public final class FisherLoot {

    private FisherLoot() {
    }

    public static int tierOf(ResourceLocation rodId) {
        return rodId == null ? 0 : FishRosterService.tierOf(rodId.getPath());
    }

    public static ItemStack roll(int tier, RandomSource random) {
        return FishRosterService.roll(tier, random);
    }
}
