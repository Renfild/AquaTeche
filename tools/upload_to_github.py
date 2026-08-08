"""
AquaTech GitHub Release Uploader
Загружает все моды и файлы сборки в GitHub Release и генерирует manifest.json
с прямыми URL для скачивания. Запускай после каждого обновления.

Требования:
  pip install requests
  GitHub token: Settings -> Developer Settings -> Personal Access Tokens
  Сохрани токен в файл .gh_token рядом со скриптом (или env GITHUB_TOKEN)
"""

import os, sys, json, hashlib, subprocess
from pathlib import Path

ROOT       = Path(r"C:\Users\xieto\Desktop\AquaTech")
CLIENT_DIR = ROOT / "dist" / "AquaTech-Client"
MANIFEST   = CLIENT_DIR / "manifest.json"

GITHUB_REPO    = "Renfild/AquaTeche"
RELEASE_TAG    = "v1.0.0"
RELEASE_NAME   = "AquaTech Modpack Files v1.0.0"
RELEASE_BODY   = "Automatic modpack update. Do not delete this release."

FOLDERS = ["mods", "config", "kubejs", "resourcepacks"]

# ─── Helpers ─────────────────────────────────────────────────────────────────
def md5_file(p: Path) -> str:
    h = hashlib.md5()
    with open(p, "rb") as f:
        for c in iter(lambda: f.read(65536), b""): h.update(c)
    return h.hexdigest()

def get_token() -> str:
    # 1. .gh_token file (highest priority — always use our explicit token)
    tf = ROOT / ".gh_token"
    if tf.exists():
        t = tf.read_text().strip()
        if t:
            return t
    # 2. Environment variable (fallback only)
    t = os.environ.get("GITHUB_TOKEN", "")
    if t:
        return t
    print("[ERROR] GitHub token not found!")
    print("  Create file: C:\\Users\\xieto\\Desktop\\AquaTech\\.gh_token")
    print("  Get token: https://github.com/settings/tokens/new  (scope: repo)")
    sys.exit(1)

# ─── Step 1: deploy runtime ───────────────────────────────────────────────────
def run_deploy():
    print("[1/4] Deploying runtime files...")
    r = subprocess.run(
        ["powershell", "-ExecutionPolicy", "Bypass", "-File", str(ROOT / "deploy_runtime.ps1")],
        cwd=str(ROOT), capture_output=True, text=True
    )
    ok_count = r.stdout.count("\nOK ")
    print(f"      {ok_count} files deployed")

# ─── Step 2: collect files ────────────────────────────────────────────────────
def collect_files() -> list[dict]:
    files = []
    for folder in FOLDERS:
        fp = CLIENT_DIR / folder
        if not fp.exists(): continue
        for f in fp.rglob("*"):
            if not f.is_file(): continue
            if f.suffix in (".tmp", ".log") or f.name.startswith("."): continue
            if "_parked" in str(f) or "aquatech-ui" in str(f): continue
            rel = f.relative_to(CLIENT_DIR).as_posix()
            files.append({
                "path": rel,
                "local": f,
                "md5": md5_file(f),
                "size": f.stat().st_size
            })
    return files

