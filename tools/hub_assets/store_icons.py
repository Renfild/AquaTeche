"""Detailed 64x64 pixel icons for the F4 shop: gems and battle pass ticket (shown 1:1 in an 80px frame).
Shapes are drawn in 32-unit coordinates through a 2x wrapper, then finished with native-resolution detail
(grain, rivets, facets, highlights). Rim light + dark outline are added on top (same look as menu_icons.py)."""
import math

from PIL import Image, ImageDraw

from menu_icons import OUT, h, ramp, _light, _dark

N = 64
S = 2
NAMES = ("gems", "ticket")


def _lerp(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3)) + (255,)


def ramp7(base):
    r = ramp(base)
    r["mlight"] = _lerp(r["light"], r["base"], .5)
    r["mshade"] = _lerp(r["base"], r["shade"], .5)
    return r


def _tone(r, t):
    for lim, key in ((.10, "hi"), (.24, "light"), (.40, "mlight"), (.58, "base"), (.74, "mshade"), (.90, "shade")):
        if t < lim:
            return r[key]
    return r["deep"]


class K:
    """2x canvas: shape calls take 32-unit coordinates, `px` takes native 64-grid pixels"""

    def __init__(self):
        self.im = Image.new("RGBA", (N, N), (0, 0, 0, 0))
        self.d = ImageDraw.Draw(self.im)

    def _p(self, pts):
        return [(x * S, y * S) for x, y in pts]

    def poly(self, pts, fill):
        self.d.polygon(self._p(pts), fill=fill)

    def rect(self, x0, y0, x1, y1, fill):
        self.d.rectangle((x0 * S, y0 * S, (x1 + 1) * S - 1, (y1 + 1) * S - 1), fill=fill)

    def ell(self, x0, y0, x1, y1, fill=None, outline=None, width=1):
        self.d.ellipse((x0 * S, y0 * S, (x1 + 1) * S - 1, (y1 + 1) * S - 1), fill=fill, outline=outline, width=width * S if outline else 1)

    def arc(self, x0, y0, x1, y1, a0, a1, fill, width=1):
        self.d.arc((x0 * S, y0 * S, (x1 + 1) * S - 1, (y1 + 1) * S - 1), a0, a1, fill=fill, width=width * S)

    def line(self, pts, fill, width=1):
        self.d.line(self._p(pts), fill=fill, width=max(1, int(width * S)))

    def px(self, x, y, fill):
        if 0 <= x < N and 0 <= y < N:
            self.im.putpixel((x, y), fill)

    def pxl(self, pts, fill):
        for (x, y) in pts:
            self.px(x, y, fill)

    def clear(self, fn):
        self.d.rectangle((0, 0, 0, 0))
        fn()


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


def _band(k, x0, y0, x1, y1, r):
    hh = (y1 - y0 + 1) * S
    for j in range(hh):
        t = j / max(1, hh - 1)
        k.d.line([(x0 * S, y0 * S + j), ((x1 + 1) * S - 1, y0 * S + j)], fill=_tone(r, t))


def _star(cx, cy, ro, ri, rot=-90):
    pts = []
    for q in range(10):
        a = math.radians(rot + q * 36)
        rr = ro if q % 2 == 0 else ri
        pts.append((cx + rr * math.cos(a), cy + rr * math.sin(a)))
    return pts


def _spark(k, x, y, r=3):
    for q in range(-r, r + 1):
        for (a, b) in ((x + q, y), (x, y + q)):
            if 0 <= a < N and 0 <= b < N and k.im.getpixel((a, b))[3] == 0:
                k.px(a, b, h("ffffff") if abs(q) <= r // 2 else h("cfe4ff"))
    k.px(x, y, h("ffffff"))


def _brilliant(k, x0, y0, w, hgt, r):
    def P(u, v):
        return (x0 + u * w, y0 + v * hgt)
    gy = .36
    facets = [
        ([P(.27, 0), P(.73, 0), P(.86, gy), P(.14, gy)], r["light"]),
        ([P(.27, 0), P(.14, gy), P(0, gy)], r["mlight"]),
        ([P(.73, 0), P(.86, gy), P(1, gy)], r["mshade"]),
        ([P(.27, 0), P(.5, gy), P(.14, gy)], r["hi"]),
        ([P(.73, 0), P(.5, gy), P(.86, gy)], r["base"]),
        ([P(.27, 0), P(.5, 0), P(.5, gy)], r["light"]),
        ([P(.5, 0), P(.73, 0), P(.5, gy)], r["mlight"]),
        ([P(0, gy), P(.14, gy), P(.5, 1)], r["base"]),
        ([P(.14, gy), P(.5, gy), P(.5, 1)], r["light"]),
        ([P(.5, gy), P(.86, gy), P(.5, 1)], r["shade"]),
        ([P(.86, gy), P(1, gy), P(.5, 1)], r["deep"]),
        ([P(.14, gy), P(.32, gy), P(.5, 1)], r["hi"]),
        ([P(.68, gy), P(.86, gy), P(.5, 1)], r["mshade"]),
        ([P(.32, gy), P(.5, gy), P(.5, 1)], r["mlight"]),
    ]
    for pts, col in facets:
        k.poly(pts, col)
    k.line([P(.14, gy), P(.86, gy)], r["hi"])
    k.line([P(.27, .02), P(.5, .02)], h("ffffff"))
    k.line([P(.2, gy + .06), P(.36, gy + .32)], h("ffffff"))
    k.line([P(.44, gy + .06), P(.48, gy + .18)], r["hi"])


def gems():
    k = K()
    k.ell(2, 26, 29, 31, fill=(0, 0, 0, 90))
    _brilliant(k, 1, 11, 17, 17, ramp7("1fa152"))
    _brilliant(k, 14, 3, 16, 16, ramp7("4ee08c"))
    _brilliant(k, 15, 16, 13, 13, ramp7("2fc46a"))
    _spark(k, 10, 16, 4)
    _spark(k, 56, 30, 3)
    _spark(k, 6, 40, 2)
    return k.im


def ticket():
    k = K()
    g = ramp7("ffc92e")
    k.rect(1, 7, 30, 25, g["base"])
    _band(k, 1, 7, 30, 25, g)
    k.ell(-2, 13, 4, 19, fill=(0, 0, 0, 0)); k.ell(27, 13, 33, 19, fill=(0, 0, 0, 0))
    k.d.rectangle((6, 18, 57, 46), outline=g["deep"])
    k.d.rectangle((8, 20, 55, 44), outline=g["light"])
    for y in range(20, 46, 3):
        k.px(40, y, g["deep"]); k.px(41, y, g["shade"])
    k.poly(_star(11, 16, 6.5, 2.8), h("fff4c0"))
    k.poly(_star(11, 16, 4.6, 2.0), h("ffffff"))
    k.line([(11, 11), (11, 15)], h("ffe9a0"))
    for (y, wdt) in ((12, 6), (15, 4), (18, 5)):
        k.line([(22, y), (22 + wdt, y)], g["deep"], 1)
        k.line([(22, y + 0.5), (22 + wdt, y + 0.5)], g["shade"], 0.5)
    for x in (44, 46, 47, 50, 52, 54):
        k.d.line([(x, 40), (x, 44)], fill=g["deep"])
    k.line([(3, 8), (12, 8)], h("fff4c0"))
    for q in range(10):
        k.px(8 + q * 2, 15 - q // 2, h("fff4c0") if q % 3 == 0 else g["hi"])
    return k.im


_DRAW = {"gems": gems, "ticket": ticket}


def icon(name):
    im = _DRAW[name]()
    _edges(im)
    return _outline(im)
