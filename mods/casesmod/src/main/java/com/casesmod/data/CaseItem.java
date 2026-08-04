package com.casesmod.data;

/**
 * Один возможный приз внутри кейса.
 * itemId       — регистри-имя предмета ("minecraft:diamond", "minecraft:netherite_ingot" и т.д.)
 * count        — количество предмета
 * weight       — вес выпадения (чем больше, тем чаще выпадает приз)
 * rarity       — COMMON, UNCOMMON, RARE, EPIC, LEGENDARY (влияет на цвет и анимацию)
 * displayName  — подпись приза в интерфейсе
 * command      — необязательная консольная команда при выигрыше (например выдача ранга/кита),
 *                поддерживает плейсхолдер %player%
 */
public class CaseItem {
    public String itemId = "minecraft:diamond";
    public int count = 1;
    public double weight = 1.0;
    public String rarity = "COMMON";
    public String displayName = "";
    public String command = "";

    public Rarity rarityEnum() {
        try { return Rarity.valueOf(rarity.toUpperCase()); }
        catch (Exception e) { return Rarity.COMMON; }
    }

    public enum Rarity {
        COMMON(0xAAAAAA), UNCOMMON(0x55FF55), RARE(0x55AAFF), EPIC(0xAA00FF), LEGENDARY(0xFFAA00);
        public final int color;
        Rarity(int color) { this.color = color; }
    }
}
