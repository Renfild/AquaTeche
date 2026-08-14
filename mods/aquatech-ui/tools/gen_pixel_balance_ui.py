#!/usr/bin/env python3
"""
Hand-crafted pixel-art fishing HUD (Stardew Valley / Terraria vibe).

Pixel-art rules applied:
- Limited palette (~14 colors), no soft gradients / blur
- 1px dark outlines, readable silhouettes at small size
- Consistent light from top-left (highlight TL, shade BR)
- Integer nearest-neighbor upscale only (no Lanczos mush)
- Animation = discrete frames, not interpolated morphs
"""
from __future__ import annotations

from pathlib import Path
from PIL import Image

# --- Cozy fishing palette (Stardew-ish wood + Terraria water) ---
OUT = (0, 0, 0, 0)
INK = (61, 43, 31, 255)          # deep wood outline
WOOD_D = (120, 78, 48, 255)
WOOD_M = (166, 116, 70, 255)
WOOD_L = (198, 150, 96, 255)
PARCH = (232, 214, 170, 255)     # parchment fill
PARCH_D = (210, 186, 138, 255)
PARCH_S = (188, 160, 112, 255)
WATER_D = (46, 90, 110, 255)
WATER_M = (74, 132, 148, 255)
WATER_L = (126, 188, 196, 255)
SAFE = (90, 168, 90, 255)
SAFE_L = (138, 204, 122, 255)
GOLD = (230, 178, 64, 255)
GOLD_L = (250, 220, 120, 255)
FISH = (220, 130, 54, 255)
FISH_L = (240, 170, 90, 255)
FISH_D = (170, 84, 36, 255)
WHITE = (250, 244, 230, 255)
DANGER = (196, 72, 64, 255)
DANGER_L = (230, 120, 100, 255)
KEY_BG = (92, 70, 52, 255)
KEY_HOT = (230, 178, 64, 255)
KEY_OK = (90, 168, 90, 255)
STAR = (230, 178, 64, 255)
STAR_D = (166, 116, 70, 255)
BUBBLE = (190, 230, 236, 255)
BUBBLE_L = (250, 250, 250, 255)

SCALE = 4  # logical pixel -> texture pixel (nearest neighbor)
DST = Path(__file__).resolve().parents[1] / "src/main/resources/assets/aquatech_ui/textures/gui/minigame/pixel"


def new(w: int, h: int) -> Image.Image:
    return Image.new("RGBA", (w, h), OUT)


def px(im: Image.Image, x: int, y: int, c) -> None:
    if 0 <= x < im.width and 0 <= y < im.height:
        im.putpixel((x, y), c)


def rect(im, x0, y0, x1, y1, c):
    for y in range(y0, y1):
        for x in range(x0, x1):
            px(im, x, y, c)


def hline(im, x0, x1, y, c):
    for x in range(x0, x1 + 1):
        px(im, x, y, c)


def vline(im, x, y0, y1, c):
    for y in range(y0, y1 + 1):
        px(im, x, y, c)


def outline_rect(im, x0, y0, x1, y1, c):
    hline(im, x0, x1, y0, c)
    hline(im, x0, x1, y1, c)
    vline(im, x0, y0, y1, c)
    vline(im, x1, y0, y1, c)


def save(im: Image.Image, name: str) -> None:
    DST.mkdir(parents=True, exist_ok=True)
    big = im.resize((im.width * SCALE, im.height * SCALE), Image.Resampling.NEAREST)
    path = DST / name
    big.save(path)
    print(f"  {name:28s} {im.width}x{im.height} -> {big.width}x{big.height}")


