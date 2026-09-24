#!/usr/bin/env python3
"""Dump AquaTech MariaDB tables to backups/db-<ts>.sql + .json (read-only).

Usage: python scripts/tasks/backup_db.py
Credentials come from .apex_mysql.json (never printed).
"""
from __future__ import annotations

import json
import sys
from datetime import datetime
from pathlib import Path

import pymysql

ROOT = Path(__file__).resolve().parents[2]
SECRETS = ROOT / ".apex_mysql.json"
OUT_DIR = ROOT / "backups"
TABLES = ("aquatech_meta", "aquatech_player_stats", "aquatech_player_link")


def sql_literal(value) -> str:
    if value is None:
        return "NULL"
    if isinstance(value, (int, float)):
        return str(value)
    if isinstance(value, (bytes, bytearray)):
        return "0x" + bytes(value).hex()
    return "'" + str(value).replace("\\", "\\\\").replace("'", "''") + "'"


def main() -> int:
    if not SECRETS.is_file():
        print("missing .apex_mysql.json", file=sys.stderr)
        return 2
    cfg = json.loads(SECRETS.read_text(encoding="utf-8"))
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    stamp = datetime.now().strftime("%Y%m%d-%H%M%S")

    conn = pymysql.connect(
        host=cfg["host"],
        port=int(cfg["port"]),
        user=cfg["username"],
        password=cfg["password"],
        database=cfg["database"],
        charset="utf8mb4",
        connect_timeout=15,
        read_timeout=60,
    )
    dump: list[str] = [
        f"-- AquaTech DB dump {stamp}",
        f"-- database: {cfg['database']} @ {cfg['host']}:{cfg['port']}",
        "SET NAMES utf8mb4;",
        "",
    ]
    snapshot: dict[str, list] = {}
    try:
        with conn.cursor() as cur:
            cur.execute("SHOW TABLES")
            present = {r[0] for r in cur.fetchall()}
            for table in TABLES:
                if table not in present:
                    print(f"  {table}: absent, skipped")
                    continue
                cur.execute(f"SELECT * FROM `{table}`")
                rows = cur.fetchall()
                cols = [d[0] for d in cur.description]
                snapshot[table] = [dict(zip(cols, row)) for row in rows]
                dump.append(f"DROP TABLE IF EXISTS `{table}`;")
                dump.append(f"CREATE TABLE `{table}` (")
                cur.execute(f"SHOW CREATE TABLE `{table}`")
                ddl = cur.fetchone()[1]
                dump.append(ddl.replace("\\n", "\n") + ";")
                for row in rows:
                    values = ", ".join(sql_literal(v) for v in row)
                    dump.append(f"INSERT INTO `{table}` VALUES ({values});")
                dump.append("")
                print(f"  {table}: {len(rows)} rows")
    finally:
        conn.close()

    sql_path = OUT_DIR / f"db-{stamp}.sql"
    json_path = OUT_DIR / f"db-{stamp}.json"
    sql_path.write_text("\n".join(dump), encoding="utf-8")
    json_path.write_text(json.dumps(snapshot, ensure_ascii=False, indent=2, default=str), encoding="utf-8")
    print(f"OK sql={sql_path.name} ({sql_path.stat().st_size} B)")
    print(f"OK json={json_path.name} ({json_path.stat().st_size} B)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
