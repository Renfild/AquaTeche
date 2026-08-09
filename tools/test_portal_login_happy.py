#!/usr/bin/env python3
"""Happy-path portal login: needs AQUATECH_TEST_NICK + AQUATECH_TEST_PASS."""
from __future__ import annotations

import json
import os
import re
import sys
import urllib.error
import urllib.request

BASE = "https://aquatech.santcrail.workers.dev"
UA = {"User-Agent": "AquaTechLauncher/2.9.20", "X-AquaTech-Launcher": "1"}


def main() -> int:
    nick = (os.environ.get("AQUATECH_TEST_NICK") or "").strip()
    password = (os.environ.get("AQUATECH_TEST_PASS") or "").strip()
    if not nick or not password:
        print("SKIP: set AQUATECH_TEST_NICK / AQUATECH_TEST_PASS for happy-path login")
        return 0

    data = json.dumps({"nick": nick, "password": password}).encode()
    req = urllib.request.Request(
        f"{BASE}/api/login",
        data=data,
        headers={**UA, "Content-Type": "application/json"},
        method="POST",
    )
    try:
        with urllib.request.urlopen(req, timeout=30) as r:
            body = json.loads(r.read().decode())
            cookie = r.headers.get("Set-Cookie") or ""
    except urllib.error.HTTPError as e:
        print("FAIL login", e.code, e.read()[:200])
        return 1

    sid = body.get("session")
    if not sid:
        m = re.search(r"at_session=([^;,\s]+)", cookie, re.I)
        sid = m.group(1) if m else None
    if not sid:
        print("FAIL no session in JSON or Set-Cookie", body)
        return 1
    print("login ok session", sid[:8] + "…")

    req2 = urllib.request.Request(
        f"{BASE}/api/me",
        headers={**UA, "Cookie": f"at_session={sid}"},
    )
    with urllib.request.urlopen(req2, timeout=30) as r:
        me = json.loads(r.read().decode())
    got = (me.get("user") or {}).get("nick")
    if not got:
        print("FAIL /api/me", me)
        return 1
    print("me ok", got)
    return 0


if __name__ == "__main__":
    sys.exit(main())
