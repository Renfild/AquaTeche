#!/usr/bin/env python3
"""Zip the C# `dotnet publish` output into dist/releases/AquaTechLauncher.zip."""
from __future__ import annotations

import shutil
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "dist" / "AquaTechLauncher"
STAGE = ROOT / "dist" / "releases" / "AquaTechLauncher"
ZIP_PATH = ROOT / "dist" / "releases" / "AquaTechLauncher.zip"


def main() -> None:
    if not SRC.is_dir():
        raise SystemExit(
            f"Missing {SRC}. Run: "
            "dotnet publish launcher/src/AquaTechLauncher/AquaTechLauncher.csproj "
            "-c Release -r win-x64 --self-contained true -o dist/AquaTechLauncher"
        )
    STAGE.parent.mkdir(parents=True, exist_ok=True)
    if STAGE.exists():
        shutil.rmtree(STAGE)
    shutil.copytree(SRC, STAGE)
    ZIP_PATH.unlink(missing_ok=True)
    with zipfile.ZipFile(ZIP_PATH, "w", zipfile.ZIP_DEFLATED) as z:
        for p in SRC.rglob("*"):
            if p.is_file():
                z.write(p, p.relative_to(SRC).as_posix())
    print(f"Created {ZIP_PATH} ({ZIP_PATH.stat().st_size / 1024 / 1024:.2f} MB)")


if __name__ == "__main__":
    main()
