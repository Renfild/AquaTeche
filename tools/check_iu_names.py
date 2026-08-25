import zipfile, json

iu_jar = 'server/mods/IndustrialUpgrade-1.20.1-3.4.0.11.jar'
with zipfile.ZipFile(iu_jar, 'r') as z:
    names = set()
    for n in z.namelist():
        if n.startswith('assets/industrialupgrade/models/item/'):
            names.add(n[len('assets/industrialupgrade/models/item/'):-5])
        elif n.startswith('assets/industrialupgrade/blockstates/'):
            names.add(n[len('assets/industrialupgrade/blockstates/'):-5])

def check(kw):
    print(f"\n--- Matches for {kw} ---")
    for n in sorted(names):
        if kw.lower() in n.lower():
            print(" ", n)

check("lead")
check("smeltery")
check("beryllium")
check("rubber")
check("overclock")
check("re_battery")
check("battery")
check("copper_cable")
