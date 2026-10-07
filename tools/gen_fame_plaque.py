"""Табличка Стены славы: рисует атлас 64x64 и собирает модель fame_plaque.json.

Плотность текселей везде одна: 2 пикселя на единицу модели (32 на блок), как у спрайта рыбы на табличке.
Лицевые грани рисуются одним видом спереди (32x32), боковые режутся из общих полос, поэтому узор волокон
не обрывается на стыках. Развёртка граней считается здесь же, руками UV не правятся.

    python tools/gen_fame_plaque.py                    пишет текстуру и модель в ресурсы мода
    python tools/gen_fame_plaque.py --preview DIR      плюс картинки для проверки глазами
    python tools/gen_fame_plaque.py --preview DIR --no-write
    python tools/gen_fame_plaque.py --no-write --fish lightning_bass --promo docs/assets/images/updates/fame-plaque.webp
"""
import argparse
import io
import json
import math
import random
import re
import zipfile
from pathlib import Path

import numpy as np
from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "mods" / "aquatech-ui" / "src" / "main" / "resources" / "assets" / "aquatech_ui"
TEXTURE_OUT = ASSETS / "textures" / "block" / "fame_plaque.png"
MODEL_OUT = ASSETS / "models" / "block" / "fame_plaque.json"

ATLAS = 64
PPU = 2  # текселей на единицу модели
UV = 16.0 / ATLAS


def rgb(*codes):
    return [tuple(int(c[i:i + 2], 16) for i in (0, 2, 4)) for c in codes]


# красное дерево: от контура до блика
WOOD = rgb("1b0b07", "2e130d", "431b12", "5a2818", "723520", "8c4529", "a85c36", "c47a4a")
# латунь: от тени до зеркального блика
BRASS = rgb("3a2209", "66420f", "8f6018", "bd8a2c", "dcab45", "f1cb70", "fff0aa")
# вода: от дна до пены
SEA = rgb("06182c", "0a2a47", "0e3d61", "14527a", "1c6a93", "2a86ae", "49a8c8", "8bd2e2")
SAND = rgb("8c7a4e", "6e5f3c", "4d422b")
STAR = rgb("c9622f", "eb8a4b")
WEED = rgb("173f2d", "226b45", "3f9a63", "6cc58a")

FACE_SHADE = {"up": 1.0, "down": 0.5, "north": 0.8, "south": 0.8, "east": 0.6, "west": 0.6}


def clamp_idx(idx, ramp):
    return int(max(0, min(len(ramp) - 1, idx)))


def runs(rng, length, tones, lo=3, hi=11):
    """Тоны вдоль линии: участки по lo..hi пикселей, чтобы получались штрихи, а не шум."""
    out = []
    while len(out) < length:
        out += [rng.choice(tones)] * rng.randint(lo, hi)
    return out[:length]


def grain(w, h, along, rng, ramp, tones, lo=3, hi=11):
    """Индексы рампы для полосы w x h; волокна идут вдоль оси `along` ('x' вдоль ширины, 'y' вдоль высоты)."""
    idx = np.zeros((h, w), dtype=int)
    lines = h if along == "x" else w
    length = w if along == "x" else h
    for c in range(lines):
        base = rng.choice(tones)
        line = runs(rng, length, [base, base, base, base - 1, base + 1], lo, hi)
        for p, t in enumerate(line):
            if along == "x":
                idx[c, p] = t
            else:
                idx[p, c] = t
    return np.clip(idx, 0, len(ramp) - 1)


def colorize(idx, ramp):
    out = np.zeros(idx.shape + (3,), np.uint8)
    for i, color in enumerate(ramp):
        out[idx == i] = color
    return out


# ---------------------------------------------------------------- вид спереди 32x32

