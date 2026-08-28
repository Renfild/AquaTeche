#!/usr/bin/env python3
"""Smoke-check Apex after deploy: panel, jars, FAWE, MariaDB, portal catalog.

Usage:
  python scripts/tasks/smoke_apex_server.py
"""
from __future__ import annotations

import json
import sys
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "scripts" / "tasks"))

import deploy_apexnodes_sftp as deploy  # noqa: E402

PORTAL = "https://aquateche.store"


def check_catalog() -> bool:
    url = f"{PORTAL}/api/catalog"
    try:
        req = urllib.request.Request(
            url,
            headers={"User-Agent": "AquaTechSmoke/1.0 (https://aquateche.store)"},
        )
        with urllib.request.urlopen(req, timeout=12) as resp:
            status = resp.status
            body = json.loads(resp.read().decode("utf-8", "replace"))
    except Exception as exc:
        print(f"FAIL catalog {url}: {exc}")
        return False
    if status != 200 or not body.get("ok"):
        print(f"FAIL catalog HTTP {status} body={str(body)[:180]}")
        return False
    items = body.get("items") or []
    print(f"catalog OK {len(items)} items purchases_enabled={body.get('purchases_enabled')}")
    return True


def check_auth_toml(sftp) -> bool:
    for remote in (
        "config/aquatech_ui-common.toml",
        "server/config/aquatech_ui-common.toml",
    ):
        try:
            with sftp.open(remote, "r") as handle:
                text = handle.read().decode("utf-8", "replace")
        except OSError:
            continue
        if "requirePortalSession = true" in text or "requirePortalSession=true" in text:
            print(f"auth requirePortalSession true OK ({remote})")
            return True
        if "requirePortalSession" in text:
            print(f"FAIL {remote} requirePortalSession is not true")
            return False
    print("WARN no aquatech_ui-common.toml with requirePortalSession")
    return True



def check_mysql() -> bool:
    secrets = deploy.load_mysql_secrets()
    if not secrets:
        print("WARN MySQL: no .apex_mysql.json — skip")
        return True
    try:
        import pymysql
    except ImportError:
        print("FAIL MySQL: pip install pymysql")
        return False
    try:
        conn = pymysql.connect(
            host=str(secrets["host"]),
            port=int(secrets.get("port") or 3306),
            user=str(secrets["username"]),
            password=str(secrets["password"]),
            database=str(secrets["database"]),
            connect_timeout=15,
            read_timeout=15,
            write_timeout=15,
        )
    except Exception as ex:  # noqa: BLE001 — smoke must report any driver/host error
        print(f"FAIL MySQL connect: {ex}")
        return False
    try:
        with conn.cursor() as cur:
            cur.execute("SELECT 1")
            cur.fetchone()
            cur.execute("SHOW TABLES LIKE 'aquatech_meta'")
            row = cur.fetchone()
        if not row:
            print("FAIL MySQL: aquatech_meta missing — run setup_apex_mysql.py")
            return False
        print(
            f"MySQL OK {secrets['username']}@{secrets['host']}:{secrets.get('port', 3306)}"
            f"/{secrets['database']} (aquatech_meta)"
        )
        return True
    finally:
        conn.close()


def main() -> int:
    deploy.load_deploy_secrets()
    state = deploy.apex_server_state()
    print(f"panel: {state}")
    ok = state == "running"

    import paramiko

    if not deploy.PASSWORD:
        print("WARN: no SFTP pass — skip jar listing")
        ok = check_mysql() and check_catalog() and ok
        return 0 if ok else 1

    t = paramiko.Transport((deploy.HOST, deploy.PORT))
    t.connect(username=deploy.USER, password=deploy.PASSWORD)
    s = paramiko.SFTPClient.from_transport(t)
    assert s is not None

    mods = sorted(n for n in s.listdir("mods") if n.endswith(".jar"))
    for prefix in ("aquatech_ui-", "aqualumen-", "packetfixer-"):
        hits = [n for n in mods if n.startswith(prefix)]
        print(f"mods/{prefix}*: {hits}")
        if len(hits) != 1:
            print(f"  FAIL expected exactly 1 {prefix}*.jar")
            ok = False

    try:
        plugins = s.listdir("plugins")
        fawe = [n for n in plugins if n.startswith("FastAsyncWorldEdit") and n.endswith(".jar")]
        print(f"plugins/FAWE: {fawe}")
        try:
            with s.open("plugins/FastAsyncWorldEdit/config.yml", "r") as f:
                text = f.read().decode("utf-8", "replace")
            if "persistent-brushes: false" in text:
                print("FAWE persistent-brushes: false OK")
            elif "persistent-brushes: true" in text:
                print("FAIL FAWE persistent-brushes still true")
                ok = False
            else:
                print("WARN could not find persistent-brushes key")
        except OSError:
            print("WARN no remote FAWE config.yml")
    except OSError as ex:
        print(f"WARN plugins list: {ex}")

    if not check_auth_toml(s):
        ok = False

    s.close()
    t.close()

    if not check_mysql():
        ok = False
    if not check_catalog():
        ok = False

    print("OK" if ok else "FAIL")
    return 0 if ok else 1


if __name__ == "__main__":
    raise SystemExit(main())
