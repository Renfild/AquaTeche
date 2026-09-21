#!/usr/bin/env python3
"""Robust single-file SFTP uploader for Apex (retries, no full mirror).

Usage:
  python scripts/tasks/apex_sftp_put.py mods server/mods/aqualumen-forge-1.20.1-0.3.55-alpha.jar [--delete old.jar]
"""
from __future__ import annotations

import argparse
import json
import os
import sys
import time
from pathlib import Path

import paramiko

ROOT = Path(__file__).resolve().parents[2]
SECRETS = ROOT / ".apex_deploy.json"


def load() -> dict:
    if SECRETS.is_file():
        return json.loads(SECRETS.read_text(encoding="utf-8"))
    return {}


def connect(secrets: dict, attempts: int = 5) -> paramiko.SFTPClient:
    host = os.environ.get("AQUATECH_SFTP_HOST") or secrets.get("sftp_host")
    port = int(os.environ.get("AQUATECH_SFTP_PORT") or secrets.get("sftp_port") or 2022)
    user = os.environ.get("AQUATECH_SFTP_USER") or secrets.get("sftp_user")
    password = os.environ.get("AQUATECH_SFTP_PASS") or secrets.get("sftp_pass")
    if not host or not user or not password:
        sys.exit("missing sftp_host/user/pass (env or .apex_deploy.json)")

    last = None
    for attempt in range(1, attempts + 1):
        try:
            transport = paramiko.Transport((host, port))
            transport.banner_timeout = 30
            transport.auth_timeout = 30
            transport.connect(username=user, password=password)
            return paramiko.SFTPClient.from_transport(transport)
        except Exception as ex:  # noqa: BLE001 - retry any transport/auth error
            last = ex
            wait = min(30, 4 * attempt)
            print(f"  sftp attempt {attempt}/{attempts} failed: {ex} (retry in {wait}s)", flush=True)
            time.sleep(wait)
    raise SystemExit(f"SFTP connect failed after {attempts} attempts: {last}")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("remote_dir")
    parser.add_argument("local_file", nargs="?")
    parser.add_argument("--delete", action="append", default=[])
    args = parser.parse_args()

    secrets = load()
    sftp = connect(secrets)
    try:
        remote_dir = "/" + args.remote_dir.strip("/")
        if args.local_file:
            local = Path(args.local_file)
            if not local.is_file():
                sys.exit(f"missing local file: {local}")
            target = f"{remote_dir}/{local.name}"
            sftp.put(str(local), target)
            print(f"OK uploaded {local.name} -> {remote_dir} ({local.stat().st_size} bytes)")
        for name in args.delete:
            remote = f"{remote_dir}/{name}"
            try:
                sftp.remove(remote)
                print(f"OK deleted {remote}")
            except FileNotFoundError:
                print(f"SKIP not found {remote}")
    finally:
        sftp.close()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
