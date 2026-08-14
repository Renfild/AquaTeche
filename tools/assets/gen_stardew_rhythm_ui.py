"""Generate Stardew Valley-style pixel art for Rhythm Hook fishing UI."""
from PIL import Image, ImageDraw
import os
import math

OUT = r"c:\Users\xieto\Desktop\AquaTech\mods\aquatech-ui\src\main\resources\assets\aquatech_ui\textures\gui\minigame\rhythm"
SCALE = 4  # logical px * SCALE = texture px

# Stardew-ish cozy palette
WOOD_D = (92, 58, 34)
WOOD_M = (120, 78, 48)
WOOD_L = (148, 100, 62)
NAIL = (196, 160, 90)
PARCH = (240, 226, 198)
PARCH_D = (220, 204, 168)
PARCH_DD = (200, 184, 148)
INK = (58, 42, 30)
WATER = (90, 168, 196)
WATER_L = (140, 210, 228)
BRASS = (196, 164, 96)
CREAM = (255, 248, 220)
GEM_OFF = (120, 100, 80)
GEM_ON = (100, 200, 220)
GEM_DONE = (110, 190, 110)
TRANS = (0, 0, 0, 0)


def px(n):
    return n * SCALE


def save(img, name):
    path = os.path.join(OUT, name)
    img.save(path)
    print(name, img.size)


def draw_panel():
    lw, lh = 140, 158
    w, h = px(lw), px(lh)
    img = Image.new("RGBA", (w, h), TRANS)
    d = ImageDraw.Draw(img)

    # Wood frame
    d.rectangle([0, 0, w - 1, h - 1], fill=WOOD_D)
    m = px(4)
    d.rectangle([m, m, w - m - 1, h - m - 1], fill=WOOD_M)
    inner = px(8)
    d.rectangle([inner, inner, w - inner - 1, h - inner - 1], fill=PARCH)

    # Inner shadow line
    d.rectangle([inner, inner, w - inner - 1, inner + px(1) - 1], fill=PARCH_D)
    d.rectangle([inner, inner, inner + px(1) - 1, h - inner - 1], fill=PARCH_D)

    # Corner nails
    nail = px(3)
    for cx, cy in [(inner + 2, inner + 2), (w - inner - nail - 2, inner + 2),
                   (inner + 2, h - inner - nail - 2), (w - inner - nail - 2, h - inner - nail - 2)]:
        d.rectangle([cx, cy, cx + nail, cy + nail], fill=NAIL)
        d.point((cx + 1, cy + 1), fill=WOOD_L)

    # Top title ribbon hint (subtle)
    ry0, ry1 = px(10), px(22)
    rx0, rx1 = px(28), px(112)
    d.rectangle([rx0, ry0, rx1, ry1], fill=PARCH_D)
    d.rectangle([rx0, ry0, rx1, ry0 + px(1) - 1], fill=PARCH_DD)

    # Bottom hint strip
    by0, by1 = px(138), px(150)
    d.rectangle([px(16), by0, px(124), by1], fill=PARCH_D)

    save(img, "panel.png")


def draw_dial():
    size = px(88)
    img = Image.new("RGBA", (size, size), TRANS)
    d = ImageDraw.Draw(img)
    cx, cy = size // 2, size // 2
    outer = px(42)
    inner = px(14)

    # Outer brass ring
    d.ellipse([cx - outer, cy - outer, cx + outer, cy + outer], fill=BRASS, outline=INK)
    d.ellipse([cx - outer + px(2), cy - outer + px(2), cx + outer - px(2), cy + outer - px(2)], fill=WOOD_M)

    # Face
    face_r = outer - px(5)
    d.ellipse([cx - face_r, cy - face_r, cx + face_r, cy + face_r], fill=PARCH, outline=PARCH_DD)

    # Tick marks (12)
    for i in range(12):
        a = math.radians(i * 30 - 90)
        r0 = face_r - px(4)
        r1 = face_r - px(1)
        x0 = cx + int(math.cos(a) * r0)
        y0 = cy + int(math.sin(a) * r0)
        x1 = cx + int(math.cos(a) * r1)
        y1 = cy + int(math.sin(a) * r1)
        d.line([(x0, y0), (x1, y1)], fill=PARCH_DD, width=max(1, SCALE // 2))

    # Hub
    d.ellipse([cx - inner, cy - inner, cx + inner, cy + inner], fill=WOOD_D, outline=INK)
    d.ellipse([cx - inner + px(2), cy - inner + px(2), cx + inner - px(2), cy + inner - px(2)], fill=BRASS)

    save(img, "dial_face.png")


def draw_needle():
    w, h = px(14), px(36)
    img = Image.new("RGBA", (w, h), TRANS)
    d = ImageDraw.Draw(img)
    cx = w // 2
    # Pointer up from bottom hub
    pts = [(cx, 0), (cx + px(3), px(8)), (cx + px(1), px(8)), (cx + px(1), h - px(4)),
           (cx - px(1), h - px(4)), (cx - px(1), px(8)), (cx - px(3), px(8))]
    d.polygon(pts, fill=BRASS, outline=INK)
    d.ellipse([cx - px(3), h - px(5), cx + px(3), h - px(1)], fill=WOOD_M, outline=INK)
    save(img, "needle.png")


def draw_bar():
    lw, lh = 104, 10
    w, h = px(lw), px(lh)
    img = Image.new("RGBA", (w, h), TRANS)
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, w - 1, h - 1], fill=WOOD_D, outline=INK)
    d.rectangle([px(1), px(1), w - px(1) - 1, h - px(1) - 1], fill=PARCH_DD)
    save(img, "bar_bg.png")

    fill = Image.new("RGBA", (w, h), TRANS)
    d2 = ImageDraw.Draw(fill)
    d2.rectangle([px(2), px(2), w - px(2) - 1, h - px(2) - 1], fill=WATER)
  # highlight
    d2.rectangle([px(2), px(2), w - px(2) - 1, px(3) - 1], fill=WATER_L)
    save(fill, "bar_fill.png")


def draw_gem(on, done=False):
    s = px(8)
    img = Image.new("RGBA", (s, s), TRANS)
    d = ImageDraw.Draw(img)
    col = GEM_DONE if done else (GEM_ON if on else GEM_OFF)
    d.ellipse([0, 0, s - 1, s - 1], fill=col, outline=INK)
    if on or done:
        d.point((px(2), px(2)), fill=CREAM)
    save(img, "gem_on.png" if on else "gem_off.png")
    if done:
        save(img, "gem_done.png")


def draw_bubble_frames():
    for fi in range(3):
        s = px(6)
        img = Image.new("RGBA", (s, s), TRANS)
        d = ImageDraw.Draw(img)
        off = fi * px(1)
        d.ellipse([off, off, s - 1 - off, s - 1 - off], outline=WATER, width=max(1, SCALE // 2))
        save(img, f"bubble_f{fi}.png")


def main():
    os.makedirs(OUT, exist_ok=True)
    # Remove old hi-res AI assets
    for f in os.listdir(OUT):
        if f.endswith(".png"):
            os.remove(os.path.join(OUT, f))
    draw_panel()
    draw_dial()
    draw_needle()
    draw_bar()
    draw_gem(False)
    draw_gem(True)
    draw_gem(True, done=True)
    draw_bubble_frames()
    print("done", len(os.listdir(OUT)), "files")


if __name__ == "__main__":
    main()
