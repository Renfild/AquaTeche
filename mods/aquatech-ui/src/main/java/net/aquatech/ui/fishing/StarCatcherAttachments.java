package net.aquatech.ui.fishing;

import net.aquatech.ui.item.RateModItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Supplier;

/**
 * StarCatcher rod attachments via reflection.
 * Rate mods stay in the bait slot; SC shrinks bait each catch, we restore with durability (~10k uses).
 */
public final class StarCatcherAttachments {
    private static final String[] SLOT_FIELDS = {"BAIT", "BOBBER", "HOOK"};
    private static final String PINNED_RATE = "AquaPinnedRate";
    private static final String PINNED_DMG = "AquaPinnedRateDmg";
    private static final int SLOT_BAIT = 0;
    private static final int SLOT_BOBBER = 1;
    private static final int SLOT_HOOK = 2;

    private static boolean probed;
    private static Method getMethod;
    private static Method setMethod;
    private static Constructor<?> containerCtor;
    private static Object emptyContainer;
    private static Object[] holders;

    private StarCatcherAttachments() {
    }

    public static int readRateMultiplier(ItemStack rodStack) {
        ItemStack rate = findRateStack(rodStack);
        if (rate.getItem() instanceof RateModItem mod) {
            return mod.getMultiplier();
        }
        return 1;
    }

    public static ItemStack readBait(ItemStack rodStack) {
        return readSlot(rodStack, SLOT_BAIT);
    }

    public static ItemStack findRateStack(ItemStack rodStack) {
        if (rodStack == null || rodStack.isEmpty()) return ItemStack.EMPTY;
        for (int i = 0; i < SLOT_FIELDS.length; i++) {
            ItemStack attached = readSlot(rodStack, i);
            if (attached.getItem() instanceof RateModItem) {
                return attached;
            }
        }
        return ItemStack.EMPTY;
    }

    public static void ensureRatePersists(ItemStack rodStack) {
        ensureRatePersists(rodStack, false);
    }

    /**
     * @param chargeCatch if true and bait was emptied by SC, restore with +1 damage (one catch use).
     *                    if false, restore at the same damage (miss / tick repair).
     */
    public static void ensureRatePersists(ItemStack rodStack, boolean chargeCatch) {
        if (rodStack == null || rodStack.isEmpty()) return;
        ensureProbed();
        if (getMethod == null || setMethod == null || holders == null) return;

        // Previous build parked rates in bobber — move them back to bait.
        ItemStack bait = readSlot(rodStack, SLOT_BAIT);
        ItemStack bobber = readSlot(rodStack, SLOT_BOBBER);
        if (bobber.getItem() instanceof RateModItem && !(bait.getItem() instanceof RateModItem)) {
            if (bait.isEmpty()) {
                writeSlot(rodStack, SLOT_BAIT, bobber.copy());
                writeSlot(rodStack, SLOT_BOBBER, ItemStack.EMPTY);
                bait = readSlot(rodStack, SLOT_BAIT);
            }
        }

        ItemStack liveRate = findRateStack(rodStack);
        CompoundTag tag = rodStack.getOrCreateTag();

        if (!liveRate.isEmpty()) {
            pin(tag, liveRate);
            // Keep rate in bait if it somehow sits only in hook/bobber.
            if (!(bait.getItem() instanceof RateModItem) && bait.isEmpty()) {
                writeSlot(rodStack, SLOT_BAIT, liveRate.copy());
                if (bobber.getItem() instanceof RateModItem) {
                    writeSlot(rodStack, SLOT_BOBBER, ItemStack.EMPTY);
                } else if (readSlot(rodStack, SLOT_HOOK).getItem() instanceof RateModItem) {
                    writeSlot(rodStack, SLOT_HOOK, ItemStack.EMPTY);
                }
            }
            return;
        }

        String pinned = tag.contains(PINNED_RATE) ? tag.getString(PINNED_RATE) : "";
        if (pinned.isEmpty()) return;
        ResourceLocation id = ResourceLocation.tryParse(pinned);
        if (id == null) return;
        Item item = BuiltInRegistries.ITEM.get(id);
        if (!(item instanceof RateModItem)) return;

        int dmg = tag.contains(PINNED_DMG) ? tag.getInt(PINNED_DMG) : 0;
        if (chargeCatch) {
            dmg++;
        }
        if (dmg >= RateModItem.MAX_CATCHES) {
            tag.remove(PINNED_RATE);
            tag.remove(PINNED_DMG);
            return;
        }

        ItemStack restore = new ItemStack(item);
        restore.setDamageValue(dmg);
        writeSlot(rodStack, SLOT_BAIT, restore);
        pin(tag, restore);
    }

