"""Generate the F4 hub pixel theme variables (9-slice borders, sprites, nav icons as CSS data URIs) -> px_vars.css.
Run: python tools/hub_assets/px_assets.py"""
import base64
import io

from PIL import Image, ImageDraw

from pathlib import Path
OUT = str(Path(__file__).resolve().parent) + "/"


def h(s, a=255):
    s = s.lstrip("#")
    return (int(s[0:2], 16), int(s[2:4], 16), int(s[4:6], 16), a)


def uri(im):
    b = io.BytesIO()
    im.save(b, "PNG")
    return "url(data:image/png;base64," + base64.b64encode(b.getvalue()).decode() + ")"


def nine(fill, hi, lo, outline, size=12):
    im = Image.new("RGBA", (size, size), h(fill))
    px = im.load()
    n = size - 1
    for i in range(size):
        px[i, 0] = px[i, n] = px[0, i] = px[n, i] = h(outline)
    for i in range(1, n):
        px[i, 1] = h(hi)
        px[1, i] = h(hi)
        px[i, n - 1] = h(lo)
        px[n - 1, i] = h(lo)
    px[1, n - 1] = h(lo)
    px[n - 1, 1] = h(lo)
    for (x, y) in ((0, 0), (n, 0), (0, n), (n, n)):
        px[x, y] = (0, 0, 0, 0)
    for (x, y) in ((1, 1),):
        px[x, y] = h(outline)
    px[n - 1, 1] = h(outline); px[1, n - 1] = h(outline); px[n - 1, n - 1] = h(outline)
    return im


def sprite(rows, pal):
    hgt = len(rows)
    wid = max(len(r) for r in rows)
    im = Image.new("RGBA", (wid, hgt), (0, 0, 0, 0))
    px = im.load()
    for y, r in enumerate(rows):
        for x, ch in enumerate(r):
            if ch in pal:
                px[x, y] = h(pal[ch])
    return im


