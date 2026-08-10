package net.aquatech.ui.inventory;

import net.aquatech.ui.block.entity.OceanFilterBlockEntity;
import net.aquatech.ui.item.UpgradeItem;
import net.aquatech.ui.registry.ModBlocks;
import net.aquatech.ui.registry.ModItems;
import net.aquatech.ui.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

/**
 * Slot layout matches {@code textures/gui/ocean_filter.png}:
 * mesh (27,23), upgrade (27,47), outputs 3×2 at (79,23).
 */
public class OceanFilterMenu extends AbstractContainerMenu {

    public final OceanFilterBlockEntity blockEntity;
    private final ContainerData data;

    public OceanFilterMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(6));
    }

    public OceanFilterMenu(int containerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.OCEAN_FILTER_MENU.get(), containerId);
        checkContainerSize(inv, 11);
        this.blockEntity = (OceanFilterBlockEntity) entity;
        this.data = data;

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            this.addSlot(new SlotItemHandler(handler, 0, 27, 23) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.is(ModItems.MESH_FILTER.get());
                }
            });

            for (int row = 0; row < 2; row++) {
                for (int col = 0; col < 3; col++) {
                    int slotIndex = 1 + row * 3 + col;
                    this.addSlot(new SlotItemHandler(handler, slotIndex, 79 + col * 18, 23 + row * 24) {
                        @Override
                        public boolean mayPlace(ItemStack stack) {
                            return false;
                        }
                    });
                }
            }

            this.addSlot(new SlotItemHandler(handler, OceanFilterBlockEntity.UPGRADE_SLOT, 27, 47) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.getItem() instanceof UpgradeItem;
                }
            });
        });

        addPlayerInventory(inv);
        addPlayerHotbar(inv);
        addDataSlots(data);
    }

    public boolean isCrafting() {
        return data.get(4) > 0;
    }

    public int getScaledProgress() {
        int progress = this.data.get(4);
        int maxProgress = this.data.get(5);
        return maxProgress != 0 && progress != 0 ? progress * 24 / maxProgress : 0;
    }

    public int getScaledEnergy() {
        int energy = getEnergy();
        int maxEnergy = getMaxEnergy();
        return maxEnergy != 0 && energy != 0 ? energy * 52 / maxEnergy : 0;
    }

    public int getEnergy() {
        int low = this.data.get(0) & 0xFFFF;
        int high = this.data.get(1) & 0xFFFF;
        return (high << 16) | low;
    }

    public int getMaxEnergy() {
        int low = this.data.get(2) & 0xFFFF;
        int high = this.data.get(3) & 0xFFFF;
        return (high << 16) | low;
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack sourceStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            sourceStack = stackInSlot.copy();

            // machine: mesh + 6 outs + upgrade = 8 visible slots in menu order 0..7
            if (index < 8) {
                if (!this.moveItemStackTo(stackInSlot, 8, 44, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (stackInSlot.is(ModItems.MESH_FILTER.get())) {
                if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (stackInSlot.getItem() instanceof UpgradeItem) {
                if (!this.moveItemStackTo(stackInSlot, 7, 8, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }

            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stackInSlot.getCount() == sourceStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(playerIn, stackInSlot);
        }

        return sourceStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()), player, ModBlocks.OCEAN_FILTER.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}
