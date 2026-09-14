package net.aquatech.machines.block.entity;

import net.aquatech.machines.compat.BotaniaManaBridge;
import net.aquatech.machines.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Цветолов: «ловит» цветы Botania из воздуха — как рыбак ловит рыбу.
 * Раз в цикл выдаёт случайный цветок из тега botania_flowers, жрёт
 * энергию (FE за тик цикла) и ману (фикс. стоимость за цветок из соседнего мана-пула).
 * Слоты: [0..2] Выход, [3] Скорость, [4] Батарея, [5] Энергоэффективность.
 */
public class FlowerCollectorBlockEntity extends BaseMachineBlockEntity {

    public static final int SLOT_OUTPUT_1 = 0;
    public static final int SLOT_OUTPUT_2 = 1;
    public static final int SLOT_OUTPUT_3 = 2;
    public static final int SLOT_SPEED = 3;
    public static final int SLOT_BATTERY = 4;
    public static final int SLOT_EFF = 5;

    /** Мана за один пойманный цветок — тянется из соседнего мана-пула Botania. */
    public static final int MANA_PER_FLOWER = 250;

    /** Таблица улова: все цветы Botania из тега (блоки -> предметы). */
    public static final TagKey<Block> FLOWER_TAG =
            TagKey.create(Registries.BLOCK, new ResourceLocation("aquatech_machines", "botania_flowers"));

    private static volatile List<Item> catchTable = null;
    private static final RandomSource RANDOM = RandomSource.create();

    public FlowerCollectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLOWER_COLLECTOR.get(), pos, state, 6, 200000, 1024, 160, 60, 200000);
        defineSlots(SLOT_OUTPUT_1, SLOT_OUTPUT_3, SLOT_SPEED, SLOT_EFF, SLOT_BATTERY);
    }

    @Override
    protected net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id, Inventory inv) {
        return new net.aquatech.machines.inventory.FlowerCollectorMenu(id, inv, this);
    }

    @Override
    protected int outputSlots() {
        return 3;
    }

    private static List<Item> catchTable() {
        List<Item> table = catchTable;
        if (table == null) {
            table = BuiltInRegistries.BLOCK.getTag(FLOWER_TAG)
                    .map(holders -> {
                        List<Item> items = new ArrayList<>();
                        for (var h : holders) {
                            Item item = h.value().asItem();
                            if (item != net.minecraft.world.item.Items.AIR) {
                                items.add(item);
                            }
                        }
                        return List.copyOf(items);
                    })
                    .orElse(List.of());
            catchTable = table;
        }
        return table;
    }

    @Nullable
    private ItemStack nextCatch() {
        List<Item> table = catchTable();
        if (table.isEmpty()) return null;
        return new ItemStack(table.get(RANDOM.nextInt(table.size())));
    }

    private boolean outputsHaveSpace() {
        for (int slot = SLOT_OUTPUT_1; slot <= SLOT_OUTPUT_3; slot++) {
            if (items.getStackInSlot(slot).getCount() < items.getSlotLimit(slot)) return true;
        }
        return false;
    }

    @Override
    protected boolean hasWork() {
        return outputsHaveSpace();
    }

    @Override
    protected void craftOnce() {
        if (level == null || level.isClientSide) return;
        // Мана списывается за каждый пойманный цветок: нет маны в соседнем пуле — улова нет.
        if (!BotaniaManaBridge.drainFromNeighbors(level, worldPosition, MANA_PER_FLOWER)) {
            return;
        }
        ItemStack catchStack = nextCatch();
        if (catchStack == null) return;
        for (int slot = SLOT_OUTPUT_1; slot <= SLOT_OUTPUT_3; slot++) {
            if (items.insertItem(slot, catchStack, false).isEmpty()) {
                return;
            }
        }
    }

    /** Мана в соседних пулах (для тултипа GUI). */
    public int neighborMana() {
        return BotaniaManaBridge.neighborMana(level, worldPosition);
    }
}
