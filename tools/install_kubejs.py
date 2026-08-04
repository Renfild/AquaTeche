# -*- coding: utf-8 -*-
"""Install KubeJS + Rhino (+ ProbeJS Legacy on clients) for MC 1.20.1 Forge."""
from __future__ import annotations

import shutil
import urllib.request
from pathlib import Path

ROOT = Path(r"C:\Users\xieto\Desktop\AquaTech")
CACHE = ROOT / "_mod_dl_cache"
CACHE.mkdir(exist_ok=True)

SERVER_AND_BOTH = [
    ROOT / "mods",
    ROOT / "server" / "mods",
    ROOT / "client" / "mods",
    ROOT / "server" / "client" / "mods",
    ROOT / "dist" / "AquaTech-Client" / "mods",
    Path(r"C:\Users\xieto\curseforge\minecraft\Instances\AquaTech\mods"),
    Path(r"C:\Users\xieto\curseforge\minecraft\Instances\AquaTech\client\mods"),
]

CLIENT_ONLY = [
    ROOT / "client" / "mods",
    ROOT / "server" / "client" / "mods",
    ROOT / "dist" / "AquaTech-Client" / "mods",
    Path(r"C:\Users\xieto\curseforge\minecraft\Instances\AquaTech\mods"),
    Path(r"C:\Users\xieto\curseforge\minecraft\Instances\AquaTech\client\mods"),
]

DOWNLOADS_BOTH = [
    (
        "https://cdn.modrinth.com/data/umyGl7zF/versions/hVR2xUSr/kubejs-forge-2001.6.5-build.26.jar",
        "kubejs-forge-2001.6.5-build.26.jar",
    ),
    (
        "https://cdn.modrinth.com/data/sk9knFPE/versions/uNALdylI/rhino-forge-2001.2.3-build.10.jar",
        "rhino-forge-2001.2.3-build.10.jar",
    ),
]

DOWNLOADS_CLIENT = [
    (
        "https://cdn.modrinth.com/data/KVw0Q70k/versions/m0TaAVwY/ProbeJSLegacy-1.20.1-6.2.0.jar",
        "ProbeJSLegacy-1.20.1-6.2.0.jar",
    ),
]


def download(url: str, name: str) -> Path:
    dest = CACHE / name
    if dest.exists() and dest.stat().st_size > 1000:
        print("cached", name, dest.stat().st_size)
        return dest
    print("download", name)
    req = urllib.request.Request(url, headers={"User-Agent": "AquaTech/1.0"})
    with urllib.request.urlopen(req, timeout=180) as r, open(dest, "wb") as f:
        shutil.copyfileobj(r, f)
    print(" ok", dest.stat().st_size)
    return dest


def install(files: list[Path], targets: list[Path], patterns: tuple[str, ...]) -> None:
    for t in targets:
        t.mkdir(parents=True, exist_ok=True)
        for pat in patterns:
            for old in t.glob(pat):
                if old.name in {f.name for f in files}:
                    continue
                print("remove old", old)
                old.unlink(missing_ok=True)
        for f in files:
            out = t / f.name
            shutil.copy2(f, out)
            print("->", out)


def main() -> None:
    both = [download(u, n) for u, n in DOWNLOADS_BOTH]
    client = [download(u, n) for u, n in DOWNLOADS_CLIENT]
    install(both, SERVER_AND_BOTH, ("kubejs-*.jar", "rhino-*.jar"))
    install(client, CLIENT_ONLY, ("ProbeJS*.jar", "probejs*.jar"))
    print("DONE")


if __name__ == "__main__":
    main()
