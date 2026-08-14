#!/usr/bin/env python3
import os
import sys
import time
from pathlib import Path
from playwright.sync_api import sync_playwright

ROOT = Path(__file__).resolve().parent.parent.parent
TOKEN_FILE = ROOT / ".cf_token"
USER_DATA_DIR = Path(os.environ["LOCALAPPDATA"]) / "Google" / "Chrome" / "User Data"


def main():
    print("Launching Chrome with User Data Dir...")
    with sync_playwright() as p:
        try:
            context = p.chromium.launch_persistent_context(
                user_data_dir=str(USER_DATA_DIR),
                channel="chrome",
                headless=False,
                args=["--start-maximized"],
            )
        except Exception as e:
            print("Fallback launch without persistent context:", e)
            browser = p.chromium.launch(
                channel="chrome", headless=False, args=["--start-maximized"]
            )
            context = browser.new_context(viewport=None)

        page = context.new_page()
        page.goto(
            "https://dash.cloudflare.com/profile/api-tokens/create",
            wait_until="domcontentloaded",
        )
        page.wait_for_timeout(3000)

        try:
            tmpl = page.locator(
                'tr:has-text("Edit Cloudflare Workers") button, div:has-text("Edit Cloudflare Workers") button'
            ).first
            if tmpl.is_visible(timeout=5000):
                print("Clicking Use template...")
                tmpl.click()
                page.wait_for_timeout(2000)

            summary = page.locator('button:has-text("Continue to summary")').first
            if summary.is_visible(timeout=5000):
                print("Clicking Continue to summary...")
                summary.click()
                page.wait_for_timeout(2000)

            create_final = page.locator('button:has-text("Create Token")').first
            if create_final.is_visible(timeout=5000):
                print("Clicking final Create Token...")
                create_final.click()
                page.wait_for_timeout(3500)
        except Exception as e:
            print("Auto-click step exception:", e)

        print("Searching for generated 40-char token...")
        extracted = None
        for _ in range(25):
            try:
                candidates = page.evaluate("""
                    () => {
                        const els = Array.from(document.querySelectorAll('input, code, textarea, span'));
                        return els.map(e => (e.value || e.textContent || '').trim())
                                  .filter(s => s.length >= 38 && s.length <= 48 && /^[A-Za-z0-9_\\-]+$/.test(s) && !s.includes('http') && !s.includes('Cloudflare'));
                    }
                """)
                if candidates:
                    extracted = candidates[0]
                    break
            except Exception:
                pass
            page.wait_for_timeout(1500)

        if extracted:
            TOKEN_FILE.write_text(extracted.strip(), encoding="utf-8")
            print("REAL_TOKEN_PERSISTENT_SAVED")
        else:
            print("PERSISTENT_EXTRACTION_FAILED")

        context.close()


if __name__ == "__main__":
    main()
