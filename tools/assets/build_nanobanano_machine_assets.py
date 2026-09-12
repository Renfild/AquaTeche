# -*- coding: utf-8 -*-
"""Process NanoBanana generated art into 64x64 block textures and 256x256 GUI atlases.

Strictly follows Minecraft 1.20.1 Forge coordinate contracts:
- FishSmokerMenu: input (56,17), fuel (56,53), output (116,35), inv (8,84), hotbar (8,142)
  overlays: flame at (57,37) from uv (176,70), arrow at (79,34) from uv (176,52)
- AutoFisherMenu: rod (16,29), upgrade (16,48), out 3x2 at (101,30)/(101,47), inv (8,84), hotbar (8,142)
  overlays: energy bar at (37,21) from uv (182,2), arrow at (70,40) from uv (180,61)
"""
import os
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(r"d:\AquaTech")
BRAIN = Path(r"C:\Users\xieto\.gemini\antigravity-ide\brain\e8997355-62d0-4496-aa07-dc7f04f75423")
BLOCK_MACHINES_DIR = ROOT / "mods/aquatech-ui/src/main/resources/assets/aquatech_ui/textures/block/machines"
BLOCK_DIR = ROOT / "mods/aquatech-ui/src/main/resources/assets/aquatech_ui/textures/block"
GUI_DIR = ROOT / "mods/aquatech-ui/src/main/resources/assets/aquatech_ui/textures/gui"

SMOKER_RAW = BRAIN / "smoker_block_face_1789068157502.jpg"
FISHER_RAW = BRAIN / "fisher_block_face_1789068177728.jpg"

BLOCK_MACHINES_DIR.mkdir(parents=True, exist_ok=True)
BLOCK_DIR.mkdir(parents=True, exist_ok=True)
GUI_DIR.mkdir(parents=True, exist_ok=True)


