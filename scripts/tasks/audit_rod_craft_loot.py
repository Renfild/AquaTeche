#!/usr/bin/env python3
"""Fail if rod N+1 recipe wants an item that rod N (and earlier) cannot catch.

Parses kubejs/server_scripts/20_aquatech_rod_crafts.js and
rollStarCatcherRodLoot() in FishingLootHandler.java.

Smelt / 9-craft equivalents (ore→ingot, slime_ball→block) count as obtainable,
but are printed as WARN so hidden gates stay visible.

Exit 1 if any ingredient first appears after the previous rod in the chain.
"""
from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
CRAFTS = ROOT / "kubejs" / "server_scripts" / "20_aquatech_rod_crafts.js"
RATES = ROOT / "kubejs" / "server_scripts" / "30_aquatech_crafting.js"
LOOT = ROOT / "mods" / "aquatech-ui" / "src" / "main" / "java" / "net" / "aquatech" / "ui" / "fishing" / "FishingLootHandler.java"

CHAIN = [
    "bamboo_rod",
    "humble_rod",
    "good_old_rod",
    "naturalist_rod",
    "slimed_rod",
    "iceborn_rod",
    "starcatcher_rod",
    "azure_crystal_rod",
    "sharktooth_rod",
    "obsidian_rod",
    "lush_glowberry_rod",
    "magmaforged_rod",
    "alpha_rod",
]

# Vanilla / early items that do not need a fishing drop.
STARTER = {
    "minecraft:string",
    "minecraft:bamboo",
    "minecraft:copper_ingot",
    "minecraft:iron_ingot",
    "minecraft:gold_ingot",
    "minecraft:redstone",
    "minecraft:leather",
    "minecraft:feather",
    "minecraft:stick",
}

# Compact gates the plan wants as real drops, not 9:1 crafts.
STRICT_COMPACT = {
    "minecraft:slime_block",
    "minecraft:diamond_block",
    "minecraft:sea_lantern",
    "minecraft:netherite_ingot",
}

# Caught form → recipe form (smelt / compact). Value is accepted if key was caught,
# except STRICT_COMPACT which still WARNs and counts as FAIL.
EQUIV = {
    "minecraft:copper_ore": "minecraft:copper_ingot",
    "minecraft:iron_ore": "minecraft:iron_ingot",
    "minecraft:gold_ore": "minecraft:gold_ingot",
    "minecraft:redstone_ore": "minecraft:redstone",
    "minecraft:lapis_ore": "minecraft:lapis_lazuli",
    "minecraft:diamond": "minecraft:diamond_block",
    "minecraft:slime_ball": "minecraft:slime_block",
    "minecraft:netherite_scrap": "minecraft:netherite_ingot",
    "minecraft:prismarine_crystals": "minecraft:sea_lantern",
    "industrialupgrade:classicore/tin": "industrialupgrade:itemingots/tin_ingot",
}

