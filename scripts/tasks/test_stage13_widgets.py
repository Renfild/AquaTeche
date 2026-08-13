#!/usr/bin/env python3
"""Stage 1.3 contracts: glass/badge/slot/dialog widgets + screen wiring.

Usage:
  python scripts/tasks/test_stage13_widgets.py
"""
from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
UI = ROOT / "mods/aquatech-ui/src/main/java/net/aquatech/ui/client"
WIDGET = UI / "gui/widget"
GUI = UI / "gui"

FILES = {
    "button": WIDGET / "AquaButton.java",
    "glass": WIDGET / "AquaGlassPanel.java",
    "badge": WIDGET / "AquaBadge.java",
    "slot": WIDGET / "AquaCaseSlot.java",
    "dialog": GUI / "AquaDialogScreen.java",
    "blur": GUI / "AquaBlurredScreen.java",
    "tab_screen": UI / "tab/OceanTabScreen.java",
    "tab_overlay": UI / "tab/OceanTabOverlay.java",
    "skill": GUI / "OceanSkillTreeScreen.java",
    "hud": UI / "hud/OceanHudOverlay.java",
    "web": GUI / "AquaWebScreen.java",
}


def fail(msg: str) -> None:
    print(f"FAIL {msg}")
    raise SystemExit(1)


def read(key: str) -> str:
    return FILES[key].read_text(encoding="utf-8")


def test_no_stubs() -> None:
    for key, path in FILES.items():
        text = path.read_text(encoding="utf-8")
        if re.search(r"\bTODO\b|NotImplementedException|throw new UnsupportedOperationException", text):
            fail(f"stub leftover in {path.name}")
    print("OK no stubs")


def test_glass() -> None:
    text = read("glass")
    for needle in ("fillRounded", "DEFAULT_RADIUS", "drawCard"):
        if needle not in text:
            fail(f"AquaGlassPanel missing {needle}")
    print("OK AquaGlassPanel")


def test_badge_slot() -> None:
    badge = read("badge")
    if "HEIGHT = 12" not in badge or "public static int draw(" not in badge:
        fail("AquaBadge missing measure/draw")
    slot = read("slot")
    for rarity in ("COMMON", "UNCOMMON", "RARE", "EPIC", "LEGENDARY"):
        if rarity not in slot:
            fail(f"AquaCaseSlot missing {rarity}")
    if "renderItem" not in slot:
        fail("AquaCaseSlot must draw ItemStack")
    print("OK AquaBadge + AquaCaseSlot")


def test_dialog() -> None:
    text = read("dialog")
    if "extends AquaBlurredScreen" not in text:
        fail("AquaDialogScreen must extend AquaBlurredScreen")
    if "isPauseScreen()" not in text or "return false" not in text:
        fail("AquaDialogScreen must keep world running")
    if "new AquaButton(" not in text:
        fail("AquaDialogScreen must use AquaButton")
    if "public static void confirm(" not in text:
        fail("AquaDialogScreen.confirm missing")
    if "returnToParent" not in text:
        fail("AquaDialogScreen must restore parent")
    print("OK AquaDialogScreen")


def test_wiring() -> None:
    tab_screen = read("tab_screen")
    if "extends AquaBlurredScreen" not in tab_screen:
        fail("OceanTabScreen must extend AquaBlurredScreen")
    if "AquaGlassPanel" not in tab_screen or "AquaBadge" not in tab_screen:
        fail("OceanTabScreen missing glass/badge")

    overlay = read("tab_overlay")
    if "AquaGlassPanel" not in overlay or "AquaBadge" not in overlay:
        fail("OceanTabOverlay missing glass/badge")

    skill = read("skill")
    if "AquaDialogScreen.confirm" not in skill:
        fail("skill tree unlock must go through AquaDialogScreen")
    if "AquaCaseSlot" not in skill or "AquaGlassPanel" not in skill:
        fail("skill tree missing slot/glass")
    if "AquaBadge" not in skill:
        fail("skill tree missing AquaBadge")

    hud = read("hud")
    if "AquaGlassPanel" not in hud:
        fail("HUD missing AquaGlassPanel")

    web = read("web")
    if "AquaGlassPanel" not in web or "new AquaButton(" not in web:
        fail("AquaWebScreen missing glass/button")

    print("OK widgets wired into TAB / K / HUD / CEF")


def main() -> int:
    for key, path in FILES.items():
        if not path.is_file():
            fail(f"missing {key}: {path}")
    test_no_stubs()
    test_glass()
    test_badge_slot()
    test_dialog()
    test_wiring()
    print("OK stage 1.3 widgets")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
