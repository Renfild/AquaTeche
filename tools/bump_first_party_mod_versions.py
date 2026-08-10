#!/usr/bin/env python3
"""Bump version string inside a Forge mods.toml jar and rename the file."""
from __future__ import annotations

import os
import re
import zipfile
from pathlib import Path


def bump_jar(src: Path, new_ver: str, dest: Path) -> None:
    if not src.is_file():
        raise SystemExit(f"missing {src}")
    dest.parent.mkdir(parents=True, exist_ok=True)
    tmp = dest.with_suffix(dest.suffix + ".tmp")
    with zipfile.ZipFile(src, "r") as zin, zipfile.ZipFile(
        tmp, "w", compression=zipfile.ZIP_DEFLATED
    ) as zout:
        for info in zin.infolist():
            data = zin.read(info.filename)
            if info.filename.replace("\\", "/").endswith("META-INF/mods.toml"):
                text = data.decode("utf-8")
                text2, n = re.subn(
                    r'(version\s*=\s*")[^"]+(")',
                    rf"\g<1>{new_ver}\2",
                    text,
                    count=1,
                )
                if n < 1:
                    raise SystemExit(f"no version= in {info.filename}")
                data = text2.encode("utf-8")
            zout.writestr(info, data)
    if dest.exists() and dest.resolve() != tmp.resolve():
        dest.unlink(missing_ok=True)
    tmp.replace(dest)
    print(f"OK {src.name} -> {dest.name} version={new_ver}")


def main() -> int:
    root = Path(__file__).resolve().parents[1]
    pairs = [
        (\"aquatech_ui\", \"1.0.3\"),
        ("casesmod", "1.0.1"),
    ]
    targets = [
        root / "server" / "mods",
        root / "mods",
        root / "client" / "mods",
        root / "dist" / "AquaTech-Client" / "mods",
    ]
    app = Path(os.environ.get("APPDATA", "")) / "AquaTech" / "mods"
    if app.is_dir():
        targets.append(app)

    for stem, ver in pairs:
        new_name = f"{stem}-{ver}.jar"
        # find any existing jar for this stem
        src = None
        for t in targets:
            if not t.is_dir():
                continue
            for cand in sorted(t.glob(f"{stem}-*.jar")):
                src = cand
                break
            if src:
                break
        if src is None:
            print("skip missing", stem)
            continue
        for t in targets:
            if not t.is_dir():
                continue
            out = t / new_name
            # use local copy if present else primary src
            local_src = next(iter(sorted(t.glob(f"{stem}-*.jar"))), src)
            bump_jar(local_src, ver, out)
            for old in t.glob(f"{stem}-*.jar"):
                if old.name != new_name:
                    old.unlink(missing_ok=True)
                    print("removed", old)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
