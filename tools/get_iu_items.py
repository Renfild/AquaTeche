import zipfile
import glob
import json
import re

# Let's inspect all registrations in IndustrialUpgrade and other mods
# In Forge 1.20.1, DeferredRegister is used. We can extract all String constants registered in Forge registries or check jar contents.
iu_items = set()
iu_jar = 'server/mods/IndustrialUpgrade-1.20.1-3.4.0.11.jar'
with zipfile.ZipFile(iu_jar, 'r') as z:
    for name in z.namelist():
        # Find all json models or lang entries
        if name.startswith('assets/industrialupgrade/models/item/'):
            sub = name[len('assets/industrialupgrade/models/item/'):-5]
            iu_items.add(sub)
        elif name.startswith('assets/industrialupgrade/blockstates/'):
            sub = name[len('assets/industrialupgrade/blockstates/'):-5]
            iu_items.add(sub)

# Let's find all item identifiers in IU
with open('iu_registered_items.txt', 'w', encoding='utf-8') as f:
    for item in sorted(iu_items):
        f.write(f"industrialupgrade:{item}\n")

print(f"Total IU item models found: {len(iu_items)}")
