"""Предпросмотр GUI механизмов с имитацией динамики (порт MachineGuiFx на PIL): шкалы, стрелка, вода, пруд, пламя.

    python tools/preview_machine_guis.py        # пишет mods/aquatech-machines/art/gui_live_preview.png

Нужен, чтобы увидеть итоговый вид без запуска игры. Числа берутся из tools/build_machine_guis.py.
"""
import math
import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

sys.path.insert(0, str(Path(__file__).resolve().parent))
import build_machine_guis as B  # noqa: E402

OUT = B.MOD / "art" / "gui_live_preview.png"
T = 37.0  # момент анимации


def lerp(a, b, t):
    return tuple(int(a[i] * (1 - t) + b[i] * t) for i in range(3)) + (255,)


def bar(d, x, y, w, h, frac, main, light, dark):
    filled = round((w - 2) * frac)
    if filled <= 0:
        return
    left, top, bottom = x + 1, y + 1, y + h - 1
    d.rectangle([left, top, left + filled - 1, bottom - 1], fill=main)
    d.rectangle([left, top, left + filled - 1, top], fill=light)
    d.rectangle([left, bottom - 1, left + filled - 1, bottom - 1], fill=dark)
    spark = int((T * 1.6) % (filled + 14)) - 10
    a, b = max(0, spark), min(filled, spark + 6)
    if b > a:
        d.rectangle([left + a, top, left + b - 1, top], fill=(255, 255, 255, 255))


def tank(d, x, y, w, h, frac, main, light, dark):
    level = round(h * frac)
    bottom = y + h
    surface = bottom - level
    for col in range(w):
        wave = round(math.sin(T * 0.16 + col * 0.9))
        top = max(y, min(bottom - 1, surface + wave))
        color = light if col < 2 else (dark if col >= w - 2 else main)
        d.rectangle([x + col, top, x + col, bottom - 1], fill=color)
        d.point((x + col, top), fill=light)


def pond(im, d, x, y, w, h, frac, working):
    water_top = y + round(h * 0.42)
    for row in range(water_top, y + h):
        depth = (row - water_top) / max(1, y + h - water_top)
        d.line([(x, row), (x + w - 1, row)], fill=lerp((0x2F, 0x8F, 0xBF), (0x0E, 0x47, 0x66), depth))
    for col in range(w):
        wave = round(math.sin(T * 0.12 + col * 0.22) * 1.4)
        d.point((x + col, water_top + wave), fill=(0xCF, 0xF3, 0xFF, 255))
    bx = x + w // 2 + 10
    bite = working and frac > 0.88
    bob = 3 if bite else round(math.sin(T * 0.15))
    by = water_top - 3 + bob
    d.line([(bx, y), (bx, by - 1)], fill=(0xE8, 0xF4, 0xFF, 255))
    d.rectangle([bx - 1, by, bx + 1, by + 2], fill=(255, 77, 77, 255))
    d.point((bx, by), fill=(255, 255, 255, 255))
    if working:
        swim = int((T * 0.55) % (w + 24)) - 12
        fx, fy = x + swim, water_top + 14 + round(math.sin(T * 0.1) * 3)
        if fx >= x and fx + 10 <= x + w:
            sprite = im.crop((80, B.ATLAS_Y, 90, B.ATLAS_Y + 6))
            im.alpha_composite(sprite, (fx, fy))


def flames(im, d, x, y, w, h):
    d.rectangle([x, y, x + w - 1, y + h - 1], fill=(255, 138, 42, 34))
    gap = (w - 72) // 4
    for flame in range(3):
        frame = (int(T / 3) + flame) % 4
        sprite = im.crop((30 + 12 * frame, B.ATLAS_Y, 42 + 12 * frame, B.ATLAS_Y + 16)).resize((24, 32), Image.NEAREST)
        im.alpha_composite(sprite, (x + gap + flame * (24 + gap), y + h - 5 - 32))


def crystal(im, d, x, y, size):
    d.rectangle([x, y, x + size - 1, y + size - 1], fill=(176, 114, 255, 34))
    frame = int(T / 6) % 3
    sprite = im.crop((24 * frame, B.ATLAS_Y + 18, 24 * frame + 24, B.ATLAS_Y + 42)).resize((size, size), Image.NEAREST)
    im.alpha_composite(sprite, (x, y))


def render(key, spec):
    im = B.build_machine(key, spec)
    d = ImageDraw.Draw(im)
    wd = spec["widgets"]
    if "pond" in wd:
        pond(im, d, *wd["pond"], 0.6, True)
    if "flames" in wd:
        flames(im, d, *wd["flames"])
    if "crystal" in wd:
        crystal(im, d, *wd["crystal"][:2], wd["crystal"][2])
    for name, rect in wd.items():
        if name.startswith("tank"):
            colors = {"tank_raw": ((21, 101, 192), (66, 165, 245), (13, 71, 161)),
                      "tank_dist": ((0, 229, 255), (224, 247, 250), (0, 176, 255)),
                      "tank_lava": ((255, 87, 34), (255, 204, 128), (216, 67, 21))}[name]
            tank(d, *rect, 0.65, *[c + (255,) for c in colors])
    if "arrow" in wd:
        ax, ay = wd["arrow"][:2]
        sprite = im.crop((0, B.ATLAS_Y, 14, B.ATLAS_Y + 17))
        im.alpha_composite(sprite, (ax, ay))
    ex, ey, ew, eh = B.ENERGY
    bar(d, ex, ey, ew, eh, 0.7, (255, 194, 31, 255), (255, 233, 160, 255), (201, 138, 10, 255))
    mana = key in ("flower_collector", "mana_fabricator")
    gen = key == "fish_generator"
    colors2 = ((176, 114, 255, 255), (224, 200, 255, 255), (122, 63, 208, 255)) if mana else \
        (((255, 106, 26, 255), (255, 210, 74, 255), (184, 58, 8, 255)) if gen else ((255, 138, 42, 255), (255, 198, 138, 255), (194, 90, 16, 255)))
    bar(d, B.BAR2[0], B.BAR2[1], B.BAR2[2], B.BAR2[3], 0.45, *colors2)
    return im.crop((0, 0, B.W, B.H))


def main():
    scale = 3
    names = list(B.MACHINES.items())
    cols = 2
    rows = (len(names) + cols - 1) // cols
    font = ImageFont.truetype(r"C:\Windows\Fonts\arialbd.ttf", 16)
    cw, ch = B.W * scale, B.H * scale
    sheet = Image.new("RGBA", (cols * (cw + 20) + 20, rows * (ch + 20) + 20), (44, 48, 60, 255))
    sd = ImageDraw.Draw(sheet)
    for i, (key, spec) in enumerate(names):
        im = render(key, spec).resize((cw, ch), Image.NEAREST)
        ox, oy = 20 + (i % cols) * (cw + 20), 20 + (i // cols) * (ch + 20)
        sheet.paste(im, (ox, oy), im)
        title = spec["title"].upper()
        tw = sd.textlength(title, font=font)
        sd.text((ox + (127 * scale) - tw / 2, oy + 4 * scale), title, fill=(58, 36, 16, 255), font=font)
    OUT.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(OUT)
    print("preview ->", OUT)


if __name__ == "__main__":
    main()
