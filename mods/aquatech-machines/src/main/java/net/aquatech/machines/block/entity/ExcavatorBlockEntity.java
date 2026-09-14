package net.aquatech.machines.block.entity;

import net.aquatech.machines.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/** Экскаватор: добывает грунт/кристаллы/ресурсы МЭ (13 видов с шансами). */
public class ExcavatorBlockEntity extends BaseMachineBlockEntity {

    public static final int SLOT_OUTPUT_START = 0;
    public static final int OUTPUT_SLOTS = 9;
    public static final int SLOT_SPEED = 9;
    public static final int SLOT_EFF = 10;
    public static final int SLOT_BATTERY = 11;

    public record Entry(String id, int weight, int min, int max, String label) {}

    public static final List<Entry> POOL = List.of(
            new Entry("ae2:certus_quartz_crystal", 30, 1, 4, "Истинный кварц"),
            new Entry("ae2:certus_quartz_dust", 20, 1, 4, "Пыль истинного кварца"),
            new Entry("ae2:sky_stone_block", 25, 2, 6, "Небесный камень"),
            new Entry("ae2:sky_dust", 15, 1, 4, "Пыль небесного камня"),
            new Entry("ae2:silicon", 15, 1, 3, "Кремний"),
            new Entry("ae2:fluix_crystal", 10, 1, 2, "Изменчивый кристалл"),
            new Entry("ae2:fluix_dust", 10, 1, 3, "Изменчивая пыль")
    );

    public ExcavatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.EXCAVATOR.get(), pos, state, OUTPUT_SLOTS + 3, 200000, 1024, 100, 60, 200000);
        defineSlots(SLOT_OUTPUT_START, SLOT_OUTPUT_START + OUTPUT_SLOTS - 1, SLOT_SPEED, SLOT_EFF, SLOT_BATTERY);
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
            stack = items.insertItem(slot, stack, false);
            if (stack.isEmpty()) break;
        }
    }
}
