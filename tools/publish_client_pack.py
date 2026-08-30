"""Build AquaTech client pack + manifest for online CDN (website + GitHub Releases).

Usage:
  python tools/publish_client_pack.py

Pack version is PACK_TAG below (keep in sync with upload_pack_release.py).
Source of truth: server/mods + client-only jars from dist/AquaTech-Client (never CurseForge first).
"""
from __future__ import annotations

import hashlib
import json
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PACK = ROOT / "dist" / "AquaTech-Client"
DOCS_PACK = ROOT / "docs" / "pack"
SERVER_MODS = ROOT / "server" / "mods"
PACK_TAG = "pack-2.9.272"
PACK_VERSION = "2.9.272"
GITHUB_RELEASE = f"https://github.com/Renfild/AquaTeche/releases/download/{PACK_TAG}"
SITE_PACK = "https://cdn.jsdelivr.net/gh/Renfild/AquaTeche@main/docs/pack"

FOLDERS = ["mods", "config", "kubejs", "resourcepacks"]
SKIP_PATH_PARTS = ("_disabled", "_parked", ".disabled", "/players/", "quest_progress", "aquatech_sync_key.json", "chunky")
# Client-only jars (not on dedicated server) — copy from previous pack or client/ if present
# ImmediatelyFast 1.2.4 crashes with Oculus 1.7 (IrisCompat ImmediateState) — omit until a compatible build.
CLIENT_ONLY_PREFIXES = (
    "mcef",
    "embeddium",
    "oculus",
    "entityculling",
    "dynamic-fps",
    "betterquestpopup",
    "fancymenu",
    "konkrete",
    "melody",
)
# Never pull these from client/ — server copy wins (and may differ by patch version)
SERVER_OWNED_PREFIXES = (
    "ftb-",
    "aquatech_ui",
    "packetfixer",
    "aqualumen",
)


def md5_file(p: Path) -> str:
    h = hashlib.md5()
    with open(p, "rb") as f:
        for chunk in iter(lambda: f.read(65536), b""):
            h.update(chunk)
    return h.hexdigest()


def asset_name(rel: str) -> str:
    rel = rel.replace("\\", "/").lstrip("/")
    folder = rel.split("/", 1)[0] if "/" in rel else ""
    if folder == "mods":
        name = Path(rel).name
    else:
        name = rel.replace("/", "__")
    return name.replace(" ", "_")


def should_skip(rel: str) -> bool:
    low = rel.replace("\\", "/").lower()
    return any(x in low for x in SKIP_PATH_PARTS)


def sync_config_kube_resources() -> None:
    """Prefer repo roots, fall back to existing pack."""
    mapping = {
        "config": [ROOT / "config", ROOT / "server" / "config", PACK / "config"],
        "kubejs": [ROOT / "kubejs", ROOT / "server" / "kubejs", PACK / "kubejs"],
        "resourcepacks": [ROOT / "resourcepacks", PACK / "resourcepacks"],
    }
    for folder, cands in mapping.items():
        src = next((c for c in cands if c.is_dir()), None)
        dst = PACK / folder
        if src is None:
            print(f"skip missing {folder}")
            continue
        if dst.exists() and dst.resolve() != src.resolve():
            shutil.rmtree(dst)
        if dst.resolve() != src.resolve():
            shutil.copytree(
                src,
                dst,
                ignore=shutil.ignore_patterns("*.tmp", "*.log", ".git", "__pycache__"),
            )
        # Drop player progress / parked
        for path in list(dst.rglob("*")):
            if not path.is_file():
                continue
            rel = path.relative_to(PACK).as_posix()
            if should_skip(rel):
                path.unlink(missing_ok=True)
        print(f"OK {folder}: {src} -> {dst}")


def sync_mods() -> None:
    dst = PACK / "mods"
    if dst.exists():
        shutil.rmtree(dst)
    dst.mkdir(parents=True)

    if not SERVER_MODS.is_dir():
        raise SystemExit(f"missing {SERVER_MODS}")

    for jar in SERVER_MODS.glob("*.jar"):
        if should_skip(jar.name):
            continue
        shutil.copy2(jar, dst / jar.name)

    # Client-only from previous pack / client/
    extras_roots = [PACK.parent / "AquaTech-Client" / "mods", ROOT / "client" / "mods", ROOT / "mods"]
    # PACK was wiped mods — use client and mods folders
    for root in (
        ROOT / "client" / "mods",
        ROOT / "mods",
        ROOT / "mods" / "aquatech-ui" / "libs",
    ):
        if not root.is_dir():
            continue
        for jar in root.glob("*.jar"):
            low = jar.name.lower()
            if should_skip(low):
                continue
            if any(low.startswith(p) or p in low for p in SERVER_OWNED_PREFIXES):
                continue
            if "1.20.4" in low:
                continue
            if not any(low.startswith(p) or p in low for p in CLIENT_ONLY_PREFIXES):
                continue
            # Skip if server already shipped a jar with same mod family prefix
            family = low.split("-")[0]
            if any(x.name.lower().startswith(family) for x in dst.glob("*.jar")):
                continue
            target = dst / jar.name
            if not target.exists():
                shutil.copy2(jar, target)
                print(f"OK client-only {jar.name}")

    # Latest compiled first-party jars from mod projects (drop older copies).
    first_party = [
        (ROOT / "mods" / "aquatech-ui" / "build" / "libs", "aquatech_ui-", "aquatech_ui-"),
        (ROOT / "mods" / "aqualumen-ui" / "build" / "libs", "aqualumen-forge-", "aqualumen-"),
    ]
    for lib_dir, glob_prefix, purge_prefix in first_party:
        jars = sorted(
            (p for p in lib_dir.glob(f"{glob_prefix}*.jar") if p.is_file()),
            key=lambda p: p.stat().st_mtime,
            reverse=True,
        )
        if not jars:
            continue
        src_jar = jars[0]
        for folder in (SERVER_MODS, dst):
            if not folder.is_dir():
                continue
            for old in folder.glob(f"{purge_prefix}*.jar"):
                if old.name != src_jar.name:
                    old.unlink(missing_ok=True)
            shutil.copy2(src_jar, folder / src_jar.name)
        print(f"OK fresh compiled {src_jar.name} from {src_jar}")

    # Remove parked leftovers
    for jar in list(dst.rglob("*.jar")):
        rel = jar.relative_to(PACK).as_posix()
        if should_skip(rel):
            jar.unlink(missing_ok=True)

    print(f"OK mods: {len(list(dst.glob('*.jar')))} jars")


