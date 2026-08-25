#!/usr/bin/env python3
"""Upload AquaTech server/ to ApexNodes (Pterodactyl SFTP) + optional panel restart.

Usage (secrets via env or gitignored .apex_deploy.json — never commit):
  $env:AQUATECH_SFTP_PASS = '...'
  $env:AQUATECH_APEX_API_KEY = 'ptlc_...'
  python scripts/tasks/deploy_apexnodes_sftp.py

Quest/config only (fast):
  $env:AQUATECH_SFTP_ONLY = 'config/ftbquests'
  python scripts/tasks/deploy_apexnodes_sftp.py

Restart panel server without upload:
  python scripts/tasks/deploy_apexnodes_sftp.py --restart-only

Optional env:
  AQUATECH_SFTP_HOST       default g-pl-3.apexnodes.xyz
  AQUATECH_SFTP_PORT       default 2022
  AQUATECH_SFTP_USER       default oxmzg5d0.6fdc6f7b
  AQUATECH_SERVER_PORT     default 21561
  AQUATECH_APEX_PANEL      default https://panel.apexnodes.xyz
  AQUATECH_APEX_SERVER_ID  default 6fdc6f7b
  AQUATECH_APEX_API_KEY    Client API key (Account -> API Credentials)
  AQUATECH_SFTP_ONLY       Comma prefixes, e.g. config/ftbquests
  AQUATECH_SKIP_REPO_SYNC  1 = skip kubejs/datapacks mirror into server/
  AQUATECH_SKIP_BACKUP     1 = skip panel world backup before full SFTP
"""
from __future__ import annotations

import argparse
import json
import os
import shutil
import sys
import tempfile
import time
import urllib.error
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SERVER = ROOT / "server"
DEPLOY_SECRETS = ROOT / ".apex_deploy.json"

# Remote paths (relative to SFTP root) -> filename prefixes that must be unique.
# After upload, delete remote jars with these prefixes that are not in the local keep-set.
FIRST_PARTY_PURGE: dict[str, tuple[str, ...]] = {
    "mods": ("aquatech_ui-", "aqualumen-", "packetfixer-"),
}

HOST = "g-pl-3.apexnodes.xyz"
PORT = 2022
USER = "oxmzg5d0.6fdc6f7b"
PASSWORD = ""
SERVER_PORT = "21561"

APEX_PANEL = "https://panel.apexnodes.xyz"
APEX_SERVER_ID = "6fdc6f7b"
APEX_API_KEY = ""

SKIP_DIRS = {
    "java17",
    "java21",
    "logs",
    "crash-reports",
    "local",
    "client",
    "world",
}
SKIP_DIR_PREFIXES = ("world_backup", "_backup", "_uuid_migrate")
SKIP_FILES = {"Arclight-1.20.1.jar", "Mohist-1.20.1.jar"}


def load_deploy_secrets() -> None:
    """Fill globals from .apex_deploy.json then env (env wins)."""
    global HOST, PORT, USER, PASSWORD, SERVER_PORT, APEX_PANEL, APEX_SERVER_ID, APEX_API_KEY
    data: dict = {}
    if DEPLOY_SECRETS.is_file():
        data = json.loads(DEPLOY_SECRETS.read_text(encoding="utf-8"))
    HOST = os.environ.get("AQUATECH_SFTP_HOST", data.get("sftp_host", HOST))
    PORT = int(os.environ.get("AQUATECH_SFTP_PORT", data.get("sftp_port", PORT)))
    USER = os.environ.get("AQUATECH_SFTP_USER", data.get("sftp_user", USER))
    PASSWORD = os.environ.get("AQUATECH_SFTP_PASS", data.get("sftp_pass", ""))
    SERVER_PORT = str(os.environ.get("AQUATECH_SERVER_PORT", data.get("server_port", SERVER_PORT)))
    APEX_PANEL = os.environ.get(
        "AQUATECH_APEX_PANEL", data.get("apex_panel", APEX_PANEL)
    ).rstrip("/")
    APEX_SERVER_ID = os.environ.get(
        "AQUATECH_APEX_SERVER_ID", data.get("apex_server_id", APEX_SERVER_ID)
    )
    APEX_API_KEY = os.environ.get("AQUATECH_APEX_API_KEY", data.get("apex_api_key", ""))


