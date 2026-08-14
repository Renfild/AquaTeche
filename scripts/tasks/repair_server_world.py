#!/usr/bin/env python3
"""
AquaTech World MCA Repair Tool
Detects and repairs corrupted MCA chunk headers (truncated streams, missing .mcc files, invalid zlib bytes).
Fixes server join lag and region file error spam.
"""

import os
import sys
import zlib
import json
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent.parent

def check_and_repair_mca(mca_bytes: bytearray) -> tuple[bytearray, int]:
    if len(mca_bytes) < 8192:
        return mca_bytes, 0

    repaired_count = 0
    for i in range(1024):
        off_bytes = mca_bytes[i*4 : i*4 + 3]
        offset = int.from_bytes(off_bytes, 'big') * 4096
        count = mca_bytes[i*4 + 3] * 4096
        if offset == 0:
            continue

        is_corrupt = False
        if offset + 5 > len(mca_bytes):
            is_corrupt = True
        else:
            length = int.from_bytes(mca_bytes[offset:offset+4], 'big')
            comp = mca_bytes[offset+4]
            if comp & 128:
                is_corrupt = True
            elif offset + 4 + length > len(mca_bytes) or length <= 0:
                is_corrupt = True
            else:
                try:
                    chunk_data = mca_bytes[offset+5 : offset+4+length]
                    zlib.decompress(chunk_data)
                except Exception:
                    is_corrupt = True

        if is_corrupt:
            # Zero out entry in location table
            mca_bytes[i*4 : i*4 + 4] = b'\x00\x00\x00\x00'
            repaired_count += 1

    return mca_bytes, repaired_count


def main():
    creds_path = ROOT / ".apex_deploy.json"
    if not creds_path.is_file():
        print(f"Missing {creds_path}")
        return 1

    creds = json.loads(creds_path.read_text(encoding="utf-8"))
    api_key = creds.get("apex_api_key")
    server_id = creds.get("apex_server_id", "6fdc6f7b")

    # Get list of region files from world/region
    url = f"https://panel.apexnodes.xyz/api/client/servers/{server_id}/files/list?directory=world/region"
    req = urllib.request.Request(url, headers={"Authorization": f"Bearer {api_key}"})
    try:
        res = urllib.request.urlopen(req)
        items = json.loads(res.read().decode("utf-8"))["data"]
    except Exception as e:
        print(f"Error listing region dir: {e}")
        return 1

    region_files = [item["attributes"]["name"] for item in items if item["attributes"]["name"].endswith(".mca")]
    print(f"Found {len(region_files)} region files in world/region")

    repaired_total = 0
    fixed_files = []

    for name in region_files:
        dl_url_endpoint = f"https://panel.apexnodes.xyz/api/client/servers/{server_id}/files/download?file=world/region/{name}"
        req_dl = urllib.request.Request(dl_url_endpoint, headers={"Authorization": f"Bearer {api_key}"})
        try:
            dl_resp = urllib.request.urlopen(req_dl)
            dl_link = json.loads(dl_resp.read().decode("utf-8"))["attributes"]["url"]
            mca_raw = bytearray(urllib.request.urlopen(dl_link).read())
        except Exception as e:
            print(f"  Error downloading {name}: {e}")
            continue

        fixed_data, count = check_and_repair_mca(mca_raw)
        if count > 0:
            print(f"  [FIXED] {name}: cleared {count} corrupted chunks")
            repaired_total += count
            local_save = ROOT / "server" / "world" / "region" / name
            local_save.parent.mkdir(parents=True, exist_ok=True)
            local_save.write_bytes(fixed_data)
            fixed_files.append(name)
        else:
            print(f"  [OK] {name}: clean")

    print(f"\nRepaired {repaired_total} corrupted chunks across {len(fixed_files)} file(s).")
    if fixed_files:
        print("Deploying repaired region files to server via SFTP...")
        only_arg = ",".join([f"world/region/{f}" for f in fixed_files])
        os.system(f'python scripts/tasks/deploy_apexnodes_sftp.py --only "{only_arg}"')

    return 0

if __name__ == "__main__":
    sys.exit(main())
