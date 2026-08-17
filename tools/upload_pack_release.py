#!/usr/bin/env python3
"""Upload dist/AquaTech-Client to GitHub Release pack-2.9.27 (draft -> upload -> publish)."""
from __future__ import annotations

import json
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
REPO = "Renfild/AquaTeche"
TAG = "pack-2.9.57"
PACK = ROOT / "dist" / "AquaTech-Client"
MANIFEST = PACK / "manifest.json"
WORKERS = 4


def token() -> str:
    t = (ROOT / ".gh_token").read_text(encoding="utf-8").strip()
    if not t:
        sys.exit("missing .gh_token")
    return t


def api(method: str, url: str, data: bytes | None = None, content_type: str | None = None, timeout: int = 600):
    headers = {
        "Authorization": f"token {token()}",
        "Accept": "application/vnd.github+json",
        "User-Agent": "AquaTechPackUploader",
    }
    if content_type:
        headers["Content-Type"] = content_type
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=timeout) as r:
            body = r.read()
            return r.status, json.loads(body) if body else {}
    except urllib.error.HTTPError as e:
        err = e.read().decode("utf-8", "replace")
        raise RuntimeError(f"HTTP {e.code} {url}: {err[:500]}") from e


def main() -> None:
    if not MANIFEST.is_file():
        sys.exit("run publish_client_pack.py first")
    man = json.loads(MANIFEST.read_text(encoding="utf-8"))
    files = man.get("files") or []
    if not files:
        sys.exit("empty manifest")

    body = (
        "## AquaTech Client Pack\n\n"
        "Modpack files for the AquaTech launcher CDN.\n"
        "Manifest: https://aquatech-7gs.pages.dev/pack/manifest.json\n"
    )
    payload = json.dumps(
        {
            "tag_name": TAG,
            "target_commitish": "main",
            "name": f"AquaTech Pack {TAG}",
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
    print(f"draft release id={release_id} tag={TAG} files={len(files)}")

    upload_url = f"https://uploads.github.com/repos/{REPO}/releases/{release_id}/assets"

    def upload_one(item: dict) -> tuple[str, bool, str]:
        rel_path = item["path"]
        aname = item.get("asset") or Path(rel_path).name
        local = PACK / rel_path.replace("/", "\\")
        if not local.is_file():
            return aname, False, "missing local"
        data = local.read_bytes()
        q = urllib.parse.urlencode({"name": aname})
        for attempt in range(4):
            try:
                api(
                    "POST",
                    f"{upload_url}?{q}",
                    data=data,
                    content_type="application/octet-stream",
                    timeout=900,
                )
                return aname, True, "ok"
            except Exception as e:
                if attempt == 3:
                    return aname, False, str(e)[:160]
                time.sleep(1.5 * (attempt + 1))
        return aname, False, "failed"

    ok = fail = 0
    with ThreadPoolExecutor(max_workers=WORKERS) as pool:
        futs = [pool.submit(upload_one, item) for item in files]
        done = 0
        for fut in as_completed(futs):
            name, success, msg = fut.result()
            done += 1
            if success:
                ok += 1
            else:
                fail += 1
                print(f"FAIL {name}: {msg}")
            if done % 25 == 0 or done == len(files):
                print(f"  progress {done}/{len(files)} ok={ok} fail={fail}")

    print(f"upload done ok={ok} fail={fail}")
    if fail:
        print("WARNING: some uploads failed вЂ” fix and re-run against a NEW tag")
        # still publish so partial is usable? better leave draft
        sys.exit(2)

    _, published = api(
        "PATCH",
        f"https://api.github.com/repos/{REPO}/releases/{release_id}",
        data=json.dumps({"draft": False, "make_latest": "false"}).encode(),
        content_type="application/json",
    )
    print("published", published.get("html_url"))


if __name__ == "__main__":
    main()
