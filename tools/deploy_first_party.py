#!/usr/bin/env python3
"""Full deploy pipeline for first-party mods (aquatech-ui, aqualumen-ui).

Steps:
  1. gradlew jar in each changed mod
  2. Copy jars to server/mods/ and mods/
  3. Bump pack version in publish_client_pack.py + upload_pack_release.py
  4. publish_client_pack.py (build manifest)
  5. upload_pack_release.py (GitHub Release)
  6. direct_deploy_apex.py (SFTP upload + server restart)
  7. smoke_apex_server.py

Usage:
  python tools/deploy_first_party.py [--mods aquatech,aqualumen] [--skip-build] [--skip-github] [--skip-apex]
"""
from __future__ import annotations

import argparse
import hashlib
import importlib.util
import os
import re
import shutil
import subprocess
import sys
import time
import urllib.request
import json
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

MOD_CONFIGS = {
    "aquatech": {
        "dir": ROOT / "mods" / "aquatech-ui",
        "build_glob": "aquatech_ui-*.jar",
        "prefix": "aquatech_ui",
    },
    "aqualumen": {
        "dir": ROOT / "mods" / "aqualumen-ui",
        "build_glob": "aqualumen-forge-*.jar",
        "prefix": "aqualumen",
    },
}

DEPLOY_TARGETS = [ROOT / "mods", ROOT / "server" / "mods"]
CLIENT_MODS = Path(os.environ.get("APPDATA", "")) / "AquaTech" / "mods"
if CLIENT_MODS.is_dir():
    DEPLOY_TARGETS.append(CLIENT_MODS)


# ─── Helpers ────────────────────────────────────────────────────────────────

def run(cmd: list[str], cwd: Path) -> None:
    print(f"\n$ {' '.join(cmd)}")
    result = subprocess.run(cmd, cwd=str(cwd), shell=(os.name == 'nt'))
    if result.returncode != 0:
        sys.exit(f"Command failed: {' '.join(cmd)}")


def md5_file(p: Path) -> str:
    h = hashlib.md5()
    with open(p, "rb") as f:
        for chunk in iter(lambda: f.read(65536), b""):
            h.update(chunk)
    return h.hexdigest()


def bump_pack_version(publish_script: Path, upload_script: Path) -> str:
    """Increment the patch version in publish_client_pack.py and upload_pack_release.py. Returns new version."""
    text = publish_script.read_text(encoding="utf-8")
    m = re.search(r'PACK_VERSION\s*=\s*"(\d+)\.(\d+)\.(\d+)"', text)
    if not m:
        sys.exit("Could not find PACK_VERSION in publish_client_pack.py")
    major, minor, patch = int(m.group(1)), int(m.group(2)), int(m.group(3))
    patch += 1
    new_ver = f"{major}.{minor}.{patch}"
    new_tag = f"pack-{new_ver}"

    def replace_in(path: Path, patterns: list[tuple[str, str]]) -> None:
        t = path.read_text(encoding="utf-8")
        for pat, repl in patterns:
            t = re.sub(pat, repl, t)
        path.write_text(t, encoding="utf-8")

    replace_in(publish_script, [
        (r'(PACK_TAG\s*=\s*")[^"]+(")', rf'\g<1>{new_tag}\2'),
        (r'(PACK_VERSION\s*=\s*")[^"]+(")', rf'\g<1>{new_ver}\2'),
    ])
    replace_in(upload_script, [
        (r'(TAG\s*=\s*")[^"]+(")', rf'\g<1>{new_tag}\2'),
    ])
    print(f"Bumped pack version -> {new_ver} (tag: {new_tag})")
    return new_ver


def copy_jars_to_targets(built: list[Path]) -> None:
    for src in built:
        # Remove old jars with same prefix in all targets
        prefix = src.name.split("-")[0]
        for dst_dir in DEPLOY_TARGETS:
            if not dst_dir.is_dir():
                continue
            for old in dst_dir.glob(f"{prefix}*.jar"):
                if old.name != src.name:
                    old.unlink(missing_ok=True)
                    print(f"  Removed old: {old}")
            dst = dst_dir / src.name
            shutil.copy2(src, dst)
            print(f"  Copied {src.name} -> {dst_dir} (md5={md5_file(dst)[:8]}…)")


def wait_for_apex(secrets: dict, timeout_s: int = 120) -> bool:
    panel = secrets.get("apex_panel", "https://panel.apexnodes.xyz").rstrip("/")
    server_id = secrets["apex_server_id"]
    api_key = secrets["api_key"]
    deadline = time.time() + timeout_s
    while time.time() < deadline:
        try:
            req = urllib.request.Request(
                f"{panel}/api/client/servers/{server_id}/resources",
                headers={
                    "Authorization": f"Bearer {api_key}",
                    "Accept": "Application/vnd.pterodactyl.v1+json",
                },
            )
            with urllib.request.urlopen(req, timeout=10) as r:
                state = json.loads(r.read()).get("attributes", {}).get("current_state")
                print(f"  Apex state: {state}")
                if state == "running":
                    return True
        except Exception as e:
            print(f"  Status check error: {e}")
        time.sleep(6)
    return False


# ─── Main ────────────────────────────────────────────────────────────────────

