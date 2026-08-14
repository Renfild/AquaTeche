"""Generate original pixel-art fishing minigame UI (StarCatcher-inspired layout)."""
from PIL import Image, ImageDraw
import os
import math

OUT = r"c:\Users\xieto\Desktop\AquaTech\mods\aquatech-ui\src\main\resources\assets\aquatech_ui\textures\gui\minigame\fishing"
SCALE = 2

# Cozy ocean palette
SKY = (120, 180, 220)
WATER_D = (30, 70, 130)
WATER_M = (50, 110, 170)
WATER_L = (80, 150, 200)
SAND = (180, 160, 120)
ROCK = (90, 80, 70)
SEAWEED = (40, 120, 60)
GLASS = (180, 220, 240, 80)
INK = (40, 30, 25)
WHITE = (245, 245, 240)
GOLD = (220, 180, 60)
GREEN = (80, 180, 90)
YELLOW = (220, 190, 70)
RED = (200, 70, 60)
TRANS = (0, 0, 0, 0)
BRASS = (190, 160, 90)


def px(n):
    return n * SCALE


def save(img, name):
    path = os.path.join(OUT, name)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)
    print(name, img.size)


def draw_tank():
    w, h = px(64), px(192)
    img = Image.new("RGBA", (w, h), TRANS)
    d = ImageDraw.Draw(img)
    # glass cylinder
    d.rectangle([px(8), px(4), w - px(8), h - px(4)], fill=WATER_D)
    d.rectangle([px(10), px(6), w - px(10), h - px(6)], fill=WATER_M)
    # surface ripples
    for x in range(px(12), w - px(12), px(4)):
        d.line([(x, px(8)), (x + px(2), px(10))], fill=WATER_L, width=1)
    # rocks + seaweed
    d.polygon([(px(10), h - px(8)), (px(22), h - px(30)), (px(14), h - px(8))], fill=ROCK)
    d.polygon([(w - px(10), h - px(8)), (w - px(24), h - px(28)), (w - px(12), h - px(8))], fill=ROCK)
    for i in range(3):
        sx = px(16 + i * 4)
        d.line([(sx, h - px(12)), (sx + px(2), h - px(36 + i * 6))], fill=SEAWEED, width=2)
    # sand bottom
    d.rectangle([px(10), h - px(14), w - px(10), h - px(6)], fill=SAND)
    # frame
    d.rectangle([px(6), px(2), px(9), h - px(2)], fill=(140, 140, 150))
    d.rectangle([w - px(9), px(2), w - px(6), h - px(2)], fill=(140, 140, 150))
    d.rectangle([px(6), px(2), w - px(6), px(5)], fill=(160, 160, 170))
    d.rectangle([px(6), h - px(5), w - px(6), h - px(2)], fill=(100, 100, 110))
    save(img, "tank_surface.png")


def draw_bar_outline():
    w, h = px(16), px(100)
    img = Image.new("RGBA", (w, h), TRANS)
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, w - 1, h - 1], outline=WHITE, width=2)
    d.rectangle([px(2), px(2), w - px(3), h - px(3)], outline=(180, 180, 180), width=1)
    # bucket base
    d.rectangle([px(1), h - px(10), w - px(2), h - px(1)], fill=(200, 200, 200), outline=INK)
    save(img, "bar_outline.png")


def draw_bar_fill():
    w, h = px(12), px(96)
    img = Image.new("RGBA", (w, h), TRANS)
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, w - 1, h - 1], fill=RED)
    d.rectangle([px(1), px(1), w - px(2), px(3)], fill=(255, 120, 100))
    save(img, "bar_fill.png")


def draw_treasure_bar():
    w, h = px(8), px(100)
    img = Image.new("RGBA", (w, h), TRANS)
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, w - 1, h - 1], outline=GOLD, width=2)
    # chest icon bottom
    d.rectangle([px(1), h - px(8), w - px(2), h - px(2)], fill=(160, 100, 40), outline=INK)
    d.rectangle([px(2), h - px(7), w - px(3), h - px(5)], fill=GOLD)
    save(img, "treasure_bar.png")


