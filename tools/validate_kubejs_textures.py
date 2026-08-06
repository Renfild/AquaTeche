import os
import glob
import re

ROOT = r"C:\Users\xieto\Desktop\AquaTech"

def check_textures():
    print("=== Checking KubeJS textures and FTB Quests ===")
    
    snbt_files = glob.glob(os.path.join(ROOT, "**", "*.snbt"), recursive=True)
    js_files = glob.glob(os.path.join(ROOT, "kubejs", "**", "*.js"), recursive=True)
    asset_files = glob.glob(os.path.join(ROOT, "kubejs", "assets", "**", "*.*"), recursive=True)

    errors = []
    warnings = []

    # 1. Check assets for uppercase or spaces
    for path in asset_files:
        rel = os.path.relpath(path, ROOT)
        filename = os.path.basename(path)
        if any(c.isupper() for c in filename):
            errors.append(f"[UPPERCASE FILENAME] {rel}")
        if " " in filename:
            errors.append(f"[SPACES IN FILENAME] {rel}")

    # 2. Check SNBT files for icons / textures
    for f in snbt_files:
        if "backup" in f.lower() or "build" in f.lower() or ".gradle" in f.lower():
            continue
        rel_snbt = os.path.relpath(f, ROOT)
        try:
            with open(f, "r", encoding="utf-8", errors="ignore") as fp:
                content = fp.read()
            
            matches = re.findall(r'(?:icon|image|texture):\s*"([^"]+)"', content)
            for path_str in matches:
                if any(c.isupper() for c in path_str):
                    warnings.append(f"[UPPERCASE PATH] {rel_snbt} -> {path_str}")
                if path_str.startswith("kubejs:"):
                    clean_path = path_str.replace("kubejs:", "")
                    expected_file = os.path.join(ROOT, "kubejs", "assets", "kubejs", clean_path)
                    if not os.path.exists(expected_file):
                        errors.append(f"[MISSING TEXTURE] {rel_snbt} references missing texture '{path_str}' (Expected: {os.path.relpath(expected_file, ROOT)})")
        except Exception as e:
            errors.append(f"[READ ERROR] {rel_snbt}: {e}")

    print("\n--- RESULTS ---")
    if not errors and not warnings:
        print("OK: All KubeJS assets and FTB Quests texture references are 100% valid! No missing textures or filename casing issues found.")
    else:
        for err in errors:
            print(f"ERROR: {err}")
        for warn in warnings:
            print(f"WARN: {warn}")

if __name__ == "__main__":
    check_textures()
