"""Neutralize aquatech SC fish preview (loot still from AquaTech rod pools)."""
from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]

# Shown in StarCatcher minigame only — AquaTech replaces drops after catch.
PREVIEW_ITEM = "minecraft:cod"

ROOTS = [
    ROOT / "datapacks/aquatech_boot_fixes/data/aquatech/starcatcher/fish",
    ROOT / "server/datapacks/aquatech_boot_fixes/data/aquatech/starcatcher/fish",
    ROOT / "server/moonlight-global-datapacks/aquatech_boot_fixes/data/aquatech/starcatcher/fish",
    ROOT / "server/world/datapacks/aquatech_boot_fixes/data/aquatech/starcatcher/fish",
]


def main() -> None:
    n = 0
    for root in ROOTS:
        if not root.is_dir():
            continue
        for path in root.glob("*.json"):
            data = json.loads(path.read_text(encoding="utf-8"))
            data["catch_info"] = {"item": PREVIEW_ITEM}
            data["has_guide_entry"] = False
            data["skips_minigame"] = False
            path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
            n += 1
    print(f"updated {n} fish json (preview={PREVIEW_ITEM})")


if __name__ == "__main__":
    main()
