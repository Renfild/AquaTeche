#!/usr/bin/env python3
"""Unified build and release orchestrator for AquaTech Launcher (Go bootstrap + C# Avalonia).

Usage:
  python tools/release_launcher.py [--version X.Y.Z] [--upload] [--dry-run]
"""
from __future__ import annotations

import argparse
import hashlib
import json
import os
import re
import shutil
import subprocess
import sys
import urllib.error
import urllib.request
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
REPO = "Renfild/AquaTeche"
DIST = ROOT / "dist"
REL = DIST / "releases"
STAGE = DIST / "AquaTechLauncher"
DOCS_MANIFEST = ROOT / "docs" / "bootstrap.json"
LAUNCHER_CONSTANTS = ROOT / "launcher" / "src" / "AquaTechLauncher.Core" / "LauncherConstants.cs"
LAUNCHER_CSPROJ = ROOT / "launcher" / "src" / "AquaTechLauncher" / "AquaTechLauncher.csproj"


def token() -> str:
    token_file = ROOT / ".gh_token"
    if not token_file.is_file():
        return ""
    return token_file.read_text(encoding="utf-8").strip()


def md5_file(path: Path) -> str:
    h = hashlib.md5()
    with open(path, "rb") as f:
        for chunk in iter(lambda: f.read(1 << 20), b""):
            h.update(chunk)
    return h.hexdigest()


def get_current_version() -> str:
    if LAUNCHER_CONSTANTS.is_file():
        content = LAUNCHER_CONSTANTS.read_text(encoding="utf-8")
        m = re.search(r'public const string Version = "([^"]+)";', content)
        if m:
            return m.group(1).strip()
    return "2.9.69"


def sync_version_in_code(version: str) -> None:
    print(f"[*] Syncing launcher version to {version}...")
    if LAUNCHER_CONSTANTS.is_file():
        content = LAUNCHER_CONSTANTS.read_text(encoding="utf-8")
        updated = re.sub(
            r'public const string Version = "[^"]+";',
            f'public const string Version = "{version}";',
            content,
        )
        LAUNCHER_CONSTANTS.write_text(updated, encoding="utf-8")

    if LAUNCHER_CSPROJ.is_file():
        content = LAUNCHER_CSPROJ.read_text(encoding="utf-8")
        v_quad = f"{version}.0" if version.count(".") == 2 else version
        content = re.sub(
            r"<AssemblyVersion>[^<]+</AssemblyVersion>",
            f"<AssemblyVersion>{v_quad}</AssemblyVersion>",
            content,
        )
        content = re.sub(
            r"<FileVersion>[^<]+</FileVersion>",
            f"<FileVersion>{v_quad}</FileVersion>",
            content,
        )
        LAUNCHER_CSPROJ.write_text(content, encoding="utf-8")


def run_command(cmd: list[str], cwd: Path | None = None) -> None:
    print(f"[>] Running: {' '.join(cmd)}")
    res = subprocess.run(cmd, cwd=cwd or ROOT, shell=False)
    if res.returncode != 0:
        raise SystemExit(f"[!] Command failed with exit code {res.returncode}: {' '.join(cmd)}")


def build_go_bootstrap(out_path: Path) -> None:
    print("[*] Building Go bootstrap...")
    out_path.parent.mkdir(parents=True, exist_ok=True)
    go_dir = ROOT / "bootstrap"
    run_command(["go", "build", "-o", str(out_path), "."], cwd=go_dir)
    print(f"[+] Go bootstrap compiled: {out_path} ({out_path.stat().st_size / (1024*1024):.2f} MB)")


def publish_dotnet_launcher(stage_dir: Path) -> None:
    print("[*] Publishing .NET 9 Avalonia Launcher...")
    if stage_dir.exists():
        shutil.rmtree(stage_dir)
    stage_dir.mkdir(parents=True, exist_ok=True)
    csproj = ROOT / "launcher" / "src" / "AquaTechLauncher" / "AquaTechLauncher.csproj"
    run_command(
        [
            "dotnet",
            "publish",
            str(csproj),
            "-c",
            "Release",
            "-r",
            "win-x64",
            "--self-contained",
            "true",
            "-o",
            str(stage_dir),
        ]
    )


def pack_launcher_zip(src_dir: Path, zip_dest: Path) -> None:
    print(f"[*] Packing {src_dir} into {zip_dest}...")
    zip_dest.parent.mkdir(parents=True, exist_ok=True)
    zip_dest.unlink(missing_ok=True)
    with zipfile.ZipFile(zip_dest, "w", zipfile.ZIP_DEFLATED) as z:
        for p in src_dir.rglob("*"):
            if p.is_file():
                z.write(p, p.relative_to(src_dir).as_posix())
    print(f"[+] Created {zip_dest} ({zip_dest.stat().st_size / (1024*1024):.2f} MB)")


