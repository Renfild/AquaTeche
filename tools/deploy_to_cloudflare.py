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


def token() -> str:
    env_t = (os.environ.get("CLOUDFLARE_API_TOKEN") or "").strip()
    if env_t:
        return env_t
    for p in (ROOT / ".cf_token", ROOT / ".cloudflare_token"):
        if p.is_file():
            return p.read_text(encoding="utf-8").strip()
    return ""


def main() -> int:
    if not DOCS.is_dir():
        print("missing docs/", file=sys.stderr)
        return 1
    t = token()
    if not t or t.startswith("TESTING") or len(t) < 30:
        print(
            "ERROR: need a real Cloudflare API token in .cf_token or CLOUDFLARE_API_TOKEN "
            "(current token missing/fake). Create one: Workers Edit permission.",
            file=sys.stderr,
        )
        return 2
    env = os.environ.copy()
    env["CLOUDFLARE_API_TOKEN"] = t
    print("[Cloudflare Deploy] Using API Token")

    mode = (sys.argv[1] if len(sys.argv) > 1 else "pages").lower()
    wrangler_js = ROOT / "tools" / "tools_npm" / "node_modules" / "wrangler" / "bin" / "wrangler.js"
    if wrangler_js.is_file():
        cmdline = f'node "{wrangler_js}"'
    else:
        npx = r"C:\PROGRA~1\nodejs\npx.cmd" if os.name == "nt" else "npx"
        cmdline = f'"{npx}" --yes wrangler@latest'

    if mode == "worker":
        cmd = f'{cmdline} deploy -c wrangler.worker.toml'
    else:
        cmd = f'{cmdline} pages deploy "{DOCS}" --project-name {PROJECT} --commit-dirty=true'

    print("Deploying", mode, "->", PROJECT)
    print(" ", cmd)
    res = subprocess.run(cmd, cwd=str(ROOT), env=env, shell=True)
    if res.returncode == 0:
        if mode == "worker":
            print("OK https://aquatech.santcrail.workers.dev")
        else:
            print("OK https://aquatech-7gs.pages.dev")
    return res.returncode


if __name__ == "__main__":
    raise SystemExit(main())
