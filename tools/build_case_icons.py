#!/usr/bin/env python3
"""Case item icons for the hub UI.

Source priority:
  1. User's Downloads / ru.minecraft.wiki Grid_<Русское имя>.png (authentic wiki renders)
  2. Mod jar textures (items as-is; blocks as a 2.5D cube of the real block texture)
Patches the `const ITEM_TEXTURES = {...};` block inside hub.html.
"""
from __future__ import annotations

import base64
import io
import json
import zipfile
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
VANILLA = Path.home() / ".gradle/caches/forge_gradle/minecraft_repo/versions/1.20.1/client-extra.jar"
MOD_JARS = {
    "avaritia": "server/mods/Re-Avaritia-forge-1.20.1-1.4.1-release.jar",
    "draconicevolution": "server/mods/Draconic-Evolution-1.20.1-3.1.2.621-universal.jar",
    "extrabotany": "server/mods/extrabotany-forge-1.20.1-1.9.2.jar",
    "mythicbotany": "server/mods/MythicBotany-1.20.1-4.0.4.jar",
    "extendedcrafting": "server/mods/ExtendedCrafting-1.20.1-6.0.10.jar",
    "ae2": "server/mods/appliedenergistics2-forge-15.4.10.jar",
    "industrialupgrade": "server/mods/IndustrialUpgrade-1.20.1-3.4.0.11.jar",
}
CASES = ROOT / "server/config/aqualumen/cases.json"
HUB = ROOT / "mods/aqualumen-ui/src/main/resources/assets/aqualumen/html/hub.html"
DOWNLOADS = Path.home() / "Downloads"
WIKI_CACHE = ROOT / "tools" / "wiki_cache"

BLOCK_HINTS = ("_block", "_table", "_compressor", "_panel", "_machine", "extreme_crafting")

