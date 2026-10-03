package store.aquateche.aqualumen.common.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientVersionTest {

    @Test
    void sameOrNewerVersionPasses() {
        assertTrue(ClientVersion.atLeast("0.3.72-alpha", 0, 3, 72));
        assertTrue(ClientVersion.atLeast("0.3.100-alpha", 0, 3, 72));
        assertTrue(ClientVersion.atLeast("0.4.0", 0, 3, 72));
        assertTrue(ClientVersion.atLeast("1.0.0-beta", 0, 3, 72));
    }

    @Test
    void olderVersionFails() {
        assertFalse(ClientVersion.atLeast("0.3.71-alpha", 0, 3, 72));
        assertFalse(ClientVersion.atLeast("0.2.99", 0, 3, 72));
    }

    @Test
    void unknownOrBrokenVersionFails() {
        assertFalse(ClientVersion.atLeast(null, 0, 3, 72));
        assertFalse(ClientVersion.atLeast("", 0, 3, 72));
        assertFalse(ClientVersion.atLeast("dev", 0, 3, 72));
        assertFalse(ClientVersion.atLeast("0.3", 0, 3, 72));
    }
}
