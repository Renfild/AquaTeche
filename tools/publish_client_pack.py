"""Build AquaTech client pack + manifest for online CDN (website + GitHub Releases).

Usage:
  python tools/publish_client_pack.py

Friends get updates from:
  https://aquatech-7gs.pages.dev/pack/manifest.json
  (each file URL points at GitHub Release pack-2.9.2)
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
DOCS_PACK = ROOT / "docs" / "pack"
PACK_TAG = "pack-2.9.2"
GITHUB_RELEASE = f"https://github.com/Renfild/AquaTeche/releases/download/{PACK_TAG}"
SITE_PACK = "https://aquatech-7gs.pages.dev/pack"

SOURCES = [
    CF,
    ROOT / "client",
    ROOT / "dist" / "AquaTech-Client",
]

FOLDERS = ["mods", "config", "kubejs", "resourcepacks"]
SKIP_NAME_PARTS = ("_disabled", "_parked", ".disabled")


def md5_file(p: Path) -> str:
    h = hashlib.md5()
    with open(p, "rb") as f:
        for chunk in iter(lambda: f.read(65536), b""):
            h.update(chunk)
    return h.hexdigest()


def asset_name(rel: str) -> str:
    """Stable GitHub Release asset name for a pack-relative path."""
    rel = rel.replace("\\", "/").lstrip("/")
    folder = rel.split("/", 1)[0] if "/" in rel else ""
    if folder == "mods":
        name = Path(rel).name
    else:
        name = rel.replace("/", "__")
    # Spaces break GitHub asset URLs; keep '+' (already used on pack-2.9.2 uploads).
    return name.replace(" ", "_")


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
    if folder == "mods":
        for jar in list(dst.glob("*.jar")) + list(dst.rglob("*.jar")):
            name = jar.name.lower()
            rel = jar.relative_to(dst).as_posix().lower()
            if any(x in name for x in SKIP_NAME_PARTS) or rel.startswith("disabled/") or "/disabled/" in f"/{rel}":
                jar.unlink(missing_ok=True)
        # drop empty disabled dirs
        for d in sorted(dst.rglob("*"), reverse=True):
            if d.is_dir():
                try:
                    next(d.iterdir())
                except StopIteration:
                    d.rmdir()
                except OSError:
                    pass
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
            if path.stat().st_size <= 0:
                continue
            rel = path.relative_to(PACK).as_posix()
            aname = asset_name(rel)
            files.append(
                {
                    "path": rel,
                    "md5": md5_file(path),
                    "size": path.stat().st_size,
                    "url": f"{GITHUB_RELEASE}/{aname}",
                    "asset": aname,
                }
            )

    manifest = {
        "version": "2.9.2",
        "mc_version": "1.20.1",
        "forge_version": "47.4.0",
        "server_ip": "katherine-hydro.tun.ply.gg",
        "server_port": 31279,
        "cdn": SITE_PACK,
        "files": files,
    }
    text = json.dumps(manifest, indent=2, ensure_ascii=False)
    out1 = PACK / "manifest.json"
    out2 = ROOT / "dist" / "launcher" / "manifest.json"
    out2.parent.mkdir(parents=True, exist_ok=True)
    DOCS_PACK.mkdir(parents=True, exist_ok=True)
    out1.write_text(text, encoding="utf-8")
    out2.write_text(text, encoding="utf-8")
    (DOCS_PACK / "manifest.json").write_text(text, encoding="utf-8")
    print(f"OK manifest: {len(files)} files -> {out1}")
    print(f"OK site manifest -> {DOCS_PACK / 'manifest.json'}")
    return out1


def main() -> int:
    src = pick_source()
    print(f"Source pack: {src}")
    PACK.mkdir(parents=True, exist_ok=True)
    for folder in FOLDERS:
        sync_folder(src, folder)
    for name in ("aquatech_ui-1.0.0.jar", "casesmod-1.0.0.jar", "packetfixer-3.3.2-forge-1.20.1.jar"):
        for cand in (ROOT / "server" / "mods" / name, ROOT / "mods" / name, CF / "mods" / name):
            if cand.exists():
                shutil.copy2(cand, PACK / "mods" / name)
                print(f"OK force {name}")
                break
    write_manifest()
    print()
    print("=== Online updates (LoliLand-style) ===")
    print(f"1) Upload jars: python tools/upload_pack_release.py")
    print(f"2) Push docs/pack/manifest.json to GitHub (Pages serves {SITE_PACK})")
    print("3) Friends: launcher DEFAULT CDN is the website — no Playit needed for mods")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
