package net.aquatech.ui.inventory;

import net.aquatech.ui.block.entity.AutoFisherBlockEntity;
import net.aquatech.ui.fishing.FishingRodCompat;
import net.aquatech.ui.item.RateModItem;
import net.aquatech.ui.item.UpgradeItem;
import net.aquatech.ui.registry.ModBlocks;
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
 * Slot layout:
 * <pre>
 *   rod      (14, 33)
 *   rate     (14, 55)  — RateModItem
 *   upgrade  (38, 55)  — speed/efficiency/double
 *   out 2×2  (120,24)/(140,24)/(120,44)/(140,44)
 * </pre>
 */
public class AutoFisherMenu extends AbstractContainerMenu {

    public final AutoFisherBlockEntity blockEntity;
    private final ContainerData data;

    public AutoFisherMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(4));
    }

    public AutoFisherMenu(int containerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.AUTO_FISHER_MENU.get(), containerId);
        checkContainerSize(inv, AutoFisherBlockEntity.SLOT_COUNT);
        this.blockEntity = (AutoFisherBlockEntity) entity;
        this.data = data;

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            this.addSlot(new SlotItemHandler(handler, 0, 14, 33) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    // StarCatcher rods are plain Items, not FishingRodItem
                    return FishingRodCompat.isResourceRod(stack);
                }
            });

            int[][] outs = {
                    {120, 24}, {140, 24},
                    {120, 44}, {140, 44}
            };
            for (int i = 0; i < outs.length; i++) {
                final int slotIndex = 1 + i;
                this.addSlot(new SlotItemHandler(handler, slotIndex, outs[i][0], outs[i][1]) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false;
                    }
                });
            }

            this.addSlot(new SlotItemHandler(handler, AutoFisherBlockEntity.UPGRADE_SLOT, 38, 55) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.getItem() instanceof UpgradeItem;
                }
            });

            this.addSlot(new SlotItemHandler(handler, AutoFisherBlockEntity.RATE_SLOT, 14, 55) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.getItem() instanceof RateModItem;
                }
            });
        });

        addPlayerInventory(inv);
        addPlayerHotbar(inv);
        addDataSlots(data);
    }

    public boolean isCrafting() {
        return data.get(2) > 0;
    }

    public int getScaledProgress() {
        int progress = this.data.get(2);
        int maxProgress = this.data.get(3);
        return maxProgress != 0 && progress != 0 ? progress * 18 / maxProgress : 0;
    }

    public int getScaledEnergy() {
        int energy = this.data.get(0);
        int maxEnergy = this.data.get(1);
        return maxEnergy != 0 && energy != 0 ? energy * 49 / maxEnergy : 0;
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

            int machineSlots = AutoFisherBlockEntity.SLOT_COUNT;
            if (index < machineSlots) {
                if (!this.moveItemStackTo(stackInSlot, machineSlots, machineSlots + 36, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (FishingRodCompat.isResourceRod(stackInSlot)) {
                if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) return ItemStack.EMPTY;
            } else if (stackInSlot.getItem() instanceof RateModItem) {
                if (!this.moveItemStackTo(stackInSlot, AutoFisherBlockEntity.RATE_SLOT, AutoFisherBlockEntity.RATE_SLOT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (stackInSlot.getItem() instanceof UpgradeItem) {
                if (!this.moveItemStackTo(stackInSlot, AutoFisherBlockEntity.UPGRADE_SLOT, AutoFisherBlockEntity.UPGRADE_SLOT + 1, false)) {
                    return ItemStack.EMPTY;
                }
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
        return stillValid(ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()), player, ModBlocks.AUTO_FISHER.get());
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
