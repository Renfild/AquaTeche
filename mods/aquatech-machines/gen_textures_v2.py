# -*- coding: utf-8 -*-
# Текстуры машин v2 — стиль LoliLand/HiTech: тёмный корпус, скошенные углы,
# болты по углам, центральная панель-экран, LED-индикатор. 16x16 -> 64x64 NEAREST.
from PIL import Image, ImageDraw
import os

OUT = "src/main/resources/assets/aquatech_machines/textures"
os.makedirs(f"{OUT}/block", exist_ok=True)
os.makedirs(f"{OUT}/gui", exist_ok=True)

# Палитра LoliLand-машин: тёмно-графитовый корпус, светлая сталь, LED
BODY    = (52, 54, 64, 255)     # графит
BODY_L  = (72, 74, 86, 255)     # светлый скос
BODY_D  = (36, 38, 46, 255)     # тёмный скол
PANEL   = (28, 30, 38, 255)     # экран/ниша
FRAME   = (120, 122, 134, 255)  # стальная рамка
FRAME_D = (78, 80, 92, 255)
BOLT    = (150, 152, 164, 255)
BLACK   = (14, 14, 18, 255)
AMBER   = (255, 176, 64, 255)
AMBER_D = (180, 110, 30, 255)
CYAN    = (64, 210, 235, 255)
CYAN_L  = (140, 240, 255, 255)
GREEN   = (80, 230, 110, 255)
GREEN_L = (170, 255, 190, 255)

ACCENT = {"fisher": CYAN, "excavator": AMBER, "extractor": GREEN}
ACCENT_L = {"fisher": CYAN_L, "excavator": (255, 220, 130, 255), "extractor": GREEN_L}


def body_base():
    im = Image.new("RGBA", (16, 16), BODY)
    d = ImageDraw.Draw(im)
    # скошенные углы
    for x, y in [(0, 0), (15, 0), (0, 15), (15, 15)]:
        d.point([(x, y)], fill=BLACK)
    # болты по углам
    for x, y in [(2, 2), (13, 2), (2, 13), (13, 13)]:
        d.point([(x, y)], fill=BOLT)
    # чёрная обводка
    d.rectangle([0, 0, 15, 0], fill=BLACK)
    d.rectangle([0, 15, 15, 15], fill=BLACK)
    d.rectangle([0, 0, 0, 15], fill=BLACK)
    d.rectangle([15, 0, 15, 15], fill=BLACK)
    return im, d


def save64(im, name):
    im.resize((64, 64), Image.NEAREST).save(f"{OUT}/block/{name}.png")
    print("  block:", name)


for m in ["fisher", "excavator", "extractor"]:
    acc = ACCENT[m]
    acc_l = ACCENT_L[m]

    # --- TOP: решётка + болты ---
    im, d = body_base()
    d.rectangle([3, 3, 12, 12], fill=PANEL, outline=BODY_D)
    for y in (5, 8, 11):
        d.line([(4, y), (11, y)], fill=BODY_D)
    save64(im, f"{m}_top")

    # --- SIDE: панель с вентиляцией ---
    im, d = body_base()
    d.rectangle([3, 3, 12, 12], fill=PANEL, outline=BODY_D)
    for y in (5, 7, 9, 11):
        d.line([(4, y), (11, y)], fill=(44, 46, 56, 255))
    save64(im, f"{m}_side")

    # --- FRONT: экран + LED (off) ---
    im, d = body_base()
    d.rectangle([3, 3, 12, 12], fill=PANEL, outline=BODY_D)
    d.rectangle([4, 4, 11, 9], fill=(20, 22, 30, 255), outline=(60, 62, 72, 255))
    d.point([(10, 12)], fill=(60, 62, 72, 255))  # LED off
    save64(im, f"{m}_front")

    # --- FRONT ON: экран светится + LED горит ---
    im, d = body_base()
    d.rectangle([3, 3, 12, 12], fill=PANEL, outline=BODY_D)
    d.rectangle([4, 4, 11, 9], fill=(20, 26, 30, 255), outline=acc)
    d.rectangle([5, 5, 10, 8], fill=(30, 40, 44, 255))
    d.line([(6, 6), (9, 7)], fill=acc_l)
    d.point([(10, 12)], fill=acc_l)
    d.rectangle([9, 12, 11, 12], fill=acc)
    save64(im, f"{m}_front_on")

    # --- BOTTOM: плита ---
    im, d = body_base()
    d.rectangle([3, 3, 12, 12], fill=BODY_D)
    save64(im, f"{m}_bottom")

