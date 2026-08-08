#!/usr/bin/env python3
"""Upload AquaTechLauncher.exe: draft → upload → publish (immutable releases)."""
from __future__ import annotations

import json
import sys
import urllib.error
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
REPO = "Renfild/AquaTeche"
TAG = "launcher-2.9.1"
EXE = ROOT / "dist" / "releases" / "AquaTechLauncher.exe"
if not EXE.is_file():
    EXE = ROOT / "dist" / "AquaTechLauncher.exe"


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
    if not EXE.is_file():
        sys.exit(f"missing {EXE}")

    body = (
        "## Скачать лаунчер AquaTech\n\n"
        "Скачай **AquaTechLauncher.exe** и запусти — как у LoliLand, один файл.\n"
    )

    payload = json.dumps(
        {
            "tag_name": TAG,
            "target_commitish": "main",
            "name": "AquaTech Launcher 2.9.1",
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
    print("draft", rel["id"])
    release_id = rel["id"]

    name = "AquaTechLauncher.exe"
    print(f"upload {name} ({EXE.stat().st_size / 1024 / 1024:.1f} MB)…")
    data = EXE.read_bytes()
    _, uploaded = api(
        "POST",
        f"https://uploads.github.com/repos/{REPO}/releases/{release_id}/assets?name={name}",
        data=data,
        content_type="application/octet-stream",
    )
    print("uploaded", uploaded.get("browser_download_url"))

    _, published = api(
        "PATCH",
        f"https://api.github.com/repos/{REPO}/releases/{release_id}",
        data=json.dumps(
            {
                "draft": False,
                "make_latest": "true",
                "name": "AquaTech Launcher 2.9.1",
                "body": body,
            }
        ).encode(),
        content_type="application/json",
    )
    print("published", published.get("html_url"))
    print(f"https://github.com/{REPO}/releases/download/{TAG}/{name}")


if __name__ == "__main__":
    main()
