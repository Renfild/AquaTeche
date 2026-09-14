package net.aquatech.ui.capability;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * Древо талантов УДАЛЕНО навсегда (решение владельца, 2026-09-15).
 * Пустой реестр: дерево не существует и не будет. Не восстанавливать.
 */
public final class SkillDefinitions {

    public enum NodeType { SMALL, NOTABLE, KEYSTONE }

    public enum SkillCategory { WATER_RESIST, SWIM_SPEED, FISHING, MACHINES }

    public record SkillDef(
            String id,
            NodeType type,
            int cost,
            String prereq,
            SkillCategory category,
            float value,
            String title,
            String effectText
    ) {
    }

    private SkillDefinitions() {
    }

    public static SkillDef get(String id) {
        return null;
    }

    public static boolean isKnown(String id) {
        return false;
    }

    public static Collection<SkillDef> all() {
        return Collections.emptyList();
    }

    public static int size() {
        return 0;
    }

    public static int costOf(String id) {
        return 0;
    }

    public static boolean canUnlock(String skillId, Set<String> unlocked) {
        return false;
    }
}
