package store.aquateche.aqualumen.common.service;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Настоящий config/aqualumen/cases.json должен читаться тем же Gson, что и CaseConfig: иначе хаб молча покажет запасные кейсы. */
class CaseConfigFileTest {

    private static final Path REPO_CONFIG = Path.of("../../config/aqualumen/cases.json");
    private static final Path SERVER_CONFIG = Path.of("../../server/config/aqualumen/cases.json");

    private static CaseConfig.Data parse(Path file) throws Exception {
        return new Gson().fromJson(Files.readString(file, StandardCharsets.UTF_8), CaseConfig.Data.class);
    }

    @Test
    void shippedCasesParseWithIntegerFields() throws Exception {
        CaseConfig.Data data = parse(REPO_CONFIG);
        assertEquals(11, data.cases.size());
        for (CaseConfig.CaseDef def : data.cases) {
            assertTrue(def.costCoins > 0, def.id);
            assertFalse(def.loot.isEmpty(), def.id);
            for (CaseConfig.LootDef loot : def.loot) {
                assertTrue(loot.weight >= 1 && loot.min >= 1 && loot.max >= loot.min, def.id + " / " + loot.label);
            }
        }
    }

    @Test
    void serverCopyMatchesRepoConfig() throws Exception {
        assertEquals(Files.readString(REPO_CONFIG, StandardCharsets.UTF_8), Files.readString(SERVER_CONFIG, StandardCharsets.UTF_8));
    }
}
