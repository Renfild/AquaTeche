package net.aquatech.ui.player;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.server.PressureBridge;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Запрет строительства глубже 50 блоков от уровня океана (Y &lt; 140 при sea = 190).
 */
@Mod.EventBusSubscriber(modid = AquaTechUI.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DeepBuildGuard {

    private DeepBuildGuard() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!PressureBridge.isBelowBuildLimit(event.getPos().getY())) return;

        Entity placer = event.getEntity();
        if (placer instanceof ServerPlayer player && canBypass(player)) {
            return;
        }

        event.setCanceled(true);
        if (placer instanceof ServerPlayer player) {
            player.displayClientMessage(Component.literal(
                    "§c⚓ Слишком глубоко! Строить можно не ниже §fY "
                            + PressureBridge.MIN_BUILD_Y
                            + " §c(§f"
                            + PressureBridge.MAX_BUILD_DEPTH_BELOW_SEA
                            + " м §cот уровня моря Y "
                            + PressureBridge.SEA_LEVEL_Y + ")."), true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onMultiPlace(BlockEvent.EntityMultiPlaceEvent event) {
        if (event.getLevel().isClientSide()) return;
        boolean tooDeep = event.getReplacedBlockSnapshots().stream()
                .anyMatch(s -> PressureBridge.isBelowBuildLimit(s.getPos().getY()));
        if (!tooDeep) return;

        Entity placer = event.getEntity();
        if (placer instanceof ServerPlayer player && canBypass(player)) {
            return;
        }

        event.setCanceled(true);
        if (placer instanceof ServerPlayer player) {
            player.displayClientMessage(Component.literal(
                    "§c⚓ Слишком глубоко для постройки (ниже Y " + PressureBridge.MIN_BUILD_Y + ")."), true);
        }
    }

    private static boolean canBypass(ServerPlayer player) {
        // OP / creative — можно; обычным игрокам нельзя
        return player.isCreative() || player.hasPermissions(2);
    }
}
