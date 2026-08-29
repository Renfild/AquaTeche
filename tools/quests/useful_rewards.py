# -*- coding: utf-8 -*-
"""Next-craft + solar-panel quest rewards. No leftover upgrade junk."""
from __future__ import annotations

import hashlib
import re
from pathlib import Path

CPL = "industrialupgrade:itemplates/copper_plate"
TPL = "industrialupgrade:itemplates/tin_plate"
IPL = "industrialupgrade:itemplates/iron_plate"
SPL = "industrialupgrade:itemplates/steel_plate"
CU = "industrialupgrade:itemingots/copper_ingot"
TIN = "industrialupgrade:itemingots/tin_ingot"
BRZ = "industrialupgrade:itemingots/bronze_ingot"
STL = "industrialupgrade:itemingots/steel_ingot"
CIRC = "industrialupgrade:crafting_elements/crafting_272_element"
ADV = "industrialupgrade:crafting_elements/crafting_273_element"
RUB = "industrialupgrade:crafting_elements/crafting_271_element"
MOT = "industrialupgrade:crafting_elements/crafting_276_element"
BAT = "industrialupgrade:battery/re_battery"
CRYS = "industrialupgrade:battery/energy_crystal"
CAB = "industrialupgrade:cable/copper_cable"
TCAB = "industrialupgrade:cable/tin_cable"
MCH = "industrialupgrade:blockresource/machine"
SOL = "industrialupgrade:solar_energy"
ASOL = "industrialupgrade:adv_solar_energy"
ISOL = "industrialupgrade:imp_solar_energy"
ADVP = "industrialupgrade:machines/advanced_solar_paneliu"
HSOL = "industrialupgrade:machines/hybrid_solar_paneliu"

SKIP_FILES = {
    "secret_aquatech.snbt",
    "endgame_aquatech.snbt",
    "1.snbt",
    "57FF374744F4AC76.snbt",
}

CHAPTER_KEY = {
    "steam_era.snbt": "steam",
    "basic_electric_era.snbt": "basic",
    "improved_electric_era.snbt": "improved",
    "1.snbt": "ch1",
    "57FF374744F4AC76.snbt": "pipes",
    "7D2835D587AABDAB.snbt": "info",
    "botania_aquatech.snbt": "botania",
    "alexscaves_aquatech.snbt": "caves",
    "ae2_aquatech.snbt": "ae2",
    "avaritia_aquatech.snbt": "avaritia",
}


def hid(seed: str) -> str:
    return hashlib.md5(seed.encode("utf-8")).hexdigest()[:16].upper()


def _stem(task: str) -> str:
    return task.split(":")[-1].lower()


def _dedupe(_task: str, pairs: list[tuple[str, int]]) -> list[tuple[str, int]]:
    out: list[tuple[str, int]] = []
    seen: set[str] = set()
    for item, count in pairs:
        if item in seen:
            continue
        seen.add(item)
        out.append((item, count))
    return out[:3]


def pack_for(task: str, chapter: str) -> list[tuple[str, int]]:
    t = _stem(task)
    ns = task.split(":")[0] if ":" in task else ""

    if chapter == "steam":
        return _dedupe(task, [("minecraft:copper_ingot", 16)] + _steam(t))
    if chapter == "basic":
        return _dedupe(task, [("minecraft:iron_ingot", 16)] + _basic(t))
    if chapter == "improved":
        return _dedupe(task, [("minecraft:gold_ingot", 8)] + _improved(t))
    if chapter == "ch1":
        return _dedupe(task, _ch1(t, task))
    if chapter == "pipes":
        return _dedupe(task, _pipes(t))
    if chapter == "info":
        return [("minecraft:cooked_salmon", 16), (CPL, 8)]
    if chapter == "botania" or ns in {
        "botania",
        "botanicalmachinery",
        "botanicalextramachinery",
        "mythicbotany",
    }:
        return _dedupe(task, _botania(t, ns))
    if chapter == "caves" or ns == "alexscaves":
        return _dedupe(task, _caves(t))
    if chapter == "ae2" or ns == "ae2":
        return _dedupe(task, _ae2(t))
    if chapter == "avaritia" or ns in {"avaritia", "avaritia_armor"}:
        return _dedupe(task, _avaritia(t, ns))
    return [(CPL, 8)]


