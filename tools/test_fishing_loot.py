import re
import sys
from pathlib import Path

# Add current dir to sys.path
sys.path.insert(0, str(Path('.').resolve()))

from tools.test_all_cases import valid_items

with open('mods/aquatech-ui/src/main/java/net/aquatech/ui/fishing/FishingLootHandler.java', 'r', encoding='utf-8') as f:
    code = f.read()

res_locs = re.findall(r'new ResourceLocation\("([^"]+)"\)', code)

lines = []
lines.append(f"Checking {len(res_locs)} ResourceLocations in FishingLootHandler.java:")
for loc in sorted(set(res_locs)):
    status = "[OK]" if loc in valid_items else "[CHECK]"
    lines.append(f"  {status} {loc}")

with open('test_fishing_loot_result.txt', 'w', encoding='utf-8') as f:
    f.write('\n'.join(lines))

print("Results written to test_fishing_loot_result.txt")
