"""Ensure LuckPerms uses Apex MariaDB (placeholders filled at SFTP deploy)."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
lp_conf_file = ROOT / "server" / "plugins" / "LuckPerms" / "config.yml"
lp_conf_file.parent.mkdir(parents=True, exist_ok=True)

content = """server: global
storage-method: MySQL
data:
  address: __AQUATECH_MYSQL_HOST__:__AQUATECH_MYSQL_PORT__
  database: __AQUATECH_MYSQL_DATABASE__
  username: __AQUATECH_MYSQL_USER__
  password: '__AQUATECH_MYSQL_PASSWORD__'
  pool-size: 10
split-storage:
  enabled: false
"""
lp_conf_file.write_text(content, encoding="utf-8")
print("[SUCCESS] LuckPerms MySQL placeholders written (secrets: .apex_mysql.json)")