def _steam(t: str) -> list[tuple[str, int]]:
    if "steam_machine" in t:
        return [(CPL, 16), (BRZ, 8)]
    if "steamboiler" in t:
        return [("minecraft:coal", 48), (CPL, 8)]
    if "pressureconverter" in t:
        return [(BRZ, 8), (CAB, 8)]
    if "silicon" in t or "crystal_handler" in t:
        return [(CPL, 8), (CIRC, 2)]
    if "laser" in t or "sharpener" in t:
        return [(IPL, 16), (CPL, 8)]
    if any(k in t for k in ("ampere", "peat_generator", "steam_converter", "steam_storage")):
        return [(SOL, 1), (CAB, 16)]
    if "electrolyzer" in t or t.endswith("oxygen"):
        return [("minecraft:bucket", 8), (CPL, 8)]
    if "gas_chamber" in t or "sulfur" in t or "coppersulfate" in t:
        return [(CPL, 16), (BRZ, 8)]
    if "487" in t or "488" in t or "programming" in t:
        return [(CIRC, 4), (RUB, 8)]
    if "272" in t:
        return [(MCH, 2), (CIRC, 4)]
    if "137" in t or "machine_casing" in t or "electronics_assembler" in t:
        return [(IPL, 16), (CIRC, 4)]
    if "handler_ore" in t or "498" in t or "499" in t:
        return [("minecraft:coal", 32), (IPL, 8)]
    if "steel" in t or "hammer" in t or "anvil" in t or "refractory" in t or "mini_smeltery" in t:
        return [(STL, 8), (SPL, 8)]
    if "soldering" in t:
        return [(CIRC, 4), (RUB, 8)]
    if "pump" in t or "boiler_controller" in t or "quarry" in t:
        return [(SOL, 1), (BRZ, 8)]
    if "503" in t:
        return [(STL, 16), (SPL, 8)]
    if "blast_furnace" in t:
        return [(SOL, 1), (STL, 16)]
    if "crystal_charge" in t or "fluid_heater" in t:
        return [(CIRC, 2), (CPL, 8)]
    return [(CPL, 12), (BRZ, 4)]


def _basic(t: str) -> list[tuple[str, int]]:
    if "276" in t or "elemotor" in t:
        return [(CIRC, 4), (CAB, 16)]
    if t == "generator_iu" or "fluid_heat" in t:
        return [(SOL, 1), (BAT, 2)]
    if "redstone_generator" in t or "geogenerator" in t:
        return [(SOL, 1), (BAT, 2)]
    if "macerator" in t:
        return [(TPL, 16), (CIRC, 2)]
    if "microchip" in t or "electronic_assembler" in t or "273" in t:
        return [(ADV, 4), (CIRC, 8)]
    if "adv_alloy" in t:
        return [(ADV, 2), (STL, 8)]
    if "alloy_smelter" in t or "gearing" in t or "welding" in t:
        return [(TIN, 16), (BRZ, 8)]
    if "plastic" in t or "polymer" in t or "plast" in t:
        return [(RUB, 16), (CIRC, 4)]
    if any(k in t for k in ("nitrogen", "nitrate", "nitric", "gas_combiner", "gas_sensor", "veingas")):
        return [("minecraft:bucket", 8), (CIRC, 2)]
    if "se_gen" in t or "sunnarium" in t:
        return [(ASOL, 1), (SOL, 2)]
    if any(k in t for k in ("diesel", "gen_disel", "gen_pet", "wind_generator", "water_generator")):
        return [(SOL, 1), (BAT, 2)]
    if "cooling" in t:
        return [(BAT, 2), (TPL, 8)]
    if "orewashing" in t or "centrifuge" in t or "enrichment" in t:
        return [(TPL, 16), (ADV, 2)]
    if "refiner" in t or "handler_ho" in t:
        return [(CIRC, 4), (STL, 8)]
    if "radioactive" in t or "waste" in t or "443" in t or t.endswith("uranium") or "ore_purifier" in t:
        return [(ASOL, 1), (ADV, 4)]
    if "item_divider" in t or "fluid_integrator" in t or "fluid_separator" in t:
        return [(CIRC, 2), (TPL, 8)]
    if "solid_mixer" in t or "solid_fluid" in t:
        return [(CIRC, 2), (TIN, 8)]
    if "79_element" in t:
        return [(ASOL, 1), (CIRC, 4)]
    if "482" in t or "acetylene" in t:
        return [(RUB, 8), (CIRC, 2)]
    if "propane" in t or "bromine" in t or "propylene" in t or "polyeth" in t:
        return [("minecraft:bucket", 8), (RUB, 8)]
    return [(TPL, 12), (CIRC, 2)]


