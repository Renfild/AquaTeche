#!/usr/bin/env python3
"""
Restore FTB Quests from ApexNodes MySQL database snapshot.
Usage:
    python tools/restore_quests_from_mysql.py [snapshot_id]
If snapshot_id is omitted, the latest snapshot is used.
"""

import json
import sys
from pathlib import Path
import pymysql

sys.stdout.reconfigure(encoding="utf-8")

ROOT_DIR = Path(__file__).resolve().parent.parent
CONFIG_DIR = ROOT_DIR / "config" / "ftbquests" / "quests"
SECRETS_FILE = ROOT_DIR / ".apex_mysql.json"

def main():
    if not SECRETS_FILE.exists():
        sys.exit("Missing .apex_mysql.json")
        
    target_snapshot = sys.argv[1] if len(sys.argv) > 1 else None
    
    cfg = json.load(open(SECRETS_FILE, encoding="utf-8"))
    conn = pymysql.connect(
        host=cfg["host"],
        port=int(cfg.get("port", 3306)),
        user=cfg["username"],
        password=cfg["password"],
        database=cfg["database"],
        charset="utf8mb4"
    )
    
    try:
        with conn.cursor() as cur:
            if not target_snapshot:
                cur.execute("SELECT snapshot_id, snapshot_name, total_files, total_quests, created_at FROM aquatech_quests_snapshots ORDER BY created_at DESC LIMIT 1")
                row = cur.fetchone()
                if not row:
                    sys.exit("No quest snapshots found in database.")
                target_snapshot = row[0]
                print(f"Using latest snapshot: {target_snapshot} ({row[1]}, {row[3]} quests, created {row[4]})")
            else:
                print(f"Restoring requested snapshot: {target_snapshot}")
                
            cur.execute("""
                SELECT file_path, file_type, content_snbt 
                FROM aquatech_quests_backup 
                WHERE snapshot_id = %s
            """, (target_snapshot,))
            
            rows = cur.fetchall()
            if not rows:
                sys.exit(f"No files found for snapshot {target_snapshot}")
                
            CONFIG_DIR.mkdir(parents=True, exist_ok=True)
            (CONFIG_DIR / "chapters").mkdir(parents=True, exist_ok=True)
            
            restored = 0
            for file_path, file_type, content_snbt in rows:
                dest_file = CONFIG_DIR / file_path
                dest_file.parent.mkdir(parents=True, exist_ok=True)
                dest_file.write_text(content_snbt, encoding="utf-8")
                print(f"  Restored: {file_path} ({len(content_snbt)} chars)")
                restored += 1
                
            print(f"\nSUCCESS: Restored {restored} quest files from MySQL snapshot '{target_snapshot}'!")
    finally:
        conn.close()

if __name__ == "__main__":
    main()
