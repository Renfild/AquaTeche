import zipfile
import json
from collections import defaultdict
import os

jar_path = 'starcatcher-2.3.19-FORGE-1.20.1 (1).jar'
fish_list = []
ru_map = {}
en_map = {}

with zipfile.ZipFile(jar_path, 'r') as z:
    # Read lang files
    if 'assets/starcatcher/lang/ru_ru.json' in z.namelist():
        ru_data = json.loads(z.read('assets/starcatcher/lang/ru_ru.json').decode('utf-8'))
        for k, v in ru_data.items():
            ru_map[k] = v

    if 'assets/starcatcher/lang/en_us.json' in z.namelist():
        en_data = json.loads(z.read('assets/starcatcher/lang/en_us.json').decode('utf-8'))
        for k, v in en_data.items():
            en_map[k] = v

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

                # Try key formats
                keys_to_try = [
                    f"item.starcatcher.{fish_id}",
                    f"item.tide.{fish_id}",
                    f"item.unusualfishmod.{fish_id}",
                    f"starcatcher.fish.{fish_id}",
                    fish_id
                ]

                ru_name = None
                for k in keys_to_try:
                    if k in ru_map:
                        ru_name = ru_map[k]
                        break

                en_name = None
                for k in keys_to_try:
                    if k in en_map:
                        en_name = en_map[k]
                        break

                if not ru_name:
                    ru_name = en_name if en_name else fish_id.replace('_', ' ').title()
                if not en_name:
                    en_name = fish_id.replace('_', ' ').title()

                fish_list.append({
                    'id': fish_id,
                    'ru_name': ru_name,
                    'en_name': en_name,
                    'rarity': rarity,
                    'weight': weight,
                    'conditions': ', '.join(conditions) if conditions else 'Обычная вода'
                })
            except Exception as e:
                print("Error parsing", name, e)

print(f"Total fish loaded: {len(fish_list)}")

by_rarity = defaultdict(list)
for f in fish_list:
    by_rarity[f['rarity']].append(f)

rarity_order = ['legendary', 'epic', 'rare', 'uncommon', 'common']
rarity_ru = {
    'legendary': 'Легендарные 🌟',
    'epic': 'Эпические 💜',
    'rare': 'Редкие 💙',
    'uncommon': 'Необычные 💚',
    'common': 'Обычные 🤍'
}

out_md = []
out_md.append("# Таблица всех рыб и шансов вылова в StarCatcher\n")
out_md.append("В моде **StarCatcher** улов делится на категории редкости (Rarity). Внутри каждой категории шанс зависит от базового веса (`weight`) рыбы относительно суммы весов всех доступных в данной локации/биуме рыб.\n")

for r in rarity_order:
    if r not in by_rarity:
        continue
    items = sorted(by_rarity[r], key=lambda x: x['weight'], reverse=True)
    total_w = sum(x['weight'] for x in items)
    out_md.append(f"## Category: {rarity_ru.get(r, r.upper())} ({len(items)} видов)\n")
    out_md.append("| ID Предмета | Название (RU / EN) | Базовый вес | Относит. Шанс | Условия улова |")
    out_md.append("|---|---|---|---|---|")
    for f in items:
        pct = (f['weight'] / total_w) * 100 if total_w > 0 else 0
        out_md.append(f"| `{f['id']}` | **{f['ru_name']}** ({f['en_name']}) | `{f['weight']}` | ~{pct:.1f}% | {f['conditions']} |")
    out_md.append("\n")

out_text = "\n".join(out_md)
with open('starcatcher_fish_chances.md', 'w', encoding='utf-8') as f:
    f.write(out_text)

print("Generated starcatcher_fish_chances.md successfully!")