def _improved(t: str) -> list[tuple[str, int]]:
    if "overclocker" in t or "transformerupgrade" in t or "upgrade_speed" in t:
        return [(ADV, 8), (CIRC, 8)]
    if "advanced_solar" in t:
        return [(HSOL, 1)]
    if "minipanel" in t:
        return [(ADVP, 1), (ASOL, 1)]
    if "solardestiller" in t:
        return [(ASOL, 1), (SOL, 2)]
    if "mfe" in t:
        return [(ASOL, 1), (CRYS, 2)]
    if "pallet_generator" in t or "lightning_rod" in t:
        return [(ASOL, 1), (ADV, 4)]
    if "purifier_soil" in t:
        return [(HSOL, 1), (ADVP, 1)]
    if "laser_polisher" in t or "farmer" in t or "crop" in t or "fertilizer" in t:
        return [(SPL, 16), (CIRC, 4)]
    if "lithium" in t or "re_battery" in t:
        return [(BAT, 4), (ADV, 2)]
    if "petrol" in t or "refiner" in t or "coke" in t or t.endswith("oil") or "251_element" in t:
        return [(STL, 16), (CIRC, 4)]
    if any(k in t for k in ("hazmat", "radioprotector", "dosimeter", "40_element")):
        return [(ADV, 2), (STL, 8)]
    if any(k in t for k in ("reactor", "uranium_fuel", "lead_box", "pellet", "radcable", "water_controller", "gas_controller")):
        return [(ASOL, 1), (ADV, 4)]
    if any(k in t for k in ("matter", "replicator", "scanner", "pattern_storage", "scrap")):
        return [(ADV, 4), (CRYS, 2)]
    if "battery_factory" in t or "socket_factory" in t or "matter_factory" in t:
        return [(BAT, 4), (ADV, 2)]
    if "electrolyzer" in t:
        return [(CIRC, 4), (CAB, 16)]
    if "molecular" in t:
        return [(ADV, 4), (STL, 8)]
    if "photoniy" in t:
        return [(HSOL, 1), (ADV, 2)]
    if any(k in t for k in ("rocket", "rover", "probe", "satellite", "research", "hydrazine")):
        return [(ADV, 4), (ISOL, 1)]
    if "aircollector" in t or "soil_analyzer" in t or "radiation_purifier" in t or "synthesis" in t:
        return [(ADV, 4), (ASOL, 1)]
    if "foam" in t or "reinforced" in t or "obsidian" in t:
        return [(STL, 16), (IPL, 16)]
    if "fisher" in t or "wither" in t or "enchanter" in t:
        return [(CIRC, 4), (ADV, 2)]
    if "mesh" in t or "quarry" in t:
        return [(STL, 16), (SPL, 8)]
    if "cooling" in t or "azure" in t or "motoroil" in t or "industrialoil" in t:
        return [("minecraft:bucket", 8), (ADV, 2)]
    if "trash" in t:
        return [(MCH, 2), (CIRC, 4)]
    if "steam_turbine" in t:
        return [(ASOL, 1), (STL, 16)]
    if "planner" in t:
        return [(CIRC, 8), (SPL, 8)]
    if "pollution" in t:
        return [(ADV, 2), (CIRC, 4)]
    return [(SPL, 8), (ADV, 2)]


