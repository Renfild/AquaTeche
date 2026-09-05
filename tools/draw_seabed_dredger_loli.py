#!/usr/bin/env python3
"""Generate authentic LoliLand Style B ("dark tech") GUI sheet for Seabed Dredger.

Reverse-engineered from LoliLand / MetaLabs (LoliEnergistics, LoliDragonMight, luminous):
- Panel body: #2E2E40 with #1B1B26 shadow and #080811 outlines.
- Circuit frame: #4EF9FF cyan lines, 4px corner brackets, 2x2 nodes.
- Custom socket slots: #232330 with #3F3F55 top light and centered socket glyph.
- Baked title plate: #0A0A0E with status square and gold T1 badge.
- Left energy gauge: (3, 17) [8x50] with lightning icon.
- Drill slot: (27, 19) with drill silhouette.
- Upgrade slot: (27, 55) with microchip silhouette.
- Progress arrow: (50, 36) [24x17].
- Output matrix: 3x3 slots at (77, 19) in reinforced container frame.
- Inventory: (8, 84), hotbar: (8, 142) with separator line.
"""
import os
from PIL import Image

def create_loli_dredger_gui(out_path: str):
    SHEET = 256
    img = Image.new("RGBA", (SHEET, SHEET), (0, 0, 0, 0))

    # Palette
    C_PANEL_DARK   = (8, 8, 17, 255)      # 080811 outline
    C_PANEL_BODY   = (46, 46, 64, 255)    # 2E2E40 machine panel
    C_PANEL_SHADOW = (27, 27, 38, 255)    # 1B1B26 bevel shadow
    C_PANEL_BEVEL  = (105, 105, 122, 255) # 69697A bevel highlight
    C_INV_BG       = (36, 36, 50, 255)    # 242432 inventory plate
    C_CYAN         = (78, 249, 255, 255)  # 4EF9FF neon cyan
    C_CYAN_DIM     = (28, 110, 125, 255)  # 1C6E7D trace
    C_MINT         = (68, 225, 170, 255)  # 44E1AA
    C_SLOT_BG      = (35, 35, 48, 255)    # 232330
    C_SLOT_HI      = (63, 63, 85, 255)    # 3F3F55
    C_SLOT_GLYPH   = (20, 20, 28, 255)    # 14141C
    C_PLATE_BG     = (10, 10, 14, 255)    # 0A0A0E
    C_PLATE_BORDER = (140, 140, 165, 255) # 8C8CA5
    C_WHITE        = (240, 245, 255, 255)
    C_GOLD         = (255, 185, 45, 255)
    C_DIVIDER      = (18, 18, 26, 255)

    def px(x, y, col):
        if 0 <= x < SHEET and 0 <= y < SHEET:
            img.putpixel((x, y), col)

    def rect(x0, y0, x1, y1, col):
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                px(x, y, col)

    def hline(x0, x1, y, col):
        rect(x0, y, x1, y, col)

    def vline(x, y0, y1, col):
        rect(x, y0, x, y1, col)

    # 1. Main Background Panel (176x166)
    W, H = 176, 166
    rect(0, 0, W - 1, H - 1, C_PANEL_DARK)
    rect(1, 1, W - 2, H - 2, C_PANEL_BODY)
    hline(1, W - 2, 1, C_PANEL_BEVEL)
    vline(1, 1, H - 2, C_PANEL_BEVEL)
    hline(1, W - 2, H - 2, C_PANEL_SHADOW)
    vline(W - 2, 1, H - 2, C_PANEL_SHADOW)

    # Divider between machine and inventory: y = 74..76
    DIV_Y = 74
    hline(1, W - 2, DIV_Y, C_PANEL_DARK)
    hline(1, W - 2, DIV_Y + 1, C_PANEL_SHADOW)
    hline(1, W - 2, DIV_Y + 2, C_PANEL_BEVEL)

    # Inventory panel body
    rect(1, DIV_Y + 3, W - 2, H - 2, C_INV_BG)
    # Hotbar divider line at y = 139
    hline(8, 168, 139, C_PANEL_DARK)
    hline(8, 168, 140, C_PANEL_SHADOW)

    # 2. Left Energy Column Rail (x = 1..13, y = 2..72)
    # Energy Bar at (3, 17) [8x50]
    ex, ey, ew, eh = 3, 17, 8, 50
    # Outer dark container
    rect(ex - 1, ey - 1, ex + ew, ey + eh, C_PANEL_DARK)
    rect(ex, ey, ex + ew - 1, ey + eh - 1, C_SLOT_GLYPH)
    # Segment marks every 10px
    for ty in range(ey + 9, ey + eh - 1, 10):
        hline(ex, ex + ew - 1, ty, C_PANEL_DARK)

    # Energy Icon: Lightning at (4, 7)
    lightning_rows = [
        "..##..",
        ".##...",
        ".####.",
        "..##..",
        ".##...",
        "##...."
    ]
    for r, l_row in enumerate(lightning_rows):
        for c, ch in enumerate(l_row):
            if ch == '#':
                px(4 + c, 8 + r, C_GOLD)

    # Vertical cyan circuit trace beside energy column
    vline(14, 4, DIV_Y - 3, C_CYAN_DIM)

    # 3. Cyan Circuit Frame in Machine Section (x = 17..172, y = 2..71)
    fx0, fy0, fx1, fy1 = 17, 2, W - 4, DIV_Y - 3
    # Perimeter traces
    hline(fx0 + 3, fx1 - 3, fy0, C_CYAN)
    # Bottom trace routed around upgrade slot (27..44) and 3x3 frame (75..132)
    hline(fx0 + 3, 25, fy1, C_CYAN)
    hline(46, 73, fy1, C_CYAN)
    hline(134, fx1 - 3, fy1, C_CYAN)

    vline(fx0, fy0 + 3, fy1 - 3, C_CYAN)
    vline(fx1, fy0 + 3, fy1 - 3, C_CYAN)

    # Corner brackets (4px) & 2x2 nodes
    for cx, dx in ((fx0, 1), (fx1, -1)):
        for cy, dy in ((fy0, 1), (fy1, -1)):
            hline(cx, cx + dx * 4, cy, C_CYAN)
            vline(cx, cy, cy + dy * 4, C_CYAN)
            rect(cx + dx * 2, cy + dy * 2, cx + dx * 3, cy + dy * 3, C_CYAN)

    # Circuit node ticks
    mx = (fx0 + fx1) // 2
    vline(mx, fy0, fy0 + 2, C_CYAN)

    # 4. Baked Title Plate (cx = 94, y = 4, width = 136)
    px0, py0, px1, py1 = 24, 4, 164, 14
    rect(px0, py0, px1, py1, C_PLATE_BG)
    hline(px0, px1, py0, C_PLATE_BORDER)
    hline(px0, px1, py1, C_PLATE_BORDER)
    vline(px0, py0, py1, C_PLATE_BORDER)
    vline(px1, py0, py1, C_PLATE_BORDER)

    # Glowing Cyan Status Indicator
    rect(px1 - 6, py0 + 3, px1 - 4, py0 + 5, C_CYAN)
    px(px1 - 5, py0 + 4, C_WHITE)

    # 3x5 Pixel Font
    FONT_3X5 = {
        'A': [0x2, 0x5, 0x7, 0x5, 0x5], 'B': [0x6, 0x5, 0x6, 0x5, 0x6],
        'C': [0x3, 0x4, 0x4, 0x4, 0x3], 'D': [0x6, 0x5, 0x5, 0x5, 0x6],
        'E': [0x7, 0x4, 0x6, 0x4, 0x7], 'F': [0x7, 0x4, 0x6, 0x4, 0x4],
        'G': [0x3, 0x4, 0x5, 0x5, 0x3], 'H': [0x5, 0x5, 0x7, 0x5, 0x5],
        'I': [0x7, 0x2, 0x2, 0x2, 0x7], 'J': [0x1, 0x1, 0x1, 0x5, 0x2],
        'K': [0x5, 0x5, 0x6, 0x5, 0x5], 'L': [0x4, 0x4, 0x4, 0x4, 0x7],
        'M': [0x5, 0x7, 0x5, 0x5, 0x5], 'N': [0x5, 0x7, 0x7, 0x5, 0x5],
        'O': [0x2, 0x5, 0x5, 0x5, 0x2], 'P': [0x6, 0x5, 0x6, 0x4, 0x4],
        'R': [0x6, 0x5, 0x6, 0x5, 0x5], 'S': [0x3, 0x4, 0x2, 0x1, 0x6],
        'T': [0x7, 0x2, 0x2, 0x2, 0x2], 'U': [0x5, 0x5, 0x5, 0x5, 0x2],
        'V': [0x5, 0x5, 0x5, 0x2, 0x2], 'W': [0x5, 0x5, 0x5, 0x7, 0x5],
        'Y': [0x5, 0x5, 0x2, 0x2, 0x2], '1': [0x2, 0x6, 0x2, 0x2, 0x7],
        '-': [0x0, 0x0, 0x7, 0x0, 0x0], ' ': [0x0, 0x0, 0x0, 0x0, 0x0],
    }

    def draw_text_3x5(text, tx, ty, col):
        curr_x = tx
        for ch in text.upper():
            rows = FONT_3X5.get(ch, [0, 0, 0, 0, 0])
            for r, mask in enumerate(rows):
                for c in range(3):
                    if (mask >> (2 - c)) & 1:
                        px(curr_x + c, ty + r, col)
            curr_x += 4

    draw_text_3x5("SEABED DREDGER", px0 + 5, py0 + 3, C_WHITE)
    draw_text_3x5("T1", px1 - 20, py0 + 3, C_GOLD)

    # 5. Slots Drawing Helper
    def draw_loli_slot(sx, sy, silhouette=None):
        rect(sx, sy, sx + 17, sy + 17, C_PANEL_DARK)
        rect(sx + 1, sy + 1, sx + 16, sy + 16, C_SLOT_BG)
        hline(sx + 1, sx + 16, sy + 1, C_SLOT_HI)
        vline(sx + 1, sy + 1, sy + 16, C_SLOT_HI)
        hline(sx + 1, sx + 16, sy + 16, C_SLOT_GLYPH)
        vline(sx + 16, sy + 1, sy + 16, C_SLOT_GLYPH)

        if silhouette == "drill":
            # Realistic drill bit silhouette (tapered shaft)
            rect(sx + 6, sy + 4, sx + 11, sy + 5, C_SLOT_GLYPH)
            rect(sx + 7, sy + 6, sx + 10, sy + 8, C_SLOT_GLYPH)
            rect(sx + 8, sy + 9, sx + 9, sy + 11, C_SLOT_GLYPH)
            px(sx + 8, sy + 12, C_SLOT_GLYPH)
        elif silhouette == "chip":
            # Chip / upgrade card silhouette
            rect(sx + 6, sy + 5, sx + 11, sy + 11, C_SLOT_GLYPH)
            hline(sx + 5, sx + 12, sy + 7, C_SLOT_GLYPH)
            hline(sx + 5, sx + 12, sy + 9, C_SLOT_GLYPH)
        else:
            # Default Loli socket glyph (central socket pin)
            rect(sx + 7, sy + 7, sx + 10, sy + 10, C_SLOT_GLYPH)
            hline(sx + 7, sx + 10, sy + 7, C_SLOT_HI)

    # Drill slot: (27, 19)
    draw_loli_slot(27, 19, "drill")
    # Upgrade slot: (27, 55)
    draw_loli_slot(27, 55, "chip")

    # Circuit traces connecting slots
    vline(22, 22, 60, C_CYAN_DIM)
    hline(22, 26, 28, C_CYAN_DIM)
    hline(22, 26, 64, C_CYAN_DIM)

    # 6. Output Matrix 3x3 at (77..130, 19..72) with collection frame
    rect(75, 17, 132, 73, C_PANEL_DARK)
    rect(76, 18, 131, 72, C_PANEL_SHADOW)
    for row in range(3):
        for col in range(3):
            draw_loli_slot(77 + col * 18, 19 + row * 18)

    # Cyan corner brackets on 3x3 frame
    rect(75, 17, 78, 17, C_CYAN)
    rect(75, 17, 75, 20, C_CYAN)
    rect(129, 17, 132, 17, C_CYAN)
    rect(132, 17, 132, 20, C_CYAN)
    rect(75, 73, 78, 73, C_CYAN)
    rect(75, 70, 75, 73, C_CYAN)
    rect(129, 73, 132, 73, C_CYAN)
    rect(132, 70, 132, 73, C_CYAN)

    # 7. Progress Arrow Track at (50, 36) [24x17]
    ax, ay = 50, 36
    # Track background (dark circuit track)
    for dx in range(2, 14, 4):
        rect(ax + dx, ay + 7, ax + dx + 2, ay + 9, C_PANEL_DARK)
    rect(ax + 14, ay + 4, ax + 16, ay + 12, C_PANEL_DARK)
    rect(ax + 17, ay + 6, ax + 19, ay + 10, C_PANEL_DARK)
    rect(ax + 20, ay + 7, ax + 21, ay + 9, C_PANEL_DARK)

    # 8. Inventory Header & Slots
    # Dark Capsule Plate for Inventory Title: (8, 72) to (58, 80)
    rect(8, 73, 58, 81, C_PLATE_BG)
    hline(8, 58, 73, C_PANEL_DARK)
    hline(8, 58, 81, C_PANEL_DARK)
    vline(8, 73, 81, C_PANEL_DARK)
    vline(58, 73, 81, C_PANEL_DARK)
    draw_text_3x5("INVENTORY", 12, 75, (180, 185, 205, 255))

    # Player Inventory (8, 84)
    for row in range(3):
        for col in range(9):
            draw_loli_slot(8 + col * 18, 84 + row * 18)

    # Hotbar (8, 142)
    for col in range(9):
        draw_loli_slot(8 + col * 18, 142)

    # -------------------------------------------------------------
    # SPRITES ZONE (x >= 176)
    # -------------------------------------------------------------

    # A. Energy Fill Sprite at (176, 0) [8x50]
    sp_ex, sp_ey = 176, 0
    for y in range(eh):
        ratio = y / float(eh)
        r = int(20 + 40 * ratio)
        g = int(245 - 60 * ratio)
        b = 255
        col = (r, g, b, 255)
        hline(sp_ex, sp_ex + ew - 1, sp_ey + y, col)
        # Glowing inner core
        hline(sp_ex + 2, sp_ex + 5, sp_ey + y, (220, 255, 255, 255))

    # Segment ticks on sprite matching track
    for ty in range(sp_ey + 9, sp_ey + eh - 1, 10):
        hline(sp_ex, sp_ex + ew - 1, ty, (10, 25, 45, 255))

    # B. Progress Arrow Fill Sprite at (176, 52) [24x17]
    sp_ax, sp_ay = 176, 52
    rect(sp_ax, sp_ay, sp_ax + 23, sp_ay + 16, (0, 0, 0, 0))

    for col_idx in range(24):
        if col_idx <= 15:
            rect(sp_ax + col_idx, sp_ay + 6, sp_ax + col_idx, sp_ay + 10, C_CYAN)
            hline(sp_ax + col_idx, sp_ax + col_idx, sp_ay + 8, C_WHITE)
        else:
            delta = col_idx - 16
            h_half = min(delta + 2, 7)
            vline(sp_ax + col_idx, sp_ay + 8 - h_half, sp_ay + 8 + h_half, C_CYAN)
            vline(sp_ax + col_idx, sp_ay + 8 - max(0, h_half - 2), sp_ay + 8 + max(0, h_half - 2), C_WHITE)

    os.makedirs(os.path.dirname(out_path), exist_ok=True)
    img.save(out_path, "PNG")
    print(f"OK generated refined LoliLand Seabed Dredger GUI -> {out_path}")

    # Generate 3x pixel preview
    gui_crop = img.crop((0, 0, 176, 166))
    preview = gui_crop.resize((176 * 3, 166 * 3), Image.NEAREST)
    preview_path = r"C:\Users\xieto\.gemini\antigravity-ide\brain\531f1ed4-ea9c-4420-b760-5e17beab3bf0\seabed_dredger_loli_preview.png"
    preview.save(preview_path)
    print(f"OK generated preview -> {preview_path}")

if __name__ == "__main__":
    out = r"d:\AquaTech\mods\aquatech-ui\src\main\resources\assets\aquatech_ui\textures\gui\seabed_dredger.png"
    create_loli_dredger_gui(out)
