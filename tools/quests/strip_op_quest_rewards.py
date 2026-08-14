# -*- coding: utf-8 -*-
"""Strip OP teleport/flight/creative rewards from FTB quest SNBT."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
CHAPTER_DIRS = [
    ROOT / "config" / "ftbquests" / "quests" / "chapters",
    ROOT / "server" / "config" / "ftbquests" / "quests" / "chapters",
]

BANNED_SUBSTR = [
    "waystones:",
    "enderio:travel_anchor",
    "enderio:staff_of_travelling",
    "ae2:quantum_ring",
    "ae2:quantum_link",
    "ae2:quantum_entangled",
    "ae2:spatial_",
    "ae2:wireless_terminal",
    "draconicevolution:dislocator",
    "draconicevolution:advanced_dislocator",
    "draconicevolution:player_dislocator",
    "draconicevolution:p2p_dislocator",
    "draconicevolution:item_wyvern_flight",
    "draconicevolution:item_draconic_flight",
    "draconicevolution:item_chaotic_flight",
    "draconicevolution:wyvern_flight",
    "draconicevolution:draconic_flight",
    "draconicevolution:chaotic_flight",
    "botania:flight_tiara",
    "botania:flugel_eye",
    "botania:world_seed",
    "avaritia:infinity_",
    "avaritia:endest_pearl",
    "industrialupgrade:teleporter",
    "industrialupgrade:frequency_transmitter",
    "industrialupgrade:jetpack",
    "industrialupgrade:advjetpack",
    "industrialupgrade:impjetpack",
    "industrialupgrade:perjetpack",
    "industrialupgrade:creative_",
    "industrialupgrade:quantum_miner",
    "mekanism:jetpack",
]

SAFE = "industrialupgrade:itemingots/copper_ingot"


def is_banned(item: str) -> bool:
    low = item.lower()
    return any(b.lower() in low for b in BANNED_SUBSTR)


def patch_file(path: Path) -> int:
    text = path.read_text(encoding="utf-8")
    replaced = 0

    def repl(m: re.Match) -> str:
        nonlocal replaced
        key, item = m.group(1), m.group(2)
        if is_banned(item):
            replaced += 1
            return f'{key}: "{SAFE}"'
        return m.group(0)

    text2 = re.sub(r'(item|icon):\s*"([^"]+)"', repl, text)
    if text2 != text:
        path.write_text(text2, encoding="utf-8")
    return replaced


def main() -> None:
    total = 0
    for d in CHAPTER_DIRS:
        if not d.exists():
            continue
        for path in sorted(d.glob("*.snbt")):
            n = patch_file(path)
            if n:
                print(f"{path.parent.parent.name}/{path.name}: {n}")
            total += n
    print(f"DONE replaced={total}")


if __name__ == "__main__":
    main()
