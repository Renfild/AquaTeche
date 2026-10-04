package store.aquateche.aqualumen.common.service;

import java.util.List;

/** The six steps of the new-player guide: what the HUD panel shows for each. Pure data, no Minecraft types. */
public final class OnboardingSteps {

    public record Step(int id, String title, String hint, int goal) {
    }

    private static final List<Step> ALL = List.of(
            new Step(1, "Создай свой остров", "Напиши в чате /is: появится твой личный остров", 1),
            new Step(2, "Забери стартовый набор", "Нажми F4, вкладка «Киты»", 1),
            new Step(3, "Поймай 3 рыбы", "Возьми удочку в руку и закинь её в воду: правая кнопка мыши", 3),
            new Step(4, "Открой подарочный кейс", "Нажми F4, вкладка «Кейсы»", 1),
            new Step(5, "Продай улов", "Подойди к торговцу рыбой на спавне и нажми по нему правой кнопкой мыши", 1),
            new Step(6, "Поймай руду", "Медь, олово, железо или уголь выпадают с удочки", 1));

    public static final int COUNT = ALL.size();

    private OnboardingSteps() {
    }

    /** Step by its number 1..COUNT, or null outside the range. */
    public static Step get(int id) {
        return id >= 1 && id <= COUNT ? ALL.get(id - 1) : null;
    }

    public static List<Step> all() {
        return ALL;
    }

    public static double fraction(int have, int goal) {
        if (goal <= 0) {
            return 1.0;
        }
        return Math.max(0.0, Math.min(1.0, (double) have / goal));
    }

    /**
     * Label for a key binding. The game prints the letter of the player's keyboard layout (the physical Y key shows
     * as the Cyrillic "Н" on a Russian layout, which looks like a Latin H), so for short names like
     * "key.keyboard.y" or "key.mouse.4" the Latin name is used; longer names keep the translated text.
     */
    public static String keyLabel(String translationKey, String translated) {
        if (translationKey != null) {
            String tail = translationKey.substring(translationKey.lastIndexOf('.') + 1);
            if (!tail.isEmpty() && tail.length() <= 3) {
                return tail.toUpperCase(java.util.Locale.ROOT);
            }
        }
        return translated;
    }

    /** Reward text arrives with legacy colour codes (section sign + char); the panel draws its own colours. */
    public static String plain(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("§.", "").trim();
    }
}
