import os
import zipfile
import shutil

AQUATECH_UI_TOML = """modLoader="javafml"
loaderVersion="[47,)"
license="All Rights Reserved"

[[mods]]
modId="aquatech_ui"
version="1.0.24"
displayName="AquaTech Ocean UI"
authors="AquaTech"
description='''Ocean-themed LoliLand-style UI: custom Tab, HUD, nametags and chat bubbles for AquaTech.'''

[[dependencies.aquatech_ui]]
    modId="forge"
    mandatory=true
    versionRange="[47,)"
    ordering="NONE"
    side="BOTH"

[[dependencies.aquatech_ui]]
    modId="minecraft"
    mandatory=true
    versionRange="[1.20.1,1.21)"
    ordering="NONE"
    side="BOTH"

[[dependencies.aquatech_ui]]
    modId="luckperms"
    mandatory=false
    versionRange="[5.0,)"
    ordering="AFTER"
    side="BOTH"

[[dependencies.aquatech_ui]]
    modId="mcef"
    mandatory=false
    versionRange="[2.1,)"
    ordering="AFTER"
    side="CLIENT"
"""

AQUALUMEN_TOML = """modLoader="javafml"
loaderVersion="[47,)"
license="MIT"
issueTrackerURL="https://aquateche.store/support"

[[mods]]
modId="aqualumen"
version="0.3.6-alpha"
displayName="AquaLumen UI"
displayURL="https://aquateche.store"
logoFile="logo.png"
authors="xietorui / aquaTeche.store"
displayTest="IGNORE_ALL_VERSION"
description='''Luminous-style hub interface (profile, store, cases, battle pass, tops) for Forge 1.20.1 and Mohist servers.'''

[[dependencies.aqualumen]]
    modId="forge"
    mandatory=true
    versionRange="[47,)"
    ordering="NONE"
    side="BOTH"

[[dependencies.aqualumen]]
    modId="minecraft"
    mandatory=true
    versionRange="[1.20.1,1.21)"
    ordering="NONE"
    side="BOTH"

[[dependencies.aqualumen]]
    modId="mcef"
    mandatory=false
    versionRange="[2.1,)"
    ordering="AFTER"
    side="CLIENT"
"""

def update_jar_entry(jar_path, entry_name, content_str):
    if not os.path.isfile(jar_path):
        return False
    
    tmp_path = jar_path + ".tmp"
    with zipfile.ZipFile(jar_path, 'r') as zin:
        with zipfile.ZipFile(tmp_path, 'w', compression=zipfile.ZIP_DEFLATED) as zout:
            for item in zin.infolist():
                if item.filename != entry_name:
                    zout.writestr(item, zin.read(item.filename))
            zout.writestr(entry_name, content_str.encode('utf-8'))
            
    try:
        os.replace(tmp_path, jar_path)
    except PermissionError:
        print(f"Skipped locked file {jar_path}")
        if os.path.exists(tmp_path):
            os.remove(tmp_path)
    print(f"Updated {entry_name} in {jar_path}")
    return True

target_locations = [
    r"mods\aquatech-ui\build\libs\aquatech_ui-1.0.24.jar",
    r"mods\aqualumen-ui\build\libs\aqualumen-forge-1.20.1-0.3.6-alpha.jar",
    r"server\mods\aquatech_ui-1.0.24.jar",
    r"server\mods\aqualumen-forge-1.20.1-0.3.6-alpha.jar",
    r"dist\AquaTech-Client\mods\aquatech_ui-1.0.24.jar",
    r"dist\AquaTech-Client\mods\aqualumen-forge-1.20.1-0.3.6-alpha.jar",
    os.path.expandvars(r"%APPDATA%\AquaTech\mods\aquatech_ui-1.0.24.jar"),
    os.path.expandvars(r"%APPDATA%\AquaTech\mods\aqualumen-forge-1.20.1-0.3.6-alpha.jar"),
]

for loc in target_locations:
    if "aquatech_ui" in loc:
        update_jar_entry(loc, "META-INF/mods.toml", AQUATECH_UI_TOML)
    elif "aqualumen" in loc:
        update_jar_entry(loc, "META-INF/mods.toml", AQUALUMEN_TOML)

print("Done updating jar files.")