ITEMS = {
    "COBBLESTONE": "minecraft:cobblestone",
    "DIRT": "minecraft:dirt",
    "CLAY_BALL": "minecraft:clay_ball",
    "BAMBOO": "minecraft:bamboo",
    "OAK_SAPLING": "minecraft:oak_sapling",
    "BIRCH_SAPLING": "minecraft:birch_sapling",
    "GRAVEL": "minecraft:gravel",
    "SAND": "minecraft:sand",
    "STRING": "minecraft:string",
    "COPPER_ORE": "minecraft:copper_ore",
    "COPPER_INGOT": "minecraft:copper_ingot",
    "IRON_ORE": "minecraft:iron_ore",
    "IRON_INGOT": "minecraft:iron_ingot",
    "COAL_ORE": "minecraft:coal_ore",
    "REDSTONE_ORE": "minecraft:redstone_ore",
    "REDSTONE": "minecraft:redstone",
    "LAPIS_ORE": "minecraft:lapis_ore",
    "LAPIS_LAZULI": "minecraft:lapis_lazuli",
    "GOLD_ORE": "minecraft:gold_ore",
    "GOLD_INGOT": "minecraft:gold_ingot",
    "SLIME_BALL": "minecraft:slime_ball",
    "SLIME_BLOCK": "minecraft:slime_block",
    "OBSIDIAN": "minecraft:obsidian",
    "CRYING_OBSIDIAN": "minecraft:crying_obsidian",
    "DIAMOND": "minecraft:diamond",
    "DIAMOND_BLOCK": "minecraft:diamond_block",
    "AMETHYST_SHARD": "minecraft:amethyst_shard",
    "AMETHYST_BLOCK": "minecraft:amethyst_block",
    "PRISMARINE_SHARD": "minecraft:prismarine_shard",
    "PRISMARINE_CRYSTALS": "minecraft:prismarine_crystals",
    "HEART_OF_THE_SEA": "minecraft:heart_of_the_sea",
    "SEA_LANTERN": "minecraft:sea_lantern",
    "NETHERITE_SCRAP": "minecraft:netherite_scrap",
    "NETHERITE_INGOT": "minecraft:netherite_ingot",
    "NETHER_STAR": "minecraft:nether_star",
    "QUARTZ": "minecraft:quartz",
    "BONE": "minecraft:bone",
    "COBWEB": "minecraft:cobweb",
    "SNOW_BLOCK": "minecraft:snow_block",
    "SNOWBALL": "minecraft:snowball",
    "ROTTEN_FLESH": "minecraft:rotten_flesh",
    "SPIDER_EYE": "minecraft:spider_eye",
    "GUNPOWDER": "minecraft:gunpowder",
    "ARROW": "minecraft:arrow",
    "ENDER_PEARL": "minecraft:ender_pearl",
    "PHANTOM_MEMBRANE": "minecraft:phantom_membrane",
    "TOTEM_OF_UNDYING": "minecraft:totem_of_undying",
    "CARROT": "minecraft:carrot",
    "POTATO": "minecraft:potato",
    "GLOWSTONE_DUST": "minecraft:glowstone_dust",
    "SUGAR": "minecraft:sugar",
    "GLASS_BOTTLE": "minecraft:glass_bottle",
    "STICK": "minecraft:stick",
    "EMERALD": "minecraft:emerald",
    "SADDLE": "minecraft:saddle",
    "LEATHER": "minecraft:leather",
    "FEATHER": "minecraft:feather",
}


def parse_crafts(text: str) -> dict[str, tuple[str | None, list[str]]]:
    """rod_short -> (previous_rod_short or None, ingredient ids)."""
    out: dict[str, tuple[str | None, list[str]]] = {}
    for m in re.finditer(
        r"event\.shaped\('starcatcher:([^']+)_rod'.*?\{(.*?)\}\s*\)\.id",
        text,
        re.S,
    ):
        rod = m.group(1) + "_rod"
        body = m.group(2)
        prev = None
        ings: list[str] = []
        for km in re.finditer(r":\s*'([^']+)'", body):
            item = km.group(1)
            if item.startswith("starcatcher:") and item.endswith("_rod"):
                prev = item.split(":", 1)[1]
            else:
                ings.append(item)
        out[rod] = (prev, ings)
    return out


def parse_rate_ings(text: str) -> dict[str, list[str]]:
    out: dict[str, list[str]] = {}
    for m in re.finditer(
        r"event\.shaped\('(aquatech_ui:rate_x\d+)'.*?\{(.*?)\}\s*\)\.id",
        text,
        re.S,
    ):
        body = m.group(2)
        ings = [km.group(1) for km in re.finditer(r":\s*'([^']+)'", body)]
        out[m.group(1)] = ings
    return out


def extract_method(src: str, name: str) -> str:
    key = f"private static List<ItemStack> {name}("
    start = src.find(key)
    if start < 0:
        raise SystemExit(f"missing {name}")
    brace = src.find("{", start)
    depth = 0
    for i, ch in enumerate(src[brace:], brace):
        if ch == "{":
            depth += 1
        elif ch == "}":
            depth -= 1
            if depth == 0:
                return src[start : i + 1]
    raise SystemExit(f"unclosed {name}")


def parse_case_blocks(method: str) -> dict[str, str]:
    cases: dict[str, str] = {}
    for m in re.finditer(r'case "([^"]+)" -> \{', method):
        name = m.group(1)
        i = m.end() - 1
        depth = 0
        for j, ch in enumerate(method[i:], i):
            if ch == "{":
                depth += 1
            elif ch == "}":
                depth -= 1
                if depth == 0:
                    cases[name] = method[i : j + 1]
                    break
    return cases


