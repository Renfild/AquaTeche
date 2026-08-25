import zipfile
import glob
from pathlib import Path

target_items = [
    ("industrialupgrade:crafting_elements/crafting_773_element", "IU_JAR", "773"),
    ("industrialupgrade:crafting_elements/crafting_772_element", "IU_JAR", "772"),
    ("industrialupgrade:classicore/lead", "IU_JAR", "lead"),
    ("industrialupgrade:smeltery/smeltery_controller", "IU_JAR", "smeltery"),
    ("industrialupgrade:smeltery/smeltery_casing", "IU_JAR", "smeltery"),
    ("industrialupgrade:baseore1/beryllium", "IU_JAR", "beryllium"),
    ("industrialupgrade:crafting_elements/crafting_271_element", "IU_JAR", "271"),
    ("industrialupgrade:upgrades/overclocker", "IU_JAR", "overclock"),
    ("industrialupgrade:battery/re_battery", "IU_JAR", "re_battery"),
    ("industrialupgrade:cable/copper_cable", "IU_JAR", "copper"),
    ("industrialupgrade:blockresource/advanced_machine", "IU_JAR", "advanced_machine"),
    ("industrialupgrade:battery/energy_crystal", "IU_JAR", "energy_crystal"),
    ("alexscaves:pearl", "AC_JAR", "pearl"),
    ("aquatech_ui:speed_x4_upgrade", "AT_JAR", "speed"),
    ("industrialupgrade:battery/lapotron_crystal", "IU_JAR", "lapotron"),
    ("industrialupgrade:itemingots/adamantium", "IU_JAR", "adamant"),
    ("industrialupgrade:nuclearresource/uranium_235", "IU_JAR", "uranium_235"),
    ("mythicbotany:alfsteel_ingot", "BOT_JAR", "alfsteel"),
    ("draconicevolution:basic_crafting_injector", "DE_JAR", "injector"),
    ("avaritia:neutron_compressor", "AV_JAR", "compressor"),
]

for item_id, jar_code, search in target_items:
    print(f"\nItem {item_id} (search '{search}'):")
    for jar in glob.glob('server/mods/*.jar'):
        try:
            with zipfile.ZipFile(jar, 'r') as z:
                for n in z.namelist():
                    if n.startswith('assets/') and 'textures/' in n and n.endswith('.png') and search.lower() in n.lower():
                        print(f"  [{Path(jar).name}] {n}")
        except: pass