def _only_prefixes() -> tuple[str, ...]:
    raw = os.environ.get("AQUATECH_SFTP_ONLY", "").strip()
    if not raw:
        return ()
    return tuple(p.strip().replace("\\", "/").strip("/") for p in raw.split(",") if p.strip())


def should_skip(rel: Path) -> bool:
    rel_posix = rel.as_posix()
    only = _only_prefixes()
    if only:
        if not any(rel_posix == p or rel_posix.startswith(p + "/") for p in only):
            return True
    parts = rel.parts
    if parts and parts[0] in SKIP_DIRS:
        if parts[0] == "world" and os.environ.get("AQUATECH_INCLUDE_WORLD", "").strip() in ("1", "true", "yes"):
            pass
        else:
            return True
    if parts and any(parts[0].startswith(p) for p in SKIP_DIR_PREFIXES):
        return True
    if rel.name in SKIP_FILES:
        return True
    if rel.suffix == ".log":
        return True
    if rel.name.endswith(".bak") or ".pre-" in rel.name:
        return True
    # Patch-patcher scratch files must never shadow the patched plugin jar.
    if rel.name.endswith(".tmp") or rel.name.startswith("_"):
        return True
    return False


def sync_repo_into_server() -> None:
    """Mirror repo kubejs + aquatech datapacks into server/ before staging."""
    if os.environ.get("AQUATECH_SKIP_REPO_SYNC", "").strip() in {"1", "true", "yes"}:
        print("  skip repo->server sync (AQUATECH_SKIP_REPO_SYNC)", flush=True)
        return

    copied = 0

    def mirror_tree(src: Path, dst: Path) -> int:
        n = 0
        if not src.is_dir():
            return 0
        for path in src.rglob("*"):
            if not path.is_file():
                continue
            rel = path.relative_to(src)
            target = dst / rel
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(path, target)
            n += 1
        return n

    # Promote FAWE patched jar if live is locked / stale
    plugins = SERVER / "plugins"
    fawe_live = plugins / "FastAsyncWorldEdit.jar"
    fawe_alt = plugins / "FastAsyncWorldEdit-patched.jar"
    fawe_scratch = ROOT / "scripts" / "scratch" / "fawe_patch" / "FastAsyncWorldEdit-patched.jar"
    if not fawe_scratch.is_file():
        fawe_scratch = ROOT / "scripts" / "scratch" / "fawe_patch" / "FastAsyncWorldEdit.jar"
    for cand in (fawe_alt, fawe_scratch):
        if cand.is_file():
            try:
                shutil.copy2(cand, fawe_live)
                print(f"  FAWE promoted {cand.name} -> FastAsyncWorldEdit.jar", flush=True)
                break
            except OSError as ex:
                print(f"  WARN FAWE promote failed ({ex})", flush=True)

    # KubeJS (repo is source of truth)
    for folder in ("server_scripts", "startup_scripts", "client_scripts"):
        src = ROOT / "kubejs" / folder
        dst = SERVER / "kubejs" / folder
        if src.is_dir():
            copied += mirror_tree(src, dst)

    # Aquatech boot-fixes fish datapack -> server copies used by Mohist / Moonlight
    fish_src = ROOT / "datapacks" / "aquatech_boot_fixes"
    if fish_src.is_dir():
        for dst in (
            SERVER / "datapacks" / "aquatech_boot_fixes",
            SERVER / "moonlight-global-datapacks" / "aquatech_boot_fixes",
            SERVER / "world" / "datapacks" / "aquatech_boot_fixes",
        ):
            copied += mirror_tree(fish_src, dst)

    # Prefer repo config for aquatech_ui when present
    ui_cfg = ROOT / "config" / "aquatech_ui-common.toml"
    if ui_cfg.is_file():
        dst = SERVER / "config" / "aquatech_ui-common.toml"
        dst.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(ui_cfg, dst)
        copied += 1

    # FTB Quests: NEVER auto-overlay. Live Apex / server editor is source of truth.
    # Opt-in only: AQUATECH_SYNC_QUESTS=1 (still prefer pulling remote first).
    if os.environ.get("AQUATECH_SYNC_QUESTS", "").strip() in {"1", "true", "yes"}:
        quests_src = ROOT / "config" / "ftbquests"
        if quests_src.is_dir():
            copied += mirror_tree(quests_src, SERVER / "config" / "ftbquests")
            print("  WARN synced repo config/ftbquests -> server (AQUATECH_SYNC_QUESTS=1)", flush=True)
    else:
        print("  skip ftbquests overlay (set AQUATECH_SYNC_QUESTS=1 to force)", flush=True)

    print(f"  repo->server sync: {copied} file(s)", flush=True)


