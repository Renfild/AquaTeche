"""Проверка страницы меню F4 (hub.html) в headless-Chromium без игры.

    python tools/test_hub_page.py

Нужен Playwright: pip install playwright && python -m playwright install chromium.
Проверяет окно торговца рыбой (одна вкладка «Рыбалка», серые кнопки без торговца) и обычное F4 (вкладки «Рыбалка» нет).
Скриншоты кладёт в mods/aquatech-machines/art/hub_test_*.png.
"""
import json
import sys
from pathlib import Path

from playwright.sync_api import sync_playwright

ROOT = Path(__file__).resolve().parent.parent
HUB = ROOT / "mods" / "aqualumen-ui" / "src" / "main" / "resources" / "assets" / "aqualumen" / "html" / "hub.html"
SHOTS = ROOT / "mods" / "aquatech-machines" / "art"


def payload(initial_tab, tabs):
    return {
        "snapshot": {
            "profile": {"name": "Tester", "rank": "Игрок", "rankColor": 9414328, "level": 3, "levelProgress": 0.4,
                        "playtimeMinutes": 90, "kills": 1, "deaths": 0, "quests": 2, "friendsOnline": 0},
            "wallet": {"coins": 12345, "gems": 7, "dailyStreak": 1, "dailyAvailable": False},
            "season": {"title": "Сезон 1", "tier": 1, "maxTier": 10, "tierProgress": 0.1, "premium": False,
                       "claimable": 0, "claimedTiers": [], "claimedPremiumTiers": []},
            "tops": [], "store": [], "cases": [], "kits": [], "warps": [], "quests": [], "market": [],
            "eventLine": "",
            "fishes": [
                {"id": "minecraft:cod", "name": "Треска", "count": 4, "priceCoins": 120, "rarity": "Обычная", "tag": "",
                 "demand": 1.0, "totalCoins": 480, "detail": "вес и свежесть"},
                {"id": "minecraft:salmon", "name": "Лосось", "count": 0, "priceCoins": 150, "rarity": "Обычная", "tag": "",
                 "demand": 1.0, "totalCoins": 0, "detail": ""},
            ],
            "server": {"name": "AquaTech", "online": 3, "slots": 100, "tps": 20.0, "build": "test"},
            "atlas": [], "atlasSummary": {"found": 0, "total": 0, "catches": 0, "recordName": "", "recordWeight": 0,
                                           "nextMilestone": 0, "nextReward": 0},
            "caseResult": None,
        },
        "receivedAt": 0,
        "initialTab": initial_tab,
        "openKey": "F4",
        "playerHead": "",
        "enabledTabs": tabs,
        "appearance": {"theme": "default", "accent": "#2fe0c0", "accentAlt": "#3b9dff", "animations": False,
                       "compact": False, "panelOpacity": 1.0},
    }


def check(name, condition, details=""):
    print(("PASS " if condition else "FAIL ") + name + (f"  [{details}]" if details and not condition else ""))
    return condition


def main():
    ok = True
    errors = []
    with sync_playwright() as p:
        browser = p.chromium.launch()
        page = browser.new_page(viewport={"width": 1280, "height": 760})
        page.on("pageerror", lambda e: errors.append(str(e)))
        page.goto(HUB.as_uri())
        page.wait_for_function("typeof (window.AquaLumen && window.AquaLumen.applySnapshot) === 'function'")

        # --- окно торговца: одна вкладка, торговца рядом нет ------------------------------------------------------
        page.evaluate("p => window.AquaLumen.applySnapshot(p)", payload("fishing", ["fishing"]))
        page.wait_for_selector(".sell-all-fish")
        ok &= check("торговец: открыта вкладка «Рыбалка», а не профиль", "скупщик" in page.inner_text("#content").lower())
        ok &= check("торговец: боковая панель скрыта", not page.is_visible(".sidebar"))
        ok &= check("торговец: плашка «Торговца рядом нет»", page.is_visible(".merchant-note"))
        ok &= check("торговец: кнопки серые", page.locator(".sell-all-fish.locked").count() == 1
                    and page.locator(".sell-single-fish.locked").count() == 2)
        page.screenshot(path=str(SHOTS / "hub_test_merchant_far.png"))

        page.evaluate("window.AquaLumen.setMerchantNear(true)")
        ok &= check("торговец рядом: плашки нет", not page.is_visible(".merchant-note"))
        ok &= check("торговец рядом: кнопки не серые", page.locator(".sell-all-fish.locked").count() == 0)
        page.screenshot(path=str(SHOTS / "hub_test_merchant_near.png"))

        # --- обычное F4: вкладки «Рыбалка» нет, открывается профиль ---------------------------------------------
        page.goto(HUB.as_uri())
        page.wait_for_function("typeof (window.AquaLumen && window.AquaLumen.applySnapshot) === 'function'")
        tabs = ["profile", "store", "cases", "pass", "events", "auction", "kits", "warps", "tops", "settings"]
        page.evaluate("p => window.AquaLumen.applySnapshot(p)", payload("profile", tabs))
        page.wait_for_selector(".nav-button")
        labels = page.locator(".nav-button").all_inner_texts()
        ok &= check("F4: нет вкладки «Рыбалка»", not any("Рыбал" in t for t in labels), str(labels))
        ok &= check("F4: боковая панель видна", page.is_visible(".sidebar"))
        page.screenshot(path=str(SHOTS / "hub_test_f4.png"))

        # --- вкладка из initialTab, которой нет в списке, заменяется первой доступной -----------------------------
        page.goto(HUB.as_uri())
        page.wait_for_function("typeof (window.AquaLumen && window.AquaLumen.applySnapshot) === 'function'")
        page.evaluate("p => window.AquaLumen.applySnapshot(p)", payload("fishing", tabs))
        ok &= check("F4: initialTab вне списка не даёт пустое окно", page.locator(".nav-button.active").count() == 1)
        browser.close()
    ok &= check("ошибок JS на странице нет", not errors, "; ".join(errors))
    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main())
