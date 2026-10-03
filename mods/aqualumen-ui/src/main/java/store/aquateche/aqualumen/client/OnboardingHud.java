package store.aquateche.aqualumen.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import store.aquateche.aqualumen.AquaLumenUI;
import store.aquateche.aqualumen.common.service.OnboardingSteps;

import java.util.List;

/**
 * New-player guide: a panel on the left that slides in, lists the five steps, shows the current hint with a
 * progress bar and drops a toast with the reward when a step is done. It folds into a small tab (key or click).
 * The server (KubeJS script + OnboardingService) owns the state; this class only draws it.
 */
@Mod.EventBusSubscriber(modid = AquaLumenUI.MODID, value = Dist.CLIENT)
public final class OnboardingHud {

    private static final int W = 156;
    private static final int PAD = 6;
    private static final int ROW = 13;
    private static final int HEADER = 16;
    private static final int TAB_W = 24;
    private static final int TAB_H = 22;
    private static final int X_HOME = 6;
    private static final int Y_HOME = 40;
    private static final long TOAST_LIFE = 4200L;
    private static final long FINISH_SHOW_MS = 7000L;

    private static final int BG = 0xD0121A24;
    private static final int BORDER = 0xFF2C4A63;
    private static final int GOLD = 0xFFFFC53D;
    private static final int GREEN = 0xFF55E07A;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int DIM = 0xFF6F8294;
    private static final int DONE = 0xFF8FB8A6;
    private static final int HINT = 0xFFB9D6E8;
    private static final int BAR_BG = 0xFF0A0F15;

    private static int step;
    private static int have;
    private static double shownHave;
    private static boolean collapsed;
    private static double open = 1.0;
    private static long appearAt;
    private static long flashUntil;
    private static long finishedAt;
    private static long toastAt;
    private static long lastFrame;
    private static String toastTitle = "";
    private static String toastReward = "";

    private static int hitX0;
    private static int hitY0;
    private static int hitX1;
    private static int hitY1;

    private OnboardingHud() {
    }

    /** Network entry point, already on the client thread. */
    public static void accept(int newStep, int newHave, int doneStep, String reward) {
        long now = System.currentTimeMillis();
        if (newStep <= 0) {
            reset();
            return;
        }
        boolean wasHidden = step <= 0;
        if (wasHidden) {
            appearAt = now;
            shownHave = newHave;
            open = collapsed ? 0.0 : 1.0;
            finishedAt = 0;
        } else if (newStep != step) {
            flashUntil = now + 900;
        }
        if (newStep > OnboardingSteps.COUNT && finishedAt == 0) {
            finishedAt = now;
        }
        if (doneStep > 0) {
            toastTitle = "Шаг " + doneStep + " пройден";
            toastReward = reward == null ? "" : reward;
            toastAt = now;
        }
        step = newStep;
        have = newHave;
    }

    private static void reset() {
        step = 0;
        have = 0;
        finishedAt = 0;
        toastAt = 0;
        lastFrame = 0;
    }

    public static void toggle() {
        if (step > 0) {
            collapsed = !collapsed;
        }
    }

    @SubscribeEvent
    public static void onMouse(ScreenEvent.MouseButtonPressed.Pre event) {
        if (step <= 0 || event.getButton() != 0) {
            return;
        }
        Screen screen = event.getScreen();
        if (!(screen instanceof ChatScreen || screen instanceof AbstractContainerScreen || screen instanceof PauseScreen)) {
            return;
        }
        double mx = event.getMouseX();
        double my = event.getMouseY();
        if (mx >= hitX0 && mx < hitX1 && my >= hitY0 && my < hitY1) {
            toggle();
            event.setCanceled(true);
        }
    }

    // ------------------------------------------------------------------ render

