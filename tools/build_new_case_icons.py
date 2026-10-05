#!/usr/bin/env python3
"""Иконки для механизмов, панелей и накопителей, которые лежат в кейсах.

    python tools/build_new_case_icons.py

Предметы, добавленные в кейсы (ADDITIONS в rebalance_cases.py), и все предметы без иконки рисуются по настоящей
модели блока (tools/render_block_icon.py). Если у модели нет elements, берётся куб из текстур модели.
Множители улова берутся из текстур мода (жемчужины), а не из старых плашек. Запускать после
build_all_case_textures.py и patch_missing_textures.py, потом tools/build_hub_html.py.
"""
import importlib.util
import io
import json
import zipfile
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
CASES = ROOT / "config" / "aqualumen" / "cases.json"
OUT_FILE = ROOT / "tools" / "extracted_case_textures.json"

JARS = {
    "industrialupgrade": ROOT / "mods" / "IndustrialUpgrade-1.20.1-3.4.0.11.jar",
    "ae2": ROOT / "mods" / "appliedenergistics2-forge-15.4.10.jar",
    "botania": ROOT / "mods" / "Botania-1.20.1-454-FORGE.jar",
    "draconicevolution": ROOT / "mods" / "Draconic-Evolution-1.20.1-3.1.2.621-universal.jar",
    "avaritia": ROOT / "mods" / "Re-Avaritia-forge-1.20.1-1.4.1-release.jar",
    "extendedcrafting": ROOT / "mods" / "ExtendedCrafting-1.20.1-6.0.10.jar",
}

TOP_KEYS = ("up", "top", "glass", "texture", "all", "outside", "Main", "1", "0")
SIDE_KEYS = ("north", "front", "south", "side", "west", "east", "all", "outside", "Main", "2", "0", "1")
RIGHT_KEYS = ("east", "side", "west", "south", "north", "all", "outside", "Main", "2", "0", "1")


def _load(name, filename):
    spec = importlib.util.spec_from_file_location(name, ROOT / "tools" / filename)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


case_textures = _load("case_textures", "build_all_case_textures.py")
render_block_icon = _load("render_block_icon", "render_block_icon.py")
rebalance_cases = _load("rebalance_cases", "rebalance_cases.py")

UI_ITEMS = ROOT / "mods" / "aquatech-ui" / "src" / "main" / "resources" / "assets" / "aquatech_ui" / "textures" / "item"
# Всегда берутся из текстур мода: в составе кейсов не должно оставаться старых иконок.
REPO_FLAT = {f"aquatech_ui:rate_x{n}": UI_ITEMS / f"rate_x{n}.png" for n in (2, 4, 8, 16, 32, 64)}


class Jars:
    def __init__(self):
        self.zips = {ns: zipfile.ZipFile(path) for ns, path in JARS.items() if path.exists()}

    def read(self, ns, inner):
        zf = self.zips.get(ns)
        if zf is None or inner not in zf.namelist():
            return None
        return zf.read(inner)

    def model(self, ns, path):
        raw = self.read(ns, f"assets/{ns}/models/{path}.json")
        return json.loads(raw) if raw else None

    def textures(self, ns, path, depth=0):
        """Все текстуры модели вместе с родительскими; значения вида #имя разворачиваются."""
        if ":" in path:
            ns, path = path.split(":", 1)
        model = self.model(ns, path)
        found = {}
        if not model or depth > 8:
            return found
        if "parent" in model:
            found.update(self.textures(ns, model["parent"], depth + 1))
        found.update(model.get("textures") or {})
        return found

    def image(self, ref):
        ns, _, path = ref.partition(":")
        if not path:
            ns, path = "minecraft", ref
        raw = self.read(ns, f"assets/{ns}/textures/{path}.png")
        if raw is None:
            return None
        im = Image.open(io.BytesIO(raw)).convert("RGBA")
        if im.height > im.width:
            im = im.crop((0, 0, im.width, im.width))
        return im


def resolve(textures, key):
    value = textures.get(key)
    guard = 0
    while isinstance(value, str) and value.startswith("#") and guard < 6:
        value = textures.get(value[1:])
        guard += 1
    return None if not isinstance(value, str) or value.startswith("#") else value


def pick(jars, textures, keys, exclude=()):
    for key in keys:
        ref = resolve(textures, key)
        if ref and key not in exclude:
            im = jars.image(ref)
            if im is not None:
                return im
    return None


def build_icon(jars, item_id, models=None):
    if models is not None:
        rendered = models.render(item_id)
        if rendered is not None:
            return rendered
    ns, _, path = item_id.partition(":")
    textures = jars.textures(ns, f"item/{path}")
    if not textures:
        return None
    top = pick(jars, textures, TOP_KEYS)
    left = pick(jars, textures, SIDE_KEYS)
    right = pick(jars, textures, RIGHT_KEYS)
    if top is None and left is None:
        particle = resolve(textures, "particle")
        left = jars.image(particle) if particle else None
    top = top or left
    left = left or top
    right = right or left
    if left is None:
        return None
    return case_textures.iso_cube(top, left, right)


def main():
    cases = json.loads(CASES.read_text(encoding="utf-8"))["cases"]
    wanted = sorted({e["item"] for c in cases for e in c["loot"] + [c["pity"]] if e["type"] == "item"})
    added_by_script = {row[0] for rows in rebalance_cases.ADDITIONS.values() for row in rows}
    icons = json.loads(OUT_FILE.read_text(encoding="utf-8"))
    jars = Jars()
    models = render_block_icon.ModelIcons(jars)
    done, failed = [], []
    for item_id in wanted:
        if item_id in REPO_FLAT:
            continue
        if item_id in icons and item_id not in added_by_script:
            continue
        icon = build_icon(jars, item_id, models)
        if icon is None:
            failed.append(item_id)
            continue
        icons[item_id] = case_textures.img_to_b64(icon)
        done.append(item_id)
    for item_id, path in REPO_FLAT.items():
        icons[item_id] = case_textures.img_to_b64(Image.open(path).convert("RGBA"))
        done.append(item_id)
    OUT_FILE.write_text(json.dumps(icons, ensure_ascii=False), encoding="utf-8")
    print(f"нарисовано иконок: {len(done)}")
    if failed:
        print("не нашлось текстур:", ", ".join(failed))


if __name__ == "__main__":
    main()
