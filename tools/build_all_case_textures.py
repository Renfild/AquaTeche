#!/usr/bin/env python3
"""
Master Case Asset Processor & Texture Builder for AquaTech.
- Extracts item & block textures from server mod JARs.
- Auto-crops animated texture strips (Avaritia Infinity Ingot, Infinity Catalyst, Neutron Ingot, Dragon Heart, Livingrock) to crisp single 16x16 frames.
- Pre-renders authentic Minecraft 3D isometric block sprites (2:1 isometric projection with shaded faces) for all blocks/machines/solar panels.
- Outputs tools/extracted_case_textures.json with pure base64 data URIs.
"""
import base64
import io
import json
import os
import zipfile
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
CASES_JSON = ROOT / "server/config/aqualumen/cases.json"
OUT_FILE = ROOT / "tools/extracted_case_textures.json"

# Mod JARs
IU_JAR = ROOT / "server/mods/IndustrialUpgrade-1.20.1-3.4.0.11.jar"
AV_JAR = ROOT / "server/mods/Re-Avaritia-forge-1.20.1-1.4.1-release.jar"
DE_JAR = ROOT / "server/mods/Draconic-Evolution-1.20.1-3.1.2.621-universal.jar"
AE_JAR = ROOT / "server/mods/appliedenergistics2-forge-15.4.10.jar"
AC_JAR = ROOT / "server/mods/alexscaves-2.0.2.jar"
AT_JAR = ROOT / "server/mods/aquatech_ui-1.0.46.jar"
BOT_JAR = ROOT / "server/mods/Botania-1.20.1-454-FORGE.jar"
MB_JAR = ROOT / "server/mods/MythicBotany-1.20.1-4.0.4.jar"
EB_JAR = ROOT / "server/mods/extrabotany-forge-1.20.1-1.9.2.jar"
EC_JAR = ROOT / "server/mods/ExtendedCrafting-1.20.1-6.0.10.jar"


def read_jar_frame0(jar_path: Path, inner_path: str) -> Image.Image:
    with zipfile.ZipFile(jar_path, "r") as zf:
        raw = zf.read(inner_path)
        im = Image.open(io.BytesIO(raw)).convert("RGBA")
        w, h = im.size
        if h > w:
            im = im.crop((0, 0, w, w))
        return im


def iso_cube(top: Image.Image, left: Image.Image, right: Image.Image = None, size: int = 64) -> Image.Image:
    """Renders authentic 2:1 isometric Minecraft block sprite with 3 faces."""
    if right is None:
        right = left
    top_16 = top.resize((16, 16), Image.NEAREST)
    left_16 = left.resize((16, 16), Image.NEAREST)
    right_16 = right.resize((16, 16), Image.NEAREST)

    top_f = top_16.transform((64, 32), Image.AFFINE, (0.25, -0.5, 8, 0.25, 0.5, -8), resample=Image.NEAREST)
    left_f = left_16.transform((32, 48), Image.AFFINE, (0.5, 0, 0, -0.25, 0.5, 0), resample=Image.NEAREST)
    right_f = right_16.transform((32, 48), Image.AFFINE, (0.5, 0, 0, 0.25, 0.5, -8), resample=Image.NEAREST)

    def shade(img, k):
        px = img.load()
        for y in range(img.height):
            for x in range(img.width):
                r, g, b, a = px[x, y]
                if a:
                    px[x, y] = (int(r * k), int(g * k), int(b * k), a)
        return img

    out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    out.alpha_composite(top_f, (0, 0))
    out.alpha_composite(shade(left_f, 0.85), (0, 16))
    out.alpha_composite(shade(right_f, 0.65), (32, 16))
    return out


def img_to_b64(img: Image.Image) -> str:
    buf = io.BytesIO()
    img.save(buf, format="PNG")
    return "data:image/png;base64," + base64.b64encode(buf.getvalue()).decode("ascii")


