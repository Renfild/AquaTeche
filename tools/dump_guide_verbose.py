import zipfile, subprocess, tempfile, os, json, re

jar_path = 'c:/Users/xieto/Desktop/AquaTech/mods/IndustrialUpgrade-1.20.1-3.4.0.11.jar'
with tempfile.TemporaryDirectory() as tmp:
    with zipfile.ZipFile(jar_path, 'r') as z:
        z.extract('com/denfop/api/guidebook/GuideBookCore.class', tmp)
        lang_data = json.loads(z.read('assets/industrialupgrade/lang/ru_ru.json').decode('utf-8'))
    
    cls_path = os.path.join(tmp, 'com/denfop/api/guidebook/GuideBookCore.class')
    # Run javap with full constant pool and verbose flag
    res = subprocess.run(['javap', '-v', '-p', '-c', cls_path], capture_output=True, text=True, encoding='utf-8', errors='ignore')
    
    with open('guide_core_verbose.txt', 'w', encoding='utf-8') as f:
        f.write(res.stdout)

print("Verbose javap saved, length:", len(res.stdout))