# ================= GUI v2 — тёмная сталь HiTech =================
def draw_slot(d, x, y):
    d.rectangle([x, y, x + 17, y + 17], fill=(18, 19, 24, 255), outline=BLACK)
    d.rectangle([x + 1, y + 1, x + 16, y + 16], outline=(70, 72, 84, 255))


def gui_panel(title_accent):
    im = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.rectangle([0, 0, 175, 165], fill=(46, 48, 58, 255), outline=BLACK)
    d.rectangle([1, 1, 174, 164], outline=(76, 78, 90, 255))
    # болты
    for sx, sy in [(3, 3), (171, 3), (3, 160), (171, 160)]:
        d.rectangle([sx, sy, sx + 1, sy + 1], fill=BOLT)
    # плашка титула
    d.rectangle([30, 2, 146, 13], fill=(18, 19, 24, 255), outline=BLACK)
    d.rectangle([31, 3, 145, 12], outline=title_accent)
    # разделитель машины и инвентаря
    d.line([(4, 80), (171, 80)], fill=BLACK)
    d.line([(4, 81), (171, 81)], fill=(76, 78, 90, 255))
    # слоты игрока
    for row in range(3):
        for col in range(9):
            draw_slot(d, 7 + col * 18, 83 + row * 18)
    for col in range(9):
        draw_slot(d, 7 + col * 18, 141)
    return im, d


def arrow_base(d, x, y):
    d.rectangle([x, y + 5, x + 14, y + 11], outline=(70, 72, 84, 255))
    d.polygon([(x + 14, y), (x + 23, y + 8), (x + 14, y + 16)], outline=(70, 72, 84, 255))


def energy_well(d, x, y):
    d.rectangle([x - 1, y - 1, x + 12, y + 50], outline=BLACK)
    d.rectangle([x, y, x + 11, y + 49], fill=(18, 19, 24, 255))


def gui_strips(im, accent, accent_light):
    d = ImageDraw.Draw(im)
    # стрелка strip (176,52) 24x17
    d.rectangle([176, 57, 176 + 14, 52 + 11], fill=accent)
    d.polygon([(176 + 14, 52), (176 + 23, 52 + 8), (176 + 14, 52 + 16)], fill=accent)
    # энергия strip (176,0) 12x50 — светлая заливка снизу, тонкие деления
    d.rectangle([176, 0, 176 + 11, 49], fill=accent)
    for yy in range(6, 50, 10):
        d.line([(176, yy), (176 + 11, yy)], fill=BLACK)


for m in ["fisher", "excavator", "extractor"]:
    acc = ACCENT[m]
    acc_l = ACCENT_L[m]
    im, d = gui_panel(acc)

    if m == "fisher":
        # удочка (44,17), апгрейд (44,53), выход (116,35), стрелка (79,34), энергия (8,18)
        draw_slot(d, 43, 16)
        draw_slot(d, 43, 52)
        draw_slot(d, 115, 34)
        arrow_base(d, 79, 34)
        energy_well(d, 8, 18)
    elif m == "excavator":
        # 3x3 выход (79..115, 17..53), стрелка (52,35), энергия (8,18)
        for row in range(3):
            for col in range(3):
                draw_slot(d, 78 + col * 18, 16 + row * 18)
        arrow_base(d, 52, 35)
        energy_well(d, 8, 18)
    else:
        # вход (44,17), выход (116,35), стрелка (79,34), энергия (8,18)
        draw_slot(d, 43, 16)
        draw_slot(d, 115, 34)
        arrow_base(d, 79, 34)
        energy_well(d, 8, 18)

    gui_strips(im, acc, acc_l)
    im.save(f"{OUT}/gui/{m}.png")
    print("  gui:", m)

print("textures v2 done")
