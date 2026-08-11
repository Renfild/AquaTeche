#!/usr/bin/env python3
"""Send Apex console commands: WorldGuard -w helpers, Chunky spawn notes.

Requires apex_api_key in .apex_deploy.json (or AQUATECH_APEX_API_KEY).
Server must be running.

Examples:
  python scripts/tasks/apex_console_ops.py --cmd "list"
  python scripts/tasks/apex_console_ops.py --wg-flag other-explosion deny
  python scripts/tasks/apex_console_ops.py --wg-flag creeper-explosion deny --world world
  python scripts/tasks/apex_console_ops.py --chunky-spawn --dry-run
  python scripts/tasks/apex_console_ops.py --chunky-spawn --radius 400
"""
from __future__ import annotations

import argparse
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "scripts" / "tasks"))

import deploy_apexnodes_sftp as deploy  # noqa: E402

# Broken-chunk / lag: pregen spawn once after map change. Chunky must be installed.
CHUNKY_SPAWN_TEMPLATE = (
    "chunky world {world}",
    "chunky center spawn",
    "chunky radius {radius}",
    "chunky shape circle",
    "chunky start",
)


def wg_flag_cmd(world: str, region: str, flag: str, value: str) -> str:
    # WorldGuard: region flag -w <world> <region> <flag> <value>
    return f"region flag -w {world} {region} {flag} {value}"


def main() -> int:
    deploy.load_deploy_secrets()
    p = argparse.ArgumentParser(description="Apex console helpers (WG / Chunky)")
    p.add_argument("--cmd", action="append", default=[], help="Raw console command (repeatable)")
    p.add_argument("--world", default="world", help="World name for WG -w (default world)")
    p.add_argument("--region", default="__global__", help="WG region id (default __global__)")
    p.add_argument(
        "--wg-flag",
        nargs=2,
        metavar=("FLAG", "VALUE"),
        action="append",
        default=[],
        help="Set WG flag via region flag -w <world> ...",
    )
    p.add_argument(
        "--chunky-spawn",
        action="store_true",
        help="Queue Chunky circular pregen around spawn",
    )
    p.add_argument("--radius", type=int, default=500, help="Chunky radius blocks (default 500)")
    p.add_argument(
        "--dry-run",
        action="store_true",
        help="Print commands only, do not hit panel API",
    )
    args = p.parse_args()

    cmds: list[str] = []
    cmds.extend(args.cmd)
    for flag, value in args.wg_flag:
        cmds.append(wg_flag_cmd(args.world, args.region, flag, value))
    if args.chunky_spawn:
        cmds.extend(
            t.format(world=args.world, radius=args.radius) for t in CHUNKY_SPAWN_TEMPLATE
        )

    if not cmds:
        p.print_help()
        print(
            "\nNotes:\n"
            "  WG without -w often fails on Mohist when default world != current.\n"
            "  Always pass --world (or rely on default 'world').\n"
            "  Chunky: run once after world wipe; watch console for progress.\n"
            "  If chunks still warn, check FAWE/WE undo + region edits near spawn.\n",
            file=sys.stderr,
        )
        return 2

    state = "dry-run"
    if not args.dry_run:
        state = deploy.apex_server_state()
        if state != "running":
            print(f"FAIL panel state={state} (need running)", file=sys.stderr)
            return 1

    for cmd in cmds:
        if args.dry_run:
            print(f"DRY: {cmd}")
        else:
            deploy.apex_command(cmd)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
