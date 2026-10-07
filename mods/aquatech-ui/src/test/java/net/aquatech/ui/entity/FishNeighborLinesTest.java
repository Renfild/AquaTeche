package net.aquatech.ui.entity;

import net.aquatech.ui.fishing.FameLogic;
import net.aquatech.ui.fishing.FishWeight;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class FishNeighborLinesTest {

    @Test
    void pickNeverRepeatsTheLastStoryAndStaysInRange() {
        Random rnd = new Random(7);
        int size = FishNeighborLines.STORIES.size();
        int last = 3;
        for (int i = 0; i < 2000; i++) {
            int next = FishNeighborLines.pickStory(size, last, rnd::nextInt);
            assertNotEquals(last, next);
            assertTrue(next >= 0 && next < size);
            last = next;
        }
    }

    @Test
    void pickCoversEveryStoryEventually() {
        Random rnd = new Random(1);
        int size = FishNeighborLines.STORIES.size();
        boolean[] seen = new boolean[size];
        int last = -1;
        for (int i = 0; i < 2000; i++) {
            last = FishNeighborLines.pickStory(size, last, rnd::nextInt);
            seen[last] = true;
        }
        for (boolean s : seen) {
            assertTrue(s);
        }
    }

    @Test
    void firstPickWithoutHistoryAndTinyLists() {
        assertEquals(0, FishNeighborLines.pickStory(1, 0, bound -> 0));
        assertEquals(2, FishNeighborLines.pickStory(5, -1, bound -> 2));
    }

    @Test
    void storiesAreShortEnoughForOneChatLine() {
        for (String story : FishNeighborLines.STORIES) {
            assertTrue(story.length() <= 110, story);
            assertFalse(story.isBlank());
        }
    }

    @Test
    void recordLinesNameTheRightPeople() {
        String taken = FishNeighborLines.record(FameLogic.Outcome.TAKEN, "Ann", "Bob", "Лещ", "3.10 кг");
        assertTrue(taken.contains("Ann") && taken.contains("Bob") && taken.contains("3.10 кг"));
        assertTrue(FishNeighborLines.record(FameLogic.Outcome.FIRST, "Ann", null, "Лещ", "1 кг").contains("Ann"));
        assertTrue(FishNeighborLines.record(FameLogic.Outcome.OWN, "Ann", null, "Лещ", "1 кг").contains("собственный"));
        assertEquals("", FishNeighborLines.record(FameLogic.Outcome.NONE, "Ann", null, "Лещ", "1 кг"));
        assertDoesNotThrow(() -> FishNeighborLines.record(FameLogic.Outcome.TAKEN, "Ann", null, "Лещ", "1 кг"));
    }

    @Test
    void heavyCatchVariantsAllMentionThePlayer() {
        for (int variant = -3; variant < 6; variant++) {
            assertTrue(FishNeighborLines.heavyCatch("Ann", variant).contains("Ann"));
        }
    }

    @Test
    void heavyMeansTopOfTheSpeciesRange() {
        // agave_bream: среднее 2000 г, разброс 1000 г, порог 2600 г
        assertTrue(FishWeight.isHeavy("starcatcher:agave_bream", 2600));
        assertFalse(FishWeight.isHeavy("starcatcher:agave_bream", 2599));
        assertFalse(FishWeight.isHeavy("minecraft:cobblestone", 999999));
    }

    @Test
    void greetingNamesThePlayerAndTellsHowToTalk() {
        for (int variant = -2; variant < 5; variant++) {
            String line = FishNeighborLines.greeting("Ann", variant);
            assertTrue(line.contains("Ann"));
            assertTrue(line.contains("ПКМ"), line);
        }
    }
}
