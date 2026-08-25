import zipfile
import glob
import json
from pathlib import Path

# Collect registered item models from all jars
registered_items = set()
for jar in glob.glob('server/mods/*.jar'):
    try:
        with zipfile.ZipFile(jar, 'r') as z:
            for name in z.namelist():
                if name.startswith('assets/') and '/models/item/' in name and name.endswith('.json'):
                    parts = name.split('/')
                    modid = parts[1]
                    idx = name.index('/models/item/') + len('/models/item/')
                    sub = name[idx:-5]
                    registered_items.add(f"{modid}:{sub}")
                elif name.startswith('assets/') and '/blockstates/' in name and name.endswith('.json'):
                    parts = name.split('/')
                    modid = parts[1]
                    item_name = parts[-1][:-5]
                    registered_items.add(f"{modid}:{item_name}")
    except Exception as e:
        print(f"Error reading {jar}: {e}")

# Also check vanilla items
vanilla_items = {
    "minecraft:diamond", "minecraft:iron_ingot", "minecraft:copper_ingot",
    "minecraft:clay_ball", "minecraft:brick", "minecraft:leather", "minecraft:coal",
    "minecraft:redstone", "minecraft:blaze_rod", "minecraft:lava_bucket", "minecraft:gold_ingot",
    "minecraft:netherite_ingot", "minecraft:nether_star", "minecraft:sea_lantern",
    "minecraft:prismarine", "minecraft:prismarine_bricks", "minecraft:dark_prismarine",
    "minecraft:prismarine_shard", "minecraft:prismarine_crystals"
}
registered_items.update(vanilla_items)

with open('config/aqualumen/cases.json', 'r', encoding='utf-8') as f:
    cases_data = json.load(f)

print("=== VALIDATING ALL CASE DROPS ===")
invalid_count = 0
for case in cases_data['cases']:
    print(f"\nCase: {case['id']} - {case['title']}")
    for l in case.get('loot', []):
        if l.get('type') == 'item':
            item_id = l.get('item', '')
            label = l.get('label', '')
            if item_id in registered_items:
                print(f"  [OK] {item_id} -> {label}")
            else:
                print(f"  [INVALID / MISSING -> WILL DROP PRISMARINE] {item_id} -> {label}")
                invalid_count += 1

print(f"\nTotal invalid items: {invalid_count}")
