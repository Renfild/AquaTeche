"""Pixel art for the catch-multiplier items (rate_x2 .. rate_x64): a pearl in an open scallop shell, 32x32.
Used by px_assets.py (F4 hub sprites) and by `python tools/hub_assets/rate_art.py` (item textures of the aquatech_ui mod)."""
import colorsys
import math
from pathlib import Path

from PIL import Image, ImageDraw

TIERS = (2, 4, 8, 16, 32, 64)
PEARL = {
    2: dict(L="d8ffe0", M="5ff09a", m="20b86a", D="0e6a3e"),
    4: dict(L="d9fbff", M="5ff0ff", m="25b5d6", D="146f90"),
    8: dict(L="f0e0ff", M="c08cff", m="8a4de0", D="4a228a"),
    16: dict(L="fff4b0", M="ffc92e", m="e8920f", D="a8580a"),
    32: dict(L="ffd9e4", M="ff6b93", m="e02a5a", D="8a0e30"),
    64: dict(L="ffffff", M="e8e0ff", m="a89cd8", D="5a4a9a"),
}
VARIANT64 = {
    "prism": PEARL[64],
    "bands": PEARL[64],
    "black": dict(L="d8ccff", M="4a3a8a", m="231648", D="0a0620"),
    "white": dict(L="ffffff", M="fffbe8", m="e8dcb0", D="a89868"),
}
BANDS = ("ff9ac8", "ffe08a", "9affc8", "8ad8ff", "c8a8ff")
SHELL = dict(a="ffe2d2", b="f6b49c", c="d77a6c", e="e98f7e", d="8a3e48", x="3a1424")
SPARK = [(27, 5, 2), (4, 6, 1), (16, 1, 1), (29, 17, 1), (2, 18, 1), (23, 2, 1)]
DIGITS = {
    "1": [".#.", "##.", ".#.", ".#.", "###"],
    "2": ["###", "..#", "###", "#..", "###"],
    "3": ["###", "..#", "###", "..#", "###"],
    "4": ["#.#", "#.#", "###", "..#", "..#"],
    "6": ["###", "#..", "###", "#.#", "###"],
    "8": ["###", "#.#", "###", "#.#", "###"],
}


def h(s, a=255):
    s = s.lstrip("#")
    return (int(s[0:2], 16), int(s[2:4], 16), int(s[4:6], 16), a)


def _wedges(d, cx, cy, a0, a1, step, r_peak, r_valley, col_a, col_b, line):
    angs = list(range(a0, a1 + 1, step))
    for i in range(len(angs) - 1):
        v0, v1, pk = angs[i], angs[i + 1], (angs[i] + angs[i + 1]) / 2
        pts = [(cx, cy)]
        for ang, r in ((v0, r_valley), (pk, r_peak), (v1, r_valley)):
            pts.append((cx + r * math.cos(math.radians(ang)), cy + r * math.sin(math.radians(ang))))
        d.polygon(pts, fill=col_a if i % 2 == 0 else col_b)
    for ang in angs:
        d.line([(cx, cy), (round(cx + r_valley * math.cos(math.radians(ang))), round(cy + r_valley * math.sin(math.radians(ang))))], fill=line)


