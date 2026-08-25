#!/usr/bin/env python3
"""Generate pixel-perfect rank pill badges matching LumenTheme and install into font/LP."""
import json
import os
import re
import shutil
import zipfile
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
MOD_RES = ROOT / "mods" / "aquatech-ui" / "src" / "main" / "resources"
MOD_TEX = MOD_RES / "assets" / "aquatech_ui" / "textures" / "ranks"
MOD_FONT = MOD_RES / "assets" / "minecraft" / "font" / "default.json"
LP_GROUPS = ROOT / "server" / "plugins" / "LuckPerms" / "yaml-storage" / "groups"
LP_USERS = ROOT / "server" / "plugins" / "LuckPerms" / "yaml-storage" / "users"
RP_ZIP = ROOT / "server" / "resourcepacks" / "AquaTech_Ranks.zip"
SRC = ROOT / "tools" / "prefix_pack_src"

BASE_CP = 0xE100

# Full 5px height font table for Cyrillic and Latin
GLYPHS_5H = {
    'А': ['0110', '1001', '1111', '1001', '1001'],
    'Б': ['1110', '1000', '1110', '1001', '1110'],
    'В': ['1110', '1001', '1110', '1001', '1110'],
    'Г': ['1111', '1000', '1000', '1000', '1000'],
    'Д': ['01110', '01010', '01010', '11111', '10001'],
    'Е': ['1111', '1000', '1110', '1000', '1111'],
    'Ё': ['0101', '1111', '1110', '1000', '1111'],
    'Ж': ['10101', '10101', '01110', '10101', '10101'],
    'З': ['1110', '0001', '0110', '0001', '1110'],
    'И': ['1001', '1001', '1011', '1101', '1001'],
    'Й': ['0110', '1001', '1011', '1101', '1001'],
    'К': ['1001', '1010', '1100', '1010', '1001'],
    'Л': ['0111', '1001', '1001', '1001', '1001'],
    'М': ['10001', '11011', '10101', '10001', '10001'],
    'Н': ['1001', '1001', '1111', '1001', '1001'],
    'О': ['0110', '1001', '1001', '1001', '0110'],
    'П': ['1111', '1001', '1001', '1001', '1001'],
    'Р': ['1110', '1001', '1110', '1000', '1000'],
    'С': ['0111', '1000', '1000', '1000', '0111'],
    'Т': ['11111', '00100', '00100', '00100', '00100'],
    'У': ['1001', '1001', '0111', '0001', '0110'],
    'Ф': ['0110', '1111', '1011', '0110', '0010'],
    'Х': ['1001', '1001', '0110', '1001', '1001'],
    'Ц': ['10010', '10010', '10010', '11111', '00001'],
    'Ч': ['1001', '1001', '0111', '0001', '0001'],
    'Ш': ['10101', '10101', '10101', '10101', '11111'],
    'Щ': ['101010', '101010', '101010', '111111', '000001'],
    'Ъ': ['1100', '0100', '0110', '0101', '0110'],
    'Ы': ['10001', '10001', '11101', '10011', '11101'],
    'Ь': ['1000', '1000', '1110', '1001', '1110'],
    'Э': ['1110', '0001', '0111', '0001', '1110'],
    'Ю': ['10011', '10101', '11101', '10101', '10011'],
    'Я': ['0111', '1001', '0111', '0101', '1001'],
    'A': ['0110', '1001', '1111', '1001', '1001'],
    'B': ['1110', '1001', '1110', '1001', '1110'],
    'C': ['0111', '1000', '1000', '1000', '0111'],
    'D': ['1110', '1001', '1001', '1001', '1110'],
    'E': ['1111', '1000', '1110', '1000', '1111'],
    'F': ['1111', '1000', '1110', '1000', '1000'],
    'G': ['0111', '1000', '1011', '1001', '0111'],
    'H': ['1001', '1001', '1111', '1001', '1001'],
    'I': ['111', '010', '010', '010', '111'],
    'J': ['0011', '0001', '0001', '1001', '0110'],
    'K': ['1001', '1010', '1100', '1010', '1001'],
    'L': ['1000', '1000', '1000', '1000', '1111'],
    'M': ['10001', '11011', '10101', '10001', '10001'],
    'N': ['1001', '1101', '1011', '1001', '1001'],
    'O': ['0110', '1001', '1001', '1001', '0110'],
    'P': ['1110', '1001', '1110', '1000', '1000'],
    'Q': ['0110', '1001', '1001', '1011', '0111'],
    'R': ['1110', '1001', '1110', '1010', '1001'],
    'S': ['0111', '1000', '0110', '0001', '1110'],
    'T': ['11111', '00100', '00100', '00100', '00100'],
    'U': ['1001', '1001', '1001', '1001', '0110'],
    'V': ['10001', '10001', '01010', '01010', '00100'],
    'W': ['10001', '10001', '10101', '11011', '10001'],
    'X': ['1001', '1001', '0110', '1001', '1001'],
    'Y': ['1001', '1001', '0110', '0010', '0010'],
    'Z': ['1111', '0001', '0110', '1000', '1111'],
    ' ': ['00'],
    '+': ['000', '010', '111', '010', '000']
}

