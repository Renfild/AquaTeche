#!/usr/bin/env python3
"""Create a Pterodactyl schedule: save + lp sync + restart at 04:00 Europe/Moscow.

Panel cron is UTC. 04:00 MSK = 01:00 UTC.
Secrets: .apex_deploy.json (apex_api_key) or AQUATECH_APEX_API_KEY.

Usage:
  python scripts/tasks/setup_apex_daily_reload.py
  python scripts/tasks/setup_apex_daily_reload.py --list
"""
from __future__ import annotations

import argparse
import json
import os
import sys
import urllib.error
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DEPLOY_SECRETS = ROOT / ".apex_deploy.json"
SCHEDULE_NAME = "daily-4am-msk-reload"

APEX_PANEL = os.environ.get("AQUATECH_APEX_PANEL", "https://panel.apexnodes.xyz").rstrip("/")
APEX_SERVER_ID = os.environ.get("AQUATECH_APEX_SERVER_ID", "6fdc6f7b")
APEX_API_KEY = os.environ.get("AQUATECH_APEX_API_KEY", "")


def load_key() -> None:
    global APEX_PANEL, APEX_SERVER_ID, APEX_API_KEY
    if DEPLOY_SECRETS.is_file():
        data = json.loads(DEPLOY_SECRETS.read_text(encoding="utf-8"))
        APEX_PANEL = os.environ.get("AQUATECH_APEX_PANEL", data.get("apex_panel", APEX_PANEL)).rstrip("/")
        APEX_SERVER_ID = os.environ.get("AQUATECH_APEX_SERVER_ID", data.get("apex_server_id", APEX_SERVER_ID))
        APEX_API_KEY = os.environ.get("AQUATECH_APEX_API_KEY", data.get("apex_api_key", APEX_API_KEY))
    if not APEX_API_KEY:
        sys.exit("Need apex_api_key in .apex_deploy.json or AQUATECH_APEX_API_KEY")


def apex_json(method: str, path: str, body: dict | None = None) -> dict:
    url = f"{APEX_PANEL}{path}"
    data = None if body is None else json.dumps(body).encode("utf-8")
    req = urllib.request.Request(
        url,
        data=data,
        headers={
            "Authorization": f"Bearer {APEX_API_KEY}",
            "Accept": "Application/vnd.pterodactyl.v1+json",
            "Content-Type": "application/json",
        },
        method=method,
    )
    try:
        with urllib.request.urlopen(req, timeout=45) as resp:
            raw = resp.read()
    except urllib.error.HTTPError as ex:
        err = ex.read().decode("utf-8", "replace")
        raise SystemExit(f"Apex {method} {path} HTTP {ex.code}: {err[:600]}") from ex
    if not raw:
        return {}
    return json.loads(raw)


def list_schedules() -> list[dict]:
    payload = apex_json("GET", f"/api/client/servers/{APEX_SERVER_ID}/schedules")
    return payload.get("data") or []


def print_schedules() -> None:
    rows = list_schedules()
    print(f"schedules {len(rows)}")
    for item in rows:
        attrs = item.get("attributes") or {}
        cron = attrs.get("cron") or {}
        print(
            f"  id={attrs.get('id')} name={attrs.get('name')!r} active={attrs.get('is_active')} "
            f"utc={cron.get('hour')}:{cron.get('minute')}"
        )
        tasks = ((attrs.get("relationships") or {}).get("tasks") or {}).get("data") or []
        for task in tasks:
            ta = task.get("attributes") or {}
            print(f"    {ta.get('action')} {ta.get('payload')} offset={ta.get('time_offset')}")


def ensure_schedule() -> int:
    for item in list_schedules():
        attrs = item.get("attributes") or {}
        if attrs.get("name") == SCHEDULE_NAME:
            sid = int(attrs["id"])
            print(f"schedule exists id={sid}", flush=True)
            apex_json(
                "POST",
                f"/api/client/servers/{APEX_SERVER_ID}/schedules/{sid}",
                {
                    "name": SCHEDULE_NAME,
                    "is_active": True,
                    "minute": "0",
                    "hour": "4",
                    "day_of_month": "*",
                    "month": "*",
                    "day_of_week": "*",
                    "only_when_online": True,
                },
            )
            print("  retargeted to 04:00 panel time (MSK)", flush=True)
            return sid
    created = apex_json(
        "POST",
        f"/api/client/servers/{APEX_SERVER_ID}/schedules",
        {
            "name": SCHEDULE_NAME,
            "is_active": True,
            "minute": "0",
            "hour": "4",
            "day_of_month": "*",
            "month": "*",
            "day_of_week": "*",
            "only_when_online": True,
        },
    )
    attrs = created.get("attributes") or created
    sid = int(attrs["id"])
    print(f"created schedule id={sid} (04:00 panel TZ / MSK)", flush=True)
    return sid


def ensure_tasks(schedule_id: int) -> None:
    existing = apex_json("GET", f"/api/client/servers/{APEX_SERVER_ID}/schedules/{schedule_id}")
    attrs = existing.get("attributes") or {}
    tasks = ((attrs.get("relationships") or {}).get("tasks") or {}).get("data") or []
    wanted = [
        ("command", "save-all", 0),
        ("command", "lp sync", 15),
        ("power", "restart", 30),
    ]
    have_set = {
        ((t.get("attributes") or {}).get("action"), (t.get("attributes") or {}).get("payload"))
        for t in tasks
    }
    for action, payload, offset in wanted:
        if (action, payload) in have_set:
            print(f"  task exists {action} {payload}", flush=True)
            continue
        apex_json(
            "POST",
            f"/api/client/servers/{APEX_SERVER_ID}/schedules/{schedule_id}/tasks",
            {
                "action": action,
                "payload": payload,
                "time_offset": offset,
                "continue_on_failure": True,
            },
        )
        print(f"  added task {action} {payload} +{offset}s", flush=True)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--list", action="store_true")
    args = parser.parse_args()
    load_key()
    if args.list:
        print_schedules()
        return 0
    schedule_id = ensure_schedule()
    ensure_tasks(schedule_id)
    print_schedules()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
