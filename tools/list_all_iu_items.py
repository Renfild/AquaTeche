import zipfile, json, re

jar_path = 'c:/Users/xieto/Desktop/AquaTech/mods/IndustrialUpgrade-1.20.1-3.4.0.11.jar'
with zipfile.ZipFile(jar_path, 'r') as z:
    # Let's inspect data/industrialupgrade or assets/industrialupgrade/models/item
    item_models = [n for n in z.namelist() if n.startswith('assets/industrialupgrade/models/item/') and n.endswith('.json')]
    block_models = [n for n in z.namelist() if n.startswith('assets/industrialupgrade/models/block/') and n.endswith('.json')]
    lang_ru = json.loads(z.read('assets/industrialupgrade/lang/ru_ru.json').decode('utf-8'))

print(f"Total item models in IU: {len(item_models)}")
print(f"Total block models in IU: {len(block_models)}")

# Extract item registry names from models
registry_items = []
for m in item_models:
    # e.g. assets/industrialupgrade/models/item/crafting_elements/silicon_crystal.json
    rel = m.replace('assets/industrialupgrade/models/item/', '').replace('.json', '')
    registry_items.append('industrialupgrade:' + rel)

print(f"Sample registry items (first 30):")
for r in registry_items[:30]:
    print(' ', r)

with open('iu_all_registry_items.txt', 'w', encoding='utf-8') as out:
    for r in sorted(registry_items):
        out.write(r + '\n')
