"""Remove IU free ore-scanner give from IUCore.loginPlayer.

Usage:
  python tools/patch_iu_no_free_scanner.py path/to/IndustrialUpgrade-*.jar
"""
from __future__ import annotations

import shutil
import subprocess
import sys
import tempfile
import zipfile
from pathlib import Path

ASM_DIR = Path(__file__).resolve().parent / "_asm"
ASM_VER = "9.7.1"
CLS = "com/denfop/IUCore.class"


def _java() -> str:
    for c in (
        Path(r"C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot\bin\java.exe"),
        Path(r"C:\Program Files\Eclipse Adoptium\jdk-25.0.1.8-hotspot\bin\java.exe"),
    ):
        if c.is_file():
            return str(c)
    return "java"


def _javac() -> str:
    j = Path(_java())
    jc = j.with_name("javac.exe" if j.suffix.lower() == ".exe" else "javac")
    return str(jc) if jc.is_file() else "javac"


def _cp() -> str:
    jars = [ASM_DIR / f"asm-{ASM_VER}.jar", ASM_DIR / f"asm-tree-{ASM_VER}.jar"]
    return ";".join(str(j) for j in jars)


def _ensure_compiled() -> None:
    src = ASM_DIR / "PatchIUNoFreeScanner.java"
    cls = ASM_DIR / "PatchIUNoFreeScanner.class"
    if cls.is_file() and cls.stat().st_mtime >= src.stat().st_mtime:
        return
    r = subprocess.run(
        [_javac(), "-cp", _cp(), "-d", str(ASM_DIR), str(src)],
        capture_output=True,
        text=True,
    )
    if r.returncode:
        raise SystemExit(r.stderr or r.stdout)


def patch_jar(jar: Path) -> None:
    _ensure_compiled()
    bak = jar.with_suffix(jar.suffix + ".pre-noscanner.bak")
    if bak.is_file():
        shutil.copy2(bak, jar)
    else:
        shutil.copy2(jar, bak)
        print(f"backup {bak.name}")
    with zipfile.ZipFile(jar, "r") as zin:
        raw = zin.read(CLS)
    with tempfile.TemporaryDirectory() as td:
        tin, tout = Path(td) / "in.class", Path(td) / "out.class"
        tin.write_bytes(raw)
        r = subprocess.run(
            [
                _java(),
                "-cp",
                _cp() + ";" + str(ASM_DIR),
                "PatchIUNoFreeScanner",
                str(tin),
                str(tout),
            ],
            capture_output=True,
            text=True,
        )
        if r.returncode:
            raise SystemExit(r.stderr or r.stdout)
        if r.stdout.strip():
            print(r.stdout.rstrip())
        patched = tout.read_bytes()
    tmp = jar.with_suffix(".jar.tmp")
    with zipfile.ZipFile(jar, "r") as zin, zipfile.ZipFile(
        tmp, "w", compression=zipfile.ZIP_DEFLATED
    ) as zout:
        for info in zin.infolist():
            data = patched if info.filename == CLS else zin.read(info.filename)
            zout.writestr(info, data)
    tmp.replace(jar)
    print(f"patched {jar}")


def main() -> int:
    if len(sys.argv) < 2:
        print("usage: patch_iu_no_free_scanner.py <IndustrialUpgrade.jar>")
        return 2
    jar = Path(sys.argv[1])
    if not jar.is_file():
        print(f"missing {jar}")
        return 1
    patch_jar(jar)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
