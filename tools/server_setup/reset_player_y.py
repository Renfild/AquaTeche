from pathlib import Path
import shutil

try:
    import nbtlib
except ImportError:
    import subprocess, sys
    subprocess.check_call([sys.executable, "-m", "pip", "install", "nbtlib", "-q"])
    import nbtlib

p = Path(r"C:\Users\xieto\Desktop\AquaTech\server\world\playerdata\2102d9ae-dcb0-3598-8846-c4e76d4134fd.dat")
backup = p.with_suffix(".dat.bak_pre_yreset")
shutil.copy2(p, backup)
nbt = nbtlib.load(p)
print("before Pos:", nbt.get("Pos"))
pos = nbt["Pos"]
y = float(pos[1])
print("Y=", y)
if y > 150:
    pos[1] = nbtlib.Double(70.0)
    nbt["Pos"] = pos
    nbt.save(p)
    print("after Pos:", nbtlib.load(p).get("Pos"))
else:
    print("Y already safe, no change")
