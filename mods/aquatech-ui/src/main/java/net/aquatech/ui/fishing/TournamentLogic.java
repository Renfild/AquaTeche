package net.aquatech.ui.fishing;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Чистая логика недельного турнира: без Minecraft-зависимостей, чтобы её можно было тестировать. */
public final class TournamentLogic {

    public static final int TOP_LIMIT = 10;
    public static final long[] PRIZE_COINS = {2500, 1000, 500};
    public static final String[] PRIZE_CASES = {"flora", "steam", "smeltery"};

    public static final class Entry {
        public String name;
        public String uuid;
        public double weight;
        public String fish;

        public Entry() {
        }

        public Entry(String name, String uuid, double weight, String fish) {
            this.name = name;
            this.uuid = uuid;
            this.weight = weight;
            this.fish = fish;
        }
    }

    public static final class Prize {
        public String uuid;
        public int week;
        public int place;
        public long coins;
        public String caseId;

        public Prize() {
        }

        public Prize(String uuid, int week, int place, long coins, String caseId) {
            this.uuid = uuid;
            this.week = week;
            this.place = place;
            this.coins = coins;
            this.caseId = caseId;
        }
    }

    private TournamentLogic() {
    }

    public static int weekId(LocalDate date) {
        return date.get(IsoFields.WEEK_BASED_YEAR) * 100 + date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
    }

    public static boolean record(List<Entry> top, String uuid, String name, double weight, String fish) {
        if (weight <= 0) {
            return false;
        }
        Entry mine = null;
        for (Entry e : top) {
            if (e.uuid.equals(uuid)) {
                mine = e;
                break;
            }
        }
        if (mine != null) {
            if (weight <= mine.weight) {
                return false;
            }
            mine.name = name;
            mine.weight = weight;
            mine.fish = fish;
        } else {
            if (top.size() >= TOP_LIMIT && weight <= top.get(top.size() - 1).weight) {
                return false;
            }
            top.add(new Entry(name, uuid, weight, fish));
        }
        top.sort(Comparator.comparingDouble((Entry e) -> e.weight).reversed());
        while (top.size() > TOP_LIMIT) {
            top.remove(top.size() - 1);
        }
        return true;
    }

    public static int placeOf(List<Entry> top, String uuid) {
        for (int i = 0; i < top.size(); i++) {
            if (top.get(i).uuid.equals(uuid)) {
                return i + 1;
            }
        }
        return 0;
    }

    /** Бустер скупщика к призу: 1 место получает большой, 2 и 3 места малый. */
    public static String boosterFor(int place) {
        return place == 1 ? "large" : place == 2 || place == 3 ? "small" : null;
    }

    public static List<Prize> prizesFor(List<Entry> top, int week) {
        List<Prize> prizes = new ArrayList<>();
        for (int i = 0; i < Math.min(PRIZE_COINS.length, top.size()); i++) {
            prizes.add(new Prize(top.get(i).uuid, week, i + 1, PRIZE_COINS[i], PRIZE_CASES[i]));
        }
        return prizes;
    }

    public static List<Prize> claimable(List<Prize> pending, String uuid) {
        List<Prize> mine = new ArrayList<>();
        for (Prize p : pending) {
            if (p.uuid.equals(uuid)) {
                mine.add(p);
            }
        }
        return mine;
    }

    public static void removeClaimed(List<Prize> pending, String uuid) {
        pending.removeIf(p -> p.uuid.equals(uuid));
    }

    public static String caseNumeral(String caseId) {
        return switch (caseId) {
            case "flora" -> "IV";
            case "steam" -> "III";
            case "smeltery" -> "II";
            default -> caseId;
        };
    }

    public static long endOfWeekendMs(ZonedDateTime now) {
        return now.toLocalDate().with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                .atStartOfDay(now.getZone()).toInstant().toEpochMilli();
    }

    public static long nextStartMs(ZonedDateTime now) {
        return now.toLocalDate().with(TemporalAdjusters.next(DayOfWeek.SATURDAY))
                .atStartOfDay(now.getZone()).toInstant().toEpochMilli();
    }

    public static String snapshotJson(int week, String status, long endsAtMs, long nextStartMs, List<Entry> top) {
        JsonObject o = new JsonObject();
        o.addProperty("week", week);
        o.addProperty("status", status);
        o.addProperty("ends_at", Instant.ofEpochMilli(endsAtMs).toString());
        o.addProperty("next_starts_at", Instant.ofEpochMilli(nextStartMs).toString());
        JsonArray arr = new JsonArray();
        for (Entry e : top) {
            JsonObject row = new JsonObject();
            row.addProperty("uuid", e.uuid);
            row.addProperty("nick", e.name);
            row.addProperty("weight", e.weight);
            row.addProperty("fish", e.fish == null ? "" : e.fish);
            arr.add(row);
        }
        o.add("top", arr);
        return o.toString();
    }
}
