#!/usr/bin/env python3
"""AquaLumen UI asset generator.

Icons are authored as real SVG stroke geometry (24x24 grid, stroke 2, round caps and
joins) and rasterised by headless Chromium, which is the same renderer that draws the
web prototype. Nothing is drawn with pixel primitives, so the shapes stay true vectors
up to the moment they are baked.

Outputs:
  * src/main/resources/assets/aqualumen/textures/gui/icons.png - white mask atlas, 7x3 cells of 64 px
  * src/main/resources/assets/aqualumen/textures/item/hub_compass.png - item icon, 32 px, transparent
  * src/main/resources/logo.png - mod list logo, 256 px
  * tools/icons.svg - sprite with every symbol, for editing in a vector editor
  * docs/icon_sheet.png - catalogue for the design doc

Requires: Pillow and a Chromium/Chrome binary (set CHROMIUM to override the path).
The icon order is read from `enum Icon` in Icons.java, so the atlas cannot drift.
"""

import math
import os
import re
import shutil
import subprocess
import sys

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BUILD = os.path.join(ROOT, "build", "render")
ICONS_JAVA = os.path.join(
    ROOT, "src/main/java/store/aquateche/aqualumen/client/render/Icons.java")
SPRITE_PATH = os.path.join(ROOT, "tools/icons.svg")
ATLAS_PATH = os.path.join(
    ROOT, "src/main/resources/assets/aqualumen/textures/gui/icons.png")
ITEM_PATH = os.path.join(
    ROOT, "src/main/resources/assets/aqualumen/textures/item/hub_compass.png")
LOGO_PATH = os.path.join(ROOT, "src/main/resources/logo.png")
SHEET_PATH = os.path.join(ROOT, "docs/icon_sheet.png")

CELL = 64
COLUMNS = 7
SUPERSAMPLE = 4
# Headless Chromium paints ~88 CSS px less than the requested window height,
# so every window is padded and the raster is cropped back to the exact size.
CHROME_PAD = 140

CANVAS = "#070C12"
SURFACE = "#0E151E"
RAISED = "#16202C"
ACCENT = "#2FE0C0"
ACCENT_ALT = "#3B9DFF"
GOLD = "#F5C25B"
TEXT = "#F2F7FA"
DIM = "#9DB2C4"
FONT = ("-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', "
        "Arial, sans-serif")


def polar(radius, degrees, cx=12.0, cy=12.0):
    angle = math.radians(degrees)
    return cx + radius * math.cos(angle), cy + radius * math.sin(angle)


def closed_path(points):
    body = " ".join("L%.2f %.2f" % point for point in points[1:])
    return "M%.2f %.2f %s Z" % (points[0][0], points[0][1], body)


def cog_path(teeth=8, outer=9.3, root=6.5, half=12.0, shoulder=7.5):
    """Cog outline: a tooth plateau at `outer`, then a shoulder back down to `root`."""
    points = []
    step = 360.0 / teeth
    for index in range(teeth):
        centre = index * step - 90.0
        points.append(polar(root, centre - half - shoulder))
        points.append(polar(outer, centre - half))
        points.append(polar(outer, centre + half))
        points.append(polar(root, centre + half + shoulder))
    return closed_path(points)


def star_path(outer=9.0, inner=3.9, cy=12.4):
    points = []
    for index in range(10):
        radius = outer if index % 2 == 0 else inner
        points.append(polar(radius, -90 + index * 36, cy=cy))
    return closed_path(points)


