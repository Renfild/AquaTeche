#!/usr/bin/env python3
"""Install rank prefix PNGs: mod font assets + LuckPerms + glyph map (no Oraxen)."""
from __future__ import annotations

import hashlib
import json
import re
import shutil
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SRC = ROOT / "tools" / "prefix_pack_src"
PACK_DIR = next(
    (p for p in ROOT.iterdir() if p.is_dir() and "префикс" in p.name.lower()),
    None,
)
MOD_RES = ROOT / "mods" / "aquatech-ui" / "src" / "main" / "resources"
MOD_TEX = MOD_RES / "assets" / "aquatech_ui" / "textures" / "ranks"
MOD_FONT = MOD_RES / "assets" / "minecraft" / "font" / "default.json"
LP_GROUPS = ROOT / "server" / "plugins" / "LuckPerms" / "yaml-storage" / "groups"
GLYPH_PROPS = ROOT / "server" / "config" / "aquatech_rank_glyphs.properties"
RP_ZIP = ROOT / "server" / "resourcepacks" / "AquaTech_Ranks.zip"
CF_RP = Path(r"C:\Users\xieto\curseforge\minecraft\Instances\AquaTech\resourcepacks\AquaTech_Ranks.zip")

BASE_CP = 0xE100

# png stem -> (lp_group, priority)
RANKS = [
    ("owner", "owner", 100),
    ("admin", "admin", 80),
    ("dev", "developer", 75),
    ("mod", "mod", 60),
    ("staff", "staff", 55),
    ("helper", "helper", 50),
    ("manager", "manager", 48),
    ("magnate", "admiral", 45),
    ("mvp", "legend", 50),
    ("vipplus", "vipplus", 42),
    ("vip", "vip", 40),
    ("streamer", "streamer", 35),
    ("twitch", "twitch", 34),
    ("youtuber", "youtuber", 34),
    ("artist", "artist", 32),
    ("builder", "builder", 28),
    ("friend", "friend", 22),
    ("trainee", "trainee", 18),
    ("player", "default", 10),
    ("npc", "npc", 5),
]

# LP groups without dedicated art -> png stem
GROUP_FALLBACK = {
    "captain": "magnate",
    "skipper": "vip",
    "sailor": "player",
    "moderator": "mod",
}


def glyph_char(i: int) -> str:
    return chr(BASE_CP + i)


def sync_textures() -> None:
    SRC.mkdir(parents=True, exist_ok=True)
    if PACK_DIR and PACK_DIR.is_dir():
        for png in PACK_DIR.glob("*.png"):
            shutil.copy2(png, SRC / png.name)
            print("SRC", png.name)
    MOD_TEX.mkdir(parents=True, exist_ok=True)
    for stem, *_ in RANKS:
        src = SRC / f"{stem}.png"
        if not src.is_file():
            print("MISSING", src)
            continue
        shutil.copy2(src, MOD_TEX / f"{stem}.png")
        print("TEX", stem)


def build_font_json() -> dict:
    providers: list[dict] = []
    for i, (stem, _group, _prio) in enumerate(RANKS):
        ch = glyph_char(i)
        providers.append(
            {
                "type": "bitmap",
                "file": f"aquatech_ui:ranks/{stem}.png",
                "ascent": 7,
                "height": 8,
                "chars": [ch],
            }
        )
    providers.append({"type": "reference", "id": "minecraft:include/default"})
    return {"providers": providers}


