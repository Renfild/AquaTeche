"""Coloured 16x16 pixel icons for the F4 hub side menu (used by px_assets.py as --i-<name>).
Flat shapes get a rim light on the top/left edges and a darker bottom/right edge, then a dark outline, like the reference UI."""
import colorsys

from PIL import Image, ImageDraw

NAMES = ("profile", "store", "cases", "pass", "fishing", "events", "auction", "kits", "warps", "tops", "settings")
OUT = (26, 14, 46, 255)


def h(s, a=255):
    s = s.lstrip("#")
    return (int(s[0:2], 16), int(s[2:4], 16), int(s[4:6], 16), a)


def _hsv(hh, s, v):
    r, g, b = colorsys.hsv_to_rgb(hh % 1.0, max(0, min(1, s)), max(0, min(1, v)))
    return (int(r * 255), int(g * 255), int(b * 255), 255)


def ramp(base):
    r, g, b, _ = h(base)
    hh, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
    return dict(hi=_hsv(hh - 0.03, s * 0.4, v + 0.32), light=_hsv(hh - 0.015, s * 0.7, v + 0.14), base=h(base),
                shade=_hsv(hh + 0.03, s * 1.08, v * 0.72), deep=_hsv(hh + 0.05, s * 1.1, v * 0.5))


def _light(c):
    r, g, b = c[:3]
    hh, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
    return _hsv(hh - 0.02, s * 0.72, v + 0.2)


def _dark(c):
    r, g, b = c[:3]
    hh, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
    return _hsv(hh + 0.03, s * 1.1, v * 0.7)


def _edges(im):
    px = im.load()
    src = im.copy().load()
    for y in range(16):
        for x in range(16):
            c = src[x, y]
            if c[3] == 0 or sum(c[:3]) < 150:
                continue

            def clear(dx, dy):
                nx, ny = x + dx, y + dy
                return not (0 <= nx < 16 and 0 <= ny < 16) or src[nx, ny][3] == 0

            if clear(0, 1) or clear(1, 0):
                px[x, y] = _dark(c)
            elif clear(0, -1) or clear(-1, 0):
                px[x, y] = _light(c)


def _outline(im):
    px = im.load()
    out = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    po = out.load()
    for y in range(16):
        for x in range(16):
            if px[x, y][3] == 0:
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    nx, ny = x + dx, y + dy
                    if 0 <= nx < 16 and 0 <= ny < 16 and px[nx, ny][3]:
                        po[x, y] = OUT
                        break
    out.alpha_composite(im)
    return out


def _new():
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    return im, ImageDraw.Draw(im)


def _clear(im, pts):
    for p in pts:
        im.putpixel(p, (0, 0, 0, 0))


def profile():
    im, d = _new()
    skin, hair, shirt = ramp("f0b88a"), ramp("7a4a24"), ramp("3b8dff")
    d.rectangle((3, 10, 12, 14), fill=shirt["base"]); _clear(im, [(3, 10), (12, 10)])
    d.rectangle((6, 9, 9, 10), fill=skin["shade"])
    d.rectangle((4, 2, 11, 9), fill=skin["base"]); _clear(im, [(4, 2), (11, 2)])
    d.rectangle((4, 2, 11, 4), fill=hair["base"]); d.rectangle((4, 5, 4, 6), fill=hair["base"]); d.rectangle((11, 5, 11, 6), fill=hair["base"])
    d.line([(5, 2), (9, 2)], fill=hair["light"])
    d.line([(6, 12), (9, 12)], fill=shirt["light"])
    im.putpixel((6, 6), OUT); im.putpixel((9, 6), OUT)
    d.line([(7, 8), (8, 8)], fill=skin["deep"])
    return im


def store():
    im, d = _new()
    bag, wood = ramp("ff8a2a"), ramp("8a4a20")
    d.rectangle((3, 6, 12, 14), fill=bag["base"])
    d.rectangle((3, 6, 12, 7), fill=bag["light"])
    for p in ((5, 6), (5, 3), (6, 2), (9, 2), (10, 3), (10, 6)):
        pass
    d.line([(5, 6), (5, 3), (6, 2), (9, 2), (10, 3), (10, 6)], fill=wood["base"])
    d.rectangle((6, 9, 9, 12), fill=h("fff4d0"))
    d.rectangle((7, 10, 8, 10), fill=h("e0343c"))
    return im


def cases():
    im, d = _new()
    wood, gold = ramp("b8742c"), ramp("ffc92e")
    d.rectangle((2, 3, 13, 7), fill=wood["light"]); _clear(im, [(2, 3), (13, 3)])
    d.rectangle((2, 8, 13, 14), fill=wood["base"])
    d.line([(2, 7), (13, 7)], fill=wood["deep"])
    for x in (4, 11):
        d.line([(x, 3), (x, 14)], fill=gold["base"])
    d.rectangle((6, 6, 9, 10), fill=gold["base"])
    im.putpixel((7, 8), OUT); im.putpixel((7, 9), OUT)
    return im


def pass_():
    im, d = _new()
    gold = ramp("ffc92e")
    d.rectangle((1, 3, 14, 12), fill=gold["base"])
    _clear(im, [(1, 7), (1, 8), (14, 7), (14, 8)])
    d.line([(1, 4), (14, 4)], fill=gold["light"])
    for y in (5, 7, 9, 11):
        im.putpixel((11, y), gold["deep"])
    star = {4: [6], 5: [5, 6, 7], 6: list(range(3, 10)), 7: [4, 5, 6, 7, 8], 8: [4, 5, 6, 7, 8], 9: [4, 5, 7, 8]}
    for y, xs in star.items():
        for x in xs:
            im.putpixel((x, y), h("fff4c0"))
    im.putpixel((6, 7), h("ffffff"))
    return im


