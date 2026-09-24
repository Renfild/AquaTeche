package net.aquatech.ui.capability;

import net.aquatech.ui.horizon.HorizonRoute;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public class OceanProgressCapability {

    public static final Capability<OceanProgressCapability> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {});

    private int aquaXp = 0;
    private boolean starterKitReceived = false;

    // ── Horizon Route ────────────────────────────────────────────────────────
    private int horizonTier = 0;
    private int seasonXp = 0;
    private long dailyDayKey = -1L;
    private int dailyType = 0;
    private int dailyProgress = 0;
    private boolean dailyClaimed = false;

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(OceanProgressCapability.class);
    }

    public int getAquaXp() { return aquaXp; }

    public int getLevel() {
        return (int) Math.floor(Math.sqrt(aquaXp / 250.0)) + 1;
    }

    public int getXpForCurrentLevel() {
        int level = getLevel() - 1;
        return level * level * 250;
    }

    public int getXpForNextLevel() {
        int level = getLevel();
        return level * level * 250;
    }

    public boolean isStarterKitReceived() { return starterKitReceived; }
    public void markStarterKitReceived() { this.starterKitReceived = true; }

    /** @return true, если выданный опыт поднял уровень океана. */
    public boolean addXp(int amount) {
        int oldLevel = getLevel();
        this.aquaXp = Math.max(0, this.aquaXp + amount);
        return getLevel() > oldLevel;
    }

    // ── Horizon getters / setters ────────────────────────────────────────────
    public int getHorizonTier() { return horizonTier; }

    public boolean setHorizonTier(int tier) {
        int clamped = Math.max(0, Math.min(HorizonRoute.MAX_TIER, tier));
        if (clamped <= horizonTier) return false;
        horizonTier = clamped;
        return true;
    }

    public void forceHorizonTier(int tier) {
        horizonTier = Math.max(0, Math.min(HorizonRoute.MAX_TIER, tier));
    }

    public int getSeasonXp() { return seasonXp; }

    public int getSeasonLevel() {
        return Math.min(HorizonRoute.SEASON_MAX_LEVEL, seasonXp / HorizonRoute.SEASON_XP_PER_LEVEL);
    }

    public void addSeasonXp(int amount) {
        seasonXp = Math.max(0, seasonXp + amount);
    }

    public long getDailyDayKey() { return dailyDayKey; }
    public int getDailyType() { return dailyType; }
    public int getDailyProgress() { return dailyProgress; }
    public boolean isDailyClaimed() { return dailyClaimed; }

    public HorizonRoute.DailyContract currentContract() {
        HorizonRoute.DailyContract[] all = HorizonRoute.DailyContract.values();
        int idx = Math.floorMod(dailyType, all.length);
        return all[idx];
    }

    /** Ensure today's contract exists; roll a new one on a new day. */
    public void ensureDaily(long dayKey) {
        if (dailyDayKey == dayKey) return;
        dailyDayKey = dayKey;
        dailyType = (int) Math.floorMod(dayKey, HorizonRoute.DailyContract.values().length);
        // slight shuffle by xp so not everyone has the same contract forever
        dailyType = Math.floorMod(dailyType + (aquaXp % 3), HorizonRoute.DailyContract.values().length);
        dailyProgress = 0;
        dailyClaimed = false;
    }

    public void addDailyProgress(int amount) {
        if (dailyClaimed) return;
        HorizonRoute.DailyContract c = currentContract();
        dailyProgress = Math.min(c.target, dailyProgress + amount);
    }

    public boolean isDailyComplete() {
        return dailyProgress >= currentContract().target;
    }

    public boolean claimDaily() {
        if (dailyClaimed || !isDailyComplete()) return false;
        dailyClaimed = true;
        addSeasonXp(HorizonRoute.DAILY_SEASON_XP);
        addXp(HorizonRoute.DAILY_AQUA_XP);
        return true;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("AquaXp", aquaXp);
        tag.putBoolean("StarterKitReceived", starterKitReceived);
        tag.putInt("HorizonTier", horizonTier);
        tag.putInt("SeasonXp", seasonXp);
        tag.putLong("DailyDayKey", dailyDayKey);
        tag.putInt("DailyType", dailyType);
        tag.putInt("DailyProgress", dailyProgress);
        tag.putBoolean("DailyClaimed", dailyClaimed);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        this.aquaXp = tag.getInt("AquaXp");
        this.starterKitReceived = tag.getBoolean("StarterKitReceived");
        this.horizonTier = tag.getInt("HorizonTier");
        this.seasonXp = tag.getInt("SeasonXp");
        this.dailyDayKey = tag.contains("DailyDayKey") ? tag.getLong("DailyDayKey") : -1L;
        this.dailyType = tag.getInt("DailyType");
        this.dailyProgress = tag.getInt("DailyProgress");
        this.dailyClaimed = tag.getBoolean("DailyClaimed");
    }

    public void copyFrom(OceanProgressCapability other) {
        this.aquaXp = other.aquaXp;
        this.starterKitReceived = other.starterKitReceived;
        this.horizonTier = other.horizonTier;
        this.seasonXp = other.seasonXp;
        this.dailyDayKey = other.dailyDayKey;
        this.dailyType = other.dailyType;
        this.dailyProgress = other.dailyProgress;
        this.dailyClaimed = other.dailyClaimed;
    }
}