    public static void render(GuiGraphics g, int screenW, int screenH) {
        Minecraft mc = Minecraft.getInstance();
        if (step <= 0 || mc.options.hideGui || mc.player == null) {
            return;
        }
        long now = System.currentTimeMillis();
        double dt = lastFrame == 0 ? 0.016 : Math.min(0.1, (now - lastFrame) / 1000.0);
        lastFrame = now;

        double exitT = 0;
        if (finishedAt > 0 && now - finishedAt > FINISH_SHOW_MS) {
            exitT = (now - finishedAt - FINISH_SHOW_MS) / 500.0;
            if (exitT >= 1.0) {
                reset();
                return;
            }
        }

        Font font = mc.font;
        boolean finished = step > OnboardingSteps.COUNT;
        OnboardingSteps.Step cur = OnboardingSteps.get(step);
        List<FormattedCharSequence> hintLines = cur == null ? List.of() : font.split(Component.literal(cur.hint()), W - 2 * PAD - 2);
        int goal = cur == null ? 1 : cur.goal();
        int expandedH = HEADER + 2 + OnboardingSteps.COUNT * ROW + 4
                + (finished ? 14 : hintLines.size() * 10 + (goal > 1 ? 13 : 0) + 4);

        open = OnboardingAnim.approach(open, collapsed ? 0.0 : 1.0, 9.0, dt);
        double e = OnboardingAnim.easeOutCubic(open);
        int curW = (int) Math.round(TAB_W + (W - TAB_W) * e);
        int curH = (int) Math.round(TAB_H + (expandedH - TAB_H) * e);

        double appear = OnboardingAnim.easeOutBack((now - appearAt) / 520.0);
        int slide = (int) Math.round(-(W + 16) * (1.0 - appear) - (W + 24) * exitT * exitT * exitT);
        int x0 = X_HOME + slide;
        int y0 = Y_HOME;
        if (x0 + curW <= 0) {
            return;
        }

        boolean flash = now < flashUntil;
        int accent = flash || finished ? GREEN : GOLD;

        hitX0 = x0;
        hitY0 = y0;
        hitX1 = x0 + (open < 0.5 ? TAB_W : W);
        hitY1 = y0 + (open < 0.5 ? TAB_H : HEADER);

        g.fill(x0 + 2, y0 + 2, x0 + curW + 2, y0 + curH + 2, 0x55000000);
        g.fill(x0, y0, x0 + curW, y0 + curH, BG);
        g.fill(x0, y0, x0 + curW, y0 + 1, BORDER);
        g.fill(x0, y0 + curH - 1, x0 + curW, y0 + curH, BORDER);
        g.fill(x0 + curW - 1, y0, x0 + curW, y0 + curH, BORDER);
        g.fill(x0, y0, x0 + 2, y0 + curH, accent);

        g.enableScissor(x0, y0, x0 + curW, y0 + curH);
        if (open > 0.45) {
            drawExpanded(g, font, x0, y0, finished, cur, hintLines, goal, accent, now, dt);
        } else {
            String label = finished ? "OK" : Math.min(step, OnboardingSteps.COUNT) + "/" + OnboardingSteps.COUNT;
            g.drawString(font, label, x0 + 2 + (TAB_W - 2 - font.width(label)) / 2, y0 + (TAB_H - 8) / 2, accent, true);
        }
        g.disableScissor();

        drawToast(g, font, y0 + curH + 5, now);
    }