def paint_sea(rng):
    """Окно 22x17: толща воды, лучи сверху, песок, водоросли, пузырьки. Середина спокойная, чтобы рыба читалась."""
    w, h = 22, 17
    tone = np.zeros((h, w), dtype=int)
    bands = [(0, 5), (1, 4), (4, 3), (9, 2), (13, 1)]  # (первая строка, тон)
    for y in range(h):
        for first, t in bands:
            if y >= first:
                tone[y, :] = t
    for (_, upper), (first, lower) in zip(bands[1:], bands[2:]):  # на стыке полос одна строка шахматки
        for x in range(w):
            tone[first - 1, x] = upper if x % 2 == 0 else lower
    # лучи света: наклонные полосы, к краю шахматка
    for x0 in (2.5, 9.5, 16.5):
        for y in range(1, 11):
            cx = x0 + 0.45 * y
            for x in range(w):
                d = abs(x - cx)
                if d < 1.0 or (d < 2.0 and (x + y) % 2 == 0 and y < 8):
                    if y < 9 or d < 1.0 and y % 2 == 0:
                        tone[y, x] += 1
    tone[0, :] = 5
    for _ in range(3):  # пенные чёрточки на поверхности
        x = rng.randint(1, w - 4)
        tone[0, x:x + rng.randint(2, 3)] = 6
    rgbimg = colorize(np.clip(tone, 0, len(SEA) - 1), SEA)

    # песок: неровная кромка, три слоя тона
    heights = runs(rng, w, [2, 3], 3, 6)
    for x in range(w):
        top = h - heights[x]
        for y in range(top, h):
            rgbimg[y, x] = SAND[0] if y == top else SAND[2] if y == h - 1 else SAND[1]
    for _ in range(3):  # камушки
        x = rng.randint(3, w - 4)
        rgbimg[h - heights[x], x] = tuple(min(255, c + 22) for c in SAND[0])

    # водоросли у краёв: плавная S-кривая, светлее к верхушке
    def weed(x0, height, phase):
        base = h - heights[min(max(x0, 0), w - 1)]
        for k in range(height):
            y = base - 1 - k
            x = x0 + int(round(0.9 * math.sin(k * 0.85 + phase)))
            if 0 <= x < w and y >= 0:
                tip = k >= height - 2
                rgbimg[y, x] = WEED[3] if tip and k == height - 1 else WEED[2] if tip else WEED[1] if k % 3 else WEED[0]

    for x0, height, phase in ((1, 8, 0.0), (3, 6, 1.7), (18, 6, 0.6), (20, 9, 2.4)):
        weed(x0, height, phase)

    top = min(h - heights[x] for x in (8, 9, 10)) - 1  # морская звезда на песке
    rgbimg[top - 1, 9] = STAR[1]
    for x, t in ((8, 0), (9, 1), (10, 0)):
        rgbimg[top, x] = STAR[t]

    for x, y, t in ((16, 3, 7), (17, 2, 6), (4, 6, 6), (3, 8, 7), (15, 8, 6)):  # пузырьки
        rgbimg[y, x] = SEA[t]

    # тень от рамы: сверху и слева темнее
    out = rgbimg.astype(float)
    out[:, 0] *= 0.78
    out[0, :] *= 0.9
    out[:, -1] *= 0.92
    return np.clip(out, 0, 255).astype(np.uint8)


def paint_front(rng):
    """Лицевая сторона: контур, валик, канавка, латунный ободок окна и табличка с подписью."""
    f = np.zeros((32, 32, 3), np.uint8)

    def put(x, y, c):
        f[y, x] = c

    # поле: канавка между валиком и ободком
    for y in range(32):
        for x in range(32):
            put(x, y, WOOD[2])
    for x in range(3, 29):
        put(x, 3, WOOD[1] if rng.random() < 0.18 else WOOD[2])
    for y in range(3, 31):
        put(3, y, WOOD[1] if rng.random() < 0.18 else WOOD[2])
        put(28, y, WOOD[3] if rng.random() < 0.8 else WOOD[2])

    # валик: контур, светлая линия и основа; свет слева сверху
    top_hi = runs(rng, 30, [5, 5, 5, 6, 5, 4])
    top_base = runs(rng, 30, [3, 3, 4, 3, 2])
    left_hi = runs(rng, 30, [5, 5, 5, 6, 5, 4])
    left_base = runs(rng, 30, [3, 3, 4, 3, 2])
    right_hi = runs(rng, 30, [5, 5, 5, 6, 5, 4])
    right_base = runs(rng, 30, [3, 3, 4, 3, 2])
    for i in range(30):
        put(1 + i, 1, WOOD[top_hi[i]])
        put(1 + i, 2, WOOD[top_base[i]])
        put(1, 1 + i, WOOD[left_hi[i]])
        put(2, 1 + i, WOOD[left_base[i]])
        put(29, 1 + i, WOOD[right_hi[i]])
        put(30, 1 + i, WOOD[right_base[i]])
    # углы сведены на ус: шов тёмной точкой у внутреннего угла
    put(1, 1, WOOD[5]); put(2, 1, WOOD[5]); put(1, 2, WOOD[5]); put(2, 2, WOOD[2])
    put(29, 1, WOOD[5]); put(30, 1, WOOD[4]); put(29, 2, WOOD[2]); put(30, 2, WOOD[3])
    for i in range(32):
        put(i, 0, WOOD[0])
        put(0, i, WOOD[0])
        put(31, i, WOOD[0])
        put(i, 31, WOOD[0])

    # окно: вода
    f[5:22, 5:27] = paint_sea(rng)

    # угловые розетки на валике: латунная шляпка 2x2 со скосом
    for x0, y0 in ((1, 1), (29, 1), (1, 29), (29, 29)):
        put(x0, y0, BRASS[5]); put(x0 + 1, y0, BRASS[4])
        put(x0, y0 + 1, BRASS[3]); put(x0 + 1, y0 + 1, BRASS[2])

    # ободок окна: латунь потемнее, свет сверху слева, тень снизу справа
    for x in range(4, 28):
        put(x, 4, BRASS[5] if x < 9 else BRASS[4] if x < 20 else BRASS[3])
        put(x, 22, BRASS[3] if x < 16 else BRASS[2])
    for y in range(5, 22):
        put(4, y, BRASS[4] if y < 13 else BRASS[3])
        put(27, y, BRASS[3] if y < 10 else BRASS[2])
    put(4, 4, BRASS[6])

    # табличка: ободок со скосом, внутри шлифованная латунь со спокойными штрихами и двумя косыми бликами
    for x in range(4, 28):
        put(x, 23, BRASS[5])
        put(x, 30, BRASS[2] if x > 12 else BRASS[3])
    for y in range(24, 30):
        put(4, y, BRASS[4])
        put(27, y, BRASS[3] if y < 27 else BRASS[2])
    put(4, 23, BRASS[6])
    row_tone = (5, 4, 4, 4, 3, 3)  # сверху светлее, внизу темнее: как у шлифованного металла
    for y in range(24, 30):
        line = runs(rng, 22, [0, 0, 0, 0, 1, -1], 8, 20)
        for i in range(22):
            t = row_tone[y - 24] + line[i]
            if i < 2 and y < 26:  # блик у левого края
                t += 1
            put(5 + i, y, BRASS[clamp_idx(t, BRASS)])
    return f


