"""Рисует иконку трофея «Старый Лей» (16x16) в ресурсы мода.

Сом смотрит влево: широкая голова, длинные усы, янтарный глаз, золотая боковая линия, ржавый крючок в губе
и искры, по которым видно, что рыба легендарная.

    python tools/gen_old_ley_icon.py
"""
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "mods" / "aquatech-ui" / "src" / "main" / "resources" / "assets" / "aquatech_ui" / "textures" / "item" / "old_ley.png"

PALETTE = {
    "K": (16, 28, 24),      # контур
    "D": (38, 78, 58),      # спина, тёмная
    "G": (66, 122, 82),     # бок
    "L": (112, 170, 106),   # блик на боку
    "C": (222, 214, 170),   # брюхо
    "c": (176, 168, 128),   # тень на брюхе
    "Y": (240, 190, 62),    # золото боковой линии
    "y": (190, 138, 40),    # тень золота
    "E": (255, 196, 48),    # глаз
    "P": (24, 16, 10),      # зрачок
    "W": (214, 204, 160),   # усы
    "F": (36, 98, 96),      # плавники
    "f": (70, 150, 140),    # свет на плавниках
    "R": (190, 98, 44),     # ржавый крючок
    "r": (122, 60, 30),     # тень ржавчины
    "S": (255, 250, 214),   # искра
    "Z": (255, 214, 92),    # золотая искра
}

def ellipse(cx, cy, rx, ry):
    return lambda x, y: ((x + 0.5 - cx) / rx) ** 2 + ((y + 0.5 - cy) / ry) ** 2 <= 1.0


# силуэт: широкая плоская голова, тело потоньше, хвост лопастью
HEAD = ellipse(5.6, 8.2, 3.9, 3.4)
BODY = ellipse(9.6, 8.0, 4.2, 2.7)
TAIL = lambda x, y: 12 <= x <= 14 and abs(y + 0.5 - 8.0) <= 0.5 + (x - 11.5) * 0.65
DORSAL = lambda x, y: 8 <= x <= 10 and 3 <= y <= 4 and y >= 6.2 - (min(x - 8, 10 - x) + 1.0) * 1.5
ANAL = lambda x, y: 9 <= x <= 12 and y == 11 and True
PECT = lambda x, y: 6 <= x <= 7 and 11 <= y <= 12


def build():
    img = [["." for _ in range(16)] for _ in range(16)]
    for y in range(16):
        for x in range(16):
            if HEAD(x, y) or BODY(x, y) or TAIL(x, y):
                top, bottom = y < 7, y >= 10
                img[y][x] = "C" if bottom else "D" if top and not (HEAD(x, y) and y == 7) else "G"
    for y in range(16):
        for x in range(16):
            if DORSAL(x, y) or ANAL(x, y) or PECT(x, y):
                img[y][x] = "F"
    # бликовая линия по спине и золотая боковая линия
    for x in range(2, 9):
        if img[6][x] == "D":
            img[6][x] = "L" if x < 6 else "G"
    for x in range(6, 14):
        if img[8][x] in ("G", "D"):
            img[8][x] = "Y" if x % 4 != 3 else "y"
    # тень на брюхе
    for x in range(3, 12):
        if img[11][x] == "C":
            img[11][x] = "c"
    # глаз и ноздря
    img[7][3] = "E"
    img[7][4] = "P"
    # контур снаружи
    out = [row[:] for row in img]
    for y in range(16):
        for x in range(16):
            if img[y][x] == ".":
                near = any(0 <= y + dy < 16 and 0 <= x + dx < 16 and img[y + dy][x + dx] not in (".", "W", "R", "r")
                           for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
                if near:
                    out[y][x] = "K"
    # усы: два длинных вверх-влево и вниз-влево, короткие на подбородке
    for x, y in ((1, 7), (0, 6), (1, 9), (0, 10), (1, 11), (0, 12)):
        out[y][x] = "W"
    # ржавый крючок в губе, висит вниз
    for x, y, ch in ((2, 11, "R"), (2, 12, "R"), (2, 13, "r"), (3, 14, "R"), (4, 13, "R")):
        out[y][x] = ch
    # искры легендарности
    for x, y, ch in ((13, 1, "S"), (15, 3, "Z"), (1, 2, "Z"), (14, 13, "Z")):
        out[y][x] = ch
    return ["".join(row) for row in out]


ART = build()


def main() -> None:
    assert len(ART) == 16 and all(len(row) == 16 for row in ART), "рисунок должен быть 16x16"
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    for y, row in enumerate(ART):
        for x, ch in enumerate(row):
            if ch != ".":
                px[x, y] = PALETTE[ch] + (255,)
    OUT.parent.mkdir(parents=True, exist_ok=True)
    img.save(OUT)
    print("записано:", OUT.relative_to(ROOT))


if __name__ == "__main__":
    main()
