#!/usr/bin/env python3
"""Regenerate the loot tables in docs/rods.html from FishingLootHandler.java.

Single source of truth = the mod code. The page shell, tier titles and the
Treasure block stay hand-written; only the per-rod tables are rebuilt, so the
public drop odds can never drift from the actual loot pools again.

Usage:
  python tools/build_rods_page.py            # rewrite tables in docs/rods.html
  python tools/build_rods_page.py --check    # exit 1 if the page is stale
"""
from __future__ import annotations

import argparse
import io
import json
import re
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "mods/aquatech-ui/src/main/java/net/aquatech/ui/fishing/FishingLootHandler.java"
ROSTER = ROOT / "mods/aquatech-ui/src/main/java/net/aquatech/ui/fishing/FishRosterService.java"
PAGE = ROOT / "docs/rods.html"
MODS = ROOT / "mods"

BEGIN = "<!-- RODS:BEGIN -->"
END = "<!-- RODS:END -->"

# article id in docs/rods.html -> StarCatcher rod id
ARTICLE_TO_ROD = {
    "t1": "bamboo_rod",
    "t2": "humble_rod",
    "t3": "good_old_rod",
    "t4": "naturalist_rod",
    "t5": "slimed_rod",
    "t6": "iceborn_rod",
    "t7": "starcatcher_rod",
    "t8": "azure_crystal_rod",
    "t9": "sharktooth_rod",
    "t10": "obsidian_rod",
    "t11": "lush_glowberry_rod",
    "t12": "magmaforged_rod",
    "t13": "alpha_rod",
    "bone": "boner_rod",
}

VANILLA = {
    "arrow": "Стрела", "beetroot_seeds": "Семена свёклы", "bone": "Кость", "carrot": "Морковь",
    "coal_ore": "Угольная руда", "cobble_stone": "Булыжник", "cobblestone": "Булыжник",
    "cobweb": "Паутина", "copper_ingot": "Медный слиток", "copper_ore": "Медная руда",
    "crying_obsidian": "Плачущий обсидиан", "diamond": "Алмаз", "diamond_block": "Алмазный блок",
    "dirt": "Земля", "emerald": "Изумруд", "ender_pearl": "Жемчуг Края", "feather": "Перо",
    "glowstone_dust": "Светокаменная пыль", "gold_ore": "Золотая руда", "gravel": "Гравий",
    "gunpowder": "Порох", "heart_of_the_sea": "Сердце моря", "iron_ingot": "Железный слиток",
    "iron_ore": "Железная руда", "lapis_lazuli": "Лазурит", "lapis_ore": "Лазуритовая руда",
    "lead": "Свинцовая пластина", "leather": "Кожа", "melon_seeds": "Семена арбуза",
    "nether_star": "Незеритовый звездочёт", "netherite_ingot": "Незеритовый слиток",
    "netherite_scrap": "Незеритовый лом", "obsidian": "Обсидиан", "phantom_membrane": "Мембрана фантома",
    "potato": "Картофель", "prismarine_crystals": "Кристаллы призмарина",
    "prismarine_shard": "Осколок призмарина", "pumpkin_seeds": "Семена тыквы",
    "quartz": "Кварц", "raw_gold": "Сырое золото", "raw_iron": "Сырое железо",
    "redstone": "Красная пыль", "redstone_ore": "Красная руда", "rotten_flesh": "Гнилая плоть",
    "saddle": "Седло", "sand": "Песок", "sea_lantern": "Морской фонарь", "slime_ball": "Слизкий шар",
    "slime_block": "Слизневый блок", "snow_block": "Снежный блок", "snowball": "Снежок",
    "spider_eye": "Глаз паука", "stick": "Палка", "sugar": "Сахар", "sugar_cane": "Сахарный тростник",
    "totem_of_undying": "Тотем", "white_wool": "Белая шерсть", "amethyst_shard": "Осколок аметиста",
    "amethyst_block": "Блок аметиста", "glass_bottle": "Стеклянная бутылка", "string": "Нить",
    "oak_sapling": "Саженец дуба", "birch_sapling": "Саженец берёзы", "bamboo": "Бамбук",
    "clay_ball": "Глина", "iron_ingot_slab": "Плита железа", "wheat_seeds": "Пшеничные семена",
    "sugar_cane": "Сахарный тростник",     "oak_log": "Дубовый брус", "kelp": "Ламинария",
    "nether_bricks": "Незеритовый кирпич", "redstone_block": "Блок красной пыли",
}


def vanilla_name(item_id: str) -> str:
    if not item_id.startswith("minecraft:"):
        return ""
    path = item_id.split(":", 1)[1]
    if path in VANILLA:
        return VANILLA[path]
    words = path.split("_")
    return " ".join(w.capitalize() for w in words)


