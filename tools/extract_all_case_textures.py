#!/usr/bin/env python3
import json
import base64
import os
import glob
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
CASES_FILE = ROOT / "server/config/aqualumen/cases.json"
OUT_FILE = ROOT / "tools/extracted_case_textures.json"

VANILLA_CACHE = Path.home() / ".gradle/caches/forge_gradle/minecraft_repo/versions/1.20.1/client-extra.jar"

def get_texture_b64(raw_bytes: bytes) -> str:
    return "data:image/png;base64," + base64.b64encode(raw_bytes).decode('ascii')

def main():
    with open(CASES_FILE, 'r', encoding='utf-8') as f:
        cases_data = json.load(f)

    target_items = set()
    for c in cases_data['cases']:
        for loot in c['loot']:
            item = loot.get('item', '')
            if item and loot.get('type', 'item') == 'item':
                target_items.add(item)

    print(f"Total target items to find: {len(target_items)}")

    # Load existing textures if available
    extracted = {}
    if OUT_FILE.exists():
        try:
            with open(OUT_FILE, 'r', encoding='utf-8') as f:
                extracted = json.load(f)
        except Exception:
            pass

    # Collect all jar paths
    jars = [VANILLA_CACHE] if VANILLA_CACHE.exists() else []
    jars.extend([Path(p) for p in glob.glob(str(ROOT / 'server/mods/*.jar'))])
    jars.extend([Path(p) for p in glob.glob(str(ROOT / 'mods/*.jar'))])
    jars.extend([Path(p) for p in glob.glob(str(ROOT / 'mods/*/build/libs/*.jar'))])

    print(f"Searching through {len(jars)} jars...")

    for jar_path in jars:
        if not jar_path.is_file():
            continue
        try:
            with zipfile.ZipFile(jar_path, 'r') as z:
                namelist = z.namelist()
                for item_id in list(target_items):
                    if item_id in extracted:
                        continue

                    ns, path = item_id.split(':', 1) if ':' in item_id else ('minecraft', item_id)
                    name = path.rsplit('/', 1)[-1]

                    # Priority search paths
                    candidates = [
                        f"assets/{ns}/textures/item/{path}.png",
                        f"assets/{ns}/textures/items/{path}.png",
                        f"assets/{ns}/textures/block/{path}.png",
                        f"assets/{ns}/textures/blocks/{path}.png",
                        f"assets/{ns}/textures/item/{name}.png",
                        f"assets/{ns}/textures/items/{name}.png",
                        f"assets/{ns}/textures/block/{name}.png",
                        f"assets/{ns}/textures/blocks/{name}.png",
                        f"assets/{ns}/textures/models/armor/{name}.png",
                        f"assets/{ns}/textures/gui/{name}.png",
                    ]

                    found = None
                    for c in candidates:
                        if c in namelist:
                            found = c
                            break

                    if not found:
                        # Fallback fuzzy match
                        matches = [n for n in namelist if n.startswith(f"assets/{ns}/textures/") and n.endswith(f"/{name}.png")]
                        if matches:
                            matches.sort(key=len)
                            found = matches[0]

                    if found:
                        data = z.read(found)
                        extracted[item_id] = get_texture_b64(data)
                        print(f"  [+] Found {item_id} -> {found} in {jar_path.name}")
        except Exception as e:
            # ignore broken jars
            pass

    # Save output
    with open(OUT_FILE, 'w', encoding='utf-8') as f:
        json.dump(extracted, f, ensure_ascii=False, indent=2)

    found_count = sum(1 for item in target_items if item in extracted)
    print(f"\nSuccessfully mapped {found_count}/{len(target_items)} case item textures into {OUT_FILE.name}!")

if __name__ == '__main__':
    main()
