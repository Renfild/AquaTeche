"""Удочка Лея: 3D-модель предмета (повёрнутые на 45 градусов бруски) и текстура 64x64.

Удилище идёт по диагонали от рукояти внизу слева к кончику вверху справа, как у обычной удочки в руке. Леска
с ржавым крючком свисает от кончика. Плотность текстуры 2 пикселя на единицу модели.
Развёртку и модель считает скрипт; руками JSON и PNG не править.

    python tools/gen_ley_rod.py                  пишет текстуру и модель в ресурсы мода
    python tools/gen_ley_rod.py --preview DIR    плюс картинки для проверки глазами
"""
import argparse
import json
import math
import random
from pathlib import Path

import numpy as np
from PIL import Image

import gen_fame_plaque as pl

ROOT = pl.ROOT
ASSETS = pl.ASSETS
TEXTURE_OUT = ASSETS / "textures" / "item" / "ley_rod_model.png"
MODEL_OUT = ASSETS / "models" / "item" / "ley_rod.json"
ATLAS = 64

SHAFT = pl.rgb("0a1519", "112329", "1a3a45", "26566a", "3f8197", "6fb3c4")
GOLD = pl.BRASS
LEATHER = pl.rgb("22160e", "38261a", "553a26", "74533a", "94704f")
RUST = pl.rgb("2a1d18", "4a2c20", "6f4029", "94572f", "bd7b45")
STEEL = pl.rgb("1c2226", "2f3a40", "485960", "6b8089", "93a8b0")
AMBER = pl.rgb("a8541a", "e08a28", "ffb63d", "ffe08a")
LINE = (232, 228, 212)


def tile(w, h, painter):
    img = np.zeros((h, w, 3), np.uint8)
    for y in range(h):
        for x in range(w):
            img[y, x] = painter(x, y, w, h)
    return img


def shaft_tile(w, h, rng, ramp=SHAFT):
    """Графит с бирюзовым отливом: светлая линия по верху, тёмная по низу, редкие штрихи."""
    streak = pl.runs(rng, w, [0, 0, 0, 1, -1], 4, 10)
    def paint(x, y, w, h):
        base = 3 if y == 0 else 2 if y < h - 1 else 1
        return ramp[pl.clamp_idx(base + streak[x], ramp)]
    return tile(w, h, paint)


def wrap_tile(w, h):
    def paint(x, y, w, h):
        return GOLD[5] if y == 0 else GOLD[4] if y < h - 1 else GOLD[2]
    return tile(w, h, paint)


def grip_tile(w, h):
    """Кожаная оплётка: золотые витки через каждые 4 пикселя."""
    def paint(x, y, w, h):
        if x % 5 == 4:
            return GOLD[3] if y % 2 == 0 else GOLD[2]
        return LEATHER[3 if y == 0 else 2 if y < h - 1 else 1]
    return tile(w, h, paint)


def solid_gold(w, h, shade=4):
    return tile(w, h, lambda x, y, w, h: GOLD[shade if y == 0 else max(2, shade - 1)])


def reel_tile(w, h):
    """Катушка: латунный корпус, внутри янтарное око Лея."""
    cx, cy = (w - 1) / 2, (h - 1) / 2
    def paint(x, y, w, h):
        d = math.hypot(x - cx, (y - cy) * 0.9)
        if d < 1.1:
            return AMBER[3]
        if d < 2.0:
            return AMBER[2]
        if d < 2.7:
            return AMBER[0]
        if y == 0 or x == 0:
            return GOLD[5]
        if y == h - 1 or x == w - 1:
            return GOLD[1]
        return GOLD[3]
    return tile(w, h, paint)


def rust_tile(w, h):
    return tile(w, h, lambda x, y, w, h: RUST[4 if (x + y) % 5 == 0 else 3 if y == 0 else 2])


def line_tile(w, h):
    return tile(w, h, lambda x, y, w, h: LINE)


def steel_tile(w, h):
    return tile(w, h, lambda x, y, w, h: STEEL[4 if y == 0 else 3 if y < h - 1 else 2])


def amber_tile(w, h):
    return tile(w, h, lambda x, y, w, h: AMBER[3 if y == 0 else 2 if y < h - 1 else 1])


