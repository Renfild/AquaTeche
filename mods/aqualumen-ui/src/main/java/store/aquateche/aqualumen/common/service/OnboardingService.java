package store.aquateche.aqualumen.common.service;

import net.minecraft.server.level.ServerPlayer;
import store.aquateche.aqualumen.common.ServerEvents;
import store.aquateche.aqualumen.network.LumenNetwork;
import store.aquateche.aqualumen.network.LumenPackets;

/** Pushes the new-player guide state to clients that can draw it; the KubeJS onboarding script drives it. */
public final class OnboardingService {

    private OnboardingService() {
    }

    /** True when this client has a mod new enough to draw the guide panel. */
    public static boolean canShow(ServerPlayer player) {
        return ServerEvents.hasClientMod(player) && ClientVersion.atLeast(ServerEvents.clientVersion(player), 0, 3, 72);
    }

    /**
     * @param step     current step 1..5, 6 = finished, 0 = hide
     * @param have     progress inside the step (fish caught for step 1)
     * @param doneStep step just completed, or 0
     * @param reward   reward text of the completed step, colour codes allowed
     * @return false when the client cannot draw the panel (the caller falls back to the action bar)
     */
    public static boolean send(ServerPlayer player, int step, int have, int doneStep, String reward) {
        if (!canShow(player)) {
            return false;
        }
        LumenNetwork.toPlayer(player, new LumenPackets.OnboardingSync(step, have, doneStep, OnboardingSteps.plain(reward)));
        return true;
    }
}
