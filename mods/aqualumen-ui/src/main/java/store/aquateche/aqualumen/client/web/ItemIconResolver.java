package store.aquateche.aqualumen.client.web;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import store.aquateche.aqualumen.AquaLumenUI;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Иконка предмета для страницы меню без OpenGL: берёт текстуру из модели предмета в ресурсах игры (layer0 у
 * плоских предметов, боковая или общая грань у блоков). Нужна аукциону: туда попадает любой предмет любого мода, и
 * заранее вшить все иконки в страницу нельзя. Вернёт null, если у предмета нет простой текстуры (яйца, щиты, сундуки).
 */
public final class ItemIconResolver {

    private static final String[] TEXTURE_KEYS = {"layer0", "all", "side", "front", "texture", "top", "particle", "end"};
    private static final int MAX_PARENT_DEPTH = 6;
    private static final Map<String, Optional<String>> CACHE = new HashMap<>();

    private ItemIconResolver() {
    }

    /** data:image/png;base64,... или null. Вызывать с клиентского потока. */
    public static String dataUrl(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return null;
        }
        return CACHE.computeIfAbsent(itemId, ItemIconResolver::resolve).orElse(null);
    }

    private static Optional<String> resolve(String itemId) {
        try {
            ResourceLocation id = ResourceLocation.tryParse(itemId);
            if (id == null) {
                return Optional.empty();
            }
            ResourceManager manager = Minecraft.getInstance().getResourceManager();
            Map<String, String> textures = new LinkedHashMap<>();
            collectTextures(manager, new ResourceLocation(id.getNamespace(), "item/" + id.getPath()), textures, 0);
            ResourceLocation texture = pickTexture(textures);
            if (texture == null) {
                return Optional.empty();
            }
            byte[] png = readFirstFrame(manager, new ResourceLocation(texture.getNamespace(), "textures/" + texture.getPath() + ".png"));
            return png == null ? Optional.empty() : Optional.of("data:image/png;base64," + Base64.getEncoder().encodeToString(png));
        } catch (IOException | RuntimeException error) {
            AquaLumenUI.LOGGER.debug("[AquaLumen CEF] no icon for {}: {}", itemId, error.toString());
            return Optional.empty();
        }
    }

    /** Текстуры модели с родителями: сначала родитель, потом свои ключи поверх. */
    private static void collectTextures(ResourceManager manager, ResourceLocation model, Map<String, String> into, int depth)
            throws IOException {
        if (depth > MAX_PARENT_DEPTH || model.getPath().startsWith("builtin/")) {
            return;
        }
        Optional<Resource> resource = manager.getResource(new ResourceLocation(model.getNamespace(), "models/" + model.getPath() + ".json"));
        if (resource.isEmpty()) {
            return;
        }
        JsonObject json;
        try (InputStream stream = resource.get().open();
             InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            json = JsonParser.parseReader(reader).getAsJsonObject();
        }
        if (json.has("parent")) {
            collectTextures(manager, new ResourceLocation(json.get("parent").getAsString()), into, depth + 1);
        }
        if (json.has("textures") && json.get("textures").isJsonObject()) {
            json.getAsJsonObject("textures").entrySet().forEach(entry -> {
                if (entry.getValue().isJsonPrimitive()) {
                    into.put(entry.getKey(), entry.getValue().getAsString());
                }
            });
        }
    }

    private static ResourceLocation pickTexture(Map<String, String> textures) {
        for (String key : TEXTURE_KEYS) {
            String value = textures.get(key);
            for (int hop = 0; value != null && value.startsWith("#") && hop < 5; hop++) {
                value = textures.get(value.substring(1));
            }
            if (value != null && !value.startsWith("#")) {
                return ResourceLocation.tryParse(value);
            }
        }
        return null;
    }

    /** PNG как есть, а у анимированной текстуры (вертикальная лента) только первый кадр. */
    private static byte[] readFirstFrame(ResourceManager manager, ResourceLocation texture) throws IOException {
        Optional<Resource> resource = manager.getResource(texture);
        if (resource.isEmpty()) {
            return null;
        }
        byte[] raw;
        try (InputStream stream = resource.get().open()) {
            raw = stream.readAllBytes();
        }
        try (NativeImage image = NativeImage.read(new java.io.ByteArrayInputStream(raw))) {
            int width = image.getWidth();
            int height = image.getHeight();
            if (height <= width) {
                return raw;
            }
            try (NativeImage frame = new NativeImage(width, width, false)) {
                image.copyRect(frame, 0, 0, 0, 0, width, width, false, false);
                return frame.asByteArray();
            }
        }
    }
}
