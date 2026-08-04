"""Boost density of vanilla underwater vegetation/decoration features
(kelp, seagrass, coral, sea pickles) so the endless ocean feels more
alive, without touching any mods (zero compatibility risk)."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "tools" / "downloads" / "vanilla_data" / "data" / "minecraft" / "worldgen" / "placed_feature"
OUT_DIRS = [
    ROOT / "server" / "world" / "datapacks" / "aquatech_ocean_life" / "data" / "minecraft" / "worldgen" / "placed_feature",
    ROOT / "server" / "world_datapack_templates" / "aquatech_ocean_life" / "data" / "minecraft" / "worldgen" / "placed_feature",
]

# file -> density multiplier (>1 = more common). For rarity_filter, chance is divided by this.
BOOSTS = {
    "kelp_warm.json": 2.5,
    "kelp_cold.json": 2.5,
    "seagrass_warm.json": 2.0,
    "seagrass_cold.json": 2.0,
    "seagrass_normal.json": 2.0,
    "seagrass_deep.json": 2.0,
    "seagrass_deep_cold.json": 2.0,
    "seagrass_deep_warm.json": 2.0,
    "seagrass_simple.json": 1.8,
    "warm_ocean_vegetation.json": 2.8,
    "sea_pickle.json": 2.5,
}

for out_dir in OUT_DIRS:
    out_dir.mkdir(parents=True, exist_ok=True)

for fname, mult in BOOSTS.items():
    data = json.loads((SRC / fname).read_text(encoding="utf-8"))
    for mod in data["placement"]:
        t = mod.get("type")
        if t == "minecraft:count":
            old = mod["count"]
            if isinstance(old, int):
                mod["count"] = max(old + 1, round(old * mult))
                print(f"{fname}: count {old} -> {mod['count']}")
        elif t == "minecraft:noise_based_count":
            old = mod["noise_to_count_ratio"]
            mod["noise_to_count_ratio"] = max(old + 1, round(old * mult))
            print(f"{fname}: noise_to_count_ratio {old} -> {mod['noise_to_count_ratio']}")
        elif t == "minecraft:rarity_filter":
            old = mod["chance"]
            mod["chance"] = max(1, round(old / mult))
            print(f"{fname}: rarity_filter chance {old} -> {mod['chance']}")
    for out_dir in OUT_DIRS:
        (out_dir / fname).write_text(json.dumps(data, indent=2), encoding="utf-8")

print("done")
