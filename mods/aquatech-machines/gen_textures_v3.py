# -*- coding: utf-8 -*-
# Текстуры машин v3 — единый стиль (тёмный корпус, болты, рамка),
# но у каждой машины СВОЁ лицо: разные фронты и детали на сторонах.
from PIL import Image, ImageDraw

OUT = "src/main/resources/assets/aquatech_machines/textures"

BODY    = (52, 54, 64, 255)
BODY_L  = (74, 76, 88, 255)
BODY_D  = (36, 38, 46, 255)
PANEL   = (28, 30, 38, 255)
PANEL_D = (20, 21, 27, 255)
FRAME   = (120, 122, 134, 255)
FRAME_D = (78, 80, 92, 255)
BOLT    = (150, 152, 164, 255)
BLACK   = (14, 14, 18, 255)
CYAN    = (64, 210, 235, 255)
CYAN_L  = (140, 240, 255, 255)
AMBER   = (255, 176, 64, 255)
AMBER_L = (255, 220, 130, 255)
AMBER_D = (160, 100, 30, 255)
GREEN   = (80, 230, 110, 255)
GREEN_L = (170, 255, 190, 255)
STEEL   = (140, 142, 154, 255)
STEEL_D = (100, 102, 114, 255)

ACC = {"fisher": (CYAN, CYAN_L), "excavator": (AMBER, AMBER_L), "extractor": (GREEN, GREEN_L)}


def base():
    im = Image.new("RGBA", (16, 16), BODY)
    d = ImageDraw.Draw(im)
    for x, y in [(0, 0), (15, 0), (0, 15), (15, 15)]:
        d.point([(x, y)], fill=BLACK)
    for x, y in [(2, 2), (13, 2), (2, 13), (13, 13)]:
        d.point([(x, y)], fill=BOLT)
    d.rectangle([0, 0, 15, 0], fill=BLACK)
    d.rectangle([0, 15, 15, 15], fill=BLACK)
    d.rectangle([0, 0, 0, 15], fill=BLACK)
    d.rectangle([15, 0, 15, 15], fill=BLACK)
    return im, d


def save64(im, name):
    im.resize((64, 64), Image.NEAREST).save(f"{OUT}/block/{name}.png")
    print("  ", name)


# ================= FISHER — лицо: аквариум-окно с рыбкой и крючком =================
# TOP: кольцо-люк с водой (забор воды для машины)
im, d = base()
d.rectangle([3, 3, 12, 12], fill=PANEL, outline=BODY_D)
d.rectangle([4, 4, 11, 11], fill=(24, 60, 90, 255), outline=FRAME_D)
d.ellipse([5, 5, 10, 10], outline=CYAN)
d.point([(7, 7), (8, 8)], fill=CYAN_L)
save64(im, "fisher_top")

# SIDE: бок с вертикальной трубой-водоводом
im, d = base()
d.rectangle([3, 2, 6, 13], fill=STEEL_D, outline=BLACK)   # труба
d.rectangle([9, 3, 12, 12], fill=PANEL, outline=BODY_D)
for y in (5, 8, 11):
    d.point([(10, y), (11, y)], fill=BODY_D)
save64(im, "fisher_side")

# FRONT OFF: окно-аквариум тёмное, крючок, LED
im, d = base()
d.rectangle([3, 3, 12, 12], fill=PANEL, outline=BODY_D)
d.rectangle([4, 4, 11, 10], fill=(16, 24, 34, 255), outline=FRAME_D)
d.line([(12, 2), (11, 5)], fill=STEEL)          # крючок сверху
d.point([(12, 12)], fill=(60, 62, 72, 255))     # LED off
save64(im, "fisher_front")

# FRONT ON: аквариум голубой, рыбка видна, LED горит
im, d = base()
d.rectangle([3, 3, 12, 12], fill=PANEL, outline=BODY_D)
d.rectangle([4, 4, 11, 10], fill=(30, 90, 130, 255), outline=CYAN)
d.ellipse([5, 6, 8, 9], fill=(255, 150, 90, 255))       # рыбка
d.point([(9, 6)], fill=(30, 30, 40, 255))               # глаз
d.line([(9, 7), (10, 8)], fill=(255, 120, 70, 255))     # хвост
d.rectangle([12, 12, 13, 12], fill=CYAN_L)              # LED on
save64(im, "fisher_front_on")

# BOTTOM: плита с дренажом
im, d = base()
d.rectangle([3, 3, 12, 12], fill=BODY_D, outline=BLACK)
d.point([(5, 5), (10, 5), (5, 10), (10, 10)], fill=BLACK)
save64(im, "fisher_bottom")

