import os
import yaml

groups_dir = "server/plugins/LuckPerms/yaml-storage/groups"
os.makedirs(groups_dir, exist_ok=True)

# 1. Default group (Player)
default_group = {
    "name": "default",
    "permissions": [
        {"permission": "essentials.spawn", "value": True},
        {"permission": "essentials.kit", "value": True},
        {"permission": "essentials.kit.starter", "value": True},
        {"permission": "essentials.home", "value": True},
        {"permission": "essentials.sethome", "value": True},
        {"permission": "essentials.delhome", "value": True},
        {"permission": "essentials.tpa", "value": True},
        {"permission": "essentials.tpaccept", "value": True},
        {"permission": "essentials.tpdeny", "value": True},
        {"permission": "essentials.pay", "value": True},
        {"permission": "essentials.balance", "value": True},
        {"permission": "essentials.msg", "value": True},
        {"permission": "essentials.r", "value": True},
        {"permission": "ftbquests.open", "value": True}
    ],
    "prefixes": [
        {"prefix": "&a[Игрок] ", "priority": 0}
    ]
}

# 2. VIP group
vip_group = {
    "name": "vip",
    "parents": ["default"],
    "permissions": [
        {"permission": "essentials.fly", "value": True},
        {"permission": "essentials.hat", "value": True},
        {"permission": "essentials.sethome.multiple.vip", "value": True}
    ],
    "prefixes": [
        {"prefix": "&e&l[VIP] ", "priority": 10}
    ]
}

# 3. Mod group
mod_group = {
    "name": "mod",
    "parents": ["vip"],
    "permissions": [
        {"permission": "essentials.kick", "value": True},
        {"permission": "essentials.mute", "value": True},
        {"permission": "essentials.ban", "value": True},
        {"permission": "essentials.tp", "value": True}
    ],
    "prefixes": [
        {"prefix": "&9&l[Модер] ", "priority": 20}
    ]
}

# 4. Developer group
dev_group = {
    "name": "developer",
    "parents": ["mod"],
    "permissions": [
        {"permission": "luckperms.*", "value": True},
        {"permission": "essentials.*", "value": True}
    ],
    "prefixes": [
        {"prefix": "&b&l[Разраб] ", "priority": 30}
    ]
}

# 5. Admin group
admin_group = {
    "name": "admin",
    "parents": ["developer"],
    "permissions": [
        {"permission": "worldguard.*", "value": True},
        {"permission": "worldedit.*", "value": True}
    ],
    "prefixes": [
        {"prefix": "&4&l[Админ] ", "priority": 40}
    ]
}

# 6. Owner group
owner_group = {
    "name": "owner",
    "parents": ["admin"],
    "permissions": [
        {"permission": "*", "value": True}
    ],
    "prefixes": [
        {"prefix": "&c&l[Владелец] ", "priority": 50}
    ]
}

groups = {
    "default.yml": default_group,
    "vip.yml": vip_group,
    "mod.yml": mod_group,
    "developer.yml": dev_group,
    "admin.yml": admin_group,
    "owner.yml": owner_group
}

for fname, data in groups.items():
    p = os.path.join(groups_dir, fname)
    with open(p, "w", encoding="utf-8") as f:
        yaml.dump(data, f, default_flow_style=False)

print("[SUCCESS] Configured minimalist, secure LuckPerms group permissions!")
