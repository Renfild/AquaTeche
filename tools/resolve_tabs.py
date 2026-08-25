import re, json, zipfile, os

jar_path = 'c:/Users/xieto/Desktop/AquaTech/mods/IndustrialUpgrade-1.20.1-3.4.0.11.jar'
with zipfile.ZipFile(jar_path, 'r') as z:
    ru_ru = json.loads(z.read('assets/industrialupgrade/lang/ru_ru.json').decode('utf-8'))
    item_models = [n.replace('assets/industrialupgrade/models/item/', '').replace('.json', '')
                   for n in z.namelist() if n.startswith('assets/industrialupgrade/models/item/') and n.endswith('.json')]
    block_models = [n.replace('assets/industrialupgrade/models/block/', '').replace('.json', '')
                    for n in z.namelist() if n.startswith('assets/industrialupgrade/models/block/') and n.endswith('.json')]

all_items = set(item_models)

# Create mapping dictionary by searching
def get_best_registry_item(qname, chunk):
    # Check if chunk mentions a specific Block entity field
    # e.g. BlockBaseMachine3Entity.steamboiler
    bm_field = re.search(r'//\s+Field\s+com/denfop/blocks/mechanism/(Block[a-zA-Z0-9_]+Entity)\.([a-zA-Z0-9_]+):', chunk)
    if bm_field:
        ent_class = bm_field.group(1)
        f_name = bm_field.group(2)
        # Check if f_name exists in item_models
        for it in all_items:
            if it.endswith('/' + f_name) or it == f_name:
                return f"industrialupgrade:{it}"

    # Check for direct item name
    for it in all_items:
        if it == qname or it.endswith('/' + qname):
            return f"industrialupgrade:{it}"

    # Check subwords
    for it in all_items:
        if qname in it:
            return f"industrialupgrade:{it}"

    # Fallback to industrialupgrade:qname
    return f"industrialupgrade:{qname}"

def parse_full_tab(dump_file, tab_title, icon_item, order_index):
    with open(dump_file, 'r', encoding='utf-8') as f:
        text = f.read()

    chunks = text.split('invokestatic  #96                 // Method com/denfop/api/guidebook/Quest$Builder.create:()')
    quests = []

    for chunk in chunks[1:]:
        name_m = re.search(r'ldc(?:_w)?\s+#\d+\s+//\s+String\s+([a-zA-Z0-9_]+)', chunk)
        qname = name_m.group(1) if name_m else "unknown"

        prev_m = re.findall(r'ldc(?:_w)?\s+#\d+\s+//\s+String\s+([a-zA-Z0-9_]+)\n.*?Quest\$Builder\.prev:\(Ljava/lang/String;\)', chunk, re.DOTALL)
        prevs = prev_m if prev_m else []

        # Find position
        pos_call = chunk.find('Quest$Builder.position:(II)')
        x, y = 0, 0
        if pos_call != -1:
            sub = chunk[:pos_call]
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

        # Titles and descriptions
        title = ru_ru.get(f"iu.guide_quest.{qname}", "")
        if not title:
            title = ru_ru.get(f"item.industrialupgrade.{qname}", "")
        if not title:
            title = ru_ru.get(f"block.industrialupgrade.{qname}", "")
        if not title:
            title = qname.replace('_', ' ').title()

        desc = ru_ru.get(f"iu.guide_quest_description.{qname}", "")
        # Clean description
        if desc:
            # Shorten description to clear concise sentences if too long
            desc_clean = desc.strip()
        else:
            desc_clean = ""

        item_id = get_best_registry_item(qname, chunk)

        quests.append({
            'name': qname,
            'prevs': prevs,
            'raw_x': x,
            'raw_y': y,
            'x': round(x / 40.0, 2),
            'y': round(y / 40.0, 2),
            'title': title,
            'desc': desc_clean,
            'item': item_id
        })

    return {
        'title': tab_title,
        'icon': icon_item,
        'order_index': order_index,
        'quests': quests
    }

steam_tab = parse_full_tab('steamTab_dump.txt', '1. Паровая эпоха', 'industrialupgrade:basemachine3/steamboiler', 1)
base_tab = parse_full_tab('baseElectricTab_dump.txt', '2. Базовая электрическая эра', 'industrialupgrade:basemachine/generator', 2)
improved_tab = parse_full_tab('improvedElectricTab_dump.txt', '3. Улучшенная электрическая эра', 'industrialupgrade:basemachine3/pallet_generator', 3)

with open('full_tabs_resolved.json', 'w', encoding='utf-8') as out:
    json.dump({'steam': steam_tab, 'base': base_tab, 'improved': improved_tab}, out, ensure_ascii=False, indent=2)

print("Saved full_tabs_resolved.json")
