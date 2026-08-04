package com.casesmod.data;

import java.util.ArrayList;
import java.util.List;

public class KitDefinition {
    public String id = "starter";
    public String displayName = "§aСтартовый набор";
    public String iconItemId = "minecraft:chest_minecart";
    /** Кулдаун между получениями кита, в секундах. 0 = без ограничений. */
    public long cooldownSeconds = 86400;
    /** Требуемое право (пусто = доступно всем) */
    public String permission = "";
    public List<KitItem> items = new ArrayList<>();

    public static class KitItem {
        public String itemId = "minecraft:bread";
        public int count = 1;
    }
}
