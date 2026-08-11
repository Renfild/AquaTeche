import os
import hashlib
import json

ROOT = r"C:\Users\xieto\Desktop\AquaTech"
CLIENT_DIR = os.path.join(ROOT, "dist", "AquaTech-Client")
OUTPUT_DIR = os.path.join(ROOT, "dist", "launcher")
os.makedirs(OUTPUT_DIR, exist_ok=True)
MANIFEST_PATH = os.path.join(OUTPUT_DIR, "manifest.json")

FOLDERS_TO_SYNC = ["mods", "config", "kubejs", "resourcepacks"]

def get_md5(filepath):
    hasher = hashlib.md5()
    with open(filepath, "rb") as f:
        for chunk in iter(lambda: f.read(65536), b""):
            hasher.update(chunk)
    return hasher.hexdigest()

def build_manifest():
    print(f"[AquaTech Launcher] Generating manifest from {CLIENT_DIR}...")
    manifest = {
        "version": "1.0.0",
        "mc_version": "1.20.1",
        "forge_version": "47.4.0",
        "server_ip": "g-pl-3.apexnodes.xyz",
        "server_port": 21561,
        "files": []
    }

    # If dist/AquaTech-Client doesn't exist, use ROOT/mods, ROOT/config, ROOT/kubejs
    source_base = CLIENT_DIR if os.path.exists(CLIENT_DIR) else ROOT

    for folder in FOLDERS_TO_SYNC:
        target_path = os.path.join(source_base, folder)
        if not os.path.exists(target_path):
            continue

        for root_dir, _, files in os.walk(target_path):
            for file in files:
                # Ignore temp / cache files
                if file.endswith(".tmp") or file.endswith(".log") or file.startswith("."):
                    continue

                full_path = os.path.join(root_dir, file)
                rel_path = os.path.relpath(full_path, source_base).replace("\\", "/")
                md5 = get_md5(full_path)
                size = os.path.getsize(full_path)

                manifest["files"].append({
                    "path": rel_path,
                    "md5": md5,
                    "size": size
                })

    with open(MANIFEST_PATH, "w", encoding="utf-8") as f:
        json.dump(manifest, f, indent=2, ensure_ascii=False)

    print(f"[AquaTech Launcher] Manifest created: {len(manifest['files'])} files logged into {MANIFEST_PATH}")
    return MANIFEST_PATH

if __name__ == "__main__":
    build_manifest()
