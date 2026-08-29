from pathlib import Path

root = Path("docs")
for p in root.rglob("*.html"):
    t = p.read_text(encoding="utf-8")
    n = t.replace("site.js?v=20260826p", "site.js?v=20260826s").replace(
        "site.js?v=20260826r", "site.js?v=20260826s"
    )
    if n != t:
        p.write_text(n, encoding="utf-8")
        print(p)
