#!/usr/bin/env python3
"""Pull the AquaTech world from Apex via SFTP into a local dated zip vault.

Usage:
  python scripts/tasks/backup_world.py            # full world backup
Keeps the last KEEP zips in backups/ (gitignored). Run daily via cron/Task Scheduler.
"""
from __future__ import annotations

import json
import sys
import time
import zipfile
from datetime import datetime
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SECRETS = ROOT / ".apex_deploy.json"
VAULT = ROOT / "backups"
KEEP = 7
WORLD_SUBDIRS = ["region", "playerdata", "entities", "poi", "data"]
EXTRA_FILES = ["level.dat", "level.dat_old", "session.lock", "servername.txt"]
DIMENSIONS = ["DIM-1", "DIM1"]


def main() -> int:
    import paramiko

    d = json.loads(SECRETS.read_text(encoding="utf-8"))
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(d["sftp_host"], port=int(d.get("sftp_port", 2022)),
                username=d["sftp_user"], password=d["sftp_pass"])
    sftp = ssh.open_sftp()

    stamp = datetime.now().strftime("%Y%m%d_%H%M")
    out = VAULT / f"world_{stamp}.zip"
    VAULT.mkdir(exist_ok=True)

    def collect(world: str) -> list[tuple[str, bytes]]:
        """Save-all flushes to disk; we read files straight from SFTP."""
        files: list[tuple[str, bytes]] = []
        base = "/" + world
        for sub in WORLD_SUBDIRS:
            try:
                for f in sftp.listdir(f"{base}/{sub}"):
                    if f.endswith(".mca") or f.endswith(".dat"):
                        files.append((f"{world}/{sub}/{f}", f"{base}/{sub}/{f}"))
            except FileNotFoundError:
                pass
        for extra in EXTRA_FILES:
            try:
                sftp.stat(f"{base}/{extra}")
                files.append((f"{world}/{extra}", f"{base}/{extra}"))
            except Exception:
                pass
        for dim in DIMENSIONS:
            for sub in WORLD_SUBDIRS:
                try:
                    for f in sftp.listdir(f"{base}/{dim}/{sub}"):
                        if f.endswith(".mca") or f.endswith(".dat"):
                            files.append((f"{world}/{dim}/{sub}/{f}", f"{base}/{dim}/{sub}/{f}"))
                except Exception:
                    pass
        return files

    # Flush world to disk first so the copy is consistent.
    key = d.get("apex_api_key")
    panel = d.get("apex_panel")
    sid = d.get("apex_server_id")
    if key and panel and sid:
        import urllib.request
        req = urllib.request.Request(
            f"{panel}/api/client/servers/{sid}/command",
            data=json.dumps({"command": "save-all flush"}).encode("utf-8"),
            headers={"Authorization": f"Bearer {key}", "Accept": "application/json",
                     "Content-Type": "application/json"}, method="POST")
        try:
            urllib.request.urlopen(req, timeout=30).read()
            time.sleep(8)
        except Exception as e:
            print("save-all flush failed (continuing):", str(e)[:80])

    files = collect("world")
    if not files:
        print("no world files found via SFTP", file=sys.stderr)
        return 1

    total = 0
    with zipfile.ZipFile(out, "w", zipfile.ZIP_DEFLATED, compresslevel=6) as z:
        for i, (arc, remote) in enumerate(files):
            try:
                with sftp.open(remote, "r") as fh:
                    data = fh.read()
                z.writestr(arc, data)
                total += len(data)
                if i % 50 == 0:
                    print(f"[{i + 1}/{len(files)}] {arc}")
            except Exception as e:
                print("skip:", remote, str(e)[:60])
    ssh.close()

    mb = total / 1e6
    print(f"OK backup -> {out.name} ({len(files)} files, {mb:.1f} MB raw)")

    # prune old backups
    zips = sorted(VAULT.glob("world_*.zip"))
    for old in zips[:-KEEP]:
        old.unlink()
        print("pruned:", old.name)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
