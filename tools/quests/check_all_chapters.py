import re
import glob
from collections import Counter

files = sorted(glob.glob(r"C:\Users\xieto\Desktop\AquaTech\server\config\ftbquests\quests\chapters\*.snbt"))

all_ids = Counter()
id_to_file = {}
all_deps = []  # (file, dep_id)
file_contents = {}

for path in files:
    with open(path, encoding="utf-8") as f:
        content = f.read()
    file_contents[path] = content
    ids = re.findall(r'id: "([0-9A-Za-z]+)"', content)
    for i in ids:
        all_ids[i] += 1
        id_to_file.setdefault(i, []).append(path)
    for dep_list in re.findall(r'dependencies: \[([^\]]*)\]', content):
        for m in re.findall(r'"([0-9A-Za-z]+)"', dep_list):
            all_deps.append((path, m))

print("=== Duplicate IDs across ALL chapters ===")
dupe_count = 0
for i, cnt in all_ids.items():
    if cnt > 1:
        dupe_count += 1
        print(f"  {i} x{cnt} in {sorted(set(id_to_file[i]))}")
print(f"total duplicate ids: {dupe_count}")

print("\n=== Dangling dependencies (pointing to id not defined anywhere) ===")
dangling = 0
for path, dep in all_deps:
    if dep not in all_ids:
        dangling += 1
        print(f"  {path}: depends on missing id {dep}")
print(f"total dangling deps: {dangling}")

print("\n=== Brace/bracket balance per file ===")
for path, content in file_contents.items():
    ob, cb = content.count("{"), content.count("}")
    osq, csq = content.count("["), content.count("]")
    if ob != cb or osq != csq:
        print(f"  UNBALANCED {path}: {{={ob}/{cb}  [={osq}/{csq}")
print("done")