WIKI_RU = {
    "avaritia:infinity_ingot": ["Слиток бесконечности (Avaritia)"],
    "avaritia:infinity_catalyst": ["Катализатор бесконечности (Avaritia)"],
    "avaritia:neutron_ingot": ["Нейтронный слиток (Avaritia)"],
    "avaritia:neutron_nugget": ["Нейтронный кусочек (Avaritia)", "Нейтронный самородок (Avaritia)"],
    "avaritia:neutron_compressor": ["Нейтронный компрессор (Avaritia)"],
    "avaritia:extreme_crafting_table": ["Экстремальный верстак (Avaritia)", "Экстремальный верстак"],
    "avaritia:crystal_matrix_ingot": ["Слиток кристаллической матрицы (Avaritia)"],
    "draconicevolution:draconium_ingot": ["Дракониевый слиток"],
    "draconicevolution:awakened_draconium_ingot": ["Пробуждённый дракониевый слиток"],
    "draconicevolution:awakened_draconium_block": ["Пробуждённый дракониевый блок"],
    "draconicevolution:draconium_core": ["Дракониевое ядро"],
    "draconicevolution:wyvern_core": ["Ядро виверны"],
    "draconicevolution:awakened_core": ["Пробуждённое ядро"],
    "draconicevolution:wyvern_energy_core": ["Энергетическое ядро виверны"],
    "draconicevolution:dragon_heart": ["Сердце дракона"],
    "draconicevolution:basic_crafting_injector": ["Основной инжектор слияния"],
    "botania:manasteel_ingot": ["Мана-сталь (Botania)", "Мана-сталь"],
    "botania:mana_pearl": ["Жемчуг маны (Botania)", "Мана-жемчуг"],
    "botania:mana_diamond": ["Мана-алмаз (Botania)", "Мана-алмаз"],
    "botania:terrasteel_ingot": ["Террасталь (Botania)", "Террасталь"],
    "botania:elementium_ingot": ["Элементий (Botania)", "Элементий"],
    "botania:life_essence": ["Эссенция жизни (Botania)"],
    "botania:rune_fire": ["Руна огня (Botania)"],
    "botania:rune_water": ["Руна воды (Botania)"],
    "botania:rune_mana": ["Руна маны (Botania)"],
    "extendedcrafting:black_iron_ingot": ["Слиток чёрного железа (Extended Crafting)", "Чёрное железо (Extended Crafting)"],
    "extendedcrafting:luminessence": ["Люминесценция (Extended Crafting)"],
    "extrabotany:orichalcos_ingot": ["Слиток орихалка (ExtraBotany)"],
    "extrabotany:aerialite_ingot": ["Слиток аэриалита (ExtraBotany)"],
    "extrabotany:spirit_fuel": ["Топливо духов (ExtraBotany)"],
    "mythicbotany:alfsteel_ingot": ["Альфсталь (MythicBotany)", "Слиток альфстали"],
    "ae2:logic_processor": ["Логический процессор (Applied Energistics 2)"],
    "ae2:calculation_processor": ["Вычислительный процессор (Applied Energistics 2)"],
    "ae2:engineering_processor": ["Инженерный процессор (Applied Energistics 2)"],
    "ae2:fluix_crystal": ["Флюиксовый кристалл (Applied Energistics 2)"],
    "ae2:item_storage_cell_16k": ["16k ячейка хранения предметов (Applied Energistics 2)", "16k-ячейка хранения предметов (Applied Energistics 2)"],
    "ae2:item_storage_cell_64k": ["64k ячейка хранения предметов (Applied Energistics 2)", "64k-ячейка хранения предметов (Applied Energistics 2)"],
    "ae2:logic_processor_press": ["Пресс логических процессоров (Applied Energistics 2)", "Пресс для логических процессоров (Applied Energistics 2)"],
    "ae2:singularity": ["Сингулярность (Applied Energistics 2)"],
    "alexscaves:dark_tatters": ["Тёмные лоскуты (Alexs Caves)"],
    "alexscaves:moth_dust": ["Пыльца мотылька (Alexs Caves)", "Мотылёвая пыльца (Alexs Caves)"],
    "alexscaves:pearl": ["Жемчужина (Alexs Caves)"],
    "alexscaves:uranium_rod": ["Урановый стержень (Alexs Caves)"],
    "industrialupgrade:machines/barion_solar_panel": ["Барионная солнечная панель (Industrial Upgrade)", "Барионная солнечная панель"],
    "industrialupgrade:machines/hadron_solar_panel": ["Адронная солнечная панель (Industrial Upgrade)", "Адронная солнечная панель"],
    "industrialupgrade:machines/graviton_solar_panel": ["Гравитационная солнечная панель (Industrial Upgrade)", "Гравитационная солнечная панель"],
    "industrialupgrade:machines/neutronium_solar_panel": ["Нейтрониевая солнечная панель (Industrial Upgrade)", "Нейтрониевая солнечная панель"],
    "industrialupgrade:machines/photonic_solar_panel": ["Фотонная солнечная панель (Industrial Upgrade)", "Фотонная солнечная панель"],
    "industrialupgrade:machines/spectral_solar_panel": ["Спектральная солнечная панель (Industrial Upgrade)", "Спектральная солнечная панель"],
    "industrialupgrade:baseore/titanium": ["Титановая руда"],
    "industrialupgrade:baseore/tungsten": ["Вольфрамовая руда"],
    "industrialupgrade:baseore/chromium": ["Хромовая руда"],
    "industrialupgrade:baseore/cobalt": ["Кобальтовая руда"],
    "industrialupgrade:baseore/nickel": ["Никелевая руда"],
    "industrialupgrade:baseore/platinum": ["Платиновая руда"],
    "industrialupgrade:baseore/silver": ["Серебряная руда"],
    "industrialupgrade:baseore2/strontium": ["Стронциевая руда"],
    "industrialupgrade:baseore2/thallium": ["Таллиевая руда"],
    "industrialupgrade:baseore2/yttrium": ["Иттриевая руда"],
    "industrialupgrade:baseore1/beryllium": ["Бериллиевая руда"],
    "industrialupgrade:classicore/lead": ["Свинцовая руда"],
    "industrialupgrade:classicore/tin": ["Оловянная руда"],
    "industrialupgrade:alloyingot/inconel": ["Инконелевый слиток", "Инконель"],
    "industrialupgrade:alloyingot/osmiridium": ["Осмиридиевый слиток", "Осмиридий"],
    "industrialupgrade:itemingots/adamantium": ["Адамантиевый слиток"],
    "industrialupgrade:itemingots/bronze_ingot": ["Бронзовый слиток"],
    "industrialupgrade:itemingots/steel_ingot": ["Стальной слиток"],
    "industrialupgrade:battery/energy_crystal": ["Энергетический кристалл"],
    "industrialupgrade:battery/lapotron_crystal": ["Лапотроновый кристалл"],
    "industrialupgrade:battery/re_battery": ["Перезаряжаемая батарея"],
    "industrialupgrade:blockresource/advanced_machine": ["Улучшенная машина"],
    "industrialupgrade:blockresource/machine": ["Машина"],
    "industrialupgrade:cable/copper_cable": ["Медный кабель"],
    "industrialupgrade:nuclearresource/uranium_235": ["Уран-235"],
    "industrialupgrade:upgrades/overclocker": ["Ускоритель"],
    "minecraft:coal": ["Уголь"],
    "minecraft:copper_ingot": ["Медный слиток"],
    "minecraft:diamond": ["Алмаз"],
    "minecraft:gold_ingot": ["Золотой слиток"],
    "minecraft:iron_ingot": ["Железный слиток"],
    "minecraft:lava_bucket": ["Ведро лавы"],
    "minecraft:leather": ["Кожа"],
    "minecraft:blaze_rod": ["Огненный стержень"],
    "minecraft:nether_star": ["Звезда Нижнего мира"],
    "minecraft:netherite_ingot": ["Незеритовый слиток"],
    "minecraft:redstone": ["Красная пыль"],
    "minecraft:dragon_egg": ["Яйцо дракона"],
}


