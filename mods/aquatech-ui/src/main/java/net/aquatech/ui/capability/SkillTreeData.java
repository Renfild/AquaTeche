package net.aquatech.ui.capability;

import java.util.Set;

/**
 * Server-authoritative skill tree helpers. Delegates to {@link SkillDefinitions}.
 */
public final class SkillTreeData {

    public static final String ORIGIN_ID = "origin";

    private SkillTreeData() {
    }

    public static boolean isKnownSkill(String skillId) {
        return SkillDefinitions.isKnown(skillId);
    }

    public static boolean canUnlock(String skillId, Set<String> unlocked) {
        return SkillDefinitions.canUnlock(skillId, unlocked);
    }

    public static int costOf(String skillId) {
        return SkillDefinitions.costOf(skillId);
    }
}
