# -*- coding: utf-8 -*-
"""Generate clean minimal AquaTech machine GUIs.

Textures are drawn to EXACT menu slot coordinates — no concept-art crops.
Style: deep ocean navy, thin cyan accents, vanilla-like slot wells.
"""
from pathlib import Path
from PIL import Image, ImageDraw

OUT = Path(r"C:\Users\xieto\Desktop\AquaTech\mods\aquatech-ui\src\main\resources\assets\aquatech_ui\textures\gui")
W, H = 176, 166

# Palette (matches UiDraw)
NAVY = (14, 36, 48, 255)
NAVY2 = (22, 54, 72, 255)
PANEL = (22, 48, 64, 255)
EDGE = (55, 95, 120, 255)
EDGE_HI = (95, 155, 185, 255)
ACCENT = (90, 200, 250, 255)
ACCENT_DIM = (50, 140, 175, 255)
SLOT_BG = (10, 22, 32, 255)
SLOT_EDGE = (70, 170, 210, 255)
INV_SLOT = (14, 28, 38, 255)
INV_EDGE = (55, 90, 110, 255)
DIV = (40, 80, 105, 255)
ARROW = (70, 220, 160, 255)
ENERGY_BOT = (20, 90, 140)
ENERGY_TOP = (100, 220, 255)
RED_BOT = (100, 30, 40)
RED_TOP = (255, 90, 90)
GREEN_BOT = (20, 90, 50)
GREEN_TOP = (120, 255, 160)
ORANGE_BOT = (120, 60, 20)
ORANGE_TOP = (255, 180, 70)


def new_atlas():
    return Image.new("RGBA", (256, 256), (0, 0, 0, 0))


def panel(d, fill=PANEL):
    d.rectangle([0, 0, W - 1, H - 1], fill=NAVY)
    for y in range(3, H - 3):
        t = (y - 3) / (H - 7)
        r = int(20 + 8 * t)
        g = int(46 + 10 * t)
        b = int(62 + 12 * t)
        d.line([(3, y), (W - 4, y)], fill=(r, g, b, 255))
    d.rectangle([3, 3, W - 4, 3], fill=EDGE_HI)
    d.rectangle([3, 3, 3, H - 4], fill=EDGE_HI)
    d.rectangle([3, H - 4, W - 4, H - 4], fill=EDGE)
    d.rectangle([W - 4, 3, W - 4, H - 4], fill=EDGE)
    d.rectangle([8, 2, W - 9, 2], fill=ACCENT)
    for x, y in ((6, 6), (W - 10, 6), (6, H - 10), (W - 10, H - 10)):
        d.rectangle([x, y, x + 3, y], fill=ACCENT)
        d.rectangle([x, y, x, y + 3], fill=ACCENT)


def divider(d, y=82):
    d.rectangle([8, y, W - 9, y], fill=DIV)
    d.rectangle([8, y + 1, W - 9, y + 1], fill=EDGE)


def slot(d, x, y, accent=SLOT_EDGE, bg=SLOT_BG):
    """18x18 chrome around 16x16 item at (x,y)."""
    d.rectangle([x - 1, y - 1, x + 16, y + 16], fill=accent)
    d.rectangle([x, y, x + 15, y + 15], fill=bg)


def inv_grid(d):
    for row in range(3):
        for col in range(9):
            slot(d, 8 + col * 18, 84 + row * 18, INV_EDGE, INV_SLOT)
    for col in range(9):
        slot(d, 8 + col * 18, 142, INV_EDGE, INV_SLOT)


def energy_well(d, x, y, w, h):
    d.rectangle([x - 1, y - 1, x + w, y + h], fill=EDGE)
    d.rectangle([x, y, x + w - 1, y + h - 1], fill=(6, 12, 18, 255))


def arrow_well(d, x, y):
    # empty chevron outline (24x17 area used by blit)
    pts = [(x, y + 4), (x + 14, y + 4), (x + 14, y), (x + 24, y + 8),
           (x + 14, y + 16), (x + 14, y + 12), (x, y + 12)]
    d.polygon(pts, outline=ACCENT_DIM, fill=(10, 22, 30, 255))


