package net.aquatech.ui.fishing;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TournamentLogicTest {

    private static final String A = "2102d9ae-dcb0-3598-8846-c4e76d4134fd";
    private static final String B = "3b5e083d-8b48-33d6-8f3c-85ab1ebb9b84";

    @Test
    void weekendStaysInOneWeekEvenWhenDayOfYearFormulaWouldSplitIt() {
        // 2024-01-06 (Sat) has dayOfYear 6, 2024-01-07 (Sun) has 7: the old dayOfYear/7 formula split them.
        assertEquals(TournamentLogic.weekId(LocalDate.of(2024, 1, 6)), TournamentLogic.weekId(LocalDate.of(2024, 1, 7)));
        assertEquals(202639, TournamentLogic.weekId(LocalDate.of(2026, 9, 26)));
        assertEquals(202639, TournamentLogic.weekId(LocalDate.of(2026, 9, 27)));
    }

    @Test
    void weekIdUsesTheIsoWeekBasedYearAtTheYearBoundary() {
        assertEquals(202601, TournamentLogic.weekId(LocalDate.of(2025, 12, 31)));
        assertEquals(202601, TournamentLogic.weekId(LocalDate.of(2026, 1, 1)));
    }

    @Test
    void recordInsertsImprovesAndRejects() {
        List<TournamentLogic.Entry> top = new ArrayList<>();
        assertTrue(TournamentLogic.record(top, A, "a", 5.0, "Щука"));
        assertTrue(TournamentLogic.record(top, B, "b", 9.0, "Окунь"));
        assertEquals(List.of(B, A), top.stream().map(e -> e.uuid).toList());
        assertFalse(TournamentLogic.record(top, A, "a", 4.0, "Плотва"), "lower catch must not replace the best");
        assertFalse(TournamentLogic.record(top, A, "a", 5.0, "Плотва"), "equal catch changes nothing");
        assertFalse(TournamentLogic.record(top, A, "a", 0, "x"));
        assertTrue(TournamentLogic.record(top, A, "a2", 11.0, "Тунец"));
        assertEquals(1, TournamentLogic.placeOf(top, A));
        assertEquals("a2", top.get(0).name);
        assertEquals("Тунец", top.get(0).fish);
        assertEquals(0, TournamentLogic.placeOf(top, "nobody"));
    }

    @Test
    void recordCapsTheBoardAtTenAndEvictsTheLightest() {
        List<TournamentLogic.Entry> top = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            assertTrue(TournamentLogic.record(top, "u" + i, "n" + i, i, "f"));
        }
        assertEquals(10, top.size());
        assertFalse(TournamentLogic.record(top, "late", "late", 1.0, "f"), "not heavier than the 10th");
        assertFalse(TournamentLogic.record(top, "tie", "tie", 1.0, "f"), "tie with the 10th does not displace it");
        assertTrue(TournamentLogic.record(top, "big", "big", 50.0, "f"));
        assertEquals(10, top.size());
        assertEquals("big", top.get(0).uuid);
        assertEquals(0, TournamentLogic.placeOf(top, "u1"), "lightest entry is evicted");
    }

    @Test
    void prizesGoToTheTopThreeWithCoinsAndKeys() {
        List<TournamentLogic.Entry> top = new ArrayList<>();
        TournamentLogic.record(top, A, "a", 9, "f");
        TournamentLogic.record(top, B, "b", 7, "f");
        List<TournamentLogic.Prize> prizes = TournamentLogic.prizesFor(top, 202639);
        assertEquals(2, prizes.size());
        assertEquals(2500L, prizes.get(0).coins);
        assertEquals("flora", prizes.get(0).caseId);
        assertEquals(1000L, prizes.get(1).coins);
        assertEquals("steam", prizes.get(1).caseId);
        assertEquals(202639, prizes.get(1).week);
        assertEquals(2, prizes.get(1).place);
        assertTrue(TournamentLogic.prizesFor(new ArrayList<>(), 202639).isEmpty());
        assertEquals("IV", TournamentLogic.caseNumeral("flora"));
        assertEquals("III", TournamentLogic.caseNumeral("steam"));
        assertEquals("II", TournamentLogic.caseNumeral("smeltery"));
        assertEquals("abyss", TournamentLogic.caseNumeral("abyss"));
    }

    @Test
    void pendingPrizesAreClaimedOnceAndOnlyByTheirOwner() {
        List<TournamentLogic.Prize> pending = new ArrayList<>();
        pending.add(new TournamentLogic.Prize(A, 202639, 1, 2500, "flora"));
        pending.add(new TournamentLogic.Prize(B, 202639, 2, 1000, "steam"));
        List<TournamentLogic.Prize> mine = TournamentLogic.claimable(pending, A);
        assertEquals(1, mine.size());
        assertEquals("flora", mine.get(0).caseId);
        TournamentLogic.removeClaimed(pending, A);
        assertTrue(TournamentLogic.claimable(pending, A).isEmpty(), "second login must not grant again");
        assertEquals(1, pending.size(), "other players' prizes stay queued");
    }

    @Test
    void weekendBoundariesFollowTheGivenZone() {
        ZoneId msk = ZoneId.of("Europe/Moscow");
        ZonedDateTime saturday = ZonedDateTime.of(2026, 9, 26, 12, 0, 0, 0, msk);
        // Monday 2026-09-28 00:00 MSK = 2026-09-27T21:00:00Z
        assertEquals(java.time.Instant.parse("2026-09-27T21:00:00Z").toEpochMilli(), TournamentLogic.endOfWeekendMs(saturday));
        ZonedDateTime tuesday = ZonedDateTime.of(2026, 9, 29, 10, 0, 0, 0, msk);
        // Saturday 2026-10-03 00:00 MSK = 2026-10-02T21:00:00Z
        assertEquals(java.time.Instant.parse("2026-10-02T21:00:00Z").toEpochMilli(), TournamentLogic.nextStartMs(tuesday));
    }

    @Test
    void snapshotJsonMatchesTheWorkerContract() {
        List<TournamentLogic.Entry> top = new ArrayList<>();
        TournamentLogic.record(top, A, "xietoru", 12.5, "Тунец");
        String json = TournamentLogic.snapshotJson(202639, "active",
                java.time.Instant.parse("2026-09-27T21:00:00Z").toEpochMilli(),
                java.time.Instant.parse("2026-10-02T21:00:00Z").toEpochMilli(), top);
        JsonObject o = JsonParser.parseString(json).getAsJsonObject();
        assertEquals(202639, o.get("week").getAsInt());
        assertEquals("active", o.get("status").getAsString());
        assertEquals("2026-09-27T21:00:00Z", o.get("ends_at").getAsString());
        assertEquals("2026-10-02T21:00:00Z", o.get("next_starts_at").getAsString());
        JsonObject first = o.getAsJsonArray("top").get(0).getAsJsonObject();
        assertEquals(A, first.get("uuid").getAsString());
        assertEquals("xietoru", first.get("nick").getAsString());
        assertEquals(12.5, first.get("weight").getAsDouble());
        assertEquals("Тунец", first.get("fish").getAsString());
    }

    @Test
    void loadsLegacyStateFileWithoutLosingTheTopThree() {
        String legacy = "{\"week\":3912,\"top\":[{\"name\":\"xietoru\",\"uuid\":\"" + A + "\",\"weight\":12.5,\"fish\":\"Тунец\"}]}";
        TournamentState state = new Gson().fromJson(legacy, TournamentState.class);
        assertEquals(3912, state.week);
        assertEquals(1, state.top.size());
        assertEquals("xietoru", state.top.get(0).name);
        assertEquals(12.5, state.top.get(0).weight);
        assertNotNull(state.pending);
        assertTrue(state.pending.isEmpty());
        assertFalse(state.awarded);
        assertTrue(state.finalSent, "legacy files must not trigger a bogus finalize retry");
        assertEquals(0L, state.endsAt);
    }

    @Test
    void firstPlaceGetsALargeBoosterSecondAndThirdASmallOne() {
        assertEquals("large", TournamentLogic.boosterFor(1));
        assertEquals("small", TournamentLogic.boosterFor(2));
        assertEquals("small", TournamentLogic.boosterFor(3));
        assertNull(TournamentLogic.boosterFor(4));
        assertNull(TournamentLogic.boosterFor(0));
    }
}
