package net.aquatech.ui.client.chat;

import net.aquatech.ui.client.ClientUiState;
import net.aquatech.ui.client.render.LumenGfx;
import net.aquatech.ui.client.render.UiDraw;
import net.aquatech.ui.client.theme.LumenTheme;
import net.aquatech.ui.compat.jei.AquaTechJeiPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.aquatech.ui.client.render.AquaFontRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Ultimate AquaChat Screen:
 * 1. Apple Segmented Control for channel switcher with live unread indicators.
 * 2. LoliLand-style Item Tagging (#id / #название) and hand sharing ([#] / [i]) with JEI integration.
 * 3. Smart @Mention Autocomplete popover with player head avatars.
 * 4. Right-Click Context Menu (PM, Copy, Trade, Ignore).
 * 5. Visual Anti-Flood Cooldown Bar for Global and Trade channels.
 */
public final class AquaChatScreen extends Screen {

    private static final List<String> INPUT_HISTORY = new ArrayList<>();
    private static int historyIndex = -1;

    private static final int INPUT_TEXT_Y = 7;
    private static final int INPUT_TEXT_H = 12;

    private static long lastGlobalSendMs = 0;
    private static long lastTradeSendMs = 0;

    private EditBox input;
    private CommandSuggestions commandSuggestions;
    private final String initialText;

    // @Mention Autocomplete state
    private final List<String> matchingMentions = new ArrayList<>();
    private int mentionSelectedIndex = 0;
    private boolean mentionActive = false;
    private int mentionStartIdx = -1;

    // Smart Item Tag Autocomplete state (#item_id / #название)
    private final List<ItemStack> matchingItems = new ArrayList<>();
    private int itemTagSelectedIndex = 0;
    private boolean itemTagActive = false;
    private int itemTagStartIdx = -1;

    // Right-Click Context Menu state
    private boolean contextMenuOpen = false;
    private int contextMenuX = 0;
    private int contextMenuY = 0;
    private AquaChatMessage contextTargetMsg = null;

    public AquaChatScreen(String initialText) {
        super(Component.literal("AquaChat"));
        this.initialText = initialText != null ? initialText : "";
    }

    @Override
    protected void init() {
        super.init();
        AquaChatManager.setChatScreenOpen(true);

        int inputX = AquaChatLayout.inputCapsuleX();
        int inputY = AquaChatLayout.inputY(this.height);
        int inputW = AquaChatLayout.inputCapsuleW();

        this.input = new EditBox(this.font, inputX + AquaChatLayout.INPUT_PAD_L, inputY + 7,
                inputW - AquaChatLayout.INPUT_PAD_L - AquaChatLayout.INPUT_PAD_R, 12, Component.literal("Chat Input")) {
            @Override
            public boolean isMouseOver(double mouseX, double mouseY) {
                int inpY = AquaChatLayout.inputY(AquaChatScreen.this.height);
                int inpX = AquaChatLayout.inputCapsuleX();
                int inpW = AquaChatLayout.inputCapsuleW();
                return this.visible && mouseX >= (double) inpX
                        && mouseX <= (double) (inpX + inpW)
                        && mouseY >= (double) inpY && mouseY <= (double) (inpY + AquaChatLayout.INPUT_H);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                int inpY = AquaChatLayout.inputY(AquaChatScreen.this.height);
                int inpX = AquaChatLayout.inputCapsuleX();
                int inpW = AquaChatLayout.inputCapsuleW();
                if (mouseX >= inpX && mouseX <= inpX + inpW && mouseY >= inpY && mouseY <= inpY + AquaChatLayout.INPUT_H) {
                    return super.mouseClicked(mouseX, this.getY() + 4, button);
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }

            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                if (AquaChatScreen.this.itemTagActive && !AquaChatScreen.this.matchingItems.isEmpty()) {
                    if (keyCode == GLFW.GLFW_KEY_UP) {
                        AquaChatScreen.this.itemTagSelectedIndex = Math.max(0, AquaChatScreen.this.itemTagSelectedIndex - 1);
                        return true;
                    }
                    if (keyCode == GLFW.GLFW_KEY_DOWN) {
                        AquaChatScreen.this.itemTagSelectedIndex = Math.min(
                                AquaChatScreen.this.matchingItems.size() - 1, AquaChatScreen.this.itemTagSelectedIndex + 1);
                        return true;
                    }
                    if (keyCode == GLFW.GLFW_KEY_TAB || keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                        AquaChatScreen.this.applyItemTag(AquaChatScreen.this.matchingItems.get(AquaChatScreen.this.itemTagSelectedIndex));
                        return true;
                    }
                    if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                        AquaChatScreen.this.itemTagActive = false;
                        return true;
                    }
                }

                if (AquaChatScreen.this.mentionActive && !AquaChatScreen.this.matchingMentions.isEmpty()) {
                    if (keyCode == GLFW.GLFW_KEY_UP) {
                        AquaChatScreen.this.mentionSelectedIndex = Math.max(0, AquaChatScreen.this.mentionSelectedIndex - 1);
                        return true;
                    }
                    if (keyCode == GLFW.GLFW_KEY_DOWN) {
                        AquaChatScreen.this.mentionSelectedIndex = Math.min(
                                AquaChatScreen.this.matchingMentions.size() - 1, AquaChatScreen.this.mentionSelectedIndex + 1);
                        return true;
                    }
                    if (keyCode == GLFW.GLFW_KEY_TAB || keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                        AquaChatScreen.this.applyMention(AquaChatScreen.this.matchingMentions.get(AquaChatScreen.this.mentionSelectedIndex));
                        return true;
                    }
                    if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                        AquaChatScreen.this.mentionActive = false;
                        return true;
                    }
                }

                if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                    AquaChatScreen.this.sendMessage();
                    return true;
                }
                if (keyCode == GLFW.GLFW_KEY_UP) {
                    AquaChatScreen.this.navigateHistory(-1);
                    return true;
                }
                if (keyCode == GLFW.GLFW_KEY_DOWN) {
                    AquaChatScreen.this.navigateHistory(1);
                    return true;
                }
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        };

        this.input.setMaxLength(256);
        this.input.setBordered(false);
        this.input.setTextColor(0xFFFFFFFF);
        this.input.setValue(this.initialText);
        this.input.setCanLoseFocus(false);
        this.input.setFocused(true);
        this.input.setFormatter((val, pos) -> FormattedCharSequence.forward(val, net.minecraft.network.chat.Style.EMPTY.withFont(AquaFontRenderer.FONT_MAIN)));

        this.commandSuggestions = new CommandSuggestions(
                this.minecraft, this, this.input, this.font, false, false, 1, 10, true, 0xF207121D
        );
        this.commandSuggestions.updateCommandInfo();

        this.input.setResponder(text -> {
            this.commandSuggestions.setAllowSuggestions(true);
            this.commandSuggestions.updateCommandInfo();
            this.checkMentionAutocomplete(text);
            this.checkItemTagAutocomplete(text);
        });

        this.addWidget(this.input);
        this.setInitialFocus(this.input);
        this.setFocused(this.input);
    }

    private void checkMentionAutocomplete(String text) {
        int cursor = this.input.getCursorPosition();
        if (cursor > 0 && cursor <= text.length()) {
            int atIdx = text.lastIndexOf('@', cursor - 1);
            if (atIdx >= 0 && atIdx < cursor) {
                String query = text.substring(atIdx + 1, cursor);
                if (!query.contains(" ")) {
                    this.matchingMentions.clear();
                    if (this.minecraft != null && this.minecraft.getConnection() != null) {
                        for (var p : this.minecraft.getConnection().getOnlinePlayers()) {
                            String pName = p.getProfile().getName();
                            if (pName.toLowerCase().startsWith(query.toLowerCase())) {
                                this.matchingMentions.add(pName);
                            }
                        }
                    }
                    if (!this.matchingMentions.isEmpty()) {
                        this.mentionActive = true;
                        this.mentionStartIdx = atIdx;
                        this.mentionSelectedIndex = 0;
                        return;
                    }
                }
            }
        }
        this.mentionActive = false;
    }

    private void applyMention(String name) {
        String val = this.input.getValue();
        int cursor = this.input.getCursorPosition();
        if (mentionStartIdx >= 0 && mentionStartIdx <= val.length()) {
            String before = val.substring(0, mentionStartIdx);
            String after = cursor <= val.length() ? val.substring(cursor) : "";
            this.input.setValue(before + "@" + name + " " + after);
            this.input.setCursorPosition(before.length() + name.length() + 2);
            this.mentionActive = false;
            this.matchingMentions.clear();
            if (this.minecraft != null) {
                this.minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                        net.minecraft.sounds.SoundEvents.NOTE_BLOCK_PLING.value(), 1.6F));
            }
        }
    }

    private void checkItemTagAutocomplete(String text) {
        int cursor = this.input.getCursorPosition();
        if (cursor > 0 && cursor <= text.length()) {
            int hashIdx = text.lastIndexOf('#', cursor - 1);
            if (hashIdx >= 0 && hashIdx < cursor) {
                String query = text.substring(hashIdx + 1, cursor);
                int spaces = 0;
                for (int i = 0; i < query.length(); i++) {
                    if (query.charAt(i) == ' ') spaces++;
                }
                if (spaces <= 1) {
                    this.matchingItems.clear();
                    String qLow = query.toLowerCase().trim();
                    int count = 0;
                    if (this.minecraft != null && this.minecraft.player != null) {
                        var inv = this.minecraft.player.getInventory();
                        for (int s = 0; s < inv.getContainerSize(); s++) {
                            ItemStack st = inv.getItem(s);
                            if (!st.isEmpty() && matchesQuery(st, qLow)) {
                                if (!containsItem(this.matchingItems, st)) {
                                    this.matchingItems.add(st.copy());
                                    count++;
                                    if (count >= 6) break;
                                }
                            }
                        }
                    }
                    if (count < 6) {
                        for (Item it : BuiltInRegistries.ITEM) {
                            ItemStack st = new ItemStack(it);
                            if (matchesQuery(st, qLow)) {
                                if (!containsItem(this.matchingItems, st)) {
                                    this.matchingItems.add(st);
                                    count++;
                                    if (count >= 6) break;
                                }
                            }
                        }
                    }
                    if (!this.matchingItems.isEmpty()) {
                        this.itemTagActive = true;
                        this.itemTagStartIdx = hashIdx;
                        this.itemTagSelectedIndex = 0;
                        return;
                    }
                }
            }
        }
        this.itemTagActive = false;
        this.matchingItems.clear();
    }

    private static boolean matchesQuery(ItemStack st, String q) {
        if (q.isEmpty()) return true;
        try {
            ResourceLocation key = BuiltInRegistries.ITEM.getKey(st.getItem());
            String path = key.getPath().toLowerCase();
            String full = key.toString().toLowerCase();
            if (path.contains(q) || full.contains(q)) {
                return true;
            }
            if ((q.equals("work") || q.equals("workbench") || q.equals("верст") || q.equals("верстак"))
                    && path.equals("crafting_table")) {
                return true;
            }
            String disp = st.getHoverName().getString().toLowerCase();
            if (disp.contains(q)) {
                return true;
            }
            if (q.contains(" ")) {
                String[] words = q.split("\\s+");
                boolean allMatch = true;
                for (String w : words) {
                    if (!disp.contains(w) && !path.contains(w)) {
                        allMatch = false;
                        break;
                    }
                }
                if (allMatch) return true;
            }
            return false;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean containsItem(List<ItemStack> list, ItemStack item) {
        for (ItemStack s : list) {
            if (s.is(item.getItem())) return true;
        }
        return false;
    }

    private void applyItemTag(ItemStack stack) {
        String val = this.input.getValue();
        int cursor = this.input.getCursorPosition();
        if (itemTagStartIdx >= 0 && itemTagStartIdx <= val.length()) {
            String before = val.substring(0, itemTagStartIdx);
            String after = cursor <= val.length() ? val.substring(cursor) : "";
            ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
            String tag = "#" + key.toString();
            this.input.setValue(before + tag + " " + after);
            this.input.setCursorPosition(before.length() + tag.length() + 1);
            this.itemTagActive = false;
            this.matchingItems.clear();
            if (this.minecraft != null) {
                this.minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                        net.minecraft.sounds.SoundEvents.ITEM_PICKUP, 1.4F));
            }
        }
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.input != null && this.input.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.contextMenuOpen && keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.contextMenuOpen = false;
            return true;
        }
        if (this.itemTagActive && !this.matchingItems.isEmpty()) {
            if (keyCode == GLFW.GLFW_KEY_UP) {
                this.itemTagSelectedIndex = Math.max(0, this.itemTagSelectedIndex - 1);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_DOWN) {
                this.itemTagSelectedIndex = Math.min(this.matchingItems.size() - 1, this.itemTagSelectedIndex + 1);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_TAB || keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                this.applyItemTag(this.matchingItems.get(this.itemTagSelectedIndex));
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                this.itemTagActive = false;
                return true;
            }
        }
        if (this.mentionActive && !this.matchingMentions.isEmpty()) {
            if (keyCode == GLFW.GLFW_KEY_UP) {
                this.mentionSelectedIndex = Math.max(0, this.mentionSelectedIndex - 1);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_DOWN) {
                this.mentionSelectedIndex = Math.min(this.matchingMentions.size() - 1, this.mentionSelectedIndex + 1);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_TAB || keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                this.applyMention(this.matchingMentions.get(this.mentionSelectedIndex));
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                this.mentionActive = false;
                return true;
            }
        }
        if (this.commandSuggestions != null && this.commandSuggestions.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_TAB) {
            AquaChatManager.cycleChannel();
            if (this.minecraft != null) {
                this.minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                        net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.2F));
            }
            return true;
        }
        if (this.input != null && this.input.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void navigateHistory(int direction) {
        if (INPUT_HISTORY.isEmpty()) return;
        historyIndex = Math.max(-1, Math.min(INPUT_HISTORY.size() - 1, historyIndex + direction));
        if (historyIndex >= 0 && historyIndex < INPUT_HISTORY.size()) {
            this.input.setValue(INPUT_HISTORY.get(historyIndex));
        } else {
            this.input.setValue("");
        }
    }

    public static boolean isHelpCommand(String text) {
        if (text == null || text.isBlank()) return false;
        String t = text.trim().toLowerCase();
        if (t.startsWith("/")) {
            t = t.substring(1).trim();
        }
        return t.equals("help") || t.equals("помощь") || t.equals("хелп")
                || t.equals("команды") || t.equals("команда") || t.equals("commands") || t.equals("?")
                || t.startsWith("help ") || t.startsWith("помощь ")
                || t.startsWith("хелп ") || t.startsWith("commands ");
    }

    private void sendMessage() {
        String text = this.input.getValue().trim();
        if (!text.isEmpty()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                // Intercept help commands (both /help and help) to show clean AquaTech player help
                if (isHelpCommand(text)) {
                    INPUT_HISTORY.remove(text);
                    INPUT_HISTORY.add(text);
                    historyIndex = INPUT_HISTORY.size();
                    this.input.setValue("");
                    AquaChatManager.showPlayerHelp();
                    this.onClose();
                    return;
                }

                // Cooldown Verification for Global, Trade & All Channels (3 seconds)
                AquaChatMessage.Channel channel = AquaChatManager.getActiveChannel();
                long now = System.currentTimeMillis();
                boolean isGlobalOrAll = (channel == AquaChatMessage.Channel.GLOBAL || channel == AquaChatMessage.Channel.ALL);
                if (isGlobalOrAll && now - lastGlobalSendMs < 3000L) {
                    long remaining = (long) Math.ceil((3000L - (now - lastGlobalSendMs)) / 1000.0);
                    mc.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                            net.minecraft.sounds.SoundEvents.NOTE_BLOCK_BASS.value(), 0.8F));
                    AquaChatManager.addMessage(Component.literal("§c[Чат] Подождите еще " + remaining + "с перед отправкой!"));
                    return;
                }
                if (channel == AquaChatMessage.Channel.TRADE && now - lastTradeSendMs < 10000L) {
                    long remaining = (long) Math.ceil((10000L - (now - lastTradeSendMs)) / 1000.0);
                    mc.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                            net.minecraft.sounds.SoundEvents.NOTE_BLOCK_BASS.value(), 0.8F));
                    AquaChatManager.addMessage(Component.literal("§6[Торговля] Подождите еще " + remaining + "с перед отправкой!"));
                    return;
                }

                if (isGlobalOrAll) {
                    lastGlobalSendMs = now;
                } else if (channel == AquaChatMessage.Channel.TRADE) {
                    lastTradeSendMs = now;
                }

                INPUT_HISTORY.remove(text);
                INPUT_HISTORY.add(text);
                historyIndex = INPUT_HISTORY.size();

                if (text.startsWith("/")) {
                    mc.player.connection.sendCommand(text.substring(1));
                    // Intentionally do NOT echo outgoing command as a fake player message
                } else {
                    if (isGlobalOrAll && !text.startsWith("!")) {
                        text = "!" + text;
                    } else if (channel == AquaChatMessage.Channel.TRADE && !text.startsWith("$") && !text.startsWith("[Trade]")) {
                        text = "$" + text;
                    }
                    mc.player.connection.sendChat(text);
                }
            }
        }
        this.onClose();
    }

    private static void echoOutgoingCommand(Minecraft mc, String command) {
        // Intentionally no-op: commands do not appear as fake player chat speech
    }

    private static boolean hidesCommandEcho(String command) {
        return true;
    }

    private static String getChannelPlaceholder(AquaChatMessage.Channel ch) {
        if (ch == null) return "Написать сообщение…";
        return switch (ch) {
            case ALL -> "Сообщение в общий чат…";
            case GLOBAL -> "Сообщение в глобал (!)…";
            case LOCAL -> "Сообщение в локал…";
            case TRADE -> "Предложение на рынок ($)…";
            case PRIVATE -> "Личное сообщение (/msg <ник> <текст>)…";
            case SYSTEM -> "Команда (/help, /spawn, /home)…";
        };
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        AquaChatOverlay.renderOpenPanel(graphics, this.height);
        AquaChatOverlay.renderOpenHistory(graphics, this.font, this.height, mouseX, mouseY);

        // Segmented Control Channel Tabs (Clean, Spacious Capsule Buttons)
        int segX = AquaChatLayout.CONTENT_X;
        int segY = AquaChatLayout.tabY(this.height);
        int segW = AquaChatLayout.CHAT_WIDTH;
        int segH = AquaChatLayout.TAB_H;

        // Subtle frosted track
        LumenGfx.roundedRect(graphics, segX, segY, segW, segH, 5, 0x4408101A);
        LumenGfx.outline(graphics, segX, segY, segW, segH, 5, 0x22384D);

        AquaChatMessage.Channel activeCh = AquaChatManager.getActiveChannel();
        AquaChatMessage.Channel[] channels = AquaChatMessage.Channel.values();
        int tabGap = 2;
        int tabPad = 2;
        int availW = segW - tabPad * 2;
        int tabW = (availW - (channels.length - 1) * tabGap) / channels.length;

        for (int ci = 0; ci < channels.length; ci++) {
            AquaChatMessage.Channel ch = channels[ci];
            boolean active = ch == activeCh;
            int curX = segX + tabPad + ci * (tabW + tabGap);
            int curW = (ci == channels.length - 1) ? (segX + segW - tabPad - curX) : tabW;

            boolean hovered = mouseX >= curX && mouseX <= curX + curW && mouseY >= segY && mouseY <= segY + segH;

            int chCol = ch.getColor();
            if (active) {
                int activeBg = ((chCol & 0x00FFFFFF) | 0x33000000);
                LumenGfx.roundedRect(graphics, curX, segY + 1, curW, segH - 2, 4, activeBg);
                LumenGfx.outline(graphics, curX, segY + 1, curW, segH - 2, 4, chCol);
            } else if (hovered) {
                LumenGfx.roundedRect(graphics, curX, segY + 1, curW, segH - 2, 4, 0x22FFFFFF);
            }

            String chLabel = ch.getLabel();
            int labelW = AquaFontRenderer.width(this.font, chLabel);
            int labelX = curX + (curW - labelW) / 2;
            int labelY = segY + (segH - 8) / 2;
            int textCol = active ? 0xFFFFFFFF : (hovered ? 0xFFF1F5F9 : 0xFF7E8E9F);
            AquaFontRenderer.draw(graphics, this.font, chLabel, labelX, labelY, textCol);

            int unread = AquaChatManager.getUnreadCount(ch);
            if (unread > 0) {
                LumenGfx.roundedRect(graphics, curX + curW - 5, segY + 3, 3, 3, 1.5F, 0xFFEF4444);
            }
        }

        // Clean subtle divider under tabs
        LumenGfx.roundedRect(graphics, segX + 4, segY + segH + 3, segW - 8, 1, 0, 0x1838BDF8);

        // Bottom Dock: [ # ] [ 📋 ] [ Input Capsule with 12/256 & Cooldown ] [ ➤ ]
        int inputY = AquaChatLayout.inputY(this.height);
        int inputH = AquaChatLayout.INPUT_H;

        // 1. Button [ # ] (Quick Item Tag Autocomplete)
        int btnHashX = AquaChatLayout.btnHashX();
        int btnSize = AquaChatLayout.BTN_SIZE;
        boolean hashHov = mouseX >= btnHashX && mouseX <= btnHashX + btnSize && mouseY >= inputY && mouseY <= inputY + inputH;
        LumenGfx.roundedRect(graphics, btnHashX, inputY, btnSize, inputH, 6, hashHov ? 0xDD162232 : 0x880B131F);
        LumenGfx.outline(graphics, btnHashX, inputY, btnSize, inputH, 6, hashHov ? 0xFF00F0FF : 0x3338BDF8);
        int hashW = AquaFontRenderer.headerWidth(this.font, "#");
        AquaFontRenderer.drawHeader(graphics, this.font, "#", btnHashX + (btnSize - hashW) / 2, inputY + 7,
                hashHov ? 0xFF00F0FF : 0xFF38BDF8);

        // 2. Input Capsule
        int capX = AquaChatLayout.inputCapsuleX();
        int capW = AquaChatLayout.inputCapsuleW();
        boolean capFocused = this.input != null && this.input.isFocused();
        LumenGfx.roundedRect(graphics, capX, inputY, capW, inputH, 6, 0xDD070D16);
        LumenGfx.outline(graphics, capX, inputY, capW, inputH, 6, capFocused ? 0x8800F0FF : 0x33384D);

        this.input.render(graphics, mouseX, mouseY, partialTick);

        if (this.input.getValue().isEmpty()) {
            String placeholder = getChannelPlaceholder(activeCh);
            AquaFontRenderer.draw(graphics, this.font, placeholder, capX + AquaChatLayout.INPUT_PAD_L, inputY + 8,
                    0xFF64748B);
        }

        // Character count: "12 / 256"
        String count = this.input.getValue().length() + " / 256";
        int countW = AquaFontRenderer.width(this.font, count);
        int countX = capX + capW - countW - 8;
        AquaFontRenderer.draw(graphics, this.font, count, countX, inputY + 8, 0xFF64748B);

        // Cooldown timer & progress bar
        long now = System.currentTimeMillis();
        long elapsed = now - (activeCh == AquaChatMessage.Channel.GLOBAL ? lastGlobalSendMs : lastTradeSendMs);
        long totalCD = activeCh == AquaChatMessage.Channel.GLOBAL ? 5000 : activeCh == AquaChatMessage.Channel.TRADE ? 15000 : 0;

        if (totalCD > 0 && elapsed < totalCD) {
            float cdPct = (float) (totalCD - elapsed) / totalCD;
            int barW = (int) ((capW - 12) * cdPct);
            LumenGfx.roundedRect(graphics, capX + 6, inputY + inputH - 2, barW, 2, 1, 0xFFF59E0B);

            long remSec = (long) Math.ceil((totalCD - elapsed) / 1000.0);
            String cdStr = remSec + " с";
            int cdW = AquaFontRenderer.width(this.font, cdStr);
            AquaFontRenderer.draw(graphics, this.font, cdStr, countX - cdW - 6, inputY + 8, 0xFFF5C25B);
        }

        // 4. Send Button [ ➤ ]
        int sendX = AquaChatLayout.sendX();
        int sendSize = AquaChatLayout.SEND_SIZE;
        boolean sendHov = mouseX >= sendX && mouseX <= sendX + sendSize
                && mouseY >= inputY && mouseY <= inputY + inputH;
        int sendBg = sendHov ? 0xFF00F0FF : 0x2200F0FF;
        LumenGfx.roundedRect(graphics, sendX, inputY, sendSize, inputH, 6, sendBg);
        LumenGfx.outline(graphics, sendX, inputY, sendSize, inputH, 6, 0xFF00F0FF);
        graphics.drawString(this.font, "➤", sendX + 9, inputY + 8, sendHov ? 0xFF050E17 : 0xFF00F0FF, false);

        // 4b. Floating Pill: Return to bottom / Unread below indicator
        if (AquaChatManager.getScrollOffset() > 0) {
            int unreadBelow = AquaChatManager.getUnreadBelowCount();
            String pillText = unreadBelow > 0 ? ("↓ Новых сообщений: " + unreadBelow) : "↓ К последним сообщениям";
            int pillTextW = AquaFontRenderer.width(this.font, pillText);
            int pillW = pillTextW + 18;
            int pillH = 18;
            int pillX = capX + (capW - pillW) / 2;
            int pillY = inputY - pillH - 4;

            boolean pillHovered = mouseX >= pillX && mouseX <= pillX + pillW && mouseY >= pillY && mouseY <= pillY + pillH;
            int pillBg = pillHovered ? 0xEE162232 : 0xDD080E17;
            int pillBorder = unreadBelow > 0 ? 0xFFF59E0B : (pillHovered ? 0xFF00F0FF : 0x8800F0FF);
            int pillColor = unreadBelow > 0 ? 0xFFFDE68A : 0xFFFFFFFF;

            LumenGfx.roundedRect(graphics, pillX, pillY, pillW, pillH, 9, pillBg);
            LumenGfx.outline(graphics, pillX, pillY, pillW, pillH, 9, pillBorder);
            AquaFontRenderer.draw(graphics, this.font, pillText, pillX + 9, pillY + 5, pillColor);
        }

        // 5. Smart @Mention Autocomplete Popover
        if (this.mentionActive && !this.matchingMentions.isEmpty()) {
            int popCount = Math.min(5, this.matchingMentions.size());
            int popH = popCount * 22 + 6;
            int popW = 160;
            int popX = capX;
            int popY = inputY - popH - 4;

            LumenGfx.roundedRect(graphics, popX, popY, popW, popH, 8, 0xEE08121E);
            LumenGfx.outline(graphics, popX, popY, popW, popH, 8, 0x3338BDF8);

            for (int mi = 0; mi < popCount; mi++) {
                String mName = this.matchingMentions.get(mi);
                int rowY = popY + 3 + mi * 22;
                boolean rowSel = mi == this.mentionSelectedIndex;
                if (rowSel) {
                    LumenGfx.roundedRect(graphics, popX + 3, rowY, popW - 6, 20, 5, 0x2E38BDF8);
                }
                var prof = ClientUiState.profileByName(mName);
                UUID u = prof != null ? prof.uuid() : null;
                UiDraw.drawPlayerHead(graphics, u, mName, popX + 6, rowY + 2, 16);
                int nickCol = prof != null ? LumenTheme.getRankColor(prof.rankId()) : 0xFFFFFFFF;
                graphics.drawString(this.font, mName, popX + 26, rowY + 6, nickCol, false);
            }
        }

        // 5b. Smart Item Autocomplete Popover (#item_id / #название)
        if (this.itemTagActive && !this.matchingItems.isEmpty()) {
            int popCount = Math.min(6, this.matchingItems.size());
            int popH = 22 + popCount * 22;
            int popW = 220;
            int popX = capX;
            int popY = inputY - popH - 4;

            LumenGfx.roundedRect(graphics, popX, popY, popW, popH, 8, 0xF208121E);
            LumenGfx.outline(graphics, popX, popY, popW, popH, 8, 0x4438BDF8);

            graphics.drawString(this.font, "Предметы (#тег)", popX + 8, popY + 6, 0xFF8B9BB0, false);
            LumenGfx.roundedRect(graphics, popX + 6, popY + 17, popW - 12, 1, 0, 0x1AFFFFFF);

            for (int ii = 0; ii < popCount; ii++) {
                ItemStack st = this.matchingItems.get(ii);
                int rowY = popY + 20 + ii * 22;
                boolean rowSel = ii == this.itemTagSelectedIndex;
                if (rowSel) {
                    LumenGfx.roundedRect(graphics, popX + 4, rowY, popW - 8, 20, 5, 0x3338BDF8);
                    LumenGfx.outline(graphics, popX + 4, rowY, popW - 8, 20, 5, 0x8838BDF8);
                }

                graphics.renderItem(st, popX + 6, rowY + 2);

                String disp = fitUi(st.getHoverName().getString(), popW - 32);
                graphics.drawString(this.font, disp, popX + 26, rowY + 3, rowSel ? 0xFFFFFFFF : 0xFFE2E8F0, false);

                ResourceLocation key = BuiltInRegistries.ITEM.getKey(st.getItem());
                String idShort = fitUi("#" + key.toString(), popW - 32);
                graphics.drawString(this.font, idShort, popX + 26, rowY + 11, 0xFF8B9BB0, false);
            }
        }

        // 6. Right-Click Context Menu
        if (this.contextMenuOpen && this.contextTargetMsg != null) {
            int cmW = 140;
            int cmH = 92;
            LumenGfx.roundedRect(graphics, this.contextMenuX, this.contextMenuY, cmW, cmH, 8, 0xF508111D);
            LumenGfx.outline(graphics, this.contextMenuX, this.contextMenuY, cmW, cmH, 8, 0x33FFFFFF);

            String sName = this.contextTargetMsg.getSenderName() != null ? this.contextTargetMsg.getSenderName() : "Игрок";
            graphics.drawString(this.font, sName, this.contextMenuX + 8, this.contextMenuY + 6, 0xFFF1F5F9, false);
            LumenGfx.roundedRect(graphics, this.contextMenuX + 6, this.contextMenuY + 18, cmW - 12, 1, 0, 0x1AFFFFFF);

            String[] cActions = {"✉ Написать в ЛС", "📋 Скопировать", "🚀 Телепорт (/tpa)", "🚫 Заблокировать"};
            for (int ci = 0; ci < cActions.length; ci++) {
                int cItemY = this.contextMenuY + 22 + ci * 16;
                boolean cItemHov = mouseX >= this.contextMenuX && mouseX <= this.contextMenuX + cmW
                        && mouseY >= cItemY && mouseY <= cItemY + 15;
                if (cItemHov) {
                    LumenGfx.roundedRect(graphics, this.contextMenuX + 4, cItemY, cmW - 8, 15, 4, 0x22FFFFFF);
                }
                graphics.drawString(this.font, cActions[ci], this.contextMenuX + 8, cItemY + 3,
                        cItemHov ? 0xFFFFFFFF : 0xFFCBD5E1, false);
            }
        }

        super.render(graphics, mouseX, mouseY, partialTick);

        // 7. Hovered Item Tooltip Render (Native Minecraft Tooltip)
        if (AquaChatOverlay.hoveredItem != null && !AquaChatOverlay.hoveredItem.isEmpty()) {
            graphics.renderComponentTooltip(this.font, this.getTooltipFromItem(this.minecraft, AquaChatOverlay.hoveredItem), (int) mouseX, (int) mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.commandSuggestions != null && this.commandSuggestions.mouseClicked(mouseX, mouseY + 24, button)) {
            return true;
        }

        // Context Menu handling
        if (this.contextMenuOpen) {
            if (mouseX >= this.contextMenuX && mouseX <= this.contextMenuX + 140
                    && mouseY >= this.contextMenuY && mouseY <= this.contextMenuY + 92) {
                int relY = (int) mouseY - (this.contextMenuY + 22);
                if (relY >= 0) {
                    int actionIdx = relY / 16;
                    String sName = this.contextTargetMsg != null ? this.contextTargetMsg.getSenderName() : null;
                    if (sName != null && this.minecraft != null) {
                        if (actionIdx == 0) { // PM
                            this.input.setValue("/msg " + sName + " ");
                            this.input.setCursorPosition(this.input.getValue().length());
                            this.setFocused(this.input);
                        } else if (actionIdx == 1) { // Copy
                            if (this.contextTargetMsg != null && this.contextTargetMsg.getMessageText() != null) {
                                this.minecraft.keyboardHandler.setClipboard(this.contextTargetMsg.getMessageText());
                            }
                        } else if (actionIdx == 2) { // Teleport / TPA
                            if (this.minecraft.player != null) {
                                this.minecraft.player.connection.sendCommand("tpa " + sName);
                            }
                        } else if (actionIdx == 3) { // Ignore
                            if (this.minecraft.player != null) {
                                this.minecraft.player.connection.sendCommand("ignore " + sName);
                            }
                        }
                    }
                }
                this.contextMenuOpen = false;
                return true;
            }
            this.contextMenuOpen = false;
        }

        // Item Tag Autocomplete Click
        if (this.itemTagActive && !this.matchingItems.isEmpty()) {
            int inputY = AquaChatLayout.inputY(this.height);
            int popCount = Math.min(6, this.matchingItems.size());
            int popH = 22 + popCount * 22;
            int popW = 220;
            int popX = AquaChatLayout.CONTENT_X + 28;
            int popY = inputY - popH - 4;

            if (mouseX >= popX && mouseX <= popX + popW && mouseY >= popY + 18 && mouseY <= popY + popH) {
                int clickedIdx = ((int) mouseY - (popY + 20)) / 22;
                if (clickedIdx >= 0 && clickedIdx < popCount) {
                    this.applyItemTag(this.matchingItems.get(clickedIdx));
                    return true;
                }
            }
        }

        // Mention Autocomplete Click
        if (this.mentionActive && !this.matchingMentions.isEmpty()) {
            int inputY = AquaChatLayout.inputY(this.height);
            int popCount = Math.min(5, this.matchingMentions.size());
            int popH = popCount * 22 + 6;
            int popX = AquaChatLayout.CONTENT_X + 28;
            int popY = inputY - popH - 4;

            if (mouseX >= popX && mouseX <= popX + 160 && mouseY >= popY && mouseY <= popY + popH) {
                int clickedIdx = ((int) mouseY - (popY + 3)) / 22;
                if (clickedIdx >= 0 && clickedIdx < popCount) {
                    this.applyMention(this.matchingMentions.get(clickedIdx));
                    return true;
                }
            }
        }

        // Right Click on Message to open Context Menu
        if (button == 1) {
            int bottomY = AquaChatLayout.messageBottom(this.height);
            int currentY = bottomY;
            List<AquaChatMessage> messages = AquaChatManager.getFilteredMessages();
            int scroll = AquaChatManager.getScrollOffset();
            int startIdx = Math.max(0, messages.size() - 1 - scroll);
            int clipTop = AquaChatLayout.messageTop(this.height);

            for (int i = startIdx; i >= 0; i--) {
                AquaChatMessage msg = messages.get(i);
                boolean isGrouped = AquaChatOverlay.isMessageGrouped(messages, i);
                int msgH = AquaChatOverlay.calculateMessageHeight(this.font, msg, isGrouped);
                currentY -= msgH + AquaChatLayout.ROW_GAP;
                if (currentY < clipTop) break;

                if (mouseX >= AquaChatLayout.CONTENT_X && mouseX <= AquaChatLayout.contentRight()
                        && mouseY >= currentY && mouseY <= currentY + msgH) {
                    if (msg.getSenderName() != null && !msg.isSystem()) {
                        this.contextTargetMsg = msg;
                        this.contextMenuOpen = true;
                        this.contextMenuX = Math.min((int) mouseX, this.width - 145);
                        this.contextMenuY = Math.min((int) mouseY, this.height - 100);
                        return true;
                    }
                }
            }
        }

        // Segmented Control Channel Tabs Selection
        int segX = AquaChatLayout.CONTENT_X;
        int segY = AquaChatLayout.tabY(this.height);
        int segW = AquaChatLayout.CHAT_WIDTH;
        int segH = AquaChatLayout.TAB_H;
        if (mouseY >= segY && mouseY <= segY + segH) {
            AquaChatMessage.Channel[] channels = AquaChatMessage.Channel.values();
            int tabGap = 2;
            int tabPad = 2;
            int availW = segW - tabPad * 2;
            int tabW = (availW - (channels.length - 1) * tabGap) / channels.length;
            for (int ci = 0; ci < channels.length; ci++) {
                int curX = segX + tabPad + ci * (tabW + tabGap);
                int curW = (ci == channels.length - 1) ? (segX + segW - tabPad - curX) : tabW;
                if (mouseX >= curX && mouseX <= curX + curW) {
                    AquaChatManager.setActiveChannel(channels[ci]);
                    if (this.minecraft != null) {
                        this.minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                                net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.2F));
                    }
                    return true;
                }
            }
        }

        int inputY = AquaChatLayout.inputY(this.height);
        int inputH = AquaChatLayout.INPUT_H;

        // Floating Pill Click: Return to bottom / Reset Scroll
        if (AquaChatManager.getScrollOffset() > 0) {
            int capX = AquaChatLayout.inputCapsuleX();
            int capW = AquaChatLayout.inputCapsuleW();
            int unreadBelow = AquaChatManager.getUnreadBelowCount();
            String pillText = unreadBelow > 0 ? ("↓ Новых сообщений: " + unreadBelow) : "↓ К последним сообщениям";
            int pillTextW = AquaFontRenderer.width(this.font, pillText);
            int pillW = pillTextW + 18;
            int pillH = 18;
            int pillX = capX + (capW - pillW) / 2;
            int pillY = inputY - pillH - 4;

            if (mouseX >= pillX && mouseX <= pillX + pillW && mouseY >= pillY && mouseY <= pillY + pillH) {
                AquaChatManager.resetScroll();
                if (this.minecraft != null) {
                    this.minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                            net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.3F));
                }
                return true;
            }
        }

        // Button [ # ] (Quick Item Tag Autocomplete)
        int btnHashX = AquaChatLayout.btnHashX();
        int btnSize = AquaChatLayout.BTN_SIZE;
        if (mouseX >= btnHashX && mouseX <= btnHashX + btnSize && mouseY >= inputY && mouseY <= inputY + inputH) {
            String v = this.input.getValue();
            if (!v.isEmpty() && !v.endsWith(" ")) {
                v += " ";
            }
            this.input.setValue(v + "#");
            this.input.setCursorPosition(this.input.getValue().length());
            this.setFocused(this.input);
            checkItemTagAutocomplete(this.input.getValue());
            if (this.minecraft != null) {
                this.minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                        net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.1F));
            }
            return true;
        }


        // Send Button [ ➤ ]
        int sendX = AquaChatLayout.sendX();
        int sendSize = AquaChatLayout.SEND_SIZE;
        if (mouseX >= sendX && mouseX <= sendX + sendSize && mouseY >= inputY && mouseY <= inputY + inputH) {
            sendMessage();
            if (this.minecraft != null) {
                this.minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                        net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
            return true;
        }

        // Left Click on Item in Chat Message -> Open Recipes in JEI!
        if (button == 0 && AquaChatOverlay.hoveredItem != null && !AquaChatOverlay.hoveredItem.isEmpty()) {
            ItemStack stackToOpen = AquaChatOverlay.hoveredItem.copy();
            boolean opened = AquaTechJeiPlugin.showRecipes(stackToOpen);
            if (opened) {
                if (this.minecraft != null) {
                    this.minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                            net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
                }
                AquaChatManager.setChatScreenOpen(false);
                return true;
            }
        }

        // Message left click (quick /msg)
        if (button == 0) {
            int bottomY = AquaChatLayout.messageBottom(this.height);
            int currentY = bottomY;
            List<AquaChatMessage> messages = AquaChatManager.getFilteredMessages();
            int scroll = AquaChatManager.getScrollOffset();
            int startIdx = Math.max(0, messages.size() - 1 - scroll);
            int clipTop = AquaChatLayout.messageTop(this.height);

            for (int i = startIdx; i >= 0; i--) {
                AquaChatMessage msg = messages.get(i);
                boolean isGrouped = AquaChatOverlay.isMessageGrouped(messages, i);
                int msgH = AquaChatOverlay.calculateMessageHeight(this.font, msg, isGrouped);
                currentY -= msgH + AquaChatLayout.ROW_GAP;
                if (currentY < clipTop) break;

                boolean inCard = mouseX >= AquaChatLayout.CONTENT_X && mouseX <= AquaChatLayout.contentRight();
                boolean inHeader = mouseY >= currentY
                        && mouseY <= currentY + AquaChatLayout.ROW_PAD + AquaChatLayout.NAME_H + 2;
                if (inCard && inHeader) {
                    if (msg.getSenderName() != null && !msg.isSystem()) {
                        this.input.setValue("/msg " + msg.getSenderName() + " ");
                        this.input.setCursorPosition(this.input.getValue().length());
                        this.setFocused(this.input);
                        return true;
                    }
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void handleAction(String action) {
        Minecraft mc = Minecraft.getInstance();
        if (action.equals("Вставить")) {
            String clip = mc.keyboardHandler.getClipboard();
            if (clip != null && !clip.isEmpty()) {
                this.input.insertText(clip);
            }
        } else if (action.equals("Вырезать")) {
            String val = this.input.getValue();
            if (!val.isEmpty()) {
                mc.keyboardHandler.setClipboard(val);
                this.input.setValue("");
            }
        } else if (action.equals("Очистить")) {
            this.input.setValue("");
        }
    }

    private String fitUi(String s, int maxW) {
        if (s == null) {
            return "";
        }
        if (this.font.width(s) <= maxW) {
            return s;
        }
        String ell = "...";
        int ew = this.font.width(ell);
        if (maxW <= ew) {
            return ell;
        }
        int n = s.length();
        while (n > 0 && this.font.width(s.substring(0, n)) + ew > maxW) {
            n--;
        }
        return n <= 0 ? ell : s.substring(0, n) + ell;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.commandSuggestions != null && this.commandSuggestions.mouseScrolled(delta)) {
            return true;
        }
        AquaChatManager.scroll((int) delta * 2);
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        AquaChatManager.setChatScreenOpen(false);
        super.onClose();
    }
}
