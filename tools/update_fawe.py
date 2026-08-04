# -*- coding: utf-8 -*-
"""Install FastAsyncWorldEdit for MC 1.20.1 + Java 17 (not Java 21 builds)."""
from __future__ import annotations

import json
import shutil
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PLUGIN_DIR = ROOT / "server" / "plugins"
UA = "AquaTechPack/1.0"


def main() -> None:
    url = (
        "https://api.modrinth.com/v2/project/fastasyncworldedit/version"
        "?game_versions=%5B%221.20.1%22%5D&loaders=%5B%22bukkit%22%5D"
    )
    req = urllib.request.Request(url, headers={"User-Agent": UA})
    versions = json.loads(urllib.request.urlopen(req, timeout=60).read())

    # FAWE 2.9+ / 2.11+ need Java 21. Pin 2.8.x for portable Java 17.
    pick = None
    for v in versions:
        vn = str(v.get("version_number", ""))
        if vn.startswith("2.8."):
            pick = v
            break
    if not pick:
        raise SystemExit("No FAWE 2.8.x for 1.20.1 found on Modrinth")

    f = pick["files"][0]
    name = f["filename"]
    cache = ROOT / "_mod_dl_cache"
    cache.mkdir(exist_ok=True)
    dest = cache / name
    if not dest.exists():
        print(f"DL {name}")
        req2 = urllib.request.Request(f["url"], headers={"User-Agent": UA})
        dest.write_bytes(urllib.request.urlopen(req2, timeout=300).read())
    else:
        print(f"HIT {name}")

    PLUGIN_DIR.mkdir(parents=True, exist_ok=True)
    disabled = PLUGIN_DIR / "disabled"
    disabled.mkdir(exist_ok=True)
    for old in PLUGIN_DIR.glob("FastAsyncWorldEdit*.jar"):
        bak = disabled / (old.stem + "-parked.jar.bak")
        shutil.move(str(old), str(bak))
        print(f"PARK {old.name} -> {bak.name}")

    target = PLUGIN_DIR / "FastAsyncWorldEdit.jar"
    shutil.copy2(dest, target)
    print(f"OK FAWE {pick['version_number']} -> {target}")


if __name__ == "__main__":
    main()
