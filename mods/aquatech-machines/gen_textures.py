# -*- coding: utf-8 -*-
# Текстуры машин AquaTech: Механизмы — стиль коптильни владельца (тёмная сталь + акценты).
# Блоки: 16x16 native → 64x64 NEAREST. GUI: 256x256, панель 176x166.
from PIL import Image, ImageDraw
import os

OUT = "src/main/resources/assets/aquatech_machines/textures"
os.makedirs(f"{OUT}/block", exist_ok=True)
os.makedirs(f"{OUT}/gui", exist_ok=True)

BLACK = (16, 16, 20, 255)
PANEL = (58, 58, 68, 255)
PANEL_L = (82, 82, 94, 255)
PANEL_D = (40, 40, 48, 255)
FRAME = (100, 100, 112, 255)
HOLE = (26, 26, 32, 255)
GOLD = (240, 190, 60, 255)
GOLD_D = (201, 134, 46, 255)
CYAN = (34, 184, 212, 255)
CYAN_L = (103, 232, 249, 255)
GREEN = (63, 226, 91, 255)
GREEN_L = (160, 255, 180, 255)
GRAY = (170, 170, 175, 255)


def base16():
    im = Image.new("RGBA", (16, 16), PANEL)
    d = ImageDraw.Draw(im)
    d.rectangle([0, 0, 15, 0], fill=PANEL_L)
    d.rectangle([0, 0, 0, 15], fill=PANEL_L)
    d.rectangle([0, 15, 15, 15], fill=PANEL_D)
    d.rectangle([15, 0, 15, 15], fill=PANEL_D)
    d.rectangle([0, 0, 15, 15], outline=BLACK)
    return im, d


def save64(im, name):
    im.resize((64, 64), Image.NEAREST).save(f"{OUT}/block/{name}.png")
    print("block:", name)


def gold_ring(d, x0, y0, x1, y1):
    d.rectangle([x0, y0, x1, y0], fill=GOLD)
    d.rectangle([x0, y1, x1, y1], fill=GOLD_D)
    d.rectangle([x0, y0, x0, y1], fill=GOLD)
    d.rectangle([x1, y0, x1, y1], fill=GOLD_D)


def draw_led(d, x, y, color, light):
    d.rectangle([x, y, x + 3, y + 3], fill=(20, 20, 24, 255))
    d.point([(x + 1, y + 1)], fill=light)


# ================= FISHER (бирюза) =================
im, d = base16()
d.rectangle([5, 5, 10, 10], fill=HOLE)
d.rectangle([4, 4, 11, 11], outline=CYAN)
d.point([(7, 7), (8, 8)], fill=CYAN_L)
save64(im, "fisher_top")

im, d = base16()
d.rectangle([2, 2, 13, 13], outline=(70, 70, 80, 255))
draw_led(d, 6, 11, CYAN, CYAN_L)
save64(im, "fisher_side")

im, d = base16()
gold_ring(d, 1, 1, 14, 14)
d.rectangle([4, 4, 11, 11], fill=HOLE, outline=CYAN)
d.ellipse([5, 5, 10, 10], outline=CYAN_L)
d.line([(12, 3), (11, 6)], fill=GRAY)   # крючок
save64(im, "fisher_front")

im, d = base16()
gold_ring(d, 1, 1, 14, 14)
d.rectangle([4, 4, 11, 11], fill=HOLE, outline=CYAN)
d.ellipse([5, 5, 10, 10], outline=CYAN_L)
d.rectangle([6, 6, 9, 9], fill=CYAN)   # датчик «видит воду»
save64(im, "fisher_front_on")

im, d = base16()
save64(im, "fisher_bottom")

# ================= EXCAVATOR (золото) =================
im, d = base16()
gold_ring(d, 1, 1, 14, 14)
d.rectangle([5, 5, 10, 10], fill=HOLE)
d.line([(5, 5), (10, 10)], fill=GOLD)
d.line([(10, 5), (5, 10)], fill=GOLD_D)
save64(im, "excavator_top")

im, d = base16()
d.rectangle([2, 2, 13, 13], outline=(70, 70, 80, 255))
for y in range(4, 13, 3):
    d.line([(2, y), (13, y)], fill=(64, 64, 74, 255))
save64(im, "excavator_side")

im, d = base16()
d.rectangle([3, 3, 12, 12], fill=HOLE)
d.ellipse([4, 4, 11, 11], outline=GRAY)
d.ellipse([6, 6, 9, 9], outline=GRAY)
d.point([(7, 4), (7, 11), (4, 7), (11, 7)], fill=GOLD)
save64(im, "excavator_front")

im, d = base16()
d.rectangle([3, 3, 12, 12], fill=HOLE)
d.ellipse([4, 4, 11, 11], outline=GOLD)
d.ellipse([6, 6, 9, 9], outline=GOLD_D)
d.point([(7, 5)], fill=(255, 230, 140, 255))
save64(im, "excavator_front_on")

im, d = base16()
save64(im, "excavator_bottom")

# ================= EXTRACTOR (зелёный) =================
im, d = base16()
d.rectangle([4, 4, 11, 11], fill=HOLE)
d.rectangle([5, 5, 10, 10], fill=(30, 60, 34, 255))
d.point([(6, 6), (9, 8)], fill=GREEN)
save64(im, "extractor_top")

im, d = base16()
d.rectangle([2, 2, 13, 13], outline=(70, 70, 80, 255))
d.line([(2, 7), (13, 7)], fill=GREEN)
d.line([(2, 8), (13, 8)], fill=(40, 140, 60, 255))
save64(im, "extractor_side")

