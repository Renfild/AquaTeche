"""64x64 pixel sprites for the big coin rewards of the season pass: a canvas coin sack and an open treasure chest
overflowing with coins and gems. Drawn natively at 64x64 (shown 1:1), rim light + dark outline added on top."""
import math
from pathlib import Path

from PIL import Image, ImageDraw

from menu_icons import OUT, h, ramp, _light, _dark

N = 64
ROOT = Path(__file__).resolve().parents[2]


def _lerp(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3)) + (255,)


def ramp7(base):
    r = ramp(base)
    r["mlight"] = _lerp(r["light"], r["base"], .5)
    r["mshade"] = _lerp(r["base"], r["shade"], .5)
    return r


def _new():
    im = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    return im, ImageDraw.Draw(im)


def _edges(im):
    px = im.load()
    src = im.copy().load()
    for y in range(N):
        for x in range(N):
            c = src[x, y]
            if c[3] == 0 or sum(c[:3]) < 150:
                continue

            def clear(dx, dy):
                nx, ny = x + dx, y + dy
                return not (0 <= nx < N and 0 <= ny < N) or src[nx, ny][3] == 0

            if clear(0, 1) or clear(1, 0):
                px[x, y] = _dark(c)
            elif clear(0, -1) or clear(-1, 0):
                px[x, y] = _light(c)


def _outline(im):
    px = im.load()
    out = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    po = out.load()
    for y in range(N):
        for x in range(N):
            if px[x, y][3] == 0:
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    nx, ny = x + dx, y + dy
                    if 0 <= nx < N and 0 <= ny < N and px[nx, ny][3]:
                        po[x, y] = OUT
                        break
    out.alpha_composite(im)
    return out


def _tone(r, t):
    for lim, key in ((.10, "light"), (.24, "mlight"), (.40, "mlight"), (.60, "base"), (.78, "mshade"), (.92, "shade")):
        if t < lim:
            return r[key]
    return r["deep"]


def _shade_cols(im, box, r, only=None):
    x0, y0, x1, y1 = box
    px = im.load()
    for x in range(x0, x1 + 1):
        t = (x - x0) / max(1, x1 - x0)
        for y in range(y0, y1 + 1):
            if px[x, y][3] and (only is None or only(x, y)):
                px[x, y] = _tone(r, t)


