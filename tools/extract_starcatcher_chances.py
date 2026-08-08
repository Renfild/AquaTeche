import zipfile
import json
from collections import defaultdict

jar_path = 'starcatcher-2.3.19-FORGE-1.20.1 (1).jar'
fish_list = []
ru_map = {}

with zipfile.ZipFile(jar_path, 'r') as z:
    # Try reading ru_ru.json for fish names
    try:
        ru_data = json.loads(z.read('assets/starcatcher/lang/ru_ru.json').decode('utf-8'))
        for k, v in ru_data.items():
            if k.startswith('item.starcatcher.'):
                fish_id = k.replace('item.starcatcher.', '').replace('item.tide.', '')
                ru_map[fish_id] = v
            elif k.startswith('item.tide.'):
                fish_id = k.replace('item.tide.', '')
                ru_map[fish_id] = v
    except Exception as e:
        print("Lang parse error:", e)

    for name in z.namelist():
        if name.startswith('data/') and '/starcatcher/fish/' in name and name.endswith('.json'):
            try:
                raw = z.read(name).decode('utf-8')
                data = json.loads(raw)
                fish_id = name.split('/')[-1].replace('.json', '')
                rarity = str(data.get('rarity', 'common')).lower()
                weight = data.get('weight', 1.0)
                
                # Check biome / dimension / depth conditions if any
                conditions = []
                if 'biomes' in data:
                    conditions.append(f"Биом: {data['biomes']}")
                if 'dimensions' in data:
                    conditions.append(f"Измерение: {data['dimensions']}")
                if 'fluid' in data:
                    conditions.append(f"Жидкость: {data['fluid']}")

                ru_name = ru_map.get(fish_id, fish_id.replace('_', ' ').title())

                fish_list.append({
                    'id': fish_id,
                    'ru_name': ru_name,
                    'rarity': rarity,
                    'weight': weight,
                    'conditions': ', '.join(conditions) if conditions else 'Любой резервуар'
                })
            except Exception as e:
                print("Error parsing", name, e)

print(f"Total StarCatcher fish parsed: {len(fish_list)}")

by_rarity = defaultdict(list)
for f in fish_list:
    by_rarity[f['rarity']].append(f)

rarity_order = ['common', 'uncommon', 'rare', 'epic', 'legendary', 'mythic']

for r in rarity_order:
    if r not in by_rarity:
        continue
    items = sorted(by_rarity[r], key=lambda x: x['weight'], reverse=True)
    total_w = sum(x['weight'] for x in items)
    print(f"\n### {r.upper()} ({len(items)} видов | Сумма весов: {total_w:.1f})")
    print("| ID | Русское название | Вес | Относит. шанс | Условия ловли |")
    print("|---|---|---|---|---|")
    for f in items:
        pct = (f['weight'] / total_w) * 100 if total_w > 0 else 0
        print(f"| `{f['id']}` | **{f['ru_name']}** | `{f['weight']}` | ~{pct:.1f}% | {f['conditions']} |")