    /**
     * StarCatcher keeps a FISHING_BOB attachment + FishingBobEntity.
     * After AquaTech rewrites loot, leftover attachment / ghost bob leaves a stuck cast line
     * and blocks new casts. Clear both; also drop vanilla {@code player.fishing} from SC's fake hook.
     */
    public static void forceReleaseBobber(ServerPlayer player) {
        if (player == null || player.level().isClientSide) return;
        player.fishing = null;
        try {
            Class<?> attachments = Class.forName("com.wdiscute.starcatcher.io.SCDataAttachments");
            Field bobField = attachments.getField("FISHING_BOB");
            Object bobSupplier = bobField.get(null);
            Method removeSupplier = null;
            try {
                removeSupplier = attachments.getMethod("remove", Entity.class, Supplier.class);
            } catch (NoSuchMethodException ignored) {
            }
            if (removeSupplier != null) {
                removeSupplier.invoke(null, player, bobSupplier);
            } else {
                Method get = attachments.getMethod("get", Entity.class, Supplier.class);
                Object att = get.invoke(null, player, bobSupplier);
                if (att != null) {
                    Method removeType = attachments.getMethod("remove", Entity.class,
                            Class.forName("net.nikdo53.neobackports.io.attachment.AttachmentType"));
                    Object type = ((Supplier<?>) bobSupplier).get();
                    removeType.invoke(null, player, type);
                }
            }
        } catch (Throwable ignored) {
        }

        AABB box = player.getBoundingBox().inflate(48.0D, 80.0D, 48.0D);
        List<Entity> nearby = player.level().getEntities(player, box, e -> {
            String n = e.getClass().getName();
            return n.contains("FishingBobEntity") || n.endsWith(".FishingBobEntity");
        });
        for (Entity bob : nearby) {
            try {
                Field owner = bob.getClass().getField("player");
                Object p = owner.get(bob);
                if (p == player) {
                    bob.discard();
                }
            } catch (Throwable t) {
                bob.discard();
            }
        }
        player.fishing = null;
    }

    /** Direct wear for AutoFisher (no SC bait shrink). */
    public static void consumeRateCatch(ItemStack rodStack) {
        if (rodStack == null || rodStack.isEmpty()) return;
        ItemStack bait = readSlot(rodStack, SLOT_BAIT);
        if (!(bait.getItem() instanceof RateModItem)) {
            ensureRatePersists(rodStack, true);
            return;
        }
        int dmg = bait.getDamageValue() + 1;
        CompoundTag tag = rodStack.getOrCreateTag();
        if (dmg >= RateModItem.MAX_CATCHES) {
            writeSlot(rodStack, SLOT_BAIT, ItemStack.EMPTY);
            tag.remove(PINNED_RATE);
            tag.remove(PINNED_DMG);
            return;
        }
        ItemStack next = bait.copy();
        next.setDamageValue(dmg);
        writeSlot(rodStack, SLOT_BAIT, next);
        pin(tag, next);
    }

    private static void pin(CompoundTag tag, ItemStack rate) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(rate.getItem());
        if (id == null) return;
        tag.putString(PINNED_RATE, id.toString());
        tag.putInt(PINNED_DMG, rate.getDamageValue());
    }

    private static ItemStack readSlot(ItemStack rodStack, int slotIndex) {
        ensureProbed();
        if (getMethod == null || holders == null || holders[slotIndex] == null) return ItemStack.EMPTY;
        try {
            Object container = getMethod.invoke(null, rodStack, holders[slotIndex]);
            return extractStack(container);
        } catch (Throwable t) {
            return ItemStack.EMPTY;
        }
    }

    private static void writeSlot(ItemStack rodStack, int slotIndex, ItemStack stack) {
        ensureProbed();
        if (setMethod == null || holders == null || holders[slotIndex] == null || containerCtor == null) return;
        try {
            Object container = stack.isEmpty() && emptyContainer != null
                    ? emptyContainer
                    : containerCtor.newInstance(stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
            setMethod.invoke(null, rodStack, holders[slotIndex], container);
        } catch (Throwable ignored) {
        }
    }

    private static ItemStack extractStack(Object container) throws Exception {
        if (container == null) return ItemStack.EMPTY;
        Method stack = container.getClass().getMethod("stack");
        Object result = stack.invoke(container);
        return result instanceof ItemStack s ? s : ItemStack.EMPTY;
    }

    private static void ensureProbed() {
        if (probed) return;
        probed = true;
        try {
            Class<?> sc = Class.forName("com.wdiscute.starcatcher.io.SCDataComponents");
            getMethod = sc.getMethod("get", ItemStack.class, Supplier.class);
            setMethod = sc.getMethod("set", ItemStack.class, Supplier.class, Object.class);
            holders = new Object[SLOT_FIELDS.length];
            for (int i = 0; i < SLOT_FIELDS.length; i++) {
                Field f = sc.getField(SLOT_FIELDS[i]);
                holders[i] = f.get(null);
            }
            Class<?> container = Class.forName("com.wdiscute.starcatcher.io.SingleStackContainer");
            containerCtor = container.getConstructor(ItemStack.class);
            try {
                Method empty = container.getMethod("empty");
                emptyContainer = empty.invoke(null);
            } catch (NoSuchMethodException ignored) {
                emptyContainer = containerCtor.newInstance(ItemStack.EMPTY);
            }
        } catch (Throwable t) {
            getMethod = null;
            setMethod = null;
            holders = null;
            containerCtor = null;
            emptyContainer = null;
        }
    }
}
