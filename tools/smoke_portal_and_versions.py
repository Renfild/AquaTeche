#!/usr/bin/env python3
"""CI smoke: portal HTTP + bootstrap/site.js version alignment (+ live Worker drift)."""
from __future__ import annotations

import json
import os
import re
import sys
import urllib.error
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BASE = "https://aquatech.santcrail.workers.dev"
UA = {"User-Agent": "AquaTechSmoke/1.0"}


def get(url: str, timeout: int = 45) -> tuple[int, bytes]:
    req = urllib.request.Request(url, headers=UA)
    with urllib.request.urlopen(req, timeout=timeout) as r:
        return r.status, r.read()


def main() -> int:
    # Local / checked-in alignment
    boot = json.loads((ROOT / "docs" / "bootstrap.json").read_text(encoding="utf-8"))
    site = (ROOT / "docs" / "assets" / "js" / "site.js").read_text(encoding="utf-8")
    m = re.search(r"client-2\.9\.\d+", site)
    if not m:
        print("FAIL: no client version in docs/assets/js/site.js")
        return 1
    if boot.get("version") not in m.group(0):
        print(f"FAIL: bootstrap {boot.get('version')} != site.js {m.group(0)}")
        return 1
    print("OK local versions", boot.get("version"), m.group(0))

    # Live API
    st, _ = get(f"{BASE}/")
    print("home", st)
    if st != 200:
        print("FAIL home")
        return 1
    st, body = get(f"{BASE}/api/catalog")
    print("catalog", st, len(body))
    if st != 200:
        print("FAIL catalog")
        return 1

    data = json.dumps({"nick": "nobody_ci", "password": "wrong"}).encode()
    req = urllib.request.Request(
        f"{BASE}/api/login",
        data=data,
        headers={**UA, "Content-Type": "application/json", "X-AquaTech-Launcher": "1"},
        method="POST",
    )
    try:
        urllib.request.urlopen(req, timeout=30)
        print("FAIL login expected 401")
        return 1
    except urllib.error.HTTPError as e:
        print("login_bad", e.code)
        if e.code != 401:
            print("FAIL login status")
            return 1

    # ensure-nick disabled
    req2 = urllib.request.Request(
        f"{BASE}/api/launcher/ensure-nick",
        data=b'{"nick":"ci_test_nick"}',
        headers={**UA, "Content-Type": "application/json"},
        method="POST",
    )
    try:
        urllib.request.urlopen(req2, timeout=30)
        print("WARN ensure-nick still 200 (Worker not redeployed yet?)")
    except urllib.error.HTTPError as e:
        print("ensure-nick", e.code)
        if e.code not in (410, 404, 401, 403):
            print("FAIL unexpected ensure-nick", e.code)
            return 1

    # Live Worker download link vs bootstrap
    st, js = get(f"{BASE}/assets/js/site.js")
    live = re.search(r"client-2\.9\.\d+", js.decode("utf-8", "replace"))
    live_ver = live.group(0) if live else "none"
    expect = f"client-{boot['version']}"
    print("live_site_js", live_ver, "expect", expect)
    if live_ver != expect:
        print(
            "WARN: live Worker site.js is stale — run: python tools/deploy_to_cloudflare.py worker"
        )
        if os.environ.get("STRICT_LIVE_DEPLOY", "").strip() in ("1", "true", "yes"):
            print("FAIL: STRICT_LIVE_DEPLOY set")
            return 1

    print("OK smoke")
    return 0


if __name__ == "__main__":
    sys.exit(main())