# ---------------------------------------------------------------- боковые полосы

def region_top(rng):
    idx = grain(32, 8, "x", rng, WOOD, (3, 3, 4, 4, 5))
    idx[0, :] = np.clip(idx[0, :] + 1, 0, 7)  # передняя кромка ловит свет
    return colorize(idx, WOOD)


def region_bottom(rng):
    idx = grain(32, 8, "x", rng, WOOD, (3, 3, 4))
    idx[7, :] = np.clip(idx[7, :] + 1, 0, 7)
    return colorize(idx, WOOD)


def region_side_outer(rng):
    idx = grain(8, 32, "y", rng, WOOD, (3, 3, 4, 4))
    idx[:, 0] = np.clip(idx[:, 0] + 1, 0, 7)
    idx[:, 7] = np.clip(idx[:, 7] + 1, 0, 7)  # грань общая для запада и востока, передняя кромка то справа, то слева
    return colorize(idx, WOOD)


def region_step(w, h, rng):
    idx = grain(w, h, "x" if w >= h else "y", rng, WOOD, (1, 2, 2))
    return colorize(idx, WOOD)


def depth_wood(w, h, rng, front_at):
    """Стенка окна из тёмного дерева: у передней кромки светлее, вглубь темнеет (затенение запечено в текстуру).
    front_at: с какой стороны полосы перёд: 'left'|'right'|'top'|'bottom'."""
    idx = np.zeros((h, w), dtype=int)
    depth_tone = (4, 4, 3, 3, 2, 2, 1)
    depth_along_y = front_at in ("top", "bottom")
    lines, length = (h, w) if depth_along_y else (w, h)  # линия = постоянная глубина, штрихи идут вдоль неё
    for c in range(lines):
        streak = runs(rng, length, [0, 0, 0, 1, -1], 4, 12)
        for p in range(length):
            x, y = (p, c) if depth_along_y else (c, p)
            d = {"left": x, "right": w - 1 - x, "top": y, "bottom": h - 1 - y}[front_at]
            idx[y, x] = depth_tone[min(d, len(depth_tone) - 1)] + streak[p]
    return colorize(np.clip(idx, 0, len(WOOD) - 1), WOOD)


def solid(w, h, color):
    return np.full((h, w, 3), color, np.uint8)


def build_regions(rng):
    """Имя -> картинка. 'front' лежит в углу (0, 0), остальное укладывается рядом."""
    return {
        "front": paint_front(rng),
        "top": region_top(rng),
        "bottom": region_bottom(rng),
        "side_outer": region_side_outer(rng),
        "step_top": region_step(32, 1, rng),
        "step_v": region_step(1, 29, rng),
        "soffit": depth_wood(26, 7, rng, "bottom"),
        "sill": depth_wood(26, 7, rng, "top"),
        "wall_e": depth_wood(7, 17, rng, "right"),
        "wall_w": depth_wood(7, 17, rng, "left"),
        "bz_h_up": solid(24, 1, BRASS[5]),
        "bz_h_dn": solid(24, 1, BRASS[1]),
        "bz_v_in": solid(1, 17, BRASS[3]),
        "bz_v_out": solid(1, 17, BRASS[2]),
        "bz_dot": solid(1, 1, BRASS[3]),
        "plate_side": solid(1, 8, BRASS[2]),
    }


def pack(regions):
    """Укладывает полосы по строкам в свободное место атласа; 'front' остаётся в (0, 0)."""
    pos = {"front": (0, 0)}
    free = [(32, 0, 32, 64), (0, 32, 32, 32)]  # правая половина и нижний левый угол
    names = sorted((n for n in regions if n != "front"), key=lambda n: -regions[n].shape[0])
    for rx, ry, rw, rh in free:
        x = y = row_h = 0
        remaining = []
        for n in names:
            h, w = regions[n].shape[:2]
            if x + w > rw:
                x, y, row_h = 0, y + row_h + 1, 0
            if y + h > rh:
                remaining.append(n)
                continue
            pos[n] = (rx + x, ry + y)
            x += w + 1
            row_h = max(row_h, h)
        names = remaining
    if names:
        raise SystemExit("не хватило места в атласе: " + ", ".join(names))
    return pos


