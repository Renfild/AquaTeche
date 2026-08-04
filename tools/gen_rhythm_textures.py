from PIL import Image, ImageDraw
import os
import math

OUT = r"c:\Users\xieto\Desktop\AquaTech\mods\aquatech-ui\src\main\resources\assets\aquatech_ui\textures\gui\minigame\rhythm"
os.makedirs(OUT, exist_ok=True)
SCALE = 4

INK = (61, 43, 31, 255)
PARCH = (232, 214, 170, 255)
SAFE = (90, 168, 90, 255)
GOLD = (230, 178, 64, 255)
DANGER = (196, 72, 64, 255)
WATER = (74, 132, 148, 255)
MUTED = (138, 110, 78, 255)
BRASS = (196, 163, 90, 255)
WOOD1 = (120, 78, 42, 255)
WOOD2 = (92, 58, 30, 255)
WOOD3 = (150, 100, 55, 255)
CREAM = (240, 228, 200, 255)
TRANS = (0, 0, 0, 0)


def save(img, name):
    path = os.path.join(OUT, name)
    img.save(path)
    print("wrote", name, img.size)


def px(logical_w, logical_h):
    return logical_w * SCALE, logical_h * SCALE


# panel 128x148
w, h = px(128, 148)
panel = Image.new("RGBA", (w, h), TRANS)
d = ImageDraw.Draw(panel)
d.rectangle([0, 0, w - 1, h - 1], fill=WOOD2)
d.rectangle([SCALE, SCALE, w - 1 - SCALE, h - 1 - SCALE], fill=WOOD1)
margin = 6 * SCALE
d.rectangle([margin, margin, w - 1 - margin, h - 1 - margin], fill=PARCH)
for i in range(0, w, 8 * SCALE):
    d.line([(i, 0), (i, margin)], fill=WOOD3, width=1)
    d.line([(i, h - margin), (i, h)], fill=WOOD3, width=1)
for cx, cy in [
    (3 * SCALE, 3 * SCALE),
    (w - 4 * SCALE, 3 * SCALE),
    (3 * SCALE, h - 4 * SCALE),
    (w - 4 * SCALE, h - 4 * SCALE),
]:
    d.ellipse([cx, cy, cx + 2 * SCALE, cy + 2 * SCALE], fill=BRASS)
d.rectangle([margin, margin, w - 1 - margin, h - 1 - margin], outline=MUTED)
save(panel, "panel.png")

# dial_face 88x88
dw, dh = px(88, 88)
dial = Image.new("RGBA", (dw, dh), TRANS)
d = ImageDraw.Draw(dial)
cx, cy = dw // 2, dh // 2
R = 42 * SCALE
d.ellipse([cx - R, cy - R, cx + R, cy + R], fill=BRASS)
d.ellipse(
    [cx - R + 3 * SCALE, cy - R + 3 * SCALE, cx + R - 3 * SCALE, cy + R - 3 * SCALE],
    fill=WOOD1,
)
d.ellipse(
    [cx - R + 6 * SCALE, cy - R + 6 * SCALE, cx + R - 6 * SCALE, cy + R - 6 * SCALE],
    fill=CREAM,
)
for i in range(12):
    a = math.radians(i * 30 - 90)
    r0, r1 = R - 10 * SCALE, R - 6 * SCALE
    x0 = cx + int(math.cos(a) * r0)
    y0 = cy + int(math.sin(a) * r0)
    x1 = cx + int(math.cos(a) * r1)
    y1 = cy + int(math.sin(a) * r1)
    d.line([(x0, y0), (x1, y1)], fill=MUTED, width=SCALE)
d.ellipse([cx - 4 * SCALE, cy - 4 * SCALE, cx + 4 * SCALE, cy + 4 * SCALE], fill=INK)
d.ellipse([cx - 2 * SCALE, cy - 2 * SCALE, cx + 2 * SCALE, cy + 2 * SCALE], fill=GOLD)
save(dial, "dial_face.png")

