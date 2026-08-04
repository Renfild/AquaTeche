# -*- coding: utf-8 -*-
"""Generate distinct pixel-art textures for AquaTech rate mods x2..x64."""
from pathlib import Path

try:
    from PIL import Image, ImageDraw
except ImportError:
    import subprocess, sys
    subprocess.check_call([sys.executable, "-m", "pip", "install", "pillow", "-q"])
    from PIL import Image, ImageDraw

OUT = Path(__file__).resolve().parents[1] / "mods" / "aquatech-ui" / "src" / "main" / "resources" / "assets" / "aquatech_ui" / "textures" / "item"
OUT.mkdir(parents=True, exist_ok=True)

# Distinct palettes per tier (ocean / metal progression)
TIERS = {
    2:  {"name": "rate_x2",  "body": (70, 130, 160), "accent": (160, 220, 255), "core": (40, 80, 100), "glyph": (230, 250, 255)},
    4:  {"name": "rate_x4",  "body": (55, 150, 95),  "accent": (140, 240, 170), "core": (25, 70, 45),  "glyph": (235, 255, 240)},
    8:  {"name": "rate_x8",  "body": (180, 140, 50), "accent": (255, 220, 90),  "core": (90, 60, 20),  "glyph": (255, 250, 210)},
    16: {"name": "rate_x16", "body": (70, 120, 200), "accent": (130, 190, 255), "core": (30, 50, 110), "glyph": (230, 240, 255)},
    32: {"name": "rate_x32", "body": (150, 70, 180), "accent": (220, 140, 255), "core": (70, 30, 90),  "glyph": (250, 230, 255)},
    64: {"name": "rate_x64", "body": (200, 70, 60),  "accent": (255, 150, 110), "core": (90, 25, 25),  "glyph": (255, 240, 230)},
}

# Tiny 3x5 digit glyphs for 2,4,8,1,6,3 (used to compose labels)
DIGITS = {
    "2": ["###", "  #", "###", "#  ", "###"],
    "4": ["# #", "# #", "###", "  #", "  #"],
    "8": ["###", "# #", "###", "# #", "###"],
    "1": [" # ", "## ", " # ", " # ", "###"],
    "6": ["###", "#  ", "###", "# #", "###"],
    "3": ["###", "  #", "###", "  #", "###"],
    "x": ["   ", "# #", " # ", "# #", "   "],
}


def draw_digit(px, x, y, ch, color):
    rows = DIGITS.get(ch)
    if not rows:
        return
    for dy, row in enumerate(rows):
        for dx, c in enumerate(row):
            if c == "#":
                px[x + dx, y + dy] = color + (255,)


def make_rate(mult: int, cfg: dict) -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    body, accent, core, glyph = cfg["body"], cfg["accent"], cfg["core"], cfg["glyph"]

    # Tackle-plate base (rounded square)
    for y in range(2, 14):
        for x in range(2, 14):
            edge = x in (2, 13) or y in (2, 13)
            px[x, y] = (accent if edge else body) + (255,)

    # Inner well
    for y in range(4, 12):
        for x in range(4, 12):
            px[x, y] = core + (255,)

    # Corner rivets (different pattern per tier band)
    rivets = {
        2: [(3, 3), (12, 3), (3, 12), (12, 12)],
        4: [(3, 3), (12, 3), (3, 12), (12, 12), (7, 3)],
        8: [(3, 3), (12, 3), (3, 12), (12, 12), (3, 7), (12, 7)],
        16: [(3, 3), (12, 3), (3, 12), (12, 12), (7, 3), (7, 12)],
        32: [(3, 3), (12, 3), (3, 12), (12, 12), (3, 7), (12, 7), (7, 3)],
        64: [(3, 3), (12, 3), (3, 12), (12, 12), (3, 7), (12, 7), (7, 3), (7, 12)],
    }
    for rx, ry in rivets.get(mult, []):
        px[rx, ry] = accent + (255,)

    # Accent stripe — unique orientation per tier
    if mult == 2:
        for x in range(4, 12):
            px[x, 5] = accent + (255,)
    elif mult == 4:
        for y in range(4, 12):
            px[5, y] = accent + (255,)
    elif mult == 8:
        for i in range(4, 12):
            px[i, i] = accent + (255,)
    elif mult == 16:
        for x in range(4, 12):
            px[x, 5] = accent + (255,)
            px[x, 10] = accent + (255,)
    elif mult == 32:
        for y in range(4, 12):
            px[5, y] = accent + (255,)
            px[10, y] = accent + (255,)
    else:  # 64
        for i in range(4, 12):
            px[i, i] = accent + (255,)
            px[i, 15 - i] = accent + (255,)

    # Label "xN" centered in well
    label = f"x{mult}"
    # approximate width
    width = len(label) * 4 - 1
    start_x = max(4, (16 - width) // 2)
    start_y = 6
    cx = start_x
    for ch in label:
        draw_digit(px, cx, start_y, ch, glyph)
        cx += 4

    # Shine pixel
    px[4, 4] = (255, 255, 255, 180)
    return img


def main():
    for mult, cfg in TIERS.items():
        img = make_rate(mult, cfg)
        path = OUT / f"{cfg['name']}.png"
        img.save(path)
        print("wrote", path)


if __name__ == "__main__":
    main()
