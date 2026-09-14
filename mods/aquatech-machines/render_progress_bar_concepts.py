#!/usr/bin/env python3
"""Generate pixel-perfect Minecraft GUI concept board for AquaTech machine progress bars.
Outputs a clean visual sheet comparing 4 distinct styles at 8x scale with 0%, 25%, 50%, 75%, 100% states.
"""
from PIL import Image, ImageDraw, ImageFont
from pathlib import Path

W_ARROW = 24
H_ARROW = 17
SCALE = 6
PADDING = 12

# Palette definition
BG_COLOR = (20, 24, 30, 255)
CARD_BG = (28, 34, 44, 255)
CARD_BORDER = (45, 55, 72, 255)
TEXT_COLOR = (220, 230, 245, 255)
SUBTEXT_COLOR = (140, 160, 185, 255)
ACCENT_CYAN = (0, 240, 255, 255)

# Mask for 24x17 standard Minecraft machine arrow
def create_arrow_mask():
    mask = Image.new("1", (W_ARROW, H_ARROW), 0)
    for y in range(H_ARROW):
        dy = abs(y - 8)
        # Arrow stem (left=0..14, y=4..12)
        if 4 <= y <= 12:
            for x in range(0, 15):
                mask.putpixel((x, y), 1)
        # Arrow head (triangle x=15..23)
        max_x = 23 - dy
        for x in range(15, max_x + 1):
            mask.putpixel((x, y), 1)
    return mask

ARROW_MASK = create_arrow_mask()

def render_style_a_laser(progress: float) -> Image.Image:
    """Style A: Luminous Neo-Cyan Laser Arrow."""
    im = Image.new("RGBA", (W_ARROW, H_ARROW), (0, 0, 0, 0))
    limit_x = int(W_ARROW * progress)
    for y in range(H_ARROW):
        dy = abs(y - 8)
        for x in range(W_ARROW):
            if not ARROW_MASK.getpixel((x, y)):
                continue
            is_border = False
            # Check if edge pixel
            for nx, ny in ((x-1,y), (x+1,y), (x,y-1), (x,y+1)):
                if 0 <= nx < W_ARROW and 0 <= ny < H_ARROW:
                    if not ARROW_MASK.getpixel((nx, ny)):
                        is_border = True
                        break
                else:
                    is_border = True
                    break
            
            if x <= limit_x and progress > 0:
                # Active filling
                if is_border:
                    im.putpixel((x, y), (0, 180, 220, 255))
                elif dy == 0:
                    im.putpixel((x, y), (240, 255, 255, 255)) # Pure white laser core
                elif dy == 1:
                    im.putpixel((x, y), (80, 245, 255, 255))  # Intense cyan glow
                elif dy == 2:
                    im.putpixel((x, y), (0, 195, 235, 255))   # Electric cyan
                else:
                    im.putpixel((x, y), (0, 140, 190, 255))   # Outer glow
            else:
                # Empty track
                if is_border:
                    im.putpixel((x, y), (14, 18, 24, 255))
                else:
                    val = 22 + dy * 2
                    im.putpixel((x, y), (val, val + 4, val + 8, 255))
    return im

def render_style_b_chevron(progress: float) -> Image.Image:
    """Style B: High-Tech Segmented Chevrons (4 distinct power chevrons)."""
    im = Image.new("RGBA", (W_ARROW, H_ARROW), (0, 0, 0, 0))
    limit_x = int(W_ARROW * progress)
    for y in range(H_ARROW):
        dy = abs(y - 8)
        for x in range(W_ARROW):
            if not ARROW_MASK.getpixel((x, y)):
                continue
            is_border = (x == 0 or not ARROW_MASK.getpixel((max(0, x-1), y)) or
                         not ARROW_MASK.getpixel((min(W_ARROW-1, x+1), y)) or
                         not ARROW_MASK.getpixel((x, max(0, y-1))) or
                         not ARROW_MASK.getpixel((x, min(H_ARROW-1, y+1))))
            
            # Segment dividers at x=5, x=11, x=17
            is_divider = (x - dy) in (3, 9, 15)
            
            if x <= limit_x and progress > 0:
                if is_border:
                    im.putpixel((x, y), (200, 80, 255, 255))
                elif is_divider:
                    im.putpixel((x, y), (50, 15, 75, 255))
                elif dy == 0:
                    im.putpixel((x, y), (255, 240, 255, 255))
                elif dy <= 2:
                    im.putpixel((x, y), (225, 100, 255, 255))
                else:
                    im.putpixel((x, y), (170, 40, 220, 255))
            else:
                if is_border:
                    im.putpixel((x, y), (16, 16, 22, 255))
                elif is_divider:
                    im.putpixel((x, y), (10, 10, 14, 255))
                else:
                    im.putpixel((x, y), (24, 24, 32, 255))
    return im

