package net.aquatech.ui.block;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Садки живут в JSON и PNG: пропавший файл или ключ ломает блок только в игре, поэтому проверяем их заранее. */
class KeepnetResourcesTest {

    private static JsonObject json(String path) throws Exception {
        try (InputStream in = KeepnetResourcesTest.class.getResourceAsStream(path)) {
            assertNotNull(in, path + " is missing from the mod resources");
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }

    private static BufferedImage png(String path) throws Exception {
        try (InputStream in = KeepnetResourcesTest.class.getResourceAsStream(path)) {
            assertNotNull(in, path + " is missing from the mod resources");
            return ImageIO.read(in);
        }
    }

    private static void exists(String path) {
        assertNotNull(KeepnetResourcesTest.class.getResource(path), path + " is missing from the mod resources");
    }

    @Test
    void everyTierHasTheAssetsItNeeds() throws Exception {
        for (KeepnetTier tier : KeepnetTier.values()) {
            String id = tier.id();
            exists("/assets/aquatech_ui/textures/block/" + id + ".png");
            exists("/assets/aquatech_ui/textures/gui/" + id + ".png");
            exists("/assets/aquatech_ui/models/item/" + id + ".json");
            JsonObject blockstate = json("/assets/aquatech_ui/blockstates/" + id + ".json");
            for (String facing : new String[]{"north", "east", "south", "west"}) {
                assertTrue(blockstate.getAsJsonObject("variants").has("facing=" + facing), id + " blockstate variant " + facing);
            }
            JsonObject model = json("/assets/aquatech_ui/models/block/" + id + ".json");
            assertEquals("minecraft:cutout", model.get("render_type").getAsString(), id + " needs cutout rendering for the net");
            assertEquals("aquatech_ui:block/" + id, model.getAsJsonObject("textures").get("0").getAsString());
            assertEquals(50, model.getAsJsonArray("elements").size(), id + " keeps the shared садок geometry");
            assertEquals("aquatech_ui:block/" + id, json("/assets/aquatech_ui/models/item/" + id + ".json").get("parent").getAsString());
        }
    }

    @Test
    void guiHeightFollowsTheRowCount() throws Exception {
        for (KeepnetTier tier : KeepnetTier.values()) {
            BufferedImage gui = png("/assets/aquatech_ui/textures/gui/" + tier.id() + ".png");
            assertEquals(256, gui.getWidth(), tier.id() + " GUI width");
            assertEquals(tier.imageHeight(), gui.getHeight(), tier.id() + " GUI height must match the menu layout");
        }
    }

    @Test
    void tiersGrowByOneRowEach() {
        KeepnetTier[] tiers = KeepnetTier.values();
        assertEquals(6, tiers.length);
        for (int i = 0; i < tiers.length; i++) {
            assertEquals(3 + i, tiers[i].rows(), tiers[i] + " rows");
            assertEquals((3 + i) * KeepnetTier.COLS, tiers[i].slots(), tiers[i] + " slots");
            assertEquals(212 + 18 * i, tiers[i].imageHeight(), tiers[i] + " GUI height");
        }
        assertEquals(27, tiers[0].slots());
        assertEquals(72, tiers[5].slots());
    }

    @Test
    void recipesChainThroughThePreviousTier() throws Exception {
        KeepnetTier[] tiers = KeepnetTier.values();
        for (int i = 0; i < tiers.length; i++) {
            String id = tiers[i].id();
            JsonObject recipe = json("/data/aquatech_ui/recipes/" + id + ".json");
            assertEquals("aquatech_ui:" + id, recipe.getAsJsonObject("result").get("item").getAsString());
            assertEquals(3, recipe.getAsJsonArray("pattern").size());
            if (i > 0) {
                String previous = "aquatech_ui:" + tiers[i - 1].id();
                assertEquals(previous, recipe.getAsJsonObject("key").getAsJsonObject("K").get("item").getAsString(), id + " is crafted from the previous tier");
            }
        }
    }

    @Test
    void lootAndAxeTagCoverEveryTier() throws Exception {
        String axe = json("/data/minecraft/tags/blocks/mineable/axe.json").getAsJsonArray("values").toString();
        for (KeepnetTier tier : KeepnetTier.values()) {
            JsonObject loot = json("/data/aquatech_ui/loot_tables/blocks/" + tier.id() + ".json");
            assertEquals("aquatech_ui:" + tier.id(),
                    loot.getAsJsonArray("pools").get(0).getAsJsonObject().getAsJsonArray("entries").get(0).getAsJsonObject().get("name").getAsString());
            assertTrue(axe.contains("aquatech_ui:" + tier.id()), tier.id() + " must be mineable by axe");
        }
    }

    @Test
    void bothLanguagesNameEveryTier() throws Exception {
        assertEquals("Fish Keepnet", json("/assets/aquatech_ui/lang/en_us.json").get("block.aquatech_ui.fish_keepnet").getAsString());
        assertEquals("Садок", json("/assets/aquatech_ui/lang/ru_ru.json").get("block.aquatech_ui.fish_keepnet").getAsString());
        for (KeepnetTier tier : KeepnetTier.values()) {
            String key = "block.aquatech_ui." + tier.id();
            assertTrue(json("/assets/aquatech_ui/lang/en_us.json").has(key), "en_us " + key);
            assertTrue(json("/assets/aquatech_ui/lang/ru_ru.json").has(key), "ru_ru " + key);
        }
    }
}
