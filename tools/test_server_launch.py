import subprocess
import time
import os

server_dir = r"c:\Users\xieto\Desktop\AquaTech\server"
java_exe = os.path.join(server_dir, "java17", "jdk-17.0.10+7", "bin", "java.exe")
if not os.path.isfile(java_exe):
    java_exe = "java"

cmd = [
    java_exe,
    "-Xms4G",
    "-Xmx8G",
    "-XX:+UseG1GC",
    "--add-opens=java.base/java.lang=ALL-UNNAMED",
    "--add-opens=java.base/java.util=ALL-UNNAMED",
    "-Dfile.encoding=UTF-8",
    "-Dforge.forceCustomPayloadLimit=true",
    "-jar",
    "Mohist-1.20.1.jar",
    "nogui"
]

print(f"Starting server in {server_dir} with {java_exe}...")
proc = subprocess.Popen(
    cmd,
    cwd=server_dir,
    stdout=subprocess.PIPE,
    stderr=subprocess.STDOUT,
    text=True,
    encoding="utf-8",
    errors="replace"
)

start_time = time.time()
try:
    while time.time() - start_time < 35:
        line = proc.stdout.readline()
        if line:
            try:
                print(line.rstrip())
            except UnicodeEncodeError:
                print(line.rstrip().encode('ascii', errors='replace').decode())
        elif proc.poll() is not None:
            print(f"Process terminated with return code {proc.returncode}")
            break
        else:
            time.sleep(0.1)
finally:
    if proc.poll() is None:
        print("Server is running healthy! Sending stop command...")
        # graceful shutdown or terminate test
        proc.terminate()
        try:
            proc.wait(timeout=10)
        except Exception:
            proc.kill()
        print("Server stopped cleanly after test launch.")
