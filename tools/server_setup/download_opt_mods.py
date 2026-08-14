# -*- coding: utf-8 -*-
"""Download Forge 1.20.1 performance mods for AquaTech."""
from __future__ import annotations

import json
import urllib.parse
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
UA = "AquaTechPack/1.0 (opt-mods)"

# slug -> which sides (both | client | server)
MODS = {
    "ferrite-core": "both",
    "modernfix": "both",
    "canary": "both",          # Lithium-like AI/physics
    "clumps": "both",          # XP orb merge
    "ai-improvements": "both", # less mob AI call volume
    "fastsuite": "both",
    "entityculling": "client",
    "immediatelyfast": "client",
    "dynamic-fps": "client",
}

OUT = {
    "both": [
        ROOT / "mods",
        ROOT / "server" / "mods",
        ROOT / "client" / "mods",
        ROOT / "server" / "client" / "mods",
        ROOT / "dist" / "AquaTech-Client" / "mods",
        Path(r"C:\Users\xieto\curseforge\minecraft\Instances\AquaTech\mods"),
        Path(r"C:\Users\xieto\curseforge\minecraft\Instances\AquaTech\client\mods"),
    ],
    "client": [
        ROOT / "client" / "mods",
        ROOT / "server" / "client" / "mods",
        ROOT / "dist" / "AquaTech-Client" / "mods",
        Path(r"C:\Users\xieto\curseforge\minecraft\Instances\AquaTech\mods"),
        Path(r"C:\Users\xieto\curseforge\minecraft\Instances\AquaTech\client\mods"),
        # also root mods for unified CF instance
        ROOT / "mods",
    ],
    "server": [
        ROOT / "server" / "mods",
        ROOT / "mods",
    ],
}


def api_get(url: str):
    req = urllib.request.Request(url, headers={"User-Agent": UA})
    with urllib.request.urlopen(req, timeout=60) as resp:
        return json.loads(resp.read().decode("utf-8"))


def pick_version(slug: str):
    q = urllib.parse.urlencode(
        {"loaders": '["forge"]', "game_versions": '["1.20.1"]'}
    )
    versions = api_get(f"https://api.modrinth.com/v2/project/{slug}/version?{q}")
    if not versions:
        raise RuntimeError(f"No Forge 1.20.1 versions for {slug}")
    # Prefer filenames that look like 1.20.1, avoid 1.20.4 etc.
    preferred = []
    for v in versions:
        gv = v.get("game_versions") or []
        if "1.20.1" not in gv and "1.20" not in gv:
            continue
        name = (v.get("files") or [{}])[0].get("filename", "")
        # skip clearly wrong MC versions in filename
        if "1.20.4" in name or "1.20.2" in name or "1.21" in name:
            continue
        preferred.append(v)
    chosen = preferred[0] if preferred else versions[0]
    files = chosen.get("files") or []
    primary = next((f for f in files if f.get("primary")), files[0])
    return chosen["version_number"], primary["filename"], primary["url"]


def download(url: str, dest: Path):
    dest.parent.mkdir(parents=True, exist_ok=True)
    req = urllib.request.Request(url, headers={"User-Agent": UA})
    with urllib.request.urlopen(req, timeout=120) as resp, dest.open("wb") as out:
        out.write(resp.read())


def main():
    cache = ROOT / "_tmp_opt_mods"
    cache.mkdir(exist_ok=True)
    for slug, side in MODS.items():
        ver, filename, url = pick_version(slug)
        jar = cache / filename
        if not jar.exists():
            print(f"DL {slug} {ver} -> {filename}")
            download(url, jar)
        else:
            print(f"CACHE {filename}")
        for d in OUT[side]:
            if not d.exists():
                d.mkdir(parents=True, exist_ok=True)
            # remove older jars of same mod prefix
            prefix = filename.split("-")[0].lower()
            for old in d.glob("*.jar"):
                n = old.name.lower()
                if n == filename.lower():
                    continue
                # rough cleanup for same mod family
                if slug.replace("-", "") in n.replace("-", "").replace("_", "") and "opti" not in n:
                    # only remove if clearly same mod
                    keys = {
                        "ferrite-core": "ferritecore",
                        "modernfix": "modernfix",
                        "canary": "canary",
                        "clumps": "clumps",
                        "ai-improvements": "ai-improvements",
                        "fastsuite": "fastsuite",
                        "entityculling": "entityculling",
                        "immediatelyfast": "immediatelyfast",
                        "dynamic-fps": "dynamic-fps",
                    }
                    key = keys[slug]
                    if key in n.replace("_", "-"):
                        print(f"  rm old {old}")
                        old.unlink()
            target = d / filename
            if not target.exists() or target.stat().st_size != jar.stat().st_size:
                target.write_bytes(jar.read_bytes())
                print(f"  -> {target}")
            else:
                print(f"  OK {target}")
    print("Done.")


if __name__ == "__main__":
    main()
