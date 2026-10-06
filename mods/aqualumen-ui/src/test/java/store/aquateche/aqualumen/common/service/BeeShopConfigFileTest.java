package store.aquateche.aqualumen.common.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Товары пчеловода (bee.* в server_shop.json): клетка с пчелой строится из SNBT, битая строка сорвала бы покупку уже после списания монет. */
class BeeShopConfigFileTest {

    private static final Path REPO_CONFIG = Path.of("../../config/aqualumen/server_shop.json");
    private static final Path SERVER_CONFIG = Path.of("../../server/config/aqualumen/server_shop.json");

    private static JsonArray items(Path file) throws Exception {
        return JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonArray("items");
    }

    @Test
    void beeOffersAreWellFormed() throws Exception {
        Set<String> ids = new HashSet<>();
        int bees = 0;
        int supplies = 0;
        for (JsonElement element : items(REPO_CONFIG)) {
            JsonObject item = element.getAsJsonObject();
            String id = item.get("id").getAsString();
            assertTrue(ids.add(id), "duplicate id " + id);
            if (!BeeKeeperProximity.isBeeOffer(id)) {
                continue;
            }
            assertTrue(item.get("price").getAsLong() > 0, id);
            if (!item.has("kind")) {
                supplies++;
                continue;
            }
            assertEquals("item_nbt", item.get("kind").getAsString(), id);
            assertTrue(item.get("price").getAsLong() >= 50_000, id + " bee costs less than 50000");
            String[] parts = item.get("payload").getAsString().split("\\|", 3);
            assertEquals(3, parts.length, id);
            assertEquals("productivebees:bee_cage", parts[0], id);
            CompoundTag tag = TagParser.parseTag(parts[2]);
            String entity = tag.getString("entity");
            assertTrue(entity.matches("productivebees:[a-z_]+"), id + " entity=" + entity);
            assertFalse(tag.getString("name").isBlank(), id);
            bees++;
        }
        assertEquals(12, bees);
        assertTrue(supplies >= 5, "starter supplies are missing");
    }

    @Test
    void serverCopyKeepsTheSameBeeOffers() throws Exception {
        assertEquals(items(REPO_CONFIG), items(SERVER_CONFIG));
    }
}
