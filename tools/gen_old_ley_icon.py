"""Рисует иконку трофея «Старый Лей» (16x16) в ресурсы мода.

    python tools/gen_old_ley_icon.py
"""
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "mods" / "aquatech-ui" / "src" / "main" / "resources" / "assets" / "aquatech_ui" / "textures" / "item" / "old_ley.png"

SIZE = 16
BACK = (38, 56, 47, 255)
BODY = (61, 90, 74, 255)
BELLY = (184, 180, 154, 255)
FIN = (53, 80, 63, 255)
SCUTE = (123, 132, 102, 255)
GILL = (93, 119, 102, 255)
EYE = (255, 194, 51, 255)
WHISKER = (138, 117, 80, 255)
RUST = (138, 74, 42, 255)
LINE = (207, 207, 196, 255)
OUTLINE = (20, 30, 26, 255)
MOSS = (79, 122, 58, 255)


def main() -> None:
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    px = img.load()

    # тело: эллипс, сверху тёмная спина, снизу светлое брюхо
    for y in range(SIZE):
        for x in range(SIZE):
            if ((x - 7.5) / 6.4) ** 2 + ((y - 9.0) / 3.2) ** 2 <= 1.0:
                dy = y - 9.0
                px[x, y] = BACK if dy < -1.4 else BELLY if dy > 1.6 else BODY

    # хвост: треугольник справа
    for x in range(12, 16):
        spread = (x - 11) * 1.1
        for y in range(SIZE):
            if abs(y - 9.0) <= spread:
                px[x, y] = FIN

    # спинной плавник и костяные пластины
    for x in range(6, 10):
        px[x, 5] = FIN
    px[7, 4] = FIN
    for x in (4, 6, 8, 10):
        px[x, 6 if x in (4, 10) else 5] = SCUTE
    px[5, 6] = MOSS

    # жабры, глаз, усы
    for y in (8, 9, 10):
        px[5, y] = GILL
    px[3, 8] = EYE
    px[0, 10] = WHISKER
    px[1, 11] = WHISKER
    px[0, 12] = WHISKER

    # ржавый крючок во рту и леска вверх
    px[1, 9] = RUST
    px[1, 10] = RUST
    for y in (3, 4, 5, 6, 7, 8):
        px[1, y] = LINE
    px[1, 8] = RUST

    # контур по прозрачным соседям
    filled = {(x, y) for y in range(SIZE) for x in range(SIZE) if px[x, y][3] > 0}
    for x, y in sorted(filled):
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            nx, ny = x + dx, y + dy
            if 0 <= nx < SIZE and 0 <= ny < SIZE and (nx, ny) not in filled:
                px[nx, ny] = OUTLINE

    OUT.parent.mkdir(parents=True, exist_ok=True)
    img.save(OUT)
    print(OUT)


if __name__ == "__main__":
    main()
