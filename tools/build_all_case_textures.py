#!/usr/bin/env python3
"""
Master Case Asset Processor & Texture Builder for AquaTech.
- Extracts item & block textures from server mod JARs.
- Auto-crops animated texture strips (e.g. Avaritia Infinity Ingot, Infinity Catalyst, Dragon Heart) to single crisp 16x16 frames.
- Pre-renders authentic Minecraft 3D isometric block sprites (30° dimetric projection with shaded faces) for all blocks/machines/solar panels.
- Outputs tools/extracted_case_textures.json with pure base64 data URIs.
"""
import base64
import io
import json
import os
import zipfile
from pathlib import Path
from PIL import Image, ImageEnhance

ROOT = Path(__file__).resolve().parent.parent
CASES_JSON = ROOT / "config/aqualumen/cases.json"
OUT_FILE = ROOT / "tools/extracted_case_textures.json"

# Mod JARs
IU_JAR = ROOT / "server/mods/IndustrialUpgrade-1.20.1-3.4.0.11.jar"
AV_JAR = ROOT / "server/mods/Re-Avaritia-forge-1.20.1-1.4.1-release.jar"
DE_JAR = ROOT / "server/mods/Draconic-Evolution-1.20.1-3.1.2.621-universal.jar"
AE_JAR = ROOT / "server/mods/appliedenergistics2-forge-15.4.10.jar"
AC_JAR = ROOT / "server/mods/alexscaves-2.0.2.jar"
AT_JAR = ROOT / "server/mods/aquatech_ui-1.0.24.jar"
BOT_JAR = ROOT / "server/mods/Botania-1.20.1-446-FORGE.jar"
MB_JAR = ROOT / "server/mods/MythicBotany-1.20.1-4.0.4.jar"
EB_JAR = ROOT / "server/mods/ExtraBotany-1.20.1-5.1.0.jar"
EC_JAR = ROOT / "server/mods/ExtendedCrafting-1.20.1-6.0.7.jar"