# ---------------------------------------------------------------- модель

def north_rect(frm, to):
    x0, x1 = round(PPU * (16 - to[0])), round(PPU * (16 - frm[0]))
    y0, y1 = round(PPU * (16 - to[1])), round(PPU * (16 - frm[1]))
    return (x0, y0, x1 - x0, y1 - y0)


def up_rect(frm, to):
    return (round(PPU * frm[0]), round(PPU * (frm[2] - 12)), round(PPU * (to[0] - frm[0])), round(PPU * (to[2] - frm[2])))


def down_rect(frm, to):
    return (round(PPU * frm[0]), round(PPU * (16 - to[2])), round(PPU * (to[0] - frm[0])), round(PPU * (to[2] - frm[2])))


def west_rect(frm, to):
    return (round(PPU * (frm[2] - 12)), round(PPU * (16 - to[1])), round(PPU * (to[2] - frm[2])), round(PPU * (to[1] - frm[1])))


def east_rect(frm, to):
    return (round(PPU * (16 - to[2])), round(PPU * (16 - to[1])), round(PPU * (to[2] - frm[2])), round(PPU * (to[1] - frm[1])))


def elements():
    """(имя, от, до, {грань: (полоса, прямоугольник в полосе или None для всей полосы)}). Единицы модели.
    Оси: перёд смотрит на север (z меньше), стена на z=16; «слева/справа» считаются глазами зрителя."""
    R = []

    def add(name, frm, to, faces):
        R.append((name, frm, to, faces))

    # рама: внешний валик на всю глубину, канавка и стенки окна глубже
    add("bead_t", (0, 14.5, 12), (16, 16, 16), {
        "north": ("front", north_rect((0, 14.5, 12), (16, 16, 16))),
        "up": ("top", up_rect((0, 14.5, 12), (16, 16, 16))),
        "west": ("side_outer", west_rect((0, 14.5, 12), (16, 16, 16))),
        "east": ("side_outer", east_rect((0, 14.5, 12), (16, 16, 16))),
        "down": ("step_top", None)})
    add("bead_r", (0, 0, 12), (1.5, 14.5, 16), {
        "north": ("front", north_rect((0, 0, 12), (1.5, 14.5, 16))),
        "west": ("side_outer", west_rect((0, 0, 12), (1.5, 14.5, 16))),
        "down": ("bottom", down_rect((0, 0, 12), (1.5, 14.5, 16))),
        "east": ("step_v", None)})
    add("bead_l", (14.5, 0, 12), (16, 14.5, 16), {
        "north": ("front", north_rect((14.5, 0, 12), (16, 14.5, 16))),
        "east": ("side_outer", east_rect((14.5, 0, 12), (16, 14.5, 16))),
        "down": ("bottom", down_rect((14.5, 0, 12), (16, 14.5, 16))),
        "west": ("step_v", None)})
    add("flat_t", (1.5, 13.5, 12.5), (14.5, 14.5, 16), {
        "north": ("front", north_rect((1.5, 13.5, 12.5), (14.5, 14.5, 16))),
        "down": ("soffit", None)})
    add("flat_r", (1.5, 5, 12.5), (2.5, 13.5, 16), {
        "north": ("front", north_rect((1.5, 5, 12.5), (2.5, 13.5, 16))),
        "east": ("wall_e", None)})
    add("flat_l", (13.5, 5, 12.5), (14.5, 13.5, 16), {
        "north": ("front", north_rect((13.5, 5, 12.5), (14.5, 13.5, 16))),
        "west": ("wall_w", None)})
    add("flat_b", (1.5, 0, 12.5), (14.5, 5, 16), {
        "north": ("front", north_rect((1.5, 0, 12.5), (14.5, 5, 16))),
        "up": ("sill", None),
        "down": ("bottom", down_rect((1.5, 0, 12.5), (14.5, 5, 16)))})
    add("sea", (2.5, 5, 14.5), (13.5, 13.5, 15), {
        "north": ("front", north_rect((2.5, 5, 14.5), (13.5, 13.5, 15)))})

    # латунный ободок окна: четыре бруска, выступают на 0.5 над полем
    add("bezel_t", (2, 13.5, 12), (14, 14, 12.5), {
        "north": ("front", north_rect((2, 13.5, 12), (14, 14, 12.5))),
        "up": ("bz_h_up", None), "down": ("bz_h_dn", None),
        "east": ("bz_dot", None), "west": ("bz_dot", None)})
    add("bezel_b", (2, 4.5, 12), (14, 5, 12.5), {
        "north": ("front", north_rect((2, 4.5, 12), (14, 5, 12.5))),
        "up": ("bz_h_up", None),
        "east": ("bz_dot", None), "west": ("bz_dot", None)})
    add("bezel_l", (13.5, 5, 12), (14, 13.5, 12.5), {
        "north": ("front", north_rect((13.5, 5, 12), (14, 13.5, 12.5))),
        "west": ("bz_v_in", None), "east": ("bz_v_out", None)})
    add("bezel_r", (2, 5, 12), (2.5, 13.5, 12.5), {
        "north": ("front", north_rect((2, 5, 12), (2.5, 13.5, 12.5))),
        "east": ("bz_v_in", None), "west": ("bz_v_out", None)})

    # табличка под подпись; перед ней рисует текст рендерер блока
    add("plate", (2, 0.5, 12), (14, 4.5, 12.5), {
        "north": ("front", north_rect((2, 0.5, 12), (14, 4.5, 12.5))),
        "down": ("bz_h_dn", None),
        "east": ("plate_side", None), "west": ("plate_side", None)})
    return R


