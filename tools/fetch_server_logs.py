import json, os, sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT / "scripts" / "tasks"))
import deploy_apexnodes_sftp as deploy

deploy.load_deploy_secrets()

print("--- Connecting to SFTP to fetch logs and crash reports ---")
import paramiko

t = paramiko.Transport((deploy.HOST, deploy.PORT))
t.connect(username=deploy.USER, password=deploy.PASSWORD)
s = paramiko.SFTPClient.from_transport(t)

# Check crash reports
try:
    crash_files = s.listdir('crash-reports')
    print(f"Found crash reports ({len(crash_files)}):", crash_files[-5:])
    if crash_files:
        latest_crash = sorted(crash_files)[-1]
        print(f"\n=== LATEST CRASH REPORT: {latest_crash} ===")
        with s.open(f'crash-reports/{latest_crash}', 'r') as f:
            content = f.read().decode('utf-8', 'replace')
            print(content[:4000])
except Exception as e:
    print(f"No crash-reports folder or error: {e}")

# Check latest.log
print("\n=== LATEST.LOG (last 100 lines) ===")
try:
    with s.open('logs/latest.log', 'r') as f:
        content = f.read().decode('utf-8', 'replace')
        lines = content.splitlines()
        for line in lines[-100:]:
            print(line)
except Exception as e:
    print(f"Error reading latest.log: {e}")

# Check debug.log
print("\n=== DEBUG.LOG (last 50 lines) ===")
try:
    with s.open('logs/debug.log', 'r') as f:
        content = f.read().decode('utf-8', 'replace')
        lines = content.splitlines()
        for line in lines[-50:]:
            print(line)
except Exception as e:
    print(f"Error reading debug.log: {e}")

s.close()
t.close()
