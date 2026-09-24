#!/usr/bin/env python3
"""Extract missing item textures for the F4 server shop from the client pack jars.

Fills docs/assets/images/items/<normalized>.png and adds entries to
tools/extracted_case_textures.json (the map embedded into hub.html as ITEM_TEXTURES).
"""
from __future__ import annotations

import json
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MODS = ROOT / "dist" / "AquaTech-Client" / "mods"
ITEMS_DIR = ROOT / "docs" / "assets" / "images" / "items"
MAP_FILE = ROOT / "tools" / "extracted_case_textures.json"

SHOP_ITEMS = [
    "ae2:silicon_press",
    "ae2:logic_processor_press",
    "ae2:calculation_processor_press",
    "ae2:engineering_processor_press",
    "ae2:name_press",
    "ae2:certus_quartz_crystal",
    "ae2:charged_certus_quartz_crystal",
    "industrialupgrade:itemingots/aluminium_ingot",
    "industrialupgrade:crafting_elements/crafting_272_element",
    "industrialupgrade:crafting_elements/crafting_273_element",
    "industrialupgrade:crafting_elements/crafting_274_element",
    "industrialupgrade:blockresource/reinforced_stone",
    "botania:manasteel_ingot",
    "botania:mana_pearl",
    "botania:mana_diamond",
    "minecraft:redstone",
    "minecraft:obsidian",
    "patchouli:guide_book",
]


def candidates(item_id: str) -> list[str]:
    ns, path = item_id.split(":", 1)
    out = []
    for folder in ("item", "items", "block", "blocks"):
        out.append(f"assets/{ns}/textures/{folder}/{path}.png")
        out.append(f"assets/{ns}/textures/{folder}/item_{path}.png")
    # path may be a nested resource location (industrialupgrade:itemingots/...)
    return out


def normalized(item_id: str) -> str:
    return item_id.replace(":", "_").replace("/", "_") + ".png"


# Явные пути: текстуры лежат не по имени реестра (проверено по моделям модов).
OVERRIDES = {
    "ae2:charged_certus_quartz_crystal": ("appliedenergistics2", "assets/ae2/textures/item/certus_quartz_crystal_charged.png"),
    "industrialupgrade:itemingots/aluminium_ingot": ("IndustrialUpgrade", "assets/industrialupgrade/textures/item/aluminium_ingot.png"),
    "industrialupgrade:crafting_elements/crafting_272_element": ("IndustrialUpgrade", "assets/industrialupgrade/textures/item/circuit.png"),
    "industrialupgrade:crafting_elements/crafting_273_element": ("IndustrialUpgrade", "assets/industrialupgrade/textures/item/advanced_circuit.png"),
    "industrialupgrade:crafting_elements/crafting_274_element": ("IndustrialUpgrade", "assets/industrialupgrade/textures/item/alloy.png"),
    "industrialupgrade:blockresource/reinforced_stone": ("IndustrialUpgrade", "assets/industrialupgrade/textures/block/reinforced_stone.png"),
    "patchouli:guide_book": ("Patchouli", "assets/patchouli/textures/item/book_brown.png"),
}
FORCE_REEXTRACT = {
    "ae2:charged_certus_quartz_crystal",
    "industrialupgrade:crafting_elements/crafting_273_element",
    "industrialupgrade:crafting_elements/crafting_274_element",
}
GENERATED = {"minecraft:obsidian"}


def make_obsidian_icon(target: Path) -> None:
    from PIL import Image

    base = (18, 14, 26, 255)
    dark = (12, 9, 18, 255)
    light = (44, 30, 62, 255)
    hi = (72, 52, 98, 255)
    im = Image.new("RGBA", (16, 16), base)
    px = im.load()
    speckles = [
        (2, 3, light), (5, 1, dark), (9, 2, light), (13, 4, dark),
        (1, 8, light), (4, 10, dark), (7, 6, hi), (11, 8, light),
        (14, 11, dark), (3, 14, light), (8, 12, dark), (12, 14, hi),
        (6, 13, light), (10, 5, dark), (2, 11, hi), (13, 8, light),
    ]
    for x, y, c in speckles:
        px[x, y] = c
    im.save(target)


def main() -> None:
    ITEMS_DIR.mkdir(parents=True, exist_ok=True)
    mapping = json.loads(MAP_FILE.read_text(encoding="utf-8"))
    jars = sorted(MODS.glob("*.jar"))
    added = []
    missing = []
    for item_id in SHOP_ITEMS:
        target = ITEMS_DIR / normalized(item_id)
        rel = f"assets/images/items/{normalized(item_id)}"
        if item_id in mapping and target.is_file() and item_id not in FORCE_REEXTRACT:
            print("skip (already mapped):", item_id)
            continue
        if item_id in GENERATED and not target.is_file():
            make_obsidian_icon(target)
            mapping[item_id] = rel
            added.append(f"{item_id} <- generated 16x16")
            print("generated", item_id)
            continue
        found = None
        override = OVERRIDES.get(item_id)
        if override:
            jar_needle, entry_path = override
            for jar in jars:
                if jar_needle.lower() not in jar.name.lower():
                    continue
                with zipfile.ZipFile(jar) as z:
                    if entry_path in set(z.namelist()):
                        found = (jar, entry_path, z.read(entry_path))
                        break
        for jar in ([] if found else jars):
            with zipfile.ZipFile(jar) as z:
                names = set(z.namelist())
                for cand in candidates(item_id):
                    if cand in names:
                        found = (jar, cand, z.read(cand))
                        break
            if found:
                break
        if not found:
            missing.append(item_id)
            continue
        jar, entry, data = found
        from PIL import Image
        import io
        try:
            im = Image.open(io.BytesIO(data))
            if im.height > im.width:
                im = im.crop((0, 0, im.width, im.width))
            im.save(target)
        except Exception:
            target.write_bytes(data)
        mapping[item_id] = rel
        added.append(f"{item_id} <- {jar.name}:{entry}")
        print("extracted", item_id, "<-", jar.name, entry)

    if added:
        MAP_FILE.write_text(json.dumps(mapping, ensure_ascii=False, indent=1) + "\n", encoding="utf-8")
    print(f"\nextracted: {len(added)}, still missing: {len(missing)}")
    for m in missing:
        print("  MISSING", m)


if __name__ == "__main__":
    main()
