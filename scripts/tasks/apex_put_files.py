#!/usr/bin/env python3
"""Upload/delete files on the Apex panel via the Pterodactyl files API.

Fallback for when SFTP auth/connection is flaky.

Usage:
  python scripts/tasks/apex_put_files.py upload mods server/mods/aqualumen-forge-1.20.1-0.3.55-alpha.jar
  python scripts/tasks/apex_put_files.py delete mods aqualumen-forge-1.20.1-0.3.53-alpha.jar
"""
from __future__ import annotations

import json
import os
import sys
import urllib.error
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SECRETS = ROOT / ".apex_deploy.json"

PANEL = os.environ.get("AQUATECH_APEX_PANEL", "https://panel.apexnodes.xyz").rstrip("/")
SERVER_ID = os.environ.get("AQUATECH_APEX_SERVER_ID", "")
API_KEY = os.environ.get("AQUATECH_APEX_API_KEY", "")


def load_secrets() -> None:
    global PANEL, SERVER_ID, API_KEY
    if SECRETS.is_file():
        data = json.loads(SECRETS.read_text(encoding="utf-8"))
        PANEL = str(data.get("apex_panel") or PANEL).rstrip("/")
        SERVER_ID = str(data.get("apex_server_id") or SERVER_ID)
        API_KEY = str(data.get("apex_api_key") or API_KEY)
    if not SERVER_ID or not API_KEY:
        sys.exit("missing apex_server_id / apex_api_key (env or .apex_deploy.json)")


def api(method: str, path: str, data: bytes | None = None, content_type: str | None = None) -> tuple[int, bytes]:
    headers = {
        "Authorization": f"Bearer {API_KEY}",
        "Accept": "application/json",
        "User-Agent": "AquaTechPanelUploader",
    }
    if content_type:
        headers["Content-Type"] = content_type
    req = urllib.request.Request(f"{PANEL}{path}", data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=600) as resp:
            return resp.status, resp.read()
    except urllib.error.HTTPError as e:
        body = e.read()
        print(f"HTTP {e.code} {path}: {body[:400].decode('utf-8', 'replace')}", file=sys.stderr)
        return e.code, body


def upload(remote_dir: str, local_path: Path) -> int:
    directory = "/" + remote_dir.strip("/")
    target = local_path.name
    boundary = "----AquaTechPanelUploadBoundary7f3c9a"
    body = b"".join([
        f"--{boundary}\r\n".encode(),
        f'Content-Disposition: form-data; name="file"; filename="{target}"\r\n'.encode(),
        b"Content-Type: application/octet-stream\r\n\r\n",
        local_path.read_bytes(),
        f"\r\n--{boundary}--\r\n".encode(),
    ])
    status, resp = api("POST", f"/api/client/servers/{SERVER_ID}/files/write?directory={directory}",
                      data=body, content_type=f"multipart/form-data; boundary={boundary}")
    if 200 <= status < 300:
        print(f"OK uploaded {target} -> {directory} ({local_path.stat().st_size} bytes)")
        return 0
    if resp:
        print(resp[:200].decode("utf-8", "replace"), file=sys.stderr)
    return 1


def delete(remote_dir: str, names: list[str]) -> int:
    root = "/" + remote_dir.strip("/")
    payload = json.dumps({"root": root, "files": names}).encode()
    status, _ = api("POST", f"/api/client/servers/{SERVER_ID}/files/delete",
                    data=payload, content_type="application/json")
    if 200 <= status < 300:
        print(f"OK deleted from {root}: {', '.join(names)}")
        return 0
    return 1


def pull(remote_dir: str, url: str) -> int:
    """Ask the panel to download a file by URL into remote_dir (bypasses body limits)."""
    directory = "/" + remote_dir.strip("/")
    payload = json.dumps({"url": url, "directory": directory}).encode()
    status, _ = api("POST", f"/api/client/servers/{SERVER_ID}/files/pull",
                    data=payload, content_type="application/json")
    if 200 <= status < 300:
        print(f"OK panel pulls {url} -> {directory}")
        return 0
    return 1


def main() -> int:
    load_secrets()
    if len(sys.argv) < 4:
        print(__doc__)
        return 2
    action, remote_dir = sys.argv[1], sys.argv[2]
    if action == "upload":
        rc = 0
        for arg in sys.argv[3:]:
            p = Path(arg)
            if not p.is_file():
                print("missing local file:", p, file=sys.stderr)
                rc = 1
                continue
            rc |= upload(remote_dir, p)
        return rc
    if action == "delete":
        return delete(remote_dir, sys.argv[3:])
    if action == "pull":
        rc = 0
        for url in sys.argv[3:]:
            rc |= pull(remote_dir, url)
        return rc
    print("unknown action:", action)
    return 2


if __name__ == "__main__":
    raise SystemExit(main())
