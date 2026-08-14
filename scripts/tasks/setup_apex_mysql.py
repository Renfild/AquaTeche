#!/usr/bin/env python3
"""Create/wire ApexNodes MariaDB for game plugins + seed helper tables.

Secrets: .apex_mysql.json (gitignored) or env:
  AQUATECH_MYSQL_HOST / PORT / DATABASE / USER / PASSWORD
  AQUATECH_APEX_API_KEY  — create DB / rotate password / console / restart
  AQUATECH_SFTP_PASS     — upload plugin configs

Usage:
  python scripts/tasks/setup_apex_mysql.py
  python scripts/tasks/setup_apex_mysql.py --no-restart
  python scripts/tasks/setup_apex_mysql.py --rotate-password
"""
from __future__ import annotations

import argparse
import json
import os
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SECRETS = ROOT / ".apex_mysql.json"
SERVER = ROOT / "server"

APEX_PANEL = os.environ.get("AQUATECH_APEX_PANEL", "https://panel.apexnodes.xyz").rstrip("/")
APEX_SERVER_ID = os.environ.get("AQUATECH_APEX_SERVER_ID", "6fdc6f7b")
APEX_API_KEY = os.environ.get("AQUATECH_APEX_API_KEY", "")
DB_NAME = "aquatech"

SCHEMA_SQL = """
CREATE TABLE IF NOT EXISTS aquatech_meta (
  k VARCHAR(64) NOT NULL PRIMARY KEY,
  v TEXT NOT NULL,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS aquatech_player_link (
  uuid CHAR(36) NOT NULL PRIMARY KEY,
  nick VARCHAR(32) NOT NULL,
  portal_user_id INT NULL,
  coins BIGINT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_aquatech_player_nick (nick)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO aquatech_meta (k, v) VALUES
  ('pack', 'AquaTech'),
  ('schema_version', '1')
ON DUPLICATE KEY UPDATE v = VALUES(v);
"""


def _apex_headers() -> dict[str, str]:
    if not APEX_API_KEY:
        sys.exit("Set AQUATECH_APEX_API_KEY")
    return {
        "Authorization": f"Bearer {APEX_API_KEY}",
        "Accept": "Application/vnd.pterodactyl.v1+json",
        "Content-Type": "application/json",
    }


def apex_json(method: str, path: str, body: dict | None = None) -> dict:
    url = f"{APEX_PANEL}{path}"
    data = None if body is None else json.dumps(body).encode("utf-8")
    req = urllib.request.Request(url, data=data, headers=_apex_headers(), method=method)
    try:
        with urllib.request.urlopen(req, timeout=45) as resp:
            raw = resp.read()
    except urllib.error.HTTPError as ex:
        err = ex.read().decode("utf-8", "replace")
        raise SystemExit(f"Apex {method} {path} HTTP {ex.code}: {err[:500]}") from ex
    if not raw:
        return {}
    return json.loads(raw)


