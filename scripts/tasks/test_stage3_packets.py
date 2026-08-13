#!/usr/bin/env python3
"""Stage 3 contracts: open-container packets, island limiters, resource cache.

Usage:
  python scripts/tasks/test_stage3_packets.py
"""
from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
UI = ROOT / "mods/aquatech-ui/src/main/java/net/aquatech/ui"

FILES = {
    "c2s": UI / "network/packet/C2SOpenContainerPacket.java",
    "s2c": UI / "network/packet/S2COpenContainerPacket.java",
    "sync": UI / "network/packet/S2CSyncLimitersPacket.java",
    "nh": UI / "network/NetworkHandler.java",
    "open": UI / "server/ContainerOpenService.java",
    "rules": UI / "skyblock/IslandLimiterRules.java",
    "tracker": UI / "skyblock/IslandLimiterTracker.java",
    "handler": UI / "skyblock/IslandLimiterHandler.java",
    "cache": UI / "client/cache/ResourceCacheManager.java",
    "limit_screen": UI / "client/gui/IslandLimiterScreen.java",
    "look_screen": UI / "client/gui/PersonalizationScreen.java",
    "client_open": UI / "client/gui/ClientContainerScreens.java",
    "cmd": UI / "command/AquaTechCommand.java",
    "menu": ROOT / "mods/casesmod/src/main/java/com/casesmod/client/gui/MainMenuScreen.java",
    "bridge": ROOT / "mods/casesmod/src/main/java/com/casesmod/client/gui/AquaContainerOverlay.java",
}


def fail(msg: str) -> None:
    print(f"FAIL {msg}")
    raise SystemExit(1)


def read(key: str) -> str:
    return FILES[key].read_text(encoding="utf-8")


def test_no_stubs() -> None:
    for key, path in FILES.items():
        text = path.read_text(encoding="utf-8")
        if re.search(r"\bTODO\b|Not yet wired|require menu/container wiring", text):
            fail(f"stub leftover in {path.name}")
    print("OK no stubs")


def test_packets() -> None:
    c2s = read("c2s")
    for name in ("STORAGE_VAULT", "BLOCK_LIMITERS", "PERSONALIZATION"):
        if name not in c2s:
            fail(f"C2S missing {name}")
    if "ContainerOpenService.open" not in c2s:
        fail("C2S must delegate to ContainerOpenService")
    open_svc = read("open")
    if "ChestMenu.threeRows" not in open_svc:
        fail("vault must open ender chest ChestMenu")
    if "S2COpenContainerPacket" not in open_svc:
        fail("limiters/look must send S2COpenContainerPacket")
    nh = read("nh")
    if 'PROTOCOL_VERSION = "7"' not in nh:
        fail("protocol must bump to 7 for new S2C")
    if "S2COpenContainerPacket" not in nh:
        fail("NetworkHandler must register S2COpenContainerPacket")
    print("OK packets + protocol 7")


def test_limiters() -> None:
    rules = read("rules")
    for bid in (
        "aquatech_ui:auto_fisher",
        "aquatech_ui:ocean_filter",
        "aquatech_ui:seabed_dredger",
        "aquatech_ui:ocean_altar",
        "aquatech_ui:abyssal_portal",
    ):
        if bid not in rules:
            fail(f"limiter rules missing {bid}")
    handler = read("handler")
    if "canPlace" not in handler or "increment" not in handler:
        fail("IslandLimiterHandler must check then commit counts")
    print("OK island limiter rules + handler")


def test_cache() -> None:
    cache = read("cache")
    for needle in (
        "aquateche.store",
        "crafatar.com",
        "sanitizeKey",
        "isAllowedUrl",
        "MAX_BYTES",
        "0x89",
        "prefetchPlayerAvatar",
        "FAIL_COOLDOWN_MS",
    ):
        if needle not in cache:
            fail(f"ResourceCacheManager missing {needle}")
    print("OK resource cache allowlist + png check")


def test_wiring() -> None:
    if "IslandLimiterScreen" not in read("client_open"):
        fail("ClientContainerScreens must open IslandLimiterScreen")
    if "PersonalizationScreen" not in read("client_open"):
        fail("ClientContainerScreens must open PersonalizationScreen")
    cmd = read("cmd")
    for lit in ('literal("vault")', 'literal("limiters")', 'literal("look")'):
        if lit not in cmd:
            fail(f"command missing {lit}")
    menu = read("menu")
    if "AquaContainerOverlay::openVault" not in menu:
        fail("F4 menu missing vault button")
    if "AquaContainerOverlay::openLimiters" not in menu:
        fail("F4 menu missing limiters button")
    print("OK F4 + commands wired")


def main() -> int:
    for key, path in FILES.items():
        if not path.is_file():
            fail(f"missing {key}: {path}")
    test_no_stubs()
    test_packets()
    test_limiters()
    test_cache()
    test_wiring()
    print("OK stage 3 packets")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
