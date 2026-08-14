import os
import yaml

lp_groups_dir = "server/plugins/LuckPerms/yaml-storage/groups"
os.makedirs(lp_groups_dir, exist_ok=True)

groups_data = {
    "owner": {
        "name": "owner",
        "permissions": [{"permission": "*", "value": True}],
        "parents": ["admin"],
        "prefixes": [{"prefix": "&c&l[Владелец] ", "priority": 100}]
    },
    "admin": {
        "name": "admin",
        "permissions": [
            {"permission": "luckperms.*", "value": True},
            {"permission": "essentials.*", "value": True},
            {"permission": "worldguard.*", "value": True},
            {"permission": "worldedit.*", "value": True},
            {"permission": "chunky.*", "value": True}
        ],
        "parents": ["developer"],
        "prefixes": [{"prefix": "&4&l[Админ] ", "priority": 90}]
    },
    "developer": {
        "name": "developer",
        "permissions": [
            {"permission": "luckperms.*", "value": True},
            {"permission": "essentials.*", "value": True}
        ],
        "parents": ["mod"],
        "prefixes": [{"prefix": "&b&l[Разраб] ", "priority": 80}]
    },
    "mod": {
        "name": "mod",
        "permissions": [
            {"permission": "essentials.kick", "value": True},
            {"permission": "essentials.mute", "value": True},
            {"permission": "essentials.ban", "value": True},
            {"permission": "essentials.teleport", "value": True}
        ],
        "parents": ["vip"],
        "prefixes": [{"prefix": "&9&l[Модер] ", "priority": 70}]
    },
    "vip": {
        "name": "vip",
        "permissions": [
            {"permission": "essentials.fly", "value": True},
            {"permission": "essentials.hat", "value": True},
            {"permission": "essentials.sethome.multiple.vip", "value": True}
        ],
        "parents": ["default"],
        "prefixes": [{"prefix": "&e&l[VIP] ", "priority": 50}]
    },
    "default": {
        "name": "default",
        "permissions": [
            {"permission": "essentials.build", "value": True},
            {"permission": "essentials.home", "value": True},
            {"permission": "essentials.sethome", "value": True},
            {"permission": "essentials.tpa", "value": True},
            {"permission": "essentials.spawn", "value": True},
            {"permission": "essentials.warp", "value": True},
            {"permission": "essentials.pay", "value": True},
            {"permission": "essentials.balance", "value": True},
            {"permission": "essentials.kit", "value": True},
            {"permission": "essentials.kits.starter", "value": True}
        ],
        "prefixes": [{"prefix": "&a[Игрок] ", "priority": 10}]
    }
}

for g_name, g_content in groups_data.items():
    g_file = os.path.join(lp_groups_dir, f"{g_name}.yml")
    with open(g_file, "w", encoding="utf-8") as f:
        yaml.dump(g_content, f, default_flow_style=False)

print("[SUCCESS] LuckPerms groups created cleanly!")