def render_style_c_hydro(progress: float) -> Image.Image:
    """Style C: Aqua Hydro Capsule (Liquid fluid with bubbles & meniscus)."""
    im = Image.new("RGBA", (W_ARROW, H_ARROW), (0, 0, 0, 0))
    limit_x = int(W_ARROW * progress)
    for y in range(H_ARROW):
        dy = abs(y - 8)
        for x in range(W_ARROW):
            if not ARROW_MASK.getpixel((x, y)):
                continue
            is_border = (x == 0 or not ARROW_MASK.getpixel((max(0, x-1), y)) or
                         not ARROW_MASK.getpixel((min(W_ARROW-1, x+1), y)) or
                         not ARROW_MASK.getpixel((x, max(0, y-1))) or
                         not ARROW_MASK.getpixel((x, min(H_ARROW-1, y+1))))
            
            # Glass tube rings at x=6, x=13
            is_ring = x in (6, 13) and 3 <= y <= 13
            
            if x <= limit_x and progress > 0:
                if is_ring:
                    im.putpixel((x, y), (120, 190, 210, 255))
                elif is_border:
                    im.putpixel((x, y), (0, 110, 150, 255))
                elif (x == limit_x or x == limit_x - 1) and dy <= 2:
                    # Meniscus wave front
                    im.putpixel((x, y), (220, 255, 255, 255))
                elif (x in (3, 9, 16) and y in (7, 9)):
                    # Water bubbles
                    im.putpixel((x, y), (210, 255, 255, 255))
                elif y <= 6:
                    # Glass reflection highlight
                    im.putpixel((x, y), (100, 230, 230, 255))
                elif y >= 11:
                    # Deeper water shadow
                    im.putpixel((x, y), (0, 120, 160, 255))
                else:
                    # Vibrant turquoise water
                    im.putpixel((x, y), (0, 190, 210, 255))
            else:
                if is_ring:
                    im.putpixel((x, y), (45, 60, 75, 255))
                elif is_border:
                    im.putpixel((x, y), (14, 20, 26, 255))
                elif y <= 6:
                    im.putpixel((x, y), (30, 42, 54, 255))
                else:
                    im.putpixel((x, y), (20, 28, 38, 255))
    return im

def render_style_d_gold_quantum(progress: float) -> Image.Image:
    """Style D: Golden Quantum Spark / Surge."""
    im = Image.new("RGBA", (W_ARROW, H_ARROW), (0, 0, 0, 0))
    limit_x = int(W_ARROW * progress)
    for y in range(H_ARROW):
        dy = abs(y - 8)
        for x in range(W_ARROW):
            if not ARROW_MASK.getpixel((x, y)):
                continue
            is_border = (x == 0 or not ARROW_MASK.getpixel((max(0, x-1), y)) or
                         not ARROW_MASK.getpixel((min(W_ARROW-1, x+1), y)) or
                         not ARROW_MASK.getpixel((x, max(0, y-1))) or
                         not ARROW_MASK.getpixel((x, min(H_ARROW-1, y+1))))
            
            if x <= limit_x and progress > 0:
                if is_border:
                    im.putpixel((x, y), (255, 180, 40, 255))
                elif dy == 0:
                    im.putpixel((x, y), (255, 255, 220, 255))
                elif dy == 1:
                    im.putpixel((x, y), (255, 220, 70, 255))
                elif dy <= 3:
                    im.putpixel((x, y), (240, 150, 20, 255))
                else:
                    im.putpixel((x, y), (180, 95, 0, 255))
            else:
                if is_border:
                    im.putpixel((x, y), (22, 18, 14, 255))
                else:
                    im.putpixel((x, y), (32, 28, 22, 255))
    return im

