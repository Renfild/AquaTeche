package net.aquatech.ui.block;

/**
 * Тиры садка: каждый следующий тир на ряд вместительнее, корпус делается из своего материала (тир 1 — дерево).
 * id это имя блока и ресурсов (модель, текстура блока, текстура GUI), число рядов совпадает с tools/build_keepnet_gui.py.
 * Чистый enum без классов Minecraft, чтобы проверять таблицу тестами.
 */
public enum KeepnetTier {
    T1("fish_keepnet", 3),
    T2("fish_keepnet_t2", 4),
    T3("fish_keepnet_t3", 5),
    T4("fish_keepnet_t4", 6),
    T5("fish_keepnet_t5", 7),
    T6("fish_keepnet_t6", 8);

    public static final int COLS = 9;
    /** Высота GUI для трёх рядов, как у механизмов. */
    private static final int BASE_IMAGE_HEIGHT = 212;
    private static final int BASE_ROWS = 3;
    private static final int ROW_HEIGHT = 18;

    private final String id;
    private final int rows;

    KeepnetTier(String id, int rows) {
        this.id = id;
        this.rows = rows;
    }

    public String id() {
        return id;
    }

    public int rows() {
        return rows;
    }

    public int slots() {
        return rows * COLS;
    }

    /** Каждый ряд сверх трёх сдвигает рамку и инвентарь игрока вниз на ряд. */
    public int inventoryShift() {
        return ROW_HEIGHT * (rows - BASE_ROWS);
    }

    public int imageHeight() {
        return BASE_IMAGE_HEIGHT + inventoryShift();
    }
}