# ------------------------------------------------------------------ icon source
# Every entry is plain SVG markup on a 24x24 grid. Stroke, caps and joins are set
# once by the wrapping <g>, so the shapes stay editable and consistent.
ICONS = {
    "player": '<circle cx="12" cy="8" r="3.8"/>'
              '<path d="M4.6 20.4v-.9A5.5 5.5 0 0 1 10.1 14h3.8a5.5 5.5 0 0 1 5.5 5.5v.9"/>',
    "bag": '<path d="M4.8 7.8h14.4l-1.1 11.6a1.6 1.6 0 0 1-1.6 1.4H7.5a1.6 1.6 0 0 1-1.6-1.4L4.8 7.8Z"/>'
           '<path d="M8.8 7.8V6.4a3.2 3.2 0 0 1 6.4 0v1.4"/>',
    "case": '<path d="M12 3.2 20.4 7.7v8.6L12 20.8 3.6 16.3V7.7L12 3.2Z"/>'
            '<path d="m3.6 7.7 8.4 4.5 8.4-4.5"/><path d="M12 12.2v8.6"/>',
    "star": '<path d="%s"/>' % star_path(),
    "chart": '<path d="M6 20v-6.8"/><path d="M12 20V5"/><path d="M18 20v-10.4"/>',
    "gear": '<path d="%s"/><circle cx="12" cy="12" r="3.1"/>' % cog_path(),
    "coin": '<circle cx="12" cy="12" r="8.6"/>'
            '<circle cx="12" cy="12" r="3.4"/>',
    "gem": '<path d="M6.4 3.4h11.2l3 5.6L12 20.6 3.4 9l3-5.6Z"/>'
           '<path d="M3.4 9h17.2"/><path d="m9.2 3.4-2 5.6 4.8 11.6L16.8 9l-2-5.6"/>',
    "key": '<circle cx="8.2" cy="15.8" r="4.6"/><path d="m11.5 12.5 8.9-8.9"/>'
           '<path d="m16.4 7.6 2.4 2.4"/><path d="m18.9 5.1 2.4 2.4"/>',
    "fish": '<path d="M15.4 12c0 2.8-2.9 5-6.5 5S2.4 14.8 2.4 12s2.9-5 6.5-5 6.5 2.2 6.5 5Z"/><path d="m17.4 12 4.2-3.2v6.4L17.4 12Z"/><path d="M6.4 10.8h.01"/>',
    "wave": '<path d="M3 9.6c1.5 0 1.5 2.4 3 2.4s1.5-2.4 3-2.4 1.5 2.4 3 2.4 1.5-2.4 3-2.4 1.5 2.4 3 2.4"/>'
            '<path d="M3 15c1.5 0 1.5 2.4 3 2.4s1.5-2.4 3-2.4 1.5 2.4 3 2.4 1.5-2.4 3-2.4 1.5 2.4 3 2.4"/>',
    "clock": '<circle cx="12" cy="12" r="8.6"/><path d="M12 7.2V12l3.4 2"/>',
    "lock": '<rect x="4.4" y="10.4" width="15.2" height="10.4" rx="2.6"/>'
            '<path d="M8 10.4V7.6a4 4 0 0 1 8 0v2.8"/><path d="M12 14.6v2.4"/>',
    "check": '<path d="m4.6 12.6 5.2 5.2L19.4 6.4"/>',
    "arrow": '<path d="M4 12h15"/><path d="m13.4 6.4 5.6 5.6-5.6 5.6"/>',
    "bell": '<path d="M18 9a6 6 0 1 0-12 0c0 6.2-2.4 7.8-2.4 7.8h16.8S18 15.2 18 9Z"/>'
            '<path d="M13.9 20.4a2.2 2.2 0 0 1-3.8 0"/>',
    "heart": '<path d="M19 13.6c1.5-1.5 3-3.3 3-5.6a5.5 5.5 0 0 0-5.5-5.5c-1.8 0-3 .5-4.5 2-1.5-1.5-2.7-2-4.5-2A5.5 5.5 0 0 0 2 8c0 2.3 1.5 4.1 3 5.6l7 7 7-7Z"/>',
    "bolt": '<path d="M13.4 2.6 4.6 13.8h6.6l-.6 7.6 8.8-11.2h-6.6l.6-7.6Z"/>',
    "shield": '<path d="M12 21.4s7.8-3.6 7.8-9.4V5.6L12 2.6 4.2 5.6V12c0 5.8 7.8 9.4 7.8 9.4Z"/>',
    "close": '<path d="m6.2 6.2 11.6 11.6"/><path d="m17.8 6.2-11.6 11.6"/>',
    "refresh": '<path d="M20.6 12a8.6 8.6 0 1 1-2.5-6.1"/><path d="M20.6 3.6v5.6H15"/>',
    "panel": '<path d="M5.2 4.6h13.6a2.6 2.6 0 0 1 2.6 2.6v9.6a2.6 2.6 0 0 1-2.6 2.6H5.2a2.6 2.6 0 0 1-2.6-2.6V7.2a2.6 2.6 0 0 1 2.6-2.6Z"/><path d="M9.4 4.6v14.8"/>',
    "layers": '<path d="M9.2 3.4h9.4a2.4 2.4 0 0 1 2.4 2.4v9.4"/><path d="M5.4 8.6h9.6a2.6 2.6 0 0 1 2.6 2.6v6.8a2.6 2.6 0 0 1-2.6 2.6H5.4a2.6 2.6 0 0 1-2.6-2.6v-6.8a2.6 2.6 0 0 1 2.6-2.6Z"/>',
}


