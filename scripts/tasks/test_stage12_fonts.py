#!/usr/bin/env python3
"""Stage 1.2 contracts: TTF providers + AquaFontRenderer wiring.

Usage:
  python scripts/tasks/test_stage12_fonts.py
"""
from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
MAIN_JSON = ROOT / "mods/aquatech-ui/src/main/resources/assets/aquatech_ui/font/main.json"
HEADER_JSON = ROOT / "mods/aquatech-ui/src/main/resources/assets/aquatech_ui/font/header.json"
RANKS_JSON = ROOT / "mods/aquatech-ui/src/main/resources/assets/aquatech_ui/font/ranks.json"
TTF = ROOT / "mods/aquatech-ui/src/main/resources/assets/aquatech_ui/font/main.ttf"
RENDERER = ROOT / "mods/aquatech-ui/src/main/java/net/aquatech/ui/client/render/AquaFontRenderer.java"
BUTTON = ROOT / "mods/aquatech-ui/src/main/java/net/aquatech/ui/client/gui/widget/AquaButton.java"
TAB = ROOT / "mods/aquatech-ui/src/main/java/net/aquatech/ui/client/tab/OceanTabOverlay.java"
SKILL = ROOT / "mods/aquatech-ui/src/main/java/net/aquatech/ui/client/gui/OceanSkillTreeScreen.java"


def fail(msg: str) -> None:
    print(f"FAIL {msg}")
    raise SystemExit(1)


def ttf_provider(path: Path, size: float) -> None:
    data = json.loads(path.read_text(encoding="utf-8"))
    providers = data.get("providers") or []
    if len(providers) != 1:
        fail(f"{path.name} expected 1 provider, got {len(providers)}")
    p = providers[0]
    if p.get("type") != "ttf":
        fail(f"{path.name} type {p.get('type')!r}, want ttf")
    if p.get("file") != "aquatech_ui:main.ttf":
        fail(f"{path.name} file {p.get('file')!r}")
    if float(p.get("size")) != size:
        fail(f"{path.name} size {p.get('size')}, want {size}")
    if float(p.get("oversample")) < 2.0:
        fail(f"{path.name} oversample too low: {p.get('oversample')}")


def test_assets() -> None:
    ttf_provider(MAIN_JSON, 10.5)
    ttf_provider(HEADER_JSON, 14.0)
    if not TTF.is_file() or TTF.stat().st_size < 10_000:
        fail(f"main.ttf missing or tiny ({TTF})")
    ranks = json.loads(RANKS_JSON.read_text(encoding="utf-8"))
    bitmaps = [p for p in ranks.get("providers") or [] if p.get("type") == "bitmap"]
    if len(bitmaps) < 10:
        fail(f"ranks.json bitmap providers {len(bitmaps)}")
    print("OK ttf + ranks assets")


def test_renderer() -> None:
    text = RENDERER.read_text(encoding="utf-8")
    for needle in (
        'new ResourceLocation("aquatech_ui", "main")',
        'new ResourceLocation("aquatech_ui", "header")',
        "withFont(FONT_MAIN)",
        "withFont(FONT_HEADER)",
        "drawWrapped",
        "wrappedHeight",
    ):
        if needle not in text:
            fail(f"AquaFontRenderer missing {needle}")
    print("OK AquaFontRenderer contract")


def test_wired() -> None:
    for path in (BUTTON, TAB, SKILL):
        text = path.read_text(encoding="utf-8")
        if "AquaFontRenderer" not in text:
            fail(f"{path.name} does not use AquaFontRenderer")
    print("OK font wired into button/tab/skill tree")


def main() -> int:
    for p in (MAIN_JSON, HEADER_JSON, RANKS_JSON, TTF, RENDERER, BUTTON, TAB, SKILL):
        if not p.is_file():
            fail(f"missing {p}")
    test_assets()
    test_renderer()
    test_wired()
    print("OK stage 1.2 fonts")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
