# -*- coding: utf-8 -*-
"""Generate unique Minecraft GUI atlases (256x256) matching AquaTech/img concept previews."""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
import math

OUT = Path(r"C:\Users\xieto\Desktop\AquaTech\mods\aquatech-ui\src\main\resources\assets\aquatech_ui\textures\gui")

# Shared layout constants (must match Java menus)
W, H = 176, 166
INV_OX, INV_OY = 7, 83


def new_atlas():
    return Image.new("RGBA", (256, 256), (0, 0, 0, 0)), None


def draw(img):
    return ImageDraw.Draw(img)


def rivet(d, x, y, color=(120, 130, 140, 255)):
    d.ellipse([x, y, x + 3, y + 3], fill=color)
    d.point((x + 1, y + 1), fill=(200, 210, 220, 255))


def bevel_panel(d, x, y, w, h, bg, edge_hi, edge_lo, border):
    d.rectangle([x, y, x + w - 1, y + h - 1], fill=bg)
    d.rectangle([x, y, x + w - 1, y], fill=edge_hi)
    d.rectangle([x, y, x, y + h - 1], fill=edge_hi)
    d.rectangle([x + w - 1, y, x + w - 1, y + h - 1], fill=edge_lo)
    d.rectangle([x, y + h - 1, x + w - 1, y + h - 1], fill=edge_lo)
    d.rectangle([x, y, x + w - 1, y + h - 1], outline=border)


