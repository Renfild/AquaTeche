import json, os, sys, socket
from pathlib import Path

with open('.apex_deploy.json', 'r') as f:
    cfg = json.load(f)

host = cfg['sftp_host']
port = int(cfg['sftp_port'])
user = cfg['sftp_user']
passwd = cfg['sftp_pass']

print(f"Connecting to {host}:{port} ({user})...")

import paramiko

# Also let's try direct IP
ip = socket.gethostbyname(host)
print(f"Host IP: {ip}")

sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.settimeout(15)
sock.connect((ip, port))
print("TCP Socket connected successfully!")

t = paramiko.Transport(sock)
t.connect(username=user, password=passwd)
print("SFTP Transport connected successfully!")

s = paramiko.SFTPClient.from_transport(t)
print("SFTP Client ready!")

# Check crash reports
try:
    crash_files = sorted(s.listdir('crash-reports'))
    print(f"\nCrash reports ({len(crash_files)}):", crash_files[-3:] if crash_files else "None")
    if crash_files:
        latest = crash_files[-1]
        print(f"\n=== LATEST CRASH: {latest} ===")
        with s.open(f'crash-reports/{latest}', 'r') as f:
            content = f.read().decode('utf-8', 'replace')
            print(content[:3500])
except Exception as e:
    print("Crash reports read info:", e)

# Check latest.log
print("\n=== LATEST.LOG (Tail) ===")
try:
    with s.open('logs/latest.log', 'r') as f:
        content = f.read().decode('utf-8', 'replace')
        lines = content.splitlines()
        for line in lines[-80:]:
            print(line)
except Exception as e:
    print("latest.log read info:", e)

# Check debug.log
print("\n=== DEBUG.LOG (Tail) ===")
try:
    with s.open('logs/debug.log', 'r') as f:
        content = f.read().decode('utf-8', 'replace')
        lines = content.splitlines()
        for line in lines[-50:]:
            print(line)
except Exception as e:
    print("debug.log read info:", e)

s.close()
t.close()
