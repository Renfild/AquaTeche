import json
import os

SOLAR_TIERS = {
    'abyss': [
        ('industrialupgrade:machines/spectral_solar_panel', 'Дифракционная солнечная панель [Тир 6]')
    ],
    'superconductor': [
        ('industrialupgrade:machines/photonic_solar_panel', 'Фотонная солнечная панель [Тир 7]'),
        ('industrialupgrade:machines/neutronium_solar_panel', 'Нейтронная солнечная панель [Тир 7]')
    ],
    'singularity': [
        ('industrialupgrade:machines/barion_solar_panel', 'Барионная солнечная панель [Тир 8]')
    ],
    'draconic': [
        ('industrialupgrade:machines/hadron_solar_panel', 'Адронная солнечная панель [Тир 9]')
    ],
    'infinity': [
        ('industrialupgrade:machines/graviton_solar_panel', 'Гравитонная солнечная панель [Тир 10]')
    ]
}

paths = [
    'server/config/aqualumen/cases.json',
    'config/aqualumen/cases.json'
]

for p in paths:
    if not os.path.exists(p):
        continue
    with open(p, 'r', encoding='utf-8') as f:
        data = json.load(f)

    for case in data['cases']:
        cid = case['id']
        case['loot'] = [l for l in case['loot'] if not (l.get('item','').endswith('solar_panel') or l.get('item','').endswith('solar_paneliu'))]
        
        cur_sum = sum(l.get('weight', 1) for l in case['loot'])
        scale = 500.0 / cur_sum if cur_sum > 0 else 1.0
        for l in case['loot']:
            l['weight'] = max(1, int(round(l.get('weight', 1) * scale)))
            
        if cid in SOLAR_TIERS:
            for item_id, label in SOLAR_TIERS[cid]:
                case['loot'].append({
                    'type': 'item',
                    'item': item_id,
                    'label': label,
                    'min': 1,
                    'max': 1,
                    'weight': 1
                })
        
        total_w = sum(l['weight'] for l in case['loot'])
        solars = [l for l in case['loot'] if l.get('item','').endswith('solar_panel')]
        for s in solars:
            pct = (s['weight'] / total_w) * 100.0
            lbl = s.get('label')
            w = s.get('weight')
            print(f"{cid} -> {lbl}: {pct:.2f}% (weight={w}/{total_w})")

    with open(p, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
    print('Updated', p)
