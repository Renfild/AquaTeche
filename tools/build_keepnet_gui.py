"""Рисует GUI садков (блок-хранилище рыбы) в стиле «Латунь», как у механизмов AquaTech.

    python tools/build_keepnet_gui.py

Тиров шесть: тир 1 это 3 ряда (27 ячеек), каждый следующий тир на ряд больше, тир 6 это 8 рядов (72 ячейки).
Результат: mods/aquatech-ui/art/fish_keepnet_gui.png (тир 1) и fish_keepnet_gui_t2..t6.png, высота GUI 212 + 18 * (ряды - 3),
и лист предпросмотра fish_keepnet_gui_preview.png.
Ячейка = предмет 16x16, координаты верхнего левого угла предмета в системе GUI: x = 47 + 18 * столбец, y = 36 + 18 * ряд.
Инвентарь игрока и рамка сдвигаются вниз на 18 пикселей за каждый ряд сверх трёх.
Имена тиров и число рядов должны совпадать с KeepnetTier.java.
"""
import math
import random
import sys
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(Path(__file__).resolve().parent))
import build_machine_guis as g  # noqa: E402

ART = ROOT / "mods" / "aquatech-ui" / "art"
PREVIEW = ART / "fish_keepnet_gui_preview.png"

# (id тира, число рядов): id совпадает с именем текстуры GUI в мод-ресурсах
TIERS = [("fish_keepnet", 3), ("fish_keepnet_t2", 4), ("fish_keepnet_t3", 5),
         ("fish_keepnet_t4", 6), ("fish_keepnet_t5", 7), ("fish_keepnet_t6", 8)]

COLS = 9
BASE_ROWS = 3
SLOT_X0 = 47                       # как PLAYER_X: столбцы садка встают над столбцами инвентаря
GRID_Y0 = 36                       # первый ряд садка всегда под табличкой
FIELD = (12, 22, 243, 118)         # свободное поле внутри рамки
BASE_HEIGHT = 212                  # высота GUI для трёх рядов, как у механизмов


def waves(c, y):
    """Волны над плитой: садок стоит в воде."""
    x0, _, x1, _ = FIELD
    for x in range(x0 + 10, x1 - 9):
        yy = y + round(1.4 * math.sin(x / 5.0))
        c.px(x, yy, g.P["water"])
        c.px(x, yy + 1, g.P["water_d"])
        if x % 7 == 0:
            c.px(x, yy - 1, g.P["sky"])


def netting(c, x0, y0, x1, y1):
    """Полоса сети под плитой: ромбовая ячейка из нитей и два поплавка по краям."""
    c.r(x0 - 1, y0 - 1, x1 + 1, y1 + 1, g.P["out"])
    c.r(x0, y0, x1, y1, g.P["field2"])
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            if (x + y) % 6 == 0 or (x - y) % 6 == 0:
                c.px(x, y, g.P["line"])
    c.bevel(x0, y0, x1, y1, g.P["shade"], g.P["hi2"])
    for fx in (x0 + 8, x1 - 16):   # пробковые поплавки
        c.r(fx, y0 + 3, fx + 8, y0 + 11, g.P["out"])
        c.r(fx + 1, y0 + 4, fx + 7, y0 + 10, g.hx("c98a4a"))
        c.r(fx + 1, y0 + 4, fx + 7, y0 + 5, g.hx("e8b878"))
        c.r(fx + 1, y0 + 9, fx + 7, y0 + 10, g.hx("8a5a2a"))
        c.px(fx + 3, y0 + 7, g.hx("8a5a2a")); c.px(fx + 5, y0 + 7, g.hx("8a5a2a"))


def build(rows):
    dy = 18 * (rows - BASE_ROWS)
    rnd = random.Random(2026)
    c = g.Canvas(256, BASE_HEIGHT + dy)
    g.frame(c, rnd, dy)
    g.plaque(c)
    g.inventory(c, dy)
    g.decal(c, "fish", rnd)
    waves(c, 28)
    g.plate(c, SLOT_X0, GRID_Y0, COLS, rows)
    top = GRID_Y0 + 18 * rows + 6
    netting(c, 28, top, 227, top + 16)
    return c.im


def preview(images):
    """Лист предпросмотра: тиры по три в ряд, рядом каждый GUI в масштабе 1:1."""
    cell_w, cell_h = 256 + 8, 302 + 8
    sheet = Image.new("RGBA", (3 * cell_w, 2 * cell_h), (30, 33, 38, 255))
    for i, im in enumerate(images):
        sheet.paste(im, ((i % 3) * cell_w + 4, (i // 3) * cell_h + 4), im)
    sheet.save(PREVIEW)


if __name__ == "__main__":
    made = []
    for name, rows in TIERS:
        img = build(rows)
        out = ART / (name.replace("fish_keepnet", "fish_keepnet_gui", 1) + ".png")
        img.save(out)
        made.append(img)
        print("saved", out.name, img.size, "slots:", COLS * rows)
    preview(made)
    print("preview", PREVIEW.name)
