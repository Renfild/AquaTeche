import paramiko
import json
import os
from pathlib import Path

ROOT = Path('.').resolve()
secrets = json.loads((ROOT / '.apex_deploy.json').read_text(encoding='utf-8'))

host = secrets.get('sftp_host', 'g-pl-3.apexnodes.xyz')
port = int(secrets.get('sftp_port', 2022))
user = secrets.get('sftp_user')
password = secrets.get('sftp_pass')

print(f"Connecting to Apex SFTP {host}:{port}...")
transport = paramiko.Transport((host, port))
transport.connect(username=user, password=password)
sftp = paramiko.SFTPClient.from_transport(transport)

apex_mods = []
for entry in sftp.listdir_attr('mods'):
    if entry.filename.endswith('.jar'):
        apex_mods.append((entry.filename, entry.st_size))

apex_mods.sort()
print(f"\n=== МОДЫ НА ХОСТИНГЕ APEX ({len(apex_mods)} шт) ===")
for name, size in apex_mods:
    print(f"  • {name} ({size} байт)")

with open('docs/pack/manifest.json', 'r', encoding='utf-8') as f:
    man = json.load(f)

pack_mods = []
for item in man.get('files', []):
    if item['path'].startswith('mods/') and item['path'].endswith('.jar'):
        pack_mods.append((item['path'].replace('mods/', ''), item['size'], item.get('url', '')))

pack_mods.sort()
print(f"\n=== МОДЫ В МАНИФЕСТЕ ЛАУНЧЕРА ДЛЯ СКАЧИВАНИЯ ({len(pack_mods)} шт) ===")
for name, size, url in pack_mods:
    print(f"  • {name} ({size} байт)\n    URL: {url}")

apex_names = set(m[0] for m in apex_mods)
pack_names = set(m[0] for m in pack_mods)

missing_in_pack = apex_names - pack_names
missing_in_apex = pack_names - apex_names

print("\n=== СРАВНЕНИЕ ===")
print("На хостинге ЕСТЬ, а лаунчер НЕ скачивает:", list(missing_in_pack) or "НЕТ (все моды с хостинга присутствуют в лаунчере)")
print("Лаунчер скачивает, а на хостинге НЕТ (клиентские моды оптимизации):", list(missing_in_apex) or "НЕТ")

sftp.close()
transport.close()