def enum_order():
    with open(ICONS_JAVA, encoding="utf-8") as handle:
        source = handle.read()
    block = source.split("public enum Icon {", 1)[1].split(";", 1)[0]
    return [name.lower() for name in re.findall(r"\b([A-Z][A-Z_]{2,})\b", block)]


def browser():
    found = os.environ.get("CHROMIUM") or shutil.which("chromium") \
        or shutil.which("chromium-browser") or shutil.which("google-chrome")
    if not found:
        sys.exit("no chromium binary found, set CHROMIUM=/path/to/chromium")
    return found


def shoot(html_path, png_path, width, height, scale=1, background=None):
    command = [
        browser(), "--headless=new", "--disable-gpu", "--no-sandbox",
        "--hide-scrollbars", "--force-device-scale-factor=%d" % scale,
        "--window-size=%d,%d" % (width, height + CHROME_PAD),
        "--screenshot=%s" % png_path, "file://%s" % html_path,
    ]
    if background:
        command.insert(1, "--default-background-color=%s" % background)
    subprocess.run(command, check=True, stdout=subprocess.DEVNULL,
                   stderr=subprocess.DEVNULL)
    shot = Image.open(png_path)
    target = (width * scale, height * scale)
    if shot.size != target:
        shot.crop((0, 0, target[0], target[1])).save(png_path)


def write(path, text):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as handle:
        handle.write(text)
    return path


def sprite(names):
    symbols = "\n".join(
        '  <symbol id="%s" viewBox="0 0 24 24">%s</symbol>' % (name, ICONS[name])
        for name in names)
    return write(SPRITE_PATH,
                 '<svg xmlns="http://www.w3.org/2000/svg" width="0" height="0" '
                 'style="display:none">\n'
                 '  <!-- AquaLumen UI icons. 24x24 grid, stroke 2, round caps and joins. -->\n'
                 '%s\n</svg>\n' % symbols)


def glyph(name, stroke="#FFFFFF", width=2.0, box="-1.4 -1.4 26.8 26.8"):
    return ('<svg viewBox="%s" xmlns="http://www.w3.org/2000/svg">'
            '<g fill="none" stroke="%s" stroke-width="%s" stroke-linecap="round" '
            'stroke-linejoin="round">%s</g></svg>' % (box, stroke, width, ICONS[name]))