def build_smoker_block_textures():
    print("Processing Smoker Block 64x64 textures...")
    raw = Image.open(SMOKER_RAW)
    # Crop bounding box: (20, 20, 1003, 1003)
    cropped = raw.crop((20, 20, 1003, 1003)).resize((64, 64), Image.Resampling.LANCZOS)
    
    # 1. Front ON (bright flames and glowing 225C display)
    front_on = cropped.convert("RGBA")
    front_on.save(BLOCK_MACHINES_DIR / "smoker_front_on.png")
    front_on.save(BLOCK_DIR / "fish_smoker_front_on.png")
    
    # 2. Front OFF (dim window and display)
    front_off = cropped.copy().convert("RGBA")
    # Dim the display area: roughly x in 25..40, y in 8..16
    for y in range(8, 16):
        for x in range(25, 40):
            r, g, b, a = front_off.getpixel((x, y))
            front_off.putpixel((x, y), (int(r * 0.25), int(g * 0.25), int(b * 0.25), a))
    # Dim the furnace window: roughly x in 22..42, y in 22..42
    for y in range(22, 43):
        for x in range(22, 43):
            r, g, b, a = front_off.getpixel((x, y))
            gray = int(r * 0.3 + g * 0.59 + b * 0.11)
            front_off.putpixel((x, y), (int(gray * 0.4), int(gray * 0.35), int(gray * 0.35), a))
            
    front_off.save(BLOCK_MACHINES_DIR / "smoker_front_off.png")
    front_off.save(BLOCK_DIR / "fish_smoker_front_off.png")
    front_off.save(BLOCK_DIR / "fish_smoker_front.png")
    
    # 3. Side texture (dark steel with rivets and bronze trim)
    side = cropped.copy().convert("RGBA")
    d_side = ImageDraw.Draw(side)
    for y in range(16, 48):
        for x in range(16, 48):
            p = cropped.getpixel((10, y))
            side.putpixel((x, y), p)
    for x in range(6, 58):
        side.putpixel((x, 32), (35, 38, 44, 255))
        side.putpixel((x, 33), (65, 70, 80, 255))
    for rx in (14, 26, 38, 50):
        for ry in (22, 42):
            d_side.ellipse([rx, ry, rx + 2, ry + 2], fill=(130, 110, 80, 255), outline=(40, 35, 30, 255))
            
    side.save(BLOCK_MACHINES_DIR / "smoker_side.png")
    side.save(BLOCK_DIR / "fish_smoker_side.png")
    
    # 4. Top texture (steel top plate with smoke vent grille)
    top = side.copy()
    d_top = ImageDraw.Draw(top)
    d_top.rectangle([18, 18, 45, 45], fill=(22, 25, 30, 255), outline=(130, 105, 60, 255))
    for sl in range(22, 43, 3):
        d_top.line([(21, sl), (42, sl)], fill=(12, 14, 18, 255), width=2)
        d_top.line([(21, sl + 1), (42, sl + 1)], fill=(50, 55, 65, 255), width=1)
    top.save(BLOCK_MACHINES_DIR / "smoker_top.png")
    top.save(BLOCK_DIR / "fish_smoker_top.png")
    
    # 5. Bottom texture
    bottom = side.copy()
    d_bot = ImageDraw.Draw(bottom)
    d_bot.rectangle([12, 12, 51, 51], fill=(28, 30, 35, 255), outline=(45, 50, 58, 255))
    bottom.save(BLOCK_MACHINES_DIR / "smoker_bottom.png")
    bottom.save(BLOCK_DIR / "fish_smoker_bottom.png")
    
    # 6. Smoker dark & gold trim swatches
    smoker_dark = Image.new("RGBA", (64, 64), (35, 38, 44, 255))
    d_d = ImageDraw.Draw(smoker_dark)
    d_d.rectangle([0, 0, 63, 63], outline=(55, 60, 70, 255))
    smoker_dark.save(BLOCK_MACHINES_DIR / "smoker_dark.png")
    
    smoker_gold = Image.new("RGBA", (64, 64), (165, 130, 65, 255))
    d_g = ImageDraw.Draw(smoker_gold)
    d_g.rectangle([0, 0, 63, 63], outline=(210, 175, 95, 255))
    smoker_gold.save(BLOCK_MACHINES_DIR / "smoker_gold.png")
    print("Smoker 64x64 block textures generated successfully.")


def build_fisher_block_textures():
    print("Processing Auto Fisher Block 64x64 textures...")
    raw = Image.open(FISHER_RAW)
    cropped = raw.crop((75, 76, 948, 946)).resize((64, 64), Image.Resampling.LANCZOS)
    
    # 1. Front ON
    front_on = cropped.convert("RGBA")
    front_on.save(BLOCK_MACHINES_DIR / "af_front_on.png")
    front_on.save(BLOCK_DIR / "auto_fisher_front_on.png")
    
    # 2. Front OFF
    front_off = cropped.copy().convert("RGBA")
    for y in range(64):
        for x in range(64):
            r, g, b, a = front_off.getpixel((x, y))
            if b > 160 and g > 140 and r < 120:
                front_off.putpixel((x, y), (int(r * 0.3), int(g * 0.4), int(b * 0.5), a))
    front_off.save(BLOCK_MACHINES_DIR / "af_front.png")
    front_off.save(BLOCK_DIR / "auto_fisher_front.png")
    front_off.save(BLOCK_DIR / "auto_fisher_front_off.png")
    
    # 3. Side texture
    side = cropped.copy().convert("RGBA")
    d_side = ImageDraw.Draw(side)
    d_side.rectangle([10, 10, 53, 53], fill=(22, 34, 46, 255), outline=(42, 60, 80, 255))
    d_side.line([(0, 32), (63, 32)], fill=(0, 210, 240, 255), width=2)
    d_side.line([(0, 31), (63, 31)], fill=(0, 140, 180, 255), width=1)
    for rx in (14, 32, 50):
        for ry in (18, 46):
            d_side.ellipse([rx, ry, rx + 2, ry + 2], fill=(60, 90, 120, 255), outline=(15, 25, 35, 255))
    side.save(BLOCK_MACHINES_DIR / "af_side.png")
    side.save(BLOCK_DIR / "auto_fisher_side.png")
    
    # 4. Top texture
    top = side.copy()
    d_top = ImageDraw.Draw(top)
    d_top.rectangle([14, 14, 49, 49], fill=(16, 26, 38, 255), outline=(0, 190, 220, 255))
    d_top.ellipse([18, 18, 45, 45], fill=(10, 18, 28, 255), outline=(35, 65, 90, 255))
    d_top.line([(31, 18), (31, 45)], fill=(0, 190, 220, 255), width=2)
    d_top.line([(18, 31), (45, 31)], fill=(0, 190, 220, 255), width=2)
    top.save(BLOCK_MACHINES_DIR / "af_top.png")
    top.save(BLOCK_DIR / "auto_fisher_top.png")
    
    # 5. Bottom texture
    bottom = side.copy()
    d_bot = ImageDraw.Draw(bottom)
    d_bot.rectangle([0, 0, 63, 63], fill=(14, 22, 32, 255), outline=(30, 48, 64, 255))
    bottom.save(BLOCK_DIR / "auto_fisher_bottom.png")
    print("Auto Fisher 64x64 block textures generated successfully.")


