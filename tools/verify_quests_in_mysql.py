import json
import sys
import pymysql
from pathlib import Path

sys.stdout.reconfigure(encoding="utf-8")

ROOT_DIR = Path(__file__).resolve().parent.parent
SECRETS_FILE = ROOT_DIR / ".apex_mysql.json"

def main():
    cfg = json.load(open(SECRETS_FILE, encoding="utf-8"))
    conn = pymysql.connect(
        host=cfg["host"],
        port=int(cfg.get("port", 3306)),
        user=cfg["username"],
        password=cfg["password"],
        database=cfg["database"],
        charset="utf8mb4"
    )
    with conn.cursor() as cur:
        cur.execute("SELECT snapshot_id, snapshot_name, total_files, total_chapters, created_at FROM aquatech_quests_snapshots ORDER BY created_at DESC LIMIT 5")
        print("Snapshots in DB:")
        for row in cur.fetchall():
            print(" ", row)
            
        cur.execute("SELECT file_path, chapter_id, chapter_title, quest_count, file_size, content_sha256 FROM aquatech_quests_backup ORDER BY id DESC LIMIT 14")
        print("\nBacked up files in last snapshot:")
        for row in cur.fetchall():
            print(f"  {row[0]:<30} | ID: {str(row[1]):<18} | Quests: {row[3]:<4} | Size: {row[4]:<6} | Title: {row[2]}")
            
    conn.close()

if __name__ == "__main__":
    main()
