import os
import yaml

wg_region_dir = "server/plugins/WorldGuard/worlds/world"
os.makedirs(wg_region_dir, exist_ok=True)

regions_data = {
    "regions": {
        "__global__": {
            "flags": {
                "creeper-explosion": "deny",
                "other-explosion": "deny",
                "tnt": "deny",
                "ghast-fireball": "deny",
                "wither-damage": "deny",
                "mob-spawning": "allow"
            },
            "owners": {},
            "members": {},
            "priority": 0,
            "type": "global"
        }
    }
}

with open(os.path.join(wg_region_dir, "regions.yml"), "w", encoding="utf-8") as f:
    yaml.dump(regions_data, f, default_flow_style=False)

print("[SUCCESS] Configured WorldGuard global region: Explosions completely disabled!")
