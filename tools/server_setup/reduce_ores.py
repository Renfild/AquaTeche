import json, math
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SRC = ROOT / "tools" / "downloads" / "vanilla_data" / "data" / "minecraft" / "worldgen" / "placed_feature"
OUT_DIRS = [
    ROOT / "server" / "world" / "datapacks" / "aquatech_rare_ores" / "data" / "minecraft" / "worldgen" / "placed_feature",
    ROOT / "server" / "world_datapack_templates" / "aquatech_rare_ores" / "data" / "minecraft" / "worldgen" / "placed_feature",
]

DIVISORS = {
    "ore_iron_small.json": 5,
    "ore_iron_middle.json": 5,
    "ore_iron_upper.json": 5,
    "ore_copper.json": 5,
    "ore_copper_large.json": 5,
    "ore_gold.json": 3,
    "ore_gold_extra.json": 3,
    "ore_redstone.json": 4,
    "ore_redstone_lower.json": 4,
    "ore_lapis.json": 4,
    "ore_lapis_buried.json": 4,
    "ore_coal_lower.json": 5,
    "ore_coal_upper.json": 5,
    "ore_diamond.json": 2,
    "ore_diamond_buried.json": 2,
    "ore_diamond_large.json": 2,
    "ore_emerald.json": 2,
}

for out_dir in OUT_DIRS:
    out_dir.mkdir(parents=True, exist_ok=True)

for fname, div in DIVISORS.items():
    data = json.loads((SRC / fname).read_text(encoding="utf-8"))
    for mod in data["placement"]:
        if mod.get("type") in ("minecraft:count", "minecraft:count_on_every_layer"):
            c = mod["count"]
            if isinstance(c, int):
                old = c
                new = max(1, math.ceil(old / div))
                mod["count"] = new
                print(f"{fname}: count {old} -> {new}")
            else:
                inner_type = c.get("type") if isinstance(c, dict) else type(c)
                print(f"{fname}: count is complex ({inner_type}), left unchanged")
        elif mod.get("type") == "minecraft:rarity_filter":
            old = mod["chance"]
            new = max(1, math.ceil(old * div))
            mod["chance"] = new
            print(f"{fname}: rarity_filter chance {old} -> {new}")
    for out_dir in OUT_DIRS:
        (out_dir / fname).write_text(json.dumps(data, indent=2), encoding="utf-8")

print("done")
