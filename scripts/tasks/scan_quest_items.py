# -*- coding: utf-8 -*-
"""Scan FTB chapter SNBT for item IDs missing from installed jar models."""
from __future__ import annotations

import re
import zipfile
from pathlib import Path

ROOT = Path(r"C:/Users/xieto/Desktop/AquaTech")
ITEM_RE = re.compile(r'(?:item|icon)\s*:\s*"([a-z0-9_.]+:[a-z0-9_./]+)"')


def jar_items(jar: Path, ns: str) -> set[str]:
    z = zipfile.ZipFile(jar)
    prefix = f"assets/{ns}/models/item/"
    out = set()
    for n in z.namelist():
        if n.startswith(prefix) and n.endswith(".json"):
            rel = n[len(prefix) : -5].replace("\\", "/")
            out.add(f"{ns}:{rel}")
    return out


def main() -> None:
    mods = ROOT / "server" / "mods"
    reg: set[str] = set()
    pairs = [
        ("IndustrialUpgrade-1.20.1-3.4.0.11.jar", "industrialupgrade"),
        ("appliedenergistics2-forge-15.4.10.jar", "ae2"),
        ("Botania-1.20.1-454-FORGE.jar", "botania"),
        ("Re-Avaritia-forge-1.20.1-1.4.1-release.jar", "avaritia"),
        ("alexscaves-2.0.2.jar", "alexscaves"),
        ("aquatech_ui-1.0.30.jar", "aquatech_ui"),
        ("starcatcher-2.3.19-FORGE-1.20.1.jar", "starcatcher"),
    ]
    for name, ns in pairs:
        p = mods / name
        if p.exists():
            got = jar_items(p, ns)
            print(f"{ns}: {len(got)}")
            reg |= got
        else:
            print("MISSING JAR", name)

    chapters = ROOT / "config" / "ftbquests" / "quests" / "chapters"
    for path in sorted(chapters.glob("*.snbt")):
        text = path.read_text(encoding="utf-8")
        bad = []
        for i, line in enumerate(text.splitlines(), 1):
            for m in ITEM_RE.finditer(line):
                it = m.group(1)
                if it.startswith("minecraft:"):
                    continue
                if it not in reg:
                    bad.append((i, it))
        print(f"{path.name}: {len(bad)} bad")
        for row in bad[:40]:
            print(f"  L{row[0]} {row[1]}")


if __name__ == "__main__":
    main()
