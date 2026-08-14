#!/usr/bin/env python3
"""Install AquaTech rank prefix PNGs into Oraxen + wire LuckPerms group prefixes."""
from __future__ import annotations

import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SRC = ROOT / "tools" / "prefix_pack_src"
ORAXEN = ROOT / "server" / "plugins" / "Oraxen"
TEX = ORAXEN / "pack" / "textures" / "aquatech" / "ranks"
GLYPHS = ORAXEN / "glyphs"
LP_GROUPS = ROOT / "server" / "plugins" / "LuckPerms" / "yaml-storage" / "groups"

# Stable PUA codepoints so TAB/LP prefixes stay consistent across reloads.
# U+E100.. range (Private Use Area)
BASE_CP = 0xE100

# png stem -> (lp_group, weight, pretty_ru)
RANK_MAP = [
    ("owner", "owner", 100, "Владелец"),
    ("admin", "admin", 80, "Админ"),
    ("dev", "developer", 75, "Разработчик"),
    ("mod", "mod", 60, "Модер"),
    ("staff", "staff", 55, "Стафф"),
    ("helper", "helper", 50, "Хелпер"),
    ("manager", "manager", 48, "Менеджер"),
    ("magnate", "admiral", 45, "Адмирал"),
    ("mvp", "legend", 50, "Легенда"),
    ("vipplus", "vipplus", 42, "VIP+"),
    ("vip", "vip", 40, "VIP"),
    ("streamer", "streamer", 35, "Стример"),
    ("twitch", "twitch", 34, "Twitch"),
    ("youtuber", "youtuber", 34, "YouTube"),
    ("artist", "artist", 32, "Артист"),
    ("builder", "builder", 28, "Билдер"),
    ("friend", "friend", 22, "Друг"),
    ("trainee", "trainee", 18, "Стажёр"),
    ("player", "default", 10, "Игрок"),
    ("npc", "npc", 5, "NPC"),
]


def glyph_char(index: int) -> str:
    return chr(BASE_CP + index)


def write_glyphs_yml(chars: dict[str, str]) -> None:
    lines = [
        "# AquaTech rank prefixes (Oraxen glyphs)",
        "# Placeholders: :aq_owner: etc. LP prefixes use the raw glyph char.",
        "",
    ]
    for i, (stem, group, _w, _pretty) in enumerate(RANK_MAP):
        gname = f"aq_{stem}"
        ch = chars[stem]
        lines += [
            f"{gname}:",
            f"  texture: aquatech/ranks/{stem}",
            "  ascent: 7",
            "  height: 8",
            f"  char: \"{ch}\"",
            "  chat:",
            "    placeholders:",
            f"      - \":aq_{stem}:\"",
            f"      - \":{stem}:\"",
            f"    permission: \"oraxen.glyph.aq_{stem}\"",
            "",
        ]
    GLYPHS.mkdir(parents=True, exist_ok=True)
    (GLYPHS / "aquatech_ranks.yml").write_text("\n".join(lines), encoding="utf-8")
    print("Wrote", GLYPHS / "aquatech_ranks.yml")


def copy_textures() -> None:
    TEX.mkdir(parents=True, exist_ok=True)
    for stem, *_ in RANK_MAP:
        src = SRC / f"{stem}.png"
        if not src.exists():
            print("MISSING texture", src)
            continue
        dst = TEX / f"{stem}.png"
        shutil.copy2(src, dst)
        print("TEX", dst.name)


