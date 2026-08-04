package net.aquatech.ui.client;

import net.aquatech.ui.client.gui.OceanGuideBookScreen;
import net.aquatech.ui.client.gui.OceanSkillTreeScreen;
import net.aquatech.ui.client.hud.RhythmHookOverlay;
import net.aquatech.ui.capability.AquaSkillCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ClientItemActions {
    private ClientItemActions() {
    }

    public static void openGuideBook() {
        Minecraft.getInstance().setScreen(new OceanGuideBookScreen());
    }

    public static boolean hasShiftDown() {
        return Screen.hasShiftDown();
    }

    public static void startRhythmHook(int seed, int fishHp, float spotSize, float yellowPad,
                                       float pointerSpeed, float decay, boolean elite, boolean treasure) {
        RhythmHookOverlay.start(seed, fishHp, spotSize, yellowPad, pointerSpeed, decay, elite, treasure);
    }

    public static void applySyncedSkills(CompoundTag tag) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || tag == null) {
            return;
        }
        mc.player.getCapability(AquaSkillCapability.INSTANCE).ifPresent(cap -> {
            cap.deserializeNBT(tag);
            ClientUiState.bumpSkillSyncGeneration();
            if (mc.screen instanceof OceanSkillTreeScreen screen) {
                screen.refreshData();
            }
        });
    }
}