def write_mod_font() -> None:
    MOD_FONT.parent.mkdir(parents=True, exist_ok=True)
    MOD_FONT.write_text(json.dumps(build_font_json(), indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print("Wrote", MOD_FONT)


def write_resource_pack_zip() -> str:
    RP_ZIP.parent.mkdir(parents=True, exist_ok=True)
    pack_meta = {
        "pack": {
            "pack_format": 15,
            "description": "AquaTech rank prefixes",
        }
    }
    font = build_font_json()
    sha = hashlib.sha1()
    with zipfile.ZipFile(RP_ZIP, "w", compression=zipfile.ZIP_DEFLATED) as zf:
        meta = json.dumps(pack_meta, indent=2).encode("utf-8")
        sha.update(meta)
        zf.writestr("pack.mcmeta", meta)
        font_bytes = json.dumps(font, indent=2, ensure_ascii=False).encode("utf-8")
        sha.update(font_bytes)
        zf.writestr("assets/minecraft/font/default.json", font_bytes)
        for stem, *_ in RANKS:
            p = MOD_TEX / f"{stem}.png"
            if not p.is_file():
                continue
            data = p.read_bytes()
            sha.update(data)
            zf.writestr(f"assets/aquatech_ui/textures/ranks/{stem}.png", data)
    digest = sha.hexdigest()
    if CF_RP.parent.exists():
        shutil.copy2(RP_ZIP, CF_RP)
        print("CF RP", CF_RP)
    print("Wrote", RP_ZIP, "sha1", digest)
    return digest


def stem_for_group(group: str) -> str:
    g = group.lower()
    for stem, lp_group, _ in RANKS:
        if lp_group == g:
            return stem
    if g in GROUP_FALLBACK:
        return GROUP_FALLBACK[g]
    return "player"


def write_glyph_properties() -> None:
    lines = ["# AquaTech rank prefix PUA glyphs (U+E100+) — used by aquatech-ui TAB/nameplate", ""]
    group_to_char: dict[str, str] = {}
    for i, (stem, lp_group, _prio) in enumerate(RANKS):
        group_to_char[lp_group] = glyph_char(i)
    for group, stem in GROUP_FALLBACK.items():
        idx = next(i for i, (s, _, __) in enumerate(RANKS) if s == stem)
        group_to_char[group] = glyph_char(idx)
    for group in sorted(group_to_char):
        lines.append(f"{group}={group_to_char[group]}")
    text = "\n".join(lines) + "\n"
    GLYPH_PROPS.parent.mkdir(parents=True, exist_ok=True)
    GLYPH_PROPS.write_text(text, encoding="utf-8")
    for extra in (
        ROOT / "config" / "aquatech_rank_glyphs.properties",
        ROOT / "defaultconfigs" / "aquatech_rank_glyphs.properties",
    ):
        extra.parent.mkdir(parents=True, exist_ok=True)
        extra.write_text(text, encoding="utf-8")
    print("Wrote", GLYPH_PROPS)


def upsert_lp_group(group: str, priority: int, prefix: str) -> None:
    path = LP_GROUPS / f"{group}.yml"
    perms: list[str] = []
    parents: list[str] = []
    if path.is_file():
        text = path.read_text(encoding="utf-8")
        section = None
        for line in text.splitlines():
            s = line.strip()
            if s == "permissions:":
                section = "perms"
                continue
            if s == "parents:":
                section = "parents"
                continue
            if s == "prefixes:":
                section = "prefixes"
                continue
            if section == "perms" and line.startswith("- "):
                perms.append(line.rstrip())
            elif section == "parents" and line.startswith("- "):
                parents.append(line.strip()[2:].strip())
    if group == "owner" and not any("*" in p for p in perms):
        perms = ["- '*'"]
    if group == "default" and not perms:
        perms = [
            "- permission: essentials.spawn",
            "- permission: ftbquests.open",
        ]
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
    esc = prefix.replace("\\", "\\\\").replace('"', '\\"')
    out.append(f'- "{esc} ":')
    out.append(f"    priority: {priority}")
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text("\n".join(out) + "\n", encoding="utf-8")
    print("LP", group, repr(prefix + " "))


def apply_luckperms() -> None:
    char_by_stem = {stem: glyph_char(i) for i, (stem, _, __) in enumerate(RANKS)}
    applied: set[str] = set()
    for stem, group, prio in RANKS:
        upsert_lp_group(group, prio, char_by_stem[stem])
        applied.add(group)
    for group, stem in GROUP_FALLBACK.items():
        if group in applied:
            continue
        prio = next((p for s, g, p in RANKS if s == stem), 10)
        upsert_lp_group(group, prio, char_by_stem[stem])
    # Horizon / misc groups that already exist
    if LP_GROUPS.is_dir():
        for yml in LP_GROUPS.glob("*.yml"):
            g = yml.stem.lower()
            if g in applied or g in GROUP_FALLBACK:
                continue
            stem = stem_for_group(g)
            prio = 15
            m = re.search(r"priority:\s*(\d+)", yml.read_text(encoding="utf-8"))
            if m:
                prio = int(m.group(1))
            upsert_lp_group(g, prio, char_by_stem[stem])


def patch_server_properties(sha1: str) -> None:
    props = ROOT / "server" / "server.properties"
    if not props.is_file():
        return
    lines = props.read_text(encoding="utf-8").splitlines()
    out = []
    seen = {"resource-pack", "resource-pack-sha1", "resource-pack-prompt", "require-resource-pack"}
    for line in lines:
        key = line.split("=", 1)[0] if "=" in line else ""
        if key in seen:
            continue
        out.append(line)
    out.append("require-resource-pack=false")
    out.append("resource-pack=")
    out.append(f"resource-pack-sha1={sha1}")
    out.append("resource-pack-prompt=§bAquaTech §7— префиксы рангов (встроены в aquatech-ui)")
    props.write_text("\n".join(out) + "\n", encoding="utf-8")
    print("Patched server.properties (pack optional; mod embeds fonts)")


def main() -> None:
    sync_textures()
    write_mod_font()
    sha1 = write_resource_pack_zip()
    write_glyph_properties()
    apply_luckperms()
    patch_server_properties(sha1)
    print("DONE — rebuild aquatech-ui: cd mods/aquatech-ui && gradlew build")


if __name__ == "__main__":
    main()
