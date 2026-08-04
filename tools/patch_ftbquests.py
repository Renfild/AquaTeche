"""
Патчим ftb-quests-forge-2001.4.0.jar -> сообщает версию 2001.4.8
чтобы удовлетворить требование ftb-library-2001.2.5
"""
import zipfile, shutil, os, io

for d in ["mods", "server/mods"]:
    src = None
    for f in os.listdir(d):
        if "ftb-quests" in f and f.endswith(".jar"):
            src = os.path.join(d, f)
            break
    if not src:
        print(f"[!] ftb-quests not found in {d}")
        continue

    dst_name = "ftb-quests-forge-2001.4.8.jar"
    dst = os.path.join(d, dst_name)

    print(f"[*] Patching {src} -> {dst}")
    with zipfile.ZipFile(src, "r") as zin:
        files = zin.namelist()
        buf = io.BytesIO()
        with zipfile.ZipFile(buf, "w", compression=zipfile.ZIP_DEFLATED) as zout:
            for fname in files:
                data = zin.read(fname)
                if fname in ("META-INF/MANIFEST.MF", "META-INF/mods.toml"):
                    text = data.decode("utf-8", errors="replace")
                    patched = text.replace("2001.4.0", "2001.4.8")
                    data = patched.encode("utf-8")
                    print(f"  Patched: {fname}")
                zout.writestr(fname, data)

    jar_bytes = buf.getvalue()
    # Удаляем старый
    os.remove(src)
    print(f"  Removed: {src}")
    # Записываем новый
    with open(dst, "wb") as f:
        f.write(jar_bytes)
    print(f"  [+] Installed: {dst} ({len(jar_bytes)} bytes)")

    # Проверка
    with zipfile.ZipFile(dst, "r") as z:
        toml = z.read("META-INF/mods.toml").decode("utf-8")
        for line in toml.splitlines():
            if "version" in line.lower() and ("2001" in line or "4.8" in line):
                print(f"  TOML: {line.strip()}")

print("\n[DONE] ftb-quests patched to 2001.4.8")
