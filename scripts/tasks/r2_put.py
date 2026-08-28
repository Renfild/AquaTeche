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


def main() -> int:
    if len(sys.argv) < 2:
        print("usage: r2_put.py <file> [object-key]", file=sys.stderr)
        return 2
    ok = put(Path(sys.argv[1]), sys.argv[2] if len(sys.argv) > 2 else None)
    return 0 if ok else 1


if __name__ == "__main__":
    raise SystemExit(main())
