# -*- coding: utf-8 -*-
from pathlib import Path
from PIL import Image, ImageDraw

OUT = Path(__file__).resolve().parent / "src/main/resources/assets/aquatech_machines/textures/gui"
OUT.mkdir(parents=True, exist_ok=True)

# LuminousUI Dark Tech Palette
BG_DARK      = (14, 16, 22, 255)
BG_PANEL     = (34, 36, 46, 255)
BG_PANEL_HI  = (52, 56, 70, 255)
BG_PANEL_LOW = (22, 24, 32, 255)
BORDER_DARK  = (10, 12, 16, 255)
BORDER_HI    = (70, 74, 92, 255)

SLOT_BG      = (18, 20, 26, 255)
SLOT_INNER_T = (12, 14, 18, 255)
SLOT_BORDER  = (54, 58, 72, 255)

CYAN_CORE    = (0, 229, 255, 255)
CYAN_HI      = (180, 245, 255, 255)
CYAN_LOW     = (0, 150, 180, 255)
CYAN_GLOW    = (230, 252, 255, 255)

AMBER_CORE   = (255, 179, 0, 255)
AMBER_HI     = (255, 230, 140, 255)
AMBER_LOW    = (200, 110, 0, 255)
AMBER_GLOW   = (255, 248, 210, 255)

GREEN_CORE   = (0, 230, 118, 255)
GREEN_HI     = (185, 246, 202, 255)
GREEN_LOW    = (0, 160, 75, 255)
GREEN_GLOW   = (230, 255, 235, 255)

RED_CORE     = (255, 60, 50, 255)
RED_HI       = (255, 180, 170, 255)
RED_LOW      = (160, 20, 15, 255)
RED_GLOW     = (255, 230, 230, 255)


def draw_slot(d, x, y, accent=None):
    # Standard clean 18x18 slot with inset bevel
    d.rectangle([x, y, x + 17, y + 17], fill=SLOT_BG, outline=BORDER_DARK)
    d.line([(x + 1, y + 1), (x + 16, y + 1)], fill=SLOT_INNER_T)
    d.line([(x + 1, y + 1), (x + 1, y + 16)], fill=SLOT_INNER_T)
    d.line([(x + 1, y + 16), (x + 16, y + 16)], fill=SLOT_BORDER)
    d.line([(x + 16, y + 1), (x + 16, y + 16)], fill=SLOT_BORDER)
    if accent:
        d.point([(x + 15, y + 2), (x + 14, y + 2)], fill=accent)


def draw_arrow_track(d, x, y):
    # Hydro-Capsule Arrow Track (Empty state)
    d.rectangle([x, y + 4, x + 14, y + 12], fill=(16, 22, 30, 255), outline=(10, 14, 20, 255))
    head_poly = [(x + 14, y), (x + 23, y + 8), (x + 14, y + 16)]
    d.polygon(head_poly, fill=(16, 22, 30, 255), outline=(10, 14, 20, 255))
    
    # Metal collar reinforcement rings at x+6 and x+13
    d.line([(x + 6, y + 3), (x + 6, y + 13)], fill=(45, 62, 80, 255))
    d.line([(x + 13, y + 3), (x + 13, y + 13)], fill=(45, 62, 80, 255))
    
    # Specular gloss
    d.line([(x + 1, y + 5), (x + 13, y + 5)], fill=(32, 45, 60, 255))
    d.line([(x + 14, y + 2), (x + 20, y + 7)], fill=(32, 45, 60, 255))
    
    # Shadow
    d.line([(x + 1, y + 11), (x + 14, y + 11)], fill=(12, 16, 22, 255))
    d.line([(x + 14, y + 15), (x + 21, y + 9)], fill=(12, 16, 22, 255))