def rot_x(deg):
    a = math.radians(deg)
    return np.array([[1, 0, 0], [0, math.cos(a), -math.sin(a)], [0, math.sin(a), math.cos(a)]])


def rot_y(deg):
    a = math.radians(deg)
    return np.array([[math.cos(a), 0, math.sin(a)], [0, 1, 0], [-math.sin(a), 0, math.cos(a)]])


SLAB_CENTER = np.array([8.0, 8.0, 14.0])  # табличка лежит у стены, z 12..16
PIVOT = np.array([8.0, 8.0, 8.0])         # предмет вращается вокруг центра клетки


def display_slot(rotation, scale, base=(0.0, 0.0, 0.0), keep_depth=True):
    """Слот display со сдвигом, который ставит табличку по центру вращения: без него она уезжала бы вбок.
    Порядок поворотов как в Minecraft: сначала X, потом Y; сдвиг считается после поворота и масштаба."""
    rot = rot_x(rotation[0]) @ rot_y(rotation[1])
    shift = -scale * (rot @ (SLAB_CENTER - PIVOT))
    if not keep_depth:
        shift[2] = 0.0
    translation = [round(float(v) * 4) / 4 + b for v, b in zip(shift, base)]
    return {"rotation": list(rotation), "translation": translation, "scale": [scale] * 3}


DISPLAY = {
    "gui": display_slot((22, 208, 0), 0.8, keep_depth=False),
    "ground": display_slot((0, 0, 0), 0.3, (0, 3, 0)),
    "fixed": display_slot((0, 180, 0), 1.0),
    "thirdperson_righthand": display_slot((75, 45, 0), 0.375, (0, 2.5, 0)),
    "firstperson_righthand": display_slot((0, 45, 0), 0.4),
}


def build_model(positions, regions):
    out_elements = []
    resolved = []  # (имя, от, до, {грань: (x, y, w, h в пикселях атласа)})
    for name, frm, to, faces in elements():
        px_faces = {}
        for direction, (region, rect) in faces.items():
            ox, oy = positions[region]
            rh, rw = regions[region].shape[:2]
            x, y, w, h = rect if rect else (0, 0, rw, rh)
            if x < 0 or y < 0 or x + w > rw or y + h > rh or w <= 0 or h <= 0:
                raise SystemExit(f"{name}.{direction}: прямоугольник {rect} вне полосы {region} {rw}x{rh}")
            px_faces[direction] = (ox + x, oy + y, w, h)
        resolved.append((name, frm, to, px_faces))
        out_elements.append({
            "name": name,
            "from": list(frm),
            "to": list(to),
            "faces": {d: {"uv": [px / 4 for px in (x, y, x + w, y + h)], "texture": "#0"}
                      for d, (x, y, w, h) in px_faces.items()},
        })
    model = {
        "ambientocclusion": False,
        "texture_size": [ATLAS, ATLAS],
        "textures": {"0": "aquatech_ui:block/fame_plaque", "particle": "aquatech_ui:block/fame_plaque"},
        "elements": out_elements,
        "display": DISPLAY,
    }
    return model, resolved


def dump_model(model):
    text = json.dumps(model, indent=2, ensure_ascii=False)
    # короткие числовые массивы в одну строку
    return re.sub(r"\[\s+([-\d.,\s]+?)\s+\]", lambda m: "[" + re.sub(r"\s+", " ", m.group(1)) + "]", text) + "\n"


def build_atlas(regions, positions):
    atlas = np.zeros((ATLAS, ATLAS, 4), np.uint8)
    atlas[..., :3] = WOOD[2]
    atlas[..., 3] = 255  # прозрачных пикселей нет: частицы при ломании берут любой кусок
    for name, img in regions.items():
        ox, oy = positions[name]
        h, w = img.shape[:2]
        atlas[oy:oy + h, ox:ox + w, :3] = img
    return atlas


# ---------------------------------------------------------------- превью

