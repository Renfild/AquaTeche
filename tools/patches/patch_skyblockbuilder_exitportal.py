# -*- coding: utf-8 -*-
"""Patch SkyblockBuilder ExitPortal coremod to no-op (Mohist-safe)."""
from __future__ import annotations

import shutil
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
NOOP_JS = r"""function initializeCoreMod() {
    // AquaTech/Mohist: original ExitPortal redirect fails to patch ServerPlayer.
    // No-op keeps SkyblockBuilder loadable without ERROR spam.
    return {};
}
"""


def patch_jar(jar: Path) -> bool:
    if not jar.exists():
        return False
    backup = jar.with_suffix(jar.suffix + ".bak_exitportal")
    if not backup.exists():
        shutil.copy2(jar, backup)
    tmp = jar.with_suffix(".tmp.jar")
    with zipfile.ZipFile(jar, "r") as zin, zipfile.ZipFile(tmp, "w") as zout:
        for info in zin.infolist():
            data = zin.read(info.filename)
            if info.filename.replace("\\", "/") == "coremods/ExitPortal.js":
                data = NOOP_JS.encode("utf-8")
            zout.writestr(info, data)
    tmp.replace(jar)
    print(f"Patched {jar}")
    return True


def main() -> None:
    names = [
        "SkyblockBuilder-1.20.1-5.1.33.jar",
    ]
    dirs = [
        ROOT / "server" / "mods",
        ROOT / "mods",
        ROOT / "client" / "mods",
        Path(r"C:\Users\xieto\curseforge\minecraft\Instances\AquaTech\mods"),
    ]
    ok = 0
    for d in dirs:
        for name in names:
            jar = d / name
            # also glob
            for j in ([jar] if jar.exists() else list(d.glob("SkyblockBuilder*.jar")) if d.exists() else []):
                if patch_jar(j):
                    ok += 1
                    break
    print(f"OK patched {ok} SkyblockBuilder jars")


if __name__ == "__main__":
    main()
