package net.aquatech.ui.client.chat;

import net.aquatech.ui.client.render.AquaFontRenderer;
import net.aquatech.ui.client.render.LumenGfx;
import net.aquatech.ui.client.render.UiDraw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class AquaChatOverlay {

    public static final int CHAT_WIDTH = AquaChatLayout.CHAT_WIDTH;
    public static final int PANEL_TOP_INSET = AquaChatLayout.PANEL_TOP_INSET;
    public static final int HEADER_INSET = AquaChatLayout.HEADER_INSET;
    public static final int TAB_INSET = AquaChatLayout.TAB_INSET;
    private static final int FADE_START_TICK = 200; // 10 seconds
    private static final int FADE_DURATION = 50;   // 2.5 seconds

    private AquaChatOverlay() {
    }

    public static void render(GuiGraphics graphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options == null) return;
        if (mc.options.hideGui) return;

        boolean chatOpen = AquaChatManager.isChatScreenOpen() || mc.screen instanceof AquaChatScreen;
        // Open chat is one Screen surface (panel + history + input). HUD overlay
        // would paint the panel on top of the input and clip the field out of the frame.
        if (chatOpen) {
            return;
        }

        List<AquaChatMessage> messages = AquaChatManager.getMessages();
        if (messages.isEmpty()) return;

        int screenHeight = mc.getWindow().getGuiScaledHeight();
        renderHistory(graphics, mc.font, messages, screenHeight, false);
    }

    public static void renderOpenPanel(GuiGraphics graphics, int screenHeight) {
        int panelTop = AquaChatLayout.panelTop(screenHeight);
        LumenGfx.gradientRounded(graphics, AquaChatLayout.PANEL_X, panelTop,
                AquaChatLayout.PANEL_W, AquaChatLayout.panelH(screenHeight), AquaChatLayout.PANEL_RADIUS,
                0xE807111C, 0xF00A1624);
        LumenGfx.outline(graphics, AquaChatLayout.PANEL_X, panelTop,
                AquaChatLayout.PANEL_W, AquaChatLayout.panelH(screenHeight), AquaChatLayout.PANEL_RADIUS, 0x332FE0C0);
    }

    public static void renderOpenHistory(GuiGraphics graphics, Font font, int screenHeight) {
        renderHistory(graphics, font, AquaChatManager.getFilteredMessages(), screenHeight, true);
    }

    private static void renderHistory(GuiGraphics graphics, Font font, List<AquaChatMessage> messages,
                                      int screenHeight, boolean chatOpen) {
        if (messages.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        int currentTick = mc.gui.getGuiTicks();
        int chatX = AquaChatLayout.CONTENT_X;
        int bottomY = chatOpen ? AquaChatLayout.messageBottom(screenHeight)
                : screenHeight - AquaChatLayout.CLOSED_BOTTOM_GAP;
        int clipTop = chatOpen ? AquaChatLayout.messageTop(screenHeight) : 0;
        int scroll = AquaChatManager.getScrollOffset();

        int totalMessages = messages.size();
        int endIndex = Math.max(0, totalMessages - scroll);
        int startIndex = Math.max(0, endIndex - (chatOpen ? 18 : 8));

        int currentY = bottomY;

        for (int i = endIndex - 1; i >= startIndex; i--) {
            AquaChatMessage msg = messages.get(i);
            int age = currentTick - msg.getCreationTick();

            float alpha = 1.0F;
            if (!chatOpen) {
                if (age > FADE_START_TICK + FADE_DURATION) {
                    continue;
                } else if (age > FADE_START_TICK) {
                    alpha = 1.0F - (float) (age - FADE_START_TICK) / (float) FADE_DURATION;
                }
            }

            if (alpha <= 0.02F) continue;

            int msgH = calculateMessageHeight(font, msg);
            currentY -= msgH + 3;

            if (chatOpen && currentY < clipTop) {
                break;
            }

            renderAquaMessage(graphics, font, msg, chatX, currentY, msgH, alpha, chatOpen);
        }
    }

    public static int calculateMessageHeight(Font font, AquaChatMessage msg) {
        String body = visibleBody(msg);
        // Vanilla font: custom TTF has no Cyrillic, so split/width must match draw.
        int lineCount = Math.max(1, font.split(Component.literal(body), CHAT_WIDTH - 40).size());
        return 16 + (lineCount * 12) + 4;
    }

    private static String visibleBody(AquaChatMessage msg) {
        String parsed = msg.getMessageText() != null ? msg.getMessageText() : "";
        if (msg.getChannel() == AquaChatMessage.Channel.PRIVATE && !parsed.isBlank()) {
            return parsed;
        }
        String src = parsed;
        if (msg.getOriginalComponent() != null) {
            String raw = msg.getOriginalComponent().getString();
            if (raw != null && !raw.isBlank()) {
                src = raw;
            }
        }
        String body = AquaChatMessage.stripChatBody(src, msg.getSenderName(), msg.getRankDisplay());
        if (body == null || body.isBlank()) {
            body = parsed;
        }
        return body != null ? body : "";
    }

    private static void renderAquaMessage(GuiGraphics graphics, Font font, AquaChatMessage msg,
                                          int x, int y, int height, float alpha, boolean chatOpen) {
        // Detect player mention (@Nick or whole-word Nick in message body)
        Minecraft mc = Minecraft.getInstance();
        boolean isMention = isMention(mc, msg);

        // Play notification sound once per mention (client-side, only for fresh messages)
        if (isMention && !msg.isMentionSoundPlayed() && !chatOpen) {
            msg.markMentionSoundPlayed();
            mc.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                    net.minecraft.sounds.SoundEvents.NOTE_BLOCK_PLING.value(), 1.4F));
        }

        // Message card plate for crisp contrast and clear separation
        if (isMention) {
            // Golden glow mention highlight
            LumenGfx.gradientRounded(graphics, x - 2, y - 1, CHAT_WIDTH + 4, height + 2, 4,
                    applyAlpha(0x4AF59E0B, alpha), applyAlpha(0x22F59E0B, alpha));
            LumenGfx.outline(graphics, x - 2, y - 1, CHAT_WIDTH + 4, height + 2, 4, applyAlpha(0xDDF59E0B, alpha));
        } else if (!chatOpen) {
            // Plate fades slower than text so contrast never drops below readable
            int plateAlpha = Math.max(150, (int) (alpha * 190.0F));
            int bgCol = (Math.min(190, plateAlpha) << 24) | 0x060D17;
            LumenGfx.roundedRect(graphics, x - 2, y - 1, CHAT_WIDTH + 4, height + 2, 4, bgCol);
        } else {
            // Subtle card separator in chat screen
            LumenGfx.roundedRect(graphics, x - 2, y - 1, CHAT_WIDTH + 4, height + 2, 4, applyAlpha(0x220E1C2B, alpha));
        }

        int curX = x + 3;

        if (msg.isSystem()) {
            // System Announcement Header
            int badgeW = AquaFontRenderer.width(font, "SERVER") + 6;
            LumenGfx.roundedRect(graphics, curX, y + 1, badgeW, 10, 2.5F, applyAlpha(0xFF2FE0C0, 0.9F * alpha));
            AquaFontRenderer.draw(graphics, font, "SERVER", curX + 3, y + 2, applyAlpha(0xFF04121A, alpha));
            curX += badgeW + 3;

            int nameW = AquaFontRenderer.width(font, "AQUATECH") + 6;
            LumenGfx.roundedRect(graphics, curX, y + 1, nameW, 10, 2.5F, applyAlpha(0xFFF59E0B, 0.25F * alpha));
            LumenGfx.outline(graphics, curX, y + 1, nameW, 10, 2.5F, applyAlpha(0xFFF59E0B, 0.8F * alpha));
            AquaFontRenderer.draw(graphics, font, "AQUATECH", curX + 3, y + 2, applyAlpha(0xFFFBBF24, alpha));

            // Timestamp on right
            if (msg.getTimeFormatted() != null) {
                int timeW = AquaFontRenderer.width(font, msg.getTimeFormatted());
                AquaFontRenderer.draw(graphics, font, msg.getTimeFormatted(), x + CHAT_WIDTH - timeW - 4, y + 2, applyAlpha(0x9E9DB2C4, alpha));
            }

            // System Message Lines (Pure white)
            List<FormattedCharSequence> lines = font.split(Component.literal(msg.getMessageText()), CHAT_WIDTH - 24);
            int textY = y + 16;
            for (FormattedCharSequence line : lines) {
                graphics.drawString(font, line, x + 6, textY, applyAlpha(0xFFFFFFFF, alpha), true);
                textY += 10;
            }
            return;
        }

        // 1. Channel Badge (Pill with vibrant channel color)
        String chTag = msg.getChannel().getTag();
        int chCol = msg.getChannel().getColor();
        int chW = Math.max(12, AquaFontRenderer.width(font, chTag) + 6);
        int chH = 11;
        LumenGfx.roundedRect(graphics, curX, y + 1, chW, chH, 2.5F, applyAlpha(chCol, alpha));
        int tagX = curX + (chW - AquaFontRenderer.width(font, chTag)) / 2;
        AquaFontRenderer.draw(graphics, font, chTag, tagX, y + 2, applyAlpha(0xFFFFFFFF, alpha));
        curX += chW + 4;

        // 2. Player Avatar
        int headSize = 12;
        int headX = curX;
        UiDraw.drawPlayerHead(graphics, msg.getSenderUuid(), msg.getSenderName(), headX, y + 1, headSize);
        curX += headSize + 5;

        // 3. Role / Rank Badge (Lumen capsule with individual rank color)
        if (msg.getRankDisplay() != null && !msg.getRankDisplay().isBlank()) {
            String rank = msg.getRankDisplay().toUpperCase();
            int rankColor = msg.getRankColor();
            int rankW = AquaFontRenderer.width(font, rank) + 6;
            int rankH = 10;
            int rankY = y + 2;

            LumenGfx.roundedRect(graphics, curX, rankY, rankW, rankH, 2.5F, applyAlpha(0xCC111924, alpha));
            LumenGfx.outline(graphics, curX, rankY, rankW, rankH, 2.5F, applyAlpha(rankColor, 0.85F * alpha));
            AquaFontRenderer.draw(graphics, font, rank, curX + 3, rankY + 1, applyAlpha(rankColor, alpha));
            curX += rankW + 4;
        }

        // 4. Sender Name (truncated before the timestamp — no collisions)
        if (msg.getSenderName() != null) {
            int nameMax = x + CHAT_WIDTH - timeSafeW(font, msg) - 8 - curX;
            String name = AquaFontRenderer.fit(font, msg.getSenderName(), Math.max(24, nameMax));
            AquaFontRenderer.draw(graphics, font, name, curX, y + 3, applyAlpha(0xFFFFFFFF, alpha));
        }

        // Timestamp on right
        if (msg.getTimeFormatted() != null) {
            int timeW = AquaFontRenderer.width(font, msg.getTimeFormatted());
            AquaFontRenderer.draw(graphics, font, msg.getTimeFormatted(), x + CHAT_WIDTH - timeW - 4, y + 3, applyAlpha(0x9E9DB2C4, alpha));
        }

        String body = visibleBody(msg);
        int bodyX = headX;
        int maxW = CHAT_WIDTH - (bodyX - x) - 8;
        List<FormattedCharSequence> bodyLines = font.split(Component.literal(body), Math.max(8, maxW));
        int bodyY = y + 16;
        int bodyColor = applyAlpha(0xFFFFFFFF, alpha);
        for (FormattedCharSequence line : bodyLines) {
            graphics.drawString(font, line, bodyX, bodyY, bodyColor, false);
            bodyY += 12;
        }
    }

    /**
     * Strict whole-word mention detection: matches "@Nick" anywhere or "Nick"
     * only when surrounded by non-word characters (space, start/end, punctuation).
     * Prevents false positives from substrings (e.g. "Max" inside "максимум").
     */
    private static boolean isMention(Minecraft mc, AquaChatMessage msg) {
        if (mc.player == null || msg.isSystem() || msg.getMessageText() == null) return false;
        String myName = mc.player.getName().getString();
        if (msg.getSenderName() != null && myName.equalsIgnoreCase(msg.getSenderName())) return false;
        String text = msg.getMessageText().toLowerCase();
        String name = myName.toLowerCase();
        if (name.isEmpty()) return false;
        int idx = 0;
        while ((idx = text.indexOf(name, idx)) >= 0) {
            int end = idx + name.length();
            boolean leftOk = idx == 0 || !isWordChar(text.charAt(idx - 1)) || text.charAt(idx - 1) == '@';
            boolean rightOk = end >= text.length() || !isWordChar(text.charAt(end));
            if (leftOk && rightOk) return true;
            idx = end;
        }
        return false;
    }

    private static boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private static int timeSafeW(Font font, AquaChatMessage msg) {
        return msg.getTimeFormatted() != null ? AquaFontRenderer.width(font, msg.getTimeFormatted()) : 0;
    }

    private static int applyAlpha(int color, float alpha) {
        int a = (int) (((color >> 24) & 0xFF) * Math.max(0.0F, Math.min(1.0F, alpha)));
        if (a == 0 && alpha > 0.01F) {
            a = (int) (alpha * 255.0F);
        }
        return (a << 24) | (color & 0x00FFFFFF);
    }
}
