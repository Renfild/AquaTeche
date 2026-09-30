package net.aquatech.ui.client.chat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AquaChatLayoutTest {

    @Test
    void emptyTabHasNoRowToHitTest() {
        assertEquals(-1, AquaChatLayout.firstVisibleIndex(0, 0));
        assertEquals(-1, AquaChatLayout.firstVisibleIndex(0, 5));
    }

    @Test
    void newestRowIsFirstWhenNotScrolled() {
        assertEquals(9, AquaChatLayout.firstVisibleIndex(10, 0));
    }

    @Test
    void scrollMovesTowardOlderRows() {
        assertEquals(6, AquaChatLayout.firstVisibleIndex(10, 3));
    }

    @Test
    void scrollPastHistoryStaysOnOldestRow() {
        assertEquals(0, AquaChatLayout.firstVisibleIndex(10, 40));
    }

    @Test
    void negativeScrollStaysOnNewestRow() {
        assertEquals(9, AquaChatLayout.firstVisibleIndex(10, -2));
    }
}
