package net.aquatech.ui.skyblock;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.network.NetworkHandler;
import net.aquatech.ui.network.packet.S2CSyncLimitersPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class IslandLimiterTracker extends SavedData {

    private static final String DATA_NAME = AquaTechUI.MOD_ID + "_island_limiters";
    private final Map<UUID, Map<String, Integer>> counts = new HashMap<>();

    public static IslandLimiterTracker get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(IslandLimiterTracker::load, IslandLimiterTracker::new, DATA_NAME);
    }

    public int count(UUID owner, String blockId) {
        if (owner == null || blockId == null) {
            return 0;
        }
        Map<String, Integer> row = counts.get(owner);
        if (row == null) {
            return 0;
        }
        return Math.max(0, row.getOrDefault(blockId, 0));
    }

    public boolean canPlace(UUID owner, String blockId) {
        return count(owner, blockId) < IslandLimiterRules.max(blockId);
    }

    public void increment(UUID owner, String blockId) {
        if (owner == null || !IslandLimiterRules.isLimited(blockId)) {
            return;
        }
        Map<String, Integer> row = counts.computeIfAbsent(owner, k -> new HashMap<>());
        row.put(blockId, count(owner, blockId) + 1);
        setDirty();
    }

    public void decrement(UUID owner, String blockId) {
        if (owner == null || !IslandLimiterRules.isLimited(blockId)) {
            return;
        }
        Map<String, Integer> row = counts.get(owner);
        if (row == null) {
            return;
        }
        int next = Math.max(0, count(owner, blockId) - 1);
        if (next == 0) {
            row.remove(blockId);
            if (row.isEmpty()) {
                counts.remove(owner);
            }
        } else {
            row.put(blockId, next);
        }
        setDirty();
    }

    public Map<String, Integer> placedSnapshot(UUID owner) {
        Map<String, Integer> out = new HashMap<>();
        for (String id : IslandLimiterRules.allMax().keySet()) {
            out.put(id, count(owner, id));
        }
        return out;
    }

    public static Map<String, Integer> maxSnapshot() {
        return new HashMap<>(IslandLimiterRules.allMax());
    }

    public void syncTo(ServerPlayer player) {
        if (player == null) {
            return;
        }
        syncTo(player, player.getUUID());
    }

    public void syncTo(ServerPlayer viewer, UUID owner) {
        if (viewer == null || owner == null) {
            return;
        }
        NetworkHandler.sendToPlayerWhenReady(
                new S2CSyncLimitersPacket(placedSnapshot(owner), maxSnapshot()),
                viewer
        );
    }

    public static IslandLimiterTracker load(CompoundTag tag) {
        IslandLimiterTracker data = new IslandLimiterTracker();
        CompoundTag owners = tag.getCompound("owners");
        for (String uuidStr : owners.getAllKeys()) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                CompoundTag rowTag = owners.getCompound(uuidStr);
                Map<String, Integer> row = new HashMap<>();
                for (String id : rowTag.getAllKeys()) {
                    if (IslandLimiterRules.isLimited(id)) {
                        row.put(id, Math.max(0, rowTag.getInt(id)));
                    }
                }
                if (!row.isEmpty()) {
                    data.counts.put(uuid, row);
                }
            } catch (IllegalArgumentException ignored) {
                // skip corrupt uuid keys
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag owners = new CompoundTag();
        for (Map.Entry<UUID, Map<String, Integer>> e : counts.entrySet()) {
            CompoundTag rowTag = new CompoundTag();
            for (Map.Entry<String, Integer> row : e.getValue().entrySet()) {
                if (row.getValue() > 0) {
                    rowTag.putInt(row.getKey(), row.getValue());
                }
            }
            if (!rowTag.isEmpty()) {
                owners.put(e.getKey().toString(), rowTag);
            }
        }
        tag.put("owners", owners);
        return tag;
    }
}