# ─── Step 3: upload to GitHub Release ────────────────────────────────────────
def upload_release(files: list[dict], token: str) -> dict[str, str]:
    """Upload all files to GitHub Release, return {rel_path: download_url}"""
    try:
        import requests
    except ImportError:
        print("[INFO] Installing 'requests'...")
        subprocess.run([sys.executable, "-m", "pip", "install", "requests", "-q"])
        import requests

    headers = {
        "Authorization": f"token {token}",
        "Accept": "application/vnd.github.v3+json"
    }
    api = f"https://api.github.com/repos/{GITHUB_REPO}"

    # Find or create draft release
    print(f"[3/4] Uploading to GitHub Release '{RELEASE_TAG}'...")
    
    # List all releases and find our tag
    r = requests.get(f"{api}/releases", headers=headers)
    r.raise_for_status()
    all_releases = r.json()
    existing = next((rel for rel in all_releases if rel["tag_name"] == RELEASE_TAG), None)

    if existing:
        release_id = existing["id"]
        existing_assets = {a["name"]: a["id"] for a in existing.get("assets", [])}
        print(f"      Found existing release id={release_id}, {len(existing_assets)} assets")
    else:
        # Create fresh draft release
        payload = {
            "tag_name": RELEASE_TAG,
            "target_commitish": "main",
            "name": RELEASE_NAME,
            "body": RELEASE_BODY,
            "draft": False,
            "prerelease": False
        }
        r = requests.post(f"{api}/releases", headers=headers, json=payload)
        r.raise_for_status()
        release = r.json()
        release_id = release["id"]
        existing_assets = {}
        print(f"      Created draft release id={release_id}")

    upload_url = f"https://uploads.github.com/repos/{GITHUB_REPO}/releases/{release_id}/assets"
    dl_base = f"https://github.com/{GITHUB_REPO}/releases/download/{RELEASE_TAG}"
    url_map = {}

    for i, item in enumerate(files):
        rel = item["path"]
        folder = rel.split("/")[0] if "/" in rel else ""
        if folder == "mods":
            aname = item["local"].name
        else:
            aname = rel.replace("/", "__")

        pct = int((i + 1) / len(files) * 100)
        url_map[rel] = f"{dl_base}/{aname}"

        # If asset exists, delete old one if needed or skip
        if aname in existing_assets:
            print(f"  [{pct:3d}%] SKIP  {aname}")
            continue

        print(f"  [{pct:3d}%] UPLOAD {aname}  ({round(item['size']/1024/1024, 2)} MB)")
        with open(item["local"], "rb") as fh:
            data = fh.read()
        up_headers = {**headers, "Content-Type": "application/octet-stream"}
        
        for attempt in range(3):
            try:
                r = requests.post(
                    f"{upload_url}?name={aname}",
                    headers=up_headers,
                    data=data,
                    timeout=300
                )
                if r.status_code in (200, 201):
                    break
                print(f"      [WARN] Upload attempt {attempt+1} failed {r.status_code}: {r.text[:150]}")
            except Exception as e:
                print(f"      [WARN] Upload attempt {attempt+1} exception: {e}")
                import time
                time.sleep(2)

    return url_map

# ─── Step 4: generate manifest.json ──────────────────────────────────────────
def build_manifest(files: list[dict], url_map: dict[str, str]):
    print("[4/4] Generating manifest.json...")
    manifest = {
        "version": "1.0.0",
        "mc_version": "1.20.1",
        "forge_version": "47.4.0",
        "server_ip": "katherine-hydro.tun.ply.gg",
        "server_port": 25565,
        "files": []
    }
    for item in files:
        manifest["files"].append({
            "path": item["path"],
            "md5": item["md5"],
            "size": item["size"],
            "url": url_map.get(item["path"], "")
        })
    MANIFEST.write_text(json.dumps(manifest, indent=2, ensure_ascii=False), "utf-8")
    # Also put it in the launcher folder
    (ROOT / "dist" / "launcher" / "manifest.json").write_text(
        json.dumps(manifest, indent=2, ensure_ascii=False), "utf-8"
    )
    print(f"      manifest.json written: {len(manifest['files'])} files")

# ─── Step 5: git push manifest ───────────────────────────────────────────────
def git_push_manifest():
    print("[5/5] Pushing manifest.json to GitHub...")
    cmds = [
        ["git", "add", "-f", "dist/AquaTech-Client/manifest.json"],
        ["git", "commit", "-m", "chore: update launcher manifest with release URLs"],
        ["git", "push"],
    ]
    for cmd in cmds:
        r = subprocess.run(cmd, cwd=str(ROOT), capture_output=True, text=True)
        out = (r.stdout + r.stderr).strip()
        if out: print("  ", out[:200])


# ─── Main ─────────────────────────────────────────────────────────────────────
if __name__ == "__main__":
    print("=" * 60)
    print("  AquaTech Launcher — GitHub Release Deploy")
    print("=" * 60)

    token = get_token()
    run_deploy()

    print("[2/4] Collecting modpack files...")
    files = collect_files()
    total_mb = round(sum(f["size"] for f in files) / 1024 / 1024, 1)
    print(f"      {len(files)} files, {total_mb} MB total")

    url_map = upload_release(files, token)
    build_manifest(files, url_map)
    git_push_manifest()

    print()
    print("=" * 60)
    print(f"  Done! {len(files)} files uploaded.")
    print("  Friends auto-update on next launcher start.")
    print("=" * 60)