# ================= EXCAVATOR — лицо: роторный ковш с зубьями =================
# TOP: привод с вращающейся шестерней
im, d = base()
d.rectangle([3, 3, 12, 12], fill=PANEL, outline=BODY_D)
d.ellipse([4, 4, 11, 11], outline=FRAME)
d.ellipse([6, 6, 9, 9], fill=BLACK, outline=FRAME_D)
for a in [(7, 3), (7, 12), (3, 7), (12, 7)]:
    d.point([a], fill=AMBER)
save64(im, "excavator_top")

# SIDE: бок с буровой колонной
im, d = base()
d.rectangle([6, 2, 9, 13], fill=STEEL_D, outline=BLACK)
for y in range(3, 13, 3):
    d.line([(6, y), (9, y)], fill=BOLT)
d.rectangle([3, 3, 5, 12], fill=PANEL, outline=BODY_D)
d.rectangle([10, 3, 12, 12], fill=PANEL, outline=BODY_D)
save64(im, "excavator_side")

# FRONT OFF: ковш-ротор тёмный, LED off
im, d = base()
d.rectangle([3, 3, 12, 12], fill=PANEL, outline=BODY_D)
d.ellipse([4, 4, 11, 11], fill=(24, 26, 34, 255), outline=FRAME_D)
d.ellipse([6, 6, 9, 9], fill=BLACK, outline=FRAME_D)
d.point([(7, 4), (7, 11), (4, 7), (11, 7)], fill=AMBER_D)  # зубья тусклые
save64(im, "excavator_front")

# FRONT ON: ковш светится янтарём, зубья видны
im, d = base()
d.rectangle([3, 3, 12, 12], fill=PANEL, outline=BODY_D)
d.ellipse([4, 4, 11, 11], fill=(30, 28, 20, 255), outline=AMBER)
d.ellipse([6, 6, 9, 9], fill=BLACK, outline=AMBER_D)
d.point([(7, 4), (7, 11), (4, 7), (11, 7)], fill=AMBER)
d.point([(12, 12), (13, 12)], fill=AMBER_L)   # LED on
save64(im, "excavator_front_on")

# BOTTOM: усиленная плита
im, d = base()
d.rectangle([2, 2, 13, 13], fill=BODY_D, outline=BLACK)
d.line([(2, 2), (13, 13)], fill=BLACK)
d.line([(13, 2), (2, 13)], fill=BLACK)
save64(im, "excavator_bottom")

# ================= EXTRACTOR — лицо: колба с трубами и зелёной жидкостью =================
# TOP: заливная горловина
im, d = base()
d.rectangle([4, 4, 11, 11], fill=PANEL, outline=BODY_D)
d.ellipse([5, 5, 10, 10], fill=(30, 60, 40, 255), outline=FRAME_D)
d.ellipse([6, 6, 9, 9], fill=(50, 140, 70, 255))
save64(im, "extractor_top")

# SIDE: труба с манометром
im, d = base()
d.rectangle([2, 3, 6, 12], fill=STEEL_D, outline=BLACK)
d.ellipse([3, 4, 5, 6], fill=(60, 62, 74, 255), outline=BLACK)  # манометр
d.rectangle([9, 2, 13, 13], fill=PANEL, outline=BODY_D)
for y in (4, 7, 10):
    d.line([(10, y), (12, y)], fill=(44, 46, 56, 255))
save64(im, "extractor_side")

# FRONT OFF: колба пустая, LED off
im, d = base()
d.rectangle([3, 3, 12, 12], fill=PANEL, outline=BODY_D)
d.rectangle([5, 3, 10, 12], fill=(20, 24, 30, 255), outline=FRAME_D)  # колба
d.rectangle([6, 5, 9, 11], fill=(16, 20, 26, 255))
d.point([(12, 12)], fill=(60, 62, 72, 255))
save64(im, "extractor_front")

# FRONT ON: колба с зелёной жидкостью и пузырьками, LED on
im, d = base()
d.rectangle([3, 3, 12, 12], fill=PANEL, outline=BODY_D)
d.rectangle([5, 3, 10, 12], fill=(18, 22, 28, 255), outline=GREEN)
d.rectangle([6, 7, 9, 11], fill=(50, 160, 80, 255))   # жидкость
d.point([(7, 6), (8, 8)], fill=GREEN_L)               # пузырьки
d.rectangle([12, 12, 13, 12], fill=GREEN_L)           # LED on
save64(im, "extractor_front_on")

# BOTTOM: плита с патрубком
im, d = base()
d.rectangle([3, 3, 12, 12], fill=BODY_D, outline=BLACK)
d.rectangle([6, 6, 9, 9], fill=BLACK, outline=BODY_D)
save64(im, "extractor_bottom")

print("textures v3 done — единый корпус, у каждой машины своё лицо")
