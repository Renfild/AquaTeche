"""Build AquaTech client pack + manifest for the launcher.

Usage:
  python tools/publish_client_pack.py

What it does:
  1) Syncs mods/kubejs/config from CurseForge instance (or server/client) into dist/AquaTech-Client
  2) Writes manifest.json (md5 of every file)
  3) Prints how to publish updates for friends (GitHub Release)

After this, the launcher copies from dist/AquaTech-Client automatically when run on this PC.
"""
from __future__ import annotations

import hashlib
import json
import shutil
import sys
from pathlib import Path

ROOT = Path(r"C:\Users\xieto\Desktop\AquaTech")
CF = Path(r"C:\Users\xieto\curseforge\minecraft\Instances\AquaTech")
PACK = ROOT / "dist" / "AquaTech-Client"
GITHUB_RELEASE = "https://github.com/Renfild/AquaTeche/releases/download/v1.0.0"
GITHUB_RAW = "https://raw.githubusercontent.com/Renfild/AquaTeche/main/dist/AquaTech-Client"

# Prefer working CurseForge instance as source of truth for the client pack
SOURCES = [
    CF,  # primary
    ROOT / "client",
    ROOT / "dist" / "AquaTech-Client",
]

FOLDERS = ["mods", "config", "kubejs", "resourcepacks"]

# Client-only: never pull these server plugins as jars into client mods
SKIP_NAME_PARTS = ("_disabled", "_parked", ".disabled")


def md5_file(p: Path) -> str:
    h = hashlib.md5()
    with open(p, "rb") as f:
        for chunk in iter(lambda: f.read(65536), b""):
            h.update(chunk)
    return h.hexdigest()


def pick_source() -> Path:
    for s in SOURCES:
        if (s / "mods").is_dir() and any((s / "mods").glob("*.jar")):
            return s
    raise SystemExit("No source pack with mods/ found")


def sync_folder(src_root: Path, folder: str) -> None:
    src = src_root / folder
    dst = PACK / folder
    if not src.exists():
        return
    if dst.exists():
        shutil.rmtree(dst)
    shutil.copytree(
        src,
        dst,
        ignore=shutil.ignore_patterns("*.tmp", "*.log", ".git", "__pycache__"),
    )
    # Clean junk jars
    if folder == "mods":
        for jar in dst.glob("*.jar"):
            name = jar.name.lower()
            if any(x in name for x in SKIP_NAME_PARTS):
                jar.unlink()
    print(f"OK {folder}: {src} -> {dst}")


def write_manifest() -> Path:
    files = []
    for folder in FOLDERS:
        root = PACK / folder
        if not root.exists():
            continue
        for path in root.rglob("*"):
            if not path.is_file():
                continue
            if path.name.startswith(".") or path.suffix.lower() in {".tmp", ".log", ".bak"}:
                continue
            rel = path.relative_to(PACK).as_posix()
            entry = {
                "path": rel,
                "md5": md5_file(path),
                "size": path.stat().st_size,
            }
            if folder == "mods":
                entry["url"] = f"{GITHUB_RELEASE}/{path.name}"
            else:
                entry["url"] = f"{GITHUB_RAW}/{rel}"
            files.append(entry)

    manifest = {
        "version": "1.0.0",
        "mc_version": "1.20.1",
        "forge_version": "47.4.0",
        "server_ip": "katherine-hydro.tun.ply.gg",
        "server_port": 25565,
        "files": files,
    }
    out1 = PACK / "manifest.json"
    out2 = ROOT / "dist" / "launcher" / "manifest.json"
    out2.parent.mkdir(parents=True, exist_ok=True)
    text = json.dumps(manifest, indent=2, ensure_ascii=False)
    out1.write_text(text, encoding="utf-8")
    out2.write_text(text, encoding="utf-8")
    print(f"OK manifest: {len(files)} files -> {out1}")
    return out1


def main() -> int:
    src = pick_source()
    print(f"Source pack: {src}")
    PACK.mkdir(parents=True, exist_ok=True)
    for folder in FOLDERS:
        sync_folder(src, folder)
    # Always ensure our latest custom jars from server/mods win
    for name in ("aquatech_ui-1.0.0.jar", "casesmod-1.0.0.jar", "packetfixer-3.3.2-forge-1.20.1.jar"):
        for cand in (ROOT / "server" / "mods" / name, ROOT / "mods" / name, CF / "mods" / name):
            if cand.exists():
                shutil.copy2(cand, PACK / "mods" / name)
                print(f"OK force {name}")
                break
    write_manifest()
    print()
    print("=== How friends get updates (no zip) ===")
    print("1) YOU: run this script after changing mods/kubejs")
    print("2) YOU: keep start_sync_server.bat running (+ Playit TCP -> 8080)")
    print("3) FRIENDS: in launcher set «URL обновлений» to Playit URL, click «Обновить сборку»")
    print("   Only changed files download by MD5.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
