package net.aquatech.ui.entity;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BeeKeeperAnimsTest {

    @Test
    void everyAnimationKeyUsedByTheEntityExistsInTheAnimationFile() throws Exception {
        JsonObject animations;
        try (InputStream in = getClass().getResourceAsStream("/assets/aquatech_ui/animations/bee_keeper.animation.json")) {
            assertTrue(in != null, "animation file is missing from the mod resources");
            animations = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                    .getAsJsonObject().getAsJsonObject("animations");
        }
        for (Field field : BeeKeeperAnims.class.getDeclaredFields()) {
            if (!Modifier.isPublic(field.getModifiers())) {
                continue;
            }
            String key = (String) field.get(null);
            assertTrue(animations.has(key), field.getName() + " -> " + key + " is not in bee_keeper.animation.json");
        }
    }

    @Test
    void geometryAndTextureShipWithTheMod() {
        assertTrue(getClass().getResource("/assets/aquatech_ui/geo/bee_keeper.geo.json") != null);
        assertTrue(getClass().getResource("/assets/aquatech_ui/textures/entity/bee_keeper.png") != null);
    }
}
