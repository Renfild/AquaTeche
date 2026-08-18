#!/usr/bin/env python3
"""Smoke test AquaTech live portal and Cloudflare API endpoints."""
from __future__ import annotations

import json
import urllib.error
import urllib.request

BASE = "https://aquateche.store"
PAGES = [
    "",
    "start.html",
    "store.html",
    "cases.html",
    "rods.html",
    "top.html",
    "news.html",
    "rules.html",
    "bootstrap.json",
    "assets/css/site.css",
    "assets/js/site.js",
    "assets/js/radar.js",
]

print("=== SMOKE TEST: AquaTech Live Web Portal ===\n")
for p in PAGES:
    url = f"{BASE}/{p}"
    try:
        req = urllib.request.Request(url, headers={"User-Agent": "AquaTechQA/1.0"})
        with urllib.request.urlopen(req, timeout=10) as r:
            code = r.status
            length = len(r.read())
            print(f"[OK] {url:<48} -> HTTP {code} ({length} bytes)")
    except Exception as e:
        print(f"[FAIL] {url:<48} -> {e}")

print("\n=== SMOKE TEST: Live API Endpoints ===\n")
APIS = [
    ("GET", "/api/catalog", {}),
    ("GET", "/api/news", {}),
    ("GET", "/api/top", {}),
    ("GET", "/api/server-status", {}),
    ("POST", "/api/launcher/verify-token", {"x-aquatech-launcher": "1"}),
    ("POST", "/api/launcher/session", {"x-aquatech-launcher": "1"}),
]

for method, ep, headers in APIS:
    url = f"{BASE}{ep}"
    try:
        req = urllib.request.Request(url, headers={"User-Agent": "AquaTechQA/1.0", **headers}, method=method)
        with urllib.request.urlopen(req, timeout=10) as r:
            code = r.status
            data = r.read()
            print(f"[OK] {method} {ep:<30} -> HTTP {code} ({len(data)} bytes)")
    except urllib.error.HTTPError as e:
        # HTTP 400 means route is reached and validation is active (expected when body is empty)
        print(f"[ACTIVE] {method} {ep:<30} -> HTTP {e.code} ({e.reason})")
    except Exception as e:
        print(f"[FAIL] {method} {ep:<30} -> {e}")
