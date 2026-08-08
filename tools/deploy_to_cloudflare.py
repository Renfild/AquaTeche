#!/usr/bin/env python3
"""Deploy docs/ portal to Cloudflare Pages project aquatech (aquatech-7gs.pages.dev)."""
from __future__ import annotations

import os
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DOCS = ROOT / "docs"
PROJECT = "aquatech"


def main() -> int:
    if not DOCS.is_dir():
        print("missing docs/", file=sys.stderr)
        return 1
    env = os.environ.copy()
    # Windows: run through cmd so npx.cmd resolves
    cmdline = (
        f'npx -y wrangler pages deploy "{DOCS}" '
        f'--project-name {PROJECT} --commit-dirty=true'
    )
    print("Deploying", DOCS, "->", PROJECT)
    print(" ", cmdline)
    res = subprocess.run(cmdline, cwd=str(ROOT), env=env, shell=True)
    if res.returncode == 0:
        print("OK https://aquatech-7gs.pages.dev")
    return res.returncode


if __name__ == "__main__":
    raise SystemExit(main())
