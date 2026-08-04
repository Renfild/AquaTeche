package net.aquatech.ui.capability;

import net.aquatech.ui.horizon.HorizonRoute;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

import java.util.HashSet;
import java.util.Set;

public class AquaSkillCapability {

    public static final Capability<AquaSkillCapability> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {});

    private int aquaXp = 0;
    private int skillPoints = 0;
    private final Set<String> unlockedSkills = new HashSet<>();
    private boolean starterKitReceived = false;

    // ── Horizon Route ────────────────────────────────────────────────────────
    private int horizonTier = 0;
    private int seasonXp = 0;
    private long dailyDayKey = -1L;
    private int dailyType = 0;
    private int dailyProgress = 0;
    private boolean dailyClaimed = false;

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(AquaSkillCapability.class);
    }

    public int getAquaXp() { return aquaXp; }
    public int getSkillPoints() { return skillPoints; }
    public void setSkillPoints(int points) { this.skillPoints = Math.max(0, points); }
    public Set<String> getUnlockedSkills() { return unlockedSkills; }
    public boolean hasSkill(String skillId) { return unlockedSkills.contains(skillId); }

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

    public boolean addXp(int amount) {
        int oldLevel = getLevel();
        this.aquaXp += amount;
        int newLevel = getLevel();
        if (newLevel > oldLevel) {
            this.skillPoints += (newLevel - oldLevel);
            return true;
        }
        return false;
    }

    public boolean unlockSkill(String skillId) {
        int cost = SkillDefinitions.costOf(skillId);
        if (cost <= 0 || unlockedSkills.contains(skillId)) return false;
        if (skillPoints >= cost) {
            skillPoints -= cost;
            unlockedSkills.add(skillId);
            return true;
        }
        return false;
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
        tag.putInt("SkillPoints", skillPoints);
        tag.putBoolean("StarterKitReceived", starterKitReceived);
        ListTag list = new ListTag();
        for (String skill : unlockedSkills) {
            list.add(StringTag.valueOf(skill));
        }
        tag.put("UnlockedSkills", list);
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
        this.skillPoints = tag.getInt("SkillPoints");
        this.starterKitReceived = tag.getBoolean("StarterKitReceived");
        this.unlockedSkills.clear();
        if (tag.contains("UnlockedSkills", Tag.TAG_LIST)) {
            ListTag list = tag.getList("UnlockedSkills", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                unlockedSkills.add(list.getString(i));
            }
        }
        this.horizonTier = tag.getInt("HorizonTier");
        this.seasonXp = tag.getInt("SeasonXp");
        this.dailyDayKey = tag.contains("DailyDayKey") ? tag.getLong("DailyDayKey") : -1L;
        this.dailyType = tag.getInt("DailyType");
        this.dailyProgress = tag.getInt("DailyProgress");
        this.dailyClaimed = tag.getBoolean("DailyClaimed");
    }

    public void copyFrom(AquaSkillCapability other) {
        this.aquaXp = other.aquaXp;
        this.skillPoints = other.skillPoints;
        this.starterKitReceived = other.starterKitReceived;
        this.unlockedSkills.clear();
        this.unlockedSkills.addAll(other.unlockedSkills);
        this.horizonTier = other.horizonTier;
        this.seasonXp = other.seasonXp;
        this.dailyDayKey = other.dailyDayKey;
        this.dailyType = other.dailyType;
        this.dailyProgress = other.dailyProgress;
        this.dailyClaimed = other.dailyClaimed;
    }
}
