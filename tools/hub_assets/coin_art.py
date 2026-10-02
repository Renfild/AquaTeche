"""AquaCoin pixel art, 32x32: thick gold coin (visible reeded edge, raised rim, recessed field) with an embossed emblem.
`python tools/hub_assets/coin_art.py [emblem]` writes docs/assets/images/coin_pixel.png (then run tools/build_hub_html.py)."""
import sys
from pathlib import Path

from PIL import Image, ImageDraw

N = 32
EMBLEMS = ("fish", "anchor", "wave", "drop")
DEFAULT = "fish"
G = dict(hi="fff8c4", light="ffe36a", base="ffc526", mid="f2a514", shade="d9820a", deep="a85806", out="4a2400")


def h(s, a=255):
    s = s.lstrip("#")
    return (int(s[0:2], 16), int(s[2:4], 16), int(s[4:6], 16), a)


def _emblem(name):
    """white-on-black mask of the emblem, centred on the coin face"""
    m = Image.new("L", (N, N), 0)
    d = ImageDraw.Draw(m)
    if name == "fish":
        d.ellipse((8, 9, 20, 19), fill=255)
        d.polygon([(19, 14), (25, 9), (24, 14), (25, 19)], fill=255)
        d.polygon([(12, 9), (15, 6), (17, 9)], fill=255)
        d.point((11, 13), fill=0)
        d.line([(14, 11), (14, 17)], fill=0)
    elif name == "anchor":
        d.ellipse((13, 6, 18, 11), outline=255, width=2)
        d.rectangle((15, 10, 16, 21), fill=255)
        d.rectangle((11, 12, 20, 13), fill=255)
        d.arc((8, 10, 23, 23), 15, 165, fill=255, width=2)
        d.polygon([(7, 16), (11, 18), (8, 20)], fill=255)
        d.polygon([(24, 16), (20, 18), (23, 20)], fill=255)
    elif name == "wave":
        for y in (9, 14, 19):
            d.line([(8, y + 2), (11, y), (14, y + 2), (17, y), (20, y + 2), (23, y)], fill=255, width=2)
    else:  # drop
        d.polygon([(15.5, 6), (21, 15), (10, 15)], fill=255)
        d.ellipse((10, 11, 21, 22), fill=255)
        d.arc((12, 14, 18, 20), 100, 200, fill=0, width=1)
    return m


def coin(emblem=DEFAULT):
    im = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    c = {k: h(v) for k, v in G.items()}
    # reeded edge (coin thickness), then the face on top of it
    d.ellipse((2, 4, 29, 30), fill=c["deep"])
    px = im.load()
    for x in range(2, 30):
        if x % 2 == 0:
            for y in range(16, 31):
                if px[x, y][3]:
                    px[x, y] = c["shade"]
    d.ellipse((2, 1, 29, 27), fill=c["base"])
    # raised rim: light on the upper-left arc, darker on the lower-right
    d.ellipse((2, 1, 29, 27), outline=c["mid"], width=2)
    d.arc((2, 1, 29, 27), 150, 320, fill=c["light"], width=2)
    d.arc((2, 1, 29, 27), 185, 265, fill=c["hi"], width=1)
    # recessed field with an inner shadow under the upper-left rim
    d.ellipse((5, 4, 26, 24), fill=c["mid"])
    d.arc((5, 4, 26, 24), 160, 300, fill=c["shade"], width=1)
    d.arc((5, 4, 26, 24), 340, 120, fill=c["light"], width=1)
    # embossed emblem: dark drop shadow down-right, bright body, top-left glint
    m = _emblem(emblem).load()
    for y in range(N):
        for x in range(N):
            if m[x, y] and x + 1 < N and y + 1 < N and px[x + 1, y + 1][3]:
                px[x + 1, y + 1] = c["deep"]
    for y in range(N):
        for x in range(N):
            if m[x, y] and px[x, y][3]:
                up = y == 0 or not m[x, y - 1]
                left = x == 0 or not m[x - 1, y]
                px[x, y] = c["hi"] if (up or left) else c["light"]
    # glints
    for (x, y) in ((8, 5), (9, 4), (7, 6), (23, 22)):
        if px[x, y][3]:
            px[x, y] = h("ffffff")
    # coloured outline
    out = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    po = out.load()
    for y in range(N):
        for x in range(N):
            if px[x, y][3] == 0:
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    nx, ny = x + dx, y + dy
                    if 0 <= nx < N and 0 <= ny < N and px[nx, ny][3]:
                        po[x, y] = c["out"]
                        break
    out.alpha_composite(im)
    return out


if __name__ == "__main__":
    name = sys.argv[1] if len(sys.argv) > 1 else DEFAULT
    dst = Path(__file__).resolve().parents[2] / "docs/assets/images/coin_pixel.png"
    coin(name).save(dst)
    print("wrote", dst, name)
