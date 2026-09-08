#!/usr/bin/env python3
"""Upload a file to Cloudflare R2. Secrets in gitignored .r2.json.

{
  "account_id": "...",
  "access_key_id": "...",
  "secret_access_key": "...",
  "bucket": "aquatech-world-backups",
  "endpoint": "https://ACCOUNT_ID.r2.cloudflarestorage.com"
}

Missing file or boto3 → skip with WARN (local zip still counts).
"""
from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SECRETS = ROOT / ".r2.json"


def load_secrets() -> dict | None:
    if not SECRETS.is_file():
        return None
    data = json.loads(SECRETS.read_text(encoding="utf-8"))
    if not data.get("access_key_id") or not data.get("bucket"):
        return None
    return data


def put(local: Path, key: str | None = None) -> bool:
    local = Path(local)
    if not local.is_file():
        print(f"WARN R2 skip, missing {local}", flush=True)
        return False
    secrets = load_secrets()
    if not secrets:
        print("WARN R2 skip: no .r2.json", flush=True)
        return False
    try:
        import boto3
        from botocore.config import Config
    except ImportError:
        print("WARN R2 skip: pip install boto3", flush=True)
        return False
    account = str(secrets.get("account_id") or "").strip()
    endpoint = str(secrets.get("endpoint") or "").strip()
    if not endpoint and account:
        endpoint = f"https://{account}.r2.cloudflarestorage.com"
    bucket = str(secrets["bucket"])
    object_key = key or local.name
    client = boto3.client(
        "s3",
        endpoint_url=endpoint,
        aws_access_key_id=str(secrets["access_key_id"]),
        aws_secret_access_key=str(secrets["secret_access_key"]),
        region_name="auto",
        config=Config(signature_version="s3v4"),
    )
    client.upload_file(str(local), bucket, object_key)
    print(f"OK R2 {bucket}/{object_key} ({local.stat().st_size} bytes)", flush=True)
    return True


def prune_r2(keep_daily: int = 7, keep_weekly: int = 4, keep_monthly: int = 3) -> int:
    secrets = load_secrets()
    if not secrets:
        return 0
    try:
        import boto3
        from botocore.config import Config
        from datetime import datetime
    except ImportError:
        return 0

    account = str(secrets.get("account_id") or "").strip()
    endpoint = str(secrets.get("endpoint") or "").strip()
    if not endpoint and account:
        endpoint = f"https://{account}.r2.cloudflarestorage.com"
    bucket = str(secrets["bucket"])

    try:
        client = boto3.client(
            "s3",
            endpoint_url=endpoint,
            aws_access_key_id=str(secrets["access_key_id"]),
            aws_secret_access_key=str(secrets["secret_access_key"]),
            region_name="auto",
            config=Config(signature_version="s3v4"),
        )
        paginator = client.get_paginator("list_objects_v2")
        objects = []
        for page in paginator.paginate(Bucket=bucket):
            for item in page.get("Contents", []):
                objects.append(item)

        if not objects:
            return 0

        runs: dict[str, list[str]] = {}
        for obj in objects:
            key = obj["Key"]
            parts = key.split("/", 1)
            prefix = parts[0] if len(parts) > 1 else key
            runs.setdefault(prefix, []).append(key)

        sorted_runs = sorted(runs.keys())
        if len(sorted_runs) <= keep_daily:
            return 0

        retained_runs = set(sorted_runs[-keep_daily:])
        older_runs = sorted_runs[:-keep_daily]
        weekly_buckets: dict[str, str] = {}
        monthly_buckets: dict[str, str] = {}

        for run in older_runs:
            try:
                dt = datetime.strptime(run[:8], "%Y%m%d")
                week_key = f"{dt.isocalendar()[0]}-W{dt.isocalendar()[1]:02d}"
                month_key = f"{dt.year}-{dt.month:02d}"
                if week_key not in weekly_buckets:
                    weekly_buckets[week_key] = run
                if month_key not in monthly_buckets:
                    monthly_buckets[month_key] = run
            except Exception:
                pass

        for wk in sorted(weekly_buckets.keys())[-keep_weekly:]:
            retained_runs.add(weekly_buckets[wk])

        for mk in sorted(monthly_buckets.keys())[-keep_monthly:]:
            retained_runs.add(monthly_buckets[mk])

        delete_keys = []
        for run in sorted_runs:
            if run not in retained_runs:
                delete_keys.extend(runs[run])

        if not delete_keys:
            return 0

        deleted_count = 0
        for i in range(0, len(delete_keys), 1000):
            batch = [{"Key": k} for k in delete_keys[i : i + 1000]]
            client.delete_objects(Bucket=bucket, Delete={"Objects": batch})
            deleted_count += len(batch)
            print(f"OK R2 pruned {len(batch)} old objects from {bucket}", flush=True)

        return deleted_count
    except Exception as exc:
        print(f"WARN R2 prune failed: {exc}", flush=True)
        return 0


def main() -> int:
    if len(sys.argv) < 2:
        print("usage: r2_put.py <file> [object-key]", file=sys.stderr)
        return 2
    ok = put(Path(sys.argv[1]), sys.argv[2] if len(sys.argv) > 2 else None)
    return 0 if ok else 1


if __name__ == "__main__":
    raise SystemExit(main())
