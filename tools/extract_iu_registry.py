import zipfile, json, re

jar_path = 'c:/Users/xieto/Desktop/AquaTech/mods/IndustrialUpgrade-1.20.1-3.4.0.11.jar'
with zipfile.ZipFile(jar_path, 'r') as z:
    ru_ru = json.loads(z.read('assets/industrialupgrade/lang/ru_ru.json').decode('utf-8'))
    
# Let's inspect all items in ru_ru.json to have a reverse lookup of item IDs by English/Russian names
print(f"Total entries in ru_ru.json: {len(ru_ru)}")

# Let's find all item / block keys
items_map = {}
for k, v in ru_ru.items():
    if k.startswith('item.industrialupgrade.') or k.startswith('block.industrialupgrade.'):
        raw_id = k.split('.', 2)[2]
        items_map[raw_id] = (k, v)

print(f"Total mapped IU items/blocks: {len(items_map)}")

# Let's save a sample of guide descriptions
guide_desc = {k: v for k, v in ru_ru.items() if 'guide' in k.lower() or 'desc' in k.lower()}
print(f"Total guide descriptions: {len(guide_desc)}")
with open('iu_guide_descriptions.json', 'w', encoding='utf-8') as out:
    json.dump(guide_desc, out, ensure_ascii=False, indent=2)
