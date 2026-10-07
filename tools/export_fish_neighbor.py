"""Выгружает рыбака-соседа из Blockbench в ресурсы мода.

    python tools/export_fish_neighbor.py

Источник правды: mods/aquatech-ui/art/fish_neighbor.bbmodel (модель, текстура и анимации нарисованы в Blockbench).
Геометрия лежит рядом, art/fish_neighbor.geo.json (File → Export → Bedrock Geometry в Blockbench).
Скрипт кладёт в assets/aquatech_ui: geo/fish_neighbor.geo.json, textures/entity/fish_neighbor.png и
animations/fish_neighbor.animation.json (формат Bedrock 1.8.0, как у GeckoLib).
Знаки при выгрузке такие же, как у Blockbench для Bedrock: у поворота меняется знак x и y, у позиции x.
"""
import base64
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
ART = ROOT / "mods" / "aquatech-ui" / "art"
RES = ROOT / "mods" / "aquatech-ui" / "src" / "main" / "resources" / "assets" / "aquatech_ui"

GEO_IDENTIFIER = "geometry.fish_neighbor"


def num(value):
    out = round(float(value), 4)
    return 0.0 if out == 0 else out


def time_key(t):
    text = ("%.4f" % t).rstrip("0")
    return text + "0" if text.endswith(".") else text


def vector(channel, point):
    x, y, z = (num(point[k]) for k in "xyz")
    if channel == "rotation":
        return [num(-x), num(-y), z]
    if channel == "position":
        return [num(-x), y, z]
    return [x, y, z]


def convert_animation(anim):
    bones = {}
    for animator in anim["animators"].values():
        channels = {}
        for key in sorted(animator.get("keyframes", []), key=lambda k: k["time"]):
            value = vector(key["channel"], key["data_points"][0])
            if key.get("interpolation") == "catmullrom":
                value = {"post": value, "lerp_mode": "catmullrom"}
            channels.setdefault(key["channel"], {})[time_key(key["time"])] = value
        if channels:
            bones[animator["name"]] = channels
    out = {}
    if anim["loop"] == "loop":
        out["loop"] = True
    elif anim["loop"] == "hold":
        out["loop"] = "hold_on_last_frame"
    out["animation_length"] = num(anim["length"])
    out["bones"] = bones
    return out


def main():
    project = json.loads((ART / "fish_neighbor.bbmodel").read_text(encoding="utf-8"))

    png = project["textures"][0]["source"].split(",", 1)[1]
    texture = RES / "textures" / "entity" / "fish_neighbor.png"
    texture.write_bytes(base64.b64decode(png))

    animations = {a["name"]: convert_animation(a) for a in project["animations"]}
    anim_file = {"format_version": "1.8.0", "animations": animations}
    (RES / "animations" / "fish_neighbor.animation.json").write_text(
        json.dumps(anim_file, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")

    geo = json.loads((ART / "fish_neighbor.geo.json").read_text(encoding="utf-8"))
    geo["minecraft:geometry"][0]["description"]["identifier"] = GEO_IDENTIFIER
    (RES / "geo" / "fish_neighbor.geo.json").write_text(
        json.dumps(geo, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print("fish_neighbor: %d animations, texture %d bytes" % (len(animations), texture.stat().st_size))


if __name__ == "__main__":
    main()
