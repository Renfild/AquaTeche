#!/usr/bin/env python3
import os
import sys
import time
from pathlib import Path
from playwright.sync_api import sync_playwright

ROOT = Path(__file__).resolve().parent.parent.parent
TOKEN_FILE = ROOT / ".cf_token"


def main():
    print("Starting Playwright script...")
    with sync_playwright() as p:
        try:
            browser = p.chromium.launch(
                channel="chrome", headless=False, args=["--start-maximized"]
            )
        except Exception:
            browser = p.chromium.launch(
                channel="msedge", headless=False, args=["--start-maximized"]
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
            if tmpl.is_visible(timeout=6000):
                print("Clicking template...")
                tmpl.click()
                page.wait_for_timeout(2000)

            summary = page.locator('button:has-text("Continue to summary")').first
            if summary.is_visible(timeout=6000):
                print("Clicking summary...")
                summary.click()
                page.wait_for_timeout(2000)

            create_final = page.locator('button:has-text("Create Token")').first
            if create_final.is_visible(timeout=6000):
                print("Clicking create final...")
                create_final.click()
                page.wait_for_timeout(4000)
        except Exception as e:
            print("Click step exception:", e)

        print("Scanning page elements...")
        extracted = None
        for i in range(20):
            inputs = page.query_selector_all("input, textarea, code")
            for inp in inputs:
                try:
                    val = (
                        inp.input_value()
                        if hasattr(inp, "input_value")
                        else inp.text_content()
                    ) or ""
                    val = val.strip()
                    if (
                        len(val) >= 35
                        and len(val) <= 50
                        and val != "TESTING_PERSISTENCE"
                        and "http" not in val
                        and "Cloudflare" not in val
                    ):
                        extracted = val
                        break
                except Exception:
                    pass
            if extracted:
                break
            page.wait_for_timeout(1500)

        if extracted:
            TOKEN_FILE.write_text(extracted.strip(), encoding="utf-8")
            print("REAL_TOKEN_FOUND_AND_SAVED")
        else:
            print("EXTRACTION_FAILED_NO_TOKEN")

        browser.close()


if __name__ == "__main__":
    main()
