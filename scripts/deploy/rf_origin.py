#!/usr/bin/env python3
"""Install Caddy reverse-proxy on a Russian VPS.

Env:
  RF_SSH_HOST   e.g. 185.x.x.x
  RF_SSH_USER   default root
  RF_SITE_HOST  public hostname, e.g. ru.aquateche.store
"""
from __future__ import annotations

import os
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent.parent
CADDY = ROOT / "scripts" / "deploy" / "rf-origin" / "Caddyfile"


def main() -> int:
    host = (os.environ.get("RF_SSH_HOST") or "").strip()
    if not host:
        print("RF_SSH_HOST not set — skip VPS origin. Site stays on aquateche.store.")
        return 0
    user = (os.environ.get("RF_SSH_USER") or "root").strip()
    site = (os.environ.get("RF_SITE_HOST") or "ru.aquateche.store").strip()
    dest = f"{user}@{host}"
    remote_caddy = "/etc/caddy/Caddyfile"
    subprocess.check_call(["scp", str(CADDY), f"{dest}:{remote_caddy}"])
    remote = f"export RF_SITE_HOST={site}; caddy reload --config {remote_caddy}"
    subprocess.check_call(["ssh", dest, remote])
    print("OK origin", site, "->", host)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
