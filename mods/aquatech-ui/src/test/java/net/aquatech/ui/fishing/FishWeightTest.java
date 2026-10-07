package net.aquatech.ui.fishing;

import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FishWeightTest {

    private static final FishWeight.Species BREAM = new FishWeight.Species(2000, 1000, 36, 12, "rare");

    @Test
    void percentileMapsToTheStarcatcherRangeAroundTheAverage() {
        // «топ 0.01 %» тяжелее всего, «топ 99.999 %» легче всего, 50 % ровно среднее
        assertEquals(2000, FishWeight.roll(BREAM, 50.0F).grams());
        assertTrue(FishWeight.roll(BREAM, 0.01F).grams() >= 2990);
        assertTrue(FishWeight.roll(BREAM, 99.999F).grams() <= 1010);
        assertTrue(FishWeight.roll(BREAM, 0.01F).grams() > FishWeight.roll(BREAM, 80.0F).grams());
    }

    @Test
    void sizeFollowsTheSamePercentile() {
        assertEquals(36, FishWeight.roll(BREAM, 50.0F).cm());
        assertTrue(FishWeight.roll(BREAM, 1.0F).cm() > FishWeight.roll(BREAM, 90.0F).cm());
    }

    @Test
    void outOfRangePercentileIsClampedAndWeightNeverDropsBelowOneGram() {
        assertEquals(FishWeight.roll(BREAM, 0.01F), FishWeight.roll(BREAM, -5.0F));
        assertEquals(FishWeight.roll(BREAM, 99.999F), FishWeight.roll(BREAM, 500.0F));
        FishWeight.Species tiny = new FishWeight.Species(5, 20, 3, 10, "trash");
        assertTrue(FishWeight.roll(tiny, 99.0F).grams() >= 1);
        assertTrue(FishWeight.roll(tiny, 99.0F).cm() >= 1);
    }

    @Test
    void formatsGramsAndKilograms() {
        assertEquals("450 г", FishWeight.format(450));
        assertEquals("999 г", FishWeight.format(999));
        assertEquals("1.00 кг", FishWeight.format(1000));
        assertEquals("14.20 кг", FishWeight.format(14200));
    }

    @Test
    void parsesTheGeneratedTableShape() {
        Map<String, FishWeight.Species> table = FishWeight.parse(new StringReader(
                "{\"starcatcher:a\":{\"avgG\":10,\"devG\":2,\"avgCm\":5,\"devCm\":1,\"rarity\":\"common\"}}"));
        assertEquals(new FishWeight.Species(10, 2, 5, 1, "common"), table.get("starcatcher:a"));
    }

    @Test
    void bundledTableCoversStarcatcherFishAndSkipsEverythingElse() {
        FishWeight.Species bream = FishWeight.species("starcatcher:agave_bream");
        assertNotNull(bream);
        assertEquals(2000, bream.avgG());
        assertEquals("rare", bream.rarity());
        assertTrue(FishWeight.isFish("starcatcher:agave_bream"));
        assertFalse(FishWeight.isFish("minecraft:cobblestone"));
        assertFalse(FishWeight.isFish("starcatcher:hopeful_bottle"), "secret bottles are not fish");
    }
}
