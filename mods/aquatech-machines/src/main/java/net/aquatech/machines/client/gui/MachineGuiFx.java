package net.aquatech.machines.client.gui;

import net.aquatech.machines.util.MachineLayout;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * Динамика GUI механизмов: шкалы, стрелка, жидкость в резервуарах, пруд, пламя и кристалл. Фон со статикой лежит в
 * текстуре, а здесь рисуется то, что движется. Время идёт в тиках с долями (игровое время плюс partialTick).
 */
final class MachineGuiFx {

    private static final int TEX = 256;
    static final int ENERGY_MAIN = 0xFFFFC21F;
    static final int ENERGY_LIGHT = 0xFFFFE9A0;
    static final int ENERGY_DARK = 0xFFC98A0A;
    static final int PROGRESS_MAIN = 0xFFFF8A2A;
    static final int PROGRESS_LIGHT = 0xFFFFC68A;
    static final int PROGRESS_DARK = 0xFFC25A10;

    private MachineGuiFx() {
    }

    /** Горизонтальная шкала: тёмный канал уже в фоне, здесь заливка с бликом и бегущей искрой. */
    static void bar(GuiGraphics g, int x, int y, int w, int h, float fraction, int main, int light, int dark, float time) {
        int filled = Math.round((w - 2) * Math.max(0f, Math.min(1f, fraction)));
        if (filled <= 0) {
            return;
        }
        int left = x + 1;
        int top = y + 1;
        int bottom = y + h - 1;
        g.fill(left, top, left + filled, bottom, main);
        g.fill(left, top, left + filled, top + 1, light);
        g.fill(left, bottom - 1, left + filled, bottom, dark);
        int spark = (int) ((time * 1.6f) % (filled + 14)) - 10;
        int from = Math.max(0, spark);
        int to = Math.min(filled, spark + 6);
        if (to > from) {
            g.fill(left + from, top, left + to, top + 1, 0xFFFFFFFF);
            g.fill(left + from, top + 1, left + to, bottom - 1, light);
        }
    }

    /** Стрелка прогресса: янтарная заливка слева направо плюс искра. */
    static void arrow(GuiGraphics g, ResourceLocation tex, int x, int y, float fraction, float time) {
        int width = Math.round(24 * Math.max(0f, Math.min(1f, fraction)));
        if (width <= 0) {
            return;
        }
        g.blit(tex, x, y, 0, MachineLayout.ATLAS_Y, width, 17, TEX, TEX);
        int spark = (int) ((time * 1.2f) % 30) - 4;
        if (spark >= 0 && spark < width) {
            g.fill(x + spark, y + 6, x + spark + 1, y + 11, 0xFFFFFFFF);
        }
    }

    /** Резервуар с жидкостью: волнистая поверхность, блик слева, тень справа и поднимающиеся пузырьки. */
    static void tank(GuiGraphics g, int x, int y, int w, int h, float fraction, int main, int light, int dark, float time) {
        int level = Math.round(h * Math.max(0f, Math.min(1f, fraction)));
        if (level <= 0) {
            return;
        }
        int bottom = y + h;
        int surface = bottom - level;
        for (int column = 0; column < w; column++) {
            int wave = Math.round((float) Math.sin(time * 0.16f + column * 0.9f));
            int top = Math.max(y, Math.min(bottom - 1, surface + wave));
            int color = column < 2 ? light : (column >= w - 2 ? dark : main);
            g.fill(x + column, top, x + column + 1, bottom, color);
            g.fill(x + column, top, x + column + 1, top + 1, light);
        }
        for (int bubble = 0; bubble < 3; bubble++) {
            int rise = (int) ((time * 0.45f + bubble * 19) % Math.max(4, level));
            int by = bottom - 2 - rise;
            int bx = x + 2 + (bubble * 4 + (int) (time / 7) % 3) % Math.max(1, w - 4);
            if (by > surface + 1) {
                g.fill(bx, by, bx + 1, by + 1, 0xCCFFFFFF);
            }
        }
    }