    private static void drawExpanded(GuiGraphics g, Font font, int x0, int y0, boolean finished,
                                     OnboardingSteps.Step cur, List<FormattedCharSequence> hintLines,
                                     int goal, int accent, long now, double dt) {
        int tx = x0 + PAD + 2;
        g.drawString(font, finished ? "Старт пройден!" : "ОБУЧЕНИЕ", tx, y0 + 4, accent, true);

        String key = LumenClient.TOGGLE_GUIDE.getTranslatedKeyMessage().getString();
        String right = (finished ? "" : Math.min(step, OnboardingSteps.COUNT) + "/" + OnboardingSteps.COUNT + "  ") + "[" + key + "]";
        g.drawString(font, right, x0 + W - PAD - 8 - font.width(right), y0 + 4, DIM, false);
        g.drawString(font, "<", x0 + W - PAD - 3, y0 + 4, DIM, false);

        int ly = y0 + HEADER + 2;
        double pulse = 0.5 + 0.5 * Math.sin(now / 260.0);
        for (OnboardingSteps.Step s : OnboardingSteps.all()) {
            int rowY = ly + (s.id() - 1) * ROW;
            boolean done = s.id() < step;
            boolean current = s.id() == step;
            if (current) {
                g.fill(x0 + 3, rowY - 1, x0 + W - 1, rowY + ROW - 2, 0x22FFC53D);
            }
            if (done) {
                drawCheck(g, tx, rowY + 2, GREEN);
            } else if (current) {
                drawArrow(g, tx + 1, rowY + 2, argb(0xFFC53D, 0.65 + 0.35 * pulse));
            } else {
                g.fill(tx + 2, rowY + 4, tx + 4, rowY + 6, DIM);
            }
            int color = done ? DONE : current ? WHITE : DIM;
            g.drawString(font, s.title(), tx + 10, rowY + 1, color, current);
        }

        int by = ly + OnboardingSteps.COUNT * ROW + 2;
        g.fill(tx, by, x0 + W - PAD, by + 1, 0x33FFFFFF);
        by += 4;
        if (finished) {
            g.drawString(font, "Дальше: квесты и меню F4", tx, by, HINT, false);
            return;
        }
        for (FormattedCharSequence line : hintLines) {
            g.drawString(font, line, tx, by, HINT, false);
            by += 10;
        }
        if (goal > 1) {
            shownHave = OnboardingAnim.approach(shownHave, have, 8.0, dt);
            int barW = W - 2 * PAD - 2;
            g.fill(tx, by + 1, tx + barW, by + 10, BAR_BG);
            int fillW = (int) Math.round((barW - 2) * OnboardingSteps.fraction((int) Math.round(shownHave * 100.0), goal * 100));
            g.fill(tx + 1, by + 2, tx + 1 + fillW, by + 9, GOLD);
            String label = Math.min(have, goal) + "/" + goal;
            g.drawString(font, label, tx + (barW - font.width(label)) / 2, by + 2, WHITE, true);
        }
    }

    private static void drawToast(GuiGraphics g, Font font, int y, long now) {
        double alpha = OnboardingAnim.toastAlpha(now - toastAt, TOAST_LIFE);
        if (alpha <= 0.0) {
            return;
        }
        double slideIn = OnboardingAnim.easeOutCubic((now - toastAt) / 350.0);
        int x = X_HOME + (int) Math.round(-(W + 16) * (1.0 - slideIn));
        int h = 30;
        g.fill(x, y, x + W, y + h, argb(0x121A24, 0.9 * alpha));
        g.fill(x, y, x + W, y + 1, argb(0x2C4A63, alpha));
        g.fill(x, y + h - 1, x + W, y + h, argb(0x2C4A63, alpha));
        g.fill(x, y, x + 2, y + h, argb(0x55E07A, alpha));
        drawCheck(g, x + 8, y + 5, argb(0x55E07A, alpha));
        g.drawString(font, toastTitle, x + 20, y + 4, argb(0x55E07A, alpha), true);
        String reward = toastReward.isEmpty() ? "Так держать!" : toastReward;
        g.drawString(font, font.plainSubstrByWidth(reward, W - 16), x + 8, y + 17, argb(toastReward.isEmpty() ? 0xB9D6E8 : 0xFFC53D, alpha), false);
    }

    // ------------------------------------------------------------------ tiny pixel icons

    private static void drawCheck(GuiGraphics g, int x, int y, int color) {
        int[][] px = {{0, 2}, {1, 3}, {2, 4}, {3, 3}, {4, 2}, {5, 1}, {6, 0}};
        for (int[] p : px) {
            g.fill(x + p[0], y + p[1], x + p[0] + 1, y + p[1] + 1, color);
            g.fill(x + p[0], y + p[1] + 1, x + p[0] + 1, y + p[1] + 2, color);
        }
    }

    private static void drawArrow(GuiGraphics g, int x, int y, int color) {
        int[] widths = {1, 2, 3, 2, 1};
        for (int r = 0; r < widths.length; r++) {
            g.fill(x, y + r, x + widths[r], y + r + 1, color);
        }
    }

    private static int argb(int rgb, double alpha) {
        int a = (int) Math.round(Math.max(0.0, Math.min(1.0, alpha)) * 255.0);
        return (Math.max(a, 8) << 24) | (rgb & 0xFFFFFF);
    }
}
