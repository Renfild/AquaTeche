package net.aquatech.ui.client.chat;

import net.aquatech.ui.client.cache.ResourceCacheManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class AquaChatManager {

    private static final int MAX_HISTORY = 250;
    private static final List<AquaChatMessage> MESSAGES = new ArrayList<>();
    private static AquaChatMessage.Channel activeChannel = AquaChatMessage.Channel.ALL;
    private static int scrollOffset = 0;
    private static int unreadBelowCount = 0;
    private static int chatScreenOpenTick = 0;
    private static boolean chatScreenOpen = false;

    private static final Map<AquaChatMessage.Channel, Integer> UNREAD_COUNTS = new java.util.concurrent.ConcurrentHashMap<>();

    private AquaChatManager() {
    }

    public static synchronized void addMessage(Component component) {
        if (component == null) return;
        int currentTick = Minecraft.getInstance().gui.getGuiTicks();
        AquaChatMessage msg = AquaChatMessage.parse(component, currentTick);
        if (msg != null) {
            addMessage(msg);
        }
    }

    private static int suppressedQuestErrors = 0;

    public static synchronized void addSystemMessage(Component component) {
        // Mohist/Paper deliver player chat as system packets. Parse like player chat
        // so ranks/heads work; parse() still falls through to a system row when needed.
        String plain = component == null ? "" : component.getString();
        if (plain.contains("Unable to open Quest GUI")) {
            // FTB Quests spam: three red error lines on every /quest open without
            // server-side quest data. Show a single dimmed notice instead.
            suppressedQuestErrors++;
            if (suppressedQuestErrors == 1) {
                addMessage(Component.literal(
                        "§7FTB Quests: книга квестов недоступна на этом сервере — дальнейшие ошибки скрыты."));
            }
            return;
        }
        addMessage(component);
    }

    public static synchronized void addSystemMessage(String text) {
        if (text == null || text.isBlank()) return;
        addSystemMessage(Component.literal(text));
    }

    public static synchronized void showPlayerHelp() {
        int currentTick = Minecraft.getInstance().gui.getGuiTicks();
        String helpText = String.join("\n",
                "§b⚡ Доступные команды сервера AquaTech:",
                "§e/spawn §7— Телепортация на спавн сервера",
                "§e/sethome [имя] §7— Установить точку дома",
                "§e/home [имя] §7— Телепортация на точку дома",
                "§e/delhome [имя] §7— Удалить точку дома",
                "§e/tpa <ник> §7— Отправить запрос телепортации к игроку",
                "§e/tpaccept §7/ §e/tpdeny §7— Принять / отклонить телепортацию",
                "§e/warp [название] §7— Список варпов / телепортация",
                "§e/ah §7— Рынок и аукцион предметов",
                "§e/kit §7— Меню доступных наборов предметов",
                "§e/money §7— Проверить баланс монет",
                "§e/pay <ник> <сумма> §7— Перевести монеты игроку",
                "§e/msg <ник> <текст> §7/ §e/r §7— Личные сообщения игрокам",
                "§6Клавиша F4 §7— Интерактивное меню (Рынок, Квесты, Магазин)"
        );
        Component comp = Component.literal(helpText);
        AquaChatMessage msg = new AquaChatMessage(null, null, "system", "СИСТЕМА",
                0xFF00B0FF, AquaChatMessage.Channel.SYSTEM, helpText, comp, currentTick, true);
        addMessage(msg);
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.getSoundManager() != null) {
            mc.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                    net.minecraft.sounds.SoundEvents.UI_TOAST_IN, 1.2F));
        }
    }

    public static synchronized void addMessage(AquaChatMessage message) {
        if (message == null) return;
        if (message.getMessageText() == null || message.getMessageText().isBlank()) return;
        MESSAGES.add(message);
        if (MESSAGES.size() > MAX_HISTORY) {
            MESSAGES.remove(0);
        }
        if (message.getSenderUuid() != null) {
            ResourceCacheManager.getInstance().prefetchPlayerAvatar(message.getSenderUuid());
            // Trigger in-world 3D speech bubble over player's head
            if (message.getMessageText() != null && !message.getMessageText().isBlank()) {
                net.aquatech.ui.client.bubble.ChatBubbleManager.addBubble(
                        message.getSenderUuid(), message.getMessageText(), 140);
            }
        }
        // Track unread badge for other channels
        if (message.getChannel() != activeChannel && !message.isSystem()) {
            UNREAD_COUNTS.put(message.getChannel(), UNREAD_COUNTS.getOrDefault(message.getChannel(), 0) + 1);
        }

        // Apple HIG Stability Principle: if player is reading scrolled-up history, freeze viewport and track new incoming count
        if (chatScreenOpen && scrollOffset > 0) {
            boolean visibleInActive;
            if (activeChannel == AquaChatMessage.Channel.ALL) {
                visibleInActive = true;
            } else if (activeChannel == AquaChatMessage.Channel.SYSTEM) {
                visibleInActive = (message.getChannel() == AquaChatMessage.Channel.SYSTEM || message.isSystem());
            } else {
                visibleInActive = (message.getChannel() == activeChannel && !message.isSystem());
            }
            if (visibleInActive) {
                scrollOffset++;
                unreadBelowCount++;
            }
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
            if (activeChannel == AquaChatMessage.Channel.SYSTEM) {
                if (msg.getChannel() == AquaChatMessage.Channel.SYSTEM || msg.isSystem()) {
                    filtered.add(msg);
                }
            } else {
                if (msg.getChannel() == activeChannel && !msg.isSystem()) {
                    filtered.add(msg);
                }
            }
        }
        return filtered;
    }

    public static AquaChatMessage.Channel getActiveChannel() {
        return activeChannel;
    }

    public static void setActiveChannel(AquaChatMessage.Channel channel) {
        activeChannel = channel != null ? channel : AquaChatMessage.Channel.ALL;
        UNREAD_COUNTS.put(activeChannel, 0);
        scrollOffset = 0;
        unreadBelowCount = 0;
    }

    public static int getUnreadCount(AquaChatMessage.Channel channel) {
        if (channel == null || channel == activeChannel) return 0;
        return UNREAD_COUNTS.getOrDefault(channel, 0);
    }

    public static void cycleChannel() {
        AquaChatMessage.Channel[] channels = AquaChatMessage.Channel.values();
        int next = (activeChannel.ordinal() + 1) % channels.length;
        setActiveChannel(channels[next]);
    }

    public static int getScrollOffset() {
        return scrollOffset;
    }

    public static int getUnreadBelowCount() {
        return scrollOffset > 0 ? unreadBelowCount : 0;
    }

    public static void scroll(int delta) {
        // Clamp so the view can't scroll above the oldest message
        int max = Math.max(0, getFilteredMessages().size() - 1);
        scrollOffset = Math.max(0, Math.min(max, scrollOffset + delta));
        if (scrollOffset == 0) {
            unreadBelowCount = 0;
        }
    }

    public static void resetScroll() {
        scrollOffset = 0;
        unreadBelowCount = 0;
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
        unreadBelowCount = 0;
    }
}
