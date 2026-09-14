package net.aquatech.machines.inventory;

import net.aquatech.machines.block.entity.BaseMachineBlockEntity;
import net.aquatech.machines.registry.ModItems;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

/**
 * Базовое меню механизма: слоты машины + инвентарь игрока + ContainerData.
 * ContainerData корректно сохраняет sync-пакеты в локальный кеш на клиенте.
 */
public abstract class BaseMachineMenu extends AbstractContainerMenu {

    protected final BaseMachineBlockEntity blockEntity;
    protected final ContainerData data;

    protected BaseMachineMenu(MenuType<?> type, int id, Inventory inv, BlockEntity be, ContainerData data) {
        super(type, id);
        this.blockEntity = (BaseMachineBlockEntity) be;
        this.data = data;
        addMachineSlots(inv);
        addPlayerSlots(inv);
        addDataSlots(data);
    }

    protected abstract void addMachineSlots(Inventory inv);

    protected Slot output(int handlerSlot, int x, int y) {
        return new SlotItemHandler(blockEntity.getItems(), handlerSlot, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        };
    }

    protected Slot speedUpgradeSlot(int handlerSlot, int x, int y) {
        return new SlotItemHandler(blockEntity.getItems(), handlerSlot, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return !stack.isEmpty() && (stack.is(ModItems.SPEED_UPGRADE_1.get())
                        || stack.is(ModItems.SPEED_UPGRADE_4.get())
                        || stack.is(ModItems.SPEED_UPGRADE.get()));
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        };
    }

    protected Slot efficiencyUpgradeSlot(int handlerSlot, int x, int y) {
        return new SlotItemHandler(blockEntity.getItems(), handlerSlot, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return !stack.isEmpty() && stack.is(ModItems.ENERGY_EFFICIENCY.get());
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        };
    }

    protected Slot batterySlot(int handlerSlot, int x, int y) {
        return new SlotItemHandler(blockEntity.getItems(), handlerSlot, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return !stack.isEmpty() && (stack.is(net.minecraft.world.item.Items.REDSTONE)
                        || stack.is(net.minecraft.world.item.Items.REDSTONE_BLOCK)
                        || stack.getCapability(ForgeCapabilities.ENERGY).isPresent()
                        || net.aquatech.machines.compat.energy.IndustrialUpgradeEnergyCompat.isElectricItem(stack));
            }

            @Override
            public int getMaxStackSize() {
                return 64;
            }

            @Override
            public int getMaxStackSize(ItemStack stack) {
                if (stack.is(net.minecraft.world.item.Items.REDSTONE)
                        || stack.is(net.minecraft.world.item.Items.REDSTONE_BLOCK)) {
                    return 64;
                }
                return 1;
            }
        };
    }

    private void addPlayerSlots(Inventory inv) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, 8 + col * 18, 142));
        }
    }

    public int getProgress() {
        return data.get(0);
    }

    public int getMaxProgress() {
        return data.get(1);
    }

    public int getScaledProgress(int width) {
        int max = getMaxProgress();
        return max <= 0 ? 0 : getProgress() * width / max;
    }

    public int getEnergy() {
        return ((data.get(3) & 0xFFFF) << 16) | (data.get(2) & 0xFFFF);
    }

    public int getMaxEnergy() {
        return ((data.get(5) & 0xFFFF) << 16) | (data.get(4) & 0xFFFF);
    }

    public BaseMachineBlockEntity getBlockEntity() {
        return blockEntity;
    }

    /**
     * Хранитель ContainerData:
     * - на сервере считывает значения из BlockEntity;
     * - на клиенте сохраняет присланные пакеты в локальный кеш!
     */
    public static ContainerData makeData(BaseMachineBlockEntity be) {
        return new ContainerData() {
            private final int[] cache = new int[6];

            @Override
            public int get(int index) {
                if (be != null && be.getLevel() != null && !be.getLevel().isClientSide) {
                    return switch (index) {
                        case 0 -> be.getProgressValue();
                        case 1 -> be.getMaxProgressValue();
                        case 2 -> be.getEnergy() & 0xFFFF;
                        case 3 -> (be.getEnergy() >>> 16) & 0xFFFF;
                        case 4 -> be.getMaxEnergy() & 0xFFFF;
                        case 5 -> (be.getMaxEnergy() >>> 16) & 0xFFFF;
                        default -> 0;
                    };
                }
                return (index >= 0 && index < cache.length) ? cache[index] : 0;
            }

            @Override
            public void set(int index, int value) {
                if (index >= 0 && index < cache.length) {
                    cache[index] = value;
                }
            }

            @Override
            public int getCount() {
                return 6;
            }
        };
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        int machineSlots = blockEntity.getItems().getSlots();

        if (index < machineSlots) {
            // Из машины -> в инвентарь игрока
            if (!moveItemStackTo(stack, machineSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // Из инвентаря игрока -> в подходящий слот машины
            boolean moved = false;
            for (int s = 0; s < machineSlots; s++) {
                Slot machineSlot = slots.get(s);
                if (machineSlot.mayPlace(stack)) {
                    if (machineSlot.getMaxStackSize(stack) == 1) {
                        if (!machineSlot.hasItem()) {
                            ItemStack single = stack.split(1);
                            machineSlot.set(single);
                            moved = true;
                            break;
                        }
                    } else {
                        if (moveItemStackTo(stack, s, s + 1, false)) {
                            moved = true;
                            break;
                        }
                    }
                }
            }
            if (!moved) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                player, blockEntity.getBlockState().getBlock());
    }
}
