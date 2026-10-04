package net.aquatech.machines.inventory;

import net.aquatech.machines.util.MachineLayout;
import net.aquatech.machines.block.entity.FishGeneratorBlockEntity;
import net.aquatech.machines.registry.ModBlocks;
import net.aquatech.machines.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class FishGeneratorMenu extends AbstractContainerMenu {

    private final FishGeneratorBlockEntity blockEntity;
    private final ContainerData data;

    public FishGeneratorMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, inv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public FishGeneratorMenu(int id, Inventory inv, BlockEntity be) {
        super(ModMenuTypes.FISH_GENERATOR.get(), id);
        this.blockEntity = (FishGeneratorBlockEntity) be;
        this.data = makeData(blockEntity);
        addSlot(new SlotItemHandler(blockEntity.getFuel(), FishGeneratorBlockEntity.SLOT_FUEL, MachineLayout.FISH_GENERATOR_FUEL_X, MachineLayout.FISH_GENERATOR_FUEL_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return FishGeneratorBlockEntity.isFuel(stack);
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, MachineLayout.PLAYER_X + col * 18, MachineLayout.PLAYER_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, MachineLayout.PLAYER_X + col * 18, MachineLayout.HOTBAR_Y));
        }
        addDataSlots(data);
    }

    /** Сервер читает значения из блока, клиент хранит присланные пакеты в кеше (как в BaseMachineMenu). */
    private static ContainerData makeData(FishGeneratorBlockEntity be) {
        return new ContainerData() {
            private final int[] cache = new int[8];

            @Override
            public int get(int index) {
                if (be != null && be.getLevel() != null && !be.getLevel().isClientSide) {
                    return switch (index) {
                        case 0 -> be.getBurnTime();
                        case 1 -> be.getBurnTotal();
                        case 2 -> be.getEnergy() & 0xFFFF;
                        case 3 -> (be.getEnergy() >>> 16) & 0xFFFF;
                        case 4 -> be.getMaxEnergy() & 0xFFFF;
                        case 5 -> (be.getMaxEnergy() >>> 16) & 0xFFFF;
                        case 6 -> be.getCurrentRate();
                        case 7 -> be.getLastTransferred();
                        default -> 0;
                    };
                }
                return index >= 0 && index < cache.length ? cache[index] : 0;
            }

            @Override
            public void set(int index, int value) {
                if (index >= 0 && index < cache.length) {
                    cache[index] = value;
                }
            }

            @Override
            public int getCount() {
                return cache.length;
            }
        };
    }

    public int getBurnTime() {
        return data.get(0);
    }

    public int getBurnTotal() {
        return data.get(1);
    }

    /** Выработка прямо сейчас в FE/t: зависит от топлива в огне, без огня ноль. */
    public int getCurrentRate() {
        return data.get(6);
    }

    /** Сколько FE за последний тик ушло авторыболовам рядом. */
    public int getLastTransferred() {
        return data.get(7);
    }

    public int getScaledBurn(int width) {
        int total = data.get(1);
        return total <= 0 ? 0 : data.get(0) * width / total;
    }

    public int getEnergy() {
        return ((data.get(3) & 0xFFFF) << 16) | (data.get(2) & 0xFFFF);
    }

    public int getMaxEnergy() {
        return ((data.get(5) & 0xFFFF) << 16) | (data.get(4) & 0xFFFF);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index == 0) {
            if (!moveItemStackTo(stack, 1, slots.size(), true)) return ItemStack.EMPTY;
        } else if (FishGeneratorBlockEntity.isFuel(stack)) {
            if (!moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
        } else {
            return ItemStack.EMPTY;
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
                player, ModBlocks.FISH_GENERATOR.get());
    }
}
