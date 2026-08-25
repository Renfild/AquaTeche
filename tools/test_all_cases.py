import json
import zipfile
import glob

# Load all valid item IDs from server mods & minecraft
valid_items = set()

# Minecraft vanilla items
for item in [
    "minecraft:diamond", "minecraft:iron_ingot", "minecraft:copper_ingot", "minecraft:gold_ingot",
    "minecraft:netherite_ingot", "minecraft:coal", "minecraft:redstone", "minecraft:blaze_rod",
    "minecraft:lava_bucket", "minecraft:nether_star", "minecraft:dragon_egg", "minecraft:clay_ball",
    "minecraft:brick", "minecraft:leather", "minecraft:prismarine_shard", "minecraft:prismarine_crystals"
]:
    valid_items.add(item)

for jar in glob.glob('server/mods/*.jar'):
    try:
        with zipfile.ZipFile(jar, 'r') as z:
            for name in z.namelist():
                if name.startswith('assets/') and ('/models/item/' in name or '/blockstates/' in name) and name.endswith('.json'):
                    parts = name.split('/')
                    modid = parts[1]
                    if '/models/item/' in name:
                        idx = name.index('/models/item/') + len('/models/item/')
                        sub = name[idx:-5]
                        valid_items.add(f"{modid}:{sub}")
                    elif '/blockstates/' in name:
                        sub = parts[-1][:-5]
                        valid_items.add(f"{modid}:{sub}")
    except:
        pass

with open('config/aqualumen/cases.json', 'r', encoding='utf-8') as f:
    cases = json.load(f)

lines = []
lines.append(f"Loaded {len(valid_items)} valid registered item keys.")

errors = 0
for case in cases['cases']:
    lines.append(f"\n--- Checking {case['id']}: {case['title']} ---")
    for l in case.get('loot', []):
        if l.get('type') == 'item':
            it = l.get('item', '')
            lbl = l.get('label', '')
            status = "[OK]" if it in valid_items else "[ERROR]"
            if status == "[ERROR]":
                errors += 1
            lines.append(f"  {status} {it} ({lbl})")

lines.append(f"\nTotal errors: {errors}")

with open('test_all_cases_result.txt', 'w', encoding='utf-8') as f:
    f.write('\n'.join(lines))

print(f"Results written. Total errors: {errors}")