def load_mod_lang(mod_id: str, jar_glob: str) -> dict[str, str]:
    jars = sorted(MODS.glob(jar_glob))
    if not jars:
        return {}
    try:
        with zipfile.ZipFile(jars[0]) as z:
            key = f"assets/{mod_id}/lang/ru_ru.json"
            if key not in z.namelist():
                return {}
            data = json.loads(z.read(key).decode("utf-8"))
    except Exception:
        return {}
    out: dict[str, str] = {}
    for k, v in data.items():
        if k.startswith("item."):
            out[k[5:]] = v
        elif k.startswith("block."):
            out[k[6:]] = v
        elif k.startswith("iu."):
            out[k[3:]] = v
    return out


# Industrial Upgrade ships no ru_ru name for these; page text stays readable.
OVERRIDES = {
    "industrialupgrade:sapling/rubber_sapling": "Резиновый саженец",
    "industrialupgrade:raw_latex": "Сырой латекс",
    "industrialupgrade:blockresource/untreated_peat": "Блок необработанного торфа",
}


def mod_name(item_id: str) -> str:
    if item_id in OVERRIDES:
        return OVERRIDES[item_id]
    ns, path = item_id.split(":", 1)
    if ns == "minecraft":
        return vanilla_name(item_id)
    for mod_id, glob in (("industrialupgrade", "IndustrialUpgrade-*.jar"), ("ae2", "appliedenergistics2*.jar")):
        if ns == mod_id:
            lang = LANG_CACHE.get(mod_id) or {}
            base = path.split("/")[-1]
            dotted = path.replace("/", ".")
            for key in (f"{path}.name", f"{ns}:{path}", path, f"{base}.name", base,
                        f"{dotted}.name", dotted, f"ingot.{base}", f"crushed.{base}",
                        f"smalldust.{base}", f"alloyingot.{base}", f"preciousgem.{base}",
                        f"blockpreciousore.{base}", f"mineral.{base}"):
                if key in lang:
                    return lang[key]
            return " ".join(w.capitalize() for w in base.split("_"))
    return path.split("/")[-1].replace("_", " ").capitalize()


LANG_CACHE: dict[str, dict[str, str]] = {}


def count_range(expr: str) -> str:
    expr = expr.strip()
    m = re.fullmatch(r"(\d+)\s*\+\s*random\.nextInt\((\d+)\)", expr)
    if m:
        lo = int(m.group(1))
        return f"{lo}–{lo + int(m.group(2)) - 1}"
    m = re.fullmatch(r"(\d+)\s*\+\s*random\.nextInt\((\d+)\s*-\s*1\)", expr)
    if m:
        lo = int(m.group(1))
        return f"{lo}–{lo + int(m.group(2)) - 1}"
    if re.fullmatch(r"\d+", expr):
        return expr
    return "1"


def parse_count(inner: str) -> str:
    """Inner text of new ItemStack(Items.X, ...) or getModItem(id, fallback, ...)."""
    parts = [p.strip() for p in split_args(inner)]
    if not parts:
        return "1"
    return count_range(parts[-1])


def split_args(text: str) -> list[str]:
    out, depth, cur = [], 0, ""
    for ch in text:
        if ch in "(":
            depth += 1
        elif ch in ")":
            depth -= 1
        if ch == "," and depth == 0:
            out.append(cur)
            cur = ""
        else:
            cur += ch
    if cur.strip():
        out.append(cur)
    return out


def parse_rods() -> dict[str, dict]:
    java = io.open(JAVA, encoding="utf-8").read()
    roster = io.open(ROSTER, encoding="utf-8").read()
    tiers = {m.group(1): int(m.group(2)) for m in re.finditer(r'ROD_TIER\.put\("([^"]+)",\s*(\d+)\)', roster)}

    start = java.index("private static List<ItemStack> rollStarCatcherRodLoot")
    body = java[start:]
    end = body.index("private static void maybeAdd")
    body = body[:end]

    rods: dict[str, dict] = {}
    for m in re.finditer(r'case "([a-z_]+)" -> \{(.*?)\n            \}', body, re.S):
        rod, chunk = m.group(1), m.group(2)
        pool: list[tuple[float, str, str]] = []
        for pm in re.finditer(
            r"maybeAdd\(pool, random, ([0-9.]+)f,\s*(?:new ItemStack\(Items\.([A-Z_0-9]+),\s*(.+?)\)|getModItem\(\"([^\"]+)\",\s*Items\.([A-Z_0-9]+),\s*(.+?)\))\);",
            chunk, re.S,
        ):
            chance = float(pm.group(1))
            if pm.group(2):
                item_id = "minecraft:" + pm.group(2).lower()
                count = count_range(pm.group(3))
            else:
                item_id = pm.group(4)
                count = count_range(pm.group(6))
            pool.append((chance, item_id, count))

        guarantee: list[tuple[float, str, str]] = []
        branches: list[tuple[float | None, str, str]] = []
        vanilla_re = re.compile(r"new ItemStack\(Items\.([A-Z_0-9]+),\s*(.+?)\)\s*;")
        mod_re = re.compile(r"getModItem\(\"([^\"]+)\",\s*Items\.([A-Z_0-9]+),\s*(.+?)\)\s*;")
        threshold_re = re.compile(r"(?:if|else if) \(rStart < ([0-9.]+)f\)")
        pending: float | None = None
        for line in chunk.splitlines():
            tm = threshold_re.search(line)
            if tm:
                pending = float(tm.group(1))
                continue
            if "guaranteed =" not in line:
                continue
            item_id = count = None
            vm = vanilla_re.search(line)
            mm = mod_re.search(line)
            if vm:
                item_id, count = "minecraft:" + vm.group(1).lower(), count_range(vm.group(2))
            elif mm:
                item_id, count = mm.group(1), count_range(mm.group(3))
            if item_id is None:
                continue
            branches.append((pending, item_id, count))
            pending = None
        prev = 0.0
        for threshold, item_id, count in branches:
            if threshold is None:
                chance = max(0.0, 1.0 - prev)
            else:
                chance = max(0.0, threshold - prev)
                prev = threshold
            guarantee.append((chance, item_id, count))

        km = re.search(r"pickFromPool\(list, pool, random, (\d+), (\d+)\)", chunk)
        stacks = (int(km.group(1)), int(km.group(2))) if km else (1, 3)
        rods[rod] = {"tier": tiers.get(rod, 0), "pool": pool, "guarantee": guarantee, "stacks": stacks}
    return rods


