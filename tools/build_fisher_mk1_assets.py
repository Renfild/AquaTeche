"""Рисует ресурсы Рыболова MK-1 и Рыбного генератора на основе текстур Рыболова MK-2.

    python tools/build_fisher_mk1_assets.py

Берёт уже нарисованные грани fisher_* (64x64 = 16x16 крупных пикселей), перекрашивает кромку и рисует иконку
на лицевой грани. Пишет текстуры, модели, blockstate, лут и теги инструмента (для всех механизмов) в mods/aquatech-machines/src/main/resources.
"""
import json
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
RES = ROOT / "mods" / "aquatech-machines" / "src" / "main" / "resources"
ASSETS = RES / "assets" / "aquatech_machines"
DATA = RES / "data"

BEVEL = (56, 60, 72, 255)       # светлая кромка MK-2
WINDOW_DARK = (14, 16, 20, 255)  # тёмное окно на лицевой грани
BLACK = (14, 15, 18, 255)

BRONZE = {"bevel": (176, 112, 52, 255), "icon": (194, 124, 58, 255), "icon_on": (255, 255, 255, 255),
          "glow": (0, 160, 210, 255), "glow_core": (180, 245, 255, 255)}
EMBER = {"bevel": (150, 66, 20, 255), "icon": (110, 48, 16, 255), "icon_on": (255, 210, 60, 255),
         "glow": (230, 100, 20, 255), "glow_core": (255, 210, 60, 255)}

# Иконки 6x6 внутри окна 6x6 (клетки 5..10 по обеим осям)
HOOK = [
    "...X..",
    "...X..",
    "...X..",
    "X..X..",
    "XXXX..",
    "......",
]
FLAME_BARS = [
    "X.X.X.",
    "X.X.X.",
    "X.X.X.",
    "X.X.X.",
    "X.X.X.",
    "X.X.X.",
]


def load16(name):
    im = Image.open(ASSETS / "textures" / "block" / f"{name}.png").convert("RGBA")
    return im.resize((16, 16), Image.NEAREST)


def save16(im, name):
    im.resize((64, 64), Image.NEAREST).save(ASSETS / "textures" / "block" / f"{name}.png")


def recolor(im, old, new):
    out = im.copy()
    px = out.load()
    for y in range(16):
        for x in range(16):
            if px[x, y] == old:
                px[x, y] = new
    return out


def draw_icon(im, art, color, ox=5, oy=5):
    px = im.load()
    for dy, row in enumerate(art):
        for dx, ch in enumerate(row):
            if ch == "X":
                px[ox + dx, oy + dy] = color


def rivets(im, color):
    px = im.load()
    for x, y in ((2, 2), (13, 2), (2, 13), (13, 13)):
        px[x, y] = color


def build_texture_set(prefix, palette, front_off_icon, front_on_icon, on_bars):
    for face in ("side", "top", "bottom"):
        base = recolor(load16(f"fisher_{face}"), BEVEL, palette["bevel"])
        save16(base, f"{prefix}_{face}")
    off = recolor(load16("fisher_front"), BEVEL, palette["bevel"])
    rivets(off, palette["bevel"])
    draw_icon(off, front_off_icon, palette["icon"])
    save16(off, f"{prefix}_front")

    on = recolor(load16("fisher_front_on"), BEVEL, palette["bevel"])
    # окно MK-2 в состоянии «включено»: голубое поле с белой сердцевиной; заменяем цвета на свои
    on = recolor(on, (0, 160, 210, 255), palette["glow"])
    on = recolor(on, (180, 245, 255, 255), palette["glow_core"])
    on = recolor(on, (255, 255, 255, 255), palette["glow_core"])
    on = recolor(on, (0, 229, 255, 255), palette["glow_core"])
    rivets(on, palette["bevel"])
    draw_icon(on, front_on_icon, WINDOW_DARK if on_bars else palette["icon_on"])
    save16(on, f"{prefix}_front_on")


def build_models(name):
    for suffix in ("", "_on"):
        src = (ASSETS / "models" / "block" / f"fisher{suffix}.json").read_text(encoding="utf-8")
        out = src.replace("aquatech_machines:block/fisher", f"aquatech_machines:block/{name}")
        (ASSETS / "models" / "block" / f"{name}{suffix}.json").write_text(out, encoding="utf-8")
    state = (ASSETS / "blockstates" / "fisher.json").read_text(encoding="utf-8")
    (ASSETS / "blockstates" / f"{name}.json").write_text(
        state.replace("aquatech_machines:block/fisher", f"aquatech_machines:block/{name}"), encoding="utf-8")
    (ASSETS / "models" / "item" / f"{name}.json").write_text(
        json.dumps({"parent": f"aquatech_machines:block/{name}"}, indent=1), encoding="utf-8")


def build_data(names):
    loot_dir = DATA / "aquatech_machines" / "loot_tables" / "blocks"
    loot_dir.mkdir(parents=True, exist_ok=True)
    for name in names:
        table = {
            "type": "minecraft:block",
            "pools": [{
                "rolls": 1.0,
                "bonus_rolls": 0.0,
                "entries": [{"type": "minecraft:item", "name": f"aquatech_machines:{name}"}],
                "conditions": [{"condition": "minecraft:survives_explosion"}],
            }],
        }
        (loot_dir / f"{name}.json").write_text(json.dumps(table, indent=2), encoding="utf-8")
    for tag in ("mineable/pickaxe", "needs_stone_tool"):
        path = DATA / "minecraft" / "tags" / "blocks" / f"{tag}.json"
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps({"replace": False, "values": [f"aquatech_machines:{n}" for n in names]}, indent=2),
                        encoding="utf-8")


def build_generator_gui():
    gui = Image.open(ASSETS / "textures" / "gui" / "fisher.png").convert("RGBA")
    panel = gui.getpixel((100, 70))
    px = gui.load()
    # слоты «ядро» и «выход» не нужны: закрашиваем рамки панелью. Слот рыбы остаётся на месте слота удочки.
    for (x0, y0, x1, y1) in ((43, 47, 64, 68), (113, 33, 134, 54)):
        for y in range(y0, y1):
            for x in range(x0, x1):
                px[x, y] = panel
    gui.save(ASSETS / "textures" / "gui" / "fish_generator.png")


def main():
    build_texture_set("fisher_mk1", BRONZE, HOOK, HOOK, on_bars=False)
    build_texture_set("fish_generator", EMBER, FLAME_BARS, FLAME_BARS, on_bars=True)
    build_models("fisher_mk1")
    build_models("fish_generator")
    # лут и теги инструмента нужны всем механизмам: без них блок при разрушении ничего не выпадает
    build_data(["fisher_mk1", "fish_generator", "fisher", "excavator", "extractor", "synthesizer", "centrifuge",
                "flower_collector", "mana_fabricator"])
    build_generator_gui()
    print("assets written")


if __name__ == "__main__":
    main()
