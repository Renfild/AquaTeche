import re
from pathlib import Path

VERSION = "20260905_apple_v2"
root = Path("docs")
count = 0
for p in root.rglob("*.html"):
    t = p.read_text(encoding="utf-8")
    n = re.sub(r"site\.js\?v=[a-zA-Z0-9_.-]+", f"site.js?v={VERSION}", t)
    n = re.sub(r"site\.css\?v=[a-zA-Z0-9_.-]+", f"site.css?v={VERSION}", n)
    if n != t:
        p.write_text(n, encoding="utf-8")
        print(f"Updated: {p}")
        count += 1
print(f"Total updated: {count}")