def write_plugin_configs(secrets: dict) -> list[Path]:
    """Write MySQL-ready configs with placeholders (secrets injected at SFTP stage)."""
    _ = secrets  # validated before call; values live in .apex_mysql.json
    written: list[Path] = []

    lp = SERVER / "plugins" / "LuckPerms" / "config.yml"
    lp.parent.mkdir(parents=True, exist_ok=True)
    lp.write_text(
        "\n".join(
            [
                "server: global",
                "storage-method: MySQL",
                "data:",
                "  address: __AQUATECH_MYSQL_HOST__:__AQUATECH_MYSQL_PORT__",
                "  database: __AQUATECH_MYSQL_DATABASE__",
                "  username: __AQUATECH_MYSQL_USER__",
                "  password: '__AQUATECH_MYSQL_PASSWORD__'",
                "  pool-size: 10",
                "split-storage:",
                "  enabled: false",
                "",
            ]
        ),
        encoding="utf-8",
    )
    written.append(lp)

    aj = SERVER / "plugins" / "ajLeaderboards" / "cache_storage.yml"
    if aj.is_file():
        text = aj.read_text(encoding="utf-8")
        lines = []
        for line in text.splitlines():
            if line.startswith("method:"):
                lines.append("method: mysql")
            elif line.startswith("ip:"):
                lines.append("ip: __AQUATECH_MYSQL_HOST__:__AQUATECH_MYSQL_PORT__")
            elif line.startswith("username:"):
                lines.append("username: __AQUATECH_MYSQL_USER__")
            elif line.startswith("password:"):
                lines.append("password: '__AQUATECH_MYSQL_PASSWORD__'")
            elif line.startswith("database:"):
                lines.append("database: __AQUATECH_MYSQL_DATABASE__")
            elif line.startswith("useSSL:"):
                lines.append("useSSL: false")
            elif line.startswith("allowPublicKeyRetrieval:"):
                lines.append("allowPublicKeyRetrieval: true")
            else:
                lines.append(line)
        aj.write_text("\n".join(lines) + "\n", encoding="utf-8")
        written.append(aj)

    sr = SERVER / "plugins" / "SkinsRestorer" / "config.yml"
    if sr.is_file():
        text = sr.read_text(encoding="utf-8")
        repls = [
            ("    type: FILE", "    type: MYSQL"),
            ("    host: localhost", "    host: __AQUATECH_MYSQL_HOST__"),
            ("    port: 3306", "    port: __AQUATECH_MYSQL_PORT__"),
            ("    database: db", "    database: __AQUATECH_MYSQL_DATABASE__"),
            ("    username: root", "    username: __AQUATECH_MYSQL_USER__"),
            ("    password: pass", "    password: __AQUATECH_MYSQL_PASSWORD__"),
            (
                "    connectionOptions: sslMode=trust&serverTimezone=UTC",
                "    connectionOptions: sslMode=disable&serverTimezone=UTC&allowPublicKeyRetrieval=true",
            ),
        ]
        for old, new in repls:
            if old in text:
                text = text.replace(old, new, 1)
        # Already-MYSQL placeholders refresh
        text = text.replace("host: g-pl-3.apexnodes.xyz", "host: __AQUATECH_MYSQL_HOST__", 1)
        sr.write_text(text, encoding="utf-8")
        written.append(sr)

    ds = SERVER / "plugins" / "DiscordSRV" / "config.yml"
    if ds.is_file():
        jdbc = (
            "jdbc:mysql://__AQUATECH_MYSQL_HOST__:__AQUATECH_MYSQL_PORT__/"
            "__AQUATECH_MYSQL_DATABASE__?autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true"
        )
        text = ds.read_text(encoding="utf-8")
        out = []
        for line in text.splitlines():
            if line.startswith("Experiment_JdbcAccountLinkBackend:"):
                out.append(f'Experiment_JdbcAccountLinkBackend: "{jdbc}"')
            elif line.startswith("Experiment_JdbcTablePrefix:"):
                out.append('Experiment_JdbcTablePrefix: "discordsrv"')
            elif line.startswith("Experiment_JdbcUsername:"):
                out.append('Experiment_JdbcUsername: "__AQUATECH_MYSQL_USER__"')
            elif line.startswith("Experiment_JdbcPassword:"):
                out.append('Experiment_JdbcPassword: "__AQUATECH_MYSQL_PASSWORD__"')
            else:
                out.append(line)
        ds.write_text("\n".join(out) + "\n", encoding="utf-8")
        written.append(ds)

    print("Wrote plugin configs (placeholders; secrets in .apex_mysql.json):", flush=True)
    for p in written:
        print(f"  {p.relative_to(ROOT)}", flush=True)
    return written


def yaml_quote(value: str) -> str:
    """Single-quote YAML scalar; escape embedded single quotes."""
    return "'" + value.replace("'", "''") + "'"


