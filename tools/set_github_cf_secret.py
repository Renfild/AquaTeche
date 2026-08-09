#!/usr/bin/env python3
"""One-time: push local Cloudflare API token into GitHub Actions secret.

Reads .cf_token / .cloudflare_token / CLOUDFLARE_API_TOKEN, then:
  gh secret set CLOUDFLARE_API_TOKEN

After this, push to main (docs/worker/functions) auto-deploys the Worker.
"""
from __future__ import annotations

import os
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def load_token() -> str:
    env_t = (os.environ.get("CLOUDFLARE_API_TOKEN") or "").strip()
    if env_t:
        return env_t
    for name in (".cf_token", ".cloudflare_token"):
        p = ROOT / name
        if p.is_file():
            return p.read_text(encoding="utf-8").strip()
    return ""


def main() -> int:
    t = load_token()
    if not t or t.startswith("TESTING") or len(t) < 30:
        print(
            "Need a real Cloudflare API token first.\n"
            "1. https://dash.cloudflare.com/profile/api-tokens → Create Token\n"
            "   Template: Edit Cloudflare Workers (or custom: Account Workers Scripts Edit + D1 Edit)\n"
            "2. Save it as .cf_token in the repo root (gitignored)\n"
            "3. Re-run: python tools/set_github_cf_secret.py",
            file=sys.stderr,
        )
        return 2

    env = os.environ.copy()
    gh_tok = ROOT / ".gh_token"
    if gh_tok.is_file() and not env.get("GH_TOKEN"):
        env["GH_TOKEN"] = gh_tok.read_text(encoding="utf-8").strip()

    proc = subprocess.run(
        ["gh", "secret", "set", "CLOUDFLARE_API_TOKEN", "--body", t],
        cwd=str(ROOT),
        env=env,
        capture_output=True,
        text=True,
    )
    if proc.returncode != 0:
        print(proc.stdout, end="")
        print(proc.stderr, file=sys.stderr, end="")
        return proc.returncode

    print("OK: CLOUDFLARE_API_TOKEN set for Renfild/AquaTeche")
    print("Next: push to main (or Actions → Deploy Worker → Run workflow)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
