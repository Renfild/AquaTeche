import os
import sys
import json
import urllib.request
import urllib.parse
import ssl

ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE

SERVER_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "server"))
PLUGINS_DIR = os.path.join(SERVER_DIR, "plugins")
MODS_DIR = os.path.join(SERVER_DIR, "mods")

os.makedirs(PLUGINS_DIR, exist_ok=True)
os.makedirs(MODS_DIR, exist_ok=True)

def download_file(url, output_path, name):
    print(f"[DOWNLOADING] {name} from {url}...")
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    }
    req = urllib.request.Request(url, headers=headers)
    try:
        with urllib.request.urlopen(req, context=ctx, timeout=30) as resp, open(output_path, "wb") as out:
            data = resp.read()
            out.write(data)
            print(f"[SUCCESS] Saved {name} ({len(data)} bytes) to {output_path}")
            return True
    except Exception as e:
        print(f"[ERROR] Failed to download {name}: {e}")
        return False

def get_mohist():
    mohist_jar = os.path.join(SERVER_DIR, "mohist-1.20.1.jar")
    api_url = "https://mohistmc.com/api/v2/projects/mohist/1.20.1/builds"
    try:
        req = urllib.request.Request(api_url, headers={"User-Agent": "Mozilla/5.0"})
        with urllib.request.urlopen(req, context=ctx, timeout=15) as resp:
            data = json.loads(resp.read().decode('utf-8'))
            builds = data.get("builds", [])
            if builds:
                latest_build = builds[-1]
                build_number = latest_build.get("number")
                download_url = f"https://mohistmc.com/api/v2/projects/mohist/1.20.1/builds/{build_number}/download"
                if download_file(download_url, mohist_jar, f"Mohist 1.20.1 (Build #{build_number})"):
                    return True
    except Exception as e:
        print(f"[WARN] Mohist API v2 failed: {e}. Trying direct GitHub release fallback...")

    github_url = "https://github.com/MohistMC/Mohist/releases/download/1.20.1/mohist-1.20.1-server.jar"
    return download_file(github_url, mohist_jar, "Mohist 1.20.1 (GitHub Release)")

if __name__ == "__main__":
    print("=== Starting Mohist 1.20.1 Downloader ===")
    res = get_mohist()
    print("Mohist download result:", res)
