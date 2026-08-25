import re
import sys
from pathlib import Path
sys.path.insert(0, str(Path('.').resolve()))

from tools.test_all_cases import valid_items

with open('mods/aquatech-ui/src/main/java/net/aquatech/ui/fishing/FishingLootHandler.java', 'r', encoding='utf-8') as f:
    code = f.read()

get_mod_items = re.findall(r'getModItem\("([^"]+)"', code)

lines = []
lines.append(f"Checking {len(get_mod_items)} getModItem calls in FishingLootHandler.java:")
errors = 0
for item in sorted(set(get_mod_items)):
    status = "[OK]" if item in valid_items else "[ERROR]"
    if status == "[ERROR]":
        errors += 1
    lines.append(f"  {status} {item}")

lines.append(f"\nTotal FishingLootHandler errors: {errors}")

with open('test_fishing_loot_result2.txt', 'w', encoding='utf-8') as f:
    f.write('\n'.join(lines))

print(f"Results written. Total errors: {errors}")
