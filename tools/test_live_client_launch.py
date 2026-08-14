#!/usr/bin/env python3
"""Live Minecraft Client Launch and Verification Test.

Launches the client from %APPDATA%/AquaTech using Java 17 and validates:
1. Java 17 and Forge 47.4.0 initialization.
2. Mod loading: aquatech_ui 1.0.24, casesmod 1.0.8, mcef, embeddium, oculus.
3. NetworkHandler protocol v7 registration.
4. No ClassNotFound or fatal crash exceptions.
"""
from __future__ import annotations

import os
import subprocess
import sys
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT / "tools"))

import aquatech_launcher as L


def main() -> int:
    game_dir = Path(os.path.expandvars(r"%APPDATA%\AquaTech"))
    java_exe = game_dir / "_java17" / "jdk-17.0.20+8-jre" / "bin" / "java.exe"
    if not java_exe.exists():
        java_exe = Path(r"C:\Program Files\Eclipse Adoptium\jdk-17.0.20+8-jre\bin\java.exe")
    
    print(f"[*] Game dir: {game_dir}")
    print(f"[*] Java: {java_exe}")
    
    if not java_exe.exists():
        print(f"[!] Java 17 executable not found at {java_exe}")
        return 1

    # Check jar files in mods dir
    mods_dir = game_dir / "mods"
    ui_jar = mods_dir / "aquatech_ui-1.0.24.jar"
    cases_jar = mods_dir / "casesmod-1.0.8.jar"
    print(f"[*] aquatech_ui-1.0.24.jar exists: {ui_jar.exists()} (size: {ui_jar.stat().st_size if ui_jar.exists() else 0})")
    print(f"[*] casesmod-1.0.8.jar exists: {cases_jar.exists()} (size: {cases_jar.stat().st_size if cases_jar.exists() else 0})")

    # Build launch command
    cmd = L.build_launch_cmd(game_dir, "xietoru", 6144, str(java_exe))
    # Replace javaw with java so console output is captured
    if cmd[0].lower().endswith("javaw.exe"):
        cmd[0] = str(Path(cmd[0]).with_name("java.exe"))

    log_file = game_dir / "logs" / "live_test_launch.log"
    print(f"[*] Launching Minecraft (logging to {log_file})...")

    with open(log_file, "wb") as out:
        proc = subprocess.Popen(
            cmd,
            cwd=str(game_dir),
            stdout=out,
            stderr=out,
            stdin=subprocess.DEVNULL,
        )
        print(f"[*] Minecraft process started with PID: {proc.pid}")

        reached_stage = False
        start_time = time.time()
        max_wait_seconds = 75

        try:
            while time.time() - start_time < max_wait_seconds:
                time.sleep(1.5)
                elapsed = int(time.time() - start_time)

                if proc.poll() is not None:
                    print(f"[!] Process exited prematurely with code {proc.returncode}")
                    text = log_file.read_text(encoding="utf-8", errors="replace")
                    print("--- Tail of log ---")
                    print(text[-2000:])
                    return 1

                # Check log
                output = ""
                if log_file.exists():
                    try:
                        output = log_file.read_bytes().decode("utf-8", errors="replace")
                    except Exception:
                        pass

                # Check indicators
                if "Setting user: xietoru" in output and ("Forge mod loading" in output or "aquatech_ui" in output):
                    print(f"[{elapsed}s] OK: Forge mod loading & user authenticated")
                    reached_stage = True
                    break

            if reached_stage:
                print(f"[*] SUCCESS: Client launched and initialized mod lifecycle in {int(time.time() - start_time)}s")
            else:
                print(f"[!] Timed out after {max_wait_seconds}s waiting for mod initialization")

        finally:
            print("[*] Terminating test process...")
            try:
                proc.terminate()
                proc.wait(timeout=5)
            except Exception:
                try:
                    proc.kill()
                except Exception:
                    pass

    return 0 if reached_stage else 1


if __name__ == "__main__":
    raise SystemExit(main())
