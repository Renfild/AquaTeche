# -*- coding: utf-8 -*-
"""Download Jade + Easy CraftTweaker + Recipe Generator (1.20.1 Forge)."""
from __future__ import annotations

import shutil
import urllib.request
from pathlib import Path

ROOT = Path(r"C:\Users\xieto\Desktop\AquaTech")
CACHE = ROOT / "_mod_dl_cache"
CACHE.mkdir(exist_ok=True)

TARGETS = [
    ROOT / "mods",
    ROOT / "server" / "mods",
    ROOT / "client" / "mods",
    ROOT / "server" / "client" / "mods",
    ROOT / "dist" / "AquaTech-Client" / "mods",
    Path(r"C:\Users\xieto\curseforge\minecraft\Instances\AquaTech\mods"),
    Path(r"C:\Users\xieto\curseforge\minecraft\Instances\AquaTech\client\mods"),
]

# Note: original curseforge.com/.../avaritia-recipe-generator is 1.12.2-only.
# For 1.20.1 we install Recipe Generator (visual CraftTweaker/Avaritia-style editor).
DOWNLOADS = [
    (
        "https://cdn.modrinth.com/data/uQorKjjW/versions/nvTbf2k1/recipe_generator-1.1.0_beta-forge-1.20.1.jar",
        "recipe_generator-1.1.0_beta-forge-1.20.1.jar",
    ),
    (
        "https://cdn.modrinth.com/data/uFnl8Tqw/versions/nlFD2wmC/EasyTweaker%201.20.1-1.0.4.jar",
        "EasyTweaker-1.20.1-1.0.4.jar",
    ),
    (
        "https://cdn.modrinth.com/data/nvQzSEkH/versions/xJQHCmWJ/Jade-1.20.1-Forge-11.13.3.jar",
        "Jade-1.20.1-Forge-11.13.3.jar",
    ),
]


def download(url: str, name: str) -> Path:
    dest = CACHE / name
    if dest.exists() and dest.stat().st_size > 1000:
        print("cached", name, dest.stat().st_size)
        return dest
    print("download", name)
    req = urllib.request.Request(url, headers={"User-Agent": "AquaTech/1.0"})
    with urllib.request.urlopen(req, timeout=120) as r, open(dest, "wb") as f:
        shutil.copyfileobj(r, f)
    print(" ok", dest.stat().st_size)
    return dest


def main() -> None:
    files = [download(u, n) for u, n in DOWNLOADS]
    for t in TARGETS:
        t.mkdir(parents=True, exist_ok=True)
        # remove older copies of same mods
        for pat in ("recipe_generator*.jar", "EasyTweaker*.jar", "Jade-1.20.1*.jar", "Jade-*Forge*.jar"):
            for old in t.glob(pat):
                # keep if same as new
                if old.name in {f.name for f in files}:
                    continue
                if "Jade" in old.name or "EasyTweaker" in old.name or "recipe_generator" in old.name:
                    print("remove old", old)
                    old.unlink(missing_ok=True)
        for f in files:
            out = t / f.name
            shutil.copy2(f, out)
            print("->", out)
    print("DONE")


if __name__ == "__main__":
    main()
