package net.aquatech.ui.client.bubble;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ChatBubbleManager {
    private static final Map<UUID, Bubble> BUBBLES = new ConcurrentHashMap<>();

    private ChatBubbleManager() {
    }

    public static void addBubble(UUID sender, String message, int durationTicks) {
        String safeMessage = message == null ? "" : message.strip();
        if (safeMessage.isEmpty()) {
            return;
        }
        if (safeMessage.length() > 64) {
            safeMessage = safeMessage.substring(0, 61) + "...";
        }
        BUBBLES.put(sender, new Bubble(safeMessage, Math.max(1, durationTicks)));
    }

    public static void tick() {
        for (Map.Entry<UUID, Bubble> entry : BUBBLES.entrySet()) {
            Bubble bubble = entry.getValue();
            if (--bubble.ticksRemaining <= 0) {
                BUBBLES.remove(entry.getKey(), bubble);
            }
        }
    }

    public static String messageFor(UUID playerId) {
        Bubble bubble = BUBBLES.get(playerId);
        return bubble == null ? null : bubble.message;
    }

    private static final class Bubble {
        private final String message;
        private int ticksRemaining;

        private Bubble(String message, int ticksRemaining) {
            this.message = message;
            this.ticksRemaining = ticksRemaining;
        }
    }
}
