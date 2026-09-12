package net.aquatech.machines.inventory;

import net.aquatech.machines.block.entity.BaseMachineBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

/**
 * Базовое меню механизма: слоты машины + инвентарь игрока + ContainerData
 * (progress, maxProgress, energy Lo/Hi, maxEnergy Lo/Hi).
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

    /** Слоты машины — координаты должны совпадать с GUI-текстурой (бокс-в-бокс). */
    protected abstract void addMachineSlots(Inventory inv);


    protected Slot output(int handlerSlot, int x, int y) {
        return new SlotItemHandler(blockEntity.getItems(), handlerSlot, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
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
        return getMaxProgress() == 0 ? 0 : getProgress() * width / getMaxProgress();
    }

    public int getEnergy() {
        return (data.get(3) << 16) | (data.get(2) & 0xFFFF);
    }

    public int getMaxEnergy() {
        return (data.get(5) << 16) | (data.get(4) & 0xFFFF);
    }

    /** Хранитель ContainerData: читает напрямую из BE. */
    public static ContainerData makeData(BaseMachineBlockEntity be) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> be.getProgressValue();
                    case 1 -> be.getMaxProgressValue();
                    case 2 -> be.getEnergy() & 0xFFFF;
                    case 3 -> (be.getEnergy() >> 16) & 0xFFFF;
                    case 4 -> be.getMaxEnergy() & 0xFFFF;
                    case 5 -> (be.getMaxEnergy() >> 16) & 0xFFFF;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
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
            if (!moveItemStackTo(stack, machineSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!moveItemStackTo(stack, 0, machineSlots, false)) {
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