def build_crafting_injector() -> Image.Image:
    """Composites authentic Draconic Evolution Crafting Injector (pedestal + glowing core)."""
    base = read_jar_frame0(DE_JAR, "assets/draconicevolution/textures/block/crafting/injector_base.png")
    with zipfile.ZipFile(DE_JAR, "r") as zf:
        raw_core = zf.read("assets/draconicevolution/textures/block/crafting/injector_core_draconium.png")
        core_l = Image.open(io.BytesIO(raw_core)).convert("L")

    core = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    c_px = core.load()
    for y in range(16):
        for x in range(16):
            v = core_l.getpixel((x, y))
            if v > 10:
                c_px[x, y] = (int(0x93 * v / 255), int(0x33 * v / 255), int(0xEA * v / 255), 255)

    inj_side = base.copy()
    inj_side.alpha_composite(core.resize((12, 8), Image.NEAREST), (2, 4))

    inj_top = base.copy()
    inj_top.alpha_composite(core.resize((8, 8), Image.NEAREST), (4, 4))

    return iso_cube(inj_top, inj_side)


# Block definitions pre-rendered as 3D isometric blocks:
SOLAR_PANELS = ["spectral", "photonic", "neutronium", "barion", "hadron", "graviton"]


def main():
    extracted = {}
    if OUT_FILE.exists():
        with open(OUT_FILE, "r", encoding="utf-8") as f:
            extracted = json.load(f)

    # 1. Solar panels (Industrial Upgrade)
    for p in SOLAR_PANELS:
        try:
            top = read_jar_frame0(IU_JAR, f"assets/industrialupgrade/textures/block/solar_panels/{p}/glass.png")
            side = read_jar_frame0(IU_JAR, f"assets/industrialupgrade/textures/block/solar_panels/{p}/side.png")
            extracted[f"industrialupgrade:machines/{p}_solar_panel"] = img_to_b64(iso_cube(top, side))
            print(f"  [Panel OK] {p}")
        except Exception as e:
            print(f"  [Panel FAIL] {p}: {e}")

    # 2. Avaritia Extreme Crafting Table (top 9x9 grid + side runes)
    try:
        ext_top = read_jar_frame0(AV_JAR, "assets/avaritia/textures/block/machine/craft/extreme_top.png")
        ext_side = read_jar_frame0(AV_JAR, "assets/avaritia/textures/block/machine/craft/extreme_side.png")
        extracted["avaritia:extreme_crafting_table"] = img_to_b64(iso_cube(ext_top, ext_side))
        print("  [Avaritia OK] extreme_crafting_table")
    except Exception as e:
        print(f"  [Avaritia FAIL] extreme_crafting_table: {e}")

    # 3. Avaritia Neutron Compressor (front chamber + side runes + top vents)
    try:
        c_front = read_jar_frame0(AV_JAR, "assets/avaritia/textures/block/machine/compressor/compressor_front.png")
        c_side = read_jar_frame0(AV_JAR, "assets/avaritia/textures/block/machine/compressor/compressor_side_left.png")
        c_top = read_jar_frame0(AV_JAR, "assets/avaritia/textures/block/machine/compressor/compressor_top.png")
        extracted["avaritia:neutron_compressor"] = img_to_b64(iso_cube(c_top, c_front, c_side))
        print("  [Avaritia OK] neutron_compressor")
    except Exception as e:
        print(f"  [Avaritia FAIL] neutron_compressor: {e}")

    # 4. Draconic Evolution Basic Crafting Injector
    try:
        extracted["draconicevolution:basic_crafting_injector"] = img_to_b64(build_crafting_injector())
        print("  [DE OK] basic_crafting_injector")
    except Exception as e:
        print(f"  [DE FAIL] basic_crafting_injector: {e}")

    # 5. Minecraft Dragon Egg 3D render
    egg_path = ROOT / "scratch/test_dragon_egg.png"
    if egg_path.exists():
        im_egg = Image.open(egg_path).convert("RGBA")
        extracted["minecraft:dragon_egg"] = img_to_b64(im_egg)
        print("  [MC OK] dragon_egg")

    # 6. Explicit 2D Items (always 1-frame cropped!)
    explicit_items = {
        # Avaritia items
        "avaritia:infinity_ingot": (AV_JAR, "assets/avaritia/textures/item/resource/infinity/infinity_ingot.png"),
        "avaritia:infinity_catalyst": (AV_JAR, "assets/avaritia/textures/item/resource/infinity/infinity_catalyst.png"),
        "avaritia:neutron_ingot": (AV_JAR, "assets/avaritia/textures/item/resource/neutron/neutron_ingot.png"),
        "avaritia:neutron_nugget": (AV_JAR, "assets/avaritia/textures/item/resource/neutron/neutron_nugget.png"),
        "avaritia:crystal_matrix_ingot": (AV_JAR, "assets/avaritia/textures/item/resource/crystal/crystal_matrix_ingot.png"),
        # Draconic Evolution
        "draconicevolution:dragon_heart": (DE_JAR, "assets/draconicevolution/textures/item/components/dragon_heart.png"),
        "draconicevolution:draconium_ingot": (DE_JAR, "assets/draconicevolution/textures/item/components/draconium_ingot.png"),
        "draconicevolution:draconium_dust": (DE_JAR, "assets/draconicevolution/textures/item/components/draconium_dust.png"),
        "draconicevolution:awakened_draconium_ingot": (DE_JAR, "assets/draconicevolution/textures/item/components/awakened_draconium_ingot.png"),
        "draconicevolution:draconium_core": (DE_JAR, "assets/draconicevolution/textures/item/components/draconium_core.png"),
        "draconicevolution:wyvern_core": (DE_JAR, "assets/draconicevolution/textures/item/components/wyvern_core.png"),
        "draconicevolution:awakened_core": (DE_JAR, "assets/draconicevolution/textures/item/components/awakened_core.png"),
        "draconicevolution:wyvern_energy_core": (DE_JAR, "assets/draconicevolution/textures/item/components/wyvern_energy_core.png"),
        # Other blocks
        "draconicevolution:awakened_draconium_block": (DE_JAR, "assets/draconicevolution/textures/block/awakened_draconium_block.png"),
        "botania:livingrock": (BOT_JAR, "assets/botania/textures/block/livingrock.png"),
        "botania:manasteel_ingot": (BOT_JAR, "assets/botania/textures/item/manasteel_ingot.png"),
        "botania:terrasteel_ingot": (BOT_JAR, "assets/botania/textures/item/terrasteel_ingot.png"),
        "botania:life_essence": (BOT_JAR, "assets/botania/textures/item/life_essence.png"),
        "mythicbotany:alfsteel_ingot": (MB_JAR, "assets/mythicbotany/textures/item/alfsteel_ingot.png"),
        "extrabotany:orichalcos_ingot": (EB_JAR, "assets/extrabotany/textures/item/orichalcos_ingot.png"),
        "extrabotany:aerialite_ingot": (EB_JAR, "assets/extrabotany/textures/item/aerialite_ingot.png"),
        "extrabotany:spirit_fuel": (EB_JAR, "assets/extrabotany/textures/item/spirit_fuel.png"),
    }

    for item_id, (jar, path) in explicit_items.items():
        try:
            im = read_jar_frame0(jar, path)
            im_48 = im.resize((48, 48), Image.NEAREST)
            extracted[item_id] = img_to_b64(im_48)
            print(f"  [Item OK] {item_id}")
        except Exception as e:
            print(f"  [Item FAIL] {item_id}: {e}")

    # 7. Clean any remaining animated strips in extracted
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
        except Exception:
            pass

    with open(OUT_FILE, "w", encoding="utf-8") as f:
        json.dump(extracted, f, ensure_ascii=False)

    print(f"\n[DONE] Wrote {len(extracted)} textures to {OUT_FILE}")


if __name__ == "__main__":
    main()