def patch_server_properties(props: Path, port: str) -> None:
    lines = props.read_text(encoding="utf-8", errors="replace").splitlines()
    out: list[str] = []
    seen_port = False
    for line in lines:
        if line.startswith("server-port="):
            out.append(f"server-port={port}")
            seen_port = True
        elif line.startswith("query.port="):
            out.append(f"query.port={port}")
        else:
            out.append(line)
    if not seen_port:
        out.append(f"server-port={port}")
    props.write_text("\n".join(out) + "\n", encoding="utf-8")


def load_mysql_secrets() -> dict | None:
    path = ROOT / ".apex_mysql.json"
    if not path.is_file():
        return None
    data = json.loads(path.read_text(encoding="utf-8"))
    if not all(data.get(k) for k in ("host", "database", "username", "password")):
        return None
    data.setdefault("port", 3306)
    return data


def inject_mysql_secrets(stage: Path) -> None:
    """Replace __AQUATECH_MYSQL_*__ placeholders in staged plugin configs."""
    secrets = load_mysql_secrets()
    if not secrets:
        for path in stage.rglob("*"):
            if not path.is_file():
                continue
            if path.suffix.lower() not in {".yml", ".yaml", ".properties", ".txt", ".toml"}:
                continue
            text = path.read_text(encoding="utf-8", errors="replace")
            if "__AQUATECH_MYSQL_" in text:
                sys.exit(
                    f"MySQL placeholders in {path.relative_to(stage)} but missing "
                    f"{ROOT / '.apex_mysql.json'} — run scripts/tasks/setup_apex_mysql.py"
                )
        return

    repl = {
        "__AQUATECH_MYSQL_HOST__": str(secrets["host"]),
        "__AQUATECH_MYSQL_PORT__": str(secrets["port"]),
        "__AQUATECH_MYSQL_DATABASE__": str(secrets["database"]),
        "__AQUATECH_MYSQL_USER__": str(secrets["username"]),
        "__AQUATECH_MYSQL_PASSWORD__": str(secrets["password"]),
    }
    patched = 0
    for path in stage.rglob("*"):
        if not path.is_file():
            continue
        if path.suffix.lower() not in {".yml", ".yaml", ".properties", ".txt", ".toml"}:
            continue
        text = path.read_text(encoding="utf-8", errors="replace")
        if "__AQUATECH_MYSQL_" not in text:
            continue
        for key, val in repl.items():
            text = text.replace(key, val)
        if "__AQUATECH_MYSQL_" in text:
            sys.exit(f"Unresolved MySQL placeholder in {path.relative_to(stage)}")
        path.write_text(text, encoding="utf-8")
        patched += 1
    if patched:
        print(f"  injected MySQL secrets into {patched} staged config(s)", flush=True)


def stage_tree() -> Path:
    sync_repo_into_server()
    tmp = Path(tempfile.mkdtemp(prefix="aquatech_apex_"))
    print(f"staging -> {tmp}", flush=True)
    only = _only_prefixes()
    if only:
        print(f"  filter: {', '.join(only)}", flush=True)
    count = 0
    for src in SERVER.rglob("*"):
        rel = src.relative_to(SERVER)
        if should_skip(rel):
            continue
        if src.is_dir():
            (tmp / rel).mkdir(parents=True, exist_ok=True)
            continue
        dst = tmp / rel
        dst.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(src, dst)
        count += 1
        if count % 500 == 0:
            print(f"  staged {count} files...", flush=True)
    if not only:
        mohist = SERVER / "Mohist-1.20.1.jar"
        if not mohist.is_file():
            sys.exit(f"missing {mohist}")
        shutil.copy2(mohist, tmp / "server.jar")
        print(f"  server.jar <= {mohist.name} ({mohist.stat().st_size // (1024*1024)} MB)", flush=True)
        props = tmp / "server.properties"
        if props.is_file():
            patch_server_properties(props, SERVER_PORT)
        eula = tmp / "eula.txt"
        if eula.is_file():
            text = eula.read_text(encoding="utf-8", errors="replace")
            if "eula=true" not in text.replace(" ", ""):
                eula.write_text("eula=true\n", encoding="utf-8")
    if count == 0:
        sys.exit("nothing to upload (check AQUATECH_SFTP_ONLY filter)")
    inject_mysql_secrets(tmp)
    total = sum(f.stat().st_size for f in tmp.rglob("*") if f.is_file())
    print(f"staged {count} files, {total / (1024**2):.1f} MiB", flush=True)
    return tmp


