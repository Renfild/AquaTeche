#!/usr/bin/env python3
"""Deploy docs/ portal.

Pages (preferred): push to main — Cloudflare Pages reads wrangler.toml
Worker Builds fallback:
  npx wrangler deploy -c wrangler.worker.toml
"""
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
    mode = (sys.argv[1] if len(sys.argv) > 1 else "pages").lower()
    if mode == "worker":
        cmdline = "npx -y wrangler deploy -c wrangler.worker.toml"
    else:
        cmdline = (
            f'npx -y wrangler pages deploy "{DOCS}" '
            f"--project-name {PROJECT} --commit-dirty=true"
        )
    print("Deploying", mode, "->", PROJECT)
    print(" ", cmdline)
    res = subprocess.run(cmdline, cwd=str(ROOT), env=os.environ.copy(), shell=True)
    if res.returncode == 0:
        print("OK https://aquatech-7gs.pages.dev")
    return res.returncode


if __name__ == "__main__":
    raise SystemExit(main())
