#!/usr/bin/env python3
"""Resume pack upload for the draft/latest release matching PACK_TAG from publish_client_pack."""
from __future__ import annotations

import json
import time
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
REPO = "Renfild/AquaTeche"
PACK = ROOT / "dist" / "AquaTech-Client"
MANIFEST = PACK / "manifest.json"

# Keep in sync with publish_client_pack.PACK_TAG
PACK_TAG = "pack-2.9.5"


def token() -> str:
    return (ROOT / ".gh_token").read_text(encoding="utf-8").strip()


def api(method, url, data=None, content_type=None, timeout=900):
    headers = {
        "Authorization": f"token {token()}",
        "Accept": "application/vnd.github+json",
        "User-Agent": "AquaTechPackUploader",
    }
    if content_type:
        headers["Content-Type"] = content_type
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    with urllib.request.urlopen(req, timeout=timeout) as r:
        body = r.read()
        return json.loads(body) if body else {}


def find_release():
    rels = api("GET", f"https://api.github.com/repos/{REPO}/releases?per_page=20")
    for r in rels:
        if r.get("tag_name") == PACK_TAG:
            return r
    raise SystemExit(f"no release {PACK_TAG} — run upload_pack_release.py first")


def main() -> None:
    import subprocess
    import sys

    subprocess.check_call([sys.executable, str(ROOT / "tools" / "publish_client_pack.py")])
    man = json.loads(MANIFEST.read_text(encoding="utf-8"))
    rel = find_release()
    rid = rel["id"]
    have = {a["name"] for a in rel.get("assets") or []}
    print("release", PACK_TAG, "id", rid, "have", len(have), "manifest", len(man["files"]))

    upload_url = f"https://uploads.github.com/repos/{REPO}/releases/{rid}/assets"
    missing = []
    for item in man["files"]:
        aname = item["asset"]
        if aname in have:
            continue
        missing.append(item)

    print("missing", len(missing))
    for item in missing:
        aname = item["asset"]
        path = PACK / item["path"]
        if not path.is_file():
            print("SKIP missing file", path)
            continue
        data = path.read_bytes()
        q = urllib.parse.urlencode({"name": aname})
        for attempt in range(3):
            try:
                api(
                    "POST",
                    f"{upload_url}?{q}",
                    data=data,
                    content_type="application/octet-stream",
                )
                print("uploaded", aname)
                break
            except Exception as e:
                print("retry", aname, e)
                time.sleep(2)
        else:
            raise SystemExit(f"failed {aname}")

    if rel.get("draft"):
        api(
            "PATCH",
            f"https://api.github.com/repos/{REPO}/releases/{rid}",
            data=json.dumps({"draft": False}).encode(),
            content_type="application/json",
        )
        print("published", PACK_TAG)


if __name__ == "__main__":
    main()
