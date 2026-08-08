#!/usr/bin/env python3
"""Publish LoliLand-style client: AquaTech.exe + AquaTechLauncher.zip."""
from __future__ import annotations

import json
import sys
import urllib.error
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
REPO = "Renfild/AquaTeche"
TAG = "client-2.9.9"
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

    man = {
        "version": "2.9.9",
        "launcher_zip": f"https://github.com/{REPO}/releases/download/{TAG}/AquaTechLauncher.zip",
        "launcher_exe": "AquaTechLauncher.exe",
        "release_base": f"https://github.com/{REPO}/releases/download/{TAG}",
        "pack_cdn": "https://cdn.jsdelivr.net/gh/Renfild/AquaTeche@main/docs/pack",
    }
    DOCS_MANIFEST.write_text(json.dumps(man, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    body = (
        "## AquaTech Client 2.9.9\n\n"
        "- Fix: wait for sound assets before launch (no mute until restart)\n"
        "- Fix: force Russian language (ru_ru) in options.txt\n"
        "- AquaTech.exe bootstrap + pack CDN / pack-2.9.2\n"
        "- Server IP: katherine-hydro.tun.ply.gg:31279\n"
    )

    payload = json.dumps(
        {
            "tag_name": TAG,
            "target_commitish": "main",
            "name": "AquaTech Client 2.9.9",
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

