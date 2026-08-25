import zipfile, subprocess, tempfile, os

jar_path = 'c:/Users/xieto/Desktop/AquaTech/mods/IndustrialUpgrade-1.20.1-3.4.0.11.jar'
found = []
with tempfile.TemporaryDirectory() as tmp:
    with zipfile.ZipFile(jar_path, 'r') as z:
        for name in z.namelist():
            if name.endswith('.class') and ('guide' in name.lower() or 'quest' in name.lower() or 'tab' in name.lower() or 'book' in name.lower()):
                z.extract(name, tmp)
                cls_path = os.path.join(tmp, name)
                res = subprocess.run(['javap', '-c', cls_path], capture_output=True, text=True, encoding='utf-8', errors='ignore')
                if 'Quest$Builder' in res.stdout or 'addQuest' in res.stdout or 'addTab' in res.stdout:
                    found.append((name, len(res.stdout)))

print("Classes defining quests:")
for n, sz in found:
    print(n, sz)