def purge_stale_first_party(sftp) -> None:
    """Delete remote first-party jars that are not the current local keep-set."""
    removed = 0
    for folder, prefixes in FIRST_PARTY_PURGE.items():
        local_dir = SERVER / folder
        keep: set[str] = set()
        if local_dir.is_dir():
            for prefix in prefixes:
                for p in local_dir.glob(f"{prefix}*.jar"):
                    keep.add(p.name)
        try:
            remote_names = sftp.listdir(folder)
        except OSError:
            continue
        for name in remote_names:
            if not name.endswith(".jar"):
                continue
            if not any(name.startswith(prefix) for prefix in prefixes):
                continue
            if name in keep:
                continue
            remote = f"{folder}/{name}"
            try:
                sftp.remove(remote)
                removed += 1
                print(f"  purged stale {remote}", flush=True)
            except OSError as ex:
                print(f"  WARN purge failed {remote}: {ex}", flush=True)
    if removed:
        print(f"OK purged {removed} stale first-party jar(s)", flush=True)
    else:
        print("OK first-party jars: no stale remotes", flush=True)


def upload_tree(local_root: Path) -> None:
    import paramiko

    if not PASSWORD:
        sys.exit(
            "Set AQUATECH_SFTP_PASS or put sftp_pass in .apex_deploy.json "
            f"(see {DEPLOY_SECRETS.name})"
        )

    transport = paramiko.Transport((HOST, PORT))
    transport.connect(username=USER, password=PASSWORD)
    sftp = paramiko.SFTPClient.from_transport(transport)
    assert sftp is not None

    uploaded = 0
    skipped = 0

    def ensure_remote_dir(remote: str) -> None:
        parts = remote.strip("/").split("/")
        cur = ""
        for part in parts:
            cur = f"{cur}/{part}" if cur else part
            try:
                sftp.stat(cur)
            except OSError:
                sftp.mkdir(cur)

    for local in sorted(local_root.rglob("*")):
        rel = local.relative_to(local_root).as_posix()
        remote = rel
        if local.is_dir():
            ensure_remote_dir(remote)
            continue
        ensure_remote_dir(str(Path(rel).parent.as_posix()) if "/" in rel else ".")
        try:
            st = sftp.stat(remote)
            if st.st_size == local.stat().st_size:
                skipped += 1
                continue
        except OSError:
            pass
        sftp.put(str(local), remote)
        uploaded += 1
        if uploaded % 25 == 0:
            print(f"  uploaded {uploaded} (skip {skipped}) last={rel}", flush=True)

    print(f"OK SFTP upload: {uploaded} new/changed, {skipped} unchanged", flush=True)
    purge_stale_first_party(sftp)
    sftp.close()
    transport.close()


def _apex_headers() -> dict[str, str]:
    if not APEX_API_KEY:
        sys.exit(
            "Set AQUATECH_APEX_API_KEY or apex_api_key in .apex_deploy.json "
            "(panel -> Account -> API Credentials)"
        )
    return {
        "Authorization": f"Bearer {APEX_API_KEY}",
        "Accept": "Application/vnd.pterodactyl.v1+json",
        "Content-Type": "application/json",
    }


def apex_json(method: str, path: str, body: dict | None = None, timeout: int = 45) -> dict:
    url = f"{APEX_PANEL}{path}"
    data = None if body is None else json.dumps(body).encode("utf-8")
    req = urllib.request.Request(url, data=data, headers=_apex_headers(), method=method)
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            raw = resp.read()
    except urllib.error.HTTPError as ex:
        err = ex.read().decode("utf-8", "replace")
        raise SystemExit(f"Apex {method} {path} HTTP {ex.code}: {err[:500]}") from ex
    if not raw:
        return {}
    return json.loads(raw)


def apex_server_state() -> str:
    data = apex_json("GET", f"/api/client/servers/{APEX_SERVER_ID}/resources", timeout=20)
    return str((data.get("attributes") or {}).get("current_state") or "unknown")


