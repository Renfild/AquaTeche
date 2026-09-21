#!/usr/bin/env python3
"""Install user-provided case chest icons everywhere.

Source: folder with <case-id>.png renders (default: Desktop\\aquaaddons\\cases_png).
Targets:
  * tools/case_icon_map.json   -> base64 data URIs (128x128) for F4 hub.html
  * docs/assets/images/cases/  -> site PNGs (256x256)

Run: python tools/install_case_chest_icons.py [source_folder]
"""
from __future__ import annotations

import base64
import io
import json
import sys
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
DEFAULT_SRC = Path.home() / "Desktop" / "aquaaddons" / "cases_png"
MAP_FILE = ROOT / "tools" / "case_icon_map.json"
CASES_JSON = ROOT / "config" / "aqualumen" / "cases.json"
SITE_DIR = ROOT / "docs" / "assets" / "images" / "cases"

HUB_SIZE = 128
SITE_SIZE = 256


def case_ids() -> list[str]:
    data = json.loads(CASES_JSON.read_text(encoding="utf-8"))
    cases = data.get("cases", data if isinstance(data, list) else [])
    return [c["id"] for c in cases if c.get("id")]


def to_png_bytes(im: Image.Image) -> bytes:
    buf = io.BytesIO()
    im.save(buf, format="PNG", optimize=True)
    return buf.getvalue()


def resize(src: Path, size: int) -> Image.Image:
    im = Image.open(src).convert("RGBA")
    if im.size != (size, size):
        im = im.resize((size, size), Image.LANCZOS)
    return im


def main() -> int:
    src_dir = Path(sys.argv[1]) if len(sys.argv) > 1 else DEFAULT_SRC
    if not src_dir.is_dir():
        print("source folder not found:", src_dir, file=sys.stderr)
        return 2

    mapping = json.loads(MAP_FILE.read_text(encoding="utf-8"))
    SITE_DIR.mkdir(parents=True, exist_ok=True)
    updated = []
    missing = []

    for case_id in case_ids():
        src = src_dir / f"{case_id}.png"
        if not src.is_file():
            missing.append(case_id)
            continue
        hub_bytes = to_png_bytes(resize(src, HUB_SIZE))
        mapping[case_id] = "data:image/png;base64," + base64.b64encode(hub_bytes).decode("ascii")
        site_bytes = to_png_bytes(resize(src, SITE_SIZE))
        (SITE_DIR / f"{case_id}.png").write_bytes(site_bytes)
        updated.append((case_id, len(hub_bytes) // 1024, len(site_bytes) // 1024))

    MAP_FILE.write_text(json.dumps(mapping, ensure_ascii=False, indent=1) + "\n", encoding="utf-8")

    print(f"updated {len(updated)} case icons from {src_dir}")
    for case_id, hub_kb, site_kb in updated:
        print(f"  {case_id:15s} hub {hub_kb:4d} KB | site {site_kb:4d} KB")
    if missing:
        print("no source art for:", ", ".join(missing))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