def open_jar(path: str) -> zipfile.ZipFile | None:
    p = ROOT / path
    return zipfile.ZipFile(p) if p.is_file() else None


JARS = {ns: open_jar(p) for ns, p in MOD_JARS.items()}
VAN = zipfile.ZipFile(VANILLA) if VANILLA.is_file() else None


TEXTURE_OVERRIDES = {
    # item_id -> (side_path, top_path) inside the mod jar
    "avaritia:extreme_crafting_table": (
        "assets/avaritia/textures/block/machine/craft/extreme_side.png",
        "assets/avaritia/textures/block/machine/craft/extreme_top.png",
    ),
}


def find_texture(item_id: str, prefer_block: bool) -> tuple[bytes, bool] | None:
    if item_id in TEXTURE_OVERRIDES:
        ns = item_id.split(":", 1)[0]
        zf = JARS.get(ns)
        side, top = TEXTURE_OVERRIDES[item_id]
        if zf and side in set(zf.namelist()):
            return zf.read(side), ("TOP", zf.read(top) if top in set(zf.namelist()) else None)
    ns, path = item_id.split(":", 1)
    name = path.rsplit("/", 1)[-1]
    looks_block = prefer_block or any(h in name for h in BLOCK_HINTS)
    order = ["block/blocks", "block/block", "block", "item/items", "item/item", "item"] if looks_block else \
            ["item/items", "item/item", "item", "block/blocks", "block/block", "block"]

    def search(zf: zipfile.ZipFile, mod_ns: str) -> tuple[bytes, bool] | None:
        if zf is None:
            return None
        names = set(zf.namelist())
        for kind in order:
            base = f"assets/{mod_ns}/textures/{kind}"
            for cand in (f"{base}/{path}.png", f"{base}/{name}.png"):
                if cand in names:
                    data = zf.read(cand)
                    if len(data) > 80:
                        return data, kind.startswith("block")
        hits = sorted(n for n in names if n.startswith(f"assets/{mod_ns}/textures/") and n.endswith(f"/{name}.png"))
        if hits:
            data = zf.read(hits[0])
            return data, "/block" in hits[0]
        # multi-texture machines: textures/block/<...>/<name>/side.png
        sides = sorted(n for n in names if n.startswith(f"assets/{mod_ns}/textures/") and n.endswith(f"/{name}/side.png"))
        if not sides and "panel" in name:
            stem = name.replace("_solar_panel", "")
            sides = sorted(n for n in names if n.startswith(f"assets/{mod_ns}/textures/") and n.endswith(f"/{stem}/side.png"))
        if sides:
            return zf.read(sides[0]), True
        return None

    if ns == "minecraft":
        for kind in (["block", "item"] if looks_block else ["item", "block"]):
            cand = f"assets/minecraft/textures/{kind}/{path}.png"
            if VAN and cand in set(VAN.namelist()):
                return VAN.read(cand), kind == "block"
        return None
    got = search(JARS.get(ns), ns)
    if got:
        return got
    for other_ns, zf in JARS.items():
        if other_ns == ns:
            continue
        got = search(zf, other_ns)
        if got:
            return got
    return None