def pack_overlays(atlas, energy_colors, arrow_color=ARROW):
    d = ImageDraw.Draw(atlas)
    bot, top = energy_colors
    for i in range(52):
        t = i / 51
        r = int(bot[0] + (top[0] - bot[0]) * (1 - t))
        g = int(bot[1] + (top[1] - bot[1]) * (1 - t))
        b = int(bot[2] + (top[2] - bot[2]) * (1 - t))
        d.line([(176, 51 - i), (187, 51 - i)], fill=(r, g, b, 255))
    # arrow strip at v=52
    d.rectangle([176, 52, 199, 68], fill=(12, 22, 30, 255))
    for i in range(24):
        hh = 4 + (i * 9) // 24
        cy = 60
        d.line([(176 + i, cy - hh // 2), (176 + i, cy + hh // 2)], fill=arrow_color)
    # flame strip
    d.rectangle([176, 70, 189, 83], fill=(20, 28, 24, 255))
    for i in range(14):
        d.line([(176 + i, 83 - i), (176 + i, 83)], fill=(180, 255, 120, 255))


def save(atlas, name):
    OUT.mkdir(parents=True, exist_ok=True)
    path = OUT / f"{name}.png"
    atlas.save(path, optimize=True)
    print("wrote", path.name, path.stat().st_size)


def make_processor(name, accent, energy_xywh, energy_colors, input_xy, out_origin, upgrade_xy, arrow_xy):
    """input + energy + arrow + 3x3 + upgrade"""
    atlas = new_atlas()
    panel_img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(panel_img)
    panel(d)
    divider(d)
    inv_grid(d)

    ex, ey, ew, eh = energy_xywh
    energy_well(d, ex, ey, ew, eh)
    slot(d, *input_xy, accent)
    ox, oy = out_origin
    for row in range(3):
        for col in range(3):
            slot(d, ox + col * 18, oy + row * 18, accent)
    slot(d, *upgrade_xy, ACCENT_DIM)
    arrow_well(d, *arrow_xy)

    # subtle machine header bar
    d.rectangle([8, 14, W - 9, 14], fill=DIV)

    atlas.paste(panel_img, (0, 0))
    pack_overlays(atlas, energy_colors, accent if len(accent) == 4 else ACCENT)
    save(atlas, name)


def make_auto_fisher():
    make_processor(
        "auto_fisher", ACCENT,
        energy_xywh=(48, 18, 12, 52),
        energy_colors=(RED_BOT, RED_TOP),
        input_xy=(26, 35),
        out_origin=(98, 17),
        upgrade_xy=(62, 58),
        arrow_xy=(70, 35),
    )


def make_ocean_filter():
    make_processor(
        "ocean_filter", ACCENT,
        energy_xywh=(8, 18, 10, 52),
        energy_colors=(ENERGY_BOT, ENERGY_TOP),
        input_xy=(26, 35),
        out_origin=(98, 17),
        upgrade_xy=(62, 58),
        arrow_xy=(70, 35),
    )


def make_seabed_dredger():
    make_processor(
        "seabed_dredger", (255, 170, 70, 255),
        energy_xywh=(8, 18, 10, 52),
        energy_colors=(ORANGE_BOT, ORANGE_TOP),
        input_xy=(26, 35),
        out_origin=(98, 17),
        upgrade_xy=(50, 58),
        arrow_xy=(70, 35),
    )


def make_hydro():
    atlas = new_atlas()
    panel_img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(panel_img)
    panel(d)
    divider(d)
    inv_grid(d)
    energy_well(d, 152, 18, 12, 52)
    slot(d, 80, 35, (120, 255, 160, 255))
    slot(d, 26, 55, ACCENT_DIM)
    # small flame well under fuel
    d.rectangle([80, 54, 95, 69], fill=EDGE)
    d.rectangle([81, 55, 94, 68], fill=(10, 20, 14, 255))
    d.rectangle([8, 14, W - 9, 14], fill=DIV)
    atlas.paste(panel_img, (0, 0))
    pack_overlays(atlas, (GREEN_BOT, GREEN_TOP), (120, 255, 160, 255))
    save(atlas, "hydro_reactor")


def make_altar():
    atlas = new_atlas()
    panel_img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(panel_img)
    panel(d, fill=(14, 42, 48, 255))
    divider(d)
    inv_grid(d)
    purple = (180, 90, 220, 255)
    gold = (240, 190, 70, 255)
    for xy in ((44, 17), (116, 17), (44, 53), (116, 53)):
        slot(d, *xy, purple)
    slot(d, 80, 35, gold)
    # conduit lines
    for x, y in ((52, 25), (124, 25), (52, 61), (124, 61)):
        d.line([(x, y), (88, 43)], fill=ACCENT_DIM, width=1)
    d.rectangle([8, 14, W - 9, 14], fill=DIV)
    atlas.paste(panel_img, (0, 0))
    pack_overlays(atlas, (ENERGY_BOT, ENERGY_TOP), ACCENT)
    save(atlas, "ocean_altar")


def make_tackle():
    atlas = new_atlas()
    panel_img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(panel_img)
    panel(d)
    divider(d)
    inv_grid(d)
    xs = (26, 62, 98, 134)
    for x in xs:
        slot(d, x, 32, ACCENT)
    d.rectangle([8, 14, W - 9, 14], fill=DIV)
    atlas.paste(panel_img, (0, 0))
    pack_overlays(atlas, (ENERGY_BOT, ENERGY_TOP), ACCENT)
    save(atlas, "tackle_box")


def main():
    make_auto_fisher()
    make_ocean_filter()
    make_seabed_dredger()
    make_hydro()
    make_altar()
    make_tackle()
    print("done — clean AquaTech GUIs")


if __name__ == "__main__":
    main()
