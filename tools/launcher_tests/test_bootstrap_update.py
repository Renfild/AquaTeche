#!/usr/bin/env python3
"""Tests for launcher auto-update via bootstrap.json.

Why updates fail for players (regression targets):
1. bootstrap.json on main still points at an old client-* tag
2. Release zip/md5 missing or mismatched
3. needs_launcher_update / NeedsUpdate logic wrong
4. Player runs AquaTechLauncher.exe directly — only AquaTech.exe applies updates

  python tools/launcher_tests/test_bootstrap_update.py
"""
from __future__ import annotations

import hashlib
import json
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))

import aquatech_launcher as L  # noqa: E402

BOOTSTRAP_URLS = [
    "https://api.github.com/repos/Renfild/AquaTeche/contents/docs/bootstrap.json?ref=main",
    "https://cdn.jsdelivr.net/gh/Renfild/AquaTeche@main/docs/bootstrap.json",
    "https://raw.githubusercontent.com/Renfild/AquaTeche/main/docs/bootstrap.json",
]
FAILED = 0


def _ok(name: str):
    print(f"  PASS  {name}")


def _fail(name: str, detail: str = ""):
    global FAILED
    FAILED += 1
    print(f"  FAIL  {name}" + (f" — {detail}" if detail else ""))


def _get_json(url: str, timeout: float = 20) -> dict:
    sep = "&" if "?" in url else "?"
    bust = f"{url}{sep}t={int(time.time())}"
    headers = {
        "User-Agent": "AquaTechBootstrapTest/1.0",
        "Cache-Control": "no-cache",
    }
    if "api.github.com" in url:
        headers["Accept"] = "application/vnd.github.raw"
    req = urllib.request.Request(bust, headers=headers)
    with urllib.request.urlopen(req, timeout=timeout) as r:
        return json.loads(r.read().decode("utf-8"))


def _head_ok(url: str, timeout: float = 30) -> tuple[bool, int]:
    req = urllib.request.Request(
        url,
        method="HEAD",
        headers={"User-Agent": "AquaTechBootstrapTest/1.0"},
    )
    try:
        with urllib.request.urlopen(req, timeout=timeout) as r:
            return True, r.status
    except urllib.error.HTTPError as ex:
        # Some CDNs reject HEAD — fall back to Range GET
        if ex.code in (403, 405):
            req2 = urllib.request.Request(
                url,
                headers={
                    "User-Agent": "AquaTechBootstrapTest/1.0",
                    "Range": "bytes=0-0",
                },
            )
            try:
                with urllib.request.urlopen(req2, timeout=timeout) as r:
                    return True, r.status
            except Exception as e2:
                return False, getattr(e2, "code", 0) or 0
        return False, ex.code
    except Exception:
        return False, 0


def test_needs_launcher_update_logic():
    print("[1] needs_launcher_update()")
    cases = [
        ("", "2.9.38", True),
        ("2.9.38", "2.9.38", False),
        ("2.9.37", "2.9.38", True),
        ("2.9.38", "2.9.37", False),
        ("2.9.38", "", False),
        ("2.9.9", "2.9.10", True),
    ]
    for local, remote, want in cases:
        got = L.needs_launcher_update(local, remote)
        if got != want:
            _fail(f"{local!r} -> {remote!r}", f"got {got} want {want}")
        else:
            _ok(f"{local or '(empty)'} -> {remote or '(empty)'} = {want}")


def test_local_bootstrap_matches_upload_script():
    print("[2] repo docs/bootstrap.json <-> upload_launcher_release TAG")
    man_path = ROOT / "docs" / "bootstrap.json"
    up_path = ROOT / "tools" / "upload_launcher_release.py"
    if not man_path.is_file():
        _fail("missing docs/bootstrap.json")
        return
    man = json.loads(man_path.read_text(encoding="utf-8"))
    text = up_path.read_text(encoding="utf-8")
    tag_line = next((ln for ln in text.splitlines() if ln.startswith("TAG = ")), "")
    tag = tag_line.split("=", 1)[1].strip().strip('"').strip("'")
    ver = man.get("version")
    if not ver:
        _fail("bootstrap missing version")
        return
    if f"client-{ver}" != tag:
        _fail("TAG mismatch", f"bootstrap={ver} TAG={tag}")
    else:
        _ok(f"version {ver} matches {tag}")
    zip_url = man.get("launcher_zip") or ""
    if ver not in zip_url:
        _fail("launcher_zip does not contain version", zip_url)
    else:
        _ok("launcher_zip embeds version")


