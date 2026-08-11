"""Harden aquatech StarCatcher fish: real minigame difficulty, no skip."""
from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]

DIFF = {
    "decay": 0.85,
    "hp": 70,
    "missPenalty": 8,
    "modifiers": [],
    "speed": 9,
    "sweetspots": [
        {
            "color_as_int": 65280,
            "hitbox_size_in_pixels": 16,
            "is_flip": False,
            "moving_rate": 0.9,
            "reward": 22,
            "sweet_spot_type": "starcatcher:normal",
            "texture_path": "starcatcher:textures/gui/minigame/spots/thin.png",
            "vanishing_rate": 0.0,
        },
        {
            "color_as_int": 65280,
            "hitbox_size_in_pixels": 16,
            "is_flip": False,
            "moving_rate": 0.9,
            "reward": 22,
            "sweet_spot_type": "starcatcher:normal",
            "texture_path": "starcatcher:textures/gui/minigame/spots/thin.png",
            "vanishing_rate": 0.0,
        },
    ],
}

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
            data["skips_minigame"] = False
            data["difficulty"] = DIFF
            path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
            n += 1
    print(f"updated {n} fish json")


if __name__ == "__main__":
    main()
