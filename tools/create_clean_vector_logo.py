#!/usr/bin/env python3
"""Generate a clean, handcrafted 2D vector graphic-design logo for AquaTech on pure black (#000000).

No AI-slop, no glossy plastic glare, no blurry airbrushed glows:
- Uses Syne-800 (clean bold geometric font).
- Handcrafted vector Oceanic Prism / Trident emblem on the left.
- 2-tone crisp cel-shaded fill (luminous cyan #00E5FF on top, deep ocean teal #0B6488 on bottom).
- Crisp 2px white bevel highlight on top edges.
- Bold dark navy / black sticker outline wrapping the entire logo.
"""
import os
from PIL import Image, ImageDraw, ImageFont

def make_clean_logo(out_path: str):
    W, H = 1000, 360
    img = Image.new("RGBA", (W, H), (0, 0, 0, 255))
    draw = ImageDraw.Draw(img)

    # Load Syne font
    font_path = r"d:\AquaTech\launcher\src\AquaTechLauncher\Assets\Fonts\syne-800.ttf"
    font_size = 110
    font = ImageFont.truetype(font_path, font_size)

    # Palette
    C_BG       = (0, 0, 0, 255)
    C_OUTLINE  = (4, 14, 24, 255)     # Deep dark navy sticker outline
    C_TOP_CYAN = (0, 229, 255, 255)   # #00E5FF
    C_BOT_TEAL = (11, 100, 136, 255)  # #0B6488
    C_MID_CYAN = (0, 180, 220, 255)
    C_WHITE    = (255, 255, 255, 255)
    C_EXTRUDE  = (7, 45, 68, 255)

    text = "AQUATECH"
    
    # 1. Measure text
    bbox = font.getbbox(text)
    tw = bbox[2] - bbox[0]
    th = bbox[3] - bbox[1]

    # Total width with emblem (110px emblem + 30px gap + tw)
    emblem_w = 110
    gap = 30
    total_w = emblem_w + gap + tw
    start_x = (W - total_w) // 2
    emblem_x = start_x
    emblem_y = (H - 120) // 2
    text_x = start_x + emblem_w + gap
    text_y = (H - th) // 2 - 10

    # 2. Draw 2.5D clean extrusion layers (offset downwards by 8px)
    for dy in range(10, 0, -1):
        # Draw outline of extruded text
        draw.text((text_x, text_y + dy), text, font=font, fill=C_EXTRUDE, stroke_width=6, stroke_fill=C_OUTLINE)

    # 3. Draw bold sticker outline for text
    draw.text((text_x, text_y), text, font=font, fill=C_BOT_TEAL, stroke_width=8, stroke_fill=C_OUTLINE)

    # 4. Cel-shaded text fill (mask split into upper cyan and lower teal)
    text_mask = Image.new("L", (W, H), 0)
    m_draw = ImageDraw.Draw(text_mask)
    m_draw.text((text_x, text_y), text, font=font, fill=255)

    # Create gradient / split image
    grad_img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    g_draw = ImageDraw.Draw(grad_img)
    split_y = text_y + int(th * 0.48)

    # Upper half: bright cyan
    g_draw.rectangle([text_x - 10, text_y - 10, text_x + tw + 10, split_y], fill=C_TOP_CYAN)
    # Lower half: deep oceanic teal
    g_draw.rectangle([text_x - 10, split_y + 1, text_x + tw + 10, text_y + th + 20], fill=C_BOT_TEAL)
    # Split transition line
    g_draw.line([text_x - 10, split_y, text_x + tw + 10, split_y], fill=C_MID_CYAN, width=2)

    # Apply text mask
    img.paste(grad_img, (0, 0), text_mask)

    # 5. Crisp top bevel highlight line (1px offset white)
    hi_mask = Image.new("L", (W, H), 0)
    hi_draw = ImageDraw.Draw(hi_mask)
    hi_draw.text((text_x, text_y - 2), text, font=font, fill=255)
    # Subtract original to keep only top rim
    base_mask = Image.new("L", (W, H), 0)
    ImageDraw.Draw(base_mask).text((text_x, text_y), text, font=font, fill=255)
    
    # 6. Handcrafted Geometric Emblem on the left (Oceanic Prism Trident)
    # Clean vector polygons:
    ecx = emblem_x + emblem_w // 2
    ecy = emblem_y + 60
    
    # Emblem extrusion
    for edy in range(8, 0, -1):
        # Outer shield/crystal shadow
        pts_shadow = [
            (ecx, ecy - 60 + edy),
            (ecx + 46, ecy - 16 + edy),
            (ecx + 36, ecy + 46 + edy),
            (ecx, ecy + 64 + edy),
            (ecx - 36, ecy + 46 + edy),
            (ecx - 46, ecy - 16 + edy)
        ]
        draw.polygon(pts_shadow, fill=C_OUTLINE)

    # Main emblem crystal polygon
    pts_main = [
        (ecx, ecy - 60),
        (ecx + 46, ecy - 16),
        (ecx + 36, ecy + 46),
        (ecx, ecy + 64),
        (ecx - 36, ecy + 46),
        (ecx - 46, ecy - 16)
    ]
    draw.polygon(pts_main, fill=C_BOT_TEAL, outline=C_OUTLINE, width=4)

    # Inner facets of the prism
    # Top-left facet (light)
    draw.polygon([(ecx, ecy - 56), (ecx, ecy + 10), (ecx - 40, ecy - 14)], fill=C_TOP_CYAN)
    # Top-right facet (mid)
    draw.polygon([(ecx, ecy - 56), (ecx + 40, ecy - 14), (ecx, ecy + 10)], fill=C_MID_CYAN)
    # Bottom-left facet
    draw.polygon([(ecx, ecy + 10), (ecx - 32, ecy + 42), (ecx, ecy + 58)], fill=C_BOT_TEAL)
    # Bottom-right facet
    draw.polygon([(ecx, ecy + 10), (ecx, ecy + 58), (ecx + 32, ecy + 42)], fill=(7, 65, 90, 255))
    # Inner energy core / trident node
    draw.polygon([(ecx, ecy - 20), (ecx + 14, ecy + 4), (ecx, ecy + 24), (ecx - 14, ecy + 4)], fill=C_WHITE)

    # Save
    os.makedirs(os.path.dirname(out_path), exist_ok=True)
    img.save(out_path, "PNG")
    print(f"OK generated handcrafted vector logo -> {out_path}")

if __name__ == "__main__":
    out = r"C:\Users\xieto\.gemini\antigravity-ide\brain\531f1ed4-ea9c-4420-b760-5e17beab3bf0\aquatech_handcrafted_logo.png"
    make_clean_logo(out)
