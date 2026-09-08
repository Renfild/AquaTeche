package net.aquatech.ui.client.chat;

import com.mojang.blaze3d.systems.RenderSystem;
import net.aquatech.ui.client.render.AquaFontRenderer;
import net.aquatech.ui.client.render.LumenGfx;
import net.aquatech.ui.client.render.UiDraw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Locale;
import net.aquatech.ui.client.theme.LumenTheme;

public final class AquaChatOverlay {

    private static final ResourceLocation COIN_TEXTURE = new ResourceLocation("aquatech_ui", "textures/gui/coin.png");
    public static ItemStack hoveredItem = ItemStack.EMPTY;

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
        int panelW = AquaChatLayout.PANEL_W;
        int panelH = AquaChatLayout.panelH(screenHeight);

        // Cyber-MMO Tactical Glass Panel
        LumenGfx.gradientRounded(graphics, AquaChatLayout.PANEL_X, panelTop,
                panelW, panelH, AquaChatLayout.PANEL_RADIUS,
                0xD808101A, 0xEB040810);
        // Outer soft cyan halo
        LumenGfx.outline(graphics, AquaChatLayout.PANEL_X - 1, panelTop - 1,
                panelW + 2, panelH + 2, AquaChatLayout.PANEL_RADIUS + 1, 0x1A00F0FF);
        // Crisp high-tech border
        LumenGfx.outline(graphics, AquaChatLayout.PANEL_X, panelTop,
                panelW, panelH, AquaChatLayout.PANEL_RADIUS, 0x3338BDF8);
        // Top edge light highlight
        LumenGfx.roundedRect(graphics, AquaChatLayout.PANEL_X + 12, panelTop + 1,
                panelW - 24, 1, 0, 0x2EFFFFFF);
    }

    public static void renderOpenHistory(GuiGraphics graphics, Font font, int screenHeight) {
        renderOpenHistory(graphics, font, screenHeight, -1, -1);
    }

    public static void renderOpenHistory(GuiGraphics graphics, Font font, int screenHeight, int mouseX, int mouseY) {
        hoveredItem = ItemStack.EMPTY;
        renderHistory(graphics, font, AquaChatManager.getFilteredMessages(), screenHeight, true, mouseX, mouseY);
    }

    private static void renderHistory(GuiGraphics graphics, Font font, List<AquaChatMessage> messages,
                                      int screenHeight, boolean chatOpen) {
        renderHistory(graphics, font, messages, screenHeight, chatOpen, -1, -1);
    }

    private static void renderHistory(GuiGraphics graphics, Font font, List<AquaChatMessage> messages,
                                      int screenHeight, boolean chatOpen, int mouseX, int mouseY) {
        if (chatOpen) {
            hoveredItem = ItemStack.EMPTY;
        }
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
        int startIndex = Math.max(0, endIndex - (chatOpen ? 20 : 8));

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

            // Group consecutive system lines under the same header
            boolean isGrouped = isMessageGrouped(messages, i);

            int msgH = calculateMessageHeight(font, msg, isGrouped);
            currentY -= msgH + AquaChatLayout.ROW_GAP;

            if (chatOpen && currentY < clipTop) {
                break;
            }

            // Smooth Slide-In Animation for fresh messages (200ms ease-out)
            long ageMs = System.currentTimeMillis() - msg.getCreationTimestamp();
            int animY = currentY;
            float animAlpha = alpha;
            if (ageMs < 200) {
                float p = ageMs / 200.0F;
                float ease = p * (2.0F - p);
                animY += (int) (6.0F * (1.0F - ease));
                animAlpha *= Math.max(0.15F, ease);
            }

            renderAquaMessage(graphics, font, msg, chatX, animY, msgH, animAlpha, chatOpen, isGrouped, mouseX, mouseY);
        }
    }

    public static boolean isMessageGrouped(List<AquaChatMessage> messages, int index) {
        return false;
    }

    public static int calculateMessageHeight(Font font, AquaChatMessage msg, boolean isGrouped) {
        if (msg == null) return 0;
        Component formatted = msg.getFormattedComponent();
        if (formatted == null || formatted.getString().isBlank()) return 0;
        int wrapW = wrapWidth();
        int lines = Math.max(1, font.split(formatted, wrapW).size());
        int padY = 4;
        int contentH = 12 + lines * AquaChatLayout.LINE;
        return padY * 2 + Math.max(AquaChatLayout.HEAD_SIZE, contentH);
    }

    public static int wrapWidth() {
        int padX = 8;
        int indent = padX + AquaChatLayout.HEAD_SIZE + AquaChatLayout.HEAD_GAP;
        return Math.max(16, CHAT_WIDTH - indent - padX);
    }

    public static int calculateMessageHeight(Font font, AquaChatMessage msg) {
        return calculateMessageHeight(font, msg, false);
    }

    private static void drawBodyLine(GuiGraphics graphics, Font font, FormattedCharSequence line,
                                     String lineStr, int x, int y, int color, float alpha) {
        int coin = lineStr.indexOf('\u00A4');
        if (coin < 0) {
            graphics.drawString(font, line, x, y, color, false);
            return;
        }
        int cx = x;
        int from = 0;
        int size = Math.max(8, font.lineHeight - 2);
        while (from < lineStr.length()) {
            int at = lineStr.indexOf('\u00A4', from);
            if (at < 0) {
                graphics.drawString(font, lineStr.substring(from), cx, y, color, false);
                break;
            }
            if (at > from) {
                String chunk = lineStr.substring(from, at);
                graphics.drawString(font, chunk, cx, y, color, false);
                cx += font.width(chunk);
            }
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, Math.max(0.0F, Math.min(1.0F, alpha)));
            graphics.blit(COIN_TEXTURE, cx, y - 1, size, size, 0, 0, 32, 32, 32, 32);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            cx += size + 1;
            from = at + 1;
        }
    }

    public static String lineToString(FormattedCharSequence seq) {
        if (seq == null) return "";
        StringBuilder sb = new StringBuilder();
        seq.accept((index, style, cp) -> {
            sb.appendCodePoint(cp);
            return true;
        });
        return sb.toString();
    }

    private static void renderAquaMessage(GuiGraphics graphics, Font font, AquaChatMessage msg,
                                          int x, int y, int height, float alpha, boolean chatOpen,
                                          boolean isGrouped, int mouseX, int mouseY) {
        if (msg == null) return;
        Component formatted = msg.getFormattedComponent();
        if (formatted == null || formatted.getString().isBlank()) return;

        Minecraft mc = Minecraft.getInstance();
        boolean isMentioned = isMention(mc, msg);

        if (isMentioned && !msg.isMentionSoundPlayed() && !chatOpen) {
            msg.markMentionSoundPlayed();
            mc.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                    net.minecraft.sounds.SoundEvents.NOTE_BLOCK_PLING.value(), 1.4F));
        }

        int cardW = CHAT_WIDTH;
        int cardH = height;
        boolean isHovered = chatOpen && mouseX >= x && mouseX <= x + cardW && mouseY >= y && mouseY <= y + cardH;

        // Minimalist nanobanano aesthetic: calm, clean, uncluttered
        if (chatOpen) {
            if (isMentioned) {
                LumenGfx.roundedRect(graphics, x, y, cardW, cardH, 5, applyAlpha(0x28F59E0B, alpha));
                LumenGfx.roundedRect(graphics, x + 1, y + 2, 3, cardH - 4, 1.5F, applyAlpha(0xFFF59E0B, alpha));
            } else if (msg.isSystem()) {
                LumenGfx.roundedRect(graphics, x, y, cardW, cardH, 5, applyAlpha(0x20061622, alpha));
                LumenGfx.roundedRect(graphics, x + 1, y + 2, 3, cardH - 4, 1.5F, applyAlpha(0xFF38BDF8, alpha));
            } else if (isHovered) {
                LumenGfx.roundedRect(graphics, x, y, cardW, cardH, 5, applyAlpha(0x1F38BDF8, alpha));
                LumenGfx.outline(graphics, x, y, cardW, cardH, 5, applyAlpha(0x3538BDF8, alpha));
            } else {
                LumenGfx.roundedRect(graphics, x, y, cardW, cardH, 5, applyAlpha(0x1408101A, alpha));
            }
        } else {
            // Closed HUD during gameplay: soft transparent pill that does not obscure gameplay
            int hudBg = isMentioned ? 0x902A1A06 : (msg.isSystem() ? 0x80061420 : 0x75050B12);
            LumenGfx.roundedRect(graphics, x, y, cardW, cardH, 5, applyAlpha(hudBg, alpha));
            if (isMentioned) {
                LumenGfx.roundedRect(graphics, x + 1, y + 2, 2, cardH - 4, 1.0F, applyAlpha(0xFFF59E0B, alpha));
            } else if (msg.isSystem()) {
                LumenGfx.roundedRect(graphics, x + 1, y + 2, 2, cardH - 4, 1.0F, applyAlpha(0xFF38BDF8, alpha));
            }
        }

        int padX = 8;
        int padY = 4;
        int headX = x + padX;
        int headY = y + padY;
        int headSize = AquaChatLayout.HEAD_SIZE;
        int textX = headX + headSize + AquaChatLayout.HEAD_GAP;
        int wrapW = wrapWidth();
        int nameY = headY + 1;

        // 1. Avatar & Author Header
        if (msg.isSystem()) {
            // Minimalist nanobanano accent: vertical glowing indicator, no broken glyph box
            LumenGfx.roundedRect(graphics, headX + 4, headY + 2, 3, headSize - 4, 1.5F, applyAlpha(0xFF38BDF8, alpha));
            LumenGfx.roundedRect(graphics, headX + 3, headY + (headSize - 6) / 2.0F, 5, 5, 2.5F, applyAlpha(0xFF00F0FF, alpha));

            int curX = textX;
            if (msg.getTimeFormatted() != null) {
                AquaFontRenderer.draw(graphics, font, msg.getTimeFormatted(), curX, nameY, applyAlpha(0xFF64748B, alpha));
                curX += AquaFontRenderer.width(font, msg.getTimeFormatted()) + 5;
            }

            String srvBadge = "СЕРВЕР";
            int srvW = AquaFontRenderer.width(font, srvBadge) + 8;
            LumenGfx.roundedRect(graphics, curX, nameY - 1, srvW, 11, 3, applyAlpha(0x2800F0FF, alpha));
            LumenGfx.outline(graphics, curX, nameY - 1, srvW, 11, 3, applyAlpha(0x5500F0FF, alpha));
            AquaFontRenderer.draw(graphics, font, srvBadge, curX + 4, nameY, applyAlpha(0xFF38BDF8, alpha));
            curX += srvW + 5;
        } else {
            UiDraw.drawPlayerHead(graphics, msg.getSenderUuid(), msg.getSenderName(), headX, headY, headSize, false);

            int curX = textX;
            if (msg.getTimeFormatted() != null) {
                AquaFontRenderer.draw(graphics, font, msg.getTimeFormatted(), curX, nameY, applyAlpha(0xFF64748B, alpha));
                curX += AquaFontRenderer.width(font, msg.getTimeFormatted()) + 5;
            }

            String sender = msg.getSenderName() != null ? msg.getSenderName() : "Игрок";
            AquaFontRenderer.drawNick(graphics, font, sender, curX, nameY, applyAlpha(0xFFF8FAFC, alpha));
            curX += AquaFontRenderer.nickWidth(font, sender) + 5;

            // Rank: user-drawn wordmark art only
            String rankGlyph = LumenTheme.getRankGlyph(msg.getRankId());
            if (!rankGlyph.isEmpty()) {
                Component g = Component.literal(rankGlyph).withStyle(net.minecraft.network.chat.Style.EMPTY
                        .withFont(new net.minecraft.resources.ResourceLocation("aquatech_ui", "ranks")));
                graphics.drawString(font, g, curX, nameY, applyAlpha(0xFFFFFFFF, alpha), false);
                curX += font.width(g) + 6;
            }

            // Channel tag (only in ALL tab to indicate source channel)
            if (AquaChatManager.getActiveChannel() == AquaChatMessage.Channel.ALL
                    && msg.getChannel() != AquaChatMessage.Channel.GLOBAL
                    && msg.getChannel() != AquaChatMessage.Channel.ALL) {
                String chTag = msg.getChannel().getTag();
                int cw = AquaFontRenderer.width(font, chTag) + 6;
                int chCol = msg.getChannel().getColor();
                int chBg = (chCol & 0x00FFFFFF) | 0x22000000;
                LumenGfx.roundedRect(graphics, curX, nameY, cw, 10, 3, applyAlpha(chBg, alpha));
                AquaFontRenderer.draw(graphics, font, chTag, curX + 3, nameY + 1, applyAlpha(chCol, alpha));
            }
        }

        // 2. Message Body Lines
        int lineY = nameY + 12;
        List<FormattedCharSequence> lines = font.split(formatted, wrapW);
        for (FormattedCharSequence line : lines) {
            String lineStr = lineToString(line);
            drawBodyLine(graphics, font, line, lineStr, textX, lineY, applyAlpha(0xFFE2E8F0, alpha), alpha);

            if (chatOpen) {
                for (AquaChatMessage.ItemTagRef tag : msg.getItemTags()) {
                    String chipLabel = "[✦ " + tag.getDisplayName() + "]";
                    int chipIdx = lineStr.indexOf(chipLabel);
                    if (chipIdx >= 0) {
                        String before = lineStr.substring(0, chipIdx);
                        int startX = textX + font.width(AquaFontRenderer.text(before));
                        int chipW = font.width(AquaFontRenderer.text(chipLabel));
                        int itemCol = tag.getRarityColor() != 0 ? tag.getRarityColor() : 0xFF38BDF8;
                        boolean tagHovered = mouseX >= startX - 2 && mouseX <= startX + chipW + 2
                                && mouseY >= lineY - 1 && mouseY <= lineY + 11;
                        if (tagHovered) {
                            hoveredItem = tag.getStack();
                            LumenGfx.roundedRect(graphics, startX - 2, lineY - 1, chipW + 4, 11, 3, 0x44000000 | (itemCol & 0x00FFFFFF));
                            LumenGfx.outline(graphics, startX - 2, lineY - 1, chipW + 4, 11, 3, itemCol);
                        } else {
                            LumenGfx.roundedRect(graphics, startX - 2, lineY - 1, chipW + 4, 11, 3, 0x1A000000 | (itemCol & 0x00FFFFFF));
                            LumenGfx.outline(graphics, startX - 2, lineY - 1, chipW + 4, 11, 3, 0x44000000 | (itemCol & 0x00FFFFFF));
                        }
                    }
                }
                if (!msg.getSharedItem().isEmpty()) {
                    String handLabel = "[✦ " + msg.getSharedItem().getHoverName().getString() + "]";
                    int chipIdx = lineStr.indexOf(handLabel);
                    if (chipIdx >= 0) {
                        String before = lineStr.substring(0, chipIdx);
                        int startX = textX + font.width(AquaFontRenderer.text(before));
                        int chipW = font.width(AquaFontRenderer.text(handLabel));
                        int itemCol = 0xFF38BDF8;
                        boolean handHovered = mouseX >= startX - 2 && mouseX <= startX + chipW + 2
                                && mouseY >= lineY - 1 && mouseY <= lineY + 11;
                        if (handHovered) {
                            hoveredItem = msg.getSharedItem();
                            LumenGfx.roundedRect(graphics, startX - 2, lineY - 1, chipW + 4, 11, 3, 0x44000000 | (itemCol & 0x00FFFFFF));
                            LumenGfx.outline(graphics, startX - 2, lineY - 1, chipW + 4, 11, 3, itemCol);
                        } else {
                            LumenGfx.roundedRect(graphics, startX - 2, lineY - 1, chipW + 4, 11, 3, 0x1A000000 | (itemCol & 0x00FFFFFF));
                            LumenGfx.roundedRect(graphics, startX - 2, lineY - 1, chipW + 4, 11, 3, 0x44000000 | (itemCol & 0x00FFFFFF));
                        }
                    }
                }
            }

            lineY += AquaChatLayout.LINE;
        }
    }

    private static String fitHeader(Font font, String s, int maxW) {
        if (s == null) {
            return "";
        }
        if (AquaFontRenderer.headerWidth(font, s) <= maxW) {
            return s;
        }
        String ell = "...";
        int ew = AquaFontRenderer.headerWidth(font, ell);
        if (maxW <= ew) {
            return ell;
        }
        int n = s.length();
        while (n > 0 && AquaFontRenderer.headerWidth(font, s.substring(0, n)) + ew > maxW) {
            n--;
        }
        return n <= 0 ? ell : s.substring(0, n) + ell;
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

    private static String formatRankBadge(String rankDisplay, int rankColor) {
        if (rankDisplay == null || rankDisplay.isBlank()) {
            return "";
        }
        String clean = rankDisplay.trim();
        String upper = clean.toUpperCase(Locale.ROOT);
        if (upper.contains("ВЛАДЕЛЕЦ") || upper.contains("OWNER")) {
            return "Владелец";
        } else if (upper.contains("РАЗРАБ") || upper.contains("DEVELOPER") || upper.equals("DEV")) {
            return "Разраб";
        } else if (upper.contains("АДМИН") || upper.contains("ADMIN")) {
            return "Админ";
        } else if (upper.contains("МОДЕР") || upper.contains("MOD") || upper.contains("ХЕЛПЕР")) {
            return "Модер";
        } else if (upper.contains("ЛЕГЕНДА") || upper.contains("LEGEND")) {
            return "Легенда";
        } else if (upper.contains("VIP") || upper.contains("ПРЕМИУМ")) {
            return "VIP";
        }
        return sentenceLabel(clean);
    }

    public static int getNanobananoRankColor(String rank, int fallbackColor) {
        if (rank == null || rank.isBlank()) return fallbackColor != 0 ? fallbackColor : 0xFF94A3B8;
        String u = rank.toUpperCase(Locale.ROOT);
        if (u.contains("ВЛАДЕЛЕЦ") || u.contains("OWNER")) return 0xFFFF3B30; // Neon Coral Red
        if (u.contains("АДМИН") || u.contains("ADMIN") || u.contains("DEV")) return 0xFFFF9500; // Warm Amber
        if (u.contains("МОДЕР") || u.contains("MOD") || u.contains("ХЕЛПЕР")) return 0xFF10B981; // Emerald Mint
        if (u.contains("VIP") || u.contains("ВИП")) return 0xFF00F0FF; // Cyber Cyan
        if (u.contains("ПРЕМИУМ") || u.contains("PREMIUM")) return 0xFFA855F7; // Royal Amethyst
        if (u.contains("ЛЕГЕНДА") || u.contains("LEGEND")) return 0xFFF59E0B; // Imperial Gold
        if (u.contains("ИГРОК") || u.contains("PLAYER")) return 0xFF94A3B8; // Refined Slate Glass
        return fallbackColor != 0 ? fallbackColor : 0xFF38BDF8;
    }

    private static String sentenceLabel(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String t = raw.trim();
        Locale ru = Locale.forLanguageTag("ru");
        if (!t.equals(t.toUpperCase(ru))) {
            return t;
        }
        String lower = t.toLowerCase(ru);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private static int applyAlpha(int color, float alpha) {
        int a = (int) (((color >> 24) & 0xFF) * Math.max(0.0F, Math.min(1.0F, alpha)));
        if (a == 0 && alpha > 0.01F) {
            a = (int) (alpha * 255.0F);
        }
        return (a << 24) | (color & 0x00FFFFFF);
    }
}
