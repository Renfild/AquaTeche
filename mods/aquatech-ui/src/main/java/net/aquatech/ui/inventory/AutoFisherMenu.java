package net.aquatech.ui.inventory;

import net.aquatech.ui.block.entity.AutoFisherBlockEntity;
import net.aquatech.ui.fishing.FishingRodCompat;
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
 * auto_fisher.png wells (outer TL → slot +1,+1):
 * rod (15,28), upgrade (15,47), out 3×2 from (100,29) pitch 20×17.
 * Arrow progress outline TL (79,40), BR (93,50).
 */
public class AutoFisherMenu extends AbstractContainerMenu {

    public static final int OUT_ORIGIN_X = 101;
    public static final int OUT_ORIGIN_Y = 30;
    public static final int OUT_CELL_W = 20;
    public static final int OUT_CELL_H = 17;
    public static final int OUT_COLS = 3;
    public static final int OUT_ROWS = 2;

    /** Full width of the atlas arrow fill strip. */
    public static final int PROGRESS_WIDTH = 22;

    public final AutoFisherBlockEntity blockEntity;
    private final ContainerData data;

    public AutoFisherMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(6));
    }

    public AutoFisherMenu(int containerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.AUTO_FISHER_MENU.get(), containerId);
        checkContainerSize(inv, AutoFisherBlockEntity.SLOT_COUNT);
        this.blockEntity = (AutoFisherBlockEntity) entity;
        this.data = data;

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            this.addSlot(new SlotItemHandler(handler, 0, 16, 29) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return FishingRodCompat.isSupportedRod(stack);
                }
            });

            for (int row = 0; row < OUT_ROWS; row++) {
                for (int col = 0; col < OUT_COLS; col++) {
                    int slotIndex = 1 + row * OUT_COLS + col;
                    int x = OUT_ORIGIN_X + col * OUT_CELL_W;
                    int y = OUT_ORIGIN_Y + row * OUT_CELL_H;
                    this.addSlot(new SlotItemHandler(handler, slotIndex, x, y) {
                        @Override
                        public boolean mayPlace(ItemStack stack) {
                            return false;
                        }
                    });
                }
            }

            this.addSlot(new SlotItemHandler(handler, AutoFisherBlockEntity.UPGRADE_SLOT, 16, 48) {
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
        return maxProgress != 0 && progress != 0 ? progress * PROGRESS_WIDTH / maxProgress : 0;
    }

    public int getScaledEnergy() {
        int energy = getEnergy();
        int maxEnergy = getMaxEnergy();
        return maxEnergy != 0 && energy != 0 ? energy * 49 / maxEnergy : 0;
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

            int machineSlots = AutoFisherBlockEntity.SLOT_COUNT;
            if (index < machineSlots) {
                if (!this.moveItemStackTo(stackInSlot, machineSlots, machineSlots + 36, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (FishingRodCompat.isSupportedRod(stackInSlot)) {
                if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) return ItemStack.EMPTY;
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
