"""Рисует текстуры Альфа-удочки StarCatcher (тир 13), вариант «Звёздная».

    python tools/build_alpha_rod_textures.py

Подмена идёт через ресурсы KubeJS (kubejs/assets/starcatcher/...): они важнее ресурсов модов и едут в клиентском паке.
Силуэт тот же, что у остальных топовых удочек (дуга и зигзаг лески). Версия «cast» это удочка без лески и поплавка:
леску в заброшенном состоянии рисует сама игра.
"""
from pathlib import Path

from PIL import Image

OUT = Path(__file__).resolve().parent.parent / "kubejs" / "assets" / "starcatcher" / "textures" / "item"

PALETTE = {
    "o": "06081c",  # контур
    "n": "141a4a",  # основа
    "m": "26338a",  # середина
    "l": "4a63d8",  # свет
    "p": "6b3fd1",  # туманность
    "c": "8ff6ff",  # голубое свечение
    "w": "ffffff",  # звезда
    "g": "ffd84a",  # золото
    "a": "b8892a",  # тёмное золото
    "x": "c9f6ff",  # леска, светлая нить
    "y": "8ad8f0",  # леска, тёмная нить
}

# Леска и поплавок живут только в «uncast»: помечены 'x', 'y' (леска) и 'g' в клетках BOBBERS.
BOBBERS = {(13, 10), (12, 15)}

GRID = [
    "...........omow.",
    ".........oomocmn",
    "........olmnpnmn",
    ".......nlnoowoln",
    "......ocno.o.no.",
    ".....ncnmnc...o.",
    "....nlnoln....x.",
    "...ocno.o.....x.",
    "...olo..o....y..",
    "..npnno.c....y..",
    "..onpco.....yg..",
    ".olnccn.....x...",
    ".omnono......y..",
    "oga.nlo.......x.",
    "aoa.nm........y.",
    "gg...o......gx..",
]


def build(with_line):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(GRID):
        row = row[:16].ljust(16, ".")
        for x, ch in enumerate(row):
            if ch == ".":
                continue
            if not with_line and (ch in "xy" or (x, y) in BOBBERS):
                continue
            hexcolor = PALETTE[ch]
            img.putpixel((x, y), tuple(int(hexcolor[i:i + 2], 16) for i in (0, 2, 4)) + (255,))
    return img


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    build(True).save(OUT / "alpha_rod_uncast.png")
    build(False).save(OUT / "alpha_rod_cast.png")
    print("written to", OUT)


if __name__ == "__main__":
    main()