def draw_slot(d, x, y, frame_col=(60, 80, 100, 255), bg_col=(10, 16, 22, 255)):
    d.rectangle([x - 1, y - 1, x + 16, y + 16], fill=frame_col)
    d.rectangle([x, y, x + 15, y + 15], fill=bg_col)
    d.line([(x - 1, y - 1), (x + 16, y - 1)], fill=(frame_col[0] // 2, frame_col[1] // 2, frame_col[2] // 2, 255))
    d.line([(x - 1, y - 1), (x - 1, y + 16)], fill=(frame_col[0] // 2, frame_col[1] // 2, frame_col[2] // 2, 255))


def draw_player_inventory(d, frame_col, bg_col):
    for row in range(3):
        for col in range(9):
            draw_slot(d, 8 + col * 18, 84 + row * 18, frame_col, bg_col)
    for col in range(9):
        draw_slot(d, 8 + col * 18, 142, frame_col, bg_col)


def build_smoker_gui():
    print("Generating Fish Smoker 256x256 GUI atlas...")
    atlas = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    d = ImageDraw.Draw(atlas)
    
    PANEL_BG = (28, 32, 38, 255)
    PANEL_BORDER = (165, 125, 65, 255)
    PANEL_INNER = (36, 42, 50, 255)
    SLOT_FRAME = (140, 105, 55, 255)
    SLOT_BG = (14, 18, 24, 255)
    
    d.rectangle([0, 0, 175, 165], fill=PANEL_BG, outline=PANEL_BORDER, width=2)
    d.rectangle([3, 3, 172, 162], outline=(60, 50, 40, 255))
    d.rectangle([6, 6, 169, 14], fill=(20, 24, 30, 255), outline=SLOT_FRAME)
    d.rectangle([6, 15, 169, 78], fill=PANEL_INNER, outline=(50, 45, 40, 255))
    
    draw_slot(d, 56, 17, SLOT_FRAME, SLOT_BG)
    draw_slot(d, 56, 53, SLOT_FRAME, SLOT_BG)
    draw_slot(d, 116, 35, (220, 170, 70, 255), (10, 14, 20, 255))
    
    d.rectangle([57, 37, 70, 50], fill=(18, 20, 24, 255), outline=(50, 45, 40, 255))
    pts = [(79, 38), (93, 38), (93, 34), (103, 42), (93, 51), (93, 47), (79, 47)]
    d.polygon(pts, fill=(18, 20, 24, 255), outline=(60, 55, 50, 255))
    
    d.line([(6, 80), (169, 80)], fill=(75, 60, 45, 255), width=1)
    draw_player_inventory(d, (70, 65, 60, 255), (20, 24, 28, 255))
    
    for cx, cy in ((3, 3), (170, 3), (3, 160), (170, 160)):
        d.ellipse([cx, cy, cx + 2, cy + 2], fill=(210, 175, 95, 255))
        
    for i in range(24):
        hh = 4 + (i * 9) // 24
        cy = 52 + 8
        d.line([(176 + i, cy - hh // 2), (176 + i, cy + hh // 2)], fill=(255, 180 + i * 3, 40, 255))
    for i in range(14):
        d.line([(176 + i, 70 + 13 - i), (176 + i, 70 + 13)], fill=(255, 90 + i * 11, 20, 255))
        
    out_path = GUI_DIR / "fish_smoker.png"
    atlas.save(out_path)
    print("Wrote", out_path)


def build_fisher_gui():
    print("Generating Auto Fisher 256x256 GUI atlas...")
    atlas = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    d = ImageDraw.Draw(atlas)
    
    PANEL_BG = (14, 24, 36, 255)
    PANEL_BORDER = (0, 200, 240, 255)
    PANEL_INNER = (20, 34, 50, 255)
    CYAN_FRAME = (0, 180, 220, 255)
    CYAN_DIM = (25, 75, 105, 255)
    SLOT_BG = (10, 18, 28, 255)
    
    d.rectangle([0, 0, 175, 165], fill=PANEL_BG, outline=PANEL_BORDER, width=2)
    d.rectangle([3, 3, 172, 162], outline=CYAN_DIM)
    d.rectangle([6, 6, 169, 14], fill=(10, 18, 28, 255), outline=CYAN_DIM)
    d.rectangle([6, 15, 169, 78], fill=PANEL_INNER, outline=CYAN_DIM)
    
    d.rectangle([36, 20, 49, 70], fill=(6, 12, 18, 255), outline=CYAN_FRAME)
    draw_slot(d, 16, 29, CYAN_FRAME, SLOT_BG)
    draw_slot(d, 16, 48, (0, 240, 180, 255), SLOT_BG)
    
    for row in range(2):
        for col in range(3):
            draw_slot(d, 101 + col * 20, 30 + row * 17, CYAN_FRAME, SLOT_BG)
            
    pts = [(70, 44), (84, 44), (84, 40), (94, 47), (84, 54), (84, 50), (70, 50)]
    d.polygon(pts, fill=(10, 18, 26, 255), outline=CYAN_DIM)
    
    d.line([(6, 80), (169, 80)], fill=CYAN_DIM, width=1)
    draw_player_inventory(d, CYAN_DIM, (12, 22, 34, 255))
    
    for cx, cy in ((4, 4), (171, 4), (4, 161), (171, 161)):
        d.ellipse([cx, cy, cx + 1, cy + 1], fill=(0, 255, 255, 255))
        
    for i in range(49):
        t = i / 48.0
        r = int(0 + 80 * (1 - t))
        g = int(140 + 115 * (1 - t))
        b = int(220 + 35 * (1 - t))
        d.line([(182, 2 + 48 - i), (182 + 11, 2 + 48 - i)], fill=(r, g, b, 255))
        
    for i in range(22):
        hh = 3 + (i * 7) // 22
        cy = 61 + 6
        d.line([(180 + i, cy - hh // 2), (180 + i, cy + hh // 2)], fill=(0, 240, 255, 255))
        
    out_path = GUI_DIR / "auto_fisher.png"
    atlas.save(out_path)
    print("Wrote", out_path)


if __name__ == "__main__":
    build_smoker_block_textures()
    build_fisher_block_textures()
    build_smoker_gui()
    build_fisher_gui()
    print("All NanoBanana machine assets processed successfully!")