def load_prev_manifest() -> dict | None:
    """Previous manifest: docs/pack/manifest.json (last published) or dist backup."""
    candidates = [
        DOCS_PACK / "manifest.json",
        ROOT / "dist" / "AquaTech-Client" / "manifest.prev.json",
    ]
    for prev in candidates:
        try:
            if prev.is_file():
                data = json.loads(prev.read_text(encoding="utf-8-sig"))
                if data.get("files") and data.get("version") != PACK_VERSION:
                    return data
        except Exception as e:
            print(f"WARN previous manifest unreadable ({prev.name}): {e}")
    return None


def backup_current_manifest() -> None:
    """Keep a copy of the current manifest before overwriting, for next delta."""
    src = DOCS_PACK / "manifest.json"
    try:
        if src.is_file():
            data = json.loads(src.read_text(encoding="utf-8-sig"))
            if data.get("version") and data["version"] != PACK_VERSION:
                shutil.copy2(src, ROOT / "dist" / "AquaTech-Client" / "manifest.prev.json")
                print(f"OK backed up manifest v{data['version']} for next delta")
    except Exception as e:
        print(f"WARN manifest backup failed: {e}")


def write_delta(manifest: dict, prev: dict | None) -> None:
    """delta.json: what changed vs previous pack. Launcher uses it to skip
    re-hashing unchanged files and show accurate download size."""
    prev_map = {f["path"]: f for f in (prev or {}).get("files", [])}
    new_map = {f["path"]: f for f in manifest["files"]}

    added = sorted(set(new_map) - set(prev_map))
    removed = sorted(set(prev_map) - set(new_map))
    changed = sorted(
        p for p in set(new_map) & set(prev_map)
        if new_map[p]["md5"] != prev_map[p].get("md5")
        or new_map[p]["size"] != prev_map[p].get("size")
    )

    delta = {
        "from_version": (prev or {}).get("version"),
        "to_version": manifest["version"],
        "added": [new_map[p] for p in added],
        "changed": [new_map[p] for p in changed],
        "removed": [{"path": p} for p in removed],
        "stats": {
            "added": len(added),
            "changed": len(changed),
            "removed": len(removed),
            "unchanged": len(new_map) - len(added) - len(changed),
            "download_bytes": sum(new_map[p]["size"] for p in added + changed),
        },
    }
    text = json.dumps(delta, indent=2, ensure_ascii=False)
    for out in (PACK / "delta.json", DOCS_PACK / "delta.json"):
        out.write_text(text, encoding="utf-8")

    st = delta["stats"]
    mb = st["download_bytes"] / 1_000_000
    print(
        f"OK delta: +{st['added']} ~{st['changed']} -{st['removed']} "
        f"(unchanged {st['unchanged']}), incremental download ~{mb:.1f} MB"
    )


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
            if should_skip(rel):
                continue
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
        "version": PACK_VERSION,
        "mc_version": "1.20.1",
        "forge_version": "47.4.0",
        "server_ip": "g-pl-3.apexnodes.xyz",
        "server_port": 21561,
        "cdn": SITE_PACK,
        "files": files,
    }
    text = json.dumps(manifest, indent=2, ensure_ascii=False)
    backup_current_manifest()
    out1 = PACK / "manifest.json"
    prev = load_prev_manifest()
    write_delta(json.loads(text), prev)
    out2 = ROOT / "dist" / "launcher" / "manifest.json"
    out2.parent.mkdir(parents=True, exist_ok=True)
    DOCS_PACK.mkdir(parents=True, exist_ok=True)
    out1.write_text(text, encoding="utf-8")
    out2.write_text(text, encoding="utf-8")
    (DOCS_PACK / "manifest.json").write_text(text, encoding="utf-8")
    print(f"OK manifest: {len(files)} files -> {out1}")
    return out1


def main() -> int:
    PACK.mkdir(parents=True, exist_ok=True)
    sync_mods()
    sync_config_kube_resources()
    repo_ftb = ROOT / "config" / "ftbquests"
    if repo_ftb.is_dir():
        dst_ftb = PACK / "config" / "ftbquests"
        if dst_ftb.exists():
            shutil.rmtree(dst_ftb)
        shutil.copytree(repo_ftb, dst_ftb)
        print(f"OK overlay ftbquests from repo -> {dst_ftb}")
    write_manifest()
    print()
    print(f"=== Pack {PACK_TAG} ===")
    print("1) python tools/upload_pack_release.py")
    print("2) git push docs/pack/manifest.json")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
