"""Local smoke tests for the LEGACY Python launcher (tools/aquatech_launcher.py).

Prefer C# coverage:
  dotnet test launcher/src/AquaTechLauncher.Core.Tests
  python tools/smoke_portal_and_versions.py

  python tools/test_launcher_smoke.py
"""
from __future__ import annotations

import json
import shutil
import subprocess
import sys
import time
import urllib.request
from pathlib import Path

print("NOTE: test_launcher_smoke.py targets legacy aquatech_launcher.py — C# is the shipped client.")

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))

import aquatech_launcher as L  # noqa: E402


def _ok(name: str):
    print(f"  PASS  {name}")


def _fail(name: str, detail: str = ""):
    print(f"  FAIL  {name}" + (f" — {detail}" if detail else ""))
    raise SystemExit(1)


def test_srg_extract(tmp: Path):
    print("[1] Forge SRG extract from bundled runtime")
    g = tmp / "srg"
    if g.exists():
        shutil.rmtree(g, ignore_errors=True)
    g.mkdir(parents=True)
    L.ensure_forge_minecraft_srg(g, log=print)
    srg = (
        g / "libraries" / "net" / "minecraft" / "client"
        / f"{L.MC_VER}-{L.MCP_VER}"
        / f"client-{L.MC_VER}-{L.MCP_VER}-srg.jar"
    )
    if not srg.exists() or srg.stat().st_size < 1_000_000:
        _fail("srg jar missing/small")
    _ok(f"srg {srg.stat().st_size} bytes")


def test_forge_launch(tmp: Path, java: str):
    print("[2] Clean Forge install + launch to menu")
    g = tmp / "forge"
    if g.exists():
        shutil.rmtree(g, ignore_errors=True)
    g.mkdir(parents=True)
    for s in ("mods", "config", "logs", "versions", "libraries", "assets"):
        (g / s).mkdir()

    if not L.install_forge_fast(g, log=lambda m: print("   ", m)):
        _fail("install_forge_fast")
    cmd = L.build_launch_cmd(g, "SmokeTest", 4096, java, log=lambda m: None)
    if cmd[0].lower().endswith("javaw.exe"):
        cmd[0] = str(Path(cmd[0]).with_name("java.exe"))

    errp = g / "logs" / "smoke.log"
    with open(errp, "wb") as err:
        proc = subprocess.Popen(
            cmd, cwd=str(g), stdout=err, stderr=err, stdin=subprocess.DEVNULL
        )
        ok = False
        for _ in range(90):  # up to 45s
            time.sleep(0.5)
            if proc.poll() is not None:
                text = errp.read_text(encoding="utf-8", errors="replace")
                _fail("process exited early", f"code={proc.returncode}\n{text[-1200:]}")
            # reopen read — child holds write handle
            try:
                text = errp.read_bytes().decode("utf-8", errors="replace")
            except OSError:
                continue
            if "ClassNotFoundException: net.minecraft.client.gui.screens.Overlay" in text:
                proc.kill()
                _fail("Overlay ClassNotFound")
            if "Setting user" in text and "Forge mod loading" in text:
                ok = True
                break
        try:
            proc.kill()
            proc.wait(timeout=5)
        except Exception:
            pass
    if not ok:
        text = errp.read_text(encoding="utf-8", errors="replace") if errp.exists() else ""
        _fail("did not reach menu", text[-1500:])
    _ok("Setting user + Forge mod loading")


