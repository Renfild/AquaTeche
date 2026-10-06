package net.aquatech.ui.block;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Садок живёт в JSON и PNG: пропавший файл или ключ ломает блок только в игре, поэтому проверяем их заранее. */
class KeepnetResourcesTest {

    private static JsonObject json(String path) throws Exception {
        try (InputStream in = KeepnetResourcesTest.class.getResourceAsStream(path)) {
            assertNotNull(in, path + " is missing from the mod resources");
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }

    private static void exists(String path) {
        assertNotNull(KeepnetResourcesTest.class.getResource(path), path + " is missing from the mod resources");
    }

    @Test
    void blockHasEveryAssetItNeeds() throws Exception {
        exists("/assets/aquatech_ui/textures/block/fish_keepnet.png");
        exists("/assets/aquatech_ui/textures/gui/fish_keepnet.png");
        exists("/assets/aquatech_ui/models/item/fish_keepnet.json");
        JsonObject blockstate = json("/assets/aquatech_ui/blockstates/fish_keepnet.json");
        for (String facing : new String[]{"north", "east", "south", "west"}) {
            assertTrue(blockstate.getAsJsonObject("variants").has("facing=" + facing), "blockstate variant " + facing);
        }
        JsonObject model = json("/assets/aquatech_ui/models/block/fish_keepnet.json");
        assertEquals("minecraft:cutout", model.get("render_type").getAsString(), "the net needs cutout rendering");
        assertEquals("aquatech_ui:block/fish_keepnet", model.getAsJsonObject("textures").get("0").getAsString());
        assertEquals(50, model.getAsJsonArray("elements").size());
    }

    @Test
    void recipeAndLootProduceTheBlock() throws Exception {
        JsonObject recipe = json("/data/aquatech_ui/recipes/fish_keepnet.json");
        assertEquals("aquatech_ui:fish_keepnet", recipe.getAsJsonObject("result").get("item").getAsString());
        assertEquals(3, recipe.getAsJsonArray("pattern").size());
        JsonObject loot = json("/data/aquatech_ui/loot_tables/blocks/fish_keepnet.json");
        assertEquals("aquatech_ui:fish_keepnet",
                loot.getAsJsonArray("pools").get(0).getAsJsonObject().getAsJsonArray("entries").get(0).getAsJsonObject().get("name").getAsString());
        assertTrue(json("/data/minecraft/tags/blocks/mineable/axe.json").getAsJsonArray("values").toString().contains("aquatech_ui:fish_keepnet"));
    }

    @Test
    void bothLanguagesNameTheBlock() throws Exception {
        assertEquals("Fish Keepnet", json("/assets/aquatech_ui/lang/en_us.json").get("block.aquatech_ui.fish_keepnet").getAsString());
        assertEquals("Садок", json("/assets/aquatech_ui/lang/ru_ru.json").get("block.aquatech_ui.fish_keepnet").getAsString());
    }
}