def _pearl(im, tier, cx, cy, r, variant="prism"):
    d = ImageDraw.Draw(im)
    pal = {k: h(v) for k, v in (VARIANT64[variant] if tier == 64 else PEARL[tier]).items()}
    d.ellipse((cx - r, cy - r, cx + r, cy + r), fill=pal["D"])
    d.ellipse((cx - r, cy - r, cx + r - 1, cy + r - 1), fill=pal["m"])
    d.ellipse((cx - r + 1, cy - r + 1, cx + r - 2, cy + r - 2), fill=pal["M"])
    d.ellipse((cx - r + 1, cy - r + 1, cx + 1, cy + 1), fill=pal["L"])
    if tier == 64 and variant == "bands":
        px = im.load()
        for y in range(cy - r, cy + r + 1):
            for x in range(cx - r, cx + r + 1):
                if (x - cx) ** 2 + (y - cy) ** 2 <= (r - 1) ** 2 and px[x, y][3]:
                    px[x, y] = h(BANDS[min(4, (y - (cy - r)) * 5 // (2 * r + 1))])
    if tier == 64 and variant == "prism":  # prismatic: hue sweeps diagonally
        px = im.load()
        for y in range(cy - r, cy + r + 1):
            for x in range(cx - r, cx + r + 1):
                if (x - cx) ** 2 + (y - cy) ** 2 <= r * r and px[x, y][3]:
                    hue = ((x - cx) + (y - cy) + 2 * r) / (4.0 * r)
                    rr, gg, bb = colorsys.hsv_to_rgb(hue % 1, 0.45, 1.0)
                    px[x, y] = (int(rr * 255), int(gg * 255), int(bb * 255), 255)
    im.putpixel((cx - r // 2 - 1, cy - r // 2 - 1), h("ffffff"))
    im.putpixel((cx - r // 2, cy - r // 2 - 1), h("ffffff"))
    im.putpixel((cx - r // 2 - 1, cy - r // 2), h("ffffff"))
    if tier == 64 and variant == "white":  # star flare on the highlight
        fx, fy = cx - r // 2 - 1, cy - r // 2 - 1
        for q in range(-4, 5):
            for (x, y) in ((fx + q, fy), (fx, fy + q)):
                if 0 <= x < 32 and 0 <= y < 32 and (q != 0):
                    im.putpixel((x, y), h("ffffff") if abs(q) <= 2 else h("fff4b0"))


def _outline(im, col):
    px = im.load()
    w, hh = im.size
    out = Image.new("RGBA", im.size, (0, 0, 0, 0))
    po = out.load()
    for y in range(hh):
        for x in range(w):
            if px[x, y][3] == 0:
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    nx, ny = x + dx, y + dy
                    if 0 <= nx < w and 0 <= ny < hh and px[nx, ny][3]:
                        po[x, y] = col
                        break
    out.alpha_composite(im)
    return out


def _digits(im, text):
    w = len(text) * 4 - 1
    ox, oy = 32 - w - 1, 32 - 5 - 1
    mask = Image.new("L", (32, 32), 0)
    mp = mask.load()
    for i, ch in enumerate(text):
        for y, row in enumerate(DIGITS[ch]):
            for x, c in enumerate(row):
                if c == "#":
                    mp[ox + i * 4 + x, oy + y] = 255
    px = im.load()
    for y in range(32):
        for x in range(32):
            if mp[x, y] == 0:
                near = any(0 <= x + dx < 32 and 0 <= y + dy < 32 and mp[x + dx, y + dy] for dx in (-1, 0, 1) for dy in (-1, 0, 1))
                if near:
                    px[x, y] = h("120a24")
    for y in range(32):
        for x in range(32):
            if mp[x, y]:
                px[x, y] = h("ffffff")


def rate_icon(tier, digits=True, variant=None):
    variant = variant or ("white" if tier == 64 else "prism")
    k = TIERS.index(tier)
    im = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    s = {key: h(v) for key, v in SHELL.items()}
    cx = 16
    # halo behind everything (dithered)
    glow = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    gp = glow.load()
    gpal = VARIANT64[variant] if tier == 64 else PEARL[tier]
    gc = gpal["M"] if not (tier == 64 and variant == "black") else "7a5cff"
    for y in range(32):
        for x in range(32):
            dist = math.hypot(x - cx, y - 16)
            if 11 < dist < 15.5 - (0 if k > 1 else 1) and (x + y) % 2 == 0 and y < 20:
                gp[x, y] = h(gc, 120)
    im.alpha_composite(glow)
    # lid (back) and cup (front)
    _wedges(d, cx, 17, 184, 356, 12, 14, 12, s["c"], s["e"], s["d"])
    _wedges(d, cx, 19, 4, 176, 14, 12.5, 11, s["b"], s["a"], s["c"])
    d.arc((4, 7, 28, 31), 8, 172, fill=s["a"])
    # hinge shadow where lid meets cup
    d.line([(5, 18), (7, 19)], fill=s["d"]); d.line([(26, 18), (24, 19)], fill=s["d"])
    # rim by tier: gold at 32, prismatic at 64
    if tier >= 32:
        for ang in range(186, 356, 6):
            x = round(cx + 14 * math.cos(math.radians(ang))); y = round(17 + 14 * math.sin(math.radians(ang)))
            if tier == 32 or (tier == 64 and variant in ("black", "white")):
                col = h("ffd23a") if (ang // 6) % 2 == 0 else h("fff4b0")
            else:
                rr, gg, bb = colorsys.hsv_to_rgb(((ang - 186) / 170.0), 0.55, 1.0)
                col = (int(rr * 255), int(gg * 255), int(bb * 255), 255)
            if 0 <= x < 32 and 0 <= y < 32:
                im.putpixel((x, y), col)
    _pearl(im, tier, cx, 16, 6, variant)
    # sparkles
    px = im.load()
    for (sx, sy, r) in SPARK[: k + 1]:
        for q in range(-r, r + 1):
            for (x, y) in ((sx + q, sy), (sx, sy + q)):
                if 0 <= x < 32 and 0 <= y < 32 and px[x, y][3] == 0:
                    px[x, y] = h("ffffff") if abs(q) <= r // 2 else h(gpal["L"])
        px[sx, sy] = h("ffffff")
    im = _outline(im, h("1a0e2e"))
    if digits:
        _digits(im, str(tier))
    return im


if __name__ == "__main__":
    root = Path(__file__).resolve().parents[2]
    out = root / "mods/aquatech-ui/src/main/resources/assets/aquatech_ui/textures/item"
    for t in TIERS:
        rate_icon(t).save(out / f"rate_x{t}.png")
        print("wrote rate_x%d.png" % t)