def load_or_create_secrets(*, rotate: bool) -> dict:
    host = os.environ.get("AQUATECH_MYSQL_HOST", "").strip()
    database = os.environ.get("AQUATECH_MYSQL_DATABASE", "").strip()
    username = os.environ.get("AQUATECH_MYSQL_USER", "").strip()
    password = os.environ.get("AQUATECH_MYSQL_PASSWORD", "").strip()
    port = int(os.environ.get("AQUATECH_MYSQL_PORT", "3306") or "3306")

    if SECRETS.is_file() and not rotate:
        data = json.loads(SECRETS.read_text(encoding="utf-8"))
        if all(data.get(k) for k in ("host", "database", "username", "password")):
            data.setdefault("port", 3306)
            return data

    if not APEX_API_KEY:
        sys.exit(f"Need {SECRETS.name} or AQUATECH_APEX_API_KEY to provision MariaDB")

    listed = apex_json("GET", f"/api/client/servers/{APEX_SERVER_ID}/databases")
    rows = listed.get("data") or []
    row = None
    for item in rows:
        attrs = item.get("attributes") or {}
        if attrs.get("name", "").endswith("_aquatech") or DB_NAME in attrs.get("name", ""):
            row = attrs
            break
    if row is None and rows:
        row = (rows[0].get("attributes") or {})

    if row is None:
        print("Creating Apex MariaDB database…", flush=True)
        created = apex_json(
            "POST",
            f"/api/client/servers/{APEX_SERVER_ID}/databases",
            {"database": DB_NAME, "remote": "%"},
        )
        row = created.get("attributes") or created

    db_id = row.get("id")
    if not db_id:
        sys.exit("Apex database response missing id")

    if rotate or not password:
        print("Rotating MariaDB password…", flush=True)
        rotated = apex_json(
            "POST",
            f"/api/client/servers/{APEX_SERVER_ID}/databases/{db_id}/rotate-password",
            {},
        )
        attrs = rotated.get("attributes") or rotated
        rel = (attrs.get("relationships") or {}).get("password") or {}
        password = ((rel.get("attributes") or {}).get("password") or "").strip()
        if not password:
            sys.exit("rotate-password returned no password")
        host_info = attrs.get("host") or row.get("host") or {}
        host = host_info.get("address") or host
        port = int(host_info.get("port") or port)
        database = attrs.get("name") or row.get("name") or database
        username = attrs.get("username") or row.get("username") or username
    else:
        host_info = row.get("host") or {}
        host = host or host_info.get("address") or ""
        port = int(host_info.get("port") or port)
        database = database or row.get("name") or ""
        username = username or row.get("username") or ""

    if not all([host, database, username, password]):
        sys.exit("Incomplete MySQL credentials")

    secrets = {
        "id": db_id,
        "host": host,
        "port": port,
        "database": database,
        "username": username,
        "password": password,
    }
    SECRETS.write_text(json.dumps(secrets, indent=2) + "\n", encoding="utf-8")
    print(f"Wrote {SECRETS.name} (gitignored)", flush=True)
    return secrets


def apply_schema(secrets: dict) -> None:
    try:
        import pymysql
    except ImportError:
        sys.exit("pip install pymysql")

    conn = pymysql.connect(
        host=secrets["host"],
        port=int(secrets["port"]),
        user=secrets["username"],
        password=secrets["password"],
        database=secrets["database"],
        charset="utf8mb4",
        connect_timeout=20,
        autocommit=True,
    )
    try:
        with conn.cursor() as cur:
            for stmt in SCHEMA_SQL.split(";"):
                sql = stmt.strip()
                if sql:
                    cur.execute(sql)
            cur.execute("SHOW TABLES")
            tables = [r[0] for r in cur.fetchall()]
        print(f"OK MariaDB schema; tables={tables}", flush=True)
    finally:
        conn.close()


def apex_command(cmd: str) -> None:
    apex_json(
        "POST",
        f"/api/client/servers/{APEX_SERVER_ID}/command",
        {"command": cmd},
    )
    print(f"OK console: {cmd}", flush=True)


def apex_state() -> str:
    data = apex_json("GET", f"/api/client/servers/{APEX_SERVER_ID}/resources")
    return str((data.get("attributes") or {}).get("current_state") or "unknown")


def apex_power(signal: str) -> None:
    apex_json("POST", f"/api/client/servers/{APEX_SERVER_ID}/power", {"signal": signal})
    print(f"OK power {signal}", flush=True)