# needle
nw, nh = px(8, 32)
needle = Image.new("RGBA", (nw, nh), TRANS)
d = ImageDraw.Draw(needle)
d.polygon(
    [
        (nw // 2, 0),
        (nw - SCALE, nh - 2 * SCALE),
        (nw // 2, nh - SCALE),
        (SCALE, nh - 2 * SCALE),
    ],
    fill=BRASS,
)
d.ellipse([nw // 2 - 2 * SCALE, nh - 4 * SCALE, nw // 2 + 2 * SCALE, nh], fill=GOLD)
save(needle, "needle.png")


def make_key(hot):
    kw, kh = px(20, 14)
    img = Image.new("RGBA", (kw, kh), TRANS)
    d = ImageDraw.Draw(img)
    bg = GOLD if hot else MUTED
    d.rounded_rectangle([0, 0, kw - 1, kh - 1], radius=2 * SCALE, fill=bg, outline=INK)
    d.rounded_rectangle(
        [4 * SCALE, 2 * SCALE, 16 * SCALE, 12 * SCALE],
        radius=SCALE,
        fill=PARCH,
        outline=INK,
    )
    col = SAFE if hot else DANGER
    d.rectangle([10 * SCALE, 3 * SCALE, 15 * SCALE, 7 * SCALE], fill=col)
    d.line([(10 * SCALE, 3 * SCALE), (10 * SCALE, 11 * SCALE)], fill=INK, width=1)
    return img


save(make_key(False), "key_rmb.png")
save(make_key(True), "key_rmb_hot.png")

# fish
fw, fh = px(14, 10)
fish = Image.new("RGBA", (fw, fh), TRANS)
d = ImageDraw.Draw(fish)
d.ellipse([2 * SCALE, 2 * SCALE, 11 * SCALE, 8 * SCALE], fill=(232, 140, 64, 255), outline=INK)
d.polygon([(2 * SCALE, 5 * SCALE), (0, 2 * SCALE), (0, 8 * SCALE)], fill=(232, 140, 64, 255))
d.ellipse([8 * SCALE, 4 * SCALE, 10 * SCALE, 6 * SCALE], fill=INK)
save(fish, "fish_icon.png")


def make_star(on):
    sw, sh = px(9, 9)
    img = Image.new("RGBA", (sw, sh), TRANS)
    d = ImageDraw.Draw(img)
    cx, cy = sw // 2, sh // 2
    col = GOLD if on else MUTED
    pts = []
    for i in range(10):
        a = math.radians(i * 36 - 90)
        r = (4 if i % 2 == 0 else 1.6) * SCALE
        pts.append((cx + int(math.cos(a) * r), cy + int(math.sin(a) * r)))
    d.polygon(pts, fill=col, outline=INK)
    return img


save(make_star(True), "star_on.png")
save(make_star(False), "star_off.png")


def make_dot(on):
    dw, dh = px(8, 8)
    img = Image.new("RGBA", (dw, dh), TRANS)
    d = ImageDraw.Draw(img)
    col = SAFE if on else MUTED
    d.ellipse([SCALE, SCALE, dw - SCALE - 1, dh - SCALE - 1], fill=col, outline=INK)
    if on:
        d.ellipse([2 * SCALE, 2 * SCALE, 4 * SCALE, 4 * SCALE], fill=CREAM)
    return img


save(make_dot(True), "dot_on.png")
save(make_dot(False), "dot_off.png")

for fi in range(3):
    bw, bh = px(8, 8)
    img = Image.new("RGBA", (bw, bh), TRANS)
    d = ImageDraw.Draw(img)
    r = (2 + fi) * SCALE
    ox = (4 - (2 + fi)) * SCALE
    d.ellipse([ox, ox, ox + 2 * r, ox + 2 * r], outline=WATER, width=SCALE)
    d.point((ox + SCALE, ox + SCALE), fill=CREAM)
    save(img, f"bubble_f{fi}.png")

for fi in range(3):
    sw, sh = px(7, 7)
    img = Image.new("RGBA", (sw, sh), TRANS)
    d = ImageDraw.Draw(img)
    cx, cy = sw // 2, sh // 2
    col = GOLD if fi % 2 == 0 else CREAM
    arm = (2 + fi % 2) * SCALE
    d.line([(cx, cy - arm), (cx, cy + arm)], fill=col, width=SCALE)
    d.line([(cx - arm, cy), (cx + arm, cy)], fill=col, width=SCALE)
    if fi == 2:
        d.line([(cx - arm, cy - arm), (cx + arm, cy + arm)], fill=col, width=1)
    save(img, f"sparkle_f{fi}.png")

for fi in range(3):
    rw, rh = px(32, 32)
    img = Image.new("RGBA", (rw, rh), TRANS)
    d = ImageDraw.Draw(img)
    cx, cy = rw // 2, rh // 2
    r = (6 + fi * 5) * SCALE
    alpha = 180 - fi * 50
    for thick in range(SCALE):
        d.ellipse(
            [cx - r - thick, cy - r - thick, cx + r + thick, cy + r + thick],
            outline=(*WATER[:3], alpha),
        )
    save(img, f"ripple_f{fi}.png")

print("done", len(os.listdir(OUT)), "files")
