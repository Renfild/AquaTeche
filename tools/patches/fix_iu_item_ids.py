# -*- coding: utf-8 -*-
"""Remap flat Industrial Upgrade item IDs to real nested registry paths."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]

# Short name / flat id path -> correct nested path (no namespace)
IU_REMAP: dict[str, str] = {
    "guide_book": "book/guide_book",
    "treetap": "tools/treetap",
    "wrench": "energy/wrench",
    "block_anvil": "block_anvil/block_anvil",
    "block_strong_anvil": "block_strong_anvil/block_strong_anvil",
    "copper_ingot": "itemingots/copper_ingot",
    "tin_ingot": "itemingots/tin_ingot",
    "bronze_ingot": "itemingots/bronze_ingot",
    "steel_ingot": "itemingots/steel_ingot",
    "copper_plate": "itemplates/copper_plate",
    "tin_plate": "itemplates/tin_plate",
    "bronze_plate": "itemplates/bronze_plate",
    "iron_plate": "itemplates/iron_plate",
    "steel_plate": "itemplates/steel_plate",
    "rawlatex": "raw_latex",
    "steam_machine": "blockresource/steam_machine",
    "machine": "blockresource/machine",
    "advanced_machine": "blockresource/advanced_machine",
    "steam_generator": "basemachine3/steam_generator",
    "steam_macerator": "basemachine3/steam_macerator",
    "steam_compressor": "basemachine3/steam_compressor",
    "steam_extractor": "basemachine3/steam_extractor",
    "steam_rolling": "basemachine3/steam_rolling",
    "steam_cutting": "basemachine3/steam_cutting",
    "steam_extruder": "basemachine3/steam_extruder",
    "blast_furnace_main": "blastfurnace/blast_furnace_main",
    "blast_furnace_part": "blastfurnace/blast_furnace_part",
    "copper_cable": "cable/copper_cable",
    "tin_cable": "cable/tin_cable",
    "gold_cable": "cable/gold_cable",
    "iron_cable": "cable/iron_cable",
    "glass_cable": "cable/glass_cable",
    "re_battery": "battery/re_battery",
    "advanced_re_battery": "battery/advanced_re_battery",
    "energy_crystal": "battery/energy_crystal",
    "lapotron_crystal": "battery/lapotron_crystal",
    "batbox_iu": "wiring_storage/batbox_iu",
    "cesu_iu": "wiring_storage/cesu_iu",
    "mfe_iu": "wiring_storage/mfe_iu",
    "mfsu_iu": "wiring_storage/mfsu_iu",
    "generator_iu": "basemachine3/generator_iu",
    "furnace_iu": "simplemachine/furnace_iu",
    "macerator": "simplemachine/macerator_iu",
    "compressor": "compressor/compressor",
    "extractor_iu": "simplemachine/extractor_iu",
    "rolling": "moremachine2/rolling",
    "cutting": "moremachine2/cutting",
    "extruder": "moremachine2/extruder",
    "alloy_smelter": "basemachine/alloy_smelter",
    "comb_macerator": "moremachine1/comb_macerator",
    "transformer": "transformer_iu/lv",
    "scanner_iu": "basemachine3/scanner_iu",
    "adv_scanner": "basemachine3/adv_scanner",
    "electronics_assembler": "electronics_assembler/electronics_assembler",
    "minipanel": "basemachine3/minipanel",
    "low_panel": "basemachine3/solar_iu",
    "advanced_solar_paneliu": "machines/advanced_solar_paneliu",
    "hybrid_solar_paneliu": "machines/hybrid_solar_paneliu",
    "sunnarium_plate": "itemsunnarium/sunnarium_plate",
    "nanocircuit": "circuit/nanocircuit",
    "quantumcircuit": "circuit/quantumcircuit",
    "plastic_plate": "synthetic_plate",
}

# Longest keys first so copper_ingot wins over copper etc.
_SORTED = sorted(IU_REMAP.keys(), key=len, reverse=True)
_PATTERN = re.compile(
    r"(industrialupgrade:)(" + "|".join(re.escape(k) for k in _SORTED) + r")(?![/\w])"
)


def remap_text(text: str) -> tuple[str, int]:
    def repl(m: re.Match) -> str:
        return m.group(1) + IU_REMAP[m.group(2)]

    new, n = _PATTERN.subn(repl, text)
    return new, n


FILES = [
    ROOT / "server" / "config" / "skyblockbuilder" / "starter_inventory.json5",
    ROOT / "config" / "skyblockbuilder" / "starter_inventory.json5",
    ROOT / "generate_workshop_quests.py",
    ROOT / "workshop_guides.py",
    ROOT / "mods" / "aquatech-ui" / "src" / "main" / "java" / "net" / "aquatech" / "ui" / "fishing" / "FishingLootHandler.java",
]

# casesmod + configs recursive
EXTRA_GLOBS = [
    "server/config/casesmod/**/*.json",
    "config/casesmod/**/*.json",
    "config/ftbquests/quests/chapters/2F_ws_industrial_upgrade.snbt",
    "server/config/ftbquests/quests/chapters/2F_ws_industrial_upgrade.snbt",
]


def main() -> None:
    paths = list(FILES)
    for g in EXTRA_GLOBS:
        paths.extend(ROOT.glob(g))
    # unique
    seen = set()
    total = 0
    for path in paths:
        path = path.resolve()
        if path in seen or not path.is_file():
            continue
        seen.add(path)
        text = path.read_text(encoding="utf-8")
        new, n = remap_text(text)
        if n:
            path.write_text(new, encoding="utf-8")
            print(f"{n:4d}  {path.relative_to(ROOT)}")
            total += n
    print(f"DONE replacements={total} files={len(seen)}")


if __name__ == "__main__":
    main()
