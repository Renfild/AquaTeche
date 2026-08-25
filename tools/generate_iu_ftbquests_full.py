import re, json, zipfile, os, hashlib

jar_path = 'c:/Users/xieto/Desktop/AquaTech/mods/IndustrialUpgrade-1.20.1-3.4.0.11.jar'
with zipfile.ZipFile(jar_path, 'r') as z:
    ru_ru = json.loads(z.read('assets/industrialupgrade/lang/ru_ru.json').decode('utf-8'))
    item_models = [n.replace('assets/industrialupgrade/models/item/', '').replace('.json', '')
                   for n in z.namelist() if n.startswith('assets/industrialupgrade/models/item/') and n.endswith('.json')]
    block_models = [n.replace('assets/industrialupgrade/models/block/', '').replace('.json', '')
                    for n in z.namelist() if n.startswith('assets/industrialupgrade/models/block/') and n.endswith('.json')]

all_items = set(item_models)

def gen_id(seed):
    return hashlib.md5(seed.encode('utf-8')).hexdigest()[:16].upper()

def get_best_registry_item(qname, chunk):
    # Check if chunk mentions a specific Block entity field
    bm_field = re.search(r'//\s+Field\s+com/denfop/blocks/mechanism/(Block[a-zA-Z0-9_]+Entity)\.([a-zA-Z0-9_]+):', chunk)
    if bm_field:
        f_name = bm_field.group(2)
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

    return f"industrialupgrade:{qname}"

def get_russian_title(qname, item_id):
    if f"iu.guide_quest.{qname}" in ru_ru and not ru_ru[f"iu.guide_quest.{qname}"].startswith('iu.'):
        return ru_ru[f"iu.guide_quest.{qname}"]

    clean_item = item_id.replace('industrialupgrade:', '')
    sub_name = clean_item.split('/')[-1]

    for k in [
        f"item.industrialupgrade.{sub_name}",
        f"block.industrialupgrade.{sub_name}",
        f"industrialupgrade.basemachine3.{sub_name}",
        f"industrialupgrade.basemachine.{sub_name}",
        f"industrialupgrade.basemachine1.{sub_name}",
        f"industrialupgrade.basemachine2.{sub_name}",
        f"industrialupgrade.basemachine4.{sub_name}",
        f"industrialupgrade.basemachine5.{sub_name}",
        f"industrialupgrade.crafting_elements.{sub_name}",
        f"industrialupgrade.itemplates.{sub_name}",
        f"industrialupgrade.itemdust.{sub_name}",
        f"item.industrialupgrade.{clean_item.replace('/', '.')}",
        f"block.industrialupgrade.{clean_item.replace('/', '.')}",
    ]:
        if k in ru_ru and not k.startswith('iu.guide_quest_description'):
            return ru_ru[k]

    for k, v in ru_ru.items():
        if not k.startswith('iu.guide_quest_description') and (k.endswith('.' + sub_name) or k.endswith('.' + qname)):
            return v

    return qname.replace('_', ' ').title()

def clean_description(desc_text):
    if not desc_text:
        return []
    desc_text = desc_text.replace('\xa0', ' ').replace('\r', '').strip()
    sentences = re.split(r'(?<=[.!?])\s+', desc_text)
    # Take first 2 sentences for a brief, readable description
    shortened = ' '.join(sentences[:2]) if len(sentences) > 1 else desc_text
    lines = []
    current_line = []
    current_len = 0
    for word in shortened.split():
        if current_len + len(word) + 1 > 55:
            lines.append(' '.join(current_line))
            current_line = [word]
            current_len = len(word)
        else:
            current_line.append(word)
            current_len += len(word) + 1
    if current_line:
        lines.append(' '.join(current_line))
    return lines

