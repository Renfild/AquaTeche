"""Generate AquaTech resource fishing rods datapack (10 tiers).

The datapack listens to fishing catches, removes vanilla caught items nearby,
and gives tier-based resource loot instead. Tier is read from held rod NBT tag:
SelectedItem.tag.aqRodTier (defaults to 1 if missing).
"""
from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUTS = [
    ROOT / "server" / "world" / "datapacks" / "aquatech_resource_rods",
    ROOT / "server" / "world_datapack_templates" / "aquatech_resource_rods",
]

POOL = {
    1: [
        ("minecraft:oak_planks", 18, 1, 3),
        ("minecraft:stick", 16, 1, 4),
        ("minecraft:cobblestone", 14, 1, 3),
        ("minecraft:kelp", 12, 1, 3),
        ("minecraft:string", 10, 1, 2),
        ("minecraft:clay_ball", 8, 1, 2),
        ("minecraft:bone", 8, 1, 2),
        ("minecraft:coal", 6, 1, 2),
        ("minecraft:iron_nugget", 5, 1, 3),
        ("minecraft:copper_ingot", 3, 1, 1),
    ],
    2: [
        ("minecraft:oak_log", 14, 1, 2),
        ("minecraft:cobblestone", 14, 1, 4),
        ("minecraft:coal", 10, 1, 3),
        ("minecraft:iron_nugget", 10, 1, 4),
        ("minecraft:copper_ingot", 8, 1, 2),
        ("minecraft:iron_ingot", 5, 1, 1),
        ("minecraft:redstone", 6, 1, 2),
        ("minecraft:string", 8, 1, 2),
        ("minecraft:clay_ball", 7, 1, 3),
        ("minecraft:nautilus_shell", 2, 1, 1),
    ],
    3: [
        ("minecraft:oak_log", 12, 1, 3),
        ("minecraft:iron_ingot", 8, 1, 2),
        ("minecraft:copper_ingot", 10, 1, 3),
        ("minecraft:redstone", 8, 1, 3),
        ("minecraft:lapis_lazuli", 6, 1, 2),
        ("minecraft:gold_nugget", 6, 1, 3),
        ("minecraft:coal", 8, 1, 3),
        ("minecraft:prismarine_shard", 5, 1, 2),
        ("minecraft:quartz", 4, 1, 2),
        ("minecraft:iron_nugget", 8, 1, 5),
    ],
    4: [
        ("minecraft:iron_ingot", 11, 1, 2),
        ("minecraft:copper_ingot", 10, 1, 3),
        ("minecraft:redstone", 9, 1, 4),
        ("minecraft:lapis_lazuli", 8, 1, 3),
        ("minecraft:gold_nugget", 7, 1, 4),
        ("minecraft:quartz", 6, 1, 3),
        ("minecraft:prismarine_crystals", 5, 1, 2),
        ("minecraft:amethyst_shard", 4, 1, 2),
        ("minecraft:obsidian", 3, 1, 1),
        ("minecraft:gold_ingot", 3, 1, 1),
    ],
    5: [
        ("minecraft:iron_ingot", 10, 1, 3),
        ("minecraft:gold_ingot", 6, 1, 2),
        ("minecraft:redstone", 10, 2, 5),
        ("minecraft:lapis_lazuli", 8, 1, 4),
        ("minecraft:amethyst_shard", 6, 1, 3),
        ("minecraft:diamond", 2, 1, 1),
        ("minecraft:emerald", 2, 1, 1),
        ("minecraft:prismarine_crystals", 6, 1, 3),
        ("minecraft:quartz", 6, 1, 4),
        ("minecraft:copper_ingot", 8, 1, 4),
    ],
    6: [
        ("minecraft:iron_ingot", 9, 1, 3),
        ("minecraft:gold_ingot", 8, 1, 2),
        ("minecraft:redstone", 8, 2, 6),
        ("minecraft:lapis_lazuli", 8, 2, 5),
        ("minecraft:diamond", 3, 1, 1),
        ("minecraft:emerald", 3, 1, 1),
        ("minecraft:amethyst_shard", 6, 2, 4),
        ("minecraft:prismarine_crystals", 7, 1, 4),
        ("minecraft:experience_bottle", 3, 1, 1),
        ("minecraft:obsidian", 4, 1, 2),
    ],
    7: [
        ("minecraft:gold_ingot", 8, 1, 3),
        ("minecraft:diamond", 4, 1, 2),
        ("minecraft:emerald", 4, 1, 2),
        ("minecraft:redstone", 7, 3, 7),
        ("minecraft:lapis_lazuli", 7, 2, 6),
        ("minecraft:amethyst_shard", 7, 2, 5),
        ("minecraft:netherite_scrap", 1, 1, 1),
        ("minecraft:prismarine_crystals", 7, 2, 5),
        ("minecraft:experience_bottle", 5, 1, 2),
        ("minecraft:ender_pearl", 4, 1, 2),
    ],
    8: [
        ("minecraft:diamond", 6, 1, 2),
        ("minecraft:emerald", 6, 1, 2),
        ("minecraft:gold_ingot", 7, 2, 4),
        ("minecraft:redstone", 6, 4, 8),
        ("minecraft:lapis_lazuli", 6, 3, 7),
        ("minecraft:netherite_scrap", 2, 1, 1),
        ("minecraft:prismarine_crystals", 7, 2, 6),
        ("minecraft:experience_bottle", 6, 1, 3),
        ("minecraft:ender_pearl", 5, 1, 3),
        ("minecraft:blaze_rod", 3, 1, 2),
    ],
    9: [
        ("minecraft:diamond", 7, 1, 3),
        ("minecraft:emerald", 7, 1, 3),
        ("minecraft:netherite_scrap", 3, 1, 1),
        ("minecraft:gold_ingot", 6, 2, 4),
        ("minecraft:redstone", 5, 4, 9),
        ("minecraft:lapis_lazuli", 5, 4, 8),
        ("minecraft:prismarine_crystals", 6, 3, 7),
        ("minecraft:experience_bottle", 7, 2, 4),
        ("minecraft:ender_pearl", 6, 1, 3),
        ("minecraft:blaze_rod", 4, 1, 2),
    ],
    10: [
        ("minecraft:diamond", 8, 2, 4),
        ("minecraft:emerald", 8, 2, 4),
        ("minecraft:netherite_scrap", 4, 1, 1),
        ("minecraft:gold_ingot", 6, 3, 5),
        ("minecraft:redstone", 4, 6, 12),
        ("minecraft:lapis_lazuli", 4, 6, 12),
        ("minecraft:prismarine_crystals", 6, 4, 9),
        ("minecraft:experience_bottle", 8, 2, 5),
        ("minecraft:ender_pearl", 6, 2, 4),
        ("minecraft:blaze_rod", 5, 1, 3),
    ],
}

