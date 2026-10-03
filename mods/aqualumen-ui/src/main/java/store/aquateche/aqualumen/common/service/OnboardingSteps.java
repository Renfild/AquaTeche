package store.aquateche.aqualumen.common.service;

import java.util.List;

/** The five steps of the new-player guide: what the HUD panel shows for each. Pure data, no Minecraft types. */
public final class OnboardingSteps {

    public record Step(int id, String title, String hint, int goal) {
    }

    private static final List<Step> ALL = List.of(
            new Step(1, "Забери стартовый набор", "F4, вкладка «Киты», или команда /kit start", 1),
            new Step(2, "Поймай 3 рыбы", "Возьми удочку в руку и закинь её в воду: правая кнопка мыши", 3),
            new Step(3, "Открой подарочный кейс", "Нажми F4, вкладка «Кейсы»", 1),
            new Step(4, "Продай улов", "F4, вкладка «Рыбалка», кнопка «Продать»", 1),
            new Step(5, "Поймай руду", "Медь, олово, железо или уголь выпадают с удочки", 1));

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

    /** Reward text arrives with legacy colour codes (section sign + char); the panel draws its own colours. */
    public static String plain(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("§.", "").trim();
    }
}