def main():
    styles = [
        ("Вариант 1: Neo-Cyan Laser (Luminous)", "Ультра-неоновый лазерный луч с белым ядром и циановым ореолом", render_style_a_laser),
        ("Вариант 2: Segmented Chevrons (Sci-Fi)", "4-ступенчатые неоновые шевроны с фиолетовой плазменной зарядкой", render_style_b_chevron),
        ("Вариант 3: Aqua Hydro Capsule (Ocean)", "Стеклянная гидро-колба с переливающейся бирюзовой водой и пузырьками", render_style_c_hydro),
        ("Вариант 4: Quantum Solar Surge (Gold)", "Квантовый золотисто-янтарный импульсный заряд с максимальной контрастностью", render_style_d_gold_quantum),
    ]
    
    stages = [0.0, 0.25, 0.50, 0.75, 1.0]
    stage_labels = ["0% (Покой)", "25%", "50%", "75%", "100% (Готово)"]
    
    # Dimensions
    card_w = (W_ARROW * SCALE + 18) * len(stages) + 40
    card_h = (H_ARROW * SCALE) + 70
    total_w = card_w + 60
    total_h = len(styles) * (card_h + 24) + 120
    
    board = Image.new("RGBA", (total_w, total_h), BG_COLOR)
    draw = ImageDraw.Draw(board)
    
    # Load TTF font
    try:
        font_title = ImageFont.truetype(r"C:\Windows\Fonts\segoeui.ttf", 20)
        font_sub = ImageFont.truetype(r"C:\Windows\Fonts\segoeui.ttf", 13)
        font_card_title = ImageFont.truetype(r"C:\Windows\Fonts\segoeuib.ttf", 16)
        font_card_desc = ImageFont.truetype(r"C:\Windows\Fonts\segoeui.ttf", 12)
        font_label = ImageFont.truetype(r"C:\Windows\Fonts\segoeui.ttf", 12)
    except Exception:
        font_title = ImageFont.load_default()
        font_sub = font_title
        font_card_title = font_title
        font_card_desc = font_title
        font_label = font_title

    # Header
    draw.text((30, 20), "AquaTech — Концепты прогресс-баров механизмов (Minecraft 1.20.1)", fill=TEXT_COLOR, font=font_title)
    draw.text((30, 50), "Размер спрайта: 24x17 px (масштаб превью 6x). Адаптировано под темный интерфейс LuminousUI.", fill=SUBTEXT_COLOR, font=font_sub)
    
    y_offset = 90
    for idx, (title, desc, renderer) in enumerate(styles):
        # Draw card background
        card_box = [30, y_offset, 30 + card_w, y_offset + card_h]
        draw.rectangle(card_box, fill=CARD_BG, outline=CARD_BORDER, width=2)
        
        # Style Title & Description
        draw.text((45, y_offset + 10), title, fill=ACCENT_CYAN, font=font_card_title)
        draw.text((45, y_offset + 32), desc, fill=SUBTEXT_COLOR, font=font_card_desc)
        
        # Render stages
        x_stage = 45
        for s_idx, (pct, label) in enumerate(zip(stages, stage_labels)):
            sprite = renderer(pct)
            sprite_scaled = sprite.resize((W_ARROW * SCALE, H_ARROW * SCALE), Image.NEAREST)
            
            # Draw slot/recess border behind sprite
            sx = x_stage
            sy = y_offset + 54
            draw.rectangle([sx - 2, sy - 2, sx + W_ARROW * SCALE + 1, sy + H_ARROW * SCALE + 1], outline=(15, 18, 24, 255), width=1)
            
            board.paste(sprite_scaled, (sx, sy), sprite_scaled)
            draw.text((sx + (W_ARROW * SCALE) // 2 - 20, sy + H_ARROW * SCALE + 6), label, fill=TEXT_COLOR, font=font_label)
            x_stage += W_ARROW * SCALE + 24
            
        y_offset += card_h + 24
        
    out_path = Path(r"C:\Users\xieto\.gemini\antigravity-ide\brain\e8997355-62d0-4496-aa07-dc7f04f75423\progress_bar_pixel_concepts.png")
    board.save(out_path, format="PNG")
    print("Saved pixel concept board to:", out_path)

if __name__ == "__main__":
    main()
