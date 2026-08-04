package net.aquatech.ui.inventory;

import net.aquatech.ui.block.entity.HydroReactorBlockEntity;
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

public class HydroReactorMenu extends AbstractContainerMenu {

    public final HydroReactorBlockEntity blockEntity;
    private final ContainerData data;

    public HydroReactorMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(4));
    }

    public HydroReactorMenu(int containerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.HYDRO_REACTOR_MENU.get(), containerId);
        checkContainerSize(inv, 2);
        this.blockEntity = (HydroReactorBlockEntity) entity;
        this.data = data;

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            this.addSlot(new SlotItemHandler(handler, 0, 80, 35) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.is(ModItems.KELP_BIO_PELLET.get());
                }
            });
            this.addSlot(new SlotItemHandler(handler, HydroReactorBlockEntity.UPGRADE_SLOT, 26, 55) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.getItem() instanceof UpgradeItem up
                            && up.getType() == UpgradeItem.UpgradeType.EFFICIENCY;
                }
            });
        });

        addPlayerInventory(inv);
        addPlayerHotbar(inv);
        addDataSlots(data);
    }

    public boolean isBurning() {
        return data.get(2) > 0;
    }

    public int getScaledBurn() {
        int burn = this.data.get(2);
        int maxBurn = this.data.get(3);
        return maxBurn != 0 && burn != 0 ? burn * 24 / maxBurn : 0;
    }

    public int getScaledEnergy() {
        int energy = this.data.get(0);
        int maxEnergy = this.data.get(1);
        return maxEnergy != 0 && energy != 0 ? energy * 52 / maxEnergy : 0;
    }

    public int getEnergy() {
        return this.data.get(0);
    }

    public int getMaxEnergy() {
        return this.data.get(1);
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack sourceStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            sourceStack = stackInSlot.copy();

            if (index < 2) {
                if (!this.moveItemStackTo(stackInSlot, 2, 38, true)) return ItemStack.EMPTY;
            } else if (stackInSlot.is(ModItems.KELP_BIO_PELLET.get())) {
                if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) return ItemStack.EMPTY;
            } else if (stackInSlot.getItem() instanceof UpgradeItem) {
                if (!this.moveItemStackTo(stackInSlot, 1, 2, false)) return ItemStack.EMPTY;
            } else {
                return ItemStack.EMPTY;
            }

            if (stackInSlot.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
            if (stackInSlot.getCount() == sourceStack.getCount()) return ItemStack.EMPTY;
            slot.onTake(playerIn, stackInSlot);
        }
        return sourceStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()), player, ModBlocks.HYDRO_REACTOR.get());
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