FACE_VERTS = {
    "down": [("min", "min", "max"), ("min", "min", "min"), ("max", "min", "min"), ("max", "min", "max")],
    "up": [("min", "max", "min"), ("min", "max", "max"), ("max", "max", "max"), ("max", "max", "min")],
    "north": [("max", "max", "min"), ("max", "min", "min"), ("min", "min", "min"), ("min", "max", "min")],
    "south": [("min", "max", "max"), ("min", "min", "max"), ("max", "min", "max"), ("max", "max", "max")],
    "west": [("min", "max", "min"), ("min", "min", "min"), ("min", "min", "max"), ("min", "max", "max")],
    "east": [("max", "max", "max"), ("max", "min", "max"), ("max", "min", "min"), ("max", "max", "min")],
}
NORMALS = {"down": (0, -1, 0), "up": (0, 1, 0), "north": (0, 0, -1), "south": (0, 0, 1), "west": (-1, 0, 0), "east": (1, 0, 0)}


def face_quad(frm, to, direction):
    pts = []
    for ax, ay, az in FACE_VERTS[direction]:
        pts.append((frm[0] if ax == "min" else to[0], frm[1] if ay == "min" else to[1], frm[2] if az == "min" else to[2]))
    return np.array(pts, float)


def rasterize(quads, rot, scale, size, center, supersample=2):
    """Ортогональный рендер без сглаживания текстур. Камера на +z смотрит в -z, x вправо, y вверх (как у предмета в GUI)."""
    s = supersample
    w, h = size[0] * s, size[1] * s
    color = np.zeros((h, w, 4), np.uint8)
    depth = np.full((h, w), -1e9)
    for q in quads:
        p = (q["pts"] - center) @ rot.T
        n = rot @ np.array(q["normal"], float)
        if n[2] <= 1e-6:
            continue
        du, dv = p[3] - p[0], p[1] - p[0]
        sc = scale * s
        origin = np.array([w / 2 + sc * p[0][0], h / 2 - sc * p[0][1]])
        a = np.array([[sc * du[0], sc * dv[0]], [-sc * du[1], -sc * dv[1]]])
        det = np.linalg.det(a)
        if abs(det) < 1e-9:
            continue
        inv = np.linalg.inv(a)
        xs = [origin[0] + a[0] @ c for c in ((0, 0), (1, 0), (0, 1), (1, 1))]
        ys = [origin[1] + a[1] @ c for c in ((0, 0), (1, 0), (0, 1), (1, 1))]
        x0, x1 = max(0, int(math.floor(min(xs)))), min(w, int(math.ceil(max(xs))) + 1)
        y0, y1 = max(0, int(math.floor(min(ys)))), min(h, int(math.ceil(max(ys))) + 1)
        if x0 >= x1 or y0 >= y1:
            continue
        gy, gx = np.mgrid[y0:y1, x0:x1]
        rel_x, rel_y = gx + 0.5 - origin[0], gy + 0.5 - origin[1]
        ss = inv[0, 0] * rel_x + inv[0, 1] * rel_y
        tt = inv[1, 0] * rel_x + inv[1, 1] * rel_y
        inside = (ss >= 0) & (ss < 1) & (tt >= 0) & (tt < 1)
        if not inside.any():
            continue
        z = p[0][2] + ss * du[2] + tt * dv[2]
        u1, v1, u2, v2 = q["uv_px"]
        tex = q["tex"]
        tu = np.clip(np.floor(u1 + ss * (u2 - u1)).astype(int), 0, tex.shape[1] - 1)
        tv = np.clip(np.floor(v1 + tt * (v2 - v1)).astype(int), 0, tex.shape[0] - 1)
        texel = tex[tv, tu]
        ok = inside & (texel[..., 3] >= 128) & (z > depth[y0:y1, x0:x1] + 1e-6)
        shaded = (texel[..., :3] * q["shade"]).clip(0, 255).astype(np.uint8)
        sub = color[y0:y1, x0:x1]
        sub[ok, :3] = shaded[ok]
        sub[ok, 3] = 255
        depth[y0:y1, x0:x1][ok] = z[ok]
    img = Image.fromarray(color, "RGBA")
    return img.resize(size, Image.LANCZOS) if s > 1 else img


def model_quads(resolved, atlas, fish=None, overlay=None, fish_scale=0.5):
    quads = []
    for name, frm, to, px_faces in resolved:
        for direction, (x, y, w, h) in px_faces.items():
            quads.append({"pts": face_quad(frm, to, direction), "normal": NORMALS[direction], "uv_px": (x, y, x + w, y + h),
                          "tex": atlas, "shade": FACE_SHADE[direction]})
    if fish is not None:  # плоский спрайт рыбы: центр в окне, на 13.12 от лица стены, как в рендерере блока
        half = fish_scale * 16 / 2
        quads.append({"pts": face_quad((8 - half, 9.28 - half, 13.12), (8 + half, 9.28 + half, 13.12), "north"),
                      "normal": NORMALS["north"], "uv_px": (0, 0, 16, 16), "tex": fish, "shade": 1.0})
    if overlay is not None:  # подпись перед табличкой; её освещает только свет блока, направленного затенения нет
        quads.append({"pts": face_quad((2, 0.5, TEXT_Z), (14, 4.5, TEXT_Z), "north"), "normal": NORMALS["north"],
                      "uv_px": (0, 0, overlay.shape[1], overlay.shape[0]), "tex": overlay, "shade": 1.0})
    return quads