def test_sync(tmp: Path, sync_base: str):
    print(f"[3] Sync hash + delete extras via {sync_base}")
    manifest = json.loads(
        urllib.request.urlopen(f"{sync_base}/manifest.json", timeout=15).read()
    )
    files = manifest.get("files") or []
    if len(files) < 5:
        _fail("manifest too small", str(len(files)))

    sample = {
        "version": "smoke",
        "files": [f for f in files if f["path"].startswith("mods/")][:10]
        + [f for f in files if f["path"].startswith("kubejs/") and int(f.get("size") or 0) < 80_000][:6],
    }
    g = tmp / "sync"
    if g.exists():
        shutil.rmtree(g, ignore_errors=True)
    g.mkdir(parents=True)
    (g / "mods").mkdir()
    (g / "config").mkdir()
    junk = g / "mods" / "ZZZ_smoke_junk.jar"
    junk.write_bytes(b"junk")
    keep = g / "options.txt"
    keep.write_text("fov:1", encoding="utf-8")

    def dl(url, dest: Path):
        dest.parent.mkdir(parents=True, exist_ok=True)
        req = urllib.request.Request(url, headers={"User-Agent": "AquaTechSmoke"})
        with urllib.request.urlopen(req, timeout=120) as r, open(dest, "wb") as f:
            shutil.copyfileobj(r, f)

    upd, fail, deleted = L.apply_manifest_sync(
        g,
        sample,
        source="cdn",
        base=sync_base,
        verify_hash=True,
        download_url=dl,
        log=lambda m: print("   ", m),
    )
    if fail:
        _fail("sync downloads failed", f"fail={fail}")
    if junk.exists():
        _fail("junk mod not deleted")
    if not keep.exists():
        _fail("options.txt was deleted")
    for item in sample["files"]:
        p = g / item["path"]
        if not p.exists():
            _fail("missing after sync", item["path"])
        if L.md5_file(p).lower() != item["md5"].lower():
            _fail("md5 mismatch", item["path"])

    # warm
    upd2, fail2, _ = L.apply_manifest_sync(
        g, sample, source="cdn", base=sync_base, verify_hash=False, download_url=dl, log=None
    )
    if upd2 != 0 or fail2 != 0:
        _fail("warm sync should be no-op", f"{upd2}/{fail2}")

    # repair
    victim = sample["files"][0]
    vp = g / victim["path"]
    vp.write_bytes(b"corrupted")
    L.apply_manifest_sync(
        g, sample, source="cdn", base=sync_base, verify_hash=True, download_url=dl, log=None
    )
    if L.md5_file(vp).lower() != victim["md5"].lower():
        _fail("corrupt file not repaired")

    _ok(f"updated={upd} deleted={deleted} warm+repair ok")


def find_java() -> str:
    j = L.find_java()
    if j:
        return j
    for p in (Path.home() / "AppData" / "Roaming" / "AquaTech" / "_java17").rglob("java.exe"):
        return str(p)
    _fail("Java 17 not found")
    return ""


def find_sync_base() -> str:
    candidates = [
        "http://127.0.0.1:8765",
        "http://127.0.0.1:8766",
        "http://127.0.0.1:18080",
        "http://127.0.0.1:28080",
        "http://127.0.0.1:8080",
    ]
    for base in candidates:
        try:
            req = urllib.request.Request(
                f"{base}/manifest.json",
                headers={"User-Agent": "AquaTechSmoke"},
            )
            with urllib.request.urlopen(req, timeout=3) as r:
                data = json.loads(r.read())
            if data.get("files"):
                return base
        except Exception:
            continue
    return ""


def main():
    print(f"AquaTech launcher smoke — {L.LAUNCHER_VER}")
    tmp = Path(r"C:\Users\xieto\AppData\Local\Temp\AquaTechSmoke")
    tmp.mkdir(parents=True, exist_ok=True)
    java = find_java()
    print(f"Java: {java}")

    test_srg_extract(tmp)
    test_forge_launch(tmp, java)

    sync = find_sync_base()
    if not sync:
        print("[3] Sync skipped — start sync server first:")
        print("    python tools/start_sync_server.py")
        print("  (then re-run this test)")
        print()
        print("PARTIAL PASS (Forge OK, sync not running)")
        return
    test_sync(tmp, sync)
    print()
    print("ALL PASS")


if __name__ == "__main__":
    main()
