#!/usr/bin/env python3
"""Upload only config/ftbquests/* from client pack into existing pack-2.9.2 release."""
from __future__ import annotations

import json
import sys
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
REPO = "Renfild/AquaTeche"
TAG = "pack-2.9.3"
PACK = ROOT / "dist" / "AquaTech-Client"
MANIFEST = PACK / "manifest.json"


def token() -> str:
    t = (ROOT / ".gh_token").read_text(encoding="utf-8").strip()
    if not t:
        sys.exit("missing .gh_token")
    return t


def api(method: str, url: str, data: bytes | None = None, content_type: str | None = None):
    headers = {
        "Authorization": f"token {token()}",
        "Accept": "application/vnd.github+json",
        "User-Agent": "AquaTechFtbUpload",
    }
    if content_type:
        headers["Content-Type"] = content_type
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=300) as r:
            body = r.read()
            return json.loads(body) if body else {}
    except urllib.error.HTTPError as e:
        err = e.read().decode("utf-8", "replace")
        raise SystemExit(f"HTTP {e.code} {url}: {err[:500]}") from e


def main() -> None:
    man = json.loads(MANIFEST.read_text(encoding="utf-8"))
    files = [
        f
        for f in man.get("files") or []
        if str(f.get("path", "")).replace("\\", "/").startswith("config/ftbquests/")
    ]
    if not files:
        sys.exit("no ftbquests in manifest")

    rel = api("GET", f"https://api.github.com/repos/{REPO}/releases/tags/{TAG}")
    release_id = rel["id"]
    existing = {a["name"]: a["id"] for a in rel.get("assets") or []}
    upload_url = f"https://uploads.github.com/repos/{REPO}/releases/{release_id}/assets"
    ok = fail = 0
    for item in files:
        aname = item["asset"]
        local = PACK / Path(item["path"])
        if not local.is_file():
            print("missing", item["path"])
            fail += 1
            continue
        if aname in existing:
            try:
                api("DELETE", f"https://api.github.com/repos/{REPO}/releases/assets/{existing[aname]}")
            except SystemExit as e:
                print("del warn", aname, e)
        data = local.read_bytes()
        q = urllib.parse.urlencode({"name": aname})
        try:
            api(
                "POST",
                f"{upload_url}?{q}",
                data=data,
                content_type="application/octet-stream",
            )
            ok += 1
            print("up", aname, len(data))
        except SystemExit as e:
            fail += 1
            print("fail", aname, e)
    print(f"done ok={ok} fail={fail}")
    if fail:
        sys.exit(2)


if __name__ == "__main__":
    main()
