# -*- coding: utf-8 -*-
"""Export client pack = server mods minus server-only jars + resource pack defaults."""
import os
import shutil
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SERVER_MODS = ROOT / "server" / "mods"
OUT = ROOT / "dist" / "AquaTech-Client"
CLIENT_MODS = OUT / "mods"

# Jars / name fragments that should NOT go to the client
SERVER_ONLY_FRAGMENTS = [
    "spark",
    "Chunky",
    "chunky",
    "Dynmap",
    "dynmap",
    "DiscordSRV",  # plugin, not in mods
    "LuckPerms",
]

SERVER_ONLY_EXACT = set()  # filled if needed

def is_server_only(name: str) -> bool:
    lower = name.lower()
    for frag in SERVER_ONLY_FRAGMENTS:
        if frag.lower() in lower:
            return True
    return name in SERVER_ONLY_EXACT

def main():
    if OUT.exists():
        shutil.rmtree(OUT)
    CLIENT_MODS.mkdir(parents=True)
    copied = 0
    skipped = []
    for jar in sorted(SERVER_MODS.glob("*.jar")):
        if is_server_only(jar.name):
            skipped.append(jar.name)
            continue
        shutil.copy2(jar, CLIENT_MODS / jar.name)
        copied += 1

    # options defaults
    options = OUT / "options.txt"
    options.write_text(
        "\n".join([
            "lang:ru_ru",
            "gamma:1.0",
            "renderDistance:10",
            "simulationDistance:8",
            "fov:0.0",
            "maxFps:120",
            "enableVsync:false",
            "guiScale:3",
        ]) + "\n",
        encoding="utf-8",
    )

    # manifest snapshot
    manifest_src = ROOT / "modlist" / "manifest.json"
    if manifest_src.exists():
        shutil.copy2(manifest_src, OUT / "manifest.json")

    readme = OUT / "README_CLIENT.txt"
    readme.write_text(
        "AquaTech: Ocean Horizon — Client Pack\n"
        "=====================================\n"
        "1. Install Forge 1.20.1 matching the server.\n"
        "2. Copy everything from mods/ into your .minecraft/mods.\n"
        "3. Optional: merge options.txt defaults.\n"
        "4. Connect to the AquaTech server.\n"
        "\n"
        f"Mods copied: {copied}\n"
        f"Skipped server-only: {len(skipped)}\n",
        encoding="utf-8",
    )

    meta = {
        "name": "AquaTech Ocean Horizon Client",
        "mods_copied": copied,
        "skipped": skipped,
    }
    (OUT / "export_meta.json").write_text(json.dumps(meta, indent=2), encoding="utf-8")
    print(f"Client pack at {OUT} ({copied} mods, skipped {len(skipped)})")

if __name__ == "__main__":
    main()