# (stem, lp_group, priority, text, color_hex)
RANKS_SPEC = [
    ("owner", "owner", 100, "ВЛАДЕЛЕЦ", "#F5C25B"),      # Ocean Gold (matches Tab!)
    ("admin", "admin", 80, "АДМИН", "#FF6B6B"),          # Coral Red
    ("dev", "developer", 75, "DEV", "#FF6B6B"),          # Coral Red
    ("mod", "mod", 60, "МОДЕР", "#FF9F43"),             # Orange
    ("staff", "staff", 55, "ПЕРСОНАЛ", "#FF6B6B"),       # Red
    ("helper", "helper", 50, "ХЕЛПЕР", "#4CD08A"),       # Emerald Green
    ("manager", "manager", 48, "КУРАТОР", "#E056FD"),    # Neon Purple
    ("magnate", "admiral", 45, "АДМИРАЛ", "#A29BFE"),    # Lavender
    ("mvp", "legend", 50, "ЛЕГЕНДА", "#E056FD"),         # Neon Purple
    ("vipplus", "vipplus", 42, "VIP+", "#FEEAA7"),       # Pale Gold
    ("vip", "vip", 40, "VIP", "#FEEAA7"),                # Pale Gold
    ("streamer", "streamer", 35, "СТРИМ", "#A29BFE"),    # Lavender
    ("twitch", "twitch", 34, "TWITCH", "#A29BFE"),       # Lavender
    ("youtuber", "youtuber", 34, "YOUTUBE", "#FF4757"),  # YT Red
    ("artist", "artist", 32, "АРТ", "#FD79A8"),          # Pink
    ("builder", "builder", 28, "БИЛДЕР", "#FAB1A0"),     # Peach
    ("friend", "friend", 22, "ДРУГ", "#55EFC4"),         # Mint
    ("trainee", "trainee", 18, "СТАЖЕР", "#FFEAA7"),     # Yellow
    ("player", "default", 10, "ИГРОК", "#81ECEC"),       # Soft Mint
    ("npc", "npc", 5, "NPC", "#A0AEC0"),                 # Grey
]

GROUP_FALLBACK = {
    "captain": "magnate",
    "skipper": "vip",
    "sailor": "player",
    "moderator": "mod",
}

def hex_to_rgb(hex_str):
    h = hex_str.lstrip('#')
    return tuple(int(h[i:i+2], 16) for i in (0, 2, 4))

def glyph_char(i: int) -> str:
    return chr(BASE_CP + i)

def create_rank_badge(text, border_hex, height=8):
    br, bg, bb = hex_to_rgb(border_hex)
    border_color = (br, bg, bb, 255)
    corner_color = (br, bg, bb, 160)
    bg_color = (max(0, int(br * 0.12)), max(0, int(bg * 0.12)), max(0, int(bb * 0.12)), 230)
    text_color = (br, bg, bb, 255)

    char_widths = []
    for ch in text.upper():
        if ch in GLYPHS_5H:
            char_widths.append(len(GLYPHS_5H[ch][0]))
        else:
            char_widths.append(4)
    
    spacing = 1
    total_text_w = sum(char_widths) + (len(text) - 1) * spacing if text else 0
    padding_x = 3
    width = total_text_w + padding_x * 2

    img = Image.new('RGBA', (width, height), (0, 0, 0, 0))
    pixels = img.load()

    # Fill background
    for y in range(height):
        for x in range(width):
            pixels[x, y] = bg_color

    # Draw border lines
    for x in range(1, width - 1):
        pixels[x, 0] = border_color
        pixels[x, height - 1] = border_color
    for y in range(1, height - 1):
        pixels[0, y] = border_color
        pixels[width - 1, y] = border_color

    # Transparent outside corners
    pixels[0, 0] = (0, 0, 0, 0)
    pixels[width - 1, 0] = (0, 0, 0, 0)
    pixels[0, height - 1] = (0, 0, 0, 0)
    pixels[width - 1, height - 1] = (0, 0, 0, 0)

    # Soft corner bevel
    pixels[1, 1] = corner_color
    pixels[width - 2, 1] = corner_color
    pixels[1, height - 2] = corner_color
    pixels[width - 2, height - 2] = corner_color

    # Render Text
    cur_x = padding_x
    start_y = 1 if height <= 7 else (height - 5) // 2

    for ch in text.upper():
        glyph = GLYPHS_5H.get(ch, GLYPHS_5H.get(' '))
        gw = len(glyph[0])
        for gy, row in enumerate(glyph):
            for gx, cell in enumerate(row):
                if cell == '1':
                    pixels[cur_x + gx, start_y + gy] = text_color
        cur_x += gw + spacing

    return img