def draw_arrow_strip(im, core_col=None, hi_col=None, low_col=None, glow_col=None):
    # Filled fluid progress arrow at (176, 52)
    ox, oy = 176, 52
    d = ImageDraw.Draw(im)
    
    shaft_rect = [ox, oy + 4, ox + 14, oy + 12]
    d.rectangle(shaft_rect, fill=core_col or (0, 190, 215, 255))
    head_poly = [(ox + 14, oy), (ox + 23, oy + 8), (ox + 14, oy + 16)]
    d.polygon(head_poly, fill=core_col or (0, 190, 215, 255))
    
    # Depth gradient
    d.line([(ox, oy + 11), (ox + 14, oy + 11)], fill=low_col or (0, 120, 160, 255))
    d.line([(ox, oy + 12), (ox + 14, oy + 12)], fill=BORDER_DARK)
    d.line([(ox + 14, oy + 15), (ox + 22, oy + 8)], fill=low_col or (0, 120, 160, 255))
    d.line([(ox + 14, oy + 16), (ox + 23, oy + 8)], fill=BORDER_DARK)
    
    # Specular
    d.line([(ox, oy + 4), (ox + 14, oy + 4)], fill=hi_col or (130, 240, 245, 255))
    d.line([(ox + 1, oy + 5), (ox + 13, oy + 5)], fill=glow_col or (185, 255, 255, 255))
    d.line([(ox + 14, oy), (ox + 23, oy + 8)], fill=hi_col or (130, 240, 245, 255))
    d.line([(ox + 14, oy + 1), (ox + 21, oy + 7)], fill=glow_col or (185, 255, 255, 255))
    
    # Metal rings
    for rx in [ox + 6, ox + 13]:
        d.line([(rx, oy + 3), (rx, oy + 13)], fill=(135, 205, 225, 255))
        d.point([(rx, oy + 3), (rx, oy + 4)], fill=(215, 250, 255, 255))
        d.point([(rx, oy + 12), (rx, oy + 13)], fill=(40, 80, 105, 255))


def draw_energy_gauge(d, x, y):
    # 12x50 Energy gauge frame in GUI
    d.rectangle([x - 1, y - 1, x + 12, y + 50], outline=BORDER_DARK, fill=BG_DARK)
    d.rectangle([x, y, x + 11, y + 49], fill=(16, 18, 24, 255))
    for gy in range(y + 9, y + 50, 10):
        d.line([(x + 1, gy), (x + 4, gy)], fill=(44, 48, 62, 255))
        d.line([(x + 7, gy), (x + 10, gy)], fill=(44, 48, 62, 255))


def draw_energy_strip(im, core_col, hi_col, low_col, glow_col):
    # 12x50 filled energy gauge at UV (176, 0)
    ox, oy = 176, 0
    d = ImageDraw.Draw(im)
    
    d.rectangle([ox, oy, ox + 11, oy + 49], fill=low_col)
    d.rectangle([ox + 2, oy + 1, ox + 9, oy + 48], fill=core_col)
    d.line([(ox + 1, oy + 1), (ox + 1, oy + 48)], fill=hi_col)
    d.line([(ox + 2, oy + 1), (ox + 2, oy + 48)], fill=glow_col)
    d.line([(ox + 10, oy + 1), (ox + 10, oy + 48)], fill=low_col)
    
    for sy in range(oy + 4, oy + 50, 5):
        d.line([(ox, sy), (ox + 11, sy)], fill=BORDER_DARK)


def draw_tank_gauge(d, x, y, tint_bg=(16, 18, 24, 255), mark_col=(50, 60, 80, 255)):
    # 12x50 Fluid tank frame
    d.rectangle([x - 1, y - 1, x + 12, y + 50], outline=BORDER_DARK, fill=BG_DARK)
    d.rectangle([x, y, x + 11, y + 49], fill=tint_bg)
    for gy in range(y + 9, y + 50, 10):
        d.line([(x + 1, gy), (x + 3, gy)], fill=mark_col)
        d.line([(x + 8, gy), (x + 10, gy)], fill=mark_col)
    d.line([(x + 1, y + 1), (x + 1, y + 48)], fill=(90, 110, 140, 120))


