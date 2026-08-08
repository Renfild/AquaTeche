#!/usr/bin/env python3
"""Finish pack-2.9.2 draft: upload missing sanitized assets, then publish."""
from __future__ import annotations

import json
import time
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
REPO = "Renfild/AquaTeche"
RELEASE_ID = 367195122
PACK = ROOT / "dist" / "AquaTech-Client"
MANIFEST = PACK / "manifest.json"


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


def main() -> None:
    # refresh pack without disabled jars + sanitized names
    import subprocess, sys
    subprocess.check_call([sys.executable, str(ROOT / "tools" / "publish_client_pack.py")])
    man = json.loads(MANIFEST.read_text(encoding="utf-8"))
    rel = api("GET", f"https://api.github.com/repos/{REPO}/releases/{RELEASE_ID}")
    have = {a["name"] for a in rel.get("assets") or []}
    print("have", len(have), "manifest", len(man["files"]))

    upload_url = f"https://uploads.github.com/repos/{REPO}/releases/{RELEASE_ID}/assets"
    missing = []
    for item in man["files"]:
        aname = item["asset"]
        if aname in have:
            continue
        local = PACK / item["path"].replace("/", "\\")
        if not local.is_file():
            print("SKIP missing local", item["path"])
            continue
        missing.append((aname, local))

    print("to upload", len(missing))
    for aname, local in missing:
        print("UPLOAD", aname, f"({local.stat().st_size/1024/1024:.1f} MB)")
        data = local.read_bytes()
        q = urllib.parse.urlencode({"name": aname})
        for attempt in range(5):
            try:
                api(
                    "POST",
                    f"{upload_url}?{q}",
                    data=data,
                    content_type="application/octet-stream",
                )
                print("  OK", aname)
                break
            except urllib.error.HTTPError as e:
                err = e.read().decode("utf-8", "replace")
                print("  fail", e.code, err[:200])
                time.sleep(2 * (attempt + 1))
        else:
            raise SystemExit(f"could not upload {aname}")

    published = api(
        "PATCH",
        f"https://api.github.com/repos/{REPO}/releases/{RELEASE_ID}",
        data=json.dumps({"draft": False, "make_latest": False}).encode(),
        content_type="application/json",
    )
    print("published", published.get("html_url"))
    # verify a known jar
    sample = next(f for f in man["files"] if f["path"].endswith("aquatech_ui-1.0.0.jar"))
    print("sample url", sample["url"])


if __name__ == "__main__":
    main()
