import re, json

with open('tools/split_tabs.py', 'r') as f:
    pass

with open('init_code.txt', 'r', encoding='utf-8') as f:
    code = f.read()

with open('iu_quest_lang.json', 'r', encoding='utf-8') as f:
    lang = json.load(f)

# Also let's load all lang keys from ru_ru.json
import zipfile
jar_path = 'c:/Users/xieto/Desktop/AquaTech/mods/IndustrialUpgrade-1.20.1-3.4.0.11.jar'
with zipfile.ZipFile(jar_path, 'r') as z:
    all_lang = json.loads(z.read('assets/industrialupgrade/lang/ru_ru.json').decode('utf-8'))

lines = code.splitlines()

current_tab = "UNKNOWN"
tab_data = {'steamTab': [], 'baseElectricTab': [], 'improvedElectricTab': []}

for line in lines:
    if '// String steamTab' in line:
        current_tab = 'steamTab'
    elif '// String baseElectricTab' in line:
        current_tab = 'baseElectricTab'
    elif '// String improvedElectricTab' in line:
        current_tab = 'improvedElectricTab'
    elif '// String advancedElectricTab' in line or '// String perElectricTab' in line:
        current_tab = 'OTHER'
    
    if current_tab in tab_data:
        tab_data[current_tab].append(line)

# Let's inspect how Quest.builder is called in each tab
for tname, tlines in tab_data.items():
    print(f"=== TAB: {tname} ===")
    ttext = '\n'.join(tlines)
    # Search for builder and method calls
    # e.g. ldc / Field / Method
    with open(f'{tname}_dump.txt', 'w', encoding='utf-8') as out:
        out.write(ttext)
    print(f"Wrote {tname}_dump.txt")