def build_atlas(names):
    rows = (len(names) + COLUMNS - 1) // COLUMNS
    cells = "".join('<div class="c">%s</div>' % glyph(name) for name in names)
    html = """<!doctype html><html><head><meta charset="utf-8"><style>
html,body{margin:0;padding:0;background:#000}
.grid{display:grid;grid-template-columns:repeat(%d,%dpx);grid-auto-rows:%dpx}
.c{width:%dpx;height:%dpx}
svg{display:block;width:100%%;height:100%%}
</style></head><body><div class="grid">%s</div></body></html>""" % (
        COLUMNS, CELL, CELL, CELL, CELL, cells)
    path = write(os.path.join(BUILD, "atlas.html"), html)
    raw = os.path.join(BUILD, "atlas_raw.png")
    shoot(path, raw, COLUMNS * CELL, rows * CELL, scale=SUPERSAMPLE)

    raw_image = Image.open(raw)
    print("   atlas raw %s" % (raw_image.size,))
    shot = raw_image.convert("L")
    shot = shot.resize((COLUMNS * CELL, rows * CELL), Image.LANCZOS)
    atlas = Image.new("RGBA", shot.size, (255, 255, 255, 0))
    atlas.putalpha(shot)
    os.makedirs(os.path.dirname(ATLAS_PATH), exist_ok=True)
    atlas.save(ATLAS_PATH)
    return atlas.size, rows


def build_sheet(names):
    rows = (len(names) + COLUMNS - 1) // COLUMNS
    sheet_h = 98 + rows * 111 + (rows - 1) * 12 + 32
    palette = [ACCENT, ACCENT_ALT, GOLD]
    cards = "".join(
        '<figure><div class="art">%s</div><figcaption>%s</figcaption></figure>'
        % (glyph(name, stroke=palette[index % len(palette)]), name)
        for index, name in enumerate(names))
    html = """<!doctype html><html><head><meta charset="utf-8"><style>
html,body{margin:0;background:%s;font-family:%s;-webkit-font-smoothing:antialiased;height:%dpx;overflow:hidden}
body{padding:28px 28px 32px}
h1{margin:0 0 6px;font-size:19px;font-weight:600;color:%s;letter-spacing:.2px}
p.sub{margin:0 0 22px;font-size:13px;color:%s}
.grid{display:grid;grid-template-columns:repeat(7,1fr);gap:12px}
figure{margin:0;background:%s;border:1px solid rgba(255,255,255,.07);border-radius:12px;padding:16px 12px 12px}
.art{height:56px;display:flex;align-items:center;justify-content:center}
.art svg{width:40px;height:40px}
figcaption{margin-top:10px;font-size:12px;color:%s;text-align:center;letter-spacing:.2px}
</style></head><body><h1>AquaLumen UI \u2014 icon set</h1>
<p class="sub">SVG stroke geometry, 24\u00d724 grid, stroke 2, round caps \u2014 tinted by the active theme at draw time</p>
<div class="grid">%s</div></body></html>""" % (CANVAS, FONT, sheet_h, TEXT, DIM, SURFACE, DIM, cards)
    path = write(os.path.join(BUILD, "sheet.html"), html)
    raw = os.path.join(BUILD, "sheet_raw.png")
    shoot(path, raw, 980, sheet_h, scale=2)
    raw_image = Image.open(raw)
    print("   sheet raw %s" % (raw_image.size,))
    sheet = raw_image.convert("RGBA").resize((980, sheet_h), Image.LANCZOS)
    os.makedirs(os.path.dirname(SHEET_PATH), exist_ok=True)
    sheet.save(SHEET_PATH)
    return sheet.size


def unpremultiply(black_png, white_png, size):
    """Recovers colour and alpha from two renders of the same art on black and white."""
    black = Image.open(black_png).convert("RGB")
    white = Image.open(white_png).convert("RGB")
    out = Image.new("RGBA", black.size, (0, 0, 0, 0))
    bpx, wpx, opx = black.load(), white.load(), out.load()
    for y in range(black.size[1]):
        for x in range(black.size[0]):
            br, bg, bb = bpx[x, y]
            wr, wg, wb = wpx[x, y]
            alpha = 255 - max(wr - br, wg - bg, wb - bb)
            if alpha <= 0:
                continue
            scale = 255.0 / alpha
            opx[x, y] = (min(255, int(br * scale)), min(255, int(bg * scale)),
                         min(255, int(bb * scale)), alpha)
    return out.resize(size, Image.LANCZOS)


