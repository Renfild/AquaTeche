package net.aquatech.ui.fishing;

import net.aquatech.ui.AquaTechUI;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Приманки из рыбного сырья. Заряжаются ПКМ с удочкой во второй руке,
 * тратятся на ручной улов и влияют только на ручную ловлю (Авторыболов их не жжёт).
 * Заряд живёт в NBT удочки: AquaBait = { id:"bait_shoal", uses:int }.
 */
@Mod.EventBusSubscriber(modid = AquaTechUI.MOD_ID)
public final class FishingBait {

    public static final String TAG = "AquaBait";
    public static final int CHARGES_PER_ITEM = 16;
    public static final int MAX_CHARGES = 64;

    public enum Kind {
        SHOAL("bait_shoal", "Стайная приманка", "§bДублирует часть стаи: шанс второго улова"),
        ORE("bait_ore", "Рудная приманка", "§6Тянет руду из донного слоя: шанс бонусного лута"),
        ABYSS("bait_abyss", "Глубинная приманка", "§5Глубинный зов: шанс рыбы следующего тира");

        public final String itemId;
        public final String label;
        public final String effect;

        Kind(String itemId, String label, String effect) {
            this.itemId = itemId;
            this.label = label;
            this.effect = effect;
        }

        public static Kind byId(String id) {
            if (id == null) return null;
            for (Kind kind : values()) {
                if (kind.itemId.equals(id)) return kind;
            }
            return null;
        }
    }

    private FishingBait() {
    }

    public static Kind kindOf(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        var key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (key == null || !AquaTechUI.MOD_ID.equals(key.getNamespace())) return null;
        return Kind.byId(key.getPath());
    }

    /** Активная приманка на удочке или null. */
    public static Kind active(ItemStack rodStack) {
        if (rodStack == null || rodStack.isEmpty() || !rodStack.hasTag()) return null;
        CompoundTag tag = rodStack.getTag();
        if (tag == null || !tag.contains(TAG, Tag.TAG_COMPOUND)) return null;
        return Kind.byId(tag.getCompound(TAG).getString("id"));
    }

    public static int uses(ItemStack rodStack) {
        if (rodStack == null || rodStack.isEmpty() || !rodStack.hasTag()) return 0;
        CompoundTag tag = rodStack.getTag();
        if (tag == null || !tag.contains(TAG, Tag.TAG_COMPOUND)) return 0;
        return Math.max(0, tag.getCompound(TAG).getInt("uses"));
    }

    /** Заряжает удочку приманкой из руки. Возвращает true, если заряд добавлен. */
    public static boolean load(ServerPlayer player, ItemStack rodStack, ItemStack baitStack) {
        Kind kind = kindOf(baitStack);
        if (kind == null || !FishingRodCompat.isSupportedRod(rodStack)) return false;
        if (FishingRodCompat.isFishOnlyRod(rodStack)) {
            player.sendSystemMessage(Component.literal("§b[Приманка] §7Эта удочка ловит только рыбу StarCatcher — приманка не нужна."));
            return false;
        }
        Kind current = active(rodStack);
        int currentUses = current == kind ? uses(rodStack) : 0;
        if (MAX_CHARGES - currentUses < CHARGES_PER_ITEM) {
            player.sendSystemMessage(Component.literal("§b[Приманка] §7Удочка уже заряжена: §f"
                    + (current != null ? current.label : "—") + " §7(" + currentUses + "/" + MAX_CHARGES + ")"));
            return false;
        }
        CompoundTag tag = rodStack.getOrCreateTag();
        CompoundTag bait = new CompoundTag();
        bait.putString("id", kind.itemId);
        bait.putInt("uses", currentUses + CHARGES_PER_ITEM);
        tag.put(TAG, bait);
        baitStack.shrink(1);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.PLAYERS, 0.7F, 1.2F);
        player.sendSystemMessage(Component.literal("§b[Приманка] §a" + kind.label + " §7заряжена: §f"
                + (currentUses + CHARGES_PER_ITEM) + " §7уловов"));
        return true;
    }

    /** Тратит один заряд после ручного улова. */
    public static void consume(ServerPlayer player, ItemStack rodStack) {
        Kind kind = active(rodStack);
        if (kind == null) return;
        CompoundTag tag = rodStack.getOrCreateTag();
        CompoundTag bait = tag.getCompound(TAG);
        int left = bait.getInt("uses") - 1;
        if (left <= 0) {
            tag.remove(TAG);
            player.sendSystemMessage(Component.literal("§b[Приманка] §7" + kind.label + " закончилась."));
            return;
        }
        bait.putInt("uses", left);
        tag.put(TAG, bait);
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack main = player.getMainHandItem();
        if (kindOf(main) == null) return;
        ItemStack rod = player.getOffhandItem();
        if (load(player, rod, main)) {
            event.setCanceled(true);
            player.swing(InteractionHand.MAIN_HAND, true);
        }
    }

    /** Строка эффекта для тултипа предмета-приманки. */
    public static Component effectLine(Kind kind) {
        return Component.literal(ChatFormatting.GRAY + kind.effect);
    }
}
