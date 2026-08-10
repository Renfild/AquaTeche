"""Push first-party jars from server/mods into the live Lodestone instance.

Keeps aquatech_ui / casesmod / packetfixer on the running Mohist host
in sync with what the launcher pack ships (avoids MATCH_VERSION kick).

Usage:
  python tools/sync_lodestone_mods.py
"""
from __future__ import annotations

import hashlib
import os
import shutil
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SERVER_MODS = ROOT / "server" / "mods"

# Filename prefixes that must match the launcher pack / server/mods
SYNC_PREFIXES = (
    "aquatech_ui-",
    "casesmod-",
    "packetfixer-",
)

LODESTONE_INSTANCES = Path.home() / ".lodestone" / "instances"


def md5_file(path: Path) -> str:
    h = hashlib.md5()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(65536), b""):
            h.update(chunk)
    return h.hexdigest()


def find_lodestone_mods() -> Path | None:
    env = os.environ.get("AQUATECH_LODESTONE_MODS", "").strip()
    if env:
        p = Path(env)
        return p if p.is_dir() else None
    if not LODESTONE_INSTANCES.is_dir():
        return None
    cands = sorted(
        LODESTONE_INSTANCES.glob("AquaTech*/mods"),
        key=lambda p: p.stat().st_mtime,
        reverse=True,
    )
    return cands[0] if cands else None


def jars_for_prefix(folder: Path, prefix: str) -> list[Path]:
    return sorted(folder.glob(f"{prefix}*.jar"))


def sync() -> int:
    if not SERVER_MODS.is_dir():
        print(f"missing {SERVER_MODS}", file=sys.stderr)
        return 1
    dest = find_lodestone_mods()
    if dest is None:
        print("Lodestone AquaTech mods folder not found (skip)")
        return 0

    print(f"Lodestone mods: {dest}")
    updated = 0
    for prefix in SYNC_PREFIXES:
        srcs = jars_for_prefix(SERVER_MODS, prefix)
        if not srcs:
            print(f"WARN no {prefix}*.jar in server/mods")
            continue
        src = srcs[-1]
        for old in jars_for_prefix(dest, prefix):
            if old.name != src.name or md5_file(old) != md5_file(src):
                old.unlink(missing_ok=True)
                print(f"  - {old.name}")
        target = dest / src.name
        if target.is_file() and md5_file(target) == md5_file(src):
            print(f"  = {src.name}")
            continue
        shutil.copy2(src, target)
        updated += 1
        print(f"  + {src.name}")

    print(f"OK lodestone sync ({updated} updated)")
    if updated:
        print("Restart Mohist in Lodestone so clients see the new versions.")
    return 0


if __name__ == "__main__":
    raise SystemExit(sync())
