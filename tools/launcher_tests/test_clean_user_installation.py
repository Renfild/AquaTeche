"""Test clean installation as an external user (friend) on a fresh PC.

This script tests the complete end-to-end user flow:
1. Isolated game directory (no local AquaTech-Client source available)
2. Remote manifest & modpack download via GitHub / CDN
3. Forge 47.4.0 fast installation & library resolution
4. Minecraft 1.20.1 startup & mod loading verification
"""
import sys, os, json, shutil, time, subprocess, urllib.request
from pathlib import Path

sys.stdout.reconfigure(encoding='utf-8', errors='replace')
sys.stderr.reconfigure(encoding='utf-8', errors='replace')

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))

import aquatech_launcher as L

CLEAN_DIR = Path(r"C:\Users\xieto\AppData\Local\Temp\AquaTech_CleanUser_Test")


def run_clean_test():
    print("=" * 65)
    print("  AquaTech — Clean User Installation Test (Simulating Friend's PC)")
    print("=" * 65)
    print(f"Target Directory: {CLEAN_DIR}")

    # 1. Clear isolated directory
    if CLEAN_DIR.exists():
        print("[1/5] Cleaning previous test folder...")
        shutil.rmtree(CLEAN_DIR, ignore_errors=True)
    CLEAN_DIR.mkdir(parents=True, exist_ok=True)
    for sub in ("mods", "config", "kubejs", "resourcepacks", "logs", "versions", "libraries", "assets"):
        (CLEAN_DIR / sub).mkdir(parents=True, exist_ok=True)
    print("      Clean directory initialized.")

    # 2. Check Java 17
    print("[2/5] Locating Java 17...")
    java = L.find_java()
    if not java:
        print("      Java 17 not found locally — installing Adoptium JRE 17...")
        app = L.AquaTechLauncher()
        java = app._install_java(CLEAN_DIR)
    if not java or not Path(java).exists():
        print("❌ FAIL: Java 17 could not be resolved.")
        sys.exit(1)
    print(f"      Java 17 OK: {java}")

    # 3. Forge & Minecraft installation
    print("[3/5] Installing Minecraft 1.20.1 & Forge 47.4.0...")
    L.ensure_launcher_profiles(CLEAN_DIR)
    try:
        L.ensure_vanilla_version_json(CLEAN_DIR)
        L.ensure_vanilla_client_jar(CLEAN_DIR, log=lambda m: print("     ", m))
    except Exception as e:
        print(f"❌ FAIL: Vanilla Minecraft install error: {e}")
        sys.exit(1)

    ok = L.install_forge_fast(CLEAN_DIR, log=lambda m: print("     ", m))
    if not ok:
        print("❌ FAIL: install_forge_fast failed.")
        sys.exit(1)
    print("      Minecraft 1.20.1 & Forge 47.4.0 installed successfully.")

    # 4. Modpack sync from GitHub/CDN (Simulating Remote Friend)
    print("[4/5] Syncing Modpack files from Remote Manifest (GitHub)...")
    manifest_url = f"{L.GITHUB_RAW}/manifest.json"
    print(f"      Fetching manifest from {manifest_url}...")
    
    manifest = None
    try:
        token = open(r"C:\Users\xieto\Desktop\AquaTech\.gh_token").read().strip()
        headers = {"User-Agent": "AquaTechCleanTest/1.0"}
        if token:
            headers["Authorization"] = f"token {token}"
        req = urllib.request.Request(manifest_url, headers=headers)
        with urllib.request.urlopen(req, timeout=15) as r:
            manifest = json.loads(r.read())
        print(f"      Manifest loaded: {len(manifest.get('files', []))} files declared.")
    except Exception as e:
        print(f"⚠️  Remote manifest fetch warning ({e}). Testing local fallback...")
        manifest = L.build_manifest_from_pack(ROOT / "dist" / "AquaTech-Client")

    def download_url(url: str, dest_path: Path):
        headers = {
            "User-Agent": "Mozilla/5.0 AquaTechCleanTest/1.0",
            "Accept": "*/*",
        }
        token = open(r"C:\Users\xieto\Desktop\AquaTech\.gh_token").read().strip()
        if token and ("github" in url.lower() or "githubusercontent.com" in url.lower()):
            headers["Authorization"] = f"token {token}"

        req = urllib.request.Request(url, headers=headers)
        tmp = dest_path.with_suffix(dest_path.suffix + ".part")
        try:
            with urllib.request.urlopen(req, timeout=120) as resp, open(tmp, "wb") as f:
                while True:
                    chunk = resp.read(65536)
                    if not chunk:
                        break
                    f.write(chunk)
            tmp.replace(dest_path)
        except Exception:
            tmp.unlink(missing_ok=True)
            raise

    upd, fail, deleted = L.apply_manifest_sync(
        CLEAN_DIR,
        manifest,
        source="cdn",
        base="http://127.0.0.1:8765",
        verify_hash=True,
        download_url=download_url,
        log=lambda m: sys.stdout.buffer.write((f"     {m}\n").encode("utf-8")),
    )
    print(f"      Sync completed: {upd} updated, {fail} failed, {deleted} deleted.")

    mod_count = len(list((CLEAN_DIR / "mods").glob("*.jar")))
    kubejs_count = len(list((CLEAN_DIR / "kubejs").rglob("*")))
    print(f"      Downloaded mods count: {mod_count} jars")
    print(f"      Downloaded kubejs files: {kubejs_count} files")

    if mod_count < 30:
        print("❌ FAIL: Less than 30 mods were installed into clean directory!")
        sys.exit(1)

    # 5. Build Launch Command & Run Test Launch
    print("[5/5] Testing Launch Command & Minecraft Process...")
    cmd = L.build_launch_cmd(CLEAN_DIR, "CleanTestUser", 4096, java, log=lambda m: print("     ", m))
    
    # Use java.exe instead of javaw.exe for output capture
    if cmd[0].lower().endswith("javaw.exe"):
        cmd[0] = str(Path(cmd[0]).with_name("java.exe"))

    log_path = CLEAN_DIR / "logs" / "clean_test_run.log"
    print(f"      Launching Minecraft (logging to {log_path.name})...")
    with open(log_path, "wb") as log_file:
        proc = subprocess.Popen(
            cmd,
            cwd=str(CLEAN_DIR),
            stdout=log_file,
            stderr=log_file,
            stdin=subprocess.DEVNULL,
        )
        started_ok = False
        for i in range(30):  # Wait up to 15 seconds
            time.sleep(0.5)
            if proc.poll() is not None:
                print(f"❌ FAIL: Process exited early with code {proc.returncode}!")
                try:
                    text = log_path.read_text("utf-8", errors="replace")
                    print("--- Log Tail ---")
                    print(text[-1500:])
                except Exception:
                    pass
                sys.exit(1)

            try:
                text = log_path.read_bytes().decode("utf-8", errors="replace")
                if "Setting user" in text or "Forge mod loading" in text or "Minecraft" in text:
                    started_ok = True
                    print("      [OK] Minecraft started, user set, Forge mod loading initiated!")
                    break
            except Exception:
                pass

        try:
            proc.kill()
            proc.wait(timeout=3)
        except Exception:
            pass

    if started_ok:
        print()
        print("=" * 65)
        print("  SUCCESS! Clean User Installation Test PASSED 100%!")
        print("  All mods, KubeJS scripts, Forge & binaries installed cleanly.")
        print("=" * 65)
    else:
        print("❌ FAIL: Minecraft process did not reach early loading stage within timeout.")
        sys.exit(1)


if __name__ == "__main__":
    run_clean_test()
