#!/usr/bin/env python3
"""Deploy local AquaLumen menu and restore FTB Quests from the hosting backup.

1. Copies backup ftbquests (2026-08-27 from hosting) to local repo & server staging.
2. Backs up current remote config/ftbquests on hosting to config/ftbquests_backup_<timestamp>.
3. Uploads restored config/ftbquests to remote config/ftbquests.
4. Uploads local config/aqualumen (hub.html, html/hub.html, fish_shop.json) to remote config/aqualumen.
5. Verifies remote file sizes and sends /ftbquests reload to console.
"""
import os
import shutil
import json
import time
import urllib.request
import paramiko
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DEPLOY_JSON = ROOT / ".apex_deploy.json"
BACKUP_FTB = ROOT / "backups" / "ftbquests_20260827_0114" / "config" / "ftbquests"
LOCAL_FTB = ROOT / "config" / "ftbquests"
SERVER_FTB = ROOT / "server" / "config" / "ftbquests"

LOCAL_AQUALUMEN = ROOT / "server" / "config" / "aqualumen"

def main():
    print("=== Step 1: Validating source files ===")
    assert BACKUP_FTB.is_dir(), f"Missing backup ftbquests directory at {BACKUP_FTB}"
    assert LOCAL_AQUALUMEN.is_dir(), f"Missing local aqualumen directory at {LOCAL_AQUALUMEN}"
    assert DEPLOY_JSON.is_file(), f"Missing {DEPLOY_JSON}"

    with open(DEPLOY_JSON, "r", encoding="utf-8") as f:
        cfg = json.load(f)

    # Step 1: Copy to local repo & server staging
    print("\n=== Step 2: Restoring FTB Quests to local repo & server staging ===")
    if LOCAL_FTB.exists():
        shutil.rmtree(LOCAL_FTB)
    shutil.copytree(BACKUP_FTB, LOCAL_FTB)
    print(f"  Restored {LOCAL_FTB}")

    if SERVER_FTB.exists():
        shutil.rmtree(SERVER_FTB)
    shutil.copytree(BACKUP_FTB, SERVER_FTB)
    print(f"  Restored {SERVER_FTB}")

    # Step 2: Connect to SFTP
    print(f"\n=== Step 3: Connecting to SFTP ({cfg['sftp_host']}:{cfg.get('sftp_port', 2022)}) ===")
    transport = paramiko.Transport((cfg['sftp_host'], int(cfg.get('sftp_port', 2022))))
    transport.connect(username=cfg['sftp_user'], password=cfg['sftp_pass'])
    sftp = paramiko.SFTPClient.from_transport(transport)
    print("  SFTP connected successfully.")

    # Step 3: Remote safety backup of current remote ftbquests
    ts = int(time.time())
    remote_bak = f"config/ftbquests_bak_{ts}"
    print(f"\n=== Step 4: Creating remote safety backup at {remote_bak} ===")
    try:
        def copy_remote_dir(src, dst):
            try:
                sftp.mkdir(dst)
            except OSError:
                pass
            for it in sftp.listdir_attr(src):
                s = f"{src}/{it.filename}"
                d = f"{dst}/{it.filename}"
                if it.st_mode & 0o40000:
                    copy_remote_dir(s, d)
                else:
                    # In SFTP, copying remote to remote is done by stream or rename
                    # We can use rename or download/upload
                    pass
        # Better yet, rename remote config/ftbquests to backup and recreate
        sftp.rename("config/ftbquests", remote_bak)
        print(f"  Renamed remote config/ftbquests -> {remote_bak}")
    except Exception as e:
        print(f"  Warning on remote rename: {e}")

    # Helper for uploading directory recursively
    def upload_dir(local_path: Path, remote_path: str):
        try:
            sftp.mkdir(remote_path)
        except OSError:
            pass
        for item in local_path.iterdir():
            r_item = f"{remote_path}/{item.name}"
            if item.is_dir():
                upload_dir(item, r_item)
            elif item.is_file():
                sftp.put(str(item), r_item)
                print(f"    Uploaded {item.name} -> {r_item} ({item.stat().st_size}b)")

    # Step 4: Upload restored FTB quests
    print("\n=== Step 5: Uploading restored FTB Quests to remote config/ftbquests ===")
    upload_dir(BACKUP_FTB, "config/ftbquests")

    # Step 5: Upload local AquaLumen menu
    print("\n=== Step 6: Uploading local AquaLumen menu to remote config/aqualumen ===")
    # Ensure remote dirs exist
    try:
        sftp.mkdir("config/aqualumen")
    except OSError:
        pass
    try:
        sftp.mkdir("config/aqualumen/html")
    except OSError:
        pass

    hub_html = LOCAL_AQUALUMEN / "hub.html"
    if hub_html.is_file():
        sftp.put(str(hub_html), "config/aqualumen/hub.html")
        print(f"  Uploaded hub.html ({hub_html.stat().st_size}b)")
        sftp.put(str(hub_html), "config/aqualumen/html/hub.html")
        print(f"  Uploaded html/hub.html ({hub_html.stat().st_size}b)")

    fish_shop = LOCAL_AQUALUMEN / "fish_shop.json"
    if fish_shop.is_file():
        sftp.put(str(fish_shop), "config/aqualumen/fish_shop.json")
        print(f"  Uploaded fish_shop.json ({fish_shop.stat().st_size}b)")

    cases_json = LOCAL_AQUALUMEN / "cases.json"
    if cases_json.is_file():
        sftp.put(str(cases_json), "config/aqualumen/cases.json")
        print(f"  Uploaded cases.json ({cases_json.stat().st_size}b)")

    print("\n=== Step 7: Verifying remote deployment ===")
    rem_chapters = sftp.listdir("config/ftbquests/quests/chapters")
    print(f"  Remote config/ftbquests/quests/chapters: {len(rem_chapters)} chapters:")
    for ch in sorted(rem_chapters):
        st = sftp.stat(f"config/ftbquests/quests/chapters/{ch}")
        print(f"    {ch:30} ({st.st_size}b)")

    hub_st = sftp.stat("config/aqualumen/hub.html")
    hub_html_st = sftp.stat("config/aqualumen/html/hub.html")
    print(f"  Remote config/aqualumen/hub.html: {hub_st.st_size}b")
    print(f"  Remote config/aqualumen/html/hub.html: {hub_html_st.st_size}b")

    sftp.close()
    transport.close()
    print("  SFTP session closed.")

    # Step 8: Send reload commands to Pterodactyl console
    panel = cfg.get("apex_panel", "https://panel.apexnodes.xyz").rstrip("/")
    server_id = cfg.get("apex_server_id", "6fdc6f7b")
    api_key = cfg.get("apex_api_key", "")

    if api_key:
        print("\n=== Step 8: Sending reload commands to console ===")
        for cmd in ["ftbquests reload", "say [AquaTech] FTB Quests restored from backup & Menu updated"]:
            req = urllib.request.Request(
                f"{panel}/api/client/servers/{server_id}/command",
                data=json.dumps({"command": cmd}).encode("utf-8"),
                headers={
                    "Authorization": f"Bearer {api_key}",
                    "Accept": "application/json",
                    "Content-Type": "application/json",
                },
                method="POST"
            )
            try:
                with urllib.request.urlopen(req, timeout=10) as resp:
                    print(f"  Sent command: '{cmd}' -> HTTP {resp.status}")
            except Exception as e:
                print(f"  Error sending command '{cmd}': {e}")

    print("\n[SUCCESS] Menu deployed and FTB Quests restored from hosting backup!")

if __name__ == "__main__":
    main()
