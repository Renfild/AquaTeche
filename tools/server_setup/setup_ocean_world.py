import os
import shutil

props_file = "server/server.properties"

# Read existing properties
props = {}
if os.path.exists(props_file):
    with open(props_file, "r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if line and not line.startswith("#") and "=" in line:
                k, v = line.split("=", 1)
                props[k.strip()] = v.strip()

# Update settings for 100% Ocean World
props["level-type"] = "minecraft:single_biome_surface"
props["generator-settings"] = '{"biome":"minecraft:deep_ocean"}'
props["allow-flight"] = "true"
props["max-tick-time"] = "60000"
props["motd"] = "\\u00A7b\\u00A7lAquaTech: Ocean Horizon \\u00A78| \\u00A77Ocean Survival"
props["spawn-protection"] = "0"
props["view-distance"] = "10"
props["simulation-distance"] = "8"

with open(props_file, "w", encoding="utf-8") as f:
    f.write("# Minecraft server properties (AquaTech Ocean World)\n")
    for k, v in props.items():
        f.write(f"{k}={v}\n")

print("[SUCCESS] Updated server.properties for 100% Ocean World!")

# Reset old land world
world_dir = "server/world"
if os.path.exists(world_dir):
    try:
        shutil.rmtree(world_dir)
        print("[SUCCESS] Removed old land world. A fresh 100% Deep Ocean World will be generated on next start!")
    except Exception as e:
        print("World removal notice:", e)