def load_sprite(spec):
    """Спрайт рыбы: путь к PNG или id предмета Starcatcher (берётся из jar в mods/)."""
    path = Path(spec)
    if path.exists():
        return np.array(Image.open(path).convert("RGBA"))
    jar = next((ROOT / "mods").glob("starcatcher-*.jar"))
    with zipfile.ZipFile(jar) as z:
        return np.array(Image.open(io.BytesIO(z.read(f"assets/starcatcher/textures/item/{spec}.png"))).convert("RGBA"))


CLIENT_EXTRA = Path.home() / ".gradle" / "caches" / "forge_gradle" / "minecraft_repo" / "versions" / "1.20.1" / "client-extra.jar"
RENDERER = ROOT / "mods" / "aquatech-ui" / "src" / "main" / "java" / "net" / "aquatech" / "ui" / "client" / "render" / "FamePlaqueRenderer.java"
TEXT_COLOR = (0x1E, 0x12, 0x06)
TEXT_Z = 11.9  # плоскость подписи перед табличкой на 12.0


class McFont:
    """Шрифт Minecraft из client-extra.jar: ширины и картинки глифов нужны, чтобы честно проверить подпись на табличке."""

    def __init__(self, jar=CLIENT_EXTRA):
        z = zipfile.ZipFile(jar)
        spec = json.loads(z.read("assets/minecraft/font/include/default.json"))
        self.glyphs = {}
        for provider in spec["providers"]:  # провайдер выше по списку главнее
            if provider["type"] != "bitmap":
                continue
            name = provider["file"].split(":")[1]
            alpha = np.array(Image.open(io.BytesIO(z.read("assets/minecraft/textures/" + name))).convert("RGBA"))[..., 3] > 0
            rows = provider["chars"]
            cell_w, cell_h = alpha.shape[1] // len(rows[0]), alpha.shape[0] // len(rows)
            for r, row in enumerate(rows):
                for c, ch in enumerate(row):
                    if ch in ("\x00", " ") or ch in self.glyphs:
                        continue
                    cell = alpha[r * cell_h:(r + 1) * cell_h, c * cell_w:(c + 1) * cell_w]
                    cols = np.where(cell.any(axis=0))[0]
                    if len(cols):
                        width = int(cols.max()) + 1
                        self.glyphs[ch] = (cell[:, :width], width + 1, 7 - provider["ascent"])

    def advance(self, ch):
        if ch == " ":
            return 4
        return (self.glyphs.get(ch) or self.glyphs["?"])[1]

    def width(self, text):
        return sum(self.advance(ch) for ch in text)

    def render(self, text):
        """Маска подписи 15 x ширина: верх строки на 3-й строке, выше место под ударения."""
        canvas = np.zeros((15, max(1, self.width(text))), bool)
        x = 0
        for ch in text:
            if ch != " ":
                bitmap, _, y_off = self.glyphs.get(ch) or self.glyphs["?"]
                h, w = bitmap.shape
                canvas[3 + y_off:3 + y_off + h, x:x + w] |= bitmap
            x += self.advance(ch)
        return canvas


def renderer_constants():
    """Масштабы подписи и рыбы берём из рендерера блока, чтобы превью не разъезжалось с игрой."""
    src = RENDERER.read_text(encoding="utf-8")
    return {k: float(re.search(k + r" = ([0-9.]+)F", src).group(1)) for k in ("TEXT_SCALE", "TEXT_MAX_WIDTH", "FISH_SCALE")}


def text_overlay(font, lines, px_per_unit, consts):
    """Слой подписи на табличку 12x4 единицы (x 2..14, y 0.5..4.5), как её рисует рендерер блока.
    lines: [(текст, верх строки в единицах модели над низом блока)]."""
    w, h = round(12 * px_per_unit), round(4 * px_per_unit)
    layer = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    for text, top_u in lines:
        width = font.width(text)
        scale = min(consts["TEXT_SCALE"], consts["TEXT_MAX_WIDTH"] / max(1, width))
        font_px = scale * 16 * px_per_unit  # пикселей слоя на пиксель шрифта
        mask = font.render(text)
        glyphs = Image.fromarray((mask * 255).astype(np.uint8), "L")
        size = (max(1, round(mask.shape[1] * font_px)), max(1, round(mask.shape[0] * font_px)))
        glyphs = glyphs.resize(size, Image.NEAREST)
        x = round(w / 2 - width / 2 * font_px)
        y = round((4.5 - top_u) * px_per_unit - 3 * font_px)
        layer.paste(Image.new("RGBA", size, TEXT_COLOR + (255,)), (x, y), glyphs)
        print(f"подпись «{text}»: {width} px шрифта, масштаб {scale:.4f}, ширина {width * scale * 16:.1f} ед. из 11")
    return np.array(layer)