def draw_upgrade_wing(d, u_ox=0, u_oy=166):
    """
    Draws the attached 34x73 Upgrade Bay at UV (u_ox, u_oy).
    Slots are completely clean and empty (no watermark symbols inside).
    """
    w, h = 34, 73
    x1, y1 = u_ox, u_oy
    x2, y2 = u_ox + w - 1, u_oy + h - 1

    # Chamfered panel polygon docking against main chassis
    poly = [
        (x1, y1),
        (x2 - 4, y1),
        (x2, y1 + 4),
        (x2, y2 - 4),
        (x2 - 4, y2),
        (x1, y2)
    ]
    d.polygon(poly, fill=BG_PANEL, outline=BORDER_DARK)

    # Highlight bevel lines
    d.line([(x1, y1 + 1), (x2 - 5, y1 + 1)], fill=BORDER_HI)
    d.line([(x2 - 4, y1 + 1), (x2 - 1, y1 + 4)], fill=BORDER_HI)
    d.line([(x2 - 1, y1 + 4), (x2 - 1, y2 - 5)], fill=BORDER_HI)
    d.line([(x1, y2 - 1), (x2 - 5, y2 - 1)], fill=BG_PANEL_LOW)
    d.line([(x2 - 4, y2 - 1), (x2 - 1, y2 - 4)], fill=BG_PANEL_LOW)

    # Header accent notch
    d.rectangle([x1 + 4, y1 + 2, x2 - 6, y1 + 4], fill=BG_DARK)
    d.point([(x1 + 14, y1 + 3), (x1 + 15, y1 + 3)], fill=(140, 145, 160, 255))

    # 3 Completely Clean Empty Slots (no watermark icons)
    s1_x, s1_y = u_ox + 9, u_oy + 6
    draw_slot(d, s1_x, s1_y)

    s2_x, s2_y = u_ox + 9, u_oy + 28
    draw_slot(d, s2_x, s2_y)

    s3_x, s3_y = u_ox + 9, u_oy + 50
    draw_slot(d, s3_x, s3_y)


def base_gui():
    """
    Creates a clean, uncluttered 256x256 GUI canvas:
    - Main chassis 176x166 with clean single border
    - Standard player inventory at (7, 83) and hotbar at (7, 141)
    - Energy gauge at (8, 20)
    """
    im = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    
    # Main panel 176x166 - clean dark industrial chassis
    d.rectangle([0, 0, 175, 165], fill=BG_PANEL, outline=BORDER_DARK)
    d.rectangle([1, 1, 174, 164], outline=BORDER_HI)
    
    # Player Inventory (3x9 at 7, 83)
    for row in range(3):
        for col in range(9):
            draw_slot(d, 7 + col * 18, 83 + row * 18)
            
    # Hotbar (1x9 at 7, 141)
    for col in range(9):
        draw_slot(d, 7 + col * 18, 141)
        
    # Energy gauge at (8, 20)
    draw_energy_gauge(d, 8, 20)
    
    return im, d


# ================= 1. FISHER GUI =================
im_f, d_f = base_gui()
# Rod at (44, 24), Core at (44, 48)
draw_slot(d_f, 44, 24)
draw_slot(d_f, 44, 48)
# Progress arrow at (74, 36)
draw_arrow_track(d_f, 74, 36)
# Output at (114, 34)
draw_slot(d_f, 114, 34)
# Upgrade Wing at UV (0, 166)
draw_upgrade_wing(d_f, u_ox=0, u_oy=166)
draw_arrow_strip(im_f, CYAN_CORE, CYAN_HI, CYAN_LOW, CYAN_GLOW)
draw_energy_strip(im_f, CYAN_CORE, CYAN_HI, CYAN_LOW, CYAN_GLOW)
im_f.save(f"{OUT}/fisher.png")
print("Saved clean fisher.png")


# ================= 2. EXCAVATOR GUI =================
im_e, d_e = base_gui()
# Centered progress arrow at (48, 36)
draw_arrow_track(d_e, 48, 36)
# Clean 3x3 AE2 Output Grid centered at x=80, 98, 116; y=17, 35, 53
for r in range(3):
    for c in range(3):
        draw_slot(d_e, 80 + c * 18, 17 + r * 18)
