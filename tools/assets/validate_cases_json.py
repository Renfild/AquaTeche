import json
from pathlib import Path

base = Path(r"C:\Users\xieto\Desktop\AquaTech\config\casesmod\cases")
for p in sorted(base.glob("*.json")):
    d = json.loads(p.read_text(encoding="utf-8"))
    assert d["id"] == p.stem, (d["id"], p.stem)
    assert d["items"], p
    w = sum(i["weight"] for i in d["items"])
    print(f"{p.name:20} price={d['price']:5} items={len(d['items']):2} weight={w:6.1f} pity={d.get('pityThreshold')}")
print("OK")
