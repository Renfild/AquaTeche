#!/usr/bin/env python3
r"""Generate two Apple-inspired, ultra-minimalist, modern TAB overlay concepts for AquaTech.

Fixes alpha blending by using proper Image.alpha_composite for all translucent glass layers.

Concept A: 'macOS Liquid Glass' — Sectioned clean list (Staff, VIP, Players), quiet typography, subtle hover strips.
Concept B: 'Apple Bento Minimal' — Top status widgets + 4-column ultra-clean squircle cards.
"""
import os
from PIL import Image, ImageDraw, ImageFont, ImageFilter

def draw_glass_rect(base_img, box, radius, fill_rgba, outline_rgba=None, width=1):
    overlay = Image.new("RGBA", base_img.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(overlay)
    d.rounded_rectangle(box, radius=radius, fill=fill_rgba, outline=outline_rgba, width=width)
    return Image.alpha_composite(base_img, overlay)

def generate_concepts():
    bg_shot = r"d:\AquaTech\scratch\promo_src\shots\Screenshot 2026-08-31 183721.jpg"
    logo_path = r"C:\Users\xieto\.gemini\antigravity-ide\brain\531f1ed4-ea9c-4420-b760-5e17beab3bf0\aquatech_logo_master.png"

    if os.path.exists(bg_shot):
        raw_bg = Image.open(bg_shot).convert("RGBA").resize((1920, 1080), Image.LANCZOS)
    else:
        raw_bg = Image.new("RGBA", (1920, 1080), (10, 20, 30, 255))

    # Dark atmospheric blur
    blurred = raw_bg.filter(ImageFilter.GaussianBlur(30))
    tint = Image.new("RGBA", (1920, 1080), (4, 8, 14, 215))
    base_canvas = Image.alpha_composite(blurred, tint)

    syne_bold = r"d:\AquaTech\launcher\src\AquaTechLauncher\Assets\Fonts\syne-800.ttf"
    f_title = ImageFont.truetype(syne_bold, 24) if os.path.exists(syne_bold) else ImageFont.truetype("arialbd.ttf", 24)
    f_sub = ImageFont.truetype("arial.ttf", 13)
    f_sec = ImageFont.truetype("arialbd.ttf", 11)
    f_name = ImageFont.truetype("arialbd.ttf", 14)
    f_meta = ImageFont.truetype("arial.ttf", 12)
    f_pill = ImageFont.truetype("arialbd.ttf", 11)
    f_foot = ImageFont.truetype("arial.ttf", 12)

    logo_im = Image.open(logo_path).convert("RGBA").resize((38, 38), Image.LANCZOS) if os.path.exists(logo_path) else None

    staff = [
        ("Renfild", "OWNER", (239, 68, 68), 12),
        ("AquaLord", "ADMIN", (248, 113, 113), 18),
        ("AbyssalEcho", "DEV", (168, 85, 247), 15),
        ("ShadowRay", "HELPER", (34, 197, 94), 20)
    ]
    vips = [
        ("OceanMaster", "MAGNATE", (234, 179, 8), 24),
        ("CyberFisher", "MVP+", (6, 182, 212), 28),
        ("DeepSeeker", "VIP+", (59, 130, 246), 32),
        ("Nautilus_99", "VIP", (96, 165, 250), 36)
    ]
    players = [
        ("BubbleCrafter", "PLAYER", (148, 163, 184), 42),
        ("SeaBreeze", "PLAYER", (148, 163, 184), 48),
        ("TritonHunter", "PLAYER", (148, 163, 184), 55),
        ("CoralViper", "PLAYER", (148, 163, 184), 64),
        ("HydroKinetics", "PLAYER", (148, 163, 184), 38),
        ("Submariner", "PLAYER", (148, 163, 184), 72),
        ("StormSurge", "PLAYER", (148, 163, 184), 81),
        ("AbyssDiver", "PLAYER", (148, 163, 184), 63)
    ]

    # ═════════════════════════════════════════════════════════════════════════
    # CONCEPT A: macOS Liquid Glass (Sectioned Minimal List)
    # ═════════════════════════════════════════════════════════════════════════
    canvas_a = base_canvas.copy()
    PW, PH = 1060, 680
    px = (1920 - PW) // 2
    py = (1080 - PH) // 2

    # Frosted glass modal (Pure Apple dark mode)
    canvas_a = draw_glass_rect(canvas_a, [px, py, px + PW, py + PH], 22, (10, 16, 26, 235), (255, 255, 255, 30), 1)
    # Inner specular rim
    canvas_a = draw_glass_rect(canvas_a, [px + 1, py + 1, px + PW - 1, py + 180], 21, (0, 0, 0, 0), (255, 255, 255, 12), 1)

    draw_a = ImageDraw.Draw(canvas_a)

    # Header
    hx = px + 32
    hy = py + 24
    if logo_im:
        canvas_a.paste(logo_im, (hx, hy - 4), logo_im)

    draw_a.text((hx + 50, hy - 6), "AquaTech", font=f_title, fill=(255, 255, 255, 255))
    draw_a.text((hx + 52, hy + 22), "Ocean Skyblock · 1.20.1", font=f_sub, fill=(148, 163, 184, 255))

    # Header stats (whisper quiet indicators)
    stats = [("38 онлайн", (34, 197, 94)), ("20.0 TPS", (56, 189, 248)), ("14 ms", (148, 163, 184))]
    rx = px + PW - 32
    for label, dot_col in reversed(stats):
        bw = f_meta.getbbox(label)[2] - f_meta.getbbox(label)[0]
        draw_a.ellipse([rx - bw - 14, hy + 12, rx - bw - 6, hy + 20], fill=dot_col)
        draw_a.text((rx - bw, hy + 8), label, font=f_meta, fill=(203, 213, 225, 255))
        rx -= (bw + 34)

    # Hairline divider
    draw_a.line([px + 32, py + 74, px + PW - 32, py + 74], fill=(255, 255, 255, 16), width=1)

    # Two columns: Left = Staff + VIP, Right = Players
    col_w = (PW - 64 - 24) // 2
    
    def render_minimal_row(canvas, dr, p_info, x, y, w):
        name, title, col, ping = p_info
        # Subtly elevated hover strip (5% white opacity)
        canvas = draw_glass_rect(canvas, [x, y, x + w, y + 42], 10, (255, 255, 255, 12), (255, 255, 255, 14), 1)
        dr = ImageDraw.Draw(canvas)
        # Squircle Avatar
        av_x, av_y = x + 8, y + 7
        dr.rounded_rectangle([av_x, av_y, av_x + 28, av_y + 28], radius=7, fill=(24, 32, 47), outline=col + (120,), width=1)
        # Face details
        dr.rectangle([av_x + 6, av_y + 6, av_x + 22, av_y + 22], fill=(226, 178, 138))
        dr.rectangle([av_x + 8, av_y + 10, av_x + 12, av_y + 13], fill=(30, 58, 138))
        dr.rectangle([av_x + 16, av_y + 10, av_x + 20, av_y + 13], fill=(30, 58, 138))
        # Name
        dr.text((av_x + 36, y + 12), name, font=f_name, fill=(241, 245, 249, 255))
        # Capsule Rank Pill
        pill_w = f_pill.getbbox(title)[2] - f_pill.getbbox(title)[0] + 16
        pill_x = av_x + 36 + f_name.getbbox(name)[2] - f_name.getbbox(name)[0] + 12
        dr.rounded_rectangle([pill_x, y + 12, pill_x + pill_w, y + 30], radius=9, fill=col + (28,), outline=col + (90,), width=1)
        dr.text((pill_x + 8, y + 14), title, font=f_pill, fill=col + (240,))
        # Ping
        p_str = f"{ping} ms"
        pw = f_meta.getbbox(p_str)[2] - f_meta.getbbox(p_str)[0]
        dr.text((x + w - 14 - pw, y + 13), p_str, font=f_meta, fill=(100, 116, 139, 255))
        return canvas

    # Left Column: Staff (4) then VIPs (4)
    lx = px + 32
    draw_a.text((lx, py + 90), "КОМАНДА ПРОЕКТА", font=f_sec, fill=(100, 116, 139, 255))
    for i, p in enumerate(staff):
        canvas_a = render_minimal_row(canvas_a, draw_a, p, lx, py + 112 + i * 48, col_w)

    draw_a = ImageDraw.Draw(canvas_a)
    draw_a.text((lx, py + 320), "СПОНСОРЫ & VIP", font=f_sec, fill=(100, 116, 139, 255))
    for i, p in enumerate(vips):
        canvas_a = render_minimal_row(canvas_a, draw_a, p, lx, py + 342 + i * 48, col_w)

    # Right Column: Players (8)
    rx_col = px + 32 + col_w + 24
    draw_a = ImageDraw.Draw(canvas_a)
    draw_a.text((rx_col, py + 90), "ИГРОКИ ОНЛАЙН", font=f_sec, fill=(100, 116, 139, 255))
    for i, p in enumerate(players):
        canvas_a = render_minimal_row(canvas_a, draw_a, p, rx_col, py + 112 + i * 48, col_w)

    # Footer
    draw_a = ImageDraw.Draw(canvas_a)
    draw_a.line([px + 32, py + PH - 46, px + PW - 32, py + PH - 46], fill=(255, 255, 255, 14), width=1)
    draw_a.text((px + 36, py + PH - 32), "aquateche.store · discord.gg/aquatech", font=f_foot, fill=(100, 116, 139, 255))
    hint_a = "[F4] Меню сервера  ·  [TAB] Закрыть"
    hw_a = f_foot.getbbox(hint_a)[2] - f_foot.getbbox(hint_a)[0]
    draw_a.text((px + PW - 36 - hw_a, py + PH - 32), hint_a, font=f_foot, fill=(100, 116, 139, 255))

    out_a = r"C:\Users\xieto\.gemini\antigravity-ide\brain\531f1ed4-ea9c-4420-b760-5e17beab3bf0\aquatech_tab_apple_liquid.png"
    canvas_a.save(out_a, "PNG")
    print("OK Concept A ->", out_a)

    # ═════════════════════════════════════════════════════════════════════════
    # CONCEPT B: Apple Bento Minimal (Widgets + 4-Column Floating Cards)
    # ═════════════════════════════════════════════════════════════════════════
    canvas_b = base_canvas.copy()
    BW, BH = 1140, 680
    bx = (1920 - BW) // 2
    by = (1080 - BH) // 2

    # Main container
    canvas_b = draw_glass_rect(canvas_b, [bx, by, bx + BW, by + BH], 24, (10, 15, 25, 235), (255, 255, 255, 26), 1)

    # Top Bento Widgets (3 widgets)
    w1_w = 400
    canvas_b = draw_glass_rect(canvas_b, [bx + 20, by + 20, bx + 20 + w1_w, by + 90], 16, (255, 255, 255, 8), (255, 255, 255, 16), 1)
    if logo_im:
        canvas_b.paste(logo_im, (bx + 34, by + 36), logo_im)
    draw_b = ImageDraw.Draw(canvas_b)
    draw_b.text((bx + 84, by + 34), "AquaTech", font=f_title, fill=(255, 255, 255, 255))
    draw_b.text((bx + 86, by + 60), "Океанский Skyblock · 1.20.1", font=f_sub, fill=(148, 163, 184, 255))

    w2_x = bx + 20 + w1_w + 14
    w2_w = 340
    canvas_b = draw_glass_rect(canvas_b, [w2_x, by + 20, w2_x + w2_w, by + 90], 16, (255, 255, 255, 8), (255, 255, 255, 16), 1)
    draw_b = ImageDraw.Draw(canvas_b)
    draw_b.text((w2_x + 18, by + 32), "СОСТОЯНИЕ СЕРВЕРА", font=f_sec, fill=(100, 116, 139, 255))
    draw_b.text((w2_x + 18, by + 54), "🟢 38 игроков", font=f_name, fill=(241, 245, 249, 255))
    draw_b.text((w2_x + 190, by + 54), "⚡ 20.0 TPS", font=f_name, fill=(56, 189, 248, 255))

    w3_x = w2_x + w2_w + 14
    w3_w = bx + BW - 20 - w3_x
    canvas_b = draw_glass_rect(canvas_b, [w3_x, by + 20, w3_x + w3_w, by + 90], 16, (255, 255, 255, 8), (255, 255, 255, 16), 1)
    draw_b = ImageDraw.Draw(canvas_b)
    draw_b.text((w3_x + 18, by + 32), "ТВОЯ СЕТЬ", font=f_sec, fill=(100, 116, 139, 255))
    draw_b.text((w3_x + 18, by + 54), "📶 14 ms · Отличное", font=f_name, fill=(34, 197, 94, 255))

    # 4-Column Minimal Player Grid (24 Cards)
    grid_y = by + 108
    cols_b = 4
    cw_b = (BW - 40 - (cols_b - 1) * 12) // cols_b
    card_h_b = 64

    all_cards = staff + vips + players + players[:8]

    for idx, p in enumerate(all_cards[:24]):
        c = idx % cols_b
        r = idx // cols_b
        cx = bx + 20 + c * (cw_b + 12)
        cy = grid_y + r * (card_h_b + 10)

        name, title, col, ping = p
        # Translucent glass card (12% white opacity)
        canvas_b = draw_glass_rect(canvas_b, [cx, cy, cx + cw_b, cy + card_h_b], 14, (255, 255, 255, 11), (255, 255, 255, 14), 1)
        dr_c = ImageDraw.Draw(canvas_b)
        # Small colored accent dot for rank
        dr_c.ellipse([cx + 10, cy + 12, cx + 18, cy + 20], fill=col)

        # Avatar 32x32
        av_x = cx + 24
        av_y = cy + 16
        dr_c.rounded_rectangle([av_x, av_y, av_x + 32, av_y + 32], radius=8, fill=(24, 32, 47), outline=col + (100,), width=1)
        dr_c.rectangle([av_x + 6, av_y + 6, av_x + 26, av_y + 26], fill=(226, 178, 138))
        dr_c.rectangle([av_x + 8, av_y + 11, av_x + 13, av_y + 15], fill=(30, 58, 138))
        dr_c.rectangle([av_x + 19, av_y + 11, av_x + 24, av_y + 15], fill=(30, 58, 138))

        # Name + Rank
        dr_c.text((av_x + 40, cy + 14), name, font=f_name, fill=(241, 245, 249, 255))
        dr_c.text((av_x + 40, cy + 34), title, font=f_pill, fill=col)

        # Ping
        p_str = f"{ping}ms"
        p_w = f_meta.getbbox(p_str)[2] - f_meta.getbbox(p_str)[0]
        dr_c.text((cx + cw_b - 12 - p_w, cy + 34), p_str, font=f_meta, fill=(100, 116, 139, 255))

    # Footer
    draw_b = ImageDraw.Draw(canvas_b)
    draw_b.text((bx + 24, by + BH - 28), "aquateche.store", font=f_foot, fill=(100, 116, 139, 255))
    hint_b = "[F4] Меню сервера  ·  [TAB] Закрыть"
    hw_b = f_foot.getbbox(hint_b)[2] - f_foot.getbbox(hint_b)[0]
    draw_b.text((bx + BW - 24 - hw_b, by + BH - 28), hint_b, font=f_foot, fill=(100, 116, 139, 255))

    out_b = r"C:\Users\xieto\.gemini\antigravity-ide\brain\531f1ed4-ea9c-4420-b760-5e17beab3bf0\aquatech_tab_apple_bento.png"
    canvas_b.save(out_b, "PNG")
    print("OK Concept B ->", out_b)

    # Copy to Desktop for user review
    desktop_a = r"C:\Users\xieto\Desktop\aquatech_tab_concept_A_liquid.png"
    desktop_b = r"C:\Users\xieto\Desktop\aquatech_tab_concept_B_bento.png"
    canvas_a.save(desktop_a, "PNG")
    canvas_b.save(desktop_b, "PNG")
    print(f"OK copied to desktop:\n  {desktop_a}\n  {desktop_b}")

if __name__ == "__main__":
    generate_concepts()