def build_item():
    art = """<svg viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg">
<defs><linearGradient id="g" x1="0" y1="0" x2="1" y2="1">
<stop offset="0" stop-color="%s"/><stop offset="1" stop-color="%s"/></linearGradient></defs>
<circle cx="32" cy="32" r="27" fill="%s" stroke="url(#g)" stroke-width="3.4"/>
<circle cx="32" cy="32" r="19" fill="none" stroke="%s" stroke-width="1.4" opacity=".3"/>
<path d="M44.5 19.5 34.6 29.4 32 32Z" fill="none"/>
<path d="M32 32 45 19l-4.4 15.6L32 32Z" fill="%s"/>
<path d="M32 32 19 45l4.4-15.6L32 32Z" fill="%s" opacity=".92"/>
<circle cx="32" cy="32" r="2.4" fill="%s"/>
</svg>""" % (ACCENT, ACCENT_ALT, SURFACE, ACCENT_ALT, GOLD, TEXT, CANVAS)
    shell = """<!doctype html><html><head><meta charset="utf-8"><style>
html,body{margin:0;padding:0;background:%s}svg{display:block;width:256px;height:256px}
</style></head><body>%s</body></html>"""
    black = write(os.path.join(BUILD, "item_black.html"), shell % ("#000", art))
    white = write(os.path.join(BUILD, "item_white.html"), shell % ("#fff", art))
    black_png = os.path.join(BUILD, "item_black.png")
    white_png = os.path.join(BUILD, "item_white.png")
    shoot(black, black_png, 256, 256)
    shoot(white, white_png, 256, 256)
    item = unpremultiply(black_png, white_png, (32, 32))
    os.makedirs(os.path.dirname(ITEM_PATH), exist_ok=True)
    item.save(ITEM_PATH)
    return item.size


def build_logo():
    html = """<!doctype html><html><head><meta charset="utf-8"><style>
html,body{margin:0;padding:0;background:%s;font-family:%s;-webkit-font-smoothing:antialiased}
.plate{width:512px;height:512px;box-sizing:border-box;padding:28px;background:%s}
.inner{height:100%%;border-radius:64px;background:linear-gradient(140deg,%s 0%%,%s 55%%,#101A24 100%%);
border:2px solid rgba(255,255,255,.08);display:flex;flex-direction:column;align-items:center;justify-content:center;gap:14px}
.mark{width:200px;height:200px}
.name{font-size:44px;font-weight:700;letter-spacing:4px;color:%s}
.tag{font-size:22px;letter-spacing:6px;color:%s}
</style></head><body><div class="plate"><div class="inner">
<div class="mark">%s</div><div class="name">AQUALUMEN</div><div class="tag">UI 1.20.1</div>
</div></div></body></html>""" % (CANVAS, FONT, CANVAS, SURFACE, RAISED, TEXT, ACCENT,
                                  glyph("wave", stroke=ACCENT, width=1.9))
    path = write(os.path.join(BUILD, "logo.html"), html)
    raw = os.path.join(BUILD, "logo_raw.png")
    shoot(path, raw, 512, 512)
    logo = Image.open(raw).convert("RGBA").resize((256, 256), Image.LANCZOS)
    logo.save(LOGO_PATH)
    return logo.size


def main():
    names = enum_order()
    missing = [name for name in names if name not in ICONS]
    extra = [name for name in ICONS if name not in names]
    if missing or extra:
        sys.exit("icon mismatch, missing=%s extra=%s" % (missing, extra))

    os.makedirs(BUILD, exist_ok=True)
    print("sprite  %s" % sprite(names))
    size, rows = build_atlas(names)
    print("atlas   %s %s (%d columns x %d rows)" % (ATLAS_PATH, size, COLUMNS, rows))
    print("item    %s %s" % (ITEM_PATH, build_item()))
    print("logo    %s %s" % (LOGO_PATH, build_logo()))
    print("sheet   %s %s" % (SHEET_PATH, build_sheet(names)))


if __name__ == "__main__":
    main()