def make_panel():
    """Wooden dialogue panel — Stardew-style bevel + parchment inset."""
    w, h = 176, 108
    im = new(w, h)
    # outer wood frame
    rect(im, 0, 0, w, h, WOOD_D)
    rect(im, 1, 1, w - 1, h - 1, WOOD_M)
    # top-left highlight / bottom-right shade
    hline(im, 1, w - 3, 1, WOOD_L)
    vline(im, 1, 1, h - 3, WOOD_L)
    hline(im, 2, w - 2, h - 2, WOOD_D)
    vline(im, w - 2, 2, h - 2, WOOD_D)
    outline_rect(im, 0, 0, w - 1, h - 1, INK)
    # inner parchment
    rect(im, 6, 6, w - 6, h - 6, PARCH)
    rect(im, 7, 7, w - 7, h - 7, PARCH)
    hline(im, 7, w - 8, 7, WHITE)
    vline(im, 7, 7, h - 8, WHITE)
    hline(im, 8, w - 7, h - 7, PARCH_S)
    vline(im, w - 7, 8, h - 7, PARCH_S)
    outline_rect(im, 5, 5, w - 6, h - 6, INK)
    # corner bolts
    for cx, cy in ((3, 3), (w - 4, 3), (3, h - 4), (w - 4, h - 4)):
        px(im, cx, cy, INK)
        px(im, cx + 1, cy, WOOD_L)
        px(im, cx, cy + 1, WOOD_L)
        px(im, cx + 1, cy + 1, WOOD_D)
    # subtle wood grain (sparse 1px marks — not noise)
    for x, y in ((18, 3), (40, 2), (72, 3), (110, 2), (140, 3),
                 (22, h - 4), (60, h - 3), (100, h - 4), (150, h - 3)):
        px(im, x, y, WOOD_D if y < h // 2 else WOOD_L)
    save(im, "panel.png")


def make_bar():
    """Water trough bar track."""
    w, h = 148, 16
    im = new(w, h)
    rect(im, 0, 0, w, h, INK)
    rect(im, 1, 1, w - 1, h - 1, WATER_D)
    rect(im, 2, 2, w - 2, h - 2, WATER_M)
    # water highlight band
    hline(im, 2, w - 3, 3, WATER_L)
    hline(im, 3, w - 4, 4, WATER_L)
    # bottom shade
    hline(im, 2, w - 3, h - 3, WATER_D)
    # end caps (pipe look)
    for x in (1, w - 3):
        rect(im, x, 1, x + 2, h - 1, WOOD_M)
        px(im, x, 1, WOOD_L)
    save(im, "bar.png")

    # wave overlay frames (transparent strip)
    for fi, offset in enumerate((0, 2, 4, 2)):
        wv = new(w, h)
        for x in range(4, w - 4):
            yy = 5 + ((x + offset) // 3) % 2
            px(wv, x, yy, (*WATER_L[:3], 160))
            if (x + offset) % 6 == 0:
                px(wv, x, yy + 1, (*WHITE[:3], 120))
        save(wv, f"wave_f{fi}.png")


def make_zones():
    """1-wide strips to tile for safe / perfect / danger."""
    for name, c, cl in (
        ("zone_safe.png", SAFE, SAFE_L),
        ("zone_perfect.png", GOLD, GOLD_L),
        ("zone_danger.png", DANGER, DANGER_L),
    ):
        im = new(2, 12)
        rect(im, 0, 0, 2, 12, c)
        hline(im, 0, 1, 1, cl)
        hline(im, 0, 1, 10, (*c[:3], 200))
        save(im, name)


def make_bobber():
    """Classic red/white bobber — 2 bob frames (Stardew float vibe)."""
    for fi, dy in enumerate((0, 1)):
        im = new(14, 20)
        # cork float body
        outline = [
            "..............",
            "...######.....",
            "..#WWWWWW#....",
            "..#WWWWWW#....",
            "..#RRRRRR#....",
            "..#RRRRRR#....",
            "..#RRRRRR#....",
            "...######.....",
            ".....##.......",
            ".....##.......",
            ".....##.......",
            ".....##.......",
            ".....##.......",
            ".....#B#......",
            "..............",
            "..............",
            "..............",
            "..............",
            "..............",
            "..............",
        ]
        cmap = {"#": INK, "W": WHITE, "R": DANGER, "B": WATER_D, ".": OUT}
        for y, row in enumerate(outline):
            for x, ch in enumerate(row):
                if ch != ".":
                    px(im, x, y + dy, cmap[ch])
        # highlight pixel on white cap
        px(im, 4, 2 + dy, (*WHITE[:3], 255))
        px(im, 5, 2 + dy, GOLD_L)
        # stem shade
        px(im, 7, 8 + dy, WOOD_D)
        px(im, 7, 10 + dy, WOOD_D)
        save(im, f"bobber_f{fi}.png")


def draw_fish(im: Image.Image, ox: int, oy: int, frame: int, facing_right: bool = True):
    """20x14 cozy fish — clear head/body/tail silhouette, 4-frame swim."""
    # Facing RIGHT: head on right, tail flaps on left.
    # #=ink O=light X=mid o=dark W=white eye *=pupil
    frames = [
        [
            "....................",
            "..........###.......",
            "....#...##OOO##.....",
            "...##..#OOOOOOO#....",
            "..#.#.#OOXXXXXXo#...",
            ".##..#OXXX*XXXXXo#..",
            "#.#..#OXXWXXXXXXo#..",
            ".##..#OXXXXXXXXXo#..",
            "..#..#oXXXXXXXXXo#..",
            "...#..#oooooooo#....",
            "....#..##ooooo##....",
            "..........###.......",
            "....................",
            "....................",
        ],
        [
            "....................",
            "..........###.......",
            ".......##OOO##......",
            "..#...#OOOOOOO#.....",
            ".##..#OOXXXXXXo#....",
            "#.#.#OXXX*XXXXXo#...",
            ".##.#OXXWXXXXXXo#...",
            "..#.#OXXXXXXXXXo#...",
            "...##oXXXXXXXXXo#...",
            ".....#oooooooo#.....",
            "......##ooooo##.....",
            "..........###.......",
            "....................",
            "....................",
        ],
        [
            "....................",
            "..........###.......",
            ".......##OOO##......",
            "......#OOOOOOO#.....",
            "..#..#OOXXXXXXo#....",
            ".##.#OXXX*XXXXXo#...",
            "#.#.#OXXWXXXXXXo#...",
            ".##.#OXXXXXXXXXo#...",
            "..#.#oXXXXXXXXXo#...",
            "...#..#oooooooo#....",
            "....#..##ooooo##....",
            "..........###.......",
            "....................",
            "....................",
        ],
        [
            "....................",
            "..........###.......",
            ".......##OOO##......",
            "..#...#OOOOOOO#.....",
            ".##..#OOXXXXXXo#....",
            "#.#.#OXXX*XXXXXo#...",
            "##..#OXXWXXXXXXo#...",
            ".#..#OXXXXXXXXXo#...",
            "..#.#oXXXXXXXXXo#...",
            "...##.#oooooooo#....",
            "......##ooooo##.....",
            "..........###.......",
            "....................",
            "....................",
        ],
    ]
    cmap = {
        "#": INK,
        "O": FISH_L,
        "X": FISH,
        "o": FISH_D,
        "W": WHITE,
        "*": INK,
    }
    grid = frames[frame % 4]
    for y, row in enumerate(grid):
        for x, ch in enumerate(row):
            if ch in cmap:
                px(im, ox + x, oy + y, cmap[ch])
    # dorsal fin accent
    px(im, ox + 11, oy + 1, FISH_D)
    px(im, ox + 12, oy + 1, INK)
    if not facing_right:
        region = im.crop((ox, oy, ox + 20, oy + 14)).transpose(Image.Transpose.FLIP_LEFT_RIGHT)
        im.paste(region, (ox, oy))


def make_fish_sheet():
    """4-frame swim cycle, right-facing + left-facing sheets (20x14 cells)."""
    cw, ch = 20, 14
    for tag, facing in (("r", True), ("l", False)):
        sheet = new(cw * 4, ch)
        for fi in range(4):
            draw_fish(sheet, fi * cw, 0, fi, facing_right=facing)
        save(sheet, f"fish_{tag}.png")
        for fi in range(4):
            save(sheet.crop((fi * cw, 0, fi * cw + cw, ch)), f"fish_{tag}_f{fi}.png")


def make_keys():
    """Chunky keycaps A/D — idle / hot / held."""
    labels = {"a": None, "d": None}  # letters drawn in-game; shapes only here
    for state, border, fill, hi in (
        ("idle", INK, KEY_BG, WOOD_L),
        ("hot", INK, KEY_HOT, GOLD_L),
        ("held", INK, KEY_OK, SAFE_L),
    ):
        im = new(18, 18)
        rect(im, 1, 1, 17, 17, border)
        rect(im, 2, 2, 16, 16, fill)
        hline(im, 2, 15, 2, hi)
        vline(im, 2, 2, 15, hi)
        hline(im, 3, 15, 15, (*INK[:3], 180))
        vline(im, 15, 3, 15, (*INK[:3], 180))
        # raised center plate
        rect(im, 4, 4, 14, 14, (*fill[:3], 255))
        hline(im, 4, 13, 4, hi)
        save(im, f"key_{state}.png")


def make_stars():
    for filled in (True, False):
        im = new(11, 11)
        # classic 5-point-ish chunky star
        pts = [
            (5, 0), (6, 0),
            (6, 1), (7, 2), (8, 2), (9, 3), (10, 3),
            (8, 4), (9, 5), (10, 6), (9, 6), (8, 7),
            (9, 9), (8, 9), (7, 8), (6, 9), (5, 10),
            (4, 9), (3, 8), (2, 9), (1, 9),
            (2, 7), (1, 6), (0, 6), (1, 5), (2, 4),
            (0, 3), (1, 3), (2, 2), (3, 2), (4, 1), (5, 0),
        ]
        for x, y in pts:
            px(im, x, y, INK)
        # fill
        if filled:
            for y in range(1, 10):
                for x in range(1, 10):
                    if im.getpixel((x, y))[3] == 0:
                        # flood-ish: only near center
                        if abs(x - 5) + abs(y - 5) <= 4:
                            px(im, x, y, STAR)
            px(im, 5, 3, GOLD_L)
            px(im, 4, 4, GOLD_L)
        else:
            for y in range(2, 9):
                for x in range(2, 9):
                    if abs(x - 5) + abs(y - 5) <= 3 and im.getpixel((x, y))[3] == 0:
                        px(im, x, y, STAR_D)
        save(im, "star_on.png" if filled else "star_off.png")


def make_bubbles():
    for fi, r in enumerate((2, 3, 2)):
        im = new(8, 8)
        cx, cy = 3, 3 + (fi % 2)
        for y in range(8):
            for x in range(8):
                d = (x - cx) ** 2 + (y - cy) ** 2
                if d <= r * r:
                    px(im, x, y, BUBBLE)
                if d <= max(0, (r - 1)) ** 2 and x <= cx and y <= cy:
                    px(im, x, y, BUBBLE_L)
        # outline
        for y in range(8):
            for x in range(8):
                if im.getpixel((x, y))[3] == 0:
                    continue
                for dx, dy in ((-1, 0), (1, 0), (0, -1), (0, 1)):
                    nx, ny = x + dx, y + dy
                    if 0 <= nx < 8 and 0 <= ny < 8 and im.getpixel((nx, ny))[3] == 0:
                        px(im, nx, ny, (*WATER_D[:3], 200))
        save(im, f"bubble_f{fi}.png")


def make_sparkles():
    """3-frame twinkle (Terraria-style)."""
    patterns = [
        [(3, 3)],
        [(3, 2), (2, 3), (3, 3), (4, 3), (3, 4)],
        [(3, 1), (3, 2), (1, 3), (2, 3), (3, 3), (4, 3), (5, 3), (3, 4), (3, 5), (2, 2), (4, 2), (2, 4), (4, 4)],
    ]
    for fi, pts in enumerate(patterns):
        im = new(7, 7)
        for x, y in pts:
            px(im, x, y, GOLD_L if fi == 2 else WHITE)
        if fi >= 1:
            px(im, 3, 3, GOLD)
        save(im, f"sparkle_f{fi}.png")


def make_meter():
    bg = new(120, 8)
    rect(bg, 0, 0, 120, 8, INK)
    rect(bg, 1, 1, 119, 7, WOOD_D)
    rect(bg, 2, 2, 118, 6, PARCH_S)
    hline(bg, 2, 117, 2, PARCH)
    save(bg, "meter_bg.png")

    fill = new(2, 6)
    rect(fill, 0, 0, 2, 6, SAFE)
    hline(fill, 0, 1, 1, SAFE_L)
    hline(fill, 0, 1, 4, (*SAFE[:3], 200))
    save(fill, "meter_fill.png")

    timef = new(2, 3)
    rect(timef, 0, 0, 2, 3, DANGER)
    hline(timef, 0, 1, 0, DANGER_L)
    save(timef, "meter_time.png")


def make_heart_burst():
    """Tiny catch-success puff frames."""
    for fi in range(3):
        im = new(16, 16)
        r = 2 + fi * 2
        for y in range(16):
            for x in range(16):
                d = abs(x - 7) + abs(y - 7)  # diamond
                if d == r:
                    px(im, x, y, SAFE_L if fi < 2 else GOLD_L)
                if d == r - 1 and fi > 0:
                    px(im, x, y, (*WHITE[:3], 180))
        save(im, f"burst_f{fi}.png")


def main():
    print("Generating pixel fishing HUD ->", DST)
    make_panel()
    make_bar()
    make_zones()
    make_bobber()
    make_fish_sheet()
    make_keys()
    make_stars()
    make_bubbles()
    make_sparkles()
    make_meter()
    make_heart_burst()
    print("Done.")


if __name__ == "__main__":
    main()
