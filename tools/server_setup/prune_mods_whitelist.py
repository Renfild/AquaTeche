# -*- coding: utf-8 -*-
"""Park non-whitelist mods; keep AquaTech curated set + hard deps."""
from __future__ import annotations

import re
import shutil
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]

# Filename substrings / exact patterns that MUST stay (case-insensitive match on filename)
KEEP_SUBSTRINGS = [
    # ours
    "aquatech_ui",
    "casesmod",
    # AE2
    "appliedenergistics2",
    "guideme",
    # Botania stack
    "Botania-",
    "BotanicalMachinery",
    "botanicalextramachinery",
    "MythicBotany",
    "extrabotany",
    "eventwrapper",
    "Patchouli",
    "LibX-",
    # Industrial Upgrade stack
    "IndustrialUpgrade",
    "powerutils",
    "quantumgenerators",
    "simplyquarries",
    # fishing / alex / bees
    "Aquaculture",
    "alexscaves",
    "alexsmobs",
    "citadel-",
    "geckolib-",
    "productivebees",
    # JEI
    "jei-",
    # FTB Quests stack
    "ftb-quests",
    "ftb-library",
    "ftb-teams",
    "item-filters",
    "ftbquestprecisionlocalizer",
    "ftbquestsentityvis",
    "ftbquestsfreezefix",
    "FTBQuestsOptimizer",
    # Draconic
    "Draconic-Evolution",
    "BrandonsCore",
    "CodeChickenLib",
    # Avaritia
    "Re-Avaritia",
    "avaritia_armor",
    # CraftTweaker
    "CraftTweaker",
    # common hard deps for kept mods
    "curios-",
    "architectury-",  # required by FTB Quests / Teams / Library / Item Filters
]

# Explicitly NEVER keep even if name overlaps
BAN_SUBSTRINGS = [
    "industrial-foregoing",  # not IU
]


def is_keep(name: str) -> bool:
    low = name.lower()
    for ban in BAN_SUBSTRINGS:
        if ban.lower() in low:
            return False
    for k in KEEP_SUBSTRINGS:
        if k.lower() in low:
            return True
    return False


def mandatory_deps(jar: Path) -> list[str]:
    try:
        with zipfile.ZipFile(jar) as z:
            raw = None
            for p in ("META-INF/mods.toml", "META-INF/neoforge.mods.toml"):
                if p in z.namelist():
                    raw = z.read(p).decode("utf-8", "replace")
                    break
            if not raw:
                return []
    except Exception:
        return []
    out = []
    # split dependency tables
    for part in re.split(r"\[\[dependencies\.[^\]]+\]\]", raw):
        if "mandatory" not in part:
            continue
        # crude: if this fragment has mandatory=true and a modId
        m_mand = re.search(r"mandatory\s*=\s*(true|false)", part, re.I)
        m_id = re.search(r'modId\s*=\s*"([^"]+)"', part)
        if m_mand and m_id and m_mand.group(1).lower() == "true":
            mid = m_id.group(1)
            if mid not in ("minecraft", "forge", "java"):
                out.append(mid)
    return out


def park_dir(mods_dir: Path, park_root: Path) -> tuple[int, int]:
    if not mods_dir.exists():
        return 0, 0
    park = park_root / mods_dir.name
    park.mkdir(parents=True, exist_ok=True)
    kept = removed = 0
    for jar in list(mods_dir.glob("*.jar")):
        if is_keep(jar.name):
            kept += 1
            continue
        dest = park / jar.name
        if dest.exists():
            dest.unlink()
        shutil.move(str(jar), str(dest))
        removed += 1
        print(f"PARK {mods_dir}: {jar.name}")
    return kept, removed


def main() -> None:
    targets = [
        ROOT / "server" / "mods",
        ROOT / "mods",
        ROOT / "client" / "mods",
        ROOT / "server" / "client" / "mods",
        ROOT / "dist" / "AquaTech-Client" / "mods",
        Path(r"C:\Users\xieto\curseforge\minecraft\Instances\AquaTech\mods"),
        Path(r"C:\Users\xieto\curseforge\minecraft\Instances\AquaTech\client\mods"),
    ]
    park_root = ROOT / "_parked_mods"
    park_root.mkdir(exist_ok=True)

    print("=== KEEP preview (server/mods) ===")
    sm = ROOT / "server" / "mods"
    if sm.exists():
        for j in sorted(sm.glob("*.jar")):
            mark = "KEEP" if is_keep(j.name) else "PARK"
            print(f"  {mark}  {j.name}")
            if is_keep(j.name):
                deps = mandatory_deps(j)
                if deps:
                    print(f"       deps: {deps}")

    total_k = total_r = 0
    for d in targets:
        k, r = park_dir(d, park_root)
        if k or r:
            print(f"== {d}: kept={k} parked={r}")
        total_k += k
        total_r += r

    print(f"\nDONE parked_to={park_root} (aggregate kept refs={total_k}, parked={total_r})")
    print("Restore: move jars back from _parked_mods/<folder>/")


if __name__ == "__main__":
    main()
