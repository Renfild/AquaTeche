import urllib.request, json

mirrors = [
    "https://aquateche.store/pack",
    "https://raw.githubusercontent.com/Renfild/AquaTeche/main/docs/pack",
    "https://cdn.jsdelivr.net/gh/Renfild/AquaTeche@main/docs/pack",
]

for base in mirrors:
    url = base + "/manifest.json"
    try:
        with urllib.request.urlopen(url, timeout=8) as r:
            data = json.loads(r.read())
            version = data.get("version", "?")
            aq = next((f for f in data.get("files", []) if "aquatech_ui" in f.get("path", "")), None)
            if aq:
                md5 = aq.get("md5", "?")
                size = aq.get("size", "?")
                print(base, "ver=" + str(version), "md5=" + str(md5)[:12], "size=" + str(size))
            else:
                print(base, "ver=" + str(version), "aquatech_ui NOT in manifest")
    except Exception as e:
        print(base, "ERROR", e)
