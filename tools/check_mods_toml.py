import os
import glob
import zipfile

patterns = [
    os.path.expandvars(r"%APPDATA%\AquaTech\mods\*.jar"),
    r"mods\*\build\libs\*.jar",
    r"server\mods\*.jar",
    r"client\mods\*.jar",
]

for pat in patterns:
    for jar in glob.glob(pat):
        try:
            with zipfile.ZipFile(jar, "r") as z:
                if "META-INF/mods.toml" in z.namelist():
                    content = z.read("META-INF/mods.toml").decode("utf-8", errors="ignore")
                    if "${" in content:
                        print("FOUND in " + jar + ":")
                        for line in content.splitlines():
                            if "${" in line:
                                print("  " + line)
        except Exception as e:
            print("Error reading " + jar + ": " + str(e))
