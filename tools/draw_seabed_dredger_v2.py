#!/usr/bin/env python3
"""Generate pixel-perfect Seabed Dredger ("Экскаватор") GUI sheet.

Exact design based on user's hand-drawn reference (auto_fisher.png) and LoliLand / Replicator textures:
- Centered floating circuitry title badge: "ЭКСКАВАТОР" in authentic Cyrillic pixel font (NO T1).
- Deep dark navy machine chassis (#0B1A26) with double border (#00384D / #007799).
- Clean, self-contained 3x3 output matrix module (no crossing lines).
- Progress arrow at (50, 36) [24x17]:
    * Main GUI has the unlit cyan outline arrow with dark interior.
    * Sprite at (176, 52) has the EXACT same outline filled with solid glowing cyan/blue.
- Energy bar at (3, 17) [8x50]:
    * Main GUI has dark track with 1px border.
    * Sprite at (176, 0) has the exact same outline filled with glowing cyan energy gradient.
- Authentic classic Minecraft player inventory frame with 18x18 slots, guaranteeing items and numbers never clip.
"""
import os
from PIL import Image

def create_dredger_v2(out_path: str):
    SHEET = 256
    img = Image.new("RGBA", (SHEET, SHEET), (0, 0, 0, 0))

    # Load hand-drawn auto_fisher reference for the classic inventory box
    ref_path = r"d:\AquaTech\scratch\proposals\aquatech-redesign-v1\reference_old\auto_fisher.png"
    ref_img = None
    if os.path.exists(ref_path):
        ref_img = Image.open(ref_path)

    # Color Palette (derived from reference_old/auto_fisher.png)
    C_OUTLINE      = (6, 16, 26, 255)     # 06101A chassis outer rim
    C_PANEL_BG     = (11, 26, 38, 255)    # 0B1A26 machine body
    C_BORDER_DARK  = (17, 37, 53, 255)    # 112535
    C_BORDER_MID   = (24, 67, 100, 255)   # 184364
    C_BORDER_CYAN  = (14, 165, 233, 255)  # 0EA5E9 circuit cyan
    C_CYAN_DIM     = (18, 90, 125, 255)   # 125A7D
    C_WHITE        = (255, 255, 255, 255)
    C_SLOT_OUTER   = (24, 67, 100, 255)
    C_SLOT_INSET   = (34, 76, 109, 255)
    C_SLOT_BG      = (11, 26, 38, 255)
    C_SLOT_PIN     = (50, 59, 68, 255)
    C_BADGE_BG     = (8, 24, 32, 255)

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

    # -------------------------------------------------------------
    # 1. Machine Upper Panel (0, 11) to (175, 83)
    # -------------------------------------------------------------
    # Outer frame
    rect(0, 11, 175, 83, C_OUTLINE)
    rect(1, 12, 174, 82, C_PANEL_BG)
    hline(2, 173, 12, C_BORDER_MID)
    vline(1, 13, 81, C_BORDER_MID)
    hline(2, 173, 82, C_BORDER_DARK)
    vline(174, 13, 81, C_BORDER_DARK)

    # Inner subtle cyan trim
    rect(3, 14, 172, 80, C_PANEL_BG)
    hline(4, 171, 14, C_BORDER_DARK)
    vline(3, 15, 79, C_BORDER_DARK)

    # Corner screw accents
    for cx in (4, 170):
        for cy in (15, 78):
            rect(cx, cy, cx + 1, cy + 1, C_BORDER_MID)
            px(cx, cy, C_CYAN_DIM)

    # -------------------------------------------------------------
    # 2. Centered Floating Title Badge: "ЭКСКАВАТОР" (x=40..135, y=0..10)
    # -------------------------------------------------------------
    bx0, by0, bx1, by1 = 41, 1, 134, 9
    # Badge background
    rect(bx0, by0, bx1, by1, C_BADGE_BG)
    # Circuit borders top & bottom
    hline(bx0 + 7, bx1 - 7, by0, C_BORDER_CYAN)
    hline(bx0 + 7, bx1 - 7, by1, C_BORDER_CYAN)

    # Left circuit bracket
    hline(bx0 + 3, bx0 + 6, by0 + 1, C_BORDER_CYAN)
    vline(bx0 + 2, by0 + 2, by0 + 3, C_BORDER_CYAN)
    hline(bx0, bx0 + 2, by0 + 4, C_BORDER_CYAN)
    vline(bx0 + 2, by0 + 5, by0 + 6, C_BORDER_CYAN)
    hline(bx0 + 3, bx0 + 6, by0 + 7, C_BORDER_CYAN)

    # Right circuit bracket
    hline(bx1 - 6, bx1 - 3, by0 + 1, C_BORDER_CYAN)
    vline(bx1 - 2, by0 + 2, by0 + 3, C_BORDER_CYAN)
    hline(bx1 - 2, bx1, by0 + 4, C_BORDER_CYAN)
    vline(bx1 - 2, by0 + 5, by0 + 6, C_BORDER_CYAN)
    hline(bx1 - 6, bx1 - 3, by0 + 7, C_BORDER_CYAN)

    # Cyrillic pixel font glyphs (5px high) matching auto_fisher
    CYR_FONT = {
        'Э': ['### ', '   #', ' ## ', '   #', '### '],
        'К': ['#  #', '# # ', '##  ', '# # ', '#  #'],
        'С': [' ## ', '#   ', '#   ', '#   ', ' ## '],
        'А': [' ## ', '#  #', '####', '#  #', '#  #'],
        'В': ['### ', '#  #', '### ', '#  #', '####'],
        'Т': ['#####', '  #  ', '  #  ', '  #  ', '  #  '],
        'О': [' ## ', '#  #', '#  #', '#  #', ' ## '],
        'Р': ['### ', '#  #', '####', '#   ', '#   ']
    }

    word = "ЭКСКАВАТОР"
    # Measure width
    total_text_w = sum(len(CYR_FONT[ch][0]) for ch in word) + (len(word) - 1)
    start_tx = (bx0 + bx1 - total_text_w) // 2 + 1
    curr_x = start_tx

    for ch in word:
        glyph = CYR_FONT[ch]
        gw = len(glyph[0])
        for r in range(5):
            for c in range(gw):
                if glyph[r][c] == '#':
                    px(curr_x + c, by0 + 2 + r, C_BORDER_CYAN)
        curr_x += gw + 1

    # -------------------------------------------------------------
    # 3. Machine Slots
    # -------------------------------------------------------------
    def draw_tech_slot(sx, sy, is_tool=False, is_upgr=False):
        rect(sx, sy, sx + 17, sy + 17, C_SLOT_OUTER)
        rect(sx + 1, sy + 1, sx + 16, sy + 16, C_SLOT_BG)
        # Concentric inner frame
        rect(sx + 2, sy + 2, sx + 15, sy + 15, C_SLOT_INSET)
        rect(sx + 3, sy + 3, sx + 14, sy + 14, C_SLOT_BG)

        if is_tool:
            # Subtle tool socket pin
            rect(sx + 7, sy + 7, sx + 10, sy + 10, C_SLOT_PIN)
        elif is_upgr:
            # Upgrade socket pin
            rect(sx + 7, sy + 7, sx + 10, sy + 10, C_SLOT_PIN)
        else:
            # Standard center socket
            rect(sx + 7, sy + 7, sx + 10, sy + 10, C_SLOT_PIN)

    # Tool / Drill slot at (27, 19)
    draw_tech_slot(27, 19, is_tool=True)
    # Upgrade slot at (27, 55)
    draw_tech_slot(27, 55, is_upgr=True)

    # -------------------------------------------------------------
    # 4. Clean 3x3 Output Module at (75, 17) to (132, 74)
    # -------------------------------------------------------------
    # Self-contained frame
    rect(75, 17, 132, 74, C_BORDER_DARK)
    rect(76, 18, 131, 73, C_PANEL_BG)

    # Corner brackets on 3x3 container
    for cx, dx in ((75, 1), (132, -1)):
        for cy, dy in ((17, 1), (74, -1)):
            hline(cx, cx + dx * 3, cy, C_BORDER_CYAN)
            vline(cx, cy, cy + dy * 3, C_BORDER_CYAN)

    # 3x3 slots: (77..130, 19..72)
    for r in range(3):
        for c in range(3):
            draw_tech_slot(77 + c * 18, 19 + r * 18)

    # -------------------------------------------------------------
    # 5. Energy Bar at (3, 17) [8x50]
    # -------------------------------------------------------------
    ex, ey, ew, eh = 3, 17, 8, 50
    # Unlit track on main GUI
    rect(ex - 1, ey - 1, ex + ew, ey + eh, C_OUTLINE)
    rect(ex, ey, ex + ew - 1, ey + eh - 1, C_SLOT_BG)
    hline(ex, ex + ew - 1, ey, C_BORDER_DARK)
    vline(ex, ey, ey + eh - 1, C_BORDER_DARK)
    hline(ex, ex + ew - 1, ey + eh - 1, C_BORDER_MID)
    vline(ex + ew - 1, ey, ey + eh - 1, C_BORDER_MID)

    # -------------------------------------------------------------
    # 6. Progress Arrow at (50, 36) [24x17]
    # -------------------------------------------------------------
    # 24x17 arrow shape definition (row_start, row_end) for each x column (0..23)
    # Shaft: col 0..14 (y: 4..12 -> height 9)
    # Head:  col 15..23 (y: 0..16 tapering to 8..8)
    def get_arrow_span(col):
        if col < 15:
            return (4, 12)
        # Tip tapering
        dt = col - 15  # 0..8
        h_half = 8 - dt
        return (8 - h_half, 8 + h_half)

    ax, ay = 50, 36
    # Draw UNLIT outline arrow on main GUI (matching sprite shape 100%)
    for c in range(24):
        y_top, y_bot = get_arrow_span(c)
        if c == 0:
            vline(ax + c, ay + y_top, ay + y_bot, C_BORDER_CYAN)
        elif c < 15:
            px(ax + c, ay + y_top, C_BORDER_CYAN)
            px(ax + c, ay + y_bot, C_BORDER_CYAN)
        else:
            # Arrow head edges
            px(ax + c, ay + y_top, C_BORDER_CYAN)
            px(ax + c, ay + y_bot, C_BORDER_CYAN)
            if c == 15:
                vline(ax + c, ay + 0, ay + y_top, C_BORDER_CYAN)
                vline(ax + c, ay + y_bot, ay + 16, C_BORDER_CYAN)

    # -------------------------------------------------------------
    # 7. Player Inventory Frame: (0..175, 83..165)
    # Copied directly from reference_old/auto_fisher.png for 100% authenticity
    # -------------------------------------------------------------
    if ref_img:
        inv_crop = ref_img.crop((0, 83, 176, 166))
        img.paste(inv_crop, (0, 83))
    else:
        # Fallback standard MC inventory box
        rect(0, 83, 175, 165, (198, 198, 198, 255))
        hline(0, 175, 83, C_WHITE)
        vline(0, 83, 165, C_WHITE)
        hline(0, 175, 165, (85, 85, 85, 255))
        vline(175, 83, 165, (85, 85, 85, 255))
        for r in range(3):
            for c in range(9):
                sx, sy = 8 + c * 18, 84 + r * 18
                rect(sx, sy, sx + 17, sy + 17, (55, 55, 55, 255))
                rect(sx + 1, sy + 1, sx + 16, sy + 16, (139, 139, 139, 255))
                hline(sx + 1, sx + 16, sy + 16, C_WHITE)
                vline(sx + 16, sy + 1, sy + 16, C_WHITE)
        for c in range(9):
            sx, sy = 8 + c * 18, 142
            rect(sx, sy, sx + 17, sy + 17, (55, 55, 55, 255))
            rect(sx + 1, sy + 1, sx + 16, sy + 16, (139, 139, 139, 255))
            hline(sx + 1, sx + 16, sy + 16, C_WHITE)
            vline(sx + 16, sy + 1, sy + 16, C_WHITE)

    # -------------------------------------------------------------
    # SPRITES ZONE (x >= 176)
    # -------------------------------------------------------------

    # A. Energy Fill Sprite at (176, 0) [8x50]
    sp_ex, sp_ey = 176, 0
    # Gradient from bright cyan (top) to deep blue (bottom) matching reference
    for y in range(eh):
        ratio = y / float(eh)
        r = int(0 + 10 * ratio)
        g = int(229 - 140 * ratio)
        b = int(255 - 100 * ratio)
        col = (r, g, b, 255)
        hline(sp_ex, sp_ex + ew - 1, sp_ey + y, col)
        # 1px glowing white left edge
        px(sp_ex + 1, sp_ey + y, (220, 250, 255, 255))

    # B. Progress Arrow Fill Sprite at (176, 52) [24x17]
    # EXACT same geometry as unlit arrow, filled solidly with bright cyan/blue
    sp_ax, sp_ay = 176, 52
    for c in range(24):
        y_top, y_bot = get_arrow_span(c)
        for y in range(y_top, y_bot + 1):
            if y == y_top or y == y_bot or c == 0 or (c == 15 and (y <= 4 or y >= 12)):
                # Outer cyan border
                px(sp_ax + c, sp_ay + y, C_BORDER_CYAN)
            elif y == 8:
                # White-hot glowing core in center
                px(sp_ax + c, sp_ay + y, (240, 255, 255, 255))
            elif abs(y - 8) == 1:
                # Light cyan core
                px(sp_ax + c, sp_ay + y, (120, 230, 255, 255))
            else:
                # Solid blue/cyan fill
                px(sp_ax + c, sp_ay + y, (14, 165, 233, 255))

    os.makedirs(os.path.dirname(out_path), exist_ok=True)
    img.save(out_path, "PNG")
    print(f"OK generated Dredger v2 -> {out_path}")

    # Generate 3x preview in artifacts
    gui_crop = img.crop((0, 0, 176, 166))
    preview = gui_crop.resize((176 * 3, 166 * 3), Image.NEAREST)
    preview_path = r"C:\Users\xieto\.gemini\antigravity-ide\brain\531f1ed4-ea9c-4420-b760-5e17beab3bf0\seabed_dredger_v2_preview.png"
    preview.save(preview_path)
    print(f"OK generated v2 preview -> {preview_path}")

if __name__ == "__main__":
    out = r"d:\AquaTech\mods\aquatech-ui\src\main\resources\assets\aquatech_ui\textures\gui\seabed_dredger.png"
    create_dredger_v2(out)
