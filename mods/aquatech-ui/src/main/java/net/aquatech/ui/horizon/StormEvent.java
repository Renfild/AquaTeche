package net.aquatech.ui.horizon;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.common.ModConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

/**
 * Weekend storm: AUTO Fri–Sun (Europe/Moscow by default), with FORCE overrides.
 */
@Mod.EventBusSubscriber(modid = AquaTechUI.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class StormEvent {

    public enum Mode {
        AUTO, FORCE_ON, FORCE_OFF
    }

    private static Mode mode = Mode.AUTO;
    private static boolean active;
    private static boolean loaded;
    private static int tickCounter;

    private StormEvent() {
    }

    public static boolean isActive() {
        return active;
    }

    public static Mode getMode() {
        return mode;
    }

    /** Manual override until /aquatech storm auto. */
    public static void setForce(boolean on) {
        mode = on ? Mode.FORCE_ON : Mode.FORCE_OFF;
        applyAndAnnounce(ServerLifecycleHooks.getCurrentServer(), true);
        save(ServerLifecycleHooks.getCurrentServer());
    }

    public static void setAuto() {
        mode = Mode.AUTO;
        applyAndAnnounce(ServerLifecycleHooks.getCurrentServer(), true);
        save(ServerLifecycleHooks.getCurrentServer());
    }

    /** @deprecated use {@link #setForce(boolean)} / {@link #setAuto()} */
    @Deprecated
    public static void setActive(boolean value) {
        setForce(value);
    }

    public static boolean scheduleWouldBeActive() {
        if (!ModConfig.AUTO_STORM_ENABLED.get()) {
            return false;
        }
        ZoneId zone = zone();
        DayOfWeek day = ZonedDateTime.now(zone).getDayOfWeek();
        return day == DayOfWeek.FRIDAY || day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    public static String statusLine() {
        String state = active ? "§9АКТИВЕН" : "§8выкл";
        String modeLabel = switch (mode) {
            case AUTO -> "AUTO";
            case FORCE_ON -> "FORCE ON";
            case FORCE_OFF -> "FORCE OFF";
        };
        String next = nextTransitionHint();
        boolean sched = scheduleWouldBeActive();
        return "§bШторм: " + state + " §7· режим §f" + modeLabel
                + " §7· расписание Пт–Вс: " + (sched ? "§aда" : "§8нет")
                + " §8| " + next;
    }

    public static String nextTransitionHint() {
        ZoneId zone = zone();
        ZonedDateTime now = ZonedDateTime.now(zone);
        DayOfWeek day = now.getDayOfWeek();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEE HH:mm");
        if (day == DayOfWeek.FRIDAY || day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            ZonedDateTime monday = now.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).toLocalDate().atStartOfDay(zone);
            return "конец AUTO: " + monday.format(fmt) + " " + zone.getId();
        }
        ZonedDateTime friday = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY)).toLocalDate().atStartOfDay(zone);
        if (!friday.isAfter(now)) {
            friday = now.with(TemporalAdjusters.next(DayOfWeek.FRIDAY)).toLocalDate().atStartOfDay(zone);
        }
        return "старт AUTO: " + friday.format(fmt) + " " + zone.getId();
    }

    private static ZoneId zone() {
        try {
            return ZoneId.of(ModConfig.STORM_TIMEZONE.get());
        } catch (Exception e) {
            return ZoneId.of("Europe/Moscow");
        }
    }

    private static boolean computeDesiredActive() {
        return switch (mode) {
            case FORCE_ON -> true;
            case FORCE_OFF -> false;
            case AUTO -> scheduleWouldBeActive();
        };
    }

    private static void applyAndAnnounce(MinecraftServer server, boolean forceAnnounce) {
        boolean desired = computeDesiredActive();
        boolean changed = desired != active;
        active = desired;
        if (server != null && (changed || forceAnnounce)) {
            Component msg = Component.literal(active
                    ? "§9⚡ Шторм Горизонта НАЧАЛСЯ! §7Редкий улов ×2 (Пт–Вс)."
                    : "§8Шторм Горизонта окончен.");
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                p.displayClientMessage(msg, false);
            }
        }
    }

    private static void save(MinecraftServer server) {
        if (server == null) return;
        ServerLevel overworld = server.overworld();
        if (overworld == null) return;
        StormSavedData data = StormSavedData.get(overworld);
        data.mode = mode;
        data.lastActive = active;
        data.setDirty();
    }

    private static void load(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        if (overworld == null) return;
        StormSavedData data = StormSavedData.get(overworld);
        mode = data.mode;
        active = data.lastActive;
        loaded = true;
        applyAndAnnounce(server, false);
        save(server);
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        load(event.getServer());
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        if (!loaded) {
            load(server);
        }
        if (++tickCounter < 400) return; // ~20s at 20 TPS
        tickCounter = 0;
        boolean before = active;
        Mode beforeMode = mode;
        applyAndAnnounce(server, false);
        if (active != before || mode != beforeMode) {
            save(server);
        } else if (active != computeDesiredActive()) {
            // should not happen; still save
            save(server);
        }
    }

    public static final class StormSavedData extends SavedData {
        private static final String DATA_NAME = AquaTechUI.MOD_ID + "_storm";

        Mode mode = Mode.AUTO;
        boolean lastActive;

        public static StormSavedData get(ServerLevel level) {
            return level.getDataStorage().computeIfAbsent(StormSavedData::load, StormSavedData::new, DATA_NAME);
        }

        public static StormSavedData load(CompoundTag tag) {
            StormSavedData data = new StormSavedData();
            try {
                data.mode = Mode.valueOf(tag.getString("Mode"));
            } catch (Exception e) {
                data.mode = Mode.AUTO;
            }
            data.lastActive = tag.getBoolean("Active");
            return data;
        }

        @Override
        public CompoundTag save(CompoundTag tag) {
            tag.putString("Mode", mode.name());
            tag.putBoolean("Active", lastActive);
            return tag;
        }
    }
}
