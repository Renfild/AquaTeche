#!/usr/bin/env python3
"""Cloudflare Pages & Cloudflare R2 Deployment Automation Tool for AquaTech."""
from __future__ import annotations

import os
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DIST_EXE = ROOT / "dist" / "releases" / "AquaTechLauncher.exe"
DESKTOP_EXE = Path.home() / "Desktop" / "AquaTechLauncher.exe"
ROOT_EXE = ROOT / "AquaTechLauncher.exe"


def prepare_web_assets():
    """Ensure AquaTechLauncher.exe is present in root directory for Cloudflare Pages / website download."""
    print("==================================================")
    print("  AquaTech Cloudflare Pages & R2 Deploy Tool")
    print("==================================================")
    
    src = None
    if DESKTOP_EXE.is_file():
        src = DESKTOP_EXE
    elif DIST_EXE.is_file():
        src = DIST_EXE

    if src:
        shutil.copy2(src, ROOT_EXE)
        mb = round(ROOT_EXE.stat().st_size / 1024 / 1024, 2)
        print(f"[OK] AquaTechLauncher.exe ({mb} MB) copied to web root for direct download.")
    else:
        print("[WARN] AquaTechLauncher.exe not found! Build launcher first using PyInstaller.")


def deploy_pages_wrangler():
    """Deploy static portal (index.html, AquaTechLauncher.exe) to Cloudflare Pages via wrangler CLI."""
    print("\n[1/2] Deploying Website to Cloudflare Pages...")
    try:
        cmd = ["npx", "-y", "wrangler", "pages", "deploy", ".", "--project-name", "aquatech-portal", "--commit-dirty=true"]
        print(f"Running: {' '.join(cmd)}")
        res = subprocess.run(cmd, cwd=str(ROOT), capture_output=True, text=True, timeout=120)
        if res.returncode == 0:
            print("[OK] Cloudflare Pages deployment successful!")
            print(res.stdout)
        else:
            print("[INFO] Wrangler deploy notice:")
            print(res.stderr or res.stdout)
            print("Tip: Run 'npx wrangler login' in terminal to authenticate Cloudflare account once.")
    except Exception as ex:
        print(f"[WARN] Wrangler CLI check: {ex}")


def show_instructions():
    """Print 1-click Cloudflare Dashboard instructions for the user."""
    print("\n==================================================")
    print("  Cloudflare Pages Deployment Summary")
    print("==================================================")
    print("Website files ready in project root:")
    print("  • index.html (LoliLand-style Minecraft Portal)")
    print("  • AquaTechLauncher.exe (61.4 MB Standalone Launcher)")
    print("  • wrangler.toml & _routes.json")
    print("\nOption A: 1-Click GitHub Integration (Recommended)")
    print("  1. Go to https://dash.cloudflare.com/ -> Workers & Pages")
    print("  2. Click 'Create application' -> 'Pages' -> 'Connect to Git'")
    print("  3. Select repo 'Renfild/AquaTeche'")
    print("  4. Framework preset: None (Static HTML)")
    print("  5. Click 'Save and Deploy'")
    print("\nOption B: Direct Upload via Wrangler CLI")
    print("  Run in terminal: npx wrangler pages deploy . --project-name aquatech-portal")
    print("==================================================")


def main():
    prepare_web_assets()
    deploy_pages_wrangler()
    show_instructions()


if __name__ == "__main__":
    main()
