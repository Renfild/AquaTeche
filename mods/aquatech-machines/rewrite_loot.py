# -*- coding: utf-8 -*-
# FisherLoot v2: resource pools by rod tier (base mode) + fish roll proxy (upgraded).
code = 'package net.aquatech.machines.util;\n\n'

code += '''import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * Ресурсный лут рыболова (без апгрейда): руда/ресурсы по тиру удочки.
 * Рыба — только с апгрейдом (FishRosterService.roll).
 */
public final class FisherLoot {

    private record Entry(String id, int weight, int min, int max) {}

    private static final List<Entry> BASE = List.of(
            new Entry("minecraft:iron_ore", 14, 1, 2),
            new Entry("minecraft:copper_ore", 14, 1, 2),
            new Entry("minecraft:coal_ore", 12, 1, 2),
            new Entry("minecraft:raw_iron", 8, 1, 2),
            new Entry("industrialupgrade:classicore/tin", 10, 1, 2)
    );
    private static final List<Entry> T3 = List.of(
            new Entry("minecraft:redstone_ore", 10, 1, 2),
            new Entry("minecraft:lapis_ore", 9, 1, 2),
            new Entry("industrialupgrade:baseore/spinel", 8, 1, 2),
            new Entry("industrialupgrade:baseore2/strontium", 8, 1, 2),
            new Entry("industrialupgrade:baseore2/barium", 8, 1, 2)
    );
    private static final List<Entry> T5 = List.of(
            new Entry("industrialupgrade:baseore/silver", 8, 1, 2),
            new Entry("industrialupgrade:baseore/nickel", 8, 1, 2),
            new Entry("industrialupgrade:baseore/aluminium", 8, 1, 2),
            new Entry("minecraft:obsidian", 7, 1, 2)
    );
    private static final List<Entry> T7 = List.of(
            new Entry("industrialupgrade:baseore/tungsten", 8, 1, 2),
            new Entry("industrialupgrade:baseore/chromium", 8, 1, 2),
            new Entry("industrialupgrade:preciousgem/sapphire_gem", 6, 1, 1),
            new Entry("industrialupgrade:preciousgem/topaz_gem", 6, 1, 1)
    );
    private static final List<Entry> T9 = List.of(
            new Entry("industrialupgrade:baseore/titanium", 8, 1, 2),
            new Entry("industrialupgrade:baseore/cobalt", 8, 1, 2),
            new Entry("minecraft:diamond_ore", 7, 1, 2),
            new Entry("industrialupgrade:alloyingot/stainless_steel", 6, 1, 1),
            new Entry("industrialupgrade:preciousgem/ruby_gem", 5, 1, 2)
    );
    private static final List<Entry> T11 = List.of(
            new Entry("industrialupgrade:baseore/platinum", 7, 1, 2),
            new Entry("industrialupgrade:alloyingot/inconel", 5, 1, 1),
            new Entry("minecraft:ancient_debris", 3, 1, 1)
    );
    private static final List<Entry> T13 = List.of(
            new Entry("industrialupgrade:baseore/iridium", 5, 1, 1),
            new Entry("industrialupgrade:alloyingot/osmiridium", 4, 1, 1),
            new Entry("minecraft:nether_star", 2, 1, 1)
    );

    private FisherLoot() {
    }

    /** Тир удочки StarCatcher по имени предмета (bamboo=1 ... alpha=13). */
    public static int tierOf(String rodPath) {
        if (rodPath == null) return 0;
        return switch (rodPath) {
            case "bamboo_rod" -> 1;
            case "humble_rod" -> 2;
            case "good_old_rod", "boner_rod" -> 3;
            case "naturalist_rod" -> 4;
            case "slimed_rod" -> 5;
            case "iceborn_rod" -> 6;
            case "starcatcher_rod" -> 7;
            case "azure_crystal_rod" -> 8;
            case "sharktooth_rod" -> 9;
            case "obsidian_rod" -> 10;
            case "lush_glowberry_rod" -> 11;
            case "magmaforged_rod" -> 12;
            case "alpha_rod" -> 13;
            default -> 0;
        };
    }

    /** Ресурсный лут по тиру (базовый режим, без апгрейда). */
    public static ItemStack rollResources(int tier, RandomSource random) {
        List<Entry> pool = new ArrayList<>(BASE);
        if (tier >= 3) pool.addAll(T3);
        if (tier >= 5) pool.addAll(T5);
        if (tier >= 7) pool.addAll(T7);
        if (tier >= 9) pool.addAll(T9);
        if (tier >= 11) pool.addAll(T11);
        if (tier >= 13) pool.addAll(T13);
        return weightedRoll(pool, random, 1 + random.nextInt(2));
    }

    /** Рыба по ростеру (с апгрейдом) — прокси к FishRosterService. */
    public static ItemStack roll(int tier, RandomSource random) {
        return FishRosterService.roll(tier, random);
    }

    private static ItemStack weightedRoll(List<Entry> pool, RandomSource random, int draws) {
        ItemStack result = ItemStack.EMPTY;
        for (int d = 0; d < draws; d++) {
            int total = 0;
            for (Entry e : pool) total += e.weight();
            int roll = random.nextInt(total);
            Entry picked = pool.get(pool.size() - 1);
            for (Entry e : pool) {
                roll -= e.weight();
                if (roll < 0) { picked = e; break; }
            }
            ResourceLocation id = new ResourceLocation(picked.id());
            Item item = ForgeRegistries.ITEMS.getValue(id);
            if (item == null || item == Items.AIR) continue;
            result = new ItemStack(item, picked.min() + random.nextInt(picked.max() - picked.min() + 1));
        }
        return result;
    }
}
'''
open("src/main/java/net/aquatech/machines/util/FisherLoot.java", "w", encoding="utf-8", newline="\n").write(code)
print("fisherloot v2 written")
