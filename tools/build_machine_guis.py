"""Рисует GUI всех механизмов AquaTech в стиле «Латунь» (золотая панель, табличка, подвесной инвентарь) и
генерирует Java-константы раскладки, чтобы меню, экраны и текстуры не расходились.

    python tools/build_machine_guis.py            # текстуры + MachineLayout.java
    python tools/build_machine_guis.py --preview  # ещё и лист предпросмотра в art/

Координаты слотов это левый верхний угол предмета 16x16 в системе GUI 256x212. Динамика (вода, огонь, шкалы, стрелки)
рисуется в Java поверх фона, а спрайты для неё лежат в нижней части каждой текстуры (y >= 212).
"""
import math
import random
import sys
import zlib
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
MOD = ROOT / "mods" / "aquatech-machines"
GUI_DIR = MOD / "src" / "main" / "resources" / "assets" / "aquatech_machines" / "textures" / "gui"
LAYOUT_JAVA = MOD / "src" / "main" / "java" / "net" / "aquatech" / "machines" / "util" / "MachineLayout.java"
PREVIEW = MOD / "art" / "gui_layout_preview.png"

W, H = 256, 212          # размер GUI
ATLAS_Y = 214            # где начинаются спрайты

# --- общая раскладка ---------------------------------------------------------------------------------------------
FRAME = (4, 14, 251, 126)            # основная панель
PLAQUE = (60, 2, 195, 17)            # табличка с названием
INV = (40, 122, 215, 207)            # панель инвентаря игрока
PLAYER_X, PLAYER_Y, HOTBAR_Y = 47, 131, 189
ENERGY = (28, 99, 180, 6)            # x, y, ширина, высота
BAR2 = (28, 110, 180, 6)
RACK = (214, 22, 241, 97)            # стойка апгрейдов
UPG_X = 220
UPG_Y = {"speed": 29, "battery": 53, "eff": 77}

# --- палитра «Латунь» --------------------------------------------------------------------------------------------
def hx(s):
    s = s.lstrip("#")
    return tuple(int(s[i:i + 2], 16) for i in (0, 2, 4)) + (255,)


P = {k: hx(v) for k, v in dict(
    out="1a0f05", dark="3a2410", shade="5a3a10", mid="c9902e", mid2="a8741f", hi="ffe39a", hi2="ffd36a",
    field="f0bf50", field2="e6b244", dot="d9a13a", line="b98826", ped="b86f2c", ped_d="8a4d1a", ped_in="7a4a20",
    bar="3a2410", baredge="7a4a1a", acc="7a3f12", rivet="fff3c0", rivet_d="7a5a18",
    glass="2a1a0c", plaq="c7ccd2", plaq_hi="e6e9ee", plaq_lo="7d838b", plaq_out="22262c",
    inv="c9c9c9", inv_w="ffffff", inv_d="373737", slot="8b8b8b",
    water="2f8fbf", water_d="1c5a7d", sky="cfe9f5", sky2="a8d4ea", flame="ff8a2a", flame2="ffd24a", mana="b072ff",
).items()}


# --- спецификация механизмов -------------------------------------------------------------------------------------
# slots: роль -> (x, y); group: плита из ячеек 18x18; rack: есть ли стойка апгрейдов; widgets: прямоугольники динамики
MACHINES = {
    "fisher_mk1": dict(
        pipes=[("h", 53, 70, 42), ("h", 53, 70, 70), ("h", 171, 188, 54)],
        title="Рыболов MK-1", rack=False, decor="fish",
        slots=dict(rod=(34, 34), battery=(34, 62), output=(190, 48)),
        widgets=dict(pond=(72, 28, 96, 60)),
    ),
    "fisher": dict(
        pipes=[("h", 53, 70, 42), ("h", 53, 70, 70), ("h", 171, 188, 54)],
        title="Авто-Рыболов MK-2", rack=True, decor="fish",
        slots=dict(rod=(34, 34), core=(34, 62), output=(190, 48)),
        widgets=dict(pond=(72, 28, 96, 60)),
    ),
    "extractor": dict(
        pipes=[("h", 81, 97, 55), ("h", 119, 140, 55)],
        title="Экстрактор", rack=True, decor="drop",
        slots=dict(input=(62, 49), output=(140, 49)),
        widgets=dict(arrow=(96, 50, 24, 17)),
    ),
    "excavator": dict(
        pipes=[("h", 93, 110, 55)],
        title="Экскаватор", rack=True, decor="gear",
        slots=dict(out0=(112, 30), out1=(130, 30), out2=(148, 30), out3=(112, 48), out4=(130, 48), out5=(148, 48),
                   out6=(112, 66), out7=(130, 66), out8=(148, 66)),
        groups=[("grid", 112, 30, 3, 3)],
        widgets=dict(arrow=(70, 50, 24, 17)),
    ),
    "centrifuge": dict(
        pipes=[("h", 43, 50, 40), ("h", 43, 50, 68), ("h", 103, 110, 55), ("h", 150, 162, 40), ("h", 150, 162, 68), ("h", 182, 189, 40), ("h", 182, 189, 68)],
        title="Центрифуга", rack=True, decor="drop",
        slots=dict(raw_in=(52, 34), raw_out=(52, 62), distill_empty=(164, 34), distill_full=(164, 62),
                   mineral0=(112, 36), mineral1=(130, 36), mineral2=(112, 54), mineral3=(130, 54)),
        groups=[("minerals", 112, 36, 2, 2)],
        widgets=dict(tank_raw=(30, 28, 12, 58), tank_dist=(190, 28, 12, 58), arrow=(80, 50, 24, 17)),
    ),
    "synthesizer": dict(
        pipes=[("h", 43, 50, 40), ("h", 43, 50, 68), ("h", 131, 144, 55), ("h", 162, 176, 55)],
        title="Гидротермальный синтезатор", rack=True, decor="crystal",
        slots=dict(fluid_in=(52, 34), fluid_out=(52, 62), input_a=(84, 34), input_b=(84, 62),
                   output1=(146, 34), output2=(146, 62), output3=(178, 48)),
        widgets=dict(tank_lava=(30, 28, 12, 58), arrow=(108, 50, 24, 17)),
    ),
    "flower_collector": dict(
        pipes=[("h", 89, 110, 55)],
        title="Цветолов", rack=True, decor="flower",
        slots=dict(out1=(112, 50), out2=(130, 50), out3=(148, 50)),
        groups=[("row", 112, 50, 3, 1)],
        widgets=dict(arrow=(66, 50, 24, 17)),
    ),
    "mana_fabricator": dict(
        pipes=[("h", 93, 108, 55)],
        title="Мана-Фабрикатор", rack=True, decor="mana",
        slots=dict(),
        widgets=dict(arrow=(70, 50, 24, 17), crystal=(110, 32, 48, 48)),
    ),
    "fish_generator": dict(
        pipes=[("h", 65, 90, 54)],
        title="Рыбный генератор", rack=False, decor="fish",
        slots=dict(fuel=(46, 48)),
        widgets=dict(flames=(92, 30, 80, 56)),
    ),
}


