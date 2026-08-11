"""Patch FAWE for Mohist: skip non-minecraft ids in IBukkitAdapter + soft TypeProperty.

Also used after jar updates. Does not change wand config (see worldedit-config.yml).

Usage:
  python tools/patch_fawe_mohist.py path/to/FastAsyncWorldEdit.jar
"""
from __future__ import annotations

import shutil
import subprocess
import sys
import tempfile
import zipfile
from pathlib import Path

IBUKKIT = "com/fastasyncworldedit/bukkit/adapter/IBukkitAdapter.class"
TYPEPROP = (
    "com/sk89q/worldedit/bukkit/adapter/ext/fawe/v1_20_R1/PaperweightAdapter$1.class"
)

ASM_DIR = Path(__file__).resolve().parent / "_asm"
ASM_VER = "9.7.1"


def _read_u2(data: bytes | bytearray, i: int) -> int:
    return (data[i] << 8) | data[i + 1]


def _java_bin() -> str:
    for candidate in (
        Path(r"C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot\bin\java.exe"),
        Path(r"C:\Program Files\Eclipse Adoptium\jdk-25.0.1.8-hotspot\bin\java.exe"),
    ):
        if candidate.is_file():
            return str(candidate)
    return "java"


def _javac_bin() -> str:
    java = Path(_java_bin())
    javac = java.with_name("javac.exe" if java.suffix.lower() == ".exe" else "javac")
    if javac.is_file():
        return str(javac)
    return "javac"


def _asm_classpath() -> str:
    jars = [
        ASM_DIR / f"asm-{ASM_VER}.jar",
        ASM_DIR / f"asm-tree-{ASM_VER}.jar",
        ASM_DIR / f"asm-commons-{ASM_VER}.jar",
    ]
    missing = [str(j) for j in jars if not j.is_file()]
    if missing:
        raise SystemExit(f"missing ASM jars: {missing}")
    return ";".join(str(j) for j in jars)


def _ensure_patcher_compiled() -> Path:
    src = ASM_DIR / "PatchIBukkit.java"
    cls = ASM_DIR / "PatchIBukkit.class"
    if not src.is_file():
        raise SystemExit(f"missing {src}")
    if cls.is_file() and cls.stat().st_mtime >= src.stat().st_mtime:
        return ASM_DIR
    cp = _asm_classpath()
    r = subprocess.run(
        [_javac_bin(), "-cp", cp, "-d", str(ASM_DIR), str(src)],
        capture_output=True,
        text=True,
    )
    if r.returncode != 0:
        raise SystemExit(f"javac PatchIBukkit failed:\n{r.stderr or r.stdout}")
    return ASM_DIR


def patch_ibukkit(data: bytes) -> bytes:
    """Replace IllegalArgumentException throws with return null (ASM recomputes frames)."""
    _ensure_patcher_compiled()
    cp = _asm_classpath() + ";" + str(ASM_DIR)
    with tempfile.TemporaryDirectory(prefix="fawe_ibukkit_") as td:
        tin = Path(td) / "in.class"
        tout = Path(td) / "out.class"
        tin.write_bytes(data)
        r = subprocess.run(
            [_java_bin(), "-cp", cp, "PatchIBukkit", str(tin), str(tout)],
            capture_output=True,
            text=True,
        )
        if r.returncode != 0:
            raise SystemExit(f"PatchIBukkit failed:\n{r.stderr or r.stdout}")
        if r.stdout.strip():
            print(r.stdout.rstrip())
        return tout.read_bytes()


def patch_typeproperty(data: bytes) -> bytes:
    """Unknown IBlockState impls fall through to IntegerProperty instead of throw."""
    patched = bytearray(data)
    hits = 0
    i = 0
    while i < len(patched) - 8:
        if patched[i] == 0x2B and patched[i + 1] == 0xC1 and patched[i + 4] == 0x99:
            window = bytes(patched[i : i + 80])
            if 0xBF in window[10:]:
                patched[i + 4] = 0xA7
                patched[i + 5] = 0x00
                patched[i + 6] = 0x03
                hits += 1
                i += 7
                continue
        i += 1
    if hits == 0:
        print("  TypeProperty: no ifeq pattern (already patched?)")
    else:
        print(f"  TypeProperty: patched {hits} branch(es)")
    return bytes(patched)


def rewrite_jar(jar: Path, replacements: dict[str, bytes]) -> None:
    tmp = jar.with_name(jar.name + ".writing")
    patched = jar.with_name(jar.stem + "-patched" + jar.suffix)
    if tmp.exists():
        tmp.unlink()
    with zipfile.ZipFile(jar, "r") as zin, zipfile.ZipFile(
        tmp, "w", compression=zipfile.ZIP_DEFLATED
    ) as zout:
        for info in zin.infolist():
            data = replacements.get(info.filename) or zin.read(info.filename)
            zout.writestr(info, data)
    if patched.exists():
        patched.unlink()
    tmp.replace(patched)
    try:
        jar.unlink(missing_ok=True)
        patched.replace(jar)
    except OSError as ex:
        print(f"  WARN could not replace {jar.name} ({ex})")
        print(f"  patched jar left at {patched}")
        # Do not raise — caller can pick up *-patched.jar
        return


def main() -> int:
    if len(sys.argv) < 2:
        print("usage: patch_fawe_mohist.py <FastAsyncWorldEdit.jar>")
        return 2
    jar = Path(sys.argv[1])
    if not jar.is_file():
        print(f"missing {jar}")
        return 1

    bak = jar.with_suffix(jar.suffix + ".pre-mohist.bak")
    upstream = jar.with_suffix(jar.suffix + ".pre-typeproperty.bak")
    if bak.is_file():
        shutil.copy2(bak, jar)
        print(f"restored from {bak.name}")
    elif upstream.is_file():
        shutil.copy2(upstream, jar)
        print(f"restored from {upstream.name}")
        shutil.copy2(jar, bak)
        print(f"backup {bak.name}")
    else:
        shutil.copy2(jar, bak)
        print(f"backup {bak.name}")

    with zipfile.ZipFile(jar, "r") as zin:
        names = set(zin.namelist())
        if IBUKKIT not in names:
            print(f"jar missing {IBUKKIT}")
            return 1
        replacements: dict[str, bytes] = {
            IBUKKIT: patch_ibukkit(zin.read(IBUKKIT)),
        }
        if TYPEPROP in names:
            replacements[TYPEPROP] = patch_typeproperty(zin.read(TYPEPROP))

    rewrite_jar(jar, replacements)
    if jar.is_file():
        print(f"patched {jar}")
    else:
        alt = jar.with_name(jar.stem + "-patched" + jar.suffix)
        print(f"patched (via {alt.name})")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
