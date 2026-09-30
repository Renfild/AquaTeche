package net.aquatech.ui.client.chat;

import java.lang.reflect.Method;

/**
 * Asks aqualumen whether its web chat owns the chat UI. No compile-time dependency between the
 * mods, so this goes through reflection; without aqualumen the native AquaChat stays in charge.
 */
public final class WebChatGate {

    private static Method enabled;
    private static boolean resolved;

    private WebChatGate() {
    }

    public static boolean active() {
        if (!resolved) {
            resolved = true;
            try {
                enabled = Class.forName("store.aquateche.aqualumen.client.chat.ChatWebOverlay").getMethod("enabled");
            } catch (ReflectiveOperationException | LinkageError ignored) {
                enabled = null;
            }
        }
        if (enabled == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(enabled.invoke(null));
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }
}
