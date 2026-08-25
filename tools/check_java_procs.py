import subprocess, json

cmd = ['wmic', 'process', 'where', "name like '%java%'", 'get', 'ProcessId,CommandLine,ExecutablePath', '/format:list']
res = subprocess.run(cmd, capture_output=True, text=True, errors='ignore')

for block in res.stdout.split('\n\n'):
    if block.strip():
        print("=== PROCESS ===")
        for line in block.strip().splitlines():
            if 'CommandLine' in line:
                print("CommandLine:", line[:250])
            else:
                print(line)
