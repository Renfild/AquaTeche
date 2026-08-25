import zipfile
import glob
import json
import re

def search_mod(mod_glob, keywords):
    print(f"\n=== Searching in {mod_glob} for {keywords} ===")
    for jar in glob.glob(f'server/mods/{mod_glob}'):
        with zipfile.ZipFile(jar, 'r') as z:
            names = [n for n in z.namelist() if n.startswith('assets/') and ('models/item/' in n or 'blockstates/' in n)]
            for kw in keywords:
                matches = [n for n in names if kw.lower() in n.lower()]
                print(f"  Keyword '{kw}':")
                for m in matches[:10]:
                    print(f"    {m}")

# 1. Lead ore in IU
search_mod('*industrialupgrade*', ['lead', 'smeltery', 'beryllium', 'rubber', 'overclock', 'battery', 'cable', 'adv_machine', 'energy_crystal', 'lapotron', 'adamant', 'uranium_235'])

# 2. Alex's Caves items
search_mod('*alexscaves*', ['pearl', 'abyss', 'marine'])

# 3. AquaTech UI items
search_mod('*aquatech*', ['speed', 'upgrade'])

# 4. MythicBotany / Botania / ExtraBotany alfsteel
search_mod('*botan*', ['alfsteel', 'orichalc'])

# 5. Draconic Evolution injectors
search_mod('*draconic*', ['injector', 'crafting'])

# 6. Avaritia compressor
search_mod('*avaritia*', ['compressor', 'neutron'])
