package net.aquatech.ui.fishing;

import net.aquatech.ui.item.RateModItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;

/**
 * Restores pinned rate mods after StarCatcher eats bait on miss/catch.
 * Synchronizes pins on GUI changes (preventing duplication when removing or disappearing when inserting).
 */
@Mod.EventBusSubscriber(modid = "aquatech_ui")
public final class RateModPersistHandler {
    private RateModPersistHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if ((player.tickCount & 15) != 0) return;

        // Never run auto-restore while the player has a container GUI open (prevents GUI desync/race)
        if (player.containerMenu != null && player.containerMenu != player.inventoryMenu) return;

        protect(player.getMainHandItem());
        protect(player.getOffhandItem());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getLevel().isClientSide) return;
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || !FishingRodCompat.isSupportedRod(stack)) return;
        // Ensure rate is pinned immediately before casting so bite/catch never eats it unpinned
        ItemStack liveRate = StarCatcherAttachments.findRateStack(stack);
        if (!liveRate.isEmpty()) {
            StarCatcherAttachments.pin(stack, liveRate);
        }
    }

    @SubscribeEvent
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        if (event.getEntity().level().isClientSide) return;
        AbstractContainerMenu menu = event.getContainer();
        if (menu == null) return;
        String menuClass = menu.getClass().getName();

        if (menuClass.contains("TackleBoxMenu")) {
            menu.addSlotListener(new ContainerListener() {
                @Override
                public void slotChanged(AbstractContainerMenu container, int slotIndex, ItemStack stack) {
                    try {
                        if (slotIndex == 2) { // BAIT_SLOT
                            ItemStack rod = container.getSlot(0).getItem();
                            if (!rod.isEmpty() && FishingRodCompat.isSupportedRod(rod)) {
                                if (stack.getItem() instanceof RateModItem) {
                                    StarCatcherAttachments.pin(rod, stack);
                                } else if (stack.isEmpty()) {
                                    StarCatcherAttachments.unpin(rod);
                                }
                            }
                        } else if (slotIndex == 0) { // ROD_SLOT
                            if (!stack.isEmpty() && FishingRodCompat.isSupportedRod(stack)) {
                                ItemStack bait = container.getSlot(2).getItem();
                                if (bait.getItem() instanceof RateModItem) {
                                    StarCatcherAttachments.pin(stack, bait);
                                } else {
                                    StarCatcherAttachments.syncRodPin(stack);
                                }
                            }
                        }
                    } catch (Throwable ignored) {
                    }
                }

                @Override
                public void dataChanged(AbstractContainerMenu container, int id, int value) {
                }
            });
        }
    }

    @SubscribeEvent
    public static void onContainerClose(PlayerContainerEvent.Close event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        AbstractContainerMenu menu = event.getContainer();
        if (menu == null) return;
        String menuClass = menu.getClass().getName();

        if (menuClass.contains("FishingRodMenu")) {
            try {
                Field isField = menu.getClass().getField("is");
                Object obj = isField.get(menu);
                if (obj instanceof ItemStack rod && !rod.isEmpty() && FishingRodCompat.isSupportedRod(rod)) {
                    StarCatcherAttachments.syncRodPin(rod);
                }
            } catch (Throwable ignored) {
            }
        } else if (menuClass.contains("TackleBoxMenu")) {
            try {
                ItemStack rod = menu.getSlot(0).getItem();
                if (!rod.isEmpty() && FishingRodCompat.isSupportedRod(rod)) {
                    StarCatcherAttachments.syncRodPin(rod);
                }
                // Protect against TackleBox losing attachments if rod was removed:
                for (int s = 1; s <= 3; s++) {
                    ItemStack leftover = menu.getSlot(s).getItem();
                    if (!leftover.isEmpty() && rod.isEmpty()) {
                        if (!player.getInventory().add(leftover)) {
                            player.drop(leftover, false);
                        }
                        menu.getSlot(s).set(ItemStack.EMPTY);
                    }
                }
            } catch (Throwable ignored) {
            }
        }
    }

    private static void protect(ItemStack stack) {
        if (stack.isEmpty()) return;
        if (!FishingRodCompat.isSupportedRod(stack)) return;
        StarCatcherAttachments.ensureRatePersists(stack, false);
    }
}
