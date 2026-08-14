"""Patch FAWE PaperweightAdapter$1 so unknown block properties (TypeProperty etc.)
fall back to IntegerProperty instead of crashing BlockTypes init on Mohist.

Usage:
  python tools/patch_fawe_typeproperty.py path/to/FastAsyncWorldEdit.jar
"""
from __future__ import annotations

import shutil
import sys
import zipfile
from pathlib import Path

# Inner class that throws on unknown IBlockState implementations
TARGET = "com/sk89q/worldedit/bukkit/adapter/ext/fawe/v1_20_R1/PaperweightAdapter$1.class"


def patch_class(data: bytes) -> bytes:
    """Replace the final 'ifeq <throw>' after BlockStateInteger check with goto IntegerProperty path.

    Pattern near end of load(IBlockState):
      aload_1
      instanceof <BlockStateInteger>
      ifeq <throw_label>     <-- change to goto <integer_property_label>
      new IntegerProperty
      ...
      areturn
      new IllegalArgumentException
      ...
      athrow
    """
    # Find: aload_1 (0x2b), instanceof (0xc1), u2, ifeq (0x99), u2
    # that is followed within ~40 bytes by athrow (0xbf) after IllegalArgumentException
    needle_athrow_msg = b"needs an update to support"
    if needle_athrow_msg not in data and b"TypeProperty" not in data:
        # Still patch structurally; message may be indy-concatenated
        pass

    patched = bytearray(data)
    hits = 0
    i = 0
    while i < len(patched) - 8:
        # aload_1, instanceof, idxhi, idxlo, ifeq, offhi, offlo
        if (
            patched[i] == 0x2B
            and patched[i + 1] == 0xC1
            and patched[i + 4] == 0x99
        ):
            # Look ahead for athrow soon after (throw path)
            window = bytes(patched[i : i + 80])
            if 0xBF in window[10:]:
                # Change ifeq -> goto (0xA7), keep the same branch offset that pointed to throw.
                # We want to SKIP the throw and go to IntegerProperty instead.
                # Current ifeq jumps TO throw when NOT integer.
                # IntegerProperty starts at i+7 (next instruction after ifeq u2).
                # So replace ifeq(throw) with goto(i+7) relative = 3? 
                # goto offset is from this instruction: goto is 3 bytes, target is i+7,
                # relative = (i+7) - i = 7. Wait: branch offset is relative to the branch opcode address.
                # goto at i+4, target IntegerProperty at i+7, offset = 3.
                patched[i + 4] = 0xA7  # goto
                patched[i + 5] = 0x00
                patched[i + 6] = 0x03  # jump to next insn (IntegerProperty new)
                hits += 1
                i += 7
                continue
        i += 1

    if hits == 0:
        raise SystemExit("no aload_1/instanceof/ifeq pattern found to patch")
    return bytes(patched)


def main() -> int:
    if len(sys.argv) < 2:
        print("usage: patch_fawe_typeproperty.py <FastAsyncWorldEdit.jar>")
        return 2
    jar = Path(sys.argv[1])
    if not jar.is_file():
        print(f"missing {jar}")
        return 1

    bak = jar.with_suffix(jar.suffix + ".pre-typeproperty.bak")
    if not bak.exists():
        shutil.copy2(jar, bak)
        print(f"backup {bak.name}")

    with zipfile.ZipFile(jar, "r") as zin:
        if TARGET not in zin.namelist():
            print(f"jar missing {TARGET}")
            return 1
        original = zin.read(TARGET)
        patched = patch_class(original)
        # rewrite jar
        tmp = jar.with_suffix(".jar.tmp")
        with zipfile.ZipFile(tmp, "w", compression=zipfile.ZIP_DEFLATED) as zout:
            for info in zin.infolist():
                data = patched if info.filename == TARGET else zin.read(info.filename)
                zout.writestr(info, data)

    tmp.replace(jar)
    print(f"patched {jar} ({TARGET})")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
