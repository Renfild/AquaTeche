package net.aquatech.ui.fishing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FishNamesTest {

    @Test
    void stripsStarcatcherRarityMarkup() {
        assertEquals("Морской окунь", FishNames.strip("<sccommon>Морской окунь</sccommon>"));
        assertEquals("Жёлтокаменная рыба «xietoru»", FishNames.strip("<scrare>Жёлтокаменная рыба «xietoru»</scrare>"));
    }

    @Test
    void leavesPlainNamesAndOddInputAlone() {
        assertEquals("Лещ", FishNames.strip("Лещ"));
        assertEquals("", FishNames.strip(null));
        assertEquals("a < b", FishNames.strip("a < b"));
        assertEquals("Лещ", FishNames.strip("  Лещ  "));
    }
}