def wiki_file(ru_name: str, en_name: str = "") -> bytes | None:
    """User's Downloads first, then ru.minecraft.wiki Grid_<имя>.png (cached)."""
    fn = f"Grid_{ru_name.replace(chr(32), chr(95))}.png"  # wiki files use underscores
    local = DOWNLOADS / fn
    if local.is_file():
        return local.read_bytes()
    WIKI_CACHE.mkdir(parents=True, exist_ok=True)
    cached = WIKI_CACHE / fn
    if cached.is_file():
        return cached.read_bytes()
    from urllib.parse import quote
    import urllib.request
    url = "https://ru.minecraft.wiki/images/" + quote(fn)
    try:
        req = urllib.request.Request(url, headers={"User-Agent": "AquaTech/1.0"})
        with urllib.request.urlopen(req, timeout=15) as r:
            data = r.read()
        if len(data) > 100 and data[:4] == bytes([0x89, 0x50, 0x4E, 0x47]):
            cached.write_bytes(data)
            return data
    except Exception:
        pass
    # file-search fallback: "<имя> (Industrial Upgrade)" etc.
    try:
        surl = "https://ru.minecraft.wiki/api.php?" + urllib.parse.urlencode({
            "action": "query", "format": "json", "list": "search",
            "srnamespace": 6, "srlimit": 5, "srsearch": ru_name})
        sreq = urllib.request.Request(surl, headers={"User-Agent": "AquaTech/1.0"})
        with urllib.request.urlopen(sreq, timeout=20) as r:
            sdata = json.load(r)
        titles = [x["title"] for x in sdata.get("query", {}).get("search", [])]
        key = ru_name.split("(")[0].strip()
        good = [t for t in titles if key.lower().replace("_", " ") in t.lower()]
        if good:
            info_url = "https://ru.minecraft.wiki/api.php?" + urllib.parse.urlencode({
                "action": "query", "format": "json", "titles": good[0],
                "prop": "imageinfo", "iiprop": "url"})
            ireq = urllib.request.Request(info_url, headers={"User-Agent": "AquaTech/1.0"})
            with urllib.request.urlopen(ireq, timeout=20) as r:
                idata = json.load(r)
            for page in idata.get("query", {}).get("pages", {}).values():
                for ii in page.get("imageinfo", []):
                    img_url = ii.get("url")
                    if not img_url:
                        continue
                    ireq2 = urllib.request.Request(img_url, headers={"User-Agent": "AquaTech/1.0"})
                    with urllib.request.urlopen(ireq2, timeout=20) as r:
                        idata2 = r.read()
                    if len(idata2) > 100:
                        ext = ".gif" if idata2[:3] == b"GIF" else ".png"
                        (WIKI_CACHE / (good[0].replace(" ", "_").replace("Файл:", "") + ext)).write_bytes(idata2)
                        return idata2
    except Exception:
        pass
    if en_name:
        en_fn = en_name + ".png"
        cached = WIKI_CACHE / en_fn
        if cached.is_file():
            return cached.read_bytes()
        url = "https://minecraft.wiki/wiki/Special:FilePath/" + quote(en_fn)
        try:
            req = urllib.request.Request(url, headers={"User-Agent": "AquaTech/1.0"})
            with urllib.request.urlopen(req, timeout=15) as r:
                data = r.read()
            if len(data) > 100 and data[:4] == bytes([0x89, 0x50, 0x4E, 0x47]):
                cached.write_bytes(data)
                return data
        except Exception:
            pass
    return None


def iso_cube(tex: Image.Image, size: int = 64, top_tex: Image.Image | None = None) -> Image.Image:
    """2:1 isometric cube: top diamond + left/right sides (verified affine)."""
    tex = tex.convert("RGBA")
    if tex.size != (16, 16):
        tex = tex.resize((16, 16), Image.NEAREST)
    top = (top_tex or tex).convert("RGBA")
    if top.size != (16, 16):
        top = top.resize((16, 16), Image.NEAREST)
    out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    # top diamond: corners (0,16) (32,0) (64,16) (32,32)
    top_f = top.transform((64, 32), Image.AFFINE, (0.25, -0.5, 8, 0.25, 0.5, -8), resample=Image.NEAREST)
    # left face: corners (0,16) (32,32) (32,64) (0,48), local origin (0,16)
    left = tex.transform((32, 48), Image.AFFINE, (0.5, 0, 0, -0.25, 0.5, 0), resample=Image.NEAREST)
    # right face: corners (32,32) (64,16) (64,48) (32,64), local origin (32,16)
    right = tex.transform((32, 48), Image.AFFINE, (0.5, 0, 0, 0.25, 0.5, -8), resample=Image.NEAREST)

    def shade(img, k):
        px = img.load()
        for y in range(img.height):
            for x in range(img.width):
                r, g, b, a = px[x, y]
                if a:
                    px[x, y] = (int(r * k), int(g * k), int(b * k), a)
        return img

    out.alpha_composite(top_f, (0, 0))
    out.alpha_composite(shade(left, 0.78), (0, 16))
    out.alpha_composite(shade(right, 0.55), (32, 16))
    return out