# --- примитивы ---------------------------------------------------------------------------------------------------
class Canvas:
    def __init__(self):
        self.im = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
        self.d = ImageDraw.Draw(self.im)

    def r(self, x0, y0, x1, y1, c):
        self.d.rectangle([x0, y0, x1, y1], fill=c)

    def ro(self, x0, y0, x1, y1, c):
        self.d.rectangle([x0, y0, x1, y1], outline=c)

    def px(self, x, y, c):
        self.d.point((x, y), c)

    def bevel(self, x0, y0, x1, y1, light, dark):
        """Объёмная кромка: свет сверху и слева, тень снизу и справа."""
        self.d.line([(x0, y0), (x1, y0)], fill=light)
        self.d.line([(x0, y0), (x0, y1)], fill=light)
        self.d.line([(x0, y1), (x1, y1)], fill=dark)
        self.d.line([(x1, y0), (x1, y1)], fill=dark)

    def rivet(self, x, y):
        self.r(x, y, x + 1, y + 1, P["rivet_d"])
        self.px(x, y, P["rivet"])


def frame(c, rnd):
    x0, y0, x1, y1 = FRAME
    c.r(x0, y0, x1, y1, P["out"])
    c.r(x0 + 1, y0 + 1, x1 - 1, y1 - 1, P["mid"])
    c.bevel(x0 + 1, y0 + 1, x1 - 1, y1 - 1, P["hi"], P["mid2"])
    c.ro(x0 + 2, y0 + 2, x1 - 2, y1 - 2, P["mid2"])
    c.r(x0 + 5, y0 + 5, x1 - 5, y1 - 5, P["out"])
    c.r(x0 + 6, y0 + 6, x1 - 6, y1 - 6, P["field"])
    # внутренняя «врезка»: тень сверху-слева, свет снизу-справа
    c.bevel(x0 + 6, y0 + 6, x1 - 6, y1 - 6, P["shade"], P["hi2"])
    # фактура: тонкие диагонали и крупинки
    for y in range(y0 + 8, y1 - 7):
        for x in range(x0 + 8, x1 - 7):
            if (x + y) % 6 == 0:
                c.px(x, y, P["field2"])
    for _ in range(90):
        c.px(rnd.randint(x0 + 8, x1 - 8), rnd.randint(y0 + 8, y1 - 8), P["dot"])
    # заклёпки по рамке
    for x in range(x0 + 14, x1 - 12, 28):
        c.rivet(x, y0 + 2)
        c.rivet(x, y1 - 3)
    for y in range(y0 + 22, y1 - 20, 30):
        c.rivet(x0 + 2, y)
        c.rivet(x1 - 3, y)
    # боковой орнамент: ромбики
    for y in range(y0 + 14, y1 - 12, 12):
        for x in (x0 + 11, x1 - 11):
            c.px(x, y, P["acc"]); c.px(x - 1, y + 1, P["acc"]); c.px(x + 1, y + 1, P["acc"]); c.px(x, y + 2, P["acc"])
    # угловые камни
    for gx, gy in ((x0 - 1, y0 - 1), (x1 - 5, y0 - 1), (x0 - 1, y1 - 5), (x1 - 5, y1 - 5)):
        c.r(gx, gy, gx + 6, gy + 6, P["out"])
        c.r(gx + 1, gy + 1, gx + 5, gy + 5, P["mid"])
        c.r(gx + 2, gy + 2, gx + 4, gy + 4, P["hi"])
        c.px(gx + 3, gy + 3, P["field"])
    # боковые вставки (скобы)
    for y in (58, 76):
        for x in (x0 - 1, x1 - 5):
            c.r(x, y, x + 5, y + 8, P["out"])
            c.r(x + 1, y + 1, x + 4, y + 7, P["mid"])
            c.r(x + 1, y + 1, x + 4, y + 2, P["hi"])
            c.px(x + 2, y + 4, P["out"]); c.px(x + 3, y + 4, P["out"])


