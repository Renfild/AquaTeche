package net.aquatech.ui.fishing;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FameLogicTest {

    private static final String SPECIES = "starcatcher:agave_bream";

    private static FameLogic.Record rec(String uuid, int grams) {
        return new FameLogic.Record(uuid, "n-" + uuid, "Лещ", grams, 30, "2026-10-07");
    }

    @Test
    void firstRecordNeedsTheSpeciesAverageSoTheVeryFirstTinyCatchIsNotARecord() {
        Map<String, FameLogic.Record> records = new HashMap<>();
        assertEquals(FameLogic.Outcome.NONE, FameLogic.apply(records, SPECIES, rec("a", 1999), 2000, null));
        assertTrue(records.isEmpty());
        assertEquals(FameLogic.Outcome.FIRST, FameLogic.apply(records, SPECIES, rec("a", 2000), 2000, null));
        assertEquals(2000, records.get(SPECIES).grams);
    }

    @Test
    void anotherPlayerTakesTheRecordOnlyWithAStrictlyHeavierFish() {
        Map<String, FameLogic.Record> records = new HashMap<>();
        FameLogic.apply(records, SPECIES, rec("a", 2500), 2000, null);
        FameLogic.Record[] prev = new FameLogic.Record[1];
        assertEquals(FameLogic.Outcome.NONE, FameLogic.apply(records, SPECIES, rec("b", 2500), 2000, prev), "tie keeps the holder");
        assertNull(prev[0]);
        assertEquals(FameLogic.Outcome.TAKEN, FameLogic.apply(records, SPECIES, rec("b", 2501), 2000, prev));
        assertEquals("a", prev[0].uuid);
        assertEquals("b", records.get(SPECIES).uuid);
    }

    @Test
    void ownRecordIsReportedSeparatelyFromTakingSomeoneElses() {
        Map<String, FameLogic.Record> records = new HashMap<>();
        FameLogic.apply(records, SPECIES, rec("a", 2500), 2000, null);
        assertEquals(FameLogic.Outcome.OWN, FameLogic.apply(records, SPECIES, rec("a", 3000), 2000, null));
        assertEquals(3000, records.get(SPECIES).grams);
    }

    @Test
    void speciesAreIndependent() {
        Map<String, FameLogic.Record> records = new HashMap<>();
        FameLogic.apply(records, SPECIES, rec("a", 2500), 2000, null);
        assertEquals(FameLogic.Outcome.FIRST, FameLogic.apply(records, "starcatcher:other", rec("b", 100), 50, null));
        assertEquals(2, records.size());
    }

    @Test
    void zeroOrNegativeWeightNeverCounts() {
        assertEquals(FameLogic.Outcome.NONE, FameLogic.judge(null, "a", 0, 0));
        assertEquals(FameLogic.Outcome.NONE, FameLogic.judge(null, "a", -5, 0));
    }
}
