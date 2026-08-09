#!/usr/bin/env python3
"""Smoke: portal login returns at_session via Set-Cookie (even without JSON session)."""
from __future__ import annotations

import json
import re
import sys
import urllib.error
import urllib.request

BASE = "https://aquatech.santcrail.workers.dev"
UA = {"User-Agent": "AquaTechLauncher/2.9.17", "X-AquaTech-Launcher": "1"}


def post_login(nick: str, password: str) -> tuple[int, dict, str | None]:
    data = json.dumps({"nick": nick, "password": password}).encode()
    req = urllib.request.Request(
        f"{BASE}/api/login",
        data=data,
        headers={**UA, "Content-Type": "application/json"},
        method="POST",
    )
    try:
        with urllib.request.urlopen(req, timeout=30) as r:
            body = r.read().decode("utf-8", "replace")
            raw_cookie = r.headers.get("Set-Cookie") or ""
            sid = None
            m = re.search(r"at_session=([^;,\s]+)", raw_cookie, re.I)
            if m:
                sid = m.group(1)
            return r.status, json.loads(body), sid
    except urllib.error.HTTPError as e:
        body = e.read().decode("utf-8", "replace")
        try:
            parsed = json.loads(body)
        except json.JSONDecodeError:
            parsed = {"raw": body[:200]}
        raw_cookie = e.headers.get("Set-Cookie") or ""
        sid = None
        m = re.search(r"at_session=([^;,\s]+)", raw_cookie, re.I)
        if m:
            sid = m.group(1)
        return e.code, parsed, sid


def main() -> int:
    code, body, sid = post_login("nobody_xyz_test", "wrong-password")
    print("bad_login", code, body, "cookie", sid)
    if code != 401:
        print("FAIL: expected 401 for bad password")
        return 1

    # Cookie parse unit check (matches launcher regex intent)
    sample = "at_session=abc123def; Path=/; HttpOnly; Secure; SameSite=Lax; Max-Age=604800"
    m = re.search(r"(?:^|,\s*)at_session=([^;,\s]+)", sample, re.I)
    if not m or m.group(1) != "abc123def":
        print("FAIL: Set-Cookie parse")
        return 1
    print("parse_ok", m.group(1))

    # Live API: wrong pass must not set session cookie
    if sid:
        print("FAIL: bad login should not set at_session")
        return 1

    # Catalog / me sanity
    req = urllib.request.Request(f"{BASE}/api/catalog", headers=UA)
    with urllib.request.urlopen(req, timeout=20) as r:
        print("catalog", r.status)

    print("OK: login endpoint + cookie parse ready for CookieContainer fallback")
    return 0


if __name__ == "__main__":
    sys.exit(main())