def _coin(d, im, cx, cy, rad, g):
    d.ellipse((cx - rad, cy - rad, cx + rad, cy + rad), fill=g["deep"])
    d.ellipse((cx - rad, cy - rad, cx + rad - 1, cy + rad - 1), fill=g["shade"])
    d.ellipse((cx - rad + 1, cy - rad + 1, cx + rad - 2, cy + rad - 2), fill=g["base"])
    d.ellipse((cx - rad + 2, cy - rad + 2, cx + rad - 4, cy + rad - 4), fill=g["light"])
    if rad >= 5:
        d.ellipse((cx - rad + 3, cy - rad + 3, cx + rad - 3, cy + rad - 3), outline=g["shade"])
        d.line([(cx, cy - rad + 4), (cx, cy + rad - 4)], fill=g["deep"])
    im.putpixel((cx - rad // 2, cy - rad // 2), h("ffffff"))
    im.putpixel((cx - rad // 2 + 1, cy - rad // 2), h("fff4c0"))


def _gem(d, cx, cy, s, r):
    d.polygon([(cx - s, cy), (cx - s // 2, cy - s), (cx + s // 2, cy - s), (cx + s, cy), (cx, cy + s)], fill=r["base"])
    d.polygon([(cx - s, cy), (cx - s // 2, cy - s), (cx, cy - s), (cx, cy)], fill=r["light"])
    d.polygon([(cx, cy), (cx + s, cy), (cx, cy + s)], fill=r["shade"])
    d.line([(cx - s // 2, cy - s), (cx + s // 2, cy - s)], fill=h("ffffff"))


def _spark(im, x, y, r=3):
    for q in range(-r, r + 1):
        for (a, b) in ((x + q, y), (x, y + q)):
            if 0 <= a < N and 0 <= b < N and im.getpixel((a, b))[3] == 0:
                im.putpixel((a, b), h("ffffff") if abs(q) <= r // 2 else h("fff0b0"))
    im.putpixel((x, y), h("ffffff"))


def bag():
    im, d = _new()
    sack, rope, gold = ramp7("a8743a"), ramp7("ead7a0"), ramp7("ffc92e")
    # sack body: wide belly + neck
    d.ellipse((6, 22, 57, 62), fill=sack["base"])
    d.polygon([(21, 14), (42, 14), (50, 30), (13, 30)], fill=sack["base"])
    _shade_cols(im, (6, 14, 57, 62), sack)
    # canvas weave: sparse darker threads
    px = im.load()
    for y in range(18, 62, 3):
        for x in range(8 + (y // 3) % 2 * 2, 56, 4):
            if px[x, y][3] and sum(px[x, y][:3]) > 200:
                px[x, y] = _dark(px[x, y])
    # patch with stitches
    d.rectangle((38, 40, 50, 50), fill=sack["mlight"])
    for x in range(38, 51, 2):
        im.putpixel((x, 40), sack["deep"]); im.putpixel((x, 50), sack["deep"])
    for y in range(40, 51, 2):
        im.putpixel((38, y), sack["deep"]); im.putpixel((50, y), sack["deep"])
    # seam stitches down the left side
    for y in range(34, 58, 3):
        d.line([(15, y), (17, y + 1)], fill=sack["deep"])
    # gathered, scalloped top
    frill = [(16, 8), (20, 12), (25, 5), (30, 11), (34, 4), (38, 11), (43, 5), (47, 12), (51, 8), (48, 18), (18, 18)]
    d.polygon(frill, fill=sack["mlight"])
    for x in (22, 28, 34, 40, 46):
        d.line([(x, 8), (x - 1, 18)], fill=sack["shade"])
    d.line([(20, 7), (25, 5), (30, 9), (34, 4)], fill=sack["hi"])
    # twisted rope with a bow
    for x in range(14, 51):
        col = rope["base"] if (x // 2) % 2 == 0 else rope["mshade"]
        d.line([(x, 18), (x, 22)], fill=col)
    d.line([(14, 18), (50, 18)], fill=rope["hi"])
    d.line([(14, 22), (50, 22)], fill=rope["shade"])
    d.ellipse((24, 20, 33, 31), fill=rope["light"]); d.ellipse((31, 20, 40, 31), fill=rope["light"])
    d.ellipse((26, 22, 31, 29), fill=sack["deep"]); d.ellipse((33, 22, 38, 29), fill=sack["deep"])
    d.ellipse((29, 19, 35, 25), fill=rope["base"])
    d.line([(30, 25), (24, 36)], fill=rope["base"], width=3)
    d.line([(34, 25), (41, 37)], fill=rope["base"], width=3)
    d.line([(24, 36), (22, 40)], fill=rope["shade"], width=2)
    d.line([(41, 37), (44, 41)], fill=rope["shade"], width=2)
    # AquaCoin emblem (the real coin art)
    coin = Image.open(ROOT / "docs/assets/images/coin_pixel.png").convert("RGBA")
    im.alpha_composite(coin, (14, 28))
    # spilled coins
    _coin(d, im, 53, 56, 6, gold)
    _coin(d, im, 8, 57, 4, gold)
    _coin(d, im, 58, 46, 3, gold)
    d.ellipse((10, 60, 54, 63), fill=(0, 0, 0, 70))
    # shine
    d.line([(10, 34), (13, 46)], fill=sack["hi"], width=2)
    _spark(im, 56, 10)
    _spark(im, 6, 20, 2)
    return im


def _plank_cols(d, im, x0, y0, x1, y1, r, seams):
    for x in range(x0, x1 + 1):
        t = (x - x0) / max(1, x1 - x0)
        d.line([(x, y0), (x, y1)], fill=_tone(r, t))
    for sx in seams:
        d.line([(sx, y0), (sx, y1)], fill=r["deep"])
        d.line([(sx + 1, y0), (sx + 1, y1)], fill=r["light"])


def chest():
    im, d = _new()
    wood, iron, gold, red = ramp7("9a5a28"), ramp7("8c93b8"), ramp7("ffc92e"), ramp7("9a1c34")
    # open lid (back): velvet lining inside, wooden rim
    d.polygon([(9, 6), (54, 6), (58, 28), (5, 28)], fill=wood["shade"])
    d.polygon([(13, 10), (50, 10), (53, 26), (10, 26)], fill=red["base"])
    d.polygon([(13, 10), (50, 10), (51, 14), (12, 14)], fill=red["light"])
    for x in range(15, 50, 6):
        d.line([(x, 12), (x - 1, 26)], fill=red["shade"])
    d.line([(9, 6), (54, 6)], fill=wood["light"], width=2)
    for (x, y) in ((11, 8), (52, 8)):
        d.ellipse((x - 1, y - 1, x + 1, y + 1), fill=iron["light"])
    # heap of treasure
    d.polygon([(6, 32), (12, 20), (22, 12), (32, 9), (42, 12), (52, 20), (58, 32)], fill=gold["base"])
    coins = [(14, 24, 6), (24, 17, 6), (34, 14, 6), (44, 18, 6), (52, 27, 5), (8, 30, 4), (20, 27, 6), (30, 24, 6), (40, 27, 6), (48, 31, 4)]
    for (cx, cy, r) in coins:
        _coin(d, im, cx, cy, r, gold)
    _gem(d, 26, 21, 4, ramp7("ff4f79"))
    _gem(d, 38, 20, 4, ramp7("5ff0ff"))
    _gem(d, 17, 30, 3, ramp7("5de58f"))
    d.ellipse((44, 22, 49, 27), fill=h("fff4f8")); im.putpixel((45, 23), h("ffffff"))
    # chest body
    d.rectangle((4, 30, 59, 60), fill=wood["base"])
    _plank_cols(d, im, 4, 30, 59, 60, wood, [14, 24, 34, 44, 54][::1])
    d.rectangle((4, 30, 59, 34), fill=wood["mlight"])
    for (x, y) in ((8, 42), (19, 50), (29, 38), (40, 52), (50, 44)):       # grain flecks
        d.line([(x, y), (x + 1, y + 4)], fill=wood["shade"])
    # front rim shadow under the heap
    d.line([(4, 35), (59, 35)], fill=wood["deep"])
    # iron bands with rivets
    for x0 in (8, 51):
        d.rectangle((x0, 30, x0 + 5, 60), fill=iron["base"])
        d.line([(x0, 30), (x0, 60)], fill=iron["hi"])
        d.line([(x0 + 5, 30), (x0 + 5, 60)], fill=iron["shade"])
        d.line([(x0 + 1, 30), (x0 + 1, 60)], fill=iron["light"])
        for y in (38, 46, 54):
            d.ellipse((x0 + 1, y, x0 + 4, y + 3), fill=iron["light"]); im.putpixel((x0 + 2, y + 1), h("ffffff"))
    # gold corner brackets
    for (x, y, fx) in ((4, 56, 1), (55, 56, -1)):
        d.polygon([(x, y), (x + fx * 5, y), (x + fx * 5, y + 4), (x, y + 4)], fill=gold["base"])
    d.rectangle((4, 59, 59, 61), fill=wood["deep"])
    # lock plate with keyhole
    d.rectangle((26, 36, 38, 50), fill=gold["base"])
    d.rectangle((26, 36, 38, 38), fill=gold["hi"])
    d.line([(26, 36), (26, 50)], fill=gold["light"])
    d.line([(38, 36), (38, 50)], fill=gold["shade"])
    d.rectangle((26, 49, 38, 50), fill=gold["deep"])
    d.ellipse((30, 40, 34, 44), fill=OUT)
    d.polygon([(31, 44), (33, 44), (34, 48), (30, 48)], fill=OUT)
    for (x, y) in ((28, 38), (36, 38), (28, 47), (36, 47)):
        im.putpixel((x, y), gold["deep"])
    _spark(im, 60, 8)
    _spark(im, 3, 14)
    _spark(im, 32, 3, 2)
    return im


def sprite(name):
    im = {"bag": bag, "chest": chest}[name]()
    _edges(im)
    return _outline(im)