def apex_command(cmd: str) -> None:
    apex_json(
        "POST",
        f"/api/client/servers/{APEX_SERVER_ID}/command",
        {"command": cmd},
        timeout=30,
    )
    print(f"OK console: {cmd}", flush=True)


def apex_list_backups() -> list[dict]:
    listed = apex_json("GET", f"/api/client/servers/{APEX_SERVER_ID}/backups", timeout=30)
    out: list[dict] = []
    for item in listed.get("data") or []:
        a = item.get("attributes") or {}
        if a:
            out.append(a)
    return out


def apex_delete_backup(uuid: str) -> None:
    apex_json(
        "DELETE",
        f"/api/client/servers/{APEX_SERVER_ID}/backups/{uuid}",
        timeout=60,
    )
    print(f"OK deleted panel backup uuid={uuid}", flush=True)


def apex_rotate_oldest_unlocked_backup() -> bool:
    """Free a slot on hosts with backup_limit=1. Returns True if something deleted."""
    rows = apex_list_backups()
    unlocked = [a for a in rows if a.get("uuid") and not a.get("is_locked")]
    if not unlocked:
        print("WARN no unlocked panel backup to rotate", flush=True)
        return False
    unlocked.sort(key=lambda a: str(a.get("created_at") or ""))
    victim = unlocked[0]
    apex_delete_backup(str(victim["uuid"]))
    return True


def apex_create_backup(
    name: str,
    *,
    wait_sec: int = 180,
    require: bool = False,
) -> str | None:
    """Create panel backup before full overwrite. Rotates if backup limit hit."""
    created: dict = {}
    for attempt in range(2):
        url = f"{APEX_PANEL}/api/client/servers/{APEX_SERVER_ID}/backups"
        body = json.dumps({"name": name, "ignored": ""}).encode("utf-8")
        req = urllib.request.Request(url, data=body, headers=_apex_headers(), method="POST")
        try:
            with urllib.request.urlopen(req, timeout=60) as resp:
                raw = resp.read()
            created = json.loads(raw) if raw else {}
            break
        except urllib.error.HTTPError as ex:
            err = ex.read().decode("utf-8", "replace")
            if ex.code == 400 and "TooManyBackups" in err and attempt == 0:
                print("panel backup limit hit — rotating oldest unlocked...", flush=True)
                if not apex_rotate_oldest_unlocked_backup():
                    raise SystemExit(f"Apex backup POST HTTP {ex.code}: {err[:500]}") from ex
                time.sleep(2)
                continue
            raise SystemExit(f"Apex backup POST HTTP {ex.code}: {err[:500]}") from ex

    attrs = created.get("attributes") or created
    uuid = str(attrs.get("uuid") or "")
    print(f"OK panel backup requested name={name!r} uuid={uuid or '?'}", flush=True)
    if wait_sec <= 0:
        return uuid or None

    deadline = time.time() + wait_sec
    while time.time() < deadline:
        for a in apex_list_backups():
            if uuid and a.get("uuid") != uuid:
                continue
            if not uuid and a.get("name") != name:
                continue
            # Panels often report is_successful=false until completed_at is set.
            if a.get("is_successful") is True:
                print(
                    f"OK backup ready bytes={a.get('bytes')} uuid={a.get('uuid')}",
                    flush=True,
                )
                return str(a.get("uuid") or uuid or "")
            if a.get("completed_at") and a.get("is_successful") is False:
                msg = f"FAIL panel backup failed uuid={a.get('uuid')}"
                if require:
                    raise SystemExit(msg)
                print(msg, flush=True)
                return str(a.get("uuid") or uuid or "")
        time.sleep(5)
        print("  waiting for panel backup...", flush=True)

    msg = f"WARN backup not finished in {wait_sec}s — continuing"
    if require:
        raise SystemExit(f"FAIL backup not finished in {wait_sec}s")
    print(msg, flush=True)
    return uuid or None


def apex_send_command(command: str) -> bool:
    url = f"{APEX_PANEL}/api/client/servers/{APEX_SERVER_ID}/command"
    body = json.dumps({"command": command}).encode("utf-8")
    req = urllib.request.Request(url, data=body, headers=_apex_headers(), method="POST")
    try:
        with urllib.request.urlopen(req, timeout=15) as resp:
            return resp.status in (200, 204)
    except Exception:
        return False


