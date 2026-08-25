import urllib.request, urllib.error, json

with open('.apex_deploy.json', 'r') as f:
    cfg = json.load(f)

panel_url = cfg['apex_panel'].rstrip('/')
server_id = cfg['apex_server_id']
api_key = cfg['apex_api_key']

headers = {
    "Authorization": f"Bearer {api_key}",
    "Accept": "application/json",
    "Content-Type": "application/json",
    "User-Agent": "AquaTechDeploy/1.0"
}

def make_req(endpoint, method="GET", body=None):
    url = f"{panel_url}{endpoint}"
    print(f"Request: {method} {url}")
    data = json.dumps(body).encode('utf-8') if body else None
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=15) as res:
            raw = res.read().decode('utf-8')
            return json.loads(raw) if raw else {}
    except urllib.error.HTTPError as e:
        err_body = e.read().decode('utf-8', 'replace')
        print(f"HTTPError {e.code}: {err_body}")
        return None
    except Exception as e:
        print(f"Error: {e}")
        return None

# 1. Get server details
server_info = make_req(f"/api/client/servers/{server_id}")
print("\nServer Info:")
if server_info:
    attr = server_info.get('attributes', {})
    print(f"Name: {attr.get('name')}, Identifier: {attr.get('identifier')}, Node: {attr.get('node')}")
    print(f"SFTP Details: {attr.get('sftp_details')}")
    print(f"Limits: {attr.get('limits')}")

# 2. Get server resources / status
res_info = make_req(f"/api/client/servers/{server_id}/resources")
print("\nResource Status:")
if res_info:
    attr = res_info.get('attributes', {})
    print(f"Current State: {attr.get('current_state')}")
    print(f"Resources: {attr.get('resources')}")

# 3. List files in crash-reports or logs
crash_files = make_req(f"/api/client/servers/{server_id}/files/list?directory=crash-reports")
if crash_files and 'data' in crash_files:
    print("\nCrash Reports on Panel:")
    for item in crash_files['data']:
        print(" ", item['attributes']['name'], item['attributes']['size'], item['attributes']['modified_at'])

# 4. Get contents of latest.log
latest_log = make_req(f"/api/client/servers/{server_id}/files/contents?file=logs%2Flatest.log")
if latest_log:
    print("\nLatest.log contents (first 2000 chars):")
    print(str(latest_log)[:2000])

# 5. List logs directory
logs_list = make_req(f"/api/client/servers/{server_id}/files/list?directory=logs")
if logs_list and 'data' in logs_list:
    print("\nLogs directory files:")
    for item in logs_list['data']:
        print(" ", item['attributes']['name'], item['attributes']['size'], item['attributes']['modified_at'])
