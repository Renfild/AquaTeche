import urllib.request, json, ssl

with open('.apex_deploy.json', 'r') as f:
    cfg = json.load(f)

url = 'https://panel.apexnodes.xyz/api/client/servers/6fdc6f7b'
headers = {
    'Authorization': f'Bearer {cfg["apex_api_key"]}',
    'Accept': 'application/json',
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'
}
req = urllib.request.Request(url, headers=headers)
try:
    with urllib.request.urlopen(req, timeout=10) as r:
        print('Status code:', r.status)
        print('Headers:', r.headers)
        raw = r.read().decode('utf-8', 'replace')
        print('Body (first 1000):', repr(raw[:1000]))
except urllib.error.HTTPError as e:
    print('HTTPError:', e.code)
    print(e.read().decode('utf-8', 'replace')[:1000])
except Exception as e:
    print('Error:', e)
