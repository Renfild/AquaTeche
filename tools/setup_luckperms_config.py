import os

lp_conf_file = "server/plugins/LuckPerms/config.yml"
os.makedirs("server/plugins/LuckPerms", exist_ok=True)

content = """server: global
storage-method: YAML
split-storage:
  enabled: false
"""
with open(lp_conf_file, "w", encoding="utf-8") as f:
    f.write(content)

print("[SUCCESS] LuckPerms set to YAML storage mode!")
