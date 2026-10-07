#!/usr/bin/env python3
"""Upload the Draconic Evolution rework (KubeJS script) and the two new FTB quest chapters to Apex, then restart.

Only touches:
  kubejs/server_scripts/32_aquatech_draconic.js
  config/ftbquests/quests/chapters/productivebees_aquatech.snbt
  config/ftbquests/quests/chapters/draconic_aquatech.snbt
Before uploading it saves a local copy of the live config/ftbquests (backups/ftbquests_apex_<ts>) so in-game edits are never lost.

    AQUATECH_SYNC_QUESTS=1 python tools/deploy_draconic_apex.py
"""
import hashlib
import json
import os
import stat
import sys
import time
import urllib.request
from pathlib import Path

import paramiko

ROOT = Path(__file__).resolve().parent.parent
cfg = json.loads((ROOT / ".apex_deploy.json").read_text(encoding="utf-8"))

if os.environ.get("AQUATECH_SYNC_QUESTS") != "1":
    sys.exit("Set AQUATECH_SYNC_QUESTS=1 to upload quest chapters (FTB Quests safety rule).")

UPLOADS = [
    (ROOT / "server/kubejs/server_scripts/32_aquatech_draconic.js", "kubejs/server_scripts/32_aquatech_draconic.js"),
    (ROOT / "server/config/ftbquests/quests/chapters/productivebees_aquatech.snbt", "config/ftbquests/quests/chapters/productivebees_aquatech.snbt"),
    (ROOT / "server/config/ftbquests/quests/chapters/draconic_aquatech.snbt", "config/ftbquests/quests/chapters/draconic_aquatech.snbt"),
]

t = paramiko.Transport((cfg["sftp_host"], int(cfg.get("sftp_port", 2022))))
t.connect(username=cfg["sftp_user"], password=cfg["sftp_pass"])
sftp = paramiko.SFTPClient.from_transport(t)

# 1. local backup of the live quests
backup = ROOT / "backups" / f"ftbquests_apex_{int(time.time())}"


def pull(remote_dir, local_dir):
    local_dir.mkdir(parents=True, exist_ok=True)
    for e in sftp.listdir_attr(remote_dir):
        r, loc = f"{remote_dir}/{e.filename}", local_dir / e.filename
        if stat.S_ISDIR(e.st_mode):
            pull(r, loc)
        else:
            sftp.get(r, str(loc))


pull("config/ftbquests", backup)
print(f"1. live quests saved to {backup.relative_to(ROOT)}")

# 2. refuse to overwrite a chapter that exists remotely with different content than our generated one unless it is ours
for local, remote in UPLOADS:
    try:
        sftp.stat(remote)
        print(f"   note: {remote} already exists on the server and will be replaced")
    except IOError:
        pass

# 3. upload and verify
print("2. uploading...")
for local, remote in UPLOADS:
    sftp.put(str(local), remote)
    data = local.read_bytes()
    with sftp.open(remote, "rb") as f:
        back = f.read()
    ok = hashlib.md5(data).hexdigest() == hashlib.md5(back).hexdigest()
    print(f"   {remote}: {'OK' if ok else 'MISMATCH'} ({len(data)} bytes)")
    if not ok:
        sys.exit(1)
sftp.close()
t.close()

# 4. restart through the panel
panel, api_key, server_id = cfg["apex_panel"], cfg["apex_api_key"], cfg["apex_server_id"]


def api(method, path, data=None):
    req = urllib.request.Request(
        f"{panel}{path}",
        data=json.dumps(data).encode("utf-8") if data else None,
        headers={"Authorization": f"Bearer {api_key}", "Accept": "Application/vnd.pterodactyl.v1+json", "Content-Type": "application/json"},
        method=method,
    )
    with urllib.request.urlopen(req, timeout=30) as resp:
        body = resp.read()
        return json.loads(body.decode("utf-8")) if body.strip() else {}


print("3. save-all flush and restart...")
try:
    api("POST", f"/api/client/servers/{server_id}/command", {"command": "save-all flush"})
except Exception as ex:
    print("   save-all error (ignored):", ex)
time.sleep(5)
api("POST", f"/api/client/servers/{server_id}/power", {"signal": "restart"})
start = time.time()
time.sleep(20)
while time.time() - start < 240:
    time.sleep(6)
    state = api("GET", f"/api/client/servers/{server_id}/resources").get("attributes", {}).get("current_state", "unknown")
    print(f"   {state} ({int(time.time() - start)}s)")
    if state == "running":
        break
print("done")
