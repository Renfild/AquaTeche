import json
import re
import glob

with open(r"C:\Users\xieto\Desktop\AquaTech\registries.json", encoding="utf-8") as f:
    REG = {k: set(v) for k, v in json.load(f).items()}

ITEM_RE = re.compile(r'(?:item|icon)\s*:\s*"([a-z0-9_.]+):([a-z0-9_./]+)"')

files = sorted(glob.glob(r"C:\Users\xieto\Desktop\AquaTech\server\config\ftbquests\quests\chapters\*.snbt"))

# a modest built-in allowlist for common vanilla ids not seen in mod translation files
# (mods rarely reference every vanilla item in their lang files, so we can't fully
# validate 'minecraft:' from REG alone). We only flag minecraft: id if it's not in
# REG['minecraft'] AND not in this manual common-sense list -- actually since that list
# would be huge, we just skip strict validation for minecraft: namespace and instead
# rely on manual review; script focuses on MODDED namespaces which is where real risk is.

total_bad = 0
for path in files:
    with open(path, encoding="utf-8") as f:
        lines = f.readlines()
    bad_here = []
    for i, line in enumerate(lines, start=1):
        for m in ITEM_RE.finditer(line):
            ns, name = m.group(1), m.group(2)
            if ns == "minecraft":
                continue
            if ns not in REG:
                bad_here.append((i, f"{ns}:{name}", "NAMESPACE NOT INSTALLED"))
                continue
            if name not in REG[ns]:
                bad_here.append((i, f"{ns}:{name}", "ITEM NOT FOUND"))
    if bad_here:
        print(f"=== {path} ===")
        for lineno, item, reason in bad_here:
            print(f"  L{lineno}: {item}  -> {reason}")
        total_bad += len(bad_here)

print(f"\nTOTAL ISSUES: {total_bad}")
