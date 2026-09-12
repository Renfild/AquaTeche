package net.aquatech.machines.block.entity;

import net.aquatech.machines.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/** Экскаватор: добывает грунт/кристаллы/ресурсы МЭ (лут бывшей Драги, усиленный). */
public class ExcavatorBlockEntity extends BaseMachineBlockEntity {

    public static final int SLOT_OUTPUT_START = 0;
    public static final int OUTPUT_SLOTS = 9;

    private record Entry(String id, int weight, int min, int max) {}

    private static final List<Entry> POOL = List.of(
            new Entry("minecraft:sand", 22, 2, 6),
            new Entry("minecraft:quartz", 16, 1, 4),
            new Entry("ae2:certus_quartz_crystal", 12, 1, 3),
            new Entry("ae2:charged_certus_quartz_crystal", 7, 1, 2),
            new Entry("ae2:sky_stone_block", 10, 1, 3),
            new Entry("ae2:sky_dust", 9, 1, 3),
            new Entry("ae2:fluix_crystal", 8, 1, 2),
            new Entry("botania:manasteel_ingot", 6, 1, 2),
            new Entry("botania:elementium_ingot", 4, 1, 1),
            new Entry("botania:terrasteel_ingot", 2, 1, 1),
            new Entry("industrialupgrade:baseore/spinel", 6, 1, 2),
            new Entry("industrialupgrade:baseore2/strontium", 6, 1, 2),
            new Entry("industrialupgrade:baseore2/barium", 5, 1, 2)
    );

    public ExcavatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.EXCAVATOR.get(), pos, state, OUTPUT_SLOTS, 100000, 512, 100, 60);
    }

    @Override
    protected net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id, Inventory inv) {
        return new net.aquatech.machines.inventory.ExcavatorMenu(id, inv, this);
    }

    @Override
    protected int outputSlots() {
        return OUTPUT_SLOTS;
    }

    @Override
    protected boolean hasWork() {
        return hasOutputRoom();
    }

    private boolean hasOutputRoom() {
        for (int i = 0; i < OUTPUT_SLOTS; i++) {
            ItemStack s = items.getStackInSlot(i);
            if (s.isEmpty() || s.getCount() < s.getMaxStackSize()) return true;
        }
        return false;
    }

    @Override
    protected void craftOnce() {
        RandomSource random = level.getRandom();
        int total = 0;
        for (Entry e : POOL) total += e.weight();
        int roll = random.nextInt(total);
        Entry picked = POOL.get(POOL.size() - 1);
        for (Entry e : POOL) {
            roll -= e.weight();
            if (roll < 0) { picked = e; break; }
        }
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(picked.id()));
        if (item == null || item == Items.AIR) return;
        ItemStack stack = new ItemStack(item, picked.min() + random.nextInt(picked.max() - picked.min() + 1));
        for (int slot = 0; slot < OUTPUT_SLOTS; slot++) {
            if (items.insertItem(slot, stack, false).isEmpty()) return;
        }
    }
}
