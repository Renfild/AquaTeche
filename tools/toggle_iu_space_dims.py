#!/usr/bin/env python3
"""Temporarily strip Industrial Upgrade space LevelStem dimensions so the server
does not register / generate planet worlds on startup.

Reversible: restores the parked original jar and optionally the parked world dims.
"""
from __future__ import annotations

import argparse
import shutil
import zipfile
from pathlib import Path

import nbtlib
from nbtlib import Compound

ROOT = Path(r"C:\Users\xieto\Desktop\AquaTech")
JAR_NAME = "IndustrialUpgrade-1.20.1-3.4.0.11.jar"
PARK = ROOT / "_parked_mods" / "iu_space_dims_enabled"
PARK_JAR = PARK / JAR_NAME
PARK_WORLD_DIMS = PARK / "world_dimensions_industrialupgrade"
WORLD_DIMS = ROOT / "server" / "world" / "dimensions" / "industrialupgrade"
LEVEL_DAT = ROOT / "server" / "world" / "level.dat"

MOD_TARGETS = [
    ROOT / "mods",
    ROOT / "server" / "mods",
    ROOT / "client" / "mods",
    ROOT / "server" / "client" / "mods",
    ROOT / "dist" / "AquaTech-Client" / "mods",
    Path(r"C:\Users\xieto\curseforge\minecraft\Instances\AquaTech\mods"),
    Path(r"C:\Users\xieto\curseforge\minecraft\Instances\AquaTech\client\mods"),
]

DIM_PREFIX = "data/industrialupgrade/dimension/"
# Keep dimension_type / biomes / noise — only LevelStem JSON makes the world load.


def find_source_jar() -> Path:
    for p in [ROOT / "server" / "mods" / JAR_NAME, ROOT / "mods" / JAR_NAME, PARK_JAR]:
        if p.is_file():
            return p
    raise SystemExit(f"Missing {JAR_NAME}")


def strip_jar(src: Path, dst: Path) -> int:
    removed = 0
    dst.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(src, "r") as zin, zipfile.ZipFile(dst, "w", compression=zipfile.ZIP_DEFLATED) as zout:
        for info in zin.infolist():
            name = info.filename
            # Only LevelStem entries under dimension/*.json (not dimension_type/)
            if name.startswith(DIM_PREFIX) and name.endswith(".json") and "/dimension_type/" not in name:
                # e.g. data/industrialupgrade/dimension/moon.json
                rest = name[len(DIM_PREFIX) :]
                if "/" not in rest.rstrip("/"):
                    removed += 1
                    continue
            zout.writestr(info, zin.read(info.filename))
    return removed


def deploy_jar(stripped: Path) -> None:
    for d in MOD_TARGETS:
        d.mkdir(parents=True, exist_ok=True)
        target = d / JAR_NAME
        if target.resolve() == stripped.resolve():
            continue
        shutil.copy2(stripped, target)
        print(f"OK jar -> {target}")


def park_world_dims() -> None:
    if not WORLD_DIMS.is_dir():
        print("No world IU dimensions folder (ok)")
        return
    PARK.mkdir(parents=True, exist_ok=True)
    if PARK_WORLD_DIMS.exists():
        shutil.rmtree(PARK_WORLD_DIMS)
    shutil.move(str(WORLD_DIMS), str(PARK_WORLD_DIMS))
    print(f"Parked world dims -> {PARK_WORLD_DIMS}")


def restore_world_dims() -> None:
    if not PARK_WORLD_DIMS.is_dir():
        print("No parked world dims to restore")
        return
    WORLD_DIMS.parent.mkdir(parents=True, exist_ok=True)
    if WORLD_DIMS.exists():
        shutil.rmtree(WORLD_DIMS)
    shutil.move(str(PARK_WORLD_DIMS), str(WORLD_DIMS))
    print(f"Restored world dims -> {WORLD_DIMS}")


def clean_level_dat() -> int:
    if not LEVEL_DAT.is_file():
        print("No level.dat")
        return 0
    nbt = nbtlib.load(LEVEL_DAT)
    data = nbt.get("Data") or nbt.get("data")
    if data is None:
        print("level.dat: no Data tag")
        return 0

    removed = 0
    # Modern path: Data.WorldGenSettings.dimensions
    wgs = data.get("WorldGenSettings")
    if isinstance(wgs, Compound):
        dims = wgs.get("dimensions")
        if isinstance(dims, Compound):
            keys = [k for k in list(dims.keys()) if str(k).startswith("industrialupgrade:")]
            for k in keys:
                del dims[k]
                removed += 1

    # Also forge/new path sometimes mirrors under Data.dimensions
    dims2 = data.get("dimensions")
    if isinstance(dims2, Compound):
        keys = [k for k in list(dims2.keys()) if str(k).startswith("industrialupgrade:")]
        for k in keys:
            del dims2[k]
            removed += 1

    if removed:
        # backup
        bak = LEVEL_DAT.with_suffix(".dat.bak_iu_space")
        if not bak.exists():
            shutil.copy2(LEVEL_DAT, bak)
            print(f"Backed up level.dat -> {bak}")
        nbt.save(LEVEL_DAT)
    print(f"Removed {removed} industrialupgrade dimension entries from level.dat")
    return removed


def disable() -> None:
    PARK.mkdir(parents=True, exist_ok=True)
    src = find_source_jar()

    # Prefer already-full jar as park source (if current is already stripped, use park)
    with zipfile.ZipFile(src, "r") as z:
        dim_count = sum(
            1
            for n in z.namelist()
            if n.startswith(DIM_PREFIX) and n.endswith(".json") and n.count("/") == 3
        )

    if dim_count == 0:
        if not PARK_JAR.is_file():
            raise SystemExit("Current IU jar already stripped and no parked original found.")
        src = PARK_JAR
        print(f"Using parked original jar: {src}")
    else:
        if not PARK_JAR.exists() or PARK_JAR.stat().st_size != src.stat().st_size:
            shutil.copy2(src, PARK_JAR)
            print(f"Parked original jar ({dim_count} dims) -> {PARK_JAR}")

    stripped = PARK / f"{JAR_NAME}.nospace"
    removed = strip_jar(PARK_JAR if PARK_JAR.is_file() else src, stripped)
    print(f"Stripped {removed} LevelStem dimension JSONs")
    deploy_jar(stripped)
    # Also write as the canonical server mods name
    shutil.copy2(stripped, ROOT / "server" / "mods" / JAR_NAME)
    park_world_dims()
    clean_level_dat()
    print("DONE: IU space dimensions disabled. Restart server.")


def enable() -> None:
    if not PARK_JAR.is_file():
        raise SystemExit(f"Missing parked jar: {PARK_JAR}")
    deploy_jar(PARK_JAR)
    shutil.copy2(PARK_JAR, ROOT / "server" / "mods" / JAR_NAME)
    restore_world_dims()
    print("DONE: IU space dimensions restored (level.dat entries may regenerate on load). Restart server.")


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("action", choices=["disable", "enable"])
    args = ap.parse_args()
    if args.action == "disable":
        disable()
    else:
        enable()


if __name__ == "__main__":
    main()