def fishing():
    im, d = _new()
    fish = ramp("2fb4e8")
    d.ellipse((1, 5, 10, 11), fill=fish["base"])
    d.polygon([(10, 8), (14, 4), (14, 12)], fill=fish["shade"])
    d.polygon([(4, 5), (7, 2), (9, 5)], fill=fish["shade"])
    d.line([(3, 10), (8, 10)], fill=fish["light"])
    d.line([(6, 6), (6, 9)], fill=fish["deep"])
    im.putpixel((3, 7), h("ffffff")); im.putpixel((3, 8), OUT)
    return im


def events():
    im, d = _new()
    gold = ramp("ffc92e")
    d.rectangle((7, 1, 8, 2), fill=gold["shade"])
    d.ellipse((3, 2, 12, 11), fill=gold["base"])
    d.rectangle((2, 9, 13, 11), fill=gold["base"])
    d.rectangle((2, 11, 13, 11), fill=gold["shade"])
    d.ellipse((6, 12, 9, 14), fill=gold["deep"])
    d.line([(5, 4), (5, 7)], fill=gold["hi"])
    return im


def auction():
    im, d = _new()
    steel, wood = ramp("aab4d4"), ramp("a8622a")
    d.polygon([(7, 8), (9, 7), (14, 12), (12, 14)], fill=wood["base"])
    d.polygon([(2, 5), (6, 1), (11, 6), (7, 10)], fill=steel["base"])
    d.line([(4, 4), (7, 2)], fill=steel["hi"])
    d.line([(5, 6), (8, 9)], fill=steel["shade"])
    d.rectangle((1, 13, 7, 14), fill=wood["deep"])
    return im


def kits():
    im, d = _new()
    red, gold = ramp("e0343c"), ramp("ffc92e")
    d.rectangle((2, 8, 13, 14), fill=red["base"])
    d.rectangle((1, 5, 14, 8), fill=red["light"])
    d.line([(2, 8), (13, 8)], fill=red["deep"])
    d.rectangle((7, 5, 8, 14), fill=gold["base"])
    d.ellipse((3, 2, 7, 5), fill=gold["base"]); d.ellipse((8, 2, 12, 5), fill=gold["base"])
    d.rectangle((7, 3, 8, 5), fill=gold["shade"])
    return im


def warps():
    im, d = _new()
    pur = ramp("8a4dff")
    d.ellipse((1, 1, 14, 14), fill=pur["base"])
    d.ellipse((4, 4, 11, 11), fill=(24, 16, 70, 255))
    for p in ((7, 5), (8, 5), (9, 6), (9, 7), (9, 8), (8, 9), (7, 9), (6, 8), (6, 7)):
        im.putpixel(p, h("5ff0ff"))
    im.putpixel((7, 7), h("ffffff")); im.putpixel((8, 7), h("ffffff"))
    return im


def tops():
    im, d = _new()
    gold = ramp("ffc92e")
    d.rectangle((4, 2, 11, 6), fill=gold["base"])
    d.polygon([(4, 6), (11, 6), (9, 9), (6, 9)], fill=gold["base"])
    d.line([(4, 3), (2, 3), (2, 6), (4, 7)], fill=gold["shade"])
    d.line([(11, 3), (13, 3), (13, 6), (11, 7)], fill=gold["shade"])
    d.rectangle((7, 9, 8, 11), fill=gold["shade"])
    d.rectangle((5, 11, 10, 12), fill=gold["base"])
    d.rectangle((4, 13, 11, 14), fill=gold["shade"])
    d.line([(5, 3), (5, 5)], fill=gold["hi"])
    im.putpixel((8, 4), h("fff4c0")); im.putpixel((7, 4), h("fff4c0"))
    return im


def settings():
    im, d = _new()
    steel = ramp("9aa4c8")
    for box in ((6, 1, 9, 3), (6, 12, 9, 14), (1, 6, 3, 9), (12, 6, 14, 9), (2, 2, 4, 4), (11, 2, 13, 4), (2, 11, 4, 13), (11, 11, 13, 13)):
        d.rectangle(box, fill=steel["base"])
    d.ellipse((3, 3, 12, 12), fill=steel["base"])
    d.ellipse((6, 6, 9, 9), fill=(24, 16, 70, 255))
    return im


def refresh():
    im, d = _new()
    c = ramp("dff1ff")
    d.arc((2, 2, 13, 13), 205, 335, fill=c["base"], width=2)
    d.arc((2, 2, 13, 13), 25, 155, fill=c["base"], width=2)
    d.polygon([(9, 2), (14, 2), (14, 7)], fill=c["light"])
    d.polygon([(6, 13), (1, 13), (1, 8)], fill=c["light"])
    return im


_DRAW = {"refresh": refresh, "profile": profile, "store": store, "cases": cases, "pass": pass_, "fishing": fishing, "events": events,
         "auction": auction, "kits": kits, "warps": warps, "tops": tops, "settings": settings}


def icon(name):
    im = _DRAW[name]()
    _edges(im)
    return _outline(im)