def slot_glow(d, x, y, size=18, glow=(0, 255, 255, 255), inner=(10, 20, 30, 255)):
    # thick neon border
    d.rectangle([x - 1, y - 1, x + size, y + size], outline=glow)
    d.rectangle([x - 2, y - 2, x + size + 1, y + size + 1], outline=(glow[0] // 2, glow[1] // 2, glow[2] // 2, 180))
    d.rectangle([x, y, x + size - 1, y + size - 1], fill=(0, 0, 0, 255))
    d.rectangle([x + 1, y + 1, x + size - 2, y + size - 2], fill=inner)
    d.rectangle([x, y, x + size - 1, y], fill=(0, 0, 0, 255))
    d.rectangle([x, y, x, y + size - 1], fill=(0, 0, 0, 255))
    d.rectangle([x + size - 1, y, x + size - 1, y + size - 1], fill=(glow[0] // 3, glow[1] // 3, glow[2] // 3, 255))
    d.rectangle([x, y + size - 1, x + size - 1, y + size - 1], fill=(glow[0] // 3, glow[1] // 3, glow[2] // 3, 255))


def slot_plain(d, x, y, size=18, fill=(25, 35, 45, 255), border=(80, 90, 100, 255)):
    d.rectangle([x, y, x + size - 1, y + size - 1], fill=fill)
    d.rectangle([x, y, x + size - 1, y], fill=(0, 0, 0, 255))
    d.rectangle([x, y, x, y + size - 1], fill=(0, 0, 0, 255))
    d.rectangle([x + size - 1, y, x + size - 1, y + size - 1], fill=border)
    d.rectangle([x, y + size - 1, x + size - 1, y + size - 1], fill=border)
    d.rectangle([x + 1, y + 1, x + size - 2, y + size - 2], fill=(fill[0] + 8, fill[1] + 8, fill[2] + 10, 255))


def inv_grid(d, ox, oy, fill=(25, 35, 45, 255), border=(70, 85, 100, 255)):
    for row in range(3):
        for col in range(9):
            slot_plain(d, ox + col * 18, oy + row * 18, fill=fill, border=border)
    for col in range(9):
        slot_plain(d, ox + col * 18, oy + 3 * 18 + 4, fill=fill, border=border)


def energy_fill_strip(d, u, v, w, h, color_bot, color_top):
    """Vertical energy fill sample for blitting (bottom = full)."""
    for i in range(h):
        t = i / max(1, h - 1)
        r = int(color_bot[0] + (color_top[0] - color_bot[0]) * (1 - t))
        g = int(color_bot[1] + (color_top[1] - color_bot[1]) * (1 - t))
        b = int(color_bot[2] + (color_top[2] - color_bot[2]) * (1 - t))
        d.line([(u, v + h - 1 - i), (u + w - 1, v + h - 1 - i)], fill=(r, g, b, 255))


def arrow_fill(d, u, v, color):
    for i in range(24):
        hh = 4 + (i * 9) // 24
        cy = v + 8
        d.line([(u + i, cy - hh // 2), (u + i, cy + hh // 2)], fill=color)


def flame_fill(d, u, v, color):
    for i in range(14):
        d.line([(u + i, v + 13 - i), (u + i, v + 13)], fill=color)


def write_px(d, x, y, text, color=(255, 255, 255, 255)):
    """Tiny 3x5 style labels via default font (Minecraft will draw real titles in Java)."""
    try:
        font = ImageFont.load_default()
        d.text((x, y), text, fill=color, font=font)
    except Exception:
        d.text((x, y), text, fill=color)


def save(img, name):
    OUT.mkdir(parents=True, exist_ok=True)
    path = OUT / f"{name}.png"
    img.save(path)
    print("wrote", path)


# ---------------------------------------------------------------------------
# AUTO-FISHER — slate + neon cyan, red FE, green arrow
# Layout: input(20,35) energy(42,18) arrow(60,36) 3x3(98,18) upgrade(62,58)
# ---------------------------------------------------------------------------
def make_auto_fisher():
    img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    d = draw(img)
    bg, bg2 = (18, 32, 48, 255), (28, 44, 62, 255)
    cyan = (0, 230, 255, 255)
    cyan_dim = (0, 140, 170, 255)
    bevel_panel(d, 0, 0, W, H, bg, (60, 80, 100, 255), (10, 18, 28, 255), cyan_dim)
    d.rectangle([2, 2, W - 3, H - 3], outline=cyan)
    # title plate
    d.rectangle([40, 3, 136, 14], fill=(12, 22, 34, 255), outline=cyan_dim)
    for p in [(4, 4), (W - 8, 4), (4, H - 8), (W - 8, H - 8)]:
        rivet(d, p[0], p[1], cyan_dim)

    # machine area separator
    d.rectangle([4, 78, W - 5, 80], fill=cyan_dim)

    slot_glow(d, 20, 35, glow=cyan, inner=(8, 28, 40, 255))  # input
    # red energy well
    d.rectangle([41, 17, 54, 70], fill=(30, 10, 10, 255), outline=(180, 40, 40, 255))
    # lightning hint
    d.polygon([(44, 62), (48, 55), (46, 55), (50, 48), (47, 56), (49, 56)], fill=(255, 60, 60, 255))
    write_px(d, 44, 66, "E", (255, 80, 80, 255))

    # green arrow well
    d.rectangle([59, 35, 84, 53], fill=(20, 40, 30, 255), outline=(40, 120, 60, 255))
    # static dim arrow body
    arrow_fill(d, 60, 36, (30, 80, 50, 255))

    for r in range(3):
        for c in range(3):
            slot_glow(d, 98 + c * 18, 17 + r * 18, glow=cyan, inner=(8, 28, 40, 255))
    slot_glow(d, 62, 57, glow=cyan_dim, inner=(8, 28, 40, 255))  # upgrade

    inv_grid(d, INV_OX, INV_OY, fill=(22, 36, 50, 255), border=(90, 110, 130, 255))

    # overlays @176
    energy_fill_strip(d, 176, 0, 12, 52, (120, 20, 20, 255), (255, 70, 70, 255))
    d.rectangle([176, 52, 199, 68], fill=(20, 40, 30, 255))
    arrow_fill(d, 176, 52, (40, 220, 80, 255))
    d.rectangle([176, 70, 189, 83], fill=(40, 30, 20, 255))
    flame_fill(d, 176, 70, (255, 140, 40, 255))
    save(img, "auto_fisher")


# ---------------------------------------------------------------------------
# HYDRO REACTOR — 3 panels, neon green, energy RIGHT
# fuel(80,35) upgrade(26,53) energy(152,18) flame(81,56)
# ---------------------------------------------------------------------------
def make_hydro_reactor():
    img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    d = draw(img)
    bg = (24, 34, 44, 255)
    green = (85, 255, 119, 255)
    green_dim = (40, 140, 70, 255)
    bevel_panel(d, 0, 0, W, H, bg, (70, 85, 100, 255), (12, 18, 24, 255), (50, 70, 90, 255))
    d.rectangle([40, 3, 136, 14], fill=(16, 22, 30, 255), outline=green_dim)
    for p in [(4, 4), (W - 8, 4), (4, 72), (W - 8, 72)]:
        rivet(d, p[0], p[1])

    # left gauge panel
    bevel_panel(d, 6, 16, 40, 58, (18, 26, 34, 255), (55, 70, 85, 255), (8, 12, 16, 255), green_dim)
    # tick gauge
    d.rectangle([10, 20, 16, 60], fill=(10, 16, 20, 255), outline=green_dim)
    for i in range(0, 40, 4):
        d.line([(16, 20 + i), (20, 20 + i)], fill=green_dim)
    write_px(d, 22, 22, "FE/t", green)
    write_px(d, 22, 52, "Max", (180, 200, 180, 255))
    slot_glow(d, 26, 53, glow=green_dim, inner=(10, 30, 18, 255))  # upgrade

    # center fuel panel
    bevel_panel(d, 50, 16, 90, 58, (14, 22, 30, 255), (55, 70, 85, 255), (8, 12, 16, 255), (40, 60, 80, 255))
    d.ellipse([68, 22, 120, 66], outline=green_dim, width=1)
    slot_glow(d, 80, 35, glow=green, inner=(8, 28, 16, 255))
    # flame well
    d.rectangle([80, 55, 94, 70], fill=(20, 30, 20, 255), outline=green_dim)

    # right energy tube
    bevel_panel(d, 144, 16, 26, 58, (18, 26, 34, 255), (55, 70, 85, 255), (8, 12, 16, 255), green_dim)
    d.rectangle([151, 17, 164, 70], fill=(10, 30, 18, 255), outline=green)

    d.rectangle([4, 78, W - 5, 80], fill=(40, 60, 50, 255))
    inv_grid(d, INV_OX, INV_OY, fill=(28, 38, 48, 255), border=(80, 100, 90, 255))

    energy_fill_strip(d, 176, 0, 12, 52, (20, 100, 40, 255), (120, 255, 150, 255))
    d.rectangle([176, 52, 199, 68], fill=(20, 40, 30, 255))
    arrow_fill(d, 176, 52, green)
    d.rectangle([176, 70, 189, 83], fill=(30, 40, 20, 255))
    flame_fill(d, 176, 70, (180, 255, 100, 255))
    save(img, "hydro_reactor")


# ---------------------------------------------------------------------------
# OCEAN FILTER — riveted industrial, turbine, 3x3 out
# input(20,35) turbine(52,28) 3x3(98,18) upgrade(62,58) energy(8,18)
# ---------------------------------------------------------------------------
def make_ocean_filter():
    img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    d = draw(img)
    bg = (36, 40, 46, 255)
    cyan = (80, 220, 255, 255)
    cyan_dim = (40, 130, 160, 255)
    bevel_panel(d, 0, 0, W, H, bg, (90, 100, 110, 255), (20, 24, 28, 255), (70, 80, 90, 255))
    d.rectangle([48, 2, 128, 14], fill=(20, 24, 28, 255), outline=cyan_dim)
    for p in [(5, 5), (W - 9, 5), (5, 72), (W - 9, 72), (5, H - 9), (W - 9, H - 9)]:
        rivet(d, p[0], p[1], (140, 150, 160, 255))

    # energy thin left
    d.rectangle([7, 17, 18, 70], fill=(12, 30, 40, 255), outline=cyan_dim)

    slot_glow(d, 20, 35, glow=cyan, inner=(12, 28, 38, 255))

    # turbine well (empty circle)
    cx, cy, rad = 64, 44, 18
    d.ellipse([cx - rad, cy - rad, cx + rad, cy + rad], fill=(20, 28, 36, 255), outline=cyan_dim, width=2)
    d.ellipse([cx - 4, cy - 4, cx + 4, cy + 4], fill=cyan_dim)
    # static blades (dim)
    for a in range(0, 360, 60):
        rad_a = math.radians(a)
        x2 = cx + int(math.cos(rad_a) * (rad - 3))
        y2 = cy + int(math.sin(rad_a) * (rad - 3))
        d.line([(cx, cy), (x2, y2)], fill=(50, 90, 110, 255), width=2)

    for r in range(3):
        for c in range(3):
            slot_glow(d, 98 + c * 18, 17 + r * 18, glow=cyan, inner=(12, 28, 38, 255))
    slot_glow(d, 62, 57, glow=cyan_dim, inner=(12, 28, 38, 255))

    write_px(d, 8, 74, "Inventory", (220, 230, 240, 255))
    d.rectangle([4, 78, W - 5, 80], fill=cyan_dim)
    inv_grid(d, INV_OX, INV_OY, fill=(32, 36, 42, 255), border=(90, 100, 110, 255))

    energy_fill_strip(d, 176, 0, 12, 52, (20, 80, 120, 255), (80, 220, 255, 255))
    # turbine fill frames: use arrow strip as bright blades
    d.rectangle([176, 52, 199, 68], fill=(20, 28, 36, 255))
    for i in range(24):
        # bright cyan sweep
        d.point((176 + i, 60), fill=cyan)
        if i % 4 == 0:
            d.line([(176 + i, 54), (176 + i, 66)], fill=cyan)
    # also full turbine overlay tile at 200,52 size 36x36
    d.ellipse([200, 52, 235, 87], fill=(30, 50, 70, 180), outline=cyan)
    for a in range(0, 360, 45):
        rad_a = math.radians(a)
        x2 = 217 + int(math.cos(rad_a) * 14)
        y2 = 69 + int(math.sin(rad_a) * 14)
        d.line([(217, 69), (x2, y2)], fill=cyan, width=2)
    d.rectangle([176, 70, 189, 83], fill=(30, 40, 20, 255))
    flame_fill(d, 176, 70, (255, 160, 40, 255))
    save(img, "ocean_filter")


# ---------------------------------------------------------------------------
# SEABED DREDGER — charcoal + orange, gauge left, 3x3 right
# drill(26,24) energy(8,18) 3x3(98,18) upgrade(50,58) progress monitor fill
# ---------------------------------------------------------------------------
def make_seabed_dredger():
    img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    d = draw(img)
    bg = (32, 34, 38, 255)
    orange = (255, 160, 40, 255)
    orange_dim = (180, 100, 30, 255)
    bevel_panel(d, 0, 0, W, H, bg, (80, 82, 86, 255), (16, 18, 20, 255), (60, 62, 66, 255))
    d.rectangle([40, 2, 136, 14], fill=(18, 20, 22, 255), outline=orange_dim)
    write_px(d, 52, 4, "SEABED DREDGER", orange)
    for p in [(5, 5), (W - 9, 5), (5, 72), (W - 9, 72)]:
        rivet(d, p[0], p[1], (150, 120, 80, 255))

    # left monitor panel
    bevel_panel(d, 6, 16, 84, 58, (22, 24, 28, 255), (70, 72, 76, 255), (10, 12, 14, 255), orange_dim)
    d.rectangle([10, 20, 70, 48], fill=(12, 14, 16, 255), outline=orange)
    # scanlines
    for y in range(22, 47, 3):
        d.line([(12, y), (68, y)], fill=(40, 30, 20, 100))
    slot_glow(d, 26, 24, glow=orange, inner=(30, 20, 10, 255))  # drill

    # energy well left edge of monitor
    d.rectangle([7, 17, 16, 70], fill=(30, 18, 8, 255), outline=orange_dim)

    # MPa gauge
    d.ellipse([14, 50, 38, 74], outline=orange_dim, width=2)
    d.ellipse([22, 58, 30, 66], fill=orange_dim)
    d.line([(26, 62), (32, 54)], fill=orange, width=1)
    write_px(d, 40, 58, "MPa", orange_dim)
    # indicator light
    d.rectangle([72, 56, 82, 66], fill=orange, outline=(255, 220, 100, 255))
    slot_glow(d, 50, 57, glow=orange_dim, inner=(30, 20, 10, 255))  # upgrade

    for r in range(3):
        for c in range(3):
            slot_glow(d, 98 + c * 18, 17 + r * 18, glow=orange, inner=(30, 20, 10, 255))

    d.rectangle([4, 78, W - 5, 80], fill=orange_dim)
    inv_grid(d, INV_OX, INV_OY, fill=(40, 36, 32, 255), border=(120, 90, 50, 255))

    energy_fill_strip(d, 176, 0, 12, 52, (120, 50, 10, 255), (255, 180, 40, 255))
    d.rectangle([176, 52, 199, 68], fill=(30, 20, 10, 255))
    arrow_fill(d, 176, 52, orange)
    # monitor progress fill strip (used as partial width over monitor)
    for i in range(24):
        d.line([(176 + i, 52), (176 + i, 68)], fill=(255, 140 + i, 20, 200))
    d.rectangle([176, 70, 189, 83], fill=(40, 30, 20, 255))
    flame_fill(d, 176, 70, orange)
    save(img, "seabed_dredger")


# ---------------------------------------------------------------------------
# OCEAN ALTAR — prismarine + runes, 4 purple corners + gold center
# TL(44,16) TR(98,16) BL(44,52) BR(98,52) center(71,34)
# ---------------------------------------------------------------------------
def make_ocean_altar():
    img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    d = draw(img)
    stone = (40, 70, 75, 255)
    stone2 = (55, 90, 95, 255)
    cyan = (60, 255, 230, 255)
    purple = (200, 80, 255, 255)
    gold = (255, 210, 60, 255)
    bevel_panel(d, 0, 0, W, H, stone, stone2, (20, 40, 45, 255), (80, 130, 140, 255))
    # brick texture hints
    for y in range(16, 76, 8):
        d.line([(4, y), (W - 5, y)], fill=(30, 55, 60, 120))
    d.rectangle([36, 2, 140, 14], fill=(20, 40, 48, 255), outline=cyan)
    write_px(d, 48, 4, "Ocean Relic Altar", cyan)

    # rune strips
    for x in range(10, 166, 8):
        d.rectangle([x, 15, x + 3, 17], fill=cyan)
        d.rectangle([x, 76, x + 3, 78], fill=cyan)

    # conduits (static)
    # center box
    cx, cy = 71 + 9, 34 + 9
    for ox, oy in [(44 + 9, 16 + 9), (98 + 9, 16 + 9), (44 + 9, 52 + 9), (98 + 9, 52 + 9)]:
        # L-shaped conduit approx
        mid_x = (ox + cx) // 2
        d.line([(ox, oy), (mid_x, oy), (mid_x, cy), (cx, cy)], fill=(40, 180, 170, 200), width=2)

    slot_glow(d, 44, 16, glow=purple, inner=(30, 10, 40, 255))
    slot_glow(d, 98, 16, glow=purple, inner=(30, 10, 40, 255))
    slot_glow(d, 44, 52, glow=purple, inner=(30, 10, 40, 255))
    slot_glow(d, 98, 52, glow=purple, inner=(30, 10, 40, 255))
    slot_glow(d, 71, 34, glow=gold, inner=(40, 30, 10, 255))

    inv_grid(d, INV_OX, INV_OY, fill=(35, 55, 60, 255), border=(70, 120, 130, 255))

    # progress = conduit brighten strip
    energy_fill_strip(d, 176, 0, 12, 52, (20, 80, 90, 255), cyan)
    d.rectangle([176, 52, 199, 68], fill=(20, 40, 48, 255))
    arrow_fill(d, 176, 52, cyan)
    # bright conduit overlay tile
    for i in range(24):
        d.line([(176 + i, 58), (176 + i, 62)], fill=cyan)
    d.rectangle([176, 70, 189, 83], fill=(40, 30, 20, 255))
    flame_fill(d, 176, 70, gold)
    save(img, "ocean_altar")


# ---------------------------------------------------------------------------
# TACKLE BOX — navy + cyan, labeled LURE/BAIT/BOBBER/HOOK
# slots at (22,38),(58,38),(94,38),(130,38)
# ---------------------------------------------------------------------------
def make_tackle_box():
    img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    d = draw(img)
    bg = (30, 48, 78, 255)
    cyan = (0, 255, 255, 255)
    cyan_dim = (0, 160, 180, 255)
    bevel_panel(d, 0, 0, W, H, bg, (70, 100, 140, 255), (16, 28, 48, 255), (50, 80, 120, 255))
    d.rectangle([28, 2, 148, 14], fill=(18, 30, 50, 255), outline=cyan_dim)
    write_px(d, 42, 4, "Fishing Rod Tackle Box", (240, 245, 255, 255))
    # anchors (simple)
    for ax in [20, 152]:
        d.ellipse([ax, 3, ax + 6, 9], outline=(140, 150, 160, 255))
        d.line([(ax + 3, 9), (ax + 3, 13)], fill=(140, 150, 160, 255))

    for p in [(5, 5), (W - 9, 5), (5, 72), (W - 9, 72)]:
        rivet(d, p[0], p[1], (150, 170, 190, 255))

    labels = ["LURE", "BAIT", "BOBBER", "HOOK"]
    xs = [22, 58, 94, 130]
    for i, sx in enumerate(xs):
        write_px(d, sx, 28, labels[i], (220, 240, 255, 255))
        slot_glow(d, sx, 38, glow=cyan, inner=(10, 24, 40, 255))

    # wavy water lines
    for x0 in [8, 100]:
        for i in range(20):
            yy = 70 + int(2 * math.sin(i / 2))
            d.point((x0 + i, yy), fill=cyan_dim)

    d.rectangle([4, 78, W - 5, 80], fill=cyan_dim)
    inv_grid(d, INV_OX, INV_OY, fill=(24, 40, 64, 255), border=(70, 110, 150, 255))

    energy_fill_strip(d, 176, 0, 12, 52, (20, 60, 100, 255), cyan)
    d.rectangle([176, 52, 199, 68], fill=(20, 40, 60, 255))
    arrow_fill(d, 176, 52, cyan)
    d.rectangle([176, 70, 189, 83], fill=(40, 30, 20, 255))
    flame_fill(d, 176, 70, (255, 160, 40, 255))
    save(img, "tackle_box")


def main():
    make_auto_fisher()
    make_hydro_reactor()
    make_ocean_filter()
    make_seabed_dredger()
    make_ocean_altar()
    make_tackle_box()
    print("done")


if __name__ == "__main__":
    main()
