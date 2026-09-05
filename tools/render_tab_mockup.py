#!/usr/bin/env python3
r"""Generate a high-fidelity in-game UI/UX mockup of the proposed new AquaTech TAB overlay.

Uses:
- Real in-game gameplay background screenshot with gaussian blur & dark vignette.
- Real new AquaTech logo emblem (transparent PNG).
- Real rank icons from aquatech_ui (owner.png, admin.png, dev.png, mvp.png, vip.png, player.png).
- Syne font for modern display branding and crisp typography.
"""
import os
import math
from PIL import Image, ImageDraw, ImageFont, ImageFilter

def render_tab_mockup(out_path: str):
    # 1. Base Game Screen (1920x1080)
    bg_shot = r"d:\AquaTech\scratch\promo_src\shots\Screenshot 2026-08-31 183721.jpg"
    if os.path.exists(bg_shot):
        canvas = Image.open(bg_shot).convert("RGBA").resize((1920, 1080), Image.LANCZOS)
    else:
        canvas = Image.new("RGBA", (1920, 1080), (10, 20, 30, 255))

    # Apply authentic Minecraft GUI background blur & dark atmospheric tint
    blurred_bg = canvas.filter(ImageFilter.GaussianBlur(18))
    dark_tint = Image.new("RGBA", (1920, 1080), (4, 10, 18, 190))
    canvas = Image.alpha_composite(blurred_bg, dark_tint)
    draw = ImageDraw.Draw(canvas)

    # 2. TAB Modal Geometry
    MODAL_W = 1200
    MODAL_H = 720
    mx = (1920 - MODAL_W) // 2
    my = (1080 - MODAL_H) // 2

    # Outer Neon Glow
    for i in range(12, 0, -2):
        alpha = int(25 * (1.0 - i / 12.0))
        glow_color = (0, 240, 255, alpha)
        draw.rounded_rectangle([mx - i, my - i, mx + MODAL_W + i, my + MODAL_H + i], radius=16 + i, outline=glow_color, width=2)

    # Frosted Panel Surface
    panel_surface = Image.new("RGBA", (MODAL_W, MODAL_H), (8, 16, 26, 240))
    p_draw = ImageDraw.Draw(panel_surface)
    p_draw.rounded_rectangle([0, 0, MODAL_W, MODAL_H], radius=14, fill=(8, 16, 26, 242), outline=(0, 240, 255, 160), width=2)
    # Inner subtle rim
    p_draw.rounded_rectangle([2, 2, MODAL_W - 2, MODAL_H - 2], radius=12, outline=(255, 255, 255, 22), width=1)
    canvas.paste(panel_surface, (mx, my), panel_surface)

    # Fonts
    syne_bold = r"d:\AquaTech\launcher\src\AquaTechLauncher\Assets\Fonts\syne-800.ttf"
    syne_med  = r"d:\AquaTech\launcher\src\AquaTechLauncher\Assets\Fonts\syne-700.ttf"
    f_title   = ImageFont.truetype(syne_bold, 28)
    f_sub     = ImageFont.truetype(syne_med, 14)
    f_pill    = ImageFont.truetype("arialbd.ttf", 13)
    f_name    = ImageFont.truetype("arialbd.ttf", 15)
    f_meta    = ImageFont.truetype("arial.ttf", 12)
    f_foot    = ImageFont.truetype("arialbd.ttf", 13)

    # ═════════════════════════════════════════════════════════════════════════
    # HEADER
    # ═════════════════════════════════════════════════════════════════════════
    hdr_y = my + 18
    hdr_left = mx + 24
    hdr_right = mx + MODAL_W - 24

    # New Official Logo Icon
    logo_path = r"C:\Users\xieto\.gemini\antigravity-ide\brain\531f1ed4-ea9c-4420-b760-5e17beab3bf0\aquatech_logo_master.png"
    if os.path.exists(logo_path):
        logo_im = Image.open(logo_path).convert("RGBA").resize((52, 52), Image.LANCZOS)
        canvas.paste(logo_im, (hdr_left, hdr_y - 2), logo_im)

    # Title & Subtitle
    draw.text((hdr_left + 62, hdr_y - 2), "AQUATECH", font=f_title, fill=(0, 240, 255, 255))
    draw.text((hdr_left + 64, hdr_y + 28), "OCEAN SKYBLOCK · MC 1.20.1", font=f_sub, fill=(120, 165, 190, 255))

    # Right Stat Pills
    pills = [
        ("🟢 38 / 100 ОНЛАЙН", (16, 185, 129), (6, 78, 59, 180)),
        ("⚡ 20.0 TPS", (56, 189, 248), (12, 74, 110, 180)),
        ("📶 14 ms", (74, 222, 128), (20, 83, 45, 180)),
        ("💎 24,500 ⛁", (245, 158, 11), (120, 53, 15, 180))
    ]

    cur_rx = hdr_right
    for label, fg_col, bg_col in reversed(pills):
        bbox = f_pill.getbbox(label)
        pw = bbox[2] - bbox[0] + 20
        ph = 28
        px = cur_rx - pw
        py = hdr_y + 10
        # draw pill
        draw.rounded_rectangle([px, py, px + pw, py + ph], radius=14, fill=bg_col, outline=fg_col + (140,), width=1)
        draw.text((px + 10, py + 6), label, font=f_pill, fill=fg_col + (255,))
        cur_rx = px - 10

    # Header Divider Line (Neon gradient style)
    div_y = my + 76
    for dx in range(MODAL_W - 48):
        t = dx / float(MODAL_W - 48)
        alpha = int(180 * (math.sin(t * math.pi)))
        draw.point((mx + 24 + dx, div_y), fill=(0, 240, 255, alpha))

    # ═════════════════════════════════════════════════════════════════════════
    # PLAYER CARDS GRID (3 Columns x 6 Rows = 18 Players Preview)
    # ═════════════════════════════════════════════════════════════════════════
    grid_top = div_y + 14
    cols = 3
    col_w = (MODAL_W - 48 - (cols - 1) * 12) // cols
    card_h = 76

    ranks_dir = r"d:\AquaTech\mods\aquatech-ui\src\main\resources\assets\aquatech_ui\textures\ranks"

    mock_players = [
        {"name": "Renfild",      "rank": "owner",   "title": "OWNER",   "col": (239, 68, 68),  "ping": 12, "ping_bars": 5, "stat": "Аквамаг · 85 ур."},
        {"name": "AquaLord",     "rank": "admin",   "title": "ADMIN",   "col": (248, 113, 113), "ping": 18, "ping_bars": 5, "stat": "Куратор · 74 ур."},
        {"name": "AbyssalEcho",  "rank": "dev",     "title": "DEV",     "col": (168, 85, 247),  "ping": 15, "ping_bars": 5, "stat": "Разработчик"},
        {"name": "OceanMaster",  "rank": "magnate", "title": "MAGNATE", "col": (234, 179, 8),   "ping": 24, "ping_bars": 4, "stat": "Торговец · 62 ур."},
        {"name": "CyberFisher",  "rank": "mvp",     "title": "MVP+",    "col": (6, 182, 212),   "ping": 28, "ping_bars": 4, "stat": "Глубоководник · 51 ур."},
        {"name": "DeepSeeker",   "rank": "vip",     "title": "VIP+",    "col": (59, 130, 246),  "ping": 32, "ping_bars": 4, "stat": "Искатель · 43 ур."},
        {"name": "Nautilus_99",  "rank": "vip",     "title": "VIP",     "col": (96, 165, 250),  "ping": 36, "ping_bars": 4, "stat": "Рыболов · 39 ур."},
        {"name": "ShadowRay",    "rank": "helper",  "title": "HELPER",  "col": (34, 197, 94),   "ping": 20, "ping_bars": 5, "stat": "Помощник"},
        {"name": "BubbleCrafter","rank": "player",  "title": "PLAYER",  "col": (148, 163, 184), "ping": 42, "ping_bars": 4, "stat": "Авантюрист · 28 ур."},
        {"name": "SeaBreeze",    "rank": "player",  "title": "PLAYER",  "col": (148, 163, 184), "ping": 48, "ping_bars": 3, "stat": "Новичок · 15 ур."},
        {"name": "TritonHunter", "rank": "player",  "title": "PLAYER",  "col": (148, 163, 184), "ping": 55, "ping_bars": 3, "stat": "Рыболов · 22 ур."},
        {"name": "CoralViper",   "rank": "player",  "title": "PLAYER",  "col": (148, 163, 184), "ping": 64, "ping_bars": 3, "stat": "Новичок · 9 ур."},
        {"name": "HydroKinetics","rank": "player",  "title": "PLAYER",  "col": (148, 163, 184), "ping": 38, "ping_bars": 4, "stat": "Инженер · 33 ур."},
        {"name": "Submariner",   "rank": "player",  "title": "PLAYER",  "col": (148, 163, 184), "ping": 72, "ping_bars": 3, "stat": "Дайвер · 19 ур."},
        {"name": "StormSurge",   "rank": "player",  "title": "PLAYER",  "col": (148, 163, 184), "ping": 81, "ping_bars": 2, "stat": "Новичок · 6 ур."}
    ]

    for idx, p in enumerate(mock_players):
        c = idx % cols
        r = idx // cols
        cx = mx + 24 + c * (col_w + 12)
        cy = grid_top + r * (card_h + 10)

        # Card Frame
        card_bg = (12, 24, 38, 220)
        border_col = p["col"] + (110,)
        draw.rounded_rectangle([cx, cy, cx + col_w, cy + card_h], radius=8, fill=card_bg, outline=border_col, width=1)
        # Left Accent Rank Strip
        draw.rounded_rectangle([cx, cy, cx + 4, cy + card_h], radius=2, fill=p["col"] + (255,))

        # Simulated 8-bit Minecraft Player Head (38x38)
        head_x = cx + 12
        head_y = cy + (card_h - 40) // 2
        # Head background base
        draw.rounded_rectangle([head_x, head_y, head_x + 40, head_y + 40], radius=5, fill=(30, 41, 59), outline=p["col"] + (180,), width=1)
        # Pixel face placeholder pattern
        draw.rectangle([head_x + 8, head_y + 8, head_x + 32, head_y + 32], fill=(226, 178, 138))
        draw.rectangle([head_x + 10, head_y + 14, head_x + 16, head_y + 18], fill=(30, 58, 138)) # Eyes
        draw.rectangle([head_x + 24, head_y + 14, head_x + 30, head_y + 18], fill=(30, 58, 138))
        draw.rectangle([head_x + 14, head_y + 24, head_x + 26, head_y + 27], fill=(136, 19, 55)) # Smile
        # Online dot
        draw.ellipse([head_x + 32, head_y + 32, head_x + 42, head_y + 42], fill=(16, 185, 129), outline=(12, 24, 38), width=2)

        # Content Position
        tx = head_x + 50
        ty = cy + 12

        # Rank Badge Icon
        rank_icon_path = os.path.join(ranks_dir, f"{p['rank']}.png")
        if os.path.exists(rank_icon_path):
            r_icon = Image.open(rank_icon_path).convert("RGBA").resize((16, 16), Image.NEAREST)
            canvas.paste(r_icon, (tx, ty - 1), r_icon)
            tx += 20

        # Player Name with Rank Color
        draw.text((tx, ty - 2), p["name"], font=f_name, fill=p["col"] + (255,))

        # Subtitle / Role / Status
        draw.text((head_x + 50, ty + 20), p["stat"], font=f_meta, fill=(148, 163, 184, 255))

        # Right side: Ping and Wifi Bars
        rx = cx + col_w - 14
        ping_str = f"{p['ping']}ms"
        p_bbox = f_meta.getbbox(ping_str)
        pw = p_bbox[2] - p_bbox[0]
        draw.text((rx - pw, ty + 20), ping_str, font=f_meta, fill=(100, 140, 160, 255))

        # 5-bar connection indicator
        bx = rx - 2
        for bar in range(5):
            bh = 3 + bar * 2.5
            b_color = (16, 185, 129, 255) if bar < p["ping_bars"] else (51, 65, 85, 255)
            draw.rectangle([bx - (4 - bar) * 4, ty + 12 - bh, bx - (4 - bar) * 4 + 2, ty + 12], fill=b_color)

    # ═════════════════════════════════════════════════════════════════════════
    # FOOTER
    # ═════════════════════════════════════════════════════════════════════════
    foot_y = my + MODAL_H - 38
    # Divider
    for dx in range(MODAL_W - 48):
        t = dx / float(MODAL_W - 48)
        alpha = int(120 * (math.sin(t * math.pi)))
        draw.point((mx + 24 + dx, foot_y - 8), fill=(0, 240, 255, alpha))

    # Links
    draw.text((mx + 28, foot_y), "🌐 aquateche.store", font=f_foot, fill=(0, 240, 255, 255))
    draw.text((mx + 175, foot_y), "·   💬 discord.gg/aquatech", font=f_foot, fill=(148, 163, 184, 255))

    # Hotkey Hints
    hint_text = "[F4] Меню сервера  ·  [F2] Скриншот  ·  [TAB] Закрыть"
    h_bbox = f_foot.getbbox(hint_text)
    hw = h_bbox[2] - h_bbox[0]
    draw.text((mx + MODAL_W - 28 - hw, foot_y), hint_text, font=f_foot, fill=(120, 160, 185, 255))

    canvas.save(out_path, "PNG")
    print(f"OK generated mockup -> {out_path}")

if __name__ == "__main__":
    out = r"C:\Users\xieto\.gemini\antigravity-ide\brain\531f1ed4-ea9c-4420-b760-5e17beab3bf0\aquatech_tab_redesign_preview.png"
    render_tab_mockup(out)