def upsert_lp_group(group: str, weight: int, pretty: str, glyph: str, parents: list[str] | None = None) -> None:
    path = LP_GROUPS / f"{group}.yml"
    # Always rewrite prefixes to glyph; keep existing permissions if file exists.
    perms: list[str] = []
    existing_parents: list[str] = parents[:] if parents else []
    if path.exists():
        text = path.read_text(encoding="utf-8")
        # crude preserve of permission lines starting with "- " under permissions:
        in_perms = False
        for line in text.splitlines():
            if line.strip() == "permissions:":
                in_perms = True
                continue
            if in_perms:
                if line.startswith("parents:") or line.startswith("prefixes:") or line.startswith("name:"):
                    in_perms = False
                    continue
                if line.startswith("- ") or line.startswith("  - "):
                    perms.append(line if line.startswith("- ") else "- " + line.strip().lstrip("- ").strip())
                elif line.strip() and not line.startswith(" ") and not line.startswith("-"):
                    in_perms = False
            if line.startswith("parents:"):
                # skip, we set below
                pass

    if not perms and group == "default":
        perms = [
            "- permission: essentials.spawn",
            "  value: true",
            "- permission: ftbquests.open",
            "  value: true",
        ]
    if group in ("owner",) and not any("*" in p for p in perms):
        perms = ["- '*'"]

    # Default parent chain for new groups
    if not existing_parents:
        if group == "owner":
            existing_parents = ["admin"]
        elif group == "admin":
            existing_parents = ["mod"]
        elif group in ("mod", "developer", "staff", "helper", "manager"):
            existing_parents = ["default"]
        elif group not in ("default", "npc"):
            existing_parents = ["default"]

    # Prefix: glyph + space (glyph renders via Oraxen pack; readable fallback after)
    # Use section-sign white so glyph isn't colored weirdly in some clients
    prefix = f"{glyph} "

    out = [f"name: {group}", "permissions:"]
    if perms:
        out.extend(perms)
    else:
        out.append("- permission: aquatech.rank." + group)
        out.append("  value: true")
    if existing_parents:
        out.append("parents:")
        for p in existing_parents:
            out.append(f"- {p}")
    out.append("prefixes:")
    # YAML quoting for unicode glyph (+ trailing space)
    out.append(f'- "{prefix}":')
    out.append(f"    priority: {weight}")

    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text("\n".join(out) + "\n", encoding="utf-8")
    print("LP", group, "->", repr(prefix), pretty)


def stem_for_group(group: str) -> str:
    for stem, g, *_ in RANK_MAP:
        if g == group:
            return stem
    return group


def write_char_map(chars: dict[str, str]) -> None:
    lines = ["# auto-generated glyph map for AquaTech TAB", "group,stem,codepoint,char"]
    for i, (stem, group, _w, _p) in enumerate(RANK_MAP):
        ch = chars[stem]
        lines.append(f"{group},{stem},{ord(ch):04X},{ch}")
    out = ROOT / "server" / "config" / "aquatech_rank_glyphs.csv"
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text("\n".join(lines) + "\n", encoding="utf-8")
    # Also Java-friendly properties
    props = ROOT / "server" / "config" / "aquatech_rank_glyphs.properties"
    plines = [f"{group}=\\u{ord(chars[stem]):04X}" for stem, group, *_ in RANK_MAP]
    # reverse map group->char without escape for file readers that understand UTF-8
    plines2 = [f"{group}={chars[stem]}" for stem, group, *_ in RANK_MAP]
    props.write_text("\n".join(plines2) + "\n", encoding="utf-8")
    print("Wrote", out)
    print("Wrote", props)


def ensure_extra_groups() -> None:
    # groups already covered by RANK_MAP; ensure captain/skipper/sailor keep text if no art
    for group, weight, pretty, parent in [
        ("captain", 30, "Капитан", "default"),
        ("skipper", 25, "Шкипер", "default"),
        ("sailor", 20, "Матрос", "default"),
    ]:
        path = LP_GROUPS / f"{group}.yml"
        if path.exists():
            continue
        path.write_text(
            "\n".join(
                [
                    f"name: {group}",
                    "permissions:",
                    f"- permission: aquatech.rank.{group}",
                    "  value: true",
                    "parents:",
                    f"- {parent}",
                    "prefixes:",
                    f'- "&b[{pretty}] ":',
                    f"    priority: {weight}",
                ]
            )
            + "\n",
            encoding="utf-8",
        )
        print("LP created text-only", group)


def main() -> None:
    if not SRC.exists():
        raise SystemExit(f"Missing source PNGs: {SRC}")
    chars = {stem: glyph_char(i) for i, (stem, *_rest) in enumerate(RANK_MAP)}
    copy_textures()
    write_glyphs_yml(chars)
    write_char_map(chars)
    for stem, group, weight, pretty in RANK_MAP:
        upsert_lp_group(group, weight, pretty, chars[stem])
    ensure_extra_groups()
    # permissions hint file
    hint = ORAXEN / "aquatech_ranks_README.txt"
    hint.write_text(
        "AquaTech rank glyphs installed.\n"
        "Chat placeholders: :aq_owner: :aq_vip: ...\n"
        "LuckPerms prefixes already set to glyph characters.\n"
        "Restart server, accept Oraxen resource pack, then: /lp sync\n"
        "PAPI: /papi ecloud Oraxen\n",
        encoding="utf-8",
    )
    print("DONE")


if __name__ == "__main__":
    main()
