package net.aquatech.ui.client.chat;

import net.aquatech.ui.client.render.AquaFontRenderer;
import net.aquatech.ui.client.render.LumenGfx;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public final class AquaChatScreen extends Screen {

    private static final List<String> INPUT_HISTORY = new ArrayList<>();
    private static int historyIndex = -1;

    /** Paste / cut / clear stay on the left; Market is the only primary jump. Copy is Ctrl+C. */
    private static final String[] TOOL_ACTIONS = {"Вставить", "Вырезать", "Очистить", "Рынок (F4)"};
    private static final int MARGIN = 10;
    private static final int INPUT_H = 20;
    private static final int INPUT_PAD_L = 14;
    private static final int INPUT_PAD_R = 74;
    private static final int INPUT_TEXT_Y = 4;
    private static final int INPUT_TEXT_H = 12;
    private static final int TOOL_H = 18;

    private EditBox input;
    private CommandSuggestions commandSuggestions;
    private final String initialText;

    public AquaChatScreen(String initialText) {
        super(Component.literal("AquaChat"));
        this.initialText = initialText != null ? initialText : "";
    }

    @Override
    protected void init() {
        super.init();
        AquaChatManager.setChatScreenOpen(true);

        int inputX = MARGIN;
        int inputY = this.height - 25;
        int inputW = AquaChatOverlay.CHAT_WIDTH + 8;

        this.input = new EditBox(this.font, inputX + INPUT_PAD_L, inputY + INPUT_TEXT_Y,
                inputW - INPUT_PAD_L - INPUT_PAD_R, INPUT_TEXT_H, Component.literal("Chat Input")) {
            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
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

        this.commandSuggestions = new CommandSuggestions(
                this.minecraft, this, this.input, this.font, false, false, 1, 10, true, 0xF207121D
        );
        this.commandSuggestions.updateCommandInfo();

        this.input.setResponder(text -> {
            this.commandSuggestions.setAllowSuggestions(true);
            this.commandSuggestions.updateCommandInfo();
        });

        this.addWidget(this.input);
        this.setInitialFocus(this.input);
        this.setFocused(this.input);
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
        if (this.commandSuggestions != null && this.commandSuggestions.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_TAB) {
            AquaChatManager.cycleChannel();
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

    private void sendMessage() {
        String text = this.input.getValue().trim();
        if (!text.isEmpty()) {
            INPUT_HISTORY.remove(text);
            INPUT_HISTORY.add(text);
            historyIndex = INPUT_HISTORY.size();

            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                if (text.startsWith("/")) {
                    mc.player.connection.sendCommand(text.substring(1));
                    echoOutgoingCommand(mc, text);
                } else {
                    AquaChatMessage.Channel channel = AquaChatManager.getActiveChannel();
                    if (channel == AquaChatMessage.Channel.GLOBAL && !text.startsWith("!")) {
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

    /** Local echo so /hub and other commands show in AquaChat (server never broadcasts them). */
    private static void echoOutgoingCommand(Minecraft mc, String command) {
        if (mc.player == null || hidesCommandEcho(command)) {
            return;
        }
        String name = mc.player.getName().getString();
        AquaChatManager.addMessage(Component.literal("<" + name + "> " + command));
    }

    private static boolean hidesCommandEcho(String command) {
        String root = command.startsWith("/") ? command.substring(1) : command;
        int space = root.indexOf(' ');
        if (space >= 0) {
            root = root.substring(0, space);
        }
        root = root.toLowerCase();
        return root.equals("login") || root.equals("l") || root.equals("register")
                || root.equals("changepassword") || root.equals("password") || root.equals("auth");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Header sits on its own strip so it never collides with toolbar controls.
        int headerY = this.height - AquaChatOverlay.HEADER_INSET;
        int brandX = MARGIN;
        AquaFontRenderer.draw(graphics, this.font, "Aqua", brandX, headerY, 0xFFFFFFFF);
        int aquaW = AquaFontRenderer.width(this.font, "Aqua");
        AquaFontRenderer.draw(graphics, this.font, "Tech", brandX + aquaW, headerY, 0xFF2FE0C0);
        AquaFontRenderer.draw(graphics, this.font, " · чат",
                brandX + AquaFontRenderer.width(this.font, "AquaTech"), headerY, 0xFF81ECEC);

        // 1. Channel Selector Tabs (Elevated above chat box with high readability)
        int tabX = MARGIN;
        int tabY = this.height - AquaChatOverlay.TAB_INSET;
        AquaChatMessage.Channel[] channels = AquaChatMessage.Channel.values();

        for (AquaChatMessage.Channel ch : channels) {
            boolean active = ch == AquaChatManager.getActiveChannel();
            String label = switch (ch) {
                case ALL -> "Все";
                case GLOBAL -> "Глобал";
                case LOCAL -> "Локал";
                case TRADE -> "Торговля";
                case PRIVATE -> "ЛС";
                case SYSTEM -> "Система";
            };

            int w = AquaFontRenderer.width(this.font, label) + 14;
            int h = 17;

            boolean hovered = mouseX >= tabX && mouseX <= tabX + w && mouseY >= tabY && mouseY <= tabY + h;

            int chColor = ch.getColor();
            // Active = filled pill in channel color with dark text (shape + fill,
            // not color alone); inactive = ghost outline.
            int bgCol = active ? (0xF0000000 | chColor) : (hovered ? 0xDD162638 : 0xAA0A131D);
            int textCol = active ? 0xFF04121A : (hovered ? 0xFFFFFFFF : 0xFF9FB2C4);
            int borderCol = active ? (0xCCFFFFFF) : (hovered ? 0x88FFFFFF : 0x22FFFFFF);

            LumenGfx.roundedRect(graphics, tabX, tabY, w, h, 4, bgCol);
            LumenGfx.outline(graphics, tabX, tabY, w, h, 4, borderCol);
            AquaFontRenderer.draw(graphics, this.font, label, tabX + 7, tabY + (h - 9) / 2, textCol);

            tabX += w + 4;
        }

        // 2. Toolbar: edit actions grouped left, Market separated as the jump-out control.
        int toolY = this.height - 43;
        int btnX = MARGIN;
        for (String action : TOOL_ACTIONS) {
            boolean isMarket = action.contains("Рынок");
            if (isMarket) {
                btnX += 8;
            }
            int bw = toolbarButtonWidth(action);
            boolean bhov = mouseX >= btnX && mouseX <= btnX + bw && mouseY >= toolY && mouseY <= toolY + TOOL_H;

            int bbg = isMarket ? (bhov ? 0xEE2A1E08 : 0xBB1E1608) : (bhov ? 0xEE162638 : 0x990C1622);
            int btc = isMarket ? (bhov ? 0xFFFDE047 : 0xFFF59E0B) : (bhov ? 0xFFFFFFFF : 0xFF9FB2C4);
            int bbd = isMarket ? (bhov ? 0xFFF59E0B : 0x88F59E0B) : (bhov ? 0x662FE0C0 : 0x22FFFFFF);

            LumenGfx.roundedRect(graphics, btnX, toolY, bw, TOOL_H, 4, bbg);
            LumenGfx.outline(graphics, btnX, toolY, bw, TOOL_H, 4, bbd);
            AquaFontRenderer.draw(graphics, this.font, action, btnX + 6, toolY + (TOOL_H - 9) / 2, btc);

            btnX += bw + 4;
        }

        // 3. Input Box Bar Background & Active Channel Border (Centered height 20px)
        int inputX = MARGIN;
        int inputY = this.height - 25;
        int inputW = AquaChatOverlay.CHAT_WIDTH + 8;
        int inputH = INPUT_H;
        int activeColor = AquaChatManager.getActiveChannel().getColor();

        LumenGfx.roundedRect(graphics, inputX, inputY, inputW, inputH, 5.5F, 0xF5081018);
        LumenGfx.outline(graphics, inputX, inputY, inputW, inputH, 5.5F, 0xCC000000 | (activeColor & 0x00FFFFFF));

        // 4. Input Text Field
        this.input.render(graphics, mouseX, mouseY, partialTick);

        // 5. Character counter (Vertically centered)
        String count = this.input.getValue().length() + " / 256";
        int countW = AquaFontRenderer.width(this.font, count);
        AquaFontRenderer.draw(graphics, this.font, count, inputX + inputW - countW - 20, inputY + 6, 0xFF8FA3B5);

        // Send button: filled pill in channel color (clear affordance), glyph centered
        int sendX = inputX + inputW - 16;
        boolean sendHov = mouseX >= sendX - 1 && mouseX <= sendX + 13 && mouseY >= inputY + 2 && mouseY <= inputY + 18;
        LumenGfx.roundedRect(graphics, sendX - 1, inputY + 2, 14, 16, 4,
                sendHov ? 0xFFF5F7FA : (0xE6000000 | activeColor));
        LumenGfx.outline(graphics, sendX - 1, inputY + 2, 14, 16, 4, sendHov ? 0xFF2FE0C0 : 0x66FFFFFF);
        AquaFontRenderer.draw(graphics, this.font, "»", sendX + 3, inputY + 6, sendHov ? 0xFF04121A : 0xFFFFFFFF);

        // 6. Command Suggestions render (Elevated above input bar with -24px offset)
        if (this.commandSuggestions != null) {
            graphics.pose().pushPose();
            graphics.pose().translate(0, -24, 0);
            this.commandSuggestions.render(graphics, mouseX, mouseY + 24);
            graphics.pose().popPose();
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.commandSuggestions != null && this.commandSuggestions.mouseClicked(mouseX, mouseY + 24, button)) {
            return true;
        }

        // 1. Channel tabs
        int tabX = MARGIN;
        int tabY = this.height - AquaChatOverlay.TAB_INSET;
        AquaChatMessage.Channel[] channels = AquaChatMessage.Channel.values();

        for (AquaChatMessage.Channel ch : channels) {
            String label = switch (ch) {
                case ALL -> "Все";
                case GLOBAL -> "Глобал";
                case LOCAL -> "Локал";
                case TRADE -> "Торговля";
                case PRIVATE -> "ЛС";
                case SYSTEM -> "Система";
            };
            int w = AquaFontRenderer.width(this.font, label) + 14;
            int h = 17;

            if (mouseX >= tabX && mouseX <= tabX + w && mouseY >= tabY && mouseY <= tabY + h) {
                AquaChatManager.setActiveChannel(ch);
                return true;
            }
            tabX += w + 4;
        }

        // 2. Action buttons
        int toolY = this.height - 43;
        int btnX = MARGIN;
        for (String action : TOOL_ACTIONS) {
            boolean isMarket = action.contains("Рынок");
            if (isMarket) {
                btnX += 8;
            }
            int bw = toolbarButtonWidth(action);

            if (mouseX >= btnX && mouseX <= btnX + bw && mouseY >= toolY && mouseY <= toolY + TOOL_H) {
                handleAction(action);
                return true;
            }
            btnX += bw + 4;
        }

        // 3. Send arrow click
        int inputX = MARGIN;
        int inputY = this.height - 25;
        int inputW = AquaChatOverlay.CHAT_WIDTH + 8;
        if (mouseX >= inputX + inputW - 20 && mouseX <= inputX + inputW && mouseY >= inputY && mouseY <= inputY + 20) {
            sendMessage();
            return true;
        }

        // 4. Quick whisper /msg <Nick> on sender-name click (header row only, not whole card)
        int bottomY = this.height - AquaChatOverlay.OPEN_BOTTOM_GAP;
        int currentY = bottomY;
        List<AquaChatMessage> messages = AquaChatManager.getFilteredMessages();
        int scroll = AquaChatManager.getScrollOffset();
        int startIdx = Math.max(0, messages.size() - 1 - scroll);

        for (int i = startIdx; i >= 0; i--) {
            AquaChatMessage msg = messages.get(i);
            int msgH = AquaChatOverlay.calculateMessageHeight(this.font, msg);
            currentY -= msgH + 3;
            if (currentY < bottomY - 175) break;

            // Only the header strip (top 14px of the card) triggers /msg
            boolean inCard = mouseX >= 8 && mouseX <= 8 + AquaChatOverlay.CHAT_WIDTH;
            boolean inHeader = mouseY >= currentY && mouseY <= currentY + 14;
            if (inCard && inHeader) {
                if (msg.getSenderName() != null && !msg.isSystem()) {
                    this.input.setValue("/msg " + msg.getSenderName() + " ");
                    this.input.setCursorPosition(this.input.getValue().length());
                    this.setFocused(this.input);
                    return true;
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
        } else if (action.contains("Рынок") || action.contains("F4")) {
            this.onClose();
            if (mc.player != null) {
                try {
                    Class<?> client = Class.forName("store.aquateche.aqualumen.client.LumenClient");
                    client.getMethod("openScreen", String.class).invoke(null, "fishing");
                } catch (Throwable t) {
                    if (mc.player.connection != null) {
                        mc.player.connection.sendCommand("shop");
                    }
                }
            }
        }
    }

    private int toolbarButtonWidth(String action) {
        return AquaFontRenderer.width(this.font, action) + 12;
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
