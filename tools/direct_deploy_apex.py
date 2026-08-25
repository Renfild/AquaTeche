import json
import time
import urllib.request
import paramiko
from pathlib import Path

secrets = json.loads(Path('.apex_deploy.json').read_text(encoding='utf-8'))
host = secrets['sftp_host']
port = int(secrets['sftp_port'])
user = secrets['sftp_user']
password = secrets['sftp_pass']
server_id = secrets['apex_server_id']
api_key = secrets['apex_api_key']
panel = secrets.get('apex_panel', 'https://panel.apexnodes.xyz').rstrip('/')

print("1. Connecting SFTP to upload new reobfuscated jars and configs...")
t = paramiko.Transport((host, port))
t.connect(username=user, password=password)
sftp = paramiko.SFTPClient.from_transport(t)

# Remove older jars if present
for old_jar in ["mods/aqualumen-forge-1.20.1-0.3.6-alpha.jar"]:
    try:
        sftp.remove(old_jar)
        print(f"  Removed {old_jar}")
    except Exception:
        pass

sftp.put(r"server\mods\aquatech_ui-1.0.24.jar", "mods/aquatech_ui-1.0.24.jar")
print("  Uploaded aquatech_ui-1.0.24.jar")
sftp.put(r"server\mods\aqualumen-forge-1.20.1-0.3.7-alpha.jar", "mods/aqualumen-forge-1.20.1-0.3.7-alpha.jar")
print("  Uploaded aqualumen-forge-1.20.1-0.3.7-alpha.jar")
sftp.put(r"server\config\aqualumen\cases.json", "config/aqualumen/cases.json")
print("  Uploaded config/aqualumen/cases.json")
sftp.put(r"server\kubejs\server_scripts\30_aquatech_crafting.js", "kubejs/server_scripts/30_aquatech_crafting.js")
print("  Uploaded kubejs/server_scripts/30_aquatech_crafting.js")

for lp_file in Path("server/plugins/LuckPerms/yaml-storage/groups").glob("*.yml"):
    remote_lp = f"plugins/LuckPerms/yaml-storage/groups/{lp_file.name}"
    try:
        sftp.put(str(lp_file), remote_lp)
    except Exception as ex:
        print(f"  LP upload error for {lp_file.name}: {ex}")
print("  Uploaded all LuckPerms group configs")

sftp.close()
t.close()


def apex_api(method, path, data=None):
    url = f"{panel}{path}"
    body = json.dumps(data).encode('utf-8') if data else None
    req = urllib.request.Request(
        url,
        data=body,
        headers={
            "Authorization": f"Bearer {api_key}",
            "Accept": "Application/vnd.pterodactyl.v1+json",
            "Content-Type": "application/json"
        },
        method=method
    )
    with urllib.request.urlopen(req, timeout=30) as resp:
        res = resp.read()
        return json.loads(res.decode('utf-8')) if res else {}

print("2. Sending kill + start signal to Apex...")
try:
    apex_api("POST", f"/api/client/servers/{server_id}/power", {"signal": "kill"})
except Exception as e:
    print(f"Kill error (ignoring): {e}")

time.sleep(3)

try:
    apex_api("POST", f"/api/client/servers/{server_id}/power", {"signal": "start"})
    print("  Start signal sent!")
except Exception as e:
    print(f"Start error: {e}")

print("3. Waiting for server to start up...")
start_time = time.time()
while time.time() - start_time < 180:
    time.sleep(5)
    res = apex_api("GET", f"/api/client/servers/{server_id}/resources")
    state = res.get("attributes", {}).get("current_state", "unknown")
    print(f"  Status: {state} ({int(time.time() - start_time)}s)")
    if state == "running":
        print("SUCCESS: Apex server is running!")
        break
