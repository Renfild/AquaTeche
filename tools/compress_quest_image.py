"""Compress PNG for FTB Quests chapter images / icons.

Usage:
  python tools/compress_quest_image.py my_art.png chapter_banner
  python tools/compress_quest_image.py my_art.png chapter_banner 512

Then in FTB Quests set image to:
  kubejs:textures/quests/chapter_banner.png

Rules for multiplayer stability:
  - Prefer resource-pack paths (kubejs:...), NOT Quest Enhance clipboard paste of huge screenshots
  - Max width ~512px, file ideally under 30KB
  - Deploy the PNG to BOTH client and server kubejs folders (this script does that)
"""
from __future__ import annotations

import sys
from pathlib import Path

from PIL import Image

ROOT = Path(r"C:\Users\xieto\Desktop\AquaTech")
CF = Path(r"C:\Users\xieto\curseforge\minecraft\Instances\AquaTech")


def main() -> int:
    if len(sys.argv) < 3:
        print("Usage: python compress_quest_image.py <input.png> <name> [max_width=512]")
        return 1

    src = Path(sys.argv[1])
    name = sys.argv[2]
    max_w = int(sys.argv[3]) if len(sys.argv) > 3 else 512
    max_kb = 30

    im0 = Image.open(src).convert("RGBA")
    dests = [
        ROOT / "kubejs/assets/kubejs/textures/quests" / f"{name}.png",
        ROOT / "server/kubejs/assets/kubejs/textures/quests" / f"{name}.png",
        CF / "kubejs/assets/kubejs/textures/quests" / f"{name}.png",
    ]

    tw = min(max_w, im0.width)
    out = im0.resize((tw, int(im0.height * tw / im0.width)), Image.Resampling.LANCZOS) if tw < im0.width else im0

    dests[0].parent.mkdir(parents=True, exist_ok=True)
    while True:
        out.save(dests[0], "PNG", optimize=True)
        size = dests[0].stat().st_size
        if size <= max_kb * 1024 or tw <= 160:
            break
        tw = int(tw * 0.85)
        out = im0.resize((tw, int(im0.height * tw / im0.width)), Image.Resampling.LANCZOS)

    data = dests[0].read_bytes()
    for d in dests:
        d.parent.mkdir(parents=True, exist_ok=True)
        d.write_bytes(data)
        print(f"OK {d} ({d.stat().st_size} bytes, {Image.open(d).size})")

    print(f"FTB image path: kubejs:textures/quests/{name}.png")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
