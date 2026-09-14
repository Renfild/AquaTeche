package store.aquateche.aqualumen.common.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.minecraftforge.fml.loading.FMLPaths;
import store.aquateche.aqualumen.AquaLumenUI;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Серверный магазин базовых ресурсов (F4 → Магазин, валюта: монеты).
 * Конфиг config/aqualumen/server_shop.json — оверпрайс относительно крафта.
 * kind = "item": выдача через StoreCatalog.giveItem (спек "modid:item[:count]").
 */
public final class ServerShopConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Path FILE = FMLPaths.CONFIGDIR.get().resolve("aqualumen/server_shop.json");

    public record ShopEntry(String id, String item, String title, long price) {
    }

    private static volatile List<ShopEntry> cached = List.of();
    private static long cachedMtime = Long.MIN_VALUE;

    private ServerShopConfig() {
    }

    private static void load() {
        long mtime;
        try {
            mtime = Files.exists(FILE) ? Files.getLastModifiedTime(FILE).toMillis() : Long.MIN_VALUE;
        } catch (IOException e) {
            mtime = Long.MIN_VALUE;
        }
        if (mtime == cachedMtime && !cached.isEmpty()) {
            return;
        }
        cachedMtime = mtime;
        if (!Files.exists(FILE)) {
            writeDefault();
            return;
        }
        try {
            JsonObject root = GSON.fromJson(Files.readString(FILE, StandardCharsets.UTF_8), JsonObject.class);
            if (root != null && root.has("items")) {
                Type type = new TypeToken<List<ShopEntry>>() {}.getType();
                List<ShopEntry> list = GSON.fromJson(root.get("items"), type);
                if (list != null) {
                    cached = List.copyOf(list);
                }
            }
        } catch (Throwable e) {
            AquaLumenUI.LOGGER.warn("server_shop.json parse failed: {}", e.toString());
        }
    }

    private static void writeDefault() {
        List<ShopEntry> defaults = defaults();
        try {
            Files.createDirectories(FILE.getParent());
            JsonArray arr = new JsonArray();
            for (ShopEntry e : defaults) {
                JsonObject o = new JsonObject();
                o.addProperty("id", e.id());
                o.addProperty("item", e.item());
                o.addProperty("title", e.title());
                o.addProperty("price", e.price());
                arr.add(o);
            }
            JsonObject root = new JsonObject();
            root.add("items", arr);
            Files.writeString(FILE, GSON.toJson(root), StandardCharsets.UTF_8);
            cached = List.copyOf(defaults);
            AquaLumenUI.LOGGER.info("Created default {}", FILE.getFileName());
        } catch (Throwable e) {
            AquaLumenUI.LOGGER.warn("Could not write server_shop.json: {}", e.toString());
            cached = List.copyOf(defaults);
        }
    }

    private static List<ShopEntry> defaults() {
        return List.of(
                // Печати AE2 для вырезателя (Inscriber) — падают только с метеоритов
                new ShopEntry("ae.silicon_press", "ae2:silicon_press:1", "Кремниевая печать", 2500),
                new ShopEntry("ae.logic_press", "ae2:logic_processor_press:1", "Логическая печать", 2500),
                new ShopEntry("ae.calc_press", "ae2:calculation_processor_press:1", "Вычислительная печать", 2500),
                new ShopEntry("ae.eng_press", "ae2:engineering_processor_press:1", "Инженерная печать", 2500),
                new ShopEntry("ae.name_press", "ae2:name_press:1", "Именная печать", 1500),
                // AE2 базовые
                new ShopEntry("ae.certus", "ae2:certus_quartz_crystal:16", "Истинный кварц ×16", 320),
                new ShopEntry("ae.charged_certus", "ae2:charged_certus_quartz_crystal:8", "Заряженный истинный кварц ×8", 260),
                // Industrial Upgrade: базовые ресурсы и электросхемы механизмов
                new ShopEntry("iu.aluminium", "industrialupgrade:itemingots/aluminium_ingot:16", "Алюминий ×16", 480),
                new ShopEntry("iu.circuit1", "industrialupgrade:crafting_elements/crafting_272_element:2", "Электронная схема ×2", 1200),
                new ShopEntry("iu.circuit2", "industrialupgrade:crafting_elements/crafting_273_element:2", "Продвинутая схема ×2", 1400),
                new ShopEntry("iu.circuit3", "industrialupgrade:crafting_elements/crafting_274_element:2", "Микропроцессор ×2", 1600),
                new ShopEntry("iu.reinforced_stone", "industrialupgrade:blockresource/reinforced_stone:16", "Укреплённый камень ×16", 400),
                // Botania
                new ShopEntry("bot.manasteel", "botania:manasteel_ingot:8", "Слиток манастали ×8", 640),
                new ShopEntry("bot.mana_pearl", "botania:mana_pearl:4", "Жемчужина маны ×4", 800),
                new ShopEntry("bot.mana_diamond", "botania:mana_diamond:4", "Алмаз маны ×4", 800),
                // Ваниль в запасе
                new ShopEntry("base.redstone", "minecraft:redstone:32", "Редстоун ×32", 160),
                new ShopEntry("base.obsidian", "minecraft:obsidian:8", "Обсидиан ×8", 240)
        );
    }

    /** Все товары магазина (сегмент F4-стора). */
    public static List<StoreCatalog.Product> products() {
        load();
        return cached.stream()
                .map(e -> new StoreCatalog.Product(e.id(), e.title(), "Серверная поставка", e.price(),
                        "coins", "item", e.item()))
                .toList();
    }

    public static StoreCatalog.Product find(String id) {
        load();
        String key = id == null ? "" : id.toLowerCase(java.util.Locale.ROOT);
        for (ShopEntry e : cached) {
            if (e.id().toLowerCase(java.util.Locale.ROOT).equals(key)) {
                return new StoreCatalog.Product(e.id(), e.title(), "Серверная поставка", e.price(),
                        "coins", "item", e.item());
            }
        }
        return null;
    }
}
