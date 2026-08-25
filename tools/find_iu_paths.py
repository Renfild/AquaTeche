import zipfile

iu_jar = 'server/mods/IndustrialUpgrade-1.20.1-3.4.0.11.jar'
with zipfile.ZipFile(iu_jar, 'r') as z:
    all_names = z.namelist()

def find_iu(patterns):
    print(f"\nSearching for {patterns}:")
    for name in all_names:
        if name.startswith('assets/industrialupgrade/models/item/') or name.startswith('assets/industrialupgrade/blockstates/'):
            for p in patterns:
                if p.lower() in name.lower():
                    print(" ", name)

find_iu(['classicore/lead', 'baseore/lead', 'deep_lead_ore', 'ore/lead'])
find_iu(['smeltery_controller', 'smeltery_casing', 'smeltery_furnace', 'smeltery/'])
find_iu(['beryllium', 'baseore2/'])
find_iu(['rubber', 'resin', 'rubber_drop'])
find_iu(['overclock', 'upgrade'])
find_iu(['re_battery', 'battery/'])
find_iu(['copper_cable', 'wiring/', 'cable'])
find_iu(['adv_machine', 'blockresource/'])