def plaque(c):
    x0, y0, x1, y1 = PLAQUE
    c.r(x0, y0, x1, y1, P["plaq_out"])
    c.r(x0 + 1, y0 + 1, x1 - 1, y1 - 1, P["plaq_lo"])
    c.r(x0 + 2, y0 + 2, x1 - 2, y1 - 2, P["plaq"])
    c.d.line([(x0 + 2, y0 + 2), (x1 - 2, y0 + 2)], fill=P["plaq_hi"])
    c.d.line([(x0 + 2, y1 - 2), (x1 - 2, y1 - 2)], fill=P["plaq_lo"])
    for x in (x0 + 3, x1 - 4):
        c.rivet(x, y0 + 5)
    # золотые торцы
    for x in (x0 - 3, x1 - 1):
        c.r(x, y0 + 2, x + 3, y1 - 2, P["out"])
        c.r(x + 1, y0 + 3, x + 2, y1 - 3, P["mid"])


def inventory(c):
    x0, y0, x1, y1 = INV
    for x in (x0 + 10, x1 - 16):  # клипсы к основной панели
        c.r(x, 120, x + 5, 130, P["out"])
        c.r(x + 1, 121, x + 4, 129, P["mid"])
        c.r(x + 1, 121, x + 4, 122, P["hi"])
        c.px(x + 2, 125, P["out"]); c.px(x + 3, 125, P["out"])
    c.r(x0, y0 + 4, x1, y1, P["inv_d"])
    c.r(x0 + 1, y0 + 5, x1 - 1, y1 - 1, P["inv_w"])
    c.r(x0 + 2, y0 + 6, x1 - 2, y1 - 2, P["inv"])
    for row in range(3):
        for col in range(9):
            cell(c, PLAYER_X + 18 * col - 1, PLAYER_Y + 18 * row - 1)
    for col in range(9):
        cell(c, PLAYER_X + 18 * col - 1, HOTBAR_Y - 1)


def cell(c, x, y):
    """Серая ячейка инвентаря 18x18 (x, y левый верхний угол ячейки)."""
    c.r(x, y, x + 17, y + 17, P["inv_w"])
    c.r(x, y, x + 16, y + 16, P["inv_d"])
    c.r(x + 1, y + 1, x + 16, y + 16, P["slot"])


def pedestal(c, x, y, tint=None):
    """Пьедестал под слотом: предмет лежит в (x, y), рамка 22x22 вокруг."""
    X, Y = x - 3, y - 3
    c.r(X - 1, Y - 1, X + 22, Y + 23, P["out"])
    c.r(X, Y, X + 21, Y + 21, P["ped_d"])
    c.r(X + 1, Y + 1, X + 20, Y + 20, P["ped"])
    c.bevel(X + 1, Y + 1, X + 20, Y + 20, P["hi2"], P["ped_d"])
    c.r(X + 3, Y + 3, X + 18, Y + 18, P["ped_in"])
    c.r(X + 3, Y + 3, X + 18, Y + 3, P["acc"])
    c.r(X + 3, Y + 3, X + 3, Y + 18, P["acc"])
    for ox, oy in ((X + 1, Y + 1), (X + 19, Y + 1), (X + 1, Y + 19), (X + 19, Y + 19)):
        c.px(ox, oy, tint or P["rivet"])


def plate(c, x, y, cols, rows):
    """Плита из ячеек 18x18 (выходы 3x3, 2x2 и в ряд). (x, y) верх-лево первого предмета."""
    X0, Y0 = x - 3, y - 3
    X1, Y1 = x + 18 * cols + 1, y + 18 * rows + 1
    c.r(X0 - 1, Y0 - 1, X1 + 1, Y1 + 1, P["out"])
    c.r(X0, Y0, X1, Y1, P["ped_d"])
    c.bevel(X0, Y0, X1, Y1, P["hi2"], P["ped_d"])
    for rr in range(rows):
        for cc in range(cols):
            sx, sy = x - 1 + 18 * cc, y - 1 + 18 * rr
            c.r(sx, sy, sx + 17, sy + 17, P["ped_d"])
            c.r(sx + 1, sy + 1, sx + 16, sy + 16, P["ped_in"])
            c.r(sx + 1, sy + 1, sx + 16, sy + 1, P["acc"])
            c.r(sx + 1, sy + 1, sx + 1, sy + 16, P["acc"])


def bar_channel(c, x, y, w, h, tick=True):
    c.r(x - 2, y - 2, x + w + 1, y + h + 1, P["out"])
    c.r(x - 1, y - 1, x + w, y + h, P["baredge"])
    c.r(x, y, x + w - 1, y + h - 1, P["bar"])
    c.r(x, y, x + w - 1, y, P["out"])
    if tick:
        for tx in range(x + 18, x + w - 1, 18):
            c.px(tx, y - 2, P["hi"])