def _ch1(t: str, task: str) -> list[tuple[str, int]]:
    if "speed_x4" in t or "speed_upgrade" in t:
        return [(SOL, 1), (CIRC, 4), (CAB, 16)]
    if "auto_fisher" in t:
        return []
    return []


def _pipes(t: str) -> list[tuple[str, int]]:
    m = re.search(r"itemcable(\d+)", t)
    if m:
        nxt = int(m.group(1)) + 1
        return [(f"industrialupgrade:wiring/itemcable{nxt}", 16)]
    if t.endswith("itemcable"):
        return [("industrialupgrade:wiring/itemcable1", 16)]
    if "chest" in t:
        return [("avaritia:compressed_chest", 1)]
    return [(CAB, 16)]


def _botania(t: str, ns: str) -> list[tuple[str, int]]:
    if "lexicon" in t:
        return [("botania:white_petal", 16), ("botania:red_petal", 16)]
    if "white_petal" in t:
        return [("botania:red_petal", 16), ("botania:blue_petal", 16), ("botania:orange_petal", 16)]
    if "apothecary" in t:
        return [("botania:livingwood", 16)]
    if "pure_daisy" in t:
        return [("botania:livingwood", 16), ("botania:livingrock", 16)]
    if "livingwood" in t:
        return [("botania:livingrock", 16)]
    if "livingrock" in t:
        return [("minecraft:iron_ingot", 16), ("botania:livingrock", 8)]
    if "mana_pool" in t and "mechanical" not in t and "base_" not in t:
        return [("botania:manasteel_ingot", 8)]
    if "spreader" in t:
        return [("botania:manasteel_ingot", 8)]
    if "mana_tablet" in t:
        return [("botania:mana_pearl", 4)]
    if "manasteel_ingot" in t:
        return [("botania:rune_mana", 2)]
    if "twig_wand" in t:
        return [("botania:livingwood", 16), ("botania:manasteel_ingot", 4)]
    if "runic_altar" in t and "mechanical" not in t:
        return [("botania:rune_mana", 4)]
    if "rune_water" in t:
        return [("minecraft:nether_wart", 8), ("minecraft:gunpowder", 16)]
    if "rune_fire" in t:
        return [("minecraft:coal", 32), ("minecraft:stone", 32)]
    if "rune_earth" in t:
        return [("minecraft:feather", 16), ("minecraft:string", 16)]
    if "rune_air" in t:
        return [("botania:manasteel_ingot", 8)]
    if "mana_pylon" in t:
        return [("botania:mana_pearl", 4), ("botania:mana_diamond", 2)]
    if "terra_plate" in t:
        return [("botania:mana_pearl", 4), ("botania:manasteel_ingot", 8)]
    if "terrasteel_ingot" in t:
        return [("botania:terrasteel_ingot", 1), ("botania:mana_diamond", 2)]
    if "terra_pick" in t:
        return [("botania:elementium_ingot", 4)]
    if "alfheim_portal" in t:
        return [("botania:elementium_ingot", 8)]
    if "elementium" in t:
        return [("botania:pixie_dust", 8), ("botania:elementium_ingot", 4)]
    if "gaia_pylon" in t:
        return [("botania:pixie_dust", 16)]
    if "gaia_ingot" in t:
        return [("botania:gaia_ingot", 1)]
    if "mechanical_daisy" in t or "base_daisy" in t:
        return [("botania:manasteel_ingot", 8), ("botania:livingwood", 16)]
    if "mechanical_apothecary" in t:
        return [("botania:rune_mana", 4)]
    if "mechanical_runic" in t:
        return [("botania:manasteel_ingot", 8)]
    if "mechanical_mana_pool" in t or "base_mana_pool" in t:
        return [("botania:manasteel_ingot", 8)]
    if "mechanical_mana_infuser" in t:
        return [("botania:manasteel_ingot", 8)]
    if "mechanical_brewery" in t:
        return [("minecraft:nether_wart", 16), ("minecraft:blaze_powder", 8)]
    if "agglomeration" in t:
        return [("botania:terrasteel_ingot", 2)]
    if "alfheim_market" in t:
        return [("botania:elementium_ingot", 8)]
    if "mana_battery" in t:
        return [("botania:manasteel_ingot", 16)]
    if "advanced_daisy" in t:
        return [("botania:livingrock", 32), ("botania:manasteel_ingot", 8)]
    if "greenhouse" in t:
        return [("botania:white_petal", 32), ("botania:red_petal", 16)]
    if "mana_infuser" in t:
        return [("botania:elementium_ingot", 8)]
    if "mana_collector" in t:
        return [("botania:manasteel_ingot", 8)]
    if "alfsteel_pylon" in t:
        return [("mythicbotany:alfsteel_nugget", 16)]
    if "alfsteel_ingot" in t:
        return [("mythicbotany:alfsteel_ingot", 1)]
    if "alfsteel_pick" in t:
        return [("mythicbotany:alfsteel_nugget", 16)]
    if "asgard" in t:
        return [("mythicbotany:alfsteel_nugget", 8)]
    return [("botania:manasteel_ingot", 8)]


