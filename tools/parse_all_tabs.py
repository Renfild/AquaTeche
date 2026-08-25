import re, json, zipfile, os

jar_path = 'c:/Users/xieto/Desktop/AquaTech/mods/IndustrialUpgrade-1.20.1-3.4.0.11.jar'
with zipfile.ZipFile(jar_path, 'r') as z:
    ru_ru = json.loads(z.read('assets/industrialupgrade/lang/ru_ru.json').decode('utf-8'))
    item_models = [n.replace('assets/industrialupgrade/models/item/', '').replace('.json', '')
                   for n in z.namelist() if n.startswith('assets/industrialupgrade/models/item/') and n.endswith('.json')]
    block_models = [n.replace('assets/industrialupgrade/models/block/', '').replace('.json', '')
                    for n in z.namelist() if n.startswith('assets/industrialupgrade/models/block/') and n.endswith('.json')]

all_items = set(item_models)

print(f"Total item models: {len(all_items)}, lang entries: {len(ru_ru)}")

def find_best_item_id(name_hint):
    name_clean = name_hint.lower().strip()
    # 1. exact match in item_models
    for it in all_items:
        if it == name_clean or it.endswith('/' + name_clean):
            return f"industrialupgrade:{it}"
    # 2. substring match
    for it in all_items:
        if name_clean in it:
            return f"industrialupgrade:{it}"
    return f"industrialupgrade:{name_clean}"

def parse_tab_dump(dump_file):
    with open(dump_file, 'r', encoding='utf-8') as f:
        text = f.read()

    # Split by Quest$Builder.create:()
    chunks = text.split('invokestatic  #96                 // Method com/denfop/api/guidebook/Quest$Builder.create:()')
    quests = []

    for chunk in chunks[1:]:
        # Extract name
        name_m = re.search(r'ldc(?:_w)?\s+#\d+\s+//\s+String\s+([a-zA-Z0-9_]+)', chunk)
        qname = name_m.group(1) if name_m else "unknown"

        # Extract prev
        prev_m = re.findall(r'//\s+String\s+([a-zA-Z0-9_]+)\n.*?Quest\$Builder\.prev:\(Ljava/lang/String;\)', chunk, re.DOTALL)
        prevs = prev_m if prev_m else []

        # Extract position
        # bipush / sipush / iconst
        pos_m = re.findall(r'(?:bipush|sipush|iconst_m1|iconst_\d)\s+(-?\d+)', chunk)
        # Quest$Builder.position:(II) takes x, y from stack
        # Let's find instructions before Quest$Builder.position:(II)
        pos_call = chunk.find('Quest$Builder.position:(II)')
        x, y = 0, 0
        if pos_call != -1:
            sub = chunk[:pos_call]
            numbers = re.findall(r'(?:bipush|sipush|iconst_m1|iconst_0|iconst_1|iconst_2|iconst_3|iconst_4|iconst_5)\s*(-?\d+)?', sub)
            parsed_nums = []
            for instr, val in re.findall(r'(bipush|sipush|iconst_m1|iconst_0|iconst_1|iconst_2|iconst_3|iconst_4|iconst_5)(?:\s+(-?\d+))?', sub):
                if instr == 'iconst_0': parsed_nums.append(0)
                elif instr == 'iconst_1': parsed_nums.append(1)
                elif instr == 'iconst_2': parsed_nums.append(2)
                elif instr == 'iconst_3': parsed_nums.append(3)
                elif instr == 'iconst_4': parsed_nums.append(4)
                elif instr == 'iconst_5': parsed_nums.append(5)
                elif instr == 'iconst_m1': parsed_nums.append(-1)
                elif val: parsed_nums.append(int(val))
            if len(parsed_nums) >= 2:
                x = parsed_nums[-2]
                y = parsed_nums[-1]

        # Extract item / block field or hint
        field_m = re.search(r'//\s+Field\s+com/denfop/[a-zA-Z0-9_/]+(?:\.([a-zA-Z0-9_]+))?:', chunk)
        field_name = field_m.group(1) if field_m and field_m.group(1) else qname

        # Find localization title and description
        title = ru_ru.get(f"iu.guide_quest.{qname}", "")
        if not title:
            title = ru_ru.get(f"item.industrialupgrade.{qname}", "")
        if not title:
            title = ru_ru.get(f"block.industrialupgrade.{qname}", "")
        if not title:
            title = qname.replace('_', ' ').title()

        desc = ru_ru.get(f"iu.guide_quest_description.{qname}", "")
        if not desc:
            desc = ru_ru.get(f"iu.guide_quest_description.{field_name}", "")

        item_id = find_best_item_id(qname)
        quests.append({
            'name': qname,
            'field': field_name,
            'prev': prevs,
            'x': x,
            'y': y,
            'title': title,
            'desc': desc,
            'item': item_id
        })

    return quests

steam_quests = parse_tab_dump('steamTab_dump.txt')
base_quests = parse_tab_dump('baseElectricTab_dump.txt')
improved_quests = parse_tab_dump('improvedElectricTab_dump.txt')

print(f"Parsed steamTab quests: {len(steam_quests)}")
print(f"Parsed baseElectricTab quests: {len(base_quests)}")
print(f"Parsed improvedElectricTab quests: {len(improved_quests)}")

with open('parsed_all_iu_quests.json', 'w', encoding='utf-8') as out:
    json.dump({
        'steam': steam_quests,
        'base': base_quests,
        'improved': improved_quests
    }, out, ensure_ascii=False, indent=2)

print("Saved to parsed_all_iu_quests.json")
