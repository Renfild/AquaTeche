package net.aquatech.ui.inventory;

import net.aquatech.ui.block.entity.KeepnetBlockEntity;
import net.aquatech.ui.registry.ModBlocks;
import net.aquatech.ui.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

/**
 * Меню садка. Координаты в системе GUI 256x212, как у механизмов; сетку ячеек рисует tools/build_keepnet_gui.py
 * (x = 47 + 18 * столбец, y = 36 + 18 * ряд), инвентарь игрока стоит там же, где у механизмов.
 */
public class KeepnetMenu extends AbstractContainerMenu {

    public static final int COLS = 9;
    public static final int ROWS = 3;
    public static final int GRID_X = 47;
    public static final int GRID_Y = 36;
    public static final int PLAYER_X = 47;
    public static final int PLAYER_Y = 131;
    public static final int HOTBAR_Y = 189;

    private static final int CONTAINER_END = KeepnetBlockEntity.SLOTS;
    private static final int PLAYER_END = CONTAINER_END + 36;

    private final ContainerLevelAccess access;

    /** Клиентская сторона: позиция блока приходит в буфере, содержимое ячеек пришлёт сервер. */
    public KeepnetMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, resolve(playerInventory, extraData));
    }

    public KeepnetMenu(int containerId, Inventory playerInventory, KeepnetBlockEntity keepnet) {
        super(ModMenuTypes.KEEPNET_MENU.get(), containerId);
        this.access = ContainerLevelAccess.create(keepnet.getLevel(), keepnet.getBlockPos());
        ItemStackHandler handler = keepnet.getInventory();
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                addSlot(new SlotItemHandler(handler, col + row * COLS, GRID_X + col * 18, GRID_Y + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, PLAYER_X + col * 18, PLAYER_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, PLAYER_X + col * 18, HOTBAR_Y));
        }
    }

    private static KeepnetBlockEntity resolve(Inventory playerInventory, FriendlyByteBuf extraData) {
        var be = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (be instanceof KeepnetBlockEntity keepnet) {
            return keepnet;
        }
        throw new IllegalStateException("Keepnet block entity is missing at the menu position");
    }

    /** Сколько ячеек садка занято: экран пишет это на табличке. */
    public int filledSlots() {
        int filled = 0;
        for (int i = 0; i < CONTAINER_END; i++) {
            if (!slots.get(i).getItem().isEmpty()) {
                filled++;
            }
        }
        return filled;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack inSlot = slot.getItem();
        ItemStack original = inSlot.copy();
        if (index < CONTAINER_END) {
            if (!moveItemStackTo(inSlot, CONTAINER_END, PLAYER_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!KeepnetBlockEntity.isFish(inSlot) || !moveItemStackTo(inSlot, 0, CONTAINER_END, false)) {
            return ItemStack.EMPTY;
        }
        if (inSlot.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (inSlot.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, inSlot);
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.FISH_KEEPNET.get());
    }
}
