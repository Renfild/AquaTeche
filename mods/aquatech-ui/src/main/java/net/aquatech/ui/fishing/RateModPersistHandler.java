package net.aquatech.ui.fishing;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Restores pinned rate mods after StarCatcher eats bait on miss (no durability charge).
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

        protect(player.getMainHandItem());
        protect(player.getOffhandItem());
    }

    private static void protect(ItemStack stack) {
        if (stack.isEmpty()) return;
        if (!FishingRodCompat.isSupportedRod(stack)) return;
        StarCatcherAttachments.ensureRatePersists(stack, false);
    }
}
