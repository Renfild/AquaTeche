import json, zipfile

jar_path = 'c:/Users/xieto/Desktop/AquaTech/mods/IndustrialUpgrade-1.20.1-3.4.0.11.jar'
with zipfile.ZipFile(jar_path, 'r') as z:
    ru_ru = json.loads(z.read('assets/industrialupgrade/lang/ru_ru.json').decode('utf-8'))

def get_russian_title(qname, item_id):
    # 1. Direct quest title
    if f"iu.guide_quest.{qname}" in ru_ru:
        return ru_ru[f"iu.guide_quest.{qname}"]
    
    # 2. Item name from registry
    # item_id: industrialupgrade:basemachine3/steamboiler
    clean_item = item_id.replace('industrialupgrade:', '')
    sub_name = clean_item.split('/')[-1]

    for k in [
        f"item.industrialupgrade.{sub_name}",
        f"block.industrialupgrade.{sub_name}",
        f"industrialupgrade.basemachine3.{sub_name}",
        f"industrialupgrade.basemachine.{sub_name}",
        f"industrialupgrade.basemachine1.{sub_name}",
        f"industrialupgrade.basemachine2.{sub_name}",
        f"industrialupgrade.basemachine4.{sub_name}",
        f"industrialupgrade.basemachine5.{sub_name}",
        f"industrialupgrade.crafting_elements.{sub_name}",
        f"industrialupgrade.itemplates.{sub_name}",
        f"industrialupgrade.itemdust.{sub_name}",
        f"item.industrialupgrade.{clean_item.replace('/', '.')}",
        f"block.industrialupgrade.{clean_item.replace('/', '.')}",
    ]:
        if k in ru_ru:
            return ru_ru[k]

    # Search in all ru_ru for exact matching key end
    for k, v in ru_ru.items():
        if k.endswith('.' + sub_name) or k.endswith('.' + qname):
            return v

    return qname.replace('_', ' ').title()

# Let's test
print("steamboiler ->", get_russian_title('steamboiler', 'industrialupgrade:basemachine3/steamboiler'))
print("steam_machine_block ->", get_russian_title('steam_machine_block', 'industrialupgrade:blockresource/bronze_machine_block'))
print("elemotor ->", get_russian_title('elemotor', 'industrialupgrade:crafting_elements/elemotor'))
print("laser_polisher ->", get_russian_title('laser_polisher', 'industrialupgrade:basemachine3/laser_polisher'))
