#!/usr/bin/env python3
"""Replace AquaTech.exe asset on an existing GitHub release tag."""
from __future__ import annotations

import json
import sys
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
REPO = "Renfild/AquaTeche"
TAG = sys.argv[1] if len(sys.argv) > 1 else "client-2.9.39"
EXE = ROOT / "dist" / "releases" / "AquaTech.exe"


def token() -> str:
    return (ROOT / ".gh_token").read_text(encoding="utf-8").strip()


def api(method: str, url: str, data: bytes | None = None, content_type: str | None = None):
    headers = {
        "Authorization": f"token {token()}",
        "Accept": "application/vnd.github+json",
        "User-Agent": "AquaTechUploader",
    }
    if content_type:
        headers["Content-Type"] = content_type
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    with urllib.request.urlopen(req, timeout=600) as r:
        body = r.read()
        if not body:
            return r.status, {}
        return r.status, json.loads(body)


def main() -> None:
    if not EXE.is_file():
        sys.exit(f"missing {EXE}")
    _, rel = api("GET", f"https://api.github.com/repos/{REPO}/releases/tags/{TAG}")
    rid = rel["id"]
    for a in rel.get("assets") or []:
        if a.get("name") == "AquaTech.exe":
            api("DELETE", f"https://api.github.com/repos/{REPO}/releases/assets/{a['id']}")
            print("deleted old AquaTech.exe")
    api(
        "POST",
        f"https://uploads.github.com/repos/{REPO}/releases/{rid}/assets?name=AquaTech.exe",
        data=EXE.read_bytes(),
        content_type="application/octet-stream",
    )
    print("uploaded", EXE.stat().st_size, "bytes to", TAG)


if __name__ == "__main__":
    main()