def test_live_bootstrap_reachable():
    print("[3] live bootstrap.json (best of mirrors)")
    local = json.loads((ROOT / "docs" / "bootstrap.json").read_text(encoding="utf-8"))
    local_ver = (local.get("version") or "").strip()

    mirrors: list[tuple[str, dict]] = []
    for url in BOOTSTRAP_URLS:
        try:
            man = _get_json(url)
            mirrors.append((url, man))
            print(f"   mirror {url.split('/')[2]} -> {man.get('version')}")
        except Exception as ex:
            print(f"   mirror fail {url}: {ex}")

    if not mirrors:
        _fail("fetch bootstrap from all mirrors")
        return

    used, man = max(
        mirrors, key=lambda x: L.pack_version_key(str(x[1].get("version") or "0"))
    )
    _ok(f"best mirror={used.split('/')[2]} version={man.get('version')}")

    ver = (man.get("version") or "").strip()
    zip_url = (man.get("launcher_zip") or "").strip()
    if not ver or not zip_url:
        _fail("invalid live manifest", str(man)[:200])
        return

    if ver != local_ver:
        _fail("best live != local bootstrap", f"live={ver} local={local_ver}")
    else:
        _ok("best live matches local docs/bootstrap.json")

    ok, code = _head_ok(zip_url)
    if not ok:
        _fail("release zip HEAD/GET", f"HTTP {code} {zip_url}")
    else:
        _ok(f"release zip reachable ({code})")

    expect = int(man.get("launcher_zip_size") or 0)
    if expect > 0:
        req = urllib.request.Request(
            zip_url,
            headers={
                "User-Agent": "AquaTechBootstrapTest/1.0",
                "Range": "bytes=0-0",
            },
        )
        with urllib.request.urlopen(req, timeout=60) as r:
            cr = r.headers.get("Content-Range") or ""
            total = None
            if "/" in cr:
                try:
                    total = int(cr.rsplit("/", 1)[-1])
                except ValueError:
                    total = None
            if total is not None and total != expect:
                _fail("Content-Range total != launcher_zip_size", f"{total} != {expect}")
            else:
                _ok(f"zip size field={expect}" + (f" remote={total}" if total else ""))
            r.read(1)


def test_go_needs_update_via_subprocess():
    print("[4] go test ./bootstrap")
    go = ROOT / "tools" / "_go" / "bin" / "go.exe"
    if not go.is_file():
        # system go
        import shutil

        which = shutil.which("go")
        if not which:
            _fail("go binary missing")
            return
        go = Path(which)
    import subprocess

    r = subprocess.run(
        [str(go), "test", "."],
        cwd=str(ROOT / "bootstrap"),
        capture_output=True,
        text=True,
        timeout=120,
    )
    if r.returncode != 0:
        _fail("go test", (r.stdout + r.stderr)[-800:])
    else:
        _ok((r.stdout or "ok").strip().splitlines()[-1] if r.stdout else "ok")


def test_check_launcher_update_available_shape():
    print("[5] check_launcher_update_available() shape")
    info = L.check_launcher_update_available()
    for key in ("local", "remote", "update_available", "hint"):
        if key not in info:
            _fail(f"missing key {key}")
            return
    if info["local"] != L.LAUNCHER_VER:
        _fail("local version", f"{info['local']} ≠ {L.LAUNCHER_VER}")
    else:
        _ok(f"local={info['local']} remote={info['remote']} update={info['update_available']}")


def main() -> int:
    print("AquaTech bootstrap / launcher auto-update tests")
    test_needs_launcher_update_logic()
    test_local_bootstrap_matches_upload_script()
    test_live_bootstrap_reachable()
    test_go_needs_update_via_subprocess()
    test_check_launcher_update_available_shape()
    print()
    if FAILED:
        print(f"FAILED: {FAILED}")
        return 1
    print("ALL PASS")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
