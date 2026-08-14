# -*- coding: utf-8 -*-
"""Write AquaTech premium cases (panel + expensive crates) to config + server."""
from __future__ import annotations

import json
from pathlib import Path

ROOTS = [
    Path(r"C:\Users\xieto\Desktop\AquaTech\config\casesmod\cases"),
    Path(r"C:\Users\xieto\Desktop\AquaTech\server\config\casesmod\cases"),
]


def item(item_id, count, weight, rarity, name, cmd=""):
    return {
        "itemId": item_id,
        "count": count,
        "weight": float(weight),
        "rarity": rarity,
        "displayName": name,
        "command": cmd,
    }


CASES = {
    "panel_case": {
        "id": "panel_case",
        "displayName": "§e§lSolar Panel Crate",
        "iconItemId": "industrialupgrade:machines/advanced_solar_paneliu",
        "price": 2500,
        "pityThreshold": 10,
        "pityRarity": "EPIC",
        "items": [
            item("industrialupgrade:basemachine3/solar_iu", 1, 22, "COMMON", "§7Solar Panel"),
            item("industrialupgrade:basemachine3/minipanel", 2, 18, "COMMON", "§7Mini Panel ×2"),
            item("industrialupgrade:machines/advanced_solar_paneliu", 1, 16, "UNCOMMON", "§aAdvanced Solar Panel", "/aquatech grantxp %player% 80"),
            item("industrialupgrade:machines/hybrid_solar_paneliu", 1, 12, "UNCOMMON", "§3Hybrid Solar Panel", "/aquatech grantxp %player% 100"),
            item("industrialupgrade:machines/ultimate_solar_paneliu", 1, 9, "RARE", "§9Perfect Hybrid Solar Panel", "/aquatech grantxp %player% 150"),
            item("industrialupgrade:machines/quantum_solar_paneliu", 1, 7, "RARE", "§2Quantum Solar Panel", "/aquatech grantxp %player% 180"),
            item("industrialupgrade:machines/spectral_solar_panel", 1, 5, "EPIC", "§eSpectral Solar Panel", "/aquatech grantxp %player% 220"),
            item("industrialupgrade:machines/singular_solar_panel", 1, 4, "EPIC", "§8Singular Solar Panel", "/aquatech grantxp %player% 250"),
            item("industrialupgrade:machines/proton_solar_panel", 1, 3, "EPIC", "§5Protonic Solar Panel", "/aquatech grantxp %player% 280"),
            item("industrialupgrade:machines/neutronium_solar_panel", 1, 2.2, "LEGENDARY", "§9Neutron Solar Panel", "/aquatech grantxp %player% 350"),
            item("industrialupgrade:machines/photonic_solar_panel", 1, 1.5, "LEGENDARY", "§aPhotonic Solar Panel", "/aquatech grantxp %player% 400"),
            item("industrialupgrade:machines/graviton_solar_panel", 1, 0.8, "LEGENDARY", "§bGraviton Solar Panel", "/aquatech grantxp %player% 500"),
            item("industrialupgrade:admpanel/admpanel", 1, 0.3, "LEGENDARY", "§a§lAdministrative Solar Panel", "/aquatech grantxp %player% 750"),
        ],
    },
    "plate_case": {
        "id": "plate_case",
        "displayName": "§b§lPlate Crate",
        "iconItemId": "industrialupgrade:itemplates/bronze_plate",
        "price": 600,
        "pityThreshold": 15,
        "pityRarity": "RARE",
        "items": [
            item("industrialupgrade:itemplates/iron_plate", 32, 20, "COMMON", "§7Iron Plate ×32"),
            item("industrialupgrade:itemplates/copper_plate", 32, 18, "COMMON", "§7Copper Plate ×32"),
            item("industrialupgrade:itemplates/tin_plate", 32, 16, "COMMON", "§7Tin Plate ×32"),
            item("industrialupgrade:itemplates/bronze_plate", 24, 14, "UNCOMMON", "§aBronze Plate ×24", "/aquatech grantxp %player% 40"),
            item("industrialupgrade:casing/copper", 8, 12, "UNCOMMON", "§aCopper Casing ×8", "/aquatech grantxp %player% 50"),
            item("industrialupgrade:casing/bronze", 8, 10, "UNCOMMON", "§aBronze Casing ×8", "/aquatech grantxp %player% 60"),
            item("industrialupgrade:casing/steel", 6, 8, "RARE", "§9Steel Casing ×6", "/aquatech grantxp %player% 100"),
            item("industrialupgrade:casing/aluminium", 6, 6, "RARE", "§9Aluminium Casing ×6", "/aquatech grantxp %player% 110"),
            item("industrialupgrade:blockresource/machine", 4, 4, "EPIC", "§5Machine Block ×4", "/aquatech grantxp %player% 160"),
            item("industrialupgrade:alloycasing/stainless_steel", 4, 2, "LEGENDARY", "§6Stainless Steel Casing ×4", "/aquatech grantxp %player% 220"),
        ],
    },
    "circuit_case": {
        "id": "circuit_case",
        "displayName": "§d§lCircuit Crate",
        "iconItemId": "industrialupgrade:crafting_elements/crafting_273_element",
        "price": 900,
        "pityThreshold": 12,
        "pityRarity": "EPIC",
        "items": [
            item("industrialupgrade:crafting_elements/crafting_272_element", 4, 28, "COMMON", "§7Electronic Circuit ×4"),
            item("industrialupgrade:crafting_elements/crafting_272_element", 8, 18, "UNCOMMON", "§aElectronic Circuit ×8", "/aquatech grantxp %player% 50"),
            item("industrialupgrade:crafting_elements/crafting_273_element", 2, 16, "UNCOMMON", "§aAdvanced Circuit ×2", "/aquatech grantxp %player% 70"),
            item("industrialupgrade:crafting_elements/crafting_273_element", 4, 12, "RARE", "§9Advanced Circuit ×4", "/aquatech grantxp %player% 120"),
            item("industrialupgrade:crafting_elements/crafting_40_element", 2, 8, "RARE", "§9IU Crafting Element ×2", "/aquatech grantxp %player% 100"),
            item("industrialupgrade:crafting_elements/crafting_290_element", 8, 8, "RARE", "§9Sticky Resin ×8", "/aquatech grantxp %player% 80"),
            item("industrialupgrade:cable/tin_cable", 64, 6, "EPIC", "§5Tin Cable ×64", "/aquatech grantxp %player% 150"),
            item("ae2:logic_processor", 4, 3, "EPIC", "§5AE2 Logic Processor ×4", "/aquatech grantxp %player% 180"),
            item("ae2:engineering_processor", 2, 1.5, "LEGENDARY", "§6AE2 Engineering Processor ×2", "/aquatech grantxp %player% 250"),
            item("ae2:calculation_processor", 2, 1.0, "LEGENDARY", "§6AE2 Calculation Processor ×2", "/aquatech grantxp %player% 250"),
        ],
    },
    "oil_case": {
        "id": "oil_case",
        "displayName": "§6§lOil Baron Crate",
        "iconItemId": "industrialupgrade:bucket/blackoil",
        "price": 1200,
        "pityThreshold": 12,
        "pityRarity": "EPIC",
        "items": [
            item("industrialupgrade:bucket/sour_light_oil", 2, 20, "COMMON", "§7Sour Light Oil ×2"),
            item("industrialupgrade:bucket/sweet_medium_oil", 2, 18, "COMMON", "§7Sweet Medium Oil ×2"),
            item("industrialupgrade:bucket/sour_medium_oil", 2, 14, "UNCOMMON", "§aSour Medium Oil ×2", "/aquatech grantxp %player% 40"),
            item("industrialupgrade:bucket/sweet_heavy_oil", 2, 12, "UNCOMMON", "§aSweet Heavy Oil ×2", "/aquatech grantxp %player% 50"),
            item("industrialupgrade:bucket/sour_heavy_oil", 2, 10, "RARE", "§9Sour Heavy Oil ×2", "/aquatech grantxp %player% 80"),
            item("industrialupgrade:bucket/blackoil", 4, 10, "RARE", "§9Fuel Oil ×4", "/aquatech grantxp %player% 100"),
            item("industrialupgrade:bucket/industrialoil", 2, 8, "EPIC", "§5Industrial Oil ×2", "/aquatech grantxp %player% 160"),
            item("industrialupgrade:bucket/motoroil", 2, 5, "EPIC", "§5Motor Oil ×2", "/aquatech grantxp %player% 180"),
            item("industrialupgrade:sensor/sensor_oil", 1, 2, "LEGENDARY", "§6Oil Sensor", "/aquatech grantxp %player% 220"),
            item("industrialupgrade:basemachine3/wireless_oil_pump", 1, 1, "LEGENDARY", "§6Wireless Oil Pump", "/aquatech grantxp %player% 400"),
        ],
    },
    "legend_case": {
        "id": "legend_case",
        "displayName": "§c§l§nOcean Legend Crate",
        "iconItemId": "aquatech_ui:abyssal_fishing_rod",
        "price": 5000,
        "pityThreshold": 8,
        "pityRarity": "EPIC",
        "items": [
            item("industrialupgrade:machines/spectral_solar_panel", 1, 14, "COMMON", "§7Spectral Solar Panel"),
            item("industrialupgrade:machines/singular_solar_panel", 1, 12, "UNCOMMON", "§aSingular Solar Panel", "/aquatech grantxp %player% 100"),
            item("industrialupgrade:machines/proton_solar_panel", 1, 10, "UNCOMMON", "§aProtonic Solar Panel", "/aquatech grantxp %player% 120"),
            item("aquatech_ui:thermal_fishing_rod", 1, 10, "RARE", "§9Thermal Fishing Rod", "/aquatech grantxp %player% 150"),
            item("aquatech_ui:ender_fishing_rod", 1, 8, "RARE", "§9Ender Fishing Rod", "/aquatech grantxp %player% 180"),
            item("aquatech_ui:abyssal_lure", 1, 8, "RARE", "§9Abyssal Lure", "/aquatech grantxp %player% 160"),
            item("aquatech_ui:sonar_goggles", 1, 7, "EPIC", "§5Sonar Goggles", "/aquatech grantxp %player% 220"),
            item("industrialupgrade:machines/photonic_solar_panel", 1, 6, "EPIC", "§5Photonic Solar Panel", "/aquatech grantxp %player% 300"),
            item("industrialupgrade:machines/graviton_solar_panel", 1, 4, "EPIC", "§5Graviton Solar Panel", "/aquatech grantxp %player% 350"),
            item("aquatech_ui:abyssal_fishing_rod", 1, 3, "LEGENDARY", "§6Abyssal Fishing Rod", "/aquatech grantxp %player% 400"),
            item("aquatech_ui:neptune_trident", 1, 1.5, "LEGENDARY", "§6Neptune Trident", "/aquatech grantxp %player% 500"),
            item("industrialupgrade:admpanel/admpanel", 1, 0.8, "LEGENDARY", "§a§lAdmin Solar Panel", "/aquatech grantxp %player% 750"),
            item("minecraft:nether_star", 1, 0.7, "LEGENDARY", "§6Nether Star", "/aquatech grantxp %player% 300"),
        ],
    },
    "bee_case": {
        "id": "bee_case",
        "displayName": "§a§lHive Crate",
        "iconItemId": "industrialupgrade:jar_bee/bees",
        "price": 700,
        "pityThreshold": 14,
        "pityRarity": "RARE",
        "items": [
            item("industrialupgrade:jar_bee/bees", 1, 22, "COMMON", "§7Bee Jar"),
            item("industrialupgrade:jar_bee/forest_bee", 1, 14, "COMMON", "§7Forest Bee"),
            item("industrialupgrade:jar_bee/plains_bee", 1, 14, "COMMON", "§7Plains Bee"),
            item("industrialupgrade:jar_bee/swamp_bee", 1, 12, "UNCOMMON", "§aSwamp Bee", "/aquatech grantxp %player% 40"),
            item("industrialupgrade:jar_bee/tropical_bee", 1, 12, "UNCOMMON", "§aTropical Bee", "/aquatech grantxp %player% 40"),
            item("industrialupgrade:jar_bee/winter_bee", 1, 10, "RARE", "§9Winter Bee", "/aquatech grantxp %player% 80"),
            item("industrialupgrade:sapling/rubber_sapling", 4, 8, "RARE", "§9Hevea Sapling ×4", "/aquatech grantxp %player% 60"),
            item("industrialupgrade:raw_latex", 32, 6, "EPIC", "§5Raw Latex ×32", "/aquatech grantxp %player% 120"),
            item("industrialupgrade:synthetic_rubber", 32, 2, "LEGENDARY", "§6Synthetic Rubber ×32", "/aquatech grantxp %player% 200"),
        ],
    },
}


def main():
    for root in ROOTS:
        root.mkdir(parents=True, exist_ok=True)
        for case_id, data in CASES.items():
            path = root / f"{case_id}.json"
            path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
            print("wrote", path)
    print("total cases written:", len(CASES), "×", len(ROOTS), "roots")


if __name__ == "__main__":
    main()