CONVERT_KILLS = [
    "minecraft:cod",
    "minecraft:salmon",
    "minecraft:tropical_fish",
    "minecraft:pufferfish",
    "minecraft:bowl",
    "minecraft:stick",
    "minecraft:string",
    "minecraft:tripwire_hook",
    "minecraft:leather",
    "minecraft:leather_boots",
    "minecraft:bone",
    "minecraft:water_bottle",
    "minecraft:ink_sac",
    "minecraft:name_tag",
    "minecraft:nautilus_shell",
    "minecraft:saddle",
]


def write(path: Path, data: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(data, encoding="utf-8")


def loot_table(tier: int) -> dict:
    entries = []
    for item, weight, cmin, cmax in POOL[tier]:
        entry = {"type": "minecraft:item", "name": item, "weight": weight}
        if cmin != cmax:
            entry["functions"] = [
                {
                    "function": "minecraft:set_count",
                    "count": {"type": "minecraft:uniform", "min": cmin, "max": cmax},
                }
            ]
        elif cmin != 1:
            entry["functions"] = [{"function": "minecraft:set_count", "count": cmin}]
        entries.append(entry)
    return {"type": "minecraft:chest", "pools": [{"rolls": 1, "entries": entries}]}


def main() -> None:
    for out in OUTS:
        write(
            out / "pack.mcmeta",
            json.dumps(
                {
                    "pack": {
                        "pack_format": 15,
                        "description": "AquaTech: 10-tier resource fishing rods progression",
                    }
                },
                ensure_ascii=False,
                indent=2,
            )
            + "\n",
        )

        write(
            out / "data/minecraft/tags/functions/load.json",
            json.dumps({"values": ["aqrod:load"]}, indent=2) + "\n",
        )

        write(
            out / "data/aqrod/functions/load.mcfunction",
            "scoreboard objectives add aqrod.tier dummy\n",
        )

        convert_lines = []
        for item in CONVERT_KILLS:
            convert_lines.append(
                f'execute as @e[type=minecraft:item,distance=..6,limit=6,sort=nearest,nbt={{Age:0s,Item:{{id:"{item}"}}}}] run kill @s'
            )
        write(out / "data/aqrod/functions/catch/convert.mcfunction", "\n".join(convert_lines) + "\n")

        main_fn = [
            "advancement revoke @s only aqrod:catch",
            "scoreboard players set @s aqrod.tier 1",
            "execute if data entity @s SelectedItem.tag.aqRodTier store result score @s aqrod.tier run data get entity @s SelectedItem.tag.aqRodTier 1",
            "function aqrod:catch/convert",
        ]
        for t in range(1, 11):
            main_fn.append(
                f"execute if score @s aqrod.tier matches {t} run loot give @s loot aqrod:rod_tier_{t}"
            )
        write(out / "data/aqrod/functions/catch/main.mcfunction", "\n".join(main_fn) + "\n")

        write(
            out / "data/aqrod/advancements/catch.json",
            json.dumps(
                {
                    "criteria": {
                        "catch": {
                            "trigger": "minecraft:fishing_rod_hooked",
                            "conditions": {
                                "rod": {"items": ["minecraft:fishing_rod"]},
                                "entity": {"type": "minecraft:item"},
                            },
                        }
                    },
                    "rewards": {"function": "aqrod:catch/main"},
                },
                indent=2,
            )
            + "\n",
        )

        for t in range(1, 11):
            write(
                out / f"data/aqrod/loot_tables/rod_tier_{t}.json",
                json.dumps(loot_table(t), indent=2) + "\n",
            )

    print("Generated aquatech_resource_rods datapack into world + template")


if __name__ == "__main__":
    main()
