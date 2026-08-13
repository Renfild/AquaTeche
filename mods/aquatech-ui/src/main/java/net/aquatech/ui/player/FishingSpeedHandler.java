package net.aquatech.ui.player;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.capability.SkillEffects;
import net.aquatech.ui.fishing.FishingRodCompat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Speeds up FishingHook wait timers when fishing-speed skills are active.
 */
@Mod.EventBusSubscriber(modid = AquaTechUI.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FishingSpeedHandler {

    private static Field TIME_UNTIL_LURED;
    private static Field TIME_UNTIL_HOOKED;
    private static boolean resolved;
    private static boolean failed;

    private static final Map<UUID, Float> BONUS_CACHE = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> BONUS_TICK = new ConcurrentHashMap<>();

    private FishingSpeedHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || failed) return;
        Player player = event.player;
        if (player.level().isClientSide) return;

        FishingHook hook = player.fishing;
        if (hook == null || !hook.isAlive()) {
            UUID id = player.getUUID();
            BONUS_CACHE.remove(id);
            BONUS_TICK.remove(id);
            return;
        }

        ItemStack rod = player.getMainHandItem();
        if (!FishingRodCompat.isSupportedRod(rod)) {
            rod = player.getOffhandItem();
        }
        if (!FishingRodCompat.isSupportedRod(rod)) return;

        float bonus = cachedBonus(player);
        accelerate(hook, bonus);
    }

    private static float cachedBonus(Player player) {
        UUID id = player.getUUID();
        int tick = player.tickCount;
        Integer last = BONUS_TICK.get(id);
        if (last != null && tick - last < 20) {
            Float cached = BONUS_CACHE.get(id);
            if (cached != null) return cached;
        }

        float bonus = 0.12f;
        bonus += SkillEffects.fishingSpeedBonus(player) * 0.75f;
        BONUS_CACHE.put(id, bonus);
        BONUS_TICK.put(id, tick);
        return bonus;
    }

    private static void accelerate(FishingHook hook, float bonus) {
        if (!resolveFields()) return;
        try {
            int lured = TIME_UNTIL_LURED.getInt(hook);
            int hooked = TIME_UNTIL_HOOKED.getInt(hook);
            int extra = Math.max(1, Math.round(bonus * 3.5f));
            if (lured > 0) {
                TIME_UNTIL_LURED.setInt(hook, Math.max(0, lured - extra));
            } else if (hooked > 0) {
                TIME_UNTIL_HOOKED.setInt(hook, Math.max(0, hooked - extra));
            }
        } catch (IllegalAccessException e) {
            failed = true;
        }
    }

    private static boolean resolveFields() {
        if (resolved) return true;
        if (failed) return false;
        String[] luredNames = {"timeUntilLured", "f_37104_", "f_37099_"};
        String[] hookedNames = {"timeUntilHooked", "f_37105_", "f_37100_"};
        for (int i = 0; i < luredNames.length; i++) {
            try {
                Field a = FishingHook.class.getDeclaredField(luredNames[i]);
                Field b = FishingHook.class.getDeclaredField(hookedNames[i]);
                a.setAccessible(true);
                b.setAccessible(true);
                if (a.getType() != int.class || b.getType() != int.class) continue;
                TIME_UNTIL_LURED = a;
                TIME_UNTIL_HOOKED = b;
                resolved = true;
                return true;
            } catch (NoSuchFieldException ignored) {
            }
        }
        failed = true;
        AquaTechUI.LOGGER.warn("FishingSpeedHandler: could not bind FishingHook timer fields");
        return false;
    }
}