def parse_tab_file(dump_file):
    with open(dump_file, 'r', encoding='utf-8') as f:
        text = f.read()

    chunks = text.split('invokestatic  #96                 // Method com/denfop/api/guidebook/Quest$Builder.create:()')
    quests = []

    for chunk in chunks[1:]:
        name_m = re.search(r'//\s+String\s+([a-zA-Z0-9_]+)\n\s+\d+:\s+invokevirtual\s+#\d+\s+//\s+Method\s+com/denfop/api/guidebook/Quest\$Builder\.name:', chunk)
        qname = name_m.group(1) if name_m else "unknown"

        prev_m = re.findall(r'//\s+String\s+([a-zA-Z0-9_]+)\n\s+\d+:\s+invokevirtual\s+#\d+\s+//\s+Method\s+com/denfop/api/guidebook/Quest\$Builder\.prev:', chunk)
        prevs = [p for p in prev_m if p != qname]

        # Position
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

        item_id = get_best_registry_item(qname, chunk)
        title = get_russian_title(qname, item_id)
        desc = ru_ru.get(f"iu.guide_quest_description.{qname}", "")

        quests.append({
            'name': qname,
            'prevs': prevs,
            'x': round(x / 40.0, 2),
            'y': round(y / 40.0, 2),
            'title': title,
            'desc': desc,
            'item': item_id
        })

    return quests

def write_snbt_chapter(tab_key, filename, chapter_id, title, icon_item, order_index, quests):
    name_to_id = {}
    for q in quests:
        name_to_id[q['name']] = gen_id(f"{tab_key}_{q['name']}")

    lines = []
    lines.append("{")
    lines.append("\tdefault_hide_dependency_lines: false")
    lines.append('\tdefault_quest_shape: ""')
    lines.append(f'\tfilename: "{filename}"')
    lines.append('\tgroup: ""')
    lines.append(f'\ticon: "{icon_item}"')
    lines.append(f'\tid: "{chapter_id}"')
    lines.append(f"\torder_index: {order_index}")
    lines.append("\tquest_links: [ ]")
    lines.append("\tquests: [")

    for q in quests:
        qid = name_to_id[q['name']]
        task_id = gen_id(f"{qid}_task")
        reward_id = gen_id(f"{qid}_reward")

        lines.append("\t\t{")
        deps = [f'"{name_to_id[p]}"' for p in q['prevs'] if p in name_to_id and name_to_id[p] != qid]
        if deps:
            lines.append(f"\t\t\tdependencies: [{', '.join(deps)}]")

        desc_lines = clean_description(q['desc'])
        if desc_lines:
            lines.append("\t\t\tdescription: [")
            for dl in desc_lines:
                safe_dl = dl.replace('"', '\\"')
                lines.append(f'\t\t\t\t"{safe_dl}"')
            lines.append("\t\t\t]")

        lines.append(f'\t\t\tid: "{qid}"')

        lines.append("\t\t\trewards: [{")
        lines.append(f'\t\t\t\tid: "{reward_id}"')
        lines.append('\t\t\t\ttype: "xp"')
        lines.append('\t\t\t\txp: 50')
        lines.append("\t\t\t}]")

        lines.append("\t\t\ttasks: [{")
        lines.append(f'\t\t\t\tid: "{task_id}"')
        lines.append(f'\t\t\t\titem: "{q["item"]}"')
        lines.append('\t\t\t\ttype: "item"')
        lines.append("\t\t\t}]")

        if q['title']:
            safe_title = q['title'].replace('"', '\\"')
            lines.append(f'\t\t\ttitle: "{safe_title}"')

        lines.append(f"\t\t\tx: {q['x']}d")
        lines.append(f"\t\t\ty: {q['y']}d")
        lines.append("\t\t}")

    lines.append("\t]")
    lines.append(f'\ttitle: "{title}"')
    lines.append("}")

    out_path = f"config/ftbquests/quests/chapters/{filename}.snbt"
    with open(out_path, 'w', encoding='utf-8') as out:
        out.write('\n'.join(lines))
    print(f"Wrote {out_path} ({len(quests)} quests)")

steam_quests = parse_tab_file('steamTab_dump.txt')
base_quests = parse_tab_file('baseElectricTab_dump.txt')
improved_quests = parse_tab_file('improvedElectricTab_dump.txt')

write_snbt_chapter('steam', 'steam_era', gen_id('chapter_steam'), '1. Паровая эпоха', 'industrialupgrade:basemachine3/steamboiler', 2, steam_quests)
write_snbt_chapter('base', 'basic_electric_era', gen_id('chapter_base_electric'), '2. Базовая электрическая эра', 'industrialupgrade:basemachine/generator', 3, base_quests)
write_snbt_chapter('improved', 'improved_electric_era', gen_id('chapter_improved_electric'), '3. Улучшенная электрическая эра', 'industrialupgrade:basemachine3/pallet_generator', 4, improved_quests)

print("All 3 chapters generated successfully!")
