package net.aquatech.ui.client.chat;

/**
 * Open-chat card is one column: panel, history, toolbar, input share the same
 * edges. Input sits inside the rounded panel, not on the stroke.
 */
public final class AquaChatLayout {

    public static final int CHAT_WIDTH = 350;
    public static final int CONTENT_X = 8;
    public static final int INNER = 6;
    public static final int PANEL_X = CONTENT_X - INNER;
    public static final int PANEL_W = CHAT_WIDTH + INNER * 2;
    public static final int PANEL_RADIUS = 10;
    /** Screen-edge gap below the panel. */
    public static final int PANEL_BOTTOM_GAP = 12;
    /** Padding inside the rounded panel under the input. */
    public static final int PANEL_INNER = 12;
    public static final int PANEL_TOP_INSET = 278;
    public static final int HEADER_INSET = 274;
    public static final int TAB_INSET = 258;
    public static final int TAB_H = 17;
    public static final int CLOSED_BOTTOM_GAP = 38;
    public static final int INPUT_H = 24;
    public static final int TOOL_H = 24;
    public static final int TOOL_GAP = 6;
    public static final int SEND_SIZE = 24;
    public static final int INPUT_PAD_L = 10;
    public static final int INPUT_PAD_R = 78;

    private AquaChatLayout() {
    }

    public static int contentRight() {
        return CONTENT_X + CHAT_WIDTH;
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

    public static int inputY(int screenH) {
        return panelBottom(screenH) - PANEL_INNER - INPUT_H;
    }

    public static int toolY(int screenH) {
        return inputY(screenH) - TOOL_GAP - TOOL_H;
    }

    /** Bottom of the last history row: above the toolbar, inside the panel. */
    public static int messageBottom(int screenH) {
        return toolY(screenH) - TOOL_GAP;
    }

    public static int messageTop(int screenH) {
        return screenH - TAB_INSET + TAB_H + 8;
    }

    public static int sendX() {
        return CONTENT_X + CHAT_WIDTH - SEND_SIZE;
    }
}