def _caves(t: str) -> list[tuple[str, int]]:
    if "cave_tablet" in t:
        return [("alexscaves:cave_tablet", 2)]
    if "cave_codex" in t:
        return [("minecraft:map", 4), ("alexscaves:cave_tablet", 1)]
    if "cave_book" in t:
        return [("alexscaves:cave_tablet", 2)]
    if "scarlet_neodymium" in t:
        return [("alexscaves:azure_neodymium_ingot", 8)]
    if "tesla_bulb" in t:
        return [("alexscaves:scarlet_neodymium_ingot", 8)]
    if "galena_gauntlet" in t:
        return [("alexscaves:scarlet_neodymium_ingot", 8)]
    if "notor_gizmo" in t:
        return [("alexscaves:magnetic_levitation_rail", 8)]
    if "magnetic_activator" in t:
        return [("alexscaves:heavyweight", 1)]
    if "resistor_shield" in t:
        return [("alexscaves:sulfur_dust", 16)]
    if "sulfur_dust" in t:
        return [("alexscaves:radon_bottle", 4)]
    if "radon_bottle" in t:
        return [("alexscaves:uranium", 8)]
    if "hazmat" in t:
        return [("alexscaves:uranium", 16)]
    if t.endswith("uranium"):
        return [("alexscaves:uranium_rod", 2)]
    if "uranium_rod" in t:
        return [("alexscaves:uranium_rod", 2)]
    if "raygun" in t:
        return [("alexscaves:amber", 16)]
    if t.endswith("amber"):
        return [("alexscaves:ambersol", 8)]
    if "ambersol" in t:
        return [("alexscaves:limestone_spear", 2)]
    if "limestone_spear" in t:
        return [("alexscaves:dinosaur_nugget", 8)]
    if "primordial_soup" in t:
        return [("alexscaves:primordial_tunic", 1)]
    if "primordial_tunic" in t:
        return [("alexscaves:pearl", 8)]
    if t.endswith("pearl") and "gazing" not in t:
        return [("alexscaves:depth_charge", 4)]
    if "sea_staff" in t:
        return [("alexscaves:pearl", 8)]
    if "gazing_pearl" in t:
        return [("alexscaves:depth_charge", 8)]
    if "depth_charge" in t:
        return [("alexscaves:caramel", 16)]
    if t.endswith("caramel"):
        return [("alexscaves:candy_cane", 8)]
    if "candy_cane" in t:
        return [("alexscaves:purple_soda_bottle", 4)]
    if "purple_soda" in t:
        return [("alexscaves:frostmint", 8)]
    if "frostmint" in t:
        return [("alexscaves:moth_ball", 8)]
    if "pure_darkness" in t:
        return [("alexscaves:moth_ball", 8)]
    if "hood_of_darkness" in t:
        return [("alexscaves:vesper_wing", 4)]
    if "cloak_of_darkness" in t:
        return [("alexscaves:occult_gem", 1)]
    if "occult_gem" in t:
        return [("alexscaves:moth_dust", 8)]
    if "desolate_dagger" in t:
        return [("alexscaves:totem_of_possession", 1)]
    return [("alexscaves:amber", 8)]


