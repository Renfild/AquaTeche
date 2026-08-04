import pathlib
import re

root = pathlib.Path(__file__).parent
base = pathlib.Path(r"C:\Users\xieto\AppData\Local\Temp\ftbq")

needles = [b"chapter_groups", b"chapter_group"]
for f in base.rglob("*.class"):
    b = f.read_bytes()
    if any(n in b for n in needles):
        strs = [s.decode() for s in re.findall(rb"[ -~]{4,}", b)]
        hits = [s for s in strs if "chapter_group" in s or "group" == s or s.endswith(".snbt")]
        if hits:
            print(f.relative_to(base), hits[:25])
