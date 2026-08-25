import zipfile
import glob
import json
from pathlib import Path

# Build registry mapping: modid -> set of item names
all_items = {}
for jar in glob.glob('server/mods/*.jar'):
    try:
        with zipfile.ZipFile(jar, 'r') as z:
            for name in z.namelist():
                if name.startswith('assets/') and '/models/item/' in name and name.endswith('.json'):
                    parts = name.split('/')
                    modid = parts[1]
                    idx = name.index('/models/item/') + len('/models/item/')
                    sub = name[idx:-5]
                    all_items.setdefault(modid, set()).add(sub)
                elif name.startswith('assets/') and '/blockstates/' in name and name.endswith('.json'):
                    parts = name.split('/')
                    modid = parts[1]
                    item_name = parts[-1][:-5]
                    all_items.setdefault(modid, set()).add(item_name)
    except: pass

with open('config/aqualumen/cases.json', 'r', encoding='utf-8') as f:
    cases = json.load(f)

report = []
for c in cases['cases']:
    report.append(f"\n==================== {c['id']}: {c['title']} ====================")
    for l in c.get('loot', []):
        if l.get('type') == 'item':
            item = l.get('item', '')
            label = l.get('label', '')
            modid, _, name = item.partition(':')
            
            valid = False
            if modid == 'minecraft':
                valid = True # Standard vanilla
            elif modid in all_items and name in all_items[modid]:
                valid = True
            
            if valid:
                report.append(f"  [OK] {item} ({label})")
            else:
                # Find best match in mod
                suggestions = []
                if modid in all_items:
                    search_term = name.split('/')[-1].split('_')[0]
                    for candidate in all_items[modid]:
                        if name.split('/')[-1] in candidate or (search_term and search_term in candidate):
                            suggestions.append(f"{modid}:{candidate}")
                report.append(f"  [FAIL -> PRISMARINE] {item} ({label}) -> suggestions: {suggestions[:5]}")

with open('case_audit_report.txt', 'w', encoding='utf-8') as f:
    f.write('\n'.join(report))

print("Audit written to case_audit_report.txt")
