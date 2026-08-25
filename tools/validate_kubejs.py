import subprocess, os

print("--- Validating KubeJS Scripts in kubejs/ and server/kubejs/ ---")

for root_dir in ['kubejs/server_scripts', 'server/kubejs/server_scripts']:
    if os.path.exists(root_dir):
        for f in os.listdir(root_dir):
            if f.endswith('.js'):
                path = os.path.join(root_dir, f)
                # Check with node -c
                res = subprocess.run(['node', '-c', path], capture_output=True, text=True, errors='ignore')
                if res.returncode != 0:
                    print(f"SYNTAX ERROR in {path}:")
                    print(res.stderr)
                else:
                    print(f"OK: {path}")
