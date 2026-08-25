import os
import glob
import subprocess
import zipfile
import shutil

javac = r"C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot\bin\javac.exe"
if not os.path.exists(javac):
    javac = r"C:\Program Files\Eclipse Adoptium\jdk-25.0.1.8-hotspot\bin\javac.exe"

print("Using javac:", javac)

cp_jars = []

# 1. Official mapped Minecraft & Forge Recompiled Jar (Complete 19.2MB official API)
forge_recomp = os.path.expanduser(r"~/.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.20.1-47.4.0_mapped_official_1.20.1/forge-1.20.1-47.4.0_mapped_official_1.20.1-recomp.jar")
if not os.path.exists(forge_recomp):
    forge_recomp = os.path.expanduser(r"~/.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.20.1-47.3.0_mapped_official_1.20.1/forge-1.20.1-47.3.0_mapped_official_1.20.1-recomp.jar")

cp_jars.append(os.path.abspath(forge_recomp))

# 2. Maven downloader (libraries: lwjgl, netty, gson, fastutil, guava, etc.)
maven_down = os.path.expanduser(r"~/.gradle/caches/forge_gradle/maven_downloader")
if os.path.exists(maven_down):
    for root, dirs, files in os.walk(maven_down):
        for f in files:
            if f.endswith(".jar") and not "-natives-" in f and not "-sources.jar" in f:
                full = os.path.join(root, f)
                if zipfile.is_zipfile(full):
                    cp_jars.append(os.path.abspath(full))

# 3. MCEF and mod jars
for j in glob.glob("mods/aquatech-ui/libs/*.jar") + glob.glob("mods/aqualumen-ui/libs/*.jar"):
    cp_jars.append(os.path.abspath(j))

# Add server libraries
for j in glob.glob("server/libraries/**/*.jar", recursive=True):
    if j.endswith(".jar") and zipfile.is_zipfile(j):
        cp_jars.append(os.path.abspath(j))

# Deduplicate while preserving order
seen = set()
deduped_cp = []
for p in cp_jars:
    norm = os.path.normpath(p).lower()
    if norm not in seen:
        seen.add(norm)
        deduped_cp.append(os.path.abspath(p).replace("\\", "/"))

cp_str = ";".join(deduped_cp)
print("Total clean CP jars:", len(deduped_cp))

def compile_and_package(mod_dir, mod_jar_name):
    print(f"\n=== Compiling {mod_dir} ===")
    out_classes = os.path.abspath(os.path.join(mod_dir, "build/classes/java/main")).replace("\\", "/")
    os.makedirs(out_classes, exist_ok=True)
    
    src_files = [os.path.abspath(f).replace("\\", "/") for f in glob.glob(f"{mod_dir}/src/main/java/**/*.java", recursive=True)]
    
    argfile = os.path.abspath(os.path.join(mod_dir, "javac_args.txt")).replace("\\", "/")
    with open(argfile, "w", encoding="utf-8") as f:
        f.write("-encoding\nutf-8\n")
        f.write("--release\n17\n")
        f.write("-cp\n")
        f.write(f'"{cp_str}"\n')
        f.write("-d\n")
        f.write(f'"{out_classes}"\n')
        for sf in src_files:
            f.write(f'"{sf}"\n')
            
    res = subprocess.run([javac, f"@{argfile}"], capture_output=True, text=True)
    if res.returncode != 0:
        print("Compilation FAILED:")
        print(res.stderr)
        return False
    print(f"Compilation SUCCEEDED! ({len(src_files)} files)")

    # Build jar package
    target_jar = os.path.abspath(os.path.join(mod_dir, "build/libs", mod_jar_name))
    os.makedirs(os.path.dirname(target_jar), exist_ok=True)
    
    # We create jar containing compiled classes + resources
    res_dir = os.path.abspath(os.path.join(mod_dir, "src/main/resources"))
    
    with zipfile.ZipFile(target_jar, "w", compression=zipfile.ZIP_DEFLATED) as z:
        # Add classes
        for root, dirs, files in os.walk(out_classes):
            for file in files:
                full_path = os.path.join(root, file)
                rel_path = os.path.relpath(full_path, out_classes).replace("\\", "/")
                z.write(full_path, rel_path)
                
        # Add resources
        if os.path.exists(res_dir):
            for root, dirs, files in os.walk(res_dir):
                for file in files:
                    full_path = os.path.join(root, file)
                    rel_path = os.path.relpath(full_path, res_dir).replace("\\", "/")
                    z.write(full_path, rel_path)

    print(f"Created jar: {target_jar} ({os.path.getsize(target_jar)} bytes)")
    
    # Copy to destination paths
    for dest_dir in ["mods", "server/mods", "dist/AquaTech-Client/mods"]:
        if os.path.exists(dest_dir):
            prefix = mod_jar_name.split("-")[0].replace("_", "-")
            for old in glob.glob(f"{dest_dir}/*.jar"):
                b = os.path.basename(old).replace("_", "-")
                if b.startswith(prefix):
                    try: os.remove(old)
                    except: pass
            dest_path = os.path.join(dest_dir, mod_jar_name)
            shutil.copy2(target_jar, dest_path)
            print(f"Deployed to: {dest_path}")
            
    return True

ok1 = compile_and_package("mods/aqualumen-ui", "aqualumen-forge-1.20.1-0.3.6-alpha.jar")
ok2 = compile_and_package("mods/aquatech-ui", "aquatech_ui-1.0.24.jar")

if ok1 and ok2:
    print("\n[SUCCESS] Both mods compiled and packaged successfully!")
else:
    print("\n[ERROR] Mod build failed.")