def wait_running(*, expect_restart: bool, timeout_sec: int = 600) -> None:
    deadline = time.time() + timeout_sec
    seen_offline = not expect_restart
    while time.time() < deadline:
        time.sleep(5)
        state = apex_state()
        print(f"  panel state: {state}", flush=True)
        if state in ("offline", "stopping", "starting"):
            seen_offline = True
        if seen_offline and state == "running":
            print("OK server running", flush=True)
            return
    print("WARN: server not confirmed running", flush=True)


def upload_configs() -> None:
    # Reuse deploy filter for plugin configs only.
    os.environ["AQUATECH_SFTP_ONLY"] = ",".join(
        [
            "plugins/LuckPerms/config.yml",
            "plugins/ajLeaderboards/cache_storage.yml",
            "plugins/SkinsRestorer/config.yml",
            "plugins/DiscordSRV/config.yml",
            "plugins/LuckPerms/yaml-storage",
        ]
    )
    # Import deploy helpers without restart.
    sys.path.insert(0, str(ROOT / "scripts" / "tasks"))
    import deploy_apexnodes_sftp as deploy  # type: ignore

    stage = deploy.stage_tree()
    try:
        deploy.upload_tree(stage)
    finally:
        import shutil

        shutil.rmtree(stage, ignore_errors=True)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--rotate-password", action="store_true")
    parser.add_argument("--no-upload", action="store_true")
    parser.add_argument("--no-restart", action="store_true")
    parser.add_argument("--skip-migrate", action="store_true", help="Skip LP export/import")
    args = parser.parse_args()

    secrets = load_or_create_secrets(rotate=args.rotate_password)
    print(
        f"MariaDB {secrets['username']}@{secrets['host']}:{secrets['port']}/{secrets['database']}",
        flush=True,
    )
    apply_schema(secrets)
    write_plugin_configs(secrets)

    if args.no_upload:
        print("Skipped SFTP (--no-upload)")
        return 0

    if not os.environ.get("AQUATECH_SFTP_PASS"):
        sys.exit("Set AQUATECH_SFTP_PASS to upload plugin configs")

    # Export ranks while still on YAML (if server running).
    if not args.skip_migrate and APEX_API_KEY:
        state = apex_state()
        print(f"panel state: {state}", flush=True)
        if state == "running":
            try:
                apex_command("lp export aquatech-pre-mysql")
                time.sleep(3)
            except SystemExit as ex:
                print(f"WARN export: {ex}", flush=True)

    upload_configs()

    if args.no_restart:
        print("Skipped restart (--no-restart)")
        return 0

    if not APEX_API_KEY:
        print("WARN: no AQUATECH_APEX_API_KEY — restart Mohist manually", flush=True)
        return 0

    state = apex_state()
    signal = "restart" if state == "running" else "start"
    apex_power(signal)
    wait_running(expect_restart=(signal == "restart"))
    # Give LuckPerms time to create tables + connect.
    time.sleep(35)
    if not args.skip_migrate:
        try:
            apex_command("lp import aquatech-pre-mysql")
            time.sleep(2)
            apex_command("lp sync")
        except SystemExit as ex:
            print(f"WARN import: {ex}", flush=True)
            print(
                "If import missing: run on console `lp export` before switch, or re-seed groups.",
                flush=True,
            )

    # Re-check tables after LP boot.
    apply_schema(secrets)
    try:
        import pymysql

        conn = pymysql.connect(
            host=secrets["host"],
            port=int(secrets["port"]),
            user=secrets["username"],
            password=secrets["password"],
            database=secrets["database"],
            connect_timeout=20,
        )
        with conn.cursor() as cur:
            cur.execute("SHOW TABLES")
            tables = [r[0] for r in cur.fetchall()]
        conn.close()
        print(f"Final tables ({len(tables)}): {', '.join(tables)}", flush=True)
    except Exception as ex:  # noqa: BLE001
        print(f"WARN final table list: {ex}", flush=True)

    print("Done. Portal site DB remains Cloudflare D1 (separate).", flush=True)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
