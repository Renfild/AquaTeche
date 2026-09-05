#!/usr/bin/env python3
"""
Save current FTB Quests to ApexNodes MySQL database (s34318_aquatech).
Creates tables `aquatech_quests_snapshots` and `aquatech_quests_backup` if needed,
and archives all chapter SNBT files, chapter_groups.snbt, and data.snbt.
"""

import hashlib
import json
import os
import re
import sys
import time
from pathlib import Path
import pymysql

ROOT_DIR = Path(__file__).resolve().parent.parent
CONFIG_DIR = ROOT_DIR / "config" / "ftbquests" / "quests"
SECRETS_FILE = ROOT_DIR / ".apex_mysql.json"

def load_mysql_config():
    if not SECRETS_FILE.exists():
        raise FileNotFoundError(f"Missing {SECRETS_FILE}")
    with open(SECRETS_FILE, "r", encoding="utf-8") as f:
        return json.load(f)

sys.stdout.reconfigure(encoding="utf-8")

def parse_chapter_meta(snbt_text: str):
    # Root chapter ID
    id_match = re.search(r'^\s*id:\s*"([A-Za-z0-9_]+)"', snbt_text, re.MULTILINE)
    chapter_id = id_match.group(1) if id_match else None
    
    # Root group
    group_match = re.search(r'^\s*group:\s*"([^"]*)"', snbt_text, re.MULTILINE)
    group = group_match.group(1) if group_match else None
    
    # Root title: single tab indentation, usually at the end of the file
    root_titles = re.findall(r'^\ttitle:\s*"([^"]+)"', snbt_text, re.MULTILINE)
    if root_titles:
        title = root_titles[-1]
    else:
        all_titles = re.findall(r'title:\s*"([^"]+)"', snbt_text)
        title = all_titles[-1] if all_titles else None
    
    # Count quests: each quest block has tasks: [
    quest_count = len(re.findall(r'tasks:\s*\[', snbt_text))
    
    return chapter_id, title, group, quest_count

def main():
    if not CONFIG_DIR.exists():
        sys.exit(f"Quests dir not found: {CONFIG_DIR}")
        
    cfg = load_mysql_config()
    print(f"Connecting to MySQL: {cfg['host']}:{cfg.get('port', 3306)} / {cfg['database']}...")
    
    conn = pymysql.connect(
        host=cfg["host"],
        port=int(cfg.get("port", 3306)),
        user=cfg["username"],
        password=cfg["password"],
        database=cfg["database"],
        charset="utf8mb4",
        connect_timeout=15,
        autocommit=False
    )
    
    timestamp_str = time.strftime("%Y%m%d_%H%M%S")
    snapshot_id = f"snapshot_{timestamp_str}"
    snapshot_name = f"AquaTech FTB Quests Live Backup {time.strftime('%Y-%m-%d %H:%M:%S')}"
    
    try:
        with conn.cursor() as cur:
            # 1. Create tables if not exist
            cur.execute("""
                CREATE TABLE IF NOT EXISTS aquatech_quests_snapshots (
                    snapshot_id VARCHAR(64) PRIMARY KEY,
                    snapshot_name VARCHAR(128) NOT NULL,
                    total_files INT NOT NULL,
                    total_chapters INT NOT NULL,
                    total_quests INT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    notes TEXT
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """)
            
            cur.execute("""
                CREATE TABLE IF NOT EXISTS aquatech_quests_backup (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    snapshot_id VARCHAR(64) NOT NULL,
                    file_path VARCHAR(255) NOT NULL,
                    file_type VARCHAR(32) NOT NULL,
                    chapter_id VARCHAR(64),
                    chapter_title VARCHAR(255),
                    chapter_group VARCHAR(64),
                    quest_count INT DEFAULT 0,
                    file_size INT NOT NULL,
                    content_sha256 VARCHAR(64) NOT NULL,
                    content_snbt MEDIUMTEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_snapshot (snapshot_id),
                    INDEX idx_filepath (file_path),
                    INDEX idx_chapter (chapter_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """)
            
            # 2. Collect all files
            files_to_save = []
            
            # Root files
            data_file = CONFIG_DIR / "data.snbt"
            if data_file.exists():
                files_to_save.append((data_file, "data", "data.snbt"))
            groups_file = CONFIG_DIR / "chapter_groups.snbt"
            if groups_file.exists():
                files_to_save.append((groups_file, "groups", "chapter_groups.snbt"))
                
            # Chapters
            chapters_dir = CONFIG_DIR / "chapters"
            for chap in sorted(chapters_dir.glob("*.snbt")):
                files_to_save.append((chap, "chapter", f"chapters/{chap.name}"))
                
            print(f"Found {len(files_to_save)} quest files to backup.")
            
            total_chapters = 0
            total_quests = 0
            
            for file_path, file_type, rel_name in files_to_save:
                content = file_path.read_text(encoding="utf-8")
                size = len(content.encode("utf-8"))
                sha256 = hashlib.sha256(content.encode("utf-8")).hexdigest()
                
                chap_id = None
                chap_title = None
                chap_group = None
                q_count = 0
                
                if file_type == "chapter":
                    chap_id, chap_title, chap_group, q_count = parse_chapter_meta(content)
                    total_chapters += 1
                    total_quests += q_count
                    print(f"  + [{rel_name}] ID: {chap_id} | Title: '{chap_title}' | Quests: {q_count} ({size} B)")
                else:
                    print(f"  + [{rel_name}] Type: {file_type} ({size} B)")
                    
                cur.execute("""
                    INSERT INTO aquatech_quests_backup 
                    (snapshot_id, file_path, file_type, chapter_id, chapter_title, chapter_group, quest_count, file_size, content_sha256, content_snbt)
                    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                """, (snapshot_id, rel_name, file_type, chap_id, chap_title, chap_group, q_count, size, sha256, content))
            
            # 3. Insert snapshot record
            cur.execute("""
                INSERT INTO aquatech_quests_snapshots 
                (snapshot_id, snapshot_name, total_files, total_chapters, total_quests, notes)
                VALUES (%s, %s, %s, %s, %s, %s)
            """, (snapshot_id, snapshot_name, len(files_to_save), total_chapters, total_quests, f"Saved from {CONFIG_DIR}"))
            
            conn.commit()
            print(f"\nSUCCESS: Snapshot {snapshot_id} saved to MySQL!")
            print(f"  Total files: {len(files_to_save)}")
            print(f"  Total chapters: {total_chapters}")
            print(f"  Total quests: {total_quests}")
            
            # Verify from database
            cur.execute("SELECT COUNT(*) FROM aquatech_quests_backup WHERE snapshot_id = %s", (snapshot_id,))
            saved_count = cur.fetchone()[0]
            print(f"  Verified rows in aquatech_quests_backup: {saved_count}")
            
    except Exception as e:
        conn.rollback()
        print(f"ERROR saving quests to MySQL: {e}", file=sys.stderr)
        raise
    finally:
        conn.close()

if __name__ == "__main__":
    main()
