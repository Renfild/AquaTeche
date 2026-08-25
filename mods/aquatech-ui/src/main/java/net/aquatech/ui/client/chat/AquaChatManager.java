package net.aquatech.ui.client.chat;

import net.aquatech.ui.client.cache.ResourceCacheManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AquaChatManager {

    private static final int MAX_HISTORY = 250;
    private static final List<AquaChatMessage> MESSAGES = new ArrayList<>();
    private static AquaChatMessage.Channel activeChannel = AquaChatMessage.Channel.ALL;
    private static int scrollOffset = 0;
    private static int chatScreenOpenTick = 0;
    private static boolean chatScreenOpen = false;

    private AquaChatManager() {
    }

    public static synchronized void addMessage(Component component) {
        if (component == null) return;
        int currentTick = Minecraft.getInstance().gui.getGuiTicks();
        AquaChatMessage msg = AquaChatMessage.parse(component, currentTick);
        addMessage(msg);
    }

    public static synchronized void addMessage(AquaChatMessage message) {
        if (message == null) return;
        MESSAGES.add(message);
        if (MESSAGES.size() > MAX_HISTORY) {
            MESSAGES.remove(0);
        }
        if (message.getSenderUuid() != null) {
            ResourceCacheManager.getInstance().prefetchPlayerAvatar(message.getSenderUuid());
        }
    }

    public static synchronized List<AquaChatMessage> getMessages() {
        return Collections.unmodifiableList(new ArrayList<>(MESSAGES));
    }

    public static synchronized List<AquaChatMessage> getFilteredMessages() {
        if (activeChannel == AquaChatMessage.Channel.ALL) {
            return getMessages();
        }
        List<AquaChatMessage> filtered = new ArrayList<>();
        for (AquaChatMessage msg : MESSAGES) {
            if (msg.getChannel() == activeChannel || msg.isSystem()) {
                filtered.add(msg);
            }
        }
        return filtered;
    }

    public static AquaChatMessage.Channel getActiveChannel() {
        return activeChannel;
    }

    public static void setActiveChannel(AquaChatMessage.Channel channel) {
        activeChannel = channel != null ? channel : AquaChatMessage.Channel.ALL;
        scrollOffset = 0;
    }

    public static void cycleChannel() {
        AquaChatMessage.Channel[] channels = AquaChatMessage.Channel.values();
        int next = (activeChannel.ordinal() + 1) % channels.length;
        setActiveChannel(channels[next]);
    }

    public static int getScrollOffset() {
        return scrollOffset;
    }

    public static void scroll(int delta) {
        // Clamp so the view can't scroll above the oldest message
        int max = Math.max(0, getFilteredMessages().size() - 1);
        scrollOffset = Math.max(0, Math.min(max, scrollOffset + delta));
    }

    public static void resetScroll() {
        scrollOffset = 0;
    }

    public static boolean isChatScreenOpen() {
        return chatScreenOpen;
    }

    public static void setChatScreenOpen(boolean open) {
        chatScreenOpen = open;
        if (open) {
            chatScreenOpenTick = Minecraft.getInstance().gui.getGuiTicks();
        } else {
            resetScroll();
        }
    }

    public static int getChatScreenOpenTick() {
        return chatScreenOpenTick;
    }

    public static void clear() {
        MESSAGES.clear();
        scrollOffset = 0;
    }
}
