#!/usr/bin/env python3
"""Smoke-check Apex after deploy: panel state, first-party jars, FAWE config hint.

Usage:
  python scripts/tasks/smoke_apex_server.py
"""
from __future__ import annotations

import json
import os
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "scripts" / "tasks"))

import deploy_apexnodes_sftp as deploy  # noqa: E402


def main() -> int:
    deploy.load_deploy_secrets()
    state = deploy.apex_server_state()
    print(f"panel: {state}")
    ok = state == "running"

    import paramiko

    if not deploy.PASSWORD:
        print("WARN: no SFTP pass — skip jar listing")
        return 0 if ok else 1

    t = paramiko.Transport((deploy.HOST, deploy.PORT))
    t.connect(username=deploy.USER, password=deploy.PASSWORD)
    s = paramiko.SFTPClient.from_transport(t)
    assert s is not None

    mods = sorted(n for n in s.listdir("mods") if n.endswith(".jar"))
    for prefix in ("aquatech_ui-", "casesmod-", "packetfixer-"):
        hits = [n for n in mods if n.startswith(prefix)]
        print(f"mods/{prefix}*: {hits}")
        if len(hits) != 1:
            print(f"  FAIL expected exactly 1 {prefix}*.jar")
            ok = False

    try:
        plugins = s.listdir("plugins")
        fawe = [n for n in plugins if n.startswith("FastAsyncWorldEdit") and n.endswith(".jar")]
        print(f"plugins/FAWE: {fawe}")
        # peek remote config if present
        try:
            with s.open("plugins/FastAsyncWorldEdit/config.yml", "r") as f:
                text = f.read().decode("utf-8", "replace")
            if "persistent-brushes: false" in text:
                print("FAWE persistent-brushes: false OK")
            elif "persistent-brushes: true" in text:
                print("FAIL FAWE persistent-brushes still true")
                ok = False
            else:
                print("WARN could not find persistent-brushes key")
        except OSError:
            print("WARN no remote FAWE config.yml")
    except OSError as ex:
        print(f"WARN plugins list: {ex}")

    s.close()
    t.close()
    print("OK" if ok else "FAIL")
    return 0 if ok else 1


if __name__ == "__main__":
    raise SystemExit(main())