def apex_power(signal: str) -> None:
    url = f"{APEX_PANEL}/api/client/servers/{APEX_SERVER_ID}/power"
    body = json.dumps({"signal": signal}).encode("utf-8")
    req = urllib.request.Request(url, data=body, headers=_apex_headers(), method="POST")
    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            code = resp.status
    except urllib.error.HTTPError as ex:
        err = ex.read().decode("utf-8", "replace")
        raise SystemExit(f"Apex power {signal} failed HTTP {ex.code}: {err[:400]}") from ex
    print(f"OK panel power '{signal}' (HTTP {code})", flush=True)


def apex_restart_or_start(wait: bool = True, timeout_sec: int = 600) -> None:
    state = apex_server_state()
    print(f"panel state before: {state}", flush=True)
    if state == "running":
        print("Flushing world chunks to disk (save-all)...", flush=True)
        apex_send_command("save-all")
        time.sleep(3)
    signal = "restart" if state == "running" else "start"
    apex_power(signal)

    if not wait:
        return

    deadline = time.time() + timeout_sec
    seen_offline = False
    while time.time() < deadline:
        time.sleep(5)
        state = apex_server_state()
        print(f"  panel state: {state}", flush=True)
        if signal == "restart":
            if state in ("offline", "stopping", "starting"):
                seen_offline = True
            if seen_offline and state == "running":
                print("OK server running after restart", flush=True)
                return
        elif state == "running":
            print("OK server started", flush=True)
            return
    print("WARN: server not confirmed running within timeout — check Apex console", flush=True)


def main() -> int:
    load_deploy_secrets()
    parser = argparse.ArgumentParser(description="Deploy AquaTech server to ApexNodes")
    parser.add_argument("--restart-only", action="store_true", help="Skip SFTP, only restart/start panel server")
    parser.add_argument("--no-restart", action="store_true", help="Upload only, do not touch panel power")
    parser.add_argument("--no-wait", action="store_true", help="Send power signal but do not wait for running")
    parser.add_argument("--fast", action="store_true", help="Fast hot-deploy (skip panel backup)")
    parser.add_argument(
        "--skip-backup",
        action="store_true",
        help="Skip panel backup before full SFTP (also AQUATECH_SKIP_BACKUP=1)",
    )
    parser.add_argument(
        "--require-backup",
        action="store_true",
        help="Fail deploy if panel backup does not finish in time",
    )
    parser.add_argument(
        "--backup-wait",
        type=int,
        default=180,
        help="Seconds to wait for panel backup (default 180; 0 = fire-and-forget)",
    )
    parser.add_argument(
        "--include-world",
        action="store_true",
        help="Include world/ region files in SFTP upload",
    )
    parser.add_argument(
        "--only",
        type=str,
        default="",
        help="Comma-separated relative paths to deploy, e.g. mods or kubejs,config",
    )
    args = parser.parse_args()

    if args.only:
        os.environ["AQUATECH_SFTP_ONLY"] = args.only
    if args.include_world:
        os.environ["AQUATECH_INCLUDE_WORLD"] = "1"

    if args.restart_only:
        apex_restart_or_start(wait=not args.no_wait)
        return 0

    if not SERVER.is_dir():
        print(f"missing {SERVER}", file=sys.stderr)
        return 1

    only = _only_prefixes()
    skip_backup = (
        args.fast
        or args.skip_backup
        or os.environ.get("AQUATECH_SKIP_BACKUP", "").strip() in ("1", "true", "yes")
        or bool(only)
    )
    if not skip_backup:
        stamp = time.strftime("%Y%m%d-%H%M%S")
        print(f"Creating panel backup before full SFTP ({stamp})...", flush=True)
        apex_create_backup(
            f"pre-deploy-{stamp}",
            wait_sec=max(0, args.backup_wait),
            require=args.require_backup,
        )
    elif only:
        print("SFTP_ONLY set — skip panel backup", flush=True)

    stage = stage_tree()
    try:
        upload_tree(stage)
    finally:
        shutil.rmtree(stage, ignore_errors=True)

    if not args.no_restart:
        print()
        print("Restarting Mohist via ApexNodes API…", flush=True)
        apex_restart_or_start(wait=not args.no_wait)
    else:
        print()
        print("Skipped panel restart (--no-restart)")

    print(f"Address: {HOST}:{SERVER_PORT}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
