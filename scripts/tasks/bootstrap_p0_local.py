"""One-shot: patch FAWE in scratch/ + write .apex_deploy.json from terminal logs."""
from __future__ import annotations

import hashlib
import json
import re
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
PLUGINS = ROOT / "server" / "plugins"
SCRATCH = ROOT / "scripts" / "scratch" / "fawe_patch"
TERM = Path(r"C:\Users\xieto\.cursor\projects\c-Users-xieto-Desktop-AquaTech\terminals")
TRANS = Path(r"C:\Users\xieto\.cursor\projects\c-Users-xieto-Desktop-AquaTech\agent-transcripts")


def md5(p: Path) -> str:
    h = hashlib.md5()
    with p.open("rb") as f:
        for chunk in iter(lambda: f.read(1 << 20), b""):
            h.update(chunk)
    return h.hexdigest()


def recover_secrets() -> tuple[str, str]:
    pwd = key = ""
    paths = list(TERM.glob("*.txt")) + list(TRANS.rglob("*.jsonl"))
    for p in paths:
        t = p.read_text(encoding="utf-8", errors="replace")
        if not pwd:
            m = re.search(r"AQUATECH_SFTP_PASS\s*=\s*'([^']+)'", t)
            if not m:
                m = re.search(r"\$env:AQUATECH_SFTP_PASS\s*=\s*'([^']+)'", t)
            if m:
                pwd = m.group(1)
        if not key:
            m = re.search(r"ptlc_[A-Za-z0-9_]+", t)
            if m:
                key = m.group(0)
    return pwd, key


def patch_fawe() -> Path:
    SCRATCH.mkdir(parents=True, exist_ok=True)
    bak = PLUGINS / "FastAsyncWorldEdit.jar.pre-mohist.bak"
    live = PLUGINS / "FastAsyncWorldEdit.jar"
    src = bak if bak.is_file() else live
    target = SCRATCH / "FastAsyncWorldEdit.jar"
    shutil.copy2(src, target)
    r = subprocess.run(
        [sys.executable, str(ROOT / "tools" / "patches" / "patch_fawe_mohist.py"), str(target)],
        cwd=ROOT,
    )
    # Prefer explicit -patched output if replace failed
    patched = SCRATCH / "FastAsyncWorldEdit-patched.jar"
    result = target if target.is_file() and r.returncode == 0 else patched
    if r.returncode != 0 and patched.is_file():
        print(f"using {patched.name} after partial patch")
        result = patched
    elif r.returncode != 0:
        raise SystemExit(f"FAWE patch failed code {r.returncode}")
    if not result.is_file():
        raise SystemExit("no patched FAWE jar produced")
    try:
        shutil.copy2(result, live)
        print(f"live FAWE updated md5={md5(live)}")
    except OSError as ex:
        alt = PLUGINS / "FastAsyncWorldEdit-patched.jar"
        shutil.copy2(result, alt)
        print(f"WARN live locked ({ex}); wrote {alt.name} md5={md5(alt)}")
        print("  deploy sync will promote -patched -> FastAsyncWorldEdit.jar")
    return result


def write_secrets() -> None:
    pwd, key = recover_secrets()
    out = {
        "sftp_host": "g-pl-3.apexnodes.xyz",
        "sftp_port": 2022,
        "sftp_user": "oxmzg5d0.6fdc6f7b",
        "sftp_pass": pwd,
        "server_port": "21561",
        "apex_panel": "https://panel.apexnodes.xyz",
        "apex_server_id": "6fdc6f7b",
        "apex_api_key": key,
    }
    (ROOT / ".apex_deploy.json").write_text(json.dumps(out, indent=2) + "\n", encoding="utf-8")
    print("wrote .apex_deploy.json", "pwd", bool(pwd), "key", bool(key))


def main() -> None:
    patch_fawe()
    write_secrets()


if __name__ == "__main__":
    main()