def build_all():
    MOD_TEX.mkdir(parents=True, exist_ok=True)
    SRC.mkdir(parents=True, exist_ok=True)

    print("Generating rank badges...")
    for stem, _group, _prio, text, col in RANKS_SPEC:
        badge = create_rank_badge(text, col, height=8)
        tex_path = MOD_TEX / f"{stem}.png"
        src_path = SRC / f"{stem}.png"
        badge.save(tex_path)
        badge.save(src_path)
        print(f"  [OK] {stem}.png -> {text} ({col}) size {badge.size}")

    # Build font providers
    providers = [
        {"type": "reference", "id": "minecraft:include/space"},
        {"type": "space", "advances": {" ": 5}}
    ]
    for i, (stem, _group, _prio, _text, _col) in enumerate(RANKS_SPEC):
        ch = glyph_char(i)
        providers.append({
            "type": "bitmap",
            "file": f"aquatech_ui:ranks/{stem}.png",
            "ascent": 7,
            "height": 8,
            "chars": [ch]
        })
    providers.append({"type": "reference", "id": "minecraft:include/default"})

    MOD_FONT.parent.mkdir(parents=True, exist_ok=True)
    MOD_FONT.write_text(json.dumps({"providers": providers}, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print("Wrote default.json font providers.")

    # Write Resource Pack Zip
    RP_ZIP.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(RP_ZIP, "w", compression=zipfile.ZIP_DEFLATED) as zf:
        pack_meta = {"pack": {"pack_format": 15, "description": "AquaTech rank prefixes"}}
        zf.writestr("pack.mcmeta", json.dumps(pack_meta, indent=2).encode("utf-8"))
        zf.writestr("assets/minecraft/font/default.json", json.dumps({"providers": providers}, indent=2, ensure_ascii=False).encode("utf-8"))
        for stem, *_ in RANKS_SPEC:
            p = MOD_TEX / f"{stem}.png"
            if p.is_file():
                zf.writestr(f"assets/aquatech_ui/textures/ranks/{stem}.png", p.read_bytes())
    print("Wrote Resource Pack zip:", RP_ZIP)

    # Update LuckPerms groups
    char_by_stem = {stem: glyph_char(i) for i, (stem, *_) in enumerate(RANKS_SPEC)}
    LP_GROUPS.mkdir(parents=True, exist_ok=True)

    for stem, group, prio, _text, _col in RANKS_SPEC:
        glyph = char_by_stem[stem]
        path = LP_GROUPS / f"{group}.yml"
        perms = []
        parents = []
        if path.is_file():
            text_lines = path.read_text(encoding="utf-8").splitlines()
            sec = None
            for l in text_lines:
                s = l.strip()
                if s == "permissions:":
                    sec = "perms"
                    continue
                if s == "parents:":
                    sec = "parents"
                    continue
                if s == "prefixes:":
                    sec = "prefixes"
                    continue
                if sec == "perms" and l.startswith("- "):
                    perms.append(l.rstrip())
                elif sec == "parents" and l.startswith("- "):
                    parents.append(l.strip()[2:].strip())

        if group == "owner" and not any("*" in p for p in perms):
            perms = ["- '*'"]
        if group == "default" and not perms:
            perms = ["- permission: essentials.spawn", "- permission: ftbquests.open"]
        if group != "default" and "default" not in parents and group not in ("owner", "admin", "npc"):
            parents = ["default"] if not parents else parents

        out = [f"name: {group}", "permissions:"]
        out.extend(perms or ["- essentials.spawn"])
        out.append("parents:")
        if parents:
            for p in parents:
                out.append(f"- {p}")
        elif group != "default":
            out.append("- default")
        out.append("prefixes:")
        out.append(f'- "{glyph} ":')
        out.append(f"    priority: {prio}")

        path.write_text("\n".join(out) + "\n", encoding="utf-8")
        print(f"  [LP Group] {group} -> prefix {repr(glyph + ' ')} (prio {prio})")

    # Update user xietoru in LuckPerms
    if LP_USERS.is_dir():
        for u in LP_USERS.glob("*.yml"):
            u_text = u.read_text(encoding="utf-8")
            if "name: xietoru" in u_text:
                u.write_text("uuid: adb6bd7b-94f9-4da6-bf89-10dfd4999161\nname: xietoru\nprimary-group: owner\nparents:\n- owner\n", encoding="utf-8")
                print("Updated LP user xietoru primary-group -> owner")

    print("\nAll rank badges and LuckPerms prefixes configured successfully!")

if __name__ == "__main__":
    build_all()
