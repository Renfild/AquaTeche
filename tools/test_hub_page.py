"""Проверка страницы меню F4 (hub.html) в headless-Chromium без игры.

    python tools/test_hub_page.py

Нужен Playwright: pip install playwright && python -m playwright install chromium.
Проверяет окно торговца рыбой (одна вкладка «Рыбалка», серые кнопки без торговца), обычное F4 (вкладки «Рыбалка» нет) и вкладку кейсов (все одиннадцать карточек с иконками).
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

        # --- кейсы из настоящего config/aqualumen/cases.json: все десять карточек с загруженными иконками ----------
        cases = json.loads((ROOT / "config" / "aqualumen" / "cases.json").read_text(encoding="utf-8"))["cases"]
        case_payload = payload("cases", tabs)
        case_payload["snapshot"]["cases"] = [
            {"id": c["id"], "title": c["title"], "rarity": c["rarity"], "cost": c["costCoins"], "count": 0,
             "pityEvery": c["pityEvery"], "loot": []} for c in cases]
        page.goto(HUB.as_uri())
        page.wait_for_function("typeof (window.AquaLumen && window.AquaLumen.applySnapshot) === 'function'")
        page.evaluate("p => window.AquaLumen.applySnapshot(p)", case_payload)
        page.wait_for_selector(".card.case")
        page.wait_for_function("[...document.querySelectorAll('.card.case .case-card-img')].every(i => i.complete && i.naturalWidth > 0)", timeout=15000)
        ok &= check("кейсы: показаны все одиннадцать", page.locator(".card.case").count() == len(cases) == 11,
                    str(page.locator(".card.case").count()))
        broken = page.evaluate("[...document.querySelectorAll('.card.case .case-card-img')].filter(i => !(i.complete && i.naturalWidth > 0)).length")
        ok &= check("кейсы: иконки у всех карточек", broken == 0, f"без иконки: {broken}")
        page.screenshot(path=str(SHOTS / "hub_test_cases.png"))

        # --- рулетка: иконка выпавшего предмета берётся по id из состава кейса (сервер присылает только название) ---
        steam = next(c for c in cases if c["id"] == "steam")
        speed = next(e for e in steam["loot"] if e["item"] == "aquatech_machines:speed_upgrade")
        spin_payload = payload("cases", tabs)
        spin_payload["snapshot"]["wallet"]["coins"] = 99999999
        spin_payload["snapshot"]["cases"] = [
            {"id": c["id"], "title": c["title"], "rarity": c["rarity"], "cost": c["costCoins"], "count": 0, "pityEvery": c["pityEvery"],
             "loot": [{"item": e.get("item", ""), "label": e["label"], "type": e["type"], "weight": e["weight"], "rarity": "epic",
                       "min": e["min"], "max": e["max"]} for e in c["loot"]]} for c in cases]
        page.goto(HUB.as_uri())
        page.wait_for_function("typeof (window.AquaLumen && window.AquaLumen.applySnapshot) === 'function'")
        page.evaluate("p => window.AquaLumen.applySnapshot(p)", spin_payload)
        page.wait_for_selector(".open-case")
        page.locator(".open-case").nth(2).click()
        page.wait_for_selector("#caseLayer.open")
        ok &= check("рулетка: хаб под окном скрыт", page.evaluate("getComputedStyle(document.getElementById('hub')).visibility") == "hidden")
        result_payload = json.loads(json.dumps(spin_payload))
        result_payload["snapshot"]["caseResult"] = {"caseId": "steam", "label": speed["label"], "rarity": "epic", "amount": 4, "type": "item"}
        page.evaluate("p => window.AquaLumen.applySnapshot(p)", result_payload)
        page.wait_for_selector("#caseReveal img.mc-icon-lg", timeout=15000)
        icon = page.evaluate("document.querySelector('#caseReveal img.mc-icon-lg').src.slice(0, 22)")
        ok &= check("рулетка: у выпавшего предмета своя иконка, а не сундук", icon == "data:image/png;base64,", icon)
        page.screenshot(path=str(SHOTS / "hub_test_reveal.png"))

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