def icon16(rows, main="e6f3ff", acc="ffd23a"):
    rows = list(rows)
    pad_top = (16 - len(rows)) // 2
    wmax = max(len(r) for r in rows)
    lpad = "." * ((16 - wmax) // 2)
    rows = ["." * 16] * pad_top + [(lpad + r).ljust(16, ".")[:16] for r in rows]
    rows += ["." * 16] * (16 - len(rows))
    base = sprite(rows, {"#": main, "+": acc})
    shadow = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    sp = shadow.load()
    bp = base.load()
    for y in range(15):
        for x in range(15):
            if bp[x, y][3] and not bp[x + 1, y + 1][3]:
                sp[x + 1, y + 1] = h("0a0620")
    out = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    out.alpha_composite(shadow)
    out.alpha_composite(base)
    return out


ICONS = {
    "profile": ["......####......", ".....######.....", ".....######.....", ".....######.....", "......####......", "................", "....########....", "...##########...", "...##########...", "...##########..."],
    "store": [".....######.....", ".....#....#.....", "....########....", "...##########...", "...##########...", "...##########...", "...##########...", "...##########...", "...##########..."],
    "cases": ["...##########...", "..############..", "..############..", "..############..", "..####++++####..", "..####++++####..", "..############..", "..############..", "..############.."],
    "pass": [".......##.......", ".......##.......", "......####......", "..############..", "...##########...", "....########....", ".....######.....", "....###..###....", "...###....###...", "...##......##..."],
    "fishing": ["................", "......####......", "....########..#.", "..##########.###", ".###+#########.#", "..##########.###", "....########..#.", "......####......"],
    "events": [".......##.......", "......####......", ".....######.....", ".....######.....", "....########....", "....########....", "...##########...", "...##########...", "..############..", "................", ".......++......."],
    "auction": ["....#########...", "...###########..", "...###########..", "....###.#####...", ".........###....", "........###.....", ".......###......", "......###.......", ".....###........"],
    "kits": ["....##....##....", "...#..####..#...", "..############..", "..############..", "..####++++####..", "..####++++####..", "..####++++####..", "..############..", "..############.."],
    "warps": [".....######.....", "...##########...", "..####....####..", ".####..++..####.", ".###..++++..###.", ".###...++...###.", ".####..++..####.", "..####....####..", "...##########...", ".....######....."],
    "tops": ["..############..", ".##############.", ".##.########.##.", ".##..######..##.", "..##.######.##..", "....#######.....", "......####......", "......####......", ".....######.....", "....########...."],
    "settings": [".......##.......", "..##...##...##..", "..###.####.###..", "...##########...", "....###..###....", ".#####....#####.", ".#####....#####.", "....###..###....", "...##########...", "..###.####.###..", "..##...##...##..", ".......##......."],
}
ICON_NAMES = list(ICONS)

css = [":root{"]
nine_defs = {
    "panel": ("120d2c", "3d2f8c", "0a0620", "05030f"),
    "inset": ("0e0a22", "241a5a", "05030f", "05030f"),
    "chip": ("17122e", "4a3d9a", "0a0620", "05030f"),
    "gray": ("5b6080", "c9cbe0", "2a2c40", "0a0a18"),
    "blue": ("2f7bff", "9fd0ff", "123a8a", "0a1a4a"),
    "purple": ("7b4cff", "cdb8ff", "2a1a80", "120a40"),
    "gold": ("ffc22e", "fff6c0", "a8680c", "3a1c00"),
    "red": ("d6212d", "ff8a8a", "7a0a12", "2a0408"),
    "lime": ("8dff3a", "eaffb0", "3a8a10", "0e3a05"),
    "done": ("2a8a38", "9dff9a", "0e3a15", "04200a"),
    "ready": ("6ed32a", "f2ffc8", "2a6a0a", "0a3a05"),
    "next": ("4a4a72", "cfd0ff", "1c1c34", "0a0a1a"),
    "lock": ("2b2a47", "5a56a0", "14122a", "0a0a18"),
    "pm_pink": ("3a1230", "ff3da8", "1a0618", "0a0210"),
    "pm_violet": ("27103f", "9a5cff", "120a24", "05030f"),
    "pm_lime": ("12301a", "9dff3a", "081a0c", "030a04"),
    "pm_orange": ("3a2010", "ff8a1f", "1a0e04", "0a0502"),
    "pm_gold": ("3a2e10", "ffd23a", "1a1404", "0a0802"),
    "shop_pink": ("a8245f", "ff7ac0", "4a0f30", "1a0410"),
    "shop_orange": ("d9701a", "ffc27a", "6a2d08", "2a1004"),
    "shop_lime": ("4aa81c", "b8ff80", "1d5a0a", "0a2a04"),
    "shop_cyan": ("1f9bb0", "8ff2ff", "0b3d4c", "041a22"),
}
for k, v in nine_defs.items():
    css.append(f"--n-{k}:{uri(nine(*v))};")

# sprites
chk = sprite(["..........ww", ".........www", "........www.", "w......www..", "ww....www...", ".ww..www....", "..wwwww.....", "...www......", "....w......."], {"w": "eaffd2"})
chk_s = sprite(["..........ww", ".........www", "........www.", "w......www..", "ww....www...", ".ww..www....", "..wwwww.....", "...www......", "....w......."], {"w": "0e5a1a"})
cs = Image.new("RGBA", (14, 11), (0, 0, 0, 0)); cs.alpha_composite(chk_s, (1, 1)); cs.alpha_composite(chk, (0, 0))
css.append(f"--s-check:{uri(cs)};")
lock = sprite(["...xxxx...", "..xx..xx..", "..x....x..", "..x....x..", ".xxxxxxxx.", ".xYYYYYYx.", ".xYYddYYx.", ".xYYddYYx.", ".xYYYddYx.", ".xYYYYYYx.", ".xxxxxxxx."], {"x": "10142a", "Y": "c9d3ea", "d": "10142a"})
css.append(f"--s-lock:{uri(lock)};")
for nm, rows in (("l", ["....x", "...xx", "..xxx", ".xxxx", "xxxxx", ".xxxx", "..xxx", "...xx", "....x"]), ("r", ["x....", "xx...", "xxx..", "xxxx.", "xxxxx", "xxxx.", "xxx..", "xx...", "x...."])):
    css.append(f"--s-arr-{nm}:{uri(sprite(rows, {'x': 'ffffff'}))};")


def diamond(fill, edge, glow=None):
    rows = ["......xx......", ".....xwwx.....", "....xwwwwx....", "...xwwwwwwx...", "..xwwwwwwwwx..", ".xwwwwwwwwwwx.", "xwwwwwwwwwwwwx", "xwwwwwwwwwwwwx", ".xwwwwwwwwwwx.", "..xwwwwwwwwx..", "...xwwwwwwx...", "....xwwwwx....", ".....xwwx.....", "......xx......"]
    im = sprite(rows, {"x": edge, "w": fill})
    px = im.load()
    for x, y in ((6, 2), (5, 3), (4, 4), (7, 2)):
        px[x, y] = h("ffffff", 160)
    return im


css.append(f"--s-dia-past:{uri(diamond('2fb83e', '0a3a10'))};")
css.append(f"--s-dia-now:{uri(diamond('35e8ff', '04323a'))};")
css.append(f"--s-dia-lock:{uri(diamond('6a6a98', '14142a'))};")
tick = sprite(["....................xx....", "..xxxxxxxxxxxxxxxxxxxxxxxx", ".xYYYYYYYYYYYYYYYYYYYYYYYYx", ".xYYYYYYYYYYYYYYYYYYYYYYYYx", "xYYYYYYYYYYYYYYYYYYYYYYYYYYx", "xYYYYYYYYYYYYYYYYYYYYYYYYYYx"], {"x": "1a0a00", "Y": "ffd23a"})
t = Image.new("RGBA", (28, 18), (0, 0, 0, 0))
td = t.load()
for y in range(2, 16):
    for x in range(1, 27):
        td[x, y] = h("ffd23a")
for x in range(1, 27):
    for y in (2, 3):
        td[x, y] = h("fff6c0")
    for y in (13, 14, 15):
        td[x, y] = h("e5a21a")
for x in range(0, 28):
    td[x, 1] = h("5a3a00") if 1 <= x <= 26 else td[x, 1]
    td[x, 16] = h("5a3a00") if 1 <= x <= 26 else td[x, 16]
for y in range(2, 16):
    td[0, y] = h("5a3a00"); td[27, y] = h("5a3a00")
for y in (8, 9):
    td[0, y] = (0, 0, 0, 0); td[27, y] = (0, 0, 0, 0)
star = [(14, 4), (13, 5), (14, 5), (15, 5), (10, 6), (11, 6), (12, 6), (13, 6), (14, 6), (15, 6), (16, 6), (17, 6), (18, 6), (11, 7), (12, 7), (13, 7), (14, 7), (15, 7), (16, 7), (17, 7), (12, 8), (13, 8), (14, 8), (15, 8), (16, 8), (12, 9), (13, 9), (15, 9), (16, 9), (11, 10), (12, 10), (16, 10), (17, 10)]
for p in star:
    td[p] = h("fffbe0")
css.append(f"--s-ticket:{uri(t)};")
gem = sprite(["...xxxx...", "..xwwwwx..", ".xwwwwwwx.", "xwwwwwwwwx", ".xwwwwwwx.", "..xwwwwx..", "...xwwx...", "....xx...."], {"x": "04200a", "w": "5df08a"})
css.append(f"--s-gem:{uri(gem)};")
import menu_icons
for name in menu_icons.NAMES:
    css.append(f"--i-{name}:{uri(menu_icons.icon(name))};")
for name, rows in ICONS.items():
    if name in menu_icons.NAMES:
        continue
    css.append(f"--i-{name}:{uri(icon16(rows))};")
close_rows = ["##......##", ".##....##.", "..##..##..", "...####...", "....##....", "....##....", "...####...", "..##..##..", ".##....##.", "##......##"]
css.append(f"--i-close:{uri(icon16(close_rows))};")
refresh_rows = ["..######.....", ".#......#....", "#........#...", "#.......###..", "#........#...", ".#......#....", "..######.....", ".............", "....######...", "...#......#..", "..###.......#", "...#.......#.", "....#.....#..", ".....######.."]
css.append(f"--i-refresh:{uri(menu_icons.icon('refresh'))};")

# rarity frames for case cards (dark fill, coloured edge)
def mix(c, k):
    c = c.lstrip("#")
    return "%02x%02x%02x" % tuple(int(int(c[i:i + 2], 16) * k) for i in (0, 2, 4))


RARITY = {"common": "9db2c4", "uncommon": "4cd08a", "rare": "3b9dff", "epic": "b072ff", "legendary": "f5c25b", "mythic": "ff4f79", "exotic": "ffe066"}
for name, col in RARITY.items():
    css.append(f"--n-r-{name}:{uri(nine(mix(col, .16), col, mix(col, .45), '05030f'))};")

# rank glyphs (coloured), a few UI icons
RANK_ART = {
    "sailor": ("2fe0c0", ["......####......", "......#..#......", "......####......", ".......##.......", "....########....", ".......##.......", ".......##.......", "#......##......#", "##.....##.....##", ".##....##....##.", "..###..##..###..", "....########....", "......####......"]),
    "skipper": ("3b9dff", [".......##.......", "..#....##....#..", "...#.######.#...", "....##....##....", "...#..####..#...", "##.#.##..##.#.##", "##.#.##..##.#.##", "...#..####..#...", "....##....##....", "...#.######.#...", "..#....##....#..", ".......##......."]),
    "captain": ("f5c25b", ["....########....", "...##########...", "..############..", "..############..", "..####++++####..", "..############..", "################", "################", ".##############.", "..############.."]),
    "admiral": ("ff8c42", [".......##.......", ".......##.......", "......####......", "......####......", "################", ".##############.", "..############..", "...##########...", "...##########...", "..####....####..", "..###......###..", ".###........###."]),
    "legend": ("c264ff", ["#......##......#", "##....####....##", "###..######..###", "################", "################", "#+############+#", "################", "################"]),
    "vip": ("ff6b6b", ["....########....", "...##++++++##...", "..############..", ".##############.", "################", ".##############.", "..############..", "...##########...", "....########....", ".....######.....", "......####......", ".......##......."]),
}
UI_ICONS = {
    "search": ["....#####.......", "...#.....#......", "..#.......#.....", "..#.......#.....", "..#.......#.....", "...#.....#......", "....#####.#.....", ".........###....", "..........###...", "...........##..."],
    "copy": ["....########....", "....#......#....", "..########.#....", "..#......#.#....", "..#......###....", "..#......#......", "..#......#......", "..########......"],
    "tick": ["..............##", ".............##.", "............##..", "##.........##...", "###.......##....", ".###.....##.....", "..###...##......", "...#####........", "....###........."],
}
for name, rows in UI_ICONS.items():
    css.append(f"--i-{name}:{uri(icon16(rows))};")

# reward sprites for the pass (built from the gold hook coin)
coin = Image.open(Path(OUT).resolve().parents[1] / "docs/assets/images/coin_pixel.png").convert("RGBA")
c1 = Image.new("RGBA", (32, 32), (0, 0, 0, 0)); c1.alpha_composite(coin, (0, 0))
c3 = Image.new("RGBA", (48, 44), (0, 0, 0, 0))
c3.alpha_composite(coin, (8, 0)); c3.alpha_composite(coin, (0, 12)); c3.alpha_composite(coin, (16, 12))
c3 = c3.crop((0, 0, 48, 44)).resize((48, 44))
css.append(f"--s-coin-1:{uri(c1)};")
css.append(f"--s-coin-3:{uri(c3)};")
import coin_loot
for nm in ("bag", "chest"):
    css.append(f"--s-{nm}:{uri(coin_loot.sprite(nm))};")
# volumetric buttons: hue-shifted ramp, light top edge, thick darker bottom band (pixel-art shading)
import colorsys


def _hsv(hh, s, v):
    r, g, b = colorsys.hsv_to_rgb(hh % 1.0, max(0, min(1, s)), max(0, min(1, v)))
    return (int(r * 255), int(g * 255), int(b * 255), 255)


def ramp(base):
    r, g, b, _ = h(base)
    hh, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
    return dict(
        hi=_hsv(hh - 0.03, s * 0.45, v + 0.30), light=_hsv(hh - 0.015, s * 0.75, v + 0.12), base=h(base),
        shade=_hsv(hh + 0.03, s * 1.1, v * 0.66), deep=_hsv(hh + 0.05, s * 1.1, v * 0.46), out=_hsv(hh + 0.07, s, v * 0.26))


def button9(base, pressed=False, W=16, H=16, thin=False):
    c = ramp(base)
    im = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    px = im.load()
    for y in range(H):
        for x in range(W):
            px[x, y] = c["base"] if not pressed else c["shade"]
    for x in range(W):
        px[x, 0] = px[x, H - 1] = c["out"]
    for y in range(H):
        px[0, y] = px[W - 1, y] = c["out"]
    if not pressed:
        for x in range(2, W - 2):
            px[x, 1] = c["hi"]
        for x in range(1, W - 1):
            px[x, 2] = c["light"]
        for y in range(2, H - 5):
            px[1, y] = c["light"]
            px[W - 2, y] = c["shade"]
        for x in range(1, W - 1):
            px[x, H - 4] = px[x, H - 3] = c["shade"]
            if not thin:
                px[x, H - 5] = c["shade"]
            px[x, H - 2] = c["deep"]
    else:
        for x in range(1, W - 1):
            px[x, 1] = px[x, 2] = c["deep"]
            px[x, H - 2] = c["light"]
            px[x, H - 3] = c["base"]
        for y in range(1, H - 2):
            px[1, y] = c["deep"]
    for (x, y) in ((0, 0), (W - 1, 0), (0, H - 1), (W - 1, H - 1)):
        px[x, y] = (0, 0, 0, 0)
    px[1, 1] = c["out"]; px[W - 2, 1] = c["out"]; px[1, H - 2] = c["out"]; px[W - 2, H - 2] = c["out"]
    return im


BUTTONS = {"gray": "6a709c", "blue": "2f7bff", "green": "3fae2a", "lime": "86e02a", "red": "d6212d", "gold": "ffb62e",
           "orange": "ff7a1f", "purple": "7b4cff", "lock": "34335a", "next": "5a5a90"}
for _n in ("gray", "blue"):
    css.append(f"--bn-{_n}:{uri(button9(BUTTONS[_n], H=15, thin=True))};")
    css.append(f"--bnp-{_n}:{uri(button9(BUTTONS[_n], True, H=15, thin=True))};")
for _n, _c in BUTTONS.items():
    css.append(f"--b-{_n}:{uri(button9(_c))};")
    css.append(f"--bp-{_n}:{uri(button9(_c, True))};")

# booster sprites: the gold coin with a green (small) or cyan double (large) arrow
def _arrow(im, col, double=False):
    d = ImageDraw.Draw(im)
    out = h("0a2a10")
    for k in range(2 if double else 1):
        oy = 15 - k * 6
        d.polygon([(21, oy), (28, oy + 7), (24, oy + 7), (24, oy + 13), (18, oy + 13), (18, oy + 7), (14, oy + 7)], fill=h(col), outline=out)
        d.line([(21, oy + 3), (21, oy + 8)], fill=h("ffffff"))
    return im


_coin32 = Image.open(Path(OUT).resolve().parents[1] / "docs/assets/images/coin_pixel.png").convert("RGBA")
for _name, _col, _dbl in (("booster", "5cff7a", False), ("booster-big", "5ff0ff", True)):
    _im = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    _im.alpha_composite(_coin32, (-3, -2))
    css.append(f"--s-{_name}:{uri(_arrow(_im, _col, _dbl))};")
import store_icons
css.append(f"--s-gem:{uri(store_icons.icon('gems'))};")
css.append(f"--s-ticket:{uri(store_icons.icon('ticket'))};")
import rate_art
for _t in rate_art.TIERS:
    css.append(f"--s-rate-{_t}:{uri(rate_art.rate_icon(_t, digits=False))};")
css.append("}")
open(OUT + "px_vars.css", "w", encoding="utf-8").write("\n".join(css))
print("vars ok", len("\n".join(css)))