def bolt_icon(c, x, y, color):
    for dx, dy in ((2, 0), (1, 1), (1, 2), (0, 3), (1, 3), (2, 3), (1, 4), (1, 5), (0, 6)):
        c.px(x + dx, y + dy, color)


def flame_icon(c, x, y):
    for dx, dy, col in ((1, 0, "ffd24a"), (0, 2, "ff8a2a"), (1, 2, "ffd24a"), (2, 2, "ff8a2a"), (0, 3, "ff6a1a"),
                        (1, 3, "ffb83a"), (2, 3, "ff6a1a"), (0, 4, "ff6a1a"), (1, 4, "ff6a1a"), (2, 4, "ff6a1a")):
        c.px(x + dx, y + dy, hx(col))


def drop_icon(c, x, y):
    for dx, dy in ((1, 0), (1, 1), (0, 2), (1, 2), (2, 2), (0, 3), (1, 3), (2, 3), (1, 4)):
        c.px(x + dx, y + dy, hx("7a3f12"))


def arrow_outline(c, x, y):
    pts = [(x, y + 5), (x + 15, y + 5), (x + 15, y), (x + 23, y + 8), (x + 15, y + 16), (x + 15, y + 11), (x, y + 11)]
    c.d.polygon([(px_ + 1, py_ + 1) for px_, py_ in pts], fill=P["out"])
    c.d.polygon(pts, fill=P["bar"], outline=P["baredge"])