# Upgrade Wing at UV (0, 166)
draw_upgrade_wing(d_e, u_ox=0, u_oy=166)
draw_arrow_strip(im_e, AMBER_CORE, AMBER_HI, AMBER_LOW, AMBER_GLOW)
draw_energy_strip(im_e, AMBER_CORE, AMBER_HI, AMBER_LOW, AMBER_GLOW)
im_e.save(f"{OUT}/excavator.png")
print("Saved clean excavator.png")


# ================= 3. EXTRACTOR GUI =================
im_x, d_x = base_gui()
# Raw Input at (44, 36)
draw_slot(d_x, 44, 36)
# Arrow at (74, 36)
draw_arrow_track(d_x, 74, 36)
# Output at (114, 34)
draw_slot(d_x, 114, 34)
# Upgrade Wing at UV (0, 166)
draw_upgrade_wing(d_x, u_ox=0, u_oy=166)
draw_arrow_strip(im_x, GREEN_CORE, GREEN_HI, GREEN_LOW, GREEN_GLOW)
draw_energy_strip(im_x, GREEN_CORE, GREEN_HI, GREEN_LOW, GREEN_GLOW)
im_x.save(f"{OUT}/extractor.png")
print("Saved clean extractor.png")


# ================= 4. SYNTHESIZER GUI =================
im_s, d_s = base_gui()
# Lava Tank (25, 22) + fluid in/out
draw_tank_gauge(d_s, 25, 22, tint_bg=(28, 14, 12, 255), mark_col=(90, 40, 30, 255))
draw_slot(d_s, 43, 24)
draw_slot(d_s, 43, 50)

# Reactor Chamber inputs
draw_slot(d_s, 68, 24)
draw_slot(d_s, 68, 50)

# Progress arrow at (92, 37)
draw_arrow_track(d_s, 92, 37)

# Output Bay
draw_slot(d_s, 126, 24)
draw_slot(d_s, 126, 50)
# 3rd Output (bonus slot)
draw_slot(d_s, 150, 37)

# Upgrade Wing
draw_upgrade_wing(d_s, u_ox=0, u_oy=166)
draw_arrow_strip(im_s, RED_CORE, RED_HI, RED_LOW, RED_GLOW)
draw_energy_strip(im_s, RED_CORE, RED_HI, RED_LOW, RED_GLOW)
im_s.save(f"{OUT}/synthesizer.png")
print("Saved clean synthesizer.png")


# ================= 5. CENTRIFUGE GUI =================
im_c, d_c = base_gui()
# Raw Water Tank (24, 22) + in/out slots
draw_tank_gauge(d_c, 24, 22, tint_bg=(14, 22, 34, 255), mark_col=(35, 65, 100, 255))
draw_slot(d_c, 41, 24)
draw_slot(d_c, 41, 50)

# Rotor progress arrow at (65, 37)
draw_arrow_track(d_c, 65, 37)

# 2x2 Mineral Grid
draw_slot(d_c, 96, 26)
draw_slot(d_c, 115, 26)
draw_slot(d_c, 96, 48)
draw_slot(d_c, 115, 48)

# Distillate Tank (138, 22) + empty/full slots
draw_tank_gauge(d_c, 138, 22, tint_bg=(10, 26, 34, 255), mark_col=(30, 80, 100, 255))
draw_slot(d_c, 155, 24)
draw_slot(d_c, 155, 50)

# Upgrade Wing
draw_upgrade_wing(d_c, u_ox=0, u_oy=166)
draw_arrow_strip(im_c, CYAN_CORE, CYAN_HI, CYAN_LOW, CYAN_GLOW)
draw_energy_strip(im_c, CYAN_CORE, CYAN_HI, CYAN_LOW, CYAN_GLOW)
im_c.save(f"{OUT}/centrifuge.png")
print("Saved clean centrifuge.png")

print("All 5 machine GUIs successfully regenerated with clean aesthetics!")