def parts(rng):
    """(имя, от, до, поворот, имя тайла); координаты в системе удилища вдоль x, потом поворот на 45 по z вокруг (8, 8, 8)."""
    rot = True
    P = []
    def add(name, x0, x1, y0, y1, z0, z1, tile_name, rotated=True):
        P.append((name, (8 + x0, 8 + y0, 8 + z0), (8 + x1, 8 + y1, 8 + z1), rotated, tile_name))
    add("butt", -10.4, -9.6, -0.95, 0.95, -0.95, 0.95, "gold")
    add("grip", -9.6, -4.6, -0.95, 0.95, -0.95, 0.95, "grip")
    add("grip_ring", -4.6, -4.0, -1.15, 1.15, -1.15, 1.15, "gold")
    add("seat", -4.0, -2.4, -1.0, 1.0, -1.0, 1.0, "steel")
    add("shaft_a", -2.4, 3.0, -0.55, 0.55, -0.55, 0.55, "shaft_a")
    add("shaft_b", 3.0, 8.0, -0.4, 0.4, -0.4, 0.4, "shaft_b")
    add("shaft_c", 8.0, 10.2, -0.28, 0.28, -0.28, 0.28, "shaft_c")
    add("wrap_1", -2.4, -2.0, -0.7, 0.7, -0.7, 0.7, "gold")
    add("wrap_2", 2.6, 3.0, -0.7, 0.7, -0.7, 0.7, "gold")
    add("wrap_3", 5.4, 5.7, -0.55, 0.55, -0.55, 0.55, "gold")
    add("wrap_4", 7.7, 8.0, -0.45, 0.45, -0.45, 0.45, "gold")
    for i, x in enumerate((-0.4, 2.0, 4.6, 7.2)):
        add(f"guide_{i}", x, x + 0.5, -1.3, -0.45, -0.28, 0.28, "gold")
    add("tip_eye", 9.9, 10.7, -0.4, 0.4, -0.4, 0.4, "gold")
    # катушка под удилищем и ручка на боку
    add("reel", -3.7, -1.9, -3.3, -1.0, -0.95, 0.95, "gold")
    add("reel_eye_s", -3.4, -2.2, -3.1, -1.2, 0.95, 1.02, "reel")
    add("reel_eye_n", -3.4, -2.2, -3.1, -1.2, -1.02, -0.95, "reel")
    add("crank_arm", -3.0, -2.7, -2.5, -2.2, 0.95, 2.4, "steel")
    add("crank_knob", -3.2, -2.5, -2.8, -1.9, 2.4, 3.0, "amber")
    # леска и крючок свисают от кончика, без поворота (кончик после поворота ближе к углу модели)
    tip = 8 + 10.7 * math.cos(math.radians(45))
    P.append(("line", (tip - 0.2, 9.4, 7.85), (tip + 0.1, tip - 0.3, 8.15), False, "line"))
    P.append(("hook_stem", (tip - 0.25, 8.2, 7.8), (tip + 0.15, 9.4, 8.2), False, "rust"))
    P.append(("hook_bend", (tip - 1.15, 7.8, 7.8), (tip + 0.15, 8.3, 8.2), False, "rust"))
    P.append(("hook_barb", (tip - 1.15, 8.3, 7.8), (tip - 0.7, 9.4, 8.2), False, "rust"))
    return P


def build_tiles(rng):
    return {
        "gold": solid_gold(5, 2), "grip": grip_tile(10, 2), "steel": steel_tile(3, 2),
        "shaft_a": shaft_tile(11, 2, rng), "shaft_b": shaft_tile(10, 2, rng), "shaft_c": shaft_tile(5, 1, rng),
        "reel": reel_tile(5, 5), "amber": amber_tile(2, 2), "rust": rust_tile(4, 4), "line": line_tile(1, 4),
    }


def pack(tiles):
    pos, x, y, row = {}, 0, 0, 0
    for name in sorted(tiles, key=lambda n: -tiles[n].shape[0]):
        h, w = tiles[name].shape[:2]
        if x + w > ATLAS:
            x, y, row = 0, y + row + 1, 0
        pos[name] = (x, y)
        x += w + 1
        row = max(row, h)
    return pos


def uv_for(tile_name, tiles, pos, direction):
    h, w = tiles[tile_name].shape[:2]
    x, y = pos[tile_name]
    return [x / 4, y / 4, (x + w) / 4, (y + h) / 4]


def rotation_matrix_z(deg):
    a = math.radians(deg)
    return np.array([[math.cos(a), -math.sin(a), 0], [math.sin(a), math.cos(a), 0], [0, 0, 1]])


def build(rng):
    tiles = build_tiles(rng)
    pos = pack(tiles)
    atlas = np.zeros((ATLAS, ATLAS, 4), np.uint8)
    atlas[..., :3] = SHAFT[1]
    atlas[..., 3] = 0  # свободное место прозрачное: у предмета нет фона
    for name, img in tiles.items():
        x, y = pos[name]
        h, w = img.shape[:2]
        atlas[y:y + h, x:x + w, :3] = img
        atlas[y:y + h, x:x + w, 3] = 255
    elements = []
    for name, frm, to, rotated, tile_name in parts(rng):
        faces = {d: {"uv": uv_for(tile_name, tiles, pos, d), "texture": "#0"} for d in ("north", "east", "south", "west", "up", "down")}
        el = {"name": name, "from": [round(v, 3) for v in frm], "to": [round(v, 3) for v in to], "faces": faces}
        if rotated:
            el["rotation"] = {"angle": 45, "axis": "z", "origin": [8, 8, 8]}
        elements.append(el)
    return atlas, elements, tiles, pos