def esc(text: str) -> str:
    return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")


def table(rows: list[tuple[float, str, str]], subtitle: str) -> str:
    out = [f'        <h3 class="loot-sub">{esc(subtitle)}</h3>',
           '        <div class="loot-table-wrap">',
           '          <table class="loot-table">',
           '            <thead><tr><th>Шанс</th><th>Предмет</th><th>Кол-во</th></tr></thead>',
           '            <tbody>']
    for chance, item_id, count in rows:
        out.append(f"            <tr><td>{chance * 100:.0f}%</td><td>{esc(mod_name(item_id))}</td><td>{esc(count)}</td></tr>")
    out += ['            </tbody>', '          </table>', '        </div>']
    return "\n".join(out)


def build_article(article_id: str, rod: str, data: dict, header_html: str) -> str:
    lo, hi = data["stacks"]
    sub = f"Из прошедших шансов берут {lo}–{hi} стака."
    parts = [f'      <article class="loot-block" id="{article_id}">', header_html.rstrip(), ""]
    if data["guarantee"]:
        parts.append(table(data["guarantee"], "Гарантия (один из)"))
        parts.append("")
    parts.append(table(data["pool"], "Пул"))
    parts.append("      </article>")
    return "\n".join(parts)


def render_regions(page: str, rods: dict[str, dict]) -> str:
    out = page
    for article_id, rod in ARTICLE_TO_ROD.items():
        data = rods.get(rod)
        if not data:
            sys.exit(f"rod {rod} not found in FishingLootHandler")
        pattern = re.compile(
            r'(      <article class="loot-block" id="' + re.escape(article_id) + r'">\n)(.*?)(      </article>)',
            re.S,
        )
        m = pattern.search(out)
        if not m:
            sys.exit(f"article {article_id} not found in docs/rods.html")
        block = m.group(2)
        hm = re.search(r"(        <header class=\"loot-head\">.*?        </header>)", block, re.S)
        if not hm:
            sys.exit(f"header missing in article {article_id}")
        header = hm.group(1)
        header = re.sub(r"<p>.*?</p>", f"<p>{sub_line(data)}</p>", header, count=1, flags=re.S)
        new_article = build_article(article_id, rod, data, header)
        out = out[:m.start()] + new_article + out[m.end():]
    return out


def sub_line(data: dict) -> str:
    lo, hi = data["stacks"]
    return f"Из прошедших шансов берут {lo}–{hi} стака."


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--check", action="store_true", help="fail if the page is stale")
    args = ap.parse_args()

    LANG_CACHE["industrialupgrade"] = load_mod_lang("industrialupgrade", "IndustrialUpgrade-*.jar")
    LANG_CACHE["ae2"] = load_mod_lang("ae2", "appliedenergistics2*.jar")

    raw = io.open(PAGE, encoding="utf-8", newline="").read()
    crlf = "\r\n" in raw
    page = raw.replace("\r\n", "\n")
    rods = parse_rods()
    updated = render_regions(page, rods)

    if args.check:
        if updated != page:
            print("STALE: docs/rods.html does not match FishingLootHandler loot pools")
            return 1
        print("OK docs/rods.html matches the loot pools")
        return 0

    if BEGIN not in updated:
        print(f"{BEGIN} / {END} markers not used; tables rewritten in place")
    out_text = updated.replace("\n", "\r\n") if crlf else updated
    io.open(PAGE, "w", encoding="utf-8", newline="").write(out_text)
    total = sum(len(r["pool"]) for r in rods.values())
    print(f"OK docs/rods.html rewritten from {len(rods)} rod pools ({total} loot entries)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