def items_in_block(block: str) -> set[str]:
    found: set[str] = set()
    for mid in re.findall(r'getModItem\("([^"]+)"', block):
        found.add(mid)
    for mid in re.findall(r'addIuChance\([^,]+,\s*[^,]+,\s*[^,]+,\s*"([^"]+)"', block):
        found.add(mid)
    for mid in re.findall(r'"(industrialupgrade:[^"]+)"', block):
        found.add(mid)
    for const in re.findall(r"Items\.([A-Z0-9_]+)", block):
        if const in ITEMS:
            found.add(ITEMS[const])
    return found


def expand(caught: set[str]) -> set[str]:
    out = set(caught) | set(STARTER)
    for src, dst in EQUIV.items():
        if src in out:
            out.add(dst)
    if "minecraft:prismarine_shard" in out and "minecraft:prismarine_crystals" in out:
        out.add("minecraft:sea_lantern")
    return out


def first_seen(loot: dict[str, set[str]], item: str) -> str | None:
    for rod in CHAIN:
        pool = expand(loot.get(rod, set()))
        if item in pool or item in loot.get(rod, set()):
            return rod
    return None


def via_equiv(item: str, caught: set[str]) -> str | None:
    for src, dst in EQUIV.items():
        if dst == item and src in caught:
            return src
    if item == "minecraft:sea_lantern" and {
        "minecraft:prismarine_shard",
        "minecraft:prismarine_crystals",
    } <= caught:
        return "prismarine_shard+crystals"
    return None


def main() -> int:
    crafts = parse_crafts(CRAFTS.read_text(encoding="utf-8"))
    method = extract_method(LOOT.read_text(encoding="utf-8"), "rollStarCatcherRodLoot")
    loot = {name: items_in_block(block) for name, block in parse_case_blocks(method).items()}

    print("=== Rod chain (recipe vs rollStarCatcherRodLoot) ===")
    fails = 0
    warns = 0
    for rod in CHAIN:
        prev, ings = crafts.get(rod, (None, []))
        if rod not in crafts:
            print(f"FAIL  {rod}: no shaped recipe")
            fails += 1
            continue
        if rod not in loot and rod not in {"bamboo_rod"}:
            # bamboo is in loot; sky/boner skipped
            pass
        allowed: set[str] = set()
        if prev:
            idx = CHAIN.index(prev) if prev in CHAIN else -1
            for earlier in CHAIN[: idx + 1]:
                allowed |= loot.get(earlier, set())
        raw_allowed = set(allowed)
        allowed = expand(allowed)

        print(f"\n{rod}  prev={prev or '-'}")
        for item in ings:
            seen = first_seen(loot, item)
            if item in STARTER:
                print(f"  OK    {item}  (starter)")
                continue
            if item in allowed:
                eq = via_equiv(item, raw_allowed)
                if eq and item not in raw_allowed:
                    kind = "FAIL" if item in STRICT_COMPACT else "WARN"
                    print(f"  {kind}  {item}  via {eq} (hidden compact/smelt)")
                    if item in STRICT_COMPACT:
                        fails += 1
                    else:
                        warns += 1
                else:
                    print(f"  OK    {item}  first={seen}")
                continue
            if seen is None:
                print(f"  FAIL  {item}  never dropped by chain rods")
                fails += 1
                continue
            if seen not in CHAIN or (prev and CHAIN.index(seen) > CHAIN.index(prev)):
                print(f"  FAIL  {item}  first={seen} (after {prev})")
                fails += 1
            else:
                print(f"  FAIL  {item}  first={seen} not in prev pool")
                fails += 1

    print("\n=== Rate crafts (30_aquatech_crafting.js) vs any chain loot ===")
    all_caught = set()
    for rod in CHAIN:
        all_caught |= loot.get(rod, set())
    all_caught = expand(all_caught)
    rates = parse_rate_ings(RATES.read_text(encoding="utf-8"))
    for rid, ings in rates.items():
        print(f"\n{rid}")
        for item in ings:
            if item.startswith("aquatech_ui:rate_") or item.startswith("botania:"):
                print(f"  SKIP  {item}")
                continue
            if item in STARTER or item in all_caught:
                print(f"  OK    {item}")
            else:
                print(f"  FAIL  {item}  not in any rod pool")
                fails += 1

    print(f"\n{fails} FAIL, {warns} WARN")
    return 1 if fails else 0


if __name__ == "__main__":
    sys.exit(main())
