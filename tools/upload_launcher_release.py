#!/usr/bin/env python3
"""Publish LoliLand-style client: Go AquaTech.exe + C# AquaTechLauncher.zip."""
from __future__ import annotations

import hashlib
import json
import sys
import urllib.error
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
REPO = "Renfild/AquaTeche"
TAG = "client-2.9.91"
REL = ROOT / "dist" / "releases"
DOCS_MANIFEST = ROOT / "docs" / "bootstrap.json"

FILES = [
    REL / "AquaTech.exe",
    REL / "AquaTechLauncher.zip",
]


def token() -> str:
    t = (ROOT / ".gh_token").read_text(encoding="utf-8").strip()
    if not t:
        sys.exit("missing .gh_token")
    return t


def md5_file(p: Path) -> str:
    h = hashlib.md5()
    with open(p, "rb") as f:
        for chunk in iter(lambda: f.read(1 << 20), b""):
            h.update(chunk)
    return h.hexdigest()


def api(method: str, url: str, data: bytes | None = None, content_type: str | None = None):
    headers = {
        "Authorization": f"token {token()}",
        "Accept": "application/vnd.github+json",
        "User-Agent": "AquaTechUploader",
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
        raise SystemExit(f"HTTP {e.code} {url}: {err[:800]}") from e


def main() -> None:
    for f in FILES:
        if not f.is_file():
            sys.exit(f"missing {f}")

    ver = TAG.removeprefix("client-")
    zip_path = REL / "AquaTechLauncher.zip"
    man = {
        "version": ver,
        "launcher_zip": "https://aquateche.store/dl/AquaTechLauncher.zip",
        "launcher_exe": "AquaTechLauncher.exe",
        "release_base": "https://aquateche.store/dl",
        "launcher_zip_md5": md5_file(zip_path),
        "launcher_zip_size": zip_path.stat().st_size,
        "pack_cdn": "https://aquateche.store/pack",
    }
    DOCS_MANIFEST.write_text(json.dumps(man, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    body = (
        f"## AquaTech Client {ver}\n\n"
        "- Bootstrap reads bootstrap.json via GitHub API (bypasses stale raw/CDN cache)\n"
    )

    payload = json.dumps(
        {
            "tag_name": TAG,
            "target_commitish": "main",
            "name": f"AquaTech Client {ver}",
            "body": body,
            "draft": True,
            "prerelease": False,
        }
    ).encode()
    _, rel = api(
        "POST",
        f"https://api.github.com/repos/{REPO}/releases",
        data=payload,
        content_type="application/json",
    )
    release_id = rel["id"]
    print("draft", release_id)

    for path in FILES:
        print(f"upload {path.name} ({path.stat().st_size / 1024 / 1024:.2f} MB)вЂ¦")
        api(
            "POST",
            f"https://uploads.github.com/repos/{REPO}/releases/{release_id}/assets?name={path.name}",
            data=path.read_bytes(),
            content_type="application/octet-stream",
        )

    _, published = api(
        "PATCH",
        f"https://api.github.com/repos/{REPO}/releases/{release_id}",
        data=json.dumps({"draft": False, "make_latest": "true"}).encode(),
        content_type="application/json",
    )
    print("published", published.get("html_url"))
    print(f"https://github.com/{REPO}/releases/download/{TAG}/AquaTech.exe")


if __name__ == "__main__":
    main()