def render_minecraft_block(top_img: Image.Image, side_img: Image.Image, out_size: int = 64) -> Image.Image:
    """Renders authentic Minecraft inventory 3D block sprite."""
    top_img = top_img.convert("RGBA")
    side_img = side_img.convert("RGBA")

    # Crop animated strip to 1st square
    tw, th = top_img.size
    if th > tw:
        top_img = top_img.crop((0, 0, tw, tw))
    sw, sh = side_img.size
    if sh > sw:
        side_img = side_img.crop((0, 0, sw, sw))

    top_16 = top_img.resize((16, 16), Image.NEAREST)
    side_16 = side_img.resize((16, 16), Image.NEAREST)

    # Shading
    left_side = ImageEnhance.Brightness(side_16.copy()).enhance(0.62)
    right_side = ImageEnhance.Brightness(side_16.copy()).enhance(0.82)

    canvas = Image.new("RGBA", (34, 34), (0, 0, 0, 0))
    top_pix = top_16.load()
    left_pix = left_side.load()
    right_pix = right_side.load()

    # Draw Top Face
    for u in range(16):
        for v in range(16):
            col = top_pix[u, v]
            if col[3] == 0:
                continue
            px = 16 + u - v
            py = 1 + (u + v) // 2
            canvas.putpixel((px, py), col)
            if (u + v) % 2 == 1:
                canvas.putpixel((px - 1, py), col)

    # Draw Left Face
    for u in range(16):
        for v in range(16):
            col = left_pix[u, v]
            if col[3] == 0:
                continue
            px = 1 + u
            py = 9 + (u // 2) + v
            canvas.putpixel((px, py), col)

    # Draw Right Face
    for u in range(16):
        for v in range(16):
            col = right_pix[u, v]
            if col[3] == 0:
                continue
            px = 17 + u
            py = 16 - (u // 2) + v
            if 0 <= px < 34 and 0 <= py < 34:
                canvas.putpixel((px, py), col)

    return canvas.resize((out_size, out_size), Image.NEAREST)


def img_to_b64(img: Image.Image) -> str:
    buf = io.BytesIO()
    img.save(buf, format="PNG")
    return "data:image/png;base64," + base64.b64encode(buf.getvalue()).decode("ascii")


def read_jar_img(jar_path: Path, inner_path: str) -> Image.Image:
    with zipfile.ZipFile(jar_path, "r") as z:
        raw = z.read(inner_path)
        im = Image.open(io.BytesIO(raw)).convert("RGBA")
        # Crop animated texture strip to 1st square frame!
        w, h = im.size
        if h > w:
            im = im.crop((0, 0, w, w))
        return im


# Block definitions that should be pre-rendered as 3D isometric blocks:
BLOCK_MAP = {
    # Solar panels
    "industrialupgrade:machines/spectral_solar_panel": (
        IU_JAR,
        "assets/industrialupgrade/textures/block/solar_panels/spectral/glass.png",
        "assets/industrialupgrade/textures/block/solar_panels/spectral/side.png",
    ),
    "industrialupgrade:machines/photonic_solar_panel": (
        IU_JAR,
        "assets/industrialupgrade/textures/block/solar_panels/photonic/glass.png",
        "assets/industrialupgrade/textures/block/solar_panels/photonic/side.png",
    ),
    "industrialupgrade:machines/neutronium_solar_panel": (
        IU_JAR,
        "assets/industrialupgrade/textures/block/solar_panels/neutronium/glass.png",
        "assets/industrialupgrade/textures/block/solar_panels/neutronium/side.png",
    ),
    "industrialupgrade:machines/barion_solar_panel": (
        IU_JAR,
        "assets/industrialupgrade/textures/block/solar_panels/barion/glass.png",
        "assets/industrialupgrade/textures/block/solar_panels/barion/side.png",
    ),
    "industrialupgrade:machines/hadron_solar_panel": (
        IU_JAR,
        "assets/industrialupgrade/textures/block/solar_panels/hadron/glass.png",
        "assets/industrialupgrade/textures/block/solar_panels/hadron/side.png",
    ),
    "industrialupgrade:machines/graviton_solar_panel": (
        IU_JAR,
        "assets/industrialupgrade/textures/block/solar_panels/graviton/glass.png",
        "assets/industrialupgrade/textures/block/solar_panels/graviton/side.png",
    ),
    # Avaritia
    "avaritia:extreme_crafting_table": (
        AV_JAR,
        "assets/avaritia/textures/block/machine/craft/extreme_top.png",
        "assets/avaritia/textures/block/machine/craft/extreme_side.png",
    ),
    "avaritia:neutron_compressor": (
        AV_JAR,
        "assets/avaritia/textures/block/machine/compressor/compressor_top.png",
        "assets/avaritia/textures/block/machine/compressor/compressor_side_left.png",
    ),
    # Draconic Evolution
    "draconicevolution:awakened_draconium_block": (
        DE_JAR,
        "assets/draconicevolution/textures/block/awakened_draconium_block.png",
        "assets/draconicevolution/textures/block/awakened_draconium_block_side.png",
    ),
    "draconicevolution:basic_crafting_injector": (
        DE_JAR,
        "assets/draconicevolution/textures/block/crafting/injector_top.png",
        "assets/draconicevolution/textures/block/crafting/injector_base.png",
    ),
    # AE2
    "ae2:drive": (
        AE_JAR,
        "assets/ae2/textures/block/drive/drive_top.png",
        "assets/ae2/textures/block/drive/drive_front.png",
    ),
    # Industrial Upgrade machines & casings
    "industrialupgrade:blockresource/machine": (
        IU_JAR,
        "assets/industrialupgrade/textures/block/advanced_machine.png",
        "assets/industrialupgrade/textures/block/advanced_machine.png",
    ),
    "industrialupgrade:blockresource/advanced_machine": (
        IU_JAR,
        "assets/industrialupgrade/textures/block/advanced_machine.png",
        "assets/industrialupgrade/textures/block/advanced_machine.png",
    ),
    "industrialupgrade:smeltery/smeltery_controller": (
        IU_JAR,
        "assets/industrialupgrade/textures/block/smeltery_casing.png",
        "assets/industrialupgrade/textures/block/smeltery_controller_front.png",
    ),
    "industrialupgrade:smeltery/smeltery_casing": (
        IU_JAR,
        "assets/industrialupgrade/textures/block/smeltery_casing.png",
        "assets/industrialupgrade/textures/block/smeltery_casing.png",
    ),
    "industrialupgrade:classicore/lead": (
        IU_JAR,
        "assets/industrialupgrade/textures/block/lead_ore.png",
        "assets/industrialupgrade/textures/block/lead_ore.png",
    ),
}

# Explicit mappings for items with custom paths:
ITEM_MAP = {
    # Avaritia items (ensures 1-frame cropped rainbow textures!)
    "avaritia:infinity_ingot": (AV_JAR, "assets/avaritia/textures/item/resource/infinity/infinity_ingot.png"),
    "avaritia:infinity_catalyst": (AV_JAR, "assets/avaritia/textures/item/resource/infinity/infinity_catalyst.png"),
    "avaritia:neutron_ingot": (AV_JAR, "assets/avaritia/textures/item/resource/neutron/neutron_ingot.png"),
    "avaritia:crystal_matrix_ingot": (AV_JAR, "assets/avaritia/textures/item/resource/crystal/crystal_matrix_ingot.png"),
    "avaritia:neutron_nugget": (AV_JAR, "assets/avaritia/textures/item/resource/neutron/neutron_nugget.png"),
    # Draconic Evolution
    "draconicevolution:awakened_core": (DE_JAR, "assets/draconicevolution/textures/item/components/awakened_core.png"),
    "draconicevolution:awakened_draconium_ingot": (DE_JAR, "assets/draconicevolution/textures/item/components/awakened_draconium_ingot.png"),
    "draconicevolution:dragon_heart": (DE_JAR, "assets/draconicevolution/textures/item/components/dragon_heart.png"),
    "draconicevolution:draconium_core": (DE_JAR, "assets/draconicevolution/textures/item/components/draconium_core.png"),
    "draconicevolution:wyvern_core": (DE_JAR, "assets/draconicevolution/textures/item/components/wyvern_core.png"),
    "draconicevolution:draconium_ingot": (DE_JAR, "assets/draconicevolution/textures/item/components/draconium_ingot.png"),
    "draconicevolution:wyvern_energy_core": (DE_JAR, "assets/draconicevolution/textures/item/components/wyvern_energy_core.png"),
    # Industrial Upgrade Items
    "industrialupgrade:crafting_elements/crafting_773_element": (IU_JAR, "assets/industrialupgrade/textures/item/crafting_773_element.png"),
    "industrialupgrade:crafting_elements/crafting_772_element": (IU_JAR, "assets/industrialupgrade/textures/item/crafting_772_element.png"),
    "industrialupgrade:baseore/titanium": (IU_JAR, "assets/industrialupgrade/textures/item/titanium_ingot.png"),
    "industrialupgrade:baseore1/beryllium": (IU_JAR, "assets/industrialupgrade/textures/item/raw_ingot_barium.png"),
    "industrialupgrade:baseore2/strontium": (IU_JAR, "assets/industrialupgrade/textures/item/raw_ingot_strontium.png"),
    "industrialupgrade:baseore2/thallium": (IU_JAR, "assets/industrialupgrade/textures/item/raw_ingot_thallium.png"),
    "industrialupgrade:baseore2/yttrium": (IU_JAR, "assets/industrialupgrade/textures/item/raw_ingot_yttrium.png"),
    "industrialupgrade:itemingots/adamantium": (IU_JAR, "assets/industrialupgrade/textures/item/adamantite_ingot.png"),
    "industrialupgrade:alloyingot/inconel": (IU_JAR, "assets/industrialupgrade/textures/item/inconel_ingot.png"),
    "industrialupgrade:alloyingot/osmiridium": (IU_JAR, "assets/industrialupgrade/textures/item/osmiridium_ingot.png"),
    "industrialupgrade:itemingots/steel_ingot": (IU_JAR, "assets/industrialupgrade/textures/item/steel.png"),
    "industrialupgrade:itemingots/bronze_ingot": (IU_JAR, "assets/industrialupgrade/textures/item/bronze.png"),
    "industrialupgrade:crafting_elements/crafting_271_element": (IU_JAR, "assets/industrialupgrade/textures/item/rubber.png"),
    "industrialupgrade:crafting_elements/crafting_273_element": (IU_JAR, "assets/industrialupgrade/textures/item/advanced_circuit.png"),
    "industrialupgrade:crafting_elements/crafting_274_element": (IU_JAR, "assets/industrialupgrade/textures/item/advanced_circuit_1.png"),
    "industrialupgrade:upgrades/overclocker": (IU_JAR, "assets/industrialupgrade/textures/item/upgrade/overclocker.png"),
    "industrialupgrade:battery/re_battery": (IU_JAR, "assets/industrialupgrade/textures/item/battery/re_battery_0.png"),
    "industrialupgrade:battery/energy_crystal": (IU_JAR, "assets/industrialupgrade/textures/item/energy_crystal_0.png"),
    "industrialupgrade:battery/lapotron_crystal": (IU_JAR, "assets/industrialupgrade/textures/item/lapotron_crystal_0.png"),
    "industrialupgrade:cable/copper_cable": (IU_JAR, "assets/industrialupgrade/textures/item/copper_cable_0.png"),
    "industrialupgrade:nuclearresource/uranium_235": (IU_JAR, "assets/industrialupgrade/textures/item/uranium_235.png"),
    # MythicBotany
    "mythicbotany:alfsteel_ingot": (MB_JAR, "assets/mythicbotany/textures/item/alfsteel_ingot.png"),
    # Alex's Caves
    "alexscaves:pearl": (AC_JAR, "assets/alexscaves/textures/item/pearl.png"),
    "alexscaves:uranium_rod": (AC_JAR, "assets/alexscaves/textures/block/uranium_rod.png"),
    "alexscaves:moth_dust": (AC_JAR, "assets/alexscaves/textures/item/moth_dust.png"),
    "alexscaves:dark_tatters": (AC_JAR, "assets/alexscaves/textures/item/dark_tatters.png"),
    # AquaTech
    "aquatech_ui:speed_x4_upgrade": (AT_JAR, "assets/aquatech_ui/textures/item/speed_upgrade.png"),
    "aquatech_ui:speed_upgrade": (AT_JAR, "assets/aquatech_ui/textures/item/speed_upgrade.png"),
    "aquatech_ui:rate_x32": (AT_JAR, "assets/aquatech_ui/textures/item/rate_x32.png"),
    "aquatech_ui:rate_x64": (AT_JAR, "assets/aquatech_ui/textures/item/rate_x64.png"),
    "aquatech_ui:mesh_filter": (AT_JAR, "assets/aquatech_ui/textures/item/mesh_filter.png"),
    # AE2
    "ae2:singularity": (AE_JAR, "assets/ae2/textures/item/singularity.png"),
    "ae2:logic_processor_press": (AE_JAR, "assets/ae2/textures/item/logic_processor_press.png"),
    "ae2:item_storage_cell_64k": (AE_JAR, "assets/ae2/textures/item/item_storage_cell_64k.png"),
    "ae2:item_storage_cell_16k": (AE_JAR, "assets/ae2/textures/item/item_storage_cell_16k.png"),
}


def main():
    extracted = {}
    if OUT_FILE.exists():
        with open(OUT_FILE, "r", encoding="utf-8") as f:
            extracted = json.load(f)

    # 1. Clean existing entries (crop animated strips to single frame)
    cleaned_count = 0
    for k, v in list(extracted.items()):
        if not v.startswith("data:image/png;base64,"):
            continue
        try:
            raw = base64.b64decode(v.split(",", 1)[1])
            im = Image.open(io.BytesIO(raw)).convert("RGBA")
            w, h = im.size
            if h > w:
                im = im.crop((0, 0, w, w))
                extracted[k] = img_to_b64(im)
                cleaned_count += 1
        except Exception:
            pass

    print(f"Cleaned {cleaned_count} animated strips in existing textures.")

    # 2. Extract & crop all explicit items
    for item_id, (jar, path) in ITEM_MAP.items():
        try:
            im = read_jar_img(jar, path)
            extracted[item_id] = img_to_b64(im)
            print(f"  [Item OK] {item_id}")
        except Exception as e:
            print(f"  [Item FAIL] {item_id} ({e})")

    # 3. Pre-render all 3D blocks
    for item_id, (jar, top_path, side_path) in BLOCK_MAP.items():
        try:
            top_im = read_jar_img(jar, top_path)
            side_im = read_jar_img(jar, side_path)
            block_3d = render_minecraft_block(top_im, side_im, 64)
            extracted[item_id] = img_to_b64(block_3d)
            print(f"  [3D Block OK] {item_id}")
        except Exception as e:
            print(f"  [3D Block FAIL] {item_id} ({e})")

    with open(OUT_FILE, "w", encoding="utf-8") as f:
        json.dump(extracted, f, ensure_ascii=False)

    print(f"\n[DONE] Successfully wrote {len(extracted)} textures to {OUT_FILE}")


if __name__ == "__main__":
    main()