def preview_front(atlas, fish, fish_scale, overlay):
    """Вид спереди; по яркости как в игре (лицевые грани 0.8, подпись и рыба без затенения)."""
    px = 16  # пикселей картинки на тексель
    tex = atlas[:32, :32, :3].astype(float) * 0.8
    img = Image.fromarray(tex.clip(0, 255).astype(np.uint8), "RGB").resize((32 * px, 32 * px), Image.NEAREST).convert("RGBA")
    if fish is not None:
        size = round(16 * px * fish_scale * 2)  # 16 текселей спрайта = 16 * scale единиц, текселей на единицу 2
        sprite = Image.fromarray(fish, "RGBA").resize((size, size), Image.NEAREST)
        layer = Image.new("RGBA", img.size, (0, 0, 0, 0))
        layer.alpha_composite(sprite, (16 * px - size // 2, round((16 - 9.28) * 2 * px) - size // 2))
        window = (5 * px, 5 * px, 27 * px, 22 * px)  # то, что выходит за окно, прячет рама
        clipped = Image.new("RGBA", img.size, (0, 0, 0, 0))
        clipped.paste(layer.crop(window), window[:2])
        img.alpha_composite(clipped)
    if overlay is not None:
        img.alpha_composite(Image.fromarray(overlay, "RGBA"), (4 * px, round((16 - 4.5) * 2 * px)))
    return img


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--preview", type=Path, help="папка для картинок проверки")
    ap.add_argument("--no-write", action="store_true", help="не трогать ресурсы мода")
    ap.add_argument("--fish", help="спрайт рыбы для превью: путь к PNG 16x16 или id предмета Starcatcher, например lightning_bass")
    ap.add_argument("--promo", type=Path, help="картинка для сайта (.webp или .png), прозрачный фон")
    ap.add_argument("--line1", default="Молниевый Окунь", help="название рыбы на табличке в превью")
    ap.add_argument("--line2", default="Алекс · 4.82 кг", help="рекордсмен и вес в превью")
    ap.add_argument("--seed", type=int, default=7)
    args = ap.parse_args()

    rng = random.Random(args.seed)
    regions = build_regions(rng)
    positions = pack(regions)
    atlas = build_atlas(regions, positions)
    model, resolved = build_model(positions, regions)

    if not args.no_write:
        TEXTURE_OUT.parent.mkdir(parents=True, exist_ok=True)
        Image.fromarray(atlas, "RGBA").save(TEXTURE_OUT)
        MODEL_OUT.write_text(dump_model(model), encoding="utf-8", newline="\n")
        print("записано:", TEXTURE_OUT.relative_to(ROOT), MODEL_OUT.relative_to(ROOT))

    if args.preview or args.promo:
        consts = renderer_constants()
        font = McFont()
        lines = [(args.line1, 3.76), (args.line2, 2.4)]  # верх строк как в FamePlaqueRenderer: -0.265 и -0.35 от центра блока
        fish_tex = load_sprite(args.fish) if args.fish else None
        center = np.array([8.0, 8.0, 14.0])
        if args.preview:
            args.preview.mkdir(parents=True, exist_ok=True)
            Image.fromarray(atlas, "RGBA").resize((512, 512), Image.NEAREST).save(args.preview / "atlas.png")
            preview_front(atlas, fish_tex, consts["FISH_SCALE"], text_overlay(font, lines, 32, consts)).save(args.preview / "front.png")
            quads = model_quads(resolved, atlas, fish_tex, text_overlay(font, lines, 64, consts), consts["FISH_SCALE"])
            # ракурсы стены: сбоку сверху, снизу с другой стороны и сильно сбоку
            for label, yaw, pitch in (("iso", 200, 22), ("iso_low", 160, -18), ("side", 235, 8)):
                rasterize(quads, rot_x(pitch) @ rot_y(yaw), 22, (420, 420), center).save(args.preview / f"{label}.png")
            # предмет в инвентаре: поворот и масштаб из display.gui
            g = DISPLAY["gui"]
            rot = rot_x(g["rotation"][0]) @ rot_y(g["rotation"][1])
            rasterize(quads, rot, 6 * g["scale"][0], (16 * 6, 16 * 6), np.array([8.0, 8.0, 8.0])).save(args.preview / "gui.png")
            print("превью:", args.preview)
        if args.promo:
            quads = model_quads(resolved, atlas, fish_tex, text_overlay(font, lines, 100, consts), consts["FISH_SCALE"])
            img = rasterize(quads, rot_x(22) @ rot_y(200), 50, (1100, 1100), center)
            box = img.getchannel("A").getbbox()
            pad = 16
            img = img.crop((box[0] - pad, box[1] - pad, box[2] + pad, box[3] + pad))
            args.promo.parent.mkdir(parents=True, exist_ok=True)
            img.save(args.promo, lossless=True, quality=100, method=6) if args.promo.suffix == ".webp" else img.save(args.promo)
            print("промо:", args.promo, img.size)


if __name__ == "__main__":
    main()
