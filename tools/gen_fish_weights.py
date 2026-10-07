"""Собирает таблицу веса и размера по видам рыбы из jar Starcatcher.

Результат: mods/aquatech-ui/src/main/resources/data/aquatech_ui/fish_weights.json
Запуск: python tools/gen_fish_weights.py [путь к starcatcher-*.jar]
"""
import json
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DEFAULT_JAR = ROOT / "client" / "mods" / "starcatcher-2.3.19-FORGE-1.20.1.jar"
OUT = ROOT / "mods" / "aquatech-ui" / "src" / "main" / "resources" / "data" / "aquatech_ui" / "fish_weights.json"
FISH_DIR = "data/starcatcher/starcatcher/fish/"


def main() -> int:
    jar = Path(sys.argv[1]) if len(sys.argv) > 1 else DEFAULT_JAR
    species = {}
    with zipfile.ZipFile(jar) as zf:
        for name in sorted(zf.namelist()):
            if not name.startswith(FISH_DIR) or not name.endswith(".json"):
                continue
            data = json.loads(zf.read(name).decode("utf-8"))
            info = data.get("catch_info", {})
            # только обычная рыба: у секретных бутылок, трофеев и extra-дропов своя логика
            if info.get("type") is not None or "item" not in info:
                continue
            sw = data["size_and_weight"]
            species[info["item"]] = {
                "avgG": int(sw["average_weight_grams"]),
                "devG": int(sw["deviation_weight_grams"]),
                "avgCm": int(sw["average_size_cm"]),
                "devCm": int(sw["deviation_size_cm"]),
                "rarity": data.get("rarity", "common"),
            }
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(json.dumps(species, ensure_ascii=False, indent=1, sort_keys=True) + "\n", encoding="utf-8")
    print(f"{len(species)} видов -> {OUT}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
