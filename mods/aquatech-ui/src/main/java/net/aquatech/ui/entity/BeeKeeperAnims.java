package net.aquatech.ui.entity;

/** Ключи анимаций из {@code animations/bee_keeper.animation.json}; файл собирает tools/build_bee_keeper_model.py. */
public final class BeeKeeperAnims {

    private static final String PREFIX = "animation.bee_keeper.";

    public static final String IDLE = PREFIX + "idle";
    public static final String IDLE_FIDGET = PREFIX + "idle_fidget";
    public static final String THANKS = PREFIX + "thanks";
    public static final String FACE_IDLE = PREFIX + "face_idle";
    public static final String FACE_HAPPY = PREFIX + "face_happy";

    private BeeKeeperAnims() {
    }
}
