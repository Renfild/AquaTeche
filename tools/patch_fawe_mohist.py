"""Patch FAWE for Mohist: skip non-minecraft ids in IBukkitAdapter + soft TypeProperty.

Also used after jar updates. Does not change wand config (see worldedit-config.yml).

Usage:
  python tools/patch_fawe_mohist.py path/to/FastAsyncWorldEdit.jar
"""
from __future__ import annotations

import shutil
import sys
import zipfile
from pathlib import Path

IBUKKIT = "com/fastasyncworldedit/bukkit/adapter/IBukkitAdapter.class"
TYPEPROP = (
    "com/sk89q/worldedit/bukkit/adapter/ext/fawe/v1_20_R1/PaperweightAdapter$1.class"
)

THROW_MSGS = (
    b"Bukkit only supports Minecraft blocks",
    b"Bukkit only supports Minecraft items",
    b"Bukkit only supports vanilla entities",
    b"Bukkit only supports vanilla biomes",
)


def _read_u2(data: bytes | bytearray, i: int) -> int:
    return (data[i] << 8) | data[i + 1]


def _utf8_and_string_consts(data: bytes | bytearray) -> dict[bytes, int]:
    """Map Utf8 bytes -> constant-pool index of the CONSTANT_String that points at it."""
    cp_count = _read_u2(data, 8)
    i = 10
    cp: list[tuple | None] = [None]
    idx = 1
    while idx < cp_count:
        tag = data[i]
        if tag == 1:
            ln = _read_u2(data, i + 1)
            s = bytes(data[i + 3 : i + 3 + ln])
            cp.append((1, s))
            i += 3 + ln
        elif tag in (7, 8, 16, 19, 20):
            cp.append((tag, _read_u2(data, i + 1)))
            i += 3
        elif tag in (3, 4):
            cp.append((tag,))
            i += 5
        elif tag in (5, 6):
            cp.append((tag,))
            cp.append(None)
            i += 9
            idx += 1
        elif tag in (9, 10, 11, 12, 17, 18):
            cp.append((tag,))
            i += 5
        elif tag == 15:
            cp.append((tag,))
            i += 4
        else:
            raise SystemExit(f"unknown CP tag {tag} at {i}")
        idx += 1

    out: dict[bytes, int] = {}
    for n, e in enumerate(cp):
        if not e or e[0] != 8:
            continue
        utf = cp[e[1]]
        if utf and utf[0] == 1 and utf[1] in THROW_MSGS:
            out[utf[1]] = n
    return out


def patch_ibukkit(data: bytes) -> bytes:
    """Replace 'throw new IllegalArgumentException(msg)' with 'return null'."""
    patched = bytearray(data)
    str_consts = _utf8_and_string_consts(patched)
    missing = [m for m in THROW_MSGS if m not in str_consts]
    if missing:
        raise SystemExit(f"IBukkitAdapter missing strings: {missing}")

    hits = 0
    for msg, sc in str_consts.items():
        found = False
        i = 0
        while i < len(patched) - 12:
            # new #IllegalArgumentException ; dup ; ldc/ldc_w sc ; invokespecial ; athrow
            if patched[i] == 0xBB and patched[i + 3] == 0x59:
                if patched[i + 4] == 0x12 and patched[i + 5] == sc:
                    end = i + 10  # bb?? ?? 59 12 xx b7 ?? ?? bf
                    if patched[i + 6] == 0xB7 and patched[i + 9] == 0xBF:
                        repl = bytes([0x01, 0xB0]) + bytes(8)
                        patched[i : i + 10] = repl
                        hits += 1
                        found = True
                        i = end
                        continue
                if patched[i + 4] == 0x13 and _read_u2(patched, i + 5) == sc:
                    end = i + 11  # bb?? ?? 59 13 xxxx b7 ?? ?? bf
                    if patched[i + 7] == 0xB7 and patched[i + 10] == 0xBF:
                        repl = bytes([0x01, 0xB0]) + bytes(9)
                        patched[i : i + 11] = repl
                        hits += 1
                        found = True
                        i = end
                        continue
            i += 1
        if not found:
            raise SystemExit(f"no throw site for {msg!r}")
    print(f"  IBukkitAdapter: patched {hits} throw sites -> return null")
    return bytes(patched)


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
    tmp = jar.with_suffix(".jar.tmp")
    with zipfile.ZipFile(jar, "r") as zin, zipfile.ZipFile(
        tmp, "w", compression=zipfile.ZIP_DEFLATED
    ) as zout:
        for info in zin.infolist():
            data = replacements.get(info.filename) or zin.read(info.filename)
            zout.writestr(info, data)
    tmp.replace(jar)


def main() -> int:
    if len(sys.argv) < 2:
        print("usage: patch_fawe_mohist.py <FastAsyncWorldEdit.jar>")
        return 2
    jar = Path(sys.argv[1])
    if not jar.is_file():
        print(f"missing {jar}")
        return 1

    bak = jar.with_suffix(jar.suffix + ".pre-mohist.bak")
    # Prefer clean upstream if older typeproperty bak exists and current is dirty
    upstream = jar.with_suffix(jar.suffix + ".pre-typeproperty.bak")
    if upstream.is_file() and not bak.is_file():
        shutil.copy2(upstream, jar)
        print(f"restored from {upstream.name}")
    if not bak.exists():
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
    print(f"patched {jar}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
