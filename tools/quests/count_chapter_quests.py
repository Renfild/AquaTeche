from pathlib import Path
import re
text = Path(__file__).resolve().parent.joinpath("generate_600_ocean_quests.py").read_text(encoding="utf-8")
parts = text.split("CHAPTERS.append((")[1:]
for i, p in enumerate(parts, 1):
    name = re.search(r'"(\d+_[^"]+)"', p).group(1)
    body = p.split("\n))")[0]
    n = len(re.findall(r"\n        q\(", body))
    print(i, name, n, "OK" if n == 50 else "BAD")
