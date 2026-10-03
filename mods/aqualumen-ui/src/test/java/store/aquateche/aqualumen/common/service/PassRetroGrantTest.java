package store.aquateche.aqualumen.common.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PassRetroGrantTest {

    @Test
    void owedTiersAreTheClaimedOnesWithExtrasNotYetGiven() {
        assertEquals(List.of(3, 8, 13), PassRetroGrant.due(Set.of(1, 2, 3, 8, 13, 20), Set.of()));
    }

    @Test
    void extrasAlreadyGivenAreNotGivenTwice() {
        assertEquals(List.of(8), PassRetroGrant.due(Set.of(3, 8), Set.of(3)));
        assertTrue(PassRetroGrant.due(Set.of(3, 8, 13), Set.of(3, 8, 13)).isEmpty());
    }

    @Test
    void unclaimedTiersAndOtherTiersOweNothing() {
        assertTrue(PassRetroGrant.due(Set.of(), Set.of()).isEmpty());
        assertTrue(PassRetroGrant.due(Set.of(1, 5, 10, 25), Set.of()).isEmpty());
    }

    @Test
    void extrasMatchTheFreeTrackTable() {
        assertTrue(PassRetroGrant.extraFor(3).smallBooster());
        assertTrue(PassRetroGrant.extraFor(8).smallBooster());
        assertEquals("starter", PassRetroGrant.extraFor(13).caseId());
        assertNull(PassRetroGrant.extraFor(5));
    }
}
