package net.aquatech.machines.block.entity;

import net.aquatech.machines.registry.ModBlockEntities;
import net.aquatech.machines.registry.ModItems;
import net.aquatech.machines.util.FisherLoot;
import net.aquatech.machines.util.RateMultiplierReader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

/**
 * Рыболов MK-2: удочка [0], Ядро Рыболова [1], Скорость [2], Энергоэффективность [3], Выход [4].
 * Базовый режим — ресурсы по тиру удочки. С ядром — редкая рыба.
 * Рейт удочки StarCatcher (х2..х64) умножает каждый улов.
 */
public class FisherBlockEntity extends BaseMachineBlockEntity {

    public static final int SLOT_ROD = 0;
    public static final int SLOT_CORE = 1;
    public static final int SLOT_SPEED = 2;
    public static final int SLOT_EFF = 3;
    public static final int SLOT_OUTPUT = 4;
    public static final int SLOT_BATTERY = 5;

    public FisherBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FISHER.get(), pos, state, 6, 200000, 1024, 100, 40, 200000);
        defineSlots(SLOT_OUTPUT, SLOT_OUTPUT, SLOT_SPEED, SLOT_EFF, SLOT_BATTERY);
    }

    @Override
    protected net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id, Inventory inv) {
        return new net.aquatech.machines.inventory.FisherMenu(id, inv, this);
    }

    @Override
    protected int outputSlots() {
        return 1;
    }

    public int rodTier() {
        ItemStack rod = items.getStackInSlot(SLOT_ROD);
        if (rod.isEmpty()) return 0;
        return FisherLoot.tierOf(rod);
    }

    public int activeRate() {
        ItemStack rod = items.getStackInSlot(SLOT_ROD);
        if (rod.isEmpty()) return 1;
        return RateMultiplierReader.read(rod);
    }

    public boolean hasFishCore() {
        return !items.getStackInSlot(SLOT_CORE).isEmpty()
                && items.getStackInSlot(SLOT_CORE).is(ModItems.FISHING_CORE.get());
    }

    /** Fish mode caps the rate at FISH_RATE_CAP so one cast cannot dump 64 fish into the shop. */
    public static final int FISH_RATE_CAP = 4;

    private static int effectiveRate(int rate, boolean fishMode) {
        int r = Math.max(1, rate);
        return fishMode ? Math.min(r, FISH_RATE_CAP) : r;
    }

    @Override
    protected boolean hasWork() {
        if (rodTier() <= 0) return false;
        ItemStack out = items.getStackInSlot(SLOT_OUTPUT);
        return out.isEmpty() || out.getCount() < out.getMaxStackSize();
    }

    @Override
    protected void craftOnce() {
        int tier = rodTier();
        if (tier <= 0 || level == null) return;
        int rate = activeRate();
        boolean fishMode = hasFishCore();
        ItemStack loot = fishMode
                ? FisherLoot.roll(tier, level.getRandom())
                : FisherLoot.rollResources(tier, level.getRandom());
        if (loot == null || loot.isEmpty()) {
            loot = FisherLoot.rollResources(tier, level.getRandom());
            if (loot.isEmpty()) {
                loot = new ItemStack(net.minecraft.world.item.Items.RAW_IRON, 1);
            }
        }

        int totalCount = Math.max(1, loot.getCount() * effectiveRate(rate, fishMode));
        while (totalCount > 0) {
            int toInsert = Math.min(loot.getMaxStackSize(), totalCount);
            ItemStack chunk = loot.copyWithCount(toInsert);
            ItemStack rest = items.insertItem(SLOT_OUTPUT, chunk, false);
            if (!rest.isEmpty()) {
                // Пытаемся сразу протолкнуть в соседние сундуки
                for (Direction side : Direction.values()) {
                    var neighbor = level.getBlockEntity(worldPosition.relative(side));
                    if (neighbor == null) continue;
                    var cap = neighbor.getCapability(ForgeCapabilities.ITEM_HANDLER, side.getOpposite()).orElse(null);
                    if (cap == null) continue;
                    rest = pushAll(cap, rest);
                    if (rest.isEmpty()) break;
                }
                // Если сундуки полны или отсутствуют — дропаем в мир сверху блока
                if (!rest.isEmpty()) {
                    net.minecraft.world.entity.item.ItemEntity drop =
                            new net.minecraft.world.entity.item.ItemEntity(
                                    level,
                                    worldPosition.getX() + 0.5,
                                    worldPosition.getY() + 1.0,
                                    worldPosition.getZ() + 0.5,
                                    rest);
                    level.addFreshEntity(drop);
                }
            }
            totalCount -= toInsert;
        }
    }
}
