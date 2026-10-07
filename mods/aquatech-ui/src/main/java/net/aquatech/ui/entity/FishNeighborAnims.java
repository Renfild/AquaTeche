package net.aquatech.ui.entity;

/** Ключи анимаций из {@code animations/fish_neighbor.animation.json}; файл выгружает tools/export_fish_neighbor.py. */
public final class FishNeighborAnims {

    private static final String PREFIX = "animation.fish_neighbor.";

    public static final String IDLE = PREFIX + "idle";
    public static final String IDLE_FIDGET = PREFIX + "idle_fidget";
    public static final String REACT = PREFIX + "react";
    public static final String TALK = PREFIX + "talk";

    private FishNeighborAnims() {
    }
}