def _ae2(t: str) -> list[tuple[str, int]]:
    certus = "ae2:certus_quartz_crystal"
    charged = "ae2:charged_certus_quartz_crystal"
    fluix = "ae2:fluix_crystal"
    glass = "ae2:fluix_glass_cable"
    if "guide" in t:
        return [(certus, 16)]
    if "charged_certus" in t:
        return [(charged, 8), (fluix, 8)]
    if "certus_quartz" in t:
        return [(certus, 16)]
    if "sky_stone" in t:
        return [("ae2:sky_stone_block", 16)]
    if "fluix_crystal" in t:
        return [(fluix, 16), ("ae2:silicon", 8)]
    if t == "silicon" or t.endswith(":silicon"):
        return [("ae2:silicon", 16)]
    if "silicon_press" in t:
        return [("ae2:logic_processor_press", 1)]
    if "inscriber" in t:
        return [("ae2:printed_silicon", 8)]
    if "charger" in t:
        return [(charged, 8)]
    if "printed_silicon" in t:
        return [("ae2:silicon", 16)]
    if "logic_processor" in t:
        return [("ae2:logic_processor", 8), (glass, 8)]
    if "calculation_processor" in t:
        return [("ae2:calculation_processor", 8), ("ae2:cell_component_1k", 2)]
    if "engineering_processor" in t:
        return [("ae2:engineering_processor", 8)]
    if "quartz_fiber" in t:
        return [("ae2:quartz_fiber", 8), (glass, 8)]
    if "fluix_glass_cable" in t:
        return [(glass, 16)]
    if "network_tool" in t:
        return [(glass, 8)]
    if "energy_acceptor" in t:
        return [("ae2:energy_cell", 1)]
    if t == "energy_cell":
        return [(glass, 8)]
    if "dense_energy_cell" in t:
        return [("ae2:energy_cell", 2)]
    if t == "drive":
        return [("ae2:item_cell_housing", 2)]
    if "item_cell_housing" in t:
        return [("ae2:cell_component_1k", 2)]
    if "cell_component_1k" in t:
        return [("ae2:item_storage_cell_1k", 2)]
    if "item_storage_cell_1k" in t:
        return [("ae2:terminal", 1)]
    if t == "terminal":
        return [("ae2:import_bus", 2)]
    if "import_bus" in t:
        return [("ae2:export_bus", 2)]
    if "export_bus" in t:
        return [("ae2:storage_bus", 2)]
    if "storage_bus" in t:
        return [("ae2:interface", 1)]
    if t == "interface":
        return [("ae2:io_port", 1)]
    if "io_port" in t:
        return [("ae2:item_storage_cell_4k", 1)]
    if "controller" in t:
        return [("ae2:fluix_smart_cable", 8)]
    if "fluix_smart_cable" in t:
        return [("ae2:fluix_covered_dense_cable", 4)]
    if "growth_accelerator" in t:
        return [(charged, 8)]
    if "item_storage_cell_4k" in t:
        return [("ae2:cell_component_16k", 1)]
    if "item_storage_cell_16k" in t:
        return [("ae2:item_storage_cell_16k", 1)]
    if "pattern_provider" in t:
        return [("ae2:blank_pattern", 8)]
    if "blank_pattern" in t:
        return [("ae2:blank_pattern", 16)]
    if "molecular_assembler" in t:
        return [("ae2:crafting_terminal", 1)]
    if "crafting_terminal" in t:
        return [("ae2:crafting_unit", 2)]
    if "crafting_unit" in t:
        return [("ae2:1k_crafting_storage", 1)]
    if "1k_crafting_storage" in t:
        return [("ae2:crafting_accelerator", 1)]
    if "fluid_storage_cell_1k" in t:
        return [("ae2:fluid_storage_cell_1k", 1)]
    if "item_storage_cell_64k" in t:
        return [("ae2:item_storage_cell_64k", 1), ("ae2:fluid_storage_cell_64k", 1)]
    if "silicon" in t:
        return [("ae2:silicon", 16)]
    return [(glass, 8)]


