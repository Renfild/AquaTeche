package net.aquatech.ui.entity;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.event.AquaFishCaughtEvent;
import net.aquatech.ui.fishing.FameService;
import net.aquatech.ui.fishing.FishCatchInfo;
import net.aquatech.ui.fishing.FishWeight;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.List;

/** Связывает Рыбака-соседа с остальной рыбалкой: рекорды стены славы и тяжёлый улов рядом. */
@Mod.EventBusSubscriber(modid = AquaTechUI.MOD_ID)
public final class FishNeighborEvents {

    /** Игрок должен быть не дальше этого от соседа, чтобы тот заметил его улов. */
    private static final double NOTICE_RANGE = 32.0D;

    private static boolean hooked;

    private FishNeighborEvents() {
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        if (!hooked) {
            hooked = true;
            FameService.addListener(FishNeighborEvents::onRecord);
        }
    }

    @SubscribeEvent
    public static void onCatch(AquaFishCaughtEvent event) {
        ServerPlayer player = event.getPlayer();
        for (ItemStack stack : event.getDrops()) {
            FishCatchInfo.Info info = FishCatchInfo.read(stack);
            if (info != null && FishWeight.isHeavy(FishCatchInfo.speciesId(stack), info.grams())) {
                String line = FishNeighborLines.heavyCatch(player.getGameProfile().getName(), player.getRandom().nextInt(3));
                for (FishNeighborEntity neighbor : neighborsNear(player)) {
                    neighbor.comment(line);
                }
                return;
            }
        }
    }

    private static void onRecord(FameService.Change change) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        String line = FishNeighborLines.record(change.outcome(), change.record().name,
                change.previous() == null ? null : change.previous().name,
                change.record().fish, FishWeight.format(change.record().grams));
        for (ServerLevel level : server.getAllLevels()) {
            List<? extends FishNeighborEntity> neighbors = level.getEntities(EntityTypeTest.forClass(FishNeighborEntity.class), Entity::isAlive);
            for (FishNeighborEntity neighbor : neighbors) {
                neighbor.comment(line);
            }
        }
    }

    private static List<? extends FishNeighborEntity> neighborsNear(ServerPlayer player) {
        return player.serverLevel().getEntities(EntityTypeTest.forClass(FishNeighborEntity.class),
                e -> e.isAlive() && e.distanceToSqr(player) <= NOTICE_RANGE * NOTICE_RANGE);
    }
}
