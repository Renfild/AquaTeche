import os, json, zipfile

server_mods_dir = 'c:/Users/xieto/Desktop/AquaTech/server/mods'
client_mods_dir = 'c:/Users/xieto/Desktop/AquaTech/mods'

server_mods = sorted(os.listdir(server_mods_dir))
client_mods = sorted(os.listdir(client_mods_dir))

print(f"Server mods count: {len(server_mods)}")
print(f"Client mods count: {len(client_mods)}")

# List of known client-only mods that CRASH a dedicated server if placed in server/mods:
client_only_indicators = [
    'oculus', 'embeddium', 'rubidium', 'optifine', 'dynamic-fps', 'dynamicfps',
    'entityculling', 'mcef', 'betterquestpopup', 'sounddevice', 'controlling',
    'drippy', 'fancymenu', 'modernui', 'itemphysic', 'customskinloader', 'skinport'
]

print("\n--- Checking for Client-Only Mods in server/mods/ ---")
found_client_only = []
for m in server_mods:
    m_lower = m.lower()
    for ind in client_only_indicators:
        if ind in m_lower and not m.endswith('.disabled') and not m.endswith('.bak'):
            found_client_only.append((m, ind))

if found_client_only:
    print("WARNING! Found potential client-only mods on server:")
    for m, ind in found_client_only:
        print(f"  CRITICAL: {m} (matches '{ind}')")
else:
    print("No client-only mods found in server/mods.")

# Check for duplicate mods in server/mods
print("\n--- Checking for Duplicate Mod Jars in server/mods/ ---")
prefix_map = {}
for m in server_mods:
    if m.endswith('.jar'):
        base = m.split('-')[0].lower()
        prefix_map.setdefault(base, []).append(m)

for base, jars in prefix_map.items():
    if len(jars) > 1:
        print(f"  Duplicate base '{base}': {jars}")
