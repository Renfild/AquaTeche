# -*- coding: utf-8 -*-
"""Download Industrial Upgrade + official addons for AquaTech (Forge 1.20.1)."""
from __future__ import annotations

import json
import shutil
import urllib.parse
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUT_DIRS = [
    ROOT / "server" / "mods",
    ROOT / "client" / "mods",
    ROOT / "mods",
]
CF = Path(r"C:\Users\xieto\curseforge\minecraft\Instances\AquaTech\mods")
if CF.exists():
    OUT_DIRS.append(CF)

UA = "AquaTechPack/1.0 (contact: local-dev)"

# Core IU + requested addons (+ Simply Quarry if available — listed as core addon by author)
PROJECTS = [
    "industrialupgrade",
    "power-utilities",       # Power Utilities (IU addon / remaster line)
    "quantum-generators",
    "simply-quarries",       # required core addon per IU Modrinth deps
]


def api_get(url: str):
    req = urllib.request.Request(url, headers={"User-Agent": UA})
    with urllib.request.urlopen(req, timeout=60) as resp:
        return json.loads(resp.read().decode("utf-8"))


def pick_version(slug: str):
    q = urllib.parse.urlencode(
        {"loaders": '["forge"]', "game_versions": '["1.20.1"]'}
    )
    try:
        versions = api_get(f"https://api.modrinth.com/v2/project/{slug}/version?{q}")
    except Exception as e:
        print(f"SKIP {slug}: {e}")
        return None
    if not versions:
        print(f"SKIP {slug}: no forge 1.20.1 versions")
        return None
    for pref in ("release", "beta", "alpha"):
        for v in versions:
            if v.get("version_type") == pref:
                return v
    return versions[0]


def download_file(url: str, dest: Path):
    req = urllib.request.Request(url, headers={"User-Agent": UA})
    with urllib.request.urlopen(req, timeout=300) as resp:
        dest.write_bytes(resp.read())


def main():
    cache = ROOT / "_mod_dl_cache"
    cache.mkdir(exist_ok=True)
    downloaded = []
    for slug in PROJECTS:
        ver = pick_version(slug)
        if not ver:
            continue
        files = [f for f in ver.get("files", []) if f.get("primary")] or ver.get("files", [])
        if not files:
            print(f"SKIP {slug}: no files")
            continue
        f = files[0]
        name = f["filename"]
        dest = cache / name
        if not dest.exists():
            print(f"DL  {slug} -> {name} ({ver.get('version_number')})")
            download_file(f["url"], dest)
        else:
            print(f"HIT {name}")
        # print deps from version
        deps = ver.get("dependencies") or []
        if deps:
            print("    deps:", [f"{d.get('dependency_type')}:{d.get('project_id')}" for d in deps[:8]])
        downloaded.append(dest)

    for d in OUT_DIRS:
        d.mkdir(parents=True, exist_ok=True)
        for jar in downloaded:
            target = d / jar.name
            shutil.copy2(jar, target)
            print(f"  -> {target}")
    print(f"OK {len(downloaded)} jars -> {len(OUT_DIRS)} folders")


if __name__ == "__main__":
    main()