# display как у item/handheld: удилище лежит в руке рукоятью вниз, кончик вперёд
HANDHELD = {
    "gui": {"rotation": [12, -28, 0], "translation": [0, 0, 0], "scale": [0.95, 0.95, 0.95]},
    "ground": {"rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [0.5, 0.5, 0.5]},
    "fixed": {"rotation": [0, 180, 0], "translation": [0, 0, 0], "scale": [1, 1, 1]},
    "thirdperson_righthand": {"rotation": [0, -90, 55], "translation": [0, 4, 0.5], "scale": [0.85, 0.85, 0.85]},
    "thirdperson_lefthand": {"rotation": [0, 90, -55], "translation": [0, 4, 0.5], "scale": [0.85, 0.85, 0.85]},
    "firstperson_righthand": {"rotation": [0, -90, 25], "translation": [1.13, 3.2, 1.13], "scale": [0.68, 0.68, 0.68]},
    "firstperson_lefthand": {"rotation": [0, 90, -25], "translation": [1.13, 3.2, 1.13], "scale": [0.68, 0.68, 0.68]},
}


def model_json(elements):
    return {
        "gui_light": "front",
        "texture_size": [ATLAS, ATLAS],
        "textures": {"0": "aquatech_ui:item/ley_rod_model", "particle": "aquatech_ui:item/ley_rod_model"},
        "elements": elements,
        "display": HANDHELD,
    }


def preview_quads(elements, atlas):
    quads = []
    for el in elements:
        frm, to = el["from"], el["to"]
        rot = rotation_matrix_z(el["rotation"]["angle"]) if "rotation" in el else None
        for d, face in el["faces"].items():
            pts = pl.face_quad(frm, to, d)
            normal = np.array(pl.NORMALS[d], float)
            if rot is not None:
                origin = np.array(el["rotation"]["origin"], float)
                pts = (pts - origin) @ rot.T + origin
                normal = rot @ normal
            u1, v1, u2, v2 = face["uv"]
            nearest = max(("up", "down", "north", "south", "east", "west"), key=lambda k: float(np.dot(normal, pl.NORMALS[k])))
            quads.append({"pts": pts, "normal": tuple(normal), "uv_px": (u1 * 4, v1 * 4, u2 * 4, v2 * 4), "tex": atlas,
                          "shade": 0.95 if nearest in ("north", "south") else pl.FACE_SHADE[nearest] + 0.15})
    return quads


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--preview", type=Path)
    ap.add_argument("--no-write", action="store_true")
    ap.add_argument("--seed", type=int, default=11)
    args = ap.parse_args()
    atlas, elements, tiles, pos = build(random.Random(args.seed))
    if not args.no_write:
        TEXTURE_OUT.parent.mkdir(parents=True, exist_ok=True)
        MODEL_OUT.parent.mkdir(parents=True, exist_ok=True)
        Image.fromarray(atlas, "RGBA").save(TEXTURE_OUT)
        MODEL_OUT.write_text(pl.dump_model(model_json(elements)), encoding="utf-8", newline="\n")
        print("записано:", TEXTURE_OUT.relative_to(ROOT), MODEL_OUT.relative_to(ROOT))
    if args.preview:
        args.preview.mkdir(parents=True, exist_ok=True)
        Image.fromarray(atlas, "RGBA").resize((512, 512), Image.NEAREST).save(args.preview / "rod_atlas.png")
        quads = preview_quads(elements, atlas)
        center = np.array([8.0, 8.0, 8.0])
        g = HANDHELD["gui"]
        rot = pl.rot_x(g["rotation"][0]) @ pl.rot_y(g["rotation"][1])
        pl.rasterize(quads, rot, 24, (400, 400), center).save(args.preview / "rod_gui_big.png")
        pl.rasterize(quads, rot, 6 * g["scale"][0], (96, 96), center).save(args.preview / "rod_gui.png")
        # вид строго спереди и сбоку
        pl.rasterize(quads, pl.rot_y(0), 24, (400, 400), center).save(args.preview / "rod_front.png")
        pl.rasterize(quads, pl.rot_y(60), 24, (400, 400), center).save(args.preview / "rod_side.png")
        print("превью:", args.preview)


if __name__ == "__main__":
    main()