def to_b64(img: Image.Image) -> str:
    buf = io.BytesIO()
    img.save(buf, "PNG")
    return "data:image/png;base64," + base64.b64encode(buf.getvalue()).decode()


def raw_b64(data: bytes) -> str:
    mime = "image/gif" if data[:3] == b"GIF" else "image/png"
    return "data:" + mime + ";base64," + base64.b64encode(data).decode()


# Exact wiki file names resolved from the mod pages (Avaritia / Industrial Upgrade)
WIKI_EXACT = {
    # Экстремальный верстак: рендера не существует ни на одной вики —
    # ближайший аутентичный рендер из той же линейки столов
    "avaritia:extreme_crafting_table": ["Тяжёлый верстак (Avaritia)"],
    "industrialupgrade:classicore/tin": ["Оловянная руда (Industrial Upgrade)"],
    "industrialupgrade:classicore/lead": ["Свинцовая руда (Industrial Upgrade)"],
}


def render_item(item_id: str) -> str:
    """Priority: wiki/Downloads renders -> authentic jar texture (blocks as 2.5D cube)."""
    ru_names = WIKI_EXACT.get(item_id) or WIKI_RU.get(item_id)
    if ru_names:
        en = "".join(w.capitalize() for w in item_id.split(":", 1)[1].split("_"))
        for ru in ru_names:
            data = wiki_file(ru, en)
            if data:
                try:
                    return to_b64(Image.open(io.BytesIO(data)).convert("RGBA"))
                except Exception:
                    continue
    prefer_block = item_id.split(":")[-1].endswith(("_block", "_table", "_compressor", "_panel"))
    got = find_texture(item_id, prefer_block)
    if got is None:
        return ""
    data, is_block = got
    top_data = None
    if isinstance(is_block, tuple):
        top_data = is_block[1]
        is_block = True
    try:
        tex = Image.open(io.BytesIO(data)).convert("RGBA")
        top_tex = Image.open(io.BytesIO(top_data)).convert("RGBA") if top_data else None
    except Exception:
        return ""
    name = item_id.split(":")[-1]
    if is_block or any(h in name for h in BLOCK_HINTS):
        return to_b64(iso_cube(tex, top_tex=top_tex))
    return to_b64(tex.resize((48, 48), Image.NEAREST))


def main() -> int:
    cases = json.loads(CASES.read_text(encoding="utf-8"))
    ids = sorted({l["item"] for c in cases["cases"] for l in c["loot"] if l.get("item")})
    hub = HUB.read_text(encoding="utf-8")
    previous = {}
    try:
        at = hub.index("const ITEM_TEXTURES = ")
        line_end = hub.index("\n", at)
        previous = json.loads(hub[at + len("const ITEM_TEXTURES = "):line_end].rstrip().rstrip(";"))
    except Exception:
        pass
    out = {}
    missing = []
    wiki_hits = 0
    for item_id in ids:
        b64 = render_item(item_id)
        if b64:
            out[item_id] = b64
            if item_id in WIKI_RU:
                wiki_hits += 1
        elif item_id in previous:
            out[item_id] = previous[item_id]
        else:
            missing.append(item_id)
    block = "const ITEM_TEXTURES = " + json.dumps(out, separators=(",", ":")) + ";"
    start = hub.index("const ITEM_TEXTURES = ")
    end = hub.index(";", hub.index("};", start)) + 1
    hub = hub[:start] + block + hub[end:]
    HUB.write_text(hub, encoding="utf-8")
    print(f"rendered {len(out)}/{len(ids)} icons ({wiki_hits} from wiki/Downloads) -> hub.html")
    if missing:
        print("no source for:")
        for m in missing:
            print("  -", m)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
