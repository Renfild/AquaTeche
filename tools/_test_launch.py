import sys, subprocess, time
from pathlib import Path
sys.path.insert(0, r"C:\Users\xieto\Desktop\AquaTech\tools")
from aquatech_launcher import build_launch_cmd, find_java, _java_major_version

game = Path.home() / "AppData" / "Roaming" / "AquaTech"
java = find_java()
print("java", java, "major", _java_major_version(java) if java else None)
assert java and _java_major_version(java) == 17, "Need Java 17"
cmd = build_launch_cmd(game, "xietoru", 4096, java, log=lambda m: None)
logdir = game / "logs"
out = open(logdir / "test_stdout.log", "w", encoding="utf-8", errors="replace")
err = open(logdir / "test_stderr.log", "w", encoding="utf-8", errors="replace")
p = subprocess.Popen(cmd, cwd=str(game), stdout=out, stderr=err)
print("pid", p.pid)
time.sleep(45)
alive = p.poll()
print("status", "RUNNING_OK" if alive is None else f"exited {alive}")
err.flush(); out.flush()
print("--- STDERR ---")
print(Path(logdir / "test_stderr.log").read_text(encoding="utf-8", errors="replace")[-2500:])
print("--- STDOUT ---")
print(Path(logdir / "test_stdout.log").read_text(encoding="utf-8", errors="replace")[-2500:])
if alive is None:
    print("Window should be visible — killing test process")
    p.terminate()
    try:
        p.wait(timeout=10)
    except Exception:
        p.kill()