def draw_pointer():
    w, h = px(32), px(16)
    img = Image.new("RGBA", (w, h), TRANS)
    d = ImageDraw.Draw(img)
    cx = w // 2
    d.polygon([(cx, px(2)), (w - px(2), h - px(4)), (cx, h - px(2)), (px(2), h - px(4))], fill=BRASS, outline=INK)
    d.line([(cx, px(4)), (cx, h - px(4))], fill=INK, width=1)
    save(img, "pointer.png")


def draw_spot(name, inner, outer, mid):
    w, h = px(16), px(48)
    img = Image.new("RGBA", (w, h), TRANS)
    d = ImageDraw.Draw(img)
    d.rectangle([px(2), 0, w - px(3), h - 1], fill=outer)
    d.rectangle([px(3), px(4), w - px(4), h - px(5)], fill=mid)
    d.rectangle([px(4), px(10), w - px(5), h - px(11)], fill=inner)
    save(img, f"spots/{name}.png")


def draw_spark(frame):
    s = px(16)
    img = Image.new("RGBA", (s, s), TRANS)
    d = ImageDraw.Draw(img)
    t = frame / 5.0
    r = int(px(2) + t * px(5))
    col = (255, int(180 - t * 80), int(80 + t * 40), int(255 - t * 200))
    d.ellipse([s // 2 - r, s // 2 - r, s // 2 + r, s // 2 + r], fill=col)
    if frame % 2 == 0:
        d.line([(s // 2, s // 2 - r), (s // 2, s // 2 + r)], fill=WHITE, width=1)
    save(img, f"spark_f{frame}.png")


def draw_key(name, label):
    w, h = px(24), px(16)
    img = Image.new("RGBA", (w, h), TRANS)
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([0, 0, w - 1, h - 1], radius=px(3), fill=(230, 230, 235), outline=INK, width=1)
    d.rounded_rectangle([px(1), px(1), w - px(2), h - px(2)], radius=px(2), fill=(210, 210, 220))
    # pixel label block
    d.rectangle([px(6), px(5), w - px(7), h - px(6)], fill=(60, 60, 70))
    save(img, f"keys/{name}.png")


def draw_key_hot(name):
    w, h = px(24), px(16)
    img = Image.new("RGBA", (w, h), TRANS)
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([0, 0, w - 1, h - 1], radius=px(3), fill=GREEN, outline=INK, width=2)
    d.rounded_rectangle([px(1), px(1), w - px(2), h - px(2)], radius=px(2), fill=(100, 210, 110))
    save(img, f"keys/{name}_hot.png")


def draw_rod():
    w, h = px(48), px(64)
    img = Image.new("RGBA", (w, h), TRANS)
    d = ImageDraw.Draw(img)
    d.line([(px(4), h - px(4)), (w - px(6), px(6))], fill=(120, 80, 40), width=3)
    d.line([(w - px(8), px(10)), (w - px(8), h - px(20))], fill=(180, 180, 180), width=1)
    d.ellipse([w - px(10), h - px(22), w - px(6), h - px(18)], fill=RED)
    save(img, "rod.png")


def main():
    if os.path.isdir(OUT):
        for root, _, files in os.walk(OUT):
            for f in files:
                if f.endswith(".png"):
                    os.remove(os.path.join(root, f))
    draw_tank()
    draw_bar_outline()
    draw_bar_fill()
    draw_treasure_bar()
    draw_pointer()
    draw_spot("normal", GREEN, YELLOW, (120, 200, 130))
    draw_spot("thin", GREEN, YELLOW, (120, 200, 130))
    draw_spot("treasure", GOLD, (255, 220, 120), (255, 240, 160))
    for i in range(6):
        draw_spark(i)
    draw_key("rmb", "RMB")
    draw_key("a", "A")
    draw_key("d", "D")
    draw_key_hot("rmb")
    draw_key_hot("a")
    draw_key_hot("d")
    draw_rod()
    print("done")


if __name__ == "__main__":
    main()