im, d = base16()
d.rectangle([4, 4, 11, 11], fill=HOLE)
d.rectangle([5, 5, 10, 10], fill=(30, 60, 34, 255))
d.rectangle([6, 6, 9, 9], fill=(50, 110, 58, 255))
draw_led(d, 11, 12, GREEN, GREEN_L)
save64(im, "extractor_front")

im, d = base16()
d.rectangle([4, 4, 11, 11], fill=HOLE)
d.rectangle([5, 5, 10, 10], fill=(50, 140, 60, 255))
d.rectangle([6, 6, 9, 9], fill=GREEN)
d.point([(6, 6), (9, 9)], fill=GREEN_L)
save64(im, "extractor_front_on")

im, d = base16()
save64(im, "extractor_bottom")

# ================= GUI (256x256, панель 176x166) =================
def gui_panel(title, accent):
    im = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.rectangle([0, 0, 175, 165], fill=PANEL)
    d.rectangle([0, 0, 175, 165], outline=BLACK)
    d.rectangle([1, 1, 174, 164], outline=(70, 70, 80, 255))
    for sx, sy in [(3, 3), (171, 3), (3, 160), (171, 160)]:
        d.rectangle([sx, sy, sx + 1, sy + 1], fill=GRAY)
    # плашка титула
    d.rectangle([34, 3, 142, 14], fill=(24, 24, 30, 255), outline=BLACK)
    d.rectangle([35, 4, 141, 13], outline=accent)
    # тонкая линия под титулом
    d.line([(6, 18), (169, 18)], fill=(52, 52, 62, 255))
    # слоты игрока
    for row in range(3):
        for col in range(9):
            draw_slot(d, 7 + col * 18, 83 + row * 18)
    for col in range(9):
        draw_slot(d, 7 + col * 18, 141)
    return im, d


def draw_slot(d, x, y):
    d.rectangle([x, y, x + 17, y + 17], fill=(20, 19, 26, 255), outline=(24, 23, 30, 255))
    d.rectangle([x + 1, y + 1, x + 16, y + 16], outline=(64, 63, 74, 255))


def arrow(d, x, y, color, fill=0):
    # фон стрелки 24x17
    d.rectangle([x, y + 5, x + 14, y + 11], outline=(52, 52, 62, 255))
    d.polygon([(x + 14, y), (x + 23, y + 8), (x + 14, y + 16)], outline=(52, 52, 62, 255))
    # заливка
    if fill > 0:
        w = min(fill, 24)
        if w <= 14:
            d.rectangle([x, y + 5, x + w, y + 11], fill=color)
        else:
            d.rectangle([x, y + 5, x + 14, y + 11], fill=color)
            k = (w - 14) / 9.0
            d.polygon([(x + 14, y + 8 - 8 * k), (x + 14 + 9 * k, y + 8), (x + 14, y + 8 + 8 * k)], fill=color)


def energy_bar(d, x, y):
    d.rectangle([x - 1, y - 1, x + 12, y + 50], outline=BLACK)
    d.rectangle([x, y, x + 11, y + 49], fill=HOLE)


def gui_progress_strip(im, color):
    # 24x17 полная стрелка для блита прогресса (176,52)
    d = ImageDraw.Draw(im)
    d.rectangle([176, 52, 176 + 14, 52 + 6], fill=color)
    d.polygon([(176 + 14, 52), (176 + 23, 52 + 8), (176 + 14, 52 + 16)], fill=color)


def gui_energy_strip(im, color, light):
    d = ImageDraw.Draw(im)
    d.rectangle([176, 0, 176 + 11, 49], fill=color)
    for yy in range(4, 50, 8):
        d.line([(176, yy), (176 + 11, yy)], fill=light)


def slot(im, x, y):
    d = ImageDraw.Draw(im)
    draw_slot(d, x - 1, y - 1)


# --- FISHER GUI ---
im, d = gui_panel("РЫБОЛОВ MK-2", CYAN)
slot(im, 44, 17)   # удочка
slot(im, 116, 35)  # выход
arrow(d, 79, 34, (60, 60, 70, 255))
energy_bar(d, 8, 18)
# подписи-иконки (упрощённо)
d.rectangle([40, 13, 48, 14], fill=CYAN)
d.rectangle([112, 31, 120, 32], fill=GOLD)
gui_progress_strip(im, CYAN)
gui_energy_strip(im, CYAN, CYAN_L)
im.save(f"{OUT}/gui/fisher.png")

# --- EXCAVATOR GUI ---
im, d = gui_panel("ЭКСКАВАТОР", GOLD)
for row in range(3):
    for col in range(3):
        slot(im, 79 + col * 18, 17 + row * 18)
arrow(d, 52, 35, (60, 60, 70, 255))
energy_bar(d, 8, 18)
gui_progress_strip(im, GOLD)
gui_energy_strip(im, GOLD, (255, 230, 140, 255))
im.save(f"{OUT}/gui/excavator.png")

# --- EXTRACTOR GUI ---
im, d = gui_panel("ЭКСТРАКТОР", GREEN)
slot(im, 44, 17)
slot(im, 116, 35)
arrow(d, 79, 34, (60, 60, 70, 255))
energy_bar(d, 8, 18)
d.rectangle([40, 13, 48, 14], fill=GREEN)
gui_progress_strip(im, GREEN)
gui_energy_strip(im, GREEN, GREEN_L)
im.save(f"{OUT}/gui/extractor.png")

print("textures done")
