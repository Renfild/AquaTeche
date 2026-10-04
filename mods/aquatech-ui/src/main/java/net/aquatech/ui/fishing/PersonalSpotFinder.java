package net.aquatech.ui.fishing;

import net.aquatech.ui.skyblock.PersonalRaftSpawner;
import net.aquatech.ui.skyblock.WorldGuardIslandLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

import java.util.function.Predicate;

/**
 * Ищет место в открытой воде вокруг плота игрока (точки лова, сокровище из глубин).
 * Раньше центром служила текущая позиция игрока: гость на чужом плоту получал точку у хозяина.
 */
final class PersonalSpotFinder {

    /** Ближе этого расстояния к чужому плоту (от центра, поверх радиуса привата) точек не ставим. */
    private static final int OTHER_RAFT_BUFFER = 80;
    private static final int LOADED_ATTEMPTS = 12;
    /** Попытки с подгрузкой чанков: тяжёлые, поэтому их мало и они идут после дешёвого прохода. */
    private static final int FORCED_ATTEMPTS = 4;

    /** Точка или сундук появляются, только пока игрок не дальше этого от центра своего плота. */
    static final int AT_HOME_BLOCKS = 200;
    /** Если игрок ушёл от плота, через сколько снова проверяем. */
    static final long AWAY_RETRY_MS = 20_000L;

    private PersonalSpotFinder() {
    }

    static boolean withinRange(double dx, double dz, int maxDistance) {
        return dx * dx + dz * dz <= (double) maxDistance * maxDistance;
    }

    /**
     * Игрок рядом со своим плотом (или плота у него нет, тогда ищем от него самого). Без этой проверки гость на
     * чужом плоту получал точку у своего, в километрах отсюда, и не мог до неё доплыть.
     */
    static boolean isNearOwnRaft(ServerLevel level, ServerPlayer owner, int maxDistance) {
        BlockPos raft = PersonalRaftSpawner.raftCenterOf(level, owner.getUUID());
        return raft == null || withinRange(owner.getX() - raft.getX(), owner.getZ() - raft.getZ(), maxDistance);
    }

    /** Вокруг плота кольцо начинается за его приватом, иначе точка попала бы в собственный регион. */
    static int ringMin(boolean aroundRaft, int configMin) {
        return aroundRaft ? Math.max(configMin, PersonalRaftSpawner.CLAIM_RADIUS + 10) : configMin;
    }

    static int ringMax(int ringMin, int configMax) {
        return Math.max(ringMin + 20, configMax);
    }

    /**
     * @param extra дополнительное условие вызывающего (зазор до других точек и т.п.)
     * @return поверхность воды или null, если подходящего места не нашлось
     */
    static BlockPos find(ServerLevel level, ServerPlayer owner, int configMin, int configMax, Predicate<BlockPos> extra) {
        BlockPos raft = PersonalRaftSpawner.raftCenterOf(level, owner.getUUID());
        boolean aroundRaft = raft != null;
        int centerX = aroundRaft ? raft.getX() : owner.getBlockX();
        int centerZ = aroundRaft ? raft.getZ() : owner.getBlockZ();
        int min = ringMin(aroundRaft, configMin);
        int max = ringMax(min, configMax);
        RandomSource random = owner.getRandom();
        for (int pass = 0; pass < 2; pass++) {
            boolean forceLoad = pass == 1;
            int attempts = forceLoad ? FORCED_ATTEMPTS : LOADED_ATTEMPTS;
            for (int attempt = 0; attempt < attempts; attempt++) {
                double angle = random.nextDouble() * Math.PI * 2.0;
                int distance = min + random.nextInt(max - min + 1);
                int x = centerX + (int) Math.round(Math.cos(angle) * distance);
                int z = centerZ + (int) Math.round(Math.sin(angle) * distance);
                if (forceLoad) {
                    preload(level, x, z);
                }
                BlockPos surface = FishingSpotService.waterSurface(level, x, z);
                if (surface == null || !FishingSpotService.isOpenWater(level, surface)) continue;
                if (WorldGuardIslandLookup.ownerAt(level, surface) != null) continue;
                if (PersonalRaftSpawner.isNearAnotherRaft(level, surface, owner.getUUID(), OTHER_RAFT_BUFFER)) continue;
                if (!extra.test(surface)) continue;
                return surface;
            }
        }
        return null;
    }

    /** Чанки вокруг кандидата, которые затрагивает проверка открытой воды. */
    private static void preload(ServerLevel level, int x, int z) {
        int ring = FishingSpotService.OPEN_WATER_RING;
        for (int dx : new int[]{-ring, 0, ring}) {
            for (int dz : new int[]{-ring, 0, ring}) {
                level.getChunk((x + dx) >> 4, (z + dz) >> 4);
            }
        }
    }
}
