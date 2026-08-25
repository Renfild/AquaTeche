import re, json

with open('init_code.txt', 'r', encoding='utf-8') as f:
    code = f.read()

# Let's find all references to tabs (steamTab, baseElectricTab, improvedElectricTab, advancedElectricTab)
# In javap verbose output, lines have comments like // String steamTab, // Method com/denfop/api/guidebook/Quest$Builder...
lines = code.splitlines()

current_tab = "UNKNOWN"
tab_data = {}

for i, line in enumerate(lines):
    if '// String steamTab' in line:
        current_tab = 'steamTab'
        tab_data[current_tab] = []
    elif '// String baseElectricTab' in line:
        current_tab = 'baseElectricTab'
        tab_data[current_tab] = []
    elif '// String improvedElectricTab' in line:
        current_tab = 'improvedElectricTab'
        tab_data[current_tab] = []
    elif '// String advancedElectricTab' in line:
        current_tab = 'advancedElectricTab'
        tab_data[current_tab] = []
    elif '// String perElectricTab' in line:
        current_tab = 'perElectricTab'
        tab_data[current_tab] = []
    
    if current_tab in tab_data:
        tab_data[current_tab].append(line)

print("Found tabs with line counts:")
for t, l in tab_data.items():
    print(f"  {t}: {len(l)} lines")
