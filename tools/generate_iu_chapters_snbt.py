import json, hashlib, re

def gen_id(seed):
    return hashlib.md5(seed.encode('utf-8')).hexdigest()[:16].upper()

with open('full_tabs_resolved.json', 'r', encoding='utf-8') as f:
    tabs_data = json.load(f)

def clean_description(desc_text):
    if not desc_text:
        return []
    # Clean non-breaking spaces or weird chars
    desc_text = desc_text.replace('\xa0', ' ').strip()
    # Split into short sentences
    sentences = re.split(r'(?<=[.!?])\s+', desc_text)
    # Take first 2-3 most important sentences
    shortened = ' '.join(sentences[:3])
    # Format into lines of ~60 chars
    lines = []
    current_line = []
    current_len = 0
    for word in shortened.split():
        if current_len + len(word) + 1 > 60:
            lines.append(' '.join(current_line))
            current_line = [word]
            current_len = len(word)
        else:
            current_line.append(word)
            current_len += len(word) + 1
    if current_line:
        lines.append(' '.join(current_line))
    return lines

def format_snbt_chapter(tab_key, filename, chapter_id, title, icon_item, order_index, quests):
    # Create quest name -> quest id map
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
        # Dependencies
        deps = []
        for p in q['prevs']:
            if p in name_to_id:
                deps.append(f'"{name_to_id[p]}"')
        if deps:
            lines.append(f"\t\t\tdependencies: [{', '.join(deps)}]")

        lines.append(f'\t\t\tid: "{qid}"')

        # Subtitle / Description
        desc_lines = clean_description(q['desc'])
        if desc_lines:
            lines.append("\t\t\tdescription: [")
            for dl in desc_lines:
                # Escape quotes
                safe_dl = dl.replace('"', '\\"')
                lines.append(f'\t\t\t\t"{safe_dl}"')
            lines.append("\t\t\t]")

        if q['title']:
            safe_title = q['title'].replace('"', '\\"')
            lines.append(f'\t\t\ttitle: "{safe_title}"')

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

        lines.append(f"\t\t\tx: {q['x']}d")
        lines.append(f"\t\t\ty: {q['y']}d")
        lines.append("\t\t}")

    lines.append("\t]")
    lines.append(f'\ttitle: "{title}"')
    lines.append("}")
    return '\n'.join(lines)

# Generate the 3 chapters
steam_snbt = format_snbt_chapter('steam', 'steam_era', gen_id('chapter_steam'), '1. Паровая эпоха', 'industrialupgrade:basemachine3/steamboiler', 2, tabs_data['steam']['quests'])
base_snbt = format_snbt_chapter('base', 'basic_electric_era', gen_id('chapter_base_electric'), '2. Базовая электрическая эра', 'industrialupgrade:basemachine/generator', 3, tabs_data['base']['quests'])
improved_snbt = format_snbt_chapter('improved', 'improved_electric_era', gen_id('chapter_improved_electric'), '3. Улучшенная электрическая эра', 'industrialupgrade:basemachine3/pallet_generator', 4, tabs_data['improved']['quests'])

with open('config/ftbquests/quests/chapters/steam_era.snbt', 'w', encoding='utf-8') as out:
    out.write(steam_snbt)

with open('config/ftbquests/quests/chapters/basic_electric_era.snbt', 'w', encoding='utf-8') as out:
    out.write(base_snbt)

with open('config/ftbquests/quests/chapters/improved_electric_era.snbt', 'w', encoding='utf-8') as out:
    out.write(improved_snbt)

print("Generated steam_era.snbt, basic_electric_era.snbt, improved_electric_era.snbt successfully!")
