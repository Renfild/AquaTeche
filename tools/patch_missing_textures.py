#!/usr/bin/env python3
"""
Patch missing item textures into extracted_case_textures.json.
Handles non-standard IU item IDs that use slash-paths like industrialupgrade:machines/barion_solar_panel.
"""
import json
import base64
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
OUT_FILE = ROOT / "tools/extracted_case_textures.json"

IU_JAR  = ROOT / "server/mods/IndustrialUpgrade-1.20.1-3.4.0.11.jar"
AC_JAR  = ROOT / "server/mods/alexscaves-2.0.2.jar"
AT_JAR  = ROOT / "server/mods/aquatech_ui-1.0.24.jar"
AV_JAR  = ROOT / "server/mods/Re-Avaritia-forge-1.20.1-1.4.1-release.jar"

def b64(z, path):
    return "data:image/png;base64," + base64.b64encode(z.read(path)).decode("ascii")

MANUAL_MAP = {
    "industrialupgrade:machines/barion_solar_panel":
        (IU_JAR, "assets/industrialupgrade/textures/block/solar_panels/barion/glass.png"),
    "industrialupgrade:machines/graviton_solar_panel":
        (IU_JAR, "assets/industrialupgrade/textures/block/solar_panels/graviton/glass.png"),
    "industrialupgrade:machines/hadron_solar_panel":
        (IU_JAR, "assets/industrialupgrade/textures/block/solar_panels/hadron/glass.png"),
    "industrialupgrade:machines/neutronium_solar_panel":
        (IU_JAR, "assets/industrialupgrade/textures/block/solar_panels/neutronium/glass.png"),
    "industrialupgrade:machines/photonic_solar_panel":
        (IU_JAR, "assets/industrialupgrade/textures/block/solar_panels/photonic/glass.png"),
    "industrialupgrade:baseore/titanium":
        (IU_JAR, "assets/industrialupgrade/textures/item/titanium_ingot.png"),
    "industrialupgrade:baseore2/strontium":
        (IU_JAR, "assets/industrialupgrade/textures/item/raw_ingot_strontium.png"),
    "industrialupgrade:baseore2/thallium":
        (IU_JAR, "assets/industrialupgrade/textures/item/raw_ingot_thallium.png"),
    "industrialupgrade:baseore2/yttrium":
        (IU_JAR, "assets/industrialupgrade/textures/item/raw_ingot_yttrium.png"),
    "industrialupgrade:alloyingot/adamantium":
        (IU_JAR, "assets/industrialupgrade/textures/item/adamantite_ingot.png"),
    "industrialupgrade:blockresource/adv_machine":
        (IU_JAR, "assets/industrialupgrade/textures/block/advanced_machine.png"),
    "industrialupgrade:crafting_elements/crafting_273_element":
        (IU_JAR, "assets/industrialupgrade/textures/item/advanced_circuit.png"),
    "industrialupgrade:crafting_elements/crafting_274_element":
        (IU_JAR, "assets/industrialupgrade/textures/item/advanced_circuit_1.png"),
    "industrialupgrade:rubber_drop/rubber_drop":
        (IU_JAR, "assets/industrialupgrade/textures/item/rubber.png"),
    "industrialupgrade:upgrades/overcloker_upgrade":
        (IU_JAR, "assets/industrialupgrade/textures/item/upgrade/overclocker.png"),
    "alexscaves:abyssal_pearl":
        (AC_JAR, "assets/alexscaves/textures/item/pearl.png"),
    "aquatech_ui:upgrade_speed_x4":
        (AT_JAR, "assets/aquatech_ui/textures/item/speed_upgrade.png"),
    # Avaritia blocks
    "avaritia:extreme_crafting_table":
        (AV_JAR, "assets/avaritia/textures/block/machine/craft/extreme_top.png"),
    "avaritia:neutronium_compressor":
        (AV_JAR, "assets/avaritia/textures/block/machine/compressor/compressor_top.png"),
}

# Side textures for 2.5D CSS isometric blocks — stored with __side suffix
SIDE_TEXTURES = {
    "industrialupgrade:machines/barion_solar_panel":
        (IU_JAR, "assets/industrialupgrade/textures/block/solar_panels/barion/side.png"),
    "industrialupgrade:machines/graviton_solar_panel":
        (IU_JAR, "assets/industrialupgrade/textures/block/solar_panels/graviton/side.png"),
    "industrialupgrade:machines/hadron_solar_panel":
        (IU_JAR, "assets/industrialupgrade/textures/block/solar_panels/hadron/side.png"),
    "industrialupgrade:machines/neutronium_solar_panel":
        (IU_JAR, "assets/industrialupgrade/textures/block/solar_panels/neutronium/side.png"),
    "industrialupgrade:machines/photonic_solar_panel":
        (IU_JAR, "assets/industrialupgrade/textures/block/solar_panels/photonic/side.png"),
    # Avaritia block sides
    "avaritia:extreme_crafting_table":
        (AV_JAR, "assets/avaritia/textures/block/machine/craft/extreme_side.png"),
    "avaritia:neutronium_compressor":
        (AV_JAR, "assets/avaritia/textures/block/machine/compressor/compressor_side_left.png"),
}


def main():
    with open(OUT_FILE, "r", encoding="utf-8") as f:
        extracted = json.load(f)

    patched = 0
    for item_id, (jar_path, tex_path) in MANUAL_MAP.items():
        if item_id in extracted:
            continue
        if not jar_path.exists():
            print(f"  [!] JAR not found: {jar_path.name}")
            continue
        try:
            with zipfile.ZipFile(jar_path, "r") as z:
                extracted[item_id] = b64(z, tex_path)
                print(f"  [+] {item_id} -> {tex_path}")
                patched += 1
        except KeyError:
            print(f"  [!] Texture not in jar: {tex_path}")

    for item_id, (jar_path, tex_path) in SIDE_TEXTURES.items():
        key = item_id + "__side"
        if key in extracted:
            continue
        if not jar_path.exists():
            continue
        try:
            with zipfile.ZipFile(jar_path, "r") as z:
                extracted[key] = b64(z, tex_path)
                print(f"  [+] {key}")
                patched += 1
        except KeyError:
            pass

    with open(OUT_FILE, "w", encoding="utf-8") as f:
        json.dump(extracted, f, ensure_ascii=False)

    print(f"\nPatched {patched} textures. Total: {len(extracted)}")


if __name__ == "__main__":
    main()
