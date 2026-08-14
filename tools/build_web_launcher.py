"""Build script for AquaTech Web-Launcher (PyInstaller)."""
from __future__ import annotations

import os
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DIST = ROOT / "dist" / "web_launcher"


def main() -> int:
    print("[AquaTech Build] Packaging Web-Launcher via PyInstaller...")
    
    cmd = [
        sys.executable,
        "-m",
        "PyInstaller",
        "--noconfirm",
        "--onedir",
        "--noconsole",
        "--name=AquaTechLauncher",
        "--exclude-module=PyQt5",
        "--exclude-module=PyQt6",
        "--exclude-module=PySide2",
        "--exclude-module=PySide6",
        "--exclude-module=numpy",
        "--exclude-module=matplotlib",
        "--exclude-module=scipy",
        "--exclude-module=pandas",
        "--exclude-module=PIL",
        f"--add-data={ROOT / 'docs'};docs",
        f"--add-data={ROOT / 'tools'};tools",
        str(ROOT / "tools" / "aquatech_web_launcher.py"),
        f"--distpath={DIST}",
        f"--workpath={ROOT / 'build' / 'pyinstaller_work'}",
    ]

    print("Running PyInstaller:", " ".join(cmd))
    res = subprocess.run(cmd, cwd=str(ROOT))
    
    if res.returncode == 0:
        exe_path = DIST / "AquaTechLauncher" / "AquaTechLauncher.exe"
        if exe_path.is_file():
            size_mb = exe_path.stat().st_size / (1024 * 1024)
            print(f"OK Executable generated successfully: {exe_path} ({size_mb:.2f} MB)")
            return 0
    
    print(f"ERROR PyInstaller failed with exit code {res.returncode}")
    return res.returncode


if __name__ == "__main__":
    sys.exit(main())
