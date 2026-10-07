package net.aquatech.ui.fishing;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Вес и размер рыбы по видам. Таблица собрана из данных Starcatcher (tools/gen_fish_weights.py), формулы повторяют
 * {@code SizeAndWeight.getRandomWeight/getRandomSize} самого Starcatcher: перцентиль «топ N %» от 0.01 до 99.999
 * переводится в значение на отрезке {@code [среднее - разброс; среднее + разброс]}.
 * Чистая логика без Minecraft, чтобы её можно было тестировать.
 */
public final class FishWeight {

    public static final String RESOURCE = "/data/aquatech_ui/fish_weights.json";
    public static final float MIN_TOP_PERCENT = 0.01F;
    public static final float MAX_TOP_PERCENT = 99.999F;

    public record Species(int avgG, int devG, int avgCm, int devCm, String rarity) {
    }

    /** Улов одной рыбы: перцентиль в процентах «топ N %», чем меньше, тем тяжелее. */
    public record Roll(int grams, int cm, float topPercent) {
    }

    private static volatile Map<String, Species> table;

    private FishWeight() {
    }

    public static Species species(String itemId) {
        return table().get(itemId);
    }

    public static boolean isFish(String itemId) {
        return table().containsKey(itemId);
    }

    /** Тяжёлый улов: примерно верхние 20 % веса вида (от среднего плюс 0.6 разброса). */
    public static boolean isHeavy(String itemId, int grams) {
        Species species = species(itemId);
        return species != null && grams >= species.avgG() + species.devG() * 0.6;
    }

    public static Roll roll(Species species, float topPercent) {
        float p = Math.max(MIN_TOP_PERCENT, Math.min(MAX_TOP_PERCENT, topPercent));
        float t = (100.0F - p) / 100.0F;
        int grams = Math.max(1, spread(species.avgG(), species.devG(), t));
        int cm = Math.max(1, spread(species.avgCm(), species.devCm(), t));
        return new Roll(grams, cm, p);
    }

    /** {@code среднее + t * 2 * разброс - разброс}, как в Starcatcher. */
    private static int spread(int average, int deviation, float t) {
        float width = deviation * 2.0F;
        return (int) (average + t * width - width / 2.0F);
    }

    /** «450 г» до килограмма, дальше «14.20 кг». */
    public static String format(int grams) {
        if (grams < 1000) {
            return grams + " г";
        }
        return String.format(Locale.ROOT, "%.2f кг", grams / 1000.0);
    }

    static Map<String, Species> parse(Reader reader) {
        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
        Map<String, Species> out = new HashMap<>();
        for (Map.Entry<String, com.google.gson.JsonElement> e : root.entrySet()) {
            JsonObject o = e.getValue().getAsJsonObject();
            out.put(e.getKey(), new Species(
                    o.get("avgG").getAsInt(),
                    o.get("devG").getAsInt(),
                    o.get("avgCm").getAsInt(),
                    o.get("devCm").getAsInt(),
                    o.get("rarity").getAsString()));
        }
        return Map.copyOf(out);
    }

    private static Map<String, Species> table() {
        Map<String, Species> t = table;
        if (t == null) {
            synchronized (FishWeight.class) {
                t = table;
                if (t == null) {
                    t = load();
                    table = t;
                }
            }
        }
        return t;
    }

    private static Map<String, Species> load() {
        try (InputStream in = FishWeight.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IOException("нет ресурса " + RESOURCE);
            }
            return parse(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("не удалось прочитать таблицу веса рыбы", e);
        }
    }
}