def update_manifest(version: str, zip_path: Path) -> dict:
    tag = f"client-{version}"
    manifest = {
        "version": version,
        "launcher_zip": "https://aquateche.store/dl/AquaTechLauncher.zip",
        "launcher_exe": "AquaTechLauncher.exe",
        "release_base": "https://aquateche.store/dl",
        "launcher_zip_md5": md5_file(zip_path),
        "launcher_zip_size": zip_path.stat().st_size,
        "pack_cdn": "https://aquateche.store/pack",
    }
    DOCS_MANIFEST.parent.mkdir(parents=True, exist_ok=True)
    DOCS_MANIFEST.write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"[+] Manifest updated: {DOCS_MANIFEST}")
    return manifest


def gh_api(method: str, url: str, gh_token: str, data: bytes | None = None, content_type: str | None = None):
    headers = {
        "Authorization": f"token {gh_token}",
        "Accept": "application/vnd.github+json",
        "User-Agent": "AquaTechReleaseBot",
    }
    if content_type:
        headers["Content-Type"] = content_type
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=600) as r:
            body = r.read()
            return r.status, json.loads(body) if body else {}
    except urllib.error.HTTPError as e:
        err = e.read().decode("utf-8", "replace")
        raise SystemExit(f"GitHub API error {e.code} {url}: {err[:800]}") from e


def upload_release(version: str, files: list[Path]) -> None:
    gh_token = token()
    if not gh_token:
        print("[!] No .gh_token found, skipping GitHub upload.")
        return

    tag = f"client-{version}"
    print(f"[*] Uploading release {tag} to GitHub ({REPO})...")
    body = (
        f"## AquaTech Client v{version}\n\n"
        "- High-performance parallel CDN mirror probing\n"
        "- Sub-second warm-start manifest synchronization\n"
        "- Native libraries extracted layout for zero-delay startup\n"
    )
    payload = json.dumps(
        {
            "tag_name": tag,
            "target_commitish": "main",
            "name": f"AquaTech Client v{version}",
            "body": body,
            "draft": True,
            "prerelease": False,
        }
    ).encode()

    _, rel = gh_api("POST", f"https://api.github.com/repos/{REPO}/releases", gh_token, data=payload, content_type="application/json")
    release_id = rel["id"]
    upload_url_tpl = rel.get("upload_url", "")
    base_upload = upload_url_tpl.split("{")[0]

    for path in files:
        print(f"[*] Uploading {path.name} ({path.stat().st_size / (1024*1024):.2f} MB)...")
        with open(path, "rb") as f:
            file_data = f.read()
        gh_api(
            "POST",
            f"{base_upload}?name={path.name}",
            gh_token,
            data=file_data,
            content_type="application/octet-stream",
        )

    _, published = gh_api(
        "PATCH",
        f"https://api.github.com/repos/{REPO}/releases/{release_id}",
        gh_token,
        data=json.dumps({"draft": False, "make_latest": "true"}).encode(),
        content_type="application/json",
    )
    print(f"[+] Successfully published release {tag} ({published.get('html_url')})")


def main() -> None:
    parser = argparse.ArgumentParser(description="Build and release AquaTech Launcher.")
    parser.add_argument("--version", default="", help="Launcher version (e.g. 2.9.69)")
    parser.add_argument("--upload", action="store_true", help="Upload release artifacts to GitHub")
    parser.add_argument("--dry-run", action="store_true", help="Build artifacts without updating release files")
    args = parser.parse_args()

    version = args.version.strip() or get_current_version()
    print(f"=== AquaTech Launcher Release Automation (v{version}) ===")

    if not args.dry_run:
        sync_version_in_code(version)

    go_exe = REL / "AquaTech.exe"
    zip_dest = REL / "AquaTechLauncher.zip"

    # Step 1: Go bootstrap
    build_go_bootstrap(go_exe)

    # Step 2: Dotnet Avalonia
    publish_dotnet_launcher(STAGE)

    # Step 3: Zip pack
    pack_launcher_zip(STAGE, zip_dest)

    # Step 4: Update bootstrap.json
    if not args.dry_run:
        manifest = update_manifest(version, zip_dest)
        print("Updated manifest:", json.dumps(manifest, indent=2))

    # Step 5: Upload if requested
    if args.upload:
        upload_release(version, [go_exe, zip_dest])

    print("\n[OK] Release preparation completed successfully!")


if __name__ == "__main__":
    main()