def tank_frame(c, x, y, w, h):
    """Стеклянный резервуар: (x, y, w, h) это внутренняя область для жидкости."""
    c.r(x - 3, y - 3, x + w + 2, y + h + 2, P["out"])
    c.r(x - 2, y - 2, x + w + 1, y + h + 1, P["mid"])
    c.bevel(x - 2, y - 2, x + w + 1, y + h + 1, P["hi"], P["mid2"])
    c.r(x - 1, y - 1, x + w, y + h, P["glass"])
    c.r(x, y, x + w - 1, y + h - 1, P["bar"])
    c.d.line([(x + 1, y + 1), (x + 1, y + h - 2)], fill=hx("6a4a24"))  # блик стекла
    for ty in range(y + 6, y + h - 2, 10):
        c.px(x + w, ty, P["hi"]); c.px(x + w + 1, ty, P["hi"])
    c.rivet(x + w // 2 - 1, y - 2)
    c.rivet(x + w // 2 - 1, y + h)


def pond_window(c, x, y, w, h):
    """Окно пруда: рама с винтами, небо внутри. Воду, поплавок и рыбу рисует Java."""
    c.r(x - 4, y - 4, x + w + 3, y + h + 3, P["out"])
    c.r(x - 3, y - 3, x + w + 2, y + h + 2, P["mid"])
    c.bevel(x - 3, y - 3, x + w + 2, y + h + 2, P["hi"], P["mid2"])
    c.r(x - 1, y - 1, x + w, y + h, P["out"])
    for yy in range(y, y + h):
        t = (yy - y) / max(1, h - 1)
        col = tuple(int(a + (b - a) * t) for a, b in zip(P["sky"][:3], P["sky2"][:3])) + (255,)
        c.d.line([(x, yy), (x + w - 1, yy)], fill=col)
    # облачка
    for cx, cy in ((x + 14, y + 8), (x + w - 28, y + 14)):
        c.r(cx, cy, cx + 8, cy + 2, hx("ffffff"))
        c.r(cx + 2, cy - 1, cx + 6, cy, hx("ffffff"))
    for sx, sy in ((x - 2, y - 2), (x + w + 1, y - 2), (x - 2, y + h + 1), (x + w + 1, y + h + 1)):
        c.r(sx, sy, sx + 1, sy + 1, P["rivet_d"]); c.px(sx, sy, P["rivet"])


def flame_box(c, x, y, w, h):
    c.r(x - 3, y - 3, x + w + 2, y + h + 2, P["out"])
    c.r(x - 2, y - 2, x + w + 1, y + h + 1, P["mid"])
    c.bevel(x - 2, y - 2, x + w + 1, y + h + 1, P["hi"], P["mid2"])
    c.r(x - 1, y - 1, x + w, y + h, P["out"])
    c.r(x, y, x + w - 1, y + h - 1, hx("1c110a"))
    # кирпичная кладка топки
    for yy in range(y, y + h, 6):
        c.d.line([(x, yy), (x + w - 1, yy)], fill=hx("2c1c10"))
        off = 8 if ((yy - y) // 6) % 2 else 0
        for xx in range(x + off, x + w, 16):
            c.d.line([(xx, yy), (xx, min(y + h - 1, yy + 5))], fill=hx("2c1c10"))
    # колосник
    c.r(x, y + h - 5, x + w - 1, y + h - 1, hx("3a2a1a"))
    for xx in range(x + 2, x + w - 2, 5):
        c.r(xx, y + h - 4, xx + 2, y + h - 2, hx("0f0905"))


def crystal_pad(c, x, y, w, h):
    c.r(x - 3, y - 3, x + w + 2, y + h + 2, P["out"])
    c.r(x - 2, y - 2, x + w + 1, y + h + 1, P["mid"])
    c.bevel(x - 2, y - 2, x + w + 1, y + h + 1, P["hi"], P["mid2"])
    c.r(x - 1, y - 1, x + w, y + h, P["out"])
    c.r(x, y, x + w - 1, y + h - 1, hx("241438"))
    for i in range(0, w, 6):
        c.d.line([(x + i, y), (x + i, y + h - 1)], fill=hx("2d1a46"))
    for i in range(0, h, 6):
        c.d.line([(x, y + i), (x + w - 1, y + i)], fill=hx("2d1a46"))


def pipe_h(c, x0, x1, y):
    """Горизонтальная латунная труба толщиной 4 между элементами (рисуется под пьедесталами)."""
    c.r(x0, y - 1, x1, y + 4, P["out"])
    c.r(x0, y, x1, y + 3, P["mid2"])
    c.d.line([(x0, y), (x1, y)], fill=P["hi2"])
    c.d.line([(x0, y + 3), (x1, y + 3)], fill=P["shade"])
    for x in range(x0 + 3, x1 - 1, 9):
        c.px(x, y + 1, P["rivet"])


def pipe_v(c, x, y0, y1):
    c.r(x - 1, y0, x + 4, y1, P["out"])
    c.r(x, y0, x + 3, y1, P["mid2"])
    c.d.line([(x, y0), (x, y1)], fill=P["hi2"])
    c.d.line([(x + 3, y0), (x + 3, y1)], fill=P["shade"])


def dial(c, cx, cy):
    """Круглый манометр 11x11 со стрелкой: просто деталь, ничего не показывает."""
    c.d.ellipse([cx - 5, cy - 5, cx + 5, cy + 5], fill=P["out"])
    c.d.ellipse([cx - 4, cy - 4, cx + 4, cy + 4], fill=P["mid"])
    c.d.ellipse([cx - 3, cy - 3, cx + 3, cy + 3], fill=hx("fff3d0"))
    for ang in range(-135, 136, 45):
        px_ = cx + int(round(math.cos(math.radians(ang - 90)) * 3))
        py_ = cy + int(round(math.sin(math.radians(ang - 90)) * 3))
        c.px(px_, py_, P["acc"])
    c.d.line([(cx, cy), (cx + 2, cy - 2)], fill=hx("c0281e"))
    c.px(cx, cy, P["out"])


def vent(c, x, y):
    c.r(x, y, x + 10, y + 11, P["out"])
    for i in range(4):
        c.r(x + 1, y + 1 + i * 3, x + 9, y + 2 + i * 3, P["shade"])
        c.d.line([(x + 1, y + 1 + i * 3), (x + 9, y + 1 + i * 3)], fill=P["mid2"])


GLYPHS = {
    "rod": [(0, 8), (1, 7), (2, 6), (3, 5), (4, 4), (5, 3), (6, 2), (7, 1), (8, 0), (8, 1), (8, 2), (8, 3), (7, 4), (6, 4)],
    "battery": [(2, 0), (3, 0), (1, 1), (4, 1), (1, 2), (4, 2), (1, 3), (4, 3), (1, 4), (4, 4), (1, 5), (4, 5), (1, 6), (4, 6),
                (1, 7), (4, 7), (2, 8), (3, 8), (3, 2), (2, 4), (3, 4), (2, 6)],
    "core": [(3, 0), (4, 0), (2, 1), (5, 1), (1, 2), (6, 2), (1, 3), (6, 3), (1, 4), (6, 4), (1, 5), (6, 5), (2, 6), (5, 6), (3, 7), (4, 7),
             (3, 3), (4, 3), (3, 4), (4, 4)],
    "bucket": [(0, 1), (1, 1), (2, 1), (3, 1), (4, 1), (5, 1), (6, 1), (1, 2), (5, 2), (1, 3), (5, 3), (1, 4), (5, 4), (2, 5), (4, 5), (3, 5)],
    "fish": [(0, 3), (1, 2), (2, 1), (3, 1), (4, 1), (5, 2), (6, 3), (5, 4), (4, 5), (3, 5), (2, 5), (1, 4), (7, 1), (8, 0), (7, 5), (8, 6),
             (8, 2), (8, 3), (8, 4)],
    "input": [(3, 0), (3, 1), (3, 2), (3, 3), (3, 4), (2, 3), (4, 3), (1, 2), (5, 2), (0, 1), (6, 1)],
    "output": [(0, 0), (1, 0), (2, 0), (3, 0), (4, 0), (0, 4), (4, 4), (0, 1), (4, 1), (0, 2), (4, 2), (0, 3), (4, 3), (1, 4), (2, 4), (3, 4), (2, 2)],
    "speed": [(0, 0), (1, 1), (2, 2), (1, 3), (0, 4), (4, 0), (5, 1), (6, 2), (5, 3), (4, 4)],
    "eff": [(3, 0), (2, 1), (4, 1), (1, 2), (5, 2), (1, 3), (5, 3), (2, 4), (4, 4), (3, 5), (3, 2), (3, 3), (3, 4)],
    "fuel": [(3, 0), (2, 1), (4, 1), (2, 2), (4, 2), (1, 3), (5, 3), (1, 4), (5, 4), (2, 5), (3, 5), (4, 5), (3, 3)],
}
ROLE_GLYPH = {
    "rod": "rod", "battery": "battery", "core": "core", "raw_in": "bucket", "distill_empty": "bucket", "fluid_in": "bucket",
    "input": "input", "input_a": "input", "input_b": "input", "fuel": "fuel",
}


def glyph(c, x, y, name):
    """Едва заметный силуэт в пустом слоте. Предмет рисуется поверх и закрывает его."""
    pts = GLYPHS[name]
    w = max(px_ for px_, _ in pts) + 1
    h = max(py_ for _, py_ in pts) + 1
    ox, oy = x + (16 - w) // 2, y + (16 - h) // 2
    for dx, dy in pts:
        c.px(ox + dx, oy + dy, hx("6a3e18"))


def decal(c, kind, rnd):
    """Тихий рисунок на поле в свободных местах: что делает механизм."""
    col = P["line"]

    def spot(x, y, pts):
        for dx, dy in pts:
            c.px(x + dx, y + dy, col)

    fish = [(0, 2), (1, 1), (2, 1), (3, 1), (4, 2), (5, 3), (6, 2), (6, 4), (1, 3), (2, 3), (3, 3), (4, 3), (7, 1), (7, 5)]
    drop = [(2, 0), (2, 1), (1, 2), (2, 2), (3, 2), (0, 3), (1, 3), (2, 3), (3, 3), (4, 3), (1, 4), (2, 4), (3, 4), (2, 5)]
    gear = [(2, 0), (3, 0), (0, 2), (1, 1), (4, 1), (5, 2), (0, 3), (5, 3), (1, 4), (4, 4), (2, 5), (3, 5), (2, 2), (3, 3)]
    gem = [(2, 0), (1, 1), (3, 1), (0, 2), (4, 2), (1, 3), (3, 3), (2, 4)]
    flower = [(2, 0), (1, 1), (3, 1), (2, 2), (0, 2), (4, 2), (1, 3), (3, 3), (2, 4)]
    swirl = [(2, 0), (3, 0), (4, 1), (4, 2), (3, 3), (2, 3), (1, 2), (2, 1), (3, 2)]
    shapes = dict(fish=fish, drop=drop, gear=gear, crystal=gem, flower=flower, mana=swirl)
    for (x, y) in ((16, 98), (222, 100), (16, 36), (230, 36)):
        if y > 90 and x > 200:
            continue
        spot(x, y, shapes[kind])


# --- спрайты динамики --------------------------------------------------------------------------------------------
def sprites(c):
    # 0: заливка стрелки 24x17 (янтарь с бликами), на ATLAS_Y
    x, y = 0, ATLAS_Y
    pts = [(x, y + 5), (x + 15, y + 5), (x + 15, y), (x + 23, y + 8), (x + 15, y + 16), (x + 15, y + 11), (x, y + 11)]
    c.d.polygon(pts, fill=hx("ffb52e"))
    c.d.line([(x, y + 6), (x + 15, y + 6)], fill=hx("ffe9a0"))
    c.d.line([(x, y + 10), (x + 15, y + 10)], fill=hx("d9831a"))
    c.d.line([(x + 15, y + 2), (x + 21, y + 8)], fill=hx("ffe9a0"))
    # 1: кадры пламени 12x16, 4 шт.: внешний язык, оранжевая середина, жёлтый и белый жар
    layers = [("ff4d14", 0), ("ff8a2a", 1), ("ffd24a", 2), ("fff0b0", 3)]
    for i in range(4):
        fx = 30 + 12 * i
        phase = i * math.pi / 2
        for shade, shrink in layers:
            for row in range(16):
                t = row / 15.0
                half = 5.4 * (1 - t) ** 0.75 * (1 + 0.22 * math.sin(phase + t * 6)) - shrink * (1.1 - 0.5 * t)
                if half < 0:
                    continue
                centre = 6 + 1.6 * math.sin(phase + t * 3.2) * t
                lo, hi = int(round(centre - half)), int(round(centre + half))
                if shrink == 3 and row > 8:
                    continue
                c.d.line([(fx + lo, ATLAS_Y + 15 - row), (fx + hi, ATLAS_Y + 15 - row)], fill=hx(shade))
    # 2: рыбка 10x6
    fx, fy = 80, ATLAS_Y
    for dx, dy in ((2, 1), (3, 1), (4, 1), (5, 1), (1, 2), (2, 2), (3, 2), (4, 2), (5, 2), (6, 2), (1, 3), (2, 3), (3, 3),
                   (4, 3), (5, 3), (6, 3), (2, 4), (3, 4), (4, 4), (5, 4), (7, 1), (8, 0), (7, 4), (8, 5), (7, 2), (7, 3), (8, 2), (8, 3)):
        c.px(fx + dx, fy + dy, hx("ffffff") if (dx + dy) % 2 else hx("ffe9a0"))
    c.px(fx + 2, fy + 2, hx("22262c"))
    # 3: огранённый кристалл маны 24x24, 3 кадра (яркость растёт к середине цикла)
    tones = [("5a2fb0", "8a54e8", "b072ff", "d9b8ff"), ("6a3cc8", "9d66ff", "c490ff", "efe0ff"), ("5a2fb0", "8a54e8", "b072ff", "d9b8ff")]
    for i, (dark, mid, light, glint) in enumerate(tones):
        cx0, cy0 = 24 * i, ATLAS_Y + 18
        top, belt, bottom = cy0 + 1, cy0 + 9, cy0 + 22
        mid_x = cx0 + 12
        outline = [(mid_x, top), (cx0 + 20, belt), (mid_x, bottom), (cx0 + 4, belt)]
        c.d.polygon(outline, fill=hx("1a0f3a"))
        inner = [(mid_x, top + 1), (cx0 + 18, belt), (mid_x, bottom - 1), (cx0 + 6, belt)]
        c.d.polygon(inner, fill=hx(mid))
        c.d.polygon([(mid_x, top + 1), (cx0 + 6, belt), (mid_x, belt + 1)], fill=hx(light))          # левая верхняя грань
        c.d.polygon([(mid_x, top + 1), (cx0 + 18, belt), (mid_x, belt + 1)], fill=hx(mid))           # правая верхняя
        c.d.polygon([(cx0 + 6, belt), (mid_x, belt + 1), (mid_x, bottom - 1)], fill=hx(mid))         # левая нижняя
        c.d.polygon([(cx0 + 18, belt), (mid_x, belt + 1), (mid_x, bottom - 1)], fill=hx(dark))       # правая нижняя
        c.d.line([(cx0 + 8, belt - 2), (cx0 + 10, belt - 4)], fill=hx(glint))
        c.px(cx0 + 9, belt - 2, hx(glint))


def build_machine(key, spec):
    rnd = random.Random(zlib.crc32(key.encode()))
    c = Canvas()
    frame(c, rnd)
    plaque(c)
    inventory(c)
    decal(c, spec["decor"], rnd)

    ex, ey, ew, eh = ENERGY
    bar_channel(c, ex, ey, ew, eh)
    bolt_icon(c, ex - 11, ey - 1, hx("ffd24a"))
    x2, y2, w2, h2 = spec["widgets"].get("bar2", BAR2)
    bar_channel(c, x2, y2, w2, h2)
    if key == "fish_generator":
        flame_icon(c, x2 - 11, y2)
    elif key in ("flower_collector", "mana_fabricator"):
        c.r(x2 - 10, y2 + 1, x2 - 6, y2 + 5, hx("b072ff"))
    else:
        for i in range(3):
            c.px(x2 - 10 + i * 2, y2 + 2, P["acc"]); c.px(x2 - 9 + i * 2, y2 + 3, P["acc"]); c.px(x2 - 10 + i * 2, y2 + 4, P["acc"])

    if spec["rack"]:
        rx0, ry0, rx1, ry1 = RACK
        c.r(rx0 - 1, ry0 - 1, rx1 + 1, ry1 + 1, P["out"])
        c.r(rx0, ry0, rx1, ry1, P["mid2"])
        c.bevel(rx0, ry0, rx1, ry1, P["shade"], P["hi2"])
        c.r(rx0 + 2, ry0 + 2, rx1 - 2, ry1 - 2, P["field2"])
        tints = dict(speed=hx("ff7a1a"), battery=hx("3dd0ff"), eff=hx("5ce05c"))
        for role, yy in UPG_Y.items():
            pedestal(c, UPG_X, yy, tints[role])
    else:
        # без стойки: пластина с эмблемой, чтобы справа не было пустоты
        rx0, ry0, rx1, ry1 = RACK
        c.r(rx0 + 4, ry0 + 20, rx1 - 4, ry0 + 52, P["mid2"])
        c.bevel(rx0 + 4, ry0 + 20, rx1 - 4, ry0 + 52, P["shade"], P["hi2"])
        decal_big(c, spec["decor"], (rx0 + rx1) // 2 - 3, ry0 + 30)

    for kind, a0, a1, at in spec.get("pipes", []):
        pipe_h(c, a0, a1, at)
    dial(c, 18, 28)
    vent(c, 13, 66)
    for group in spec.get("groups", []):
        _, gx, gy, cols, rows = group
        plate(c, gx, gy, cols, rows)
    group_cells = set()
    for group in spec.get("groups", []):
        _, gx, gy, cols, rows = group
        for rr in range(rows):
            for cc in range(cols):
                group_cells.add((gx + 18 * cc, gy + 18 * rr))
    for role, (sx, sy) in spec["slots"].items():
        if (sx, sy) in group_cells:
            continue
        pedestal(c, sx, sy)
        if role in ROLE_GLYPH:
            glyph(c, sx, sy, ROLE_GLYPH[role])
        elif role.startswith("raw_out") or role.endswith("_out") or role.startswith("output") or role.startswith("distill_full"):
            glyph(c, sx, sy, "output")

    if spec["rack"]:
        for role, yy in UPG_Y.items():
            glyph(c, UPG_X, yy, {"speed": "speed", "battery": "battery", "eff": "eff"}[role])

    wd = spec["widgets"]
    if "arrow" in wd:
        ax, ay, _, _ = wd["arrow"]
        arrow_outline(c, ax, ay)
    for name, (tx, ty, tw, th) in wd.items():
        if name.startswith("tank"):
            tank_frame(c, tx, ty, tw, th)
    if "pond" in wd:
        pond_window(c, *wd["pond"])
    if "flames" in wd:
        flame_box(c, *wd["flames"])
    if "crystal" in wd:
        crystal_pad(c, *wd["crystal"])

    sprites(c)
    return c.im


def decal_big(c, kind, x, y):
    colr = P["shade"]
    maps = dict(
        fish=[(0, 3), (1, 2), (2, 1), (3, 1), (4, 1), (5, 2), (6, 3), (7, 4), (8, 3), (9, 2), (9, 6), (6, 5), (5, 6), (4, 6), (3, 6), (2, 5), (1, 4)],
    )
    for dx, dy in maps.get(kind, maps["fish"]):
        c.px(x + dx - 4, y + dy, colr)


# --- генерация Java ----------------------------------------------------------------------------------------------
def java_layout():
    lines = [
        "package net.aquatech.machines.util;",
        "",
        "// GENERATED by tools/build_machine_guis.py. Do not edit by hand: change the script and rerun it.",
        "",
        "/** Раскладка GUI механизмов: размеры, слоты (левый верх предмета 16x16) и прямоугольники динамики. */",
        "public final class MachineLayout {",
        "",
        f"    public static final int IMAGE_W = {W};",
        f"    public static final int IMAGE_H = {H};",
        f"    public static final int ATLAS_Y = {ATLAS_Y};",
        f"    public static final int PLAYER_X = {PLAYER_X};",
        f"    public static final int PLAYER_Y = {PLAYER_Y};",
        f"    public static final int HOTBAR_Y = {HOTBAR_Y};",
        f"    public static final int ENERGY_X = {ENERGY[0]};",
        f"    public static final int ENERGY_Y = {ENERGY[1]};",
        f"    public static final int ENERGY_W = {ENERGY[2]};",
        f"    public static final int ENERGY_H = {ENERGY[3]};",
        f"    public static final int BAR2_X = {BAR2[0]};",
        f"    public static final int BAR2_Y = {BAR2[1]};",
        f"    public static final int BAR2_W = {BAR2[2]};",
        f"    public static final int BAR2_H = {BAR2[3]};",
        f"    public static final int UPG_X = {UPG_X};",
        f"    public static final int UPG_SPEED_Y = {UPG_Y['speed']};",
        f"    public static final int UPG_BATTERY_Y = {UPG_Y['battery']};",
        f"    public static final int UPG_EFF_Y = {UPG_Y['eff']};",
        f"    public static final int PLAQUE_CENTER_X = {(PLAQUE[0] + PLAQUE[2]) // 2};",
        f"    public static final int PLAQUE_TEXT_Y = {PLAQUE[1] + 5};",
        "",
        "    private MachineLayout() {",
        "    }",
        "",
    ]
    for key, spec in MACHINES.items():
        up = key.upper()
        lines.append(f"    // {spec['title']}")
        for role, (sx, sy) in spec["slots"].items():
            lines.append(f"    public static final int {up}_{role.upper()}_X = {sx};")
            lines.append(f"    public static final int {up}_{role.upper()}_Y = {sy};")
        for name, rect in spec["widgets"].items():
            for part, val in zip("XYWH", rect):
                lines.append(f"    public static final int {up}_{name.upper()}_{part} = {val};")
        lines.append("")
    lines.append("}")
    return "\n".join(lines) + "\n"


def preview(images):
    scale = 2
    cols = 3
    rows = (len(images) + cols - 1) // cols
    sheet = Image.new("RGBA", (cols * (W * scale + 16) + 16, rows * (H * scale + 16) + 16), (50, 52, 62, 255))
    for i, (key, im) in enumerate(images):
        crop = im.crop((0, 0, W, H)).resize((W * scale, H * scale), Image.NEAREST)
        ox = 16 + (i % cols) * (W * scale + 16)
        oy = 16 + (i // cols) * (H * scale + 16)
        sheet.paste(crop, (ox, oy), crop)
        d = ImageDraw.Draw(sheet)
        spec = MACHINES[key]
        for role, (sx, sy) in spec["slots"].items():
            d.rectangle([ox + sx * scale, oy + sy * scale, ox + (sx + 16) * scale - 1, oy + (sy + 16) * scale - 1],
                        outline=(255, 0, 200, 255))
        if spec["rack"]:
            for yy in UPG_Y.values():
                d.rectangle([ox + UPG_X * scale, oy + yy * scale, ox + (UPG_X + 16) * scale - 1, oy + (yy + 16) * scale - 1],
                            outline=(255, 0, 200, 255))
        d.text((ox + 4, oy + H * scale - 12), key, fill=(255, 255, 255, 255))
    PREVIEW.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(PREVIEW)
    print("preview ->", PREVIEW)


def main():
    GUI_DIR.mkdir(parents=True, exist_ok=True)
    made = []
    for key, spec in MACHINES.items():
        im = build_machine(key, spec)
        im.save(GUI_DIR / f"{key}.png")
        made.append((key, im))
    with open(LAYOUT_JAVA, "w", encoding="utf-8", newline="") as out:
        out.write(java_layout())
    print(f"written {len(made)} textures and {LAYOUT_JAVA.name}")
    if "--preview" in sys.argv:
        preview(made)


if __name__ == "__main__":
    main()
