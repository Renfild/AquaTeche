# -*- coding: utf-8 -*-
"""Create AquaTech boot-fix datapack (VS tags + Apotheosis rarities)."""
from __future__ import annotations

import json
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
PACK_NAME = "aquatech_boot_fixes"
PACK_ROOT = ROOT / "datapacks" / PACK_NAME

MISSING_TAGS = [
    "create_vibrant_vaults:item_vaults",
    "create_vibrant_vaults:shipping_containers",
    "forge:storage_blocks/annealed_copper",
    "forge:storage_blocks/beryllium",
    "forge:storage_blocks/lithium",
    "forge:storage_blocks/magnetic_iron",
    "forge:storage_blocks/opal",
    "forge:storage_blocks/polyethylene",
    "forge:storage_blocks/raw_aluminum",
    "forge:storage_blocks/raw_beryllium",
    "forge:storage_blocks/raw_lithium",
    "forge:storage_blocks/raw_magnetite",
    "forge:storage_blocks/raw_opal",
    "forge:storage_blocks/raw_tantalite",
    "hexcasting:amethyst_blocks",
    "hexcasting:slate_blocks",
    "immersive_weathering:leaf_piles",
    "minecraft:carpets",
    "natures_spirit:chalk",
    "natures_spirit:chalk_slabs",
    "natures_spirit:chalk_stairs",
    "natures_spirit:chiseled_travertine",
    "natures_spirit:cobbled_travertine",
    "natures_spirit:cobbled_travertine_slab",
    "natures_spirit:cobbled_travertine_stairs",
    "natures_spirit:cracked_travertine_bricks",
    "natures_spirit:cracked_travertine_tiles",
    "natures_spirit:kaolin",
    "natures_spirit:kaolin_brick_slabs",
    "natures_spirit:kaolin_brick_stairs",
    "natures_spirit:kaolin_bricks",
    "natures_spirit:kaolin_slabs",
    "natures_spirit:kaolin_stairs",
    "natures_spirit:mossy_cobbled_travertine",
    "natures_spirit:mossy_cobbled_travertine_slab",
    "natures_spirit:mossy_cobbled_travertine_stairs",
    "natures_spirit:mossy_travertine_brick_slab",
    "natures_spirit:mossy_travertine_brick_stairs",
    "natures_spirit:mossy_travertine_brick_wall",
    "natures_spirit:mossy_travertine_bricks",
    "natures_spirit:polished_travertine",
    "natures_spirit:polished_travertine_slab",
    "natures_spirit:polished_travertine_stairs",
    "natures_spirit:polished_travertine_wall",
    "natures_spirit:travertine",
    "natures_spirit:travertine_brick_slab",
    "natures_spirit:travertine_brick_stairs",
    "natures_spirit:travertine_brick_wall",
    "natures_spirit:travertine_bricks",
    "natures_spirit:travertine_slab",
    "natures_spirit:travertine_stairs",
    "natures_spirit:travertine_tile_slab",
    "natures_spirit:travertine_tile_stairs",
    "natures_spirit:travertine_tile_wall",
    "natures_spirit:travertine_tiles",
    "simplest_compression:compressed_sand",
]

# Soften top rarities so helmet/boots/bow categories have enough affixes.
MYTHIC = {
    "ordinal": 4,
    "color": "#ED7014",
    "material": "apotheosis:mythic_material",
    "weight": 40,
    "quality": 20,
    "rules": [
        {"type": "stat", "chance": 1.0},
        {"type": "stat", "chance": 1.0},
        {"type": "stat", "chance": 0.85, "backup": {"type": "ability", "chance": 0.4}},
        {"type": "ability", "chance": 1.0},
        {"type": "ability", "chance": 0.75},
        {"type": "socket", "chance": 1.0},
        {"type": "socket", "chance": 0.85},
        {"type": "socket", "chance": 0.65},
        {"type": "durability", "chance": 0.35},
    ],
}

ANCIENT = {
    "ordinal": 5,
    "color": "rainbow",
    "material": "apotheosis:ancient_material",
    "weight": 0,
    "quality": 0,
    "rules": [
        {"type": "ancient", "chance": 1.0},
        {"type": "stat", "chance": 1.0},
        {"type": "stat", "chance": 1.0, "backup": {"type": "ability", "chance": 0.7}},
        {"type": "stat", "chance": 0.9, "backup": {"type": "ability", "chance": 0.5}},
        {"type": "ability", "chance": 1.0},
        {"type": "ability", "chance": 0.8},
        {"type": "ability", "chance": 0.45},
        {"type": "socket", "chance": 1.0},
        {"type": "socket", "chance": 0.85},
        {"type": "socket", "chance": 0.65},
        {"type": "socket", "chance": 0.45},
        {"type": "durability", "chance": 0.75},
    ],
}


def tag_path(tag_id: str) -> Path:
    ns, path = tag_id.split(":", 1)
    # block tags (VS mass uses block tags)
    return PACK_ROOT / "data" / ns / "tags" / "blocks" / f"{path}.json"


def write_tag(tag_id: str) -> None:
    p = tag_path(tag_id)
    p.parent.mkdir(parents=True, exist_ok=True)
    values = []
    if tag_id == "minecraft:carpets":
        # 1.20.1 renamed carpets -> wool_carpets; alias for VS mass files
        values = ["#minecraft:wool_carpets"]
    p.write_text(json.dumps({"replace": False, "values": values}, indent=2) + "\n", encoding="utf-8")
    # also item tags for completeness
    item_p = PACK_ROOT / "data" / tag_id.split(":")[0] / "tags" / "items" / f"{tag_id.split(':',1)[1]}.json"
    item_p.parent.mkdir(parents=True, exist_ok=True)
    item_p.write_text(json.dumps({"replace": False, "values": values}, indent=2) + "\n", encoding="utf-8")


def main() -> None:
    if PACK_ROOT.exists():
        shutil.rmtree(PACK_ROOT)
    PACK_ROOT.mkdir(parents=True)

    (PACK_ROOT / "pack.mcmeta").write_text(
        json.dumps(
            {
                "pack": {
                    "pack_format": 15,
                    "description": "AquaTech boot fixes: VS missing tags + Apotheosis rarity soft rules",
                }
            },
            indent=2,
        )
        + "\n",
        encoding="utf-8",
    )

    for tag in MISSING_TAGS:
        write_tag(tag)

    rarity_dir = PACK_ROOT / "data" / "apotheosis" / "rarities"
    rarity_dir.mkdir(parents=True, exist_ok=True)
    (rarity_dir / "mythic.json").write_text(json.dumps(MYTHIC, indent=2) + "\n", encoding="utf-8")
    (rarity_dir / "ancient.json").write_text(json.dumps(ANCIENT, indent=2) + "\n", encoding="utf-8")

    # Neutralize VS mass compat for absent mods (empty mass lists)
    compat_empty = []
    for name in (
        "natures_spirit",
        "hexcasting",
        "immersive_weathering",
        "create_vibrant_vaults",
        "simplest_compression",
    ):
        p = PACK_ROOT / "data" / "valkyrienskies" / "vs_mass" / "compat" / f"{name}.json"
        p.parent.mkdir(parents=True, exist_ok=True)
        p.write_text(json.dumps(compat_empty, indent=2) + "\n", encoding="utf-8")

    print(f"OK datapack {PACK_ROOT} tags={len(MISSING_TAGS)}")


if __name__ == "__main__":
    main()
