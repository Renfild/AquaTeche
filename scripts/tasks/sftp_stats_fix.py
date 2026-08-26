#!/usr/bin/env python3
"""Upload first-party mod jars + MariaDB stats config. Purge only aqualumen-/aquatech_ui- remotes.

Does not touch packetfixer / Chunky. Secrets stay in .apex_deploy.json / .apex_mysql.json.

Usage:
  python scripts/tasks/sftp_stats_fix.py jar1 jar2
"""
from __future__ import annotations

import json
import posixpath
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "scripts" / "tasks"))
import deploy_apexnodes_sftp as deploy  # noqa: E402

PURGE_PREFIXES = ("aqualumen-", "aquatech_ui-")


def put_file(sftp, local: Path, remote: str) -> None:
    parent = posixpath.dirname(remote)
    if parent and parent != ".":
        parts = parent.split("/")
        cur = ""
        for part in parts:
            cur = f"{cur}/{part}" if cur else part
            try:
                sftp.stat(cur)
            except OSError:
                sftp.mkdir(cur)
    sftp.put(str(local), remote)
    print(f"  put {remote} ({local.stat().st_size} bytes)", flush=True)


def main() -> int:
    jars = [Path(a) for a in sys.argv[1:]]
    if not jars:
        sys.exit("usage: sftp_stats_fix.py <jar> [jar...]")
    for jar in jars:
        if not jar.is_file():
            sys.exit(f"missing jar: {jar}")

    deploy.load_deploy_secrets()
    import paramiko

    if not deploy.PASSWORD:
        sys.exit("Need sftp_pass in .apex_deploy.json")

    transport = paramiko.Transport((deploy.HOST, deploy.PORT))
    transport.connect(username=deploy.USER, password=deploy.PASSWORD)
    sftp = paramiko.SFTPClient.from_transport(transport)
    assert sftp is not None

    keep: set[str] = set()
    try:
        for jar in jars:
            remote = f"mods/{jar.name}"
            put_file(sftp, jar, remote)
            keep.add(jar.name)

        secrets = deploy.load_mysql_secrets()
        if secrets:
            import tempfile

            with tempfile.NamedTemporaryFile(
                "w", suffix=".json", delete=False, encoding="utf-8"
            ) as handle:
                handle.write(json.dumps(secrets, indent=2) + "\n")
                tmp = Path(handle.name)
            try:
                put_file(sftp, tmp, "config/aquatech_mysql.json")
            finally:
                tmp.unlink(missing_ok=True)
        else:
            print("WARN no .apex_mysql.json — skip config/aquatech_mysql.json", flush=True)

        try:
            names = sftp.listdir("mods")
        except OSError as ex:
            print(f"WARN listdir mods: {ex}", flush=True)
            names = []
        for name in names:
            if not name.endswith(".jar"):
                continue
            if not any(name.startswith(p) for p in PURGE_PREFIXES):
                continue
            if name in keep:
                continue
            remote = f"mods/{name}"
            sftp.remove(remote)
            print(f"  purged {remote}", flush=True)
    finally:
        sftp.close()
        transport.close()
    print("OK sftp stats fix", flush=True)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
