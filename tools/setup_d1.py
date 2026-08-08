#!/usr/bin/env python3
"""Create D1 database, apply migrations, bind to Pages project + wrangler.toml.

Env / files:
  CLOUDFLARE_API_TOKEN  or  .cf_token
  CLOUDFLARE_ACCOUNT_ID (optional)

Token needs: Account → D1 → Edit, and Account → Cloudflare Pages → Edit.
"""
from __future__ import annotations

import json
import os
import re
import sys
import urllib.error
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
WRANGLER = ROOT / "wrangler.toml"
MIGRATIONS = ROOT / "migrations"
ACCOUNT_DEFAULT = "b855fc404ea390174024f1d249cb1964"
DB_NAME = "aquatech"
PAGES_PROJECT = "aquatech"


def token() -> str:
    t = (os.environ.get("CLOUDFLARE_API_TOKEN") or "").strip()
    if t:
        return t
    for p in (ROOT / ".cf_token", ROOT / ".cloudflare_token"):
        if p.is_file():
            return p.read_text(encoding="utf-8").strip()
    sys.exit("missing CLOUDFLARE_API_TOKEN or .cf_token")


def account() -> str:
    return (os.environ.get("CLOUDFLARE_ACCOUNT_ID") or ACCOUNT_DEFAULT).strip()


def api(method: str, url: str, data: dict | None = None):
    body = None if data is None else json.dumps(data).encode()
    req = urllib.request.Request(
        url,
        data=body,
        method=method,
        headers={
            "Authorization": f"Bearer {token()}",
            "Content-Type": "application/json",
            "User-Agent": "AquaTechD1Setup",
        },
    )
    try:
        with urllib.request.urlopen(req, timeout=120) as r:
            return json.loads(r.read().decode())
    except urllib.error.HTTPError as e:
        err = e.read().decode("utf-8", "replace")
        raise SystemExit(f"HTTP {e.code}: {err[:800]}") from e


def list_dbs() -> list[dict]:
    out = api("GET", f"https://api.cloudflare.com/client/v4/accounts/{account()}/d1/database")
    return list(out.get("result") or [])


def ensure_db() -> str:
    for d in list_dbs():
        if d.get("name") == DB_NAME:
            print("D1 exists", d["uuid"])
            return d["uuid"]
    created = api(
        "POST",
        f"https://api.cloudflare.com/client/v4/accounts/{account()}/d1/database",
        {"name": DB_NAME},
    )
    uid = created["result"]["uuid"]
    print("D1 created", uid)
    return uid


def patch_wrangler(db_id: str) -> None:
    block = (
        "[[d1_databases]]\n"
        'binding = "DB"\n'
        f'database_name = "{DB_NAME}"\n'
        f'database_id = "{db_id}"\n'
    )
    text = WRANGLER.read_text(encoding="utf-8")
    if "[[d1_databases]]" in text and not text.split("[[d1_databases]]")[0].rstrip().endswith("#"):
        # Replace first active (uncommented) database_id, or whole commented stub
        if re.search(r'(?m)^\[\[d1_databases\]\]', text):
            text = re.sub(
                r'(?ms)^\[\[d1_databases\]\].*?(?=^\[|\Z)',
                block + "\n",
                text,
                count=1,
            )
        else:
            text = re.sub(
                r"(?ms)^# D1 binding is filled by:.*?^# database_id = \"<uuid>\"\n?",
                block + "\n",
                text,
                count=1,
            )
    else:
        text = re.sub(
            r"(?ms)^# D1 binding is filled by:.*?^# database_id = \"<uuid>\"\n?",
            block + "\n",
            text,
            count=1,
        )
        if "[[d1_databases]]" not in text or not re.search(r'(?m)^\[\[d1_databases\]\]', text):
            text = text.rstrip() + "\n\n" + block
    WRANGLER.write_text(text if text.endswith("\n") else text + "\n", encoding="utf-8")
    print("updated", WRANGLER)


def split_sql(sql: str) -> list[str]:
    """Split on ';' outside of single-quoted string literals / -- comments."""
    parts: list[str] = []
    buf: list[str] = []
    i = 0
    in_str = False
    while i < len(sql):
        ch = sql[i]
        if in_str:
            buf.append(ch)
            if ch == "'":
                if i + 1 < len(sql) and sql[i + 1] == "'":
                    buf.append(sql[i + 1])
                    i += 2
                    continue
                in_str = False
            i += 1
            continue
        if ch == "'":
            in_str = True
            buf.append(ch)
            i += 1
            continue
        if ch == "-" and i + 1 < len(sql) and sql[i + 1] == "-":
            while i < len(sql) and sql[i] != "\n":
                i += 1
            continue
        if ch == ";":
            stmt = "".join(buf).strip()
            if stmt:
                parts.append(stmt)
            buf = []
            i += 1
            continue
        buf.append(ch)
        i += 1
    stmt = "".join(buf).strip()
    if stmt:
        parts.append(stmt)
    return parts


def apply_sql(db_id: str, sql: str) -> None:
    parts = split_sql(sql)
    for i, part in enumerate(parts, 1):
        print(f"  query {i}/{len(parts)}…")
        api(
            "POST",
            f"https://api.cloudflare.com/client/v4/accounts/{account()}/d1/database/{db_id}/query",
            {"sql": part},
        )


def bind_pages(db_id: str) -> None:
    """Attach D1 + purchase flag to Pages production/preview."""
    base = {
        "compatibility_date": "2026-08-08",
        "d1_databases": {"DB": {"id": db_id}},
        "env_vars": {
            "PURCHASES_ENABLED": {"type": "plain_text", "value": "false"},
            "SITE_CANONICAL": {
                "type": "plain_text",
                "value": "https://aquatech-7gs.pages.dev",
            },
            "PROJECT_NAME": {"type": "plain_text", "value": "AquaTech"},
        },
    }
    payload = {
        "deployment_configs": {
            "production": base,
            "preview": base,
        }
    }
    out = api(
        "PATCH",
        f"https://api.cloudflare.com/client/v4/accounts/{account()}/pages/projects/{PAGES_PROJECT}",
        payload,
    )
    if not out.get("success"):
        raise SystemExit(f"Pages bind failed: {out}")
    print("bound D1 to Pages project", PAGES_PROJECT)


def main() -> None:
    db_id = ensure_db()
    patch_wrangler(db_id)
    for path in sorted(MIGRATIONS.glob("*.sql")):
        print("migrate", path.name)
        apply_sql(db_id, path.read_text(encoding="utf-8"))
    try:
        bind_pages(db_id)
    except SystemExit as e:
        print("WARN pages bind:", e, file=sys.stderr)
    print("OK D1 ready", db_id)


if __name__ == "__main__":
    main()