def _avaritia(t: str, ns: str) -> list[tuple[str, int]]:
    nn = "avaritia:neutron_nugget"
    ni = "avaritia:neutron_ingot"
    mx = "avaritia:crystal_matrix_ingot"
    if "diamond_lattice" in t:
        return [("avaritia:diamond_lattice", 4)]
    if "crystal_matrix_ingot" in t:
        return [(mx, 2)]
    if "compressed_crafting_table" in t:
        return [("minecraft:diamond", 16)]
    if "double_compressed" in t:
        return [("minecraft:diamond", 16), (mx, 1)]
    if "nether_crafting" in t:
        return [("minecraft:netherite_ingot", 1)]
    if "sculk_crafting" in t:
        return [("minecraft:echo_shard", 8)]
    if "end_crafting" in t:
        return [("minecraft:ender_pearl", 16)]
    if "extreme_crafting_table" in t:
        return [(mx, 2)]
    if "extreme_anvil" in t:
        return [("minecraft:iron_block", 8)]
    if "extreme_smithing" in t:
        return [("avaritia:upgrade_smithing_template", 1), (mx, 1)]
    if "neutron_collector" in t:
        return [("avaritia:neutron_pile", 32)]
    if "neutron_pile" in t:
        return [("avaritia:neutron_pile", 32)]
    if "neutron_nugget" in t:
        return [(nn, 16)]
    if "neutron_ingot" in t:
        return [(ni, 2)]
    if "neutron_gear" in t:
        return [(nn, 16)]
    if "star_fuel" in t:
        return [("avaritia:refined_coal", 32)]
    if "neutron_compressor" in t:
        return [("avaritia:neutron_pile", 32)]
    if "singularity" in t:
        return [(nn, 16)]
    if "infinity_catalyst" in t:
        return [(ni, 2)]
    if t.endswith("infinity_ingot"):
        return [(ni, 4)]
    if "ultimate_stew" in t:
        return [("avaritia:cosmic_meatballs", 4)]
    if "blaze_cube" in t:
        return [("avaritia:blaze_cube", 4)]
    if "blaze_helmet" in t or "blaze_chest" in t:
        return [("avaritia:blaze_cube", 4)]
    if "crystal_core" in t or "crystal_helmet" in t:
        return [(mx, 2)]
    if t.startswith("infinity_") or "infinity_sword" in t or "infinity_pick" in t or "infinity_chest" in t or "infinity_helmet" in t:
        return [(ni, 2)]
    if "endest_pearl" in t:
        return [(nn, 16)]
    if "compressed_chest" in t:
        return [("avaritia:compressed_chest", 1)]
    return [(nn, 8)]


XP_RE = re.compile(
    r'\{\s*\n\t\t\t\tid: "[^"]+"\s*\n\t\t\t\ttype: "xp"\s*\n\t\t\t\txp: \d+\s*\n\t\t\t\}',
    re.M,
)
QUEST_RE = re.compile(r"(		\{[\s\S]*?\n		\})")
TASK_ITEM_RE = re.compile(
    r'tasks: \[[\s\S]*?item: (?:"([^"]+)"|\{[^}]*id: "([^"]+)")',
)