def main() -> None:
    ap = argparse.ArgumentParser(description="Deploy first-party mods")
    ap.add_argument("--mods", default="aquatech,aqualumen",
                    help="Comma-separated mod keys: aquatech,aqualumen")
    ap.add_argument("--skip-build", action="store_true", help="Skip gradlew jar")
    ap.add_argument("--skip-github", action="store_true", help="Skip GitHub release upload")
    ap.add_argument("--skip-apex", action="store_true", help="Skip Apex server deploy")
    ap.add_argument("--no-bump", action="store_true", help="Don't bump pack version (e.g. config-only deploy)")
    args = ap.parse_args()

    mod_keys = [k.strip() for k in args.mods.split(",")]
    built_jars: list[Path] = []

    # ── Step 1: Build jars ───────────────────────────────────────────────────
    if not args.skip_build:
        print("\n=== Step 1: Building mod jars ===")
        gradlew = "gradlew.bat" if sys.platform == "win32" else "./gradlew"
        for key in mod_keys:
            cfg = MOD_CONFIGS.get(key)
            if not cfg:
                sys.exit(f"Unknown mod key: {key}")
            mod_dir: Path = cfg["dir"]
            print(f"\nBuilding {key} in {mod_dir}…")
            run([gradlew, "jar"], cwd=mod_dir)
            libs = mod_dir / "build" / "libs"
            jars = sorted(libs.glob(cfg["build_glob"]))
            if not jars:
                sys.exit(f"No jar found in {libs} matching {cfg['build_glob']}")
            built_jars.append(jars[-1])
            print(f"  Built: {jars[-1].name}")
    else:
        print("\n=== Step 1: Skipping build ===")
        for key in mod_keys:
            cfg = MOD_CONFIGS.get(key)
            if not cfg:
                continue
            libs = cfg["dir"] / "build" / "libs"
            jars = sorted(libs.glob(cfg["build_glob"]))
            if jars:
                built_jars.append(jars[-1])
                print(f"  Using existing: {jars[-1].name}")

    # ── Step 2: Copy jars to mods/, server/mods/, client ────────────────────
    if built_jars:
        print("\n=== Step 2: Deploying jars to local directories ===")
        copy_jars_to_targets(built_jars)

    # ── Step 3: Bump pack version ────────────────────────────────────────────
    publish_script = ROOT / "tools" / "publish_client_pack.py"
    upload_script = ROOT / "tools" / "upload_pack_release.py"

    if not args.no_bump:
        print("\n=== Step 3: Bumping pack version ===")
        new_version = bump_pack_version(publish_script, upload_script)
    else:
        m = re.search(r'PACK_VERSION\s*=\s*"([^"]+)"', publish_script.read_text(encoding="utf-8"))
        new_version = m.group(1) if m else "?"
        print(f"\n=== Step 3: Skipping version bump (current: {new_version}) ===")

    # ── Step 4: Build client pack manifest ──────────────────────────────────
    print("\n=== Step 4: Building client pack manifest ===")
    run([sys.executable, "tools/publish_client_pack.py"], cwd=ROOT)

    # ── Step 4b: Push manifest.json to git so CDN serves the updated file ───
    # Without this the launcher downloads the stale manifest and reverts jars!
    print("\n=== Step 4b: Pushing manifest.json to git ===")
    run(["git", "add", "docs/pack/manifest.json"], cwd=ROOT)
    # Check if there's actually something to commit
    diff = subprocess.run(["git", "diff", "--cached", "--quiet"], cwd=str(ROOT))
    if diff.returncode != 0:
        run(["git", "commit", "-m", f"chore: pack manifest {new_version}"], cwd=ROOT)
        run(["git", "push", "origin", "main"], cwd=ROOT)
    else:
        print("  manifest.json unchanged, skipping push")

    # ── Step 5: Upload GitHub Release ───────────────────────────────────────
    if not args.skip_github:
        print("\n=== Step 5: Uploading GitHub Release ===")
        run([sys.executable, "tools/upload_pack_release.py"], cwd=ROOT)
    else:
        print("\n=== Step 5: Skipping GitHub release ===")

    # ── Step 6: Deploy to Apex ───────────────────────────────────────────────
    if not args.skip_apex:
        print("\n=== Step 6: Deploying to Apex server ===")
        run([sys.executable, "tools/direct_deploy_apex.py"], cwd=ROOT)

        print("\n=== Step 6b: Waiting for server to start ===")
        secrets_path = ROOT / ".apex_deploy.json"
        if secrets_path.exists():
            secrets = json.loads(secrets_path.read_text(encoding="utf-8"))
            # apex_deploy uses different key names
            secrets.setdefault("api_key", secrets.get("apex_api_key", ""))
            ok = wait_for_apex(secrets)
            if not ok:
                print("WARNING: Server did not reach 'running' within timeout!")
            else:
                print("Server is RUNNING!")
    else:
        print("\n=== Step 6: Skipping Apex deploy ===")

    # ── Step 7: Smoke test ───────────────────────────────────────────────────
    smoke = ROOT / "scripts" / "tasks" / "smoke_apex_server.py"
    if smoke.exists() and not args.skip_apex:
        print("\n=== Step 7: Smoke test ===")
        run([sys.executable, str(smoke)], cwd=ROOT)

    print(f"\n[OK] Deploy complete! Pack version: {new_version}")
    print("Players will receive the update automatically via the launcher.\n")


if __name__ == "__main__":
    main()
