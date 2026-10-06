"""Рисует GUI садка (блок-хранилище рыбы) в стиле «Латунь», как у механизмов AquaTech.

    python tools/build_keepnet_gui.py

Результат: mods/aquatech-ui/art/fish_keepnet_gui.png (256x256, сам GUI 256x212, ниже пусто) и лист предпросмотра.
Раскладка: 27 ячеек (3 ряда по 9), как у сундука, плюс инвентарь игрока. Ячейка = предмет 16x16, координаты
верхнего левого угла предмета в системе GUI 256x212: x = 47 + 18 * столбец, y = 36 + 18 * ряд.
Чтобы изменить вместимость, поменяй ROWS (1..4): плита, окно воды и сетка под ней сдвинутся сами.
"""
import math
import random
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(Path(__file__).resolve().parent))
import build_machine_guis as g  # noqa: E402

OUT = ROOT / "mods" / "aquatech-ui" / "art" / "fish_keepnet_gui.png"
PREVIEW = ROOT / "mods" / "aquatech-ui" / "art" / "fish_keepnet_gui_preview.png"

COLS, ROWS = 9, 3
SLOT_X0 = 47                       # как PLAYER_X: столбцы садка встают над столбцами инвентаря
GRID_Y0 = 36 + (3 - ROWS) * 9      # при другом числе рядов плита остаётся по центру поля
FIELD = (12, 22, 243, 118)         # свободное поле внутри рамки


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


def build():
    rnd = random.Random(2026)
    c = g.Canvas()
    g.frame(c, rnd)
    g.plaque(c)
    g.inventory(c)
    g.decal(c, "fish", rnd)
    waves(c, 28)
    g.plate(c, SLOT_X0, GRID_Y0, COLS, ROWS)
    top = GRID_Y0 + 18 * ROWS + 6
    netting(c, 28, top, 227, min(top + 16, 116))
    return c.im


def preview(im):
    crop = im.crop((0, 0, 256, 212))
    big = crop.resize((256 * 3, 212 * 3), resample=0)
    big.save(PREVIEW)


if __name__ == "__main__":
    img = build()
    img.save(OUT)
    preview(img)
    print("saved", OUT, img.size, "slots:", COLS * ROWS)
