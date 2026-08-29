package net.aquatech.ui.client.chat;

/**
 * One column for the open-chat card: panel, messages, toolbar, and input share
 * the same left/right edges. Do not size the EditBox from a second formula.
 */
public final class AquaChatLayout {

    public static final int CHAT_WIDTH = 350;
    public static final int CONTENT_X = 8;
    public static final int INNER = 6;
    public static final int PANEL_X = CONTENT_X - INNER;
    public static final int PANEL_W = CHAT_WIDTH + INNER * 2;
    public static final int PANEL_BOTTOM_GAP = 8;
    public static final int PANEL_TOP_INSET = 278;
    public static final int HEADER_INSET = 274;
    public static final int TAB_INSET = 258;
    /** Gap from screen bottom to last message row when the chat screen is open. */
    public static final int OPEN_BOTTOM_GAP = 68;
    public static final int CLOSED_BOTTOM_GAP = 38;
    public static final int INPUT_H = 24;
    public static final int INPUT_BOTTOM_GAP = 10;
    public static final int TOOL_H = 24;
    public static final int TOOL_GAP = 6;
    public static final int SEND_SIZE = 24;
    public static final int INPUT_PAD_L = 10;
    /** Counter + send control on the trailing edge. */
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
        return screenH - INPUT_BOTTOM_GAP - INPUT_H;
    }

    public static int toolY(int screenH) {
        return inputY(screenH) - TOOL_GAP - TOOL_H;
    }

    public static int sendX() {
        return CONTENT_X + CHAT_WIDTH - SEND_SIZE;
    }
}
