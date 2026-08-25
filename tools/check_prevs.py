import json

with open('full_tabs_resolved.json', 'r', encoding='utf-8') as f:
    data = json.load(f)

for tab_name, tab in data.items():
    print(f"=== {tab_name} ===")
    for q in tab['quests'][:10]:
        print(f"  Quest: {q['name']:25} | prevs: {q['prevs']}")
