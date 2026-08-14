"""Print Cloudflare NS for aquateche.store and check zone/domain status."""
from __future__ import annotations

import json
import os
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
ZONE = "aquateche.store"
ACCOUNT = "b855fc404ea390174024f1d249cb1964"


def token() -> str:
    t = (os.environ.get("CLOUDFLARE_API_TOKEN") or "").strip()
    if t:
        return t
    p = ROOT / ".cf_token"
    return p.read_text(encoding="utf-8").strip() if p.is_file() else ""


def api(url: str):
    req = urllib.request.Request(
        url,
        headers={"Authorization": f"Bearer {token()}", "Content-Type": "application/json"},
    )
    with urllib.request.urlopen(req, timeout=60) as r:
        return json.loads(r.read().decode())


def main() -> None:
    z = api(f"https://api.cloudflare.com/client/v4/zones?name={ZONE}")["result"][0]
    print("zone:", z["name"], "status:", z["status"])
    print("Cloudflare nameservers (set these at the registrar):")
    for ns in z.get("name_servers") or []:
        print(" ", ns)
    print("old NS:", ", ".join(z.get("original_name_servers") or []))
    d = api(f"https://api.cloudflare.com/client/v4/accounts/{ACCOUNT}/workers/domains")
    print("worker custom domains:")
    for row in d.get("result") or []:
        if row.get("zone_name") == ZONE:
            print(f"  {row['hostname']} -> {row['service']} enabled={row.get('enabled')}")


if __name__ == "__main__":
    main()