def _format_item(qid: str, item: str, count: int, idx: int) -> str:
    """FTB ItemReward needs an ItemStack compound. Bare string IDs render as ? crate."""
    rid = hid(f"{qid}_{item}_{idx}_useful")
    n = max(1, int(count))
    return (
        "{\n"
        f'				id: "{rid}"\n'
        "				item: {\n"
        f"					Count: {n}b\n"
        f'					id: "{item}"\n'
        "				}\n"
        '				type: "item"\n'
        "			}"
    )


def apply_to_file(path: Path, models: set[str]) -> int:
    name = path.name
    if name in SKIP_FILES:
        return 0
    chapter = CHAPTER_KEY.get(name)
    if not chapter:
        return 0
    text = path.read_text(encoding="utf-8")
    chunks: list[str] = []
    last = 0
    changed = 0
    for m in QUEST_RE.finditer(text):
        chunks.append(text[last:m.start()])
        block = m.group(1)
        last = m.end()
        rm = re.search(r"rewards: \[([\s\S]*?)\]\s*\n\t\t\ttasks:", block)
        if not rm:
            chunks.append(block)
            continue
        body = rm.group(1)
        if "tag: {" in body:
            chunks.append(block)
            continue
        tm = TASK_ITEM_RE.search(block)
        task = (tm.group(1) or tm.group(2)) if tm else ""
        if chapter == "info" and "starcatcher:" in body:
            chunks.append(block)
            continue
        pack = pack_for(task, chapter) if task else []
        if not pack:
            chunks.append(block)
            continue
        for item, _c in pack:
            if not item.startswith("minecraft:") and item not in models:
                raise SystemExit(f"{name} reward missing model {item} (task {task})")
        qid_m = re.search(r'^\t\t\tid: "([0-9A-Fa-f]+)"', block, re.M)
        qid = qid_m.group(1) if qid_m else hid(name + task)
        xp_bits = XP_RE.findall(body)
        item_bits = [_format_item(qid, it, cnt, i) for i, (it, cnt) in enumerate(pack)]
        new_body = ",".join(item_bits + xp_bits)
        block = block[: rm.start(1)] + new_body + block[rm.end(1) :]
        chunks.append(block)
        changed += 1
    chunks.append(text[last:])
    path.write_text("".join(chunks), encoding="utf-8")
    return changed


STRING_ITEM_REWARD = re.compile(
    r"\{\s*\n"
    r"(?:\t\t\t\tcount: (\d+)\s*\n)?"
    r'\t\t\t\tid: "([^"]+)"\s*\n'
    r"(?:\t\t\t\tcount: (\d+)\s*\n)?"
    r'\t\t\t\titem: "([^"]+)"\s*\n'
    r'\t\t\t\ttype: "item"\s*\n'
    r"\t\t\t\}"
)


def wrap_string_rewards(path: Path) -> int:
    """Keep reward items, rewrite to FTB ItemStack compound so they render."""
    text = path.read_text(encoding="utf-8")
    n = 0

    def repl(m: re.Match) -> str:
        nonlocal n
        n += 1
        count = m.group(1) or m.group(3) or "1"
        rid = m.group(2)
        item = m.group(4)
        return (
            "{\n"
            f'				id: "{rid}"\n'
            "				item: {\n"
            f"					Count: {count}b\n"
            f'					id: "{item}"\n'
            "				}\n"
            '				type: "item"\n'
            "			}"
        )

    new = STRING_ITEM_REWARD.sub(repl, text)
    if new != text:
        path.write_text(new, encoding="utf-8")
    return n


def apply_all(cfg: Path, srv: Path, models: set[str]) -> None:
    for folder in (cfg, srv):
        for path in sorted(folder.glob("*.snbt")):
            if path.name in SKIP_FILES:
                w = wrap_string_rewards(path)
                if w:
                    print(f"  wrap {path.name}: {w}")
                continue
            n = apply_to_file(path, models)
            if n:
                print(f"  useful {path.name}: {n}")
