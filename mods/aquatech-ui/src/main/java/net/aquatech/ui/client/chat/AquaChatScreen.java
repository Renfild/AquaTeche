package net.aquatech.ui.client.chat;

import com.mojang.blaze3d.platform.InputConstants;
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

        int inputX = 10;
        int inputY = this.height - 25;
        int inputW = AquaChatOverlay.CHAT_WIDTH + 8;
        int inputH = 20;

        this.input = new EditBox(this.font, inputX + 8, inputY + 6, inputW - 75, 9, Component.literal("Chat Input")) {
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

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 1. Channel Selector Tabs (Elevated above chat box with high readability)
        int tabX = 10;
        int tabY = this.height - 250;
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
            int h = 15;

            boolean hovered = mouseX >= tabX && mouseX <= tabX + w && mouseY >= tabY && mouseY <= tabY + h;

            int chColor = ch.getColor();
            int bgCol = active ? 0xEE0A1826 : (hovered ? 0xDD152638 : 0xAA0A131D);
            int textCol = active ? chColor : (hovered ? 0xFFFFFFFF : 0xFF8FA3B5);
            int borderCol = active ? chColor : (hovered ? 0x88FFFFFF : 0x22FFFFFF);

            LumenGfx.roundedRect(graphics, tabX, tabY, w, h, 3.5F, bgCol);
            LumenGfx.outline(graphics, tabX, tabY, w, h, 3.5F, borderCol);
            AquaFontRenderer.draw(graphics, this.font, label, tabX + 7, tabY + 3, textCol);

            // Active indicator bar
            if (active) {
                LumenGfx.roundedRect(graphics, tabX + 4, tabY + h - 2, w - 8, 2, 1, chColor);
            }

            tabX += w + 4;
        }

        // 2. Toolbar Action Buttons (Вставить, Копировать, Вырезать, Очистить, Рынок)
        int toolY = this.height - 43;
        int btnX = 10;
        String[] actions = {"Вставить", "Копировать", "Вырезать", "Очистить", "Рынок (F4)"};

        for (String action : actions) {
            int bw = AquaFontRenderer.width(this.font, action) + 8;
            int bh = 13;
            boolean isMarket = action.contains("Рынок");
            boolean bhov = mouseX >= btnX && mouseX <= btnX + bw && mouseY >= toolY && mouseY <= toolY + bh;

            int bbg = isMarket ? (bhov ? 0xEE2A1E08 : 0xBB1E1608) : (bhov ? 0xEE162638 : 0x990C1622);
            int btc = isMarket ? (bhov ? 0xFFFDE047 : 0xFFF59E0B) : (bhov ? 0xFFFFFFFF : 0xFF9FB2C2);
            int bbd = isMarket ? (bhov ? 0xFFF59E0B : 0x88F59E0B) : (bhov ? 0x662FE0C0 : 0x22FFFFFF);

            LumenGfx.roundedRect(graphics, btnX, toolY, bw, bh, 3, bbg);
            LumenGfx.outline(graphics, btnX, toolY, bw, bh, 3, bbd);
            AquaFontRenderer.draw(graphics, this.font, action, btnX + 4, toolY + 2, btc);

            btnX += bw + 4;
        }

        // Right title: AquaTech OceanChat
        int logoX = 10 + AquaChatOverlay.CHAT_WIDTH - 85;
        AquaFontRenderer.draw(graphics, this.font, "Aqua", logoX, toolY + 2, 0xFFFFFFFF);
        AquaFontRenderer.draw(graphics, this.font, "Tech", logoX + AquaFontRenderer.width(this.font, "Aqua"), toolY + 2, 0xFF2FE0C0);
        AquaFontRenderer.draw(graphics, this.font, " • Chat", logoX + AquaFontRenderer.width(this.font, "AquaTech"), toolY + 2, 0xFF81ECEC);

        // 3. Input Box Bar Background & Active Channel Border (Centered height 20px)
        int inputX = 10;
        int inputY = this.height - 25;
        int inputW = AquaChatOverlay.CHAT_WIDTH + 8;
        int inputH = 20;
        int activeColor = AquaChatManager.getActiveChannel().getColor();

        LumenGfx.roundedRect(graphics, inputX, inputY, inputW, inputH, 5.5F, 0xF5081018);
        LumenGfx.outline(graphics, inputX, inputY, inputW, inputH, 5.5F, 0xCC000000 | (activeColor & 0x00FFFFFF));

        // 4. Input Text Field
        this.input.render(graphics, mouseX, mouseY, partialTick);

        // 5. Character counter (Vertically centered)
        String count = this.input.getValue().length() + " / 256";
        int countW = AquaFontRenderer.width(this.font, count);
        AquaFontRenderer.draw(graphics, this.font, count, inputX + inputW - countW - 20, inputY + 6, 0xFF657B8C);

        // Send arrow button »
        int sendX = inputX + inputW - 14;
        AquaFontRenderer.draw(graphics, this.font, "»", sendX, inputY + 5, activeColor);

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
        int tabX = 10;
        int tabY = this.height - 250;
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
            int h = 15;

            if (mouseX >= tabX && mouseX <= tabX + w && mouseY >= tabY && mouseY <= tabY + h) {
                AquaChatManager.setActiveChannel(ch);
                return true;
            }
            tabX += w + 4;
        }

        // 2. Action buttons
        int toolY = this.height - 43;
        int btnX = 10;
        String[] actions = {"Вставить", "Копировать", "Вырезать", "Очистить", "Рынок (F4)"};

        for (String action : actions) {
            int bw = AquaFontRenderer.width(this.font, action) + 8;
            int bh = 13;

            if (mouseX >= btnX && mouseX <= btnX + bw && mouseY >= toolY && mouseY <= toolY + bh) {
                handleAction(action);
                return true;
            }
            btnX += bw + 4;
        }

        // 3. Send arrow click
        int inputX = 10;
        int inputY = this.height - 25;
        int inputW = AquaChatOverlay.CHAT_WIDTH + 8;
        if (mouseX >= inputX + inputW - 20 && mouseX <= inputX + inputW && mouseY >= inputY && mouseY <= inputY + 20) {
            sendMessage();
            return true;
        }

        // 4. Quick whisper /msg <Nick> on message click
        int bottomY = this.height - 48;
        int currentY = bottomY;
        List<AquaChatMessage> messages = AquaChatManager.getFilteredMessages();
        int scroll = AquaChatManager.getScrollOffset();
        int startIdx = Math.max(0, messages.size() - 1 - scroll);

        for (int i = startIdx; i >= 0; i--) {
            AquaChatMessage msg = messages.get(i);
            int msgH = AquaChatOverlay.calculateMessageHeight(this.font, msg);
            currentY -= msgH + 3;
            if (currentY < bottomY - 175) break;

            if (mouseX >= 8 && mouseX <= 8 + AquaChatOverlay.CHAT_WIDTH && mouseY >= currentY && mouseY <= currentY + msgH) {
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
        } else if (action.equals("Копировать")) {
            String val = this.input.getValue();
            if (!val.isEmpty()) {
                mc.keyboardHandler.setClipboard(val);
            } else {
                List<AquaChatMessage> msgs = AquaChatManager.getMessages();
                if (!msgs.isEmpty()) {
                    mc.keyboardHandler.setClipboard(msgs.get(msgs.size() - 1).getMessageText());
                }
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
