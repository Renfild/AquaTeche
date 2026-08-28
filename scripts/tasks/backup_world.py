#!/usr/bin/env python3
"""Snapshot live Apex world + FTB quests into backups/ (gitignored).

Usage:
  python scripts/tasks/backup_world.py              # save-all, panel backup, local quests+progress
  python scripts/tasks/backup_world.py --full-regions  # also SFTP every .mca (slow)

Keeps the last KEEP zips per prefix. Panel hosts often have backup_limit=1
(rotates the previous panel snapshot). Local zips are the durable copy.
"""
from __future__ import annotations

import argparse
import stat
import sys
import time
import zipfile
from datetime import datetime
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(Path(__file__).resolve().parent))
import deploy_apexnodes_sftp as deploy  # noqa: E402

VAULT = ROOT / "backups"
KEEP = 7
QUEST_DIRS = (
    "config/ftbquests",
    "config/ftbteams",
    "world/ftbquests",
    "world/ftbteams",
)
PROGRESS_DIRS = (
    "world/playerdata",
    "world/advancements",
    "world/stats",
    "world/data",
    "world/entities",
    "world/poi",
)
PROGRESS_FILES = (
    "world/level.dat",
    "world/level.dat_old",
    "world/session.lock",
    "world/uid.dat",
    "world/servername.txt",
)
REGION_DIRS = (
    "world/region",
    "world/DIM-1/region",
    "world/DIM1/region",
)


def sftp_listdir(sftp, remote: str) -> list:
    try:
        return sftp.listdir_attr(remote)
    except FileNotFoundError:
        return []
    except OSError:
        return []


def sftp_walk_files(sftp, remote: str, arc_prefix: str) -> list[tuple[str, str]]:
    out: list[tuple[str, str]] = []
    for attr in sftp_listdir(sftp, remote):
        name = attr.filename
        if name in (".", ".."):
            continue
        rpath = f"{remote}/{name}"
        arc = f"{arc_prefix}/{name}"
        mode = attr.st_mode or 0
        if stat.S_ISDIR(mode):
            out.extend(sftp_walk_files(sftp, rpath, arc))
        else:
            out.append((arc, rpath))
    return out


def collect_named(sftp, remotes: tuple[str, ...], files: tuple[str, ...] = ()) -> list[tuple[str, str]]:
    found: list[tuple[str, str]] = []
    for remote in remotes:
        found.extend(sftp_walk_files(sftp, remote, remote))
    for extra in files:
        try:
            sftp.stat(extra)
            found.append((extra, extra))
        except OSError:
            pass
    return found


def write_zip(sftp, dest: Path, pairs: list[tuple[str, str]], label: str) -> tuple[int, int]:
    if not pairs:
        print(f"WARN {label}: nothing to zip", flush=True)
        return 0, 0
    total = 0
    ok = 0
    dest.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(dest, "w", zipfile.ZIP_DEFLATED, compresslevel=6) as zf:
        for i, (arc, remote) in enumerate(pairs):
            try:
                with sftp.open(remote, "r") as fh:
                    data = fh.read()
                zf.writestr(arc, data)
                total += len(data)
                ok += 1
                if i % 40 == 0:
                    print(f"  [{i + 1}/{len(pairs)}] {arc}", flush=True)
            except Exception as exc:
                print(f"  skip {remote}: {str(exc)[:80]}", flush=True)
    print(f"OK {label} -> {dest.name} ({ok} files, {total / 1e6:.1f} MB raw)", flush=True)
    return ok, total


def prune(prefix: str) -> None:
    zips = sorted(VAULT.glob(f"{prefix}_*.zip"))
    for old in zips[:-KEEP]:
        old.unlink()
        print("pruned:", old.name, flush=True)


def main() -> int:
    parser = argparse.ArgumentParser(description="Backup live Apex world + FTB quests")
    parser.add_argument(
        "--full-regions",
        action="store_true",
        help="Also SFTP overworld/nether/end region .mca files (slow)",
    )
    parser.add_argument(
        "--skip-panel",
        action="store_true",
        help="Do not create an Apex panel backup",
    )
    parser.add_argument(
        "--backup-wait",
        type=int,
        default=420,
        help="Seconds to wait for panel backup (default 420)",
    )
    args = parser.parse_args()

    import paramiko

    deploy.load_deploy_secrets()
    if not deploy.PASSWORD:
        print("Need sftp_pass in .apex_deploy.json", file=sys.stderr)
        return 1

    stamp = datetime.now().strftime("%Y%m%d_%H%M")
    VAULT.mkdir(exist_ok=True)

    print("Flushing world (save-all flush)...", flush=True)
    try:
        deploy.apex_command("save-all flush")
        time.sleep(8)
    except SystemExit as exc:
        print("save-all flush failed (continuing):", str(exc)[:80], flush=True)
    except Exception as exc:
        print("save-all flush failed (continuing):", str(exc)[:80], flush=True)

    if not args.skip_panel:
        name = f"live-{stamp}-world-quests"
        print(f"Apex panel backup {name!r}...", flush=True)
        try:
            uuid = deploy.apex_create_backup(
                name, wait_sec=max(0, args.backup_wait), require=False
            )
            print(f"panel backup uuid={uuid or '?'}", flush=True)
        except SystemExit as exc:
            print("panel backup failed (continuing with SFTP):", str(exc)[:200], flush=True)

    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(
        deploy.HOST,
        port=int(deploy.PORT),
        username=deploy.USER,
        password=deploy.PASSWORD,
    )
    sftp = ssh.open_sftp()

    quest_pairs = collect_named(sftp, QUEST_DIRS)
    quest_zip = VAULT / f"ftbquests_{stamp}.zip"
    write_zip(sftp, quest_zip, quest_pairs, "quests")
    prune("ftbquests")

    # Unpacked copy so a restore is a folder copy, not unzip-first.
    unpacked = VAULT / f"ftbquests_{stamp}"
    if quest_pairs:
        for arc, remote in quest_pairs:
            dest = unpacked / arc
            dest.parent.mkdir(parents=True, exist_ok=True)
            try:
                sftp.get(remote, str(dest))
            except Exception as exc:
                print(f"  unpack skip {remote}: {str(exc)[:80]}", flush=True)
        print(f"OK unpacked quests -> {unpacked}", flush=True)

    progress_pairs = collect_named(sftp, PROGRESS_DIRS, PROGRESS_FILES)
    if args.full_regions:
        progress_pairs.extend(collect_named(sftp, REGION_DIRS))
        dims = sftp_walk_files(sftp, "world/dimensions", "world/dimensions")
        progress_pairs.extend(
            p for p in dims if p[0].endswith(".mca") or p[0].endswith(".dat")
        )
    else:
        print("skip region/*.mca (use --full-regions); panel backup holds chunks", flush=True)

    world_zip = VAULT / f"world_{stamp}.zip"
    write_zip(sftp, world_zip, progress_pairs, "world")
    prune("world")
    ssh.close()

    import r2_put

    for archive in (quest_zip, world_zip):
        if archive.is_file():
            r2_put.put(archive, f"{stamp}/{archive.name}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