    /** Пруд Рыболова: вода с двумя слоями волн, леска, поплавок, который клюёт к концу цикла, и рыбка. */
    static void pond(GuiGraphics g, ResourceLocation tex, int x, int y, int w, int h, float fraction, boolean working, float time) {
        int waterTop = y + Math.round(h * 0.42f);
        for (int row = waterTop; row < y + h; row++) {
            float depth = (row - waterTop) / (float) Math.max(1, y + h - waterTop);
            g.fill(x, row, x + w, row + 1, lerp(0xFF2F8FBF, 0xFF0E4766, depth));
        }
        for (int column = 0; column < w; column++) {
            int wave = Math.round((float) (Math.sin(time * 0.12f + column * 0.22f) * 1.4f));
            int surface = waterTop + wave;
            g.fill(x + column, surface, x + column + 1, surface + 1, 0xFFCFF3FF);
            g.fill(x + column, waterTop + 5 + Math.round((float) Math.sin(time * 0.09f + column * 0.5f)),
                    x + column + 1, waterTop + 6 + Math.round((float) Math.sin(time * 0.09f + column * 0.5f)), 0x553FB2E0);
        }
        int bobberX = x + w / 2 + 10;
        boolean bite = working && fraction > 0.88f;
        int bob = bite ? 3 + (int) (Math.sin(time * 0.9f) * 1.5f) : Math.round((float) Math.sin(time * 0.15f));
        int bobberY = waterTop - 3 + bob;
        g.fill(bobberX, y, bobberX + 1, bobberY, 0xFFE8F4FF);
        g.fill(bobberX - 1, bobberY, bobberX + 2, bobberY + 3, 0xFFFF4D4D);
        g.fill(bobberX - 1, bobberY, bobberX + 2, bobberY + 1, 0xFFFFFFFF);
        if (bite) {
            for (int drop = 0; drop < 4; drop++) {
                int dx = (int) (Math.sin(time * 0.4f + drop * 1.7f) * 5);
                int dy = -((int) (time * 0.8f + drop * 3) % 6);
                g.fill(bobberX + dx, waterTop + dy, bobberX + dx + 1, waterTop + dy + 1, 0xFFFFFFFF);
            }
        }
        if (working) {
            int swim = (int) ((time * 0.55f) % (w + 24)) - 12;
            int fy = waterTop + 14 + Math.round((float) Math.sin(time * 0.1f) * 3);
            int fx = x + swim;
            if (fx >= x && fx + 10 <= x + w) {
                g.blit(tex, fx, fy, 80, MachineLayout.ATLAS_Y, 10, 6, TEX, TEX);
            }
        }
    }

    /** Топка: три языка пламени из спрайтов, красноватое свечение и искры. Горит только пока есть топливо. */
    static void flames(GuiGraphics g, ResourceLocation tex, int x, int y, int w, int h, boolean burning, float time) {
        if (!burning) {
            return;
        }
        g.fill(x, y, x + w, y + h, 0x22FF8A2A);
        int flameWidth = 24;
        int gap = (w - 3 * flameWidth) / 4;
        for (int flame = 0; flame < 3; flame++) {
            int frame = ((int) (time / 3f) + flame) % 4;
            int fx = x + gap + flame * (flameWidth + gap);
            g.blit(tex, fx, y + h - 5 - 32, flameWidth, 32, 30 + 12 * frame, MachineLayout.ATLAS_Y, 12, 16, TEX, TEX);
        }
        for (int spark = 0; spark < 5; spark++) {
            int sx = x + 6 + (spark * 17 + (int) (time * 0.3f)) % (w - 12);
            int sy = y + h - 12 - (int) ((time * 0.7f + spark * 11) % (h - 14));
            g.fill(sx, sy, sx + 1, sy + 1, 0xFFFFD24A);
        }
    }

    /** Кристалл маны в центре площадки: три кадра пульсации, ореол и искры по кругу. */
    static void crystal(GuiGraphics g, ResourceLocation tex, int x, int y, int size, boolean working, float time) {
        int frame = working ? ((int) (time / 6f)) % 3 : 0;
        if (working) {
            g.fill(x, y, x + size, y + size, 0x22B072FF);
        }
        g.blit(tex, x, y, size, size, 24 * frame, MachineLayout.ATLAS_Y + 18, 24, 24, TEX, TEX);
        if (working) {
            for (int spark = 0; spark < 4; spark++) {
                double angle = time * 0.08 + spark * Math.PI / 2;
                int sx = x + size / 2 + (int) (Math.cos(angle) * (size / 2 - 3));
                int sy = y + size / 2 + (int) (Math.sin(angle) * (size / 2 - 3));
                g.fill(sx, sy, sx + 1, sy + 1, 0xFFE8D0FF);
            }
        }
    }

    private static int lerp(int from, int to, float t) {
        int r = Math.round(((from >> 16) & 0xFF) * (1 - t) + ((to >> 16) & 0xFF) * t);
        int gr = Math.round(((from >> 8) & 0xFF) * (1 - t) + ((to >> 8) & 0xFF) * t);
        int b = Math.round((from & 0xFF) * (1 - t) + (to & 0xFF) * t);
        return 0xFF000000 | (r << 16) | (gr << 8) | b;
    }
}
