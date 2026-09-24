package net.aquatech.ui.client.chat;

/**
 * AquaChat layout: refined glass panel, minimalist row geometry and responsive tabs.
 */
public final class AquaChatLayout {

    public static final int CHAT_WIDTH = 320;
    public static final int CONTENT_X = 12;
    public static final int INNER = 8;
    public static final int PANEL_X = CONTENT_X - INNER;
    public static final int PANEL_W = CHAT_WIDTH + INNER * 2;
    public static final int PANEL_RADIUS = 10;
    /** Screen-edge gap below the panel. */
    public static final int PANEL_BOTTOM_GAP = 12;
    /** Padding inside the rounded panel under the input. */
    public static final int PANEL_INNER = 8;
    public static final int PANEL_TOP_INSET = 296;

    public static final int TAB_H = 20;
    public static final int HEADER_INSET = 290;
    public static final int TAB_INSET = 268;
    public static final int NAME_H = 10;
    public static final int CLOSED_BOTTOM_GAP = 36;
    public static final int INPUT_H = 26;
    public static final int BTN_SIZE = 26;
    public static final int SEND_SIZE = 28;
    public static final int HEAD_SIZE = 18;
    public static final int HEAD_GAP = 7;
    public static final int LINE = 11;
    public static final int ROW_PAD = 4;
    public static final int ROW_GAP = 4;
    public static final int BUBBLE_PAD_X = 6;
    public static final int BUBBLE_PAD_Y = 4;
    public static final int BUBBLE_R = 6;
    public static final int INPUT_PAD_L = 8;
    public static final int INPUT_PAD_R = 56;

    /** Левая колонка каналов (вариант «лента-док»). */
    public static final int RAIL_W = 112;
    public static final int RAIL_ROW_H = 22;
    public static final int RAIL_GAP = 4;
    /** Полоса быстрых команд над строкой ввода. */
    public static final int CHIPS_H = 20;
    public static final int CHIPS_GAP = 6;

    private AquaChatLayout() {
    }

    public static int feedX() {
        return CONTENT_X + RAIL_W + 8;
    }

    public static int feedW() {
        return CHAT_WIDTH - RAIL_W - 8;
    }

    public static int contentRight() {
        return feedX() + feedW();
    }

    public static int railX() {
        return CONTENT_X;
    }

    public static int railW() {
        return RAIL_W;
    }

    public static int railTop(int screenH) {
        return panelTop(screenH) + 30;
    }

    public static int railRowY(int screenH, int index) {
        return railTop(screenH) + index * (RAIL_ROW_H + RAIL_GAP);
    }

    public static int railCardY(int screenH) {
        return chipsY(screenH) - 30;
    }

    public static int chipsY(int screenH) {
        return inputY(screenH) - CHIPS_H - CHIPS_GAP;
    }

    public static int panelTop(int screenH) {
        return screenH - PANEL_TOP_INSET;
    }

    public static int panelBottom(int screenH) {
        return screenH - PANEL_BOTTOM_GAP;
    }

    public static int panelH(int screenH) {
        return panelBottom(screenH) - panelTop(screenH);
    }

    public static int tabY(int screenH) {
        return panelTop(screenH) + 8;
    }

    public static int inputY(int screenH) {
        return panelBottom(screenH) - PANEL_INNER - INPUT_H;
    }

    public static int btnHashX() {
        return CONTENT_X;
    }

    public static int inputCapsuleX() {
        return CONTENT_X + BTN_SIZE + 6;
    }

    public static int sendX() {
        return CONTENT_X + CHAT_WIDTH - SEND_SIZE;
    }

    public static int inputCapsuleW() {
        return sendX() - 6 - inputCapsuleX();
    }

    /** Bottom of the last history row: above the chips row and the input dock. */
    public static int messageBottom(int screenH) {
        return chipsY(screenH) - 6;
    }

    public static int messageTop(int screenH) {
        return panelTop(screenH) + 30;
    }
}
